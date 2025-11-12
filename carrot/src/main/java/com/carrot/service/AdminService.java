package com.carrot.service;

import com.carrot.entity.AdminLog;
import com.carrot.constant.ModerationStatus;
// import com.example.user.UserAccount; // 팀원 코드 받으면 주석 해제 예정
// import com.example.user.Role; // 팀원 코드 받으면 주석 해제 예정
import com.carrot.entity.Item;
import com.carrot.repository.AdminLogRepository;
import com.carrot.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 관리자 기능을 담당하는 서비스 클래스
 * - 부적절한 콘텐츠 필터링
 * - 아이템 블라인드/삭제/복원 처리
 * - 관리자 권한 확인
 */
@Service
@Transactional // 모든 메소드가 트랜잭션 안에서 실행됨
public class AdminService {

    @Autowired
    private ItemRepository itemRepository;  // 이 줄 추가

    @Autowired
    private AdminLogRepository adminLogRepository;  // 이 줄 추가

    /**
     * 부적절한 단어 목록
     * TODO: 실제 운영 시에는 DB 테이블이나 외부 설정 파일로 관리 예정
     * TODO: 더 많은 금지어, 정규식 패턴, AI 기반 필터링 등으로 확장 가능
     */
    private final List<String> INAPPROPRIATE_WORDS = Arrays.asList(
            "욕설1", "욕설2", "사기", "도둑", "가짜"
            // 실제 운영시에는 더 많은 단어들...
    );

    /**
     * 사용자가 관리자 권한을 가지고 있는지 확인 (임시 버전)
     *
     * @param userEmail 확인할 사용자 이메일
     * @return 관리자이면 true, 아니면 false
     *
     * TODO: 팀원의 UserAccount 엔티티 받으면 이렇게 변경 예정:
     * public boolean isAdmin(UserAccount user) {
     *     return user.getRoles().contains(Role.ROLE_ADMIN);
     * }
     */
    public boolean isAdmin(String userEmail) {
        // 임시로 하드코딩된 관리자 이메일 목록으로 체크
        List<String> adminEmails = Arrays.asList("admin@carrot.com", "manager@carrot.com");
        return adminEmails.contains(userEmail);
    }

    /**
     * 주어진 텍스트에 부적절한 단어가 포함되어 있는지 확인
     *
     * @param content 검사할 텍스트 내용
     * @return 부적절한 단어가 있으면 true, 없으면 false
     */
    public boolean containsInappropriateContent(String content) {
        // null이나 빈 문자열이면 문제없음
        if (content == null || content.trim().isEmpty()) {
            return false;
        }

        // 대소문자 구분 없이 검사하기 위해 소문자로 변환
        String lowerContent = content.toLowerCase();

        // 금지어 목록 중 하나라도 포함되어 있으면 true 반환
        return INAPPROPRIATE_WORDS.stream()
                .anyMatch(word -> lowerContent.contains(word.toLowerCase()));
    }

    /**
     * 텍스트에서 발견된 부적절한 단어들을 찾아서 목록으로 반환
     *
     * @param content 검사할 텍스트 내용
     * @return 발견된 부적절한 단어들의 목록
     */
    public List<String> findInappropriateWords(String content) {
        // null이나 빈 문자열이면 빈 목록 반환
        if (content == null || content.trim().isEmpty()) {
            return Arrays.asList();
        }

        String lowerContent = content.toLowerCase();

        // 금지어 목록에서 실제로 포함된 단어들만 필터링해서 반환
        return INAPPROPRIATE_WORDS.stream()
                .filter(word -> lowerContent.contains(word.toLowerCase()))
                .toList(); // Java 16+ 문법, 이전 버전은 .collect(Collectors.toList()) 사용
    }

    /**
     * 특정 아이템을 블라인드(숨김) 처리
     * - 사용자에게는 보이지 않지만 완전 삭제는 아님
     * - 관리자만 다시 복원 가능
     *
     * @param itemId 블라인드할 아이템 ID
     * @param reason 블라인드 처리 사유
     * @param moderatorEmail 처리하는 관리자의 이메일
     * @return 생성된 AdminLog 엔티티
     * @throws IllegalArgumentException 관리자 권한이 없는 경우
     */
    public AdminLog blindItem(Long itemId, String reason, String moderatorEmail) {
        // 관리자 권한 확인 - 권한이 없으면 예외 발생
        if (!isAdmin(moderatorEmail)) {
            throw new IllegalArgumentException("관리자 권한이 필요합니다.");
        }

        // 새로운 관리 로그 생성
        AdminLog adminLog = new AdminLog();
        adminLog.setItemId(itemId);
        adminLog.blindWithReason(reason, moderatorEmail); // 블라인드 상태로 설정

        // TODO: 실제로는 AdminLogRepository.save(adminLog)로 DB에 저장
        // TODO: 또한 실제 Item 엔티티의 상태도 업데이트 필요
        return adminLog;
    }

