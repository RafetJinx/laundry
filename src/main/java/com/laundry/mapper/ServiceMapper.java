package com.laundry.mapper;

import com.laundry.dto.ServiceResponseDto;
import com.laundry.dto.ServiceRequestDto;
import com.laundry.entity.Service;
import com.laundry.util.Format;

import java.time.ZoneOffset;
import java.util.stream.Collectors;

public class ServiceMapper {

    public static Service toEntity(ServiceRequestDto dto) {
        Service entity = new Service();
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        return entity;
    }

    public static ServiceResponseDto toResponseDto(Service entity) {
        return ServiceResponseDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .createdAt(Format.format(entity.getCreatedAt().toInstant(ZoneOffset.UTC)))
                .updatedAt(Format.format(entity.getUpdatedAt().toInstant(ZoneOffset.UTC)))
                .prices(
                        entity.getServicePrices().stream()
                                .map(ServicePriceMapper::toDto)
                                .collect(Collectors.toList())
                )
                .build();
    }
}
