package campusconnect;

import campusconnect.db.JsonDatabase;
import campusconnect.model.*;
import campusconnect.service.*;
import campusconnect.ui.Menu;
import java.nio.file.Path;
import java.util.*;

public class CampusConnectApp {
    private final JsonDatabase db;
    private final AuthService authService;
    private final CourseService courseService;
    private final QuestionService questionService;
    private final AnswerService answerService;
    private final VoteService voteService;
    private final SearchService searchService;
    private final NotificationService notificationService;
    private final AuditService auditService;
    private final Scanner scanner;
    private User currentUser;

    public CampusConnectApp(Path dbPath) {
        this.db = new JsonDatabase(dbPath);
        this.authService = new AuthService(db);
        this.courseService = new CourseService(db);
        this.questionService = new QuestionService(db);
        this.notificationService = new NotificationService(db);
        this.answerService = new AnswerService(db, notificationService);
        this.voteService = new VoteService(db);
        this.searchService = new SearchService(db);
        Path logPath = dbPath.getParent() == null ? Path.of("data", "campusconnect-logs.json") : dbPath.getParent().resolve("campusconnect-logs.json");
        this.auditService = new AuditService(new campusconnect.db.JsonLogDatabase(logPath));
        this.scanner = new Scanner(System.in);
    }

    public void run() {
        // Clear screen at application start
        clearScreen();
        
        while (true) {
            if (currentUser == null) {
                int sel = Menu.choose("CampusConnect", java.util.List.of("Login", "Register", "Exit"));
                if (sel == 0) handleLogin();
                else if (sel == 1) handleRegister();
                else if (sel == 2) break;
            } else {
                if (currentUser.isProfessor()) {
                    showProfessorMenu();
                } else {
                    showStudentMenu();
                }
            }
        }
    }

    private void showStudentMenu() {
        java.util.List<String> opts = new java.util.ArrayList<>();
        opts.add("Browse all courses");
        opts.add("My enrolled courses");
        opts.add("Enroll in a course");
        opts.add("View course questions");
        opts.add("Ask a question");
        opts.add("Search questions");
        opts.add("My notifications");
        opts.add("Logout");
        opts.add("Exit");
        
        int sel = Menu.choose("Student: " + currentUser.getUsername(), opts);
        
        switch (sel) {
            case 0 -> uiListCourses();
            case 1 -> uiListUserCourses();
            case 2 -> uiEnroll();
            case 3 -> uiListCourseQuestions();
            case 4 -> uiAskQuestion();
            case 5 -> uiSearchQuestions();
            case 6 -> uiNotifications();
            case 7 -> currentUser = null;
            case 8 -> System.exit(0);
        }
    }

    private void showProfessorMenu() {
        java.util.List<String> opts = new java.util.ArrayList<>();
        opts.add("Browse all courses");
        opts.add("View course questions");
        opts.add("Search questions");
        opts.add("My notifications");
        opts.add("Logout");
        opts.add("Exit");
        
        int sel = Menu.choose("Professor: " + currentUser.getUsername(), opts);
        
        switch (sel) {
            case 0 -> uiListCourses();
            case 1 -> uiListCourseQuestions();
            case 2 -> uiSearchQuestions();
            case 3 -> uiNotifications();
            case 4 -> currentUser = null;
            case 5 -> System.exit(0);
        }
    }

    private void handleLogin() {
        clearScreen();
        String username = promptNonEmpty("Username");
        Optional<User> u = authService.login(username);
        if (u.isPresent()) {
            currentUser = u.get();
            auditService.record(currentUser.getId(), "login");
        } else {
            System.out.println("User not found");
            waitForBack();
        }
    }

    private void handleRegister() {
        clearScreen();
        String username = promptNonEmpty("New username");
        Role role = promptRoleInteractive();
        try {
            currentUser = authService.register(username, role);
            auditService.record(currentUser.getId(), "register");
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            waitForBack();
        }
    }

    private void uiListCourses() {
        clearScreen();
        List<Course> courses = courseService.listCourses();
        
        if (courses.isEmpty()) {
            System.out.println("No courses available");
        } else {
            List<Long> enrolledIds = courseService.listUserCourses(currentUser.getId())
                .stream().map(Course::getId).toList();
            
            System.out.println("All Courses:");
            System.out.println("=".repeat(50));
            for (Course c : courses) {
                String enrolled = enrolledIds.contains(c.getId()) ? " [ENROLLED]" : "";
                System.out.println(c.getId() + ": " + c.getName() + enrolled);
            }
        }
        waitForBack();
    }

