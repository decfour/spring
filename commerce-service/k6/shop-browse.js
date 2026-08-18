import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const MAX_VUS = Number(__ENV.MAX_VUS || 200);
const QUICK = __ENV.QUICK === 'true';
const ITEM_IDS = (__ENV.ITEM_IDS || '4,5,6,7,8').split(',');

const stages = QUICK
  ? [
      { duration: '3s', target: 50 },
      { duration: '5s', target: MAX_VUS },
      { duration: '5s', target: MAX_VUS },
      { duration: '5s', target: 0 },
    ]
  : [
      { duration: '30s', target: 50 },
      { duration: '1m', target: MAX_VUS },
      { duration: '2m', target: MAX_VUS },
      { duration: '1m', target: 0 },
    ];

export const options = {
  scenarios: {
    shop_browse: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages,
      gracefulRampDown: '10s',
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.01'],
    'http_req_duration{name:GET /shop}': ['p(95)<2000'],
    'http_req_duration{name:GET /shop/item}': ['p(95)<2000'],
  },
};

export default function () {
  const listResponse = http.get(`${BASE_URL}/shop`, {
    tags: { name: 'GET /shop' },
  });
  check(listResponse, { 'list status is 200': (res) => res.status === 200 });

  sleep(Math.random() + 0.5);

  const itemId = ITEM_IDS[Math.floor(Math.random() * ITEM_IDS.length)];
  const detailResponse = http.get(`${BASE_URL}/shop/item/${itemId}`, {
    tags: { name: 'GET /shop/item' },
  });
  check(detailResponse, { 'detail status is 200': (res) => res.status === 200 });

  sleep(Math.random() + 0.5);
}
