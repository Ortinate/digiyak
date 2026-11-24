package com.digiyak.identity.api;

import com.digiyak.identity.api.dto.AuthDtos.AppointCampusAdminRequest;
import com.digiyak.identity.api.dto.AuthDtos.AuditLogResponse;
import com.digiyak.identity.api.dto.AuthDtos.OverridePostpaidRequest;
import com.digiyak.identity.api.dto.AuthDtos.UserProfileResponse;
import com.digiyak.identity.internal.AuthService;
import com.digiyak.shared.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/super-admin")
@PreAuthorize("hasRole('SUPER_ADMIN')")
@Tag(name = "Super Admin", description = "Global multi-campus governance, Campus Admin delegation & audit logs")
public class SuperAdminController {

    private final AuthService authService;

    public SuperAdminController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/admins")
    @Operation(summary = "Appoint a CAMPUS_ADMIN for a specific university campus")
    public UserProfileResponse appointCampusAdmin(
            @AuthenticationPrincipal AuthenticatedUser actor,
            @Valid @RequestBody AppointCampusAdminRequest request,
            HttpServletRequest httpRequest
    ) {
        return authService.appointCampusAdmin(actor, request, httpRequest.getRemoteAddr());
    }

    @DeleteMapping("/admins/{userId}")
    @Operation(summary = "Revoke CAMPUS_ADMIN privileges from a user")
    public UserProfileResponse revokeCampusAdmin(
            @AuthenticationPrincipal AuthenticatedUser actor,
            @PathVariable UUID userId,
            HttpServletRequest httpRequest
    ) {
        return authService.revokeCampusAdmin(actor, userId, httpRequest.getRemoteAddr());
    }

    @PatchMapping("/users/{userId}/postpaid")
    @Operation(summary = "Override Pay-After-Delivery / Physical Cash eligibility for a student")
    public UserProfileResponse overridePostpaid(
            @AuthenticationPrincipal AuthenticatedUser actor,
            @PathVariable UUID userId,
            @Valid @RequestBody OverridePostpaidRequest request,
            HttpServletRequest httpRequest
    ) {
        return authService.overridePostpaidEligibility(actor, userId, request, httpRequest.getRemoteAddr());
    }

    @GetMapping("/audit-logs")
    @Operation(summary = "Inspect immutable audit trail across all campuses (optional campus filter)")
    public Page<AuditLogResponse> auditLogs(
            @RequestParam(required = false) UUID campusId,
            @PageableDefault(size = 50) Pageable pageable
    ) {
        return authService.listAuditLogs(campusId, pageable);
    }
}
