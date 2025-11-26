package campusconnect.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class User {
    private long id;
    private String username;
    private Role role;

    public User() {}

    public User(long id, String username, Role role) {
        this.id = id;
        this.username = username;
        this.role = role;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    @JsonSetter("professor")
    public void setProfessor(boolean professor) {
        this.role = professor ? Role.PROFESSOR : Role.STUDENT;
    }

    public boolean isProfessor() {
        return role == Role.PROFESSOR;
    }

    public boolean isStudent() {
        return role == Role.STUDENT;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return id == user.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
