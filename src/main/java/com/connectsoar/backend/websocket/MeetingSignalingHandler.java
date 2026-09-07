package com.connectsoar.backend.websocket;

import com.connectsoar.backend.model.Meeting;
import com.connectsoar.backend.repository.MeetingParticipantRepository;
import com.connectsoar.backend.repository.MeetingRepository;
import com.connectsoar.backend.security.UserPrincipal;
import com.connectsoar.backend.service.MeetingChatService;
import com.connectsoar.backend.service.MeetingSessionService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MeetingSignalingHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(MeetingSignalingHandler.class);

    private final ObjectMapper objectMapper;
    private final MeetingRepository meetingRepository;
    private final MeetingParticipantRepository participantRepository;
    private final MeetingSessionService sessionService;
    private final MeetingChatService chatService;

    // meetingId -> Set of active WebSocket sessions
    private final Map<String, Set<WebSocketSession>> meetingRooms = new ConcurrentHashMap<>();
    // sessionId -> meetingId
    private final Map<String, String> sessionMeetingMap = new ConcurrentHashMap<>();
    // sessionId -> userId
    private final Map<String, String> sessionUserMap = new ConcurrentHashMap<>();

    public MeetingSignalingHandler(ObjectMapper objectMapper,
                                   MeetingRepository meetingRepository,
                                   MeetingParticipantRepository participantRepository,
                                   MeetingSessionService sessionService,
                                   MeetingChatService chatService) {
        this.objectMapper = objectMapper;
        this.meetingRepository = meetingRepository;
        this.participantRepository = participantRepository;
        this.sessionService = sessionService;
        this.chatService = chatService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        UserPrincipal principal = (UserPrincipal) session.getAttributes().get("userPrincipal");
        String uri = session.getUri() != null ? session.getUri().getPath() : "";
        String meetingId = extractMeetingIdFromUri(uri);

        if (meetingId == null || principal == null) {
            log.warn("Invalid connection parameters. Closing session: {}", session.getId());
            session.close(CloseStatus.BAD_DATA);
            return;
        }

        Optional<Meeting> meetingOpt = meetingRepository.findById(meetingId);
        if (meetingOpt.isEmpty()) {
            meetingOpt = meetingRepository.findByMeetingCode(meetingId);
        }

        if (meetingOpt.isEmpty()) {
            log.warn("Meeting not found: {}. Closing session: {}", meetingId, session.getId());
            session.close(CloseStatus.NOT_ACCEPTABLE);
            return;
        }

        Meeting meeting = meetingOpt.get();
        String resolvedMeetingId = meeting.getId();

        // Verify access
        boolean isHost = meeting.getHostUserId().equals(principal.getUserId());
        boolean isParticipant = participantRepository.findByMeetingIdAndUserId(resolvedMeetingId, principal.getUserId()).isPresent();
        if (!isHost && !principal.isAdmin() && !meeting.isOpenRoom() && !isParticipant) {
            log.warn("User {} is not authorized for meeting {}", principal.getUserId(), resolvedMeetingId);
            session.close(CloseStatus.NOT_ACCEPTABLE);
            return;
        }

        meetingRooms.computeIfAbsent(resolvedMeetingId, k -> Collections.newSetFromMap(new ConcurrentHashMap<>())).add(session);
        sessionMeetingMap.put(session.getId(), resolvedMeetingId);
        sessionUserMap.put(session.getId(), principal.getUserId());

        sessionService.createOrUpdateSession(resolvedMeetingId, principal.getUserId(), session.getId(), meeting.isMuteParticipantsOnEntry());

        log.info("WebSocket connected for user {} in meeting {}", principal.getUserId(), resolvedMeetingId);

        // Notify room about new peer
        Map<String, Object> joinNotification = new HashMap<>();
        joinNotification.put("type", "USER_JOINED");
        joinNotification.put("meetingId", resolvedMeetingId);
        joinNotification.put("userId", principal.getUserId());
        joinNotification.put("name", principal.getName());
        joinNotification.put("email", principal.getEmail());
        joinNotification.put("role", isHost ? "HOST" : "PARTICIPANT");
        joinNotification.put("sessionId", session.getId());

        broadcastToRoom(resolvedMeetingId, session, joinNotification);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        UserPrincipal principal = (UserPrincipal) session.getAttributes().get("userPrincipal");
        String meetingId = sessionMeetingMap.get(session.getId());

        if (meetingId == null || principal == null) {
            return;
        }

        JsonNode rootNode = objectMapper.readTree(message.getPayload());
        String messageType = rootNode.path("type").asText("").toUpperCase();

        Optional<Meeting> meetingOpt = meetingRepository.findById(meetingId);
        if (meetingOpt.isEmpty()) {
            return;
        }
        Meeting meeting = meetingOpt.get();
        boolean isHost = meeting.getHostUserId().equals(principal.getUserId());
        boolean isAdmin = principal.isAdmin();

        // Enforce permissions
        if (!isHost && !isAdmin) {
            if ("SCREEN_SHARE_STARTED".equals(messageType) && !meeting.isAllowScreenSharing()) {
                sendErrorMessage(session, "SCREEN_SHARE_DISABLED", "Screen sharing is not permitted in this meeting.");
                return;
            }
            if ("CAMERA_ON".equals(messageType) && !meeting.isAllowParticipantVideo()) {
                sendErrorMessage(session, "CAMERA_DISABLED", "Video camera is not permitted in this meeting.");
                return;
            }
            if ("UNMUTE".equals(messageType) && !meeting.isAllowParticipantAudio()) {
                sendErrorMessage(session, "MICROPHONE_DISABLED", "Audio microphone is not permitted in this meeting.");
                return;
            }
            if ("CHAT_MESSAGE".equals(messageType) && !meeting.isAllowParticipantChat()) {
                sendErrorMessage(session, "CHAT_DISABLED", "Chat is disabled in this meeting.");
                return;
            }
        }

        // Handle specific message types
        if ("CHAT_MESSAGE".equals(messageType)) {
            String text = rootNode.path("message").asText("");
            if (!text.isBlank()) {
                var savedChat = chatService.sendMessage(principal, meetingId, text);
                Map<String, Object> chatBroadcast = new HashMap<>();
                chatBroadcast.put("type", "CHAT_MESSAGE");
                chatBroadcast.put("meetingId", meetingId);
                chatBroadcast.put("data", savedChat);
                broadcastToRoom(meetingId, null, chatBroadcast); // Send to all including sender
            }
            return;
        }

        if ("MEDIA_STATE_CHANGED".equals(messageType) || "MUTE".equals(messageType) || "UNMUTE".equals(messageType) ||
                "CAMERA_ON".equals(messageType) || "CAMERA_OFF".equals(messageType) ||
                "SCREEN_SHARE_STARTED".equals(messageType) || "SCREEN_SHARE_STOPPED".equals(messageType)) {

            Boolean mic = rootNode.has("microphoneEnabled") ? rootNode.get("microphoneEnabled").asBoolean() :
                    ("UNMUTE".equals(messageType) ? true : ("MUTE".equals(messageType) ? false : null));
            Boolean camera = rootNode.has("cameraEnabled") ? rootNode.get("cameraEnabled").asBoolean() :
                    ("CAMERA_ON".equals(messageType) ? true : ("CAMERA_OFF".equals(messageType) ? false : null));
            Boolean screenShare = rootNode.has("screenSharing") ? rootNode.get("screenSharing").asBoolean() :
                    ("SCREEN_SHARE_STARTED".equals(messageType) ? true : ("SCREEN_SHARE_STOPPED".equals(messageType) ? false : null));

            sessionService.updateMediaState(meetingId, principal.getUserId(), mic, camera, screenShare);
        }

        // Target-specific signaling (OFFER, ANSWER, ICE_CANDIDATE) vs Broadcast
        String targetUserId = rootNode.path("targetUserId").asText(null);
        Map<String, Object> payload = objectMapper.convertValue(rootNode, Map.class);
        payload.put("senderUserId", principal.getUserId());
        payload.put("senderSessionId", session.getId());

        if (targetUserId != null && !targetUserId.isBlank()) {
            sendToUser(meetingId, targetUserId, payload);
        } else {
            broadcastToRoom(meetingId, session, payload);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String meetingId = sessionMeetingMap.remove(session.getId());
        String userId = sessionUserMap.remove(session.getId());

        if (meetingId != null) {
            Set<WebSocketSession> sessions = meetingRooms.get(meetingId);
            if (sessions != null) {
                sessions.remove(session);
                if (sessions.isEmpty()) {
                    meetingRooms.remove(meetingId);
                }
            }

            if (userId != null) {
                sessionService.markUserLeft(meetingId, userId);

                Map<String, Object> leaveNotification = new HashMap<>();
                leaveNotification.put("type", "USER_LEFT");
                leaveNotification.put("meetingId", meetingId);
                leaveNotification.put("userId", userId);
                leaveNotification.put("sessionId", session.getId());

                broadcastToRoom(meetingId, null, leaveNotification);
            }
        }

        log.info("WebSocket disconnected: session={}, user={}", session.getId(), userId);
    }

    private void broadcastToRoom(String meetingId, WebSocketSession excludeSession, Map<String, Object> messageMap) {
        Set<WebSocketSession> sessions = meetingRooms.get(meetingId);
        if (sessions == null || sessions.isEmpty()) return;

        try {
            String json = objectMapper.writeValueAsString(messageMap);
            TextMessage textMessage = new TextMessage(json);

            for (WebSocketSession s : sessions) {
                if (s.isOpen() && (excludeSession == null || !s.getId().equals(excludeSession.getId()))) {
                    try {
                        s.sendMessage(textMessage);
                    } catch (IOException e) {
                        log.warn("Failed to send WS message to session {}: {}", s.getId(), e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            log.error("Broadcast serialization error: {}", e.getMessage());
        }
    }

    private void sendToUser(String meetingId, String targetUserId, Map<String, Object> messageMap) {
        Set<WebSocketSession> sessions = meetingRooms.get(meetingId);
        if (sessions == null || sessions.isEmpty()) return;

        try {
            String json = objectMapper.writeValueAsString(messageMap);
            TextMessage textMessage = new TextMessage(json);

            for (WebSocketSession s : sessions) {
                String sessionUser = sessionUserMap.get(s.getId());
                if (s.isOpen() && targetUserId.equals(sessionUser)) {
                    try {
                        s.sendMessage(textMessage);
                    } catch (IOException e) {
                        log.warn("Failed to send direct WS message to session {}: {}", s.getId(), e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            log.error("Direct message serialization error: {}", e.getMessage());
        }
    }

    private void sendErrorMessage(WebSocketSession session, String code, String message) {
        if (!session.isOpen()) return;
        try {
            Map<String, Object> err = new HashMap<>();
            err.put("type", "ERROR");
            err.put("errorCode", code);
            err.put("message", message);
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(err)));
        } catch (IOException e) {
            log.warn("Failed to send WS error: {}", e.getMessage());
        }
    }

    private String extractMeetingIdFromUri(String uri) {
        if (uri == null) return null;
        String prefix = "/ws/meetings/";
        int idx = uri.indexOf(prefix);
        if (idx != -1) {
            String sub = uri.substring(idx + prefix.length());
            int slashIdx = sub.indexOf('/');
            return slashIdx != -1 ? sub.substring(0, slashIdx) : sub;
        }
        return null;
    }
}
