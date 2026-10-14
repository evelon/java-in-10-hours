package ledger.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import ledger.domain.Category;
import ledger.domain.Entry;
import ledger.domain.Expense;
import ledger.domain.Income;
import ledger.domain.InvalidEntryException;
import ledger.domain.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

// 07의 계약 테스트 5개를 상속하고, 파일 형식과 다시 읽기를 추가로 검증한다
class CsvLedgerRepositoryTest extends LedgerRepositoryContractTest {

    private static final Entry LUNCH = new Expense(LocalDate.of(2026, 10, 11), Category.FOOD, Money.of(12000), "점심 김밥", "회사", "야근");
    private static final Entry RENT = new Expense(LocalDate.of(2026, 10, 1), Category.HOUSING, Money.of(500000), "");
    private static final Entry SALARY = new Income(LocalDate.of(2026, 10, 25), Category.SALARY, Money.of(3000000), "월급");

    @TempDir
    Path dir;

    private Path file() {
        return dir.resolve("ledger.csv");
    }

    @Override
    protected LedgerRepository createRepository() {
        return new CsvLedgerRepository(file());
    }

    @Test
    @DisplayName("저장한 항목을 새 저장소로 다시 읽으면 같은 항목이 같은 순서로 나온다")
    void reloadsSavedEntries() {
        LedgerRepository repository = new CsvLedgerRepository(file());
        repository.save(LUNCH);
        repository.saveAll(List.of(RENT, SALARY));

        assertThat(new CsvLedgerRepository(file()).findAll()).containsExactly(LUNCH, RENT, SALARY);
    }

    @Test
    @DisplayName("파일은 UTF-8 CSV이고, 금액은 쉼표 없는 정수, 태그는 사전순 공백 구분이다")
    void writesCsvFormat() throws IOException {
        new CsvLedgerRepository(file()).saveAll(List.of(LUNCH, RENT, SALARY));

        assertThat(Files.readAllLines(file(), StandardCharsets.UTF_8)).containsExactly(
                "date,type,category,amount,memo,tags",
                "2026-10-11,EXPENSE,식비,12000,점심 김밥,야근 회사",
                "2026-10-01,EXPENSE,주거,500000,,",
                "2026-10-25,INCOME,급여,3000000,월급,");
    }

    @Test
    @DisplayName("파일이 없으면 비어 있고, 기존 파일에는 헤더를 다시 쓰지 않고 이어 쓴다")
    void appendsToExistingFile() throws IOException {
        assertThat(new CsvLedgerRepository(file()).findAll()).isEmpty();
        assertThat(file()).doesNotExist();

        new CsvLedgerRepository(file()).save(RENT);
        LedgerRepository reopened = new CsvLedgerRepository(file());
        reopened.save(SALARY);

        assertThat(reopened.findAll()).containsExactly(RENT, SALARY);
        assertThat(Files.readAllLines(file(), StandardCharsets.UTF_8))
                .filteredOn(line -> line.startsWith("date,"))
                .hasSize(1);
    }

    @Test
    @DisplayName("저장 파일에 잘못된 행이 있으면 행 번호를 붙인 InvalidEntryException으로 멈춘다")
    void rejectsBrokenFile() throws IOException {
        Files.write(file(), List.of(
                "date,type,category,amount,memo,tags",
                "2026-10-01,EXPENSE,주거,500000,,",
                "2026.10.11,EXPENSE,식비,12000,,"), StandardCharsets.UTF_8);

        assertThatThrownBy(() -> new CsvLedgerRepository(file()))
                .isInstanceOf(InvalidEntryException.class)
                .hasMessage("3행: 날짜 형식 오류: 2026.10.11");
    }
}
