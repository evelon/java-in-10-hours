package ledger.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.Objects;

public final class Money implements Comparable<Money> {
    private final BigDecimal amount;

    public Money(BigDecimal amount) {
        Objects.requireNonNull(amount, "amount");
        if (amount.signum() < 0) {
            throw new IllegalArgumentException("음수 금액: " + amount);
        }
        try {
            // BigDecimal.equals는 scale까지 비교하므로 scale 0으로 맞춘다(1.00 -> 1)
            this.amount = amount.setScale(0, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("원 미만 금액: " + amount, e);
        }
    }

    public static Money of(long won) {
        return new Money(BigDecimal.valueOf(won));
    }

    public static Money of(String text) {
        try {
            return new Money(new BigDecimal(text.replace(",", "")));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("금액 형식 오류: " + text, e);
        }
    }

    public BigDecimal amount() {
        return amount;
    }

    public Money plus(Money other) {
        return new Money(amount.add(other.amount));
    }

    public Money minus(Money other) {
        return new Money(amount.subtract(other.amount));
    }

    @Override
    public int compareTo(Money other) {
        return amount.compareTo(other.amount);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Money)) {
            return false;
        }
        Money other = (Money) o;
        return amount.equals(other.amount);
    }

    @Override
    public int hashCode() {
        return amount.hashCode();
    }

    @Override
    public String toString() {
        // 기본 Locale에 맡기면 환경에 따라 구분자가 '.'이 될 수 있다
        return String.format(Locale.ROOT, "%,d원", amount.toBigInteger());
    }
}
