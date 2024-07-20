

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.util.regex.Pattern;

public class ClientInstance {
    // define attributes for the ClientInstance object
    String hostname;
    int port;
    String clientId;
    User user;
    byte cache;
    boolean isStudent;
    boolean isAuthenticated;

    public ClientInstance(String hostname, int port, User user) {
        // constructor class for the client instance
        this.hostname = hostname;
        this.port = port;
        this.user = user;
    }

    public static boolean isValid(String input) {
        String regex = "^\\{.*\\}$";
        Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);

        return pattern.matcher(input).matches();
    }

    public JSONArray displayQuestionSet(JSONObject challengeObj) {
        System.out.println("CHALLENGE " + challengeObj.getInt("challenge_id") + " (" + challengeObj.get("challenge_name") + ")");
        Scanner scanner = new Scanner(System.in);

        JSONArray questions = challengeObj.getJSONArray("questions");
        JSONArray solutions = new JSONArray();
        this.cache = 0;
        int count = 1;
        for (int i = 0; i < questions.length(); i++) {
            JSONObject question = questions.getJSONObject(i);
            JSONObject answer = new JSONObject();
            this.cache += (byte) question.getInt("score");

            System.out.println(count + ". " + question.get("question") + " (" + question.get("score") + " Marks)");

            answer.put("question_id", question.getInt("id"));
            System.out.print(" - ");
            answer.put("answer", scanner.nextLine());

            solutions.put(answer);
            count++;
            System.out.print("\n");
        }
        return solutions;
    }

    public void start() throws IOException {
        // Todo: create a parent menu

        // execute code for interacting with the server
        try (
                Socket socket = new Socket(hostname, port);
                BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader consoleInput = new BufferedReader(new InputStreamReader(System.in));
        ) {
            this.clientId = (String) socket.getInetAddress().getHostAddress();
            Serializer serializer = new Serializer(this.user);

            printMenu();

            System.out.print("[" + this.clientId + "] (" + this.user.username + ") -> ");
            // read command line input

            // Continuously read from the console and send to the server
            ClientController clientController = new ClientController(user);
            String regex = "^\\{.*\\}$";
            Pattern pattern = Pattern.compile(regex);


            String userInput;
            while ((userInput = consoleInput.readLine()) != null) {
                // send command to the server

                if (userInput.equals("logout") && (this.user.isAuthenticated)) {
                    System.out.println("Session successfully logged out");
                    this.user.logout();
                    System.out.print("[" + this.clientId + "] (" + (!this.user.username.isBlank() ? this.user.username : null) + ") -> ");
                    continue;
                }

                String serializedCommand = serializer.serialize(userInput);

                if (isValid(serializedCommand)) {
                    output.println(serializedCommand);

                    // read response here from the server
                    String response = input.readLine();

                    this.user = clientController.exec(response);

                    if (!pattern.matcher(this.user.output).matches()) {
                        System.out.println("\n" + user.output + "\n");
                    } else {
                        JSONObject questions = new JSONObject(this.user.output);
                        JSONArray answerSet = displayQuestionSet(questions);

                        JSONObject obj = new JSONObject();
                        obj.put("attempt", answerSet);
                        obj.put("participant_id", this.user.id);
                        obj.put("command", "attempt");
                        obj.put("challenge_id", questions.getInt("challenge_id"));
                        obj.put("total_score", this.cache);

                        String inp = obj.toString();
                        output.println(inp);
                    }
                } else {
                    System.out.println(serializedCommand);
                }
                // prompt for the next instruction
                System.out.print("[" + this.clientId + "] (" + this.user.username + ") -> ");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.out.println("Connection with the server timeout");
        }
    }

    private void printMenu() {
        System.out.println("\n *****  MATHEMATICS  CHALLENGE  MENU   *****");

        System.out.println("Register your details in this format: \n\n" +
                "(register username firstname lastname emailAddress date_of_birth school_registration_number path_to_image):\n");
        System.out.println("Enter login to access this menu:\n");
        System.out.println("viewChallenges");
        System.out.println("attemptChallenge");
        System.out.println("viewApplicants");
        System.out.println("confirm yes/no \n\n");
    }
}
