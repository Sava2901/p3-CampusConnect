package campusconnect.service;

import campusconnect.db.DataStore;
import campusconnect.db.JsonDatabase;
import campusconnect.interfaces.Notifier;
import campusconnect.model.Answer;
import campusconnect.model.Notification;
import campusconnect.model.Question;
import java.time.Instant;
import java.util.Optional;

public class AnswerService {
    private final JsonDatabase db;
    private final Notifier notifier;

    public AnswerService(JsonDatabase db) {
        this(db, null);
    }

    public AnswerService(JsonDatabase db, Notifier notifier) {
        this.db = db;
        this.notifier = notifier;
    }

    public Answer answer(long authorId, long questionId, String content, boolean official) {
        DataStore s = db.load();
        if (official) {
            boolean exists = s.getAnswers().stream().anyMatch(a -> a.getQuestionId() == questionId && a.isOfficial());
            if (exists) throw new IllegalStateException("official answer already exists");
        }
        long id = db.nextAnswerId(s);
        Answer a = new Answer(id, questionId, authorId, content, official);
        s.getAnswers().add(a);
        db.save(s);

        if (official && notifier != null) {
            Optional<Question> q = s.getQuestions().stream().filter(qq -> qq.getId() == questionId).findFirst();
            if (q.isPresent()) {
                Notification n = new Notification(0, q.get().getAuthorId(), "Your question received an official answer.", Instant.now(), false);
                notifier.notify(n);
            }
        }
        return a;
    }

    public Optional<Answer> findOfficial(long questionId) {
        DataStore s = db.load();
        return s.getAnswers().stream().filter(a -> a.getQuestionId() == questionId && a.isOfficial()).findFirst();
    }
}
