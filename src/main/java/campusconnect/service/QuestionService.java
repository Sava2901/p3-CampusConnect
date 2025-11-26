package campusconnect.service;

import campusconnect.db.DataStore;
import campusconnect.db.JsonDatabase;
import campusconnect.model.Answer;
import campusconnect.model.Question;
import campusconnect.model.Vote;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class QuestionService {
    private final JsonDatabase db;

    public QuestionService(JsonDatabase db) {
        this.db = db;
    }

    public Question ask(long userId, long courseId, String title, String content, List<String> tags) {
        DataStore s = db.load();
        long id = db.nextQuestionId(s);
        Question q = new Question(id, courseId, userId, title, content, tags == null ? new ArrayList<>() : tags, Instant.now());
        s.getQuestions().add(q);
        db.save(s);
        return q;
    }

    public List<Question> listByCourse(long courseId) {
        DataStore s = db.load();
        return s.getQuestions().stream().filter(q -> q.getCourseId() == courseId).collect(Collectors.toList());
    }

    public Optional<Question> findById(long id) {
        DataStore s = db.load();
        return s.getQuestions().stream().filter(q -> q.getId() == id).findFirst();
    }

    public int score(long questionId) {
        DataStore s = db.load();
        return s.getVotes().stream().filter(v -> v.getQuestionId() == questionId).mapToInt(Vote::getValue).sum();
    }

    public Optional<Answer> officialAnswer(long questionId) {
        DataStore s = db.load();
        return s.getAnswers().stream().filter(a -> a.getQuestionId() == questionId && a.isOfficial()).findFirst();
    }
}