    private void uiEnroll() {
        // Access control: Only students can enroll in courses
        if (currentUser.isProfessor()) {
            clearScreen();
            System.out.println("Professors don't need to enroll in courses");
            System.out.println("You have access to all courses by default");
            waitForBack();
            return;
        }
        
        List<Course> courses = courseService.listCourses();
        if (courses.isEmpty()) {
            clearScreen();
            System.out.println("No courses available");
            waitForBack();
            return;
        }
        
        // Filter out courses already enrolled in
        List<Long> enrolledIds = courseService.listUserCourses(currentUser.getId())
            .stream().map(Course::getId).toList();
        List<Course> availableCourses = courses.stream()
            .filter(c -> !enrolledIds.contains(c.getId()))
            .toList();
        
        if (availableCourses.isEmpty()) {
            clearScreen();
            System.out.println("You are already enrolled in all available courses");
            waitForBack();
            return;
        }
        
        List<String> options = new ArrayList<>();
        for (Course c : availableCourses) {
            options.add(c.getName());
        }
        options.add("Cancel");
        
        int sel = Menu.choose("Select a course to enroll in:", options);
        if (sel == options.size() - 1) return; // Cancel
        
        Course selected = availableCourses.get(sel);
        courseService.enroll(currentUser.getId(), selected.getId());
        auditService.record(currentUser.getId(), "enroll:" + selected.getId());
        clearScreen();
        System.out.println("Successfully enrolled in: " + selected.getName());
        waitForBack();
    }

    private void uiListUserCourses() {
        clearScreen();
        List<Course> list = courseService.listUserCourses(currentUser.getId());
        
        if (list.isEmpty()) {
            System.out.println("You are not enrolled in any courses yet");
        } else {
            System.out.println("My Enrolled Courses:");
            System.out.println("=".repeat(50));
            for (Course c : list) {
                System.out.println(c.getId() + ": " + c.getName());
            }
        }
        waitForBack();
    }

    private void uiListCourseQuestions() {
        List<Course> availableCourses;
        String title;
        
        if (currentUser.isProfessor()) {
            // Professors can view questions from all courses
            availableCourses = courseService.listCourses();
            title = "Select a course to view questions:";
        } else {
            // Students can only view questions from enrolled courses
            availableCourses = courseService.listUserCourses(currentUser.getId());
            title = "Select an enrolled course to view questions:";
        }
        
        if (availableCourses.isEmpty()) {
            clearScreen();
            if (currentUser.isProfessor()) {
                System.out.println("No courses available");
            } else {
                System.out.println("You are not enrolled in any courses");
            }
            waitForBack();
            return;
        }
        
        List<String> options = new ArrayList<>();
        for (Course c : availableCourses) {
            options.add(c.getName());
        }
        options.add("Cancel");
        
        int sel = Menu.choose(title, options);
        if (sel == options.size() - 1) return; // Cancel
        
        Course selected = availableCourses.get(sel);
        List<Question> qs = questionService.listByCourse(selected.getId());
        
        if (qs.isEmpty()) {
            clearScreen();
            System.out.println("Questions in " + selected.getName() + ":");
            System.out.println("=".repeat(50));
            System.out.println("No questions yet");
            waitForBack();
            return;
        }
        
        // Interactive question selection
        while (true) {
            List<String> questionOptions = new ArrayList<>();
            for (Question q : qs) {
                int score = questionService.score(q.getId());
                questionOptions.add(q.getTitle() + " [score: " + score + "]");
            }
            questionOptions.add("Back");
            
            int qSel = Menu.choose("Questions in " + selected.getName(), questionOptions);
            if (qSel == questionOptions.size() - 1) break; // Back
            
            // Show question detail page (read-only mode - just view with back option)
            showQuestionDetailReadOnly(qs.get(qSel));
        }
    }

