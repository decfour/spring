# 🛒 Commerce Service
> **Spring Boot와 Spring Data JPA 기반 이커머스 시스템**

## Key Objectives
* **아키텍처 분리**: Controller-Service-Repository 계층 간 책임을 명확히 분리하여 비즈니스 로직의 독립성 확보.
* **데이터 무결성**: `@Transactional` 중심의 원자성 보장 및 멀티스레드 환경의 동시성 이슈 제어.
* **성능 최적화**: 지연 로딩(`LAZY`), `Fetch Join`, `Batch Size` 메커니즘을 통한 N+1 쿼리 문제 해결.
* **보안 인프라**: Spring Security와 JWT를 결합한 중앙 집중형 인증/인가 체계 및 통합 예외 설계.
* **클라우드 배포**: AWS EC2 및 Ubuntu 환경 활용, 웹 애플리케이션 실제 구동 환경 인프라 배포.

## Tech Stack
* **Language**: Java 17
* **Framework**: Spring Boot 3.2.5, Spring Security 6
* **ORM**: Spring Data JPA (Hibernate)
* **Database**: MySQL 8.0.44
* **Build Tool**: Gradle
* **Template Engine**: Thymeleaf
* **Infrastructure**: AWS EC2

---

## Main Features & Technical Achievements

### 1. Spring Security & JWT 기반 무상태(Stateless) 인증 체계
* **Stateless 인증**: `HttpSession`을 탈피하고 분산 환경 확장에 최적화된 **JWT 기반 무상태 시스템** 구현.
* **보안 필터 체인 제어**: `OncePerRequestFilter` 기반 커스텀 JWT 필터를 구현하고, 미인증 접근 시 `AuthenticationEntryPoint`를 통해 안전한 로그인 리다이렉트 처리.
* **세션 독립적 데이터 바인딩**: 커스텀 ArgumentResolver(`@Login`)를 설계하여 컨트롤러와 Thymeleaf 뷰 레이어에 인증 객체를 세션 없이 안전하게 바인딩.

### 2. 도메인 정합성 중심의 JPA 서비스 레이어 설계
* **트랜잭션 원자성 확보**: 주문 생성, 재고 차감, 장바구니 비우기 등 복합 로직을 단일 트랜잭션으로 묶어 예외 발생 시 롤백 보장.
* **글로벌 예외 처리기**: `@ControllerAdvice` 기반 셰어드 아키텍처를 구축하여 도메인 예외를 중앙 집중 제어하고 응답 규격 통일.
* **변경 감지(Dirty Checking)**: 명시적 Update 쿼리 호출을 지양하고, 영속성 컨텍스트의 메커니즘을 활용해 안전하게 변경 사항 반영.

### 3. 분산 환경 동시성(Concurrency) 검증 및 제어
* **멀티스레드 부하 테스트**: `ExecutorService`와 `CountDownLatch`를 활용해 100명의 동시 주문 레이스 컨디션 테스트 케이스 자원 구축.
* **비관적 락(`PESSIMISTIC_WRITE`)**: 동시 경합으로 인한 재고 음수(-) 현상 및 데이터 유실을 방지하고자 DB 레벨의 독점 락 적용.
* **자원 고갈 방지 (선택 적용)**: JPA 쿼리 힌트(`lock.timeout`)를 결합하여 락 장기 소유로 인한 커넥션 풀 고갈을 방지하는 방어적 코드 구축.

---

## Troubleshooting

### 1. 동시성 경합으로 인한 재고 데이터 정합성 붕괴
* **문제 상황**: 다중 주문 요청 시 상품 재고 유실 발생.
* **원인 분석**: 다중 스레드가 동일 상품 재고를 동시에 읽고 덮어쓰며 레이스 컨디션(Race Condition)으로 인해 재고 유실. 
* **해결**
    * **비관적 락(`PESSIMISTIC_WRITE`)**: DB 레벨에서 트랜잭션 순차 제어를 강제하여 데이터 무결성 확보.
    * **락 타임아웃 제한**: 커넥션 풀 고갈을 방지하고자 JPA 쿼리 힌트(`lock.timeout`)를 적용해 자원 대기 시간 최소화.

### 2. JPA 대량 조회 시 N+1 쿼리 폭탄 및 페이징 과부하
* **문제 상황**: 주문 목록 조회 및 Thymeleaf 뷰 렌더링 시 연관 엔티티 초기화 쿼리가 연쇄 폭발하여 성능 저하.
* **원인 분석**: 지연 로딩(`LAZY`) 상태의 프록시 객체들을 루프 순회하며 엔티티 개수만큼 SQL이 추가 발행.
* **해결**
    * **글로벌 LAZY 통일**: `@ManyToOne` 관계의 기본 설정을 지연 로딩으로 전면 수정하여 불필요한 조회 차단.
    * **Fetch Join 적용**: ToOne 관계는 커스텀 JPQL `join fetch`를 통해 단 하나의 쿼리로 묶어서 조회 성능 최적화.
    * **Batch Size 도입**: ToMany 관계의 페이징 성능 저하를 막기 위해 `default_batch_fetch_size: 100`을 설정, `IN` 절로 묶어 다건 청구.

### 3. 분산 환경 확장을 위한 무상태(Stateless) JWT 인증 마이그레이션
* **문제 상황**: `HttpSession` 기반 인터셉터 방식은 대규모 트래픽 및 서버 스케일아웃(Scale-out) 시 메모리 부하와 세션 동기화 한계 존재.
* **원인 분석**
    * **구형 세션 잔재**: JWT 전환 후 세션은 파기되었으나, 공통 헤더 등 뷰 레이어에 `session.loginMember` 참조 코드가 남아 SpEL 예외 유발.
    * **컨텍스트 꼬임**: 미인증 유저를 로그인 창으로 리다이렉트하는 과정에서 파라미터가 중복 조립되어 경로가 정적 리소스(Static)로 오인됨.
* **해결**
    * **뷰 레이어 리팩터링**: 모든 세션 의존성을 제거하고 커스텀 아규먼트 리졸버(`@Login Member`) 주입 객체 기반으로 뷰 로직 일괄 전환.
    * **라우팅 안전장치**: 시큐리티 인가 규칙을 정비하고, 로그인 컨트롤러 내 `redirectURL` 파라미터 가드 코드를 배치해 무상태 인증 완성.