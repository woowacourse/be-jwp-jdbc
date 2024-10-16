package com.interface21.jdbc.core;

import com.interface21.jdbc.datasource.DataSourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int executeUpdate(String sql, Object... parameters) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try(PreparedStatement pstmt = connection.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            setParameters(pstmt, parameters);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new JdbcTemplateException(e);
        }
    }

    public <T> T execute(String sql, RowMapper<T> rowMapper, Object... parameters) {
        return executeQuery(sql, rs -> getResult(rs, rowMapper), parameters);
    }

    public <T> List<T> executeList(String sql, RowMapper<T> rowMapper, Object... parameters){
        return executeQuery(sql, rs -> getResults(rs, rowMapper), parameters);
    }

    private <T> T executeQuery(String sql, Function<ResultSet, T> func , Object... parameters) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = executeQuery(pstmt, parameters)) {
            log.debug("query : {}", sql);

            return func.apply(rs);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new JdbcTemplateException(e);
        }
    }

    private ResultSet executeQuery(PreparedStatement pstmt, Object... parameters) throws SQLException {
        setParameters(pstmt, parameters);
        return pstmt.executeQuery();
    }

    private void setParameters(PreparedStatement pstmt, Object... parameters) throws SQLException {
        for (int i = 0; i < parameters.length; i++) {
            pstmt.setObject(i + 1, parameters[i]);
        }
    }

    private <T> T getResult(ResultSet rs, RowMapper<T> rowMapper) {
        try {
            T result = null;
            if (rs.next()) {
                result = rowMapper.doMapping(rs);
            }
            if (rs.next()) {
                throw new JdbcTemplateException("두 개 이상의 검색 결과가 존재합니다.");
            }
            return result;
        } catch (SQLException e) {
            throw new JdbcTemplateException(e);
        }
    }

    private <T> List<T> getResults(ResultSet rs, RowMapper<T> rowMapper) {
        try {
            return mapResults(rs, rowMapper);
        } catch (SQLException e) {
            throw new JdbcTemplateException(e);
        }
    }

    private static <T> List<T> mapResults(ResultSet rs, RowMapper<T> rowMapper) throws SQLException {
        List<T> results = new ArrayList<>();
        while (rs.next()) {
            results.add(rowMapper.doMapping(rs));
        }
        return results;
    }
}
