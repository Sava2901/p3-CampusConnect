package campusconnect.model;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

public class Question {
    private long id;
    private long courseId;
    private long authorId;
    private String title;
    private String content;
    private List<String> tags;
    private Instant createdAt;

    public Question() {}

    public Question(long id, long courseId, long authorId, String title, String content, List<String> tags, Instant createdAt) {
        this.id = id;
        this.courseId = courseId;
        this.authorId = authorId;
        this.title = title;
        this.content = content;
        this.tags = tags;
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getCourseId() {
        return courseId;
    }

    public void setCourseId(long courseId) {
        this.courseId = courseId;
    }

    public long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(long authorId) {
        this.authorId = authorId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Question)) return false;
        Question question = (Question) o;
        return id == question.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
