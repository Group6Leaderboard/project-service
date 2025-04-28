package com.example.leaderboard_ms_project.controller;

import com.example.leaderboard_ms_project.dto.ApiResponse;
import com.example.leaderboard_ms_project.dto.ProjectDto;
import com.example.leaderboard_ms_project.dto.Studentproject;
import com.example.leaderboard_ms_project.dto.UserDto;
import com.example.leaderboard_ms_project.service.StudentProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/student-projects")
public class StudentProjectController {

    private final StudentProjectService studentProjectService;

    @Autowired
    public StudentProjectController(StudentProjectService studentProjectService) {
        this.studentProjectService = studentProjectService;
    }

    @PostMapping("/students/{studentId}/projects/{projectId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COLLEGE')")
    public ResponseEntity<ApiResponse<Studentproject>> assignProjectToStudent(
            @PathVariable UUID studentId,
            @PathVariable UUID projectId) {

        ApiResponse<Studentproject> response = studentProjectService.assignProjectToStudent(studentId, projectId);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MENTOR') or hasRole('COLLEGE') or hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<?>> getStudentProjects(
            @RequestParam(value = "studentId", required = false) UUID studentId,
            @RequestParam(value = "projectId", required = false) UUID projectId) {

        if (studentId != null) {
            ApiResponse<List<Studentproject>> response = studentProjectService.getProjectsForStudent(studentId);
            return ResponseEntity.status(response.getStatus()).body(response);
        } else if (projectId != null) {
            ApiResponse<List<Map<String, Object>>> response = studentProjectService.getUsersForProject(projectId);
            return ResponseEntity.status(response.getStatus()).body(response);
        } else {
            ApiResponse<List<Studentproject>> response = studentProjectService.getAllStudentProjects();
            return ResponseEntity.status(response.getStatus()).body(response);
        }
    }

    @GetMapping("/members")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MENTOR') or hasRole('COLLEGE')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getMembersForProject(
            @RequestParam UUID projectId) {

        ApiResponse<List<Map<String, Object>>> response = studentProjectService.getUsersForProject(projectId);
        return ResponseEntity.status(response.getStatus()).body(response);
    }




    @GetMapping("/projects")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MENTOR') or hasRole('COLLEGE') or hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<List<ProjectDto>>> getProjectsForStudent(
            @RequestParam UUID studentId) {

        ApiResponse<List<ProjectDto>> response = studentProjectService.getProjectsForStudents(studentId);
        return ResponseEntity.status(response.getStatus()).body(response);
    }


    @DeleteMapping("/{studentProjectId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COLLEGE')")
    public ResponseEntity<ApiResponse<String>> deleteStudentProject(@PathVariable UUID studentProjectId) {
        ApiResponse<String> response = studentProjectService.deleteStudentProject(studentProjectId);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    // Internal API endpoints that other services might need
    @GetMapping("/internal/student/{studentId}/projects")
    public ResponseEntity<List<UUID>> getStudentProjectIds(@PathVariable UUID studentId) {
        List<UUID> projectIds = studentProjectService.getProjectIdsForStudent(studentId);
        return ResponseEntity.ok(projectIds);
    }
}