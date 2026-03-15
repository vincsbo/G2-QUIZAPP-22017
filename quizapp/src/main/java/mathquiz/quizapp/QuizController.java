package mathquiz.quizapp;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@Controller
public class QuizController {

    private final MathService mathService;
    private final ScoreRepository scoreRepository;
    private final UserService userService;

    public QuizController(MathService mathService, ScoreRepository scoreRepository, UserService userService) {
        this.mathService = mathService;
        this.scoreRepository = scoreRepository;
        this.userService = userService;
    }

    // LOGIN PAGE
    @GetMapping("/")
    public String login() {
        return "login";
    }

    // PROCESS LOGIN
    @PostMapping("/login")
    public String processLogin(@RequestParam String username,
                               @RequestParam String password,
                               HttpSession session,
                               Model model) {

        if (userService.authenticate(username, password)) {
            session.setAttribute("username", username);
            return "redirect:/dashboard";
        }

        model.addAttribute("error", "Invalid username or password");
        return "login";
    }

    // REGISTER PAGE
    @GetMapping("/register")
    public String register() {
        return "register";
    }

    // PROCESS REGISTER
    @PostMapping("/register")
    public String processRegister(@RequestParam String username,
                                  @RequestParam String password,
                                  Model model) {

        try {
            userService.registerUser(username, password);
            model.addAttribute("success", "Registration successful! Please login.");
            return "login";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }

    // DASHBOARD
    @GetMapping("/dashboard")
    public String dash(HttpSession session, Model model) {
        String username = (String) session.getAttribute("username");
        if (username == null) return "redirect:/";

        model.addAttribute("username", username);

        List<Score> topScores = scoreRepository.findTop10ByOrderByScoreValueDesc();
        model.addAttribute("topScores", topScores);

        return "dashboard";
    }

    // PROFILE PAGE
   @GetMapping("/profile")
public String profile(HttpSession session, Model model) {
    String username = (String) session.getAttribute("username");
    
    if (username == null) {
        return "redirect:/";
    }
    
    List<Score> scores = scoreRepository.findByUsername(username);
    
    // Ensure scores is never null for the stream
    if (scores == null) scores = new ArrayList<>();
    
    int totalGames = scores.size();
    
    // Added .filter() to prevent null value crashes
    int bestScore = scores.stream()
            .filter(s -> s.getScoreValue() != null)
            .mapToInt(Score::getScoreValue)
            .max()
            .orElse(0);
    
    model.addAttribute("username", username);
    model.addAttribute("totalGames", totalGames);
    model.addAttribute("bestScore", bestScore);
    
    return "profile";
}

    // STATISTICS PAGE
   @GetMapping("/statistics")
public String statistics(HttpSession session, Model model) {
    String username = (String) session.getAttribute("username");
    if (username == null) return "redirect:/";
    
    List<Score> scores = scoreRepository.findByUsername(username);
    if (scores == null) {
        scores = new ArrayList<>();
    }
    
    int totalGames = scores.size();
    
    // Safely calculate total score by filtering out potential nulls
    int totalScore = scores.stream()
            .filter(s -> s.getScoreValue() != null)
            .mapToInt(Score::getScoreValue)
            .sum();
            
    double avgScore = totalGames > 0 ? (double) totalScore / totalGames : 0.0;
    
    model.addAttribute("totalGames", totalGames);
    model.addAttribute("totalScore", totalScore); // Needed for the new design
    model.addAttribute("avgScore", String.format("%.2f", avgScore));
    
    return "statistics";
}

    // LOGOUT
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    // START QUIZ
    @GetMapping("/quiz")
    public String start(@RequestParam String difficulty,
                        HttpSession session,
                        Model model) {

        String username = (String) session.getAttribute("username");
        if (username == null) return "redirect:/";

        List<Map<String, Object>> questions = mathService.generateQuiz(difficulty);

        session.setAttribute("questions", questions);
        session.setAttribute("difficulty", difficulty);

        model.addAttribute("questions", questions);
        model.addAttribute("difficulty", difficulty);

        return "quiz";
    }

    
         // SUBMIT QUIZ
 @PostMapping("/quiz/submit")
public String submit(HttpSession session,
                     jakarta.servlet.http.HttpServletRequest request) {

    String username = (String) session.getAttribute("username");
    if (username == null) {
        return "redirect:/";
    }

    Object qObj = session.getAttribute("questions");

    if (qObj == null) {
        return "redirect:/dashboard";
    }

    @SuppressWarnings("unchecked")
    List<Map<String, Object>> questions = (List<Map<String, Object>>) qObj;

    String difficulty = (String) session.getAttribute("difficulty");
    if (difficulty == null) {
        difficulty = "easy";
    }

    int score = 0;

    for (int i = 0; i < questions.size(); i++) {

        String userAnswer = request.getParameter("answer_" + i);

        if (userAnswer == null) {
            continue;
        }

        String correctAnswer = String.valueOf(
                questions.get(i).get("answer")
        );

        if (userAnswer.equals(correctAnswer)) {
            score++;
        }
    }

    try {

        Score scoreEntity = new Score();
        scoreEntity.setUsername(username);
        scoreEntity.setScoreValue(score);
        scoreEntity.setDifficulty(difficulty);

        scoreRepository.save(scoreEntity);

    } catch (Exception e) {

        e.printStackTrace();

        return "redirect:/dashboard";
    }

    session.setAttribute("summaryScore", score);
    session.setAttribute("summaryDifficulty", difficulty);

    session.removeAttribute("questions");
    session.removeAttribute("difficulty");

   
    return "redirect:/summary?score=" + score + "&difficulty=" + difficulty;
}
    // SUMMARY PAGE
   @GetMapping("/summary")
public String summary(@RequestParam(required = false, defaultValue = "0") Integer score,
                      @RequestParam(required = false, defaultValue = "easy") String difficulty,
                      HttpSession session, 
                      Model model) {
                      
    String username = (String) session.getAttribute("username");
    if (username == null) return "redirect:/";

    // 1. Logic for statistics
    int totalQuestions = 10;
    int correct = score;
    int wrong = Math.max(0, totalQuestions - score);
    double accuracy = ((double) score / totalQuestions) * 100;

    // 2. Add to model (Matches your summary.html variables)
    model.addAttribute("score", score);
    model.addAttribute("difficulty", difficulty);
    model.addAttribute("correct", correct);
    model.addAttribute("wrong", wrong);
    model.addAttribute("accuracy", String.format("%.1f", accuracy));

    // 3. Leaderboard
    List<Score> topScores = scoreRepository.findTop5ByOrderByScoreValueDesc();
    model.addAttribute("topScores", topScores);

    return "summary";
}
    }
