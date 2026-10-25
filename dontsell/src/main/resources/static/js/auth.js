const signInLink = document.getElementById('sign-in-link');
const signOutButton = document.getElementById('sign-out-button');
const authStatus = document.getElementById('auth-status');

// 로그인 상태에 따라 화면 갱신
function showAuthState(memberId) {
    if (signInLink) signInLink.hidden = memberId !== null;
    if (signOutButton) signOutButton.hidden = memberId === null;

    if (authStatus) {
        authStatus.textContent = memberId !== null
            ? memberId + '번 회원으로 로그인 중입니다.'
            : '';
    }
}

// 서버에 로그인 상태 확인
async function checkAuthentication() {
    const token = sessionStorage.getItem('accessToken');

    if (!token) {
        showAuthState(null);
        return;
    }

    try {
        const response = await authFetch('/auth/me');

        if (response.ok) {
            const memberId = await response.text();
            showAuthState(memberId);
        } else if (response.status === 401) {
            sessionStorage.removeItem('accessToken');
            showAuthState(null);
        } else {
            throw new Error('인증 상태 확인 실패');
        }
    } catch {
        if (authStatus) {
            authStatus.textContent = '로그인 상태를 확인하지 못했습니다.';
        }
    }
}

// 로그아웃
if (signOutButton) {
    signOutButton.addEventListener('click', () => {
        sessionStorage.removeItem('accessToken');
        showAuthState(null);
        window.location.replace('/sign-in.html');
    });
}

// 페이지 표시 시 로그인 상태 확인
window.addEventListener('pageshow', checkAuthentication);
