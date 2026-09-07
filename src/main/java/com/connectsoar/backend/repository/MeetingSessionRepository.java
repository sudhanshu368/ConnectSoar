package com.connectsoar.backend.repository;

import com.connectsoar.backend.model.MeetingSession;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class MeetingSessionRepository {

    private final Map<String, MeetingSession> sessionStorage = new ConcurrentHashMap<>();

    public MeetingSession save(MeetingSession session) {
        if (session.getCreatedAt() == null) {
            session.setCreatedAt(LocalDateTime.now());
        }
        session.setUpdatedAt(LocalDateTime.now());
        sessionStorage.put(session.getId(), session);
        return session;
    }

    public Optional<MeetingSession> findById(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(sessionStorage.get(id));
    }

    public Optional<MeetingSession> findByMeetingIdAndUserId(String meetingId, String userId) {
        if (meetingId == null || userId == null) return Optional.empty();
        return sessionStorage.values().stream()
                .filter(s -> meetingId.equals(s.getMeetingId()) && userId.equals(s.getUserId()))
                .findFirst();
    }

    public Optional<MeetingSession> findBySessionId(String sessionId) {
        if (sessionId == null) return Optional.empty();
        return sessionStorage.values().stream()
                .filter(s -> sessionId.equals(s.getSessionId()))
                .findFirst();
    }

    public Optional<MeetingSession> findByMeetingIdAndSessionId(String meetingId, String sessionId) {
        if (meetingId == null || sessionId == null) return Optional.empty();
        return sessionStorage.values().stream()
                .filter(s -> meetingId.equals(s.getMeetingId()) && sessionId.equals(s.getSessionId()))
                .findFirst();
    }

    public List<MeetingSession> findAllByMeetingId(String meetingId) {
        if (meetingId == null) return List.of();
        return sessionStorage.values().stream()
                .filter(s -> meetingId.equals(s.getMeetingId()))
                .collect(Collectors.toList());
    }

    public List<MeetingSession> findAll() {
        return new ArrayList<>(sessionStorage.values());
    }

    public void deleteByMeetingIdAndUserId(String meetingId, String userId) {
        sessionStorage.values().removeIf(s -> meetingId.equals(s.getMeetingId()) && userId.equals(s.getUserId()));
    }

    public void deleteBySessionId(String sessionId) {
        sessionStorage.values().removeIf(s -> sessionId.equals(s.getSessionId()));
    }

    public void clear() {
        sessionStorage.clear();
    }
}
