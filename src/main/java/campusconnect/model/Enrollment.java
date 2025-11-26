package campusconnect.model;

import java.util.Objects;

public class Enrollment {
    private long userId;
    private long courseId;

    public Enrollment() {}

    public Enrollment(long userId, long courseId) {
        this.userId = userId;
        this.courseId = courseId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getCourseId() {
        return courseId;
    }

    public void setCourseId(long courseId) {
        this.courseId = courseId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Enrollment)) return false;
        Enrollment that = (Enrollment) o;
        return userId == that.userId && courseId == that.courseId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, courseId);
    }
}
