package com.laundry.service;

import com.laundry.dto.OrderRequestDto;
import com.laundry.dto.OrderResponseDto;
import com.laundry.entity.Order;
import com.laundry.entity.OrderStatusHistory;
import com.laundry.entity.User;
import com.laundry.exception.AccessDeniedException;
import com.laundry.exception.BadRequestException;
import com.laundry.exception.NotFoundException;

import java.util.List;

public interface OrderService {

    /**
     * Creates a new {@link Order} entity based on the provided DTO.
     * <ul>
     *   <li>Requires the user to have an {@code ADMIN} role to proceed.</li>
     *   <li>Looks up the associated {@link User} by ID.</li>
     *   <li>Maps DTO fields to a new {@link Order} entity.</li>
     *   <li>Auto-calculates item prices where necessary.</li>
     *   <li>Saves the final entity and returns a corresponding DTO.</li>
     * </ul>
     *
     * @param requestDto the DTO containing the order creation data
     * @param currentUserId the ID of the currently logged-in user
     * @param currentUserRole the role of the currently logged-in user
     * @return an {@link OrderResponseDto} representing the newly created order
     * @throws NotFoundException if the associated user is not found
     * @throws AccessDeniedException if the current user does not have {@code ADMIN} privileges
     * @throws BadRequestException if any invalid data is provided
     */
    OrderResponseDto createOrder(OrderRequestDto requestDto,
                                 Long currentUserId,
                                 String currentUserRole)
            throws NotFoundException, AccessDeniedException, BadRequestException;

    /**
     * Fully updates an existing {@link Order} by overwriting all updatable
     * fields with values from the provided DTO.
     * <ul>
     *   <li>Requires the user to have an {@code ADMIN} role.</li>
     *   <li>Checks if the order exists by the given ID.</li>
     *   <li>Auto-calculates item prices, if missing.</li>
     *   <li>Recomputes the total order amount.</li>
     *   <li>Saves the updated entity and returns a response DTO.</li>
     * </ul>
     *
     * @param id the ID of the order to update
     * @param requestDto the DTO containing updated order data
     * @param currentUserId the ID of the currently logged-in user
     * @param currentUserRole the role of the currently logged-in user
     * @return an {@link OrderResponseDto} reflecting the updated order
     * @throws NotFoundException if the order with the specified ID does not exist
     * @throws AccessDeniedException if the current user does not have {@code ADMIN} privileges
     * @throws BadRequestException if any invalid update data is provided
     */
    OrderResponseDto updateOrder(Long id,
                                 OrderRequestDto requestDto,
                                 Long currentUserId,
                                 String currentUserRole)
            throws NotFoundException, AccessDeniedException, BadRequestException;

    /**
     * Partially updates an existing {@link Order} by modifying only the
     * fields present in the provided DTO.
     * <ul>
     *   <li>Requires the user to have an {@code ADMIN} role.</li>
     *   <li>Checks if the order exists by the given ID.</li>
     *   <li>Auto-calculates item prices, if missing.</li>
     *   <li>Recomputes the total order amount.</li>
     *   <li>Saves the updated entity and returns a response DTO.</li>
     * </ul>
     *
     * @param id the ID of the order to patch
     * @param requestDto the DTO containing partial order data
     * @param currentUserId the ID of the currently logged-in user
     * @param currentUserRole the role of the currently logged-in user
     * @return an {@link OrderResponseDto} reflecting the patched order
     * @throws NotFoundException if the order with the specified ID does not exist
     * @throws AccessDeniedException if the current user does not have {@code ADMIN} privileges
     */
    OrderResponseDto patchOrder(Long id,
                                OrderRequestDto requestDto,
                                Long currentUserId,
                                String currentUserRole)
            throws NotFoundException, AccessDeniedException, BadRequestException;

    /**
     * Retrieves a specific {@link Order} by its ID. This operation
     * requires an {@code ADMIN} role.
     * <ul>
     *   <li>Checks if the order with the given ID exists.</li>
     *   <li>Returns a mapped DTO if found.</li>
     * </ul>
     *
     * @param id the ID of the order to retrieve
     * @param currentUserId the ID of the currently logged-in user
     * @param currentUserRole the role of the currently logged-in user
     * @return an {@link OrderResponseDto} representing the found order
     * @throws NotFoundException if the order with the specified ID does not exist
     * @throws AccessDeniedException if the current user lacks {@code ADMIN} privileges
     */
    OrderResponseDto getOrderById(Long id,
                                  Long currentUserId,
                                  String currentUserRole)
            throws NotFoundException, AccessDeniedException;

    /**
     * Retrieves all orders in the system. This operation requires an
     * {@code ADMIN} role.
     * <ul>
     *   <li>Loads all orders from the repository.</li>
     *   <li>Maps each to a DTO and returns as a list.</li>
     * </ul>
     *
     * @param currentUserId the ID of the currently logged-in user
     * @param currentUserRole the role of the currently logged-in user
     * @return a list of {@link OrderResponseDto} objects representing all orders
     * @throws AccessDeniedException if the current user lacks {@code ADMIN} privileges
     */
    List<OrderResponseDto> getAllOrders(Long currentUserId,
                                        String currentUserRole)
            throws AccessDeniedException;

    /**
     * Deletes an order identified by the given {@code id}. This operation
     * requires an {@code ADMIN} role.
     * <ul>
     *   <li>Checks if the order with the specified ID exists.</li>
     *   <li>Deletes the order if found.</li>
     * </ul>
     *
     * @param id the ID of the order to delete
     * @param currentUserId the ID of the currently logged-in user
     * @param currentUserRole the role of the currently logged-in user
     * @throws NotFoundException if the order with the specified ID does not exist
     * @throws AccessDeniedException if the current user lacks {@code ADMIN} privileges
     */
    void deleteOrder(Long id,
                     Long currentUserId,
                     String currentUserRole)
            throws NotFoundException, AccessDeniedException;

    /**
     * Advances the status of an existing order through its typical lifecycle.
     * Only administrators can change order status. The sequence of statuses is:
     * <ul>
     *   <li>PENDING &rarr; IN_PROGRESS</li>
     *   <li>IN_PROGRESS &rarr; COMPLETED</li>
     *   <li>COMPLETED &rarr; DELIVERED</li>
     *   <li>DELIVERED &rarr; (no further status change allowed)</li>
     * </ul>
     * <p>
     * Records each status transition in the {@link OrderStatusHistory} table.
     *
     * @param orderId the ID of the order to advance
     * @param currentUserId the ID of the currently logged-in user
     * @param currentUserRole the role of the currently logged-in user
     * @return an updated {@link OrderResponseDto} reflecting the new status
     * @throws NotFoundException if the specified order does not exist
     * @throws AccessDeniedException if the current user lacks {@code ADMIN} privileges
     * @throws BadRequestException if no further status transitions are possible
     */
    OrderResponseDto advanceOrderStatus(Long orderId,
                                        Long currentUserId,
                                        String currentUserRole)
            throws NotFoundException, AccessDeniedException, BadRequestException;
}
