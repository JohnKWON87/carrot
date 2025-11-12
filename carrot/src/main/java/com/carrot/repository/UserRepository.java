package com.carrot.repository;

import com.carrot.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // 아이디로 사용자 찾기
    Optional<User> findByUsername(String username);

    // 이메일로 사용자 찾기
    Optional<User> findByEmail(String email);

    // 전화번호로 사용자 찾기
    Optional<User> findByPhone(String phone);

    // 아이디 중복 체크
    boolean existsByUsername(String username);

    // 이메일 중복 체크
    boolean existsByEmail(String email);

    // 전화번호 중복 체크
    boolean existsByPhone(String phone);

    // 활성화된 사용자만 찾기
    Optional<User> findByUsernameAndEnabled(String username, boolean enabled);

    List<User> findAllByOrderByCreatedAtDesc();
    Page<User> findAllByOrderByCreatedAtDesc(Pageable pageable);

    List<User> findByUsernameContainingOrNameContainingOrEmailContaining(
            String username, String name, String email);

    long countByEnabled(boolean enabled);
    long countByRole(String role);

    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :startDate")
    long countNewUsersThisMonth(@Param("startDate") LocalDateTime startDate);
}
