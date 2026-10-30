
# API 명세서

**The Hitchhiker's Guide to Earth**  

본 문서는 프로젝트의 주요 API와 기능을 정리한 공개용 명세서입니다.

코드 대조 기준: 2026-10-31.

## 1. 공통 사항

- **Base URL:** `http://localhost:8080` (로컬 개발 환경)
- **인증:** JWT 기반 쿠키 인증
- **요청 형식:** Form Data / JSON / Multipart
- **응답 형식:** HTML / Redirect / JSON
- **권한:** 공개, 로그인 사용자, 작성자, 관리자
- **페이지 번호:** `page`는 0부터 시작합니다. 별도 안내가 없는 목록은 Spring Data 페이징을 사용합니다.
- **오류 응답:** 일반 화면은 오류 HTML 또는 컨트롤러별 리다이렉트, JSON 요청은 HTTP 상태와 오류 JSON을 사용합니다. 상세 기준은 [예외 처리 기준](EXCEPTION_HANDLING.md)을 참고합니다.

> 본 프로젝트는 Spring MVC와 Thymeleaf를 기반으로 하며, 화면 반환 API와 JSON API를 함께 사용합니다.

---

## 2. 회원 및 인증

| Method | Endpoint | 기능 | 인증 |
|---|---|---|---|
| GET | `/member/sign-up` | 회원가입 페이지 | 공개 |
| POST | `/member/sign-up` | 회원가입 | 공개 |
| GET | `/member/sign-in` | 로그인 페이지 | 공개 |
| POST | `/member/sign-in` | 로그인 및 JWT 발급 | 공개 |
| POST | `/member/sign-out` | 로그아웃 | 필요 |
| GET | `/member/my-page` | 마이페이지 | 필요 |
| GET | `/member/my-course` | 내 산책 코스 조회 | 필요 |
| GET | `/member/my-item` | 내 상품 조회 | 필요 |
| GET | `/member/my-review` | 내 리뷰 조회 | 필요 |
| GET | `/member/my-order` | 내 주문 내역 조회 | 필요 |

- 회원가입 폼 필드: `name`, `email`, `signInId`, `signInPassword`. 화면에서는 이메일을 두 칸으로 입력하지만, 서버에는 합쳐진 `email`을 전달합니다.
- 로그인 폼 필드: `signInId`, `signInPassword`, `redirectURL`(기본 `/`). 성공하면 `Authorization` 쿠키를 발급하고 복귀 경로로 리다이렉트합니다. 검증·인증 실패 시 입력 오류가 포함된 로그인 화면을 반환합니다.
- 로그아웃은 인증 쿠키를 만료시키고 `302 /`로 이동합니다. 별도 로그아웃 화면은 없습니다.
- 내 코스·상품·리뷰·주문은 기본 페이지 크기 7, `id` 내림차순입니다.

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

### 산책 리뷰

| Method | Endpoint | 기능 | 인증 |
|---|---|---|---|
| GET | `/walk/course/{courseId}/review` | 리뷰 목록 | 공개 |
| GET | `/walk/course/{courseId}/review/{reviewId}` | 리뷰 상세 | 공개 |
| GET | `/walk/course/{courseId}/review/add` | 리뷰 작성 페이지 | 필요 |
| POST | `/walk/course/{courseId}/review/add` | 리뷰 등록 | 필요 |
| GET | `/walk/course/{courseId}/review/{reviewId}/edit` | 리뷰 수정 페이지 | 작성자 |
| POST | `/walk/course/{courseId}/review/{reviewId}/edit` | 리뷰 수정 | 작성자 |
| POST | `/walk/course/{courseId}/review/{reviewId}/delete` | 리뷰 삭제 | 작성자/관리자 |

리뷰 목록은 기본 10개씩 `id` 내림차순으로 조회합니다. 등록·수정은 폼의 `title`, `content`를 사용하고, 성공하면 해당 리뷰 상세로 이동합니다. 삭제 성공 시 해당 코스의 리뷰 목록으로 이동합니다.

### 발견 장소 핀

