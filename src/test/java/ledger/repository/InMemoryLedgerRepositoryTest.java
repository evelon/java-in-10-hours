package ledger.repository;

// 학습자가 직접 작성하는 테스트의 예시 답안. steps/07에는 넣지 않는다.
class InMemoryLedgerRepositoryTest extends LedgerRepositoryContractTest {

    @Override
    protected LedgerRepository createRepository() {
        return new InMemoryLedgerRepository();
    }
}
