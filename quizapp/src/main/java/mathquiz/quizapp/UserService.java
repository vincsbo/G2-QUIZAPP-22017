package mathquiz.quizapp;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public User registerUser(String username, String password) {

        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists");
        }

        validatePassword(password);

        String hashed = passwordEncoder.encode(password);

        User user = new User(username, hashed);

        return userRepository.save(user);
    }

    public boolean authenticate(String username, String password) {

        return userRepository.findByUsername(username)
                .map(user -> passwordEncoder.matches(password, user.getPassword()))
                .orElse(false);
    }

    private void validatePassword(String password) {

        if (password.length() < 8)
            throw new RuntimeException("Password must be at least 8 characters");

        if (!password.matches(".*[A-Z].*"))
            throw new RuntimeException("Password must contain uppercase letter");

        if (!password.matches(".*[0-9].*"))
            throw new RuntimeException("Password must contain number");
    }
}