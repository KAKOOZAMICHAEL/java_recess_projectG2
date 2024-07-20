

import org.json.*;

import javax.mail.MessagingException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Controller {
    JSONObject obj;

    public Controller(JSONObject obj) {
        this.obj = obj;
    }
    private JSONObject login(JSONObject obj) throws SQLException, ClassNotFoundException {
        // logic to log in a student this can work with isAuthenticated == false only (!isAuthenticated)
        DbConnection dbConnection = new DbConnection();

        JSONArray tokens = obj.getJSONArray("tokens");

        String username = tokens.get(1).toString();
        String email = tokens.get(2).toString();

        JSONObject clientResponse = new JSONObject();
        clientResponse.put("command", "login");
        clientResponse.put("username", username);
        clientResponse.put("email", email);

        String readParticipantQuery = "SELECT * FROM participants";
        ResultSet participantResultSet = dbConnection.read(readParticipantQuery);
        while (participantResultSet.next()) {
            if (username.equals(participantResultSet.getString("username")) && email.equals(participantResultSet.getString("email"))) {
                // there is a match here

                String regNo = participantResultSet.getString("school_registration_number");

                clientResponse.put("participant_id", participantResultSet.getInt("id"));
                clientResponse.put("school_registration_number", regNo);
                clientResponse.put("school_name","undefined");
                clientResponse.put("isStudent", true);
                clientResponse.put("isAuthenticated", true);
                clientResponse.put("status", true);

                return  clientResponse;
            }
        }


        String readRepresentativeQuery = "SELECT * FROM schools";
        ResultSet representativeResultSet = dbConnection.read(readRepresentativeQuery);
        while (representativeResultSet.next()) {
            if (username.equals(representativeResultSet.getString("representative_name")) && email.equals(representativeResultSet.getString("representative_email"))) {
                // there is a match

                String schoolName = representativeResultSet.getString("school_name");
                String regNo = representativeResultSet.getString("school_registration_number");

                clientResponse.put("participant_id", 0);
                clientResponse.put("school_name", schoolName);
                clientResponse.put("school_registration_number", regNo);
                clientResponse.put("isStudent", false);
                clientResponse.put("isAuthenticated", true);
                clientResponse.put("status", true);

                return clientResponse;
            }
        }

        clientResponse.put("isStudent", false);
        clientResponse.put("isAuthenticated", false);
        clientResponse.put("status", false);
        clientResponse.put("reason", "Invalid credentials. check the details provided");

        return clientResponse;
    }

    private JSONObject register(JSONObject obj) throws IOException, MessagingException, SQLException, ClassNotFoundException {
        // logic to register student this can work with isAuthenticated == false only (!isAuthenticated)
        Email emailAgent = new Email();
        DbConnection dbConnection = new DbConnection();

        JSONArray tokens = obj.getJSONArray("tokens");
        JSONObject participantObj = new JSONObject();
        participantObj.put("username", tokens.get(1));
        participantObj.put("firstname", tokens.get(2));
        participantObj.put("lastname", tokens.get(3));
        participantObj.put("email", tokens.get(4));
        participantObj.put("date_of_birth", tokens.get(5));
        participantObj.put("school_registration_number", tokens.get(6));
        participantObj.put("image_file", tokens.get(7));

        JSONObject clientResponse = new JSONObject();
        clientResponse.put("command", "register");

        ResultSet rs = dbConnection.getRepresentative(participantObj.getString("school_registration_number"));
        String representativeEmail;
        if (rs.next()) {
            representativeEmail = rs.getString("representative_email");
        } else {
            clientResponse.put("status", false);
            clientResponse.put("reason", "school does not exist in our database");

            return clientResponse;
        }


        LocalStorage localStorage = new LocalStorage("participants.json");
        if (!localStorage.read().toString().contains(participantObj.toString())) {
            localStorage.add(participantObj);
            clientResponse.put("status", true);
            clientResponse.put("reason", "Participant created successfully awaiting representative approval");

            emailAgent.sendParticipantRegistrationRequestEmail(representativeEmail, participantObj.getString("email"), participantObj.getString("username"));

            return clientResponse;
        }

        clientResponse.put("status", false);
        clientResponse.put("reason", "Participant creation failed found an existing participant object");

        return clientResponse;
    }

    private JSONObject attemptChallenge(JSONObject obj) throws SQLException, ClassNotFoundException {
        // logic to attempt a challenge respond with the random questions if user is eligible (student, isAuthenticated)
        JSONObject clientResponse = new JSONObject();
        JSONArray questions = new JSONArray();

        DbConnection dbConnection = new DbConnection();


//        dbConnection.read("SELECT `challenge_name` FROM `mtchallenge`")

        int challengeId = Integer.parseInt((String) new JSONArray(obj.get("tokens").toString()).get(1));
        ResultSet challengeQuestions;
        challengeQuestions = dbConnection.getChallengeQuestions(challengeId);


        while (challengeQuestions.next()) {
            JSONObject question = new JSONObject();
            question.put("id", challengeQuestions.getString("id"));
            question.put("question", challengeQuestions.getString("question"));
            question.put("score", challengeQuestions.getString("score"));

            questions.put(question);
        }

        clientResponse.put("command", "attemptChallenge");
        clientResponse.put("questions", questions);
        clientResponse.put("challenge_id", challengeId);
        clientResponse.put("challenge_name", challengeId);
        return clientResponse;
    }

    private JSONObject viewChallenges(JSONObject obj) throws SQLException, ClassNotFoundException {
        JSONObject clientResponse = new JSONObject();

        DbConnection dbConnection = new DbConnection();
        ResultSet availableChallenges = dbConnection.getChallenges();
        JSONArray challenges = new JSONArray();

        while (availableChallenges.next()) {
            JSONObject challenge = new JSONObject();
            challenge.put("id", availableChallenges.getInt("id"));
            challenge.put("challenge_name", availableChallenges.getString("challenge_name"));
            challenge.put("duration", availableChallenges.getInt("duration"));
            challenge.put("start_date", availableChallenges.getDate("start_date"));
            challenge.put("end_date", availableChallenges.getDate("end_date"));

            challenges.put(challenge);
        }

        clientResponse.put("command", "viewChallenges");
        clientResponse.put("challenges", challenges.toString());

        return clientResponse;
    }

    private JSONObject confirm(JSONObject obj) throws IOException, SQLException, ClassNotFoundException {
        // Initialize file storage for participants
        LocalStorage fileStorage = new LocalStorage("participants.json");

        // Extract username from object
        String username = obj.getString("username");
        JSONObject participant = fileStorage.readEntryByUserName(username);

        // Prepare client response JSON object
        JSONObject clientResponse = new JSONObject();
        clientResponse.put("command", "confirm");

        // Check if participant exists
        if (participant.isEmpty()) {
            clientResponse.put("status", false);
            clientResponse.put("reason", "Invalid command. Check the username provided");
            return clientResponse;
        }

        // Initialize database connection
        DbConnection dbConnection = new DbConnection();

        // Confirm or reject participant based on 'confirm' flag
        if (obj.getBoolean("confirm")) {
            dbConnection.createParticipant(participant.getString("username"), participant.getString("firstname"),
                    participant.getString("lastname"), participant.getString("email"),
                    participant.getString("date_of_birth"), participant.getString("school_registration_number"), participant.getString("image_file"));
            fileStorage.deleteEntryByUserName(username);
            clientResponse.put("reason", participant.getString("firstname") + " " + participant.getString("lastname") +
                    " (" + participant.getString("email") + ") confirmed successfully");
        } else {
            dbConnection.createParticipantRejected(participant.getString("username"), participant.getString("firstname"),
                    participant.getString("lastname"), participant.getString("email"),
                    participant.getString("date_of_birth"), participant.getString("school_registration_number"), participant.getString("image_file"));
            fileStorage.deleteEntryByUserName(username);
            clientResponse.put("reason", participant.getString("firstname") + " " + participant.getString("lastname") +
                    " (" + participant.getString("email") + ") rejected successfully");
        }
        clientResponse.put("status", true);
        return clientResponse;
    }

    private JSONObject viewApplicants(JSONObject obj) throws IOException {
        // logic to confirm registered students (representatives, isAuthenticated)
        String regNo = obj.getString("school_registration_number");

        LocalStorage localStorage = new LocalStorage("participants.json");

        String participants = localStorage.filterParticipantsByRegNo(regNo);

        JSONObject clientResponse = new JSONObject();
        clientResponse.put("command", "viewApplicants");
        clientResponse.put("applicants", participants);


        return clientResponse;
    }

    public JSONObject attempt(JSONObject obj) throws SQLException, ClassNotFoundException {
        JSONArray attempt = obj.getJSONArray("attempt");
        DbConnection dbConnection = new DbConnection();

        JSONObject attemptEvaluation = new JSONObject();
        attemptEvaluation.put("score", dbConnection.getAttemptScore(attempt, obj.getInt("participant_id")));
        attemptEvaluation.put("participant_id", obj.getInt("participant_id"));
        attemptEvaluation.put("challenge_id", obj.getInt("challenge_id"));
        attemptEvaluation.put("total_score", obj.getInt("total_score"));

        dbConnection.createChallengeAttempt(attemptEvaluation);

        // get the score


        // SELECT score

        // add the attempt record


        return new JSONObject();
    }

    public JSONObject run() throws IOException, SQLException, ClassNotFoundException, MessagingException {
        switch (this.obj.get("command").toString()) {
            case "login":
                // call login logic
                return this.login(this.obj);

            case "register":
                // call login logic
                return this.register(this.obj);

            case "viewChallenges":
                // call login logic
                return this.viewChallenges(this.obj);

            case "attemptChallenge":
                // call login logic
                return this.attemptChallenge(this.obj);

            case "confirm":
                // call login logic
                return this.confirm(this.obj);

            case "viewApplicants":
                return this.viewApplicants(this.obj);

            case "attempt":
                // handle attempts here
                return this.attempt(this.obj);

            default:
                // command unresolved
                JSONObject outputObj = new JSONObject();
                outputObj.put("command", "exception");
                outputObj.put("reason", "Invalid command");

                return outputObj;
        }
    }
}

