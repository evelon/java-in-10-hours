package ledger.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.Objects;

public record Money(BigDecimal amount) implements Comparable<Money> {

    public static final Money ZERO = of(0);

    public Money {
        Objects.requireNonNull(amount, "amount");
        if (amount.signum() < 0) {
            throw new IllegalArgumentException("음수 금액: " + amount);
        }
        try {
            // record의 equals는 BigDecimal.equals를 쓰고, 이는 scale까지 비교하므로 scale 0으로 맞춘다(1.00 -> 1)
            amount = amount.setScale(0, RoundingMode.UNNECESSARY);
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

    public Money plus(Money other) {
        return new Money(amount.add(other.amount));
    }

    public Money minus(Money other) {
        return new Money(amount.subtract(other.amount));
    }

    public BigDecimal difference(Money other) {
        return amount.subtract(other.amount);
    }

    public int percentOf(Money whole) {
        if (whole.amount.signum() == 0) {
            throw new IllegalArgumentException("0원 대비 비율 계산 불가: " + this);
        }
        // scale과 RoundingMode를 주지 않으면 나누어떨어지지 않을 때 ArithmeticException이 난다
        return amount.multiply(BigDecimal.valueOf(100))
                .divide(whole.amount, 0, RoundingMode.HALF_UP)
                .intValueExact();
    }

    public static String format(BigDecimal won) {
        // 기본 Locale에 맡기면 환경에 따라 구분자가 '.'이 될 수 있다
        return String.format(Locale.ROOT, "%,d원", won.toBigInteger());
    }

    @Override
    public int compareTo(Money other) {
        return amount.compareTo(other.amount);
    }

    @Override
    public String toString() {
        return format(amount);
    }
}
