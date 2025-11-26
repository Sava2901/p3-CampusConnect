package campusconnect.model;

import java.time.Instant;

public class AuditLog {
    private long id;
    private long actorUserId;
    private String action;
    private Instant timestamp;

    public AuditLog() {}

    public AuditLog(long id, long actorUserId, String action, Instant timestamp) {
        this.id = id;
        this.actorUserId = actorUserId;
        this.action = action;
        this.timestamp = timestamp;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getActorUserId() {
        return actorUserId;
    }

    public void setActorUserId(long actorUserId) {
        this.actorUserId = actorUserId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
