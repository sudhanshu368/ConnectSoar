package com.connectsoar.backend.controller;

import com.connectsoar.backend.dto.ApiResponse;
import com.connectsoar.backend.dto.ChatMessageDto;
import com.connectsoar.backend.security.UserPrincipal;
import com.connectsoar.backend.service.MeetingChatService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/api/meetings/{meetingId}/messages", "/api/v1/meetings/{meetingId}/messages"})
public class MeetingChatController {

    private final MeetingChatService chatService;

    public MeetingChatController(MeetingChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ChatMessageDto>>> getMessages(
            @PathVariable("meetingId") String meetingId,
            @RequestAttribute("userPrincipal") UserPrincipal principal) {
        List<ChatMessageDto> messages = chatService.getMessages(principal, meetingId);
        return ResponseEntity.ok(ApiResponse.ok(messages));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ChatMessageDto>> sendMessage(
            @PathVariable("meetingId") String meetingId,
            @RequestAttribute("userPrincipal") UserPrincipal principal,
            @RequestBody Map<String, String> payload) {
        String messageContent = payload.get("message");
        ChatMessageDto message = chatService.sendMessage(principal, meetingId, messageContent);
        return new ResponseEntity<>(ApiResponse.ok("Message sent successfully", message), HttpStatus.CREATED);
    }
}
