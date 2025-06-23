package chat.llm.giga.model.store;

import chat.llm.giga.model.Message;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Component
/**
 * Как бы база данных
 */
public class GarbageRepository {

    private static final List<Message> GARBAGE_ENTITIES = new ArrayList<>();

    public Long countAll() {
        return (long) GARBAGE_ENTITIES.size();
    }

    public List<Message> findAll() {
        return GARBAGE_ENTITIES;
    }

    public Optional<Message> findBySender(String sender) {

        return GARBAGE_ENTITIES
                .stream()
                .filter(goodDto -> Objects.equals(goodDto.getSender(), sender))
                .findFirst();
    }

    public void add(Message entity) {
        GARBAGE_ENTITIES.add(entity);
    }
}
