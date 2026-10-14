package ledger.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

// 05에서 추가하는 리포트용 Money 메서드. MoneyTest(03)는 그대로 둔다
class MoneyReportTest {

    @Test
    @DisplayName("difference는 음수가 될 수 있고 format은 부호와 쉼표를 붙인다")
    void differenceCanBeNegative() {
        BigDecimal difference = Money.of(1000).difference(Money.of(3000));

        assertThat(difference).isEqualByComparingTo("-2000");
        assertThat(Money.format(difference)).isEqualTo("-2,000원");
        assertThat(Money.format(new BigDecimal("2154700"))).isEqualTo("2,154,700원");
    }

    @ParameterizedTest
    @CsvSource({"500000, 59", "312400, 37", "32900, 4"})
    @DisplayName("percentOf는 전체 대비 비율을 정수 %로 반올림한다")
    void percentOfRoundsHalfUp(long won, int expected) {
        assertThat(Money.of(won).percentOf(Money.of(845300))).isEqualTo(expected);
    }

    @Test
    @DisplayName("0원 대비 비율은 계산할 수 없다")
    void percentOfZeroIsRejected() {
        assertThatThrownBy(() -> Money.of(1000).percentOf(Money.ZERO))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
