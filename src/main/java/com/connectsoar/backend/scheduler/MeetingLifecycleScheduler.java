package com.connectsoar.backend.scheduler;

import com.connectsoar.backend.enums.MeetingStatus;
import com.connectsoar.backend.enums.MeetingType;
import com.connectsoar.backend.model.Meeting;
import com.connectsoar.backend.repository.MeetingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@EnableScheduling
public class MeetingLifecycleScheduler {

    private static final Logger log = LoggerFactory.getLogger(MeetingLifecycleScheduler.class);

    private final MeetingRepository meetingRepository;

    public MeetingLifecycleScheduler(MeetingRepository meetingRepository) {
        this.meetingRepository = meetingRepository;
    }

    /**
     * Checks scheduled meetings every 60 seconds.
     */
    @Scheduled(fixedRate = 60000)
    public void processScheduledMeetings() {
        LocalDateTime now = LocalDateTime.now();
        List<Meeting> allMeetings = meetingRepository.findAll();

        for (Meeting meeting : allMeetings) {
            // Auto-complete meetings that exceeded their scheduled duration significantly (e.g. ended 24 hours ago)
            if (meeting.getStatus() == MeetingStatus.LIVE && meeting.getScheduledEndTime() != null) {
                if (now.isAfter(meeting.getScheduledEndTime().plusHours(4))) {
                    log.info("Auto-completing prolonged meeting: {}", meeting.getId());
                    meeting.setStatus(MeetingStatus.COMPLETED);
                    meeting.setEndedAt(now);
                    meeting.setUpdatedAt(now);
                    meetingRepository.save(meeting);
                }
            }
        }
    }
}
