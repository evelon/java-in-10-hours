package ledger.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public record Expense(LocalDate date, Category category, Money amount, String memo, Set<String> tags)
        implements Entry {

    public Expense {
        Objects.requireNonNull(date, "date");
        Objects.requireNonNull(category, "category");
        Objects.requireNonNull(amount, "amount");
        memo = EntryRules.memo(memo);
        tags = EntryRules.tags(tags);
    }

    public Expense(LocalDate date, Category category, Money amount, String memo, String... tags) {
        // Set.of는 중복을 거부하므로 List를 거쳐 copyOf로 합친다
        this(date, category, amount, memo, Set.copyOf(List.of(tags)));
    }
}
