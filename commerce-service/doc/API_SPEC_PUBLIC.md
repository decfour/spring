
# API 명세서

**The Hitchhiker's Guide to Earth**  

본 문서는 프로젝트의 주요 API와 기능을 정리한 공개용 명세서입니다.

## 1. 공통 사항

- **Base URL:** `http://localhost:8080` (로컬 개발 환경)
- **인증:** JWT 기반 쿠키 인증
- **요청 형식:** Form Data / JSON / Multipart
- **응답 형식:** HTML / Redirect / JSON
- **권한:** 공개, 로그인 사용자, 작성자, 관리자

> 본 프로젝트는 Spring MVC와 Thymeleaf를 기반으로 하며, 화면 반환 API와 JSON API를 함께 사용합니다.

---

## 2. 회원 및 인증

| Method | Endpoint | 기능 | 인증 |
|---|---|---|---|
| GET | `/member/sign-on` | 회원가입 페이지 | 공개 |
| POST | `/member/sign-on` | 회원가입 | 공개 |
| GET | `/member/sign-in` | 로그인 페이지 | 공개 |
| POST | `/member/sign-in` | 로그인 및 JWT 발급 | 공개 |
| POST | `/member/sign-out` | 로그아웃 | 필요 |
| GET | `/member/my-page` | 마이페이지 | 필요 |
| GET | `/member/my-course` | 내 산책 코스 조회 | 필요 |
| GET | `/member/my-item` | 내 상품 조회 | 필요 |
| GET | `/member/my-review` | 내 리뷰 조회 | 필요 |
| GET | `/member/my-order` | 내 주문 내역 조회 | 필요 |

---

## 3. 산책 경로

| Method | Endpoint | 기능 | 인증 |
|---|---|---|---|
| GET | `/walk` | 산책 메인 페이지 | 공개 |
| GET | `/walk/course/nearby` | 위치 기반 주변 코스 검색 | 공개 |
| GET | `/walk/course/hit` | 인기 코스 조회 | 공개 |
| GET | `/walk/course/{id}` | 코스 상세 조회 | 공개 |
| GET | `/walk/course/add` | 코스 등록 페이지 | 필요 |
| POST | `/walk` | 코스 등록 | 필요 |
| POST | `/walk/course/{id}/delete` | 코스 삭제 | 작성자/관리자 |
| POST | `/walk/course/route` | 경로 계산 | 필요 |
| POST | `/walk/course/{id}/like` | 코스 추천 | 필요 |
| POST | `/walk/course/{id}/tag` | 코스 태그 등록 | 필요 |

### 주요 API 예시

**주변 산책 코스 조회**

```http
GET /walk/course/nearby?lat=37.5665&lng=126.9780&page=0&size=3
```

- 사용자 지정 좌표를 기준으로 반경 1km 이내의 코스 검색
- 출발점까지의 거리 오름차순 정렬
- 페이징된 JSON 데이터 반환

**경로 계산**

```http
POST /walk/course/route
Content-Type: application/json

{
  "startLat": 37.5665,
  "startLng": 126.978,
  "endLat": 37.57,
  "endLng": 126.982
}
```

---

## 4. 상점 및 주문

### 상품

| Method | Endpoint | 기능 | 인증 |
|---|---|---|---|
| GET | `/shop` | 상품 목록 조회 | 공개 |
| GET | `/shop/item/{id}` | 상품 상세 조회 | 공개 |
| GET | `/shop/add` | 상품 등록 페이지 | 필요 |
| POST | `/shop/add` | 상품 등록 | 필요 |
| GET | `/shop/item/{id}/edit` | 상품 수정 페이지 | 작성자/관리자 |
| POST | `/shop/item/{id}/edit` | 상품 수정 | 작성자/관리자 |
| POST | `/shop/item/{id}/delete` | 상품 삭제 | 작성자/관리자 |
| POST | `/shop/item/{id}/restore` | 상품 복원 | 작성자 |

### 리뷰

| Method | Endpoint | 기능 | 인증 |
|---|---|---|---|
| GET | `/shop/item/{itemId}/review` | 리뷰 목록 | 공개 |
| GET | `/shop/item/{itemId}/review/{reviewId}` | 리뷰 상세 | 공개 |
| GET | `/shop/item/{itemId}/review/add` | 리뷰 작성 페이지 | 필요 |
| POST | `/shop/item/{itemId}/review/add` | 리뷰 등록 | 필요 |
| GET | `/shop/item/{itemId}/review/{reviewId}/edit` | 리뷰 수정 페이지 | 작성자 |
| POST | `/shop/item/{itemId}/review/{reviewId}/edit` | 리뷰 수정 | 작성자 |
| POST | `/shop/item/{itemId}/review/{reviewId}/delete` | 리뷰 삭제 | 작성자/관리자 |

