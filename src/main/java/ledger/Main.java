package ledger;

import ledger.domain.Money;
import org.apache.commons.lang3.StringUtils;

public class Main {
    public static void main(String[] args) {
        long total = 0;
        for (String arg : args) {
            total += Long.parseLong(arg);
        }

        int width = Money.format(total).length();
        for (String arg : args) {
            System.out.println("  " + StringUtils.leftPad(Money.format(Long.parseLong(arg)), width));
        }
        System.out.println("= " + Money.format(total));
    }
}
