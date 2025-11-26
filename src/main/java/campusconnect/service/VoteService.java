package campusconnect.service;

import campusconnect.db.DataStore;
import campusconnect.db.JsonDatabase;
import campusconnect.model.Vote;

public class VoteService {
    private final JsonDatabase db;

    public VoteService(JsonDatabase db) {
        this.db = db;
    }

    public void vote(long userId, long questionId, int value) {
        if (value != 1 && value != -1) throw new IllegalArgumentException("invalid vote value");
        DataStore s = db.load();
        boolean exists = s.getVotes().stream().anyMatch(v -> v.getUserId() == userId && v.getQuestionId() == questionId);
        if (exists) {
            s.getVotes().stream().filter(v -> v.getUserId() == userId && v.getQuestionId() == questionId).forEach(v -> v.setValue(value));
        } else {
            s.getVotes().add(new Vote(questionId, userId, value));
        }
        db.save(s);
    }
}
