package ledger.domain;

import java.util.Locale;

public final class Money {
    private Money() {
    }

    public static String format(long won) {
        // 기본 Locale에 맡기면 환경에 따라 구분자가 '.'이 될 수 있다
        return String.format(Locale.ROOT, "%,d원", won);
    }
}
