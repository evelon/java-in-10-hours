package ledger.domain;

import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

// Income과 Expense의 compact constructor가 같이 쓰는 검증
final class EntryRules {

    // CSV는 쉼표로, 태그 목록은 공백으로 나누고, CLI는 '#'로 태그를 알아본다
    private static final Pattern INVALID_TAG = Pattern.compile("|.*[\\s,#].*");

    private EntryRules() {
    }

    static String memo(String memo) {
        Objects.requireNonNull(memo, "memo");
        if (memo.contains(",")) {
            throw new InvalidEntryException("메모에 쉼표 사용 불가: " + memo);
        }
        return memo;
    }

    static Set<String> tags(Set<String> tags) {
        Set<String> copy = Set.copyOf(tags);
        for (String tag : copy) {
            if (INVALID_TAG.matcher(tag).matches()) {
                throw new InvalidEntryException("태그 형식 오류: " + tag);
            }
        }
        return copy;
    }
}
