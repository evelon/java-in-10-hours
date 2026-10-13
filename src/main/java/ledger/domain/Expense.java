package ledger.domain;

import java.time.LocalDate;
import java.util.Set;

public record Expense(LocalDate date, Category category, Money amount, String memo, Set<String> tags)
        implements Entry {

    public Expense {
        throw new UnsupportedOperationException("TODO: step 04");
    }

    public Expense(LocalDate date, Category category, Money amount, String memo, String... tags) {
        this(date, category, amount, memo, Set.of());
        throw new UnsupportedOperationException("TODO: step 04");
    }
}
