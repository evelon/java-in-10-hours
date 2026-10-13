# `ledger` 자바 실무 적응 과제 — 설계 문서

https://www.baeldung.com/get-started-with-java-series
위 링크의 내용을 학습한다.

## 1. 목적과 대상

- **컨셉:** 터미널 가계부 `ledger` 하나를 단계별로 키우며 위 Baeldung 시리즈의 내용을 익힌다. 각 단계는 시리즈의 글을 읽고, 그 내용을 가계부 코드에 적용하는 구조다.
- **목표:** 시리즈의 핵심 주제(기초 문법, OOP, 문자열, 예외, 배열, 컬렉션, 스트림, I/O, 개발 환경)를 실제 코드로 써 본다. 빌드·테스트는 실무 기본인 Gradle과 JUnit으로 한다.
- **대상:** 다른 언어 경험이 있고 Git 기본 사용이 가능하지만, 자바 문법, 자바식 OOP, 자바 빌드 환경은 처음인 사람.
- **분량:** 10단계(00~09), 약 9시간 10분. 글 읽는 시간을 포함한다.
- **제외:** Spring Boot, 웹 계층, DB, 로깅 프레임워크, mock 라이브러리, CI.

## 2. 설계 원칙

1. 00~01 단계에서만 `javac`/`java`/`jar`를 직접 쓴다. 02부터는 Gradle 표준 레이아웃이다.
2. 검증은 `./gradlew test` 하나다. `check.sh` 같은 별도 래퍼를 두지 않는다.
3. 03부터 각 단계는 **미리 주어진 실패하는 테스트**를 통과시키는 방식이다. 00~02는 README의 확인 명령과 기대 출력으로 자가 확인한다. 학습자가 테스트를 추가로 작성하는 부분은 단계 문서에 명시한다.
4. 문법은 필요한 시점에 도입한다. 시리즈 글은 해당 문법이 필요한 단계에 배정한다(7절 각 단계의 "읽을 글").
5. 단계마다 코드를 누적해서 키운다. 마지막에는 `./gradlew run`으로 동작하는 CLI가 나온다.
6. 학습자는 Git으로 작업하고 단계마다 커밋한다.
7. 주어진 테스트는 학습자 코드에 대해 컴파일되어야 하므로, 테스트가 의존하는 **공개 API(클래스·메서드 시그니처)는 단계 문서에 명시**한다. 내부 구현은 자유다.
8. 표기: 문서는 한국어, 코드·식별자·커밋 메시지는 영어. 사용자 출력과 오류 메시지는 09단계 데모 형식을 따른다. 테스트 이름은 영어 메서드명에 `@DisplayName` 한국어를 허용한다. Baeldung 글은 영어 제목 그대로 표기한다.

## 3. 환경 요구사항

- JDK 21 이상 설치. 빌드 toolchain은 21로 고정한다. JDK 22 이후에 추가된 기능은 다루지 않는다.
  - JDK 21이 없고 더 높은 버전만 있어도 동작하도록 `settings.gradle.kts`에 `org.gradle.toolchains.foojay-resolver-convention` 플러그인을 둔다(toolchain 자동 다운로드).
- Gradle은 wrapper(`./gradlew`)로만 사용한다. 학습자는 Gradle을 따로 설치하지 않는다. wrapper 파일은 02단계 배포물로 제공한다.
- Gradle은 Kotlin DSL(`build.gradle.kts`)을 쓴다.
- 에디터는 자유. 00~01은 터미널 사용을 권장한다(IDE가 빌드를 대신하지 않게). IntelliJ는 02부터 Gradle 프로젝트로 임포트한다.
- 사용 라이브러리: JUnit 6, AssertJ. JUnit 6의 테스트 API(Jupiter)는 JUnit 5와 같으므로 JUnit 5 기준의 글과 자료를 그대로 읽어도 된다. 01단계 외부 JAR 예시는 `commons-lang3`(전이 의존성 없음).
- Gradle과 라이브러리 버전은 정답 리포 제작 시점의 최신 안정판으로 정하고 `build.gradle.kts`에 고정한다. 정한 버전은 `docs/BUILD-LOG.md`에 기록한다.

