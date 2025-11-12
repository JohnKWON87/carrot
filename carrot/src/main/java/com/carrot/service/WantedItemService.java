package com.carrot.service;

import com.carrot.entity.WantedItem;
import com.carrot.entity.User;
import com.carrot.repository.WantedItemRepository;
import com.carrot.constant.WantedStatus;
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
public class WantedItemService {

    @Autowired
    private WantedItemRepository wantedItemRepository;

    @Autowired
    private AdminService adminService;

    /**
     * 구매희망상품 등록
     */
    public WantedItem registerWantedItem(WantedItem wantedItem) throws Exception {
        try {
            // 입력 검증
            validateWantedItem(wantedItem);

            // 저장
            WantedItem savedWantedItem = wantedItemRepository.save(wantedItem);

            // 자동 필터링 검사
            adminService.autoModerateContent(
                    savedWantedItem.getId(),
                    savedWantedItem.getTitle(),
                    savedWantedItem.getDescription(),
                    "system@carrot.com"
            );

            return savedWantedItem;

        } catch (Exception e) {
            throw new Exception("구매희망상품 등록 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 구매희망상품 수정
     */
    public WantedItem updateWantedItem(Long wantedItemId, WantedItem updatedWantedItem, User currentUser) throws Exception {
        WantedItem existingWantedItem = wantedItemRepository.findById(wantedItemId)
                .orElseThrow(() -> new Exception("구매희망상품을 찾을 수 없습니다."));

        // 수정 권한 확인
        if (!existingWantedItem.getBuyer().getId().equals(currentUser.getId())) {
            throw new Exception("구매희망상품을 수정할 권한이 없습니다.");
        }

        // 필드 업데이트
        existingWantedItem.setTitle(updatedWantedItem.getTitle());
        existingWantedItem.setDescription(updatedWantedItem.getDescription());
        existingWantedItem.setMaxPrice(updatedWantedItem.getMaxPrice());
        existingWantedItem.setCategory(updatedWantedItem.getCategory());
        existingWantedItem.setLocation(updatedWantedItem.getLocation());

        // 검증 및 저장
        validateWantedItem(existingWantedItem);
        return wantedItemRepository.save(existingWantedItem);
    }

    /**
     * 구매희망상품 삭제
     */
    public void deleteWantedItem(Long wantedItemId, User currentUser) throws Exception {
        WantedItem wantedItem = wantedItemRepository.findById(wantedItemId)
                .orElseThrow(() -> new Exception("구매희망상품을 찾을 수 없습니다."));

        if (!wantedItem.getBuyer().getId().equals(currentUser.getId()) && !currentUser.isAdmin()) {
            throw new Exception("구매희망상품을 삭제할 권한이 없습니다.");
        }

        wantedItemRepository.delete(wantedItem);
    }

    /**
     * 구매희망상품 상세 조회
     */
    public WantedItem getWantedItemDetail(Long wantedItemId) throws Exception {
        WantedItem wantedItem = wantedItemRepository.findById(wantedItemId)
                .orElseThrow(() -> new Exception("구매희망상품을 찾을 수 없습니다."));

        // 조회수 증가
        wantedItem.incrementViewCount();
        wantedItemRepository.save(wantedItem);

        return wantedItem;
    }

    /**
     * 모든 활성 구매희망상품 조회
     */
    @Transactional(readOnly = true)
    public List<WantedItem> getAllActiveWantedItems() {
        return wantedItemRepository.findByWantedStatusOrderByCreatedAtDesc(WantedStatus.ACTIVE);
    }

    /**
     * 페이징된 구매희망상품 목록 조회
     */
    @Transactional(readOnly = true)
    public Page<WantedItem> getWantedItemsWithPaging(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return wantedItemRepository.findByWantedStatusOrderByCreatedAtDesc(WantedStatus.ACTIVE, pageable);
    }

    /**
     * 특정 사용자의 구매희망상품 조회
     */
    @Transactional(readOnly = true)
    public List<WantedItem> getWantedItemsByBuyer(User buyer) {
        return wantedItemRepository.findByBuyerOrderByCreatedAtDesc(buyer);
    }

    /**
     * 구매희망상품 상태 변경
     */
    public WantedItem changeWantedItemStatus(Long wantedItemId, WantedStatus newStatus, User currentUser) throws Exception {
        WantedItem wantedItem = wantedItemRepository.findById(wantedItemId)
                .orElseThrow(() -> new Exception("구매희망상품을 찾을 수 없습니다."));

        if (!wantedItem.getBuyer().getId().equals(currentUser.getId())) {
            throw new Exception("구매희망상품 상태를 변경할 권한이 없습니다.");
        }

        wantedItem.setWantedStatus(newStatus);
        return wantedItemRepository.save(wantedItem);
    }

    /**
     * 입력값 검증
     */
    private void validateWantedItem(WantedItem wantedItem) throws Exception {
        if (wantedItem.getTitle() == null || wantedItem.getTitle().trim().length() < 5) {
            throw new Exception("구매희망상품 제목은 5글자 이상이어야 합니다.");
        }
        if (wantedItem.getTitle().length() > 100) {
            throw new Exception("구매희망상품 제목은 100글자를 초과할 수 없습니다.");
        }
        if (wantedItem.getDescription() == null || wantedItem.getDescription().trim().length() < 10) {
            throw new Exception("구매희망상품 설명은 10글자 이상이어야 합니다.");
        }
        if (wantedItem.getMaxPrice() == null || wantedItem.getMaxPrice() < 0) {
            throw new Exception("올바른 최대 희망가격을 입력해주세요.");
        }
        if (wantedItem.getCategory() == null || wantedItem.getCategory().trim().isEmpty()) {
            throw new Exception("카테고리를 선택해주세요.");
        }
        if (wantedItem.getLocation() == null || wantedItem.getLocation().trim().isEmpty()) {
            throw new Exception("거래 희망 지역을 입력해주세요.");
        }
    }

    public Optional<WantedItem> findById(Long id) {
        return wantedItemRepository.findById(id);
    }
}