# 03 클래스와 JUnit 6

## 목표

`Money`를 `BigDecimal` 기반 불변 클래스로 다시 만들고, JUnit 6 테스트로 검증한다.

## 예상 시간

60분

## 읽을 글

- [Java Classes and Objects](https://www.baeldung.com/java-classes-objects)
- [A Guide to Constructors in Java](https://www.baeldung.com/java-constructors)
- [Guide to the this Java Keyword](https://www.baeldung.com/java-this)
- [Immutable Objects in Java](https://www.baeldung.com/java-immutable-object)
- [Pass-By-Value as a Parameter Passing Mechanism in Java](https://www.baeldung.com/java-pass-by-value-or-pass-by-reference)
- [Java equals() and hashCode() Contracts](https://www.baeldung.com/java-equals-hashcode-contracts)
- [Guide to hashCode() in Java](https://www.baeldung.com/java-hashcode)
- [Comparing Objects in Java](https://www.baeldung.com/java-comparing-objects)
- [Java toString() Method](https://www.baeldung.com/java-tostring)
- [Method Overloading and Overriding in Java](https://www.baeldung.com/java-method-overload-override)

JUnit 6의 테스트 API(Jupiter)는 JUnit 5와 같다. JUnit 5 기준의 자료를 그대로 읽어도 된다.

## 시작하기

`steps/03/`의 파일을 복사한다.

```
src/test/java/ledger/domain/MoneyTest.java
```

`Money` 스텁은 없다. 01에서 만든 `Money`를 이 단계에서 직접 고친다. 그래서 복사 직후의 `./gradlew test`는 실행 실패가 아니라 컴파일 실패다. 다음 순서로 red가 바뀐다.

1. 그대로 실행: `package org.junit.jupiter.api does not exist`, `package org.assertj.core.api does not exist`
2. 요구사항 1을 마친 뒤: `cannot find symbol`, `constructor Money in class Money cannot be applied to given types`(01 `Money`와 API가 다름)
3. 아래 공개 API를 맞춘 뒤: 테스트 실행 실패. 여기서부터 요구사항 2~11을 하나씩 통과시킨다.

## 맞춰야 할 공개 API

```java
package ledger.domain;

public final class Money implements Comparable<Money> {
    public Money(BigDecimal amount)
    public static Money of(long won)
    public static Money of(String text)
    public BigDecimal amount()
    public Money plus(Money other)
    public Money minus(Money other)
    public int compareTo(Money other)
    public boolean equals(Object o)
    public int hashCode()
    public String toString()
}
```

01의 `Money.format(long)`은 없어진다. 04에서 `Money`를 record로 바꿀 것이므로 접근자 이름은 `getAmount()`가 아니라 `amount()`다.

## 요구사항

1. `build.gradle.kts`에 테스트 의존성을 설정한다. JUnit BOM(`org.junit:junit-bom:6.1.3`)을 `platform`으로 넣고 `org.junit.jupiter:junit-jupiter`, AssertJ(`org.assertj:assertj-core:3.27.7`)를 `testImplementation`으로, `org.junit.platform:junit-platform-launcher`를 `testRuntimeOnly`로 넣는다. `test` 태스크에 `useJUnitPlatform()`을 설정한다.
2. `Money.of("12,000")`, `Money.of("12000")`, `Money.of(12000)`은 서로 같다. (`ofLongAndOfStringAreEqual`)
3. `new Money(new BigDecimal("1.00"))`은 `Money.of(1)`과 `equals`·`hashCode`가 같고, `amount()`는 scale 0인 `1`이다. (`normalizesScale`)
4. `Money.of(-1)`은 `IllegalArgumentException`을 던진다. (`rejectsNegativeAmount`)
5. `Money.of("1.5")`는 `IllegalArgumentException`을 던진다. (`rejectsFractionalAmount`)
6. `Money.of("abc")`와 `Money.of("")`는 `IllegalArgumentException`을 던진다. (`rejectsNonNumericText`)
7. `plus`는 합계를 새 객체로 반환하고, 원래 객체의 값은 바뀌지 않는다. (`plusReturnsNewMoney`)
8. `minus`는 차액을 반환한다: `1000 - 300 = 700`. (`minusReturnsDifference`)
9. `minus`의 결과가 음수면 `IllegalArgumentException`을 던진다. (`minusRejectsNegativeResult`)
10. `Collections.sort`로 정렬하면 금액 오름차순이 된다. (`sortsByAmount`)
11. `toString`은 `12,000원`, `0원`, `1,000,000원` 형식이다. (`formatsWithCommaAndWon`)
12. **직접 작성할 테스트:** `@ParameterizedTest`로 `Money`의 경계값을 검증하는 테스트를 1개 이상 `src/test/java/ledger/domain/`에 작성한다. 예: 0과 `Long.MAX_VALUE`는 만들어지고, -1과 `Long.MIN_VALUE`는 예외가 난다.
13. `Main`을 새 `Money` API로 고친다. `./gradlew -q run --args="12000 3500 1000000"`의 출력이 01의 기대 출력과 같다.
14. `./gradlew -q run --args="12,000 3500"`처럼 쉼표가 든 금액도 받는다.

## 완료 기준

```
./gradlew test
```

`MoneyTest` 10개와 직접 작성한 테스트가 모두 통과한다. 이어서 요구사항 13·14의 `run` 출력을 확인한다.

```
./gradlew -q run --args="12000 3500 1000000"
     12,000원
      3,500원
  1,000,000원
= 1,015,500원

./gradlew -q run --args="12,000 3500"
  12,000원
   3,500원
= 15,500원
```

## 실무 체크포인트

- `equals`를 재정의하면 `hashCode`도 재정의한다. 둘 중 하나만 바꾸면 `HashSet`·`HashMap`에서 같은 금액을 다른 키로 취급한다.
- `BigDecimal.equals`는 scale까지 비교한다(`1.0` ≠ `1.00`). 생성자에서 scale 0으로 맞춘다. `setScale(0, RoundingMode.UNNECESSARY)`는 버릴 자리가 있으면 `ArithmeticException`을 던진다.
- `new BigDecimal("abc")`의 `NumberFormatException`은 그대로 새지 않게 `IllegalArgumentException`으로 감싸고 cause를 넘긴다.
- Gradle 9부터는 `junit-platform-launcher`를 `testRuntimeOnly`로 직접 선언해야 테스트가 실행된다.
- 테스트 하나에는 한 가지 동작만 검증하고, 메서드 이름은 그 동작을 영어로, `@DisplayName`은 한국어로 쓴다.
