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
