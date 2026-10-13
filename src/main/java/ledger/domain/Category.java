package ledger.domain;

import java.util.Objects;

public enum Category {
    FOOD("식비"), HOUSING("주거"), TRANSPORT("교통"), SALARY("급여"), ETC("기타");

    private final String label;

    Category(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public static Category fromLabel(String label) {
        Objects.requireNonNull(label, "label");
        for (Category category : values()) {
            if (category.label.equals(label)) {
                return category;
            }
        }
        throw new IllegalArgumentException("모르는 카테고리: " + label);
    }
}
