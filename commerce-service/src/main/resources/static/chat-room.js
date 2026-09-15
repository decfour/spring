const root = document.getElementById('chat-room');
const roomId = root.dataset.roomId;
const courseId = root.dataset.courseId;
const memberId = root.dataset.memberId;

const form = document.getElementById('message-form');
const input = document.getElementById('message-content');
const conversation = document.getElementById('conversation');

// STOMP 클라이언트 생성
const protocol = location.protocol === 'https:' ? 'wss' : 'ws';
const client = new StompJs.Client({
     brokerURL: `${protocol}://${location.host}/ws/chat`
});

client.onConnect = () => {
    console.log('웹소켓 연결 성공');
    console.log('현재 채팅방:', roomId);

    // 수신(구독)
    client.subscribe('/topic/chat/' + roomId, frame => {
        const message = JSON.parse(frame.body);

        const nearBottom = conversation.scrollHeight - conversation.scrollTop - conversation.clientHeight < 80;
        conversation.appendChild(createMessageElement(message));
        if (nearBottom || String(message.senderId) === memberId) {
            conversation.scrollTop = conversation.scrollHeight;
        }
    });
};

form.addEventListener('submit', event => {
    event.preventDefault();

    // 송신
    client.publish({
        destination: '/app/chat/' + roomId + '/send',
        body: JSON.stringify({
            content: input.value
        })
    });

    // 메시지 입력창 비우기
    input.value = '';
});

let oldestMessageId = null;
let hasMoreMessages = true;

async function loadMessages() {
    const response = await fetch(
        `/walk/course/${courseId}/chat/${roomId}/messages`
    );

    const data = await response.json();
    const messages = data.content.reverse();

    messages.forEach(message => {
        conversation.appendChild(
            createMessageElement(message)
        );
    });

    if (messages.length > 0) {
        oldestMessageId = messages[0].id;
    }

    hasMoreMessages = !data.last;
}

async function loadPreviousMessages() {
    if (!oldestMessageId || !hasMoreMessages) {
        return;
    }

    const response = await fetch(
        `/walk/course/${courseId}/chat/${roomId}/messages?before=${oldestMessageId}`
    );

    const data = await response.json();
    const messages = data.content.reverse();

    const fragment = document.createDocumentFragment();

    messages.forEach(message => {
        fragment.appendChild(
            createMessageElement(message)
        );
    });

    const oldHeight = conversation.scrollHeight;
    conversation.prepend(fragment);
    conversation.scrollTop += conversation.scrollHeight - oldHeight;

    if (messages.length > 0) {
        oldestMessageId = messages[0].id;
    }

    hasMoreMessages = !data.last;
}

function createMessageElement(message) {
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

conversation.addEventListener('scroll', () => {
    if (conversation.scrollTop === 0) {
        loadPreviousMessages();
    }
});

loadMessages().then(() => { conversation.scrollTop = conversation.scrollHeight; });
client.activate();