package com.example.leaderboard_ms_project.service;

import com.example.leaderboard_ms_project.dto.ApiResponse;
import com.example.leaderboard_ms_project.dto.ProjectDto;
import com.example.leaderboard_ms_project.dto.ProjectResponseDto;
import com.example.leaderboard_ms_project.dto.UserDto;
import com.example.leaderboard_ms_project.entity.Project;
import com.example.leaderboard_ms_project.exception.ResourceNotFoundException;
import com.example.leaderboard_ms_project.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${user.service.url}")
    private String userServiceUrl;

    @Value("${college.service.url}")
    private String collegeServiceUrl;

    public List<ProjectResponseDto> getAllProjects() {
        String loggedInUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        String token = getTokenFromContext();
        List<Project> projects;

        // Get user information from user service
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        try {
            String userUrl = userServiceUrl + "/api/users/email/" + loggedInUserEmail;
            Map userMap = restTemplate.getForObject(userUrl, Map.class);

            if (userMap == null || userMap.get("id") == null || userMap.get("role_id") == null) {
                throw new ResourceNotFoundException("No user exists");
            }

            UUID userId = UUID.fromString(userMap.get("id").toString());
            UUID roleId = UUID.fromString(userMap.get("role_id").toString());

            String roleUrl = userServiceUrl + "/api/roles/" + roleId;
            Map roleMap = restTemplate.getForObject(roleUrl, Map.class);

            if (roleMap == null || roleMap.get("name") == null) {
                throw new ResourceNotFoundException("Role not found for user");
            }

            String roleName = roleMap.get("name").toString().toUpperCase();



            if (roleName.equals("ADMIN")) {
                projects = projectRepository.findByIsDeletedFalse();
            } else if (roleName.equals("MENTOR")) {
                projects = projectRepository.findByMentorAndIsDeletedFalse(userId);
            } else if (roleName.equals("COLLEGE")) {
                projects = projectRepository.findByCollegeAndIsDeletedFalse(userId);
            } else if (roleName.equals("STUDENT")) {
                projects = projectRepository.findByStudentAndIsDeletedFalse(userId);
            } else {
                throw new IllegalArgumentException("Unauthorized access");
            }


        } catch (Exception e) {
            // Log the exception for debugging
            e.printStackTrace();
            throw new ResourceNotFoundException("Error fetching user details: " + e.getMessage());
        }

        return projects.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public boolean existsById(UUID id) {
        return projectRepository.existsByIdAndIsDeletedFalse(id);
    }

    private String getTokenFromContext() {
        // Get the JWT token from the security context
        return SecurityContextHolder.getContext().getAuthentication().getCredentials().toString();
    }

    public Optional<ProjectResponseDto> getProjectById(UUID projectId) {
        return projectRepository.findByIdAndIsDeletedFalse(projectId)
                .map(this::mapToDto);
    }
    public ProjectResponseDto createProject(ProjectDto projectDto) {
        String token = getTokenFromContext();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);

        // Validate mentor exists
        if (projectDto.getMentorId() == null) {
            throw new IllegalArgumentException("Mentor ID is required");
        }

        // Check if mentor exists via user service
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
//        try {
//            String mentorUrl = userServiceUrl + "/api/users/" + projectDto.getMentorId();
//            Map<String, Object> mentorMap = restTemplate.exchange(
//                    mentorUrl,
//                    HttpMethod.GET,
//                    requestEntity,
//                    new ParameterizedTypeReference<Map<String, Object>>() {}
//            ).getBody();
//
//            if (mentorMap == null || mentorMap.get("id") == null) {
//                throw new ResourceNotFoundException("Mentor not found");
//            }
//        } catch (Exception e) {
//            throw new ResourceNotFoundException("Error verifying mentor: " + e.getMessage());
//        }
        try {
            String mentorUrl = userServiceUrl + "/api/users/id/" + projectDto.getMentorId();
            System.out.println("Mentor URL: " + mentorUrl);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    mentorUrl,
                    HttpMethod.GET,
                    requestEntity,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null || response.getBody().get("id") == null) {
                throw new ResourceNotFoundException("Mentor not found with ID: " + projectDto.getMentorId());
            }
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new ResourceNotFoundException("Error verifying mentor: " + e.getMessage());
        }


        // Validate college if provided
        if (projectDto.getCollegeId() != null) {
            try {
                String collegeUrl = userServiceUrl + "/api/colleges/" + projectDto.getCollegeId();
                Map<String, Object> collegeMap = restTemplate.exchange(
                        collegeUrl,
                        HttpMethod.GET,
                        requestEntity,
                        new ParameterizedTypeReference<Map<String, Object>>() {}
                ).getBody();

                if (collegeMap == null || collegeMap.get("response") == null) {
                    throw new ResourceNotFoundException("College not found");
                }
            } catch (Exception e) {
                throw new ResourceNotFoundException("Error verifying college: " + e.getMessage());
            }
        }

        Project project = new Project();
        project.setName(projectDto.getName());
        project.setDescription(projectDto.getDescription());
        project.setMentor(projectDto.getMentorId());
        project.setCollege(projectDto.getCollegeId());
        project.setCreatedAt(LocalDateTime.now());

        Project savedProject = projectRepository.save(project);
        return mapToDto(savedProject);
    }

    public ProjectResponseDto updateProject(UUID projectId, ProjectDto projectDto) {
        Project project = projectRepository.findByIdAndIsDeletedFalse(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        String token = getTokenFromContext();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

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
            // Verify mentor exists via user service
            try {
                String mentorUrl = userServiceUrl + "/api/users/" + projectDto.getMentorId();
                Map<String, Object> mentorMap = restTemplate.exchange(
                        mentorUrl,
                        HttpMethod.GET,
                        requestEntity,
                        new ParameterizedTypeReference<Map<String, Object>>() {}
                ).getBody();

                if (mentorMap == null || mentorMap.get("id") == null) {
                    throw new ResourceNotFoundException("Mentor not found");
                }

                project.setMentor(projectDto.getMentorId());
            } catch (Exception e) {
                throw new ResourceNotFoundException("Error verifying mentor: " + e.getMessage());
            }
        }

        if (projectDto.getCollegeId() != null) {
            // Verify college exists via user service
            try {
                String collegeUrl = userServiceUrl + "/api/colleges/" + projectDto.getCollegeId();
                Map<String, Object> collegeMap = restTemplate.exchange(
                        collegeUrl,
                        HttpMethod.GET,
                        requestEntity,
                        new ParameterizedTypeReference<Map<String, Object>>() {}
                ).getBody();

                if (collegeMap == null || collegeMap.get("id") == null) {
                    throw new ResourceNotFoundException("College not found");
                }

                project.setCollege(projectDto.getCollegeId());
            } catch (Exception e) {
                throw new ResourceNotFoundException("Error verifying college: " + e.getMessage());
            }
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
        project.setUpdatedAt(LocalDateTime.now());
        projectRepository.save(project);
        return true;
    }

    private ProjectResponseDto mapToDto(Project project) {
        ProjectResponseDto dto = new ProjectResponseDto();
        dto.setId(project.getId());
        dto.setName(project.getName());
        dto.setDescription(project.getDescription());
        dto.setScore(project.getScore());
        dto.setCollegeId(project.getCollege());
        dto.setMentorId(project.getMentor());
        dto.setCreatedAt(project.getCreatedAt());
        return dto;
    }
}