# BUILD-LOG

단계별 제작 기록(설계 11절 7번). 다음 세션은 이 파일로 맥락을 이어받는다.

## 환경

- 제작 환경: macOS, 로컬 JDK는 Temurin 25.0.4.1뿐이다. 00~01은 `javac --release 21`로 컴파일한다(class 파일 major version 65 확인).

## 00 첫 실행과 기본 문법

확인 명령:

```
javac --release 21 -d out Hello.java
jar --create --file hello.jar --main-class Hello -C out .
java -jar hello.jar 1000000 3 12
```

기대 출력:

```
1 1002500
2 1005006
3 1007518
4 1010036
5 1012561
6 1015092
7 1017629
8 1020173
9 1022723
10 1025279
11 1027842
12 1030411
```

인자 없이 실행하면 stderr에 `usage: java -jar hello.jar <원금> <연이율%> <개월>`를 쓰고 종료 코드 1.

- (a) 테스트: 없음(00~02는 확인 명령). 위 기대 출력은 같은 규칙을 Python으로 따로 계산한 값과 `diff`로 대조했다.
- (b) 설계 대비: 7절에 없던 계산 규칙과 출력 형식을 정해 7절 00에 반영했다. 설계는 `.gitignore`를 02에서 만들게 되어 있지만, 00부터 최소한으로 둔다(`out/`, `hello.jar`).
- (c) 다음 단계 영향: 01에서 `Hello.java`를 삭제하고, 02에서는 `ledger.*`만 옮긴다(02의 "00~01 코드를 옮긴다"를 그에 맞게 고칠 것). `.gitignore`에 `*.jar`를 쓰지 않는다. 쓰면 02의 `gradle-wrapper.jar`까지 무시된다. 01에서 `lib/`를 추가한다.

## 01 패키지와 클래스패스

- 외부 JAR: `commons-lang3` **3.21.0**(2026-09-29 릴리스, Maven Central 최신 안정판). POM의 의존성은 모두 test scope라 런타임 전이 의존성이 없다.

준비(JAR은 커밋하지 않는다):

```
mkdir -p lib
curl -o lib/commons-lang3-3.21.0.jar https://repo.maven.apache.org/maven2/org/apache/commons/commons-lang3/3.21.0/commons-lang3-3.21.0.jar
```

확인 명령:

```
javac --release 21 -d out -cp lib/commons-lang3-3.21.0.jar $(find src -name '*.java')
java -cp out:lib/commons-lang3-3.21.0.jar ledger.Main 12000 3500 1000000
```

기대 출력:

```
     12,000원
      3,500원
  1,000,000원
= 1,015,500원
```

실패 확인(체크포인트):
- 실행 시 `-cp`에서 JAR을 빼면 `java.lang.NoClassDefFoundError: org/apache/commons/lang3/StringUtils`가 나고 종료 코드 1.
- 컴파일 시 `-cp`를 빼면 `error: package org.apache.commons.lang3 does not exist`.
- `Money.format`에서 `public`을 빼면 `error: format(long) is not public in Money; cannot be accessed from outside package`.

- (a) 테스트: 없음(확인 명령). 위 출력은 모두 실제로 실행해 확인했다. Windows에서는 클래스패스 구분자가 `;`이다.
- (b) 설계 대비: 7절 01에 `Money`·`Main`의 내용과 소스 위치(`src/ledger/...`)를 명시했다. `Money.format`은 `Locale.ROOT`로 포맷한다. 기본 Locale에 맡기면 환경에 따라 구분자가 바뀐다.
- (c) 다음 단계 영향: 02는 `git mv src/ledger src/main/java/ledger`로 옮기고, `./gradlew run --args="12000 3500 1000000"`의 출력이 위와 같아야 한다. 7절 02의 "00~01 코드를 옮긴다"는 02에서 고친다. 03에서 `Money`를 재작성하면 `Money.format(long)`이 없어지므로 `Main`도 함께 고친다. `StringUtils`는 계속 쓰므로 `commons-lang3` 의존성은 유지한다.

## 02 Gradle 전환

- 버전: Gradle **9.8.1**(2026-10-07, 최신 안정판), foojay-resolver-convention **1.0.0**, toolchain으로 받은 JDK는 Temurin 21.0.12.1.
- wrapper는 `gradle wrapper --gradle-version 9.8.1 --distribution-type bin --gradle-distribution-sha256-sum <sha>`로 생성했다. `distributionSha256Sum`으로 배포본을 고정한다. `step-02-start` 커밋은 wrapper 파일 4개(`gradlew`, `gradlew.bat`, `gradle/wrapper/*`)만 담는다.

확인 명령:

```
./gradlew build
./gradlew -q run --args="12000 3500 1000000"
git status --short --ignored
git ls-files gradle/wrapper
```

기대 결과:
- `build`: `BUILD SUCCESSFUL`, `:test NO-SOURCE`.
- `run`: 01의 기대 출력과 같다.
- `git status`: `build/`, `.gradle/`이 `!!`(ignored)로 나온다.
- `git ls-files`: `gradle-wrapper.jar`, `gradle-wrapper.properties`가 나온다.

