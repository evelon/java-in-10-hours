package ledger.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import ledger.domain.Category;
import ledger.domain.Entry;
import ledger.domain.Expense;
import ledger.domain.Income;
import ledger.domain.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

// LedgerRepository 구현이 지켜야 할 계약. 구현마다 이 클래스를 상속해 createRepository만 채운다
public abstract class LedgerRepositoryContractTest {

    private static final Entry LUNCH = new Expense(LocalDate.of(2026, 10, 11), Category.FOOD, Money.of(12000), "점심", "회사");
    private static final Entry SALARY = new Income(LocalDate.of(2026, 10, 25), Category.SALARY, Money.of(3000000), "월급");
    private static final Entry RENT = new Expense(LocalDate.of(2026, 9, 1), Category.HOUSING, Money.of(500000), "");

    private LedgerRepository repository;

    // 테스트마다 비어 있는 새 저장소를 만든다
    protected abstract LedgerRepository createRepository();

    @BeforeEach
    void setUp() {
        repository = createRepository();
    }

    @Test
    @DisplayName("처음에는 비어 있고, 저장한 순서대로 돌려준다")
    void returnsEntriesInSavedOrder() {
        assertThat(repository.findAll()).isEmpty();

        repository.save(SALARY);
        repository.save(RENT);
        repository.save(LUNCH);

        assertThat(repository.findAll()).containsExactly(SALARY, RENT, LUNCH);
    }

    @Test
    @DisplayName("같은 항목을 두 번 저장하면 두 번 나온다")
    void keepsDuplicates() {
        repository.save(LUNCH);
        repository.save(LUNCH);

        assertThat(repository.findAll()).containsExactly(LUNCH, LUNCH);
    }

    @Test
    @DisplayName("saveAll은 기존 항목 뒤에 받은 순서대로 붙인다")
    void saveAllAppendsInOrder() {
        repository.save(RENT);

        repository.saveAll(List.of(LUNCH, SALARY));

        assertThat(repository.findAll()).containsExactly(RENT, LUNCH, SALARY);
    }

    @Test
    @DisplayName("findByMonth는 그 달 항목만 저장 순서대로 돌려준다")
    void findByMonthFiltersByMonth() {
        repository.saveAll(List.of(LUNCH, RENT, SALARY));

        assertThat(repository.findByMonth(YearMonth.of(2026, 10))).containsExactly(LUNCH, SALARY);
        assertThat(repository.findByMonth(YearMonth.of(2026, 8))).isEmpty();
    }

    @Test
    @DisplayName("돌려준 리스트는 바꿀 수 없다")
    void returnsUnmodifiableLists() {
        repository.save(LUNCH);

        assertThatThrownBy(() -> repository.findAll().add(SALARY))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> repository.findByMonth(YearMonth.of(2026, 10)).clear())
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
