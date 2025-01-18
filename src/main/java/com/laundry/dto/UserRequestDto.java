package com.laundry.dto;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UserRequestDto {

    @JsonView(Views.Create.class)
    @NotNull(
            groups = ValidationGroups.Create.class,
            message = "Username is required (Create)"
    )
    @NotBlank(
            groups = ValidationGroups.Create.class,
            message = "Username is required (Create)"
    )
    @Size(
            max = 255,
            message = "Username must be less than 255 characters"
    )
    String username;

    @JsonView({Views.Create.class, Views.Update.class, Views.Patch.class})
    @Size(
            max = 255,
            message = "DisplayName must be less than 255 characters"
    )
    String displayName;

    @JsonView({Views.Create.class, Views.Update.class, Views.Patch.class})
    @NotNull(
            groups = {ValidationGroups.Create.class, ValidationGroups.Update.class},
            message = "Password is required"
    )
    @NotBlank(
            groups = {ValidationGroups.Create.class, ValidationGroups.Update.class},
            message = "Password cannot be blank"
    )
    @Size(
            max = 255,
            message = "Password must be less than 255 characters"
    )
    @Size(
            min = 8,
            message = "Password must be at least 8 characters"
    )
    String password;

    @JsonView({Views.Create.class, Views.Update.class, Views.Patch.class})
    @NotNull(
            groups = {ValidationGroups.Create.class, ValidationGroups.Update.class},
            message = "Email is required"
    )
    @NotBlank(
            groups = {ValidationGroups.Create.class, ValidationGroups.Update.class},
            message = "Email cannot be blank"
    )
    @Size(
            max = 255,
            message = "Email must be less than 255 characters"
    )
    @Email(
            message = "Email should be valid",
            groups = {ValidationGroups.Create.class, ValidationGroups.Update.class}
    )
    String email;

    @JsonView({Views.Create.class, Views.Update.class, Views.Patch.class})
    @Size(
            max = 20,
            message = "Phone must be less than 20 characters"
    )
    String phone;

    @JsonView({Views.Create.class, Views.Update.class, Views.Patch.class})
    @Size(
            max = 255,
            message = "Address must be less than 255 characters"
    )
    String address;

    @JsonView({Views.Create.class, Views.Update.class, Views.Patch.class})
    @Size(
            max = 50,
            message = "Role must be less than 50 characters"
    )
    String role;
}
