package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OrderBasedPreparedStatementSetterTest {

    @DisplayName("순서에 따라 preparedStatement의 파라미터를 설정한다.")
    @Test
    void setValues() throws SQLException {
        // given
        Map<Integer, Object> parameters = new HashMap<>();
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        PreparedStatementSetter preparedStatementSetter = new OrderBasedPreparedStatementSetter("myungoh", 25);
        doAnswer((a) -> parameters.put(1, "myungoh")).when(preparedStatement).setObject(1, "myungoh");
        doAnswer((a) -> parameters.put(2, 25)).when(preparedStatement).setObject(2, 25);

        // when
        preparedStatementSetter.setValues(preparedStatement);

        // then
        assertThat(parameters.get(1)).isEqualTo("myungoh");
        assertThat(parameters.get(2)).isEqualTo(25);
    }
}
