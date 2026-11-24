package com.digiyak.identity.api;

import com.digiyak.identity.api.dto.AuthDtos.OptInVendorRequest;
import com.digiyak.identity.api.dto.AuthDtos.UpdateMomoRequest;
import com.digiyak.identity.api.dto.AuthDtos.UserProfileResponse;
import com.digiyak.identity.internal.AuthService;
import com.digiyak.shared.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/me")
@Tag(name = "User Profile", description = "Self-service student profile, MoMo wallet & role activation")
public class UserProfileController {

    private final AuthService authService;

    public UserProfileController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping
    @Operation(summary = "Fetch current authenticated student profile")
    public UserProfileResponse me(@AuthenticationPrincipal AuthenticatedUser user) {
        return authService.getProfile(user.userId());
    }

    @PatchMapping("/momo")
    @Operation(summary = "Set or update MoMo payout wallet (MTN, Telecel, AirtelTigo)")
    public UserProfileResponse updateMomo(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody UpdateMomoRequest request
    ) {
        return authService.updateMomo(user.userId(), request);
    }

    @PostMapping("/roles/runner")
    @Operation(summary = "Activate student Runner side-hustle role (requires configured MoMo wallet)")
    public UserProfileResponse optInRunner(@AuthenticationPrincipal AuthenticatedUser user) {
        return authService.optInRunner(user.userId());
    }

    @PostMapping("/roles/vendor")
    @Operation(summary = "Activate campus Vendor Partner dispatch role")
    public UserProfileResponse optInVendor(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody OptInVendorRequest request
    ) {
        return authService.optInVendor(user.userId(), request);
    }
}
