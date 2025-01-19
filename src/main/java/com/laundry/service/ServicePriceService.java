package com.laundry.service;

import com.laundry.dto.ServicePriceRequestDto;
import com.laundry.dto.ServicePriceResponseDto;
import com.laundry.entity.Service;
import com.laundry.exception.AccessDeniedException;
import com.laundry.exception.BadRequestException;
import com.laundry.exception.NotFoundException;

import java.util.List;

public interface ServicePriceService {
    /**
     * The set of all currencies for which automatic conversion is handled.
     * If a user provides a price in one of these currencies, the system calculates
     * equivalent prices for the others in this list.
     */
    static final List<String> AUTO_CURRENCIES = List.of("TRY", "USD", "EUR", "GBP");

    /**
     * Creates a new ServicePrice record for a specific service ID, provided by an ADMIN user.
     * Once the main ServicePrice is saved, other currency prices are automatically generated
     * or updated based on the conversion from the source currency to TRY, and from TRY
     * to the remaining currencies in {@link #AUTO_CURRENCIES}.
     *
     * @param serviceId       the ID of the {@link Service} for which we create a price
     * @param requestDto      the data transfer object containing currency code and price
     * @param currentUserId   the ID of the user requesting the operation
     * @param currentUserRole the role of the user (must be ADMIN)
     * @return a {@link ServicePriceResponseDto} representing the newly created price
     * @throws NotFoundException      if the specified Service does not exist
     * @throws AccessDeniedException  if the current user is not an ADMIN
     * @throws BadRequestException    if a ServicePrice with the same currency code already exists
     */
    ServicePriceResponseDto createServicePrice(Long serviceId,
                                               ServicePriceRequestDto requestDto,
                                               Long currentUserId,
                                               String currentUserRole)
            throws NotFoundException, AccessDeniedException, BadRequestException;

    /**
     * Updates an existing ServicePrice record. If the currency code changes, we ensure
     * no duplicate entry exists for that new currency. Afterwards, if the updated currency
     * is one of the auto-handled currencies, we also update the associated conversion-based
     * records in other currencies.
     *
     * @param id              the ID of the ServicePrice record to update
     * @param requestDto      the data transfer object containing new currency code and price
     * @param currentUserId   the ID of the user requesting the update
     * @param currentUserRole the role of the user (must be ADMIN)
     * @return a {@link ServicePriceResponseDto} representing the updated price
     * @throws NotFoundException      if no ServicePrice is found for the given ID
     * @throws AccessDeniedException  if the current user is not an ADMIN
     * @throws BadRequestException    if changing the currency code would cause a duplicate
     */
    ServicePriceResponseDto updateServicePrice(Long id,
                                               ServicePriceRequestDto requestDto,
                                               Long currentUserId,
                                               String currentUserRole)
            throws NotFoundException, AccessDeniedException, BadRequestException;

    /**
     * Retrieves a single ServicePrice by its ID. Only ADMINs may perform this operation.
     *
     * @param id              the ID of the ServicePrice
     * @param currentUserId   the ID of the user requesting the operation
     * @param currentUserRole the role of the user (must be ADMIN)
     * @return a {@link ServicePriceResponseDto} for the requested record
     * @throws NotFoundException     if no record is found with the given ID
     * @throws AccessDeniedException if the user is not an ADMIN
     */
    ServicePriceResponseDto getServicePrice(Long id,
                                            Long currentUserId,
                                            String currentUserRole)
            throws NotFoundException, AccessDeniedException;

    /**
     * Retrieves all ServicePrice records for a given Service ID. Only ADMINs may perform this operation.
     *
     * @param serviceId       the ID of the Service
     * @param currentUserId   the ID of the user requesting the operation
     * @param currentUserRole the role of the user (must be ADMIN)
     * @return a list of {@link ServicePriceResponseDto} associated with the given Service
     * @throws NotFoundException     if the Service ID does not exist
     * @throws AccessDeniedException if the user is not an ADMIN
     */
    List<ServicePriceResponseDto> getAllPricesByService(Long serviceId,
                                                        Long currentUserId,
                                                        String currentUserRole)
            throws NotFoundException, AccessDeniedException;

    /**
     * Retrieves a single ServicePrice by the given Service ID and currency code.
     * Only ADMINs may perform this operation.
     *
     * @param serviceId       the ID of the Service
     * @param currencyCode    the currency code to look up
     * @param currentUserId   the ID of the user requesting the operation
     * @param currentUserRole the role of the user (must be ADMIN)
     * @return the matching {@link ServicePriceResponseDto}
     * @throws NotFoundException     if no matching ServicePrice is found
     * @throws AccessDeniedException if the user is not an ADMIN
     */
    ServicePriceResponseDto getServicePriceByServiceAndCurrencyCode(Long serviceId,
                                                                    String currencyCode,
                                                                    Long currentUserId,
                                                                    String currentUserRole)
            throws NotFoundException, AccessDeniedException;

    /**
     * Deletes a ServicePrice record by its ID. Only ADMINs may perform this operation.
     *
     * @param id              the ServicePrice ID to delete
     * @param currentUserId   the ID of the user requesting deletion
     * @param currentUserRole the role of the user (must be ADMIN)
     * @throws NotFoundException     if the specified ID does not map to any ServicePrice
     * @throws AccessDeniedException if the user is not an ADMIN
     */
    void deleteServicePrice(Long id,
                            Long currentUserId,
                            String currentUserRole)
            throws NotFoundException, AccessDeniedException;
}
