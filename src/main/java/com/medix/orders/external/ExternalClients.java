package com.medix.orders.external;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Thin wrappers over the three external providers. Each call is made inline from the
 * checkout thread with no retry queue and no circuit breaker.
 */
@Component
public class ExternalClients {

    private final RestTemplate restTemplate;
    private final String paymentBaseUrl;
    private final String insurerBaseUrl;
    private final String courierBaseUrl;

    public ExternalClients(RestTemplate restTemplate,
                           @Value("${pharmacy.payment.base-url}") String paymentBaseUrl,
                           @Value("${pharmacy.insurer.base-url}") String insurerBaseUrl,
                           @Value("${pharmacy.courier.base-url}") String courierBaseUrl) {
        this.restTemplate = restTemplate;
        this.paymentBaseUrl = paymentBaseUrl;
        this.insurerBaseUrl = insurerBaseUrl;
        this.courierBaseUrl = courierBaseUrl;
    }

    /** Captures payment via the external gateway. Blocks the checkout thread. */
    public String capturePayment(String customerId, BigDecimal amount, String currency) {
        Map<String, Object> body = Map.of(
                "customerId", customerId,
                "amount", amount,
                "currency", currency);
        ResponseEntity<Map> resp = restTemplate.postForEntity(
                paymentBaseUrl + "/v1/captures", body, Map.class);
        return String.valueOf(resp.getBody().get("captureId"));
    }

    /**
     * Files an insurance claim with the external insurer. This provider has a p95 of
     * ~11s and enforces a hard per-country daily quota, surfacing HTTP 429 when the
     * quota is exhausted.
     */
    public String fileClaim(Long orderId, String country, BigDecimal amount) {
        Map<String, Object> body = Map.of(
                "orderId", orderId,
                "country", country,
                "amount", amount);
        try {
            ResponseEntity<Map> resp = restTemplate.postForEntity(
                    insurerBaseUrl + "/claims", body, Map.class);
            return String.valueOf(resp.getBody().get("claimId"));
        } catch (RestClientResponseException e) {
            if (e.getRawStatusCode() == 429) {
                throw new InsurerQuotaExceededException(country);
            }
            throw e;
        }
    }

    /** Books a courier for delivery. No retry is attempted on timeout. */
    public String bookCourier(Long orderId, String country) {
        Map<String, Object> body = Map.of(
                "orderId", orderId,
                "country", country);
        ResponseEntity<Map> resp = restTemplate.postForEntity(
                courierBaseUrl + "/bookings", body, Map.class);
        return String.valueOf(resp.getBody().get("bookingId"));
    }

    public static class InsurerQuotaExceededException extends RuntimeException {
        public InsurerQuotaExceededException(String country) {
            super("insurer quota_exceeded country=" + country);
        }
    }
}
