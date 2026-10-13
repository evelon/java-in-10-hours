package ledger.domain;

import java.time.YearMonth;
import java.util.Objects;

public record BudgetOverrun(Category category, YearMonth month, Money spent, Money limit) {

    public BudgetOverrun {
        Objects.requireNonNull(category, "category");
        Objects.requireNonNull(month, "month");
        Objects.requireNonNull(spent, "spent");
        Objects.requireNonNull(limit, "limit");
    }
}
