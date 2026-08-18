import http from 'k6/http';
import { check, sleep } from 'k6';

// 실행 예시
// k6 run -e BASE_URL=http://localhost:8080 k6/walk-nearby.js
// k6 run -e BASE_URL=http://<test-server>:8080 -e MAX_VUS=500 k6/walk-nearby.js
const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const MAX_VUS = Number(__ENV.MAX_VUS || 500);
const LAT = Number(__ENV.LAT || 37.5665); // 서울 시청 부근
const LNG = Number(__ENV.LNG || 126.9780);
const QUICK = __ENV.QUICK === 'true';

const stages = QUICK
  ? [
      { duration: '3s', target: 50 },
      { duration: '5s', target: 200 },
      { duration: '5s', target: MAX_VUS },
      { duration: '5s', target: MAX_VUS },
      { duration: '5s', target: 0 },
    ]
  : [
      { duration: '30s', target: 50 },
      { duration: '1m', target: 200 },
      { duration: '2m', target: MAX_VUS },
      { duration: '1m', target: MAX_VUS },
      { duration: '1m', target: 0 },
    ];

export const options = {
  scenarios: {
    nearby_course_list: {
      executor: 'ramping-vus',
      startVUs: 0,
      gracefulRampDown: '30s',
      stages,
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<1000', 'p(99)<2000'],
  },
};

export default function () {
  // 실제 화면에서는 지도 클릭/현재 위치에 따라 좌표가 바뀐다.
  // 약 500m 범위의 좌표를 섞어 특정 한 쿼리만 반복되는 상황을 피한다.
  const lat = LAT + (Math.random() - 0.5) * 0.009;
  const lng = LNG + (Math.random() - 0.5) * 0.011;
  const page = Math.random() < 0.8 ? 0 : 1;

  const response = http.get(
    `${BASE_URL}/walk/course/nearby?lat=${lat}&lng=${lng}&page=${page}&size=3`,
    { tags: { name: 'GET /walk/course/nearby' } },
  );

  check(response, {
    'status is 200': (res) => res.status === 200,
    'returns JSON': (res) => (res.headers['Content-Type'] || '').includes('application/json'),
  });

  // 화면에서 사용자가 지도를 보고 다음 위치를 선택하기 전의 짧은 체류 시간을 모사한다.
  sleep(Math.random() * 2 + 1);
}
