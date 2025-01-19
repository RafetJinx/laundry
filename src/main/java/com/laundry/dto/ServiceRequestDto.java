package com.laundry.dto;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Value;

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
            max = 1020,
            message = "Description must be less than 1020 characters"
    )
    String description;
}
