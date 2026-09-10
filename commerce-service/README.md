# The Hitchhiker's Guide to Earth

Spring Boot & MySQL & Kakao Map API 기반 산책 경로 추천 및 쇼핑 서비스

---

## 접속 주소

[https://decfour.dev](https://decfour.dev)
(ID: test  PASSWORD: 1234)

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

### 주변 코스 조회 성능
* 문제: 수많은 코스들 존재 시, 주변 코스 조회 과정에서의 성능, 부하 문제 발생
* 해결: Bounding Box에 사용되는 start_lat, start_lng 에 B-Tree 인덱스를 추가
* 결과: P95 응답 시간: 15.09초 → 98.6ms / 예상 탐색 행: 9,792개 → 675개

### 동시 주문 재고 정합성
* 문제: 동시 주문 시 재고 중복 차감 가능
* 해결: `Pessimistic Lock` 적용
* 결과: 사용자 100명 동시 주문 상황 가정 테스트 결과 재고 정상 차감

### JPA N+1
* 문제: 연관 엔티티 조회 시 추가 쿼리 발생 (ex: 댓글 정보 + 댓글 작성자)
* 해결: `Fetch Join` 적용
* 결과: 불필요한 쿼리문 감소
