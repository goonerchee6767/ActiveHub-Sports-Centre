import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Scanner;


public class InputHelper {

    public static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("H:mm").withResolverStyle(ResolverStyle.STRICT);

    private final Scanner scanner;

    public InputHelper(Scanner scanner) {
        this.scanner = scanner;
    }

    
    public String readLine(String prompt) {
        System.out.print(prompt);
        if (!scanner.hasNextLine()) {
            System.out.println();
            System.out.println("Input ended. Exiting ActiveHub System.");
            System.exit(0);
        }
        return scanner.nextLine().trim();
    }

   
    public String readText(String prompt) {
        while (true) {
            String value = readLine(prompt).replace("|", "/");
            if (!value.isEmpty()) {
                return value;
            }
            System.out.println("  >> This field cannot be empty.");
        }
    }

    public String readName(String prompt) {
        while (true) {
            String name = readText(prompt);
            if (name.matches("[A-Za-z][A-Za-z .'/@-]{0,49}")) {
                return name;
            }
            System.out.println("  >> Name must start with a letter and contain letters only (max 50 characters).");
        }
    }

    
    public String readContactNumber(String prompt) {
        while (true) {
            String raw = readLine(prompt);
            String cleaned = raw.replaceAll("[\\s-]", "");
            if (cleaned.matches("\\+?\\d{9,12}")) {
                return cleaned;
            }
            System.out.println("  >> Invalid contact number. Example: 012-3456789");
        }
    }

    public int readInt(String prompt, int min, int max) {
        while (true) {
            String text = readLine(prompt);
            try {
                int value = Integer.parseInt(text);
                if (value >= min && value <= max) {
                    return value;
                }
                System.out.println("  >> Please enter a number between " + min + " and " + max + ".");
            } catch (NumberFormatException e) {
                System.out.println("  >> Invalid input. Please enter a whole number.");
            }
        }
    }

    public double readDouble(String prompt, double min, double max) {
        while (true) {
            String text = readLine(prompt);
            try {
                double value = Double.parseDouble(text);
                if (value >= min && value <= max) {
                    return value;
                }
                System.out.printf("  >> Please enter an amount between %.2f and %.2f.%n", min, max);
            } catch (NumberFormatException e) {
                System.out.println("  >> Invalid input. Please enter a number (e.g. 50.00).");
            }
        }
    }

   
    public LocalDate readDate(String prompt, boolean allowBlankForToday) {
        while (true) {
            String text = readLine(prompt);
            if (text.isEmpty() && allowBlankForToday) {
                return LocalDate.now();
            }
            try {
                return LocalDate.parse(text, DATE_FORMAT);
            } catch (DateTimeParseException e) {
                System.out.println("  >> Invalid date. Use yyyy-MM-dd, e.g. " + LocalDate.now().plusDays(1));
            }
        }
    }

  
    public LocalTime readTime(String prompt, LocalTime defaultValue) {
        while (true) {
            String text = readLine(prompt);
            if (text.isEmpty() && defaultValue != null) {
                return defaultValue;
            }
            try {
                return LocalTime.parse(text, TIME_FORMAT);
            } catch (DateTimeParseException e) {
                System.out.println("  >> Invalid time. Use 24-hour HH:mm, e.g. 09:00 or 15:30");
            }
        }
    }

    public boolean confirm(String prompt) {
        while (true) {
            String text = readLine(prompt).toUpperCase();
            if (text.equals("Y") || text.equals("YES")) {
                return true;
            }
            if (text.equals("N") || text.equals("NO")) {
                return false;
            }
            System.out.println("  >> Please enter Y or N.");
        }
    }

    public void pause() {
        readLine("\nPress Enter to continue...");
    }

    // ------------------------------------------------------------------ static display helpers

    public static String repeat(char c, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(c);
        }
        return sb.toString();
    }

    public static void printHeader(String title) {
        System.out.println();
        System.out.println(repeat('=', 74));
        System.out.println("  " + title);
        System.out.println(repeat('=', 74));
    }
}
