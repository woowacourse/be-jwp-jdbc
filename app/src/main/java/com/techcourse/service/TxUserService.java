package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.Callable;
import javax.sql.DataSource;

public class TxUserService implements UserService {

	private final UserService userService;

	public TxUserService(final UserService userService) {
		this.userService = userService;
	}

	@Override
	public User findById(final long id) {
		return executeInReadOnly(() -> userService.findById(id));
	}

	@Override
	public void insert(final User user) {
		executeInTransaction(() -> userService.insert(user));
	}

	@Override
	public void changePassword(final long id, final String newPassword, final String createdBy) {
		executeInTransaction(() -> userService.changePassword(id, newPassword, createdBy));
	}

	private void executeInTransaction(final Runnable runnable) {
		DataSource dataSource = DataSourceConfig.getInstance();
		Connection connection = DataSourceUtils.getConnection(dataSource);
		TransactionSynchronizationManager.bindResource(dataSource, connection);

		try {
			connection.setAutoCommit(false);
			runnable.run();
			connection.commit();
		} catch (Exception e) {
			rollbackWithException(connection, e);
		} finally {
			DataSourceUtils.releaseConnection(connection, dataSource);
		}
	}

	private <T> T executeInReadOnly(final Callable<T> callable) {
		DataSource dataSource = DataSourceConfig.getInstance();
		Connection connection = DataSourceUtils.getConnection(dataSource);

		try {
			connection.setAutoCommit(false);
			connection.setReadOnly(true);
			T result = callable.call();
			connection.commit();

			return result;
		} catch (Exception e) {
			rollbackWithException(connection, e);
		} finally {
			DataSourceUtils.releaseConnection(connection, dataSource);
		}

		return null;
	}

	private void rollbackWithException(final Connection conn, final Exception e) {
		try {
			conn.rollback();
		} catch (SQLException rollbackException) {
			throw new DataAccessException(rollbackException);
		}
		throw new DataAccessException(e);
	}
}
