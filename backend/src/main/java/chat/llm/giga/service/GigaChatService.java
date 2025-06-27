package chat.llm.giga.service;

import chat.llm.giga.model.Message;

import chat.llm.giga.model.Promt;
import chat.llm.giga.model.PromtMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class GigaChatService {
    private final String accessToken = "eyJjdHkiOiJqd3QiLCJlbmMiOiJBMjU2Q0JDLUhTNTEyIiwiYWxnIjoiUlNBLU9BRVAtMjU2In0.OuAwfYtDkzlCCKGwQOUFnoo7pDY6U6kSTOT22kVNHAmaienulHE0mcRPwZ9xYuQq7kz4EiaWJp8-yAllDqBk4FS9PBUpoTlcOzexw9wQQKfd2ODEDd2r4cpZicUmoqzLJYUSNNyqUjdZbeefb9usWQ2116e5P3-WsJbXllWRdZfugCX0GAvNbQ1NbOMBjebAR83iXivOsr8JPe_xzhOY6kgcUGUrHVylghg8sLoM79R7T_Ids5i_LZHCCdbj_E3-1Jg2Wb-ShQ_ubEFf5eC9jp3pve5M1Kmv1HE2ON6BZ4JdTfSyGAYuLGSmbEvWL36KeIt9j97Z82D37TMUEc4DNw.Kbr_MZMu96VIZaehFR-mfw.Q-tfJAw-AQAW6TX_vK0vtZ3KWrQhDz7gMxScdt614HM-KWnNCnx3J-TlqrCk10QDZg9IR3AXK63j3elRS5FoNqAh9GtVIS96I-f-VJ4x7oAisRRpVhDAnLxYXlu9jMITvy0KCxtJQY5zh39VK33YF9lt6cqexm_hGZ6m-NC9wKSlLC7GKTJ6acyepJsUHiRXKT5ltc6k7zBpi424ZefEciQ96M1X9URrN-uuF_-MVeNlP6MRlOc1MemWk7lcQJTkWZBSyZMrocosnSWqjEvS8mJy9Q8CNTahIspW9-6l6sYc4ekZfbcQRlPgoHALJg-nXEQghBNPPuWG5PEH-rFMOU41boEDQwAWqGr-wvqrXEVpHH7YApfG7Vti_PJGGX5MMb7ia245ZfXNIYKXH2NC83o6ZE3exyFJ1OyqzYMygi_InIgIGeVG9hJackSL4xQm-6EaoyF9Uu2_bm_yASmyaRm7vAbxXosqXFLoqOAZS78OjabeLEuv3Ujhipu4Sh4owu8as8OdnDpZsaj1LpZdmkkhk5AbcByMmVpMzalWfERTDrHBYOkc2YqaQw25jGLDpkdRZw_1sgNZ00VGlc4uNmyP1xHwKC4pA238nLEsM9vMEf_SBWHNCerIpfrDRL1n5QKAy1HbVudEePuJJVcbv_EdYK8j6PUYO656OGejX6z29_TvADpZmob_wQ6KUXprgT7ndunSqJ_BpAUVUZOQlBjQ7pp0Zw9DfbuKBYYCjrE.85c653DY18512xAFZBQonp310pWDWfCeXRHYuwQwPoE";
    String url = "https://gigachat.devices.sberbank.ru/api/v1/chat/completions";
    private final SimpMessagingTemplate template;
    String status = "общение";
    BigDecimal balance = BigDecimal.valueOf(0);

    public void executeLLM(Message message) throws JsonProcessingException {

        if (status.equals("общение")) {
            ResponseEntity<String> response = enrichment(message);


            ObjectMapper mapper = new ObjectMapper();
            JsonNode modelResponse = mapper.readTree(response.getBody());
            String modelResponseContent = modelResponse
                    .path("choices")
                    .path(0)
                    .path("message")
                    .path("content")
                    .asText();
            System.out.println(modelResponseContent);
            switch (modelResponseContent) {
                case "Лимит." -> giveMeMainLimit();
                case "Ключевое слово." -> changeMainLimit();
                default ->
                        template.convertAndSend("/topic/group", new Message("Giga", modelResponseContent, LocalDateTime.now().toString()));
            }
            return;
        }
        if (status.equals("изменения лимита")) {
            if (!message.getContent().isEmpty()) { //todo проверка на число
                try {
                    balance = balance.add(BigDecimal.valueOf(Long.parseLong(message.getContent())));
                } finally {
                    template.convertAndSend("/topic/group",
                            new Message("Giga",
                                    "Напишите числами ", LocalDateTime.now().toString()));
                }

                template.convertAndSend("/topic/group",
                        new Message("Giga",
                                "Ваш лимит: " + balance, LocalDateTime.now().toString()));
            }
            status = "общение";
            return;
        }
        if (status.equals("проверка слова")) {
            if (message.getContent().equals("ключевое слово")) {
                template.convertAndSend("/topic/group",
                        new Message("Giga",
                                "Насколько вы хотите изменить лимит.", LocalDateTime.now().toString()));
                status = "изменения лимита";
            } else {
                template.convertAndSend("/topic/group",
                        new Message("Giga",
                                "Неправильное ключевое слово.", LocalDateTime.now().toString()));
                status = "общение";
            }
            return;
        }


    }

    public ResponseEntity<String> enrichment(Message message) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + accessToken);
//        headers.set("X-Session-ID", "dfa87a40-99a9-42c4-b810-6c7caa1e1e8");
        headers.set("X-Client-ID", new UUID(3, 3).toString());
        headers.set("X-Request-ID", new UUID(4, 4).toString());
        headers.set("X-Session-ID", new UUID(5, 5).toString());
        Promt promt = initialVerification();
        PromtMessage userMessage = new PromtMessage("user", message.getContent());
        promt.getMessages().add(userMessage);
        // Преобразуем в JSON
        ObjectMapper objectMapper = new ObjectMapper();
        String json = null;
        try {
            json = objectMapper.writeValueAsString(promt);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        HttpEntity<String> entity = new HttpEntity<>(json, headers);

        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.postForEntity(url, entity, String.class);
    }

    public Promt initialVerification() {
        Promt promt = new Promt();
        PromtMessage promtMessage = new PromtMessage("system", "Ты - персонаж технической поддержки. " +
                "Твоя задача — помогать пользователям решать технические проблемы." +
                """
                         Тебе будет дан запрос от пользователя.
                         Если пользователь хочет изменить лимит, то необходимо вернуть ответ в виде: "Ключевое слово.".
                         Если пользователь хочет узнать лимит, то необходимо вернуть ответ в виде: "Лимит.".
                         В любом другом случае необходимо дать ответ пользователю в рамках своей роли.
                        
                        """
        );
        promt.getMessages().add(promtMessage);
        return promt;
    }

    public void sending() {

    }

    public void changeMainLimit() {
        Map<String, Object> headers = new HashMap<>();
        headers.put("change-limit", "header-value");
        headers.put("another-header", 12345);
        status = "проверка слова";
        MessageHeaders messageHeaders = new MessageHeaders(headers);

        template.convertAndSend("/topic/group",
                new Message("Giga", "Напишите ваше ключевое слово, чтобы изменить лимит.", LocalDateTime.now().toString()),
                messageHeaders);
        System.out.println("Проверяем ключевое слово...");
    }

    public void giveMeMainLimit() {
        System.out.println("Запрашиваем лимит...");
        Map<String, Object> headers = new HashMap<>();
        headers.put("limit", "limit-value");
        template.convertAndSend("/topic/group", new Message("Giga", "Ваш лимит: " + balance, LocalDateTime.now().toString()),
                headers);

    }
}
