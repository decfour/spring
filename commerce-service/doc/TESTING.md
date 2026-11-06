# 핵심 테스트

테스트는 업무 규칙·권한·동시성과 코스 상세 화면의 렌더링에 집중합니다. 각 테스트의 한글 `@DisplayName`으로 검증 목적을 확인할 수 있습니다.

| 위치 | 검증 범위 |
| --- | --- |
| `domain/purchase` | 주문 금액·상품 연결·중복 취소 시 재고 중복 복구 방지 |
| `service` | 로그인·회원 중복, 주문 생성·취소·재고, 채팅 참여·퇴장, 코스 평가·리뷰·쪽지·인기 목록, 본인 활동 조회 |
| `web/controller` | 리뷰·댓글 수정·삭제 권한, 주문·평가·쪽지 요청 거부, 리뷰 작성자의 별점, 코스·리뷰 상세 렌더링 |
| `exception` | 업무 오류의 상태 코드·HTML/JSON 응답, 내부 오류 정보 숨김 |

스타일 이름·아이콘 치수·단순 링크·메서드 전달만 확인하는 테스트와 검증 없는 조회 예제는 제외합니다. 비회원·일반 회원·작성자의 상세 화면, 별점 유무에 따른 리뷰 표시는 같은 테스트에 입력만 바꿔 확인합니다.

## 실행

```powershell
.\gradlew.bat test

# 별점 기능만 확인
.\gradlew.bat test --tests '*WalkCourseRatingServiceTest'
```

`MyServiceTest`, `PostConcurrencyTest`, `PurchaseConcurrencyTest`는 실제 MySQL `commerce_test`를 사용하는 통합 테스트입니다. 접속 설정은 `src/test/resources/application.properties`에 있습니다. 동시성 테스트는 테스트 DB 데이터를 정리하므로 운영 DB로 연결하지 않습니다.

기존 스키마에서 실행한다면 별점 테이블 생성 후 [별점 마이그레이션](../scripts/sql/migrate-course-rating-count.sql)을 **테스트 DB에도** 적용합니다. Hibernate의 `ddl-auto=update`는 삭제된 `like_count` 칼럼을 자동 제거하지 않습니다.

테스트 리포트: `build/reports/tests/test/index.html`.

k6는 별도의 부하 테스트입니다. 실행 시나리오는 [k6 안내](../k6/README.md), 원본 측정치는 `k6/results`에 보관합니다. 원본 JSON은 애플리케이션 실행이나 JUnit 테스트에 사용되지 않습니다.
