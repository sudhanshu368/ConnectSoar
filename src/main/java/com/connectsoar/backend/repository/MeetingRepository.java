package com.connectsoar.backend.repository;

import com.connectsoar.backend.enums.MeetingStatus;
import com.connectsoar.backend.model.Meeting;
import com.connectsoar.backend.model.Profile;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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

    public Optional<Meeting> findByMeetingCode(String meetingCode) {
        if (meetingCode == null || meetingCode.isBlank()) return Optional.empty();
        String normalized = meetingCode.trim().replaceAll("\\s+", "").toUpperCase();
        return meetingStorage.values().stream()
                .filter(m -> m.getMeetingCode() != null && m.getMeetingCode().replaceAll("-", "").equalsIgnoreCase(normalized.replaceAll("-", "")))
                .findFirst();
    }

    public List<Meeting> findAll() {
        return new ArrayList<>(meetingStorage.values());
    }

    public List<Meeting> findAllByHostId(String hostId) {
        if (hostId == null) return List.of();
        return meetingStorage.values().stream()
                .filter(m -> hostId.equals(m.getHostUserId()))
                .sorted(Comparator.comparing(Meeting::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }

    public List<Meeting> findByIds(List<String> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        Set<String> idSet = Set.copyOf(ids);
        return meetingStorage.values().stream()
                .filter(m -> idSet.contains(m.getId()))
                .sorted(Comparator.comparing(Meeting::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }

    public List<Meeting> findFiltered(String tab,
                                      String search,
                                      MeetingStatus filterStatus,
                                      String currentUserId,
                                      boolean isAdmin,
                                      Set<String> participantMeetingIds,
                                      Map<String, Profile> profileMap,
                                      int page,
                                      int size) {
        List<Meeting> stream = filterMeetings(tab, search, filterStatus, currentUserId, isAdmin, participantMeetingIds, profileMap);

        Comparator<Meeting> comparator;
        if ("upcoming".equalsIgnoreCase(tab)) {
            comparator = Comparator.comparing(Meeting::getScheduledStartTime, Comparator.nullsLast(Comparator.naturalOrder()));
        } else if ("live".equalsIgnoreCase(tab)) {
            comparator = Comparator.comparing(Meeting::getStartedAt, Comparator.nullsLast(Comparator.reverseOrder()));
        } else if ("completed".equalsIgnoreCase(tab)) {
            comparator = Comparator.comparing(Meeting::getEndedAt, Comparator.nullsLast(Comparator.reverseOrder()));
        } else {
            comparator = Comparator.comparing(Meeting::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder()));
        }

        int skip = Math.max(0, page) * Math.max(1, size);
        return stream.stream()
                .sorted(comparator)
                .skip(skip)
                .limit(Math.max(1, size))
                .collect(Collectors.toList());
    }

    public long countFiltered(String tab,
                              String search,
                              MeetingStatus filterStatus,
                              String currentUserId,
                              boolean isAdmin,
                              Set<String> participantMeetingIds,
                              Map<String, Profile> profileMap) {
        return filterMeetings(tab, search, filterStatus, currentUserId, isAdmin, participantMeetingIds, profileMap).size();
    }

    private List<Meeting> filterMeetings(String tab,
                                        String search,
                                        MeetingStatus filterStatus,
                                        String currentUserId,
                                        boolean isAdmin,
                                        Set<String> participantMeetingIds,
                                        Map<String, Profile> profileMap) {
        return meetingStorage.values().stream()
                .filter(m -> {
                    // Authorization filter
                    if ("my".equalsIgnoreCase(tab)) {
                        return currentUserId != null && currentUserId.equals(m.getHostUserId());
                    }

                    if (isAdmin) {
                        // Admin can see everything
                        return true;
                    }

                    boolean isHost = currentUserId != null && currentUserId.equals(m.getHostUserId());
                    boolean isParticipant = participantMeetingIds != null && participantMeetingIds.contains(m.getId());
                    boolean isOpen = m.isOpenRoom();

                    return isHost || isParticipant || isOpen;
                })
                .filter(m -> {
                    // Tab filter
                    if ("upcoming".equalsIgnoreCase(tab)) {
                        return m.getStatus() == MeetingStatus.SCHEDULED;
                    } else if ("live".equalsIgnoreCase(tab)) {
                        return m.getStatus() == MeetingStatus.LIVE;
                    } else if ("completed".equalsIgnoreCase(tab)) {
                        return m.getStatus() == MeetingStatus.COMPLETED;
                    } else if ("my".equalsIgnoreCase(tab)) {
                        return true;
                    }
                    return true;
                })
                .filter(m -> {
                    // Explicit status filter if provided
                    if (filterStatus != null) {
                        return m.getStatus() == filterStatus;
                    }
                    return true;
                })
                .filter(m -> {
                    // Search filter
                    if (search == null || search.isBlank()) return true;
                    String query = search.trim().toLowerCase();

                    boolean matchTitle = m.getTitle() != null && m.getTitle().toLowerCase().contains(query);
                    boolean matchCode = m.getMeetingCode() != null && m.getMeetingCode().toLowerCase().contains(query);

                    Profile host = profileMap != null ? profileMap.get(m.getHostUserId()) : null;
                    boolean matchHostName = host != null && host.getName() != null && host.getName().toLowerCase().contains(query);
                    boolean matchHostEmail = host != null && host.getEmail() != null && host.getEmail().toLowerCase().contains(query);

                    return matchTitle || matchCode || matchHostName || matchHostEmail;
                })
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
