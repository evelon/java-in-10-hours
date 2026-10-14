# 04 도메인 모델

## 목표

`Money`를 record로 바꾸고, `Category` enum과 sealed `Entry` 계층(`Income`, `Expense`)을 만든다.

## 예상 시간

55분

## 읽을 글

- [Java Record Keyword](https://www.baeldung.com/java-record-keyword)
- [A Guide to Java Enums](https://www.baeldung.com/a-guide-to-java-enums)
- [Sealed Classes and Interfaces in Java](https://www.baeldung.com/java-sealed-classes-interfaces)
- [Pattern Matching for Switch](https://www.baeldung.com/java-switch-pattern-matching)
- [Java Switch Statement](https://www.baeldung.com/java-switch)
- [Java instanceof Operator](https://www.baeldung.com/java-instanceof)
- [Comparing Strings in Java](https://www.baeldung.com/java-compare-strings)
- [Varargs in Java](https://www.baeldung.com/java-varargs)

## 시작하기

`steps/04/`의 파일을 복사한다.

```
src/main/java/ledger/domain/Category.java     스텁
src/main/java/ledger/domain/Entry.java        스텁
src/main/java/ledger/domain/Income.java       스텁
src/main/java/ledger/domain/Expense.java      스텁
src/test/java/ledger/domain/CategoryTest.java
src/test/java/ledger/domain/EntryTest.java
src/test/java/ledger/domain/MoneyRecordTest.java
```

`Money`는 스텁이 없다. 03에서 만든 `Money`를 직접 고친다. 복사 직후 `./gradlew test`를 실행하면 03 테스트는 통과하고, 주어진 04 테스트는 모두 실패한다(`UnsupportedOperationException`, `MoneyRecordTest`는 `AssertionFailedError`).

## 맞춰야 할 공개 API

```java
package ledger.domain;

public record Money(BigDecimal amount) implements Comparable<Money> {
    // 03의 공개 API를 그대로 유지한다: of(long), of(String), amount(), plus, minus, compareTo, toString
}

public enum Category {
    FOOD("식비"), HOUSING("주거"), TRANSPORT("교통"), SALARY("급여"), ETC("기타");

    public String label()
    public static Category fromLabel(String label)
}

public sealed interface Entry permits Income, Expense {
    LocalDate date();
    Category category();
    Money amount();
    String memo();
    Set<String> tags();

    default Type type()
    default YearMonth month()

    enum Type { INCOME, EXPENSE }
}

public record Income(LocalDate date, Category category, Money amount, String memo, Set<String> tags)
        implements Entry {
    public Income(LocalDate date, Category category, Money amount, String memo, String... tags)
}

public record Expense(LocalDate date, Category category, Money amount, String memo, Set<String> tags)
        implements Entry {
    public Expense(LocalDate date, Category category, Money amount, String memo, String... tags)
}
```

## 요구사항

`Money`

1. `Money`는 record다. (`MoneyRecordTest.moneyIsRecord`)
2. 03의 `MoneyTest`와 직접 작성한 테스트를 고치지 않고 그대로 통과한다.

`Category`

3. 다섯 카테고리 모두 `label()`이 한국어 라벨을 반환하고, `fromLabel(라벨)`이 같은 상수를 반환한다. (`CategoryTest.convertsBetweenCategoryAndLabel`)
4. `fromLabel(new String("식비"))`도 `FOOD`를 반환한다. (`CategoryTest.comparesLabelByEquals`)
5. 모르는 라벨은 `IllegalArgumentException`이고, 메시지는 `모르는 카테고리: 간식`이다. (`CategoryTest.rejectsUnknownLabel`)

`Entry`, `Income`, `Expense`

6. varargs로 받은 태그를 보관하고, 중복 태그는 하나로 합친다. (`EntryTest.collectsVarargsTags`)
7. 태그 없이 만들면 `tags()`는 빈 집합이다. (`EntryTest.noTagsMeansEmptySet`)
8. `tags()`는 수정할 수 없다(`add`하면 `UnsupportedOperationException`). 생성자에 넘긴 집합을 나중에 바꿔도 항목의 태그는 바뀌지 않는다. (`EntryTest.tagsAreImmutableCopy`)
9. `type()`은 `Income`이면 `INCOME`, `Expense`면 `EXPENSE`다. (`EntryTest.typeFollowsImplementation`)
10. `month()`는 날짜의 연월(`YearMonth`)이다. (`EntryTest.monthIsYearMonthOfDate`)
11. 날짜·카테고리·금액·메모·태그가 같은 항목은 `equals`·`hashCode`가 같다. 태그 순서와 금액 입력 형식(`12000`, `"12,000"`)은 결과에 영향을 주지 않는다. 값이 같아도 `Income`과 `Expense`는 다르다. (`EntryTest.equalsByValue`)

직접 작성할 테스트와 기존 코드

12. **직접 작성할 테스트:** 금액을 `double`로 계산하면 틀리는 케이스를 1개 이상 `src/test/java/ledger/domain/`에 작성한다. 같은 계산을 `BigDecimal`로 하면 맞는다는 것도 함께 확인한다. 예: `0.1 + 0.2`, `1500 * 1.1`. `1000 * 1.1`은 `double`로도 정확히 `1100.0`이라 예시가 되지 않는다.
13. `./gradlew -q run --args="12000 3500 1000000"`의 출력이 01의 기대 출력과 그대로 같다.

## 완료 기준

```
./gradlew test
```

03 테스트, 주어진 04 테스트(`CategoryTest`, `EntryTest`, `MoneyRecordTest`), 직접 작성한 테스트가 모두 통과한다. 요구사항 13의 `run` 출력도 확인한다.

## 실무 체크포인트

- 문자열은 `==`가 아니라 `equals`로 비교한다. 리터럴끼리는 `==`도 우연히 통과하므로 요구사항 4가 그 차이를 잡는다.
- `Money`를 record로 바꾸면 03에서 직접 쓴 `equals`·`hashCode`가 사라진다. record의 `equals`는 `BigDecimal.equals`를 쓰고 이는 scale까지 비교하므로, scale 정규화는 compact constructor에 둔다.
- `Set.of(...)`는 중복 원소가 있으면 `IllegalArgumentException`을 던진다. varargs 태그는 `Set.copyOf(List.of(tags))`처럼 중복을 허용하는 경로로 합친다.
- 불변 객체는 받은 컬렉션을 그대로 보관하지 않고 compact constructor에서 `Set.copyOf`로 복사한다.
- `type()`은 sealed 인터페이스이므로 `switch` 패턴 매칭으로 쓰면 `default` 없이도 모든 경우가 처리됐는지 컴파일러가 확인한다.
