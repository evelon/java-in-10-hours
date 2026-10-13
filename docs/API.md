# 공개 API

주어진 테스트가 의존하는 공개 시그니처(설계 11절 4번).

- `Money`, `Entry`, `Ledger`, `LedgerRepository`와 이들의 시그니처에 등장하는 타입(`Category`, `Budget`, `BudgetOverrun`, `InvalidEntryException`)은 아래로 **미리 고정**한다. 각 메서드에 적힌 단계가 그 메서드를 처음 구현하는 단계다. 그 단계의 주어진 테스트가 이 시그니처에 의존한다.
- 고정한 시그니처는 바꾸지 않는다. 바꿔야 하면 영향받는 단계를 모두 고치고 태그를 다시 찍는다(11절 4번). 이후 단계에서 메서드를 **추가**하는 것은 허용한다.
- service(`LedgerService`, `ReportService`), `CsvLedgerRepository`, 은행 CSV 임포트, CLI는 11절 4번대로 각 도입 단계(07·08·09)에서 이 문서에 추가한다.

## 공통 규칙

- 패키지: `Money`부터 `InvalidEntryException`까지는 `ledger.domain`, `LedgerRepository`는 `ledger.repository`.
- `null` 인자는 `NullPointerException`이다(`Objects.requireNonNull`). 따로 테스트하지 않는다.
- 예외 정책
  - 값 타입(`Money`, `Category`)의 검증 실패는 `IllegalArgumentException`이다.
  - 항목(`Entry`) 수준의 검증 실패는 06부터 `InvalidEntryException`이다. 하위 예외가 있으면 cause로 보존한다.
- 예외 메시지는 `<사유>: <값>` 형식이다. 09 데모의 `17행: 날짜 형식 오류: 2026.10.11`은 이 메시지 앞에 행 번호를 붙인 것이다. 테스트가 그대로 검증하는 메시지는 아래와 같다.

| 상황 | 메시지 | 예외 | 단계 |
|---|---|---|---|
| 음수 금액 | `음수 금액: -1` | IAE | 03 |
| 원 미만 금액 | `원 미만 금액: 1.5` | IAE | 03 |
| 숫자가 아닌 금액 | `금액 형식 오류: abc` | IAE | 03 |
| 모르는 카테고리 | `모르는 카테고리: 간식` | IAE | 04 |
| 날짜 형식 | `날짜 형식 오류: 2026.10.11` | `InvalidEntryException` | 06 |
| 메모의 쉼표 | `메모에 쉼표 사용 불가: 점심,김밥` | `InvalidEntryException` | 06 |
| 잘못된 태그 | `태그 형식 오류: 회 사` | `InvalidEntryException` | 06 |

`Entry.parse`는 IAE 메시지를 그대로 `InvalidEntryException`의 메시지로 쓰고, IAE를 cause로 둔다.

## `Money` (03 도입, 04에서 record로 전환)

```java
public record Money(BigDecimal amount) implements Comparable<Money> {   // 03은 final class, 04부터 record
    public static final Money ZERO;                  // 05

    public Money                                     // 03 (04부터 compact constructor) null·음수·원 미만 거부, scale 0으로 정규화
    public static Money of(long won)                 // 03
    public static Money of(String text)              // 03 "12000", "12,000"
    public BigDecimal amount()                       // 03 scale 0
    public Money plus(Money other)                   // 03
    public Money minus(Money other)                  // 03 결과가 음수면 IAE
    public BigDecimal difference(Money other)        // 05 this - other, 음수 가능(설계 6절의 "차액 계산 메서드")
    public int percentOf(Money whole)                // 05 this / whole * 100, 정수 HALF_UP. whole이 0원이면 IAE
    public static String format(BigDecimal won)      // 05 "12,000원", "-12,000원". toString과 리포트 순액이 같이 쓴다
    public int compareTo(Money other)                // 03
    public String toString()                         // 03 "12,000원", "0원"
    // equals·hashCode: 03은 직접 작성, 04부터 record가 생성
}
```

