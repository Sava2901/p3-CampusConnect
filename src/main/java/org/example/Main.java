package org.example;

import campusconnect.CampusConnectApp;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) {
        Path dbPath = Path.of("data", "campusconnect.json");
        CampusConnectApp app = new CampusConnectApp(dbPath);
        app.run();
    }
}
