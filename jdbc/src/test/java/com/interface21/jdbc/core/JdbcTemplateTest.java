package com.interface21.jdbc.core;

import static com.interface21.jdbc.core.fixture.UserFixture.DORA;
import static com.interface21.jdbc.core.fixture.UserFixture.GUGU;
import static org.assertj.core.api.Assertions.assertThat;

import com.interface21.jdbc.core.fixture.DataSourceConfig;
import com.interface21.jdbc.core.fixture.DatabasePopulatorUtils;
import com.interface21.jdbc.core.fixture.User;
import com.interface21.jdbc.core.fixture.UserRowMapper;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    JdbcTemplate jdbcTemplate;
    UserRowMapper userRowMapper;

    @BeforeEach
    void setup() {
        jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());
        userRowMapper = new UserRowMapper();
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
    }

    @Nested
    class Query {
        @Test
        void 데이터_여러개를_조회할_수_있다() {
            // given
            createUser(GUGU.user());
            createUser(DORA.user());

            // when
            List<User> results = jdbcTemplate.query("select * from users", userRowMapper);

            // then
            assertThat(results).hasSize(2);
        }

        @Test
        void 데이터가_없을_땐_빈_리스트를_반환한다() {
            // when
            List<User> results = jdbcTemplate.query("select * from users", userRowMapper);

            // then
            assertThat(results).hasSize(0);
        }
    }

    @Nested
    class Update {
        @Test
        void 데이터를_추가할_수_있다() {
            // given
            User user = GUGU.user();

            // when
            int rowsAffected = jdbcTemplate.update("insert into users (account, password, email) values (?, ?, ?)",
                    user.getAccount(), user.getPassword(), user.getEmail());

            // then
            assertThat(rowsAffected).isEqualTo(1);
            assertThat(findAllUser()).hasSize(1);
            assertThat(findAllUser().getFirst().getAccount()).isEqualTo(user.getAccount());
        }

        @Test
        void 데이터를_업데이트할_수_있다() {
            // given
            createUser(GUGU.user());

            // when
            int rowsAffected = jdbcTemplate.update("UPDATE users SET account = ? WHERE id = ?", "newGugu", 1L);

            // then
            assertThat(rowsAffected).isEqualTo(1);
            assertThat(findAllUser()).hasSize(1);
            assertThat(findAllUser().getFirst().getAccount()).isEqualTo("newGugu");
        }
    }

    private void createUser(User user) {
        jdbcTemplate.update("insert into users (account, password, email) values (?, ?, ?)",
                user.getAccount(), user.getPassword(), user.getEmail());
    }

    private List<User> findAllUser() {
        return jdbcTemplate.query("select * from users", userRowMapper);
    }
}