- `percentOf`의 예: 09 데모의 `500,000원 / 845,300원` → `59`, `312,400원` → `37`, `32,900원` → `4`.
- CSV에 쓸 때는 `amount().toPlainString()`(쉼표 없는 정수)을 쓴다.

## `Category` (04)

```java
public enum Category {
    FOOD("식비"), HOUSING("주거"), TRANSPORT("교통"), SALARY("급여"), ETC("기타");

    public String label()                            // 04
    public static Category fromLabel(String label)   // 04 모르는 라벨은 IAE
}
```

출력과 두 CSV 형식의 `category` 필드에는 모두 라벨(`식비`)을 쓴다.

## `Entry`, `Income`, `Expense` (04)

```java
public sealed interface Entry permits Income, Expense {
    LocalDate date();
    Category category();
    Money amount();
    String memo();                                   // 메모가 없으면 "" (null 아님)
    Set<String> tags();                              // 불변. '#' 없이 저장한다

    default Type type()                              // 04 switch 패턴 매칭으로 구현
    default YearMonth month()                        // 04 YearMonth.from(date())

    static Entry parse(Type type, String date, String category,
                       String amount, String memo, Set<String> tags)   // 06
        // date "yyyy-MM-dd", category 라벨, amount "12000"·"12,000"
        // 실패하면 InvalidEntryException(위 메시지 표)

    enum Type { INCOME, EXPENSE }                    // 04 저장 CSV의 type 값과 같다
}

public record Income(LocalDate date, Category category, Money amount,
                     String memo, Set<String> tags) implements Entry {
    public Income                                    // 04 compact: null 검사, tags = Set.copyOf(tags). 06: 메모·태그 검증
    public Income(LocalDate date, Category category, Money amount,
                  String memo, String... tags)       // 04 varargs. 중복 태그는 하나로 합친다
}

public record Expense(...)                           // Income과 같은 구성
```

- 06의 검증: 메모에 쉼표가 있으면 오류. 태그는 비어 있지 않아야 하고 공백·쉼표·`#`이 있으면 오류.
- `tags()`의 순회 순서는 보장하지 않는다. 출력과 CSV 저장에서는 사전순으로 정렬한다.
- `record`이므로 같은 값의 `Income`은 같다(`equals`). 저장 후 다시 읽은 항목과의 비교에 쓴다.

## `Budget`, `BudgetOverrun` (05)

```java
public record Budget(Category category, Money limit)                                    // 05
public record BudgetOverrun(Category category, YearMonth month, Money spent, Money limit) // 05
```

`BudgetOverrun`은 09 데모의 `warning: 식비 예산 초과: 312,400원 / 300,000원 (2026-10)` 한 줄에 대응한다.

## `Ledger` (05)

여러 항목과 예산으로 집계를 계산하는 불변 도메인 객체다. 저장은 하지 않는다. 07부터 service가 repository에서 읽은 항목과 세션의 예산으로 만들어 쓴다.

```java
public final class Ledger {
    public Ledger(Collection<? extends Entry> entries,
                  Collection<Budget> budgets)                      // 05 둘 다 복사해 보관. 같은 카테고리 예산이 여럿이면 나중 것
    public List<Entry> entries()                                   // 05 불변(List.copyOf), 받은 순서
    public Optional<Budget> budgetOf(Category category)            // 05
    public Money totalIncome(YearMonth month)                      // 05 없으면 Money.ZERO
    public Money totalExpense(YearMonth month)                     // 05 없으면 Money.ZERO
    public Map<Category, Money> expenseByCategory(YearMonth month) // 05 지출이 있는 카테고리만. 금액 내림차순, 같으면 Category 선언 순. 불변
    public List<BudgetOverrun> overBudget(YearMonth month)         // 05 지출 > 한도인 카테고리만(같으면 초과 아님). Category 선언 순
}
```

09 데모의 리포트는 `totalIncome`, `totalExpense`, `totalIncome.difference(totalExpense)`(순액), `expenseByCategory`, `percentOf`로 만든다.

