package ledger.domain;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.Objects;
import java.util.Set;

public sealed interface Entry permits Income, Expense {
    LocalDate date();

    Category category();

    Money amount();

    String memo();

    Set<String> tags();

    default Type type() {
        // sealed라서 default 없이도 모든 경우를 다룬 것이 컴파일 시점에 확인된다
        return switch (this) {
            case Income income -> Type.INCOME;
            case Expense expense -> Type.EXPENSE;
        };
    }

    default YearMonth month() {
        return YearMonth.from(date());
    }

    static Entry parse(Type type, String date, String category,
                       String amount, String memo, Set<String> tags) {
        Objects.requireNonNull(type, "type");
        LocalDate parsedDate;
        try {
            parsedDate = LocalDate.parse(date);
        } catch (DateTimeParseException e) {
            throw new InvalidEntryException("날짜 형식 오류: " + date, e);
        }
        Category parsedCategory;
        Money parsedAmount;
        try {
            parsedCategory = Category.fromLabel(category);
            parsedAmount = Money.of(amount);
        } catch (IllegalArgumentException e) {
            // 값 타입의 IAE를 항목 수준 예외로 바꾸되, 메시지와 원인은 그대로 둔다
            throw new InvalidEntryException(e.getMessage(), e);
        }
        return switch (type) {
            case INCOME -> new Income(parsedDate, parsedCategory, parsedAmount, memo, tags);
            case EXPENSE -> new Expense(parsedDate, parsedCategory, parsedAmount, memo, tags);
        };
    }

    enum Type { INCOME, EXPENSE }
}
