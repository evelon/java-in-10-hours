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
        // sealed라서 default 없이도 모든 경우를 다룬 것이 컴파일 시점에 확인된다
        return switch (this) {
            case Income income -> Type.INCOME;
            case Expense expense -> Type.EXPENSE;
        };
    }

    default YearMonth month() {
        return YearMonth.from(date());
    }

    enum Type { INCOME, EXPENSE }
}
