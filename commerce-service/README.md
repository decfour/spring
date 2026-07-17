# 🛒 Commerce Service

> Spring Boot와 Spring Data JPA 기반 이커머스 서비스

## 주요 기능

- 회원가입 / 로그인(JWT)
- 상품 관리
- 장바구니
- 주문
- 관리자 공지사항

## Key Objectives

- 계층형 아키텍처 기반 역할 분리
- 주문 및 재고 데이터 무결성 보장
- Fetch Join, Batch Fetch Size 기반 조회 성능 최적화
- Spring Security + JWT Stateless 인증
- AWS EC2 배포

## Tech Stack

**Backend**
- Java 17
- Spring Boot 3.2.5
- Spring Security 6
- Spring Data JPA (Hibernate)

**Database**
- MySQL 8

**Frontend**
- Thymeleaf

**Deployment**
- AWS EC2 (Ubuntu)

## Main Features

### JWT 기반 Stateless 인증

- Spring Security + JWT 기반 인증/인가
- Custom JWT Filter 및 AuthenticationEntryPoint 적용
- `@Login ArgumentResolver` 활용

### 주문 처리 및 서비스 계층

- `@Transactional` 기반 주문 처리 및 재고 차감
- Dirty Checking 활용
- OrderItem 스냅샷 저장

### 동시성 제어

- `ExecutorService` + `CountDownLatch` 기반 동시 주문 테스트
- `PESSIMISTIC_WRITE` 적용

### 조회 성능 최적화

- LAZY Loading 기반 연관관계 설계
- ToOne Fetch Join / ToMany Batch Fetch Size 적용
- N+1 문제 해결

### AWS EC2 배포

- Ubuntu 기반 EC2 환경 구성
- Spring Boot JAR 배포
- MySQL 연동

## Troubleshooting

### 1. 동시 주문 재고 정합성 문제

**문제**
- Race Condition으로 재고 차감 오류 발생

**해결**
- `PESSIMISTIC_WRITE` 적용
- Lock Timeout 설정

**결과**
- 동시 주문 환경에서 재고 정합성 확보

### 2. N+1 문제

**문제**
- 연관 엔티티 접근 시 추가 SQL 발생

**해결**
- Fetch Join + Batch Fetch Size 적용

**결과**
- 조회 쿼리 감소 및 성능 개선

### 3. Session 인증 → JWT 전환

**문제**
- Session 기반 인증의 확장성 한계

**해결**
- Spring Security + JWT Stateless 구조 적용

**결과**
- 서버 확장에 유리한 인증 구조 구축