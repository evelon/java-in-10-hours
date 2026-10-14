package ledger.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CategoryTest {

    @ParameterizedTest
    @CsvSource({"FOOD, 식비", "HOUSING, 주거", "TRANSPORT, 교통", "SALARY, 급여", "ETC, 기타"})
    @DisplayName("카테고리와 한국어 라벨은 서로 변환된다")
    void convertsBetweenCategoryAndLabel(Category category, String label) {
        assertThat(category.label()).isEqualTo(label);
        assertThat(Category.fromLabel(label)).isSameAs(category);
    }

    @Test
    @DisplayName("라벨은 ==가 아니라 equals로 비교한다")
    void comparesLabelByEquals() {
        // 리터럴은 같은 객체로 공유되므로, ==로 비교하는 구현은 new String에서만 실패한다
        String label = new String("식비");

        assertThat(Category.fromLabel(label)).isSameAs(Category.FOOD);
    }

    @Test
    @DisplayName("모르는 라벨은 예외가 난다")
    void rejectsUnknownLabel() {
        assertThatThrownBy(() -> Category.fromLabel("간식"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("모르는 카테고리: 간식");
    }
}
