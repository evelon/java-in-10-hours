# 02 Gradle 전환

## 목표

01의 수동 빌드를 Gradle 프로젝트로 옮겨 `./gradlew` 한 줄로 빌드·실행한다.

## 예상 시간

50분

## 읽을 글

Gradle은 Baeldung 시리즈 범위 밖이라 읽을 글이 없다. 아래 최소 설명으로 대신한다.

- **wrapper:** `./gradlew`는 정해진 버전의 Gradle을 내려받아 실행하는 스크립트다. Gradle을 따로 설치하지 않는다. wrapper 파일은 커밋한다.
- **`settings.gradle.kts`:** 프로젝트 이름과 settings 플러그인을 적는다. `org.gradle.toolchains.foojay-resolver-convention` 플러그인을 두면 필요한 JDK가 없을 때 자동으로 내려받는다.
- **`build.gradle.kts`:** 빌드 정의다. 이 단계에서 쓰는 블록은 다음과 같다.
  - `plugins`: `java`(컴파일·테스트·JAR), `application`(`run` 태스크)
  - `java { toolchain { ... } }`: 컴파일·실행에 쓸 JDK 버전(21)
  - `repositories`: 라이브러리를 받을 곳(`mavenCentral()`)
  - `dependencies`: 라이브러리. `implementation`은 main 코드용, `testImplementation`은 테스트 코드용이다.
  - `application { mainClass = ... }`: `run`이 실행할 클래스
- **표준 레이아웃:** main 소스는 `src/main/java`, 테스트 소스는 `src/test/java`에 둔다. 산출물은 `build/`에 생긴다.

## 시작하기

`steps/02/`의 wrapper 파일을 학습자 리포 루트에 복사한다.

```
gradlew
gradlew.bat
gradle/wrapper/gradle-wrapper.jar
gradle/wrapper/gradle-wrapper.properties
```

01 소스를 표준 레이아웃으로 옮긴다.

```
git mv src/ledger src/main/java/ledger
```

## 맞춰야 할 공개 API

이 단계에는 주어진 테스트가 없고, 아래 확인 명령이 테스트 역할을 한다. 확인 명령이 의존하는 것은 다음과 같다.

- 소스 위치: `src/main/java/ledger/Main.java`, `src/main/java/ledger/domain/Money.java`(코드는 01 그대로)
- `run` 태스크가 실행할 클래스: `ledger.Main`
- 커밋된 wrapper 파일: `gradle/wrapper/gradle-wrapper.jar`, `gradle/wrapper/gradle-wrapper.properties`

## 요구사항

1. `./gradlew build`가 `BUILD SUCCESSFUL`로 끝난다. 테스트는 아직 없으므로 `:test`는 `NO-SOURCE`다.
2. `./gradlew -q run --args="12000 3500 1000000"`의 출력이 01의 기대 출력과 정확히 같다.
   - 01에서 `-cp`로 넣던 `commons-lang3` 3.21.0을 의존성으로 선언해야 한다.
3. `./gradlew build` 뒤 `git status --short --ignored`에서 `build/`와 `.gradle/`이 `!!`(ignored)로 나온다.
4. `git ls-files gradle/wrapper`에 `gradle-wrapper.jar`와 `gradle-wrapper.properties`가 나온다.

이 단계에서 직접 작성할 테스트는 없다.

## 완료 기준

```
./gradlew build
./gradlew -q run --args="12000 3500 1000000"
git status --short --ignored
git ls-files gradle/wrapper
```

기대 결과:

- `build`: `BUILD SUCCESSFUL`, `> Task :test NO-SOURCE`
- `run`:

  ```
       12,000원
        3,500원
    1,000,000원
  = 1,015,500원
  ```

- `git status`(변경을 모두 커밋한 뒤):

  ```
  !! .gradle/
  !! build/
  ```

- `git ls-files`:

  ```
  gradle/wrapper/gradle-wrapper.jar
  gradle/wrapper/gradle-wrapper.properties
  ```

## 실무 체크포인트

- `build/`, `.gradle/`은 커밋하지 않고 wrapper는 커밋한다. `.gitignore`에 `*.jar`를 쓰면 `gradle-wrapper.jar`까지 무시된다.
- 00~01의 `out/`, `hello.jar`, `lib/`는 더 쓰지 않는다. 디렉터리를 지우고 `.gitignore`에서도 뺀다. 남겨 두면 `git status`에 `??`로 나온다.
- `implementation`과 `testImplementation`의 차이: 03에서 JUnit은 `testImplementation`으로 넣는다. main 코드에서는 보이지 않는다.
- IntelliJ는 이제 Gradle 프로젝트로 임포트한다. IDE의 빌드 설정이 아니라 `build.gradle.kts`가 기준이다.
- (선택) 컴파일 경고를 빌드 실패로 만들면 경고를 놓치지 않는다. 과제 범위 밖이지만 켜 두고 진행해도 된다. 주어진 테스트에서는 경고가 나지 않는다.

  ```kotlin
  tasks.withType<JavaCompile>().configureEach {
      options.compilerArgs.addAll(listOf("-Xlint:all", "-Werror"))
  }
  ```
