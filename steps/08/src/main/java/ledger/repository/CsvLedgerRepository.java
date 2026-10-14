package ledger.repository;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import ledger.domain.Entry;

public class CsvLedgerRepository implements LedgerRepository {

    public CsvLedgerRepository(Path file) {
        throw new UnsupportedOperationException("TODO: step 08");
    }

    @Override
    public void save(Entry entry) {
        throw new UnsupportedOperationException("TODO: step 08");
    }

    @Override
    public void saveAll(Collection<? extends Entry> entries) {
        throw new UnsupportedOperationException("TODO: step 08");
    }

    @Override
    public List<Entry> findAll() {
        throw new UnsupportedOperationException("TODO: step 08");
    }
}
