package com.laundry.service;

import com.laundry.dto.OrderItemRequestDto;
import com.laundry.dto.OrderItemResponseDto;
import com.laundry.entity.Order;
import com.laundry.entity.OrderItem;
import com.laundry.entity.ServicePrice;
import com.laundry.exception.AccessDeniedException;
import com.laundry.exception.BadRequestException;
import com.laundry.exception.NotFoundException;

import java.util.List;

public interface OrderItemService {

    /**
     * Creates a new {@link OrderItem} for a given order. Requires ADMIN
     * permissions to proceed. If the provided {@code priceAmount} is {@code null},
     * this method automatically computes it based on the {@link ServicePrice}
     * for the associated service and the specified quantity in grams.
     *
     * @param orderId        the ID of the {@link Order} to which this item will belong
     * @param requestDto     the {@link OrderItemRequestDto} containing item details
     * @param currentUserId  the ID of the currently logged-in user
     * @param currentUserRole the role of the currently logged-in user (must be ADMIN)
     * @return an {@link OrderItemResponseDto} representing the newly created item
     * @throws NotFoundException if the order or service is not found
     * @throws AccessDeniedException if the current user does not have ADMIN role
     * @throws BadRequestException if any invalid data is provided
     */
    OrderItemResponseDto createOrderItem(Long orderId,
                                         OrderItemRequestDto requestDto,
                                         Long currentUserId,
                                         String currentUserRole)
            throws NotFoundException, AccessDeniedException, BadRequestException;

    /**
     * Updates an existing {@link OrderItem}, allowing changes to service,
     * quantity, or price. Requires ADMIN permissions.
     * <p>
     * If a new {@code serviceId} is provided, the item will switch to that service.
     * If the {@code priceAmount} is omitted, it is recalculated from the
     * corresponding {@link ServicePrice} based on the new/unchanged quantity.
     *
     * @param itemId          the ID of the {@link OrderItem} to update
     * @param requestDto      the partial or full data for updating the item
     * @param currentUserId   the ID of the currently logged-in user
     * @param currentUserRole the role of the currently logged-in user (must be ADMIN)
     * @return an {@link OrderItemResponseDto} reflecting the updated item
     * @throws NotFoundException if the order item or the new service is not found
     * @throws AccessDeniedException if the current user does not have ADMIN role
     * @throws BadRequestException if any invalid data is provided
     */
    OrderItemResponseDto updateOrderItem(Long itemId,
                                         OrderItemRequestDto requestDto,
                                         Long currentUserId,
                                         String currentUserRole)
            throws NotFoundException, AccessDeniedException, BadRequestException;

    /**
     * Retrieves a specific {@link OrderItem} by its ID. If the current user is not
     * an ADMIN, they must be the owner of the parent {@link Order} to access this item.
     *
     * @param itemId          the ID of the {@link OrderItem} to retrieve
     * @param currentUserId   the ID of the currently logged-in user
     * @param currentUserRole the role of the currently logged-in user
     * @return an {@link OrderItemResponseDto} representing the found item
     * @throws NotFoundException if the item does not exist
     * @throws AccessDeniedException if the current user lacks permission
     */
    OrderItemResponseDto getOrderItem(Long itemId,
                                      Long currentUserId,
                                      String currentUserRole)
            throws NotFoundException, AccessDeniedException;

    /**
     * Retrieves all {@link OrderItem}s associated with a particular {@link Order}.
     * If the current user is not an ADMIN, they must be the owner of the order
     * to view its items.
     *
     * @param orderId         the ID of the parent order
     * @param currentUserId   the ID of the currently logged-in user
     * @param currentUserRole the role of the currently logged-in user
     * @return a list of {@link OrderItemResponseDto} objects associated with the given order
     * @throws NotFoundException if the order with the given {@code orderId} does not exist
     * @throws AccessDeniedException if the current user lacks permission
     */
    List<OrderItemResponseDto> getItemsByOrder(Long orderId,
                                               Long currentUserId,
                                               String currentUserRole)
            throws NotFoundException, AccessDeniedException;

    /**
     * Deletes a specific {@link OrderItem} by its ID. This action requires
     * ADMIN privileges.
     *
     * @param itemId          the ID of the item to delete
     * @param currentUserId   the ID of the currently logged-in user
     * @param currentUserRole the role of the currently logged-in user (must be ADMIN)
     * @throws NotFoundException if the item does not exist
     * @throws AccessDeniedException if the current user does not have ADMIN permissions
     */
    void deleteOrderItem(Long itemId,
                         Long currentUserId,
                         String currentUserRole)
            throws NotFoundException, AccessDeniedException;
}
