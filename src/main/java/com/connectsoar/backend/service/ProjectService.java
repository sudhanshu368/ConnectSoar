package com.connectsoar.backend.service;

import com.connectsoar.backend.dto.*;
import com.connectsoar.backend.enums.ErrorCode;
import com.connectsoar.backend.enums.ModuleStatus;
import com.connectsoar.backend.enums.ModuleType;
import com.connectsoar.backend.enums.ProjectMemberRole;
import com.connectsoar.backend.enums.ProjectStatus;
import com.connectsoar.backend.enums.Role;
import com.connectsoar.backend.exception.ApiException;
import com.connectsoar.backend.model.*;
import com.connectsoar.backend.repository.*;
import com.connectsoar.backend.security.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProjectService {

    private static final Logger log = LoggerFactory.getLogger(ProjectService.class);

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectModuleRepository projectModuleRepository;
    private final ProjectActivityLogRepository activityLogRepository;
    private final ProfileRepository profileRepository;

    public ProjectService(ProjectRepository projectRepository,
                          ProjectMemberRepository projectMemberRepository,
                          ProjectModuleRepository projectModuleRepository,
                          ProjectActivityLogRepository activityLogRepository,
                          ProfileRepository profileRepository) {
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.projectModuleRepository = projectModuleRepository;
        this.activityLogRepository = activityLogRepository;
        this.profileRepository = profileRepository;
    }

    public List<ProjectResponse> getProjectsForUser(UserPrincipal user) {
        List<Project> all = projectRepository.findAll();
        List<Project> accessible;

        if (user.isAdmin()) {
            accessible = all;
        } else {
            // Manager or Employee: only projects where they are assigned as a member or manager or creator
            List<String> userProjectIds = projectMemberRepository.findByUserId(user.getUserId())
                    .stream()
                    .map(ProjectMember::getProjectId)
                    .collect(Collectors.toList());

            accessible = all.stream()
                    .filter(p -> userProjectIds.contains(p.getId()) || user.getUserId().equals(p.getCreatedByUserId()))
                    .collect(Collectors.toList());
        }

        // Apply 3-day inactivity check: if inactive >= 3 days, auto-transition to ON_HOLD
        LocalDateTime now = LocalDateTime.now();
        for (Project project : accessible) {
            checkAndApplyInactivityHold(project, now);
        }

        return accessible.stream()
                .map(p -> buildProjectResponse(p, false))
                .collect(Collectors.toList());
    }

    public ProjectResponse getProjectById(String projectId, UserPrincipal user) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ApiException(ErrorCode.PROJECT_NOT_FOUND, "Project not found.", HttpStatus.NOT_FOUND));

        if (!canUserAccessProject(project, user)) {
            throw new ApiException(ErrorCode.FORBIDDEN, "Access denied: you are not a member of this project.", HttpStatus.FORBIDDEN);
        }

        checkAndApplyInactivityHold(project, LocalDateTime.now());
        return buildProjectResponse(project, true);
    }

    public ProjectResponse createProject(CreateProjectRequest request, UserPrincipal user) {
        if (!user.isAdmin() && user.getRole() != Role.manager) {
            throw new ApiException(ErrorCode.FORBIDDEN, "Only Admins and Managers can create projects.", HttpStatus.FORBIDDEN);
        }

        String projectId = UUID.randomUUID().toString();
        Project project = Project.builder()
                .id(projectId)
                .name(request.getName().trim())
                .description(request.getDescription())
                .status(ProjectStatus.IN_PROGRESS)
                .startDate(request.getStartDate() != null ? request.getStartDate() : LocalDate.now())
                .deadline(request.getDeadline())
                .createdByUserId(user.getUserId())
                .lastActivityAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        projectRepository.save(project);

        // If manager or admin created, add them as MANAGER in project_members
        ProjectMember creatorMember = ProjectMember.builder()
                .id(UUID.randomUUID().toString())
                .projectId(projectId)
                .userId(user.getUserId())
                .memberRole(ProjectMemberRole.MANAGER)
                .assignedAt(LocalDateTime.now())
                .build();
        projectMemberRepository.save(creatorMember);

        // Activity log
        logActivity(projectId, null, user.getUserId(), user.getName(), "PROJECT_CREATED", null,
                ProjectStatus.IN_PROGRESS.name(), "Project created with deadline " + project.getDeadline());

        log.info("Project created: {} by user {}", project.getName(), user.getUserId());
        return buildProjectResponse(project, true);
    }

    public ProjectResponse updateProject(String projectId, UpdateProjectRequest request, UserPrincipal user) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ApiException(ErrorCode.PROJECT_NOT_FOUND, "Project not found.", HttpStatus.NOT_FOUND));

        if (!canUserManageProject(project, user)) {
            throw new ApiException(ErrorCode.FORBIDDEN, "Only Admins and Project Managers can edit this project.", HttpStatus.FORBIDDEN);
        }

        String oldStatus = project.getStatus().name();
        if (request.getName() != null && !request.getName().isBlank()) {
            project.setName(request.getName().trim());
        }
        if (request.getDescription() != null) {
            project.setDescription(request.getDescription());
        }
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            ProjectStatus newStatus = ProjectStatus.fromString(request.getStatus());
            if (newStatus != project.getStatus()) {
                project.setStatus(newStatus);
                logActivity(projectId, null, user.getUserId(), user.getName(), "PROJECT_STATUS_CHANGED",
                        oldStatus, newStatus.name(), "Project status updated to " + newStatus);
            }
        }
        if (request.getStartDate() != null) {
            project.setStartDate(request.getStartDate());
        }
        if (request.getDeadline() != null) {
            LocalDate oldDeadline = project.getDeadline();
            project.setDeadline(request.getDeadline());
            if (!request.getDeadline().equals(oldDeadline)) {
                logActivity(projectId, null, user.getUserId(), user.getName(), "DEADLINE_CHANGED",
                        String.valueOf(oldDeadline), String.valueOf(request.getDeadline()), "Project deadline changed");
            }
        }

        project.setUpdatedAt(LocalDateTime.now());
        projectRepository.save(project);

        return buildProjectResponse(project, true);
    }

    public void deleteProject(String projectId, UserPrincipal user) {
        if (!user.isAdmin()) {
            throw new ApiException(ErrorCode.FORBIDDEN, "Only Admins can delete or archive projects.", HttpStatus.FORBIDDEN);
        }

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ApiException(ErrorCode.PROJECT_NOT_FOUND, "Project not found.", HttpStatus.NOT_FOUND));

        projectModuleRepository.deleteByProjectId(projectId);
        projectMemberRepository.deleteByProjectId(projectId);
        activityLogRepository.deleteByProjectId(projectId);
        projectRepository.deleteById(projectId);

        log.info("Project {} deleted by Admin {}", projectId, user.getUserId());
    }

    public ProjectMemberDto addProjectMember(String projectId, AddProjectMemberRequest request, UserPrincipal user) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ApiException(ErrorCode.PROJECT_NOT_FOUND, "Project not found.", HttpStatus.NOT_FOUND));

        if (!canUserManageProject(project, user)) {
            throw new ApiException(ErrorCode.FORBIDDEN, "Only Admins and Project Managers can add members.", HttpStatus.FORBIDDEN);
        }

        Profile targetProfile = profileRepository.findById(request.getUserId())
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND, "User to add not found.", HttpStatus.NOT_FOUND));

        ProjectMemberRole role = ProjectMemberRole.fromString(request.getMemberRole());

        // Check if already member
        Optional<ProjectMember> existing = projectMemberRepository.findByProjectIdAndUserId(projectId, request.getUserId());
        ProjectMember member;
        if (existing.isPresent()) {
            member = existing.get();
            member.setMemberRole(role);
        } else {
            member = ProjectMember.builder()
                    .id(UUID.randomUUID().toString())
                    .projectId(projectId)
                    .userId(request.getUserId())
                    .memberRole(role)
                    .assignedAt(LocalDateTime.now())
                    .build();
        }
        projectMemberRepository.save(member);

        logActivity(projectId, null, user.getUserId(), user.getName(), "MEMBER_ASSIGNED", null,
                role.name(), "Added " + targetProfile.getName() + " as " + role.name());

        return toMemberDto(member);
    }

    public void removeProjectMember(String projectId, String userId, UserPrincipal user) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ApiException(ErrorCode.PROJECT_NOT_FOUND, "Project not found.", HttpStatus.NOT_FOUND));

        if (!canUserManageProject(project, user)) {
            throw new ApiException(ErrorCode.FORBIDDEN, "Only Admins and Project Managers can remove members.", HttpStatus.FORBIDDEN);
        }

        projectMemberRepository.deleteByProjectIdAndUserId(projectId, userId);
        logActivity(projectId, null, user.getUserId(), user.getName(), "MEMBER_REMOVED", userId, null, "Removed member from project");
    }

    public List<ProjectActivityLogDto> getProjectActivityLogs(String projectId, UserPrincipal user) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ApiException(ErrorCode.PROJECT_NOT_FOUND, "Project not found.", HttpStatus.NOT_FOUND));

        if (!canUserAccessProject(project, user)) {
            throw new ApiException(ErrorCode.FORBIDDEN, "Access denied to project activity logs.", HttpStatus.FORBIDDEN);
        }

        return activityLogRepository.findByProjectId(projectId).stream()
                .map(l -> new ProjectActivityLogDto(l.getId(), l.getProjectId(), l.getModuleId(),
                        l.getUserId(), l.getUserName(), l.getAction(), l.getOldStatus(), l.getNewStatus(),
                        l.getDetails(), l.getCreatedAt()))
                .collect(Collectors.toList());
    }

    public ProjectSummaryStatsDto getSummaryStats(UserPrincipal user) {
        List<ProjectResponse> projects = getProjectsForUser(user);

        int totalProjects = projects.size();
        int activeProjects = 0;
        int holdProjects = 0;
        int completedProjects = 0;
        int delayedProjects = 0;
        int totalModules = 0;
        int completedModules = 0;
        int delayedModules = 0;

        for (ProjectResponse p : projects) {
            if ("IN_PROGRESS".equalsIgnoreCase(p.getStatus())) activeProjects++;
            else if ("ON_HOLD".equalsIgnoreCase(p.getStatus())) holdProjects++;
            else if ("COMPLETED".equalsIgnoreCase(p.getStatus())) completedProjects++;

            if (p.isDelayed()) delayedProjects++;

            totalModules += p.getTotalModules();
            completedModules += p.getCompletedModules();

            for (ModuleResponse m : p.getModules()) {
                if (m.isDelayed()) delayedModules++;
            }
        }

        return new ProjectSummaryStatsDto(totalProjects, activeProjects, holdProjects,
                completedProjects, delayedProjects, totalModules, completedModules, delayedModules);
    }

    // ---------------- Helper & Business Rule Methods ----------------

    /**
     * Business Rule: If a project has 3 continuous days without any module status update / activity,
     * it automatically transitions to ON_HOLD.
     */
    public void checkAndApplyInactivityHold(Project project, LocalDateTime now) {
        if (project.getStatus() == ProjectStatus.IN_PROGRESS && project.getLastActivityAt() != null) {
            long daysInactive = ChronoUnit.DAYS.between(project.getLastActivityAt(), now);
            if (daysInactive >= 3) {
                log.info("Project {} has been inactive for {} days. Auto-holding.", project.getName(), daysInactive);
                project.setStatus(ProjectStatus.ON_HOLD);
                project.setUpdatedAt(now);
                projectRepository.save(project);

                logActivity(project.getId(), null, "system", "System Automation", "STATUS_AUTO_HOLD",
                        ProjectStatus.IN_PROGRESS.name(), ProjectStatus.ON_HOLD.name(),
                        "Project automatically set to ON_HOLD due to 3 consecutive days of module inactivity (" + daysInactive + " days).");
            }
        }
    }

    public boolean canUserAccessProject(Project project, UserPrincipal user) {
        if (user.isAdmin()) return true;
        if (user.getUserId().equals(project.getCreatedByUserId())) return true;
        return projectMemberRepository.findByProjectIdAndUserId(project.getId(), user.getUserId()).isPresent();
    }

    public boolean canUserManageProject(Project project, UserPrincipal user) {
        if (user.isAdmin()) return true;
        if (user.getRole() != Role.manager) return false;
        if (user.getUserId().equals(project.getCreatedByUserId())) return true;
        return projectMemberRepository.findByProjectIdAndUserId(project.getId(), user.getUserId())
                .map(m -> m.getMemberRole() == ProjectMemberRole.MANAGER)
                .orElse(false);
    }

    public void logActivity(String projectId, String moduleId, String userId, String userName,
                            String action, String oldStatus, String newStatus, String details) {
        ProjectActivityLog log = ProjectActivityLog.builder()
                .id(UUID.randomUUID().toString())
                .projectId(projectId)
                .moduleId(moduleId)
                .userId(userId)
                .userName(userName != null ? userName : "User")
                .action(action)
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .details(details)
                .createdAt(LocalDateTime.now())
                .build();
        activityLogRepository.save(log);
    }

    public ProjectResponse buildProjectResponse(Project project, boolean includeModules) {
        List<ProjectMember> members = projectMemberRepository.findByProjectId(project.getId());
        List<ProjectMemberDto> memberDtos = members.stream().map(this::toMemberDto).collect(Collectors.toList());

        List<ProjectModule> modules = projectModuleRepository.findByProjectId(project.getId());
        List<ModuleResponse> moduleDtos = includeModules ? modules.stream().map(this::toModuleResponse).collect(Collectors.toList()) : Collections.emptyList();

        int totalModules = modules.size();
        int completedModules = (int) modules.stream().filter(m -> m.getStatus() == ModuleStatus.COMPLETED).count();

        // Calculate overall, frontend and backend progress
        int overallProgress = totalModules == 0 ? 0 : (int) Math.round(modules.stream().mapToInt(ProjectModule::getProgressPercentage).average().orElse(0));
        
        List<ProjectModule> feModules = modules.stream().filter(m -> m.getModuleType() == ModuleType.FRONTEND).collect(Collectors.toList());
        int feProgress = feModules.isEmpty() ? 0 : (int) Math.round(feModules.stream().mapToInt(ProjectModule::getProgressPercentage).average().orElse(0));

        List<ProjectModule> beModules = modules.stream().filter(m -> m.getModuleType() == ModuleType.BACKEND).collect(Collectors.toList());
        int beProgress = beModules.isEmpty() ? 0 : (int) Math.round(beModules.stream().mapToInt(ProjectModule::getProgressPercentage).average().orElse(0));

        LocalDate today = LocalDate.now();
        boolean isDelayed = project.getDeadline() != null && today.isAfter(project.getDeadline()) && project.getStatus() != ProjectStatus.COMPLETED;

        LocalDateTime lastAct = project.getLastActivityAt() != null ? project.getLastActivityAt() : project.getCreatedAt();
        long daysInactive = lastAct != null ? ChronoUnit.DAYS.between(lastAct, LocalDateTime.now()) : 0;
        boolean isHoldDueToInactivity = project.getStatus() == ProjectStatus.ON_HOLD && daysInactive >= 3;

        return ProjectResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .status(project.getStatus().name())
                .startDate(project.getStartDate())
                .deadline(project.getDeadline())
                .createdByUserId(project.getCreatedByUserId())
                .lastActivityAt(project.getLastActivityAt())
                .totalModules(totalModules)
                .completedModules(completedModules)
                .overallProgressPercentage(overallProgress)
                .frontendProgressPercentage(feProgress)
                .backendProgressPercentage(beProgress)
                .isDelayed(isDelayed)
                .isHoldDueToInactivity(isHoldDueToInactivity)
                .daysInactive(daysInactive)
                .members(memberDtos)
                .modules(moduleDtos)
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .build();
    }

    public ProjectMemberDto toMemberDto(ProjectMember member) {
        Profile p = profileRepository.findById(member.getUserId()).orElse(null);
        return ProjectMemberDto.builder()
                .id(member.getId())
                .projectId(member.getProjectId())
                .userId(member.getUserId())
                .name(p != null ? p.getName() : "Team Member")
                .email(p != null ? p.getEmail() : "")
                .imageUrl(p != null ? p.getImageUrl() : null)
                .memberRole(member.getMemberRole().name())
                .assignedAt(member.getAssignedAt())
                .build();
    }

    public ModuleResponse toModuleResponse(ProjectModule m) {
        LocalDate today = LocalDate.now();
        boolean isDelayed = m.getDeadline() != null && today.isAfter(m.getDeadline()) && m.getStatus() != ModuleStatus.COMPLETED;

        return ModuleResponse.builder()
                .id(m.getId())
                .projectId(m.getProjectId())
                .title(m.getTitle())
                .description(m.getDescription())
                .moduleType(m.getModuleType().name())
                .status(m.getStatus().name())
                .progressPercentage(m.getProgressPercentage())
                .assignedToUserId(m.getAssignedToUserId())
                .assignedToName(m.getAssignedToName())
                .deadline(m.getDeadline())
                .isDelayed(isDelayed)
                .lastUpdatedByUserId(m.getLastUpdatedByUserId())
                .lastUpdatedByName(m.getLastUpdatedByName())
                .lastUpdatedAt(m.getLastUpdatedAt())
                .createdAt(m.getCreatedAt())
                .updatedAt(m.getUpdatedAt())
                .build();
    }
}
