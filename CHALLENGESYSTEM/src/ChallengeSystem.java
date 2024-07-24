import jxl.Workbook;
import jxl.Sheet;
import jxl.Cell;
import jxl.read.biff.BiffException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public class ChallengeSystem {

    private static Map<String, String> users = new HashMap<>();
    private static Map<Integer, String> challenges = new HashMap<>();
    private static List<String> questions = new ArrayList<>();
    private static String excelFilePath = "/home/lovis/Desktop/WILLIAM/PROJECT ONE/CHALLENGESYSTEM/questions.xls";
    private static boolean loggedIn = false;
    private static String loggedInUsername = null;
    private static Map<String, Integer> userAttempts = new HashMap<>();
    private static final int MAX_ATTEMPTS = 10;
    private static int currentChallengeNumber = 1; // Start challenge number from 1
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        // Initialize some users and challenges
        users.put("user1", "password1");
        users.put("user2", "password2");

      /* challenges.put(1, "Open");
        challenges.put(2, "Open");
        challenges.put(3, "Open");
        challenges.put(4, "Open");
        challenges.put(5, "Open");
        challenges.put(6, "Open");
        challenges.put(7, "Open");
        challenges.put(8, "Open");
        challenges.put(9, "Open");
        challenges.put(10, "Open");*/

        loadQuestionsFromExcel(excelFilePath);

        String previousCommand = "";
        while (true) {
            if (previousCommand.isEmpty()) {
                System.out.println("Enter login command.");
            } else if (previousCommand.equalsIgnoreCase("login")) {
            } else if (previousCommand.equalsIgnoreCase("viewChallenges")) {
            } else if (previousCommand.equalsIgnoreCase("attemptChallenge")) {
            } else if (previousCommand.equalsIgnoreCase("logout")) {
            }

            String commandLine = scanner.nextLine();
            String[] commandArgs = commandLine.split(" ");
            if (commandArgs.length > 0) {
                previousCommand = commandArgs[0];

                if (previousCommand.equalsIgnoreCase("login")) {
                    login(scanner);
                    System.out.println("Enter viewChallenges command");
                } else if (previousCommand.equalsIgnoreCase("viewChallenges")) {
                    viewChallenges();
                    System.out.println("Enter attemptChallenge command");
                } else if (previousCommand.equalsIgnoreCase("attemptChallenge")) {
                    attemptChallenge();
                    if (userAttempts.get(loggedInUsername) >= MAX_ATTEMPTS) {
                        System.out.println("Enter logout command");
                    } else {
                        System.out.println("Enter next command");
                    }
                } else if (previousCommand.equalsIgnoreCase("next")) {
                    attemptChallenge();
                    if (userAttempts.get(loggedInUsername) >= MAX_ATTEMPTS) {
                        System.out.println("Enter logout command");
                    } else {
                        System.out.println("Enter next command");
                    }
                } else if (previousCommand.equalsIgnoreCase("logout")) {
                    logout();
                    System.out.println("Enter exit command");
                } else if (previousCommand.equalsIgnoreCase("exit")) {
                    break;
                } else {
                    System.out.println("Sorry!! \nTRY AGAIN!!");
                }
            } else {
                System.out.println("No command entered.");
            }
        }

        scanner.close();
    }

    private static void loadQuestionsFromExcel(String excelFilePath) {
        try (FileInputStream inputStream = new FileInputStream(new File(excelFilePath))) {
            Workbook workbook = Workbook.getWorkbook(inputStream);
            Sheet sheet = workbook.getSheet(0); //

            for (int row = 0; row < sheet.getRows(); row++) {
                Cell cell = sheet.getCell(0, row);
                questions.add(cell.getContents());
            }
            workbook.close();
        } catch (BiffException | IOException e) {
            e.printStackTrace();
        }
    }

    private static void login(Scanner scanner) {
        System.out.println("Enter username:");
        String username = scanner.nextLine();
        System.out.println("Enter password:");
        String password = scanner.nextLine();

        if (users.containsKey(username) && users.get(username).equals(password)) {
            loggedIn = true;
            loggedInUsername = username;
            userAttempts.put(username, 0);
            System.out.println("Login successful. Welcome " + username + "!");
        } else {
            System.out.println("Invalid username or password.");
        }
    }

    private static void logout() {
        loggedIn = false;
        loggedInUsername = null;
        System.out.println("Logged out successfully.");
    }

    private static void viewChallenges() {
        if (!loggedIn) {
            System.out.println("You must be logged in to view challenges.");
            return;
        }

        System.out.println("WELCOME TO THE CHALLENGE PART. \nMARK TIME. \nWISH YOU ALL THE BEST.");
        for (Map.Entry<Integer, String> entry : challenges.entrySet()) {
            System.out.println("Challenge " + entry.getKey() + ": " + entry.getValue());
        }
    }

    private static void attemptChallenge() {
        if (!loggedIn) {
            System.out.println("You must be logged in to attempt challenges.");
            return;
        }

        if (loggedInUsername == null) {
            System.out.println("No user is logged in.");
            return;
        }

        Integer attempts = userAttempts.get(loggedInUsername);
        if (attempts == null) {
            System.out.println("User not found.");
            return;
        }

        if (attempts >= MAX_ATTEMPTS) {
            System.out.println("You have reached the maximum number of attempts. Please log out.");
            return;
        }

        if (currentChallengeNumber > challenges.size()) {
            currentChallengeNumber = 1;
        }

        System.out.println("Challenge in progress:");
        if (!questions.isEmpty()) {
            String question = questions.get(new Random().nextInt(questions.size()));
            System.out.println(currentChallengeNumber + ". " + question);
            userAttempts.put(loggedInUsername, attempts + 1);
            System.out.println("You have " + (MAX_ATTEMPTS - attempts - 1) + " attempts left.");
        } else {
            System.out.println("No questions available.");
        }
        currentChallengeNumber++;
    }
}
