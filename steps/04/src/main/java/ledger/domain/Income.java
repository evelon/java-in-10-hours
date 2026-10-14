package ledger.domain;

import java.time.LocalDate;
import java.util.Set;

public record Income(LocalDate date, Category category, Money amount, String memo, Set<String> tags)
        implements Entry {

    public Income {
        throw new UnsupportedOperationException("TODO: step 04");
    }

    public Income(LocalDate date, Category category, Money amount, String memo, String... tags) {
        this(date, category, amount, memo, Set.of());
        throw new UnsupportedOperationException("TODO: step 04");
    }
}