## 4. 리포지토리 구조와 배포 방식

두 리포지토리로 나눈다.

**A. 과제 리포 (`ledger-exercises`, 학습자 배포용)**

```
README.md                  전체 안내, 진행 방법, 단계 목록
docs/
  00-first-run.md          단계별 과제 설명 (형식은 5절)
  01-packages-classpath.md
  ...
  09-cli.md
steps/
  00/ ... 09/              단계 시작 시 학습자 리포에 복사할 파일
```

- `steps/NN/`에는 **새로 추가되는 파일만** 둔다: 주어진 테스트, 테스트 픽스처(예: `bank.csv`), 새 클래스의 스텁, (02의 경우) wrapper 파일.
- 학습자가 이미 작성한 main 코드를 덮어쓰는 파일은 넣지 않는다. 기존 클래스를 바꿔야 하면 단계 문서의 요구사항으로 설명한다.
- 스텁은 시그니처만 갖추고 본문은 `throw new UnsupportedOperationException("TODO: step NN");`로 둔다. 테스트가 컴파일은 되고 실행에서 실패(red)하게 하기 위해서다.

**B. 정답 리포 (`ledger-solution`)**

- 단계마다 `step-NN-start`, `step-NN-done` 태그를 둔다. start는 `step-(N-1)-done` + `steps/NN/` 파일을 적용한 상태(테스트 실패), done은 정답이다.
- 태그 간 diff가 그 단계의 학습 내용이 되도록 커밋을 정리한다.
- 제작용 문서도 여기 둔다: `docs/design.md`(이 문서), `docs/API.md`, `docs/BUILD-LOG.md`.
- 학습자에게 공개한다. 막혔을 때 `step-NN-done`과 비교하는 용도다. README에 "단계를 끝내기 전에는 보지 말 것"을 안내한다.

**학습자 리포의 최종 모습(Gradle 단일 모듈)**

```
settings.gradle.kts
build.gradle.kts
gradlew, gradlew.bat, gradle/wrapper/
src/main/java/ledger/
  domain/        Money, Entry(Income, Expense), Category, Budget, BudgetOverrun, Ledger, InvalidEntryException
  service/       LedgerService, ReportService
  repository/    LedgerRepository, InMemoryLedgerRepository, CsvLedgerRepository
  cli/           Repl, Command, 각 명령 구현
  Main.java      객체 조립(composition root)
src/test/java/ledger/...   main과 같은 패키지 구조
src/test/resources/        테스트 픽스처
samples/bank.csv           데모용 은행 CSV
.gitignore
```

## 5. 단계 문서 형식

각 `docs/NN-*.md`는 다음 순서로 쓴다.

1. 목표 (한 문장)
2. 예상 시간
3. 읽을 글 (Baeldung 글 제목과 링크, 7절 배정대로)
4. 시작하기 (`steps/NN/`에서 복사할 파일)
5. 맞춰야 할 공개 API (주어진 테스트가 의존하는 시그니처)
6. 요구사항 (번호 목록, 테스트로 검증 가능하게. 학습자가 직접 작성할 테스트 포함)
7. 완료 기준 (03부터는 보통 `./gradlew test` 통과)
8. 실무 체크포인트 (3~5줄, 리뷰 포인트와 흔한 실수)

## 6. 공통 명세

여러 단계에 걸쳐 쓰이는 규칙이다. 단계별 테스트는 이 명세를 기준으로 만든다.

**도메인**