- (a) 테스트: 없음(확인 명령). 위 항목은 모두 실제로 실행해 확인했다. `run` 출력은 01 기대 출력과 `diff`로 대조했다. class 파일은 major version 65이고, `runtimeClasspath`에 `commons-lang3:3.21.0`이 들어간다.
- toolchain 자동 다운로드: 빈 `GRADLE_USER_HOME`에서 `-Dorg.gradle.java.installations.auto-detect=false`로 빌드해 foojay가 JDK 21을 내려받는 것을 확인했다(약 36초). `-P`로 넘기면 Gradle 9.8.1에서 deprecation 경고가 나므로 `-D`를 쓴다.
- (b) 설계 대비: 7절 02의 옮길 대상을 `ledger.*`로 고치고, `.gitignore`에서 00~01 항목을 지운다는 점과 `run` 확인 인자를 명시했다. 플러그인은 설계대로 `java`와 `application`을 둘 다 적었다(`application`이 `java`를 포함하지만 학습용으로 명시).
- (c) 다음 단계 영향: 03은 `build.gradle.kts`에 JUnit BOM, `junit-jupiter`, `testRuntimeOnly("org.junit.platform:junit-platform-launcher")`, AssertJ, `useJUnitPlatform()`을 추가한다. 버전은 03 작업 시점의 최신 안정판으로 정한다.

## 03 클래스와 JUnit 6

- 버전: JUnit BOM **6.1.3**(최신 안정판), AssertJ **3.27.7**(4.0.0-M1은 마일스톤이라 제외). 처음에는 설계의 "JUnit 5" 표기를 따라 5.14.4를 썼다가, 최신 안정판 규칙을 우선해 6.1.3으로 바꾸고 설계 3·7절의 표기도 JUnit 6으로 고쳤다. 테스트 코드(`org.junit.jupiter`)는 바뀌지 않았다.
- 주어진 테스트: `src/test/java/ledger/domain/MoneyTest.java` 10개. 스텁은 없다(01의 `Money`를 덮어쓰게 되므로). `step-03-start` = `step-02-done` + `MoneyTest.java`.
- 학습자 테스트 예시 답안: `MoneyBoundaryTest.java`(`@ParameterizedTest` 2개, 케이스 5개). done에만 있고 `steps/03`에는 넣지 않는다.
- 공개 API: `docs/API.md`.

start에서 red 확인(학습자가 겪는 순서):
1. 그대로 `./gradlew test`: `compileTestJava FAILED`, `package org.assertj.core.api does not exist`, `package org.junit.jupiter.api does not exist`.
2. 의존성과 `useJUnitPlatform()`만 추가: `compileTestJava FAILED`, `cannot find symbol`, `constructor Money in class Money cannot be applied to given types`(01의 `Money` API 불일치).

done 확인:

```
./gradlew clean build                        → BUILD SUCCESSFUL
build/test-results: MoneyTest tests=10, MoneyBoundaryTest tests=5, failures=0, errors=0
./gradlew -q run --args="12000 3500 1000000" → 01 기대 출력과 같다
./gradlew -q run --args="12,000 3500"        → 쉼표 금액도 받는다(= 15,500원)
```

- (a) 테스트: 15개 통과(주어진 10 + 학습자 예시 5). JUnit 6.1.3에서 실제로 실행해 확인했다. start의 red 2단계도 6.1.3 기준으로 다시 확인했다. `git diff step-03-start -- MoneyTest.java`가 비어 있음을 확인했다(테스트를 정답에 맞춰 고치지 않았다).
- (b) 설계 대비: 03의 red는 실행 실패가 아니라 컴파일 실패다(4절 스텁 규칙의 예외). 7절 03에 반영했다. `Main`은 새 `Money` API로 고쳤다. 오류 메시지는 09 데모 형식(`<사유>: <값>`)을 따른다: `음수 금액: -1`, `원 미만 금액: 1.5`, `금액 형식 오류: abc`.
- (c) 다음 단계 영향: 04에서 `record Money(BigDecimal amount)`로 바꿀 때 `MoneyTest`와 `MoneyBoundaryTest`를 수정 없이 통과해야 한다. 검증과 scale 정규화는 compact constructor로 옮기고, `toString`은 override를 유지한다. `of(String)`은 `"1,2,3"`, `"1e3"`처럼 느슨한 입력도 받는다. CLI 입력 검증을 엄격하게 할지는 06에서 정한다.

## API 사전 고정 (세션 A 종료 후)

`Money`, `Entry`, `Ledger`, `LedgerRepository`와 이들의 시그니처에 등장하는 타입(`Category`, `Budget`, `BudgetOverrun`, `InvalidEntryException`)의 공개 시그니처를 `docs/API.md`에 미리 고정했다. 메서드마다 도입 단계(04~07)를 적었다. 설계 11절 4번의 "도입 시점에 고정"을 이 타입들에 한해 "지금 고정, 도입 단계에 구현"으로 바꾸고 설계에도 반영했다. service, CSV 구현, 임포트, CLI는 원래대로 각 단계에서 추가한다.

- 검증: API대로 쓴 스켈레톤을 scratchpad에서 `javac --release 21`로 컴파일·실행했다(리포에는 넣지 않음).
  - record의 canonical 생성자와 varargs 생성자가 오버로드로 공존하고, varargs 0개도 의도대로 해석된다.
  - sealed `switch`가 동작한다.
  - `percentOf`가 데모 값 59·37·4를 낸다.
  - `format(difference(...))`가 `-2,000원`을 낸다.
