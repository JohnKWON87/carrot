package com.carrot.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "agreements")
public class Agreement {

    @Id
    @Column(name="id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "userid")
    private Long userId;

    @Column(name = "productid")
    private Long productId;

    @Column(name = "price")
    private Integer price;

    @NotBlank
    @Column(nullable = false, length = 100)
    private String buyer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private com.carrot.entity.DealMethod method = com.carrot.entity.DealMethod.DIRECT;

    @Column(name = "place", length=255)
    private String place;

    @Column(name = "direct_time", length=32)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime directTime;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Column(name = "direct_date")
    private LocalDate directDate;

    @Column(name = "address", length=100)
    private String address;

    @Column(name = "receiver", length=50)
    private String receiver;

    @Column(name = "phone", length=50)
    private String phone;

    /** 추가 요청 메모 */
    @Column(name ="note", length = 1000)
    private String note; // [ADD] HTML: *{note}

    public Agreement() {}

    // [FIX] 컨트롤러에서 saved.getId()를 사용할 수 있도록 추가
    // getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public Integer getPrice() { return price; }
    public void setPrice(Integer price) { this.price = price; }

    public String getBuyer() { return buyer; }
    public void setBuyer(String buyer) { this.buyer = buyer; }

    public com.carrot.entity.DealMethod getMethod() { return method; }
    public void setMethod(com.carrot.entity.DealMethod method) { this.method = method; }

    // public DirectInfo getDirectInfo() { return directInfo; }
    // public void setDirectInfo(DirectInfo directInfo) { this.directInfo = directInfo; }

    public String getPlace() { return place; }
    public void setPlace(String place) { this.place = place; }

    public LocalDateTime getDirectTime() { return directTime; }
    public void setDirectTime(LocalDateTime directTime) { this.directTime = directTime; }

    public LocalDate getDirectDate() { return directDate; }
    public void setDirectDate(LocalDate directDate) { this.directDate = directDate; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getReceiver() { return receiver; }
    public void setReceiver(String receiver) { this.receiver = receiver; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    // public DirectInfo getDirectInfo() { return directInfo; }
    // public void setDirectInfo(DirectInfo directInfo) { this.directInfo = directInfo; }

    // public ShippingInfo getShippingInfo() { return shippingInfo; }
    // public void setShippingInfo(ShippingInfo shippingInfo) { this.shippingInfo = shippingInfo; }

}
