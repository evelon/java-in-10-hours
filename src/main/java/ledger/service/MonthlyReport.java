package ledger.service;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import ledger.domain.Category;
import ledger.domain.Money;

public record MonthlyReport(YearMonth month, Money income, Money expense, BigDecimal net,
                            List<CategoryLine> categories) {

    public MonthlyReport {
        throw new UnsupportedOperationException("TODO: step 07");
    }

    public record CategoryLine(Category category, Money amount, int percent) {

        public CategoryLine {
            throw new UnsupportedOperationException("TODO: step 07");
        }
    }
}
