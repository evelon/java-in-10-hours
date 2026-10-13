package ledger.domain;

import java.time.YearMonth;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class Ledger {

    public Ledger(Collection<? extends Entry> entries, Collection<Budget> budgets) {
        throw new UnsupportedOperationException("TODO: step 05");
    }

    public List<Entry> entries() {
        throw new UnsupportedOperationException("TODO: step 05");
    }

    public Optional<Budget> budgetOf(Category category) {
        throw new UnsupportedOperationException("TODO: step 05");
    }

    public Money totalIncome(YearMonth month) {
        throw new UnsupportedOperationException("TODO: step 05");
    }

    public Money totalExpense(YearMonth month) {
        throw new UnsupportedOperationException("TODO: step 05");
    }

    public Map<Category, Money> expenseByCategory(YearMonth month) {
        throw new UnsupportedOperationException("TODO: step 05");
    }

    public List<BudgetOverrun> overBudget(YearMonth month) {
        throw new UnsupportedOperationException("TODO: step 05");
    }
}
