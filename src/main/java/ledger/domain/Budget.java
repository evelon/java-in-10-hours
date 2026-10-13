package ledger.domain;

import java.util.Objects;

public record Budget(Category category, Money limit) {

    public Budget {
        Objects.requireNonNull(category, "category");
        Objects.requireNonNull(limit, "limit");
    }
}