| Method | Endpoint | 기능 | 인증 | 성공 응답 |
|---|---|---|---|---|
| GET | `/walk/course/{courseId}/pins` | 발견 장소 전체 목록 | 공개 | `200` JSON 배열 |
| POST | `/walk/course/{courseId}/pins` | 발견 장소 등록 | 필요 | `201` 생성된 핀 JSON |
| DELETE | `/walk/course/{courseId}/pins/{pinId}` | 발견 장소 삭제 | 핀 작성자/관리자 | `204` 본문 없음 |

```http
POST /walk/course/10/pins
Content-Type: application/json

{
  "title": "노을이 보이는 벤치",
  "content": "잠시 쉬어가기 좋은 곳입니다.",
  "latitude": 37.5665,
  "longitude": 126.9780
}
```

- `title`: 공백 불가, 최대 80자. `content`: 공백 불가, 최대 1000자.
- `latitude`: 필수, -90~90. `longitude`: 필수, -180~180.
- 코스 경로에서 30m 이내인 좌표만 등록할 수 있습니다. 입력값·거리 조건 위반은 `400`, 대상 없음은 `404`, 삭제 권한 부족은 `403`입니다.
- 응답 필드: `id`, `title`, `content`, `latitude`, `longitude`, `creatorName`, `createdAt`, `deletable`.
- 목록은 페이징 없이 핀 ID 내림차순으로 반환합니다. `deletable`은 현재 사용자 기준 삭제 가능 여부이며, 화면의 발견 장소 수는 목록 길이로 계산합니다.

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

경로 계산 응답은 `routeData`(경도·위도 순서의 좌표 배열을 담은 JSON 문자열), `distance`(m), `duration`(초)입니다.

### 추천·태그 응답

- `POST /walk/course/{id}/like`: 요청 본문 없이 호출합니다. 성공 시 `200`과 갱신된 추천 수(JSON 숫자)를 반환합니다. 중복 추천은 `409`와 `{"message":"이미 추천했습니다"}`, 컨트롤러에서 로그인 사용자를 찾지 못하면 `401`입니다.
- `POST /walk/course/{id}/tag`: JSON `{"tag":"NATURE"}`를 전달합니다. 성공 시 `200`이며 본문은 없습니다. 잘못된 태그 코드는 `400`, 중복 태그는 `409`입니다.
- 태그 코드: `NATURE`, `CITY`, `QUIET`, `HOT`, `NIGHT`, `MORNING`, `LONG_DISTANCE`, `SHORT_DISTANCE`, `LOVE`, `PET`, `FAMILY`.

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

상품 목록의 기본 페이지 크기는 8이며 `id` 내림차순입니다. 등록·수정은 상품 이미지 파일을 포함할 수 있는 `multipart/form-data` 요청을 사용합니다.

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
| GET | `/community` | 공지사항 + 일반 게시글 목록 | 공개 |
| GET | `/community/hits` | 공지사항 + 인기 게시글 목록 | 공개 |
| GET | `/community/post/{id}` | 게시글 상세 | 공개 |
| GET | `/community/add` | 게시글 작성 페이지 | 필요 |
| POST | `/community/add` | 게시글 등록 | 필요 |
| GET | `/community/post/{id}/edit` | 게시글 수정 페이지 | 작성자/관리자 |
| POST | `/community/post/{id}/edit` | 게시글 수정 | 작성자/관리자 |
| POST | `/community/post/{id}/delete` | 게시글 삭제 | 작성자/관리자 |
| POST | `/community/post/{id}/like` | 게시글 추천 | 필요 |
| POST | `/comment/{postId}/add` | 댓글 등록 | 필요 |
| POST | `/comment/{id}/delete` | 댓글 삭제 | 작성자/관리자 |

### Talk 목록의 독립 페이징

두 목록 화면 모두 위쪽에 공지사항, 아래쪽에 게시글을 표시하며 HTML로 반환합니다.

| Query | 기본값 | 설명 |
|---|---|---|
| `page` | `0` | 게시글 페이지 번호 |
| `size` | `10` | 게시글 페이지 크기 |
| `sort` | `id,desc` | 게시글 정렬 |
| `noticePage` | `0` | 공지사항 페이지 번호. 음수는 0으로 처리 |

- 공지사항은 `noticePage`를 사용해 **3개씩 고정**, `id` 내림차순으로 조회합니다. 게시글의 `size`·`sort`는 공지사항 조회에 영향을 주지 않습니다.
- 공지 페이지 버튼은 현재 게시글 페이지·크기를 유지하고, 게시글 페이지 버튼은 현재 공지 페이지·게시글 크기를 유지합니다.
- HIT 목록은 추천 수 20 이상인 게시글을 조회합니다.

