package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.RowMapper;
import com.interface21.jdbc.querybuilder.ConditionExpression;
import com.interface21.jdbc.querybuilder.QueryBuilder;
import com.interface21.jdbc.querybuilder.query.Query;
import com.techcourse.dao.rowmapper.UserRowMapper;
import com.techcourse.domain.User;
import java.sql.Connection;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);
    private static final RowMapper<User> rowMapper = new UserRowMapper();

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(Connection connection, User user) {
        Query query = createQueryForInsert();
        jdbcTemplate.queryForUpdate(connection, query.getSql(), user.getAccount(), user.getPassword(),
                user.getEmail());
    }

    public void update(Connection connection, User user) {
        Query query = createQueryForUpdate();

        jdbcTemplate.queryForUpdate(
                connection,
                query.getSql(),
                user.getAccount(),
                user.getPassword(),
                user.getEmail(),
                user.getId()
        );
    }

    public List<User> findAll(Connection connection) {
        Query query = new QueryBuilder()
                .selectFrom("users")
                .build();
        return jdbcTemplate.query(connection, query.getSql(), rowMapper);
    }

    public User findById(Connection connection, Long id) {
        Query query = resolveEqualSql("id");
        return jdbcTemplate.queryForObject(connection, query.getSql(), rowMapper, id);
    }

    public User findByAccount(Connection connection, String account) {
        Query query = resolveEqualSql("account");
        return jdbcTemplate.queryForObject(connection, query.getSql(), rowMapper, account);
    }

    private Query createQueryForInsert() {
        return new QueryBuilder()
                .insert("account", "password", "email")
                .into("users")
                .build();
    }

    private Query createQueryForUpdate() {
        return new QueryBuilder()
                .update("users")
                .set("account", "password", "email")
                .where(ConditionExpression.eq("id"))
                .build();
    }

    public Query resolveEqualSql(String fieldName) {
        return new QueryBuilder()
                .selectFrom("users")
                .where(ConditionExpression.eq(fieldName))
                .build();
    }
}
