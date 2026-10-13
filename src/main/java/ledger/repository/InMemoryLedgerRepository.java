package ledger.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import ledger.domain.Entry;

public class InMemoryLedgerRepository implements LedgerRepository {

    private final List<Entry> entries = new ArrayList<>();

    @Override
    public void save(Entry entry) {
        entries.add(Objects.requireNonNull(entry, "entry"));
    }

    @Override
    public List<Entry> findAll() {
        return List.copyOf(entries);
    }
}
