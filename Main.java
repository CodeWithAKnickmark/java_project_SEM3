import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * PS No. 72 - Email Inbox Manager
 * Console-based DSA project using Java standard library only.
 *
 * Data structures:
 * 1. PriorityQueue<Email> - priority first, then arrival time (oldest first)
 * 2. Stack<String>        - recently opened email IDs
 * 3. HashMap<String, Email> - O(1) average email-ID lookup
 * 4. ArrayList + Comparator - sorting by sender/date
 *
 * Data is loaded from inbox.txt at startup and saved on exit.
 */
public class Main {

    private static final String DATA_FILE = "inbox.txt";
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    enum Priority {
        HIGH(3), NORMAL(2), LOW(1);

        final int importance;

        Priority(int importance) {
            this.importance = importance;
        }

        static Priority fromString(String value) {
            return Priority.valueOf(value.trim().toUpperCase(Locale.ROOT));
        }

        @Override
        public String toString() {
            String name = name().toLowerCase(Locale.ROOT);
            return Character.toUpperCase(name.charAt(0)) + name.substring(1);
        }
    }

    static class Email {
        private final String id;
        private final String sender;
        private final String subject;
        private final String body;
        private final Priority priority;
        private final long arrivalTime;
        private final long arrivalOrder;
        private boolean read;

        Email(String id, String sender, String subject, String body,
              Priority priority, long arrivalTime, long arrivalOrder,
              boolean read) {
            this.id = id;
            this.sender = sender;
            this.subject = subject;
            this.body = body;
            this.priority = priority;
            this.arrivalTime = arrivalTime;
            this.arrivalOrder = arrivalOrder;
            this.read = read;
        }

        String getId() { return id; }
        String getSender() { return sender; }
        String getSubject() { return subject; }
        String getBody() { return body; }
        Priority getPriority() { return priority; }
        long getArrivalTime() { return arrivalTime; }
        long getArrivalOrder() { return arrivalOrder; }
        boolean isRead() { return read; }
        void markRead() { read = true; }

        String getFormattedDate() {
            return Instant.ofEpochMilli(arrivalTime)
                    .atZone(ZoneId.systemDefault())
                    .format(DATE_FORMAT);
        }

        void displayShort() {
            System.out.println(id + " | " + sender + " | " + priority
                    + " | " + (read ? "Read" : "Unread")
                    + " | " + subject);
        }

        void displayFull() {
            System.out.println("\n---------------- EMAIL ----------------");
            System.out.println("Email ID : " + id);
            System.out.println("From     : " + sender);
            System.out.println("Subject  : " + subject);
            System.out.println("Priority : " + priority);
            System.out.println("Received : " + getFormattedDate());
            System.out.println("Status   : " + (read ? "Read" : "Unread"));
            System.out.println("Message  : " + body);
            System.out.println("---------------------------------------");
        }
    }

    // Higher importance first. For equal priority, older arrival comes first.
    private static final Comparator<Email> PRIORITY_ORDER =
            Comparator.comparingInt((Email e) -> e.getPriority().importance)
                    .reversed()
                    .thenComparingLong(Email::getArrivalTime)
                    .thenComparingLong(Email::getArrivalOrder);

    private final PriorityQueue<Email> inboxQueue =
            new PriorityQueue<>(PRIORITY_ORDER);

    private final HashMap<String, Email> emailMap = new HashMap<>();

    // Stack stores IDs so that the most recently opened email is on top.
    private final Stack<String> recentlyOpened = new Stack<>();

    private long nextArrivalOrder = 1;
    private int nextEmailNumber = 101;

    public static void main(String[] args) {
        Main manager = new Main();
        manager.loadInbox();

        System.out.println("======================================");
        System.out.println("         EMAIL INBOX MANAGER");
        System.out.println("======================================");

        manager.runMenu();
    }

