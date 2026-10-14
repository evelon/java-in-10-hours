package ledger.service;

import java.util.Collection;
import java.util.List;
import ledger.domain.Budget;
import ledger.domain.BudgetOverrun;
import ledger.domain.Entry;
import ledger.repository.LedgerRepository;

public class LedgerService {

    public LedgerService(LedgerRepository repository) {
        throw new UnsupportedOperationException("TODO: step 07");
    }

    public void setBudget(Budget budget) {
        throw new UnsupportedOperationException("TODO: step 07");
    }

    public List<BudgetOverrun> add(Entry entry) {
        throw new UnsupportedOperationException("TODO: step 07");
    }

    public List<BudgetOverrun> addAll(Collection<? extends Entry> entries) {
        throw new UnsupportedOperationException("TODO: step 07");
    }
}
