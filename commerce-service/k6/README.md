# k6 부하 테스트

## 구성 및 결과 문서

| 파일 | 용도 |
| --- | --- |
| `shop.js` | 상품 목록(`GET /shop`) 단순 조회 테스트 |
| `shop-browse.js` | 상품 목록 → 상세 조회 탐색 흐름 테스트 |
| `load.js` | 로그인 후 주문 생성 테스트 |
| `shop-result.md` | 홈·상품·장바구니·주문 시나리오의 결과 |
| `walk-nearby.js` | 주변 산책 코스 목록 조회 테스트 |
| `walk-data-scale.js` | 임시 코스 생성·대량 조회·자동 삭제 테스트 |
| `walk-result.md` | 2026-08-19 산책 목록 조회 결과 |

## 산책 코스 목록 조회

`walk-nearby.js`는 산책 화면에서 실제 목록을 채우는 아래 API를 테스트합니다.

```text
GET /walk/course/nearby?lat={latitude}&lng={longitude}&page={page}&size=3
```

기본 시나리오는 0명에서 50명, 200명, 최대 500 VU까지 단계적으로 올리고 1분 유지합니다. 각 VU는 서울 시청 인근 약 500m 범위에서 서로 다른 좌표와 페이지를 요청한 뒤 1~3초간 대기합니다.

```powershell
# 로컬 서버
k6 run -e BASE_URL=http://localhost:8080 k6/walk-nearby.js

# 별도 성능 테스트 서버 (예: 최대 1,000 VU)
k6 run -e BASE_URL=http://<test-server>:8080 -e MAX_VUS=1000 k6/walk-nearby.js

# 500 VU까지 올리는 23초 압축 스파이크 테스트
k6 run -e BASE_URL=http://<test-server>:8080 -e QUICK=true k6/walk-nearby.js
```

## 상품 탐색 흐름

```powershell
k6 run -e BASE_URL=http://localhost:8080 k6/shop-browse.js
k6 run -e BASE_URL=http://<test-server>:8080 -e QUICK=true k6/shop-browse.js
```

## 임시 데이터 기반 산책 목록 조회

`walk-data-scale.js`는 기존 `test` 계정으로 임시 산책 코스를 만들고, 완료 시 같은 실행 ID를 가진 코스만 삭제합니다. 기존 코스·회원 데이터는 변경하지 않습니다.

```powershell
k6 run -e BASE_URL=http://<test-server>:8080 -e DATASET_SIZE=1000 -e RUN_ID=walk-scale-20260819 k6/walk-data-scale.js
```

중단으로 `teardown`이 실행되지 않으면 `RUN_ID`를 유지해 같은 식별자의 코스를 수동 정리할 수 있도록, 실행 로그를 보관합니다.

운영 서버에 실행하기 전에는 테스트 시간, 최대 VU, DB 모니터링 계획을 확정하세요. 이 조회는 `ST_Distance_Sphere` 정렬을 수행하므로 코스 데이터가 많으면 MySQL CPU와 쿼리 실행 계획을 함께 관찰해야 합니다.

성공 기준은 HTTP 실패율 1% 미만, P95 1초 미만, P99 2초 미만입니다. 기준을 초과하면 k6 출력의 `http_req_duration`, 애플리케이션 CPU/GC, MySQL slow query log 및 `EXPLAIN`을 함께 확인합니다.

`QUICK=true`는 실행 환경 제약이나 사전 점검용 스파이크 프로파일입니다. 지속 부하의 안정성 판단에는 기본 5분 30초 시나리오를 사용합니다.
