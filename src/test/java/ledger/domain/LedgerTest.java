package ledger.domain;

import static ledger.domain.Category.ETC;
import static ledger.domain.Category.FOOD;
import static ledger.domain.Category.HOUSING;
import static ledger.domain.Category.SALARY;
import static ledger.domain.Category.TRANSPORT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LedgerTest {

    private static final YearMonth OCTOBER = YearMonth.of(2026, 10);

    // 09 데모의 2026-10과 같은 합계. 다른 달 항목은 집계에서 빠져야 한다
    private static final List<Entry> ENTRIES = List.of(
            income("2026-10-25", SALARY, 3_000_000),
            expense("2026-10-01", HOUSING, 500_000),
            expense("2026-10-05", TRANSPORT, 32_900),
            expense("2026-10-11", FOOD, 12_000),
            expense("2026-10-12", FOOD, 300_400),
            expense("2026-09-30", FOOD, 99_000),
            income("2026-11-01", SALARY, 1_000_000));

    @Test
    @DisplayName("월별 수입·지출 합계는 그 달 항목만 더한다")
    void totalsForMonth() {
        Ledger ledger = new Ledger(ENTRIES, List.of());

        assertThat(ledger.totalIncome(OCTOBER)).isEqualTo(Money.of(3_000_000));
        assertThat(ledger.totalExpense(OCTOBER)).isEqualTo(Money.of(845_300));
        assertThat(Money.format(ledger.totalIncome(OCTOBER).difference(ledger.totalExpense(OCTOBER))))
                .isEqualTo("2,154,700원");
    }

    @Test
    @DisplayName("항목이 없는 달의 합계는 0원이다")
    void totalsForEmptyMonth() {
        Ledger ledger = new Ledger(ENTRIES, List.of());

        assertThat(ledger.totalIncome(YearMonth.of(2026, 12))).isEqualTo(Money.ZERO);
        assertThat(ledger.totalExpense(YearMonth.of(2026, 12))).isEqualTo(Money.ZERO);
    }

    @Test
    @DisplayName("카테고리별 합계는 지출만, 금액 내림차순이다")
    void expenseByCategorySortedByAmount() {
        Ledger ledger = new Ledger(ENTRIES, List.of());

        assertThat(ledger.expenseByCategory(OCTOBER)).containsExactly(
                entry(HOUSING, Money.of(500_000)),
                entry(FOOD, Money.of(312_400)),
                entry(TRANSPORT, Money.of(32_900)));
    }

    @Test
    @DisplayName("카테고리별 합계가 같으면 Category 선언 순이다")
    void expenseByCategoryTieFollowsDeclarationOrder() {
        Ledger ledger = new Ledger(List.of(
                expense("2026-10-01", ETC, 1000),
                expense("2026-10-02", FOOD, 1000)), List.of());

        assertThat(ledger.expenseByCategory(OCTOBER)).containsExactly(
                entry(FOOD, Money.of(1000)),
                entry(ETC, Money.of(1000)));
    }

    @Test
    @DisplayName("카테고리로 예산을 찾고, 같은 카테고리 예산이 여럿이면 나중 것이다")
    void budgetOfReturnsLatestBudget() {
        Ledger ledger = new Ledger(ENTRIES, List.of(
                new Budget(FOOD, Money.of(200_000)),
                new Budget(FOOD, Money.of(300_000))));

        assertThat(ledger.budgetOf(FOOD)).contains(new Budget(FOOD, Money.of(300_000)));
        assertThat(ledger.budgetOf(HOUSING)).isEmpty();
    }

    @Test
    @DisplayName("지출이 한도를 넘은 카테고리만 Category 선언 순으로 돌려준다(같으면 초과 아님)")
    void overBudgetOnlyWhenSpentExceedsLimit() {
        Ledger ledger = new Ledger(ENTRIES, List.of(
                new Budget(TRANSPORT, Money.of(30_000)),
                new Budget(HOUSING, Money.of(500_000)),
                new Budget(FOOD, Money.of(300_000))));

        assertThat(ledger.overBudget(OCTOBER)).containsExactly(
                new BudgetOverrun(FOOD, OCTOBER, Money.of(312_400), Money.of(300_000)),
                new BudgetOverrun(TRANSPORT, OCTOBER, Money.of(32_900), Money.of(30_000)));
    }

    @Test
    @DisplayName("받은 컬렉션을 복사해 두고, 돌려주는 컬렉션은 바꿀 수 없다")
    void keepsCopiesAndReturnsUnmodifiableCollections() {
        List<Entry> source = new ArrayList<>(ENTRIES);
        Ledger ledger = new Ledger(source, List.of());

        source.clear();

        assertThat(ledger.entries()).containsExactlyElementsOf(ENTRIES);
        assertThatThrownBy(() -> ledger.entries().add(expense("2026-10-01", FOOD, 1)))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> ledger.expenseByCategory(OCTOBER).put(ETC, Money.of(1)))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    private static Income income(String date, Category category, long won) {
        return new Income(LocalDate.parse(date), category, Money.of(won), "");
    }

    private static Expense expense(String date, Category category, long won) {
        return new Expense(LocalDate.parse(date), category, Money.of(won), "");
    }
}
