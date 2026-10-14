# 09 CLI 완성

## 목표

명령을 읽어 실행하는 REPL을 만들고, `Main`에서 repository → service → REPL을 조립해 `./gradlew run`으로 동작하는 가계부를 완성한다.

## 예상 시간

70분

## 읽을 글

- [Read and Write User Input in Java](https://www.baeldung.com/java-console-input-output)
- [Formatting Output with printf() in Java](https://www.baeldung.com/java-printstream-printf)
- [Callback Functions in Java](https://www.baeldung.com/java-callback-functions)
- [Functional Programming in Java](https://www.baeldung.com/java-functional-programming)

## 시작하기

`steps/09/`의 파일을 복사한다.

```
src/main/java/ledger/cli/Repl.java      스텁
src/test/java/ledger/cli/ReplTest.java
samples/bank.csv                        데모용 은행 CSV(데이터 42행, 17행이 날짜 오류)
```

복사 직후 `./gradlew test`에서는 `ReplTest` 6개가 모두 `UnsupportedOperationException`으로 실패하고, 08까지의 테스트는 통과한다.

## 맞춰야 할 공개 API

```java
package ledger.cli;

public class Repl {
    public Repl(InputStream in, PrintStream out, Clock clock,
                LedgerService ledgerService, ReportService reportService)
    public int run()    // quit 또는 입력 끝까지 실행하고 종료 코드를 반환한다
}
```

```java
package ledger;

public class Main {
    public static void main(String[] args)    // args[0]: 저장 파일 경로
}
```

명령 문법(설계 6절):

```
budget <카테고리> <금액>
add <expense|income> <yyyy-MM-dd|today> <카테고리> <금액> [메모...] [#태그...]
import <경로>
report <yyyy-MM>
quit
```

## 요구사항

`Repl` (`ReplTest`. 고정 `Clock`으로 today는 2026-10-11이다)

1. 빈 저장 파일에서 아래 데모를 그대로 재현한다. 프롬프트 `ledger> `는 줄바꿈 없이 출력하고, 출력은 프롬프트로 시작한다. `run()`은 0을 반환하고, 저장 파일에는 42건이 남는다. (`reproducesDemo`)

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

   테스트는 입력을 화면에 다시 찍지 않으므로, 실제 출력에는 프롬프트 뒤에 명령이 보이지 않는다.

2. `add`는 `yyyy-MM-dd` 날짜도 받는다. 메모가 없으면 `added:` 줄에서 메모를 생략한다. `#`로 시작하는 토큰은 태그이고, 중복 없이 사전순으로 `#`를 붙여 출력한다. 예: `#회사 #야근 #회사` → `#야근 #회사`. (`addsEntries`)
3. `add`로 예산을 넘으면 `added:` 다음 줄에 `warning: 교통 예산 초과: 12,300원 / 10,000원 (2026-10)`을 출력한다. (`warnsAfterAdd`)
4. `report`에서 지출만 있으면 순액은 음수(`순액 -12,000원`)이고 비율은 `100%`다. 항목이 없는 달은 수입·지출·순액이 모두 `0원`이고 카테고리 행이 없다. (`reportsNegativeAndEmptyMonths`)
5. 잘못된 입력은 한 줄로 알리고 다음 명령을 받는다. 빈 줄은 아무것도 출력하지 않는다. 오류가 난 입력은 아무것도 저장하지 않는다. (`reportsErrorsAndKeepsRunning`)

   | 입력 | 출력 |
   |---|---|
   | `hello` | `error: 모르는 명령: hello` |
   | (빈 줄) | (출력 없음) |
   | `add expense today 식비` | `usage: add <expense\|income> <yyyy-MM-dd\|today> <카테고리> <금액> [메모...] [#태그...]` |
   | `add spend today 식비 12000` | `error: 구분 오류: spend` |
   | `add expense 2026.10.11 식비 12000` | `error: 날짜 형식 오류: 2026.10.11` |
   | `add expense today 식비 1,2,3` | `error: 금액 형식 오류: 1,2,3` |
   | `add expense today 식비 12000 #회#사` | `error: 태그 형식 오류: 회#사` |
   | `budget 간식 1000` | `error: 모르는 카테고리: 간식` |
   | `report 2026.10` | `error: 월 형식 오류: 2026.10` |
   | `import nowhere.csv` | `error: 파일 읽기 오류: nowhere.csv` |
   | `import <MS949 파일>` | `error: UTF-8이 아닌 파일: <경로>` |

6. `quit` 없이 입력이 끝나도(Ctrl-D) `quit`처럼 `saved <N> entries`, `bye`를 출력하고 0을 반환한다. (`endOfInputActsLikeQuit`)

`Main`과 실행

7. `./gradlew run`이 키보드(stdin) 입력을 받는다. 빈 저장 파일을 첫 인자로 주고 데모 명령을 넣으면 요구사항 1과 같은 결과가 나오고(`added:`의 날짜만 실행한 날), 종료 코드는 0, 저장 파일은 43줄(헤더 + 42)이다.
8. 같은 저장 파일로 다시 실행해 `quit`하면 `saved 42 entries`가 나온다. 항목은 `add`/`import` 시점에 이미 파일에 저장돼 있다.
9. 저장 파일을 읽지 못하면 stderr에 `error: <경로>: <메시지>`를 쓰고 종료 코드 1로 끝난다. 예: 헤더 다음 줄이 `bad`인 파일 → `error: <경로>: 2행: 필드 수 오류: bad`.

기존 코드

10. 08까지의 테스트를 고치지 않고 그대로 통과한다.
11. `ledger.domain`은 `ledger.service`·`ledger.repository`·`ledger.cli`를 import하지 않는다. `grep -rE "import ledger\.(service|repository|cli)" src/main/java/ledger/domain`의 결과가 없어야 한다.

이 단계에서 직접 작성할 테스트는 없다.

## 완료 기준

```
./gradlew test
```

08까지의 테스트와 `ReplTest` 6개가 모두 통과한다. 이어서 요구사항 7~9를 확인한다.

```
: > /tmp/ledger-demo.csv
printf 'budget 식비 300000\nadd expense today 식비 12,000 점심 김밥 #회사\nimport samples/bank.csv\nreport 2026-10\nquit\n' \
  | ./gradlew -q run --console=plain --args=/tmp/ledger-demo.csv
echo $?                       # 0
wc -l < /tmp/ledger-demo.csv  # 43

printf 'quit\n' | ./gradlew -q run --console=plain --args=/tmp/ledger-demo.csv
# ledger> saved 42 entries
# bye

printf 'date,type,category,amount,memo,tags\nbad\n' > /tmp/broken.csv
./gradlew -q run --console=plain --args=/tmp/broken.csv; echo $?
# error: /tmp/broken.csv: 2행: 필드 수 오류: bad
# 1
```

마지막으로 `./gradlew run --console=plain`으로 직접 명령을 입력해 본다.

## 실무 체크포인트

- 명령 파싱(줄을 이름과 인자로 나누기)과 실행을 분리한다. `Command`를 함수형 인터페이스로 두고 `Map<String, Command>`에 람다나 메서드 참조로 등록하면, 명령을 추가해도 `if`/`switch`가 늘지 않는다.
- 입출력(`InputStream`/`PrintStream`)과 시간(`Clock`)을 주입받으면 테스트에서 고정할 수 있다. `System.in`·`LocalDate.now()`를 직접 부르면 테스트할 수 없다. `./gradlew run`은 기본으로 stdin을 넘기지 않으므로 `run` 태스크의 `standardInput`을 `System.in`으로 연결한다.
- 사용자 입력은 신뢰하지 않는다. 다만 잡는 예외는 입력 오류(`IllegalArgumentException`, `InvalidEntryException`, `UncheckedIOException`)로 한정한다. 그 밖의 예외는 버그이므로 숨기지 않는다.
- 종료 코드는 정상 0, 시작 실패 1로 구분한다. 자바는 경로의 `~`를 확장하지 않는다(셸이 하는 일이다).
- `ledger.csv`는 `.gitignore`에 넣는다. `Main`이 더 이상 `StringUtils`를 쓰지 않으면 `commons-lang3` 의존성도 지운다. `ReplTest`는 `samples/bank.csv`를 상대 경로로 읽으므로 IDE에서 작업 디렉터리를 프로젝트 루트로 둔다.
