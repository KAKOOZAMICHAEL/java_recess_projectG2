

import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.*;

public class DbConnection {
    // database connection parameters
    String url = "jdbc:mysql://localhost:3306/math-challenge";
    String username = "root";
    String password = "";
    Connection conn;
    Statement statement;
    private Connection connection;

    public DbConnection() throws SQLException, ClassNotFoundException {
        Class.forName("com.mysql.cj.jdbc.Driver");

        this.conn = DriverManager.getConnection(this.url, this.username, this.password);
        this.statement = conn.createStatement();
    }

    public Connection getConnection() {
        return this.connection;
    }

    public void create(String sqlCommand) throws SQLException {
        this.statement.execute(sqlCommand);
    }

    public ResultSet read(String sqlCommand) throws SQLException {
        return this.statement.executeQuery(sqlCommand);
    }

    public void update(String sqlCommand) throws SQLException {
        this.statement.execute(sqlCommand);
    }

    public void delete(String sqlCommand) throws SQLException {
        this.statement.execute(sqlCommand);
    }

    public void close() throws SQLException {
        if (this.statement != null) this.statement.close();
        if (this.conn != null) this.conn.close();
    }

    public void createParticipant(String username, String firstname, String lastname, String emailAddress, String dob, String regNo, String imagePath) throws SQLException {
        String sql = "INSERT INTO `participants` (`username`, `firstname`, `lastname`, `email`, `date_of_birth`, `school_registration_number`, `image_file`) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = this.conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, firstname);
            stmt.setString(3, lastname);
            stmt.setString(4, emailAddress);
            stmt.setString(5, dob);
            stmt.setString(6, regNo);
            stmt.setString(7, imagePath);
            stmt.executeUpdate();
        }
    }

    public void createParticipantRejected(String username, String firstname, String lastname, String emailAddress, String dob, String regNo, String imagePath) throws SQLException {
        String sql = "INSERT INTO `rejected_participants` (`username`, `firstname`, `lastname`, `email`, `date_of_birth`, `school_registration_number`) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = this.conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, firstname);
            stmt.setString(3, lastname);
            stmt.setString(4, emailAddress);
            stmt.setString(5, dob);
            stmt.setString(6, regNo);
            stmt.executeUpdate();
        }
    }

    public ResultSet getChallenges() throws SQLException {
        String sql = "SELECT * FROM `math-challenge`.`challenges` WHERE `start_date` <= CURRENT_DATE AND `end_date` >= CURRENT_DATE;";
        return this.statement.executeQuery(sql);
    }

    public ResultSet getChallengeQuestions(int challenge_id) throws SQLException {
        String sql = "SELECT qa.* FROM `question_answers` qa JOIN `math-challenge`.`challenge_question_answers` cqa ON qa.id = cqa.question WHERE cqa.challenge = ?";
        PreparedStatement preparedStatement = this.conn.prepareStatement(sql);
        preparedStatement.setInt(1, challenge_id);
        return preparedStatement.executeQuery();
    }

    public int getAttemptScore(JSONArray attempt, int participant) throws SQLException {
        int score = 0;
        for (int i = 0; i < attempt.length(); i++) {
            JSONObject obj = attempt.getJSONObject(i);



            if (obj.get("answer").equals("-")) {
                score += 0;
                this.addAttempt(participant, obj.getInt("question_id"), false);
                continue;
            }

            String sql = "SELECT `score` FROM `question_answers` WHERE `id` = " + obj.getInt("question_id") + " AND `answer` = " + obj.get("answer") + ";";
            ResultSet questionScore = this.statement.executeQuery(sql);

            if (questionScore.next()) {
                score += questionScore.getInt("score");
                this.addAttempt(participant, obj.getInt("question_id"), true);
            } else {
                score -= 3;
                this.addAttempt(participant, obj.getInt("question_id"), false);
            }

        }
        return score;
    }

    public void createChallengeAttempt(JSONObject obj) throws SQLException {
        String sql = "INSERT INTO `participant_challenges` (`participant`, `challenge`, `score`, `total`) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = this.conn.prepareStatement(sql)) {
            ps.setInt(1, obj.getInt("participant_id"));
            ps.setInt(2, obj.getInt("challenge_id"));
            ps.setInt(3, obj.getInt("score"));
            ps.setInt(4, obj.getInt("total_score"));
            ps.executeUpdate();
        };
    }

    public ResultSet getRepresentative(String regNo) throws SQLException {
        String sqlCommand = "SELECT * FROM `schools` WHERE school_registration_number = " + regNo + ";";
        return this.statement.executeQuery(sqlCommand);
    }

    public ResultSet getSchool(String regNo) throws SQLException {
        String sqlCommand = "SELECT name FROM `schools` WHERE school_registration_number = " + regNo + ";";
        return this.statement.executeQuery(sqlCommand);
    }

    public void addAttempt(int participant, int question, boolean status) throws SQLException {
        String sqlCommand = "INSERT INTO `attempts` (`status`, `question`, `participant`) VALUES (" + (status ? "'correct'" : "'wrong'") + ", " + question + ", " + participant + ");";
        this.statement.execute(sqlCommand);
    }

}
