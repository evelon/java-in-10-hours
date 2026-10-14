# 07 인터페이스와 레이어

## 목표

저장소를 인터페이스로 추상화하고, 생성자로 저장소를 받는 service 레이어를 만든다.

## 예상 시간

50분

## 읽을 글

- [Java Interfaces](https://www.baeldung.com/java-interfaces)
- [Static and Default Methods in Interfaces in Java](https://www.baeldung.com/java-static-default-methods)
- [Abstract Classes in Java](https://www.baeldung.com/java-abstract-class)
- [Inheritance and Composition (Is-a vs Has-a relationship) in Java](https://www.baeldung.com/java-inheritance-composition)

## 시작하기

`steps/07/`의 파일을 복사한다.

```
src/main/java/ledger/repository/LedgerRepository.java          스텁
src/main/java/ledger/repository/InMemoryLedgerRepository.java  스텁
src/main/java/ledger/service/LedgerService.java                스텁
src/main/java/ledger/service/ReportService.java                스텁
src/main/java/ledger/service/MonthlyReport.java                스텁
src/test/java/ledger/repository/LedgerRepositoryContractTest.java
src/test/java/ledger/service/LedgerServiceTest.java
src/test/java/ledger/service/ReportServiceTest.java
```

복사 직후 `./gradlew test`에서는 service 테스트 5개만 실패한다. `LedgerRepositoryContractTest`는 추상 클래스라서 상속한 테스트를 쓰기 전에는 실행되지 않는다. 요구사항 1의 상속 테스트를 추가하면 계약 테스트 5개도 실행되어 실패한다.

## 맞춰야 할 공개 API

```java
package ledger.repository;

public interface LedgerRepository {
    void save(Entry entry);
    default void saveAll(Collection<? extends Entry> entries)
    List<Entry> findAll();
    default List<Entry> findByMonth(YearMonth month)
}

public class InMemoryLedgerRepository implements LedgerRepository {
    public InMemoryLedgerRepository()
}
```

```java
package ledger.service;

public class LedgerService {
    public LedgerService(LedgerRepository repository)
    public void setBudget(Budget budget)
    public List<BudgetOverrun> add(Entry entry)
    public List<BudgetOverrun> addAll(Collection<? extends Entry> entries)
}

public class ReportService {
    public ReportService(LedgerRepository repository)
    public MonthlyReport report(YearMonth month)
}

public record MonthlyReport(YearMonth month, Money income, Money expense, BigDecimal net,
                            List<CategoryLine> categories) {
    public record CategoryLine(Category category, Money amount, int percent) { }
}
```

## 요구사항

직접 작성할 테스트

1. **직접 작성할 테스트:** `LedgerRepositoryContractTest`를 상속한 `InMemoryLedgerRepositoryTest`를 `src/test/java/ledger/repository/`에 작성한다. `createRepository()`에서 새 `InMemoryLedgerRepository`를 반환하기만 하면 된다. 요구사항 2~6은 이 테스트로 실행된다.

`LedgerRepository` 계약

2. 처음에는 비어 있고, `findAll()`은 저장한 순서대로 돌려준다. (`returnsEntriesInSavedOrder`)
3. 같은 항목을 두 번 저장하면 두 번 나온다. (`keepsDuplicates`)
4. `saveAll`은 기존 항목 뒤에 받은 순서대로 붙인다. (`saveAllAppendsInOrder`)
5. `findByMonth`는 그 달 항목만 저장 순서대로 돌려주고, 없으면 빈 리스트다. (`findByMonthFiltersByMonth`)
6. `findAll()`과 `findByMonth()`가 돌려준 리스트는 수정할 수 없다(`UnsupportedOperationException`). (`returnsUnmodifiableLists`)

`LedgerService`

7. `add`는 항목을 저장소에 저장하고, 예산이 없으면 빈 경고 리스트를 반환한다. (`addSavesEntry`)
8. 같은 카테고리에 `setBudget`을 다시 하면 나중 예산이 적용된다. `add`로 그 달 지출이 한도를 넘으면 `BudgetOverrun(카테고리, 월, 누적 지출, 한도)`를 반환하고, 한도와 같으면 경고가 없다. (`addWarnsWhenBudgetExceeded`)
9. `addAll`은 받은 항목을 모두 저장하고, **이번에 추가한 지출이 걸린** (카테고리, 월) 중 초과한 것만 반환한다. 이미 초과돼 있던 다른 (카테고리, 월)은 다시 경고하지 않는다. 순서는 월 오름차순, 같은 달에서는 `Category` 선언 순이다. (`addAllWarnsOnlyForTouchedCategoryAndMonth`)

`ReportService`

10. `report(month)`는 그 달의 수입, 지출, 순액(`net`, 수입 - 지출), 지출 카테고리별 금액과 비율을 담은 `MonthlyReport`를 반환한다. 카테고리 행은 금액 내림차순이다. 다른 달 항목은 빠진다. 예: 2026-10은 수입 3,000,000원, 지출 845,300원, 순액 2154700, 행은 주거 59%·식비 37%·교통 4%. (`reportsMonth`)
11. 항목이 없는 달은 수입·지출이 `Money.ZERO`, 순액이 `BigDecimal.ZERO`, 카테고리 행이 빈 리스트다. (`reportsEmptyMonth`)

기존 코드

12. 06까지의 테스트를 고치지 않고 그대로 통과한다.
13. `ledger.domain`은 `ledger.service`·`ledger.repository`를 import하지 않는다. `grep -rE "import ledger\.(service|repository)" src/main/java/ledger/domain`의 결과가 없어야 한다.
14. `./gradlew -q run --args="12000 3500 1000000"`의 출력이 01의 기대 출력과 그대로 같다.

## 완료 기준

```
./gradlew test
```

06까지의 테스트, 계약 테스트(직접 작성한 상속 테스트로 실행), `LedgerServiceTest`, `ReportServiceTest`가 모두 통과한다. 요구사항 13의 `grep`과 14의 `run` 결과도 확인한다.

## 실무 체크포인트

- service는 `InMemoryLedgerRepository`가 아니라 `LedgerRepository` 인터페이스에 의존한다. 08에서 파일 저장소로 바꿔도 service 코드는 그대로다.
- 의존 대상은 `new`로 직접 만들지 않고 생성자로 받는다(생성자 주입). 테스트에서 원하는 구현을 넣을 수 있다.
- 패키지 의존 방향은 service → repository → domain이다. domain은 아무것도 모른다.
- 계약 테스트를 추상 클래스로 두면 구현이 늘어도 `createRepository()`만 채워 같은 계약을 검증한다. 08의 CSV 저장소도 이 계약을 상속한다.
- 집계는 05의 `Ledger`를 다시 쓴다. service는 저장소의 항목과 세션 예산을 `Ledger`에 넘겨 계산만 맡긴다(상속이 아니라 조합).
