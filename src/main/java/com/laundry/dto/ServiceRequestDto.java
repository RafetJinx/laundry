package com.laundry.dto;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class ServiceRequestDto {

    @JsonView(Views.Create.class)
    @NotNull(
            groups = ValidationGroups.Create.class,
            message = "Name is required for create"
    )
    @NotBlank(
            groups = ValidationGroups.Create.class,
            message = "Name cannot be blank for create"
    )
    @Size(
            max = 255,
            message = "Name must be less than 255 characters"
    )
    String name;

    @JsonView({Views.Create.class, Views.Update.class, Views.Patch.class})
    @Size(
            max = 255,
            message = "Description must be less than 255 characters"
    )
    String description;

    @JsonView({Views.Create.class, Views.Update.class, Views.Patch.class})
    @NotNull(
            groups = {ValidationGroups.Create.class, ValidationGroups.Update.class},
            message = "Price is required"
    )
    @DecimalMin(
            value = "0.0",
            inclusive = false,
            message = "Price must be greater than 0"
    )
    BigDecimal price;

    @JsonView({Views.Create.class, Views.Update.class, Views.Patch.class})
    @NotNull(
            groups = {ValidationGroups.Create.class, ValidationGroups.Update.class},
            message = "Currency code is required"
    )
    @NotBlank(
            groups = {ValidationGroups.Create.class, ValidationGroups.Update.class},
            message = "Currency code cannot be blank"
    )
    @Size(
            min = 3,
            max = 3,
            message = "Currency code must be 3 characters (e.g. 'TRY')"
    )
    String currencyCode;
}