- 6절 명세 대비 추가한 메서드: `Money.ZERO`·`difference`·`percentOf`·`format(BigDecimal)`(05), `Entry.type()`·`month()`(04), `Entry.parse`(06), `Entry.Type`, `BudgetOverrun`, `LedgerRepository.saveAll`(07).
- 결정과 근거
  - `Ledger`는 저장을 하지 않는 불변 집계 객체(`new Ledger(entries, budgets)`)다. 07부터 항목은 repository가, 예산은 service가 세션 메모리에 갖고 있으므로, 둘을 받아 계산만 하는 형태가 05와 07 모두에 맞는다.
  - 순액은 음수가 될 수 있어 `Money`로 표현할 수 없다. `difference`는 `BigDecimal`을 반환하고, `Money.format`이 포맷을 하나로 모은다.
  - `Entry.parse`는 06의 "잘못된 날짜·모르는 카테고리·메모 쉼표" 테스트의 진입점이다. 08(저장 CSV, 은행 CSV)과 09(`add`)가 이 하나를 같이 쓴다. `today`는 CLI가 `Clock`으로 날짜로 바꾼 뒤 넘긴다. 은행 CSV는 `입금`/`출금`을 `Type`으로 바꿔 넘기고, 태그는 빈 집합이다.
  - 예외: 값 타입은 IAE, 항목 수준은 `InvalidEntryException`이다. 메시지는 `<사유>: <값>`이고, 08 임포트는 `N행: ` 접두사를 붙여 보고한다(09 데모와 일치).
  - 태그는 `#` 없이 저장한다. 공백·쉼표·`#`은 금지한다(CSV 공백 구분, CLI 파싱 모호성 제거). `Set.copyOf`라 순회 순서가 보장되지 않으므로, 출력과 저장 시 사전순으로 정렬한다.
  - CSV의 `category` 필드는 라벨(`식비`)이다. 은행 CSV와 같은 파싱 경로를 쓰기 위해서다. `type` 필드만 enum 이름(`INCOME`/`EXPENSE`)이다.
  - repository는 write-through다. 그래서 `quit`의 "저장"은 저장된 항목 수를 보고하는 것이 된다(6절에 반영). 강제 종료해도 데이터를 잃지 않는다.
- 다음 단계 영향
  - **04는 `step-03-done`이 아니라 `api-v1` 태그에서 시작한다.** `api-v1`은 이 API 고정 커밋이다(`step-03-done` 다음 커밋). 설계 11.2에 반영했다.
  - 04: `Money`를 record로 바꿀 때 `Money.format`은 아직 만들지 않는다(05). `toString` override는 유지한다.
  - 08: 메모가 빈 행은 `split(",")`이 끝의 빈 필드를 버린다. `split(",", -1)`을 써야 한다(체크포인트 후보).
  - 요청에 "04~11"이라고 되어 있었으나 단계는 09까지라서 04~09로 해석했다.

## 04 도메인 모델

- 시작: `api-v1`(`step-03-done` 아님, 설계 11절 2번).
- 주어진 테스트(`src/test/java/ledger/domain/`): `CategoryTest` 3개(`@CsvSource` 5건 포함, 실행 7건), `EntryTest` 6개, `MoneyRecordTest` 1개. 스텁: `Category`, `Entry`, `Income`, `Expense`(본문 `UnsupportedOperationException("TODO: step 04")`). `Money`는 스텁이 없다(학습자의 03 `Money`를 고친다). `step-04-start` = `api-v1` + 위 테스트 3개 + 스텁 4개.
- 학습자 테스트 예시 답안: `DoublePitfallTest`(2개, `0.1 + 0.2`, `1500 * 1.1`). done에만 있고 `steps/04`에는 넣지 않는다.

start에서 red 확인: `./gradlew test` → `29 tests completed, 14 failed`. 실패는 주어진 04 테스트 14건 전부(`Category`·`Entry` 쪽은 `UnsupportedOperationException`, `MoneyRecordTest`는 `AssertionFailedError`). 03의 15건은 통과한다.

done 확인:

```
./gradlew clean test                         → BUILD SUCCESSFUL
build/test-results: MoneyTest 10, MoneyBoundaryTest 5, CategoryTest 7, EntryTest 6, MoneyRecordTest 1, DoublePitfallTest 2, failures=0, errors=0
./gradlew build                              → BUILD SUCCESSFUL
./gradlew -q run --args="12000 3500 1000000" → 01 기대 출력과 같다
```

