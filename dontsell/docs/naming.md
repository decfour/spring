# DB 명명 규칙

- 테이블: 소문자 단수형. 여러 단어는 `snake_case`. 같은 기능에 속하는 테이블은 `chat_room`, `chat_message`처럼 공통 접두어를 유지.
- 기본 키: 모든 테이블에서 `id`.
- 외래 키: 참조하는 테이블 이름 + `_id` (`member_id`, `account_id`, `chat_room_id`).
- 일반 컬럼: 소문자 `snake_case`. 의미가 분명한 짧은 단어 사용.
- 약어: `id`, `avg`처럼 의미가 익숙한 경우만 사용. `quantity`, `password` 등은 유지.
- 시각: 의미 + `_at` (`created_at`, `updated_at`, `executed_at`).
- Java 필드: `camelCase` (`avgPrice`, `createdAt`). DB 이름과 다르면 매핑에 명시.
- 적용된 Flyway 파일은 수정하지 않고 새 버전에서 변경.

| 테이블 | 컬럼 |
| --- | --- |
| member | id, alias, email, password, created_at, updated_at |
| account | id, member_id, balance, created_at |
| stock | id, symbol, name, exchange |
| holding | id, account_id, stock_id, quantity, avg_price |
| trade | id, account_id, stock_id, type, quantity, price, executed_at |
| chat_room | id, name, stock_id, member_id, created_at |
| chat_message | id, chat_room_id, member_id, content, created_at |
| auth_session | id, member_id, token_hash, expires_at |
