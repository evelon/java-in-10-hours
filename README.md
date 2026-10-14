> 이 저장소의 과제들은 AI를 통해 만들어졌습니다.

# ledger: 자바 실무 적응 과제

터미널 가계부 `ledger` 하나를 10단계에 걸쳐 키우며 Baeldung의 [Get Started with Java](https://www.baeldung.com/get-started-with-java-series) 시리즈를 익힌다. 각 단계에서 시리즈의 글을 읽고, 그 내용을 가계부 코드에 적용한다. 마지막에는 `./gradlew run`으로 동작하는 CLI 가계부가 나온다.

- **대상:** 다른 언어 경험이 있고 Git을 쓸 줄 알지만, 자바 문법·자바식 OOP·자바 빌드 환경은 처음인 사람
- **분량:** 10단계, 약 9시간 10분(글 읽는 시간 포함)
- **다루지 않는 것:** Spring Boot, 웹, DB, 로깅 프레임워크, mock 라이브러리, CI

## 준비물

- JDK 21 이상. 21보다 높은 버전만 있어도 된다. 02단계부터는 Gradle이 JDK 21을 자동으로 내려받는다.
- Git
- 터미널. 00~01단계는 IDE 없이 터미널에서 하기를 권한다. IntelliJ는 02단계부터 Gradle 프로젝트로 연다.
- 인터넷 연결(01단계 JAR 다운로드, 02단계부터 Gradle·라이브러리 다운로드)

Gradle은 따로 설치하지 않는다. 02단계에서 wrapper(`./gradlew`)를 받는다.

## 이 리포의 구성

```
README.md        이 문서
docs/NN-*.md     단계별 과제 설명
steps/NN/        단계를 시작할 때 학습자 리포에 복사할 파일(02~09)
src/ 등          정답 코드(마지막 단계까지 끝난 상태)
docs/design.md, docs/API.md, docs/BUILD-LOG.md   제작용 문서
```

**정답이 같은 리포에 있다.** 루트의 `src/`, `build.gradle.kts` 등은 09단계까지 끝낸 정답이다. 단계를 끝내기 전에는 `src/`와 `step-NN-done` 태그를 보지 않는다.

## 진행 방법

1. 이 리포를 내려받는다. 과제는 이 리포가 아니라 따로 만든 빈 Git 리포(학습자 리포)에서 한다.

   ```
   git clone <이 리포 주소> ledger-course
   mkdir my-ledger && cd my-ledger && git init
   ```

2. 단계마다 다음을 반복한다.
   1. 단계 문서(`docs/NN-*.md`)의 "읽을 글"을 읽는다.
   2. `steps/NN/`이 있으면 그 안의 파일을 학습자 리포에 그대로 복사한다. 경로는 학습자 리포 루트 기준으로 되어 있다.

      ```
      cp -R ../ledger-course/steps/03/. .
      ```

   3. 단계 문서의 요구사항을 구현한다. 03단계부터는 복사한 테스트가 처음에 실패하는 것이 정상이다.
   4. 완료 기준을 통과하면 커밋한다. 03단계부터는 `./gradlew test`가 기준이고, 00~02단계는 단계 문서의 확인 명령으로 확인한다.

3. 막히면 단계 문서의 "실무 체크포인트"를 다시 본다. 그래도 막히면 이 리포에서 그 단계의 정답만 비교한다.

   ```
   git diff step-03-start step-03-done
   ```

   `step-NN-start`는 그 단계를 시작하는 상태(직전 단계 정답 + `steps/NN/`), `step-NN-done`은 그 단계의 정답이다.

## 단계

| 단계 | 내용 | 시간 |
|---|---|---|
| [00 첫 실행과 기본 문법](docs/00-first-run.md) | `javac`/`java`/`jar`로 월별 잔액 표를 출력하는 JAR 만들기 | 50분 |
| [01 패키지와 클래스패스](docs/01-packages-classpath.md) | 패키지 분리, 외부 JAR을 클래스패스에 넣어 빌드·실행 | 50분 |
| [02 Gradle 전환](docs/02-gradle.md) | 수동 빌드를 Gradle 프로젝트로 옮기기 | 50분 |
| [03 클래스와 JUnit 6](docs/03-classes-junit.md) | `BigDecimal` 기반 불변 `Money`, JUnit·AssertJ 테스트 | 60분 |
| [04 도메인 모델](docs/04-domain-model.md) | record, enum, sealed 인터페이스로 항목 모델 만들기 | 55분 |
| [05 컬렉션과 스트림](docs/05-collections-streams.md) | 월별·카테고리별 집계와 예산 초과 판단 | 60분 |
| [06 예외와 검증](docs/06-exceptions-validation.md) | 커스텀 예외와 입력 검증 | 45분 |
| [07 인터페이스와 레이어](docs/07-interfaces-layers.md) | 저장소 인터페이스와 service 레이어 | 50분 |
| [08 파일 I/O와 CSV](docs/08-file-io-csv.md) | CSV 저장·로드, 은행 CSV 임포트 | 60분 |
| [09 CLI 완성](docs/09-cli.md) | REPL과 `Main` 조립으로 가계부 완성 | 70분 |

## 과제 없이 읽기만 하는 글

과제에 넣지 않은 시리즈 글이다. 해당 단계를 마친 뒤 읽는다.

- **00 이후:** [Stack Memory and Heap Space in Java](https://www.baeldung.com/java-stack-heap)
- **03 이후:** [A Guide to Creating Objects in Java](https://www.baeldung.com/java-initialization) / [Concrete Class in Java](https://www.baeldung.com/java-concrete-class) / [Object Type Casting in Java](https://www.baeldung.com/java-type-casting) / [How to Make a Deep Copy of an Object in Java](https://www.baeldung.com/java-deep-copy) / [Composition, Aggregation, and Association in Java](https://www.baeldung.com/java-composition-aggregation-association)
- **04 이후:** [All About String in Java](https://www.baeldung.com/java-string) / [Why String Is Immutable in Java?](https://www.baeldung.com/java-string-immutable)
- **05 이후:** [A Guide to the Java LinkedList](https://www.baeldung.com/java-linkedlist) / [Converting Between an Array and a List in Java](https://www.baeldung.com/convert-array-to-list-and-list-to-array) / [Initialize a HashMap in Java](https://www.baeldung.com/java-initialize-hashmap) / [Guide to the java.util.Arrays Class](https://www.baeldung.com/java-util-arrays) / [Initializing Arrays in Java](https://www.baeldung.com/java-initialize-array) / [Guide to the Java forEach Loop](https://www.baeldung.com/foreach-java) / [Java Stream Filter with Lambda Expression](https://www.baeldung.com/java-stream-filter-lambda) / [The Java Stream API Tutorial](https://www.baeldung.com/java-8-streams)
- **08 이후:** [Java - Create a File](https://www.baeldung.com/java-how-to-create-a-file) / [Guide to Java OutputStream](https://www.baeldung.com/java-outputstream) / [Introduction to Java Serialization](https://www.baeldung.com/java-serialization) / [The transient Keyword in Java](https://www.baeldung.com/java-transient-keyword)
- **09 이후:** [A Guide to Java Modularity](https://www.baeldung.com/java-modularity) / [Guide to Java Reflection](https://www.baeldung.com/java-reflection)
