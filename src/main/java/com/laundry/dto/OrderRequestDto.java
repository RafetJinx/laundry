package com.laundry.dto;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.util.List;

@Value
@Builder
public class OrderRequestDto {

    // userId - hangi kullanıcıya ait?
    // Create'de (admin iseniz) set edebilirsiniz, normal user kendisi sipariş oluşturursa
    // Service katmanında "currentUser" üzerinden override de yapılabilir.
    @JsonView(Views.Create.class)
    @NotNull(
            groups = ValidationGroups.Create.class,
            message = "User ID is required for create"
    )
    Long userId;

    // totalAmount
    @JsonView({Views.Create.class, Views.Update.class, Views.Patch.class})
    @NotNull(
            groups = {ValidationGroups.Create.class, ValidationGroups.Update.class},
            message = "Total amount is required"
    )
    @DecimalMin(
            value = "0.0",
            inclusive = false,
            message = "Total amount must be greater than 0"
    )
    BigDecimal totalAmount;

    // currencyCode
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

    // paymentStatus (ör: PENDING, PAID, CANCELLED)
    @JsonView({Views.Create.class, Views.Update.class, Views.Patch.class})
    @Size(
            max = 50,
            message = "Payment status must be less than 50 characters"
    )
    String paymentStatus;

    // orderStatus (ör: "YIKAMA", "KURUTMA", "ÜTÜLEME", "PAKETLEME", "BARKODLAMA")
    @JsonView({Views.Create.class, Views.Update.class, Views.Patch.class})
    @Size(
            max = 50,
            message = "Order status must be less than 50 characters"
    )
    String orderStatus;

    // orderItems: Siparişi oluştururken eklenecek item'lar
    @JsonView({Views.Create.class, Views.Update.class, Views.Patch.class})
    List<OrderItemRequestDto> orderItems;
}
