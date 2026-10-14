# 01 패키지와 클래스패스

## 목표

코드를 패키지 2개로 나누고, 외부 JAR을 클래스패스에 직접 넣어 빌드·실행한다.

## 예상 시간

50분

## 읽을 글

- [Guide to Java Packages](https://www.baeldung.com/java-packages)
- [Access Modifiers in Java](https://www.baeldung.com/java-access-modifiers)
- [A Guide to the Static Keyword in Java](https://www.baeldung.com/java-static)
- [The "final" Keyword in Java](https://www.baeldung.com/java-final)
- [Class Loaders in Java](https://www.baeldung.com/java-classloaders)

## 시작하기

`steps/01/`에서 복사할 파일은 없다. 00의 학습자 리포에서 이어서 한다.

외부 JAR `commons-lang3`를 `lib/`에 받는다.

```
mkdir -p lib
curl -o lib/commons-lang3-3.21.0.jar https://repo.maven.apache.org/maven2/org/apache/commons/commons-lang3/3.21.0/commons-lang3-3.21.0.jar
```

## 맞춰야 할 공개 API

이 단계에는 주어진 테스트가 없고, 아래 확인 명령이 테스트 역할을 한다. 확인 명령이 의존하는 것은 다음과 같다.

```java
// src/ledger/Main.java
package ledger;
public class Main {
    public static void main(String[] args)
}

// src/ledger/domain/Money.java
package ledger.domain;
public final class Money {
    public static String format(long won)
}
```

## 요구사항

1. `javac --release 21 -d out -cp lib/commons-lang3-3.21.0.jar $(find src -name '*.java')`로 컴파일된다.
2. `java -cp out:lib/commons-lang3-3.21.0.jar ledger.Main 12000 3500 1000000`의 출력이 아래 기대 출력과 정확히 같다.
   - `Money.format(long)`은 금액을 `12,000원` 형식으로 만든다.
   - 인자로 받은 금액을 한 줄에 하나씩, 앞에 공백 2칸을 두고 출력한다. 각 금액은 합계 문자열의 폭에 맞춰 `StringUtils.leftPad`로 오른쪽 정렬한다.
   - 마지막 줄은 `= <합계>`다.
3. 실행할 때 `-cp`에서 JAR을 빼면(`java -cp out ledger.Main 12000`) `java.lang.NoClassDefFoundError: org/apache/commons/lang3/StringUtils`가 나고 종료 코드 1로 끝난다.
4. 컴파일할 때 `-cp`를 빼면 `error: package org.apache.commons.lang3 does not exist`가 난다.
5. `Money.format`에서 `public`을 빼면 컴파일할 때 `error: format(long) is not public in Money; cannot be accessed from outside package`가 난다. 확인한 뒤 `public`을 되돌린다.

이 단계에서 직접 작성할 테스트는 없다.

## 완료 기준

스크립트 없이 아래 수동 명령만으로 빌드·실행한다.

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

요구사항 3~5의 오류도 각각 직접 만들어 확인한다.

## 실무 체크포인트

- Windows에서는 클래스패스 구분자가 `:`가 아니라 `;`다.
- `String.format("%,d", ...)`의 구분자는 기본 Locale에 따라 바뀐다. `Locale.ROOT`를 명시한다.
- `lib/`는 `.gitignore`에 넣고 JAR을 커밋하지 않는다. 00의 `Hello.java`는 더 쓰지 않으니 지운다.
- 파일이 늘면 `find`로 소스 모으기, JAR 버전이 박힌 클래스패스, 테스트 실행 수단이 없다는 점이 번거로워진다. 왜 빌드 도구가 필요한지 커밋 메시지에 한 문단으로 적어 둔다.
