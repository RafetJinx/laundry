package com.laundry.service.impl;

import com.laundry.dto.OrderItemRequestDto;
import com.laundry.dto.OrderItemResponseDto;
import com.laundry.entity.*;
import com.laundry.exception.AccessDeniedException;
import com.laundry.exception.BadRequestException;
import com.laundry.exception.NotFoundException;
import com.laundry.helper.RoleGuard;
import com.laundry.mapper.OrderItemMapper;
import com.laundry.repository.*;
import com.laundry.service.OrderItemService;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
@Transactional
public class OrderItemServiceImpl implements OrderItemService {

    private final OrderItemRepository orderItemRepository;

    private final OrderRepository orderRepository;

    private final ServiceRepository serviceRepository;

    private final ServicePriceRepository servicePriceRepository;

    public OrderItemServiceImpl(OrderItemRepository orderItemRepository,
                                OrderRepository orderRepository,
                                ServiceRepository serviceRepository,
                                ServicePriceRepository servicePriceRepository) {
        this.orderItemRepository = orderItemRepository;
        this.orderRepository = orderRepository;
        this.serviceRepository = serviceRepository;
        this.servicePriceRepository = servicePriceRepository;
    }

    /**
     * Create a new order item under the specified order.
     * If requestDto.priceAmount is null, we compute it by:
     *    (weight in KG) * (ServicePrice for order's currency code).
     */
    @Override
    public OrderItemResponseDto createOrderItem(Long orderId,
                                                OrderItemRequestDto requestDto,
                                                Long currentUserId,
                                                String currentUserRole)
            throws NotFoundException, AccessDeniedException, BadRequestException {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found: " + orderId));

        RoleGuard.requireAdminRole("You do not have permission to add order item", currentUserRole);

        Service service = serviceRepository.findById(requestDto.getServiceId())
                .orElseThrow(() -> new NotFoundException("Service not found: " + requestDto.getServiceId()));

        // Build the item, but we might override the priceAmount if it's null
        OrderItem item = OrderItemMapper.toEntity(requestDto, order, service);

        // If priceAmount is null, compute from ServicePrice × weight
        if (requestDto.getPriceAmount() == null) {
            BigDecimal computedPrice = computePriceFromServicePrice(order, service, requestDto.getWeight());
            item.setPriceAmount(computedPrice);
        }

        orderItemRepository.save(item);
        return OrderItemMapper.toResponseDto(item);
    }

    @Override
    public OrderItemResponseDto updateOrderItem(Long itemId,
                                                OrderItemRequestDto requestDto,
                                                Long currentUserId,
                                                String currentUserRole)
            throws NotFoundException, AccessDeniedException, BadRequestException {

        OrderItem existing = orderItemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("OrderItem not found: " + itemId));

        RoleGuard.requireAdminRole("You do not have permission to update order item", currentUserRole);

        // Possibly user wants to change service, quantity, or price
        if (requestDto.getServiceId() != null &&
                !requestDto.getServiceId().equals(existing.getService().getId())) {
            // We check if new service is valid
            Service newService = serviceRepository.findById(requestDto.getServiceId())
                    .orElseThrow(() -> new NotFoundException("Service not found: " + requestDto.getServiceId()));
            existing.setService(newService);
        }

        if (requestDto.getQuantity() != null) {
            existing.setQuantity(requestDto.getQuantity());
        }

        if (requestDto.getWeight() != null) {
            existing.setWeight(requestDto.getWeight());
        }

        // Price
        if (requestDto.getPriceAmount() != null) {
            // user provided a price
            existing.setPriceAmount(requestDto.getPriceAmount());
        } else {
            // compute again from weight
            BigDecimal computed = computePriceFromServicePrice(
                    existing.getOrder(),
                    existing.getService(),
                    existing.getWeight()
            );
            existing.setPriceAmount(computed);
        }

        orderItemRepository.save(existing);
        return OrderItemMapper.toResponseDto(existing);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderItemResponseDto getOrderItem(Long itemId,
                                             Long currentUserId,
                                             String currentUserRole)
            throws NotFoundException, AccessDeniedException {

        OrderItem item = orderItemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("OrderItem not found: " + itemId));

        Order parent = item.getOrder();
        if (!"ROLE_ADMIN".equals(currentUserRole) &&
                !parent.getUser().getId().equals(currentUserId)) {
            throw new AccessDeniedException("You do not have permission to view this item");
        }

        return OrderItemMapper.toResponseDto(item);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderItemResponseDto> getItemsByOrder(Long orderId,
                                                      Long currentUserId,
                                                      String currentUserRole)
            throws NotFoundException, AccessDeniedException {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found: " + orderId));

        if (!"ROLE_ADMIN".equals(currentUserRole) &&
                !order.getUser().getId().equals(currentUserId)) {
            throw new AccessDeniedException("You do not have permission to view items of this order");
        }

        return order.getOrderItems().stream()
                .map(OrderItemMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteOrderItem(Long itemId,
                                Long currentUserId,
                                String currentUserRole)
            throws NotFoundException, AccessDeniedException {

        OrderItem existing = orderItemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("OrderItem not found: " + itemId));

        RoleGuard.requireAdminRole("You do not have permission to delete this order item", currentUserRole);

        orderItemRepository.delete(existing);
    }

    /**
     * Computes the price of an {@link OrderItem} based on the item's quantity
     * (in grams) and the {@link ServicePrice} for the corresponding service
     * and the order's currency code.
     * <ul>
     *   <li>Looks up the {@link ServicePrice} by {@code serviceId} and {@code currencyCode}.</li>
     *   <li>Converts the quantity from grams to kilograms.</li>
     *   <li>Multiplies the per-kg service price by the item's weight (in kg).</li>
     *   <li>Rounds the result to 2 decimal places using {@link RoundingMode#HALF_UP}.</li>
     * </ul>
     *
     * @param order         the parent {@link Order} that determines the currency code
     * @param service       the {@link Service} whose price is used
     * @param weight the item weight in grams
     * @return a {@link BigDecimal} representing the computed total price
     * @throws NotFoundException if the corresponding service price is not found
     */
    private BigDecimal computePriceFromServicePrice(Order order,
                                                    Service service,
                                                    BigDecimal weight)
            throws NotFoundException {

        // 1) find ServicePrice for (serviceId, order.currencyCode)
        ServicePrice sp = servicePriceRepository.findByServiceIdAndCurrencyCode(
                service.getId(),
                order.getCurrencyCode()
        ).orElseThrow(() -> new NotFoundException(
                "No ServicePrice found for service ID=" + service.getId()
                        + " and currency=" + order.getCurrencyCode()
        ));

        // 2) Convert grams to KG
        BigDecimal kilos = weight.divide(BigDecimal.valueOf(1000), 3, RoundingMode.HALF_UP);

        // 3) multiply servicePrice × kilos
        return sp.getPrice()
                .multiply(kilos)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
