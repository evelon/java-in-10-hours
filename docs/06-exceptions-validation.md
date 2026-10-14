# 06 예외와 검증

## 목표

커스텀 예외 `InvalidEntryException`을 만들고, 문자열 입력을 검증해 항목으로 바꾸는 `Entry.parse`를 구현한다.

## 예상 시간

45분

## 읽을 글

- [Exception Handling in Java](https://www.baeldung.com/java-exceptions)
- [Checked and Unchecked Exceptions in Java](https://www.baeldung.com/java-checked-unchecked-exceptions)
- [Create a Custom Exception in Java](https://www.baeldung.com/java-new-custom-exception)
- [Chained Exceptions in Java](https://www.baeldung.com/java-chained-exceptions)
- [Difference Between Throw and Throws in Java](https://www.baeldung.com/java-throw-throws)
- [Guide to Inheritance in Java](https://www.baeldung.com/java-inheritance)
- [Guide to the super Java Keyword](https://www.baeldung.com/java-super)

## 시작하기

`steps/06/`의 파일을 복사한다.

```
src/main/java/ledger/domain/InvalidEntryException.java   스텁
src/test/java/ledger/domain/EntryValidationTest.java
```

`Entry.parse`는 스텁이 없다. 04의 `Entry`에 직접 추가한다. 그래서 red는 두 단계다.

1. 복사 직후 `./gradlew test`: 컴파일 실패. `cannot find symbol`(`Entry.parse`)
2. `Entry.parse`를 추가한 뒤: 성공 케이스를 뺀 오류 케이스가 실패한다. 스텁 생성자가 `UnsupportedOperationException`을 던지기 때문이다.

## 맞춰야 할 공개 API

```java
package ledger.domain;

public class InvalidEntryException extends RuntimeException {
    public InvalidEntryException(String message)
    public InvalidEntryException(String message, Throwable cause)
}

public sealed interface Entry permits Income, Expense {
    // 04까지의 API에 추가
    static Entry parse(Type type, String date, String category,
                       String amount, String memo, Set<String> tags)
}
```

`date`는 `yyyy-MM-dd`, `category`는 한국어 라벨, `amount`는 `12000` 또는 `12,000` 형식이다.

## 요구사항

`Entry.parse` 성공

1. `parse`는 문자열 입력으로 `Expense`·`Income`을 만든다. `"12,000"`과 `"3000000"`을 모두 금액으로 받고, 메모 `""`와 빈 태그 집합도 받는다. (`parsesValidInput`)

`Entry.parse` 실패. 모두 `InvalidEntryException`이다.

2. 날짜 형식이 틀리면 메시지는 `날짜 형식 오류: <입력>`이고, cause는 `DateTimeParseException`이다. `2026.10.11`, `2026-02-30`(없는 날짜), `26-10-11`이 모두 해당한다. (`rejectsInvalidDate`)
3. 모르는 카테고리면 메시지는 `모르는 카테고리: 간식`이고, cause는 `IllegalArgumentException`이다. (`rejectsUnknownCategory`)
4. 금액이 잘못되면 `Money`가 던진 `IllegalArgumentException`의 메시지를 그대로 쓰고, 그 예외를 cause로 둔다. (`rejectsInvalidAmount`)

   | 입력 | 메시지 |
   |---|---|
   | `-1` | `음수 금액: -1` |
   | `1.5` | `원 미만 금액: 1.5` |
   | `abc` | `금액 형식 오류: abc` |
   | `1,2,3` | `금액 형식 오류: 1,2,3` |
   | `1e3` | `금액 형식 오류: 1e3` |

   `1,2,3`과 `1e3`을 거부하려면 03의 `Money.of(String)`을 `12000`, `12,000` 형식만 받도록 엄격하게 고쳐야 한다.

메모·태그 검증

5. 메모에 쉼표가 있으면 `new Expense(...)`와 `Entry.parse(INCOME, ...)` 모두 `InvalidEntryException`을 던지고, 메시지는 `메모에 쉼표 사용 불가: 점심,김밥`이다. (`rejectsCommaInMemo`)
6. 태그가 비어 있거나 공백·쉼표·`#`을 포함하면 `new Income(..., "월급", tag)`가 `InvalidEntryException`을 던지고, 메시지는 `태그 형식 오류: <태그>`다. 대상 태그는 `회 사`, `""`, `#회사`, `회,사`다. (`rejectsInvalidTag`)

기존 코드

7. 05까지의 테스트를 고치지 않고 그대로 통과한다.
8. `./gradlew -q run --args="12000 3500 1000000"`의 출력이 01의 기대 출력과 그대로 같다.
9. `./gradlew -q run --args="1,2,3"`은 `IllegalArgumentException: 금액 형식 오류: 1,2,3`으로 끝난다.

이 단계에서 직접 작성할 테스트는 없다. 주어진 테스트가 `assertThatThrownBy`로 오류 유형을 모두 다룬다.

## 완료 기준

```
./gradlew test
```

05까지의 테스트와 `EntryValidationTest`가 모두 통과한다. 요구사항 8·9의 `run` 결과도 확인한다.

## 실무 체크포인트

- 입력 검증 실패는 호출자가 미리 막을 수 있는 프로그래밍 오류가 아니라 입력 데이터의 문제다. 그래도 `RuntimeException`(unchecked)으로 둔 이유를 스스로 정리해 본다. checked로 두면 07~09의 모든 호출부가 `throws`를 달아야 한다.
- 하위 예외를 감쌀 때는 `super(message, cause)`로 cause를 넘긴다. `catch`에서 예외를 삼키거나 메시지만 옮기면 원인을 추적할 수 없다.
- 메시지에는 무엇이 틀렸는지와 그 값을 함께 쓴다(`<사유>: <값>`). 08 임포트는 이 메시지 앞에 행 번호만 붙여 보고한다.
- 메모·태그 검증은 `Income`·`Expense` 두 record에 똑같이 필요하다. 같은 코드를 두 번 쓰지 말고 `ledger.domain` 안의 package-private 헬퍼로 모은다.
- 02의 선택 설정 `-Xlint:all -Werror`를 켰다면 `InvalidEntryException`에서 `[serial]` 경고로 빌드가 멈춘다. 예외는 `Serializable`이므로 `private static final long serialVersionUID = 1L;`을 추가한다.
