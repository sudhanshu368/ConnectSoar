package com.connectsoar.backend.repository;

import com.connectsoar.backend.model.MeetingParticipant;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class MeetingParticipantRepository {

    private final Map<String, MeetingParticipant> participantStorage = new ConcurrentHashMap<>();

    public MeetingParticipant save(MeetingParticipant participant) {
        if (participant.getCreatedAt() == null) {
            participant.setCreatedAt(LocalDateTime.now());
        }
        participant.setUpdatedAt(LocalDateTime.now());
        participantStorage.put(participant.getId(), participant);
        return participant;
    }

    public Optional<MeetingParticipant> findById(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(participantStorage.get(id));
    }

    public Optional<MeetingParticipant> findByMeetingIdAndUserId(String meetingId, String userId) {
        if (meetingId == null || userId == null) return Optional.empty();
        return participantStorage.values().stream()
                .filter(p -> meetingId.equals(p.getMeetingId()) && userId.equals(p.getUserId()))
                .findFirst();
    }

    public List<MeetingParticipant> findAllByMeetingId(String meetingId) {
        if (meetingId == null) return List.of();
        return participantStorage.values().stream()
                .filter(p -> meetingId.equals(p.getMeetingId()))
                .collect(Collectors.toList());
    }

    public List<MeetingParticipant> findAllByUserId(String userId) {
        if (userId == null) return List.of();
        return participantStorage.values().stream()
                .filter(p -> userId.equals(p.getUserId()))
                .collect(Collectors.toList());
    }

    public int countByMeetingId(String meetingId) {
        if (meetingId == null) return 0;
        return (int) participantStorage.values().stream()
                .filter(p -> meetingId.equals(p.getMeetingId()))
                .count();
    }

    public void deleteByMeetingIdAndUserId(String meetingId, String userId) {
        participantStorage.values().removeIf(p -> meetingId.equals(p.getMeetingId()) && userId.equals(p.getUserId()));
    }

    public void deleteByMeetingId(String meetingId) {
        participantStorage.values().removeIf(p -> meetingId.equals(p.getMeetingId()));
    }

    public void clear() {
        participantStorage.clear();
    }
}
