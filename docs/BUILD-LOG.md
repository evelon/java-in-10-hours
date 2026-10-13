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
