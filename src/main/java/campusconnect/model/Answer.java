package campusconnect.model;

import java.util.Objects;

public class Answer {
    private long id;
    private long questionId;
    private long authorId;
    private String content;
    private boolean official;

    public Answer() {}

    public Answer(long id, long questionId, long authorId, String content, boolean official) {
        this.id = id;
        this.questionId = questionId;
        this.authorId = authorId;
        this.content = content;
        this.official = official;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(long questionId) {
        this.questionId = questionId;
    }

    public long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(long authorId) {
        this.authorId = authorId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isOfficial() {
        return official;
    }

    public void setOfficial(boolean official) {
        this.official = official;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Answer)) return false;
        Answer answer = (Answer) o;
        return id == answer.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
