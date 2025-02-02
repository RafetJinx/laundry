package com.laundry.controller;

import com.laundry.dto.*;
import com.laundry.security.JwtUtil;
import com.laundry.service.ServicePriceService;
import io.swagger.v3.oas.annotations.Operation;              // Example for OpenAPI
import io.swagger.v3.oas.annotations.tags.Tag;               // Example for OpenAPI
import io.swagger.v3.oas.annotations.Parameter;              // Example for OpenAPI
import io.swagger.v3.oas.annotations.responses.ApiResponse;  // Example for OpenAPI
import io.swagger.v3.oas.annotations.responses.ApiResponses; // Example for OpenAPI
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing ServicePrice operations.
 * <p>Handles creation, retrieval, update, and deletion of currency-specific prices
 * for a given Service. Admin-only endpoints (role checks are performed per request).</p>
 *
 * <p>Supported endpoints:</p>
 * <ul>
 *   <li><strong>POST /api/services/{serviceId}/prices</strong>: Create a ServicePrice</li>
 *   <li><strong>GET /api/services/{serviceId}/prices</strong>: List all prices for the service</li>
 *   <li><strong>GET /api/services/{serviceId}/prices/{priceId}</strong>: Get a price by priceId</li>
 *   <li><strong>GET /api/services/{serviceId}/prices/currency/{currencyCode}</strong>: Get a price by currency</li>
 *   <li><strong>PUT /api/services/{serviceId}/prices/{priceId}</strong>: Update a ServicePrice</li>
 *   <li><strong>DELETE /api/services/{serviceId}/prices/{priceId}</strong>: Delete a ServicePrice</li>
 * </ul>
 */
@Tag(name = "Service Price Management")
@RestController
@RequestMapping("/api/services/{serviceId}/prices")
@Slf4j
public class ServicePriceController {

    private final ServicePriceService servicePriceService;

    public ServicePriceController(ServicePriceService servicePriceService) {
        this.servicePriceService = servicePriceService;
    }