- (a) 테스트: 31건 통과(03의 15 + 주어진 04 실행 14 + 학습자 예시 2). 실제로 실행해 확인했다. `git diff step-03-start -- MoneyTest.java`가 비어 있다. `Money`를 record로 바꾼 뒤에도 03 테스트를 고치지 않고 통과했다. 주어진 04 테스트도 `step-04-start` 이후 바뀌지 않았다.
- (b) 설계 대비: 7절 04에 주어진 테스트 구성, `Money` 스텁이 없는 이유, `double` 테스트를 학습자가 쓴다는 점을 적었다. `CategoryTest`에 `new String("식비")` 케이스를 넣었다. `==`로 비교하는 구현은 리터럴끼리는 우연히 통과하므로, 체크포인트("문자열은 `equals`")를 테스트로 잡기 위해서다. 처음 고른 `double` 예시 `1000 * 1.1`은 double에서도 정확히 `1100.0`이라 실패했다. `1500 * 1.1`(= `1650.0000000000002`)로 바꿨다. 예시 값은 jshell로 확인한 것만 쓴다.
- (c) 다음 단계 영향: varargs 생성자는 `Set.of(tags)`를 쓰면 중복 태그에서 IAE가 난다. `Set.copyOf(List.of(tags))`를 쓴다(체크포인트 후보). 메모 `null`은 NPE다(공통 규칙). 메모가 없으면 `""`를 넘긴다. 06의 메모·태그 검증은 `Income`·`Expense`의 compact constructor에 넣는다. 지금은 두 record에 같은 코드가 중복되어 있다. 06에서 검증이 늘면 `ledger.domain` 안의 package-private 헬퍼로 모은다. `Money.ZERO`·`difference`·`percentOf`·`format`은 아직 없다(05).

## 05 컬렉션과 스트림

- 시작: `step-04-done`(그 뒤의 API 태그 없음).
- 주어진 테스트(`src/test/java/ledger/domain/`): `LedgerTest` 7개, `MoneyReportTest` 3개(`@CsvSource` 3건 포함, 실행 5건). 데이터는 09 데모의 2026-10 값(수입 3,000,000원, 지출 845,300원, 비율 59·37·4)에 맞췄다. 다른 달 항목을 섞어 월 필터를 함께 검증한다. 스텁: `Budget`, `BudgetOverrun`, `Ledger`. `Money`는 스텁이 없다(학습자 코드를 고친다). `step-05-start` = `step-04-done` + 위 테스트 2개 + 스텁 3개.
- 학습자 테스트: 05는 따로 정하지 않았다(설계 7절 05에 없음).

start에서 red 확인(학습자가 겪는 순서):
1. 그대로 `./gradlew test`: `compileTestJava FAILED`, `cannot find symbol`(`Money.ZERO`·`difference`·`format`·`percentOf`)만 나온다.
2. `Money`만 고친 뒤: `43 tests completed, 7 failed`. 실패는 `LedgerTest` 7개 전부(스텁의 `UnsupportedOperationException`)이고, `MoneyReportTest`를 포함한 나머지는 통과한다.

done 확인:

```
./gradlew clean test                         → BUILD SUCCESSFUL
build/test-results: MoneyTest 10, MoneyBoundaryTest 5, MoneyRecordTest 1, DoublePitfallTest 2, CategoryTest 7, EntryTest 6, MoneyReportTest 5, LedgerTest 7, failures=0, errors=0
./gradlew build                              → BUILD SUCCESSFUL
./gradlew -q run --args="12000 3500 1000000" → 01 기대 출력과 같다
```

- (a) 테스트: 43건 통과(04까지 31 + 주어진 05 실행 12). 실제로 실행해 확인했다. `git diff step-05-start -- src/test`가 비어 있고, `MoneyTest`는 03 이후 그대로다. `docs/API.md`는 `api-v1`과 같다(시그니처 변경 없음).
- (b) 설계 대비: 7절 05에 `Money` 리포트 메서드 추가, 주어진 테스트 구성, 2단계 red를 적었다. 반복문과 스트림 중에서는 이렇게 남겼다. 합계와 카테고리별 집계는 스트림이다(`filter`→`reduce`, `groupingBy`+`reducing`). `overBudget`은 반복문이다. 예산마다 조회·비교·생성을 하는 부분은 반복문이 더 읽기 쉽다. `percentOf`의 0원 IAE 메시지는 `0원 대비 비율 계산 불가: <this>`다. 메시지 표에는 없으므로 테스트는 타입만 검증한다.
- (c) 다음 단계 영향:
  - `expenseByCategory`는 `LinkedHashMap`을 `Collections.unmodifiableMap`으로 감싸 돌려준다. `Map.copyOf`는 순서를 보장하지 않는다(체크포인트 후보). 동률 정렬은 `groupingBy`의 `HashMap` 순서에 기대지 않고 `thenComparing(comparingByKey())`로 명시한다.
  - 예산은 `EnumMap`에 두므로 `overBudget`이 별도 정렬 없이 Category 선언 순이 된다. 07 `LedgerService`는 세션 예산을 `Collection<Budget>`으로 넘기면 된다. 같은 카테고리를 다시 설정하면 나중 것이 이긴다.
  - `Money.toString`은 이제 `format(amount)`를 쓴다. 09 리포트의 순액은 `Money.format(totalIncome.difference(totalExpense))`다.

## 06 예외와 검증

- 시작: `step-05-done`(그 뒤의 API 태그 없음).
- 주어진 테스트: `src/test/java/ledger/domain/EntryValidationTest.java` 6개(`@ParameterizedTest` 3개 포함, 실행 15건). API.md 메시지 표의 06 항목과 03·04의 IAE 메시지가 `Entry.parse`를 거쳐 그대로 나오는지, cause가 보존되는지 검증한다. 스텁: `InvalidEntryException`(생성자 본문 `UnsupportedOperationException`). `step-06-start` = `step-05-done` + 위 테스트 + 스텁.
- 학습자 테스트: 없음. 주어진 테스트가 `assertThatThrownBy`로 5가지 오류 유형을 모두 다룬다. 7절 06에 명시했다.

