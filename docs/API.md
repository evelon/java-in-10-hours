# 공개 API

주어진 테스트가 의존하는 공개 시그니처(설계 11절 4번). 타입이 처음 등장한 단계의 done 시점에 고정하고, 이후 단계는 바꾸지 않는다. 추가는 허용한다.

## `ledger.domain.Money` (03 도입, 04에서 record로 전환)

```java
public final class Money implements Comparable<Money> {
    public Money(BigDecimal amount)        // null이면 NPE, 음수·원 미만이면 IllegalArgumentException, scale 0으로 정규화
    public static Money of(long won)
    public static Money of(String text)    // "12000", "12,000". 숫자가 아니면 IllegalArgumentException
    public BigDecimal amount()             // scale 0
    public Money plus(Money other)
    public Money minus(Money other)        // 결과가 음수면 IllegalArgumentException
    public int compareTo(Money other)
    public boolean equals(Object o)
    public int hashCode()
    public String toString()               // "12,000원", "0원"
}
```

- 04에서 `record Money(BigDecimal amount)`로 바꿔도 위 시그니처가 그대로 성립하도록 접근자 이름을 `amount()`로 둔다.
- 검증 예외는 `IllegalArgumentException`이다. 06 이후에도 유지하고, `InvalidEntryException`은 입력·항목 단계에서 감싼다.
