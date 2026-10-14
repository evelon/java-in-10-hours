package ledger.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class EntryValidationTest {

    private static final LocalDate DATE = LocalDate.of(2026, 10, 11);

    @Test
    @DisplayName("parse는 문자열 입력으로 수입·지출 항목을 만든다")
    void parsesValidInput() {
        Entry expense = Entry.parse(Entry.Type.EXPENSE, "2026-10-11", "식비", "12,000", "점심 김밥", Set.of("회사"));
        Entry income = Entry.parse(Entry.Type.INCOME, "2026-10-25", "급여", "3000000", "", Set.of());

        assertThat(expense).isEqualTo(new Expense(DATE, Category.FOOD, Money.of(12000), "점심 김밥", "회사"));
        assertThat(income).isEqualTo(new Income(LocalDate.of(2026, 10, 25), Category.SALARY, Money.of(3000000), ""));
    }

    @ParameterizedTest
    @ValueSource(strings = {"2026.10.11", "2026-02-30", "26-10-11"})
    @DisplayName("날짜 형식이 틀리면 원인 예외를 보존한 InvalidEntryException이 난다")
    void rejectsInvalidDate(String date) {
        assertThatThrownBy(() -> Entry.parse(Entry.Type.EXPENSE, date, "식비", "12000", "", Set.of()))
                .isInstanceOf(InvalidEntryException.class)
                .hasMessage("날짜 형식 오류: " + date)
                .hasCauseInstanceOf(DateTimeParseException.class);
    }

    @Test
    @DisplayName("모르는 카테고리는 IAE를 원인으로 보존한 InvalidEntryException이다")
    void rejectsUnknownCategory() {
        assertThatThrownBy(() -> Entry.parse(Entry.Type.EXPENSE, "2026-10-11", "간식", "12000", "", Set.of()))
                .isInstanceOf(InvalidEntryException.class)
                .hasMessage("모르는 카테고리: 간식")
                .hasCauseInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "-1    | 음수 금액: -1",
            "1.5   | 원 미만 금액: 1.5",
            "abc   | 금액 형식 오류: abc",
            "1,2,3 | 금액 형식 오류: 1,2,3",
            "1e3   | 금액 형식 오류: 1e3"})
    @DisplayName("잘못된 금액은 IAE 메시지를 그대로 쓴 InvalidEntryException이다")
    void rejectsInvalidAmount(String amount, String message) {
        assertThatThrownBy(() -> Entry.parse(Entry.Type.EXPENSE, "2026-10-11", "식비", amount, "", Set.of()))
                .isInstanceOf(InvalidEntryException.class)
                .hasMessage(message)
                .hasCauseInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("메모에 쉼표가 있으면 생성자와 parse 모두 거부한다")
    void rejectsCommaInMemo() {
        assertThatThrownBy(() -> new Expense(DATE, Category.FOOD, Money.of(12000), "점심,김밥"))
                .isInstanceOf(InvalidEntryException.class)
                .hasMessage("메모에 쉼표 사용 불가: 점심,김밥");
        assertThatThrownBy(() -> Entry.parse(Entry.Type.INCOME, "2026-10-11", "급여", "1000", "점심,김밥", Set.of()))
                .isInstanceOf(InvalidEntryException.class)
                .hasMessage("메모에 쉼표 사용 불가: 점심,김밥");
    }

    @ParameterizedTest
    @ValueSource(strings = {"회 사", "", "#회사", "회,사"})
    @DisplayName("태그가 비어 있거나 공백·쉼표·#이 있으면 거부한다")
    void rejectsInvalidTag(String tag) {
        assertThatThrownBy(() -> new Income(DATE, Category.SALARY, Money.of(1000), "", "월급", tag))
                .isInstanceOf(InvalidEntryException.class)
                .hasMessage("태그 형식 오류: " + tag);
    }
}
