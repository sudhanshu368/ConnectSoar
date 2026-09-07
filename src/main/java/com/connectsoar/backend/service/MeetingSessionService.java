package com.connectsoar.backend.service;

import com.connectsoar.backend.model.MeetingSession;
import com.connectsoar.backend.repository.MeetingSessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class MeetingSessionService {

    private static final Logger log = LoggerFactory.getLogger(MeetingSessionService.class);

    private final MeetingSessionRepository sessionRepository;

    public MeetingSessionService(MeetingSessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    public MeetingSession createOrUpdateSession(String meetingId, String userId, String sessionId, boolean muteOnEntry) {
        Optional<MeetingSession> existing = sessionRepository.findByMeetingIdAndUserId(meetingId, userId);
        LocalDateTime now = LocalDateTime.now();

        MeetingSession session;
        if (existing.isPresent()) {
            session = existing.get();
            session.setSessionId(sessionId);
            session.setConnectionStatus("CONNECTED");
            session.setLastSeenAt(now);
            session.setLeftAt(null);
            session.setUpdatedAt(now);
            if (muteOnEntry) {
                session.setMicrophoneEnabled(false);
            }
        } else {
            session = MeetingSession.builder()
                    .id(UUID.randomUUID().toString())
                    .meetingId(meetingId)
                    .userId(userId)
                    .sessionId(sessionId)
                    .connectionStatus("CONNECTED")
                    .microphoneEnabled(!muteOnEntry)
                    .cameraEnabled(true)
                    .screenSharing(false)
                    .joinedAt(now)
                    .lastSeenAt(now)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();
        }

        return sessionRepository.save(session);
    }

    public MeetingSession updateMediaState(String meetingId, String userId, Boolean mic, Boolean camera, Boolean screenShare) {
        Optional<MeetingSession> existing = sessionRepository.findByMeetingIdAndUserId(meetingId, userId);
        LocalDateTime now = LocalDateTime.now();

        MeetingSession session;
        if (existing.isPresent()) {
            session = existing.get();
            if (mic != null) session.setMicrophoneEnabled(mic);
            if (camera != null) session.setCameraEnabled(camera);
            if (screenShare != null) session.setScreenSharing(screenShare);
            session.setLastSeenAt(now);
            session.setUpdatedAt(now);
        } else {
            session = MeetingSession.builder()
                    .id(UUID.randomUUID().toString())
                    .meetingId(meetingId)
                    .userId(userId)
                    .sessionId(UUID.randomUUID().toString())
                    .connectionStatus("CONNECTED")
                    .microphoneEnabled(mic != null ? mic : true)
                    .cameraEnabled(camera != null ? camera : true)
                    .screenSharing(screenShare != null ? screenShare : false)
                    .joinedAt(now)
                    .lastSeenAt(now)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();
        }

        return sessionRepository.save(session);
    }

    public void removeSession(String sessionId) {
        sessionRepository.deleteBySessionId(sessionId);
    }

    public void markUserLeft(String meetingId, String userId) {
        Optional<MeetingSession> sessionOpt = sessionRepository.findByMeetingIdAndUserId(meetingId, userId);
        if (sessionOpt.isPresent()) {
            MeetingSession session = sessionOpt.get();
            session.setConnectionStatus("DISCONNECTED");
            session.setLeftAt(LocalDateTime.now());
            sessionRepository.save(session);
        }
    }

    public List<MeetingSession> getActiveSessions(String meetingId) {
        return sessionRepository.findAllByMeetingId(meetingId);
    }
}
