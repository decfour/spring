// 채팅방 이름 입력
const roomName = document.getElementById('room-name');

// 공백 이름 검사
function updateTitle() {
    roomName.setCustomValidity(roomName.value.trim() ? '' : '채팅방 이름을 입력해주세요.');
}

// 대화상자 열기·닫기
document.querySelectorAll('[data-open]').forEach(button => {
    button.addEventListener('click', () => document.getElementById(button.dataset.open).showModal());
});
document.querySelectorAll('[data-close]').forEach(button => {
    button.addEventListener('click', () => button.closest('dialog').close());
});

// 입력 검사·실패한 등록 복원
if (roomName) {
    roomName.addEventListener('input', updateTitle);
    updateTitle();
    if (roomName.value) document.getElementById('create-room').showModal();
}
