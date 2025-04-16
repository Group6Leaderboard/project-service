package com.example.leaderboard_ms_project.service;

import com.example.leaderboard_ms_project.dto.ProjectDto;
import com.example.leaderboard_ms_project.dto.ProjectResponseDto;
import com.example.leaderboard_ms_project.entity.College;
import com.example.leaderboard_ms_project.entity.Project;
import com.example.leaderboard_ms_project.entity.StudentProject;
import com.example.leaderboard_ms_project.entity.User;
import com.example.leaderboard_ms_project.exception.ResourceNotFoundException;
import com.example.leaderboard_ms_project.repository.CollegeRepository;
import com.example.leaderboard_ms_project.repository.ProjectRepository;
import com.example.leaderboard_ms_project.repository.StudentProjectRepository;
import com.example.leaderboard_ms_project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CollegeRepository collegeRepository;
    @Autowired
    private StudentProjectRepository studentProjectRepository;

    public List<ProjectResponseDto> getAllProjects() {
        String loggedInUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        List<Project> projects;

        if (hasRole("ADMIN")) {
            projects = projectRepository.findByIsDeletedFalse();

        } else if (hasRole("MENTOR")) {
            UUID mentorId = userRepository.findByEmailAndIsDeletedFalse(loggedInUserEmail).orElseThrow(() -> new ResourceNotFoundException("Mentor not found")).getId();
            projects = projectRepository.findByMentorIdAndIsDeletedFalse(mentorId);

        } else if (hasRole("COLLEGE")) {
            UUID collegeId = collegeRepository.findByEmailAndIsDeletedFalse(loggedInUserEmail)
                    .orElseThrow(() -> new ResourceNotFoundException("College not found"))
                    .getId();
            projects = projectRepository.findByCollegeIdAndIsDeletedFalse(collegeId);

        } else if (hasRole("STUDENT")) {
            UUID studentId = userRepository.findByEmailAndIsDeletedFalse(loggedInUserEmail)
                    .orElseThrow(() -> new ResourceNotFoundException("Student not found"))
                    .getId();

            projects = studentProjectRepository.findByStudentIdAndIsDeletedFalse(studentId)
                    .stream()
                    .map(StudentProject::getProject)
                    .collect(Collectors.toList());

        } else {
            throw new IllegalArgumentException("Unauthorized access");
        }

        return projects.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private boolean hasRole(String role) {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_" + role));
    }

    public Optional<ProjectResponseDto> getProjectById(UUID projectId) {
        return projectRepository.findByIdAndIsDeletedFalse(projectId)
                .map(this::mapToDto);
    }

    public ProjectResponseDto createProject(ProjectDto projectDto) {
        if (projectDto.getMentorId() == null) {
            throw new IllegalArgumentException("Mentor ID is required");
        }

        Project project = new Project();
        project.setName(projectDto.getName());
        project.setDescription(projectDto.getDescription());

        User mentor = userRepository.findByIdAndIsDeletedFalse(projectDto.getMentorId())
                .orElseThrow(() -> new ResourceNotFoundException("Mentor not found"));
        project.setMentor(mentor);

        if (projectDto.getCollegeId() != null) {
            College college = collegeRepository.findByIdAndIsDeletedFalse(projectDto.getCollegeId())
                    .orElseThrow(() -> new ResourceNotFoundException("College not found"));
            project.setCollege(college);
        }

        project.setCreatedAt(LocalDateTime.now());
        Project savedProject = projectRepository.save(project);

        return mapToDto(savedProject);
    }

    public ProjectResponseDto updateProject(UUID projectId, ProjectDto projectDto) {
        Project project = projectRepository.findByIdAndIsDeletedFalse(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        if (projectDto.getName() != null) {
            project.setName(projectDto.getName());
        }
        if (projectDto.getDescription() != null) {
            project.setDescription(projectDto.getDescription());
        }
        if (projectDto.getScore() != null) {
            project.setScore(projectDto.getScore());
        }

        if (projectDto.getMentorId() != null) {
            User mentor = userRepository.findByIdAndIsDeletedFalse(projectDto.getMentorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Mentor not found"));
            project.setMentor(mentor);
        }

        if (projectDto.getCollegeId() != null) {
            College college = collegeRepository.findByIdAndIsDeletedFalse(projectDto.getCollegeId())
                    .orElseThrow(() -> new ResourceNotFoundException("College not found"));
            project.setCollege(college);
        }

        project.setUpdatedAt(LocalDateTime.now());

        Project updatedProject = projectRepository.save(project);
        return mapToDto(updatedProject);
    }

    public boolean deleteProject(UUID projectId) {
        Project project = projectRepository.findByIdAndIsDeletedFalse(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        if (project.isDeleted()) {
            return false;
        }

        project.setDeleted(true);
        projectRepository.save(project);
        return true;
    }

    private ProjectResponseDto mapToDto(Project project) {
        ProjectResponseDto dto = new ProjectResponseDto();
        dto.setId(project.getId());
        dto.setName(project.getName());
        dto.setDescription(project.getDescription());
        dto.setScore(project.getScore());
        dto.setCollegeId(project.getCollege() != null ? project.getCollege().getId() : null);
        dto.setMentorId(project.getMentor() != null ? project.getMentor().getId() : null);
        dto.setCreatedAt(project.getCreatedAt());
        return dto;
    }
}
