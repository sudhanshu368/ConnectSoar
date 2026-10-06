package com.connectsoar.backend.controller;

import com.connectsoar.backend.dto.*;
import com.connectsoar.backend.enums.Role;
import com.connectsoar.backend.security.RequireRole;
import com.connectsoar.backend.security.UserPrincipal;
import com.connectsoar.backend.service.ProjectModuleService;
import com.connectsoar.backend.service.ProjectService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/projects", "/api/projects"})
@RequireRole({Role.admin, Role.manager, Role.employee})
public class ProjectController {

    private static final Logger log = LoggerFactory.getLogger(ProjectController.class);

    private final ProjectService projectService;
    private final ProjectModuleService moduleService;

    public ProjectController(ProjectService projectService, ProjectModuleService moduleService) {
        this.projectService = projectService;
        this.moduleService = moduleService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProjectResponse>>> getProjects(
            @RequestAttribute("userPrincipal") UserPrincipal principal) {
        List<ProjectResponse> projects = projectService.getProjectsForUser(principal);
        return ResponseEntity.ok(ApiResponse.ok("Projects fetched successfully.", projects));
    }

    @GetMapping("/stats/summary")
    public ResponseEntity<ApiResponse<ProjectSummaryStatsDto>> getSummaryStats(
            @RequestAttribute("userPrincipal") UserPrincipal principal) {
        ProjectSummaryStatsDto stats = projectService.getSummaryStats(principal);
        return ResponseEntity.ok(ApiResponse.ok("Project statistics fetched successfully.", stats));
    }

    @GetMapping("/modules/my-assigned")
    public ResponseEntity<ApiResponse<List<ModuleResponse>>> getMyAssignedModules(
            @RequestAttribute("userPrincipal") UserPrincipal principal) {
        List<ModuleResponse> modules = moduleService.getMyAssignedModules(principal);
        return ResponseEntity.ok(ApiResponse.ok("Assigned modules fetched successfully.", modules));
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<ApiResponse<ProjectResponse>> getProjectById(
            @PathVariable("projectId") String projectId,
            @RequestAttribute("userPrincipal") UserPrincipal principal) {
        ProjectResponse project = projectService.getProjectById(projectId, principal);
        return ResponseEntity.ok(ApiResponse.ok(project));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProjectResponse>> createProject(
            @Valid @RequestBody CreateProjectRequest request,
            @RequestAttribute("userPrincipal") UserPrincipal principal) {
        ProjectResponse project = projectService.createProject(request, principal);
        return new ResponseEntity<>(ApiResponse.ok("Project created successfully.", project), HttpStatus.CREATED);
    }

    @PatchMapping("/{projectId}")
    public ResponseEntity<ApiResponse<ProjectResponse>> updateProject(
            @PathVariable("projectId") String projectId,
            @RequestBody UpdateProjectRequest request,
            @RequestAttribute("userPrincipal") UserPrincipal principal) {
        ProjectResponse updated = projectService.updateProject(projectId, request, principal);
        return ResponseEntity.ok(ApiResponse.ok("Project updated successfully.", updated));
    }

    @PutMapping("/{projectId}")
    public ResponseEntity<ApiResponse<ProjectResponse>> updateProjectPut(
            @PathVariable("projectId") String projectId,
            @RequestBody UpdateProjectRequest request,
            @RequestAttribute("userPrincipal") UserPrincipal principal) {
        ProjectResponse updated = projectService.updateProject(projectId, request, principal);
        return ResponseEntity.ok(ApiResponse.ok("Project updated successfully.", updated));
    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<ApiResponse<Void>> deleteProject(
            @PathVariable("projectId") String projectId,
            @RequestAttribute("userPrincipal") UserPrincipal principal) {
        projectService.deleteProject(projectId, principal);
        return ResponseEntity.ok(ApiResponse.ok("Project deleted successfully.", null));
    }

    // --- Members Endpoints ---

    @PostMapping("/{projectId}/members")
    public ResponseEntity<ApiResponse<ProjectMemberDto>> addMember(
            @PathVariable("projectId") String projectId,
            @Valid @RequestBody AddProjectMemberRequest request,
            @RequestAttribute("userPrincipal") UserPrincipal principal) {
        ProjectMemberDto member = projectService.addProjectMember(projectId, request, principal);
        return new ResponseEntity<>(ApiResponse.ok("Member added successfully.", member), HttpStatus.CREATED);
    }

    @DeleteMapping("/{projectId}/members/{userId}")
    public ResponseEntity<ApiResponse<Void>> removeMember(
            @PathVariable("projectId") String projectId,
            @PathVariable("userId") String userId,
            @RequestAttribute("userPrincipal") UserPrincipal principal) {
        projectService.removeProjectMember(projectId, userId, principal);
        return ResponseEntity.ok(ApiResponse.ok("Member removed successfully.", null));
    }

    // --- Modules Endpoints ---

    @GetMapping("/{projectId}/modules")
    public ResponseEntity<ApiResponse<List<ModuleResponse>>> getProjectModules(
            @PathVariable("projectId") String projectId,
            @RequestAttribute("userPrincipal") UserPrincipal principal) {
        List<ModuleResponse> modules = moduleService.getModulesByProject(projectId, principal);
        return ResponseEntity.ok(ApiResponse.ok(modules));
    }

    @PostMapping("/{projectId}/modules")
    public ResponseEntity<ApiResponse<ModuleResponse>> createModule(
            @PathVariable("projectId") String projectId,
            @Valid @RequestBody CreateModuleRequest request,
            @RequestAttribute("userPrincipal") UserPrincipal principal) {
        ModuleResponse module = moduleService.createModule(projectId, request, principal);
        return new ResponseEntity<>(ApiResponse.ok("Module created successfully.", module), HttpStatus.CREATED);
    }

    @PatchMapping("/modules/{moduleId}")
    public ResponseEntity<ApiResponse<ModuleResponse>> updateModule(
            @PathVariable("moduleId") String moduleId,
            @RequestBody UpdateModuleRequest request,
            @RequestAttribute("userPrincipal") UserPrincipal principal) {
        ModuleResponse module = moduleService.updateModule(moduleId, request, principal);
        return ResponseEntity.ok(ApiResponse.ok("Module updated successfully.", module));
    }

    /**
     * Update module status/progress.
     * Enforces:
     * - Employee can ONLY update their own assigned module.
     * - Member name and current timestamp recorded in module.
     * - Inactivity timer reset on project.
     */
    @PatchMapping("/modules/{moduleId}/status")
    public ResponseEntity<ApiResponse<ModuleResponse>> updateModuleStatus(
            @PathVariable("moduleId") String moduleId,
            @Valid @RequestBody UpdateModuleStatusRequest request,
            @RequestAttribute("userPrincipal") UserPrincipal principal) {
        ModuleResponse module = moduleService.updateModuleStatus(moduleId, request, principal);
        return ResponseEntity.ok(ApiResponse.ok("Module status updated successfully.", module));
    }

    @DeleteMapping("/modules/{moduleId}")
    public ResponseEntity<ApiResponse<Void>> deleteModule(
            @PathVariable("moduleId") String moduleId,
            @RequestAttribute("userPrincipal") UserPrincipal principal) {
        moduleService.deleteModule(moduleId, principal);
        return ResponseEntity.ok(ApiResponse.ok("Module deleted successfully.", null));
    }

    // --- Activity / Audit Trail ---

    @GetMapping("/{projectId}/activity")
    public ResponseEntity<ApiResponse<List<ProjectActivityLogDto>>> getProjectActivity(
            @PathVariable("projectId") String projectId,
            @RequestAttribute("userPrincipal") UserPrincipal principal) {
        List<ProjectActivityLogDto> logs = projectService.getProjectActivityLogs(projectId, principal);
        return ResponseEntity.ok(ApiResponse.ok(logs));
    }
}
