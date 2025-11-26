package campusconnect.ui;

import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.InfoCmp;
import org.jline.utils.NonBlockingReader;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Menu {
    public static int choose(String title, List<String> options) {
        try {
            // Attempt to create a system terminal with better configuration for Windows
            TerminalBuilder builder = TerminalBuilder.builder()
                .system(true)
                .jna(true)
                .jansi(true);
            
            Terminal terminal = builder.build();
            terminal.enterRawMode();
            terminal.puts(InfoCmp.Capability.cursor_invisible);
            NonBlockingReader reader = terminal.reader();
            AtomicInteger idx = new AtomicInteger(0);
            AtomicBoolean done = new AtomicBoolean(false);
            AtomicBoolean quit = new AtomicBoolean(false);

            Thread renderer = new Thread(() -> {
                int prev = -1;
                while (!done.get() && !quit.get()) {
                    int current = idx.get();
                    if (current != prev) {
                        System.out.print("\033[H\033[2J");
                        System.out.flush();
                        System.out.println(title);
                        System.out.println("Use arrow keys/WASD to navigate, Enter to select");
                        System.out.println();
                        for (int i = 0; i < options.size(); i++) {
                            String arrow = (i == current) ? " > " : "   ";
                            System.out.println(arrow + (i + 1) + ". " + options.get(i));
                        }
                        prev = current;
                    }
                    try { Thread.sleep(16); } catch (InterruptedException ignored) {}
                }
            });
            renderer.start();

            while (!done.get() && !quit.get()) {
                int ch = reader.read();
                if (ch == -1) continue;
                
                // Handle ANSI escape sequences (Unix/Linux/Mac)
                if (ch == 27) {
                    int next = reader.read(10);
                    if (next == '[' || next == 'O') {
                        int dir = reader.read(10);
                        if (dir == 'A') idx.set((idx.get() + options.size() - 1) % options.size());
                        else if (dir == 'B') idx.set((idx.get() + 1) % options.size());
                    }
                }
                // Handle Windows arrow keys (extended ASCII)
                else if (ch == 224 || ch == 0) {
                    int code = reader.read(10);
                    if (code == 72) idx.set((idx.get() + options.size() - 1) % options.size()); // Up
                    else if (code == 80) idx.set((idx.get() + 1) % options.size()); // Down
                }
                // WASD/VI keys navigation
                else if (ch == 'w' || ch == 'W' || ch == 'k' || ch == 'K') {
                    idx.set((idx.get() + options.size() - 1) % options.size());
                } else if (ch == 's' || ch == 'S' || ch == 'j' || ch == 'J') {
                    idx.set((idx.get() + 1) % options.size());
                }
                // Enter key
                else if (ch == '\r' || ch == '\n' || ch == 10 || ch == 13) {
                    done.set(true);
                }
                // Number selection
                else if (ch >= '1' && ch <= '9') {
                    int n = ch - '0';
                    if (n >= 1 && n <= options.size()) {
                        idx.set(n - 1);
                        done.set(true);
                    }
                }
            }

            terminal.puts(InfoCmp.Capability.cursor_visible);
            terminal.close();
            renderer.join();
            return idx.get();
        } catch (Throwable e) {
            // Fallback to simple numbered menu if terminal initialization fails
            return fallback(title, options);
        }
    }

    private static int fallback(String title, List<String> options) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("\033[H\033[2J");
            System.out.flush();
            System.out.println(title);
            System.out.println("=".repeat(Math.max(30, title.length())));
            for (int i = 0; i < options.size(); i++) {
                System.out.println((i + 1) + ". " + options.get(i));
            }
            System.out.println();
            System.out.print("Enter choice (1-" + options.size() + "): ");
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) continue;
            try {
                int n = Integer.parseInt(input);
                if (n >= 1 && n <= options.size()) {
                    return n - 1;
                } else {
                    System.out.println("Invalid choice. Please enter a number between 1 and " + options.size());
                    System.out.println("Press Enter to continue...");
                    scanner.nextLine();
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
                System.out.println("Press Enter to continue...");
                scanner.nextLine();
            }
        }
    }

    /**
     * Wait for any key press without allowing text input
     */
    public static void waitForKey(String message) {
        try {
            TerminalBuilder builder = TerminalBuilder.builder()
                .system(true)
                .jna(true)
                .jansi(true);
            
            Terminal terminal = builder.build();
            terminal.enterRawMode();
            NonBlockingReader reader = terminal.reader();
            
            System.out.println();
            System.out.println(message);
            
            // Clear any buffered input first
            while (reader.peek(10) > 0) {
                reader.read(10);
            }
            
            // Wait for any key press
            reader.read();
            
            terminal.close();
        } catch (Throwable e) {
            // Fallback: use System.in directly to avoid showing text input
            try {
                System.out.println();
                System.out.println(message);
                // Read single byte from System.in - won't display typed characters
                System.in.read();
                // Clear the rest of the line (including newline)
                while (System.in.available() > 0) {
                    System.in.read();
                }
            } catch (Exception ex) {
                // If all else fails, use scanner
                Scanner scanner = new Scanner(System.in);
                scanner.nextLine();
            }
        }
    }
}
