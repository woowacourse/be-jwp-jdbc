package com.interface21.jdbc.core.extractor;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.interface21.jdbc.core.JdbcTemplateTest;
import com.interface21.jdbc.core.User;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ManualExtractorTest extends JdbcTemplateTest {


    @DisplayName("ManualExtractor을 사용해 데이터를 가져올 수 있다.")
    @Test
    void green() throws SQLException {
        User expected = new User("1", "one");
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("id")).thenReturn(expected.getId());
        when(resultSet.getString("name")).thenReturn(expected.getName());
        ResultSetExtractor<User> extractor = new ManualExtractor<>(resultSet,
                rs -> new User(rs.getString("id"), rs.getString("name"))
        );

        User actual = extractor.extractOne();

        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @DisplayName("ExtractionRule가 잘못 되었다면 예외가 발생한다.")
    @Test
    void red() throws SQLException {
        User expected = new User("1", "one");
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString(anyString())).thenThrow(SQLException.class);
        ResultSetExtractor<User> extractor = new ManualExtractor<>(resultSet,
                rs -> new User(rs.getString("empty"), rs.getString("empty2"))
        );

        Assertions.assertThatThrownBy(extractor::extractOne).isInstanceOf(SQLException.class);
    }
}