- `Money`: 원화 전용. 내부는 `BigDecimal`, scale 0으로 정규화한다. 음수 금액은 허용하지 않는다(차액 계산이 필요하면 별도 메서드). 출력 형식은 `12,000원`.
- `Category`: `enum`. 한국어 라벨을 가진다(예: `FOOD("식비")`, `HOUSING("주거")`, `TRANSPORT("교통")`, `SALARY("급여")`, `ETC("기타")`). `Category.fromLabel(String)`으로 변환하고, 모르는 라벨은 예외.
- `Entry`: `sealed interface`, 구현은 `record Income`, `record Expense`. 공통 필드는 `LocalDate date`, `Category category`, `Money amount`, `String memo`, `Set<String> tags`.
- `Budget`: `record Budget(Category category, Money limit)`. 월 단위로 적용한다.

**CLI 문법**

```
budget <카테고리> <금액>
add <expense|income> <yyyy-MM-dd|today> <카테고리> <금액> [메모...] [#태그...]
import <경로>
report <yyyy-MM>
quit
```

- 금액은 `12000`, `12,000` 둘 다 받는다.
- `today`는 주입된 `Clock` 기준이다(테스트에서 날짜 고정).
- `add`/`import` 직후, 예산을 넘은 (카테고리, 월)마다 경고 1줄을 출력한다.
- `report`의 카테고리 행은 지출만, 금액 내림차순, 비율은 지출 합계 대비 정수 %(`HALF_UP`).
- 예산은 세션 메모리에만 둔다. 항목은 `add`/`import` 시점에 바로 저장한다(write-through). `quit`은 저장된 항목 수를 보고하고 종료 코드 0을 반환한다.
- 저장 파일 경로는 `main`의 첫 인자, 없으면 `ledger.csv`.

**CSV 형식** (08단계)

- 저장 파일: 헤더 `date,type,category,amount,memo,tags`. `type`은 `INCOME`/`EXPENSE`, `amount`는 쉼표 없는 정수, `tags`는 공백 구분. UTF-8.
- 은행 CSV(`samples/bank.csv`, 제공): 헤더 `거래일,구분,금액,카테고리,적요`. `구분`은 `입금`/`출금`, 날짜는 `yyyy-MM-dd`, 금액은 쉼표 없는 정수. UTF-8.
- 두 형식 모두 필드에 쉼표·따옴표가 없다고 가정한다. 메모에 쉼표가 들어오면 검증 오류로 처리한다.

## 7. 단계별 설계

### 00 첫 실행과 기본 문법 — 50분
- 읽을 글: Difference Between JVM, JRE, and JDK / Introduction to Basic Syntax in Java / Introduction to Java Primitives / Java main() Method Explained / Control Structures in Java / A Guide to Java Loops
- 내용: `Hello.java`를 `javac`로 컴파일하고 `java`로 실행한다. `.class`가 무엇인지 확인한다. `main`의 `args`를 받아 원금·이율·기간으로 월별 잔액 표를 출력하는 작은 프로그램을 만든다(primitive, 조건문, 반복문). `Main-Class`가 지정된 실행 가능한 JAR을 `jar` 명령으로 직접 생성한다. 빌드 산출물(`out/`, `hello.jar`)은 최소 `.gitignore`로 제외한다.
  - 계산 규칙: 인자는 원금(원), 연이율(정수 %), 개월 수. 월 복리, `long` 정수 연산으로 월 이자 = `잔액 * 연이율 / 1200`(원 미만 버림). 출력은 한 줄에 `<월> <잔액>`. 인자 개수가 틀리면 usage를 stderr에 쓰고 종료 코드 1.
- 완료 기준: `java -jar hello.jar 1000000 3 12`가 기대 출력과 같다(첫 줄 `1 1002500`, 마지막 줄 `12 1030411`. 전체는 `docs/BUILD-LOG.md`).
- 실무 체크포인트: `JAVA_HOME`, 컴파일 타깃(`--release`). 정수 나눗셈과 오버플로, `double`의 오차(04에서 다시 다룬다).

