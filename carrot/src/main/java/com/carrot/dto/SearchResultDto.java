package com.carrot.dto;

/**
 * 검색 결과 DTO
 * 검색 결과를 화면에 표시하기 위한 데이터 전송 객체
 */
public class SearchResultDto {
    private String id;
    private String title;
    private String description;
    private int price;
    private String category;
    private String location;
    private String timeAgo;
    private String imageUrl;
    private boolean isLiked; // 찜 여부 (나중에 추가 기능용)
    private int viewCount;   // 조회수 (나중에 추가 기능용)

    // 기본 생성자
    public SearchResultDto() {}

    // 전체 매개변수 생성자
    public SearchResultDto(String id, String title, String description, int price,
                           String category, String location, String timeAgo, String imageUrl) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.price = price;
        this.category = category;
        this.location = location;
        this.timeAgo = timeAgo;
        this.imageUrl = imageUrl;
        this.isLiked = false;
        this.viewCount = 0;
    }

    // 확장 생성자 (나중에 더 많은 정보가 필요할 때)
    public SearchResultDto(String id, String title, String description, int price,
                           String category, String location, String timeAgo, String imageUrl,
                           boolean isLiked, int viewCount) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.price = price;
        this.category = category;
        this.location = location;
        this.timeAgo = timeAgo;
        this.imageUrl = imageUrl;
        this.isLiked = isLiked;
        this.viewCount = viewCount;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getTimeAgo() {
        return timeAgo;
    }

    public void setTimeAgo(String timeAgo) {
        this.timeAgo = timeAgo;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean isLiked() {
        return isLiked;
    }

    public void setLiked(boolean liked) {
        isLiked = liked;
    }

    public int getViewCount() {
        return viewCount;
    }

    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }

    // 편의 메서드들

    /**
     * 가격을 포맷팅된 문자열로 반환
     * @return 쉼표가 포함된 가격 문자열 (예: "1,000,000원")
     */
    public String getFormattedPrice() {
        return String.format("%,d원", this.price);
    }

    /**
     * 카테고리를 한글명으로 반환
     * @return 한글 카테고리명
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
     * 제목이 너무 길 경우 축약
     * @param maxLength 최대 길이
     * @return 축약된 제목
     */
    public String getTruncatedTitle(int maxLength) {
        if (this.title.length() <= maxLength) {
            return this.title;
        }
        return this.title.substring(0, maxLength) + "...";
    }

    /**
     * 설명이 너무 길 경우 축약
     * @param maxLength 최대 길이
     * @return 축약된 설명
     */
    public String getTruncatedDescription(int maxLength) {
        if (this.description.length() <= maxLength) {
            return this.description;
        }
        return this.description.substring(0, maxLength) + "...";
    }

    @Override
    public String toString() {
        return "SearchResultDto{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", price=" + price +
                ", category='" + category + '\'' +
                ", location='" + location + '\'' +
                ", timeAgo='" + timeAgo + '\'' +
                '}';
    }
}