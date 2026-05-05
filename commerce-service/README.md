# 🛒 Commerce Service
> **Spring Boot를 활용한 이커머스 핵심 도메인 설계 및 기능 구현 프로젝트**

## 🚀 Key Objectives
* **계층형 아키텍처 설계**: Controller-Service-Repository 계층을 통한 객체지향적 책임 분리
* **데이터 영속화 전략**: 초기 인메모리(HashMap) 저장소에서 MySQL로의 점진적 전환 및 JPA 활용
* **확장성 고려**: 인터페이스 기반 리포지토리 설계를 통한 데이터 저장 기술 변경의 유연성 확보 (OCP 준수)
* **실무적 UI 구현**: Thymeleaf를 활용한 동적 HTML 렌더링 및 상품 관리 시스템 구축

## 🛠 Tech Stack
* **Framework**: Spring Boot 3.x
* **Language**: Java 17
* **Database**: ConcurrentHashMap(Initial Phase) -> MySQL(Scheduled)
* **Build**: Gradle
* **Template Engine**: Thymeleaf

## 📌 Main Features (현재 구현 단계)
* **상품 관리 시스템 (CRUD)**: 상품 등록, 목록 조회, 상세 조회 기능 구현
* **유연한 데이터 계층**: 인터페이스-구현체 분리를 통해 데이터 저장 기술(Memory -> DB) 전환 준비 완료
* **비즈니스 로직 보호**: 도메인 모델과 서비스 계층의 분리로 핵심 로직의 독립성 유지