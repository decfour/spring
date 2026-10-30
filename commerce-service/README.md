# The Hitchhiker's Guide to Earth

사용자가 원하는 위치를 기준으로 주변 산책 경로를 탐색하고 공유할 수 있는 웹 서비스입니다.

산책 경로를 따라 발견한 장소와 요소를 메모하고, 다른 이용자들과 후기 및 경험을 공유할 수 있습니다.

실시간 채팅을 통한 함께 걷기, 커뮤니티, 중고 거래 기능을 제공하여 사용자 간 소통을 지원합니다.

## 🔗 접속 주소

**https://decfour.dev**

**테스트 계정**
- ID: `test`
- PASSWORD: `1234`

---

## 🚀 주요 기능

- **사용자 인증:** JWT 및 Spring Security 기반 로그인 및 인증
- **산책 경로 공유:** 경로 등록, 수정, 추천 및 위치 기반 주변 경로 탐색
- **산책 경로 발견:** 실제 경로 주변에 발견한 장소나 요소를 메모하고 다른 사용자와 공유
- **산책 경로 리뷰:** 산책 경로에 대한 후기 작성 및 조회
- **함께 걷기:** 산책 경로별 채팅방 생성 및 WebSocket / STOMP 기반 실시간 채팅
- **상점:** 사용자 간 중고 거래, 상품 관리 및 주문 기능
- **커뮤니티:** 게시글과 댓글을 통한 사용자 간 소통
- **마이페이지:** 사용자 정보 및 활동 관리

---

## 🛠️ 기술 스택

### Backend

