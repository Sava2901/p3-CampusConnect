package campusconnect.db;

import campusconnect.interfaces.Repository;
import campusconnect.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class JsonDatabase implements Repository {
    private final Path path;
    private final ObjectMapper mapper;

    public JsonDatabase(Path path) {
        this.path = path;
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
        this.mapper.findAndRegisterModules();
        this.mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Override
    public DataStore load() {
        try {
            if (!Files.exists(path)) {
                Files.createDirectories(path.getParent());
                save(new DataStore());
            }
            byte[] bytes = Files.readAllBytes(path);
            if (bytes.length == 0) return new DataStore();
            return mapper.readValue(bytes, DataStore.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void save(DataStore store) {
        try {
            byte[] bytes = mapper.writeValueAsBytes(store);
            Files.write(path, bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public long nextId(List<?> list, java.util.function.Function<Object, Long> idGetter) {
        Optional<Long> max = list.stream().map(idGetter).max(Comparator.naturalOrder());
        return max.orElse(0L) + 1;
    }

    public long nextUserId(DataStore s) {
        return nextId(s.getUsers(), o -> ((User) o).getId());
    }

    public long nextCourseId(DataStore s) {
        return nextId(s.getCourses(), o -> ((Course) o).getId());
    }

    public long nextQuestionId(DataStore s) {
        return nextId(s.getQuestions(), o -> ((Question) o).getId());
    }

    public long nextAnswerId(DataStore s) {
        return nextId(s.getAnswers(), o -> ((Answer) o).getId());
    }

    public long nextNotificationId(DataStore s) {
        return nextId(s.getNotifications(), o -> ((Notification) o).getId());
    }

    public long nextAuditLogId(DataStore s) {
        return nextId(s.getAuditLogs(), o -> ((AuditLog) o).getId());
    }
}
