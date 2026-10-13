package ledger.domain;

public record Budget(Category category, Money limit) {

    public Budget {
        throw new UnsupportedOperationException("TODO: step 05");
    }
}
