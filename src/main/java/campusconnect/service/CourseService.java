package campusconnect.service;

import campusconnect.db.DataStore;
import campusconnect.db.JsonDatabase;
import campusconnect.model.Course;
import campusconnect.model.Enrollment;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CourseService {
    private final JsonDatabase db;

    public CourseService(JsonDatabase db) {
        this.db = db;
    }

    public List<Course> listCourses() {
        return db.load().getCourses();
    }

    public Course createCourse(String name) {
        DataStore s = db.load();
        long id = db.nextCourseId(s);
        Course c = new Course(id, name);
        s.getCourses().add(c);
        db.save(s);
        return c;
    }

    public void enroll(long userId, long courseId) {
        DataStore s = db.load();
        boolean exists = s.getEnrollments().stream().anyMatch(e -> e.getUserId() == userId && e.getCourseId() == courseId);
        if (exists) return;
        s.getEnrollments().add(new Enrollment(userId, courseId));
        db.save(s);
    }

    public List<Course> listUserCourses(long userId) {
        DataStore s = db.load();
        List<Long> ids = s.getEnrollments().stream().filter(e -> e.getUserId() == userId).map(Enrollment::getCourseId).collect(Collectors.toList());
        return s.getCourses().stream().filter(c -> ids.contains(c.getId())).collect(Collectors.toList());
    }

    public Optional<Course> findById(long id) {
        DataStore s = db.load();
        return s.getCourses().stream().filter(c -> c.getId() == id).findFirst();
    }
}
