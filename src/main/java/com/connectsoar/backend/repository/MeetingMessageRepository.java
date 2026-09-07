package com.connectsoar.backend.repository;

import com.connectsoar.backend.model.MeetingMessage;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class MeetingMessageRepository {

    private final Map<String, MeetingMessage> messageStorage = new ConcurrentHashMap<>();

    public MeetingMessage save(MeetingMessage message) {
        if (message.getCreatedAt() == null) {
            message.setCreatedAt(LocalDateTime.now());
        }
        messageStorage.put(message.getId(), message);
        return message;
    }

    public List<MeetingMessage> findAllByMeetingIdOrderByCreatedAtAsc(String meetingId) {
        if (meetingId == null) return List.of();
        return messageStorage.values().stream()
                .filter(m -> meetingId.equals(m.getMeetingId()))
                .sorted(Comparator.comparing(MeetingMessage::getCreatedAt))
                .collect(Collectors.toList());
    }

    public void deleteByMeetingId(String meetingId) {
        messageStorage.values().removeIf(m -> meetingId.equals(m.getMeetingId()));
    }

    public void clear() {
        messageStorage.clear();
    }
}
