package campusconnect.service;

import campusconnect.db.JsonLogDatabase;
import campusconnect.db.LogStore;
import campusconnect.model.AuditLog;
import java.time.Instant;
import java.util.List;

public class AuditService {
    private final JsonLogDatabase logDb;

    public AuditService(JsonLogDatabase logDb) {
        this.logDb = logDb;
    }

    public void record(long actorUserId, String action) {
        LogStore s = logDb.load();
        long id = logDb.nextAuditLogId(s);
        AuditLog log = new AuditLog(id, actorUserId, action, Instant.now());
        s.getAuditLogs().add(log);
        logDb.save(s);
    }

    public List<AuditLog> getAllLogs() {
        LogStore s = logDb.load();
        return s.getAuditLogs();
    }
}
