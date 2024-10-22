package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void execute(String sql, Object... args) {
        executeStatement(sql, PreparedStatement::execute, new ArgumentPreparedStatementSetter(args));
    }

    public int update(String sql, Object... args) {
        return update(sql, new ArgumentPreparedStatementSetter(args));
    }

    public int update(String sql, PreparedStatementSetter preparedStatementSetter) {
        return executeStatement(sql, PreparedStatement::executeUpdate, preparedStatementSetter);
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
        return queryForObject(sql, rowMapper, new ArgumentPreparedStatementSetter(args));
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, PreparedStatementSetter preparedStatementSetter) {
        List<T> results = query(sql, rowMapper, preparedStatementSetter);
        validateSingleResult(results);
        return results.getFirst();
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) {
        return query(sql, rowMapper, new ArgumentPreparedStatementSetter(args));
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, PreparedStatementSetter preparedStatementSetter) {
        StatementExecutor<List<T>> statementExecutor = preparedStatement -> mapRows(preparedStatement, rowMapper);
        return executeStatement(sql, statementExecutor, preparedStatementSetter);
    }

    private <T> T executeStatement(
            String sql,
            StatementExecutor<T> statementExecutor,
            PreparedStatementSetter preparedStatementSetter) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement preparedStatement = prepareStatement(connection, sql, preparedStatementSetter)) {
            log.debug("query : {}", sql);

            return statementExecutor.execute(preparedStatement);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);

            throw new DataAccessException(e);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    private PreparedStatement prepareStatement(
            Connection connection, String sql, PreparedStatementSetter preparedStatementSetter) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatementSetter.setValues(preparedStatement);
        return preparedStatement;
    }

    private <T> List<T> mapRows(PreparedStatement preparedStatement, RowMapper<T> rowMapper) throws SQLException {
        ResultSet resultSet = preparedStatement.executeQuery();

        List<T> results = new ArrayList<>();
        while (resultSet.next()) {
            results.add(rowMapper.mapRow(resultSet));
        }
        return results;
    }

    private <T> void validateSingleResult(List<T> results) {
        if (results.isEmpty()) {
            throw new DataAccessException("쿼리 실행 결과가 1개이기를 기대했지만, 0개입니다.");
        }

        if (results.size() > 1) {
            throw new DataAccessException("쿼리 실행 결과가 1개이기를 기대했지만, 2개 이상입니다.");
        }
    }
}
