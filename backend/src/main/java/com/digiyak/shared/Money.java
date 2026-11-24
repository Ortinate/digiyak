package com.digiyak.shared;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Immutable money value object stored strictly in integer minor units (pesewas for GHS).
 * Example: GHS 8.50 == 850 pesewas. Never use floating-point math for money.
 */
public record Money(long pesewas, String currency) {

    public static final String DEFAULT_CURRENCY = "GHS";
    private static final int BPS_DENOMINATOR = 10_000;

    public Money {
        Objects.requireNonNull(currency, "currency must not be null");
        if (currency.length() != 3) {
            throw new IllegalArgumentException("currency must be a 3-letter ISO code: " + currency);
        }
    }

    public static Money ghs(long pesewas) {
        return new Money(pesewas, DEFAULT_CURRENCY);
    }

    public static Money zero() {
        return ghs(0L);
    }

    public Money plus(Money other) {
        requireSameCurrency(other);
        return new Money(Math.addExact(this.pesewas, other.pesewas), this.currency);
    }

    public Money minus(Money other) {
        requireSameCurrency(other);
        return new Money(Math.subtractExact(this.pesewas, other.pesewas), this.currency);
    }

    /**
     * Computes a basis-point share (e.g. 1500 bps = 15.00%) using HALF_UP rounding.
     */
    public Money percentageBps(int basisPoints) {
        if (basisPoints < 0 || basisPoints > BPS_DENOMINATOR) {
            throw new IllegalArgumentException("basisPoints must be between 0 and 10000: " + basisPoints);
        }
        long share = BigDecimal.valueOf(this.pesewas)
                .multiply(BigDecimal.valueOf(basisPoints))
                .divide(BigDecimal.valueOf(BPS_DENOMINATOR), 0, RoundingMode.HALF_UP)
                .longValueExact();
        return new Money(share, this.currency);
    }

    /**
     * Splits this delivery fee cleanly into (runnerShare, platformCut) so both sum to exactly this.pesewas.
     */
    public TwoWaySplit splitTwoWays(int platformCutBps) {
        Money platformCut = percentageBps(platformCutBps);
        Money runnerShare = this.minus(platformCut);
        return new TwoWaySplit(runnerShare, platformCut);
    }

    public boolean isPositive() {
        return pesewas > 0;
    }

    public boolean isNegative() {
        return pesewas < 0;
    }

    public String formatDisplay() {
        BigDecimal major = BigDecimal.valueOf(pesewas, 2);
        return ("GHS".equals(currency) ? "₵" : currency + " ") + major.toPlainString();
    }

    private void requireSameCurrency(Money other) {
        Objects.requireNonNull(other, "other money must not be null");
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Currency mismatch: " + this.currency + " vs " + other.currency);
        }
    }

    public record TwoWaySplit(Money runnerShare, Money platformCut) {
    }
}
