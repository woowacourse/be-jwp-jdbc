package com.interface21.jdbc.core;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcTemplate {

    private static final int OBJECT_INDEX = 0;
    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(String sql, PreparedStatementSetter preparedStatementSetter) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatementSetter.setValues(preparedStatement);
            preparedStatement.executeUpdate();
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
    }

    public <T> T queryForObject(String sql, PreparedStatementSetter preparedStatementSetter, RowMapper<T> rowMapper) {
        return query(sql, preparedStatementSetter, rowMapper).get(OBJECT_INDEX);
    }

    public <T> List<T> queryForList(String sql, PreparedStatementSetter preparedStatementSetter, RowMapper<T> rowMapper) {
        return query(sql, preparedStatementSetter, rowMapper);
    }

    private <T> List<T> query(String sql, PreparedStatementSetter preparedStatementSetter, RowMapper<T> rowMapper) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatementSetter.setValues(preparedStatement);
            ResultSet resultSet = preparedStatement.executeQuery();

            List<T> results = new ArrayList<>();
            while (resultSet.next()) {
                results.add(rowMapper.mapRow(resultSet));
            }
            return results;
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
    }
}