### 01 패키지와 클래스패스 — 50분
- 읽을 글: Guide to Java Packages / Access Modifiers in Java / A Guide to the Static Keyword in Java / The "final" Keyword in Java / Class Loaders in Java
- 내용: `package` 선언과 디렉터리 구조, `javac -d out`. 외부 JAR(`commons-lang3`)을 `-cp`로 추가한다. 패키지 2개(`ledger.domain`, `ledger`)로 나눈 간단한 `Money` 유틸(`format(long)` → `12,000원`)과 이를 쓰는 `Main`(인자로 받은 금액들을 `StringUtils.leftPad`로 오른쪽 정렬해 출력하고 합계를 출력)을 만든다. 소스는 `src/ledger/...`에 둔다. 00의 `Hello.java`는 삭제한다. JAR은 `lib/`에 받아 두고 커밋하지 않는다. 접근 제어자를 바꿔 가며 다른 패키지에서 무엇이 보이는지 확인한다. 파일이 늘 때 수동 빌드가 번거로운 지점(순서, 클래스패스 관리, 테스트 실행 불가)을 직접 겪는다.
- 완료 기준: 스크립트 없이 수동 명령만으로 빌드·실행한다.
- 실무 체크포인트: 클래스패스에서 JAR을 빼면 `NoClassDefFoundError`가 난다. 왜 빌드 도구가 필요한가를 커밋 메시지나 README에 한 문단으로 적는다.

### 02 Gradle 전환 — 50분
- 읽을 글: 없음(시리즈 범위 밖). 단계 문서에 Gradle 최소 설명을 직접 쓴다.
- 제공: wrapper 파일(`gradlew`, `gradlew.bat`, `gradle/wrapper/`).
- 내용: 01 코드(`ledger.*`)를 Gradle 프로젝트로 옮긴다(`git mv src/ledger src/main/java/ledger`). `settings.gradle.kts`(foojay 플러그인), `build.gradle.kts`(`java`·`application` 플러그인, toolchain 21, `repositories`, `dependencies`, `mainClass`), `.gitignore`(00~01의 `out/`·`hello.jar`·`lib/`를 지우고 `build/`, `.gradle/`, IDE 파일). 01에서 `-cp`로 넣던 `commons-lang3`가 `implementation` 한 줄이 되는 것을 확인한다.
- 완료 기준: `./gradlew build` 성공, `./gradlew run --args="12000 3500 1000000"`의 출력이 01과 같다. 테스트는 아직 없다(`test` 태스크는 `NO-SOURCE`).
- 실무 체크포인트: `build/`는 커밋하지 않는다. wrapper는 커밋한다. `implementation`과 `testImplementation`의 차이.

### 03 클래스와 JUnit 6 — 60분
- 읽을 글: Java Classes and Objects / A Guide to Constructors in Java / Guide to the this Java Keyword / Immutable Objects in Java / Pass-By-Value as a Parameter Passing Mechanism in Java / Java equals() and hashCode() Contracts / Guide to hashCode() in Java / Comparing Objects in Java / Java toString() Method / Method Overloading and Overriding in Java
- 내용: JUnit 6(BOM) + AssertJ 의존성과 `useJUnitPlatform()`을 학습자가 설정한다. 설정 전에는 주어진 테스트가 컴파일되지 않는 것이 정상이다. `Money`를 `BigDecimal` 기반 불변 클래스로 완성한다: 생성자 검증, 정적 팩터리 오버로드(`of(long)`, `of(String)`), 더하기·빼기, `Comparable`, 직접 작성한 `equals`/`hashCode`/`toString`(포맷). `@ParameterizedTest`로 경계값을 검증하는 테스트는 학습자가 1개 이상 직접 작성한다. 주어진 테스트는 `MoneyTest`(10개)뿐이고 `Money` 스텁은 주지 않는다(학습자의 01 `Money`를 덮어쓰게 되므로 4절 스텁 규칙의 예외). 따라서 start의 red는 컴파일 실패다. 기존 `Main`은 새 `Money` API에 맞게 고친다. 공개 API는 `docs/API.md`.
- 완료 기준: `./gradlew test` 통과.
- 실무 체크포인트: `equals`를 재정의하면 `hashCode`도 재정의한다. 테스트 이름 규칙, 한 테스트에 한 가지 검증. Gradle 9부터는 `junit-platform-launcher`를 `testRuntimeOnly`로 명시해야 한다.

