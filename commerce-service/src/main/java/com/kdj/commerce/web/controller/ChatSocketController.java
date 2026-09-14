package com.kdj.commerce.web.controller;

import com.kdj.commerce.domain.member.Member;
import com.kdj.commerce.service.ChatMessageService;
import com.kdj.commerce.service.MemberService;
import com.kdj.commerce.web.dto.chat.ChatMessageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ChatSocketController {
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageService chatMessageService;

    public record SendRequest(String content) {}

    // STOMP 메시지를 받는다.
    @MessageMapping("/chat/{roomId}/send")
    public void send(
            @DestinationVariable Long roomId,
            @Payload SendRequest request,
            Principal principal
    ) {
        ChatMessageResponse savedMessage =
                chatMessageService.send(
                        roomId,
                        principal.getName(),
                        request.content()
                );

        // 구독하는 이들에게 메시지 뿌리기
        messagingTemplate.convertAndSend(
                "/topic/chat/" + roomId,
                savedMessage
        );
    }
}
