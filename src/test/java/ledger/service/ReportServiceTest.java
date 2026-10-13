package ledger.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import ledger.domain.Category;
import ledger.domain.Expense;
import ledger.domain.Income;
import ledger.domain.Money;
import ledger.repository.InMemoryLedgerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ReportServiceTest {

    private static final YearMonth OCTOBER = YearMonth.of(2026, 10);

    private final InMemoryLedgerRepository repository = new InMemoryLedgerRepository();
    private final ReportService service = new ReportService(repository);

    @Test
    @DisplayName("월 리포트는 수입·지출·순액과 지출 카테고리별 금액·비율이다(09 데모 값)")
    void reportsMonth() {
        repository.saveAll(List.of(
                new Income(LocalDate.parse("2026-10-25"), Category.SALARY, Money.of(3_000_000), "월급"),
                expense("2026-10-01", Category.HOUSING, 500_000),
                expense("2026-10-11", Category.FOOD, 312_400),
                expense("2026-10-05", Category.TRANSPORT, 32_900),
                expense("2026-09-30", Category.FOOD, 99_000)));

        assertThat(service.report(OCTOBER)).isEqualTo(new MonthlyReport(
                OCTOBER, Money.of(3_000_000), Money.of(845_300), new BigDecimal("2154700"), List.of(
                        new MonthlyReport.CategoryLine(Category.HOUSING, Money.of(500_000), 59),
                        new MonthlyReport.CategoryLine(Category.FOOD, Money.of(312_400), 37),
                        new MonthlyReport.CategoryLine(Category.TRANSPORT, Money.of(32_900), 4))));
    }

    @Test
    @DisplayName("항목이 없는 달은 모두 0원이고 카테고리 행이 없다")
    void reportsEmptyMonth() {
        assertThat(service.report(OCTOBER)).isEqualTo(
                new MonthlyReport(OCTOBER, Money.ZERO, Money.ZERO, BigDecimal.ZERO, List.of()));
    }

    private static Expense expense(String date, Category category, long won) {
        return new Expense(LocalDate.parse(date), category, Money.of(won), "");
    }
}
