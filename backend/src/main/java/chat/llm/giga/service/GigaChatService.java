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
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class GigaChatService {
    private final String accessToken = "eyJjdHkiOiJqd3QiLCJlbmMiOiJBMjU2Q0JDLUhTNTEyIiwiYWxnIjoiUlNBLU9BRVAtMjU2In0.uorjbQTZLeFQ38TE_fktakITHYjA-htFqpV54xkqxHBuHZaY0sKzZGjy-OkvfAKtZ10Sezp_ZRN_m9LCd_hHVGLXGlfdf1-qYcueLM9DGHUzCrHS40ExdM22HS3Iqa-vE5c576fxOM7foxJEbPqnTMfmTI-gnexV6eLEw1OoqbBSvS7xGFm0sJ8R1ltmZmFpi7aB0xyoCZwrupTz0JdNcZ7KdI_cfXs7TPOgJu5I7BPrjXo-bBuby8OgVMEUG_ZKXx_ecjL_Pyv8EnEtUJyk3uZnnDBNJHwrrV7COpLbl2bOaL2JggB8kjMzeujod5YWwmqzmD5FHyuEfXXBERPIJg.DLKfPMPghVufKt_5_2CMvw.tUhLuZQgxYoBffIIpjXPqCEYc5m6iKoNNiVHa5RpZZOjoZtddnIaLmBhZ4gCGprEYaFEEc1uNgPEm31LuAkiUquk6wXXvrzTkoriGo-x1f0u1Ivr3uSfWJ_mL3vYqOOJzrKTiok9mIBCHhYc6F9V2VIfKQnLTbaQ5v6g5qjSs8UY7F88AWmqEKFEUeUZ-M9FzVdRMHzfLtaFw9g8a-LZAscjM_M4PdK-juz06q3wbJThWpcxEJx8127ElyXyY0KrYRYg_HhIYM_AywdcyfmC1P7RuoPPgrIB7aP3vIp8YTemcm1rTorRt1gCUlZ7eZLepsPdycvhclG8sRHeskyWG9Ik6bIPz4aNKL5VFWML3EXsB9aFruqoTzOhqUOnpYKo47lVa_tf4nbNFm5EbYjk6GSPdiSkZJaeMTgWPJHBP-l4pSWPee2zMG-X-XxTnuGR7nBfwqTelugGYIBOUK86tVobO2Eu0R3Mfw0jFrKj3vdWyvj-swIzlfIkfi8pCD-pe5IzbmOxar5bCNQWU15yJBzWbeg68AcmEOgUo6RMnqOZ5AmX58EWYXeCkCH6R9tub-H8U-IX7X9Driz_F30RDaW5o0spOrmU7mS4LiKzw0E43U15xUy5L0V8Ri4oCuwhWrbdEr7_3NafNlps_lHJ0Nm98jqZYe-Hu9knqGAWiiWK7E5M1AqfSeCpUE_jzGCBFOBg5-L-cqDNE_x_MBheTSdaXVAWRKB1G_zulS60uA4.4cFmr4lqqjWIYVL8_9ajotjosmdK62fr2k1uV5ePwXU";
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
//        headers.set("X-Session-ID", "dfa87a40-99a9-42c4-b810-6c7caa1e1e8");
        headers.set("X-Client-ID", new UUID(3,3).toString());
        headers.set("X-Request-ID", new UUID(4,4).toString());
        headers.set("X-Session-ID", new UUID(5,5).toString());
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
                         В любом другом случае необходимо дать ответ пользователю в рамках своей роли.
                        
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
