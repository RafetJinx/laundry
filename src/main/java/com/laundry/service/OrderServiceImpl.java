package com.laundry.service;

import com.laundry.dto.OrderItemRequestDto;
import com.laundry.dto.OrderRequestDto;
import com.laundry.dto.OrderResponseDto;
import com.laundry.entity.*;
import com.laundry.exception.AccessDeniedException;
import com.laundry.exception.BadRequestException;
import com.laundry.exception.NotFoundException;
import com.laundry.helper.RoleGuard;
import com.laundry.mapper.OrderMapper;
import com.laundry.repository.*;
import com.laundry.util.OrderUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
@Transactional
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private ServicePriceRepository servicePriceRepository;
    @Autowired
    private OrderSHistoryRepository orderSHistoryRepository;
    @Autowired
    private OrderPSHistoryRepository orderPSHistoryRepository;

    @Override
    public OrderResponseDto createOrder(OrderRequestDto requestDto,
                                        Long currentUserId,
                                        String currentUserRole)
            throws NotFoundException, AccessDeniedException, BadRequestException {

        User user = userRepository.findById(requestDto.getUserId())
                .orElseThrow(() -> new NotFoundException("User not found: " + requestDto.getUserId()));

        RoleGuard.requireAdminRole(currentUserRole, "You do not have permission to create an order");

        List<Service> foundServices = serviceRepository.findAllById(extractServiceIds(requestDto));

        Order order = OrderMapper.toEntity(requestDto, user, foundServices);
        autoCalculateItemPrices(order);
        order.setTotalAmount(OrderUtil.computeOrderItemsTotal(order.getOrderItems()));

        orderRepository.save(order);

        return OrderMapper.toResponseDto(order);
    }

    @Override
    public OrderResponseDto updateOrder(Long id,
                                        OrderRequestDto requestDto,
                                        Long currentUserId,
                                        String currentUserRole)
            throws NotFoundException, AccessDeniedException, BadRequestException {

        Order existing = getExistingOrder(id);
        RoleGuard.requireAdminRole(currentUserRole, "You do not have permission to update this order");

        List<Long> serviceIds = extractServiceIds(requestDto);
        List<Service> foundServices = serviceRepository.findAllById(extractServiceIds(requestDto));

        updateEntity(existing, requestDto, foundServices, currentUserId);
        autoCalculateItemPrices(existing);
        existing.setTotalAmount(OrderUtil.computeOrderItemsTotal(existing.getOrderItems()));

        orderRepository.save(existing);

        return OrderMapper.toResponseDto(existing);
    }

    @Override
    public OrderResponseDto patchOrder(Long id,
                                       OrderRequestDto requestDto,
                                       Long currentUserId,
                                       String currentUserRole) {
        Order existing = getExistingOrder(id);
        RoleGuard.requireAdminRole(currentUserRole, "No permission to patch this order");
        List<Service> foundServices = serviceRepository.findAllById(extractServiceIds(requestDto));

        patchEntity(existing, requestDto, foundServices, currentUserId);
        autoCalculateItemPrices(existing);
        existing.setTotalAmount(OrderUtil.computeOrderItemsTotal(existing.getOrderItems()));

        orderRepository.save(existing);
        return OrderMapper.toResponseDto(existing);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponseDto getOrderById(Long id,
                                         Long currentUserId,
                                         String currentUserRole)
            throws NotFoundException, AccessDeniedException {

        Order order = getExistingOrder(id);

        RoleGuard.requireAdminRole(currentUserRole, "You do not have permission to view this order");

        return OrderMapper.toResponseDto(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDto> getAllOrders(Long currentUserId, String currentUserRole)
            throws AccessDeniedException {

        RoleGuard.requireAdminRole(currentUserRole, "You do not have permission to view all orders");
        return orderRepository.findAll().stream()
                .map(OrderMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteOrder(Long id,
                            Long currentUserId,
                            String currentUserRole)
            throws NotFoundException, AccessDeniedException {

        Order order = getExistingOrder(id);
        RoleGuard.requireAdminRole(currentUserRole, "You do not have permission to delete this order");
        orderRepository.delete(order);
    }

    @Override
    public OrderResponseDto advanceOrderStatus(Long orderId,
                                               Long currentUserId,
                                               String currentUserRole)
            throws NotFoundException, AccessDeniedException, BadRequestException {

        RoleGuard.requireAdminRole(currentUserRole, "Only admin can change order status");

        Order order = getExistingOrder(orderId);
        OrderStatus currentStatus = order.getStatus();
        OrderStatus nextStatus;

        switch (currentStatus) {
            case PENDING -> {
                order.setStatus(OrderStatus.IN_PROGRESS);
                nextStatus = OrderStatus.IN_PROGRESS;
            }
            case IN_PROGRESS -> {
                order.setStatus(OrderStatus.COMPLETED);
                nextStatus = OrderStatus.COMPLETED;
            }
            case COMPLETED -> {
                order.setStatus(OrderStatus.DELIVERED);
                nextStatus = OrderStatus.DELIVERED;
            }
            case DELIVERED -> {
                throw new BadRequestException("Order is already DELIVERED. No further status change possible.");
            }
            default -> {
                throw new BadRequestException("Cannot advance from status: " + currentStatus);
            }
        }

        // Record this status transition
        OrderStatusHistory orderStatusHistory = new OrderStatusHistory();
        orderStatusHistory.setOrder(order);
        orderStatusHistory.setOldStatus(currentStatus);
        orderStatusHistory.setNewStatus(nextStatus);
        orderStatusHistory.setChangedAt(LocalDateTime.now());
        orderStatusHistory.setChangedBy(currentUserId);

        orderRepository.save(order);
        orderSHistoryRepository.save(orderStatusHistory);

        return OrderMapper.toResponseDto(order);
    }

    /**
     * Retrieves an existing {@link Order} entity by its unique identifier.
     * If the order is not found, throws a {@link NotFoundException}.
     *
     * @param id the unique identifier of the order
     * @return an existing {@link Order} entity
     * @throws NotFoundException if no order with the given {@code id} exists
     */
    private Order getExistingOrder(Long id) throws NotFoundException {
        return orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Order not found with id: " + id));
    }

    /**
     * Extracts a distinct list of service IDs from the {@code orderItems}
     * in the provided {@link OrderRequestDto}, in order to perform batch
     * lookups.
     *
     * @param requestDto the DTO containing order items
     * @return a list of distinct service IDs, or an empty list if no items
     */
    private List<Long> extractServiceIds(OrderRequestDto requestDto) {
        if (requestDto.getOrderItems() == null) {
            return Collections.emptyList();
        }
        return requestDto.getOrderItems().stream()
                .map(OrderItemRequestDto::getServiceId)
                .distinct()
                .toList();
    }

    /**
     * Applies partial updates from an {@link OrderRequestDto} to an
     * existing {@link Order} entity. The logic for updating is the same as
     * {@link #updateEntity(Order, OrderRequestDto, List, Long)}, allowing for
     * shared code between {@code patchOrder} and {@code updateOrder}.
     *
     * @param existing the existing {@link Order} entity to patch
     * @param requestDto the DTO containing partial order updates
     * @param foundServices a list of {@link Service} entities that match the item IDs
     * @param currentUserId the ID of the currently logged-in user
     */
    private void patchEntity(Order existing,
                             OrderRequestDto requestDto,
                             List<Service> foundServices,
                             Long currentUserId) {
        updateEntity(existing, requestDto, foundServices, currentUserId);
    }

    /**
     * Updates an existing {@link Order} entity's fields (either partial or full)
     * based on values provided in the {@link OrderRequestDto}.
     * <ul>
     *   <li>Updates the currency code if provided.</li>
     *   <li>Updates the payment status if provided and logs the change in
     *       {@link OrderPaymentStatusHistory}.</li>
     *   <li>Updates the order status if provided.</li>
     *   <li>Replaces the existing order items if a new list is provided.</li>
     * </ul>
     *
     * @param existing the existing {@link Order} entity to update
     * @param requestDto the DTO containing new order values
     * @param foundServices the list of {@link Service} entities matched by ID
     * @param currentUserId the ID of the currently logged-in user
     * @throws BadRequestException if the payment status or item service references are invalid
     */
    private void updateEntity(Order existing,
                              OrderRequestDto requestDto,
                              List<Service> foundServices,
                              Long currentUserId) {
        if (requestDto.getCurrencyCode() != null) {
            existing.setCurrencyCode(requestDto.getCurrencyCode());
        }
        if (requestDto.getPaymentStatus() != null) {
            PaymentStatus newPaymentStatus = OrderUtil.parsePaymentStatus(requestDto.getPaymentStatus());

            OrderPaymentStatusHistory orderPaymentStatusHistory = new OrderPaymentStatusHistory();
            orderPaymentStatusHistory.setOrder(existing);
            orderPaymentStatusHistory.setOldPaymentStatus(existing.getPaymentStatus());
            orderPaymentStatusHistory.setNewPaymentStatus(newPaymentStatus);
            orderPaymentStatusHistory.setChangedAt(LocalDateTime.now());
            orderPaymentStatusHistory.setChangedBy(currentUserId);

            existing.setPaymentStatus(newPaymentStatus);
            orderPSHistoryRepository.save(orderPaymentStatusHistory);
        }
        if (requestDto.getOrderStatus() != null) {
            existing.setStatus(OrderUtil.parseOrderStatus(requestDto.getOrderStatus()));
        }

        if (requestDto.getOrderItems() != null) {
            List<OrderItem> updatedItems = OrderUtil.buildOrderItems(existing, requestDto, foundServices);
            existing.getOrderItems().clear();
            existing.getOrderItems().addAll(updatedItems);
        }
    }

    /**
     * Automatically calculates prices for any {@link OrderItem} that does not
     * have a {@code priceAmount} set. The calculation is based on:
     * <ul>
     *   <li>The matching {@link ServicePrice} (identified by the item's service ID and the order's currency).</li>
     *   <li>The item's quantity (converted from grams to kilograms).</li>
     *   <li>Multiplies {@code price} by the item's weight in kilograms.</li>
     * </ul>
     *
     * @param order the {@link Order} entity containing items to be updated
     * @throws BadRequestException if a service or corresponding service price is missing
     */
    private void autoCalculateItemPrices(Order order) {
        for (OrderItem item : order.getOrderItems()) {
            // If price is already set, skip
            if (item.getPriceAmount() == null) {
                Service svc = item.getService();
                if (svc == null) {
                    throw new BadRequestException("No service specified for item");
                }

                // Retrieve the matching service price record
                ServicePrice sp = servicePriceRepository
                        .findByServiceIdAndCurrencyCode(svc.getId(), order.getCurrencyCode())
                        .orElseThrow(() -> new BadRequestException(
                                "No ServicePrice for service=" + svc.getId()
                                        + " and currency=" + order.getCurrencyCode()
                        ));

                // Convert grams to kilograms
                BigDecimal kg = BigDecimal.valueOf(item.getQuantity())
                        .divide(BigDecimal.valueOf(1000), 3, RoundingMode.HALF_UP);

                // Multiply service price by item weight
                BigDecimal autoPrice = sp.getPrice()
                        .multiply(kg)
                        .setScale(2, RoundingMode.HALF_UP);

                item.setPriceAmount(autoPrice);
            }
        }
    }

}