start에서 red 확인(학습자가 겪는 순서):
1. 그대로 `./gradlew test`: `compileTestJava FAILED`, `cannot find symbol`(`Entry.parse`)만 나온다.
2. `Entry.parse`만 추가한 뒤: `58 tests completed, 14 failed`. 성공 케이스(`parsesValidInput`)만 통과하고, 오류 케이스 14건은 모두 실패한다(스텁 예외 생성자의 UOE).

done 확인:

```
./gradlew clean build                        → BUILD SUCCESSFUL
build/test-results: 05까지 43 + EntryValidationTest 15, failures=0, errors=0
./gradlew -q run --args="12000 3500 1000000" → 01 기대 출력과 같다
./gradlew -q run --args="1,2,3"              → IllegalArgumentException: 금액 형식 오류: 1,2,3
```

- (a) 테스트: 58건 통과(05까지 43 + 주어진 06 실행 15). 실제로 실행해 확인했다. `git diff step-06-start -- src/test`가 비어 있고, `MoneyTest`는 03 이후 그대로다. `docs/API.md`는 `api-v1`과 같다.
- (b) 설계 대비:
  - 03 (c)에서 미룬 결정으로 `Money.of(String)`을 엄격하게 바꿨다. 정규식 `-?(\d+|\d{1,3}(,\d{3})+)(\.\d+)?`으로 형식만 거른다. 부호와 소수점은 형식상 허용하고, 생성자의 `음수 금액`·`원 미만 금액` 검증에 넘긴다. 그래야 메시지 표가 유지된다. 시그니처는 그대로이고 동작만 좁아졌다.
  - 형식 오류는 이제 `NumberFormatException`을 cause로 갖지 않는다. 정규식에서 먼저 거르기 때문이다.
  - 7절 06에 태그 검증, `Money.of` 변경, 주어진 테스트, 2단계 red를 적었다.
- (c) 다음 단계 영향:
  - 메모·태그 검증은 package-private `EntryRules`(`ledger.domain`)에 모았다. 04에서 예고한 `Income`·`Expense` 중복 제거다. 공개 API가 아니므로 API.md에는 넣지 않았다.
  - `Entry.parse`의 검증 순서는 날짜 → 카테고리 → 금액 → 메모·태그다. 한 행에 오류가 여럿이면 앞의 것 하나만 보고한다. 08 임포트의 `N행: <메시지>`는 이 메시지를 쓴다.
  - 날짜는 `LocalDate.parse`(ISO, STRICT)다. `2026-02-30`도 `날짜 형식 오류`가 된다. `today` 변환은 09 CLI의 몫이다.
  - `Money` 안에서 정규식 static 필드는 `ZERO`보다 앞에 둔다. `ZERO`를 `of(String)`으로 만들도록 바뀌어도 초기화 순서 때문에 NPE가 나지 않게 하기 위해서다.

## 07 인터페이스와 레이어

- 시작: `step-06-done`(그 뒤의 API 태그 없음).
- 기존 코드 변경: 없음. 도메인은 이미 `ledger.domain`에 있어 옮길 것이 없다. `Main`은 09에서 다시 쓴다. 문서는 `API.md`(service 절 추가만, 기존 부분은 바이트 단위로 동일), `design.md` 7절 07, 이 파일을 고쳤다.
- 새 타입: `ledger.repository.LedgerRepository`·`InMemoryLedgerRepository`, `ledger.service.LedgerService`·`ReportService`·`MonthlyReport`(중첩 `CategoryLine`).
- 주어진 테스트: `repository/LedgerRepositoryContractTest`(추상, 5개), `service/LedgerServiceTest` 3개, `service/ReportServiceTest` 2개. 스텁은 위 새 타입 전부다(본문 `UnsupportedOperationException("TODO: step 07")`). `step-07-start` = `step-06-done` + 위 테스트 3개 + 스텁 5개.
- 학습자 테스트 예시 답안: `repository/InMemoryLedgerRepositoryTest`(계약 상속, `createRepository()`만 구현). done에만 있고 `steps/07`에는 넣지 않는다.

start에서 red 확인(학습자가 겪는 순서):
1. 그대로 `./gradlew test`: `63 tests completed, 5 failed`. service 테스트 5개만 실패한다. 계약 테스트는 추상 클래스라 실행되지 않는다.
2. 상속 테스트를 추가한 뒤: `68 tests completed, 10 failed`(계약 5 + service 5).

done 확인:

```
./gradlew clean build                        → BUILD SUCCESSFUL
build/test-results: 06까지 58 + InMemoryLedgerRepositoryTest 5, LedgerServiceTest 3, ReportServiceTest 2, failures=0, errors=0
grep "import ledger.(service|repository)" src/main/java/ledger/domain → 없음(의존 방향 확인)
./gradlew -q run --args="12000 3500 1000000" → 01 기대 출력과 같다
```

