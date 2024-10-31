package com.techcourse.dao;

import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.PreparedStatementSetter;
import com.techcourse.domain.User;

public class UserDao {

	private static final Logger log = LoggerFactory.getLogger(UserDao.class);

	private final JdbcTemplate jdbcTemplate;
	private final UserRowMapper userRowMapper;

	public UserDao(final DataSource dataSource) {
		this(new JdbcTemplate(dataSource));
	}

	public UserDao(final JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
		this.userRowMapper = UserRowMapper.getInstance();
	}

	public void insert(final User user) {
		final var sql = "insert into users (account, password, email) values (?, ?, ?)";

		jdbcTemplate.update(sql, pstmt -> {
			pstmt.setString(1, user.getAccount());
			pstmt.setString(2, user.getPassword());
			pstmt.setString(3, user.getEmail());
		});
	}

	public void update(final User user) {
		final var sql = "update users set account = ?, password = ?, email = ? where id = ?";

		jdbcTemplate.update(sql, pstmt -> {
			pstmt.setString(1, user.getAccount());
			pstmt.setString(2, user.getPassword());
			pstmt.setString(3, user.getEmail());
			pstmt.setLong(4, user.getId());
		});
	}

	public List<User> findAll() {
		final var sql = "select id, account, password, email from users";

		return jdbcTemplate.query(sql, userRowMapper, pstmt -> {
		});
	}

	public User findById(final Long id) {
		final var sql = "select id, account, password, email from users where id = ?";
		PreparedStatementSetter pss = pstmt -> pstmt.setLong(1, id);

		return jdbcTemplate.queryForObject(sql, userRowMapper, pss);
	}

	public User findByAccount(final String account) {
		final var sql = "select id, account, password, email from users where account = ?";
		PreparedStatementSetter pss = pstmt -> pstmt.setString(1, account);

		return jdbcTemplate.queryForObject(sql, userRowMapper, pss);
	}
}