![Java](https://img.shields.io/badge/Java_17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot_3.2.5-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring_Security-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white)
![Spring Data JPA](https://img.shields.io/badge/Spring_Data_JPA-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![Hibernate](https://img.shields.io/badge/Hibernate-59666C?style=for-the-badge&logo=hibernate&logoColor=white)

### Database

![MySQL](https://img.shields.io/badge/MySQL_8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white)

### Frontend

![Thymeleaf](https://img.shields.io/badge/Thymeleaf-005F0F?style=for-the-badge&logo=thymeleaf&logoColor=white)
![JavaScript](https://img.shields.io/badge/JavaScript-F7DF1E?style=for-the-badge&logo=javascript&logoColor=black)

### Infra & Build

![AWS EC2](https://img.shields.io/badge/AWS_EC2-FF9900?style=for-the-badge&logo=amazonec2&logoColor=white)
![Ubuntu](https://img.shields.io/badge/Ubuntu-E95420?style=for-the-badge&logo=ubuntu&logoColor=white)
![Nginx](https://img.shields.io/badge/Nginx-009639?style=for-the-badge&logo=nginx&logoColor=white)
![Gradle](https://img.shields.io/badge/Gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white)

### Communication & API

![WebSocket](https://img.shields.io/badge/WebSocket-4353FF?style=for-the-badge)
![STOMP](https://img.shields.io/badge/STOMP-555555?style=for-the-badge)
![Kakao Map API](https://img.shields.io/badge/Kakao_Map_API-FFCD00?style=for-the-badge&logo=kakao&logoColor=black)

---

## 🏗️ 시스템 아키텍처

![시스템 아키텍처](doc/image/architecture.png)

- **AWS EC2:** Spring Boot 애플리케이션 및 MySQL 운영
- **Nginx:** HTTPS 요청 처리 및 Spring Boot 애플리케이션으로의 리버스 프록시
- **Spring Boot:** 비즈니스 로직, 인증 및 WebSocket 기반 실시간 채팅 처리
- **MySQL:** 회원, 산책 경로, 상품, 주문 및 커뮤니티 데이터 관리
- **Kakao Map API:** 지도 표시 및 위치 기반 산책 경로 제공
- **GitHub Actions:** 배포 자동화

---

## 🔧 Troubleshooting

### 1. 주변 코스 조회 성능 개선

**문제**
- 코스 데이터 증가에 따라 주변 산책 경로 조회 성능 저하
- 약 10,000개의 코스 데이터와 최대 500 VU 환경에서 P95 응답 시간이 15.09초까지 증가

**원인 분석**
- 기존 쿼리에서 전체 코스를 대상으로 거리 계산을 수행하여 Full Table Scan 발생
- SQL 실행 계획에서 불필요하게 많은 데이터가 탐색되는 문제 확인

**해결**
- Bounding Box를 활용해 주변 코스의 검색 범위를 제한
- `start_lat`, `start_lng` 컬럼에 B-Tree 인덱스를 적용하여 탐색 대상 축소
- SQL 실행 계획을 비교하여 인덱스 적용 효과 검증

**결과**

| 측정 항목 | 개선 전 | 개선 후 |
| --- | ---: | ---: |
| P95 응답 시간 | 15.09초 | 98.6ms |
| 평균 응답 시간 | 6.01초 | 39.68ms |
| 예상 탐색 행 | 9,792개 | 675개 |

- P95 응답 시간 **약 99.3% 감소**
- 약 10,000개 코스 데이터를 활용한 부하 테스트 수행
- 최대 500 VU(가상 사용자) 환경에서 개선 효과 확인

### 2. 동시 주문 시 재고 정합성 문제

**문제**
- 여러 사용자가 동시에 상품을 주문할 경우 일부 재고 차감이 반영되지 않는 문제 발생
- 테스트 과정에서 주문 요청 100건 중 재고 차감이 67건만 반영되는 현상 확인
- Race Condition으로 인해 재고 수량을 초과하는 주문 발생 가능

**원인 분석**
- 여러 트랜잭션이 동일한 재고를 조회한 후 각각 수정하는 과정에서 경쟁 상태 발생
- 재고 조회와 차감 사이에 다른 트랜잭션이 개입할 수 있는 구조 확인

**해결**
- JPA의 `PESSIMISTIC_WRITE`를 이용한 비관적 락(Pessimistic Lock) 적용
- 동일 상품에 대한 재고 변경 작업을 트랜잭션 단위로 직렬화
- 무한 대기를 방지하기 위한 락 타임아웃 설정

**결과**
- 100명의 가상 회원과 100개의 가상 상품을 이용하여 32개 스레드 환경에서 동시성 테스트 수행
- 테스트 결과 재고가 정상 차감되어 최종 재고 0개 확인
- 각 가상 회원의 주문 내역이 정상적으로 생성된 것을 확인

### 3. 상품 가격 변동에 따른 주문 데이터 정합성

**문제**
- 상품 가격이 변경될 경우 과거 주문 내역의 결제 금액에도 영향을 줄 가능성 존재
- 주문 데이터가 상품의 현재 가격을 직접 참조하면 주문 시점의 가격을 보존하기 어려움

**해결**
- `PurchaseItem` 중간 엔티티를 통해 상품과 주문 데이터를 연결
- 주문 시점의 상품 가격을 Snapshot으로 저장
- 이후 상품 가격이 변경되더라도 기존 주문의 결제 금액이 유지되도록 설계

**결과**
- 상품 할인 및 가격 변경 이후에도 과거 주문의 결제 금액 유지
- 상품 정보와 주문 당시 거래 정보의 정합성 확보

### 4. JPA N+1 문제 해결

**문제**
- 댓글 목록 조회 시 작성자 연관 엔티티를 개별 조회하면서 추가 SQL 쿼리 발생
- 댓글 수가 증가할수록 Member 조회 쿼리가 반복적으로 실행되는 문제 확인

**원인 분석**
- 댓글 목록 조회 이후 작성자 정보에 접근하면서 연관 엔티티에 대한 추가 조회 발생
- 댓글 수에 비례하여 SQL 실행 횟수가 증가하는 N+1 문제 확인

**해결**
- Fetch Join을 적용하여 댓글과 작성자 정보를 함께 조회
- 페이징 처리를 위해 별도의 `countQuery` 분리
- 연관 엔티티에 대한 불필요한 개별 조회 제거

**결과**
- 댓글 작성자 조회 과정에서 발생하던 추가 SQL 쿼리 제거
- 댓글 수 증가에 따른 불필요한 Member 조회 방지

---

## 🖥️ 주요 화면

### 1. 산책 경로

**주변 산책 경로 탐색**

![산책 경로 목록](doc/image/walk_01.PNG)

현재 위치 또는 사용자가 지정한 위치를 기준으로 주변 산책 경로를 탐색할 수 있습니다.

**산책 경로 안내**

![산책 경로 안내](doc/image/walk_02.PNG)

주변 산책 경로 목록에서 코스를 선택하면 지도에 해당 경로가 표시됩니다.

**산책 경로 상세**

![산책 경로 상세](doc/image/walk_03.PNG)

산책 경로에 대한 설명, 추천, 태그 및 리뷰 기능을 제공합니다.

**산책 경로 발견**

![산책 경로 발견](doc/image/walk_04.PNG)

실제 산책 경로의 좌표를 기준으로 주변에 발견한 장소나 요소를 메모하여 다른 이용자와 공유할 수 있습니다.

지도에 표시된 발견 핀을 선택하면 다른 이용자가 남긴 메모를 확인할 수 있습니다.

### 2. 함께 걷기

**채팅방 목록**

![함께 걷기 목록](doc/image/chat_01.PNG)

함께 산책할 이용자를 모집하기 위해 채팅방을 생성하고 참여할 수 있습니다.

**실시간 채팅**

![함께 걷기 상세](doc/image/chat_02.PNG)

WebSocket / STOMP를 이용하여 참여자 간 실시간 메시지를 주고받을 수 있습니다.

- 채팅방 최대 인원: 5명
- 방장이 퇴장하면 채팅방 종료

### 3. 커뮤니티

![커뮤니티 목록](doc/image/talk_01.PNG)

게시글과 댓글, 추천을 통해 사용자 간 정보를 공유하고 소통할 수 있습니다.

추천 수가 20개 이상인 게시글은 HIT 게시판에서 조회할 수 있습니다.

### 4. 상점

**상품 목록**

![상점 목록](doc/image/shop_01.PNG)

사용자가 상품을 등록하고 조회하여 중고 거래를 진행할 수 있습니다.

**상품 상세**

![상점 상세](doc/image/shop_02.PNG)

상품을 즉시 구매하거나 장바구니에 담아 주문할 수 있습니다.

### 5. 마이페이지

![마이페이지 상세](doc/image/mypage_01.PNG)

사용자가 등록한 산책 경로, 상품, 리뷰 및 주문 내역 등 개인 활동을 조회할 수 있습니다.

---

## 📂 프로젝트 구조

```text
src
├── main
│   ├── java/com/kdj/commerce
│   │   ├── config
│   │   ├── domain
│   │   │   ├── cart
│   │   │   ├── chat
│   │   │   ├── community
│   │   │   ├── item
│   │   │   ├── member
│   │   │   ├── notice
│   │   │   ├── purchase
│   │   │   ├── review
│   │   │   └── walk
│   │   ├── exception
│   │   ├── service
│   │   └── web
│   │       ├── argumentresolver
│   │       ├── controller
│   │       ├── dto
│   │       ├── file
│   │       └── security
│   └── resources
│       ├── static
│       │   ├── css
│       │   ├── images
│       │   │   ├── branding
│       │   │   └── icons
│       │   └── js
│       └── templates
│           ├── cart
│           ├── chat
│           ├── common
│           ├── community
│           ├── error
│           ├── member
│           ├── notice
│           ├── purchase
│           ├── review
│           ├── shop
│           └── walk
│               └── review
└── test
    └── java/com/kdj/commerce
        ├── domain
        │   └── purchase
        ├── exception
        ├── service
        └── web
            └── controller
```

---

## 📚 프로젝트 문서

- [API 명세서](doc/API_SPEC_PUBLIC.md)