- (a) 테스트: 68건 통과(06까지 58 + 계약 5 + service 5). 실제로 실행해 확인했다. `git diff step-07-start -- src/test`에서 주어진 테스트는 바뀌지 않았고, 기존 domain 코드·테스트는 `step-06-done`과 같다.
- (b) 설계 대비: 7절 07에 학습자 상속 테스트, 주어진 테스트 구성, 계약 테스트가 상속 전에는 실행되지 않는다는 점을 적었다. API.md에 `InMemoryLedgerRepository`와 service 절을 추가했다(11절 4번, 07 도입 고정).
- 결정과 근거:
  - 경고 범위: `addAll`은 "이번에 추가한 지출이 걸린 (카테고리, 월) 중 초과"만 돌려준다. 6절의 "예산을 넘은 (카테고리, 월)마다 경고 1줄"을 매 `add`마다 이미 초과된 모든 카테고리를 반복 경고하는 것으로 읽으면 출력이 시끄러워진다. 09 데모(`import` 뒤 식비 경고 1줄)와도 맞는 쪽을 골랐다.
  - 정렬: 월 오름차순, 같은 달 안에서는 Category 선언 순(`Ledger.overBudget` 순서)이다.
  - 예산은 `LedgerService`의 `EnumMap`에 두고 `new Ledger(repository.findAll(), budgets.values())`로 계산한다. `Ledger`는 05 그대로다.
  - `ReportService`는 `findByMonth`(default 메서드)로 그 달만 읽는다. 순액은 `BigDecimal net`이다.
  - `findByMonth`의 기본 구현은 `Stream.toList()`(불변)다. `InMemoryLedgerRepository.findAll`은 `List.copyOf` 스냅숏이다.
- (c) 다음 단계 영향:
  - 08 `CsvLedgerRepository` 테스트는 `LedgerRepositoryContractTest`를 상속하고 `@TempDir`로 `createRepository()`를 채운다. `saveAll`은 override해서 한 번에 기록한다(API.md).
  - `LedgerService.addAll`은 `saveAll` 한 번을 호출하므로, 08 임포트는 유효한 행을 모아 `addAll`로 넘기면 된다. 잘못된 행 보고는 임포트 쪽 책임이다.
  - 09: `add`/`import`의 경고는 `add`/`addAll`의 반환값을 그대로 출력한다. `report`는 `MonthlyReport`를 출력한다. `quit`의 항목 수는 `repository.findAll().size()`다. 서비스에 개수 메서드가 필요하면 09에서 추가한다(추가는 허용).

## 08 파일 I/O와 CSV

- 시작: `step-07-done`(그 뒤의 API 태그 없음).
- 완료 기준: 7절 08 "CSV 저장/로드/임포트 테스트 통과". 요청에는 "8절의 04번"이라고 되어 있었다. 8절은 시간 합계이고 번호 항목이 없어서 08단계 완료 기준으로 해석했다.
- 기존 코드 변경: 없음. 기존 공개 시그니처는 그대로다. `API.md`는 08 절을 덧붙이기만 했다(`git diff step-07-done -- docs/API.md`에 삭제된 줄 없음).
- 새 타입(`ledger.repository`): `CsvLedgerRepository`, `BankCsvImporter`, `ImportResult`(중첩 `RowError`).
- 주어진 테스트: `repository/CsvLedgerRepositoryTest`(계약 5개 상속 + 4개, `@TempDir`), `repository/BankCsvImporterTest` 4개. 스텁은 새 타입 3개 전부다(본문 `UnsupportedOperationException("TODO: step 08")`). `step-08-start` = `step-07-done` + 위 테스트 2개 + 스텁 3개. 픽스처 파일은 없다. 테스트가 `@TempDir`에 직접 쓴다.
- 학습자 테스트: 없음. 계약 상속은 07에서 이미 해 봤으므로 08에서는 주어진다.

start에서 red 확인: `./gradlew test` → `81 tests completed, 13 failed`. 실패는 주어진 08 테스트 13건 전부(UOE)이고, 07까지의 68건은 통과한다. 컴파일 오류는 없다.

done 확인:

```
./gradlew clean test                         → BUILD SUCCESSFUL
build/test-results: 07까지 68 + CsvLedgerRepositoryTest 9, BankCsvImporterTest 4, total=81, failures=0, errors=0
./gradlew build                              → BUILD SUCCESSFUL
./gradlew -q run --args="12000 3500 1000000" → 01 기대 출력과 같다
```

- (a) 테스트: 81건 통과(07까지 68 + 주어진 08 실행 13). 실제로 실행해 확인했다. `git diff step-08-start -- src/test`가 비어 있다.
- (b) 설계 대비: 4절 패키지 목록에 `BankCsvImporter`·`ImportResult`를 추가했다. 7절 08에 구현 방식, 주어진 테스트, 체크포인트(MS949, `split(",", -1)`, 부분 실패 정책)를 적었다.
- 결정과 근거:
  - 임포트는 `ledger.repository`에 둔다. 6절 CSV 형식을 다루는 파일 어댑터끼리 모은다. 패키지를 새로 만들지 않는다.
  - `BankCsvImporter.read`는 파싱만 하고 저장하지 않는다. 저장과 예산 경고는 07의 `LedgerService.addAll` 하나로 모은다.
  - 부분 실패: 은행 CSV는 잘못된 행을 건너뛰고 모은다. 자기 저장 파일은 깨진 행에서 `N행: ` 접두사를 붙인 `InvalidEntryException`으로 멈춘다. 저장 파일의 행을 조용히 버리면 데이터를 잃는다.
  - 행 번호는 헤더를 1행으로 센 파일의 행 번호다(에디터 행 번호와 같다).
  - 은행 CSV는 `split(",", 5)`이다. 적요에 쉼표가 있으면 마지막 필드에 그대로 남아 `Entry` 검증의 `메모에 쉼표 사용 불가: 점심,김밥`으로 보고된다(6절 "검증 오류로 처리"). 저장 파일은 `split(",", -1)`이고 필드는 정확히 6개여야 한다.
  - 새 메시지: `필드 수 오류: <행>`, `구분 오류: <값>`, `헤더 오류: <헤더>`(IAE, 파일 전체 거부).
  - `CsvLedgerRepository`는 생성 시 `Files.readAllLines`로 한 번 읽고, 저장마다 `Files.write(..., CREATE, APPEND)`로 덧붙인다. 파일이 없거나 비어 있으면 헤더를 먼저 쓴다. 파일 쓰기가 성공한 뒤에만 메모리에 반영한다. `BankCsvImporter`는 `BufferedReader`와 try-with-resources로 읽는다(읽을 글 두 가지를 모두 써 보게 한다).
  - 인코딩은 UTF-8 고정이다. MS949 파일은 `MalformedInputException`(cause) → `UncheckedIOException`이 되고, 이것을 테스트로 검증한다. UTF-8 BOM은 처리하지 않는다(헤더 오류가 된다).
