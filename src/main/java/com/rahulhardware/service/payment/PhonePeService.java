package com.rahulhardware.service.payment;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.rahulhardware.dto.phonepay.PhonePeCreateOrderRequest;
import com.rahulhardware.dto.phonepay.PhonePeCreateOrderResponse;
import com.rahulhardware.service.sms.SmsService;

@Service
public class PhonePeService {

        private final SmsService smsService;

        @Value("${phonepe.merchant-id}")
        private String merchantId;

        @Value("${phonepe.client-id}")
        private String clientId;

        @Value("${phonepe.client-secret}")
        private String clientSecret;

        @Value("${phonepe.client-version}")
        private String clientVersion;

        @Value("${phonepe.auth-url}")
        private String authUrl;

        @Value("${phonepe.create-order-url}")
        private String createOrderUrl;

        @Value("${phonepe.status-url}")
        private String statusUrl;

        private final RestTemplate restTemplate = new RestTemplate();

        private String accessToken;

        private long tokenExpiryEpochSeconds = 0;

        public PhonePeService(SmsService smsService) {
                this.smsService = smsService;
        }

        public PhonePeCreateOrderResponse createOrder(
                        PhonePeCreateOrderRequest request) {

                String authToken = getAuthToken();

                String merchantOrderId = request.getMerchantOrderId();

                if (merchantOrderId == null || merchantOrderId.isBlank()) {
                        merchantOrderId = "ORDER_" + UUID.randomUUID();
                }

                long amountInPaise = Math.round(request.getAmount() * 100);

                Map<String, Object> paymentFlow = new HashMap<>();
                paymentFlow.put("type", "PG_CHECKOUT");

                Map<String, Object> body = new HashMap<>();
                body.put("merchantOrderId", merchantOrderId);
                body.put("amount", amountInPaise);
                body.put("expireAfter", 1200);
                body.put("paymentFlow", paymentFlow);

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("Authorization", "O-Bearer " + authToken);

                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

                ResponseEntity<Map> response = restTemplate.postForEntity(
                                createOrderUrl,
                                entity,
                                Map.class);

                Map<String, Object> responseBody = response.getBody();

                if (responseBody == null) {
                        throw new RuntimeException(
                                        "PhonePe create order failed");
                }

                return new PhonePeCreateOrderResponse(
                                merchantOrderId,
                                String.valueOf(responseBody.get("orderId")),
                                String.valueOf(responseBody.get("token")),
                                String.valueOf(responseBody.get("state")));
        }

        public Map checkStatus(String merchantOrderId) {

                String authToken = getAuthToken();

                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", "O-Bearer " + authToken);

                HttpEntity<Void> entity = new HttpEntity<>(headers);

                String url = statusUrl + "/" + merchantOrderId + "/status";

                ResponseEntity<Map> response = restTemplate.exchange(
                                url,
                                HttpMethod.GET,
                                entity,
                                Map.class);

                Map responseBody = response.getBody();

                if (responseBody != null &&
                                "COMPLETED".equals(responseBody.get("state"))) {

                        String customerMobile = "8884592481";
                        String customerName = "Rahul";

                        smsService.sendOrderSuccessSms(
                                        customerMobile,
                                        customerName,
                                        Double.parseDouble(
                                                        responseBody.get("amount").toString()) / 100);
                }

                return responseBody;
        }

        private String getAuthToken() {

                long now = System.currentTimeMillis() / 1000;

                if (accessToken != null &&
                                now < tokenExpiryEpochSeconds - 60) {
                        return accessToken;
                }

                HttpHeaders headers = new HttpHeaders();

                headers.setContentType(
                                MediaType.APPLICATION_FORM_URLENCODED);

                LinkedMultiValueMap<String, String> body = new LinkedMultiValueMap<>();

                body.add("client_id", clientId);
                body.add("client_version", clientVersion);
                body.add("client_secret", clientSecret);
                body.add("grant_type", "client_credentials");

                HttpEntity<LinkedMultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

                ResponseEntity<Map> response = restTemplate.postForEntity(
                                authUrl,
                                request,
                                Map.class);

                Map<String, Object> responseBody = response.getBody();

                if (responseBody == null ||
                                responseBody.get("access_token") == null) {

                        throw new RuntimeException(
                                        "PhonePe auth token failed");
                }

                accessToken = responseBody.get("access_token").toString();

                Object expiresAt = responseBody.get("expires_at");

                tokenExpiryEpochSeconds = expiresAt == null
                                ? now + 3600
                                : Long.parseLong(expiresAt.toString());

                return accessToken;
        }
}