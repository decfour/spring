import http from 'k6/http';
import { sleep } from 'k6';

export const options = {
    vus: 100,
    duration: '30s',
};

const BASE_URL = 'http://13.125.248.110:8080';

export default function () {

    // 로그인
    const loginRes = http.post(`${BASE_URL}/member/login`, {
        loginId: 'test',
        loginPassword: '1234',
        redirectURL: '/',
    });

    // JWT 쿠키 유지
    const cookies = loginRes.cookies;

    // 랜덤 상품
    const ids = [4, 5, 6, 7, 8];
    const id = ids[Math.floor(Math.random() * ids.length)];

    // 주문
    http.post(
        `${BASE_URL}/order/create`,
        {
            orderType: 'ONE',
            itemId: id,
            count: 1,
            receiverName: '홍길동',
            address: '서울시 강남구',
        },
        {
            cookies: cookies,
        }
    );

    sleep(1);
}