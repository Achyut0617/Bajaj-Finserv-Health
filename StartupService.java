package com.bajaj.qualifier.service;

import com.bajaj.qualifier.model.SqlSubmissionRequest;
import com.bajaj.qualifier.model.WebhookRequest;
import com.bajaj.qualifier.model.WebhookResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Runs automatically on application startup (no controller/endpoint needed).
 *
 * Flow:
 *  1. POST to generateWebhook  →  receive webhook URL + accessToken
 *  2. Determine question from regNo last two digits (47 = odd → Question 1)
 *  3. POST final SQL query to the webhook URL with JWT in Authorization header
 */
@Service
public class StartupService implements ApplicationRunner {

    // ── Your personal details ──────────────────────────────────────────────────
    private static final String NAME   = "John Doe";       // ← replace with your name
    private static final String REG_NO = "REG12347";       // ← replace with your regNo
    private static final String EMAIL  = "john@example.com"; // ← replace with your email

    // ── Endpoint ───────────────────────────────────────────────────────────────
    private static final String GENERATE_WEBHOOK_URL =
            "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";

    // ── SQL query for Question 1 (regNo last two digits = odd) ─────────────────
    // Find the highest salary NOT paid on the 1st of any month,
    // along with employee name, age, and department.
    private static final String FINAL_SQL_QUERY =
            "SELECT p.AMOUNT AS SALARY, " +
            "CONCAT(e.FIRST_NAME, ' ', e.LAST_NAME) AS NAME, " +
            "FLOOR(DATEDIFF(CURDATE(), e.DOB) / 365) AS AGE, " +
            "d.DEPARTMENT_NAME " +
            "FROM PAYMENTS p " +
            "JOIN EMPLOYEE e ON p.EMP_ID = e.EMP_ID " +
            "JOIN DEPARTMENT d ON e.DEPARTMENT = d.DEPARTMENT_ID " +
            "WHERE DAY(p.PAYMENT_TIME) != 1 " +
            "ORDER BY p.AMOUNT DESC " +
            "LIMIT 1";

    private final RestTemplate restTemplate;

    @Autowired
    public StartupService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        System.out.println("=== Bajaj Finserv Qualifier - Starting up ===");

        // ── Step 1: Generate webhook ───────────────────────────────────────────
        WebhookResponse webhookResponse = generateWebhook();
        if (webhookResponse == null) {
            System.err.println("[ERROR] Failed to generate webhook. Aborting.");
            return;
        }

        String webhookUrl  = webhookResponse.getWebhook();
        String accessToken = webhookResponse.getAccessToken();

        System.out.println("[INFO] Webhook URL   : " + webhookUrl);
        System.out.println("[INFO] Access Token  : " + accessToken);

        // ── Step 2: Determine question (last two digits of regNo) ──────────────
        String digits = REG_NO.replaceAll("[^0-9]", "");
        int lastTwo   = Integer.parseInt(digits.substring(digits.length() - 2));
        boolean isOdd = (lastTwo % 2 != 0);
        System.out.println("[INFO] Last two digits of regNo: " + lastTwo
                           + " → Question " + (isOdd ? "1 (Odd)" : "2 (Even)"));

        // ── Step 3: Submit SQL answer to webhook ───────────────────────────────
        submitAnswer(webhookUrl, accessToken, FINAL_SQL_QUERY);
    }

    // ── Helper: POST to generateWebhook ───────────────────────────────────────
    private WebhookResponse generateWebhook() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            WebhookRequest body = new WebhookRequest(NAME, REG_NO, EMAIL);
            HttpEntity<WebhookRequest> entity = new HttpEntity<>(body, headers);

            System.out.println("[INFO] Calling generateWebhook endpoint...");
            ResponseEntity<WebhookResponse> response =
                    restTemplate.postForEntity(GENERATE_WEBHOOK_URL, entity, WebhookResponse.class);

            System.out.println("[INFO] generateWebhook HTTP status: " + response.getStatusCode());
            return response.getBody();

        } catch (Exception e) {
            System.err.println("[ERROR] generateWebhook failed: " + e.getMessage());
            return null;
        }
    }

    // ── Helper: POST SQL answer with JWT to webhook URL ───────────────────────
    private void submitAnswer(String webhookUrl, String accessToken, String sqlQuery) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            // JWT token in Authorization header (no "Bearer " prefix — raw token as per spec)
            headers.set("Authorization", accessToken);

            SqlSubmissionRequest body = new SqlSubmissionRequest(sqlQuery);
            HttpEntity<SqlSubmissionRequest> entity = new HttpEntity<>(body, headers);

            System.out.println("[INFO] Submitting SQL answer to: " + webhookUrl);
            System.out.println("[INFO] SQL Query: " + sqlQuery);

            ResponseEntity<String> response =
                    restTemplate.postForEntity(webhookUrl, entity, String.class);

            System.out.println("[INFO] Submission HTTP status  : " + response.getStatusCode());
            System.out.println("[INFO] Submission response body: " + response.getBody());
            System.out.println("=== Submission complete ===");

        } catch (Exception e) {
            System.err.println("[ERROR] Answer submission failed: " + e.getMessage());
        }
    }
}
