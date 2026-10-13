package ledger.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

// 학습자가 직접 작성하는 테스트의 예시 답안. steps/04에는 넣지 않는다.
class DoublePitfallTest {

    @Test
    void doubleSumIsNotExact() {
        assertThat(0.1 + 0.2).isNotEqualTo(0.3);
        assertThat(new BigDecimal("0.1").add(new BigDecimal("0.2"))).isEqualTo(new BigDecimal("0.3"));
    }

    @Test
    void doubleVatIsNotExact() {
        // 1,500원에 부가세 10%를 더한 1,650원이 double로는 1650.0000000000002가 된다
        assertThat(1500 * 1.1).isNotEqualTo(1650.0);
        assertThat(new BigDecimal("1500").multiply(new BigDecimal("1.1")))
                .isEqualByComparingTo(new BigDecimal("1650"));
    }
}
