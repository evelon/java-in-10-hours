package ledger.repository;

import java.util.List;
import ledger.domain.Entry;

public class InMemoryLedgerRepository implements LedgerRepository {

    @Override
    public void save(Entry entry) {
        throw new UnsupportedOperationException("TODO: step 07");
    }

    @Override
    public List<Entry> findAll() {
        throw new UnsupportedOperationException("TODO: step 07");
    }
}
