package ledger;

import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.time.Clock;
import ledger.cli.Repl;
import ledger.domain.InvalidEntryException;
import ledger.repository.CsvLedgerRepository;
import ledger.repository.LedgerRepository;
import ledger.service.LedgerService;
import ledger.service.ReportService;

// 객체 조립(composition root): repository → service → Repl
public class Main {
    public static void main(String[] args) {
        Path file = Path.of(args.length > 0 ? args[0] : "ledger.csv");
        LedgerRepository repository;
        try {
            repository = new CsvLedgerRepository(file);
        } catch (IllegalArgumentException | InvalidEntryException | UncheckedIOException e) {
            System.err.println("error: " + file + ": " + e.getMessage());
            System.exit(1);
            return;
        }
        Repl repl = new Repl(System.in, System.out, Clock.systemDefaultZone(),
                new LedgerService(repository), new ReportService(repository));
        System.exit(repl.run());
    }
}
