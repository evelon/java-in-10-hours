package ledger.service;

import java.time.YearMonth;
import java.util.List;
import java.util.Objects;
import ledger.domain.Ledger;
import ledger.domain.Money;
import ledger.repository.LedgerRepository;

public class ReportService {

    private final LedgerRepository repository;

    public ReportService(LedgerRepository repository) {
        this.repository = Objects.requireNonNull(repository, "repository");
    }

    public MonthlyReport report(YearMonth month) {
        Ledger ledger = new Ledger(repository.findByMonth(month), List.of());
        Money income = ledger.totalIncome(month);
        Money expense = ledger.totalExpense(month);
        // 카테고리 행이 있으면 지출 합계는 0원이 아니므로 percentOf가 실패하지 않는다
        List<MonthlyReport.CategoryLine> lines = ledger.expenseByCategory(month).entrySet().stream()
                .map(e -> new MonthlyReport.CategoryLine(e.getKey(), e.getValue(), e.getValue().percentOf(expense)))
                .toList();
        return new MonthlyReport(month, income, expense, income.difference(expense), lines);
    }
}
