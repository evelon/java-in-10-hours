package ledger.domain;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Set;

public sealed interface Entry permits Income, Expense {
    LocalDate date();

    Category category();

    Money amount();

    String memo();

    Set<String> tags();

    default Type type() {
        throw new UnsupportedOperationException("TODO: step 04");
    }

    default YearMonth month() {
        throw new UnsupportedOperationException("TODO: step 04");
    }

    enum Type { INCOME, EXPENSE }
}
