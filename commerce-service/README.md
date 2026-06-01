# 🛒 Commerce Service
> **Spring Boot와 Spring Data JPA를 활용한 이커머스 핵심 기능 구현 프로젝트**

## 🚀 Key Objectives
* **계층형 아키텍처 설계**: Controller-Service-Repository 구조를 통한 책임 분리 및 비즈니스 로직 독립성 확보.
* **데이터 영속화(Persistence) 전략**: 인터페이스 기반 설계를 바탕으로 인메모리(HashMap) 저장소에서 **MySQL로 전환 및 데이터 영속화**.
* **트랜잭션 기반 데이터 정합성 보장**: `@Transactional`을 활용한 서비스 레이어 원자성 확보 및 트랜잭션 전파 속성을 고려한 데이터 제어.
* **예외 처리 및 방어적 설계**: 사용자 조작 및 동시성 상황에서 발생할 수 있는 재고 부족과 데이터 왜곡을 백엔드 코어에서 차단.

## 🛠 Tech Stack
* **Framework**: Spring Boot 3.x
* **Language**: Java 17
* **ORM & Database**: Spring Data JPA, MySQL 8.x
* **Build**: Gradle
* **Template Engine**: Thymeleaf

## 📌 Main Features & Technical Achievements
### 1. 주문 및 장바구니 도메인 정합성 제어
* **트랜잭션 원자성(Atomicity) 확보**: '장바구니 상품 주문'과 '장바구니 비우기'를 단일 트랜잭션으로 통합하여 예외 발생 시 전 과정 롤백(All-or-Nothing) 처리.
* **JPA 변경 감지(Dirty Checking) 활용**: 별도의 Update 쿼리 작성 없이, 엔티티 상태 변경을 통한 객체지향적 데이터 수정 및 반영.

### 2. 재고 부족(NotEnoughStockException) 예외 처리 및 2중 검증
* **프론트엔드 1차 검증**: Thymeleaf 동적 렌더링을 활용하여 상품 상태(`item.open`) 및 실시간 재고(`item.stock`)에 따른 수량 제한(`th:max`) 분기 처리.
* **백엔드 2차 검증**: 클라이언트 단의 UI 조작이나 동시 요청으로 화면 검증이 우회되더라도, 영속성 컨텍스트의 최신 재고를 검증하여 `NotEnoughStockException` 유발.
* **전역 예외 핸들링**: `@ExceptionHandler`를 통해 재고 부족 예외 발생 시, 사용자에게 에러 메시지를 전달하고 지정된 페이지로 안전하게 리다이렉트 처리.

### 3. 구조적 개선 및 방어적 코드 작성
* **Java Optional 활용**: `orElseGet()` 및 람다식을 활용하여 `null` 직접 반환을 지양하고 `NullPointerException(NPE)` 발생 가능성 차단.
* **인터페이스 기반 구조**: 리포지토리 계층의 인터페이스-구현체 분리를 통해 핵심 비즈니스 로직 수정 없이 데이터 저장소 기술(Memory ➡️ MySQL) 교체.
