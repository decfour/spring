# 쇼핑 기능 부하 테스트 결과

## 테스트 환경

| Item | Value |
|------|-------|
| Tool | k6 |
| Server | AWS EC2 (Ubuntu) |
| Backend | Spring Boot 3.2.5 |
| Language | Java 17 |
| Database | MySQL 8 |
| WAS | Embedded Tomcat |
| 실행 일시 | 기존 기록에 없음 |
| 대상 서버 | AWS EC2 (주소 미기록) |

---

## 성공 기준

| 지표 | 기준 |
| --- | ---: |
| HTTP 실패율 | 1% 미만 |
| P95 응답 시간 | 1초 미만 |

---

## 시나리오 1. 홈 화면 조회

### 테스트 대상

`GET /`

### 시나리오

- Virtual Users: 10
- Duration: 10s

### 결과

| Metric | Value |
|------|------:|
| Average Response Time | 8.98 ms |
| P95 Response Time | 13.24 ms |
| Max Response Time | 34.58 ms |
| Throughput | 1090 req/s |
| Total Requests | 10,918 |
| Failed Requests | 0% |

### 결론

- Home 화면 반복 조회 테스트
- 모든 요청 정상 처리 (HTTP Failure 0%)
- 평균 응답 시간 8.98ms 유지

---

## 시나리오 2. 상품 목록 조회

### 테스트 대상

`GET /shop`

### 시나리오

- Virtual Users: 100
- Duration: 30s

### 결과

| Metric | Value |
|------|------:|
| Average Response Time | 218.15 ms |
| P95 Response Time | 586.45 ms |
| Max Response Time | 1.38 s |
| Throughput | 456 req/s |
| Total Requests | 13,749 |
| Failed Requests | 0% |

### 서버 모니터링

| Process | Observation |
|---------|-------------|
| Spring Boot (Java) | CPU 약 179% |
| MySQL | CPU 약 20% |
| Memory | 약 360MB |

### 결론

- 상품 목록 반복 조회 테스트
- 100명의 동시 사용자 환경에서도 요청 실패 없음
- Spring Boot 애플리케이션이 주요 CPU를 사용
- MySQL은 비교적 여유 있는 상태 확인

---

## 시나리오 3. 상품 목록 → 상세 조회

### 테스트 흐름

```text
GET /shop
      ↓
sleep(1)
      ↓
GET /shop/item/{id}
      ↓
sleep(1)
```

### 시나리오

- Virtual Users: 100
- Duration: 30s

### 결과

| Metric | Value |
|------|------:|
| Average Response Time | 27.35 ms |
| P95 Response Time | 77.03 ms |
| Max Response Time | 341.13 ms |
| Throughput | 95.94 req/s |
| Total Requests | 3,000 |
| Failed Requests | 0% |

### 서버 모니터링

| Process | Observation |
|---------|-------------|
| Spring Boot (Java) | CPU 약 72% |
| MySQL | CPU 약 6% |
| Memory | 약 380MB |

### 결론

- 실제 사용자의 상품 탐색 흐름을 가정한 테스트
- 페이지 체류 시간을 고려하여 `sleep()`을 적용
- 모든 요청 정상 처리 (HTTP Failure 0%)
- 평균 응답 시간 27.35ms 유지

---

## 시나리오 4. 로그인 → 상품 조회 → 장바구니 추가

### 테스트 흐름

```text
POST /member/login
        ↓
GET /shop
        ↓
GET /shop/item/{id}
        ↓
POST /cart/add
```

### 시나리오

- Virtual Users: 100
- Duration: 30s

### 결과

| Metric | Value |
|------|------:|
| Average Response Time | 45.68 ms |
| P95 Response Time | 187.71 ms |
| HTTP Failure Rate | 0% |

### 검증

| Item | Result |
|------|--------|
| Login | Success |
| JWT Cookie | Automatically maintained |
| Cart | Items successfully added |

### 결론

- 로그인 후 JWT Cookie 기반 인증 상태 유지 확인
- 상품 조회 후 장바구니 추가까지 정상 수행
- 장바구니 데이터 정상 생성 확인
- 모든 요청 정상 처리 (HTTP Failure 0%)

---

## 시나리오 5. 로그인 → 상품 조회 → 주문 생성

### 테스트 흐름

```text
POST /member/login
        ↓
GET /shop
        ↓
GET /shop/item/{id}
        ↓
POST /order/create
```

