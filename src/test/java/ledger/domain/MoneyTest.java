package ledger.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MoneyTest {

    @Test
    @DisplayName("long과 문자열로 만든 같은 금액은 같다(쉼표 유무 무관)")
    void ofLongAndOfStringAreEqual() {
        assertThat(Money.of("12,000")).isEqualTo(Money.of(12000));
        assertThat(Money.of("12000")).isEqualTo(Money.of(12000));
    }

    @Test
    @DisplayName("scale이 달라도 금액이 같으면 equals와 hashCode가 같다")
    void normalizesScale() {
        Money money = new Money(new BigDecimal("1.00"));

        assertThat(money).isEqualTo(Money.of(1)).hasSameHashCodeAs(Money.of(1));
        assertThat(money.amount()).isEqualTo(BigDecimal.ONE);
    }

    @Test
    @DisplayName("음수 금액은 만들 수 없다")
    void rejectsNegativeAmount() {
        assertThatThrownBy(() -> Money.of(-1)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("원 미만 금액은 만들 수 없다")
    void rejectsFractionalAmount() {
        assertThatThrownBy(() -> Money.of("1.5")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("숫자가 아닌 문자열은 금액이 아니다")
    void rejectsNonNumericText() {
        assertThatThrownBy(() -> Money.of("abc")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Money.of("")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("plus는 새 객체를 반환하고 원본은 바뀌지 않는다")
    void plusReturnsNewMoney() {
        Money base = Money.of(1000);

        Money sum = base.plus(Money.of(500));

        assertThat(sum).isEqualTo(Money.of(1500));
        assertThat(base).isEqualTo(Money.of(1000));
    }

    @Test
    @DisplayName("minus는 차액을 반환한다")
    void minusReturnsDifference() {
        assertThat(Money.of(1000).minus(Money.of(300))).isEqualTo(Money.of(700));
    }

    @Test
    @DisplayName("minus의 결과가 음수면 예외가 난다")
    void minusRejectsNegativeResult() {
        assertThatThrownBy(() -> Money.of(300).minus(Money.of(1000)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("정렬하면 금액 오름차순이 된다")
    void sortsByAmount() {
        List<Money> moneys = new ArrayList<>(List.of(Money.of(3000), Money.of(10), Money.of(200)));

        Collections.sort(moneys);

        assertThat(moneys).containsExactly(Money.of(10), Money.of(200), Money.of(3000));
    }

    @Test
    @DisplayName("toString은 천 단위 쉼표와 '원'을 붙인다")
    void formatsWithCommaAndWon() {
        assertThat(Money.of(12000)).hasToString("12,000원");
        assertThat(Money.of(0)).hasToString("0원");
        assertThat(Money.of(1000000)).hasToString("1,000,000원");
    }
}
