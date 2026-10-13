package ledger.domain;

import java.time.YearMonth;

public record BudgetOverrun(Category category, YearMonth month, Money spent, Money limit) {

    public BudgetOverrun {
        throw new UnsupportedOperationException("TODO: step 05");
    }
}
