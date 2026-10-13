package ledger.repository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import ledger.domain.Entry;
import ledger.domain.InvalidEntryException;

// 은행 CSV(설계 6절)를 항목으로 읽는다. 저장은 하지 않는다. 잘못된 행은 건너뛰고 (행 번호, 사유)를 모은다
public final class BankCsvImporter {

    static final String HEADER = "거래일,구분,금액,카테고리,적요";

    private BankCsvImporter() {
    }

    public static ImportResult read(Path file) {
        List<Entry> entries = new ArrayList<>();
        List<ImportResult.RowError> errors = new ArrayList<>();
        // UTF-8이 아닌 파일(MS949 등)은 MalformedInputException → UncheckedIOException
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String header = reader.readLine();
            if (!HEADER.equals(header)) {
                throw new IllegalArgumentException("헤더 오류: " + header);
            }
            int lineNumber = 1;
            String line;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) {
                    continue;
                }
                try {
                    entries.add(parseLine(line));
                } catch (InvalidEntryException e) {
                    errors.add(new ImportResult.RowError(lineNumber, e.getMessage()));
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return new ImportResult(entries, errors);
    }

    private static Entry parseLine(String line) {
        // limit 5: 적요(마지막 필드)에 쉼표가 있으면 그대로 남겨 Entry 검증의 "메모에 쉼표 사용 불가"로 보고한다
        String[] fields = line.split(",", 5);
        if (fields.length != 5) {
            throw new InvalidEntryException("필드 수 오류: " + line);
        }
        Entry.Type type = switch (fields[1]) {
            case "입금" -> Entry.Type.INCOME;
            case "출금" -> Entry.Type.EXPENSE;
            default -> throw new InvalidEntryException("구분 오류: " + fields[1]);
        };
        return Entry.parse(type, fields[0], fields[3], fields[2], fields[4], Set.of());
    }
}
