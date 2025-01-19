package com.laundry.mapper;

import com.laundry.dto.ServicePriceRequestDto;
import com.laundry.dto.ServicePriceResponseDto;
import com.laundry.entity.Service;
import com.laundry.entity.ServicePrice;
import com.laundry.util.Format;

import java.time.ZoneOffset;

public class ServicePriceMapper {

    public static ServicePrice toEntity(ServicePriceRequestDto dto, Service service) {
        ServicePrice entity = new ServicePrice();
        entity.setService(service);
        entity.setPrice(dto.getPrice());
        entity.setCurrencyCode(dto.getCurrencyCode());
        return entity;
    }

    public static ServicePriceResponseDto toDto(ServicePrice entity) {
        return ServicePriceResponseDto.builder()
                .id(entity.getId())
                .price(entity.getPrice())
                .currencyCode(entity.getCurrencyCode())
                .createdAt(Format.format(entity.getCreatedAt().toInstant(ZoneOffset.UTC)))
                .updatedAt(Format.format(entity.getUpdatedAt().toInstant(ZoneOffset.UTC)))
                .build();
    }
}
