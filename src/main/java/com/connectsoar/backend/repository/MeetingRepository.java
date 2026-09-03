package com.connectsoar.backend.repository;

import com.connectsoar.backend.enums.MeetingStatus;
import com.connectsoar.backend.model.Meeting;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class MeetingRepository {

    private final Map<String, Meeting> meetingStorage = new ConcurrentHashMap<>();

    public Meeting save(Meeting meeting) {
        if (meeting.getCreatedAt() == null) {
            meeting.setCreatedAt(LocalDateTime.now());
        }
        meeting.setUpdatedAt(LocalDateTime.now());
        meetingStorage.put(meeting.getId(), meeting);
        return meeting;
    }

    public Optional<Meeting> findById(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(meetingStorage.get(id));
    }

    public List<Meeting> findAll() {
        return new ArrayList<>(meetingStorage.values());
    }

    public List<Meeting> findAllByHostId(String hostId) {
        return meetingStorage.values().stream()
                .filter(m -> hostId.equals(m.getHostId()))
                .sorted(Comparator.comparing(Meeting::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }

    public List<Meeting> findByIds(List<String> ids) {
        return meetingStorage.values().stream()
                .filter(m -> ids.contains(m.getId()))
                .sorted(Comparator.comparing(Meeting::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }

    public void deleteById(String id) {
        if (id != null) {
            meetingStorage.remove(id);
        }
    }

    public void clear() {
        meetingStorage.clear();
    }
}
