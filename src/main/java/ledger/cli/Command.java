package ledger.cli;

import java.util.List;

// 명령 하나의 실행. 파싱(줄을 이름과 인자로 나누기)은 Repl이 하고, 명령은 인자만 받는다
@FunctionalInterface
interface Command {
    void execute(List<String> args);
}
