package com.connectsoar.backend.repository;

import com.connectsoar.backend.model.MeetingInvitation;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class MeetingInvitationRepository {

    private final Map<String, MeetingInvitation> invitationStorage = new ConcurrentHashMap<>();

    public MeetingInvitation save(MeetingInvitation invitation) {
        if (invitation.getCreatedAt() == null) {
            invitation.setCreatedAt(LocalDateTime.now());
        }
        invitation.setUpdatedAt(LocalDateTime.now());
        invitationStorage.put(invitation.getId(), invitation);
        return invitation;
    }

    public Optional<MeetingInvitation> findById(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(invitationStorage.get(id));
    }

    public Optional<MeetingInvitation> findByMeetingIdAndEmail(String meetingId, String email) {
        if (meetingId == null || email == null) return Optional.empty();
        return invitationStorage.values().stream()
                .filter(i -> meetingId.equals(i.getMeetingId()) && email.equalsIgnoreCase(i.getEmail()))
                .findFirst();
    }

    public Optional<MeetingInvitation> findByToken(String token) {
        if (token == null) return Optional.empty();
        return invitationStorage.values().stream()
                .filter(i -> token.equals(i.getToken()))
                .findFirst();
    }

    public List<MeetingInvitation> findAllByMeetingId(String meetingId) {
        if (meetingId == null) return List.of();
        return invitationStorage.values().stream()
                .filter(i -> meetingId.equals(i.getMeetingId()))
                .collect(Collectors.toList());
    }

    public List<MeetingInvitation> findAll() {
        return new ArrayList<>(invitationStorage.values());
    }

    public void deleteByMeetingId(String meetingId) {
        invitationStorage.values().removeIf(i -> meetingId.equals(i.getMeetingId()));
    }

    public void clear() {
        invitationStorage.clear();
    }
}