    private void runMenu() {
        Scanner input = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println("\n============== MAIN MENU ==============");
            System.out.println("1. Receive a new email");
            System.out.println("2. Open email by ID");
            System.out.println("3. Open next priority email");
            System.out.println("4. Open most recently received email");
            System.out.println("5. Search email by ID");
            System.out.println("6. Sort emails by sender");
            System.out.println("7. Sort emails by date");
            System.out.println("8. Mark email as read");
            System.out.println("9. Display inbox (priority order)");
            System.out.println("10. View recently opened emails");
            System.out.println("11. Save and exit");
            System.out.print("Enter your choice: ");

            String choice = input.nextLine().trim();

            switch (choice) {
                case "1":
                    receiveEmail(input);
                    break;
                case "2":
                    openEmailById(input);
                    break;
                case "3":
                    openNextPriorityEmail();
                    break;
                case "4":
                    openMostRecentlyReceivedEmail();
                    break;
                case "5":
                    searchEmail(input);
                    break;
                case "6":
                    sortBySender();
                    break;
                case "7":
                    sortByDate();
                    break;
                case "8":
                    markEmailAsRead(input);
                    break;
                case "9":
                    displayInbox();
                    break;
                case "10":
                    displayRecentlyOpened();
                    break;
                case "11":
                    saveInbox();
                    System.out.println("Inbox saved. Exiting Email Inbox Manager.");
                    running = false;
                    break;
                default:
                    System.out.println("Invalid menu choice. Enter a number from 1 to 11.");
            }
        }
        input.close();
    }

    // Requirement 1: Receive and store a new email.
    private void receiveEmail(Scanner input) {
        System.out.println("\n---------- RECEIVE EMAIL ----------");

        String sender = readRequiredField(input, "Enter sender: ");
        if (sender == null) return;

        String subject = readRequiredField(input, "Enter subject: ");
        if (subject == null) return;

        System.out.print("Enter message: ");
        String body = input.nextLine().trim();

        Priority priority;
        while (true) {
            System.out.print("Enter priority (High/Normal/Low): ");
            String value = input.nextLine().trim();

            try {
                priority = Priority.fromString(value);
                break;
            } catch (IllegalArgumentException ex) {
                System.out.println("Invalid priority. Choose High, Normal, or Low.");
            }
        }

        String id = generateEmailId();
        long arrivalTime = System.currentTimeMillis();
        long arrivalOrder = nextArrivalOrder++;

        Email email = new Email(id, sender, subject, body, priority,
                arrivalTime, arrivalOrder, false);

        emailMap.put(id, email);
        inboxQueue.offer(email);

        System.out.println("Email received: " + id + " | " + sender
                + " | " + priority);
    }

    private String readRequiredField(Scanner input, String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = input.nextLine().trim();

            if (!value.isEmpty()) {
                return value;
            }

            System.out.println("This field cannot be empty.");
            System.out.print("Try again? (Y/N): ");
            String answer = input.nextLine().trim();

            if (answer.equalsIgnoreCase("N")) {
                System.out.println("Email receiving cancelled.");
                return null;
            }
        }
    }

    private String generateEmailId() {
        while (emailMap.containsKey("EM" + nextEmailNumber)) {
            nextEmailNumber++;
        }
        return "EM" + nextEmailNumber++;
    }

    // Requirement 2: Open by ID using HashMap lookup.
    private void openEmailById(Scanner input) {
        System.out.print("Enter email ID to open: ");
        String id = input.nextLine().trim().toUpperCase(Locale.ROOT);

        Email email = emailMap.get(id);
        if (email == null) {
            System.out.println("Email " + id + " not found.");
            return;
        }

        email.markRead();
        recentlyOpened.push(email.getId());
        System.out.println("Opened: " + email.getId() + " | " + email.getSender());
        email.displayFull();
    }

    // Opens the highest-priority unread email. Equal priorities use arrival order.
    private void openNextPriorityEmail() {
        Email email = getNextUnreadEmail();

        if (email == null) {
            System.out.println("No unread emails available.");
            return;
        }

        email.markRead();
        recentlyOpened.push(email.getId());
        System.out.println("Next priority email:");
        System.out.println("Opened: " + email.getId() + " | " + email.getSender());
        email.displayFull();
    }

    // The newest received unread email, not the most recently opened email.
    private void openMostRecentlyReceivedEmail() {
        Email newest = null;

        for (Email email : emailMap.values()) {
            if (!email.isRead()
                    && (newest == null
                    || email.getArrivalOrder() > newest.getArrivalOrder())) {
                newest = email;
            }
        }

        if (newest == null) {
            System.out.println("No unread emails available.");
            return;
        }

        newest.markRead();
        recentlyOpened.push(newest.getId());
        System.out.println("Opened: " + newest.getId() + " | " + newest.getSender());
        newest.displayFull();
    }

    // Requirement 3: Search by email ID using HashMap.
    private void searchEmail(Scanner input) {
        System.out.print("Enter email ID to search: ");
        String id = input.nextLine().trim().toUpperCase(Locale.ROOT);

        Email email = emailMap.get(id);
        if (email == null) {
            System.out.println("Email " + id + " not found.");
        } else {
            System.out.println("Email found:");
            email.displayFull();
        }
    }

    // Requirement 4: Sort by sender using ArrayList and Comparator.
    private void sortBySender() {
        if (emailMap.isEmpty()) {
            System.out.println("Inbox is empty.");
            return;
        }

        ArrayList<Email> emails = getEmailsAsList();
        emails.sort(Comparator.comparing(
                Email::getSender, String.CASE_INSENSITIVE_ORDER));

        System.out.println("\n---------- EMAILS SORTED BY SENDER ----------");
        for (Email email : emails) {
            email.displayShort();
        }
    }

    // Requirement 5: Sort by arrival date/time (oldest first).
    private void sortByDate() {
        if (emailMap.isEmpty()) {
            System.out.println("Inbox is empty.");
            return;
        }

        ArrayList<Email> emails = getEmailsAsList();
        emails.sort(Comparator.comparingLong(Email::getArrivalTime)
                .thenComparingLong(Email::getArrivalOrder));

        System.out.println("\n---------- EMAILS SORTED BY DATE ----------");
        for (Email email : emails) {
            email.displayShort();
            System.out.println("   Received: " + email.getFormattedDate());
        }
    }

    private void markEmailAsRead(Scanner input) {
        System.out.print("Enter email ID to mark as read: ");
        String id = input.nextLine().trim().toUpperCase(Locale.ROOT);

        Email email = emailMap.get(id);
        if (email == null) {
            System.out.println("Email " + id + " not found.");
            return;
        }

        if (email.isRead()) {
            System.out.println("Email " + id + " is already marked as read.");
        } else {
            email.markRead();
            System.out.println("Email " + id + " marked as read.");
        }
    }

    // Displays all emails without changing the actual PriorityQueue.
    private void displayInbox() {
        if (inboxQueue.isEmpty()) {
            System.out.println("Inbox is empty.");
            return;
        }

        PriorityQueue<Email> copy = new PriorityQueue<>(inboxQueue);
        System.out.println("\n---------- INBOX (PRIORITY ORDER) ----------");

        while (!copy.isEmpty()) {
            copy.poll().displayShort();
        }
    }

    // Stack.peek() gives the most recently opened email.
    private void displayRecentlyOpened() {
        if (recentlyOpened.isEmpty()) {
            System.out.println("No emails have been opened yet.");
            return;
        }

        System.out.println("\n---------- RECENTLY OPENED EMAILS ----------");
        for (int i = recentlyOpened.size() - 1; i >= 0; i--) {
            String id = recentlyOpened.get(i);
            Email email = emailMap.get(id);

            if (email != null) {
                System.out.println(email.getId() + " | " + email.getSender()
                        + " | " + email.getSubject());
            }
        }
    }

    // Finds the first unread email in priority order without modifying the queue.
    private Email getNextUnreadEmail() {
        PriorityQueue<Email> copy = new PriorityQueue<>(inboxQueue);

        while (!copy.isEmpty()) {
            Email email = copy.poll();
            if (!email.isRead()) {
                return email;
            }
        }
        return null;
    }

    private ArrayList<Email> getEmailsAsList() {
        return new ArrayList<>(emailMap.values());
    }

    // Load all saved emails when the program starts.
    private void loadInbox() {
        File file = new File(DATA_FILE);

        if (!file.exists()) {
            System.out.println("No existing inbox file. Starting with an empty inbox.");
            return;
        }

        int loaded = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file),
                        StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty() || line.startsWith("#")) {
                    continue;
                }

                try {
                    String[] parts = line.split("\\|", -1);
                    if (parts.length != 8) {
                        System.out.println("Skipped invalid data row in inbox.txt.");
                        continue;
                    }

                    String id = parts[0];
                    String sender = decode(parts[1]);
                    String subject = decode(parts[2]);
                    Priority priority = Priority.fromString(parts[3]);
                    long arrivalTime = Long.parseLong(parts[4]);
                    long arrivalOrder = Long.parseLong(parts[5]);
                    boolean read = Boolean.parseBoolean(parts[6]);
                    String body = decode(parts[7]);

                    if (id.isEmpty() || sender.trim().isEmpty()
                            || subject.trim().isEmpty()) {
                        System.out.println("Skipped email with missing required fields.");
                        continue;
                    }

                    Email email = new Email(id, sender, subject, body, priority,
                            arrivalTime, arrivalOrder, read);

                    if (!emailMap.containsKey(id)) {
                        emailMap.put(id, email);
                        inboxQueue.offer(email);
                        loaded++;
                        nextArrivalOrder = Math.max(nextArrivalOrder, arrivalOrder + 1);
                        updateNextEmailNumber(id);
                    }
                } catch (RuntimeException ex) {
                    System.out.println("Skipped an unreadable row in inbox.txt.");
                }
            }

            System.out.println("Loaded " + loaded + " email(s) from inbox.txt.");

        } catch (IOException ex) {
            System.out.println("Could not load inbox.txt: " + ex.getMessage());
        }
    }

    // Save all emails, including read/unread status, when exiting.
    private void saveInbox() {
        try (PrintWriter writer = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(DATA_FILE),
                        StandardCharsets.UTF_8))) {

            writer.println("# id|sender(base64)|subject(base64)|priority|arrivalTime"
                    + "|arrivalOrder|read|body(base64)");

            ArrayList<Email> emails = getEmailsAsList();
            emails.sort(Comparator.comparingLong(Email::getArrivalOrder));

            for (Email email : emails) {
                writer.println(
                        email.getId() + "|"
                        + encode(email.getSender()) + "|"
                        + encode(email.getSubject()) + "|"
                        + email.getPriority() + "|"
                        + email.getArrivalTime() + "|"
                        + email.getArrivalOrder() + "|"
                        + email.isRead() + "|"
                        + encode(email.getBody())
                );
            }

            System.out.println("Saved " + emails.size() + " email(s) to " + DATA_FILE + ".");
        } catch (IOException ex) {
            System.out.println("Could not save inbox: " + ex.getMessage());
        }
    }

    private String encode(String value) {
        return Base64.getEncoder().encodeToString(
                value.getBytes(StandardCharsets.UTF_8));
    }

    private String decode(String value) {
        return new String(Base64.getDecoder().decode(value),
                StandardCharsets.UTF_8);
    }

    private void updateNextEmailNumber(String id) {
        if (id != null && id.matches("EM\\d+")) {
            try {
                int number = Integer.parseInt(id.substring(2));
                nextEmailNumber = Math.max(nextEmailNumber, number + 1);
            } catch (NumberFormatException ignored) {
                // Keep the current next ID if the numeric suffix is too large.
            }
        }
    }
}
