package com.laundry.service.impl;

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
import com.laundry.service.OrderService;
import com.laundry.util.OrderUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import com.laundry.specification.OrderSpecification;

import java.awt.print.PrinterException;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    private final UserRepository userRepository;

    private final ServiceRepository serviceRepository;

    private final ServicePriceRepository servicePriceRepository;

    private final OrderSHistoryRepository orderSHistoryRepository;

    private final ProductRepository productRepository;

    private final OrderPSHistoryRepository orderPSHistoryRepository;

    private final QrCodePrintingService qrCodePrintingService;

    public OrderServiceImpl(OrderRepository orderRepository,
                            UserRepository userRepository,
                            ServiceRepository serviceRepository,
                            ServicePriceRepository servicePriceRepository,
                            OrderSHistoryRepository orderSHistoryRepository,
                            ProductRepository productRepository,
                            OrderPSHistoryRepository orderPSHistoryRepository,
                            QrCodePrintingService qrCodePrintingService) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.serviceRepository = serviceRepository;
        this.servicePriceRepository = servicePriceRepository;
        this.orderSHistoryRepository = orderSHistoryRepository;
        this.productRepository = productRepository;
        this.orderPSHistoryRepository = orderPSHistoryRepository;
        this.qrCodePrintingService = qrCodePrintingService;
    }

    @Override
    public OrderResponseDto createOrder(OrderRequestDto requestDto,
                                        Long currentUserId,
                                        String currentUserRole)
            throws Exception {

        User user = userRepository.findById(requestDto.getUserId())
                .orElseThrow(() -> new NotFoundException("User not found: " + requestDto.getUserId()));

        RoleGuard.requireAdminRole(currentUserRole, "You do not have permission to create an order");

        List<Service> foundServices = serviceRepository.findAllById(extractServiceIds(requestDto));

        Product product = productRepository.findById(requestDto.getProductId())
                .orElseThrow(() -> new NotFoundException("Product not found: " + requestDto.getProductId()));

        Order order = OrderMapper.toEntity(requestDto, user, product, foundServices);

        if (order.getStatus() == null) {
            order.setStatus(OrderStatus.PENDING);
        }
        if (order.getPaymentStatus() == null) {
            order.setPaymentStatus(PaymentStatus.PENDING);
        }
        if (order.getCurrencyCode() == null || order.getCurrencyCode().trim().isBlank()) {
            order.setCurrencyCode("TRY");
        }

        autoCalculateItemPrices(order);
        order.setTotalAmount(OrderUtil.computeOrderItemsTotal(order.getOrderItems()));

        LocalDateTime createdDate = LocalDateTime.now();
        int year = createdDate.getYear() % 100;
        String month = String.format("%02d", createdDate.getMonthValue());
        String day = String.format("%02d", createdDate.getDayOfMonth());

        LocalDateTime orderDate = createdDate.toLocalDate().atStartOfDay();
        LocalDateTime startOfDay = orderDate.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = orderDate.plusDays(1).toLocalDate().atStartOfDay();

        long dailyOrderCount = orderRepository.countByCreatedAtBetween(startOfDay, endOfDay);
        String sequence = String.format("%05d", dailyOrderCount + 1);
        String referenceNumber = String.format("%02d%s%s%s", year, month, day, sequence);


        order.setReferenceNo(referenceNumber);
        orderRepository.save(order);

        try {
            String displayName = user.getDisplayName();
            String serviceType = buildServiceTypeString(order);
            String weight = getTotalWeightAsString(order);
            int quantity = getTotalItemCount(order);

            qrCodePrintingService.printReceipt(
                    referenceNumber,
                    displayName,
                    order.getProduct().getName(),
                    serviceType,
                    weight,
                    quantity,
                    order.getCreatedAt().toString()
            );
        } catch (PrinterException | IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new Exception(e);
        }

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


    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponseDto> searchOrders(Long userId,
                                               OrderStatus status,
                                               LocalDateTime startDate,
                                               LocalDateTime endDate,
                                               BigDecimal minAmount,
                                               BigDecimal maxAmount,
                                               String keyword,
                                               Pageable pageable) {

        Specification<Order> spec = Specification.where(null);

        if (userId != null) {
            spec = spec.and(com.laundry.specification.OrderSpecification.hasUserId(userId));
        }
        if (status != null) {
            spec = spec.and(OrderSpecification.hasStatus(status));
        }
        if (startDate != null && endDate != null) {
            spec = spec.and(OrderSpecification.createdBetween(startDate, endDate));
        }
        if (minAmount != null && maxAmount != null) {
            spec = spec.and(OrderSpecification.totalAmountBetween(minAmount, maxAmount));
        }
        if (keyword != null && !keyword.isBlank()) {
            spec = spec.and(OrderSpecification.referenceNoContains(keyword));
        }
        Page<Order> orders = orderRepository.findAll(spec, pageable);
        return orders.map(OrderMapper::toResponseDto);
    }

    @Override
    public void printOrder(Long orderId, Long currentUserId, String currentUserRole) throws Exception {
        RoleGuard.requireAdminRole(currentUserRole, "You do not have permission to print this order");

        Order order = getExistingOrder(orderId);

        String displayName = order.getUser().getDisplayName();
        String productType = order.getProduct().getName();
        String servicesString = buildServiceTypeString(order);
        String weight = getTotalWeightAsString(order);
        int quantity = getTotalItemCount(order);
        String orderDate = order.getCreatedAt().toString();

        try {
            qrCodePrintingService.printReceipt(
                    order.getReferenceNo(),
                    displayName,
                    productType,
                    servicesString,
                    weight,
                    quantity,
                    orderDate
            );
        } catch (Exception e) {
            throw new Exception("Error printing order: " + e.getMessage(), e);
        }
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

    private String buildServiceTypeString(Order order) {
        return order.getOrderItems().stream()
                .map(item -> item.getService().getName())
                .distinct()
                .collect(Collectors.joining(", "));
    }

    private String getTotalWeightAsString(Order order) {
        BigDecimal total = order.getOrderItems().stream()
                .map(OrderItem::getWeight)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return total.toPlainString();
    }

    private int getTotalItemCount(Order order) {
        return order.getOrderItems().stream()
                .mapToInt(OrderItem::getQuantity)
                .sum();
    }

}
