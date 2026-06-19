package com.growvy.service;

import com.growvy.dto.res.ChatMessageResponse;
import com.growvy.dto.res.ChatRoomListResponse;
import com.growvy.entity.ChatMessage;
import com.growvy.entity.ChatRoom;
import com.growvy.entity.User;
import com.growvy.repository.ChatMessageRepository;
import com.growvy.repository.ChatRoomRepository;
import com.growvy.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository; // 유저 조회를 위해 주입 필요
    private final SimpMessagingTemplate messagingTemplate;

    // 1. 내 채팅방 목록 조회 (마지막 메시지 포함)
    @Transactional
    public List<ChatRoomListResponse> getMyChatRooms(User user) {
        // 내 방 목록 전부 가져오기
        List<ChatRoom> rooms = chatRoomRepository.findMyChatRooms(user.getId());

        return rooms.stream().map(room -> {
                    boolean isEmployer = room.getEmployer().getId().equals(user.getId());

                    // 상대방 이름 세팅
                    String partnerName = isEmployer
                            ? room.getJobSeeker().getUser().getName() // 🌟 이렇게 진짜 닉네임을 가져오게 수정!
                            : room.getEmployer().getName();

                    // 🌟 방의 가장 최근 메시지 1개 조회
                    ChatMessage lastMsg = chatMessageRepository.findTopByRoom_IdOrderByCreatedAtDesc(room.getId())
                            .orElse(null);

                    // 메시지가 있으면 내용과 시간 세팅, 아직 메시지가 한 개도 없다면 빈 문자열과 방 생성 시간 세팅
                    String lastMessageContent = (lastMsg != null) ? lastMsg.getContent() : "";
                    LocalDateTime lastMessageTime = (lastMsg != null) ? lastMsg.getCreatedAt() : room.getCreatedAt();

                    return ChatRoomListResponse.builder()
                            .roomId(room.getId())
                            .postId(room.getJobPost().getId())
                            .postTitle(room.getJobPost().getTitle())
                            .partnerName(partnerName)
                            .lastMessage(lastMessageContent)     // 🌟 추가
                            .lastMessageTime(lastMessageTime) // 🌟 추가
                            .build();
                })
                // 🌟 마지막 메시지가 가장 최신인 방이 맨 위로 오도록 내림차순 정렬
                .sorted((r1, r2) -> r2.getLastMessageTime().compareTo(r1.getLastMessageTime()))
                .collect(Collectors.toList());
    }

    // 2. 특정 채팅방의 메시지 내역 조회 (이전 코드와 동일)
    @Transactional
    public List<ChatMessageResponse> getMessages(Long roomId, User user) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방이 존재하지 않습니다."));

        // 권한 체크 로직 (JobSeeker PK 이름에 맞춰 getUserId() 또는 getProfileId()로 수정)
        if (!room.getEmployer().getId().equals(user.getId()) && !room.getJobSeeker().getUserId().equals(user.getId())) {
            throw new IllegalArgumentException("이 채팅방에 접근할 권한이 없습니다.");
        }

        List<ChatMessage> messages = chatMessageRepository.findByRoom_IdOrderByCreatedAtAsc(roomId);

        return messages.stream().map(msg -> ChatMessageResponse.builder()
                .messageId(msg.getId())
                .senderId(msg.getSender().getId())
                .content(msg.getContent())
                .createdAt(msg.getCreatedAt())
                .isMine(msg.getSender().getId().equals(user.getId()))
                .build()
        ).toList();
    }


    @Transactional
    public void sendMessage(Long roomId, Long senderId, String content) {
        // 1. 방과 보낸 유저 확인
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방이 존재하지 않습니다."));
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new IllegalArgumentException("유저가 존재하지 않습니다."));

        // 2. 메시지 DB 저장
        ChatMessage message = ChatMessage.builder()
                .room(room)
                .sender(sender)
                .content(content)
                .createdAt(LocalDateTime.now())
                .build();
        chatMessageRepository.save(message);

        // 3. 웹소켓으로 보낼 응답 데이터 세팅 (이전 조회용 DTO 재활용)
        ChatMessageResponse response = ChatMessageResponse.builder()
                .messageId(message.getId())
                .senderId(sender.getId())
                .content(message.getContent())
                .createdAt(message.getCreatedAt())
                .isMine(false) // 받는 사람 입장에선 상대방 메시지이므로 일단 false (프론트에서 senderId로 구분)
                .build();

        // 4. 해당 방을 구독 중인 클라이언트들에게 실시간으로 쏘기!
        messagingTemplate.convertAndSend("/sub/chat/room/" + roomId, response);
    }
}