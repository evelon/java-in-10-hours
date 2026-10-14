package ledger.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

// MoneyTest(03)는 그대로 두고, record 전환만 따로 확인한다
class MoneyRecordTest {

    @Test
    @DisplayName("Money는 record다")
    void moneyIsRecord() {
        assertThat(Money.class.isRecord()).isTrue();
    }
}
