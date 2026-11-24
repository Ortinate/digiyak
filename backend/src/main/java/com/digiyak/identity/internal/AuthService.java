package com.digiyak.identity.internal;

import com.digiyak.identity.IdentityModuleApi;
import com.digiyak.identity.api.dto.AuthDtos.AppointCampusAdminRequest;
import com.digiyak.identity.api.dto.AuthDtos.AuditLogResponse;
import com.digiyak.identity.api.dto.AuthDtos.AuthResponse;
import com.digiyak.identity.api.dto.AuthDtos.LoginRequest;
import com.digiyak.identity.api.dto.AuthDtos.OptInVendorRequest;
import com.digiyak.identity.api.dto.AuthDtos.OverridePostpaidRequest;
import com.digiyak.identity.api.dto.AuthDtos.RegisterRequest;
import com.digiyak.identity.api.dto.AuthDtos.UpdateMomoRequest;
import com.digiyak.identity.api.dto.AuthDtos.UserProfileResponse;
import com.digiyak.identity.domain.AdminAuditLog;
import com.digiyak.identity.domain.RefreshToken;
import com.digiyak.identity.domain.User;
import com.digiyak.identity.domain.UserRole;
import com.digiyak.shared.ApiException;
import com.digiyak.shared.AuthenticatedUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@Transactional
public class AuthService implements IdentityModuleApi {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AdminAuditLogRepository auditLogRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            AdminAuditLogRepository auditLogRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider
    ) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.auditLogRepository = auditLogRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public AuthResponse register(RegisterRequest req) {
        String normalizedEmail = req.email().trim().toLowerCase();
        String normalizedPhone = User.normalizeGhanaPhone(req.phone());

        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw ApiException.conflict("EMAIL_ALREADY_REGISTERED", "An account with this email already exists");
        }
        if (userRepository.existsByPhone(normalizedPhone)) {
            throw ApiException.conflict("PHONE_ALREADY_REGISTERED", "An account with this phone number already exists");
        }
        if (req.studentIdNumber() != null && !req.studentIdNumber().isBlank()
                && userRepository.existsByStudentIdNumber(req.studentIdNumber().trim())) {
            throw ApiException.conflict("STUDENT_ID_EXISTS", "This Student ID is already registered");
        }

        String hash = passwordEncoder.encode(req.password());
        User user = new User(
                req.campusId(),
                normalizedEmail,
                normalizedPhone,
                hash,
                req.fullName(),
                req.studentIdNumber()
        );
        if (req.momoProvider() != null && req.momoNumber() != null && !req.momoNumber().isBlank()) {
            user.updateMomoWallet(req.momoProvider(), req.momoNumber());
        }

        userRepository.save(user);
        return issueTokenPair(user);
    }

    public AuthResponse login(LoginRequest req) {
        String id = req.identifier().trim();
        User user = (id.contains("@")
                ? userRepository.findByEmailIgnoreCase(id)
                : userRepository.findByPhone(User.normalizeGhanaPhone(id)))
                .orElseThrow(() -> ApiException.unauthorized("Invalid credentials"));

        if (!user.isActive()) {
            throw ApiException.forbidden("ACCOUNT_SUSPENDED", "This account has been suspended");
        }
        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw ApiException.unauthorized("Invalid credentials");
        }

        return issueTokenPair(user);
    }

    public AuthResponse refresh(String rawRefreshToken) {
        String hash = jwtTokenProvider.hashRefreshToken(rawRefreshToken);
        RefreshToken existing = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> ApiException.unauthorized("Invalid refresh token"));

        if (!existing.isUsable(Instant.now())) {
            throw ApiException.unauthorized("Refresh token expired or revoked");
        }

        existing.revoke();
        refreshTokenRepository.save(existing);

        User user = userRepository.findById(existing.getUserId())
                .orElseThrow(() -> ApiException.unauthorized("Account not found"));
        if (!user.isActive()) {
            throw ApiException.forbidden("ACCOUNT_SUSPENDED", "This account has been suspended");
        }

        return issueTokenPair(user);
    }

    public void logout(UUID userId) {
        refreshTokenRepository.revokeAllForUser(userId);
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(UUID userId) {
        return UserProfileResponse.from(requireUser(userId));
    }

    public UserProfileResponse updateMomo(UUID userId, UpdateMomoRequest req) {
        User user = requireUser(userId);
        user.updateMomoWallet(req.momoProvider(), req.momoNumber());
        return UserProfileResponse.from(userRepository.save(user));
    }

    public UserProfileResponse optInRunner(UUID userId) {
        User user = requireUser(userId);
        if (user.getMomoProvider() == null || user.getMomoNumber() == null) {
            throw ApiException.badRequest("MOMO_REQUIRED_FOR_RUNNER",
                    "Configure your MoMo wallet before activating the Runner role");
        }
        user.addRole(UserRole.RUNNER);
        return UserProfileResponse.from(userRepository.save(user));
    }

    public UserProfileResponse optInVendor(UUID userId, OptInVendorRequest req) {
        User user = requireUser(userId);
        user.setBusinessName(req.businessName());
        user.addRole(UserRole.VENDOR_PARTNER);
        return UserProfileResponse.from(userRepository.save(user));
    }

    public UserProfileResponse appointCampusAdmin(
            AuthenticatedUser actor,
            AppointCampusAdminRequest req,
            String ipAddress
    ) {
        requireSuperAdmin(actor);
        User target = requireUser(req.userId());
        target.setCampusId(req.campusId());
        target.addRole(UserRole.CAMPUS_ADMIN);
        userRepository.save(target);

        recordAdminAction(
                actor.userId(),
                UserRole.SUPER_ADMIN.name(),
                req.campusId(),
                "GRANT_CAMPUS_ADMIN",
                "USER",
                target.getId(),
                "{\"email\":\"" + target.getEmail() + "\"}",
                ipAddress
        );
        return UserProfileResponse.from(target);
    }

    public UserProfileResponse revokeCampusAdmin(AuthenticatedUser actor, UUID targetUserId, String ipAddress) {
        requireSuperAdmin(actor);
        User target = requireUser(targetUserId);
        target.removeRole(UserRole.CAMPUS_ADMIN);
        userRepository.save(target);

        recordAdminAction(
                actor.userId(),
                UserRole.SUPER_ADMIN.name(),
                target.getCampusId(),
                "REVOKE_CAMPUS_ADMIN",
                "USER",
                target.getId(),
                null,
                ipAddress
        );
        return UserProfileResponse.from(target);
    }

    public UserProfileResponse overridePostpaidEligibility(
            AuthenticatedUser actor,
            UUID targetUserId,
            OverridePostpaidRequest req,
            String ipAddress
    ) {
        User target = requireUser(targetUserId);
        if (!actor.canManageCampus(target.getCampusId())) {
            throw ApiException.forbidden("FORBIDDEN_CAMPUS_SCOPE", "Cannot manage users outside your campus");
        }
        target.setAllowPostpaid(req.allowPostpaid());
        userRepository.save(target);

        String actorRole = actor.isSuperAdmin() ? UserRole.SUPER_ADMIN.name() : UserRole.CAMPUS_ADMIN.name();
        recordAdminAction(
                actor.userId(),
                actorRole,
                target.getCampusId(),
                "OVERRIDE_POSTPAID_ELIGIBILITY",
                "USER",
                target.getId(),
                "{\"allowPostpaid\":" + req.allowPostpaid() + "}",
                ipAddress
        );
        return UserProfileResponse.from(target);
    }

    @Transactional(readOnly = true)
    public Page<AuditLogResponse> listAuditLogs(UUID campusId, Pageable pageable) {
        Page<AdminAuditLog> page = campusId != null
                ? auditLogRepository.findByTargetCampusIdOrderByCreatedAtDesc(campusId, pageable)
                : auditLogRepository.findAllByOrderByCreatedAtDesc(pageable);
        return page.map(AuditLogResponse::from);
    }

    // ---- IdentityModuleApi implementation (cross-module contract) ----

    @Override
    @Transactional(readOnly = true)
    public UserDeliveryEligibility getDeliveryEligibility(UUID userId) {
        User user = requireUser(userId);
        return new UserDeliveryEligibility(
                user.getId(),
                user.getCampusId(),
                user.getFullName(),
                user.getPhone(),
                user.isActive(),
                user.isVerified(),
                user.isAllowPostpaid(),
                user.getNoShowCount()
        );
    }

    @Override
    public void recordNoShow(UUID userId) {
        User user = requireUser(userId);
        user.recordNoShow();
        userRepository.save(user);
    }

    @Override
    public void recordAdminAction(
            UUID actorUserId,
            String actorRole,
            UUID targetCampusId,
            String action,
            String targetEntityType,
            UUID targetEntityId,
            String metadataJson,
            String ipAddress
    ) {
        UserRole role = UserRole.valueOf(actorRole);
        AdminAuditLog log = new AdminAuditLog(
                actorUserId,
                role,
                targetCampusId,
                action,
                targetEntityType,
                targetEntityId,
                metadataJson,
                ipAddress
        );
        auditLogRepository.save(log);
    }

    private AuthResponse issueTokenPair(User user) {
        String accessToken = jwtTokenProvider.createAccessToken(user);
        String rawRefresh = jwtTokenProvider.generateRawRefreshToken();
        String refreshHash = jwtTokenProvider.hashRefreshToken(rawRefresh);
        Instant refreshExpiry = Instant.now().plusSeconds(jwtTokenProvider.getRefreshExpirationSeconds());

        refreshTokenRepository.save(new RefreshToken(user.getId(), refreshHash, refreshExpiry));

        return new AuthResponse(
                accessToken,
                rawRefresh,
                jwtTokenProvider.getAccessExpirationSeconds(),
                UserProfileResponse.from(user)
        );
    }

    private User requireUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> ApiException.notFound("USER_NOT_FOUND", "User not found"));
    }

    private void requireSuperAdmin(AuthenticatedUser actor) {
        if (!actor.isSuperAdmin()) {
            throw ApiException.forbidden("SUPER_ADMIN_REQUIRED", "Only SUPER_ADMIN may perform this operation");
        }
    }
}