- (c) 다음 단계 영향:
  - 09 `import`: `BankCsvImporter.read(path)` → `service.addAll(result.entries())`의 경고 출력 → `imported N entries, M errors` → 오류마다 `  <line>행: <reason>`. 파일 없음·인코딩 오류는 `UncheckedIOException`, 헤더 오류는 IAE이므로 Repl이 잡아서 출력한다.
  - 09 `Main`: `new CsvLedgerRepository(Path.of(args.length > 0 ? args[0] : "ledger.csv"))`. 깨진 저장 파일이면 생성자에서 예외가 나므로 시작 시 메시지를 출력하고 종료 코드 1이 자연스럽다.
  - `samples/bank.csv`는 09에서 만든다. 09 데모에 맞추려면 데이터 행 42개(유효 41 + 17행 날짜 오류 1), 2026-10 수입 3,000,000원, 은행 지출 합계 833,300원(식비 300,400·주거 500,000·교통 32,900)이어야 한다. 직접 추가하는 식비 12,000원과 합쳐 데모의 845,300원·식비 312,400원이 된다.

## 09 CLI 완성

- 시작: `step-08-done`(그 뒤의 API 태그 없음).
- 완료 기준: 7절 09 "명령 파싱·실행 테스트 통과, 데모 동작(`./gradlew run --console=plain`)". 요청에는 "8절의 09번"이라고 되어 있었다. 8절은 시간 합계이고 번호 항목이 없어서 08 때처럼 09단계 완료 기준으로 해석했다.
- 기존 공개 시그니처 변경: 없음. `LedgerService.count()`를 **추가**했다(API.md가 허용하는 추가, 07 (c)에서 예고). `API.md`는 CLI 절을 덧붙이기만 했다(`git diff step-08-done -- docs/API.md`에 삭제된 줄 없음).
- 새 타입(`ledger.cli`): `Repl`(공개), `Command`(package-private 함수형 인터페이스). 각 명령은 `Repl`의 private 메서드다. 명령마다 클래스를 만들지 않았다(설계 4절 고침).
- 그 밖의 변경: `Main`을 composition root로 다시 썼다. `build.gradle.kts`에서 `commons-lang3`를 지우고(더 쓰지 않음) `run`의 `standardInput`을 연결했다. `.gitignore`에 `ledger.csv`. `samples/bank.csv` 추가.
- 주어진 테스트: `cli/ReplTest` 6개. 스텁: `Repl`(생성자·`run` 본문 UOE). 제공 파일: `samples/bank.csv`. `step-09-start` = `step-08-done` + `ReplTest` + `samples/bank.csv` + `Repl` 스텁. `Main`, `Command`, `count()`, `build.gradle.kts`·`.gitignore` 변경은 학습자 몫이라 done에만 있다.
- 학습자 테스트: 없음.

start에서 red 확인: `./gradlew test` → `87 tests completed, 6 failed`. 실패는 `ReplTest` 6개 전부(스텁 UOE)이고, 08까지의 81건은 통과한다.

done 확인:

```
./gradlew clean test                         → BUILD SUCCESSFUL
build/test-results: 08까지 81 + ReplTest 6, total=87, failures=0, errors=0
./gradlew build                              → BUILD SUCCESSFUL
grep "import ledger.(service|repository|cli)" src/main/java/ledger/domain → 없음
printf '<데모 명령 5줄>' | ./gradlew -q run --console=plain --args=<빈 임시 파일>
  → 데모와 같다(added 날짜만 시스템 날짜 2026-10-13). 종료 코드 0, 저장 파일 43줄(헤더 + 42)
같은 파일로 다시 실행해 quit → saved 42 entries
깨진 저장 파일로 실행 → stderr "error: <경로>: 2행: 필드 수 오류: bad", 종료 코드 1
```