    private void showQuestionDetailReadOnly(Question question) {
        clearScreen();
        
        int score = questionService.score(question.getId());
        
        // Get question author
        User questionAuthor = db.load().getUsers().stream()
            .filter(u -> u.getId() == question.getAuthorId())
            .findFirst()
            .orElse(null);
        String authorName = questionAuthor != null ? questionAuthor.getUsername() : "Unknown User";
        String authorRole = questionAuthor != null ? questionAuthor.getRole().toString() : "";
        
        // Blog-style header
        System.out.println();
        System.out.println("+" + "=".repeat(68) + "+");
        System.out.println("|" + centerText(question.getTitle(), 68) + "|");
        System.out.println("+" + "=".repeat(68) + "+");
        System.out.println();
        System.out.println("Posted by: " + authorName + " (" + authorRole + ") | Score: " + score);
        if (!question.getTags().isEmpty()) {
            System.out.println("Tags: " + String.join(", ", question.getTags()));
        }
        System.out.println();
        System.out.println("-".repeat(70));
        
        // Question content with word wrap
        wrapAndPrintText(question.getContent(), 70);
        
        System.out.println("-".repeat(70));
        System.out.println();
        
        // Get all answers
        List<Answer> allAnswers = db.load().getAnswers().stream()
            .filter(a -> a.getQuestionId() == question.getId())
            .toList();
        
        // Separate official and non-official answers
        Optional<Answer> officialAnswer = allAnswers.stream()
            .filter(Answer::isOfficial)
            .findFirst();
        
        List<Answer> otherAnswers = allAnswers.stream()
            .filter(a -> !a.isOfficial())
            .toList();
        
        // Display official answer first if exists
        if (officialAnswer.isPresent()) {
            Answer official = officialAnswer.get();
            User answerAuthor = db.load().getUsers().stream()
                .filter(u -> u.getId() == official.getAuthorId())
                .findFirst()
                .orElse(null);
            String answerAuthorName = answerAuthor != null ? answerAuthor.getUsername() : "Unknown";
            
            System.out.println("+" + "-".repeat(68) + "+");
            System.out.println("| >>> OFFICIAL ANSWER by " + answerAuthorName + 
                " ".repeat(Math.max(1, 68 - 28 - answerAuthorName.length())) + "|");
            System.out.println("+" + "-".repeat(68) + "+");
            System.out.println();
            wrapAndPrintText(official.getContent(), 70);
            System.out.println();
            System.out.println("=".repeat(70));
            System.out.println();
        }
        
        // Display other answers
        if (!otherAnswers.isEmpty()) {
            System.out.println("Other Responses (" + otherAnswers.size() + "):");
            System.out.println();
            
            for (int i = 0; i < otherAnswers.size(); i++) {
                Answer answer = otherAnswers.get(i);
                User answerAuthor = db.load().getUsers().stream()
                    .filter(u -> u.getId() == answer.getAuthorId())
                    .findFirst()
                    .orElse(null);
                String answerAuthorName = answerAuthor != null ? answerAuthor.getUsername() : "Unknown";
                String answerAuthorRole = answerAuthor != null ? answerAuthor.getRole().toString() : "";
                
                System.out.println("Response #" + (i + 1) + " by " + answerAuthorName + " (" + answerAuthorRole + "):");
                System.out.println("-".repeat(70));
                wrapAndPrintText(answer.getContent(), 70);
                System.out.println("-".repeat(70));
                System.out.println();
            }
        } else if (!officialAnswer.isPresent()) {
            System.out.println("No answers yet. Be the first to respond!");
            System.out.println();
        }
        
        // Build the menu title with the entire question page content
        StringBuilder menuHeader = new StringBuilder();
        menuHeader.append("\n");
        menuHeader.append("+" + "=".repeat(68) + "+\n");
        menuHeader.append("|" + centerText(question.getTitle(), 68) + "|\n");
        menuHeader.append("+" + "=".repeat(68) + "+\n");
        menuHeader.append("\n");
        menuHeader.append("Posted by: " + authorName + " (" + authorRole + ") | Score: " + score + "\n");
        if (!question.getTags().isEmpty()) {
            menuHeader.append("Tags: " + String.join(", ", question.getTags()) + "\n");
        }
        menuHeader.append("\n");
        menuHeader.append("-".repeat(70) + "\n");
        menuHeader.append(question.getContent() + "\n");
        menuHeader.append("-".repeat(70) + "\n");
        menuHeader.append("\n");
        
        // Add official answer if exists
        if (officialAnswer.isPresent()) {
            Answer official = officialAnswer.get();
            User answerAuthor2 = db.load().getUsers().stream()
                .filter(u -> u.getId() == official.getAuthorId())
                .findFirst()
                .orElse(null);
            String answerAuthorName2 = answerAuthor2 != null ? answerAuthor2.getUsername() : "Unknown";
            
            menuHeader.append("+" + "-".repeat(68) + "+\n");
            menuHeader.append("| >>> OFFICIAL ANSWER by " + answerAuthorName2 + 
                " ".repeat(Math.max(1, 68 - 28 - answerAuthorName2.length())) + "|\n");
            menuHeader.append("+" + "-".repeat(68) + "+\n");
            menuHeader.append("\n");
            menuHeader.append(official.getContent() + "\n");
            menuHeader.append("\n");
            menuHeader.append("=".repeat(70) + "\n");
            menuHeader.append("\n");
        }
        
        // Add other answers
        if (!otherAnswers.isEmpty()) {
            menuHeader.append("Other Responses (" + otherAnswers.size() + "):\n");
            menuHeader.append("\n");
            
            for (int i = 0; i < otherAnswers.size(); i++) {
                Answer answer = otherAnswers.get(i);
                User answerAuthor3 = db.load().getUsers().stream()
                    .filter(u -> u.getId() == answer.getAuthorId())
                    .findFirst()
                    .orElse(null);
                String answerAuthorName3 = answerAuthor3 != null ? answerAuthor3.getUsername() : "Unknown";
                String answerAuthorRole3 = answerAuthor3 != null ? answerAuthor3.getRole().toString() : "";
                
                menuHeader.append("Response #" + (i + 1) + " by " + answerAuthorName3 + " (" + answerAuthorRole3 + "):\n");
                menuHeader.append("-".repeat(70) + "\n");
                menuHeader.append(answer.getContent() + "\n");
                menuHeader.append("-".repeat(70) + "\n");
                menuHeader.append("\n");
            }
        } else if (!officialAnswer.isPresent()) {
            menuHeader.append("No answers yet. Be the first to respond!\n");
            menuHeader.append("\n");
        }
        
        menuHeader.append("\nWhat would you like to do?");
        
        List<String> actions = new ArrayList<>();
        actions.add("Answer");
        actions.add("Vote");
        actions.add("Back");

        int action = Menu.choose(menuHeader.toString(), actions);
        
        if (action == 0) { // Answer
            if (currentUser.isProfessor()) {
                // Professors can provide official answers
                clearScreen();
                System.out.println("Answering: " + question.getTitle());
                System.out.println("=".repeat(70));
                String content = promptNonEmpty("Your official answer");
                try {
                    Answer a = answerService.answer(currentUser.getId(), question.getId(), content, true);
                    auditService.record(currentUser.getId(), "answer:" + a.getId());
                    clearScreen();
                    System.out.println("Official answer posted successfully!");
                    Menu.waitForKey("Press any key to continue...");
                } catch (IllegalStateException e) {
                    clearScreen();
                    System.out.println(e.getMessage());
                    Menu.waitForKey("Press any key to continue...");
                }
            } else {
                // Students can provide non-official answers
                clearScreen();
                System.out.println("Answering: " + question.getTitle());
                System.out.println("=".repeat(70));
                String content = promptNonEmpty("Your answer");
                try {
                    Answer a = answerService.answer(currentUser.getId(), question.getId(), content, false);
                    auditService.record(currentUser.getId(), "answer:" + a.getId());
                    clearScreen();
                    System.out.println("Answer posted successfully!");
                    Menu.waitForKey("Press any key to continue...");
                } catch (IllegalStateException e) {
                    clearScreen();
                    System.out.println(e.getMessage());
                    Menu.waitForKey("Press any key to continue...");
                }
            }
        } else if (action == 1) { // Vote
            List<String> voteOptions = new ArrayList<>();
            voteOptions.add("Upvote (+1)");
            voteOptions.add("Downvote (-1)");
            voteOptions.add("Cancel");
            
            int voteChoice = Menu.choose("How would you like to vote?", voteOptions);
            
            if (voteChoice == 0) { // Upvote
                try {
                    voteService.vote(currentUser.getId(), question.getId(), 1);
                    auditService.record(currentUser.getId(), "vote:" + question.getId() + ":1");
                    clearScreen();
                    System.out.println("Upvote recorded successfully!");
                    Menu.waitForKey("Press any key to continue...");
                } catch (IllegalArgumentException e) {
                    clearScreen();
                    System.out.println(e.getMessage());
                    Menu.waitForKey("Press any key to continue...");
                }
            } else if (voteChoice == 1) { // Downvote
                try {
                    voteService.vote(currentUser.getId(), question.getId(), -1);
                    auditService.record(currentUser.getId(), "vote:" + question.getId() + ":-1");
                    clearScreen();
                    System.out.println("Downvote recorded successfully!");
                    Menu.waitForKey("Press any key to continue...");
                } catch (IllegalArgumentException e) {
                    clearScreen();
                    System.out.println(e.getMessage());
                    Menu.waitForKey("Press any key to continue...");
                }
            }
            // If Cancel (choice == 2), do nothing and return
        }
        // If Back (action == 2), just return
    }

