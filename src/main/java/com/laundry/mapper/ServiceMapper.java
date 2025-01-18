package com.laundry.mapper;

import com.laundry.dto.ServiceRequestDto;
import com.laundry.dto.ServiceResponseDto;
import com.laundry.entity.Service;

import static com.laundry.mapper.DateTimeUtil.formatLocalDateTime;

public class ServiceMapper {

    public static Service toEntity(ServiceRequestDto requestDto) {
        if (requestDto == null) {
            return null;
        }

        Service service = new Service();
        service.setName(requestDto.getName());
        service.setDescription(requestDto.getDescription());
        service.setPrice(requestDto.getPrice());
        service.setCurrencyCode(requestDto.getCurrencyCode());
        return service;
    }

    public static ServiceResponseDto toResponseDto(Service service) {
        if (service == null) {
            return null;
        }

        return ServiceResponseDto.builder()
                .id(service.getId())
                .name(service.getName())
                .description(service.getDescription())
                .price(service.getPrice())
                .currencyCode(service.getCurrencyCode())
                .createdAt(formatLocalDateTime(service.getCreatedAt()))
                .updatedAt(formatLocalDateTime(service.getUpdatedAt()))
                .build();
    }
}
