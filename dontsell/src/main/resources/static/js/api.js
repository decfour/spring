// 로그인 및 회원가입 요청
async function publicAuthRequest(path, options = {}) {
    return fetch(path, {
        ...options,
        cache: 'no-store'
    });
}

// JWT 인증이 필요한 API 요청
async function authFetch(path, options = {}) {
    const token = sessionStorage.getItem('accessToken');

    return fetch(path, {
        ...options,
        headers: {
            ...options.headers,
            ...(token ? { Authorization: `Bearer ${token}` } : {})
        },
        cache: 'no-store'
    });
}