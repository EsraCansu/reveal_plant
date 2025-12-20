package plant_village.service;

import plant_village.model.User;
import plant_village.repository.UserRepository;
import plant_village.exception.ValidationException;
import plant_village.exception.ResourceNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    
    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @Override
    @Transactional
    public User registerNewUser(User user) {
        // 1. Email kontrolü (Primary check - email must be unique)
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new ValidationException("Bu e-posta adresi zaten kayıtlı.");
        }

        // 2. Varsayılan değerler
        user.setCreatedAt(LocalDateTime.now());
        user.setLastLogin(LocalDateTime.now());
        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("USER");
        }
        
        // 3. Şifreleme (Güvenlik Koordinasyonu)
        if (user.getPasswordHash() != null) {
            user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        }
        
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
        // existsByUserName yerine findByUserName üzerinden kontrol daha güvenlidir
        return userRepository.findByUserName(userName).isPresent();
    }
    
    public boolean verifyPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
    
    public BCryptPasswordEncoder getPasswordEncoder() {
        return this.passwordEncoder;
    }
    
    @Override
    public java.util.List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    @Transactional
    public User updateUser(User user) {
        User existingUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı."));

        // Sadece şifre değişmişse tekrar hash'le
        if (user.getPasswordHash() != null && !user.getPasswordHash().isEmpty()) {
            if (!user.getPasswordHash().startsWith("$2a$")) { // BCrypt kontrolü
                user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
            }
        }
        
        // Diğer alanları güncelle (Koordinasyon: Mevcut tarihi koru vb.)
        user.setCreatedAt(existingUser.getCreatedAt()); 
        
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteUser(Integer userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("Kullanıcı bulunamadı.");
        }
        userRepository.deleteById(userId);
    }
}