package chat.llm.giga.service;

import chat.llm.giga.model.Message;

import chat.llm.giga.model.Promt;
import chat.llm.giga.model.PromtMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;


@Service
public class GigaChatService {
    private final String accessToken = "eyJjdHkiOiJqd3QiLCJlbmMiOiJBMjU2Q0JDLUhTNTEyIiwiYWxnIjoiUlNBLU9BRVAtMjU2In0.KPmnzzjhUo1Fsc5D2dVcaP8_pUODK9KEQ9vgHSSDla0Qt3y-6lGnCHUYCJkDaa6B_OGei41FB38GH8PEKK-OI_3nMi4ceND1YlMovrVBMk5VXfK9DHexj-rxxPXzvRMP7vmjr067NPHQpcEp6LZ-XK1GBXXhNGpomtgj43WCfR-iAYCfbBBHMPj_P4l-gLPX_X5dBhz0WQ1nYSIlOpw62Eq78yX0k1Ht9Ha1ubhCv1Ka9GwoIhKTqF8_9mzGovBpYigScksspwQhPXJA6T6Li5Kfc8irc4auRHqDZUW4hFt7SSckjrWRzrV11Pk3WKNWfRqCBJv0NZz2RuVUNz3DaQ.8iTFceC8esyiyh6WadRmHg.F9nVb9xlP_rosJtdv4PB9zP_TtDWrmXrkVQ_jo8qSoXiD69OJTUyQrDp7r_RdOp8ISqItrxeHzWtfR9dC48RWNq93LnUXzls0XRLp8llCQ7FedoEn-vr79OB0lIs4q1S291xl8haCmctEGVAAwsfIY-CE0LkVINr6eFyHjv0FhRRAFIBb5--GWdBkX0Bo2OW7FDhHEYO8WX7QGfOVV_z_LyAr_0vIPWUufORym10r_NrLqCtutK1jB0LbOZ81nzpNhEFpauirX8FB6RfaE2ZCHqZn_eUeapihhbpI31KNjDaK1K-fHV-kEEaOcP-3VeNrPAQ_LqnNynkg9xljXIwY1_Ns5BtyzsR58Z5c3Ju4p2N97qas8gst73TwaQWVlRid2ZYai9DGxz7UHLMmYnsbfULJI0ynPjsVcWo4TkqIA2AFZYBNs9zTKRxApja_CC0KJqs_VBHYS4EldRCA3USUqlKuHn1p-1q8CS9RIjkdjUkfiEfbtI2Ed014cOTg2_ZH45zYoLtavmMSRz7_whqGSgmgcFfCnQzWYHMKkK7pihlNAqPtz7f_dzSy5Ii1Yf-yWcrXvu8jvlcw7y0OGvq6uO2-Mc8AM3JKa2CNtifmfpeErosqmybM23-NlHSgrfuTfjsRH05Bh_Wm11Cuf8FHOLCYHxdHwVHPqQEzPqRW_f_GDSItw9p_jLGs2Ip8kR4a8AmLwL2-9h8-GeqOqZXAYAKHhzKJ32iEkiQGr8_HmE.uYaKuIxEOgSadX_7mnqim9otzJyq6YAi_sMLjsdY28k";
    String url = "https://gigachat.devices.sberbank.ru/api/v1/chat/completions";


    public Message executeLLM(Message message) throws JsonProcessingException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + accessToken);
        Promt promt = enrichment();
        PromtMessage userMessage = new PromtMessage("user", message.getContent());
        promt.getMessages().add(userMessage);
        // Преобразуем в JSON
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(promt);
        HttpEntity<String> entity = new HttpEntity<>(json, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);


        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(response.getBody());
        String messageContent = rootNode
                .path("choices")
                .path(0)
                .path("message")
                .path("content")
                .asText();
        System.out.println(messageContent);
        return new Message("Giga", messageContent, LocalDateTime.now().toString());
    }

    public Promt enrichment() {
        Promt promt = new Promt();
        PromtMessage promtMessage = new PromtMessage("system", "Ты - персонаж технической поддержки. Твоя задача — помогать пользователям решать технические проблемы.");
        promt.getMessages().add(promtMessage);
        return promt;
    }

    public void sending() {

    }
}
