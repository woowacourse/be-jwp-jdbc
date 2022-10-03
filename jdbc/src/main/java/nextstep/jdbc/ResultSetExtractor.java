package nextstep.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface ResultSetExtractor<T> {

    T extract(ResultSet resultSet) throws SQLException;
}