### 04 도메인 모델 — 55분
- 읽을 글: Java Record Keyword / A Guide to Java Enums / Sealed Classes and Interfaces in Java / Pattern Matching for Switch / Java Switch Statement / Java instanceof Operator / Comparing Strings in Java / Varargs in Java
- 내용: `Money`를 `record`로 바꾼다. 03의 테스트가 그대로 통과해야 한다(테스트가 리팩터링 안전망임을 체험). 03에서 손으로 쓴 `equals`/`hashCode`가 사라지는 것을 확인한다. `Entry` 계층(`Income`, `Expense`)과 `Category`를 6절 명세대로 만든다. 날짜는 `LocalDate`/`YearMonth`를 쓴다. 태그는 varargs로 받는다. `double`로 금액을 다루면 틀리는 케이스를 확인하는 테스트는 학습자가 1개 이상 직접 작성한다. 주어진 테스트는 `CategoryTest`(3), `EntryTest`(6), `MoneyRecordTest`(1, `Money`가 record인지 확인)다. `Money`는 학습자의 03 코드를 고치는 것이므로 스텁을 주지 않는다. 그래서 start에서 `Money` 쪽 red는 `MoneyRecordTest` 하나다.
- 완료 기준: 도메인 테스트 통과.
- 실무 체크포인트: 문자열은 `==`가 아니라 `equals`로 비교한다. `BigDecimal`의 `equals`는 scale까지 비교하므로(`1.0` ≠ `1.00`) record의 compact constructor에서 scale을 정규화한다. 도메인 객체는 가능한 한 불변(`Set.copyOf`).

### 05 컬렉션과 스트림 — 60분
- 읽을 글: Guide to the Java ArrayList / A Guide to Java HashMap / A Guide to HashSet in Java / The Basics of Java Generics / Guide to the Diamond Operator in Java / A Guide to Iterator in Java / Comparator and Comparable in Java / Guide To Java Optional / Functional Interfaces in Java / Introduction to Java Streams / Guide to Java Collectors
- 내용: `Budget`을 추가하고, `Ledger`에 월별 합계, 카테고리별 합계, 예산 초과 판단을 구현한다. 카테고리로 예산을 찾는 메서드는 `Optional`을 반환한다. 같은 집계를 반복문 버전과 스트림 버전으로 한 번씩 써 보고, 남길 버전을 고른다. `Money`에 리포트용 메서드(`ZERO`, `difference`, `percentOf`, `format`)를 추가한다. 주어진 테스트는 `LedgerTest`(7)와 `MoneyReportTest`(3)다. 스텁은 `Budget`, `BudgetOverrun`, `Ledger`이고, `Money`는 학습자 코드를 고치는 것이므로 스텁이 없다. 그래서 start의 red는 먼저 컴파일 실패(`Money`의 새 메서드 없음)이고, `Money`를 고친 뒤에는 `LedgerTest`의 실행 실패다.
- 완료 기준: 집계 테스트 통과.
- 실무 체크포인트: 컬렉션을 그대로 반환하지 않고 `List.copyOf`나 불변 뷰를 쓴다. 순회 중 삭제는 `ConcurrentModificationException`을 낸다(`removeIf` 사용). `BigDecimal.divide`는 scale과 `RoundingMode`를 명시한다(안 하면 `ArithmeticException`). 스트림 남용(가독성 저하) 경계.

