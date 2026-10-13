package ledger.service;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Objects;
import ledger.domain.Category;
import ledger.domain.Money;

// net은 음수가 될 수 있어 Money가 아니다. 출력은 Money.format(net)
public record MonthlyReport(YearMonth month, Money income, Money expense, BigDecimal net,
                            List<CategoryLine> categories) {

    public MonthlyReport {
        Objects.requireNonNull(month, "month");
        Objects.requireNonNull(income, "income");
        Objects.requireNonNull(expense, "expense");
        Objects.requireNonNull(net, "net");
        categories = List.copyOf(categories);
    }

    // 지출 카테고리 한 행. percent는 지출 합계 대비 정수 %(HALF_UP)
    public record CategoryLine(Category category, Money amount, int percent) {

        public CategoryLine {
            Objects.requireNonNull(category, "category");
            Objects.requireNonNull(amount, "amount");
        }
    }
}
