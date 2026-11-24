package com.digiyak;

import com.digiyak.delivery.domain.DeliveryEnums.CashCollectionStatus;
import com.digiyak.delivery.domain.DeliveryEnums.DeliverySource;
import com.digiyak.delivery.domain.DeliveryEnums.DeliveryStatus;
import com.digiyak.delivery.domain.DeliveryEnums.PackageCategory;
import com.digiyak.delivery.domain.DeliveryEnums.PayLinkStatus;
import com.digiyak.delivery.domain.DeliveryEnums.PaymentMethod;
import com.digiyak.delivery.domain.DeliveryEnums.PaymentTiming;
import com.digiyak.delivery.domain.DeliveryRequest;
import com.digiyak.delivery.domain.PaymentLink;
import com.digiyak.identity.domain.User;
import com.digiyak.identity.domain.UserRole;
import com.digiyak.identity.internal.JwtTokenProvider;
import com.digiyak.shared.ApiException;
import com.digiyak.shared.AuthenticatedUser;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IdentityAndDeliveryUnitTests {

    private static final String TEST_SECRET = "digiyak-unit-test-secret-key-at-least-64-characters-long-1234567890";

    @Test
    void jwtSignsAndVerifiesStudentAndSuperAdminTokens() {
        JwtTokenProvider provider = new JwtTokenProvider(TEST_SECRET, 900, 604800, new ObjectMapper());
        UUID campusId = UUID.randomUUID();

        User student = new User(campusId, "Ama@UG.edu.gh", "024-123-4567", "hashed", "Ama Mensah", "10987654");
        student.addRole(UserRole.RUNNER);

        String token = provider.createAccessToken(student);
        AuthenticatedUser parsed = provider.parseAndVerify(token);

        assertThat(parsed.userId()).isEqualTo(student.getId());
        assertThat(parsed.campusId()).isEqualTo(campusId);
        assertThat(parsed.email()).isEqualTo("ama@ug.edu.gh");
        assertThat(parsed.isRunner()).isTrue();
        assertThat(parsed.isSuperAdmin()).isFalse();
        assertThat(student.getPhone()).isEqualTo("233241234567");

        // Tampered token must be rejected
        String tampered = token.substring(0, token.length() - 2) + "xx";
        assertThatThrownBy(() -> provider.parseAndVerify(tampered))
                .isInstanceOf(ApiException.class);

        // Global SUPER_ADMIN with null campusId can manage any campus
        User superAdmin = new User(null, "hq@digiyak.app", "+233551112222", "hashed", "Digiyak HQ", null);
        superAdmin.addRole(UserRole.SUPER_ADMIN);
        AuthenticatedUser parsedAdmin = provider.parseAndVerify(provider.createAccessToken(superAdmin));

        assertThat(parsedAdmin.campusId()).isNull();
        assertThat(parsedAdmin.isSuperAdmin()).isTrue();
        assertThat(parsedAdmin.canManageCampus(UUID.randomUUID())).isTrue();
    }

    @Test
    void antiGhostingLocksOutPostpaidAfterTwoNoShows() {
        User buyer = new User(UUID.randomUUID(), "kwame@ug.edu.gh", "0501234567", "hashed", "Kwame", null);
        assertThat(buyer.isAllowPostpaid()).isTrue();

        buyer.recordNoShow();
        assertThat(buyer.getNoShowCount()).isEqualTo(1);
        assertThat(buyer.isAllowPostpaid()).isTrue();

        buyer.recordNoShow();
        assertThat(buyer.getNoShowCount()).isEqualTo(2);
        assertThat(buyer.isAllowPostpaid()).isFalse();
    }

    @Test
    void momoPrepaidLifecycleAndExactTwoWaySplit() {
        DeliveryRequest req = sampleDelivery(PaymentMethod.MOMO_PREPAID, 800L);

        assertThat(req.getStatus()).isEqualTo(DeliveryStatus.AWAITING_PAYMENT);
        assertThat(req.getRunnerPayoutPesewas()).isEqualTo(680L); // 85%
        assertThat(req.getPlatformCutPesewas()).isEqualTo(120L);  // 15%
        assertThat(req.getRunnerPayoutPesewas() + req.getPlatformCutPesewas()).isEqualTo(800L);

        req.markPaidAndDispatch();
        assertThat(req.getStatus()).isEqualTo(DeliveryStatus.FINDING_RUNNER);

        req.markRunnerAssigned();
        assertThatThrownBy(() -> req.markPickedUp("9999".equals(req.getPickupCode()) ? "0000" : "9999", null))
                .isInstanceOf(ApiException.class);

        req.markPickedUp(req.getPickupCode(), null);
        assertThat(req.getStatus()).isEqualTo(DeliveryStatus.IN_TRANSIT);

        req.completeWithPin(req.getDeliveryPin(), false, null);
        assertThat(req.getStatus()).isEqualTo(DeliveryStatus.DELIVERED);
    }

    @Test
    void payAfterDeliveryTransitionsThroughHandoffPayment() {
        DeliveryRequest req = sampleDelivery(PaymentMethod.MOMO_PAY_AFTER_DELIVERY, 1000L);
        assertThat(req.getStatus()).isEqualTo(DeliveryStatus.FINDING_RUNNER);

        req.markRunnerAssigned();
        req.markPickedUp(req.getPickupCode(), null);
        req.markAwaitingHandoffPayment();
        assertThat(req.getStatus()).isEqualTo(DeliveryStatus.AWAITING_HANDOFF_PAYMENT);

        // Payment link completes and unlocks DELIVERED status
        PaymentLink link = PaymentLink.create(req.getId(), req.getRecipientPhone(), req.getDeliveryFeePesewas());
        assertThat(link.getStatus()).isEqualTo(PayLinkStatus.ACTIVE);
        link.markPaid();
        req.markPaidAndDispatch();

        assertThat(link.getStatus()).isEqualTo(PayLinkStatus.PAID);
        assertThat(req.getStatus()).isEqualTo(DeliveryStatus.DELIVERED);
    }

    @Test
    void physicalCashRequiresCashConfirmationAndValidPin() {
        DeliveryRequest req = sampleDelivery(PaymentMethod.PHYSICAL_CASH, 800L);
        assertThat(req.getStatus()).isEqualTo(DeliveryStatus.FINDING_RUNNER);
        assertThat(req.getCashCollectionStatus()).isEqualTo(CashCollectionStatus.PENDING_COLLECTION);

        req.markRunnerAssigned();
        req.markPickedUp(req.getPickupCode(), null);

        // Must fail if runner hasn't confirmed physical cash collected
        assertThatThrownBy(() -> req.completeWithPin(req.getDeliveryPin(), false, null))
                .isInstanceOf(ApiException.class);

        req.completeWithPin(req.getDeliveryPin(), true, null);
        assertThat(req.getStatus()).isEqualTo(DeliveryStatus.DELIVERED);
        assertThat(req.getCashCollectionStatus()).isEqualTo(CashCollectionStatus.COLLECTED_BY_RUNNER);
        assertThat(req.getCashConfirmedAt()).isNotNull();
    }

    private DeliveryRequest sampleDelivery(PaymentMethod method, long feePesewas) {
        return DeliveryRequest.create(
                UUID.randomUUID(),
                UUID.randomUUID(),
                DeliverySource.STUDENT_P2P,
                PackageCategory.PREPAID_FOOD,
                "1 pack Waakye prepaid under Ama",
                null,
                UUID.randomUUID(),
                "Night Market",
                new BigDecimal("5.650500"),
                new BigDecimal("-0.186900"),
                "Auntie Muni",
                "0241112222",
                null,
                null,
                UUID.randomUUID(),
                "Commonwealth Hall",
                new BigDecimal("5.652200"),
                new BigDecimal("-0.188100"),
                "Ama Mensah",
                "0243334444",
                "Room B14",
                method,
                method == PaymentMethod.MOMO_PREPAID ? PaymentTiming.PREPAID : PaymentTiming.PAY_ON_DELIVERY,
                feePesewas,
                1500,
                "idem-" + UUID.randomUUID()
        );
    }
}
