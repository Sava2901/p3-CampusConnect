package campusconnect.model;

import java.util.Objects;

public class Vote {
    private long questionId;
    private long userId;
    private int value;

    public Vote() {}

    public Vote(long questionId, long userId, int value) {
        this.questionId = questionId;
        this.userId = userId;
        this.value = value;
    }

    public long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(long questionId) {
        this.questionId = questionId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vote)) return false;
        Vote vote = (Vote) o;
        return questionId == vote.questionId && userId == vote.userId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(questionId, userId);
    }
}
