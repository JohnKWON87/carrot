package com.carrot.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "wishlist",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "item_id"})
        })
public class Wishlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wishlist_id")
    private Long id;

    // 찜한 사용자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 찜한 상품
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    // 찜한 날짜
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // JPA 라이프사이클 콜백
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // 기본 생성자
    public Wishlist() {}

    // 생성자
    public Wishlist(User user, Item item) {
        this.user = user;
        this.item = item;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // 편의 메서드
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

    @Override
    public String toString() {
        return "Wishlist{" +
                "id=" + id +
                ", userId=" + (user != null ? user.getId() : "null") +
                ", itemId=" + (item != null ? item.getId() : "null") +
                ", createdAt=" + createdAt +
                '}';
    }


}