package ledger.domain;

public enum Category {
    FOOD("식비"), HOUSING("주거"), TRANSPORT("교통"), SALARY("급여"), ETC("기타");

    Category(String label) {
        // TODO: step 04
    }

    public String label() {
        throw new UnsupportedOperationException("TODO: step 04");
    }

    public static Category fromLabel(String label) {
        throw new UnsupportedOperationException("TODO: step 04");
    }
}
