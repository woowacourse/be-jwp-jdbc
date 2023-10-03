package org.springframework.jdbc.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T query(final String sql, final PreparedStatementExecutor<T> preparedStatementExecutor, final Object... arguments) {
        try (final var connection = dataSource.getConnection();
             final var preparedStatement = connection.prepareStatement(sql)) {
            log.debug("query : {}", sql);

            setObjectToPreparedStatement(preparedStatement, arguments);

            return preparedStatementExecutor.execute(preparedStatement);
        } catch (final SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private void setObjectToPreparedStatement(final PreparedStatement preparedStatement, final Object... arguments) throws SQLException {
        for (int parameterIndex = 1; parameterIndex < arguments.length + 1; parameterIndex++) {
            preparedStatement.setObject(parameterIndex, arguments[parameterIndex - 1]);
        }
    }

    public <T> List<T> queryForList(final String sql, final RowMapper<T> rowMapper) {
        return query(sql, preparedStatement -> {
            final var resultSet = preparedStatement.executeQuery();
            return getObjects(rowMapper, resultSet);
        });
    }

    private <T> List<T> getObjects(final RowMapper<T> rowMapper, final ResultSet resultSet) throws SQLException {
        final List<T> result = new ArrayList<>();
        while (resultSet.next()) {
            final T data = rowMapper.map(resultSet);
            result.add(data);
        }
        return result;
    }

    public <T> Optional<T> queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... arguments) {
        final List<T> results = query(sql, preparedStatement -> {
            final ResultSet resultSet = preparedStatement.executeQuery();
            return getObjects(rowMapper, resultSet);
        }, arguments);
        return Optional.ofNullable(results.iterator().next());
    }

    public void update(final String sql, final Object... arguments) {
        query(sql, PreparedStatement::executeUpdate, arguments);
    }
}
