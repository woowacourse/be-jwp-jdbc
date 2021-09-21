package nextstep.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface RowMapper<T> {
    T apply(ResultSet rs) throws SQLException;
}
