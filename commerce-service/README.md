# The Hitchhiker's Guide to Earth

Spring Boot & MySQL & Kakao Map API 기반 산책 경로 추천 및 쇼핑 서비스

---

## 접속 주소

[https://decfour.dev](https://decfour.dev)

---

## 주요 기능

* JWT 기반 인증
* 산책 서비스 (경로 등록 / 수정 / 추천)
* 상점 서비스 (상품 등록 / 수정 / 삭제)
* 마이페이지
* 커뮤니티
* 공지사항

---

## 기술 스택

| 분류         | 기술                                |
| ---------- | --------------------------------- |
| Language   | Java 17                           |
| Framework  | Spring Boot 3.2.5                 |
| Security   | Spring Security 6.2.4             |
| ORM        | Spring Data JPA / Hibernate 6.4.4 |
| Database   | MySQL 8.0                         |
| Frontend   | Thymeleaf / JavaScript            |
| API        | Kakao Map API                     |
| Build      | Gradle                            |
| Server     | AWS EC2 / Ubuntu                  |
| Web Server | Nginx                             |

---

## Troubleshooting

### 동시 주문 재고 정합성

* 문제: 동시 주문 시 재고 중복 차감 가능
* 해결: `Pessimistic Lock` 적용

### 벌크 UPDATE와 영속성 컨텍스트

* 문제: 벌크 연산 후 DB와 영속성 컨텍스트 상태 불일치
* 해결: `clearAutomatically = true` 적용

### JPA N+1

* 문제: 연관 엔티티 조회 시 추가 쿼리 발생 (예: 게시물 정보 + 게시물 작성자)
* 해결: `Fetch Join` 적용

---

## 배포

```text
Client
  ↓
Nginx
  ↓
Spring Boot
  ↓
MySQL
```

* AWS EC2 배포
* Nginx Reverse Proxy 구성
* 환경변수 기반 운영 설정
