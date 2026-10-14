package ledger.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EntryTest {

    private static final LocalDate DATE = LocalDate.of(2026, 10, 11);

    @Test
    @DisplayName("varargs로 받은 태그를 보관하고 중복은 하나로 합친다")
    void collectsVarargsTags() {
        Expense expense = new Expense(DATE, Category.FOOD, Money.of(12000), "점심 김밥", "회사", "점심", "회사");

        assertThat(expense.tags()).containsExactlyInAnyOrder("회사", "점심");
    }

    @Test
    @DisplayName("태그 없이 만들면 태그는 빈 집합이다")
    void noTagsMeansEmptySet() {
        Income income = new Income(DATE, Category.SALARY, Money.of(3000000), "");

        assertThat(income.tags()).isEmpty();
    }

    @Test
    @DisplayName("태그는 바꿀 수 없고, 넘긴 집합을 나중에 바꿔도 영향이 없다")
    void tagsAreImmutableCopy() {
        Set<String> source = new HashSet<>(Set.of("회사"));
        Expense expense = new Expense(DATE, Category.FOOD, Money.of(12000), "", source);

        source.add("점심");

        assertThat(expense.tags()).containsExactly("회사");
        assertThatThrownBy(() -> expense.tags().add("야식"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("type은 수입이면 INCOME, 지출이면 EXPENSE다")
    void typeFollowsImplementation() {
        Entry income = new Income(DATE, Category.SALARY, Money.of(3000000), "월급");
        Entry expense = new Expense(DATE, Category.FOOD, Money.of(12000), "점심");

        assertThat(income.type()).isEqualTo(Entry.Type.INCOME);
        assertThat(expense.type()).isEqualTo(Entry.Type.EXPENSE);
    }

    @Test
    @DisplayName("month는 날짜의 연월이다")
    void monthIsYearMonthOfDate() {
        Entry entry = new Expense(LocalDate.of(2026, 10, 31), Category.FOOD, Money.of(12000), "");

        assertThat(entry.month()).isEqualTo(YearMonth.of(2026, 10));
    }

    @Test
    @DisplayName("값이 같은 항목은 같고, 수입과 지출은 값이 같아도 다르다")
    void equalsByValue() {
        Expense expense = new Expense(DATE, Category.FOOD, Money.of(12000), "점심", "회사", "점심");

        assertThat(expense)
                .isEqualTo(new Expense(DATE, Category.FOOD, Money.of("12,000"), "점심", "점심", "회사"))
                .hasSameHashCodeAs(new Expense(DATE, Category.FOOD, Money.of("12,000"), "점심", "점심", "회사"));
        assertThat(new Income(DATE, Category.FOOD, Money.of(12000), "점심", "회사", "점심"))
                .isNotEqualTo(expense);
    }
}
