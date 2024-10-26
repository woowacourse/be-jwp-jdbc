package com.interface21.jdbc.core;

import com.interface21.jdbc.IncorrectResultSizeDataAccessException;
import com.interface21.jdbc.TestUser;
import com.interface21.jdbc.datasource.DataSourceUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class JdbcTemplateTest {

    private final RowMapper<TestUser> userRowMapper = resultSet -> new TestUser(
            resultSet.getLong("id"),
            resultSet.getString("account")
    );
    private JdbcTemplate jdbcTemplate;
    @Mock
    private DataSource dataSource;
    @Mock
    private Connection connection;
    @Mock
    private PreparedStatement preparedStatement;
    @Mock
    private ResultSet resultSet;

    @BeforeEach
    public void setUp() throws SQLException {
        MockitoAnnotations.openMocks(this); // Initialize mocks
        jdbcTemplate = new JdbcTemplate(dataSource);
        when(dataSource.getConnection()).thenReturn(connection);
        DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);
        when(connection.getMetaData()).thenReturn(databaseMetaData);
        when(databaseMetaData.getURL()).thenReturn("jdbc");
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
    }

    @Test
    @DisplayName("업데이트 성공")
    public void testUpdate() throws SQLException {
        // Given
        String sql = "UPDATE users SET account = ? WHERE id = ?";
        Object[] params = {"newAccount", 1};
        when(connection.prepareStatement(sql)).thenReturn(preparedStatement);

        // When
        jdbcTemplate.update(sql, params);

        // Then
        verify(preparedStatement).setObject(1, "newAccount");
        verify(preparedStatement).setObject(2, 1);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    @DisplayName("쿼리로 객체 조회 성공")
    public void testQueryForObject() throws SQLException {
        // Given
        String sql = "SELECT * FROM users WHERE account = ?";
        Object[] params = {"testAccount"};
        TestUser expectedUser = new TestUser(1L, "testAccount");

        when(connection.prepareStatement(sql)).thenReturn(preparedStatement);
        setupResultSetForSingleUser(expectedUser);

        // When
        TestUser actualUser = jdbcTemplate.queryForObject(sql, userRowMapper, params);

        // Then
        assertEquals(expectedUser, actualUser);
    }

    @Test
    @DisplayName("쿼리로 다수 객체 조회 성공")
    public void testQuery() throws SQLException {
        // Given
        String sql = "SELECT * FROM users";
        TestUser user1 = new TestUser(1L, "account1");
        TestUser user2 = new TestUser(2L, "account2");

        when(connection.prepareStatement(sql)).thenReturn(preparedStatement);
        setupResultSetForMultipleUsers(user1, user2);

        // When
        List<TestUser> actualUsers = jdbcTemplate.query(sql, userRowMapper);

        // Then
        assertEquals(2, actualUsers.size());
        assertEquals(user1, actualUsers.get(0));
        assertEquals(user2, actualUsers.get(1));
    }

    @Test
    @DisplayName("객체 조회 시 결과가 없으면 예외 발생")
    public void testQueryForObjectNotFound() throws SQLException {
        // Given
        String sql = "SELECT * FROM users WHERE account = ?";
        Object[] params = {"nonExistentAccount"};

        when(connection.prepareStatement(sql)).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        // When & Then
        assertThrows(NoSuchElementException.class, () -> {
            jdbcTemplate.queryForObject(sql, userRowMapper, params);
        });
    }

    @Test
    @DisplayName("객체 조회 시 다수의 결과가 반환되면 예외 발생")
    public void testQueryForObjectMultipleResults() throws SQLException {
        // Given
        String sql = "SELECT * FROM users WHERE account = ?";
        Object[] params = {"duplicateAccount"};

        when(connection.prepareStatement(sql)).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true).thenReturn(true).thenReturn(false);

        // When & Then
        assertThrows(IncorrectResultSizeDataAccessException.class, () -> {
            jdbcTemplate.queryForObject(sql, userRowMapper, params);
        });
    }


    private void setupResultSetForSingleUser(TestUser expectedUser) throws SQLException {
        when(resultSet.next()).thenReturn(true).thenReturn(false);
        when(resultSet.getLong("id")).thenReturn(expectedUser.getId());
        when(resultSet.getString("account")).thenReturn(expectedUser.getAccount());
    }

    private void setupResultSetForMultipleUsers(TestUser user1, TestUser user2) throws SQLException {
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getLong("id")).thenReturn(user1.getId(), user2.getId());
        when(resultSet.getString("account")).thenReturn(user1.getAccount(), user2.getAccount());
    }
}


