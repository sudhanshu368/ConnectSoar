package com.connectsoar.backend.scheduler;

import com.connectsoar.backend.enums.ProjectStatus;
import com.connectsoar.backend.model.Project;
import com.connectsoar.backend.repository.ProjectRepository;
import com.connectsoar.backend.service.ProjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@EnableScheduling
public class ProjectInactivityScheduler {

    private static final Logger log = LoggerFactory.getLogger(ProjectInactivityScheduler.class);

    private final ProjectRepository projectRepository;
    private final ProjectService projectService;

    public ProjectInactivityScheduler(ProjectRepository projectRepository, ProjectService projectService) {
        this.projectRepository = projectRepository;
        this.projectService = projectService;
    }

    /**
     * Business Rule:
     * "project me 3 days continue km se km ek bhi module pe work nhi hoga means status change nhi hoga to project ka status hold ho jayega"
     * 
     * Runs every 10 minutes to verify all in-progress projects against the 3-day inactivity threshold.
     */
    @Scheduled(fixedRate = 600000) // Every 10 minutes
    public void evaluateProjectInactivity() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime cutoff = now.minusDays(3);

        List<Project> inactiveProjects = projectRepository.findActiveProjectsInactiveSince(cutoff);

        if (!inactiveProjects.isEmpty()) {
            log.info("Found {} projects inactive for >= 3 continuous days. Processing auto-hold...", inactiveProjects.size());

            for (Project project : inactiveProjects) {
                long days = ChronoUnit.DAYS.between(project.getLastActivityAt(), now);
                log.warn("Auto-holding project '{}' (ID: {}). Inactivity duration: {} days.", project.getName(), project.getId(), days);

                project.setStatus(ProjectStatus.ON_HOLD);
                project.setUpdatedAt(now);
                projectRepository.save(project);

                projectService.logActivity(project.getId(), null, "system", "System Automation",
                        "STATUS_AUTO_HOLD", ProjectStatus.IN_PROGRESS.name(), ProjectStatus.ON_HOLD.name(),
                        "Project automatically placed ON_HOLD due to 3 continuous days without any module status changes (" + days + " days inactive).");
            }
        }
    }
}
