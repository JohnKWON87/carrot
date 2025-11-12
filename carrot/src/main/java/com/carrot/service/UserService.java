package com.carrot.service;

import com.carrot.entity.User;
import com.carrot.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;  // 이 import 필요
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AdminLogService adminLogService;

    // === 중복 체크 메서드들 ===

    public boolean isUsernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean isEmailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean isPhoneExists(String phone) {
        return userRepository.existsByPhone(phone);
    }

    // === 기존 메서드들 ===

    public User registerUser(User user) throws Exception {
        validateUserInput(user);
        checkDuplicates(user);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("USER");
        user.setEnabled(true);
        return userRepository.save(user);
    }

    public User authenticate(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsernameAndEnabled(username, true);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (passwordEncoder.matches(password, user.getPassword())) {
                return user;
            }
        }
        return null;
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    public User updateUser(User user) throws Exception {
        User existingUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new Exception("사용자를 찾을 수 없습니다."));

        existingUser.setName(user.getName());
        existingUser.setEmail(user.getEmail());
        existingUser.setPhone(user.getPhone());

        return userRepository.save(existingUser);
    }

    public void changePassword(String username, String oldPassword, String newPassword) throws Exception {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new Exception("사용자를 찾을 수 없습니다."));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new Exception("기존 비밀번호가 일치하지 않습니다.");
        }

        validatePassword(newPassword);
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    // === 관리자용 메서드들 ===

    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public Page<User> getUsersWithPaging(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return userRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    public User toggleUserStatus(Long userId) throws Exception {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new Exception("사용자를 찾을 수 없습니다."));

        user.setEnabled(!user.isEnabled());
        return userRepository.save(user);
    }

    public User changeUserRole(Long userId, String newRole) throws Exception {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new Exception("사용자를 찾을 수 없습니다."));

        if (!"USER".equals(newRole) && !"ADMIN".equals(newRole)) {
            throw new Exception("올바르지 않은 역할입니다.");
        }

        user.setRole(newRole);
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public List<User> searchUsers(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllUsers();
        }
        return userRepository.findByUsernameContainingOrNameContainingOrEmailContaining(
                keyword, keyword, keyword);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getUserStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userRepository.count());
        stats.put("activeUsers", userRepository.countByEnabled(true));
        stats.put("adminUsers", userRepository.countByRole("ADMIN"));

        // ✅ 수정: 이번 달 1일 0시 0분을 기준으로 계산
        LocalDateTime startOfMonth = LocalDateTime.now()
                .withDayOfMonth(1)
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);
        stats.put("newUsersThisMonth", userRepository.countNewUsersThisMonth(startOfMonth));

        return stats;
    }

    // === 유효성 검사 메서드들 ===

    private void validateUserInput(User user) throws Exception {
        validateUsername(user.getUsername());
        validatePassword(user.getPassword());
        validateEmail(user.getEmail());
        validatePhone(user.getPhone());
        validateName(user.getName());
    }

    private void validateUsername(String username) throws Exception {
        if (username == null || username.trim().isEmpty()) {
            throw new Exception("아이디를 입력해주세요.");
        }

        if (username.length() < 4 || username.length() > 20) {
            throw new Exception("아이디는 4~20자 사이여야 합니다.");
        }

        if (!Pattern.matches("^[a-zA-Z0-9]+$", username)) {
            throw new Exception("아이디는 영문, 숫자만 사용 가능합니다.");
        }
    }

    private void validatePassword(String password) throws Exception {
        if (password == null || password.trim().isEmpty()) {
            throw new Exception("비밀번호를 입력해주세요.");
        }

        if (password.length() < 8 || password.length() > 20) {
            throw new Exception("비밀번호는 8~20자 사이여야 합니다.");
        }

        boolean hasLetter = Pattern.compile("[a-zA-Z]").matcher(password).find();
        boolean hasDigit = Pattern.compile("\\d").matcher(password).find();
        boolean hasSpecial = Pattern.compile("[!@#$%^&*(),.?\":{}|<>]").matcher(password).find();

        if (!hasLetter || !hasDigit || !hasSpecial) {
            throw new Exception("비밀번호는 영문, 숫자, 특수문자를 모두 포함해야 합니다.");
        }
    }

    private void validateEmail(String email) throws Exception {
        if (email == null || email.trim().isEmpty()) {
            throw new Exception("이메일을 입력해주세요.");
        }

        String emailPattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        if (!Pattern.matches(emailPattern, email)) {
            throw new Exception("올바른 이메일 형식을 입력해주세요.");
        }
    }

    private void validatePhone(String phone) throws Exception {
        if (phone == null || phone.trim().isEmpty()) {
            throw new Exception("전화번호를 입력해주세요.");
        }

        String phonePattern = "^010-\\d{4}-\\d{4}$";
        if (!Pattern.matches(phonePattern, phone)) {
            throw new Exception("전화번호는 010-0000-0000 형식으로 입력해주세요.");
        }
    }

    private void validateName(String name) throws Exception {
        if (name == null || name.trim().isEmpty()) {
            throw new Exception("이름을 입력해주세요.");
        }

        if (name.length() < 2 || name.length() > 10) {
            throw new Exception("이름은 2~10자 사이여야 합니다.");
        }

        if (!Pattern.matches("^[가-힣a-zA-Z\\s]+$", name)) {
            throw new Exception("이름은 한글, 영문만 사용 가능합니다.");
        }
    }

    private void checkDuplicates(User user) throws Exception {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new Exception("이미 사용 중인 아이디입니다.");
        }

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new Exception("이미 사용 중인 이메일입니다.");
        }

        if (userRepository.existsByPhone(user.getPhone())) {
            throw new Exception("이미 사용 중인 전화번호입니다.");
        }
    }

    /**
     * 사용자 추방 (영구 비활성화)
     */
    public User banUser(Long userId, String reason, String adminEmail) throws Exception {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new Exception("사용자를 찾을 수 없습니다."));

        // 관리자는 추방할 수 없음
        if (user.isAdmin()) {
            throw new Exception("관리자는 추방할 수 없습니다.");
        }

        // 이미 비활성화된 사용자 체크
        if (!user.isEnabled()) {
            throw new Exception("이미 비활성화된 사용자입니다.");
        }

        // 사용자 비활성화
        user.setEnabled(false);
        User bannedUser = userRepository.save(user);

        return bannedUser;
    }
}