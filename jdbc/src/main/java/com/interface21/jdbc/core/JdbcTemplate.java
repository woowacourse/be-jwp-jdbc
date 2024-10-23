package com.interface21.jdbc.core;

import com.interface21.jdbc.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcTemplate {

    private final DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(String sql, PreparedStatementSetter preparedStatementSetter) {
        try (PreparedStatement preparedStatement = DataSourceUtils.getConnection(dataSource).prepareStatement(sql)) {

            preparedStatementSetter.setValues(preparedStatement);
            preparedStatement.executeUpdate();
        } catch (SQLException sqlException) {
            throw new DataAccessException("update 메서드를 실행하는 과정에서 예상치 못한 예외가 발생했습니다.", sqlException);
        }
    }

    public <T> T queryForObject(String sql, PreparedStatementSetter preparedStatementSetter, RowMapper<T> rowMapper) {
        return executeQuery(sql, preparedStatementSetter, rowMapper).getFirst();
    }

    public <T> List<T> queryForList(String sql, PreparedStatementSetter preparedStatementSetter, RowMapper<T> rowMapper) {
        return executeQuery(sql, preparedStatementSetter, rowMapper);
    }

    private <T> List<T> executeQuery(String sql, PreparedStatementSetter preparedStatementSetter, RowMapper<T> rowMapper) {
        try (PreparedStatement preparedStatement = DataSourceUtils.getConnection(dataSource).prepareStatement(sql)) {

            preparedStatementSetter.setValues(preparedStatement);
            ResultSet resultSet = preparedStatement.executeQuery();

            List<T> results = new ArrayList<>();
            while (resultSet.next()) {
                results.add(rowMapper.mapRow(resultSet));
            }
            return results;
        } catch (SQLException sqlException) {
            throw new DataAccessException("query 메서드를 실행하는 과정에서 예상치 못한 예외가 발생했습니다.", sqlException);
        }
    }
}
