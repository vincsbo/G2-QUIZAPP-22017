package mathquiz.quizapp;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ScoreRepository extends JpaRepository<Score, Long> {
    
    List<Score> findTop5ByOrderByScoreValueDesc();
    
    List<Score> findTop10ByOrderByScoreValueDesc();
    
    // This should work, but let's ensure it never returns null
    List<Score> findByUsername(String username);
    
    // Alternative: Use Optional to handle null cases
    Optional<Score> findTopByUsernameOrderByScoreValueDesc(String username);
}