## `InvalidEntryException` (06)

```java
public class InvalidEntryException extends RuntimeException {
    public InvalidEntryException(String message)                   // 06
    public InvalidEntryException(String message, Throwable cause)  // 06
}
```

## `LedgerRepository` (07, `ledger.repository`)

```java
public interface LedgerRepository {
    void save(Entry entry);                                        // 07
    default void saveAll(Collection<? extends Entry> entries)      // 07 기본 구현은 save 반복. CSV 구현은 한 번에 기록
    List<Entry> findAll();                                         // 07 불변, 저장한 순서. 중복 검사 없음
    default List<Entry> findByMonth(YearMonth month)               // 07 findAll()을 month()로 거른다
}
```

- 계약(07 `LedgerRepositoryContractTest`가 검증): 저장한 순서대로 돌려준다. 같은 항목을 두 번 저장하면 두 번 나온다. 반환한 리스트를 바꾸려 하면 `UnsupportedOperationException`이 난다.
- 구현은 `save`·`saveAll` 시점에 바로 기록한다(write-through). CSV 구현의 I/O 실패는 `UncheckedIOException`이다. 그래서 `quit`이 따로 기록할 것은 없고, 저장된 항목 수(`findAll().size()`)를 보고한다.

## `InMemoryLedgerRepository` (07, `ledger.repository`)

```java
public class InMemoryLedgerRepository implements LedgerRepository {
    public InMemoryLedgerRepository()                              // 07 빈 저장소. findAll은 List.copyOf(스냅숏)
}
```

## service (07, `ledger.service`)

07 도입 시점에 고정한다(설계 11절 4번). 둘 다 생성자로 `LedgerRepository`를 받는다(구현이 아닌 인터페이스에 의존).

```java
public class LedgerService {
    public LedgerService(LedgerRepository repository)              // 07
    public void setBudget(Budget budget)                           // 07 세션 메모리. 같은 카테고리는 나중 것
    public List<BudgetOverrun> add(Entry entry)                    // 07 addAll(List.of(entry))
    public List<BudgetOverrun> addAll(Collection<? extends Entry> entries)
        // 07 saveAll로 저장한 뒤, 이번에 추가한 지출이 걸린 (카테고리, 월) 중 예산 초과만. 월 오름차순, 같은 달은 Category 선언 순. 불변
}

public class ReportService {
    public ReportService(LedgerRepository repository)              // 07
    public MonthlyReport report(YearMonth month)                   // 07 findByMonth로 읽어 Ledger로 집계
}

public record MonthlyReport(YearMonth month, Money income, Money expense,
                            BigDecimal net, List<CategoryLine> categories) {    // 07 categories는 불변 복사
    public record CategoryLine(Category category, Money amount, int percent)   // 07 percent = amount.percentOf(expense)
}
```

- `add`·`addAll`의 반환값이 09의 `warning:` 줄이다. 이미 초과였던 (카테고리, 월)이라도 이번 추가와 무관하면 다시 경고하지 않는다. 수입은 경고 대상이 아니다.
- `MonthlyReport`는 09 `report` 출력의 데이터다. `net`은 음수가 될 수 있어 `BigDecimal`이고, `Money.format(net)`으로 출력한다. `categories`는 `Ledger.expenseByCategory`의 순서(금액 내림차순, 같으면 Category 선언 순)다.

## CSV 저장소와 은행 CSV 임포트 (08, `ledger.repository`)

08 도입 시점에 고정한다(설계 11절 4번). 파일 형식은 설계 6절. 둘 다 UTF-8로만 읽고 쓰며, I/O 실패(UTF-8이 아닌 파일 포함)는 `UncheckedIOException`이다. 행 번호는 헤더를 1행으로 센 파일의 행 번호다.

