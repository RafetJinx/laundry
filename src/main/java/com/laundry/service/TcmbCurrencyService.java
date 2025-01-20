package com.laundry.service;

import com.laundry.exception.InternalServerErrorException;
import com.laundry.exception.RestClientException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Service that retrieves currency exchange rates from the Turkish Central Bank (TCMB)
 * and stores them in an in-memory map. Provides methods to query or convert between
 * currencies, all relative to the TRY base rate.
 * <p>
 * <strong>Usage:</strong>
 * <ul>
 *   <li>This service automatically updates rates once on application startup
 *       via {@link #updateRatesFromTcmb()}.</li>
 *   <li>Optionally, you can schedule recurring updates using a Spring Scheduler
 *       (e.g., {@code @Scheduled(cron = "0 0 7 * * ?")}).</li>
 *   <li>Use {@link #getRateForCurrency(String)} to retrieve the exchange rate
 *       for a currency code relative to TRY, or {@link #convert(double, String, String)}
 *       to convert from one currency to another.</li>
 * </ul>
 *
 * <p>Example:
 * <pre>
 *  double rateUsd = tcmbService.getRateForCurrency("USD"); // e.g. 35.41 (means 1 USD = 35.41 TRY)
 *  double eurValue = tcmbService.convert(100, "USD", "EUR"); // convert $100 to EUR
 * </pre>
 */
@Slf4j
@Service
public class TcmbCurrencyService {

    @Value("${app.tcmb.url}")
    private String tcmbUrl;
    private final Map<String, Double> currencyToTryRate = new HashMap<>();

    /**
     * Initializes the service by fetching and parsing the TCMB XML data once
     * at application startup. If any error occurs (e.g., network or parse failure),
     * it is logged, and a {@link RestClientException} or
     * {@link InternalServerErrorException} may be thrown to indicate the issue.
     * <p>
     * In a production environment, you might handle these exceptions more gracefully
     * or apply a fallback strategy if the remote servicep is unavailable.
     */
    @PostConstruct
    public void updateRatesFromTcmb() {
        try {
            RestTemplate restTemplate = new RestTemplate();

            log.info("Fetching TCMB rates from {}", tcmbUrl);
            String xml = restTemplate.getForObject(tcmbUrl, String.class);
            if (xml == null || xml.isBlank()) {
                log.error("Received empty or null response from TCMB at {}", Instant.now());
                throw new RestClientException("TCMB returned empty response.");
            }

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
            doc.getDocumentElement().normalize();

            NodeList currencyNodes = doc.getElementsByTagName("Currency");
            int count = 0;
            for (int i = 0; i < currencyNodes.getLength(); i++) {
                Element currencyEl = (Element) currencyNodes.item(i);
                String code = currencyEl.getAttribute("CurrencyCode");

                String forexBuyingStr = getTagValue("ForexBuying", currencyEl);
                if (forexBuyingStr == null || forexBuyingStr.isBlank()) {
                    // Skip if missing or blank
                    continue;
                }

                double forexBuying = Double.parseDouble(forexBuyingStr);
                currencyToTryRate.put(code, forexBuying);
                count++;
            }

            currencyToTryRate.put("TRY", 1.0);

            log.info("TCMB rates updated successfully. Found {} currency entries.", count);

        } catch (RestClientException restEx) {
            log.error("Error calling TCMB service: {}", restEx.getMessage(), restEx);
            // Rethrow a custom exception that the global handler can interpret as 502
            throw new RestClientException("Failed to call TCMB: " + restEx.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error parsing TCMB data: {}", e.getMessage(), e);
            // Rethrow as a 500-level internal error
            throw new InternalServerErrorException("Parsing error from TCMB response: " + e.getMessage());
        }
    }

    /**
     * Retrieves the exchange rate for a given currency code relative to TRY.
     * Example: if "USD" maps to 35.41, it means 1 USD = 35.41 TRY.
     *
     * @param currencyCode the currency code to look up (e.g. "USD", "EUR", "TRY")
     * @return the numeric rate as a Double, or null if the currency is not present
     */
    public Double getRateForCurrency(String currencyCode) {
        return currencyToTryRate.get(currencyCode);
    }

    /**
     * Converts the given amount from one currency to another based on the rates in
     * {@link #currencyToTryRate}. Internally, this method does:
     * <ol>
     *   <li>Converts {@code amount} from {@code fromCurrency} to TRY.</li>
     *   <li>Converts that result from TRY to {@code toCurrency}.</li>
     * </ol>
     * <p>Example: converting 100 USD to EUR uses two steps:
     * <pre>
     *    100 USD -> 100 * rate(USD) = X in TRY
     *    X TRY -> X / rate(EUR) = Y in EUR
     * </pre>
     *
     * @param amount       the monetary amount to be converted
     * @param fromCurrency the currency code of the source (e.g. "USD")
     * @param toCurrency   the currency code of the target (e.g. "EUR")
     * @return the converted amount
     * @throws IllegalArgumentException if {@code fromCurrency} or {@code toCurrency} is not found in the map
     */
    public double convert(double amount, String fromCurrency, String toCurrency) {
        Double fromRate = currencyToTryRate.get(fromCurrency);
        Double toRate = currencyToTryRate.get(toCurrency);

        if (fromRate == null) {
            log.error("Unsupported currency: {} requested for conversion", fromCurrency);
            throw new IllegalArgumentException("Unsupported currency: " + fromCurrency);
        }
        if (toRate == null) {
            log.error("Unsupported currency: {} requested for conversion", toCurrency);
            throw new IllegalArgumentException("Unsupported currency: " + toCurrency);
        }

        double amountInTry = amount * fromRate;  // fromCurrency -> TRY
        return amountInTry / toRate;             // TRY -> toCurrency
    }

    /**
     * Retrieves the text content of a given {@code tag} within the provided XML element.
     * If the tag is not found or the content is empty, returns null.
     *
     * @param tag     the XML tag name (e.g. "ForexBuying")
     * @param element the parent element
     * @return trimmed text content, or null if none is found
     */
    private String getTagValue(String tag, Element element) {
        NodeList nodeList = element.getElementsByTagName(tag);
        if (nodeList.getLength() > 0) {
            return nodeList.item(0).getTextContent().trim();
        }
        return null;
    }
}
