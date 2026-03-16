package mathquiz.quizapp;

import jakarta.persistence.*;

@Entity
public class Score {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String username;
    private Integer scoreValue;
    private String difficulty;
    
    // Constructors
    public Score() {}
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public Integer getScoreValue() { return scoreValue; }
    public void setScoreValue(Integer scoreValue) { this.scoreValue = scoreValue; }
    
    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
}