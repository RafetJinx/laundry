package com.laundry.service;

import com.laundry.dto.ServiceRequestDto;
import com.laundry.dto.ServiceResponseDto;
import com.laundry.exception.AccessDeniedException;
import com.laundry.exception.BadRequestException;
import com.laundry.exception.NotFoundException;
import com.laundry.helper.RoleGuard;

import java.util.List;

public interface ServiceService {

    /**
     * Creates a new Service entity based on the provided {@code requestDto}.
     * Only administrators are allowed to create new services.
     * <ul>
     *   <li>Checks for admin role permissions using {@link RoleGuard#requireAdminRole(String, String)}</li>
     *   <li>Validates that the service name is not already in use</li>
     *   <li>Performs any additional formatting or entity updates</li>
     *   <li>Saves the service to the repository</li>
     * </ul>
     *
     * @param requestDto     the DTO containing the new service details
     * @param currentUserId  the ID of the currently logged-in user (not used directly here but part of the method signature)
     * @param currentUserRole the role of the currently logged-in user
     * @return a {@link ServiceResponseDto} representing the newly created Service
     * @throws AccessDeniedException if the current user is not an admin
     * @throws BadRequestException if a service with the same name already exists
     */
    ServiceResponseDto createService(ServiceRequestDto requestDto,
                                     Long currentUserId,
                                     String currentUserRole)
            throws AccessDeniedException, BadRequestException;

    /**
     * Fully updates an existing Service entity identified by {@code id}.
     * Only administrators are allowed to update existing services.
     * <ul>
     *   <li>Checks for admin role permissions using {@link RoleGuard#requireAdminRole(String, String)}</li>
     *   <li>Retrieves the existing service entity and validates it</li>
     *   <li>Validates name changes (checks for conflicts)</li>
     *   <li>Applies the updates from {@code requestDto} and saves the entity</li>
     * </ul>
     *
     * @param id              the ID of the service to update
     * @param requestDto      the DTO containing updated service data
     * @param currentUserId   the ID of the currently logged-in user (not used directly here but part of the method signature)
     * @param currentUserRole the role of the currently logged-in user
     * @return a {@link ServiceResponseDto} representing the updated Service
     * @throws NotFoundException if the service with the specified {@code id} is not found
     * @throws AccessDeniedException if the current user is not an admin
     * @throws BadRequestException if a service with the updated name already exists
     */
    ServiceResponseDto updateService(Long id,
                                     ServiceRequestDto requestDto,
                                     Long currentUserId,
                                     String currentUserRole)
            throws NotFoundException, AccessDeniedException, BadRequestException;

    /**
     * Partially updates an existing Service entity identified by {@code id}.
     * Only administrators are allowed to perform this operation.
     * <ul>
     *   <li>Checks for admin role permissions using {@link RoleGuard#requireAdminRole(String, String)}</li>
     *   <li>Retrieves the existing service entity and validates it</li>
     *   <li>Validates name changes (checks for conflicts)</li>
     *   <li>Applies only the non-null fields from {@code requestDto}</li>
     * </ul>
     *
     * @param id              the ID of the service to patch
     * @param requestDto      the DTO containing partial updated data
     * @param currentUserId   the ID of the currently logged-in user (not used directly here but part of the method signature)
     * @param currentUserRole the role of the currently logged-in user
     * @return a {@link ServiceResponseDto} representing the patched Service
     * @throws NotFoundException if the service with the specified {@code id} is not found
     * @throws AccessDeniedException if the current user is not an admin
     * @throws BadRequestException if a service with the updated name already exists
     */
    ServiceResponseDto patchService(Long id,
                                    ServiceRequestDto requestDto,
                                    Long currentUserId,
                                    String currentUserRole)
            throws NotFoundException, AccessDeniedException, BadRequestException;

    /**
     * Retrieves a specific service by its ID. This method can be called by
     * any user (no role check here) and does not modify the database state.
     *
     * @param id the ID of the service to retrieve
     * @return a {@link ServiceResponseDto} containing the service details
     * @throws NotFoundException if the service with the specified {@code id} is not found
     */
    ServiceResponseDto getServiceById(Long id)
            throws NotFoundException;

    /**
     * Retrieves all services available in the system. This method can be called by
     * any user (no role check here) and does not modify the database state.
     *
     * @return a list of {@link ServiceResponseDto} objects representing all services
     */
    List<ServiceResponseDto> getAllServices();

    /**
     * Deletes the service identified by the given {@code id}. Only administrators are
     * allowed to delete existing services.
     * <ul>
     *   <li>Checks for admin role permissions using {@link RoleGuard#requireAdminRole(String, String)}</li>
     *   <li>Retrieves the existing service entity and validates it</li>
     *   <li>Removes the service from the repository</li>
     * </ul>
     *
     * @param id              the ID of the service to delete
     * @param currentUserId   the ID of the currently logged-in user (not used directly here but part of the method signature)
     * @param currentUserRole the role of the currently logged-in user
     * @throws NotFoundException if the service with the specified {@code id} is not found
     * @throws AccessDeniedException if the current user is not an admin
     */
    void deleteService(Long id,
                       Long currentUserId,
                       String currentUserRole)
            throws NotFoundException, AccessDeniedException;
}
