package com.laundry.service;

import com.laundry.dto.ServiceRequestDto;
import com.laundry.dto.ServiceResponseDto;
import com.laundry.exception.AccessDeniedException;
import com.laundry.exception.BadRequestException;
import com.laundry.exception.NotFoundException;

import java.util.List;

public interface ServiceService {

    ServiceResponseDto createService(ServiceRequestDto requestDto,
                                     Long currentUserId,
                                     String currentUserRole)
            throws AccessDeniedException, BadRequestException;

    ServiceResponseDto updateService(Long id,
                                     ServiceRequestDto requestDto,
                                     Long currentUserId,
                                     String currentUserRole)
            throws NotFoundException, AccessDeniedException, BadRequestException;

    ServiceResponseDto patchService(Long id,
                                    ServiceRequestDto requestDto,
                                    Long currentUserId,
                                    String currentUserRole)
            throws NotFoundException, AccessDeniedException, BadRequestException;

    ServiceResponseDto getServiceById(Long id)
            throws NotFoundException;

    List<ServiceResponseDto> getAllServices();

    void deleteService(Long id,
                       Long currentUserId,
                       String currentUserRole)
            throws NotFoundException, AccessDeniedException;
}
