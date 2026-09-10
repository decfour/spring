document.querySelectorAll('[data-open]').forEach(button => {
    button.addEventListener('click', () => document.getElementById(button.dataset.open).showModal());
});
document.querySelectorAll('[data-close]').forEach(button => {
    button.addEventListener('click', () => button.closest('dialog').close());
});
const roomName = document.getElementById('room-name');
function updateTitle() {
    roomName.setCustomValidity(roomName.value.trim() ? '' : '채팅방 이름을 입력해주세요.');
}
if (roomName) {
    roomName.addEventListener('input', updateTitle);
    updateTitle();
    if (roomName.value) document.getElementById('create-room').showModal();
}
