package com.laundry.dto;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class OrderItemRequestDto {

    // serviceId (ilgili Service entity'yi temsil edecek)
    // Create'de zorunlu, Update/Patch senaryosuna göre opsiyonel
    @JsonView(Views.Create.class)
    @NotNull(
            groups = ValidationGroups.Create.class,
            message = "Service ID is required for create"
    )
    Long serviceId;

    // priceAmount
    @JsonView({Views.Create.class, Views.Update.class, Views.Patch.class})
    @NotNull(
            groups = {ValidationGroups.Create.class, ValidationGroups.Update.class},
            message = "Price amount is required"
    )
    @DecimalMin(
            value = "0.0",
            inclusive = false,
            message = "Price amount must be greater than 0"
    )
    BigDecimal priceAmount;

    // quantity
    @JsonView({Views.Create.class, Views.Update.class, Views.Patch.class})
    @NotNull(
            groups = {ValidationGroups.Create.class, ValidationGroups.Update.class},
            message = "Quantity is required"
    )
    @Min(
            value = 1,
            message = "Quantity must be at least 1"
    )
    Integer quantity;
}
