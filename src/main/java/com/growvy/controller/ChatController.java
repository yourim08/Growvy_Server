package com.growvy.controller;

import com.growvy.dto.ChatDto;
import com.growvy.dto.res.ChatMessageResponse;
import com.growvy.dto.res.ChatRoomListResponse;
import com.growvy.entity.User;
import com.growvy.annotation.CurrentUser; // 사용하시는 커스텀 어노테이션에 맞게 적용
import com.growvy.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @Operation(summary = "내 채팅방 목록 조회")
    @GetMapping("/rooms")
    public ResponseEntity<List<ChatRoomListResponse>> getMyChatRooms(@CurrentUser User user) {
        return ResponseEntity.ok(chatService.getMyChatRooms(user));
    }

    @Operation(summary = "특정 채팅방 메시지 내역 조회")
    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<List<ChatMessageResponse>> getMessages(
            @PathVariable("roomId") Long roomId,
            @CurrentUser User user) {
        return ResponseEntity.ok(chatService.getMessages(roomId, user));
    }

    @Operation(summary = "채팅 메시지 전송 (HTTP 용)")
    @PostMapping("/rooms/{roomId}/messages")
    public ResponseEntity<Void> sendMessage(
            @PathVariable("roomId") Long roomId,
            @RequestBody ChatDto chatDto,
            @CurrentUser User user) { // 또는 파라미터에서 바로 senderId를 받아도 됨

        chatService.sendMessage(roomId, user.getId(), chatDto.getMessage());
        return ResponseEntity.ok().build();
    }
}