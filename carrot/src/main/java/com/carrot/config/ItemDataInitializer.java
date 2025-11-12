package com.carrot.config;

import com.carrot.entity.Item;
import com.carrot.entity.User;
import com.carrot.repository.ItemRepository;
import com.carrot.repository.UserRepository;
import com.carrot.constant.ItemSellStatus;
import com.carrot.constant.ModerationStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 테스트용 상품 데이터를 초기화하는 클래스
 * DataInitializer 다음에 실행되도록 @Order(2) 설정
 */
@Component
@Order(2) // DataInitializer(사용자 생성) 다음에 실행
public class ItemDataInitializer implements CommandLineRunner {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        createTestItems();
    }

    private void createTestItems() {
        // 이미 테스트 데이터가 있으면 생성하지 않음
        if (itemRepository.count() > 0) {
            System.out.println("===============================================");
            System.out.println("📦 테스트 상품 데이터가 이미 존재합니다.");
            System.out.println("===============================================");
            return;
        }

        // 테스트 사용자 조회
        Optional<User> testUserOpt = userRepository.findByUsername("testuser");
        Optional<User> adminOpt = userRepository.findByUsername("admin");

        if (testUserOpt.isEmpty()) {
            System.out.println("⚠️  테스트 사용자가 없어서 상품 데이터를 생성하지 않습니다.");
            return;
        }

        User testUser = testUserOpt.get();
        User admin = adminOpt.orElse(testUser);

        // 테스트 상품들 생성
        createElectronicsItems(testUser, admin);
        createClothesItems(testUser, admin);
        createMiscItems(testUser, admin);

        System.out.println("===============================================");
        System.out.println("📦 테스트 상품 데이터가 생성되었습니다:");
        System.out.println("   총 " + itemRepository.count() + "개의 상품이 등록되었습니다.");
        System.out.println("===============================================");
    }

    private void createElectronicsItems(User testUser, User admin) {
        // 가전제품 카테고리 상품들
        Item[] electronicsItems = {
                createItem("아이폰 13 Pro 128GB 판매",
                        "상태 좋은 아이폰 13 프로입니다. 케이스와 함께 판매합니다. 배터리 성능 90% 이상, 외관 상태 양호합니다. 직거래 선호합니다.",
                        850000, "electronics", "서울 강남구", testUser,
                        "/images/electronics/iphone13.jpg"),

                createItem("갤럭시 S22 Ultra 256GB",
                        "거의 새 제품, 박스 포함 모든 구성품 있습니다. 스크린 보호필름 부착되어 있고, 케이스도 같이 드립니다. 급매로 내놓습니다.",
                        700000, "electronics", "서울 서초구", admin,
                        "/images/electronics/galaxy-s22.jpg"),

                createItem("맥북 에어 M1 2021년형",
                        "사용감 거의 없는 맥북 에어입니다. 학업용으로 가볍게 사용했습니다. 충전 사이클 50회 미만, 완전 새것과 동일합니다.",
                        1200000, "electronics", "서울 마포구", testUser,
                        "/images/electronics/macbook-air.jpg"),

                createItem("LG 그램 17인치 노트북",
                        "가벼운 17인치 노트북, 업무용으로 완벽합니다. Intel i7, 16GB RAM, 512GB SSD 탑재. 키보드 및 화면 상태 우수합니다.",
                        900000, "electronics", "경기 성남시", admin,
                        "/images/electronics/lg-gram.jpg"),

                createItem("에어팟 프로 2세대 새상품",
                        "미개봉 새상품입니다. 선물받았는데 이미 있어서 판매합니다. 정품 인증 가능하며, 영수증도 같이 드립니다.",
                        250000, "electronics", "서울 홍대", testUser,
                        "/images/electronics/airpods-pro.jpg"),

                createItem("삼성 갤럭시탭 S8 11인치",
                        "태블릿과 키보드 커버 세트로 판매합니다. 거의 사용하지 않아 상태 매우 좋습니다. 동영상 시청용, 업무용으로 좋습니다.",
                        450000, "electronics", "부산 해운대구", admin,
                        "/images/electronics/galaxy-tab.jpg")
        };

        for (Item item : electronicsItems) {
            itemRepository.save(item);
        }
    }

    private void createClothesItems(User testUser, User admin) {
        // 의류 카테고리 상품들
        Item[] clothesItems = {
                createItem("나이키 에어포스1 280mm",
                        "280mm 사이즈, 몇 번 신지 않은 상태입니다. 흰색 깔끔한 디자인으로 어떤 옷과도 잘 어울립니다. 박스 포함 판매합니다.",
                        120000, "clothes", "서울 홍대", testUser,
                        "/images/clothes/nike-airforce.jpg"),

                createItem("아디다스 스탠스미스 260mm",
                        "깔끔한 화이트 스탠스미스, 260mm입니다. 한 시즌 착용했지만 관리 잘 해서 상태 좋습니다. 세탁 완료된 상태입니다.",
                        80000, "clothes", "부산 해운대구", admin,
                        "/images/clothes/adidas-stansmith.jpg"),

                createItem("유니클로 다운 패딩 점퍼 L사이즈",
                        "작년 겨울에 구매한 유니클로 다운 점퍼입니다. 따뜻하고 가벼워서 실용적입니다. 드라이클리닝 완료, 보관상태 우수합니다.",
                        50000, "clothes", "서울 강남구", testUser,
                        "/images/clothes/uniqlo-padding.jpg"),

                createItem("조던 1 하이 브레드 270mm",
                        "조던 1 하이 브레드 정품입니다. 270mm, 상태 양호합니다. 신발 관리 도구로 깨끗하게 관리했습니다. 박스와 함께 판매.",
                        180000, "clothes", "서울 용산구", admin,
                        "/images/clothes/jordan1.jpg"),

                createItem("노스페이스 플리스 자켓 100 사이즈",
                        "노스페이스 정품 플리스 자켓 판매합니다. 100 사이즈(L), 보온성 좋고 활동하기 편합니다. 세탁 완료, 냄새 없음.",
                        60000, "clothes", "경기 수원시", testUser,
                        "/images/clothes/northface-fleece.jpg")
        };

        for (Item item : clothesItems) {
            itemRepository.save(item);
        }
    }

    private void createMiscItems(User testUser, User admin) {
        // 기타 및 잡화 카테고리 상품들
        Item[] miscItems = {
                createItem("원목 책상 1200x600",
                        "이케아에서 구매한 원목 책상입니다. 스크래치 거의 없어요. 서랍 2개 있고, 조립 상태로 직거래만 가능합니다.",
                        150000, "misc", "서울 용산구", testUser,
                        "/images/misc/wooden-desk.jpg"),

                createItem("허먼밀러 에어론 의자 B사이즈",
                        "정품 허먼밀러 에어론 의자입니다. 재택근무용으로 구매했는데 거의 사용안했어요. 등받이 조절 가능, 상태 완벽합니다.",
                        800000, "misc", "경기 수원시", admin,
                        "/images/misc/herman-miller.jpg"),

                createItem("캐논 EOS R50 미러리스 카메라",
                        "카메라 입문용으로 좋은 캐논 R50입니다. 렌즈킷으로 판매하며, 구매한 지 3개월 정도 되었습니다. 박스, 설명서 모두 있음.",
                        650000, "misc", "서울 마포구", testUser,
                        "/images/misc/canon-camera.jpg"),

                createItem("다이슨 V11 무선청소기",
                        "다이슨 V11 무선청소기 판매합니다. 흡입력 좋고 무선이라 편리합니다. 브러시 헤드 여러 개 포함, 충전 거치대도 있습니다.",
                        300000, "misc", "서울 강남구", admin,
                        "/images/misc/dyson-v11.jpg"),

                createItem("닌텐도 스위치 OLED 화이트",
                        "닌텐도 스위치 OLED 화이트 모델입니다. 게임팩 몇 개와 함께 판매합니다. 보호필름, 케이스 포함. 상태 매우 좋습니다.",
                        280000, "misc", "부산 부산진구", testUser,
                        "/images/misc/nintendo-switch.jpg"),

                createItem("브레빌 에스프레소 머신",
                        "브레빌 바리스타 익스프레스 에스프레소 머신입니다. 집에서 카페 퀄리티의 커피를 즐길 수 있습니다. 사용법 설명해드려요.",
                        450000, "misc", "서울 서초구", admin,
                        "/images/misc/breville-espresso.jpg")
        };

        for (Item item : miscItems) {
            itemRepository.save(item);
        }
    }

    /**
     * Item 생성 헬퍼 메서드
     */
    private Item createItem(String title, String description, Integer price,
                            String category, String location, User seller, String imageUrl) {
        Item item = new Item();
        item.setTitle(title);
        item.setDescription(description);
        item.setPrice(price);
        item.setCategory(category);
        item.setLocation(location);
        item.setSeller(seller);
        item.setSellStatus(ItemSellStatus.SELL);
        item.setModerationStatus(ModerationStatus.VISIBLE);
        item.setViewCount(0);
        item.setWishCount(0);
        item.setImageUrl(imageUrl);  // 이미지 URL 설정
        return item;
    }
}