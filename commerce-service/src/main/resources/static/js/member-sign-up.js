// 이메일 입력 요소
const email = document.getElementById('email');
const emailLocal = document.getElementById('emailLocal');
const emailDomain = document.getElementById('emailDomain');

// 이메일 입력 복원
function restoreEmail() {
    const separator = email.value.lastIndexOf('@');
    if (separator >= 0) {
        emailLocal.value = email.value.slice(0, separator);
        emailDomain.value = email.value.slice(separator + 1);
    } else {
        emailLocal.value = email.value;
    }
}

// 이메일 조합·길이 검사
function syncEmail() {
    email.value = emailLocal.value + '@' + emailDomain.value;
    emailDomain.setCustomValidity(email.value.length > 100 ? '이메일은 전체 100자 이내로 입력해주세요.' : '');
}

// 이벤트 연결·초기 동기화
restoreEmail();
emailLocal.addEventListener('input', syncEmail);
emailDomain.addEventListener('input', syncEmail);
email.form.addEventListener('submit', syncEmail);
syncEmail();