    private void showQuestionDetail(Question question) {
        clearScreen();
        
        int score = questionService.score(question.getId());
            
            // Get question author
            User questionAuthor = db.load().getUsers().stream()
                .filter(u -> u.getId() == question.getAuthorId())
                .findFirst()
                .orElse(null);
            String authorName = questionAuthor != null ? questionAuthor.getUsername() : "Unknown User";
            String authorRole = questionAuthor != null ? questionAuthor.getRole().toString() : "";
            
            // Blog-style header
            System.out.println();
            System.out.println("+" + "=".repeat(68) + "+");
            System.out.println("|" + centerText(question.getTitle(), 68) + "|");
            System.out.println("+" + "=".repeat(68) + "+");
            System.out.println();
            System.out.println("Posted by: " + authorName + " (" + authorRole + ") | Score: " + score);
            if (!question.getTags().isEmpty()) {
                System.out.println("Tags: " + String.join(", ", question.getTags()));
            }
            System.out.println();
            System.out.println("-".repeat(70));
            
            // Question content with word wrap
            wrapAndPrintText(question.getContent(), 70);
            
            System.out.println("-".repeat(70));
            System.out.println();
            
            // Get all answers
            List<Answer> allAnswers = db.load().getAnswers().stream()
                .filter(a -> a.getQuestionId() == question.getId())
                .toList();
            
            // Separate official and non-official answers
            Optional<Answer> officialAnswer = allAnswers.stream()
                .filter(Answer::isOfficial)
                .findFirst();
            
            List<Answer> otherAnswers = allAnswers.stream()
                .filter(a -> !a.isOfficial())
                .toList();
            
            // Display official answer first if exists
            if (officialAnswer.isPresent()) {
                Answer official = officialAnswer.get();
                User answerAuthor = db.load().getUsers().stream()
                    .filter(u -> u.getId() == official.getAuthorId())
                    .findFirst()
                    .orElse(null);
                String answerAuthorName = answerAuthor != null ? answerAuthor.getUsername() : "Unknown";
                
                System.out.println("+" + "-".repeat(68) + "+");
                System.out.println("| >>> OFFICIAL ANSWER by " + answerAuthorName + 
                    " ".repeat(Math.max(1, 68 - 28 - answerAuthorName.length())) + "|");
                System.out.println("+" + "-".repeat(68) + "+");
                System.out.println();
                wrapAndPrintText(official.getContent(), 70);
                System.out.println();
                System.out.println("=".repeat(70));
                System.out.println();
            }
            
            // Display other answers
            if (!otherAnswers.isEmpty()) {
                System.out.println("Other Responses (" + otherAnswers.size() + "):");
                System.out.println();
                
                for (int i = 0; i < otherAnswers.size(); i++) {
                    Answer answer = otherAnswers.get(i);
                    User answerAuthor = db.load().getUsers().stream()
                        .filter(u -> u.getId() == answer.getAuthorId())
                        .findFirst()
                        .orElse(null);
                    String answerAuthorName = answerAuthor != null ? answerAuthor.getUsername() : "Unknown";
                    String answerAuthorRole = answerAuthor != null ? answerAuthor.getRole().toString() : "";
                    
                    System.out.println("Response #" + (i + 1) + " by " + answerAuthorName + " (" + answerAuthorRole + "):");
                    System.out.println("-".repeat(70));
                    wrapAndPrintText(answer.getContent(), 70);
                    System.out.println("-".repeat(70));
                    System.out.println();
                }
            } else if (!officialAnswer.isPresent()) {
                System.out.println("No answers yet. Be the first to respond!");
                System.out.println();
            }
            
        // Action menu
        List<String> actions = new ArrayList<>();
        if (currentUser.isProfessor()) {
            actions.add("Answer (official)");
        }
        actions.add("Upvote (+1)");
        actions.add("Downvote (-1)");
        actions.add("Back");
        
        int action = Menu.choose("Actions", actions);
        
        int voteUpIndex = currentUser.isProfessor() ? 1 : 0;
        int voteDownIndex = currentUser.isProfessor() ? 2 : 1;
        int backIndex = currentUser.isProfessor() ? 3 : 2;
        
        if (currentUser.isProfessor() && action == 0) { // Answer (official)
            clearScreen();
            System.out.println("Answering: " + question.getTitle());
            System.out.println("=".repeat(70));
            String content = promptNonEmpty("Your answer");
            try {
                Answer a = answerService.answer(currentUser.getId(), question.getId(), content, true);
                auditService.record(currentUser.getId(), "answer:" + a.getId());
                clearScreen();
                System.out.println("Official answer posted successfully!");
                Menu.waitForKey("Press any key to continue...");
            } catch (IllegalStateException e) {
                clearScreen();
                System.out.println(e.getMessage());
                Menu.waitForKey("Press any key to continue...");
            }
        } else if (action == voteUpIndex) { // Upvote
            try {
                voteService.vote(currentUser.getId(), question.getId(), 1);
                auditService.record(currentUser.getId(), "vote:" + question.getId() + ":1");
                clearScreen();
                System.out.println("Upvote recorded successfully!");
                Menu.waitForKey("Press any key to continue...");
            } catch (IllegalArgumentException e) {
                clearScreen();
                System.out.println(e.getMessage());
                Menu.waitForKey("Press any key to continue...");
            }
        } else if (action == voteDownIndex) { // Downvote
            try {
                voteService.vote(currentUser.getId(), question.getId(), -1);
                auditService.record(currentUser.getId(), "vote:" + question.getId() + ":-1");
                clearScreen();
                System.out.println("Downvote recorded successfully!");
                Menu.waitForKey("Press any key to continue...");
            } catch (IllegalArgumentException e) {
                clearScreen();
                System.out.println(e.getMessage());
                Menu.waitForKey("Press any key to continue...");
            }
        }
        // If back (backIndex), just return
    }

