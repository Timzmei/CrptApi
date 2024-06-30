package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class CrptApi {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final long timeIntervalMillis;
    private final Semaphore semaphore;
    private final ScheduledExecutorService scheduler;

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
        this.timeIntervalMillis = timeUnit.toMillis(1);
        this.semaphore = new Semaphore(requestLimit);
        this.scheduler = Executors.newScheduledThreadPool(1);

        scheduler.scheduleAtFixedRate(() -> {
            semaphore.release(requestLimit - semaphore.availablePermits());
        }, timeIntervalMillis, timeIntervalMillis, TimeUnit.MILLISECONDS);
    }

    public void createDocument(Document document, String signature) throws IOException, InterruptedException {
        semaphore.acquire();

        String json = objectMapper.writeValueAsString(document);
        ObjectNode jsonNode = objectMapper.readValue(json, ObjectNode.class);
        jsonNode.put("signature", signature);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://ismp.crpt.ru/api/v3/lk/documents/create"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonNode.toString()))
                .build();

        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            // Обработка сетевых ошибок
            System.out.println("Ошибка отправки запроса: " + e.getMessage());
            return;
        } catch (InterruptedException e) {
            // Обработка прерывания
            System.out.println("Прерывание запроса: " + e.getMessage());
            return;
        }

        if (response.statusCode() >= 400) {
            // Обработка ошибок из ответа API
            System.out.println("Ошибка ответа API: " + response.statusCode() + " - " + response.body());
            return;
        }

        // Обработка успешного ответа
        System.out.println("Документ создан успешно: " + response.body());
    }

    public void shutdown() {
        scheduler.shutdown();
    }

    public static class Document {
        private String participantInn;
        private String docId;
        private String docStatus;
        private String docType;
        private boolean importRequest;
        private String ownerInn;
        private String producerInn;
        private String productionDate;
        private String productionType;
        private Product[] products;
        private String regDate;
        private String regNumber;

        public Document(String participantInn, String docId, String docStatus, String docType, boolean importRequest, String ownerInn, String producerInn, String productionDate, String productionType, Product[] products, String regDate, String regNumber) {
            this.participantInn = participantInn;
            this.docId = docId;
            this.docStatus = docStatus;
            this.docType = docType;
            this.importRequest = importRequest;
            this.ownerInn = ownerInn;
            this.producerInn = producerInn;
            this.productionDate = productionDate;
            this.productionType = productionType;
            this.products = products;
            this.regDate = regDate;
            this.regNumber = regNumber;
        }
    }

    public static class Product {
        private String certificateDocument;
        private String certificateDocumentDate;
        private String certificateDocumentNumber;
        private String ownerInn;
        private String producerInn;
        private String productionDate;
        private String tnvedCode;
        private String uitCode;
        private String uituCode;

        public Product(String certificateDocument, String certificateDocumentDate, String certificateDocumentNumber, String ownerInn, String producerInn, String productionDate, String tnvedCode, String uitCode, String uituCode) {
            this.certificateDocument = certificateDocument;
            this.certificateDocumentDate = certificateDocumentDate;
            this.certificateDocumentNumber = certificateDocumentNumber;
            this.ownerInn = ownerInn;
            this.producerInn = producerInn;
            this.productionDate = productionDate;
            this.tnvedCode = tnvedCode;
            this.uitCode = uitCode;
            this.uituCode = uituCode;
        }
    }
}
