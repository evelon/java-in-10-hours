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
