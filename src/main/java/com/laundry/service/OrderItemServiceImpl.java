package com.laundry.service;

import com.laundry.dto.OrderItemRequestDto;
import com.laundry.dto.OrderItemResponseDto;
import com.laundry.entity.*;
import com.laundry.exception.AccessDeniedException;
import com.laundry.exception.BadRequestException;
import com.laundry.exception.NotFoundException;
import com.laundry.helper.RoleGuard;
import com.laundry.mapper.OrderItemMapper;
import com.laundry.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import javax.management.relation.Role;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
@Transactional
public class OrderItemServiceImpl implements OrderItemService {

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private ServicePriceRepository servicePriceRepository;

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
            BigDecimal computedPrice = computePriceFromServicePrice(order, service, requestDto.getQuantity());
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

        Order parentOrder = existing.getOrder();

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

        // If user gave a priceAmount, we treat it as final
        // If null, we compute from ServicePrice
        if (requestDto.getPriceAmount() != null) {
            existing.setPriceAmount(requestDto.getPriceAmount());
        } else {
            BigDecimal computed = computePriceFromServicePrice(
                    parentOrder,
                    existing.getService(),
                    existing.getQuantity()
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

        Order parent = existing.getOrder();
        RoleGuard.requireAdminRole("You do not have permission to delete this order item", currentUserRole);

        orderItemRepository.delete(existing);
    }

    /**
     * If price is not given manually, compute from:
     *  (ServicePrice for order's currency) × (quantity in KG)
     *  quantity is stored in grams. E.g. 1023 => 1.023 kg
     */
    private BigDecimal computePriceFromServicePrice(Order order,
                                                    Service service,
                                                    Integer quantityGrams)
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
        BigDecimal kilos = BigDecimal.valueOf(quantityGrams)
                .divide(BigDecimal.valueOf(1000), 3, RoundingMode.HALF_UP);

        // 3) multiply servicePrice × kilos
        return sp.getPrice().multiply(kilos).setScale(2, RoundingMode.HALF_UP);
    }
}
