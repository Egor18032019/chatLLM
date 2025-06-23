package chat.llm.giga.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Promt {
    private String model = "GigaChat";
    private List<PromtMessage> messages = new ArrayList<>();
    private Double temperature = 0.7;
    private Integer max_tokens = 512;
}
/*
    String promt = """
            {
              "model": "GigaChat",
              "messages": [
                {
                  "role": "system",
                  "content": "Ты - персонаж технической поддержки. Твоя задача — помогать пользователям решать технические проблемы."
                },
                {
                  "role": "user",
                  "content": "Как мне поменять пароль от почты?"
                }
              ],
              "temperature": 0.7,
              "max_tokens": 512
            }
            """;
 */