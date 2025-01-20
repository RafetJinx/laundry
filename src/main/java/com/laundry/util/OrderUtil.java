package com.laundry.util;

import com.laundry.dto.OrderItemRequestDto;
import com.laundry.dto.OrderRequestDto;
import com.laundry.entity.*;
import com.laundry.exception.BadRequestException;
import com.laundry.mapper.OrderItemMapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class OrderUtil {

    private OrderUtil() {

    }

    /**
     * Attempts to parse the provided {@code statusStr} into a valid
     * {@link PaymentStatus} enum value. If {@code statusStr} is null or empty,
     * this method defaults to {@link PaymentStatus#PENDING}.
     * <p>
     * If the provided string does not match any known enum constant,
     * it throws a {@link BadRequestException}.
     *
     * @param statusStr the string representing a payment status (e.g. "PAID", "REFUNDED")
     * @return a {@link PaymentStatus} enum value, defaulting to {@link PaymentStatus#PENDING} if null/blank
     * @throws BadRequestException if {@code statusStr} is not null/blank and does not match a valid enum constant
     */
    public static PaymentStatus parsePaymentStatus(String statusStr) {
        if (statusStr == null || statusStr.isBlank()) {
            return PaymentStatus.PENDING;
        }
        try {
            return PaymentStatus.valueOf(statusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid payment status: " + statusStr);
        }
    }

    /**
     * Attempts to parse the provided {@code statusStr} into a valid
     * {@link OrderStatus} enum value. If {@code statusStr} is null or empty,
     * this method defaults to {@link OrderStatus#PENDING}.
     * <p>
     * If the provided string does not match any known enum constant,
     * it throws a {@link BadRequestException}.
     *
     * @param statusStr the string representing an order status (e.g. "IN_PROGRESS", "COMPLETED")
     * @return an {@link OrderStatus} enum value, defaulting to {@link OrderStatus#PENDING} if null/blank
     * @throws BadRequestException if {@code statusStr} is not null/blank and does not match a valid enum constant
     */
    public static OrderStatus parseOrderStatus(String statusStr) {
        if (statusStr == null || statusStr.isBlank()) {
            return OrderStatus.PENDING;
        }
        try {
            return OrderStatus.valueOf(statusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid order status: " + statusStr);
        }
    }

    /**
     * Builds a list of {@link OrderItem} entities based on the provided
     * {@link OrderRequestDto#getOrderItems()} list, matching each item with the appropriate
     * {@link Service} entity from {@code foundServices}. If no matching service is found,
     * the corresponding {@link OrderItem} will have a null {@link Service} reference.
     * <p>
     * This method also associates each item with the parent {@link Order}.
     *
     * @param existing       the parent {@link Order} entity to which the items belong
     * @param requestDto     the DTO containing item details, including service IDs
     * @param foundServices  a list of {@link Service} entities matched by ID
     * @return a list of newly constructed {@link OrderItem} entities
     */
    public static List<OrderItem> buildOrderItems(Order existing,
                                                  OrderRequestDto requestDto,
                                                  List<Service> foundServices) {
        List<OrderItem> updatedItems = new ArrayList<>();
        for (OrderItemRequestDto itemDto : requestDto.getOrderItems()) {
            Service matchedService = (foundServices == null)
                    ? null
                    : foundServices.stream()
                    .filter(svc -> svc.getId().equals(itemDto.getServiceId()))
                    .findFirst()
                    .orElse(null);

            OrderItem itemEntity = OrderItemMapper.toEntity(itemDto, existing, matchedService);
            updatedItems.add(itemEntity);
        }
        return updatedItems;
    }

    /**
     * Computes the total cost of all provided {@link OrderItem} objects.
     * <p>
     * The total is calculated by summing up the product of:
     * <ul>
     *   <li>each item's {@code priceAmount} (as a {@link BigDecimal})</li>
     *   <li>each item's quantity (converted from grams to kilograms by dividing by 1000.0)</li>
     * </ul>
     * The result is then scaled to 2 decimal places using {@link RoundingMode#HALF_UP}.
     *
     * @param orderItems the list of {@link OrderItem} entities whose total cost is to be computed.
     * @return the total cost of the given items, or {@link BigDecimal#ZERO} if the list is null or empty.
     */
    public static BigDecimal computeOrderItemsTotal(List<OrderItem> orderItems) {
        if (orderItems == null || orderItems.isEmpty()) {
            return BigDecimal.ZERO;
        }
        double sum = 0.0;
        for (OrderItem item : orderItems) {
            sum += item.getPriceAmount().doubleValue() * (item.getQuantity() / 1000.0);
        }
        return BigDecimal.valueOf(sum).setScale(2, RoundingMode.HALF_UP);
    }
}
