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

    private Service getExistingService(Long id) throws NotFoundException {
        return serviceRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Service not found with id: " + id));
    }

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

    public void updateEntity(Service existing, ServiceRequestDto dto) {
        if (dto.getName() != null) {
            existing.setName(Format.capitalizeString(dto.getName().trim()));
        }
        if (dto.getDescription() != null) {
            existing.setDescription(dto.getDescription().trim());
        }
    }
}
