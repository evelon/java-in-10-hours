# 05 컬렉션과 스트림

## 목표

`Ledger`에 월별 합계, 카테고리별 합계, 예산 초과 판단을 컬렉션과 스트림으로 구현한다.

## 예상 시간

60분

## 읽을 글

- [Guide to the Java ArrayList](https://www.baeldung.com/java-arraylist)
- [A Guide to Java HashMap](https://www.baeldung.com/java-hashmap)
- [A Guide to HashSet in Java](https://www.baeldung.com/java-hashset)
- [The Basics of Java Generics](https://www.baeldung.com/java-generics)
- [Guide to the Diamond Operator in Java](https://www.baeldung.com/java-diamond-operator)
- [A Guide to Iterator in Java](https://www.baeldung.com/java-iterator)
- [Comparator and Comparable in Java](https://www.baeldung.com/java-comparator-comparable)
- [Guide To Java Optional](https://www.baeldung.com/java-optional)
- [Functional Interfaces in Java](https://www.baeldung.com/java-8-functional-interfaces)
- [Introduction to Java Streams](https://www.baeldung.com/java-8-streams-introduction)
- [Guide to Java Collectors](https://www.baeldung.com/java-collectors)

## 시작하기

`steps/05/`의 파일을 복사한다.

```
src/main/java/ledger/domain/Budget.java          스텁
src/main/java/ledger/domain/BudgetOverrun.java   스텁
src/main/java/ledger/domain/Ledger.java          스텁
src/test/java/ledger/domain/LedgerTest.java
src/test/java/ledger/domain/MoneyReportTest.java
```

`Money`는 스텁이 없다. 04의 `Money`에 메서드를 직접 추가한다. 그래서 red는 두 단계다.

1. 복사 직후 `./gradlew test`: 컴파일 실패. `cannot find symbol`(`Money.ZERO`, `difference`, `percentOf`, `format`)
2. `Money`에 아래 공개 API를 추가한 뒤: `LedgerTest` 7개가 스텁의 `UnsupportedOperationException`으로 실패한다. 나머지는 통과한다.

## 맞춰야 할 공개 API

```java
package ledger.domain;

public record Money(BigDecimal amount) implements Comparable<Money> {
    // 04까지의 API에 추가
    public static final Money ZERO
    public BigDecimal difference(Money other)
    public int percentOf(Money whole)
    public static String format(BigDecimal won)
}

public record Budget(Category category, Money limit) { }

public record BudgetOverrun(Category category, YearMonth month, Money spent, Money limit) { }

public final class Ledger {
    public Ledger(Collection<? extends Entry> entries, Collection<Budget> budgets)
    public List<Entry> entries()
    public Optional<Budget> budgetOf(Category category)
    public Money totalIncome(YearMonth month)
    public Money totalExpense(YearMonth month)
    public Map<Category, Money> expenseByCategory(YearMonth month)
    public List<BudgetOverrun> overBudget(YearMonth month)
}
```

## 요구사항

`Money` 리포트 메서드

1. `difference`는 `this - other`를 `BigDecimal`로 반환하고, 음수가 될 수 있다. `Money.format`은 `BigDecimal`을 `-2,000원`, `2,154,700원` 형식으로 만든다. (`MoneyReportTest.differenceCanBeNegative`)
2. `percentOf(whole)`은 전체 대비 비율을 정수 %로 반올림(`HALF_UP`)한다. 845,300원 대비 500,000원은 59, 312,400원은 37, 32,900원은 4다. (`MoneyReportTest.percentOfRoundsHalfUp`)
3. 0원 대비 `percentOf`는 `IllegalArgumentException`이다. (`MoneyReportTest.percentOfZeroIsRejected`)

`Ledger` 집계

4. `totalIncome(month)`와 `totalExpense(month)`는 그 달 항목만 더한다. 다른 달 항목은 빠진다. (`LedgerTest.totalsForMonth`)
5. 항목이 없는 달의 합계는 `Money.ZERO`다. (`LedgerTest.totalsForEmptyMonth`)
6. `expenseByCategory(month)`는 그 달의 지출만 카테고리별로 더하고, 금액 내림차순으로 순회되는 `Map`을 반환한다. (`LedgerTest.expenseByCategorySortedByAmount`)
7. 카테고리별 합계가 같으면 `Category` 선언 순이다. (`LedgerTest.expenseByCategoryTieFollowsDeclarationOrder`)

예산

8. `budgetOf(category)`는 그 카테고리의 예산을 `Optional`로 반환한다. 같은 카테고리 예산이 여럿이면 나중 것이고, 없으면 빈 `Optional`이다. (`LedgerTest.budgetOfReturnsLatestBudget`)
9. `overBudget(month)`는 그 달 지출이 한도를 **넘은** 카테고리만 `BudgetOverrun(카테고리, 월, 지출, 한도)`로 반환한다. 지출과 한도가 같으면 초과가 아니다. 순서는 `Category` 선언 순이다. (`LedgerTest.overBudgetOnlyWhenSpentExceedsLimit`)

불변성

10. 생성자에 넘긴 컬렉션을 나중에 바꿔도 `entries()`는 바뀌지 않는다. `entries()`와 `expenseByCategory(month)`가 돌려준 컬렉션은 수정할 수 없다(`UnsupportedOperationException`). (`LedgerTest.keepsCopiesAndReturnsUnmodifiableCollections`)

기존 코드

11. 04까지의 테스트를 고치지 않고 그대로 통과한다.
12. `./gradlew -q run --args="12000 3500 1000000"`의 출력이 01의 기대 출력과 그대로 같다.

이 단계에서 직접 작성할 테스트는 없다.

## 완료 기준

```
./gradlew test
```

04까지의 테스트와 주어진 05 테스트(`LedgerTest`, `MoneyReportTest`)가 모두 통과한다. 요구사항 12의 `run` 출력도 확인한다.

## 실무 체크포인트

- 같은 집계를 반복문 버전과 스트림 버전으로 한 번씩 써 보고, 읽기 쉬운 쪽을 남긴다. 단순 합계는 스트림이, 조회·비교·생성이 섞인 로직은 반복문이 읽기 쉬운 경우가 많다.
- 내부 컬렉션을 그대로 반환하지 않는다. `List.copyOf`는 불변 복사본을 만든다. 다만 `Map.copyOf`는 순회 순서를 보장하지 않으므로, 순서가 있는 맵은 `LinkedHashMap`을 `Collections.unmodifiableMap`으로 감싼다.
- `BigDecimal.divide`는 scale과 `RoundingMode`를 명시한다. 안 하면 나누어떨어지지 않을 때 `ArithmeticException`이 난다.
- 순회 중에 컬렉션에서 원소를 지우면 `ConcurrentModificationException`이 난다. `removeIf`를 쓴다.
- 동률 정렬을 `HashMap`의 순서에 기대지 않는다. `Comparator`에 `thenComparing`으로 2차 기준을 명시한다.
