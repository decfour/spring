# Don't Sell API Specification

현재 Access Token만 사용하는 구조 기준입니다.

## 1. Overview

- **Base URL:** `http://localhost:8080`
- **요청 Content-Type:** `application/json`
- **인증 방식:** JWT Bearer Token (HS256)
- **Access Token 유효기간:** 15분
- Refresh Token, 인증 쿠키, 서버 로그인 상태 저장은 사용하지 않습니다.

| 기능 | Method | Endpoint | 인증 | 성공 응답 |
|---|---|---|---|---|
| 회원가입 | POST | `/auth/sign-up` | 불필요 | 201 |
| 로그인 | POST | `/auth/sign-in` | 불필요 | 200 |
| 로그인 회원 ID 확인 | GET | `/auth/me` | Access Token | 200 |

## 2. 회원가입

`POST /auth/sign-up`

### Request

| 필드 | 타입 | 필수 | 검증 조건 |
|---|---|---|---|
| alias | String | O | 공백만 입력 불가, 최대 20자, 중복 불가 |
| email | String | O | 이메일 형식, 최대 254자, 중복 불가 |
| password | String | O | 공백만 입력 불가, 8~72자, UTF-8 기준 최대 72바이트 |

```json
{
  "alias": "물린사람",
  "email": "member@example.com",
  "password": "password123"
}
```

### Response — 201 Created

```json
{
  "id": 1,
  "alias": "물린사람",
  "email": "member@example.com"
}
```

비밀번호는 BCrypt 해시로 저장하며 응답하지 않습니다. 가입 후 별도로 로그인합니다.
현재 회원 저장까지 구현되어 있으며 계좌 생성과 초기 자금 지급은 포함하지 않습니다.

### Error

| 상태 코드 | 발생 조건 |
|---|---|
| 400 Bad Request | 필수값 누락 또는 입력값 검증 실패 |
| 409 Conflict | 사전 검사에서 이메일 또는 별명 중복 확인 |

동시 가입으로 사전 검사를 통과한 DB UNIQUE 오류는 현재 별도의 409 변환 처리가 없습니다.

## 3. 로그인

`POST /auth/sign-in`

### Request

| 필드 | 타입 | 필수 | 검증 조건 |
|---|---|---|---|
| email | String | O | 이메일 형식, 최대 254자 |
| password | String | O | 최대 200자, 공백만 입력 불가 |

```json
{
  "email": "member@example.com",
  "password": "password123"
}
```

가입할 때 입력한 원래 비밀번호를 전송합니다. DB의 BCrypt 해시를 입력하지 않습니다.

### Response — 200 OK

```json
{
  "accessToken": "<JWT>"
}
```

현재 응답은 `accessToken` 하나입니다. `tokenType`, `expiresIn`, `refreshToken`은 반환하지 않습니다.
HTML 페이지는 토큰을 현재 탭의 `sessionStorage`에 보관합니다.

### Error

| 상태 코드 | 발생 조건 |
|---|---|
| 400 Bad Request | 필수값 누락 또는 입력값 검증 실패 |
| 401 Unauthorized | 이메일 또는 비밀번호 불일치 |

## 4. JWT 인증 및 회원 ID 확인

`GET /auth/me`

```http
Authorization: Bearer <accessToken>
```

### Response — 200 OK

```http
Content-Type: text/plain;charset=UTF-8

1
```

JWT의 `sub`에 담긴 회원 ID를 문자열로 반환합니다. 회원 객체나 별명은 반환하지 않으며 회원 DB를 재조회하지 않습니다.

- `JwtTokenProvider`가 서명 키와 JWT 발급·검증을 담당합니다.
- 발급자는 `dontsell`, `sub`는 회원 ID입니다.
- Spring Security가 서명·발급자·시간 유효성을 검증합니다.
- 토큰 누락·만료·검증 실패 시 401입니다.
- `api.js`의 `authFetch()`가 저장된 토큰을 Bearer 헤더에 넣습니다.
- 서버 세션, Refresh Token 쿠키, CSRF 토큰 발급·재발급 API는 사용하지 않습니다.

## 5. 로그아웃

현재 서버 로그아웃 API는 없습니다.

```javascript
sessionStorage.removeItem('accessToken');
window.location.replace('/sign-in.html');
```

홈과 채팅방의 버튼은 현재 탭의 토큰을 삭제합니다.
이미 복사된 토큰이나 다른 탭의 토큰은 만료 전까지 유효합니다.

## 6. 실행 설정 및 오류 응답

필수 환경 변수는 `DB_PASSWORD`, `JWT_SECRET`입니다.
비밀키는 무작위 문자열을 사용하고 UTF-8 기준 최소 32바이트여야 합니다.

공통 예외 응답 DTO는 아직 없으므로 오류 본문의 `message` 유무는 보장하지 않습니다.
현재 HTML은 상태 코드로 실패 원인을 구분합니다.

과거 인증용 `auth_session` 테이블은 현재 코드에서 사용하지 않습니다.
이미 적용한 Flyway 이력과 충돌하지 않도록 V2 마이그레이션은 유지합니다.
