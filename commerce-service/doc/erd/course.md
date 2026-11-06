# 산책·채팅 데이터 구조

현재 JPA 엔티티 기준 구조입니다. 회원은 산책·채팅에서 참조하는 필드만 표시합니다.

```mermaid
erDiagram
    member ||--o{ walk_course : creates
    member ||--o{ walk_course_rating : rates
    member ||--o{ walk_course_review : writes
    member ||--o{ walk_course_pin : marks
    walk_course ||--o{ walk_course_tag : has
    walk_course ||--o{ walk_course_rating : receives
    walk_course ||--o{ walk_course_review : has
    walk_course ||--o{ walk_course_pin : has
    walk_course ||--o{ chat_room : has
    member ||--o{ chat_room : hosts
    chat_room ||--o{ chat_member : includes
    member ||--o{ chat_member : joins
    chat_room ||--o{ chat_message : contains
    member ||--o{ chat_message : sends

    member {
        bigint id PK
        varchar name
    }
    walk_course {
        bigint id PK
        bigint creator_id FK
        varchar title
        text content
        double start_lat
        double start_lng
        double end_lat
        double end_lng
        text route_data
        int distance
        int duration
        bigint rating_count
        datetime created_at
    }
    walk_course_tag {
        bigint id PK
        bigint walk_course_id FK
        varchar tag
    }
    walk_course_rating {
        bigint id PK
        bigint course_id FK
        bigint member_id FK
        int score
    }
    walk_course_review {
        bigint id PK
        bigint course_id FK
        bigint creator_id FK
        varchar title
        text content
        datetime created_at
    }
    walk_course_pin {
        bigint id PK
        bigint course_id FK
        bigint creator_id FK
        varchar title
        varchar content
        double latitude
        double longitude
        datetime created_at
    }
    chat_room {
        bigint id PK
        bigint walk_course_id FK
        bigint host_id FK
        varchar title
        varchar status
        datetime created_at
    }
    chat_member {
        bigint id PK
        bigint chat_room_id FK
        bigint member_id FK
        datetime joined_at
    }
    chat_message {
        bigint id PK
        bigint chat_room_id FK
        bigint sender_id FK
        varchar content
        datetime created_at
    }
```

- 별점은 `1~5`의 정수이며 `(course_id, member_id)` 조합은 유일합니다.
- `rating_count`는 저장된 평가 수입니다. 최초 평가·취소 시 코스를 잠그고 별점과 같은 트랜잭션으로 갱신합니다.
- 평균 평점은 저장 칼럼이 없으며 Hibernate `@Formula`로 조회 시 계산합니다.
- 리뷰와 별점은 독립적입니다. 리뷰 상세는 작성자의 현재 별점을 조회합니다.
- 코스 삭제 시 태그·평가·리뷰·핀은 엔티티의 삭제 전파 대상으로 처리합니다.
- 채팅 참여의 `(chat_room_id, member_id)` 조합은 유일합니다.
