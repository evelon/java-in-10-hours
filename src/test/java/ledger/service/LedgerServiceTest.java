package ledger.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import ledger.domain.Budget;
import ledger.domain.BudgetOverrun;
import ledger.domain.Category;
import ledger.domain.Entry;
import ledger.domain.Expense;
import ledger.domain.Income;
import ledger.domain.Money;
import ledger.repository.InMemoryLedgerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LedgerServiceTest {

    private static final YearMonth OCTOBER = YearMonth.of(2026, 10);
    private static final YearMonth NOVEMBER = YearMonth.of(2026, 11);

    private final InMemoryLedgerRepository repository = new InMemoryLedgerRepository();
    private final LedgerService service = new LedgerService(repository);

    @Test
    @DisplayName("add는 항목을 저장하고, 예산이 없으면 경고가 없다")
    void addSavesEntry() {
        Entry lunch = expense("2026-10-11", Category.FOOD, 12_000);

        List<BudgetOverrun> warnings = service.add(lunch);

        assertThat(warnings).isEmpty();
        assertThat(repository.findAll()).containsExactly(lunch);
    }

    @Test
    @DisplayName("add로 한도를 넘으면 그 카테고리·월의 초과를 돌려준다(한도와 같으면 초과 아님)")
    void addWarnsWhenBudgetExceeded() {
        service.setBudget(new Budget(Category.FOOD, Money.of(100_000)));
        service.setBudget(new Budget(Category.FOOD, Money.of(300_000)));

        assertThat(service.add(expense("2026-10-01", Category.FOOD, 300_000))).isEmpty();
        assertThat(service.add(expense("2026-10-11", Category.FOOD, 12_400))).containsExactly(
                new BudgetOverrun(Category.FOOD, OCTOBER, Money.of(312_400), Money.of(300_000)));
    }

    @Test
    @DisplayName("addAll은 이번에 추가한 지출이 걸린 카테고리·월의 초과만 월, Category 선언 순으로 돌려준다")
    void addAllWarnsOnlyForTouchedCategoryAndMonth() {
        service.setBudget(new Budget(Category.FOOD, Money.of(10_000)));
        service.setBudget(new Budget(Category.TRANSPORT, Money.of(10_000)));
        service.add(expense("2026-10-01", Category.FOOD, 20_000));

        List<BudgetOverrun> warnings = service.addAll(List.of(
                expense("2026-11-02", Category.FOOD, 20_000),
                expense("2026-10-03", Category.TRANSPORT, 20_000),
                new Income(LocalDate.parse("2026-10-25"), Category.SALARY, Money.of(3_000_000), "")));

        assertThat(warnings).containsExactly(
                new BudgetOverrun(Category.TRANSPORT, OCTOBER, Money.of(20_000), Money.of(10_000)),
                new BudgetOverrun(Category.FOOD, NOVEMBER, Money.of(20_000), Money.of(10_000)));
        assertThat(repository.findAll()).hasSize(4);
    }

    private static Expense expense(String date, Category category, long won) {
        return new Expense(LocalDate.parse(date), category, Money.of(won), "");
    }
}
