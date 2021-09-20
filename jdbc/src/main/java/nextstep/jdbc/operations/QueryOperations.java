package nextstep.jdbc.operations;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import nextstep.jdbc.utils.ResultSetExtractor;
import nextstep.jdbc.utils.RowMapper;

public interface QueryOperations {

    <T> List<T> queryForList(String sql, Class<T> type, Object ... args) throws SQLException;

    <T> List<T> query(String sql, RowMapper<T> rowMapper, Object ... args) throws SQLException;

    <T> T query(String sql, ResultSetExtractor<T> rse, Object ... args) throws SQLException;

    <T> Optional<T> queryForObject(String sql, Class<T> type, Object ... args) throws SQLException;

    <T> Optional<T> queryForObject(String sql, RowMapper<T> rowMapper, Object ... args) throws SQLException;

    int update(String sql, Object ... args) throws SQLException;
}
