package com.digiyak.identity.api;

import com.digiyak.identity.api.dto.AuthDtos.AuthResponse;
import com.digiyak.identity.api.dto.AuthDtos.LoginRequest;
import com.digiyak.identity.api.dto.AuthDtos.RefreshRequest;
import com.digiyak.identity.api.dto.AuthDtos.RegisterRequest;
import com.digiyak.identity.internal.AuthService;
import com.digiyak.shared.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Student, Vendor & Runner registration and JWT auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new campus student account")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    @Operation(summary = "Log in with campus email or Ghanaian phone number")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Rotate refresh token and issue a fresh JWT access token")
    public AuthResponse refresh(@Valid @RequestBody RefreshRequest request) {
        return authService.refresh(request.refreshToken());
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Revoke all active refresh tokens for the authenticated user")
    public void logout(@AuthenticationPrincipal AuthenticatedUser user) {
        if (user != null) {
            authService.logout(user.userId());
        }
    }
}
