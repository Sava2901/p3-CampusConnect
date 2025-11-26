package campusconnect.service;

import campusconnect.db.DataStore;
import campusconnect.db.JsonDatabase;
import campusconnect.model.Question;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class SearchService {
    private final JsonDatabase db;

    public SearchService(JsonDatabase db) {
        this.db = db;
    }

    public List<Question> search(String query) {
        String q = query == null ? "" : query.toLowerCase(Locale.ROOT);
        DataStore s = db.load();
        return s.getQuestions().stream().filter(x -> x.getTitle().toLowerCase(Locale.ROOT).contains(q) || x.getContent().toLowerCase(Locale.ROOT).contains(q)).collect(Collectors.toList());
    }
}
