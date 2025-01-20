package com.laundry.service;

import com.laundry.dto.OrderSHistoryResponseDto;
import com.laundry.entity.OrderStatus;
import com.laundry.exception.NotFoundException;
import com.laundry.mapper.OrderSHistoryMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface OrderSHistoryService {

    /**
     * Retrieves a single order status history record by its unique identifier.
     * <ul>
     *   <li>Queries the database using the provided {@code id}.</li>
     *   <li>Maps the entity to a DTO via {@link OrderSHistoryMapper}.</li>
     *   <li>Throws {@link NotFoundException} if no record is found.</li>
     * </ul>
     *
     * @param id the unique identifier of the order status history record
     * @return an {@link OrderSHistoryResponseDto} representing the found record
     * @throws NotFoundException if the order status history record with the given {@code id} does not exist
     */
    OrderSHistoryResponseDto findById(Long id);

    /**
     * Retrieves a page of order status history records filtered by a specific order ID.
     * This method is restricted to users with the {@code ADMIN} role.
     * <ul>
     *   <li>Queries the repository by {@code orderId} with pagination.</li>
     *   <li>Maps each entity to a DTO via {@link OrderSHistoryMapper}.</li>
     * </ul>
     *
     * @param orderId  the ID of the order whose status history should be retrieved
     * @param pageable the pagination information
     * @return a {@link Page} of {@link OrderSHistoryResponseDto} objects
     */
    Page<OrderSHistoryResponseDto> findByOrderId(Long orderId, Pageable pageable);

    /**
     * Retrieves a page of order status history records filtered by their old (previous) status.
     * This method is restricted to users with the {@code ADMIN} role.
     * <ul>
     *   <li>Queries the repository by {@code oldStatus} with pagination.</li>
     *   <li>Maps each entity to a DTO via {@link OrderSHistoryMapper}.</li>
     * </ul>
     *
     * @param oldStatus the previous status of the orders to filter on
     * @param pageable  the pagination information
     * @return a {@link Page} of {@link OrderSHistoryResponseDto} objects
     */
    Page<OrderSHistoryResponseDto> findByOldStatus(OrderStatus oldStatus, Pageable pageable);

    /**
     * Retrieves a page of order status history records filtered by their new (updated) status.
     * This method is restricted to users with the {@code ADMIN} role.
     * <ul>
     *   <li>Queries the repository by {@code newStatus} with pagination.</li>
     *   <li>Maps each entity to a DTO via {@link OrderSHistoryMapper}.</li>
     * </ul>
     *
     * @param newStatus the new status of the orders to filter on
     * @param pageable  the pagination information
     * @return a {@link Page} of {@link OrderSHistoryResponseDto} objects
     */
    Page<OrderSHistoryResponseDto> findByNewStatus(OrderStatus newStatus, Pageable pageable);

    /**
     * Retrieves a page of order status history records that were changed
     * within a specific date/time range. This method is restricted to users
     * with the {@code ADMIN} role.
     * <ul>
     *   <li>Filters records by {@code changedAtAfter} and {@code changedAtBefore} boundaries.</li>
     *   <li>Maps each entity to a DTO via {@link OrderSHistoryMapper}.</li>
     * </ul>
     *
     * @param changedAtAfter the lower bound of the change time range (inclusive)
     * @param changedAtBefore the upper bound of the change time range (inclusive)
     * @param pageable        the pagination information
     * @return a {@link Page} of {@link OrderSHistoryResponseDto} objects
     */
    Page<OrderSHistoryResponseDto> findByChangedAtBetween(LocalDateTime changedAtAfter, LocalDateTime changedAtBefore, Pageable pageable);

    /**
     * Retrieves a page of order status history records made by a specific user
     * (identified by {@code changedBy}). This method is restricted to users with
     * the {@code ADMIN} role.
     * <ul>
     *   <li>Queries the repository by {@code changedBy} (user ID) with pagination.</li>
     *   <li>Maps each entity to a DTO via {@link OrderSHistoryMapper}.</li>
     * </ul>
     *
     * @param changedBy the ID of the user who made the changes
     * @param pageable  the pagination information
     * @return a {@link Page} of {@link OrderSHistoryResponseDto} objects
     */
    Page<OrderSHistoryResponseDto> findByChangedBy(Long changedBy, Pageable pageable);
}