- (a) 테스트: 87건 통과(08까지 81 + 주어진 09 6). 실제로 실행해 확인했다. `git diff step-09-start -- src/test samples`가 비어 있다. `ReplTest` 작성 중 `#회 사` 케이스를 고쳤다. CLI는 공백으로 나누므로 `#회 사`는 태그 `회` + 메모 `사`가 되어 정상 추가된다. 태그 검증까지 가는 `#회#사`로 바꿨다(start 커밋 전).
- (b) 설계 대비: 4절 `cli/` 항목, 7절 09에 파싱·실행 분리 방식, 오류 출력, `Main`·빌드 변경, 제공 파일과 주어진 테스트를 적었다. 데모 아래 주석에 터미널 에코와 `today` 날짜 차이를 적었다.
- 결정과 근거:
  - 오류 출력은 `warning:`과 짝을 맞춘 `error: <메시지>`, 인자 개수 오류는 00의 usage 줄과 같은 `usage: <문법>`이다. 둘 다 stdout이고 REPL은 계속된다.
  - `Repl`이 잡는 예외는 `IllegalArgumentException`, `InvalidEntryException`, `UncheckedIOException`뿐이다. 그 밖의 예외는 버그이므로 숨기지 않는다. `YearMonth.parse`의 `DateTimeParseException`은 `report`에서 `월 형식 오류`(IAE)로 바꾼다. 임포트의 I/O 실패는 cause가 `CharacterCodingException`이면 `UTF-8이 아닌 파일`, 아니면 `파일 읽기 오류`다.
  - 입력 끝(Ctrl-D)은 `quit`과 같다. 프롬프트 뒤에 빈 줄을 하나 찍어 `saved`가 새 줄에서 시작하게 한다. 종료 코드는 둘 다 0이다. 1은 `Main`이 저장 파일을 못 읽을 때뿐이다.
  - 출력은 `printf`의 `%n`이다. 테스트는 프롬프트를 지우고 `lines()`로 비교하므로 OS 줄바꿈과 무관하다.
  - `add` 경고는 `added` 다음 줄, `import` 경고는 요약 앞이다(데모 순서).
  - `samples/bank.csv`의 9월 2건은 월 필터가 리포트에 섞이지 않는 것을 데모에서도 보여 준다. 9월 식비는 예산 미만이라 경고가 나지 않는다.
- (c) 다음 단계 영향(세션 D):
  - `steps/09/`: `ReplTest.java`, `samples/bank.csv`, `Repl` 스텁. 단계 문서에 `run`의 `standardInput` 설정과 `ledger.csv` gitignore를 요구사항으로 적는다(없으면 `./gradlew run`이 입력을 못 받는다. 체크포인트 후보).
  - `ReplTest`의 데모 테스트는 `samples/bank.csv`를 상대 경로로 읽는다. Gradle 테스트의 작업 디렉터리가 프로젝트 루트라서 된다. IDE에서 작업 디렉터리를 바꾸면 실패한다.
  - 01·02의 `commons-lang3` 설명은 그대로 두되, 단계 문서 09에 "더 쓰지 않으면 의존성도 지운다"를 체크포인트로 넣을 수 있다.

## 세션 D: `steps/`, 단계 문서, README

- 만든 것: `steps/02~09/`, `docs/00~09-*.md`, 루트 `README.md`. 배포를 두 리포에서 리포 하나로 바꾸고 설계 4·9·10절에 반영했다(커밋 `f494125`, `1f8d650`).
- `steps/NN/` 추출: `git diff --diff-filter=A <기준 태그> step-NN-start -- . ':!docs'`의 파일을 그대로 복사했다. 기준 태그는 `step-(N-1)-done`이고, 04만 `api-v1`이다.

검증(2026-10-14, 모두 실제로 실행):

```
steps/NN과 start 태그에서 추가된 파일 비교      → 02~09 모두 파일 목록·내용이 같다. start 커밋은 추가(A)만 있다
주어진 테스트: steps 사본과 HEAD 비교          → 모두 같다. done 커밋은 주어진 테스트를 고치지 않았다
태그별 ./gradlew test (worktree)
  03 start 컴파일 실패 / done 15 통과
  04 start 29 중 14 실패 / done 31 통과
  05 start 컴파일 실패 / done 43 통과
  06 start 컴파일 실패 / done 58 통과
  07 start 63 중 5 실패 / done 68 통과
  08 start 81 중 13 실패 / done 81 통과
  09 start 87 중 6 실패 / done 87 통과
00~02 확인 명령(done 태그)                     → 00 첫 줄 1 1002500, 끝 줄 12 1030411, 인자 오류 종료 코드 1
                                                 01 수동 빌드 출력 = 02 ./gradlew run 출력, 02 test NO-SOURCE
09 데모(./gradlew run, 빈 저장 파일)           → 7절 데모와 같다(added 날짜만 다름)
단계 문서 10개                                → 5절의 8개 절 순서, "읽을 글"이 7절 배정과 같다
```

- 설계 대비: 4절 최종 구조에 `MonthlyReport`를 넣고, 없는 `src/test/resources/`를 지웠다. 6절 경고 규칙을 API.md(이번 추가와 관련된 (카테고리, 월)만)에 맞췄다.
- 남은 일
  - Baeldung 링크 85개 확인(10절 3번): 스크립트 요청은 모두 403(봇 차단)이라 확인하지 못했다. 브라우저로 확인해야 한다.
  - 리허설과 7절 시간 보정(10절 5번, 11절 6번): 아직 하지 않았다. 7절 시간은 추정치다.
  - 원격 리포에 push할 때 태그도 함께 올린다(`git push --tags`). README의 `git diff step-NN-start step-NN-done` 안내가 태그에 의존한다.
