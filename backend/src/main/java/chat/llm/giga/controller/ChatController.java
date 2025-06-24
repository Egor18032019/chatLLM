package chat.llm.giga.controller;

import chat.llm.giga.model.Message;
import chat.llm.giga.service.GigaChatService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

@RestController
@RequiredArgsConstructor
public class ChatController {
    private final SimpMessagingTemplate template;
    private final GigaChatService gigaChatService;

    @PostMapping(value = "/api/send", consumes = "application/json", produces = "application/json")
    public void sendMessage(@RequestBody Message message,
                            @RequestHeader Map<String, String> headers) throws JsonProcessingException {
//        System.out.println("headers = " + headers);
        message.setTimestamp(LocalDateTime.now().toString());
        template.convertAndSend("/topic/group", message);
        gigaChatService.executeLLM(message);


    }

    //    -------------- WebSocket API ----------------
    @MessageMapping("/sendMessage")
    @SendTo("/topic/group")
    public Message broadcastGroupMessage(@Payload Message message,
                                         SimpMessageHeaderAccessor headerAccessor) {

        // Получение всех заголовков
        Map<String, Object> allHeaders = headerAccessor.toMap();
        System.out.println(allHeaders);
        return message;
    }

    @MessageMapping("/newUser")
    @SendTo("/topic/group")
    public Message addUser(@Payload Message message,
                           SimpMessageHeaderAccessor headerAccessor) {
        // Add user in web socket session
        Objects.requireNonNull(headerAccessor.getSessionAttributes()).put("username", message.getSender());
        return message;
    }
}
