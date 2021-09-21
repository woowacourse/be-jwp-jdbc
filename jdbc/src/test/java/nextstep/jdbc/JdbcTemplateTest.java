package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import nextstep.jdbc.exception.JdbcNotFoundException;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private static final TestUserRowMapper TEST_USER_ROW_MAPPER = new TestUserRowMapper();
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws SQLException, IOException {
        JdbcDataSource dataSource = getJdbcDataSource();

        final String sql = readSqlFile();

        try (Connection conn = dataSource.getConnection();
            Statement statement = conn.createStatement()) {
            statement.execute(sql);
        }

        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    private String readSqlFile() throws IOException {
        final URL url = getClass().getClassLoader().getResource("schema.sql");
        final File file = new File(url.getFile());
        final String sql = Files.readString(file.toPath());
        return sql;
    }

    private JdbcDataSource getJdbcDataSource() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;");
        dataSource.setUser("");
        dataSource.setPassword("");
        return dataSource;
    }

    @DisplayName("sql문을 통해 단일 데이터 조회")
    @Test
    void query() {
        TestUser user = jdbcTemplate.query("select id, account, password, email from users where account = ?", TEST_USER_ROW_MAPPER, "junroot");

        assertThat(user.getAccount()).isEqualTo("junroot");
        assertThat(user.getPassword()).isEqualTo("rootzzang123");
        assertThat(user.getEmail()).isEqualTo("rootjjang@gmail.com");
    }

    @DisplayName("만족하는 데이터가 없는 경우 예외 처리")
    @Test
    void InvalidQuery() {
        assertThatThrownBy(() -> jdbcTemplate.query("select id, account, password, email from users where account = ?", new TestUserRowMapper(), "junriot"))
            .isExactlyInstanceOf(JdbcNotFoundException.class);
    }

    @DisplayName("sql문을 통해 데이터 전체 조회")
    @Test
    void queryAsList() {
        List<TestUser> users = jdbcTemplate.queryAsList("select id, account, password, email from users", TEST_USER_ROW_MAPPER);

        assertThat(users).hasSize(2);
    }
}
