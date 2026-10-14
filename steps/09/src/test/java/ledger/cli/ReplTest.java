package ledger.cli;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import ledger.repository.CsvLedgerRepository;
import ledger.repository.InMemoryLedgerRepository;
import ledger.repository.LedgerRepository;
import ledger.service.LedgerService;
import ledger.service.ReportService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ReplTest {

    private static final String PROMPT = "ledger> ";
    // 서울 기준 2026-10-11 12:00. today가 이 날짜가 된다
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-11T03:00:00Z"), ZoneId.of("Asia/Seoul"));

    @TempDir
    Path dir;

    private String rawOutput;
    private int exitCode;

    // 입력 줄들로 세션을 돌리고, 프롬프트를 지운 출력 줄을 돌려준다(입력은 화면에 다시 찍히지 않는다)
    private List<String> run(LedgerRepository repository, String... inputLines) {
        ByteArrayInputStream in = new ByteArrayInputStream(
                (String.join("\n", inputLines) + "\n").getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(bytes, true, StandardCharsets.UTF_8);

        exitCode = new Repl(in, out, CLOCK, new LedgerService(repository), new ReportService(repository)).run();

        rawOutput = bytes.toString(StandardCharsets.UTF_8);
        return rawOutput.replace(PROMPT, "").lines().toList();
    }

    private List<String> run(String... inputLines) {
        return run(new InMemoryLedgerRepository(), inputLines);
    }

    @Test
    @DisplayName("설계 7절 09의 데모를 빈 저장 파일에서 그대로 재현하고, 42건이 파일에 남는다")
    void reproducesDemo() {
        Path file = dir.resolve("ledger.csv");

        List<String> output = run(new CsvLedgerRepository(file),
                "budget 식비 300000",
                "add expense today 식비 12,000 점심 김밥 #회사",
                "import samples/bank.csv",
                "report 2026-10",
                "quit");

        assertThat(output).containsExactly(
                "budget: 식비 300,000원",
                "added: 2026-10-11 식비 12,000원 점심 김밥 #회사",
                "warning: 식비 예산 초과: 312,400원 / 300,000원 (2026-10)",
                "imported 41 entries, 1 errors",
                "  17행: 날짜 형식 오류: 2026.10.11",
                "== 2026-10 ==",
                "수입 3,000,000원",
                "지출 845,300원",
                "순액 2,154,700원",
                "- 주거 500,000원 (59%)",
                "- 식비 312,400원 (37%)",
                "- 교통 32,900원 (4%)",
                "saved 42 entries",
                "bye");
        assertThat(rawOutput).startsWith(PROMPT);
        assertThat(exitCode).isZero();
        assertThat(new CsvLedgerRepository(file).findAll()).hasSize(42);
    }

    @Test
    @DisplayName("add는 날짜를 직접 받고, 메모가 없으면 생략하며, 태그는 중복 없이 사전순으로 출력한다")
    void addsEntries() {
        List<String> output = run(
                "add income 2026-10-25 급여 3000000",
                "add expense 2026-10-12 식비 9000 저녁 국밥 #회사 #야근 #회사",
                "quit");

        assertThat(output).containsExactly(
                "added: 2026-10-25 급여 3,000,000원",
                "added: 2026-10-12 식비 9,000원 저녁 국밥 #야근 #회사",
                "saved 2 entries",
                "bye");
    }

    @Test
    @DisplayName("add로 예산을 넘으면 added 다음 줄에 경고한다")
    void warnsAfterAdd() {
        List<String> output = run(
                "budget 교통 10000",
                "add expense today 교통 12300 택시",
                "quit");

        assertThat(output).containsExactly(
                "budget: 교통 10,000원",
                "added: 2026-10-11 교통 12,300원 택시",
                "warning: 교통 예산 초과: 12,300원 / 10,000원 (2026-10)",
                "saved 1 entries",
                "bye");
    }

    @Test
    @DisplayName("지출만 있으면 순액은 음수이고, 항목이 없는 달은 0원이다")
    void reportsNegativeAndEmptyMonths() {
        List<String> output = run(
                "add expense today 식비 12000",
                "report 2026-10",
                "report 2026-11",
                "quit");

        assertThat(output).containsExactly(
                "added: 2026-10-11 식비 12,000원",
                "== 2026-10 ==",
                "수입 0원",
                "지출 12,000원",
                "순액 -12,000원",
                "- 식비 12,000원 (100%)",
                "== 2026-11 ==",
                "수입 0원",
                "지출 0원",
                "순액 0원",
                "saved 1 entries",
                "bye");
    }

    @Test
    @DisplayName("잘못된 입력은 한 줄로 알리고 다음 명령을 받는다. 아무것도 저장하지 않는다")
    void reportsErrorsAndKeepsRunning() throws IOException {
        Path ms949 = dir.resolve("ms949.csv");
        Files.write(ms949, List.of("거래일,구분,금액,카테고리,적요"), Charset.forName("MS949"));

        List<String> output = run(
                "hello",
                "",
                "add expense today 식비",
                "add spend today 식비 12000",
                "add expense 2026.10.11 식비 12000",
                "add expense today 식비 1,2,3",
                "add expense today 식비 12000 #회#사",
                "budget 간식 1000",
                "report 2026.10",
                "import nowhere.csv",
                "import " + ms949,
                "quit");

        assertThat(output).containsExactly(
                "error: 모르는 명령: hello",
                "usage: add <expense|income> <yyyy-MM-dd|today> <카테고리> <금액> [메모...] [#태그...]",
                "error: 구분 오류: spend",
                "error: 날짜 형식 오류: 2026.10.11",
                "error: 금액 형식 오류: 1,2,3",
                "error: 태그 형식 오류: 회#사",
                "error: 모르는 카테고리: 간식",
                "error: 월 형식 오류: 2026.10",
                "error: 파일 읽기 오류: nowhere.csv",
                "error: UTF-8이 아닌 파일: " + ms949,
                "saved 0 entries",
                "bye");
    }

    @Test
    @DisplayName("quit 없이 입력이 끝나도(Ctrl-D) quit처럼 보고하고 종료 코드 0이다")
    void endOfInputActsLikeQuit() {
        List<String> output = run("add expense today 식비 12000");

        assertThat(output).endsWith("saved 1 entries", "bye");
        assertThat(exitCode).isZero();
    }
}