```http
GET /community?page=1&size=10&noticePage=2
GET /community/hits?page=1&size=10&noticePage=2
```

위 요청은 각각 게시글 두 번째 페이지와 공지사항 세 번째 페이지를 함께 표시합니다.

### 공지사항

| Method | Endpoint | 기능 | 인증 |
|---|---|---|---|
| GET | `/notice` | 기존 공지 목록 주소 → Talk 리다이렉트 | 공개 |
| GET | `/notice/{id}` | 공지 상세 | 공개 |
| GET | `/notice/add` | 공지 등록 페이지 | 관리자 |
| POST | `/notice/add` | 공지 등록 | 관리자 |
| GET | `/notice/{id}/edit` | 공지 수정 페이지 | 관리자 |
| POST | `/notice/{id}/edit` | 공지 수정 | 관리자 |
| POST | `/notice/{id}/delete` | 공지 삭제 | 관리자 |

- 별도 공지 목록 화면은 제거되었으며, 공지 목록은 Talk 일반·HIT 화면에 포함됩니다.
- `GET /notice?page=2`는 `302`로 `/community?noticePage=2`에 이동합니다. `page` 기본값은 0입니다.
- 등록·수정 성공 시 `/notice/{id}` 상세 화면으로, 삭제 성공 시 `/community`로 리다이렉트합니다. 관리자 권한이 없는 등록·수정·삭제 요청도 컨트롤러에서 `/community`로 돌려보냅니다. 비로그인 요청은 보안 설정에 의해 먼저 로그인 화면으로 이동할 수 있습니다.

---

## 6. 실시간 채팅

### HTTP API

기본 경로: `/walk/course/{courseId}/chat`

| Method | Endpoint | 기능 |
|---|---|---|
| GET | 기본 경로 | 채팅방 목록 |
| POST | 기본 경로 | 채팅방 생성 |
| GET | `/{roomId}` | 채팅방 입장 및 조회 |
| POST | `/{roomId}/join` | 채팅방 참여 |
| POST | `/{roomId}/leave` | 채팅방 퇴장 |
| GET | `/{roomId}/messages` | 이전 채팅 이력 조회 |

모든 채팅 HTTP API는 로그인이 필요합니다.

- 목록 파라미터: `page`(기본 0, 음수는 0), `mine`(기본 `false`). `mine=true`이면 참여 중인 방을 조회하며, 페이지 크기는 4로 고정됩니다.
- 방 생성 폼 필드: `title`. 생성·참여 성공 시 해당 방 화면으로 이동합니다.
- 채팅방 정원은 방장을 포함해 5명입니다. 방장이 퇴장하면 방이 종료되고 참여 정보가 삭제됩니다.
- 메시지 이력은 `before`가 없으면 최신 메시지, 있으면 해당 메시지 ID보다 작은 이전 메시지를 최대 30개씩 ID 내림차순으로 반환합니다. 응답은 `content`, `last` 등을 포함하는 `Slice` JSON입니다.

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
- 채팅방 화면 조회 시에는 코스 일치 여부·방 종료 여부·참여 여부를 검사합니다.
- 현재 메시지 이력 API와 STOMP 전송·구독에는 별도 참여 권한 검증이 구현되어 있지 않습니다. 화면의 입장 검사와 메시지 API의 권한 검증을 동일한 보장으로 보지 않습니다.
- 메시지 저장 후 해당 채팅방 구독자에게 브로드캐스트
- 이전 메시지는 커서 기반으로 최대 30개씩 조회

---

## 7. 이미지

### 이미지

| Method | Endpoint | 기능 | 인증 |
|---|---|---|---|
| GET | `/images/{fileName}` | 이미지 파일 조회 | 공개 |

상품 이미지 업로드는 상품 등록 및 수정 과정에서 처리합니다.

`/images/{fileName}`은 업로드 파일 조회용 컨트롤러 경로입니다. 로고·아이콘 같은 정적 자산은 `/images/branding/title.png`, `/images/icons/heart.svg`처럼 하위 폴더 경로를 사용해 이 컨트롤러와 구분합니다.
