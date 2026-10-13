package ledger;

import ledger.domain.Money;
import org.apache.commons.lang3.StringUtils;

public class Main {
    public static void main(String[] args) {
        Money total = Money.of(0);
        for (String arg : args) {
            total = total.plus(Money.of(arg));
        }

        int width = total.toString().length();
        for (String arg : args) {
            System.out.println("  " + StringUtils.leftPad(Money.of(arg).toString(), width));
        }
        System.out.println("= " + total);
    }
}
