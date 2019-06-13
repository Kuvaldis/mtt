package com.revolut.mtt.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

/**
 * Class representing a user. Made immutable.
 */
@Value
@Builder(toBuilder = true, builderClassName = "UserBuilder")
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(builder = User.UserBuilder.class)
public class User {

    private final Long id;

    private final String username;

    @JsonPOJOBuilder(withPrefix = "")
    public static class UserBuilder {
    }
}
