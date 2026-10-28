# 예외 처리 기준

예상 가능한 업무 실패는 `BusinessException`의 하위 예외로 표현한다.
기술 장애나 프로그래밍 오류를 이 예외로 감싸서 사용자 오류로 바꾸지 않는다.

| 예외 | HTTP 상태 | 용도 |
| --- | --- | --- |
| `InvalidRequestException` | 400 | 잘못된 수량, 지원하지 않는 파일, 잘못된 요청 조합 |
| `AuthenticationRequiredException` | 401 | 로그인 정보 없음 |
| `PermissionDeniedException` | 403 | 로그인했으나 해당 작업의 권한 없음 |
| `ResourceNotFoundException` | 404 | 조회 대상 없음 |
| `BusinessConflictException` | 409 | 중복 추천, 정원 초과, 종료된 채팅방 등 현재 상태와 충돌 |
| `DuplicateMemberException` | 409 | 이메일·아이디 중복. `Field`로 오류 입력란 구분 |
| `NotEnoughStockException` | 409 | 재고 부족 |

## 처리 위치

- 기본 처리는 `GlobalExceptionHandler`에 둔다. HTTP 상태를 설정하고 일반 페이지는 오류 뷰, `@ResponseBody`·`@RestController` 요청이나 JSON을 요청한 클라이언트에는 `{status, message}`를 반환한다.
- 회원가입은 중복 필드에 `BindingResult` 오류를 표시한다. 오류 문구로 필드를 추측하지 않는다.
- 채팅 화면에서 업무 실패 후 목록으로 돌아가야 할 때는 `BusinessException`만 잡아 플래시 메시지를 표시한다. 이 흐름은 3xx 리다이렉트다.
- 코스 추천의 중복 안내처럼 별도 응답이 필요한 기존 흐름은 컨트롤러에서 구체적인 업무 예외만 잡는다.
- 입력 DTO의 `@Valid`와 폼 오류 표시는 유지한다.

## 서버 오류

파일 저장 실패, 외부 API 장애, 예상하지 못한 `IllegalStateException`·`IllegalArgumentException` 등은 500으로 처리한다. 원인 예외는 서버 로그에 기록하고 내부 경로나 외부 응답 원문은 사용자에게 노출하지 않는다.

Spring MVC가 정의한 요청 오류는 원래 HTTP 상태와 헤더를 유지한다. 정적 리소스가 없으면 404를 반환한다.

새 기능마다 예외 클래스를 만들기보다 위 분류를 먼저 사용한다. 별도 필드 정보나 별도 처리 흐름이 필요한 경우에만 구체적인 예외를 추가한다.
