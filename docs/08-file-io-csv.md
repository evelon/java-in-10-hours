# 08 파일 I/O와 CSV

## 목표

항목을 CSV 파일에 저장·로드하는 저장소와, 은행 CSV를 읽어 항목으로 바꾸는 임포터를 만든다.

## 예상 시간

60분

## 읽을 글

- [How to Read a File in Java](https://www.baeldung.com/reading-file-in-java)
- [Java - Write to File](https://www.baeldung.com/java-write-to-file)
- [Introduction to the Java NIO2 File API](https://www.baeldung.com/java-nio-2-file-api)
- [Guide to BufferedReader](https://www.baeldung.com/java-buffered-reader)
- [Java - Try with Resources](https://www.baeldung.com/java-try-with-resources)
- [Java String Conversions](https://www.baeldung.com/java-string-conversions)
- [Arrays in Java: A Reference Guide](https://www.baeldung.com/java-arrays-guide)

## 시작하기

`steps/08/`의 파일을 복사한다.

```
src/main/java/ledger/repository/CsvLedgerRepository.java   스텁
src/main/java/ledger/repository/BankCsvImporter.java       스텁
src/main/java/ledger/repository/ImportResult.java          스텁
src/test/java/ledger/repository/CsvLedgerRepositoryTest.java
src/test/java/ledger/repository/BankCsvImporterTest.java
```

복사 직후 `./gradlew test`에서는 주어진 08 테스트 13건이 모두 `UnsupportedOperationException`으로 실패하고, 07까지의 테스트는 통과한다. 테스트용 CSV 파일은 따로 없다. 테스트가 `@TempDir`에 직접 쓴다.

## 맞춰야 할 공개 API

```java
package ledger.repository;

public class CsvLedgerRepository implements LedgerRepository {
    public CsvLedgerRepository(Path file)
    public void save(Entry entry)
    public void saveAll(Collection<? extends Entry> entries)
    public List<Entry> findAll()
}

public final class BankCsvImporter {
    public static ImportResult read(Path file)
}

public record ImportResult(List<Entry> entries, List<RowError> errors) {
    public record RowError(int line, String reason) { }
}
```

## 요구사항

`CsvLedgerRepository`

1. 07의 계약 테스트 5개를 그대로 통과한다. `CsvLedgerRepositoryTest`가 `LedgerRepositoryContractTest`를 상속하고 있다. (`returnsEntriesInSavedOrder`, `keepsDuplicates`, `saveAllAppendsInOrder`, `findByMonthFiltersByMonth`, `returnsUnmodifiableLists`)
2. 저장한 항목을 같은 파일로 만든 새 저장소에서 읽으면 같은 항목이 같은 순서로 나온다. (`reloadsSavedEntries`)
3. 파일은 UTF-8이고 아래 형식이다. (`writesCsvFormat`)
   - 첫 줄은 헤더 `date,type,category,amount,memo,tags`다.
   - `type`은 `INCOME`/`EXPENSE`, `category`는 한국어 라벨, `amount`는 쉼표 없는 정수다.
   - 메모나 태그가 없으면 빈 필드로 둔다.
   - 태그는 `#` 없이 사전순으로, 공백으로 구분한다.

   ```
   date,type,category,amount,memo,tags
   2026-10-11,EXPENSE,식비,12000,점심 김밥,야근 회사
   2026-10-01,EXPENSE,주거,500000,,
   2026-10-25,INCOME,급여,3000000,월급,
   ```

4. 파일이 없으면 빈 저장소이고, 저장하기 전에는 파일을 만들지 않는다. 기존 파일에 저장하면 헤더를 다시 쓰지 않고 끝에 이어 쓴다. (`appendsToExistingFile`)
5. 저장 파일에 잘못된 행이 있으면 생성자가 `InvalidEntryException`을 던지고, 메시지는 `<행 번호>행: <사유>`다. 헤더가 1행이다. 예: `3행: 날짜 형식 오류: 2026.10.11`. (`rejectsBrokenFile`)

`BankCsvImporter`

6. `read`는 은행 CSV(헤더 `거래일,구분,금액,카테고리,적요`)를 파일 순서대로 읽는다. `입금`은 `Income`, `출금`은 `Expense`이고, 태그는 비어 있다. 적요가 빈 행도 받는다. (`readsValidRows`)
7. 잘못된 행은 건너뛰고 `RowError(행 번호, 사유)`로 모은다. 행 번호는 헤더가 1행이다. 사유는 다음과 같다. (`collectsInvalidRows`)

   | 행 | 사유 |
   |---|---|
   | `2026.10.11,출금,8000,식비,점심` | `날짜 형식 오류: 2026.10.11` |
   | `2026-10-12,출금,3000,간식,과자` | `모르는 카테고리: 간식` |
   | `2026-10-13,출금,9000,식비,점심,김밥` | `메모에 쉼표 사용 불가: 점심,김밥` |
   | `2026-10-14,환불,9000,식비,` | `구분 오류: 환불` |
   | `2026-10-15,출금,9000` | `필드 수 오류: 2026-10-15,출금,9000` |

   필드가 5개보다 많은 행은 필드 수 오류가 아니다. 다섯 번째 필드 이후는 모두 적요로 보고, 메모 검증에서 걸러진다.
8. 헤더가 다르면 파일 전체를 거부한다. `IllegalArgumentException`이고 메시지는 `헤더 오류: <헤더>`다. (`rejectsWrongHeader`)
9. UTF-8이 아닌 파일(MS949)은 `UncheckedIOException`이고, cause는 `CharacterCodingException`이다. (`rejectsNonUtf8File`)

기존 코드

10. 07까지의 테스트를 고치지 않고 그대로 통과한다.
11. `./gradlew -q run --args="12000 3500 1000000"`의 출력이 01의 기대 출력과 그대로 같다.

이 단계에서 직접 작성할 테스트는 없다. 계약 상속은 07에서 이미 해 봤으므로 08에서는 주어진다.

## 완료 기준

```
./gradlew test
```

07까지의 테스트와 `CsvLedgerRepositoryTest`(계약 5 + 4), `BankCsvImporterTest`(4)가 모두 통과한다. 요구사항 11의 `run` 출력도 확인한다.

## 실무 체크포인트

- 인코딩을 기본값에 맡기지 않고 `StandardCharsets.UTF_8`을 명시한다. 국내 은행 CSV는 MS949인 경우가 많고, UTF-8로 읽으면 `MalformedInputException`이 난다. 이 예외를 조용히 넘기지 않는다.
- `"a,b,,".split(",")`는 끝의 빈 필드를 버린다. 빈 메모·태그가 있는 저장 파일은 `split(",", -1)`로 나눈다. 은행 CSV처럼 마지막 필드에 나머지를 몰아야 하면 `split(",", 5)`처럼 limit을 준다.
- 부분 실패 정책을 정한다. 남이 준 은행 CSV는 잘못된 행을 건너뛰고 보고한다. 자기 저장 파일은 깨진 행을 건너뛰면 데이터를 잃으므로 멈춘다.
- 파일 경로는 `String`이 아니라 `Path`로 다룬다. 읽기는 `Files.readAllLines`나 `BufferedReader`, 쓰기는 `Files.write(..., CREATE, APPEND)`로 하고, 스트림을 직접 열면 try-with-resources로 닫는다.
- 행 파싱은 06의 `Entry.parse` 하나를 다시 쓴다. 검증 메시지가 그대로 `RowError`의 사유가 된다.
