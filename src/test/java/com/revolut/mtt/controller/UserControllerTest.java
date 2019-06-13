package com.revolut.mtt.controller;

import com.revolut.mtt.model.User;
import com.revolut.mtt.repository.UserRepository;
import com.revolut.mtt.validation.ValidationError;
import com.revolut.mtt.validation.ValidationException;
import org.jooby.Result;
import org.jooby.Status;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @InjectMocks
    private UserController userController;

    @Mock
    private UserRepository userRepository;

    @Test
    void existing_user_should_be_returned_from_repository() throws Exception {
        // given
        final long userId = 1L;
        when(userRepository.fetchUser(userId))
                .thenReturn(Optional.of(new User(userId, "ross")));

        // when
        final Result userResult = userController.fetchUser(userId);

        // then
        assertNotNull(userResult);
        assertEquals(Optional.of(Status.OK), userResult.status());
        assertEquals(new User(userId, "ross"), userResult.get());
        verify(userRepository).fetchUser(userId);
    }

    @Test
    void non_existing_user_should_return_not_found_status() throws Exception {
        // given
        final long userId = 2L;

        // when
        final Result userResult = userController.fetchUser(userId);

        // then
        assertNotNull(userResult);
        assertEquals(Optional.of(Status.NOT_FOUND), userResult.status());
        verify(userRepository).fetchUser(userId);
    }

    @Test
    void user_should_be_created_and_saved_in_repository() throws Exception {
        // given
        final User user = new User(null, "rachel");
        when(userRepository.createUser(user))
                .thenReturn(new User(1L, "rachel"));

        // when
        final Result createdUserResult = userController.createUser(user);

        // then
        assertNotNull(createdUserResult);
        assertEquals(Optional.of(Status.CREATED), createdUserResult.status());
        assertEquals(new User(1L, "rachel"), createdUserResult.get());
        verify(userRepository).createUser(user);
    }

    @Test
    void user_without_username_should_not_be_created() {
        // given
        final User user = User.builder().build();

        // when
        final ValidationException validationException =
                assertThrows(ValidationException.class, () -> userController.createUser(user));

        // then
        final List<ValidationError> errors = validationException.getErrors();
        assertEquals(1, errors.size());
        final ValidationError error = errors.get(0);
        assertEquals("username", error.getField());
    }

    @Test
    void user_with_existing_username_should_not_be_created() throws SQLException {
        // given
        final User user = new User(null, "phoebe");
        when(userRepository.fetchUserByUsername("phoebe"))
                .thenReturn(Optional.of(new User(1L, "phoebe")));

        // when
        final ValidationException validationException =
                assertThrows(ValidationException.class, () -> userController.createUser(user));

        // then
        final List<ValidationError> errors = validationException.getErrors();
        assertEquals(1, errors.size());
        final ValidationError error = errors.get(0);
        assertEquals("username", error.getField());
    }
}