    private void wrapAndPrintText(String text, int maxWidth) {
        String[] lines = text.split("\n");
        for (String line : lines) {
            if (line.length() <= maxWidth) {
                System.out.println(line);
            } else {
                String[] words = line.split(" ");
                StringBuilder currentLine = new StringBuilder();
                for (String word : words) {
                    if (currentLine.length() + word.length() + 1 > maxWidth) {
                        System.out.println(currentLine.toString());
                        currentLine = new StringBuilder(word);
                    } else {
                        if (currentLine.length() > 0) currentLine.append(" ");
                        currentLine.append(word);
                    }
                }
                if (currentLine.length() > 0) {
                    System.out.println(currentLine.toString());
                }
            }
        }
    }

    private String centerText(String text, int width) {
        if (text.length() >= width) {
            return text.substring(0, width);
        }
        int padding = (width - text.length()) / 2;
        int rightPadding = width - text.length() - padding;
        return " ".repeat(padding) + text + " ".repeat(rightPadding);
    }

    private void uiAskQuestion() {
        List<Course> enrolledCourses = courseService.listUserCourses(currentUser.getId());
        if (enrolledCourses.isEmpty()) {
            clearScreen();
            System.out.println("You must enroll in a course before asking a question");
            waitForBack();
            return;
        }
        
        List<String> options = new ArrayList<>();
        for (Course c : enrolledCourses) {
            options.add(c.getName());
        }
        options.add("Cancel");
        
        int sel = Menu.choose("Select a course for your question:", options);
        if (sel == options.size() - 1) return; // Cancel
        
        Course selected = enrolledCourses.get(sel);
        clearScreen();
        System.out.println("Asking question in: " + selected.getName());
        System.out.println("=".repeat(50));
        String title = promptNonEmpty("Question title");
        String content = promptNonEmpty("Question content");
        System.out.print("Tags (comma-separated, optional): ");
        String tagsStr = scanner.nextLine();
        List<String> tags = tagsStr.isBlank() ? new ArrayList<>() : Arrays.stream(tagsStr.split(",")).map(String::trim).filter(x -> !x.isEmpty()).toList();
        Question q = questionService.ask(currentUser.getId(), selected.getId(), title, content, tags);
        auditService.record(currentUser.getId(), "ask:" + q.getId());
        System.out.println("Question created successfully! (ID: " + q.getId() + ")");
        waitForBack();
    }

