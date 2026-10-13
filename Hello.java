public class Hello {
    public static void main(String[] args) {
        if (args.length != 3) {
            System.err.println("usage: java -jar hello.jar <원금> <연이율%> <개월>");
            System.exit(1);
        }

        long balance = Long.parseLong(args[0]);
        int ratePercent = Integer.parseInt(args[1]);
        int months = Integer.parseInt(args[2]);

        for (int month = 1; month <= months; month++) {
            // 월 이자 = 잔액 * 연이율% / 12 / 100, 원 미만은 정수 나눗셈으로 버린다
            balance += balance * ratePercent / 1200;
            System.out.println(month + " " + balance);
        }
    }
}