    /**
     * Creates a ServicePrice record for the specified service.
     *
     * @param serviceId   the ID of the service
     * @param requestDto  the DTO containing currency code and price information
     * @param authentication the Spring Security authentication token
     * @return a {@link ServicePriceResponseDto} wrapped in {@link ApiResponse} upon success
     */
    @Operation(
            summary = "Create a new ServicePrice for a given service",
            description = "Requires ADMIN privileges. Automatically converts price into other currencies if configured."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Service price created successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Service not found"),
            @ApiResponse(responseCode = "400", description = "Bad request if price for the currency already exists")
    })
    @PostMapping
    public ResponseEntity<com.laundry.dto.ApiResponse<ServicePriceResponseDto>> createServicePrice(
            @Parameter(description = "The ID of the service") @PathVariable Long serviceId,
            @RequestBody ServicePriceRequestDto requestDto,
            Authentication authentication
    ) {
        Long currentUserId = JwtUtil.getUserIdFromAuthentication(authentication);
        String currentUserRole = JwtUtil.getRoleFromAuthentication(authentication);

        ServicePriceResponseDto created = servicePriceService.createServicePrice(
                serviceId,
                requestDto,
                currentUserId,
                currentUserRole
        );
        return ResponseEntity.ok(
                com.laundry.dto.ApiResponse.success("Service price created successfully", created)
        );
    }

    /**
     * Retrieves all ServicePrice records for the specified service.
     *
     * @param serviceId the ID of the service
     * @param authentication the Spring Security authentication token
     * @return a list of {@link ServicePriceResponseDto} in an {@link ApiResponse}
     */
    @Operation(
            summary = "Retrieve all prices for a given service",
            description = "Requires ADMIN privileges. Returns a list of ServicePriceResponseDto."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of service prices"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Service not found")
    })
    @GetMapping
    public ResponseEntity<com.laundry.dto.ApiResponse<List<ServicePriceResponseDto>>> getAllPricesByService(
            @Parameter(description = "The ID of the service") @PathVariable Long serviceId,
            Authentication authentication
    ) {
        Long currentUserId = JwtUtil.getUserIdFromAuthentication(authentication);
        String currentUserRole = JwtUtil.getRoleFromAuthentication(authentication);

        List<ServicePriceResponseDto> prices = servicePriceService.getAllPricesByService(
                serviceId,
                currentUserId,
                currentUserRole
        );
        return ResponseEntity.ok(
                com.laundry.dto.ApiResponse.success("Prices for service " + serviceId, prices)
        );
    }

    /**
     * Retrieves a single ServicePrice by its priceId.
     *
     * @param serviceId the ID of the service
     * @param priceId the ID of the ServicePrice
     * @param authentication the Spring Security authentication token
     * @return a {@link ServicePriceResponseDto} if found
     */
    @Operation(
            summary = "Retrieve a single ServicePrice by priceId",
            description = "Requires ADMIN privileges."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Service price found"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "ServicePrice not found")
    })
    @GetMapping("/{priceId}")
    public ResponseEntity<com.laundry.dto.ApiResponse<ServicePriceResponseDto>> getServicePrice(
            @Parameter(description = "The ID of the service") @PathVariable Long serviceId,
            @Parameter(description = "The ID of the ServicePrice") @PathVariable Long priceId,
            Authentication authentication
    ) {
        Long currentUserId = JwtUtil.getUserIdFromAuthentication(authentication);
        String currentUserRole = JwtUtil.getRoleFromAuthentication(authentication);

        ServicePriceResponseDto dto = servicePriceService.getServicePrice(
                priceId,
                currentUserId,
                currentUserRole
        );
        return ResponseEntity.ok(
                com.laundry.dto.ApiResponse.success("Service price found", dto)
        );
    }

    /**
     * Retrieves a single ServicePrice by currency code for the specified service.
     *
     * @param serviceId     the ID of the service
     * @param currencyCode  the currency code to look up
     * @param authentication the Spring Security authentication token
     * @return a {@link ServicePriceResponseDto} if found
     */
    @Operation(
            summary = "Retrieve a single ServicePrice by currency code",
            description = "Requires ADMIN privileges."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Service price found"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "ServicePrice not found")
    })
    @GetMapping("/currency/{currencyCode}")
    public ResponseEntity<com.laundry.dto.ApiResponse<ServicePriceResponseDto>> getServicePriceByCurrency(
            @Parameter(description = "The ID of the service") @PathVariable Long serviceId,
            @Parameter(description = "The currency code, e.g. USD, EUR") @PathVariable String currencyCode,
            Authentication authentication
    ) {
        Long currentUserId = JwtUtil.getUserIdFromAuthentication(authentication);
        String currentUserRole = JwtUtil.getRoleFromAuthentication(authentication);

        ServicePriceResponseDto dto = servicePriceService.getServicePriceByServiceAndCurrencyCode(
                serviceId,
                currencyCode,
                currentUserId,
                currentUserRole
        );
        return ResponseEntity.ok(
                com.laundry.dto.ApiResponse.success("Service price found by currency", dto)
        );
    }

    /**
     * Updates an existing ServicePrice by its priceId.
     *
     * @param serviceId   the ID of the service
     * @param priceId     the ID of the ServicePrice to update
     * @param requestDto  the new price details
     * @param authentication the Spring Security authentication token
     * @return the updated {@link ServicePriceResponseDto}
     */
    @Operation(
            summary = "Update an existing ServicePrice",
            description = "Requires ADMIN privileges. If the currency code changes, checks for duplicates."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Service price updated successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "ServicePrice not found"),
            @ApiResponse(responseCode = "400", description = "Duplicate currency code or invalid data")
    })
    @PutMapping("/{priceId}")
    public ResponseEntity<com.laundry.dto.ApiResponse<ServicePriceResponseDto>> updateServicePrice(
            @Parameter(description = "The ID of the service") @PathVariable Long serviceId,
            @Parameter(description = "The ID of the ServicePrice") @PathVariable Long priceId,
            @RequestBody ServicePriceRequestDto requestDto,
            Authentication authentication
    ) {
        Long currentUserId = JwtUtil.getUserIdFromAuthentication(authentication);
        String currentUserRole = JwtUtil.getRoleFromAuthentication(authentication);

        ServicePriceResponseDto updated = servicePriceService.updateServicePrice(
                priceId,
                requestDto,
                currentUserId,
                currentUserRole
        );
        return ResponseEntity.ok(
                com.laundry.dto.ApiResponse.success("Service price updated successfully", updated)
        );
    }

    /**
     * Deletes an existing ServicePrice by its priceId.
     *
     * @param serviceId       the ID of the service
     * @param priceId         the ID of the ServicePrice to delete
     * @param authentication  the Spring Security authentication token
     * @return a success message if deletion was successful
     */
    @Operation(
            summary = "Delete an existing ServicePrice",
            description = "Requires ADMIN privileges."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Service price deleted"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "ServicePrice not found")
    })
    @DeleteMapping("/{priceId}")
    public ResponseEntity<com.laundry.dto.ApiResponse<?>> deleteServicePrice(
            @Parameter(description = "The ID of the service") @PathVariable Long serviceId,
            @Parameter(description = "The ID of the ServicePrice") @PathVariable Long priceId,
            Authentication authentication
    ) {
        Long currentUserId = JwtUtil.getUserIdFromAuthentication(authentication);
        String currentUserRole = JwtUtil.getRoleFromAuthentication(authentication);

        servicePriceService.deleteServicePrice(
                priceId,
                currentUserId,
                currentUserRole
        );
        return ResponseEntity.ok(
                com.laundry.dto.ApiResponse.success("Service price deleted", null)
        );
    }
}
