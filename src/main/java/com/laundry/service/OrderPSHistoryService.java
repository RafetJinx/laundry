package com.laundry.service;

import com.laundry.dto.OrderPSHistoryResponseDto;
import com.laundry.entity.PaymentStatus;
import com.laundry.exception.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface OrderPSHistoryService {

    /**
     * Retrieves a single order payment status history record by its unique identifier.
     * This method is restricted to users with the {@code ADMIN} role.
     *
     * @param id the unique identifier of the order payment status history record
     * @return an {@link OrderPSHistoryResponseDto} representing the found record
     * @throws NotFoundException if no record is found with the specified {@code id}
     */
    OrderPSHistoryResponseDto findById(Long id);

    /**
     * Retrieves a paginated list of payment status history records for a specific order,
     * identified by {@code orderId}. This method is restricted to users with the {@code ADMIN} role.
     *
     * @param orderId  the ID of the order whose payment status history should be retrieved
     * @param pageable the pagination information (page number, size, sorting, etc.)
     * @return a paginated list of {@link OrderPSHistoryResponseDto} objects
     */
    Page<OrderPSHistoryResponseDto> findByOrderId(Long orderId, Pageable pageable);

    /**
     * Retrieves a paginated list of payment status history records filtered by their old (previous) status.
     * This method is restricted to users with the {@code ADMIN} role.
     *
     * @param oldPaymentStatus the old payment status to filter on (e.g., PENDING, PAID)
     * @param pageable         the pagination information (page number, size, sorting, etc.)
     * @return a paginated list of {@link OrderPSHistoryResponseDto} objects
     */
    Page<OrderPSHistoryResponseDto> findByOldPaymentStatus(PaymentStatus oldPaymentStatus, Pageable pageable);

    /**
     * Retrieves a paginated list of payment status history records filtered by their new (updated) status.
     * This method is restricted to users with the {@code ADMIN} role.
     *
     * @param newPaymentStatus the new payment status to filter on (e.g., PAID, REFUNDED)
     * @param pageable         the pagination information (page number, size, sorting, etc.)
     * @return a paginated list of {@link OrderPSHistoryResponseDto} objects
     */
    Page<OrderPSHistoryResponseDto> findByNewPaymentStatus(PaymentStatus newPaymentStatus, Pageable pageable);

    /**
     * Retrieves a paginated list of payment status history records changed within a
     * specific date/time range. This method is restricted to users with the {@code ADMIN} role.
     *
     * @param changedAtAfter  the lower bound of the change time range (inclusive)
     * @param changedAtBefore the upper bound of the change time range (inclusive)
     * @param pageable        the pagination information (page number, size, sorting, etc.)
     * @return a paginated list of {@link OrderPSHistoryResponseDto} objects
     */
    Page<OrderPSHistoryResponseDto> findByChangedAtBetween(LocalDateTime changedAtAfter, LocalDateTime changedAtBefore, Pageable pageable);

    /**
     * Retrieves a paginated list of payment status history records filtered by the
     * user ID that performed the status change. This method is restricted to users
     * with the {@code ADMIN} role.
     *
     * @param changedBy the ID of the user who performed the status change
     * @param pageable  the pagination information (page number, size, sorting, etc.)
     * @return a paginated list of {@link OrderPSHistoryResponseDto} objects
     */
    Page<OrderPSHistoryResponseDto> findByChangedBy(Long changedBy, Pageable pageable);
}
