package org.example;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        CrptApi api = new CrptApi(TimeUnit.MINUTES, 10);

        CrptApi.Product product = new CrptApi.Product(
                "certificate_document",
                "2020-01-23",
                "certificate_document_number",
                "owner_inn",
                "producer_inn",
                "2020-01-23",
                "tnved_code",
                "uit_code",
                "uitu_code"
        );

        CrptApi.Document document = new CrptApi.Document(
                "participantInn",
                "docId",
                "docStatus",
                "LP_INTRODUCE_GOODS",
                true,
                "ownerInn",
                "producerInn",
                "2020-01-23",
                "productionType",
                new CrptApi.Product[]{product},
                "2020-01-23",
                "regNumber"
        );

        String signature = "подпись";
        api.createDocument(document, signature);

        api.shutdown();
    }
    }
}