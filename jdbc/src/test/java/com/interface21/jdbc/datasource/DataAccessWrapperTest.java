package com.interface21.jdbc.datasource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.interface21.dao.DataAccessException;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DataAccessWrapperTest {

    private Connection connection;
    private PreparedStatement preparedStatement;
    private DataSource dataSource;
    private DataAccessWrapper accessWrapper;

    @BeforeEach
    public void setUp() throws SQLException {
        connection = mock(Connection.class);
        dataSource = mock(DataSource.class);
        preparedStatement = mock(PreparedStatement.class);
        accessWrapper = new DataAccessWrapper(dataSource);
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
    }

    @AfterEach
    void tearDown() {
        while (TransactionSynchronizationManager.isTransactionActive()) {
            TransactionSynchronizationManager.popConnection();
        }
    }

    @DisplayName("트랜잭션이 열리지 않은 커넥션의 경우 connection을 닫는다")
    @Test
    void closeConnection_When_TransactionIsUnactive() throws SQLException {
        Object dummy = new Object();
        ThrowingFunction<PreparedStatement, Object, Exception> function = (pstmt) -> dummy;

        accessWrapper.apply("dummySql", function);

        verify(connection, times(1)).close();
    }

    @DisplayName("트랜잭션이 열린 커넥션의 경우 connection을 닫지 않는다.")
    @Test
    void notCloseConnection_When_TransactionIsUnactive() throws SQLException {
        Object dummy = new Object();
        ThrowingFunction<PreparedStatement, Object, Exception> function = (pstmt) -> dummy;

        TransactionSynchronizationManager.pushConnection(mock(Connection.class));
        accessWrapper.apply("dummySql", function);

        verify(connection, never()).close();
    }

    @DisplayName("에러가 발생할 경우 DataAccessException으로 전환된다.")
    @Test
    void throwDataAccessException_When_ExceptionIsOccurred() {
        ThrowingFunction<PreparedStatement, Void, Exception> function = (pstmt) -> {
            throw new Exception("throw exception");
        };

        assertThatThrownBy(() -> accessWrapper.apply("dummySql", function))
                .isInstanceOf(DataAccessException.class);
    }

    @DisplayName("bifunction의 결과값을 전달한다")
    @Test
    void returnValueOfFunctionResult() {
        Object dummy = new Object();

        ThrowingFunction<PreparedStatement, Object, Exception> function = (pstmt) -> dummy;

        assertThat(accessWrapper.apply("dummySql", function)).isEqualTo(dummy);
    }
}
