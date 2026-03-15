package mathquiz.quizapp;

import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class MathService {
    
    private final Random random = new Random();

    public List<Map<String, Object>> generateQuiz(String difficulty) {
        List<Map<String, Object>> quiz = new ArrayList<>();
        int range = getRange(difficulty);

        for (int i = 0; i < 10; i++) {
            Map<String, Object> question = generateQuestion(range, i);
            quiz.add(question);
        }
        return quiz;
    }

    private int getRange(String difficulty) {
        return switch (difficulty.toLowerCase()) {
            case "hard" -> 100;
            case "medium" -> 50;
            default -> 10;
        };
    }

    private Map<String, Object> generateQuestion(int range, int index) {
        int n1 = random.nextInt(range) + 1;
        int n2 = random.nextInt(range) + 1;
        
        String[] ops = {"+", "-", "*"};
        String op = ops[random.nextInt(ops.length)];
        
        // Adjust for subtraction to avoid negative
        if (op.equals("-") && n1 < n2) {
            int temp = n1;
            n1 = n2;
            n2 = temp;
        }

        int correctAnswer = calculateAnswer(n1, n2, op);
        
        // Generate 4 options (A, B, C, D)
        List<Integer> options = generateOptions(correctAnswer, range);
        Collections.shuffle(options);
        
        // Find correct option letter
        char correctLetter = (char) ('A' + options.indexOf(correctAnswer));
        
        Map<String, Object> q = new HashMap<>();
        q.put("id", index);
        q.put("question", n1 + " " + op + " " + n2 + " = ?");
        q.put("answer", correctLetter); // A, B, C, or D
        q.put("answerValue", correctAnswer); // actual number for display
        q.put("optionA", options.get(0));
        q.put("optionB", options.get(1));
        q.put("optionC", options.get(2));
        q.put("optionD", options.get(3));
        
        return q;
    }

    private List<Integer> generateOptions(int correct, int range) {
        Set<Integer> opts = new HashSet<>();
        opts.add(correct);
        
        while (opts.size() < 4) {
            int offset = random.nextInt(range / 2 + 5) - (range / 4);
            int wrong = correct + offset;
            if (wrong != correct && wrong >= 0) {
                opts.add(wrong);
            }
        }
        
        return new ArrayList<>(opts);
    }

    private int calculateAnswer(int n1, int n2, String op) {
        return switch (op) {
            case "+" -> n1 + n2;
            case "-" -> n1 - n2;
            case "*" -> n1 * n2;
            default -> 0;
        };
    }
}