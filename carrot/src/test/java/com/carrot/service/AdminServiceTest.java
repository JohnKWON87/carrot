package com.carrot.service;

import com.carrot.entity.AdminLog;
import com.carrot.constant.ModerationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * AdminService의 단위 테스트
 * - 부적절한 단어 필터링 테스트
 * - 관리자 권한 확인 테스트
 * - 아이템 관리 기능 테스트
 */
@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties") // 테스트용 설정
class AdminServiceTest {

    private AdminService adminService;

    @BeforeEach
    void setUp() {
        adminService = new AdminService();
    }

    // ========================================
    // 관리자 권한 테스트
    // ========================================

    @Test
    @DisplayName("관리자 이메일로 권한 확인 - 성공")
    void isAdmin_ValidAdminEmail_ReturnsTrue() {
        // Given
        String adminEmail = "admin@carrot.com";

        // When
        boolean result = adminService.isAdmin(adminEmail);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("일반 사용자 이메일로 권한 확인 - 실패")
    void isAdmin_RegularUserEmail_ReturnsFalse() {
        // Given
        String userEmail = "user@carrot.com";

        // When
        boolean result = adminService.isAdmin(userEmail);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("null 이메일로 권한 확인 - 실패")
    void isAdmin_NullEmail_ReturnsFalse() {
        // When
        boolean result = adminService.isAdmin(null);

        // Then
        assertThat(result).isFalse();
    }

    // ========================================
    // 부적절한 단어 필터링 테스트
    // ========================================

    @Test
    @DisplayName("부적절한 단어 포함된 텍스트 - 감지 성공")
    void containsInappropriateContent_WithBadWords_ReturnsTrue() {
        // Given
        String content = "이것은 사기 상품입니다";

        // When
        boolean result = adminService.containsInappropriateContent(content);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("정상 텍스트 - 감지 실패")
    void containsInappropriateContent_WithCleanContent_ReturnsFalse() {
        // Given
        String content = "좋은 상품을 판매합니다";

        // When
        boolean result = adminService.containsInappropriateContent(content);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("빈 문자열 - 감지 실패")
    void containsInappropriateContent_WithEmptyString_ReturnsFalse() {
        // Given
        String content = "";

        // When
        boolean result = adminService.containsInappropriateContent(content);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("null 입력 - 감지 실패")
    void containsInappropriateContent_WithNull_ReturnsFalse() {
        // When
        boolean result = adminService.containsInappropriateContent(null);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("부적절한 단어 목록 반환 테스트")
    void findInappropriateWords_WithBadWords_ReturnsWordList() {
        // Given
        String content = "이것은 사기이고 가짜 상품입니다";

        // When
        List<String> result = adminService.findInappropriateWords(content);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).contains("사기", "가짜");
    }

    @Test
    @DisplayName("정상 텍스트에서 부적절한 단어 목록 - 빈 리스트")
    void findInappropriateWords_WithCleanContent_ReturnsEmptyList() {
        // Given
        String content = "좋은 상품입니다";

        // When
        List<String> result = adminService.findInappropriateWords(content);

        // Then
        assertThat(result).isEmpty();
    }

    // ========================================
    // 아이템 관리 기능 테스트
    // ========================================

    @Test
    @DisplayName("관리자가 아이템 블라인드 처리 - 성공")
    void blindItem_WithAdminUser_Success() {
        // Given
        Long itemId = 1L;
        String reason = "부적절한 내용";
        String adminEmail = "admin@carrot.com";

        // When
        AdminLog result = adminService.blindItem(itemId, reason, adminEmail);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getItemId()).isEqualTo(itemId);
        assertThat(result.getModerationStatus()).isEqualTo(ModerationStatus.BLINDED);
        assertThat(result.getModerationReason()).isEqualTo(reason);
        assertThat(result.getModeratorEmail()).isEqualTo(adminEmail);
    }

    @Test
    @DisplayName("일반 사용자가 아이템 블라인드 시도 - 실패")
    void blindItem_WithRegularUser_ThrowsException() {
        // Given
        Long itemId = 1L;
        String reason = "부적절한 내용";
        String userEmail = "user@carrot.com";

        // When & Then
        assertThatThrownBy(() -> adminService.blindItem(itemId, reason, userEmail))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("관리자 권한이 필요합니다.");
    }

    @Test
    @DisplayName("관리자가 아이템 삭제 처리 - 성공")
    void deleteItem_WithAdminUser_Success() throws Exception {
        // Given
        Long itemId = 2L;
        String reason = "스팸";
        String adminEmail = "manager@carrot.com";

        // When
        AdminLog result = adminService.deleteItem(itemId, reason, adminEmail);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getItemId()).isEqualTo(itemId);
        assertThat(result.getModerationStatus()).isEqualTo(ModerationStatus.DELETED);
        assertThat(result.getModerationReason()).isEqualTo(reason);
    }

    @Test
    @DisplayName("관리자가 아이템 복원 처리 - 성공")
    void restoreItem_WithAdminUser_Success() {
        // Given
        Long itemId = 3L;
        String adminEmail = "admin@carrot.com";

        // When
        AdminLog result = adminService.restoreItem(itemId, adminEmail);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getItemId()).isEqualTo(itemId);
        assertThat(result.getModerationStatus()).isEqualTo(ModerationStatus.VISIBLE);
        assertThat(result.getModerationReason()).isEqualTo("복원됨");
    }

    // ========================================
    // 자동 필터링 테스트
    // ========================================

    @Test
    @DisplayName("자동 필터링 - 부적절한 단어 감지하여 블라인드")
    void autoModerateContent_WithBadWords_ReturnsAdminLog() {
        // Given
        Long itemId = 4L;
        String title = "판매합니다";
        String content = "이것은 가짜 상품입니다";
        String systemEmail = "system@carrot.com";

        // When
        AdminLog result = adminService.autoModerateContent(itemId, title, content, systemEmail);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getItemId()).isEqualTo(itemId);
        assertThat(result.getModerationStatus()).isEqualTo(ModerationStatus.BLINDED);
        assertThat(result.getModerationReason()).contains("부적절한 단어 감지");
        assertThat(result.getModerationReason()).contains("가짜");
    }

    @Test
    @DisplayName("자동 필터링 - 정상 내용으로 통과")
    void autoModerateContent_WithCleanContent_ReturnsNull() {
        // Given
        Long itemId = 5L;
        String title = "좋은 상품 판매";
        String content = "정말 좋은 상품입니다";
        String systemEmail = "system@carrot.com";

        // When
        AdminLog result = adminService.autoModerateContent(itemId, title, content, systemEmail);

        // Then
        assertThat(result).isNull(); // 문제없으면 null 반환
    }

    @Test
    @DisplayName("자동 필터링 - 시스템 관리자 권한 없음으로 실패")
    void autoModerateContent_WithNonSystemUser_ThrowsException() {
        // Given
        Long itemId = 6L;
        String title = "판매합니다";
        String content = "사기 상품";
        String nonSystemEmail = "user@carrot.com"; // 관리자 아님

        // When & Then
        assertThatThrownBy(() ->
                adminService.autoModerateContent(itemId, title, content, nonSystemEmail))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("관리자 권한이 필요합니다.");
    }
}