### 06 예외와 검증 — 45분
- 읽을 글: Exception Handling in Java / Checked and Unchecked Exceptions in Java / Create a Custom Exception in Java / Chained Exceptions in Java / Difference Between Throw and Throws in Java / Guide to Inheritance in Java / Guide to the super Java Keyword
- 내용: 커스텀 예외를 설계한다(`InvalidEntryException extends RuntimeException`, `super(message, cause)`). checked와 unchecked의 선택 기준을 정한다. 입력 검증(음수 금액, 잘못된 날짜, 모르는 카테고리, 메모의 쉼표, 잘못된 태그)을 구현한다. 진입점은 `Entry.parse`이고, 메모·태그 검증은 `Income`·`Expense`의 compact constructor에 둔다. `Money.of(String)`은 `12000`·`12,000` 형식만 받도록 엄격하게 바꾼다(`1,2,3`, `1e3` 거부). 주어진 테스트는 `EntryValidationTest`(6)다. 오류 케이스는 모두 `assertThatThrownBy`로 메시지와 cause까지 검증한다. 학습자가 따로 작성할 테스트는 없다. 스텁은 `InvalidEntryException`뿐이다. `Entry.parse`는 학습자의 `Entry`에 추가하는 메서드라 스텁이 없다. 그래서 start의 red는 먼저 컴파일 실패이고, `parse`를 추가한 뒤에는 오류 케이스의 실행 실패다.
- 완료 기준: 오류 케이스 테스트 통과.
- 실무 체크포인트: 예외 삼키지 않기, 원인(cause) 보존, 메시지에 맥락 포함.

### 07 인터페이스와 레이어 — 50분
- 읽을 글: Java Interfaces / Static and Default Methods in Interfaces in Java / Abstract Classes in Java / Inheritance and Composition (Is-a vs Has-a relationship) in Java
- 내용: `domain`/`service`/`repository` 패키지로 분리한다. `LedgerRepository` 인터페이스(`findByMonth`는 default 메서드)와 `InMemoryLedgerRepository`를 만든다. `LedgerService`(항목 추가, 예산 경고)와 `ReportService`(월 리포트 데이터)는 생성자로 repository를 받는다(composition). service 테스트는 `InMemoryLedgerRepository`를 넣어서 한다. repository 계약 테스트(`LedgerRepositoryContractTest`, 추상 클래스)를 주고, 이를 상속한 테스트로 `InMemoryLedgerRepository`가 통과하게 한다. 상속 테스트(`InMemoryLedgerRepositoryTest`, `createRepository()` 하나)는 학습자가 직접 작성한다. 주어진 테스트는 계약 5개, `LedgerServiceTest` 3개, `ReportServiceTest` 2개다. 스텁은 repository·service의 새 타입 전부다. 계약 테스트는 추상 클래스라서 상속 테스트를 쓰기 전에는 실행되지 않는다. service 시그니처는 `docs/API.md`에 추가했다.
- 완료 기준: service 테스트와 계약 테스트 통과.
- 실무 체크포인트: 구현이 아닌 인터페이스에 의존, 생성자 주입, 패키지 의존 방향(domain은 아무것도 모른다).

### 08 파일 I/O와 CSV — 60분
- 읽을 글: How to Read a File in Java / Java - Write to File / Introduction to the Java NIO2 File API / Guide to BufferedReader / Java - Try with Resources / Java String Conversions / Arrays in Java: A Reference Guide
- 내용: `CsvLedgerRepository`(저장·로드)가 07의 계약 테스트를 통과하게 한다. 행은 `String.split`으로 배열로 나누고, 문자열을 숫자·날짜로 변환한다. 은행 CSV 임포트에서 잘못된 행은 건너뛰고 (행 번호, 사유)를 모아서 보고한다. 형식은 6절. 테스트는 `@TempDir`을 쓴다.
- 완료 기준: CSV 저장/로드/임포트 테스트 통과.
- 실무 체크포인트: 인코딩을 기본값에 맡기지 않는다(국내 은행 CSV는 MS949인 경우가 많다). 파일 경로는 `Path`로 다룬다. 부분 실패 처리 정책.

