package com.laundry.dto;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class ServicePriceRequestDto {

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
            message = "Currency code must be exactly 3 characters (e.g. 'TRY')"
    )
    String currencyCode;
}
