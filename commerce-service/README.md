# The Hitchhiker's Guide to Earth

산책 경로 공유 서비스

---

## 접속 주소

https://decfour.dev

- ID: `test`
- PASSWORD: `1234`

---

## 주요 기능

- JWT 기반 사용자 인증
- WebSocket / STOMP 기반 실시간 채팅
- 산책 서비스 (경로 등록 / 수정 / 추천)
- 상점 서비스 (상품 등록 / 수정 / 삭제)
- 마이페이지
- 커뮤니티
- 공지사항

---

## 기술 스택

| 분류 | 기술 |
| --- | --- |
| Language | Java 17 |
| Framework | Spring Boot 3.2.5 |
| Security | Spring Security 6.2.4 / JWT |
| ORM | Spring Data JPA / Hibernate 6.4.4 |
| Database | MySQL 8.0 |
| Realtime | WebSocket / STOMP |
| Frontend | Thymeleaf / JavaScript |
| API | Kakao Map API |
| Build | Gradle |
| Infra | AWS EC2 / Ubuntu / Nginx |

---

## Troubleshooting

### 주변 코스 조회 성능 개선

- 문제: 코스 데이터 증가에 따라 주변 코스 조회 성능 저하
- 해결: `start_lat`, `start_lng`에 B-Tree 인덱스 적용
- 결과: P95 응답 시간 15.09초 → 98.6ms / 예상 탐색 행 9,792개 → 675개

### 동시 주문 재고 정합성

- 문제: 여러 사용자가 동시에 주문할 경우 동일 재고를 조회하여 초과 판매가 발생할 가능성
- 해결: 재고 조회 및 차감 과정에 비관적 락(Pessimistic Lock) 적용
- 결과: 사용자 100명의 동시 주문 상황을 가정한 테스트에서 재고 정합성 유지

### JPA N+1 문제

- 문제: 댓글 목록 조회 시 작성자 연관 엔티티를 개별 조회하면서 추가 쿼리 발생
- 해결: Fetch Join을 적용하여 댓글과 작성자 정보를 한 번에 조회
- 결과: 연관 엔티티 조회를 위한 불필요한 추가 쿼리 제거