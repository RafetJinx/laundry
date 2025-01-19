package com.laundry.service;

import com.laundry.dto.OrderItemRequestDto;
import com.laundry.dto.OrderRequestDto;
import com.laundry.dto.OrderResponseDto;
import com.laundry.entity.Order;
import com.laundry.entity.Service;
import com.laundry.entity.User;
import com.laundry.exception.AccessDeniedException;
import com.laundry.exception.BadRequestException;
import com.laundry.exception.NotFoundException;
import com.laundry.helper.RoleGuard;
import com.laundry.mapper.OrderMapper;
import com.laundry.repository.OrderRepository;
import com.laundry.repository.ServiceRepository;
import com.laundry.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

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

    @Override
    public OrderResponseDto createOrder(OrderRequestDto requestDto,
                                        Long currentUserId,
                                        String currentUserRole)
            throws NotFoundException, AccessDeniedException, BadRequestException {

        User user = userRepository.findById(requestDto.getUserId())
                .orElseThrow(() -> new NotFoundException("User not found: " + requestDto.getUserId()));

        // If admin, can create an order for any user; if not admin, user must match
        if (!"ROLE_ADMIN".equals(currentUserRole) && !user.getId().equals(currentUserId)) {
            throw new AccessDeniedException("You do not have permission to create an order for this user");
        }

        List<Long> serviceIds = extractServiceIds(requestDto);
        List<Service> foundServices = serviceRepository.findAllById(serviceIds);

        Order order = OrderMapper.toEntity(requestDto, user, foundServices);
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
        List<Service> foundServices = serviceRepository.findAllById(serviceIds);

        OrderMapper.updateEntity(existing, requestDto, foundServices);
        orderRepository.save(existing);
        return OrderMapper.toResponseDto(existing);
    }

    @Override
    public OrderResponseDto patchOrder(Long id,
                                       OrderRequestDto requestDto,
                                       Long currentUserId,
                                       String currentUserRole)
            throws NotFoundException, AccessDeniedException, BadRequestException {

        Order existing = getExistingOrder(id);
        RoleGuard.requireAdminRole(currentUserRole, "You do not have permission to patch this order");

        List<Long> serviceIds = extractServiceIds(requestDto);
        List<Service> foundServices = serviceRepository.findAllById(serviceIds);

        OrderMapper.patchEntity(existing, requestDto, foundServices);
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
        // If not admin, must be the owner
        if (!"ROLE_ADMIN".equals(currentUserRole) && !order.getUser().getId().equals(currentUserId)) {
            throw new AccessDeniedException("You do not have permission to view this order");
        }
        return OrderMapper.toResponseDto(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDto> getAllOrders(Long currentUserId, String currentUserRole)
            throws AccessDeniedException {

        RoleGuard.requireAdminRole(currentUserRole, "Only admin can view all orders");
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

    private Order getExistingOrder(Long id) throws NotFoundException {
        return orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Order not found with id: " + id));
    }

    /**
     * If the order has items, extract distinct serviceIds to fetch them all at once.
     */
    private List<Long> extractServiceIds(OrderRequestDto requestDto) {
        if (requestDto.getOrderItems() == null) {
            return Collections.emptyList();
        }
        return requestDto.getOrderItems().stream()
                .map(OrderItemRequestDto::getServiceId)
                .distinct()
                .collect(Collectors.toList());
    }
}
