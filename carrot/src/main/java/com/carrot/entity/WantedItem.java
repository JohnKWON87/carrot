package com.carrot.entity;

import com.carrot.constant.WantedStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "wanted_items")
public class WantedItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wanted_item_id")
    private Long id;

    @Column(nullable = false, length = 100)
    private String title; // 원하는 상품 제목

    @Column(nullable = false, length = 2000)
    private String description; // 상품 설명/조건

    @Column(nullable = false)
    private Integer maxPrice; // 최대 희망 가격

    @Column(nullable = false, length = 50)
    private String category; // 카테고리 (electronics, clothes, misc)

    @Column(nullable = false, length = 100)
    private String location; // 거래 희망 지역

    // 구매희망 상태 (ACTIVE, MATCHED, CANCELLED)
    @Enumerated(EnumType.STRING)
    @Column(name = "wanted_status", nullable = false)
    private WantedStatus wantedStatus = WantedStatus.ACTIVE;

    // 구매희망자 정보 (User 엔티티와 연결)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", nullable = false)
    private User buyer;

    // 조회수
    @Column(name = "view_count", nullable = false)
    private Integer viewCount = 0;

    // 관심 개수 (나중에 관심 기능 추가 예정)
    @Column(name = "interest_count", nullable = false)
    private Integer interestCount = 0;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // JPA 생명주기 콜백
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // 기본 생성자
    public WantedItem() {}

    // 생성자
    public WantedItem(String title, String description, Integer maxPrice, String category,
                      String location, User buyer) {
        this.title = title;
        this.description = description;
        this.maxPrice = maxPrice;
        this.category = category;
        this.location = location;
        this.buyer = buyer;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getMaxPrice() { return maxPrice; }
    public void setMaxPrice(Integer maxPrice) { this.maxPrice = maxPrice; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public WantedStatus getWantedStatus() { return wantedStatus; }
    public void setWantedStatus(WantedStatus wantedStatus) { this.wantedStatus = wantedStatus; }

    public User getBuyer() { return buyer; }
    public void setBuyer(User buyer) { this.buyer = buyer; }

    public Integer getViewCount() { return viewCount; }
    public void setViewCount(Integer viewCount) { this.viewCount = viewCount; }

    public Integer getInterestCount() { return interestCount; }
    public void setInterestCount(Integer interestCount) { this.interestCount = interestCount; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // 편의 메서드들

    /**
     * 가격을 포맷팅된 문자열로 반환
     */
    public String getFormattedMaxPrice() {
        return String.format("%,d원", this.maxPrice);
    }

    /**
     * 카테고리를 한글명으로 반환
     */
    public String getCategoryName() {
        switch (this.category) {
            case "electronics": return "가전제품";
            case "clothes": return "의류";
            case "misc": return "기타 및 잡화";
            default: return "기타";
        }
    }

    /**
     * 등록일로부터 얼마나 지났는지 계산
     */
    public String getTimeAgo() {
        LocalDateTime now = LocalDateTime.now();
        long minutes = java.time.Duration.between(createdAt, now).toMinutes();

        if (minutes < 1) return "방금 전";
        if (minutes < 60) return minutes + "분 전";

        long hours = minutes / 60;
        if (hours < 24) return hours + "시간 전";

        long days = hours / 24;
        if (days < 30) return days + "일 전";

        long months = days / 30;
        if (months < 12) return months + "개월 전";

        long years = months / 12;
        return years + "년 전";
    }

    /**
     * 구매희망이 활성 상태인지 확인
     */
    public boolean isActive() {
        return wantedStatus == WantedStatus.ACTIVE;
    }

    /**
     * 조회수 증가
     */
    public void incrementViewCount() {
        this.viewCount++;
    }

    /**
     * 관심 개수 증가
     */
    public void incrementInterestCount() {
        this.interestCount++;
    }

    /**
     * 관심 개수 감소
     */
    public void decrementInterestCount() {
        if (this.interestCount > 0) {
            this.interestCount--;
        }
    }

    @Override
    public String toString() {
        return "WantedItem{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", maxPrice=" + maxPrice +
                ", category='" + category + '\'' +
                ", wantedStatus=" + wantedStatus +
                ", buyer=" + (buyer != null ? buyer.getUsername() : "null") +
                '}';
    }
}