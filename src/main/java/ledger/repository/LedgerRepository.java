package ledger.repository;

import java.time.YearMonth;
import java.util.Collection;
import java.util.List;
import ledger.domain.Entry;

// 계약: 저장한 순서대로 돌려주고, 중복을 검사하지 않으며, 돌려준 리스트는 바꿀 수 없다
public interface LedgerRepository {
    void save(Entry entry);

    default void saveAll(Collection<? extends Entry> entries) {
        for (Entry entry : entries) {
            save(entry);
        }
    }

    List<Entry> findAll();

    default List<Entry> findByMonth(YearMonth month) {
        // Stream.toList()는 바꿀 수 없는 리스트를 돌려준다
        return findAll().stream().filter(entry -> entry.month().equals(month)).toList();
    }
}
