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
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;


@Service
@RequiredArgsConstructor
public class GigaChatService {
    private final String accessToken = "eyJjdHkiOiJqd3QiLCJlbmMiOiJBMjU2Q0JDLUhTNTEyIiwiYWxnIjoiUlNBLU9BRVAtMjU2In0.Exwr0hGOUusA-xYrl6gbhkZO2iqd9-gP5YP7bY_GGDs5Tk4v4exEA2Lq216gtN_83QU6grvUcCKBJBfM4UH_PszFV2OVM7tgNxGzwtc0O8nU69lcr-EHrwu360MIhQXY7LxUERohqscH1SNCe5ZWUuV2teFh5duRcOeiy9YYQi5E7jHNf7ytFPVWxdbyG2GkZPPjMnkl4n8kblflxy3WF246Fu5KX91A0MKDA16cR6wNkDd5sktETWVw8rMWm0lA7PN9m_Hx6lzaFbLnrZfnUnwKA2O-826YBtHLph8UDT3Y2I8ph9LRmGgyVsaBLm40dsUCB8-KbHBQ0dcwwguHlg.O2xPaJ1pqkkpSLQtgdHcVg.0lfnIQF-49MCWAn7-PBRvFei-1OzB3tq0PnV56t86e1UpdNBEXDIzNYcb7719T0BkMjFs7ybsSCloiKLGE4ByW6yNuLhI28ZSjWFxhkmZVjkFM3Ib7eEEYzQZn1Q33QvJLgpw8QdM2Mkdv0nQ1_R9fQAGSyCJyEQb2BlfP0pAgv2HV-AKH-EL8COsQYRi13I4jHhYXBUdurKbG32lTffWTiMhthaJG0m7NMHVtcpqfM8cFcR7MfPRLlP3-ucJneoft2gX--jRaWGCxXwlplCQvwC8ax1eBU6PtXFBd8nkVWJrCarQhPsDPX9D8ezBSuOSqI26VSeI53GK1dADUsf8EY8H6gDJ-Q8LRnQsjqZpWsmU5BfUVdAYrHQYxmVV_I8h8wsbGcD4KUYxYY4UC75LSJCjO1zMYZs7HS3-OTwzs8zeUwt0zFgrWbPDouo5FFAUVl7M4xfCqLMByB9giHVgoQvBB7zloXj_U3i-fF18edSNpSImTXCX202hYi9QDBpCd8J7gg8NCrSvNoTF8qFjLaytyAMI-hvHGoxR3fVSjtxjBuH7CgIBOTBQovEEs8riR7QDja4vVeyiATFOKhLXj2Hzgx6fGwx9iRyabmEtAP32NkgLqIEKiAuzkj6dbL1PDCnB-SJIG046U6QhjD64AP7zz4vZfVjv4S5d10qotfBGp7oq5E-tDLKgeLmj6J2SYpTCJ95GOewkvPS-FIT0jXmcGwLd8CkoAGz5KQexa8.Ebh-HY5EJt5y2ehZOTnchzgwgbAgYagMAxhGROFE0Fk";
    String url = "https://gigachat.devices.sberbank.ru/api/v1/chat/completions";
    private final SimpMessagingTemplate template;
    String status = "общение";
    BigDecimal balance = BigDecimal.valueOf(0);

    public void executeLLM(Message message) throws JsonProcessingException {

        if (status.equals("общение")){
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
                case "Баланс." -> giveMeBalance();
                case "Ключевое слово." -> changeMainBalance(message.getContent(), modelResponseContent);
                default ->
                        template.convertAndSend("/topic/group", new Message("Giga", modelResponseContent, LocalDateTime.now().toString()));
            }
            return;
        }
        if (status.equals("изменения баланса")) {
            if (!message.getContent().isEmpty()) { //todo проверка на число
                balance = balance.add(BigDecimal.valueOf(Long.parseLong(message.getContent())));
                template.convertAndSend("/topic/group",
                        new Message("Giga",
                                "Ваш баланс: " + balance, LocalDateTime.now().toString()));
            }
            status = "общение";
            return;
        }
        if (status.equals("проверка слова")) {
            if (message.getContent().equals("ключевое слово")) {
                template.convertAndSend("/topic/group",
                        new Message("Giga",
                                "Насколько вы хотите изменить баланс.", LocalDateTime.now().toString()));
                status = "изменения баланса";
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
        headers.set("X-Session-ID", "dfa87a40-99a9-42c4-b810-6c7caa1e1e8");
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
                         Тебе будет дан ответ пользователя.
                         Если пользователь хочет сменить баланс, то необходимо вернуть ответ в виде: "Ключевое слово.".
                         Если пользователь хочет узнать баланс, то необходимо вернуть ответ в виде: "Баланс.".
                         В любом другом случае необходимо вернуть ответ в виде краткой выжимки и ответа пользователя.
                        
                        """
        );
        promt.getMessages().add(promtMessage);
        return promt;
    }

    public void sending() {

    }

    public void changeMainBalance(String userMessage, String modelResponseContent) {
        Map<String, Object> headers = new HashMap<>();
        headers.put("change-balance", "header-value");
        headers.put("another-header", 12345);
        status = "проверка слова";
        MessageHeaders messageHeaders = new MessageHeaders(headers);

        template.convertAndSend("/topic/group",
                new Message("Giga", "Напишите ваше ключевое слово, чтобы изменить баланс.", LocalDateTime.now().toString()),
                messageHeaders);
        System.out.println("Проверяем ключевое слово...");


    }

    public void giveMeBalance() {
        System.out.println("Запрашиваем баланс...");
        Map<String, Object> headers = new HashMap<>();
        headers.put("balance", "balance-value");
        template.convertAndSend("/topic/group", new Message("Giga", "Ваш баланс: " + balance, LocalDateTime.now().toString()),
                headers);

    }
}
