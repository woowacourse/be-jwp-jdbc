package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(String sql, Object... args) {
        execute(sql, PreparedStatement::executeUpdate, args);
    }

    public Object queryForObject(String sql, RowMapper rowMapper, Object... args) {
        return query(sql, rowMapper::mapRow, args);
    }

    public List<Object> queryForList(String sql, RowMapper rowMapper) {
        return queryList(sql, rowMapper);
    }

    private <T> T execute(String sql, ExecuteStrategy<T> strategy, Object... args) {
        try (Connection conn = dataSource.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            putArguments(pstmt, args);
            return strategy.execute(pstmt);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    private <T> T query(String sql, RowMapper<T> rowMapper, Object... args) {
        try (Connection conn = dataSource.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            putArguments(pstmt, args);

            ResultSet resultSet = pstmt.executeQuery();
            List<T> result = new ArrayList<>();
            for (int i = 0; resultSet.next(); i++) {
                result.add(rowMapper.mapRow(resultSet, i));
            }
            return result.get(0);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    private <T> List<T> queryList(String sql, RowMapper<T> rowMapper) {
        try (Connection conn = dataSource.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            ResultSet resultSet = pstmt.executeQuery();
            List<T> result = new ArrayList<>();
            for (int i = 0; resultSet.next(); i++) {
                result.add(rowMapper.mapRow(resultSet, i));
            }
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    private void putArguments(PreparedStatement pstmt, Object[] args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            pstmt.setObject(i + 1, args[i]);
        }
    }
}
