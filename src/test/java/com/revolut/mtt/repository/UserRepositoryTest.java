package com.revolut.mtt.repository;

import com.github.database.rider.core.api.connection.ConnectionHolder;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.junit5.DBUnitExtension;
import com.revolut.mtt.model.User;
import com.revolut.mtt.database.SchemaInit;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(DBUnitExtension.class)
class UserRepositoryTest {

    private UserRepository userRepository;

    private static ConnectionHolder connectionHolder = () -> DriverManager.getConnection("jdbc:h2:mem:mtt-test;DB_CLOSE_DELAY=-1");

    @BeforeAll
    static void beforeAll() throws SQLException {
        SchemaInit.initSchema(connectionHolder.getConnection());
    }

    @BeforeEach
    void setUp() {
        this.userRepository = new UserRepository(connectionHolder::getConnection);
    }

    @Test
    @DataSet("existing_users.yml")
    void should_fetch_user_by_id() throws SQLException {
        // given
        final Long userId = 2L;

        /// when
        final Optional<User> optionalUser = userRepository.fetchUser(userId);

        // then
        assertTrue(optionalUser.isPresent());
        final User user = optionalUser.get();
        assertEquals(2L, user.getId());
        assertEquals("rachel", user.getUsername());
    }

    @Test
    @DataSet("existing_users.yml")
    void should_not_fetch_non_existing_user() throws SQLException {
        // given
        final Long userId = 32L;

        /// when
        final Optional<User> optionalUser = userRepository.fetchUser(userId);

        // then
        assertFalse(optionalUser.isPresent());
    }

    @Test
    @DataSet("existing_users.yml")
    void should_fetch_user_by_username() throws SQLException {
        // given
        final String username = "chandler";

        /// when
        final Optional<User> optionalUser = userRepository.fetchUserByUsername(username);

        // then
        assertTrue(optionalUser.isPresent());
        final User user = optionalUser.get();
        assertEquals(3L, user.getId());
        assertEquals("chandler", user.getUsername());
    }

    @Test
    @DataSet("existing_users.yml")
    void should_not_fetch_non_existing_user_by_username() throws SQLException {
        // given
        final String username = "stive";

        /// when
        final Optional<User> optionalUser = userRepository.fetchUserByUsername(username);

        // then
        assertFalse(optionalUser.isPresent());
    }

    @Test
    @DataSet("existing_users.yml")
    void should_create_new_user() throws SQLException {
        // given
        final User newUser = User.builder()
                .username("monica")
                .build();

        // when
        final User user = userRepository.createUser(newUser);

        // then
        assertNotNull(user);
        assertNotNull(user.getId());
        assertEquals("monica", user.getUsername());
    }
}