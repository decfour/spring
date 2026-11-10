// 화면·채팅 연결 설정
const root = document.getElementById('chat-room');
const roomId = root.dataset.roomId;
const courseId = root.dataset.courseId;
const memberId = root.dataset.memberId;

const form = document.getElementById('message-form');
const input = document.getElementById('message-content');
const conversation = document.getElementById('conversation');

const protocol = location.protocol === 'https:' ? 'wss' : 'ws';
const client = new StompJs.Client({
    brokerURL: `${protocol}://${location.host}/ws/chat`
});
const messagesUrl = `/walk/course/${courseId}/chat/${roomId}/messages`;
let oldestId = null;
let hasMore = true;

// 최근 메시지 조회
async function loadMessages() {
    const messages = await fetchMessages(messagesUrl);
    messages.forEach(message => conversation.appendChild(createMessage(message)));
}

// 이전 메시지 조회·스크롤 유지
async function loadPreviousMessages() {
    if (!oldestId || !hasMore) return;

    const messages = await fetchMessages(`${messagesUrl}?before=${oldestId}`);
    const fragment = document.createDocumentFragment();
    messages.forEach(message => fragment.appendChild(createMessage(message)));

    const oldHeight = conversation.scrollHeight;
    conversation.prepend(fragment);
    conversation.scrollTop += conversation.scrollHeight - oldHeight;
}

// 메시지 조회·페이지 상태 갱신
async function fetchMessages(url) {
    const response = await fetch(url);
    const data = await response.json();
    const messages = data.content.reverse();

    if (messages.length > 0) {
        oldestId = messages[0].id;
    }
    hasMore = !data.last;
    return messages;
}

// 실시간 구독
function connect() {
    console.log('웹소켓 연결 성공');
    console.log('현재 채팅방:', roomId);
    client.subscribe('/topic/chat/' + roomId, receiveMessage);
}

// 메시지 수신
function receiveMessage(frame) {
    const message = JSON.parse(frame.body);
    const nearBottom = conversation.scrollHeight - conversation.scrollTop - conversation.clientHeight < 80;

    conversation.appendChild(createMessage(message));
    if (nearBottom || String(message.senderId) === memberId) {
        scrollToBottom();
    }
}

// 메시지 전송
function sendMessage(event) {
    event.preventDefault();
    client.publish({
        destination: '/app/chat/' + roomId + '/send',
        body: JSON.stringify({ content: input.value })
    });
    input.value = '';
}

// 메시지 화면 생성
function createMessage(message) {
    const mine = message.senderId != null && String(message.senderId) === memberId;
    const element = document.createElement('div');
    element.className = mine ? 'message mine' : 'message';

    const body = document.createElement('div');
    body.className = 'message-body';
    const sender = document.createElement('div');
    sender.className = 'sender';
    sender.textContent = mine ? '나' : message.senderName;

    const line = document.createElement('div');
    line.className = 'bubble-line';
    const bubble = document.createElement('div');
    bubble.className = 'bubble';
    bubble.textContent = message.content;
    line.appendChild(bubble);

    if (message.createdAt) {
        const date = new Date(message.createdAt);
        if (!Number.isNaN(date.getTime())) {
            const time = document.createElement('time');
            time.dateTime = message.createdAt;
            time.textContent = date.toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit' });
            time.title = date.toLocaleString('ko-KR');
            line.appendChild(time);
        }
    }
    body.append(sender, line);
    element.appendChild(body);
    return element;
}

// 최신 메시지로 이동
function scrollToBottom() {
    conversation.scrollTop = conversation.scrollHeight;
}

// 상단 스크롤 처리
function loadOnScroll() {
    if (conversation.scrollTop === 0) {
        loadPreviousMessages();
    }
}

// 이벤트 연결·초기 조회
client.onConnect = connect;
form.addEventListener('submit', sendMessage);
conversation.addEventListener('scroll', loadOnScroll);
loadMessages().then(scrollToBottom);
client.activate();
