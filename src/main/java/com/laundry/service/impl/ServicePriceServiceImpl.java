package com.laundry.service.impl;

import com.laundry.dto.ServicePriceRequestDto;
import com.laundry.dto.ServicePriceResponseDto;
import com.laundry.entity.Service;
import com.laundry.entity.ServicePrice;
import com.laundry.exception.AccessDeniedException;
import com.laundry.exception.BadRequestException;
import com.laundry.exception.NotFoundException;
import com.laundry.helper.RoleGuard;
import com.laundry.mapper.ServicePriceMapper;
import com.laundry.repository.ServicePriceRepository;
import com.laundry.repository.ServiceRepository;
import com.laundry.service.ServicePriceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Manages operations on ServicePrice entities, including creation, update, and retrieval
 * of prices in multiple currencies. When a price is created or updated, the system
 * automatically converts that price to other currencies (defined in {@link #AUTO_CURRENCIES})
 * and creates or updates corresponding records.
 *
 * <p>This implementation enforces ADMIN-only access for all CRUD operations, as specified
 * by role checks in each method. Conversion rates come from {@link TcmbCurrencyService}.
 *
 * <p>Primary responsibilities:
 * <ul>
 *     <li>Validating user role and existence checks for Services and ServicePrice records</li>
 *     <li>Creating/updating a main ServicePrice entry in whatever currency the user provides</li>
 *     <li>Automatically converting the provided price to TRY, then from TRY to other currencies,
 *         to keep multiple currency prices in sync</li>
 *     <li>Providing retrieval and deletion methods, restricted to ADMIN users</li>
 * </ul>
 */
@Slf4j
@org.springframework.stereotype.Service
@Transactional
public class ServicePriceServiceImpl implements ServicePriceService {

    private final ServiceRepository serviceRepository;

    private final ServicePriceRepository servicePriceRepository;

    private final TcmbCurrencyService tcmbCurrencyService;

    public ServicePriceServiceImpl(ServiceRepository serviceRepository,
                                   ServicePriceRepository servicePriceRepository,
                                   TcmbCurrencyService tcmbCurrencyService) {
        this.serviceRepository = serviceRepository;
        this.servicePriceRepository = servicePriceRepository;
        this.tcmbCurrencyService = tcmbCurrencyService;
    }

    @Override
    public ServicePriceResponseDto createServicePrice(Long serviceId,
                                                      ServicePriceRequestDto requestDto,
                                                      Long currentUserId,
                                                      String currentUserRole)
            throws NotFoundException, AccessDeniedException, BadRequestException {

        RoleGuard.requireAdminRole(currentUserRole, "Only admin can create service prices");

        Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new NotFoundException("Service not found: " + serviceId));

        if (servicePriceRepository.existsByServiceIdAndCurrencyCode(serviceId, requestDto.getCurrencyCode())) {
            throw new BadRequestException("A price with currency code " + requestDto.getCurrencyCode()
                    + " already exists for service ID " + serviceId);
        }

        ServicePrice mainEntity = handleMainPriceCreation(service, requestDto);

        syncOtherCurrencies(service, requestDto.getPrice(), requestDto.getCurrencyCode());

        return ServicePriceMapper.toDto(mainEntity);
    }

    @Override
    public ServicePriceResponseDto updateServicePrice(Long id,
                                                      ServicePriceRequestDto requestDto,
                                                      Long currentUserId,
                                                      String currentUserRole)
            throws NotFoundException, AccessDeniedException, BadRequestException {

        RoleGuard.requireAdminRole(currentUserRole, "Only admin can update service prices");

        ServicePrice existing = servicePriceRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("ServicePrice not found: " + id));

        if (!existing.getCurrencyCode().equals(requestDto.getCurrencyCode())) {
            if (servicePriceRepository.existsByServiceIdAndCurrencyCode(
                    existing.getService().getId(),
                    requestDto.getCurrencyCode())) {
                throw new BadRequestException("A price with currency code " + requestDto.getCurrencyCode()
                        + " already exists for service ID " + existing.getService().getId());
            }
        }

        handleMainPriceUpdate(existing, requestDto);
        syncOtherCurrencies(existing.getService(), requestDto.getPrice(), requestDto.getCurrencyCode());

        return ServicePriceMapper.toDto(existing);
    }

    @Override
    public ServicePriceResponseDto getServicePrice(Long id,
                                                   Long currentUserId,
                                                   String currentUserRole)
            throws NotFoundException, AccessDeniedException {
        RoleGuard.requireAdminRole(currentUserRole, "Only admin can view service prices");

        ServicePrice entity = servicePriceRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("ServicePrice not found: " + id));

        return ServicePriceMapper.toDto(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServicePriceResponseDto> getAllPricesByService(Long serviceId,
                                                               Long currentUserId,
                                                               String currentUserRole)
            throws NotFoundException, AccessDeniedException {

        RoleGuard.requireAdminRole(currentUserRole, "Only admin can view service prices");

        Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new NotFoundException("Service not found: " + serviceId));

        List<ServicePrice> prices = service.getServicePrices();
        return prices.stream()
                .map(ServicePriceMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ServicePriceResponseDto getServicePriceByServiceAndCurrencyCode(Long serviceId,
                                                                           String currencyCode,
                                                                           Long currentUserId,
                                                                           String currentUserRole)
            throws NotFoundException, AccessDeniedException {

        RoleGuard.requireAdminRole(currentUserRole, "Only admin can view service prices");

        ServicePrice entity = servicePriceRepository
                .findByServiceIdAndCurrencyCode(serviceId, currencyCode)
                .orElseThrow(() -> new NotFoundException("ServicePrice not found for service ID: "
                        + serviceId + " and currency: " + currencyCode));

        return ServicePriceMapper.toDto(entity);
    }

    @Override
    public void deleteServicePrice(Long id,
                                   Long currentUserId,
                                   String currentUserRole)
            throws NotFoundException, AccessDeniedException {

        RoleGuard.requireAdminRole(currentUserRole, "Only admin can delete service prices");

        ServicePrice entity = servicePriceRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("ServicePrice not found: " + id));
        servicePriceRepository.delete(entity);
    }

    /**
     * Creates the primary ServicePrice record based on user input, without handling
     * any auto-conversion logic.
     *
     * @param service the Service entity to link
     * @param dto     the {@link ServicePriceRequestDto} containing currency code and price
     * @return the newly saved ServicePrice entity
     */
    private ServicePrice handleMainPriceCreation(Service service, ServicePriceRequestDto dto) {
        ServicePrice mainEntity = ServicePriceMapper.toEntity(dto, service);
        servicePriceRepository.save(mainEntity);
        return mainEntity;
    }

    /**
     * Updates an existing ServicePrice entity with new currency code and price,
     * without performing any additional currency conversions.
     *
     * @param existing the existing ServicePrice record to be updated
     * @param dto      the new data to apply
     */
    private void handleMainPriceUpdate(ServicePrice existing, ServicePriceRequestDto dto) {
        existing.setCurrencyCode(dto.getCurrencyCode());
        existing.setPrice(dto.getPrice());
        servicePriceRepository.save(existing);
    }

    /**
     * Converts a source price (with its currency) into TRY, then iterates through
     * {@link #AUTO_CURRENCIES} to either create or update additional ServicePrice
     * records in other currencies. The source currency is skipped to avoid overriding
     * what the user explicitly provided.
     *
     * @param service        the Service for which these prices are managed
     * @param sourcePrice    the numeric value entered by the user
     * @param sourceCurrency the currency code of the entered value
     */
    private void syncOtherCurrencies(Service service, BigDecimal sourcePrice, String sourceCurrency) {
        double priceInTry = tcmbCurrencyService.convert(
                sourcePrice.doubleValue(),
                sourceCurrency,
                "TRY"
        );
        BigDecimal bdPriceInTry = BigDecimal.valueOf(priceInTry);

        for (String targetCurrency : AUTO_CURRENCIES) {
            if (targetCurrency.equalsIgnoreCase(sourceCurrency)) {
                // Skip rewriting the same currency that user just entered
                continue;
            }
            createOrUpdateAutoCurrencyPrice(service, bdPriceInTry, targetCurrency);
        }
    }

    /**
     * Either creates or updates a ServicePrice record in the specified <code>targetCurrency</code>,
     * derived from a base TRY amount. If a record for that currency already exists, its price
     * is updated. Otherwise, a new record is created.
     *
     * @param service        the Service entity for which the price is being calculated
     * @param tryPrice       the source price in TRY
     * @param targetCurrency the currency code to convert into and update/create
     */
    private void createOrUpdateAutoCurrencyPrice(Service service,
                                                 BigDecimal tryPrice,
                                                 String targetCurrency) {
        double convertedValue = tcmbCurrencyService.convert(
                tryPrice.doubleValue(),
                "TRY",
                targetCurrency
        );

        boolean exists = servicePriceRepository.existsByServiceIdAndCurrencyCode(
                service.getId(),
                targetCurrency
        );

        if (exists) {
            ServicePrice existingSp = servicePriceRepository
                    .findByServiceIdAndCurrencyCode(service.getId(), targetCurrency)
                    .orElse(null);
            if (existingSp != null) {
                existingSp.setPrice(BigDecimal.valueOf(convertedValue));
                servicePriceRepository.save(existingSp);
            }
        } else {
            ServicePrice newSp = new ServicePrice();
            newSp.setService(service);
            newSp.setCurrencyCode(targetCurrency);
            newSp.setPrice(BigDecimal.valueOf(convertedValue));
            servicePriceRepository.save(newSp);
        }
    }
}