    /**
     * 특정 아이템을 삭제 처리
     * - 블라인드보다 더 강한 조치
     * - 완전 삭제는 아니지만 복원이 더 어려움
     *
     * @param itemId 삭제할 아이템 ID
     * @param reason 삭제 처리 사유
     * @param moderatorEmail 처리하는 관리자의 이메일
     * @return 생성된 AdminLog 엔티티
     */
    public AdminLog deleteItem(Long itemId, String reason, String moderatorEmail) throws Exception {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new Exception("상품을 찾을 수 없습니다."));

        // 상태를 DELETED로 변경 (DB에서 제거하지 않음)
        item.setModerationStatus(ModerationStatus.DELETED);
        itemRepository.save(item);

        // 로그 기록
        AdminLog log = new AdminLog();
        log.setItemId(itemId);
        log.setModerationStatus(ModerationStatus.DELETED);
        log.setModerationReason(reason);
        log.setModeratorEmail(moderatorEmail);
        log.setModeratedAt(LocalDateTime.now());

        return adminLogRepository.save(log);
    }

    // 완전 삭제를 원한다면 별도 메서드 추가:
    public void permanentlyDeleteItem(Long itemId, String reason, String moderatorEmail) throws Exception {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new Exception("상품을 찾을 수 없습니다."));

        // 로그 먼저 기록
        AdminLog log = new AdminLog();
        log.setItemId(itemId);
        log.setModerationStatus(ModerationStatus.DELETED);
        log.setModerationReason("영구 삭제: " + reason);
        log.setModeratorEmail(moderatorEmail);
        log.setModeratedAt(LocalDateTime.now());
        adminLogRepository.save(log);

        // DB에서 완전 제거
        itemRepository.delete(item);
    }

    /**
     * 블라인드되거나 삭제된 아이템을 복원
     * - VISIBLE 상태로 되돌림
     *
     * @param itemId 복원할 아이템 ID
     * @param moderatorEmail 처리하는 관리자의 이메일
     * @return 생성된 AdminLog 엔티티
     */
    public AdminLog restoreItem(Long itemId, String moderatorEmail) {
        // 관리자 권한 확인
        if (!isAdmin(moderatorEmail)) {
            throw new IllegalArgumentException("관리자 권한이 필요합니다.");
        }

        AdminLog adminLog = new AdminLog();
        adminLog.setItemId(itemId);
        adminLog.restore(moderatorEmail); // VISIBLE 상태로 복원

        return adminLog;
    }

    /**
     * ⭐ 자동 필터링의 핵심 메소드 ⭐
     * 아이템의 제목과 내용을 자동으로 검사해서 부적절한 내용이 있으면 자동 블라인드
     *
     * 사용 시나리오:
     * 1. 사용자가 새 글 작성 시
     * 2. 기존 글 수정 시
     * 3. 정기적인 배치 검사 시
     *
     * @param itemId 검사할 아이템 ID
     * @param title 아이템 제목
     * @param content 아이템 내용/설명
     * @param systemAdminEmail 시스템 관리자 이메일 (자동 처리용)
     * @return 문제가 발견되어 블라인드된 경우 AdminLog, 문제없으면 null
     */
    public AdminLog autoModerateContent(Long itemId, String title, String content, String systemAdminEmail) {
        // 제목과 내용을 합쳐서 부적절한 단어 검사, autoModerateContent() 메서드가 자동으로 부적절한 단어 검사 , 발견시 블라인드 처리
        List<String> inappropriateWords = findInappropriateWords(title + " " + content);

        // 부적절한 단어가 발견된 경우
        if (!inappropriateWords.isEmpty()) {
            // 발견된 단어들을 포함한 사유 메시지 생성
            String reason = "부적절한 단어 감지: " + String.join(", ", inappropriateWords);

            // 자동으로 블라인드 처리
            return blindItem(itemId, reason, systemAdminEmail);
        }

        // 문제없음 - null 반환
        return null;
    }
}