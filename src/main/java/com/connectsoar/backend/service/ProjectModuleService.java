package com.connectsoar.backend.service;

import com.connectsoar.backend.dto.CreateModuleRequest;
import com.connectsoar.backend.dto.ModuleResponse;
import com.connectsoar.backend.dto.UpdateModuleRequest;
import com.connectsoar.backend.dto.UpdateModuleStatusRequest;
import com.connectsoar.backend.enums.ErrorCode;
import com.connectsoar.backend.enums.ModuleStatus;
import com.connectsoar.backend.enums.ModuleType;
import com.connectsoar.backend.enums.ProjectStatus;
import com.connectsoar.backend.enums.Role;
import com.connectsoar.backend.exception.ApiException;
import com.connectsoar.backend.model.Profile;
import com.connectsoar.backend.model.Project;
import com.connectsoar.backend.model.ProjectModule;
import com.connectsoar.backend.repository.ProfileRepository;
import com.connectsoar.backend.repository.ProjectModuleRepository;
import com.connectsoar.backend.repository.ProjectRepository;
import com.connectsoar.backend.security.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProjectModuleService {

    private static final Logger log = LoggerFactory.getLogger(ProjectModuleService.class);

    private final ProjectModuleRepository moduleRepository;
    private final ProjectRepository projectRepository;
    private final ProfileRepository profileRepository;
    private final ProjectService projectService;

    public ProjectModuleService(ProjectModuleRepository moduleRepository,
                                ProjectRepository projectRepository,
                                ProfileRepository profileRepository,
                                ProjectService projectService) {
        this.moduleRepository = moduleRepository;
        this.projectRepository = projectRepository;
        this.profileRepository = profileRepository;
        this.projectService = projectService;
    }

    public List<ModuleResponse> getModulesByProject(String projectId, UserPrincipal user) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ApiException(ErrorCode.PROJECT_NOT_FOUND, "Project not found.", HttpStatus.NOT_FOUND));

        if (!projectService.canUserAccessProject(project, user)) {
            throw new ApiException(ErrorCode.FORBIDDEN, "Access denied: you are not a member of this project.", HttpStatus.FORBIDDEN);
        }

        return moduleRepository.findByProjectId(projectId).stream()
                .map(projectService::toModuleResponse)
                .collect(Collectors.toList());
    }

    public List<ModuleResponse> getMyAssignedModules(UserPrincipal user) {
        return moduleRepository.findByAssignedToUserId(user.getUserId()).stream()
                .map(projectService::toModuleResponse)
                .collect(Collectors.toList());
    }

    public ModuleResponse createModule(String projectId, CreateModuleRequest request, UserPrincipal user) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ApiException(ErrorCode.PROJECT_NOT_FOUND, "Project not found.", HttpStatus.NOT_FOUND));

        if (!projectService.canUserManageProject(project, user)) {
            throw new ApiException(ErrorCode.FORBIDDEN, "Only Admins and Project Managers can create modules.", HttpStatus.FORBIDDEN);
        }

        String assignedName = request.getAssignedToName();
        if (request.getAssignedToUserId() != null && (assignedName == null || assignedName.isBlank())) {
            assignedName = profileRepository.findById(request.getAssignedToUserId())
                    .map(Profile::getName)
                    .orElse("Assigned Member");
        }

        String moduleId = UUID.randomUUID().toString();
        ProjectModule module = ProjectModule.builder()
                .id(moduleId)
                .projectId(projectId)
                .title(request.getTitle().trim())
                .description(request.getDescription())
                .moduleType(ModuleType.fromString(request.getModuleType()))
                .status(ModuleStatus.TODO)
                .progressPercentage(0)
                .assignedToUserId(request.getAssignedToUserId())
                .assignedToName(assignedName)
                .createdByUserId(user.getUserId())
                .deadline(request.getDeadline())
                .lastUpdatedByUserId(user.getUserId())
                .lastUpdatedByName(user.getName())
                .lastUpdatedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        moduleRepository.save(module);

        // Update project lastActivityAt
        project.setLastActivityAt(LocalDateTime.now());
        project.setUpdatedAt(LocalDateTime.now());
        projectRepository.save(project);

        projectService.logActivity(projectId, moduleId, user.getUserId(), user.getName(),
                "MODULE_CREATED", null, ModuleStatus.TODO.name(),
                "Module '" + module.getTitle() + "' created and assigned to " + (assignedName != null ? assignedName : "unassigned"));

        log.info("Created module: {} for project: {} by user: {}", module.getTitle(), projectId, user.getUserId());
        return projectService.toModuleResponse(module);
    }

    public ModuleResponse updateModule(String moduleId, UpdateModuleRequest request, UserPrincipal user) {
        ProjectModule module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new ApiException(ErrorCode.MODULE_NOT_FOUND, "Module not found.", HttpStatus.NOT_FOUND));

        Project project = projectRepository.findById(module.getProjectId())
                .orElseThrow(() -> new ApiException(ErrorCode.PROJECT_NOT_FOUND, "Project not found.", HttpStatus.NOT_FOUND));

        if (!projectService.canUserManageProject(project, user)) {
            throw new ApiException(ErrorCode.FORBIDDEN, "Only Admins and Project Managers can edit module specifications.", HttpStatus.FORBIDDEN);
        }

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            module.setTitle(request.getTitle().trim());
        }
        if (request.getDescription() != null) {
            module.setDescription(request.getDescription());
        }
        if (request.getModuleType() != null) {
            module.setModuleType(ModuleType.fromString(request.getModuleType()));
        }
        if (request.getAssignedToUserId() != null) {
            module.setAssignedToUserId(request.getAssignedToUserId());
            String assignedName = request.getAssignedToName();
            if (assignedName == null || assignedName.isBlank()) {
                assignedName = profileRepository.findById(request.getAssignedToUserId())
                        .map(Profile::getName)
                        .orElse("Assigned Member");
            }
            module.setAssignedToName(assignedName);
        }
        if (request.getDeadline() != null) {
            module.setDeadline(request.getDeadline());
        }

        module.setLastUpdatedByUserId(user.getUserId());
        module.setLastUpdatedByName(user.getName());
        module.setLastUpdatedAt(LocalDateTime.now());
        module.setUpdatedAt(LocalDateTime.now());

        moduleRepository.save(module);

        // Update project lastActivityAt
        project.setLastActivityAt(LocalDateTime.now());
        project.setUpdatedAt(LocalDateTime.now());
        projectRepository.save(project);

        projectService.logActivity(project.getId(), moduleId, user.getUserId(), user.getName(),
                "MODULE_UPDATED", null, null, "Module details updated");

        return projectService.toModuleResponse(module);
    }

    /**
     * CRITICAL BUSINESS RULE IMPLEMENTATION:
     * 1. Employee CANNOT complete or update another member's module.
     * 2. The member who updates status has their name, date and time recorded and visible to all.
     * 3. Project last_activity_at is updated to prevent inactivity auto-hold.
     */
    public ModuleResponse updateModuleStatus(String moduleId, UpdateModuleStatusRequest request, UserPrincipal user) {
        ProjectModule module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new ApiException(ErrorCode.MODULE_NOT_FOUND, "Module not found.", HttpStatus.NOT_FOUND));

        Project project = projectRepository.findById(module.getProjectId())
                .orElseThrow(() -> new ApiException(ErrorCode.PROJECT_NOT_FOUND, "Project not found.", HttpStatus.NOT_FOUND));

        // Check Employee restriction:
        // "Employee doosre member ka module complete nahi kar sakta."
        if (user.getRole() == Role.employee && !user.isAdmin()) {
            if (module.getAssignedToUserId() == null || !module.getAssignedToUserId().equals(user.getUserId())) {
                log.warn("Blocked employee {} from updating module {} assigned to {}",
                        user.getUserId(), moduleId, module.getAssignedToUserId());
                throw new ApiException(ErrorCode.UNAUTHORIZED_MODULE_UPDATE,
                        "Permission Denied: Employees can only update their own assigned modules. You cannot complete or update another member's module.",
                        HttpStatus.FORBIDDEN);
            }
        }

        // Validate access to project
        if (!projectService.canUserAccessProject(project, user)) {
            throw new ApiException(ErrorCode.FORBIDDEN, "Access denied: you are not assigned to this project.", HttpStatus.FORBIDDEN);
        }

        String oldStatus = module.getStatus().name();
        ModuleStatus newStatus = ModuleStatus.fromString(request.getStatus());

        module.setStatus(newStatus);
        if (request.getProgressPercentage() != null) {
            module.setProgressPercentage(request.getProgressPercentage());
        } else if (newStatus == ModuleStatus.COMPLETED) {
            module.setProgressPercentage(100);
        }

        // AUDIT TRAIL: Save updating member's name and timestamp
        LocalDateTime updateTime = LocalDateTime.now();
        module.setLastUpdatedByUserId(user.getUserId());
        module.setLastUpdatedByName(user.getName());
        module.setLastUpdatedAt(updateTime);
        module.setUpdatedAt(updateTime);

        moduleRepository.save(module);

        // INACTIVITY RULE: Reset 3-day inactivity timer on parent project
        project.setLastActivityAt(updateTime);
        project.setUpdatedAt(updateTime);
        
        // If project was ON_HOLD due to inactivity and work resumed, reactivate it to IN_PROGRESS
        if (project.getStatus() == ProjectStatus.ON_HOLD) {
            project.setStatus(ProjectStatus.IN_PROGRESS);
            projectService.logActivity(project.getId(), moduleId, user.getUserId(), user.getName(),
                    "PROJECT_RESUMED", ProjectStatus.ON_HOLD.name(), ProjectStatus.IN_PROGRESS.name(),
                    "Project resumed to IN_PROGRESS as module activity was detected.");
        }
        projectRepository.save(project);

        String detailText = "Status changed from " + oldStatus + " to " + newStatus.name()
                + " (Progress: " + module.getProgressPercentage() + "%) by " + user.getName();
        if (request.getComment() != null && !request.getComment().isBlank()) {
            detailText += ". Comment: " + request.getComment().trim();
        }

        projectService.logActivity(project.getId(), moduleId, user.getUserId(), user.getName(),
                "MODULE_STATUS_UPDATED", oldStatus, newStatus.name(), detailText);

        log.info("Module {} status updated to {} by user {} ({})", moduleId, newStatus, user.getName(), user.getUserId());
        return projectService.toModuleResponse(module);
    }

    public void deleteModule(String moduleId, UserPrincipal user) {
        ProjectModule module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new ApiException(ErrorCode.MODULE_NOT_FOUND, "Module not found.", HttpStatus.NOT_FOUND));

        Project project = projectRepository.findById(module.getProjectId())
                .orElseThrow(() -> new ApiException(ErrorCode.PROJECT_NOT_FOUND, "Project not found.", HttpStatus.NOT_FOUND));

        if (!projectService.canUserManageProject(project, user)) {
            throw new ApiException(ErrorCode.FORBIDDEN, "Only Admins and Project Managers can delete modules.", HttpStatus.FORBIDDEN);
        }

        moduleRepository.deleteById(moduleId);
        projectService.logActivity(project.getId(), moduleId, user.getUserId(), user.getName(),
                "MODULE_DELETED", module.getStatus().name(), null, "Module '" + module.getTitle() + "' deleted");

        log.info("Module {} deleted by user {}", moduleId, user.getUserId());
    }
}
