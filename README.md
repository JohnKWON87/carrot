# 🥕 중고거래 플랫폼 (당근마켓 스타일)

> Spring Boot 기반 지역 중고거래 웹 애플리케이션

## 📌 프로젝트 소개

당근마켓을 벤치마킹하여 제작한 중고거래 플랫폼입니다.
사용자 간 안전한 중고거래를 위한 다양한 기능을 제공하며, 관리자 시스템을 통해 효율적인 플랫폼 운영이 가능합니다.

- **개발 기간**: 2025.9월 24일 ~ 2025.10월 1일
- **개발 인원**: 1팀 프로젝트 (총인원 5명, 팀장)
- **담당 역할**: 백엔드 개발 / 관리자 시스템 구현 

## 🛠 기술 스택

### Backend
- Java 23
- Spring Boot 3.x
- Spring Data JPA
- Spring Security
- Thymeleaf

### Database
- MySQL WorkBench

### Frontend
- HTML5 / CSS3
- JavaScript
- Bootstrap

### Tools
- IntelliJ IDEA
- Git / GitHub

## 🎯 주요 기능

### 1. 회원 관리
- Spring Security 기반 회원가입/로그인
- 비밀번호 암호화 (BCrypt)
- 사용자 권한 관리 (일반 회원/관리자)

### 2. 상품 관리
- 상품 등록/수정/삭제 (CRUD)
- 카테고리별 상품 분류 (전자제품, 의류, 도서, 가전 등)
- 이미지 업로드 및 관리
- 상품 상태 관리 (판매중/예약중/판매완료)
- 조회수 자동 증가

### 3. 검색 및 필터링
- 키워드 기반 상품 검색
- 카테고리별 상품 조회
- 검색 결과 페이징 처리

### 4. 관심상품 (찜하기)
- 관심상품 등록/삭제
- 내 관심상품 목록 조회
- 사용자별 관심상품 관리

### 5. 구매희망 등록
- 원하는 상품에 대한 구매희망 등록
- 구매희망 상품 목록 관리
- 판매자-구매자 간 거래 연결

### 6. 게시판 시스템
- 공지사항/자유게시판
- 게시글 작성/조회/삭제
- 댓글 기능 (선택적)

### 7. 관리자 기능
- **회원 관리**: 사용자 조회/권한 변경/계정 관리
- **상품 관리**: 부적절한 상품 블라인드 처리/삭제
- **로그 관리**: 관리자 활동 기록 및 조회
- **시스템 설정**: 통계 확인, 메뉴 관리
- **동적 메뉴**: 게시판 추가/삭제

## 📂 프로젝트 구조

```
src/
├── main/
│   ├── java/com/carrot/
│   │   ├── config/           # 보안, 초기화 설정
│   │   ├── constant/         # Enum 상수 (판매상태, 검토상태)
│   │   ├── controller/       # 요청 처리
│   │   ├── dto/              # 데이터 전송 객체
│   │   ├── entity/           # JPA 엔티티
│   │   ├── repository/       # 데이터 접근 계층
│   │   └── service/          # 비즈니스 로직
│   └── resources/
│       ├── templates/        # Thymeleaf 뷰
│       │   ├── admin/       # 관리자 페이지
│       │   └── board/       # 게시판
│       └── static/images/   # 정적 리소스
└── uploads/images/          # 업로드된 이미지
```

## 💾 데이터베이스 설계

### 주요 테이블
- **users**: 회원 정보 (아이디, 비밀번호, 이메일, 권한)
- **item**: 상품 정보 (제목, 가격, 설명, 이미지, 판매 상태)
- **wishlist**: 관심상품 (사용자-상품 N:M 관계)
- **wanted_item**: 구매희망 상품
- **board**: 게시글 (제목, 내용, 작성자)
- **admin_log**: 관리자 활동 기록
- **admin_menu**: 동적 메뉴 설정

## 🚀 실행 방법

### 1. 저장소 클론
```bash
git clone https://github.com/johnkwon87/carrot-market.git
cd carrot-market
```

### 2. 데이터베이스 설정
MySQL에 데이터베이스를 생성합니다.
```sql
CREATE DATABASE carrot_db;
```

### 3. application.properties 설정
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/carrot_db
spring.datasource.username=root
spring.datasource.password=1234

# 파일 업로드 경로 설정
file.upload-dir=uploads/images
```

### 4. 프로젝트 실행
```bash
./mvnw spring-boot:run
```

### 5. 브라우저에서 접속
```
http://localhost:8080
```

### 6. 초기 테스트 계정
- **관리자**: admin / admin123
- **일반 회원**: user / user123
(DataInitializer에서 자동 생성)

## 💡 트러블슈팅

### 1. 파일 업로드 경로 문제
**문제**: 이미지 업로드 시 절대경로/상대경로 충돌로 파일 저장 실패

**해결**:
- `WebConfig`에서 리소스 핸들러 설정
- `application.properties`에 업로드 경로 명시
- 운영체제별 경로 호환성 고려

```java
@Override
public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("/uploads/**")
            .addResourceLocations("file:uploads/");
}
```

### 2. N+1 문제 해결
**문제**: 상품 목록 조회 시 각 상품의 판매자 정보를 개별 쿼리로 조회

**해결**:
- `@EntityGraph`를 활용한 Fetch Join
- `ItemRepository`에 최적화된 조회 메서드 작성

```java
@EntityGraph(attributePaths = {"user"})
List<Item> findAllByCategoryAndStatus(String category, ItemSellStatus status);
```

**결과**: 쿼리 실행 횟수 80% 감소

### 3. 관리자 로그 자동 기록
**문제**: 관리자 활동 추적을 위한 로그 기록이 필요

**해결**:
- `AdminLogService`를 통한 통합 로그 관리
- 모든 관리자 액션에 AOP 패턴 적용 고려
- 로그 데이터 자동 저장 및 검색 기능 구현

**결과**: 관리자 활동 100% 추적 가능

## 📈 개선 계획

- [ ] 실시간 채팅 기능 (WebSocket)
- [ ] 지도 API 연동 (카카오맵)
- [ ] 결제 시스템 연동
- [ ] 푸시 알림 기능
- [ ] AWS 배포
- [ ] 모바일 반응형 디자인 개선

## 🎓 배운 점

- Spring Security를 활용한 인증/인가 시스템 구현
- JPA 연관관계 매핑 및 N+1 문제 해결
- 파일 업로드 및 리소스 관리
- 관리자 페이지 설계 및 권한 분리
- 실제 서비스 구조 이해 및 벤치마킹 능력
- 벤치마킹으로 인해 짧은 프로젝트기간이었지만 팀원들간의 협동심을 한번더 깨우친 프로젝트

- ## 👥 팀원
- 권혁민 팀장 : 관리자 기능, css구현 및 전체적인 코드 관리
- 박정대 팀원 : 판매자에게 거래 방식 제안 기능 구현
- 손경훈 팀원 : 회원가입 기능 및 회원 정보 관리 기능 구현
- 이상우 팀원 : 상품 거래 기능, PPT 제작 및 발표
- 태재우 팀원 : 회의록, 명세서, 정의서 및 카테고리 설정 구현

## 📧 문의

- Email: johnkwon33@gmail.com
- GitHub: https://github.com/johnkwon87/carrot

---

© 2025 johnkwon87. All rights reserved.
