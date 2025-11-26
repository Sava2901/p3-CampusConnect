package campusconnect.db;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Optional;

public class JsonLogDatabase {
    private final Path path;
    private final ObjectMapper mapper;

    public JsonLogDatabase(Path path) {
        this.path = path;
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
        this.mapper.findAndRegisterModules();
        this.mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public LogStore load() {
        try {
            if (!Files.exists(path)) {
                Files.createDirectories(path.getParent());
                save(new LogStore());
            }
            byte[] bytes = Files.readAllBytes(path);
            if (bytes.length == 0) return new LogStore();
            return mapper.readValue(bytes, LogStore.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void save(LogStore store) {
        try {
            byte[] bytes = mapper.writeValueAsBytes(store);
            Files.write(path, bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public long nextAuditLogId(LogStore s) {
        Optional<Long> max = s.getAuditLogs().stream().map(l -> l.getId()).max(Comparator.naturalOrder());
        return max.orElse(0L) + 1;
    }
}
