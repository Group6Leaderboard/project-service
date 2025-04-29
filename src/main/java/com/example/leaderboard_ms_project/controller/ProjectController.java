package com.example.leaderboard_ms_project.controller;

import com.example.leaderboard_ms_project.dto.ApiResponse;
import com.example.leaderboard_ms_project.dto.ProjectDto;
import com.example.leaderboard_ms_project.dto.ProjectResponseDto;
import com.example.leaderboard_ms_project.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @PreAuthorize("hasRole('ADMIN') or hasRole('MENTOR') or hasRole('COLLEGE') or hasRole('STUDENT')")
    @GetMapping
    @Transactional
    public ResponseEntity<ApiResponse<List<ProjectResponseDto>>> getAllProjects() {
        List<ProjectResponseDto> projects = projectService.getAllProjects();
        return ApiResponse.success(projects, "Projects fetched successfully");
    }


    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProjectResponseDto>> createProject(@RequestBody ProjectDto projectDto) {
        System.out.println("In func");
        ProjectResponseDto savedProject = projectService.createProject(projectDto);
        return ApiResponse.created(savedProject, "Project created successfully");
    }


    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MENTOR')")
    public ResponseEntity<ApiResponse<ProjectResponseDto>> updateProject(
            @PathVariable UUID id,
            @RequestBody ProjectDto projectDto) {
        ProjectResponseDto updatedProject = projectService.updateProject(id, projectDto);
        return ApiResponse.success(updatedProject, "Project updated successfully");
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectResponseDto>> getProjectById(@PathVariable UUID id) {
        System.out.println("In func");
        ProjectResponseDto project = projectService.getProjectById(id)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));
        return ApiResponse.success(project, "Project fetched successfully");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteProject(@PathVariable UUID id) {
        boolean deleted = projectService.deleteProject(id);
        if (deleted) {
            return ApiResponse.success(null, "Project deleted successfully");
        } else {
            return ApiResponse.notFound("Project not found");
        }
    }

    // Internal API for other microservices to use
    @GetMapping("/internal/exists/{id}")
    public ResponseEntity<Boolean> checkProjectExists(@PathVariable UUID id) {
        return ResponseEntity.ok(projectService.existsById(id));
    }
}