### 09 CLI 완성 — 70분
- 읽을 글: Read and Write User Input in Java / Formatting Output with printf() in Java / Callback Functions in Java / Functional Programming in Java
- 내용: REPL을 만든다(문법은 6절). 명령 파싱과 실행을 분리한다. `Command`는 함수형 인터페이스로 두고 `Map<String, Command>`에 람다로 등록한다. 입출력은 `InputStream`/`PrintStream`, 시간은 `Clock`을 주입받게 하여 테스트 가능하게 한다. `Main`에서 repository → service → `Repl`을 직접 조립한다.
- 완료 기준: 명령 파싱·실행 테스트 통과, 아래 데모 동작(`./gradlew run --console=plain`).
- 실무 체크포인트: 입출력·시간 주입, 종료 코드, 사용자 입력은 항상 신뢰하지 않는다. 자바는 경로의 `~`를 확장하지 않는다(셸이 하는 일).

```
ledger> budget 식비 300000
budget: 식비 300,000원
ledger> add expense today 식비 12,000 점심 김밥 #회사
added: 2026-10-11 식비 12,000원 점심 김밥 #회사
ledger> import samples/bank.csv
warning: 식비 예산 초과: 312,400원 / 300,000원 (2026-10)
imported 41 entries, 1 errors
  17행: 날짜 형식 오류: 2026.10.11
ledger> report 2026-10
== 2026-10 ==
수입 3,000,000원
지출 845,300원
순액 2,154,700원
- 주거 500,000원 (59%)
- 식비 312,400원 (37%)
- 교통 32,900원 (4%)
ledger> quit
saved 42 entries
bye
```

(빈 저장 파일에서 시작: 직접 추가 1건 + 임포트 41건 = 42건)

### 과제 없이 읽기만 하는 글

과제에 억지로 넣으면 설계가 부자연스러워지는 글이다. README에 목록만 두고, 관련 단계를 마친 뒤 읽게 한다.

- 00 이후: Stack Memory and Heap Space in Java
- 03 이후: A Guide to Creating Objects in Java / Concrete Class in Java / Object Type Casting in Java / How to Make a Deep Copy of an Object in Java / Composition, Aggregation, and Association in Java
- 04 이후: All About String in Java (시리즈) / Why String Is Immutable in Java?
- 제외: Unnamed Classes and Instance Main Methods in Java (JDK 25 정식 기능. 이 과제는 JDK 21 범위만 다룬다)
- 05 이후: A Guide to the Java LinkedList / Converting Between an Array and a List in Java / Initialize a HashMap in Java / Guide to the java.util.Arrays Class / Initializing Arrays in Java / Guide to the Java forEach Loop / Java Stream Filter with Lambda Expression / The Java Stream API Tutorial
- 08 이후: Java - Create a File / Guide to Java OutputStream / Introduction to Java Serialization / The transient Keyword in Java
- 09 이후: A Guide to Java Modularity / Guide to Java Reflection

## 8. 시간 합계

00(50) + 01(50) + 02(50) + 03(60) + 04(55) + 05(60) + 06(45) + 07(50) + 08(60) + 09(70) = **550분(약 9시간 10분)**.

## 9. 진행 방식 (학습자용)

1. Git 리포를 만든다. 00단계는 빈 리포에서 시작한다.
2. 단계마다 단계 문서의 "읽을 글"을 읽고, `steps/NN/`의 파일을 복사한다.
3. 완료 기준을 통과하면 커밋한다. (00~02는 README의 확인 명령으로 대신한다.)
4. 막히면 해당 단계 문서의 실무 체크포인트를 다시 보고, 정답 리포의 `step-NN-done` 태그와 비교한다.

## 10. 제작 순서

1. 정답 리포: 00~09 전 단계를 끝까지 구현하고 단계별 커밋·태그를 정리한다. (설계가 실제로 9시간 안에 되는지 검증) 작업 방식은 11절의 제작 규칙을 따른다.
2. `steps/NN/` 만들기: 각 단계에서 새로 추가된 테스트·픽스처와, 새 클래스의 스텁을 뽑는다(11절 5번).
3. `docs/NN-*.md` 작성 (5절 형식). "읽을 글"의 링크가 살아 있는지 확인한다.
4. 루트 `README.md` 작성
5. 리허설: 새 환경에서 00부터 `steps/`만 보고 따라 해 보며 시간·막히는 지점을 보정

