package ledger.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

// 학습자가 직접 작성하는 테스트의 예시 답안. steps/03에는 넣지 않는다.
class MoneyBoundaryTest {

    @ParameterizedTest
    @ValueSource(longs = {0, 1, Long.MAX_VALUE})
    void acceptsNonNegativeAmounts(long won) {
        assertThat(Money.of(won).amount()).isEqualTo(BigDecimal.valueOf(won));
    }

    @ParameterizedTest
    @ValueSource(longs = {-1, Long.MIN_VALUE})
    void rejectsNegativeAmounts(long won) {
        assertThatThrownBy(() -> Money.of(won)).isInstanceOf(IllegalArgumentException.class);
    }
}
