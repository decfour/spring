# 🛒 Commerce Service
> **Spring Boot와 Spring Data JPA를 활용한 이커머스 핵심 도메인 설계 및 기능 구현 프로젝트**

## 🚀 Key Objectives
* **계층형 아키텍처 설계**: Controller-Service-Repository 계층 구조를 통한 객체지향적 책임 분리 및 비즈니스 루틴 독립성 확보
* **데이터 영속화(Persistence) 전략**: 인터페이스 기반 설계를 바탕으로 인메모리(HashMap) 저장소에서 실무형 데이터베이스인 **MySQL로의 완벽한 전환 및 영속화 성공**
* **JPA 메커니즘 활용**: 데이터베이스 중심의 쿼리 작성 방식에서 벗어나, 엔티티 매핑 및 **변경 감지(Dirty Checking)**를 활용한 객체지향적 데이터 제어
* **실무적 UI 구현**: Thymeleaf template engine을 활용한 동적 HTML 렌더링 및 상품 관리 시스템 구축

## 🛠 Tech Stack
* **Framework**: Spring Boot 3.x
* **Language**: Java 17
* **ORM & Database**: Spring Data JPA, MySQL 8.x
* **Build**: Gradle
* **Template Engine**: Thymeleaf

## 📌 Main Features & Technical Achievements (현재 구현 단계)
* **상품 및 회원 관리 시스템 (CRUD)**: 상품 등록, 목록 조회, 상세 조회 및 상품 정보 수정 기능 완벽 구현
* **선언적 트랜잭션 관리**: 서비스 레이어에 `@Transactional`을 적용하여 비즈니스 로직의 원자성을 보장하고, `readOnly = true` 옵션을 통한 데이터 조회 성능 최적화
* **JPA 엔티티 매핑**: 자바 객체와 relational database 간의 패러다임 불일치를 해결하기 위해 `@Entity`, `@Id(IDENTITY)`, `@Enumerated(STRING)` 등의 매핑 전략 도입
* **유연한 아키텍처 구조**: 인터페이스-구현체 분리를 통해 시스템 핵심 비즈니스 로직의 변경 없이 저장소 기술(Memory ➡️ MySQL)을 유연하게 교체 (OCP, 개방-폐쇄 원칙 준수)
* **유효성 검증(Validation)**: `@Valid`와 `BindingResult`를 활용하여 웹 계층에서의 상품 등록/수정 데이터 검증 및 에러 핸들링 구현