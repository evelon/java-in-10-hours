package ledger.repository;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import ledger.domain.Entry;
import ledger.domain.InvalidEntryException;

// 저장 파일(설계 6절 CSV 형식). 생성할 때 한 번 읽고, 저장할 때마다 파일 끝에 덧붙인다(write-through)
public class CsvLedgerRepository implements LedgerRepository {

    static final String HEADER = "date,type,category,amount,memo,tags";

    private final Path file;
    private final List<Entry> entries = new ArrayList<>();

    public CsvLedgerRepository(Path file) {
        this.file = Objects.requireNonNull(file, "file");
        if (Files.exists(file)) {
            load();
        }
    }

    @Override
    public void save(Entry entry) {
        saveAll(List.of(entry));
    }

    @Override
    public void saveAll(Collection<? extends Entry> newEntries) {
        List<Entry> copy = List.copyOf(newEntries);
        List<String> lines = new ArrayList<>();
        try {
            if (Files.notExists(file) || Files.size(file) == 0) {
                lines.add(HEADER);
            }
            for (Entry entry : copy) {
                lines.add(toLine(entry));
            }
            // 인코딩을 기본값에 맡기지 않는다
            Files.write(file, lines, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        // 파일에 쓴 뒤에만 메모리에 반영한다. 쓰기가 실패하면 둘 다 그대로다
        entries.addAll(copy);
    }

    @Override
    public List<Entry> findAll() {
        return List.copyOf(entries);
    }

    private void load() {
        List<String> lines;
        try {
            lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        if (lines.isEmpty()) {
            return;
        }
        if (!lines.get(0).equals(HEADER)) {
            throw new IllegalArgumentException("헤더 오류: " + lines.get(0));
        }
        // 자기 저장 파일이 깨졌으면 건너뛰지 않고 멈춘다. 행 번호는 헤더가 1행이다
        for (int i = 1; i < lines.size(); i++) {
            try {
                entries.add(parseLine(lines.get(i)));
            } catch (InvalidEntryException e) {
                throw new InvalidEntryException((i + 1) + "행: " + e.getMessage(), e);
            }
        }
    }

    private static String toLine(Entry entry) {
        return String.join(",",
                entry.date().toString(),
                entry.type().name(),
                entry.category().label(),
                entry.amount().amount().toPlainString(),
                entry.memo(),
                String.join(" ", entry.tags().stream().sorted().toList()));
    }

    private static Entry parseLine(String line) {
        // limit -1: 메모·태그가 빈 행도 끝의 빈 필드를 버리지 않는다
        String[] fields = line.split(",", -1);
        if (fields.length != 6) {
            throw new InvalidEntryException("필드 수 오류: " + line);
        }
        Entry.Type type;
        try {
            type = Entry.Type.valueOf(fields[1]);
        } catch (IllegalArgumentException e) {
            throw new InvalidEntryException("구분 오류: " + fields[1], e);
        }
        Set<String> tags = fields[5].isEmpty() ? Set.of() : Set.copyOf(List.of(fields[5].split(" ", -1)));
        return Entry.parse(type, fields[0], fields[2], fields[3], fields[4], tags);
    }
}
