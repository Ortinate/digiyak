package com.digiyak.identity.domain;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "users", schema = "identity")
public class User {

    public static final int POSTPAID_LOCKOUT_THRESHOLD = 2;

    @Id
    private UUID id;

    @Column(name = "campus_id")
    private UUID campusId;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false, unique = true, length = 20)
    private String phone;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(name = "student_id_number", unique = true, length = 64)
    private String studentIdNumber;

    @Column(name = "business_name", length = 150)
    private String businessName;

    @Enumerated(EnumType.STRING)
    @Column(name = "momo_provider", length = 20)
    private MomoProvider momoProvider;

    @Column(name = "momo_number", length = 20)
    private String momoNumber;

    @Column(name = "paystack_recipient_code", length = 100)
    private String paystackRecipientCode;

    @Column(name = "no_show_count", nullable = false)
    private int noShowCount;

    @Column(name = "allow_postpaid", nullable = false)
    private boolean allowPostpaid = true;

    @Column(name = "is_verified", nullable = false)
    private boolean verified;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "user_roles",
            schema = "identity",
            joinColumns = @JoinColumn(name = "user_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 32)
    private Set<UserRole> roles = EnumSet.noneOf(UserRole.class);

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected User() {
    }

    public User(
            UUID campusId,
            String email,
            String phone,
            String passwordHash,
            String fullName,
            String studentIdNumber
    ) {
        this.id = UUID.randomUUID();
        this.campusId = campusId;
        this.email = email.trim().toLowerCase();
        this.phone = normalizeGhanaPhone(phone);
        this.passwordHash = passwordHash;
        this.fullName = fullName.trim();
        this.studentIdNumber = studentIdNumber != null && !studentIdNumber.isBlank() ? studentIdNumber.trim() : null;
        this.roles = EnumSet.of(UserRole.CUSTOMER);
        this.noShowCount = 0;
        this.allowPostpaid = true;
        this.active = true;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
        if (this.createdAt == null) {
            this.createdAt = now;
        }
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }

    /**
     * Normalizes Ghanaian phone formats (024XXXXXXX, +23324XXXXXXX, 23324XXXXXXX) to 233XXXXXXXXX.
     */
    public static String normalizeGhanaPhone(String raw) {
        String digits = raw.replaceAll("[^0-9]", "");
        if (digits.startsWith("0") && digits.length() == 10) {
            return "233" + digits.substring(1);
        }
        if (digits.startsWith("233") && digits.length() == 12) {
            return digits;
        }
        return digits;
    }

    public void addRole(UserRole role) {
        this.roles.add(role);
    }

    public void removeRole(UserRole role) {
        this.roles.remove(role);
    }

    public boolean hasRole(UserRole role) {
        return this.roles.contains(role);
    }

    public void updateMomoWallet(MomoProvider provider, String momoNumber) {
        this.momoProvider = provider;
        this.momoNumber = normalizeGhanaPhone(momoNumber);
        this.paystackRecipientCode = null; // Re-register Paystack recipient on next payout
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName != null && !businessName.isBlank() ? businessName.trim() : null;
    }

    /**
     * Anti-ghosting guard: 2 or more no-shows restrict the user to MOMO_PREPAID only.
     */
    public void recordNoShow() {
        this.noShowCount += 1;
        if (this.noShowCount >= POSTPAID_LOCKOUT_THRESHOLD) {
            this.allowPostpaid = false;
        }
    }

    public void setAllowPostpaid(boolean allowPostpaid) {
        this.allowPostpaid = allowPostpaid;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setCampusId(UUID campusId) {
        this.campusId = campusId;
    }

    public UUID getId() {
        return id;
    }

    public UUID getCampusId() {
        return campusId;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getFullName() {
        return fullName;
    }

    public String getStudentIdNumber() {
        return studentIdNumber;
    }

    public String getBusinessName() {
        return businessName;
    }

    public MomoProvider getMomoProvider() {
        return momoProvider;
    }

    public String getMomoNumber() {
        return momoNumber;
    }

    public String getPaystackRecipientCode() {
        return paystackRecipientCode;
    }

    public int getNoShowCount() {
        return noShowCount;
    }

    public boolean isAllowPostpaid() {
        return allowPostpaid;
    }

    public boolean isVerified() {
        return verified;
    }

    public boolean isActive() {
        return active;
    }

    public Set<UserRole> getRoles() {
        return Collections.unmodifiableSet(roles);
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
