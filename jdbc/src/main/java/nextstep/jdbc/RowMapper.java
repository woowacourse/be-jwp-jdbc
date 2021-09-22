package nextstep.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface RowMapper<T> {

    T map(final ResultSet resultSet) throws SQLException;
}
