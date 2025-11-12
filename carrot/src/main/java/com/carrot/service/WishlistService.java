package com.carrot.service;

import com.carrot.entity.Wishlist;
import com.carrot.entity.User;
import com.carrot.entity.Item;
import com.carrot.repository.WishlistRepository;
import com.carrot.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class WishlistService {

    @Autowired
    private WishlistRepository wishlistRepository;

    @Autowired
    private ItemRepository itemRepository;

    /**
     * 찜하기 / 찜 취소 토글
     */
    public boolean toggleWishlist(Long itemId, User user) throws Exception {
        // 상품 조회
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new Exception("상품을 찾을 수 없습니다."));

        // 자신의 상품은 찜할 수 없음
        if (item.getSeller().getId().equals(user.getId())) {
            throw new Exception("자신의 상품은 찜할 수 없습니다.");
        }

        // 기존 찜 정보 조회
        Optional<Wishlist> existingWishlist = wishlistRepository.findByUserAndItem(user, item);

        if (existingWishlist.isPresent()) {
            // 이미 찜한 상품이면 찜 취소
            wishlistRepository.delete(existingWishlist.get());

            // 상품의 찜 개수 감소
            item.decrementWishCount();
            itemRepository.save(item);

            return false; // 찜 취소됨
        } else {
            // 찜하지 않은 상품이면 찜하기
            Wishlist wishlist = new Wishlist(user, item);
            wishlistRepository.save(wishlist);

            // 상품의 찜 개수 증가
            item.incrementWishCount();
            itemRepository.save(item);

            return true; // 찜 추가됨
        }
    }

    /**
     * 찜 여부 확인
     */
    @Transactional(readOnly = true)
    public boolean isWishlisted(Long itemId, User user) {
        try {
            Item item = itemRepository.findById(itemId).orElse(null);
            if (item == null) {
                return false;
            }
            return wishlistRepository.existsByUserAndItem(user, item);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 사용자의 찜 목록 조회
     */
    @Transactional(readOnly = true)
    public List<Wishlist> getUserWishlist(User user) {
        return wishlistRepository.findAvailableWishlistByUser(user);
    }

    /**
     * 사용자의 찜 목록 조회 (페이징)
     */
    @Transactional(readOnly = true)
    public Page<Wishlist> getUserWishlistWithPaging(User user, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return wishlistRepository.findVisibleWishlistByUser(user, pageable);
    }

    /**
     * 사용자의 전체 찜 개수 조회
     */
    @Transactional(readOnly = true)
    public long getUserWishlistCount(User user) {
        return wishlistRepository.countByUser(user);
    }

    /**
     * 카테고리별 찜 목록 조회
     */
    @Transactional(readOnly = true)
    public List<Wishlist> getUserWishlistByCategory(User user, String category) {
        return wishlistRepository.findByUserAndItemCategory(user, category);
    }

    /**
     * 키워드로 찜 목록 검색
     */
    @Transactional(readOnly = true)
    public List<Wishlist> searchUserWishlist(User user, String keyword) {
        return wishlistRepository.searchWishlistByUserAndKeyword(user, keyword);
    }

    /**
     * 가격 범위별 찜 목록 조회
     */
    @Transactional(readOnly = true)
    public List<Wishlist> getUserWishlistByPriceRange(User user, Integer minPrice, Integer maxPrice) {
        return wishlistRepository.findWishlistByUserAndPriceRange(user, minPrice, maxPrice);
    }

    /**
     * 최근 N일간 찜한 상품들 조회
     */
    @Transactional(readOnly = true)
    public List<Wishlist> getRecentUserWishlist(User user, int days) {
        return wishlistRepository.findRecentWishlistByUser(user, days);
    }

    /**
     * 사용자의 찜한 상품 평균 가격 계산
     */
    @Transactional(readOnly = true)
    public Double getUserWishlistAveragePrice(User user) {
        Double avgPrice = wishlistRepository.findAverageWishlistPriceByUser(user);
        return avgPrice != null ? avgPrice : 0.0;
    }

    /**
     * 특정 상품의 찜 개수 조회
     */
    @Transactional(readOnly = true)
    public long getItemWishlistCount(Long itemId) {
        try {
            Item item = itemRepository.findById(itemId).orElse(null);
            if (item == null) {
                return 0;
            }
            return wishlistRepository.countByItem(item);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 찜 목록에서 특정 상품 제거
     */
    public void removeFromWishlist(Long itemId, User user) throws Exception {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new Exception("상품을 찾을 수 없습니다."));

        Optional<Wishlist> wishlist = wishlistRepository.findByUserAndItem(user, item);
        if (wishlist.isPresent()) {
            wishlistRepository.delete(wishlist.get());

            // 상품의 찜 개수 감소
            item.decrementWishCount();
            itemRepository.save(item);
        } else {
            throw new Exception("찜하지 않은 상품입니다.");
        }
    }

    /**
     * 인기 상품 통계 조회 (찜 개수 기준)
     */
    @Transactional(readOnly = true)
    public List<Object[]> getItemWishStatistics() {
        return wishlistRepository.findItemWishStatistics();
    }
}