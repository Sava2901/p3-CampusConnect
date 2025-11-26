package org.example;

import campusconnect.CampusConnectApp;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) {
        // Use command line argument for database path, or default to campusconnect.json
        Path dbPath;
        if (args.length > 0) {
            dbPath = Path.of(args[0]);
        } else {
            dbPath = Path.of("data", "campusconnect.json");
        }
        CampusConnectApp app = new CampusConnectApp(dbPath);
        app.run();
    }
}
