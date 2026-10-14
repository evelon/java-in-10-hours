package ledger.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import ledger.domain.Category;
import ledger.domain.Expense;
import ledger.domain.Income;
import ledger.domain.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class BankCsvImporterTest {

    private static final String HEADER = "거래일,구분,금액,카테고리,적요";

    @TempDir
    Path dir;

    private Path write(Charset charset, String... lines) throws IOException {
        Path file = dir.resolve("bank.csv");
        Files.write(file, List.of(lines), charset);
        return file;
    }

    @Test
    @DisplayName("입금은 Income, 출금은 Expense로 파일 순서대로 읽고 태그는 비어 있다")
    void readsValidRows() throws IOException {
        Path file = write(StandardCharsets.UTF_8, HEADER,
                "2026-10-25,입금,3000000,급여,월급",
                "2026-10-01,출금,500000,주거,");

        ImportResult result = BankCsvImporter.read(file);

        assertThat(result.entries()).containsExactly(
                new Income(LocalDate.of(2026, 10, 25), Category.SALARY, Money.of(3000000), "월급"),
                new Expense(LocalDate.of(2026, 10, 1), Category.HOUSING, Money.of(500000), ""));
        assertThat(result.errors()).isEmpty();
    }

    @Test
    @DisplayName("잘못된 행은 건너뛰고 (행 번호, 사유)를 모은다. 행 번호는 헤더가 1행이다")
    void collectsInvalidRows() throws IOException {
        Path file = write(StandardCharsets.UTF_8, HEADER,
                "2026-10-03,출금,4500,식비,커피",
                "2026.10.11,출금,8000,식비,점심",
                "2026-10-12,출금,3000,간식,과자",
                "2026-10-13,출금,9000,식비,점심,김밥",
                "2026-10-14,환불,9000,식비,",
                "2026-10-15,출금,9000",
                "2026-10-16,출금,1450,교통,버스");

        ImportResult result = BankCsvImporter.read(file);

        assertThat(result.entries()).extracting(entry -> entry.memo()).containsExactly("커피", "버스");
        assertThat(result.errors()).containsExactly(
                new ImportResult.RowError(3, "날짜 형식 오류: 2026.10.11"),
                new ImportResult.RowError(4, "모르는 카테고리: 간식"),
                new ImportResult.RowError(5, "메모에 쉼표 사용 불가: 점심,김밥"),
                new ImportResult.RowError(6, "구분 오류: 환불"),
                new ImportResult.RowError(7, "필드 수 오류: 2026-10-15,출금,9000"));
    }

    @Test
    @DisplayName("헤더가 다르면 IAE다")
    void rejectsWrongHeader() throws IOException {
        Path file = write(StandardCharsets.UTF_8, "date,type,category,amount,memo,tags");

        assertThatThrownBy(() -> BankCsvImporter.read(file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("헤더 오류: date,type,category,amount,memo,tags");
    }

    @Test
    @DisplayName("UTF-8이 아닌 파일(MS949)은 기본값으로 대충 읽지 않고 UncheckedIOException이다")
    void rejectsNonUtf8File() throws IOException {
        Path file = write(Charset.forName("MS949"), HEADER, "2026-10-03,출금,4500,식비,커피");

        assertThatThrownBy(() -> BankCsvImporter.read(file))
                .isInstanceOf(UncheckedIOException.class)
                .hasCauseInstanceOf(CharacterCodingException.class);
    }
}
