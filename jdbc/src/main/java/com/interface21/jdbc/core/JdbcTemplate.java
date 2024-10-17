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

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(String sql, PreparedStatementSetter preparedStatementSetter) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatementSetter.setValues(preparedStatement);
            int rowCount = preparedStatement.executeUpdate();
            log.debug("query : {}, rowCount : {}", sql, rowCount);
            return rowCount;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        } finally {
            try {
                if (connection.getAutoCommit()) {
                    DataSourceUtils.releaseConnection(connection, dataSource);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, PreparedStatementSetter preparedStatementSetter) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatementSetter.setValues(preparedStatement);
            ResultSet resultSet = preparedStatement.executeQuery();
            return getObject(rowMapper, resultSet);
        } catch (SQLException e) {
            log.error("SQL error during queryForObject: {}", e.getMessage(), e);
            throw new DataAccessException(e);
        } finally {
            try {
                if (connection.getAutoCommit()) {
                    DataSourceUtils.releaseConnection(connection, dataSource);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, PreparedStatementSetter preparedStatementSetter) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        List<T> result = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatementSetter.setValues(preparedStatement);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                T row = rowMapper.mapRow(resultSet, resultSet.getRow());
                result.add(row);
            }
            log.debug("query : {}", sql);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        } finally {
            try {
                if (connection.getAutoCommit()) {
                    DataSourceUtils.releaseConnection(connection, dataSource);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        return result;
    }

    private <T> T getObject(RowMapper<T> rowMapper, ResultSet resultSet) throws SQLException {
        if (resultSet.next()) {
            return rowMapper.mapRow(resultSet, resultSet.getRow());
        }
        return null;
    }
}
