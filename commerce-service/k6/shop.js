import http from 'k6/http';

export const options = {
  vus: 200,
  duration: '30s',
};

export default function () {
  http.get('http://13.125.248.110/shop');
}