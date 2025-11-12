package com.carrot.controller;

import com.carrot.service.AdminService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;

/**
 * AdminController의 통합 테스트
 * - REST API 엔드포인트 테스트
 * - HTTP 요청/응답 테스트
 * - 예외 상황 처리 테스트
 */
@WebMvcTest(AdminController.class)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminService adminService;

    @Autowired
    private ObjectMapper objectMapper;

    // ========================================
    // 관리자 권한 확인 API 테스트
    // ========================================

    @Test
    @DisplayName("관리자 권한 확인 API - 관리자 성공")
    void checkAdminAuth_ValidAdmin_ReturnsSuccess() throws Exception {
        // Given
        String adminEmail = "admin@carrot.com";
        when(adminService.isAdmin(adminEmail)).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/api/admin/check-auth")
                        .param("email", adminEmail))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isAdmin").value(true))
                .andExpect(jsonPath("$.email").value(adminEmail))
                .andExpect(jsonPath("$.message").value("관리자 권한 있음"));

        verify(adminService).isAdmin(adminEmail);
    }

    @Test
    @DisplayName("관리자 권한 확인 API - 일반 사용자")
    void checkAdminAuth_RegularUser_ReturnsFalse() throws Exception {
        // Given
        String userEmail = "user@carrot.com";
        when(adminService.isAdmin(userEmail)).thenReturn(false);

        // When & Then
        mockMvc.perform(get("/api/admin/check-auth")
                        .param("email", userEmail))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isAdmin").value(false))
                .andExpect(jsonPath("$.message").value("관리자 권한 없음"));
    }

    // ========================================
    // 아이템 블라인드 처리 API 테스트
    // ========================================

    @Test
    @DisplayName("아이템 블라인드 API - 성공")
    void blindItem_ValidRequest_ReturnsSuccess() throws Exception {
        // Given
        Long itemId = 1L;
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("reason", "부적절한 내용");
        requestBody.put("moderatorEmail", "admin@carrot.com");

        // AdminService.blindItem() 모킹은 복잡하므로 생략
        // 실제로는 MockBean으로 AdminLog를 반환하도록 설정

        // When & Then
        mockMvc.perform(post("/api/admin/items/{itemId}/blind", itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("아이템이 블라인드 처리되었습니다."))
                .andExpect(jsonPath("$.itemId").value(itemId));
    }

    @Test
    @DisplayName("아이템 블라인드 API - 사유 없음으로 실패")
    void blindItem_NoReason_ReturnsBadRequest() throws Exception {
        // Given
        Long itemId = 1L;
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("reason", ""); // 빈 사유
        requestBody.put("moderatorEmail", "admin@carrot.com");

        // When & Then
        mockMvc.perform(post("/api/admin/items/{itemId}/blind", itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("블라인드 사유를 입력해주세요."));
    }

    @Test
    @DisplayName("아이템 블라인드 API - 권한 없음으로 실패")
    void blindItem_NoPermission_ReturnsForbidden() throws Exception {
        // Given
        Long itemId = 1L;
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("reason", "부적절한 내용");
        requestBody.put("moderatorEmail", "user@carrot.com");

        when(adminService.blindItem(eq(itemId), eq("부적절한 내용"), eq("user@carrot.com")))
                .thenThrow(new IllegalArgumentException("관리자 권한이 필요합니다."));

        // When & Then
        mockMvc.perform(post("/api/admin/items/{itemId}/blind", itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("관리자 권한이 필요합니다."));
    }

    // ========================================
    // 부적절한 단어 검사 API 테스트
    // ========================================

    @Test
    @DisplayName("부적절한 단어 검사 API - 문제 있는 내용")
    void checkContent_WithBadWords_ReturnsDetected() throws Exception {
        // Given
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("title", "판매합니다");
        requestBody.put("content", "이것은 가짜 상품입니다");

        when(adminService.containsInappropriateContent(anyString())).thenReturn(true);
        when(adminService.findInappropriateWords(anyString())).thenReturn(Arrays.asList("가짜"));

        // When & Then
        mockMvc.perform(post("/api/admin/check-content")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hasInappropriateContent").value(true))
                .andExpect(jsonPath("$.inappropriateWords").isArray())
                .andExpect(jsonPath("$.inappropriateWords[0]").value("가짜"))
                .andExpect(jsonPath("$.message").value("부적절한 단어가 감지되었습니다: 가짜"));
    }

    @Test
    @DisplayName("부적절한 단어 검사 API - 정상 내용")
    void checkContent_WithCleanContent_ReturnsClean() throws Exception {
        // Given
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("title", "좋은 상품");
        requestBody.put("content", "정말 좋습니다");

        when(adminService.containsInappropriateContent(anyString())).thenReturn(false);
        when(adminService.findInappropriateWords(anyString())).thenReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(post("/api/admin/check-content")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hasInappropriateContent").value(false))
                .andExpect(jsonPath("$.inappropriateWords").isEmpty())
                .andExpect(jsonPath("$.message").value("검사 완료: 문제없음"));
    }

    // ========================================
    // 자동 필터링 API 테스트
    // ========================================

    @Test
    @DisplayName("자동 필터링 API - 문제 감지하여 블록")
    void autoModerate_WithBadContent_ReturnsBlocked() throws Exception {
        // Given
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("itemId", "1");
        requestBody.put("title", "판매");
        requestBody.put("content", "사기 상품");

        // AdminLog 모킹 (실제로는 더 복잡)
        // when(adminService.autoModerateContent(...)).thenReturn(adminLog);

        // When & Then
        mockMvc.perform(post("/api/admin/auto-moderate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andDo(print());
        // 응답 검증은 실제 AdminLog 모킹 후 추가
    }

    // ========================================
    // 통계 조회 API 테스트
    // ========================================

    @Test
    @DisplayName("관리 통계 조회 API")
    void getStats_ReturnsStats() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/admin/stats"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stats").exists())
                .andExpect(jsonPath("$.message").value("통계 조회 완료"));
    }
}