### 장바구니 및 주문

| Method | Endpoint | 기능 | 인증 |
|---|---|---|---|
| GET | `/cart` | 장바구니 조회 | 필요 |
| POST | `/cart/item/add` | 상품 담기 | 필요 |
| POST | `/cart/item/{cartItemId}/delete` | 장바구니 상품 삭제 | 본인 |
| GET | `/order/one` | 단품 주문 페이지 | 필요 |
| GET | `/order/cart` | 장바구니 주문 페이지 | 필요 |
| POST | `/order/create` | 주문 생성 및 재고 차감 | 필요 |
| POST | `/order/{id}/cancel` | 주문 취소 및 재고 복원 | 본인 |

---

## 5. 커뮤니티 및 공지사항

### 커뮤니티

| Method | Endpoint | 기능 | 인증 |
|---|---|---|---|
| GET | `/community` | 게시글 목록 | 공개 |
| GET | `/community/hits` | 인기 게시글 | 공개 |
| GET | `/community/post/{id}` | 게시글 상세 | 공개 |
| GET | `/community/add` | 게시글 작성 페이지 | 필요 |
| POST | `/community/add` | 게시글 등록 | 필요 |
| GET | `/community/post/{id}/edit` | 게시글 수정 페이지 | 작성자/관리자 |
| POST | `/community/post/{id}/edit` | 게시글 수정 | 작성자/관리자 |
| POST | `/community/post/{id}/delete` | 게시글 삭제 | 작성자/관리자 |
| POST | `/community/post/{id}/like` | 게시글 추천 | 필요 |
| POST | `/comment/{postId}/add` | 댓글 등록 | 필요 |
| POST | `/comment/{id}/delete` | 댓글 삭제 | 작성자/관리자 |

### 공지사항

| Method | Endpoint | 기능 | 인증 |
|---|---|---|---|
| GET | `/notice` | 공지 목록 | 공개 |
| GET | `/notice/{id}` | 공지 상세 | 공개 |
| GET | `/notice/add` | 공지 등록 페이지 | 관리자 |
| POST | `/notice/add` | 공지 등록 | 관리자 |
| GET | `/notice/{id}/edit` | 공지 수정 페이지 | 관리자 |
| POST | `/notice/{id}/edit` | 공지 수정 | 관리자 |
| POST | `/notice/{id}/delete` | 공지 삭제 | 관리자 |

---

## 6. 실시간 채팅

### HTTP API

기본 경로: `/walk/course/{courseId}/chat`

| Method | Endpoint | 기능 |
|---|---|---|
| GET | `/` | 채팅방 목록 |
| POST | `/` | 채팅방 생성 |
| GET | `/{roomId}` | 채팅방 입장 및 조회 |
| POST | `/{roomId}/join` | 채팅방 참여 |
| POST | `/{roomId}/leave` | 채팅방 퇴장 |
| GET | `/{roomId}/messages` | 이전 채팅 이력 조회 |

모든 채팅 HTTP API는 로그인이 필요합니다.

### WebSocket / STOMP

| 구분 | 경로 | 설명 |
|---|---|---|
| CONNECT | `/ws/chat` | WebSocket 연결 |
| SEND | `/app/chat/{roomId}/send` | 메시지 전송 |
| SUBSCRIBE | `/topic/chat/{roomId}` | 메시지 구독 |

**메시지 전송 예시**

```json
{
  "content": "입구에서 만나요."
}
```

**메시지 수신 예시**

```json
{
  "id": 99,
  "senderId": 7,
  "senderName": "산책자",
  "content": "입구에서 만나요.",
  "createdAt": "2026-10-18T14:30:00"
}
```

- JWT 쿠키 기반 연결 인증
- 채팅방 참여자만 메시지 조회 및 전송 가능
- 메시지 저장 후 해당 채팅방 구독자에게 브로드캐스트
- 이전 메시지는 커서 기반으로 최대 30개씩 조회

---

## 7. 이미지

### 이미지

| Method | Endpoint | 기능 | 인증 |
|---|---|---|---|
| GET | `/images/{fileName}` | 이미지 파일 조회 | 공개 |

상품 이미지 업로드는 상품 등록 및 수정 과정에서 처리합니다.
