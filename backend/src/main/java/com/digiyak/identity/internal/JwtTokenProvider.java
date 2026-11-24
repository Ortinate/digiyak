package com.digiyak.identity.internal;

import com.digiyak.identity.domain.User;
import com.digiyak.identity.domain.UserRole;
import com.digiyak.shared.ApiException;
import com.digiyak.shared.AuthenticatedUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Standard RFC 7519 HS256 JWT signer/verifier + SHA-256 refresh token generator.
 * Zero extra third-party JWT library overhead; uses constant-time HMAC signature checks.
 */
@Component
public class JwtTokenProvider {

    private static final String HMAC_ALG = "HmacSHA256";
    private static final String HEADER_JSON = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
    private static final Base64.Encoder URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder URL_DECODER = Base64.getUrlDecoder();
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final byte[] secretBytes;
    private final long accessExpirationSeconds;
    private final long refreshExpirationSeconds;
    private final ObjectMapper objectMapper;

    public JwtTokenProvider(
            @Value("${digiyak.jwt.secret}") String secret,
            @Value("${digiyak.jwt.access-expiration-seconds:900}") long accessExpirationSeconds,
            @Value("${digiyak.jwt.refresh-expiration-seconds:604800}") long refreshExpirationSeconds,
            ObjectMapper objectMapper
    ) {
        if (secret == null || secret.length() < 32) {
            throw new IllegalArgumentException("digiyak.jwt.secret must be at least 32 characters");
        }
        this.secretBytes = secret.getBytes(StandardCharsets.UTF_8);
        this.accessExpirationSeconds = accessExpirationSeconds;
        this.refreshExpirationSeconds = refreshExpirationSeconds;
        this.objectMapper = objectMapper;
    }

    public String createAccessToken(User user) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(accessExpirationSeconds);

        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", user.getId().toString());
        claims.put("email", user.getEmail());
        if (user.getCampusId() != null) {
            claims.put("campusId", user.getCampusId().toString());
        }
        claims.put("roles", user.getRoles().stream().map(UserRole::name).sorted().collect(Collectors.toList()));
        claims.put("iat", now.getEpochSecond());
        claims.put("exp", exp.getEpochSecond());

        String encodedHeader = URL_ENCODER.encodeToString(HEADER_JSON.getBytes(StandardCharsets.UTF_8));
        String payloadJson = objectMapper.writeValueAsString(claims);
        String encodedPayload = URL_ENCODER.encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8));
        String signingInput = encodedHeader + "." + encodedPayload;
        String signature = URL_ENCODER.encodeToString(sign(signingInput));

        return signingInput + "." + signature;
    }

    public AuthenticatedUser parseAndVerify(String token) {
        if (token == null || token.isBlank()) {
            throw ApiException.unauthorized("Missing bearer token");
        }
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw ApiException.unauthorized("Malformed JWT");
        }

        String signingInput = parts[0] + "." + parts[1];
        byte[] expectedSig = sign(signingInput);
        byte[] actualSig;
        try {
            actualSig = URL_DECODER.decode(parts[2]);
        } catch (IllegalArgumentException ex) {
            throw ApiException.unauthorized("Invalid JWT signature encoding");
        }

        if (!MessageDigest.isEqual(expectedSig, actualSig)) {
            throw ApiException.unauthorized("Invalid JWT signature");
        }

        try {
            String payloadJson = new String(URL_DECODER.decode(parts[1]), StandardCharsets.UTF_8);
            JsonNode claims = objectMapper.readTree(payloadJson);

            long exp = claims.path("exp").asLong(0L);
            if (exp <= Instant.now().getEpochSecond()) {
                throw ApiException.unauthorized("JWT has expired");
            }

            UUID userId = UUID.fromString(claims.path("sub").asString());
            String email = claims.path("email").asString();
            UUID campusId = claims.hasNonNull("campusId") && !claims.path("campusId").asString().isBlank()
                    ? UUID.fromString(claims.path("campusId").asString())
                    : null;

            Set<String> roles = new HashSet<>();
            JsonNode rolesNode = claims.path("roles");
            if (rolesNode.isArray()) {
                for (JsonNode r : rolesNode) {
                    roles.add(r.asString());
                }
            }

            return new AuthenticatedUser(userId, campusId, email, Set.copyOf(roles));
        } catch (ApiException ex) {
            throw ex;
        } catch (Exception ex) {
            throw ApiException.unauthorized("Invalid JWT payload");
        }
    }

    public String generateRawRefreshToken() {
        byte[] bytes = new byte[48];
        SECURE_RANDOM.nextBytes(bytes);
        return URL_ENCODER.encodeToString(bytes);
    }

    public String hashRefreshToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("SHA-256 unavailable", ex);
        }
    }

    public long getAccessExpirationSeconds() {
        return accessExpirationSeconds;
    }

    public long getRefreshExpirationSeconds() {
        return refreshExpirationSeconds;
    }

    private byte[] sign(String signingInput) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALG);
            mac.init(new SecretKeySpec(secretBytes, HMAC_ALG));
            return mac.doFinal(signingInput.getBytes(StandardCharsets.UTF_8));
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("Failed to sign JWT", ex);
        }
    }
}
