package com.carrot.config;

import com.carrot.entity.User;
import com.carrot.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Order(1) // 사용자 생성을 먼저 실행
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        createAdminAccount();
    }

    private void createAdminAccount() {
        // 관리자 계정이 이미 존재하는지 확인
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin1234!"));
            admin.setName("관리자");
            admin.setEmail("admin@carrot.com");
            admin.setPhone("010-0000-0000");
            admin.setRole("ADMIN");
            admin.setEnabled(true);

            userRepository.save(admin);
            System.out.println("===============================================");
            System.out.println("관리자 계정이 생성되었습니다:");
            System.out.println("   아이디: admin");
            System.out.println("   비밀번호: admin1234!");
            System.out.println("===============================================");
        }

        // 테스트용 일반 사용자 계정도 생성 (선택사항)
        if (!userRepository.existsByUsername("testuser")) {
            User testUser = new User();
            testUser.setUsername("testuser");
            testUser.setPassword(passwordEncoder.encode("test1234!"));
            testUser.setName("테스트사용자");
            testUser.setEmail("test@carrot.com");
            testUser.setPhone("010-1111-1111");
            testUser.setRole("USER");
            testUser.setEnabled(true);

            userRepository.save(testUser);
            System.out.println("===============================================");
            System.out.println("테스트 사용자 계정이 생성되었습니다:");
            System.out.println("   아이디: testuser");
            System.out.println("   비밀번호: test1234!");
            System.out.println("===============================================");
        }
    }
}