package plant_village.service.impl;

import plant_village.model.User;
import plant_village.repository.UserRepository;
import plant_village.service.UserService;
import plant_village.exception.ValidationException;
import plant_village.exception.ResourceNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service // it provide managing Spring this class
public class UserServiceImpl implements UserService { // interface

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    
    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder(); // default strength 10
    }

    @Override
    public User registerNewUser(User user) {
        if (checkUsernameExists(user.getUserName())) {
            throw new ValidationException("Bu kullanıcı adı zaten kullanılıyor.");
        }
        if (findByEmail(user.getEmail()).isPresent()) {
            throw new ValidationException("Bu e-posta adresi zaten kayıtlı.");
        }

        user.setCreatedAt(LocalDateTime.now());
        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("USER");
        }
        String plainPassword = user.getPassword();
        String hashedPassword = passwordEncoder.encode(plainPassword);
        user.setPassword(hashedPassword);
        
        return userRepository.save(user);
    }

    @Override
    public Optional<User> findById(Integer userId) {
        return userRepository.findById(userId);
    }
    
    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    @Override
    public boolean checkUsernameExists(String userName) {
        return userRepository.existsByUserName(userName);
    }
    
    public boolean verifyPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    public User updateUser(User user) {
        // check the user exist
        if (!findById(user.getUserId()).isPresent()) {
            throw new ResourceNotFoundException("Kullanıcı bulunamadı.");
        }
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            if (!user.getPassword().startsWith("$2a$")) {
                String hashedPassword = passwordEncoder.encode(user.getPassword());
                user.setPassword(hashedPassword);
            }
        }
        return userRepository.save(user);
    }

    public void deleteUser(Integer userId) {
        if (!findById(userId).isPresent()) {
            throw new ResourceNotFoundException("Kullanıcı bulunamadı.");
        }
        userRepository.deleteById(userId);
    }
}