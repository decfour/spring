# API 명세서 (공개용)

| 항목 | 내용 |
| --- | --- |
| 서비스 | The Hitchhiker's Guide to Earth — 산책 경로 공유 서비스 |
| 문서 기준일 | 2026-10-18 |
| 기준 | 제공된 원문에 기록된 현재 API 동작 |
| 로컬 주소 | `http://localhost:8080` |
| 공통 접두사 | 없음 |
| 범위 | 사용자 기능 HTTP 엔드포인트, WebSocket/STOMP |

> 이 문서는 제공된 원문을 바탕으로 작성한 공개용 요약입니다. 운영 환경 정보·내부 보안 점검 상세·운영 관리 API는 공개 범위에서 제외했습니다. 이용 조건 표기는 모든 경로의 권한 검사 구현을 보증하지 않습니다. 소스 코드를 추가 검증한 문서는 아니며, 1~10절은 현재 동작, 11절은 미구현 개선 제안입니다.

## 목차

1. [공통 계약](#1-공통-계약)
2. [회원·인증](#2-회원인증)
3. [산책 코스](#3-산책-코스)
4. [상품·리뷰](#4-상품리뷰)
5. [장바구니·주문](#5-장바구니주문)
6. [커뮤니티·댓글·공지](#6-커뮤니티댓글공지)
7. [채팅·WebSocket](#7-채팅websocket)
8. [이미지](#8-이미지)
9. [오류 처리](#9-오류-처리)
10. [연동 주의사항](#10-연동-주의사항)
11. [개선 과제](#11-개선-과제)

## 1. 공통 계약

### 요청·응답 표기

| 유형 | 요청 | 정상 응답 |
| --- | --- | --- |
| 화면 조회 | GET 경로·쿼리 | 보통 `200 text/html` |
| 폼 처리 | `application/x-www-form-urlencoded` | 보통 `302` + `Location`; 검증 실패 시 HTML 또는 리다이렉트 |
| 이미지 포함 상품 폼 | `multipart/form-data` | HTML 또는 리다이렉트 |
| JSON 처리 | `application/json` | JSON 객체·숫자 또는 빈 본문; 개별 계약 참고 |
| 이미지 조회 | GET | 파일 리소스; MIME 타입은 리소스 처리에 따름 |

- 문자열은 UTF-8입니다. 아래 표에서 `HTML`은 화면 응답, `302 경로`는 해당 경로로의 리다이렉트입니다.
- 경로의 `{id}`, `{courseId}`, `{roomId}`, `{itemId}`, `{reviewId}`, `{cartItemId}`, `{postId}`는 별도 설명이 없으면 필수 정수 식별자(Java `Long`)입니다.
- 폼 DTO의 `id`는 신규 등록에 필요하지 않으며, 수정 대상은 URL의 ID를 기준으로 합니다.

### 인증·권한

| 항목 | 현재 계약 |
| --- | --- |
| 인증 수단 | 로그인 시 발급하는 `Authorization=<JWT>` 쿠키 |
| 쿠키 속성 | `HttpOnly`, `Path=/`, `Max-Age=1800` |
| 토큰 유효기간 | 공통 설정 기준 30분 |
| HTTP 인증 헤더 | `Authorization: Bearer ...` 미지원. 필터는 쿠키만 읽음 |
| 세션·토큰 갱신 | 서버 세션 미사용. 갱신 토큰·재발급 API 없음 |
| 미인증 접근 | 보호 경로는 보통 `302 /member/sign-in?redirectURL=...`; JSON 요청도 동일 |
| 역할 | `USER`, `ADMIN` |

이용 조건의 **공개**는 미인증 사용 가능, **로그인**은 인증 필요, **작성자/관리자**는 둘 중 하나를 뜻합니다. **본인·작성자·관리자**는 해당 사용자를 대상으로 하는 기능입니다.


### 페이징

`Pageable` 목록은 `page`(0부터), `size`, `sort=필드,asc|desc`를 사용합니다. 기본 크기는 최대 크기를 뜻하지 않습니다. 저장소의 고정 정렬이 우선할 수 있어 임의 필드 정렬은 보장하지 않습니다.

| 목록 | 기본 크기 | 정렬·예외 |
| --- | --- | --- |
| 주변 코스 | 3 | 출발점까지 거리 오름차순 |
| 인기 코스 | 3 | 추천 수 내림차순 → 생성일 내림차순 |
| 상품 | 8 | `id,desc` |
| 리뷰·게시글·댓글·공지 | 10 | Pageable 기본 `id,desc` |
| 인기 게시글 | 10 | 추천 20개 이상, 추천 수 내림차순 |
| 내 코스·상품·리뷰·주문 | 7 | Pageable 기본 `id,desc`; 내 코스는 저장소에서 생성일 내림차순 |
| 채팅방 | 4 고정 | 일반 목록은 생성일·ID 내림차순. 음수 `page`는 0으로 보정 |
| 채팅 이력 | 30 고정 | ID 내림차순, `before` 커서 사용 |

JSON 목록은 Spring Data `Page`/`Slice`를 직접 직렬화합니다. `content`, `number`, `size`, `numberOfElements`, `first`, `last`, `empty`, `sort`, `pageable` 등이 포함되며 `Page`에는 `totalElements`, `totalPages`도 있습니다. 내부 메타데이터는 고정 DTO 계약이 아니며 `Slice`는 전체 건수를 제공하는 계약이 아닙니다.

## 2. 회원·인증

### 엔드포인트

| 메서드 | 경로 | 이용 조건 | 입력 | 정상 응답 |
| --- | --- | --- | --- | --- |
| GET | `/` | 공개 | 없음 | 홈 HTML |
| GET | `/member/sign-on` | 공개 | 없음 | 가입 폼 HTML |
| POST | `/member/sign-on` | 공개 | 가입 폼 | `302 /` |
| GET | `/member/sign-in` | 공개 | `redirectURL` 선택, 기본 `/` | 로그인 폼 HTML |
| POST | `/member/sign-in` | 공개 | 로그인 폼, `redirectURL` 선택 | 인증 쿠키 발급, `302 {redirectURL}` |
| POST | `/member/sign-out` | 로그인 | 없음 | 인증 쿠키 만료, `302 /` |
| GET | `/member/my-page` | 로그인 | 없음 | 마이페이지 HTML |
| GET | `/member/my-course` | 로그인 | Pageable | 내 코스 HTML |
| GET | `/member/my-item` | 로그인 | Pageable | 내 상품 HTML |
| GET | `/member/my-review` | 로그인 | Pageable | 내 리뷰 HTML |
| GET | `/member/my-order` | 로그인 | Pageable | 내 주문 HTML |

### 입력·예외

| 폼 | 필드 | 타입 | 필수·제약 |
| --- | --- | --- | --- |
| 가입 | `name` | string | 필수, 공백 불가, 2~10자, 한글·영문·숫자 |
| 가입 | `email` | string | 필수, 공백 불가, 이메일 형식, 최대 100자 |
| 가입 | `signInId` | string | 필수, 공백 불가, 4~20자, 영문·숫자·`_` |
| 가입 | `signInPassword` | string | 필수, 공백 불가, 8~20자 |
| 로그인 | `signInId`, `signInPassword` | string | 필수(`@NotEmpty`) |

- 가입 역할은 `USER`입니다. 검증 실패 시 가입 폼을 재표시합니다. 중복 가입은 서비스 예외에 따라 필드 오류 또는 공통 HTML 오류가 됩니다.
- 로그인 불일치는 `아이디 또는 비밀번호가 맞지 않습니다.` 오류가 있는 로그인 HTML이며, JSON 401 계약이 아닙니다.
- `redirectURL` 예시는 서비스 내부 상대 경로(`/`)를 사용합니다.

### 로그인 예시

```http
POST /member/sign-in HTTP/1.1
Content-Type: application/x-www-form-urlencoded

signInId=example_user&signInPassword=ExamplePass123&redirectURL=/
```

자격증명이 유효한 경우의 응답 형태입니다. 예시 계정이 실제로 존재한다는 뜻은 아닙니다.

```http
HTTP/1.1 302
Set-Cookie: Authorization=<JWT>; Path=/; Max-Age=1800; HttpOnly
Location: /
```

## 3. 산책 코스

### 엔드포인트

| 메서드 | 경로 | 이용 조건 | 입력 | 정상 응답 |
| --- | --- | --- | --- | --- |
| GET | `/walk` | 공개 | 없음 | 산책 메인 HTML |
| GET | `/walk/course/nearby` | 공개 | `lat`, `lng`, Pageable | `200` JSON `Page<WalkCourseResponse>` |
| GET | `/walk/course/hit` | 공개 | Pageable | 인기 코스 HTML |
| GET | `/walk/course/{id}` | 공개 | 코스 ID | 코스 상세 HTML |
| GET | `/walk/course/add` | 로그인 | 없음 | 등록 폼 HTML |
| POST | `/walk` | 로그인 회원 사용 | 코스 폼 | `302 /walk` |
| POST | `/walk/course/{id}/delete` | 작성자/관리자 | 코스 ID | `302 /walk` |
| POST | `/walk/course/route` | 로그인 | 경로 계산 JSON | `200` JSON `WalkRouteResult` |
| POST | `/walk/course/{id}/like` | 로그인 | 본문 없음 | `200` JSON 정수(추천 수) |
| POST | `/walk/course/{id}/tag` | 로그인 | `{"tag":"NATURE"}` | `200` 빈 본문 |

코스 수정 엔드포인트는 없습니다. 삭제 권한이 없으면 코스 상세로 이동하며, 등록 폼 검증 실패는 폼 HTML을 반환합니다.

### 주변 코스 조회

`lat`, `lng`는 필수 double입니다. 기준 좌표에서 **출발점이 1,000m 이내**인 코스를 구면거리로 조회하여 가까운 순으로 반환합니다. 검색 반경을 변경하는 파라미터는 없습니다.

```http
GET /walk/course/nearby?lat=37.5665&lng=126.9780&page=0&size=3
Accept: application/json
```

`content` 원소의 필드:

| 필드 | JSON 타입 | 의미 |
| --- | --- | --- |
| `id` | integer | 코스 ID |
| `title`, `content` | string | 제목, 후기 |
| `startLat`, `startLng` | number | 출발 위도·경도 |
| `endLat`, `endLng` | number | 도착 위도·경도 |
| `routeData` | string | 좌표 배열을 직렬화한 JSON 문자열 |
| `distance`, `duration` | integer | 저장된 거리·소요시간 값; 단위는 원문 구현에 명시되지 않음 |
| `likeCount` | integer | 추천 수 |

### 코스 등록 입력

| 필드 | 타입 | 필수·제약 |
| --- | --- | --- |
| `title` | String | 필수, 공백 불가, 최대 30자 |
| `content` | String | 선택, 최대 500자 |
| `startLat`, `startLng`, `endLat`, `endLng` | Double | 좌표 값; 유효한 위도·경도 전달 |
| `routeData` | String | 경로 JSON 문자열 |
| `distance`, `duration` | Integer | 거리·소요시간 값 |

좌표·경로·거리·소요시간의 생략 가능 여부는 이 공개 문서에서 확정하지 않습니다. 정상 등록에 필요한 값을 전달하세요.

### 경로 계산 예시

네 좌표는 Double이며 정상 연동을 위해 모두 전달해야 합니다.

```http
POST /walk/course/route
Cookie: Authorization=<JWT>
Content-Type: application/json

{"startLat":37.5665,"startLng":126.978,"endLat":37.57,"endLng":126.982}
```

응답 본문 예시:

```json
{"routeData":"[[126.978,37.5665],[126.982,37.57]]","distance":800,"duration":600}
```

`routeData`는 **배열이 아닌 문자열**입니다. 좌표 순서와 거리·시간 값은 외부 응답을 변환 없이 사용하며, 거리·시간의 단위는 명시되어 있지 않습니다. 외부 연동 실패는 정형 JSON 오류로 변환되지 않습니다.

### 추천·태그 예외

- 추천 성공 본문 예시는 `12`입니다. 회원별 중복 추천은 `409 application/json`, `{"message":"이미 추천했습니다"}`입니다.
- 태그 허용값: `NATURE`, `CITY`, `QUIET`, `HOT`, `NIGHT`, `MORNING`, `LONG_DISTANCE`, `SHORT_DISTANCE`, `LOVE`, `PET`, `FAMILY`.
- 잘못된 enum·중복 태그·없는 코스 등의 처리된 입력 오류는 `400` 문자열 본문입니다. 예: `이미 등록된 태그입니다.`
- `tag`를 누락하지 마세요. 잘못된 요청의 오류 형식은 9절도 참고하세요.

## 4. 상품·리뷰

### 엔드포인트

| 메서드 | 경로 | 이용 조건 | 입력 | 정상 응답 |
| --- | --- | --- | --- | --- |
| GET | `/shop` | 공개 | Pageable | 상품 목록 HTML |
| GET | `/shop/item/{id}` | 공개 | 상품 ID | 상품 상세 HTML |
| GET | `/shop/add` | 로그인 | 없음 | 상품 폼 HTML |
| POST | `/shop/add` | 로그인 | 상품 폼 | `302 /shop/item/{생성ID}` |
| GET | `/shop/item/{id}/edit` | 작성자/관리자 | 상품 ID | 상품 수정 폼 HTML |
| POST | `/shop/item/{id}/edit` | 작성자/관리자 | 상품 폼 | `302 /shop/item/{id}` |
| POST | `/shop/item/{id}/delete` | 작성자/관리자 | 상품 ID | `302 /shop` |
| POST | `/shop/item/{id}/restore` | 작성자 | 상품 ID | `302 /shop/item/{id}` |
| GET | `/shop/item/{itemId}/review` | 공개 | Pageable | 리뷰 목록 HTML |
| GET | `/shop/item/{itemId}/review/{reviewId}` | 공개 | 경로 ID | 리뷰 상세 HTML |
| GET | `/shop/item/{itemId}/review/add` | 로그인 | 상품 ID | 리뷰 폼 HTML |
| POST | `/shop/item/{itemId}/review/add` | 로그인 | 리뷰 폼 | `302 /shop/item/{itemId}/review/{생성ID}` |
| GET | `/shop/item/{itemId}/review/{reviewId}/edit` | 작성자 | 경로 ID | 리뷰 수정 폼 HTML |
| POST | `/shop/item/{itemId}/review/{reviewId}/edit` | 작성자 | 리뷰 폼 | `302` 리뷰 상세 |
| POST | `/shop/item/{itemId}/review/{reviewId}/delete` | 작성자/관리자 | 경로 ID | `302` 리뷰 목록 |

### 상품 폼

| 필드 | 타입 | 필수·제약 |
| --- | --- | --- |
| `name` | string | 필수, 공백 불가, 최대 30자 |
| `price` | integer | 필수, 100~10,000,000원 |
| `stock` | integer | 필수, 0~9,999 |
| `description` | string | 선택 |
| `open` | boolean | 기본 true |
| `deleted` | boolean | 기본 false |
| `itemType` | enum | 필수: `BOOK`, `VIDEO`, `ETC` |
| `deliveryType` | enum | 필수: `STANDARD`, `EXPRESS`, `FREE` |
| `imageFile` | binary | 선택, multipart 파일 |

업로드는 상품 폼에서 처리하며 별도 이미지 업로드 API는 없습니다. 파일당·요청 전체 제한은 **각각 5MB**입니다. 일반 등록·수정에서는 `deleted` 기본값을 사용하고, 삭제·복원은 전용 경로를 사용합니다.

### 리뷰 입력·예외

- `title`, `content`: 필수 string, 공백 불가. DTO 최대 길이 제약 없음. `id`는 선택 Long입니다.
- 상품·리뷰 검증 실패 시 해당 폼 HTML을 반환합니다.
- 변경 권한이 없으면 상품은 상품 상세, 리뷰는 리뷰 상세로 리다이렉트합니다. 관리자도 **상품 복원·리뷰 수정에는 작성자 권한**이 필요합니다.

## 5. 장바구니·주문

### 엔드포인트

| 메서드 | 경로 | 이용 조건 | 입력 | 정상 응답 |
| --- | --- | --- | --- | --- |
| GET | `/cart` | 로그인 | 없음 | 장바구니 HTML |
| POST | `/cart/item/add` | 로그인 | `itemId` Long, `itemCount` int 필수 | `302 /shop/item/{itemId}` |
| POST | `/cart/item/{cartItemId}/delete` | 본인 | 항목 ID | `302 /cart` |
| GET | `/order/one` | 로그인 | `itemId` Long, `itemCount` int 필수 | 주문 폼 HTML |
| GET | `/order/cart` | 로그인 | 없음 | 주문 폼 HTML; 빈 장바구니면 `302 /cart` |
| POST | `/order/create` | 로그인 | 주문 생성 폼 | `302 /member/my-order` |
| POST | `/order/{id}/cancel` | 본인 | 주문 ID | `302 /member/my-order` |

### 주문 생성 입력

| 필드 | 타입 | 필수·제약 |
| --- | --- | --- |
| `purchaseType` | string | 필수, `ONE` 또는 `CART` |
| `itemId` | Long | `ONE` 시 필요 |
| `quantity` | Integer | `ONE` 시 필요, 1 이상 |
| `receiverName` | string | 필수 파라미터 |
| `receiverAddress` | string | 필수 파라미터 |

`CART`는 로그인 회원의 장바구니 전체로 주문하며 `itemId`, `quantity`를 사용하지 않습니다. 장바구니 추가·단품 주문 화면 조회는 `itemCount`, 주문 생성은 **`quantity`**를 사용합니다.

### 업무 규칙·예외

- 같은 상품을 장바구니에 추가하면 수량을 합산하여 재고와 비교합니다. 요청 수량은 양수로 전달하세요.
- 주문은 현재 상품 가격을 사용합니다. 판매 중단·삭제 상품, 0 이하 수량, 재고 부족, 빈 장바구니 주문은 허용하지 않습니다.
- 장바구니 주문 성공 시 장바구니를 비우고, 실패는 트랜잭션 롤백 대상입니다.
- 상태는 `ORDER` → 취소 시 `CANCEL`입니다. 취소 시 재고를 복원하며 재취소로 중복 복원하지 않습니다.
- 타인 주문 취소 요청은 주문 목록으로 리다이렉트합니다.
- 결제 승인·환불 API, 주문 생성 멱등성 키 계약은 없습니다. 생성 응답은 주문 ID JSON이 아닙니다.

### 단품 주문 예시

```http
POST /order/create
Cookie: Authorization=<JWT>
Content-Type: application/x-www-form-urlencoded

purchaseType=ONE&itemId=1&quantity=1&receiverName=Hong&receiverAddress=Seoul
```

정상 처리 응답:

```http
HTTP/1.1 302
Location: /member/my-order
```

## 6. 커뮤니티·댓글·공지

### 엔드포인트

| 메서드 | 경로 | 이용 조건 | 입력 | 정상 응답 |
| --- | --- | --- | --- | --- |
| GET | `/community` | 공개 | Pageable | 게시글 목록 HTML |
| GET | `/community/hits` | 공개 | Pageable | 인기 게시글 HTML |
| GET | `/community/post/{id}` | 공개 | 댓글 Pageable | 게시글 상세 HTML; 조회수 증가 |
| GET | `/community/add` | 로그인 | 없음 | 게시글 폼 HTML |
| POST | `/community/add` | 로그인 | 게시글 폼 | `302 /community` |
| GET | `/community/post/{id}/edit` | 작성자/관리자 | 게시글 ID | 수정 폼 HTML |
| POST | `/community/post/{id}/edit` | 작성자/관리자 | 게시글 폼 | `302` 게시글 상세 |
| POST | `/community/post/{id}/delete` | 작성자/관리자 | 게시글 ID | `302 /community` |
| POST | `/community/post/{id}/like` | 로그인 | 본문 없음 | `302` 게시글 상세 |
| POST | `/comment/{postId}/add` | 로그인 | 댓글 폼 | `302 /community/post/{postId}` |
| POST | `/comment/{id}/delete` | 작성자/관리자 | 댓글 ID | `302` 소속 게시글 상세 |
| GET | `/notice` | 공개 | Pageable | 공지 목록 HTML |
| GET | `/notice/{id}` | 공개 | 공지 ID | 공지 상세 HTML |
| GET | `/notice/add` | 관리자 | 없음 | 공지 폼 HTML |
| POST | `/notice/add` | 관리자 | 공지 폼 | `302 /notice/{생성ID}` |
| GET | `/notice/{id}/edit` | 관리자 | 공지 ID | 수정 폼 HTML |
| POST | `/notice/{id}/edit` | 관리자 | 공지 폼 | `302 /notice/{id}` |
| POST | `/notice/{id}/delete` | 관리자 | 공지 ID | `302 /notice` |

### 입력·예외

| 폼 | 필드 | 필수·제약 |
| --- | --- | --- |
| 게시글 | `title` | 필수, 공백 불가, 최대 30자 |
| 게시글 | `content` | 필수, 공백 불가 |
| 댓글 | `content` | 필수, 공백 불가, 최대 500자 |
| 공지 | `title`, `content` | 모두 필수, 공백 불가 |

- 게시글·공지 검증 실패는 폼 HTML, 댓글 검증 실패는 오류 JSON 없이 게시글 상세로 리다이렉트합니다.
- 게시글·댓글 변경 권한이 없으면 게시글 상세로, 공지 관리 권한이 없으면 공지 목록으로 리다이렉트합니다.
- 게시글 추천은 JSON이 아닌 리다이렉트입니다. 중복 추천은 공통 HTML 오류 처리됩니다.
- 인기 게시글은 추천 수 20 이상이 대상입니다.

## 7. 채팅·WebSocket

### 채팅방 HTTP 엔드포인트

모든 경로는 로그인 필요입니다. 표의 `B`는 **`/walk/course/{courseId}/chat`**입니다.

| 메서드 | 경로 | 추가 조건·입력 | 정상 응답 |
| --- | --- | --- | --- |
| GET | `B` | `page` int 기본 0, `mine` boolean 기본 false | 채팅방 목록 HTML |
| POST | `B` | 폼 `title` 필수, 공백 불가, 1~30자 | `302 B/{생성roomId}` |
| POST | `B/{roomId}/join` | 본문 없음; 코스 소속·OPEN 검사 | `302 B/{roomId}` |
| GET | `B/{roomId}` | 참여자; 코스 소속·OPEN 검사 | 채팅방 HTML |
| POST | `B/{roomId}/leave` | 참여자; 본문 없음, 코스 소속·OPEN 검사 | `302 B` |
| GET | `B/{roomId}/messages` | `before` Long 선택 | `200` JSON `Slice<ChatMessageResponse>` |

- 일반 목록은 OPEN 방, `mine=true`는 참여 중인 방 조회 분기입니다.
- 생성자는 자동 참여하며 방장 포함 최대 5명, 중복 참여 불가입니다.
- 방장이 퇴장하면 방을 닫고 모든 참여 정보를 삭제합니다.
- 생성·참여·상세·퇴장의 업무 검증 실패는 주로 목록 리다이렉트와 flash `error`로 전달합니다. 생성 실패에는 `draftTitle`도 전달합니다.
- 없는 코스 조회를 포함한 오류 응답은 9절을 참고하세요.

### 메시지 이력

`before` 생략 시 최신 30개, 전달 시 `id < before`인 메시지 최대 30개를 **ID 내림차순**으로 반환합니다. 다음 요청에는 받은 메시지의 최소 ID를 `before`로 전달합니다. `page`, `size`는 이 API의 입력 계약이 아닙니다.

```http
GET /walk/course/10/chat/20/messages?before=100
Cookie: Authorization=<JWT>
Accept: application/json
```

응답 `content` 배열의 원소 예시입니다. 같은 구조가 STOMP 수신 본문에도 사용됩니다.

```json
{"id":99,"senderId":7,"senderName":"산책자","content":"입구에서 만나요.","createdAt":"2026-10-18T14:30:00"}
```

| 필드 | 타입 | 설명 |
| --- | --- | --- |
| `id`, `senderId` | integer | 메시지·발신자 ID |
| `senderName`, `content` | string | 발신자 이름·메시지 |
| `createdAt` | string | LocalDateTime, UTC offset 없음 |


### WebSocket / STOMP 계약

| 항목 | 계약 |
| --- | --- |
| 연결 | `/ws/chat` |
| 프로토콜 | WebSocket 위 STOMP, SockJS 엔드포인트 없음 |
| 인증 | HTTP 핸드셰이크의 JWT 쿠키 인증 |
| Origin | 로컬 개발 주소는 `http://localhost:8080`. 배포 환경의 허용 Origin은 별도 설정 확인 |
| SEND | `/app/chat/{roomId}/send` |
| SUBSCRIBE | `/topic/chat/{roomId}` |
| 발행 본문 | `{"content":"입구에서 만나요."}` |
| 수신 본문 | 위 `ChatMessageResponse` 구조 |
| 메시지 제약 | 공백 불가, 1~1000자 |

발신자는 인증 정보로 서버가 결정하므로 `senderId`를 보낼 필요가 없습니다. 저장 후 같은 방 topic으로 브로드캐스트합니다. 전용 STOMP 오류 DTO·메시지 ACK·재전송 중복 제거 계약은 없습니다.


## 8. 이미지

| 메서드 | 경로 | 이용 조건 | 응답 |
| --- | --- | --- | --- |
| GET | `/images/{fileName}` | 공개 | 파일 리소스 |

MIME 타입과 없는 이미지의 응답은 리소스 처리에 따르며 별도 오류 DTO는 없습니다. 운영 관리 엔드포인트와 프로파일별 노출 설정은 이 공개 문서의 범위에서 제외합니다.

## 9. 오류 처리

공통 `ApiResponse`·`errorCode` JSON 스키마는 없습니다. **상태 코드, Content-Type, 본문, Location을 함께 확인**해야 합니다.

| 상황 | 현재 처리 |
| --- | --- |
| 보호 경로 미인증·JWT 만료 | 보통 로그인 화면으로 `302` |
| 폼 검증 실패 | HTML 재표시 또는 개별 리다이렉트 |
| 중복 회원·재고 부족·입력·업무 규칙 오류 | `error/4xx` HTML, 화면 모델 `errorMessage` |
| 일반 `Exception` | `error/5xx` HTML, 메시지 `서버 오류` |
| 리소스 미존재 예외 | 명시적 `404`, 빈 본문 |
| 산책 코스 중복 추천 | 명시적 `409` JSON |
| 태그의 잡힌 `IllegalArgumentException` | 명시적 `400` 문자열 |
| 주문의 `PermissionDeniedException` | 주문 목록 리다이렉트 |

`error/4xx`, `error/5xx`는 **템플릿 이름**입니다. 공통 핸들러에 HTTP 상태 설정이 없어 **200 오류 화면이 될 수 있습니다**. 필수 파라미터 누락·타입 변환 실패·잘못된 JSON·`ResponseStatusException`도 일괄 400/404 JSON을 보장하지 않습니다. 화면 모델의 `errorMessage`가 JSON `message` 필드로 반환되는 것은 아닙니다.

## 10. 연동 주의사항

- 요청 예시는 테스트 환경에서 사용하고 유효한 계정·쿠키·대상 ID를 준비하세요. 예시 값은 운영 자격증명이 아닙니다.
- 자동 리다이렉트 후의 `200 HTML`은 로그인 성공을 뜻하지 않습니다. 상태 코드·Content-Type·Location과 응답 내용을 함께 확인하세요.
- 거리·시간의 단위를 추정하지 마세요. 단위는 현재 원문에 명시되어 있지 않습니다.
- 주문 생성에는 멱등성 키 계약이 없으므로 중복 제출을 주의하세요.
- 접근 제어의 보안 검증 결과와 운영 설정은 이 문서의 공개 범위에 포함하지 않습니다.

## 11. 개선 과제

> 아래는 **미구현 제안**입니다. 현재 제공되는 API 계약으로 해석하지 마세요.

| 영역 | 개선 과제 |
| --- | --- |
| 오류 처리 | JSON 공통 오류 DTO·명확한 HTTP 상태 코드 도입, HTML 예외 처리와 분리 |
| 응답 계약 | Page/Slice 대신 고정 응답 DTO 도입; 거리·시간 단위 명문화 |
| 주문 | 중복 제출 정책 및 멱등성 키 검토 |



