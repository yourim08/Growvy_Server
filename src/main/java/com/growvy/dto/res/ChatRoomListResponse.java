package com.growvy.dto.res;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ChatRoomListResponse {
    private Long roomId;
    private Long postId;
    private String postTitle;
    private String partnerName; // 대화 상대방 이름 (내가 구인자면 구직자 이름, 구직자면 구인자 이름)

    // 🌟 추가된 필드
    private String lastMessage;
    private LocalDateTime lastMessageTime;
}