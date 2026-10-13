package ledger.service;

import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import ledger.domain.Budget;
import ledger.domain.BudgetOverrun;
import ledger.domain.Category;
import ledger.domain.Entry;
import ledger.domain.Ledger;
import ledger.repository.LedgerRepository;

public class LedgerService {

    private final LedgerRepository repository;
    // 예산은 세션 메모리에만 둔다(설계 6절)
    private final Map<Category, Budget> budgets = new EnumMap<>(Category.class);

    public LedgerService(LedgerRepository repository) {
        this.repository = Objects.requireNonNull(repository, "repository");
    }

    public void setBudget(Budget budget) {
        budgets.put(budget.category(), budget);
    }

    public List<BudgetOverrun> add(Entry entry) {
        return addAll(List.of(entry));
    }

    // 저장한 뒤, 이번에 추가한 지출이 걸린 (카테고리, 월) 중 예산을 넘은 것을 돌려준다
    public List<BudgetOverrun> addAll(Collection<? extends Entry> entries) {
        List<Entry> added = List.copyOf(entries);
        repository.saveAll(added);

        List<Entry> expenses = added.stream().filter(e -> e.type() == Entry.Type.EXPENSE).toList();
        Ledger ledger = new Ledger(repository.findAll(), budgets.values());
        return expenses.stream()
                .map(Entry::month)
                .distinct()
                .sorted()
                .flatMap(month -> ledger.overBudget(month).stream())
                .filter(overrun -> expenses.stream().anyMatch(e ->
                        e.month().equals(overrun.month()) && e.category() == overrun.category()))
                .toList();
    }
}
