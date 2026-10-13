package ledger.cli;

import java.io.InputStream;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import java.util.Set;
import ledger.domain.Budget;
import ledger.domain.BudgetOverrun;
import ledger.domain.Category;
import ledger.domain.Entry;
import ledger.domain.InvalidEntryException;
import ledger.domain.Money;
import ledger.repository.BankCsvImporter;
import ledger.repository.ImportResult;
import ledger.service.LedgerService;
import ledger.service.MonthlyReport;
import ledger.service.ReportService;

// 문법은 설계 6절. 입출력과 시간은 주입받는다(테스트에서 고정)
public class Repl {

    private static final String PROMPT = "ledger> ";

    private final Scanner in;
    private final PrintStream out;
    private final Clock clock;
    private final LedgerService ledgerService;
    private final ReportService reportService;
    private final Map<String, Command> commands;
    private boolean running = true;

    public Repl(InputStream in, PrintStream out, Clock clock,
                LedgerService ledgerService, ReportService reportService) {
        this.in = new Scanner(Objects.requireNonNull(in, "in"), StandardCharsets.UTF_8);
        this.out = Objects.requireNonNull(out, "out");
        this.clock = Objects.requireNonNull(clock, "clock");
        this.ledgerService = Objects.requireNonNull(ledgerService, "ledgerService");
        this.reportService = Objects.requireNonNull(reportService, "reportService");
        this.commands = Map.of(
                "budget", this::budget,
                "add", this::add,
                "import", this::importFile,
                "report", this::report,
                "quit", args -> running = false);
    }

    // quit이나 입력 끝(Ctrl-D)까지 명령을 읽는다. 종료 코드를 돌려준다
    public int run() {
        while (running) {
            out.print(PROMPT);
            if (!in.hasNextLine()) {
                out.println();
                break;
            }
            execute(in.nextLine());
        }
        // 저장은 add·import 때 이미 끝났다(write-through). 여기서는 개수만 보고한다
        out.printf("saved %d entries%n", ledgerService.count());
        out.println("bye");
        return 0;
    }

    private void execute(String line) {
        if (line.isBlank()) {
            return;
        }
        List<String> tokens = List.of(line.trim().split("\\s+"));
        Command command = commands.get(tokens.get(0));
        if (command == null) {
            out.println("error: 모르는 명령: " + tokens.get(0));
            return;
        }
        try {
            command.execute(tokens.subList(1, tokens.size()));
        } catch (IllegalArgumentException | InvalidEntryException | UncheckedIOException e) {
            // 사용자 입력 오류는 알리고 다음 명령을 받는다
            out.println("error: " + e.getMessage());
        }
    }

    private void budget(List<String> args) {
        if (wrongArgs(args, 2, 2, "budget <카테고리> <금액>")) {
            return;
        }
        Budget budget = new Budget(Category.fromLabel(args.get(0)), Money.of(args.get(1)));
        ledgerService.setBudget(budget);
        out.printf("budget: %s %s%n", budget.category().label(), budget.limit());
    }

    private void add(List<String> args) {
        if (wrongArgs(args, 4, Integer.MAX_VALUE,
                "add <expense|income> <yyyy-MM-dd|today> <카테고리> <금액> [메모...] [#태그...]")) {
            return;
        }
        Entry.Type type = switch (args.get(0)) {
            case "expense" -> Entry.Type.EXPENSE;
            case "income" -> Entry.Type.INCOME;
            default -> throw new InvalidEntryException("구분 오류: " + args.get(0));
        };
        String date = args.get(1).equals("today") ? LocalDate.now(clock).toString() : args.get(1);
        List<String> memo = new ArrayList<>();
        Set<String> tags = new HashSet<>();
        for (String word : args.subList(4, args.size())) {
            if (word.startsWith("#")) {
                tags.add(word.substring(1));
            } else {
                memo.add(word);
            }
        }
        Entry entry = Entry.parse(type, date, args.get(2), args.get(3), String.join(" ", memo), tags);
        List<BudgetOverrun> overruns = ledgerService.add(entry);
        out.println("added: " + describe(entry));
        printWarnings(overruns);
    }

    private void importFile(List<String> args) {
        if (wrongArgs(args, 1, 1, "import <경로>")) {
            return;
        }
        ImportResult result;
        try {
            result = BankCsvImporter.read(Path.of(args.get(0)));
        } catch (UncheckedIOException e) {
            String reason = e.getCause() instanceof CharacterCodingException ? "UTF-8이 아닌 파일" : "파일 읽기 오류";
            throw new IllegalArgumentException(reason + ": " + args.get(0), e);
        }
        printWarnings(ledgerService.addAll(result.entries()));
        out.printf("imported %d entries, %d errors%n", result.entries().size(), result.errors().size());
        for (ImportResult.RowError error : result.errors()) {
            out.printf("  %d행: %s%n", error.line(), error.reason());
        }
    }

    private void report(List<String> args) {
        if (wrongArgs(args, 1, 1, "report <yyyy-MM>")) {
            return;
        }
        YearMonth month;
        try {
            month = YearMonth.parse(args.get(0));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("월 형식 오류: " + args.get(0), e);
        }
        MonthlyReport report = reportService.report(month);
        out.printf("== %s ==%n", report.month());
        out.printf("수입 %s%n", report.income());
        out.printf("지출 %s%n", report.expense());
        out.printf("순액 %s%n", Money.format(report.net()));
        for (MonthlyReport.CategoryLine line : report.categories()) {
            out.printf("- %s %s (%d%%)%n", line.category().label(), line.amount(), line.percent());
        }
    }

    private boolean wrongArgs(List<String> args, int min, int max, String usage) {
        if (args.size() >= min && args.size() <= max) {
            return false;
        }
        out.println("usage: " + usage);
        return true;
    }

    private void printWarnings(List<BudgetOverrun> overruns) {
        for (BudgetOverrun overrun : overruns) {
            out.printf("warning: %s 예산 초과: %s / %s (%s)%n",
                    overrun.category().label(), overrun.spent(), overrun.limit(), overrun.month());
        }
    }

    // "2026-10-11 식비 12,000원 점심 김밥 #회사". 메모가 없으면 생략, 태그는 사전순
    private static String describe(Entry entry) {
        List<String> parts = new ArrayList<>(List.of(
                entry.date().toString(), entry.category().label(), entry.amount().toString()));
        if (!entry.memo().isEmpty()) {
            parts.add(entry.memo());
        }
        entry.tags().stream().sorted().map(tag -> "#" + tag).forEach(parts::add);
        return String.join(" ", parts);
    }
}
