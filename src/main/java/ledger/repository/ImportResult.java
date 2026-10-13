package ledger.repository;

import java.util.List;
import ledger.domain.Entry;

// 임포트 결과: 유효한 항목(파일 순서)과 건너뛴 행. 둘 다 불변 복사
public record ImportResult(List<Entry> entries, List<RowError> errors) {

    public ImportResult {
        entries = List.copyOf(entries);
        errors = List.copyOf(errors);
    }

    // line은 헤더를 1행으로 센 파일의 행 번호, reason은 InvalidEntryException 메시지
    public record RowError(int line, String reason) {
    }
}