### 시나리오

- Virtual Users: 100
- Duration: 30s

### 결과

| Metric | Value |
|------|------:|
| Average Response Time | 1.37 s |
| P95 Response Time | 3.42 s |
| Max Response Time | 7.38 s |
| Throughput | 56 req/s |
| Total Requests | 2,064 |
| Failed Requests | 0% |

### 서버 모니터링

| Process | Observation |
|---------|-------------|
| Spring Boot (Java) | CPU 약 170% |
| MySQL | CPU 약 15% |
| Memory | 약 410MB |
| Swap | 약 758MB 사용 |

### 검증

| Item | Result |
|------|--------|
| Login | Success |
| JWT Cookie | Automatically maintained |
| Order | Successfully created |
| Inventory | Successfully updated |

### 결론

- 로그인부터 주문 생성까지 실제 사용자 구매 시나리오를 가정한 테스트
- 주문 생성 및 재고 차감이 정상적으로 수행됨을 확인
- 모든 요청 정상 처리 (HTTP Failure 0%)
- 조회 테스트 대비 쓰기 트랜잭션으로 인해 응답 시간이 증가했지만 안정적으로 처리
- Spring Boot 애플리케이션이 주요 CPU를 사용하였으며 MySQL은 상대적으로 여유 있는 상태를 유지

---

## 시나리오 6. 상품 목록 조회 재측정

### 테스트 대상

`GET https://decfour.dev/shop`

### 시나리오

| 항목 | 값 |
| --- | --- |
| 실행 일시 | 2026-08-19 (KST) |
| 가상 사용자 | 200 VU |
| 유지 시간 | 20초 |
| 테스트 유형 | 읽기 전용 반복 조회 |

### 결과

| 지표 | 결과 |
| --- | ---: |
| 총 HTTP 요청 | 6,054 |
| 평균 처리량 | 296.65 req/s |
| HTTP 실패율 | 0.00% |
| 평균 응답 시간 | 654.82ms |
| 중앙값 응답 시간 | 590.24ms |
| P90 응답 시간 | 1.45s |
| P95 응답 시간 | 1.75s |
| 최대 응답 시간 | 4.26s |

### 결론

200 VU에서 요청 실패는 없었지만 P95가 1초 목표를 초과했습니다. 기존 100 VU 테스트의 P95(586.45ms)보다도 높으므로, 상품 수·이미지 크기·Thymeleaf 렌더링 시간과 데이터베이스 조회 쿼리를 우선 점검합니다.

---

## 시나리오 7. 상품 목록 → 상세 조회 재측정

### 테스트 대상

`GET https://decfour.dev/shop` → `GET https://decfour.dev/shop/item/{id}`

### 시나리오

| 항목 | 값 |
| --- | --- |
| 실행 일시 | 2026-08-19 (KST) |
| 가상 사용자 | 최대 200 VU |
| 프로파일 | 50 → 200 VU 램프업, 200 VU 5초 유지, 램프다운 |
| 테스트 시간 | 18초 압축 스파이크 |
| 대상 상품 | 현재 배포 환경의 상품 ID 4~8 중 임의 선택 |
| 사용자 행동 | 목록 조회 후 0.5~1.5초 대기, 상세 조회 후 0.5~1.5초 대기 |

### 결과

| 지표 | 상품 목록 | 상품 상세 | 전체 |
| --- | ---: | ---: | ---: |
| HTTP 요청 | 1,190 | 1,190 | 2,380 |
| 평균 응답 시간 | 17.10ms | 16.69ms | 16.89ms |
| P95 응답 시간 | 28.28ms | 26.36ms | 27.68ms |
| 최대 응답 시간 | 168.14ms | 157.58ms | 168.14ms |
| HTTP 실패율 | 0.00% | 0.00% | 0.00% |

### 결론

실제 탐색 흐름에서는 200 VU까지 목록·상세 조회 모두 P95 2초 기준을 통과했습니다. 목록 단독 반복 조회(시나리오 6)는 초당 약 297건으로 요청이 몰리는 조건이고, 이 시나리오는 사용자 체류 시간을 포함해 전체 처리량이 118.63 req/s입니다. 따라서 목록 단독 조회 결과는 피크 요청률, 탐색 흐름 결과는 일반 사용자 경험 기준으로 해석합니다.
