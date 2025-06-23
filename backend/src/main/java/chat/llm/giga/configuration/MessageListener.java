package chat.llm.giga.configuration;


import chat.llm.giga.model.Message;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class MessageListener {
    final SimpMessagingTemplate template;

    public MessageListener(SimpMessagingTemplate template) {
        this.template = template;
    }


    public void listen(Message message) {
        System.out.println("Отправка сообщений всем пользователям");
        template.convertAndSend("/topic/group", message);
    }
}