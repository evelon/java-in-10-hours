package ledger.domain;

// 사용자 입력 오류라 호출하는 쪽마다 처리를 강제할 이유가 없다. 그래서 unchecked로 둔다
public class InvalidEntryException extends RuntimeException {

    public InvalidEntryException(String message) {
        super(message);
    }

    public InvalidEntryException(String message, Throwable cause) {
        super(message, cause);
    }
}
