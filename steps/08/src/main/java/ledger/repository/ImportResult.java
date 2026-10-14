package ledger.repository;

import java.util.List;
import ledger.domain.Entry;

public record ImportResult(List<Entry> entries, List<RowError> errors) {

    public ImportResult {
        throw new UnsupportedOperationException("TODO: step 08");
    }

    public record RowError(int line, String reason) {

        public RowError {
            throw new UnsupportedOperationException("TODO: step 08");
        }
    }
}
