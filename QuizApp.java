import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

class DBConnection {

    // CHANGE these to match your setup
    private static final String URL = "jdbc:mysql://localhost:3306/quiz_db";
    private static final String USER = "root";
    private static final String PASSWORD = "qwerty";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
class Question {
    String question;
    String[] options;
    int correctAnswer;

    Question(String question, String[] options, int correctAnswer) {
        this.question = question;
        this.options = options;
        this.correctAnswer = correctAnswer;
    }
}

public class QuizApp extends JFrame {

    CardLayout card = new CardLayout();
    JPanel mainJPanel = new JPanel(card);

    JPanel card1 = new JPanel();
    JPanel card2 = new JPanel();

    java.util.List<Question> questions = new ArrayList<>();

    int current = 0;
    int score = 0;

    boolean answerChecked = false;

    // NEW: shows "Question X of Y" above the question
    JLabel progressLabel = new JLabel("", SwingConstants.CENTER);

    JLabel questionLabel = new JLabel("", SwingConstants.CENTER);

    JRadioButton opt1 = new JRadioButton();
    JRadioButton opt2 = new JRadioButton();
    JRadioButton opt3 = new JRadioButton();

    ButtonGroup group = new ButtonGroup();

    // CHANGED: starts as "Check Answer" instead of "Next"
    JButton nextBtn = new JButton("Check Answer");

    JLabel resultLabel = new JLabel();

    // NEW: lets the user restart the quiz from the result screen
    JButton playAgainBtn = new JButton("Play Again");

    public final() {
        setTitle("Java Quiz Application");

        // NEW: style for the progress label
        progressLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        progressLabel.setForeground(Color.GRAY);
        progressLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        questionLabel.setFont(new Font("Arial", Font.BOLD, 18));
        questionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        nextBtn.setFont(new Font("Arial", Font.BOLD, 15));
        nextBtn.setBackground(new Color(33, 150, 243));
        nextBtn.setForeground(Color.WHITE);
        nextBtn.setFocusPainted(false);
        nextBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        Font f = new Font("Arial", Font.PLAIN, 16);

        opt1.setFont(f);
        opt2.setFont(f);
        opt3.setFont(f);

        opt1.setBackground(card1.getBackground());
        opt2.setBackground(card1.getBackground());
        opt3.setBackground(card1.getBackground());

        setTitle("Quiz");
        setSize(420, 360); // CHANGED: slightly taller to fit the progress label
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        card1.setBackground(new Color(245, 248, 255));
        card2.setBackground(new Color(245, 248, 255));

        // CHANGED: question creation pulled out into its own method, bank expanded to 8
        loadQuestions();

        // Quiz Card
        card1.setLayout(new BoxLayout(card1, BoxLayout.Y_AXIS));
        card1.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        group.add(opt1);
        group.add(opt2);
        group.add(opt3);

        // NEW: progress label added above the question
        card1.add(progressLabel);
        card1.add(Box.createVerticalStrut(10));
        card1.add(questionLabel);

        card1.add(Box.createVerticalStrut(20));
        card1.add(opt1);

        card1.add(Box.createVerticalStrut(10));
        card1.add(opt2);

        card1.add(Box.createVerticalStrut(10));
        card1.add(opt3);

        card1.add(Box.createVerticalStrut(20));
        card1.add(nextBtn);
        card1.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(33, 150, 243), 2, true),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        // Result Card
        // CHANGED: card2 now uses BoxLayout so the Play Again button stacks under the result text
        card2.setLayout(new BoxLayout(card2, BoxLayout.Y_AXIS));
        resultLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // NEW: style for Play Again button
        playAgainBtn.setFont(new Font("Arial", Font.BOLD, 15));
        playAgainBtn.setBackground(new Color(33, 150, 243));
        playAgainBtn.setForeground(Color.WHITE);
        playAgainBtn.setFocusPainted(false);
        playAgainBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        card2.add(Box.createVerticalStrut(20));
        card2.add(resultLabel);
        card2.add(Box.createVerticalStrut(20));
        card2.add(playAgainBtn); // NEW