    private void uiVoteQuestion() {
        List<Course> availableCourses;
        String title;
        
        if (currentUser.isProfessor()) {
            // Professors can vote on questions from all courses
            availableCourses = courseService.listCourses();
            title = "Select a course:";
        } else {
            // Students can only vote on questions from enrolled courses
            availableCourses = courseService.listUserCourses(currentUser.getId());
            title = "Select an enrolled course:";
        }
        
        if (availableCourses.isEmpty()) {
            clearScreen();
            if (currentUser.isProfessor()) {
                System.out.println("No courses available");
            } else {
                System.out.println("You are not enrolled in any courses");
            }
            waitForBack();
            return;
        }
        
        // First select course
        List<String> courseOptions = new ArrayList<>();
        for (Course c : availableCourses) {
            courseOptions.add(c.getName());
        }
        courseOptions.add("Cancel");
        
        int courseSel = Menu.choose(title, courseOptions);
        if (courseSel == courseOptions.size() - 1) return; // Cancel
        
        Course selected = availableCourses.get(courseSel);
        List<Question> questions = questionService.listByCourse(selected.getId());
        
        if (questions.isEmpty()) {
            clearScreen();
            System.out.println("No questions in this course yet");
            waitForBack();
            return;
        }
        
        // Interactive question selection - show question detail when selected
        while (true) {
            List<String> questionOptions = new ArrayList<>();
            for (Question q : questions) {
                int score = questionService.score(q.getId());
                questionOptions.add(q.getTitle() + " [score: " + score + "]");
            }
            questionOptions.add("Back");
            
            int questionSel = Menu.choose("Select a question to vote on:", questionOptions);
            if (questionSel == questionOptions.size() - 1) break; // Back
            
            Question selectedQuestion = questions.get(questionSel);
            
            // Show question detail page where user can vote
            showQuestionDetail(selectedQuestion);
        }
    }

