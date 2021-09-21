package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import nextstep.exception.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(String query, Object... values) {

        PreparedStatementSetter pstmtSetter = valuesPreparedStatementSetter(values);

        try (Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)){

            log.debug("query : {}", query);

            pstmtSetter.setValues(pstmt);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e.getMessage());
        }
    }

    public <T> T queryForObject(String query, RowMapper<T> rowMapper, Object... values) {

        PreparedStatementSetter pstmtSetter = valuesPreparedStatementSetter(values);

        try (Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)){

            log.debug("query : {}", query);

            pstmtSetter.setValues(pstmt);
            ResultSet rs = executeQuery(pstmt);
            rs.next();
            return rowMapper.mapRow(rs);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e.getMessage());
        }
    }

    public <T> List<T> queryForList(String query, RowMapper<T> rowMapper, Object... values) {
        PreparedStatementSetter pstmtSetter = valuesPreparedStatementSetter(values);

        try (Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)){

            log.debug("query : {}", query);

            pstmtSetter.setValues(pstmt);
            ResultSet rs = executeQuery(pstmt);

            List<T> result = new ArrayList<>();

            while(rs.next()) {
                result.add(rowMapper.mapRow(rs));
            }
            return result;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e.getMessage());
        }
    }

    private PreparedStatementSetter valuesPreparedStatementSetter(Object[] values) {
        return new valuesPreparedStatementSetter(values);
    }

    private ResultSet executeQuery(PreparedStatement pstmt) throws SQLException {
        return pstmt.executeQuery();
    }
}
