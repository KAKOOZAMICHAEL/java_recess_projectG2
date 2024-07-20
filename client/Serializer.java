


import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Scanner;

public class Serializer {
    User user;

    public Serializer(User user) {
        this.user = user;
    }

    public String login() {
        if (this.user.isAuthenticated) {
            return "Session already authenticated";
        }

        // collect user login details
        Scanner scanner = new Scanner(System.in);
        System.out.println("Please enter user login details (username and email)");
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Email: ");
        String email = scanner.nextLine();

        System.out.println("\n");
        String[] tokens = new String[3];
        tokens[0] = "login";
        tokens[1] = username;
        tokens[2] = email;

        JSONObject obj = new JSONObject();
        obj.put("command", "login");
        obj.put("isAuthenticated", false);
        obj.put("tokens", tokens);
        obj.put("isStudent", false);

        return obj.toString(4);
    }

    public String register(String[] arr) {
        JSONObject obj = new JSONObject();
        obj.put("command", "register");
        obj.put("isAuthenticated", user.isAuthenticated);
        obj.put("tokens", arr);
        obj.put("tokenized_image", tokenizeImage(arr[7]));
        obj.put("isStudent", user.isStudent);


        return obj.toString(4);
    }

    private static JSONObject tokenizeImage(String path) {
        JSONObject jsonObject = new JSONObject();
        JSONArray arr = new JSONArray();

        File file = new File(path);
        if (!file.exists()) {
            return new JSONObject();
        }

        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            byte[] buffer = new byte[4 * 1024];
            int bytesRead;


            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                JSONObject obj = new JSONObject();
                byte[] bufferCopy = new byte[bytesRead];
                System.arraycopy(buffer, 0, bufferCopy, 0, bytesRead);

                obj.put("buffer", bufferCopy);
                obj.put("size", bytesRead);
                arr.put(obj);
            }

            jsonObject.put("data", arr);
            jsonObject.put("size", new File(path).length());

        } catch (IOException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public String attemptChallenge(String[] tokens) {
        JSONObject obj = new JSONObject();
        obj.put("command", "attemptChallenge");
        obj.put("tokens", tokens);

        return obj.toString(4);
    }

    public String viewApplicants() {
        JSONObject obj = new JSONObject();
        obj.put("command", "viewApplicants");
        obj.put("isAuthenticated", this.user.isAuthenticated);
        obj.put("isStudent", this.user.isStudent);
        obj.put("school_registration_number", this.user.regNo);


        return obj.toString(4);
    }

    public String confirm(String[] arr) {
        JSONObject obj = new JSONObject();
        obj.put("command", "confirm");
        obj.put("username", arr[2]);
        obj.put("school_registration_number", this.user.regNo);
        obj.put("confirm", (arr[1].toLowerCase().equals("yes")) ? true : false);
        obj.put("tokens", arr);

        return obj.toString(4);
    }

    public String viewChallenges() {
        JSONObject obj = new JSONObject();
        obj.put("command", "viewChallenges");
        obj.put("isAuthenticated", this.user.isAuthenticated);
        obj.put("isStudent", this.user.isStudent);


        return obj.toString(4);
    }

    public String logout() {
        this.user.logout();
        return "Successfully logged out";
    }

    public String serialize(String command) {
        String[] tokens = command.split("\\s+");

        if (!user.isAuthenticated && tokens[0].equals("register")) {
            return this.register(tokens);
        }

        if (!user.isAuthenticated && tokens[0].equals("login")) {
            return this.login();
        }

        if (!user.isAuthenticated) {
            return "Session unauthenticated first login by entering command login";
        }

        if (user.isStudent) {
            switch (tokens[0]) {
                case "logout":
                    return this.logout();

                case "viewChallenges":
                    return this.viewChallenges();

                case "attemptChallenge":
                    return this.attemptChallenge(tokens);

                default:
                    return "Invalid student command";
            }
        } else {
            switch (tokens[0]) {
                case "logout":
                    return this.logout();

                case "confirm":
                    return this.confirm(tokens);

                case "viewApplicants":
                    return this.viewApplicants();

                default:
                    return "Invalid school representative command";
            }
        }

    }
}