    private void uiSearchQuestions() {
        clearScreen();
        String query = promptNonEmpty("Search query");
        List<Question> results = searchService.search(query);
        
        if (results.isEmpty()) {
            clearScreen();
            System.out.println("No questions found matching: " + query);
            waitForBack();
            return;
        }
        
        // Interactive question selection from search results
        while (true) {
            List<String> questionOptions = new ArrayList<>();
            for (Question q : results) {
                int score = questionService.score(q.getId());
                questionOptions.add(q.getTitle() + " [score: " + score + "]");
            }
            questionOptions.add("Back");
            
            int qSel = Menu.choose("Search results for: \"" + query + "\"", questionOptions);
            if (qSel == questionOptions.size() - 1) break; // Back
            
            // Show question detail page (read-only mode)
            showQuestionDetailReadOnly(results.get(qSel));
        }
    }

    private void uiAnswerQuestionOfficial() {
        // Access control: Only professors can provide official answers
        if (!currentUser.isProfessor()) {
            clearScreen();
            System.out.println("Access denied: Only professors can provide official answers");
            waitForBack();
            return;
        }
        
        List<Course> allCourses = courseService.listCourses();
        if (allCourses.isEmpty()) {
            clearScreen();
            System.out.println("No courses available");
            waitForBack();
            return;
        }
        
        // First select course
        List<String> courseOptions = new ArrayList<>();
        for (Course c : allCourses) {
            courseOptions.add(c.getName());
        }
        courseOptions.add("Cancel");
        
        int courseSel = Menu.choose("Select a course:", courseOptions);
        if (courseSel == courseOptions.size() - 1) return; // Cancel
        
        Course selected = allCourses.get(courseSel);
        List<Question> questions = questionService.listByCourse(selected.getId());
        
        if (questions.isEmpty()) {
            clearScreen();
            System.out.println("No questions in this course yet");
            waitForBack();
            return;
        }
        
        // Then select question
        while (true) {
            List<String> questionOptions = new ArrayList<>();
            for (Question q : questions) {
                int score = questionService.score(q.getId());
                questionOptions.add(q.getTitle() + " [score: " + score + "]");
            }
            questionOptions.add("Back");
            
            int questionSel = Menu.choose("Select a question to answer:", questionOptions);
            if (questionSel == questionOptions.size() - 1) break; // Back
            
            Question selectedQuestion = questions.get(questionSel);
            
            // Show question detail page where professor can answer
            showQuestionDetail(selectedQuestion);
        }
    }

