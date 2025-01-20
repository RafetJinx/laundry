package com.laundry.service;

import com.laundry.dto.ServiceRequestDto;
import com.laundry.dto.ServiceResponseDto;
import com.laundry.entity.Service;
import com.laundry.exception.AccessDeniedException;
import com.laundry.exception.BadRequestException;
import com.laundry.exception.NotFoundException;
import com.laundry.helper.RoleGuard;
import com.laundry.mapper.ServiceMapper;
import com.laundry.repository.ServiceRepository;
import com.laundry.util.Format;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
@Transactional
public class ServiceServiceImpl implements ServiceService {

    @Autowired
    private ServiceRepository serviceRepository;

    @Override
    public ServiceResponseDto createService(ServiceRequestDto requestDto,
                                            Long currentUserId,
                                            String currentUserRole)
            throws AccessDeniedException, BadRequestException {

        RoleGuard.requireAdminRole(currentUserRole, "Only admin can create services");

        if (serviceRepository.existsByName(requestDto.getName())) {
            throw new BadRequestException("Service with the same name already exists: " + requestDto.getName());
        }

        Service service = ServiceMapper.toEntity(requestDto);
        updateEntity(service, requestDto);
        serviceRepository.save(service);
        return ServiceMapper.toResponseDto(service);
    }

    @Override
    public ServiceResponseDto updateService(Long id,
                                            ServiceRequestDto requestDto,
                                            Long currentUserId,
                                            String currentUserRole)
            throws NotFoundException, AccessDeniedException, BadRequestException {

        RoleGuard.requireAdminRole(currentUserRole, "Only admin can update services");

        Service existing = getExistingService(id);
        validateNameChange(existing, requestDto);

        updateEntity(existing, requestDto);
        serviceRepository.save(existing);

        return ServiceMapper.toResponseDto(existing);
    }

    @Override
    public ServiceResponseDto patchService(Long id,
                                           ServiceRequestDto requestDto,
                                           Long currentUserId,
                                           String currentUserRole)
            throws NotFoundException, AccessDeniedException, BadRequestException {

        RoleGuard.requireAdminRole(currentUserRole, "Only admin can update services");

        Service existing = getExistingService(id);
        validateNameChange(existing, requestDto);

        updateEntity(existing, requestDto);
        serviceRepository.save(existing);

        return ServiceMapper.toResponseDto(existing);
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceResponseDto getServiceById(Long id) throws NotFoundException {
        Service service = getExistingService(id);
        return ServiceMapper.toResponseDto(service);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceResponseDto> getAllServices() {
        return serviceRepository.findAll().stream()
                .map(ServiceMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteService(Long id,
                              Long currentUserId,
                              String currentUserRole)
            throws NotFoundException, AccessDeniedException {

        RoleGuard.requireAdminRole(currentUserRole, "Only admin can delete services");

        Service service = getExistingService(id);
        serviceRepository.delete(service);
    }

    /**
     * Retrieves an existing service by its ID. If the service does not exist,
     * throws a {@link NotFoundException}.
     *
     * @param id the ID of the service
     * @return the {@link Service} entity
     * @throws NotFoundException if the service with the specified {@code id} is not found
     */
    private Service getExistingService(Long id) throws NotFoundException {
        return serviceRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Service not found with id: " + id));
    }

    /**
     * Validates if the name of the service being updated/created conflicts
     * with any existing service in the system. If {@code requestCapitalizedName}
     * conflicts with a different service, a {@link BadRequestException} is thrown.
     *
     * @param existing   the existing {@link Service} being updated
     * @param requestDto the data transfer object containing the new service name
     * @throws BadRequestException if the updated service name conflicts with another existing service
     */
    private void validateNameChange(Service existing, ServiceRequestDto requestDto)
            throws BadRequestException {
        String requestCapitalizedName = Format.capitalizeString(requestDto.getName());
        if (requestCapitalizedName != null
                && !requestCapitalizedName.equals(existing.getName())
                && serviceRepository.existsByName(requestCapitalizedName)) {
            throw new BadRequestException(
                    "Service with the same name already exists: " + requestDto.getName()
            );
        }
    }

    /**
     * Updates the {@link Service} entity fields with values from the
     * {@link ServiceRequestDto}. Fields that are {@code null} in the DTO
     * will be ignored, leaving the existing entity fields unchanged.
     *
     * @param existing the existing {@link Service} entity to update
     * @param dto      the {@link ServiceRequestDto} containing updated fields
     */
    public void updateEntity(Service existing, ServiceRequestDto dto) {
        if (dto.getName() != null) {
            existing.setName(Format.capitalizeString(dto.getName().trim()));
        }
        if (dto.getDescription() != null) {
            existing.setDescription(dto.getDescription().trim());
        }
    }
}