## 11. 제작 규칙 (이 문서를 읽고 작업하는 Claude/제작자용)

한 번에 전 단계를 생성하면 뒤쪽 단계의 품질과 단계 간 정합성이 무너진다. 아래 규칙으로 나눠서 만든다.

1. **한 번에 한 단계만 만든다.** 단계 하나를 끝내기 전에 다음 단계를 시작하지 않는다.
2. **검증된 코드 위에서 쌓는다.** 단계 N 작업은 `step-(N-1)-done` 태그에서 시작한다. 단, 그 뒤에 API 태그(`api-vN`)가 있으면 가장 최근 API 태그에서 시작한다. API 태그는 `docs/API.md`의 고정 시그니처를 바꾼 커밋에 `api-v1`, `api-v2`, … 순번으로 단다. 단계를 끝낼 때마다 `./gradlew test`(00~02는 README의 확인 명령)를 실제로 실행해 통과를 확인하고, 통과 후에만 `step-NN-done` 태그를 찍는다. 실행하지 않은 것을 통과했다고 쓰지 않는다.
3. **세션을 나눈다.** 권장 구분은 다음과 같다. 세션 시작 시에는 이 문서, `docs/API.md`, `docs/BUILD-LOG.md`, 시작 태그(2번)의 코드만 읽는다.
   - 세션 A: 00~03 (환경 전환 구간. 가장 신중하게)
   - 세션 B: 04~07 (도메인과 구조)
   - 세션 C: 08~09 (I/O, CLI)
   - 세션 D: `steps/` 분리, `docs/`, 루트 README, 리허설 보정
4. **공개 API는 도입 시점에 고정한다.** 타입이 처음 등장한 단계의 done 시점에 공개 시그니처를 `docs/API.md`에 기록한다(`Money`는 03·04, `Entry`·`Category`는 04, `Budget`·`Ledger`는 05, `LedgerRepository`·service는 07, CLI는 09). 단, `Money`·`Entry`·`Ledger`·`LedgerRepository`와 그 시그니처에 등장하는 타입은 세션 A 종료 후 `docs/API.md`에 미리 고정했다. 각 메서드는 거기 적힌 단계에서 구현한다. 이후 단계는 이 시그니처를 바꾸지 않는다. 바꿔야 하면 영향받는 단계를 모두 고치고 태그를 다시 찍는다.
5. **start는 done에서 파생한다.** `step-NN-done`에서 이번 단계에 추가된 구현을 스텁(4절)으로 바꾸고 테스트를 남겨 `step-NN-start`를 만든다. 테스트를 따로 새로 쓰지 않는다. 그래야 테스트와 정답이 어긋나지 않는다. start에서 테스트가 실패하는 것도 실제로 확인한다.
6. **시간은 추정이 아니라 측정으로 보정한다.** 정답 구현 시간의 약 3~4배에 글 읽는 시간을 더해 학습 시간으로 보되, 최종 값은 세션 D의 리허설 결과로 7절의 시간을 수정한다.
7. **단계 완료마다 짧게 기록한다.** 단계 끝에 (a) 통과한 테스트 수, (b) 설계 대비 바꾼 점, (c) 다음 단계에 영향을 주는 결정을 한두 줄씩 `docs/BUILD-LOG.md`에 남긴다. 다음 세션은 이 파일로 맥락을 이어받는다.
8. **이 문서와 어긋나면 이 문서를 고친다.** 구현 중 설계가 틀린 것을 발견하면 코드만 고치지 말고 6·7절의 해당 부분도 함께 수정한다.
9. **테스트 규모:** 단계당 주어진 테스트 5~10개. 데모 출력 문자열은 09단계 테스트가 그대로 검증한다.

