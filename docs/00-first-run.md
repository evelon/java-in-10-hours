# 00 첫 실행과 기본 문법

## 목표

`javac`/`java`/`jar`를 직접 써서 월별 잔액 표를 출력하는 실행 가능한 JAR을 만든다.

## 예상 시간

50분

## 읽을 글

- [Difference Between JVM, JRE, and JDK](https://www.baeldung.com/jvm-vs-jre-vs-jdk)
- [Introduction to Basic Syntax in Java](https://www.baeldung.com/java-syntax)
- [Introduction to Java Primitives](https://www.baeldung.com/java-primitives)
- [Java main() Method Explained](https://www.baeldung.com/java-main-method)
- [Control Structures in Java](https://www.baeldung.com/java-control-structures)
- [A Guide to Java Loops](https://www.baeldung.com/java-loops)

## 시작하기

빈 Git 리포에서 시작한다. `steps/00/`에서 복사할 파일은 없다.

## 맞춰야 할 공개 API

이 단계에는 주어진 테스트가 없고, 아래 확인 명령이 테스트 역할을 한다. 확인 명령이 의존하는 것은 다음뿐이다.

- 리포 루트의 `Hello.java`
- 기본 패키지(`package` 선언 없음)의 `public class Hello`
- `public static void main(String[] args)`

## 요구사항

1. `javac --release 21 -d out Hello.java`로 컴파일된다.
2. `jar --create --file hello.jar --main-class Hello -C out .`로 실행 가능한 JAR이 만들어진다.
3. `java -jar hello.jar 1000000 3 12`의 출력이 아래 기대 출력과 정확히 같다.
   - 인자는 차례로 원금(원), 연이율(정수 %), 개월 수다.
   - 월 복리다. 매달 `잔액 * 연이율 / 1200`을 이자로 더한다. 원 미만은 `long` 정수 나눗셈으로 버린다.
   - 한 줄에 `<월> <잔액>`을 출력한다. 월은 1부터 센다.
4. `java -jar hello.jar`(인자 없이)는 stderr에 `usage: java -jar hello.jar <원금> <연이율%> <개월>`를 쓰고 종료 코드 1로 끝난다.

이 단계에서 직접 작성할 테스트는 없다.

## 완료 기준

리포 루트에서 아래 명령을 실행한다.

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

이어서 인자 없이 실행해 요구사항 4를 확인한다.

```
java -jar hello.jar; echo $?
```

기대 출력(첫 줄은 stderr):

```
usage: java -jar hello.jar <원금> <연이율%> <개월>
1
```

## 실무 체크포인트

- `java -version`과 `javac -version`이 서로 다르면 `JAVA_HOME`과 `PATH`를 확인한다. JDK 21보다 높은 버전만 있어도 `--release 21`이면 21용 class 파일이 나온다.
- `balance * ratePercent / 1200`에서 곱셈을 먼저 하지 않으면 이자가 0이 된다(정수 나눗셈). 반대로 `int`로 곱하면 큰 원금에서 오버플로가 난다. 잔액은 `long`으로 둔다.
- 금액을 `double`로 계산하면 오차가 생긴다. 04단계에서 다시 다룬다.
- 빌드 산출물 `out/`, `hello.jar`는 `.gitignore`로 커밋에서 뺀다. `*.jar`로 쓰면 02단계의 `gradle-wrapper.jar`까지 무시되니 파일명을 그대로 쓴다.