```java
public class CsvLedgerRepository implements LedgerRepository {
    public CsvLedgerRepository(Path file)                          // 08 파일이 있으면 바로 읽는다. 없으면 빈 저장소(첫 저장 때 헤더와 함께 만든다)
        // 헤더가 다르면 IAE "헤더 오류: <헤더>". 잘못된 행이 있으면 건너뛰지 않고 InvalidEntryException "<N>행: <메시지>"
    // save·saveAll: 파일 끝에 덧붙인 뒤 메모리에 반영(write-through). saveAll은 한 번에 기록. findAll은 List.copyOf
}

public final class BankCsvImporter {
    public static ImportResult read(Path file)                     // 08 저장하지 않는다. 헤더가 다르면 IAE "헤더 오류: <헤더>". 빈 줄은 건너뛴다
}

public record ImportResult(List<Entry> entries, List<RowError> errors) {   // 08 둘 다 불변 복사. entries는 파일 순서
    public record RowError(int line, String reason)                // 08 reason은 InvalidEntryException 메시지
}
```

- 08에서 추가한 메시지(`InvalidEntryException`, 메시지 표의 형식과 같다): `필드 수 오류: <행>`, `구분 오류: <값>`(저장 파일의 `type`, 은행 CSV의 `구분`).
- 09 데모의 `17행: 날짜 형식 오류: 2026.10.11`은 `RowError(17, "날짜 형식 오류: 2026.10.11")`를 `<line>행: <reason>`으로 출력한 것이다. 유효한 항목은 `LedgerService.addAll(result.entries())`로 저장한다.

## CLI (09, `ledger.cli`)

09 도입 시점에 고정한다(설계 11절 4번). 주어진 테스트(`ReplTest`)는 `Repl`만 쓴다. `Command`는 package-private이라 공개 API가 아니다.

```java
public class Repl {
    public Repl(InputStream in, PrintStream out, Clock clock,
                LedgerService ledgerService, ReportService reportService)   // 09 입력은 UTF-8로 읽는다. today = LocalDate.now(clock)
    public int run()                                                         // 09 quit 또는 입력 끝까지 실행. 종료 코드 0
}

@FunctionalInterface
interface Command { void execute(List<String> args); }                      // 09 package-private. Repl이 Map<String, Command>에 등록
```

`LedgerService`에 추가(09, 기존 시그니처는 그대로):

```java
public int count()                                                           // 09 저장된 항목 수. quit의 "saved N entries"
```

출력 형식(프롬프트 `ledger> `는 줄바꿈 없이 출력). 데모 문자열은 설계 7절 09.

| 상황 | 출력 |
|---|---|
| `budget` | `budget: 식비 300,000원` |
| `add` | `added: <날짜> <라벨> <금액>[ <메모>][ #태그...]` 메모가 없으면 생략, 태그는 사전순 |
| 예산 초과(`add`는 `added` 다음, `import`는 요약 앞) | `warning: 식비 예산 초과: 312,400원 / 300,000원 (2026-10)` |
| `import` | `imported <N> entries, <M> errors`, 오류마다 `  <행>행: <사유>` |
| `report` | `== 2026-10 ==`, `수입 …`, `지출 …`, `순액 …`(`Money.format(net)`), 카테고리마다 `- 주거 500,000원 (59%)` |
| `quit`·입력 끝 | `saved <N> entries`, `bye`. 입력 끝이면 앞에 빈 줄 하나 |
| 인자 개수 오류 | `usage: <설계 6절의 해당 문법에서 명령 이름부터>` |
| 입력 오류 | `error: <메시지>` 후 다음 명령. 메시지는 위 메시지 표와 08 메시지, 그리고 아래 |

- 09에서 추가한 메시지: `모르는 명령: <이름>`, `구분 오류: <값>`(`add`의 `expense|income`), `월 형식 오류: <값>`, `파일 읽기 오류: <경로>`, `UTF-8이 아닌 파일: <경로>`.
- 토큰은 공백으로 나눈다. `add`의 다섯 번째 토큰부터 `#`로 시작하면 태그(`#` 제거), 아니면 메모 단어다. 메모는 단어 사이를 공백 하나로 다시 잇는다.
