package com.carrot.entity;

import com.carrot.constant.ItemSellStatus;
import com.carrot.constant.ModerationStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "items")
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long id;

    @Column(nullable = false, length = 100)
    private String title; // 상품 제목

    @Column(nullable = false, length = 2000)
    private String description; // 상품 설명

    @Column(nullable = false)
    private Integer price; // 상품 가격

    @Column(nullable = false, length = 50)
    private String category; // 카테고리 (electronics, clothes, misc)

    @Column(nullable = false, length = 100)
    private String location; // 거래 지역

    // 판매 상태 (SELL, BUY, RESERVED, SOLD_OUT)
    @Enumerated(EnumType.STRING)
    @Column(name = "sell_status", nullable = false)
    private ItemSellStatus sellStatus = ItemSellStatus.SELL;

    // 관리자 검토 상태 (VISIBLE, BLINDED, DELETED)
    @Enumerated(EnumType.STRING)
    @Column(name = "moderation_status", nullable = false)
    private ModerationStatus moderationStatus = ModerationStatus.VISIBLE;

    // 판매자 정보 (User 엔티티와 연결)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    // 조회수
    @Column(name = "view_count", nullable = false)
    private Integer viewCount = 0;

    // 찜 개수 (나중에 Wishlist와 연동)
    @Column(name = "wish_count", nullable = false)
    private Integer wishCount = 0;

    // 대표 이미지 URL (나중에 다중 이미지 지원 예정)
    @Column(name = "image_url", length = 500)
    private String imageUrl;

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
    public Item() {}

    // 생성자
    public Item(String title, String description, Integer price, String category,
                String location, User seller) {
        this.title = title;
        this.description = description;
        this.price = price;
        this.category = category;
        this.location = location;
        this.seller = seller;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getPrice() { return price; }
    public void setPrice(Integer price) { this.price = price; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public ItemSellStatus getSellStatus() { return sellStatus; }
    public void setSellStatus(ItemSellStatus sellStatus) { this.sellStatus = sellStatus; }

    public ModerationStatus getModerationStatus() { return moderationStatus; }
    public void setModerationStatus(ModerationStatus moderationStatus) {
        this.moderationStatus = moderationStatus;
    }

    public User getSeller() { return seller; }
    public void setSeller(User seller) { this.seller = seller; }

    public Integer getViewCount() { return viewCount; }
    public void setViewCount(Integer viewCount) { this.viewCount = viewCount; }

    public Integer getWishCount() { return wishCount; }
    public void setWishCount(Integer wishCount) { this.wishCount = wishCount; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // 편의 메서드들

    /**
     * 가격을 포맷팅된 문자열로 반환
     */
    public String getFormattedPrice() {
        return String.format("%,d원", this.price);
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
     * 판매 가능한 상태인지 확인
     */
    public boolean isAvailableForSale() {
        return sellStatus == ItemSellStatus.SELL &&
                moderationStatus == ModerationStatus.VISIBLE;
    }

    /**
     * 관리자에 의해 숨겨진 상태인지 확인
     */
    public boolean isBlocked() {
        return moderationStatus == ModerationStatus.BLINDED ||
                moderationStatus == ModerationStatus.DELETED;
    }

    /**
     * 조회수 증가
     */
    public void incrementViewCount() {
        this.viewCount++;
    }

    /**
     * 찜 개수 증가
     */
    public void incrementWishCount() {
        this.wishCount++;
    }

    /**
     * 찜 개수 감소
     */
    public void decrementWishCount() {
        if (this.wishCount > 0) {
            this.wishCount--;
        }
    }

    @Override
    public String toString() {
        return "Item{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", price=" + price +
                ", category='" + category + '\'' +
                ", sellStatus=" + sellStatus +
                ", seller=" + (seller != null ? seller.getUsername() : "null") +
                '}';
    }
}