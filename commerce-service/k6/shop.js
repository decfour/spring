import http from 'k6/http';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

export const options = {
  vus: 200,
  duration: '30s',
};

export default function () {
  http.get(`${BASE_URL}/shop`);
}
