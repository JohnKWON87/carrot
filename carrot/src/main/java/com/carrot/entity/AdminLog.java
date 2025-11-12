package com.carrot.entity;

import com.carrot.constant.ModerationStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "admin_log")
@Getter
@Setter
@ToString
public class AdminLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "admin_log_id")
    private Long id;

    // 관리 대상 아이템 ID (나중에 Item 엔티티와 연결될 FK)
    @Column(name = "item_id", nullable = false)
    private Long itemId;

    // 현재 관리 상태
    @Enumerated(EnumType.STRING)
    @Column(name = "moderation_status", nullable = false)
    private ModerationStatus moderationStatus = ModerationStatus.VISIBLE;

    // 관리 사유 (블라인드/삭제 이유)
    @Column(name = "moderation_reason", length = 500)
    private String moderationReason;

    // 처리한 관리자 (임시로 String 사용, 나중에 UserAccount로 변경)
    @Column(name = "moderator_email", nullable = false)
    private String moderatorEmail;


    // 관리 처리 시각
    @Column(name = "moderated_at", nullable = false)
    private LocalDateTime moderatedAt;

    // 생성 시각
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // 수정 시각
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 생성 시 자동으로 시간 설정
    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.moderatedAt = now;
    }

    // 업데이트 시 자동으로 시간 설정
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
        this.moderatedAt = LocalDateTime.now();
    }

    // 편의 메소드: 블라인드 처리 (임시 버전)
    public void blindWithReason(String reason, String moderatorEmail) {
        this.moderationStatus = ModerationStatus.BLINDED;
        this.moderationReason = reason;
        this.moderatorEmail = moderatorEmail;
        this.moderatedAt = LocalDateTime.now();
    }

    // 편의 메소드: 삭제 처리 (임시 버전)
    public void deleteWithReason(String reason, String moderatorEmail) {
        this.moderationStatus = ModerationStatus.DELETED;
        this.moderationReason = reason;
        this.moderatorEmail = moderatorEmail;
        this.moderatedAt = LocalDateTime.now();
    }

    // 편의 메소드: 복원 처리 (임시 버전)
    public void restore(String moderatorEmail) {
        this.moderationStatus = ModerationStatus.VISIBLE;
        this.moderationReason = "복원됨";
        this.moderatorEmail = moderatorEmail;
        this.moderatedAt = LocalDateTime.now();
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }


}