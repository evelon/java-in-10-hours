package ledger.domain;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class Ledger {

    private final List<Entry> entries;
    // EnumMap은 Category 선언 순으로 순회한다
    private final Map<Category, Budget> budgets = new EnumMap<>(Category.class);

    public Ledger(Collection<? extends Entry> entries, Collection<Budget> budgets) {
        this.entries = List.copyOf(entries);
        for (Budget budget : budgets) {
            this.budgets.put(budget.category(), budget);
        }
    }

    public List<Entry> entries() {
        return entries;
    }

    public Optional<Budget> budgetOf(Category category) {
        return Optional.ofNullable(budgets.get(category));
    }

    public Money totalIncome(YearMonth month) {
        return total(month, Entry.Type.INCOME);
    }

    public Money totalExpense(YearMonth month) {
        return total(month, Entry.Type.EXPENSE);
    }

    public Map<Category, Money> expenseByCategory(YearMonth month) {
        Map<Category, Money> sums = entriesOf(month, Entry.Type.EXPENSE)
                .collect(Collectors.groupingBy(Entry::category,
                        Collectors.reducing(Money.ZERO, Entry::amount, Money::plus)));

        // Map.copyOf는 순서를 지키지 않으므로 LinkedHashMap을 불변 뷰로 감싼다
        Map<Category, Money> sorted = new LinkedHashMap<>();
        sums.entrySet().stream()
                .sorted(Map.Entry.<Category, Money>comparingByValue(Comparator.reverseOrder())
                        .thenComparing(Map.Entry.comparingByKey()))
                .forEach(e -> sorted.put(e.getKey(), e.getValue()));
        return Collections.unmodifiableMap(sorted);
    }

    public List<BudgetOverrun> overBudget(YearMonth month) {
        Map<Category, Money> spentByCategory = expenseByCategory(month);
        List<BudgetOverrun> overruns = new ArrayList<>();
        for (Budget budget : budgets.values()) {
            Money spent = spentByCategory.getOrDefault(budget.category(), Money.ZERO);
            if (spent.compareTo(budget.limit()) > 0) {
                overruns.add(new BudgetOverrun(budget.category(), month, spent, budget.limit()));
            }
        }
        return List.copyOf(overruns);
    }

    private Money total(YearMonth month, Entry.Type type) {
        return entriesOf(month, type).map(Entry::amount).reduce(Money.ZERO, Money::plus);
    }

    private Stream<Entry> entriesOf(YearMonth month, Entry.Type type) {
        return entries.stream().filter(e -> e.type() == type && e.month().equals(month));
    }
}