        card1.setBackground(new Color(230, 240, 255));   // Light Blue
        card2.setBackground(new Color(232, 245, 233));   // Light Green
        mainJPanel.setBackground(new Color(245, 245, 245)); // Light Gray

        // Add Cards
        mainJPanel.add(card1, "QUESTION");
        mainJPanel.add(card2, "RESULT");

        add(mainJPanel);

        // Load First Question
        loadQuestion();

        nextBtn.addActionListener(e -> {

            // First Click -> Check Answer
            if (!answerChecked) {

                // NEW: don't let the user proceed without picking an option
                if (!opt1.isSelected() && !opt2.isSelected() && !opt3.isSelected()) {
                    JOptionPane.showMessageDialog(this, "Please select an answer!");
                    return;
                }

                checkAnswer();
                answerChecked = true;
                nextBtn.setText("Next"); // NEW: button label changes to reflect next click's action
                return;
            }

            // Second Click -> Next Question
            current++;

            if (current < questions.size()) {
                loadQuestion();
                card.show(mainJPanel, "QUESTION");
            } else {
                resultLabel.setFont(new Font("Arial", Font.BOLD, 22));
                resultLabel.setForeground(new Color(0, 128, 0));
                resultLabel.setText(
                        "<html><center>🎉 Quiz Completed!<br><br>Score: "
                                + score + " / " + questions.size()
                                + "</center></html>");

                card.show(mainJPanel, "RESULT");
            }
        });

        // NEW: resets state and restarts the quiz from question 1
        playAgainBtn.addActionListener(e -> {
            current = 0;
            score = 0;
            loadQuestion();
            card.show(mainJPanel, "QUESTION");
        });

        setLocationRelativeTo(null);
        setVisible(true);
    }

    // CHANGED: questions now come from the MySQL "questions" table instead of being hardcoded
    private void loadQuestions() {
        String sql = "SELECT question, option1, option2, option3, correct_answer FROM questions";

        try (Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String questionText = rs.getString("question");
                String[] options = {
                        rs.getString("option1"),
                        rs.getString("option2"),
                        rs.getString("option3")
                };
                int correctAnswer = rs.getInt("correct_answer");

                questions.add(new Question(questionText, options, correctAnswer));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Could not load questions from database.\n" + e.getMessage());
        }

        if (questions.isEmpty()) {
            loadDefaultQuestions();
        }
    }

    private void loadDefaultQuestions() {
        questions.add(new Question("What is the capital of France?", new String[]{"Paris", "Rome", "Madrid"}, 0));
        questions.add(new Question("Which language is used for Android development?", new String[]{"Swift", "Kotlin", "Ruby"}, 1));
        questions.add(new Question("Which planet is closest to the Sun?", new String[]{"Venus", "Mercury", "Mars"}, 1));
    }

    // Load Question
    private void loadQuestion() {

        Question q = questions.get(current);

        // NEW: update progress label for the current question
        progressLabel.setText("Question " + (current + 1) + " of " + questions.size());

        questionLabel.setText(q.question);

        opt1.setText(q.options[0]);
        opt2.setText(q.options[1]);
        opt3.setText(q.options[2]);

        group.clearSelection();

        // Reset Colors
        opt1.setForeground(Color.BLACK);
        opt2.setForeground(Color.BLACK);
        opt3.setForeground(Color.BLACK);

        nextBtn.setText("Check Answer"); // NEW: reset button label for the new question
        answerChecked = false;
    }

    // Check Answer
    private void checkAnswer() {

        Question q = questions.get(current);

        JRadioButton[] options = {opt1, opt2, opt3};

        int selected = -1;

        if (opt1.isSelected())
            selected = 0;
        else if (opt2.isSelected())
            selected = 1;
        else if (opt3.isSelected())
            selected = 2;

        // Show correct answer in GREEN
        options[q.correctAnswer].setForeground(Color.GREEN);

        // Show wrong selected answer in RED
        if (selected != -1 && selected != q.correctAnswer) {
            options[selected].setForeground(Color.RED);
        }

        // Increase score
        if (selected == q.correctAnswer) {
            score++;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(QuizApp::new);
    }
}

