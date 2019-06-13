package com.revolut.mtt.controller;

import com.revolut.mtt.model.User;
import com.revolut.mtt.repository.UserRepository;
import com.revolut.mtt.validation.ValidationError;
import com.revolut.mtt.validation.ValidationException;
import org.jooby.MediaType;
import org.jooby.Result;
import org.jooby.Results;
import org.jooby.Status;
import org.jooby.mvc.Body;
import org.jooby.mvc.GET;
import org.jooby.mvc.POST;
import org.jooby.mvc.Path;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Entry point for user operations.
 */
@Singleton
@Path("/users")
public class UserController {

    private final UserRepository userRepository;

    @Inject
    public UserController(final UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GET
    @Path("/{userId}")
    public Result fetchUser(final Long userId) throws Exception {
        return userRepository.fetchUser(userId)
                .map(Results::ok)
                .orElseGet(() -> Results.with(Status.NOT_FOUND));
    }

    @POST
    public Result createUser(final @Body User user) throws Exception {
        final List<ValidationError> validationErrors = validateNewUser(user);
        if (!validationErrors.isEmpty()) {
            throw new ValidationException(validationErrors);
        }
        final User createdUser = userRepository.createUser(user);
        return Results.with(createdUser, Status.CREATED)
                .type(MediaType.json);
    }

    private List<ValidationError> validateNewUser(final User user) throws SQLException {
        final List<ValidationError> errors = new ArrayList<>();
        if (user == null) {
            errors.add(ValidationError.builder()
                    .message("User should not be null")
                    .build());
            return errors;
        }

        if (user.getUsername() == null || user.getUsername().isEmpty()) {
            errors.add(ValidationError.builder()
                    .field("username")
                    .message("Username should not be empty")
                    .build());
            return errors;
        }

        if (userRepository.fetchUserByUsername(user.getUsername()).isPresent()) {
            errors.add(ValidationError.builder()
                    .field("username")
                    .message("Username already exists")
                    .build());
        }

        return errors;
    }
}
