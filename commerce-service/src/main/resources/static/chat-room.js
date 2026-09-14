const root = document.getElementById('chat-room');
const roomId = root.dataset.roomId;

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

        const messageElement = document.createElement('p');

        messageElement.textContent =
            message.senderName + ': ' + message.content;

        conversation.appendChild(messageElement);
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

// ws/chat 연결 시도
client.activate();