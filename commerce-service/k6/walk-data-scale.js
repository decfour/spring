import http from 'k6/http';
import { check, sleep } from 'k6';

// 임시 코스 생성 → 주변 코스 조회 부하 테스트 → 생성 코스 삭제를 한 번에 수행한다.
// 생성 데이터는 RUN_ID 접두어로 식별되며, 기존 데이터에는 영향을 주지 않는다.
const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const DATASET_SIZE = Number(__ENV.DATASET_SIZE || 1000);
const MAX_VUS = Number(__ENV.MAX_VUS || 500);
const RUN_ID = __ENV.RUN_ID || `walk-scale-${Date.now()}`;
const QUICK = __ENV.QUICK === 'true';
const CENTER_LAT = 0;
const CENTER_LNG = 0;
const BATCH_SIZE = 20;

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
      stages,
      gracefulRampDown: '30s',
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.01'],
    'http_req_duration{name:GET /walk/course/nearby}': ['p(95)<1000', 'p(99)<2000'],
  },
};

function login() {
  const response = http.post(`${BASE_URL}/member/login`, {
    loginId: 'test',
    loginPassword: '1234',
    redirectURL: '/',
  }, { redirects: 0 });
  const token = response.cookies.Authorization && response.cookies.Authorization[0].value;

  if (!token) {
    throw new Error('임시 데이터용 test 계정 로그인에 실패했습니다.');
  }
  return token;
}

function authParams(token) {
  return {
    headers: {
      Cookie: `Authorization=${token}`,
      'Content-Type': 'application/x-www-form-urlencoded',
    },
    redirects: 0,
  };
}

function courseBody(index) {
  // 중심점에서 약 300m 이내에 분포시켜 nearby 쿼리의 정렬 대상을 늘린다.
  const lat = CENTER_LAT + ((index % 40) - 20) * 0.0001;
  const lng = CENTER_LNG + ((Math.floor(index / 40) % 25) - 12) * 0.0001;

  const form = {
    name: `${RUN_ID}-${index}`,
    review: 'Temporary load-test course. Automatically deleted after the test.',
    startLat: lat,
    startLng: lng,
    endLat: lat + 0.0001,
    endLng: lng + 0.0001,
    routeData: `[[${lng},${lat}],[${lng + 0.0001},${lat + 0.0001}]]`,
    distance: 100,
    duration: 60,
  };

  return Object.entries(form)
    .map(([key, value]) => `${encodeURIComponent(key)}=${encodeURIComponent(value)}`)
    .join('&');
}

function findTemporaryCourseIds(runId) {
  const response = http.get(
    `${BASE_URL}/walk/course/nearby?lat=${CENTER_LAT}&lng=${CENTER_LNG}&page=0&size=2000`,
    { tags: { name: 'GET /walk/course/nearby' } },
  );
  const courses = response.json('content') || [];

  return courses
    .filter((course) => course.name && course.name.startsWith(`${runId}-`))
    .map((course) => course.id);
}

export function setup() {
  const token = login();
  let created = 0;

  for (let start = 0; start < DATASET_SIZE; start += BATCH_SIZE) {
    const requests = [];
    const end = Math.min(start + BATCH_SIZE, DATASET_SIZE);
    for (let index = start; index < end; index += 1) {
      requests.push(['POST', `${BASE_URL}/walk`, courseBody(index), authParams(token)]);
    }

    const responses = http.batch(requests);
    created += responses.filter((response) => response.status === 302).length;
  }

  // 일부 생성 실패가 있어도 teardown이 실행되어, 생성된 데이터는 반드시 정리한다.
  return { token, runId: RUN_ID, created, expected: DATASET_SIZE };
}

export default function () {
  const lat = CENTER_LAT + (Math.random() - 0.5) * 0.005;
  const lng = CENTER_LNG + (Math.random() - 0.5) * 0.005;
  const page = Math.random() < 0.8 ? 0 : 1;
  const response = http.get(
    `${BASE_URL}/walk/course/nearby?lat=${lat}&lng=${lng}&page=${page}&size=3`,
    { tags: { name: 'GET /walk/course/nearby' } },
  );

  check(response, {
    'status is 200': (res) => res.status === 200,
    'returns JSON': (res) => (res.headers['Content-Type'] || '').includes('application/json'),
  });
  sleep(Math.random() * 2 + 1);
}

export function teardown(data) {
  const ids = findTemporaryCourseIds(data.runId);
  const params = authParams(data.token);

  for (let start = 0; start < ids.length; start += BATCH_SIZE) {
    const requests = ids
      .slice(start, start + BATCH_SIZE)
      .map((id) => ['POST', `${BASE_URL}/walk/course/${id}/delete`, null, params]);
    http.batch(requests);
  }

  const remaining = findTemporaryCourseIds(data.runId);
  if (remaining.length !== 0) {
    throw new Error(`임시 코스 정리 실패: remaining=${remaining.length}, runId=${data.runId}`);
  }

  console.log(`Temporary walk courses cleaned up: runId=${data.runId}, created=${data.created}/${data.expected}`);
}
