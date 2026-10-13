package ledger.repository;

import java.time.YearMonth;
import java.util.Collection;
import java.util.List;
import ledger.domain.Entry;

public interface LedgerRepository {
    void save(Entry entry);

    default void saveAll(Collection<? extends Entry> entries) {
        throw new UnsupportedOperationException("TODO: step 07");
    }

    List<Entry> findAll();

    default List<Entry> findByMonth(YearMonth month) {
        throw new UnsupportedOperationException("TODO: step 07");
    }
}