    private void uiNotifications() {
        clearScreen();
        List<Notification> list = notificationService.listByUser(currentUser.getId());
        
        if (list.isEmpty()) {
            System.out.println("No notifications");
            waitForBack();
            return;
        }
        
        System.out.println("Your Notifications:");
        System.out.println("=".repeat(50));
        for (Notification n : list) {
            String status = n.isRead() ? "READ" : "NEW";
            System.out.println("[" + status + "] " + n.getMessage());
        }
        notificationService.markAllRead(currentUser.getId());
        System.out.println();
        System.out.println("All notifications marked as read.");
        waitForBack();
    }

    private void uiViewAuditLogs() {
        if (!currentUser.isProfessor()) {
            clearScreen();
            System.out.println("Access denied: This feature is only available to professors");
            waitForBack();
            return;
        }
        
        clearScreen();
        System.out.println("Recent Audit Logs:");
        System.out.println("=".repeat(50));
        
        List<AuditLog> logs = auditService.getAllLogs();
        if (logs.isEmpty()) {
            System.out.println("No audit logs available");
        } else {
            // Show last 20 logs
            int count = Math.min(20, logs.size());
            for (int i = logs.size() - count; i < logs.size(); i++) {
                AuditLog log = logs.get(i);
                System.out.println("[" + log.getTimestamp() + "] User " + log.getActorUserId() + ": " + log.getAction());
            }
        }
        waitForBack();
    }

    private void clearScreen() {
        try {
            new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
        } catch (Exception e) {
            // Fallback to ANSI codes if cls fails
            System.out.print("\033[H\033[2J");
            System.out.flush();
        }
    }

    private void waitForBack() {
        Menu.waitForKey("Press any key to go back...");
    }

    private String promptNonEmpty(String label) {
        while (true) {
            System.out.print(label + ": ");
            String v = scanner.nextLine();
            if (v != null && !v.trim().isEmpty()) return v.trim();
            System.out.println("Please enter a non-empty value");
        }
    }

    private long promptLong(String label) {
        while (true) {
            System.out.print(label + ": ");
            String s = scanner.nextLine();
            try {
                return Long.parseLong(s.trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number");
            }
        }
    }

    private int promptVote() {
        while (true) {
            System.out.print("Vote (+1 or -1): ");
            String s = scanner.nextLine();
            try {
                int v = Integer.parseInt(s.trim());
                if (v == 1 || v == -1) return v;
            } catch (NumberFormatException ignored) {}
            System.out.println("Please enter +1 or -1");
        }
    }

    private Role promptRole() {
        while (true) {
            System.out.print("Role (student/professor): ");
            String r = scanner.nextLine();
            if ("student".equalsIgnoreCase(r)) return Role.STUDENT;
            if ("professor".equalsIgnoreCase(r)) return Role.PROFESSOR;
            System.out.println("Please enter 'student' or 'professor'");
        }
    }

    private Role promptRoleInteractive() {
        int sel = Menu.choose("Select your role:", List.of("Student", "Professor"));
        return (sel == 0) ? Role.STUDENT : Role.PROFESSOR;
    }
}
