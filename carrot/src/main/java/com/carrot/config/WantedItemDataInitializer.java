package com.carrot.config;

import com.carrot.entity.WantedItem;
import com.carrot.entity.User;
import com.carrot.repository.WantedItemRepository;
import com.carrot.repository.UserRepository;
import com.carrot.constant.WantedStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 테스트용 구매희망상품 데이터를 초기화하는 클래스
 */
@Component
@Order(3) // ItemDataInitializer 다음에 실행
public class WantedItemDataInitializer implements CommandLineRunner {

    @Autowired
    private WantedItemRepository wantedItemRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        createTestWantedItems();
    }

    private void createTestWantedItems() {
        // 이미 테스트 데이터가 있으면 생성하지 않음
        if (wantedItemRepository.count() > 0) {
            System.out.println("===============================================");
            System.out.println("🛒 테스트 구매희망상품 데이터가 이미 존재합니다.");
            System.out.println("===============================================");
            return;
        }

        // 테스트 사용자 조회
        Optional<User> testUserOpt = userRepository.findByUsername("testuser");
        Optional<User> adminOpt = userRepository.findByUsername("admin");

        if (testUserOpt.isEmpty()) {
            System.out.println("⚠️  테스트 사용자가 없어서 구매희망상품 데이터를 생성하지 않습니다.");
            return;
        }

        User testUser = testUserOpt.get();
        User admin = adminOpt.orElse(testUser);

        // 테스트 구매희망상품들 생성
        createElectronicsWantedItems(testUser, admin);
        createClothesWantedItems(testUser, admin);
        createMiscWantedItems(testUser, admin);

        System.out.println("===============================================");
        System.out.println("🛒 테스트 구매희망상품 데이터가 생성되었습니다:");
        System.out.println("   총 " + wantedItemRepository.count() + "개의 구매희망상품이 등록되었습니다.");
        System.out.println("===============================================");
    }

    private void createElectronicsWantedItems(User testUser, User admin) {
        WantedItem[] electronicsWantedItems = {
                createWantedItem("아이폰 14 Pro 구매희망합니다",
                        "아이폰 14 Pro 256GB 이상을 찾고 있습니다.\n" +
                                "- 색상: 딥퍼플 또는 스페이스블랙 선호\n" +
                                "- 배터리 성능 85% 이상\n" +
                                "- 외관 상태 양호한 제품\n" +
                                "- 박스 및 액세서리 포함 시 우대",
                        1000000, "electronics", "서울 강남구", testUser),

                createWantedItem("갤럭시탭 S8 구매합니다",
                        "갤럭시탭 S8 11인치 WiFi 모델을 구매하고 싶습니다.\n" +
                                "- 128GB 또는 256GB\n" +
                                "- 키보드 커버 포함 시 추가 금액 지불 가능\n" +
                                "- 강남, 서초 지역 직거래 선호",
                        400000, "electronics", "서울 서초구", admin),

                createWantedItem("맥북 에어 M2 찾습니다",
                        "맥북 에어 M2 2022년형을 구매하려고 합니다.\n" +
                                "- 8GB RAM / 256GB SSD 이상\n" +
                                "- 실버 또는 스페이스그레이\n" +
                                "- 사용감 적은 제품 우대\n" +
                                "- AppleCare+ 남아있으면 더욱 좋습니다",
                        1300000, "electronics", "경기 성남시", testUser)
        };

        for (WantedItem wantedItem : electronicsWantedItems) {
            wantedItemRepository.save(wantedItem);
        }
    }

    private void createClothesWantedItems(User testUser, User admin) {
        WantedItem[] clothesWantedItems = {
                createWantedItem("나이키 조던 1 구매희망",
                        "나이키 에어조던 1 하이 또는 로우를 찾고 있습니다.\n" +
                                "- 사이즈: 270mm\n" +
                                "- 색상: 브레드, 시카고, 로얄 선호\n" +
                                "- 상태: 8/10 이상\n" +
                                "- 정품 인증서 또는 영수증 있으면 좋겠습니다",
                        200000, "clothes", "서울 홍대", testUser),

                createWantedItem("유니클로 다운패딩 구매",
                        "유니클로 울트라라이트다운 재킷을 구매하고 싶습니다.\n" +
                                "- 사이즈: L (100)\n" +
                                "- 색상: 블랙, 네이비, 그레이\n" +
                                "- 작년 또는 올해 모델\n" +
                                "- 세탁 완료된 깨끗한 상태",
                        40000, "clothes", "부산 해운대구", admin),

                createWantedItem("아디다스 스탠스미스 흰색",
                        "아디다스 스탠스미스 화이트/그린을 찾습니다.\n" +
                                "- 사이즈: 265mm\n" +
                                "- 상태: 상급 이상 (발가락 부분 변색 없는 것)\n" +
                                "- 박스 있으면 우대\n" +
                                "- 서울 전지역 직거래 가능",
                        70000, "clothes", "서울 용산구", testUser)
        };

        for (WantedItem wantedItem : clothesWantedItems) {
            wantedItemRepository.save(wantedItem);
        }
    }

    private void createMiscWantedItems(User testUser, User admin) {
        WantedItem[] miscWantedItems = {
                createWantedItem("이케아 책상 구매희망",
                        "이케아 책상을 구매하고 싶습니다.\n" +
                                "- 크기: 120x60cm 이상\n" +
                                "- 높이 조절 가능한 것 우대\n" +
                                "- 서랍 포함된 모델 선호\n" +
                                "- 원목 또는 화이트 색상\n" +
                                "- 수원, 용인 지역 직거래만 가능",
                        100000, "misc", "경기 수원시", testUser),

                createWantedItem("다이슨 무선청소기 찾습니다",
                        "다이슨 무선청소기 V10 이상 모델을 구매하려고 합니다.\n" +
                                "- 배터리 수명 양호한 제품\n" +
                                "- 브러시 헤드 여러 개 포함\n" +
                                "- 충전 거치대 포함 필수\n" +
                                "- A/S 가능한 정품만",
                        250000, "misc", "서울 마포구", admin),

                createWantedItem("캐논 카메라 DSLR 구매",
                        "캐논 DSLR 카메라를 구매하고 싶습니다.\n" +
                                "- 기종: 80D, 90D 또는 6D Mark II\n" +
                                "- 렌즈킷 포함 (18-55mm 또는 24-105mm)\n" +
                                "- 셔터 수 5만 이하\n" +
                                "- 메모리카드, 가방 등 액세서리 포함 시 우대",
                        600000, "misc", "부산 부산진구", testUser)
        };

        for (WantedItem wantedItem : miscWantedItems) {
            wantedItemRepository.save(wantedItem);
        }
    }

    private WantedItem createWantedItem(String title, String description, Integer maxPrice,
                                        String category, String location, User buyer) {
        WantedItem wantedItem = new WantedItem();
        wantedItem.setTitle(title);
        wantedItem.setDescription(description);
        wantedItem.setMaxPrice(maxPrice);
        wantedItem.setCategory(category);
        wantedItem.setLocation(location);
        wantedItem.setBuyer(buyer);
        wantedItem.setWantedStatus(WantedStatus.ACTIVE);
        wantedItem.setViewCount(0);
        wantedItem.setInterestCount(0);
        return wantedItem;
    }
}