package com.growvy.dto.res;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
public class ChatMessageResponse {
    private Long messageId;
    private Long senderId;
    private String content;
    private LocalDateTime createdAt;
    private boolean isMine; // 프론트에서 내가 보낸 건지(오른쪽 말풍선) 쉽게 구분하기 위한 필드
}