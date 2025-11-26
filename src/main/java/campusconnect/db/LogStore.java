package campusconnect.db;

import campusconnect.model.AuditLog;
import java.util.ArrayList;
import java.util.List;

public class LogStore {
    private List<AuditLog> auditLogs = new ArrayList<>();

    public List<AuditLog> getAuditLogs() {
        return auditLogs;
    }

    public void setAuditLogs(List<AuditLog> auditLogs) {
        this.auditLogs = auditLogs;
    }
}
