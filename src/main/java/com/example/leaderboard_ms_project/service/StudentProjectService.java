package com.example.leaderboard_ms_project.service;

import com.example.leaderboard_ms_project.dto.ApiResponse;
import com.example.leaderboard_ms_project.dto.ProjectDto;
import com.example.leaderboard_ms_project.dto.Studentproject;
import com.example.leaderboard_ms_project.dto.UserDto;
import com.example.leaderboard_ms_project.entity.Project;
import com.example.leaderboard_ms_project.entity.StudentProject;
import com.example.leaderboard_ms_project.exception.ResourceNotFoundException;
import com.example.leaderboard_ms_project.repository.ProjectRepository;
import com.example.leaderboard_ms_project.repository.StudentProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StudentProjectService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private StudentProjectRepository studentProjectRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Value("${user.service.url}")
    private String userServiceUrl;


    public ApiResponse<Studentproject> assignProjectToStudent(UUID studentId, UUID projectId) {
        String token = getTokenFromContext();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        try {
            // Fetch the student details from user service
            String studentUrl = userServiceUrl + "/api/users/id/" + studentId;
            Map<String, Object> userMap = restTemplate.exchange(
                    studentUrl,
                    HttpMethod.GET,
                    requestEntity,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            ).getBody();

            if (userMap == null || userMap.get("id") == null || userMap.get("college_id") == null) {
                throw new ResourceNotFoundException("Student not found with ID: " + studentId);
            }

            // Extract the student's college ID
            UUID studentCollegeId = UUID.fromString(userMap.get("college_id").toString());

            // Fetch the project from the repository
            Project project = projectRepository.findByIdAndIsDeletedFalse(projectId)
                    .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + projectId));

            // Verify student and project belong to the same college
            if (project.getCollege() != null && studentCollegeId != null &&
                    !project.getCollege().equals(studentCollegeId)) {
                throw new IllegalArgumentException("Student and Project belong to different colleges!");
            }

            // Check if the student is already assigned to this project
            Optional<StudentProject> existingAssignment = studentProjectRepository
                    .findByStudentAndProjectAndIsDeletedFalse(studentId, projectId);

            if (existingAssignment.isPresent()) {
                throw new IllegalArgumentException("This student is already assigned to this project");
            }

            // Create new assignment
            StudentProject studentProject = new StudentProject();
            studentProject.setStudent(studentId);
            studentProject.setProject(projectId);
            studentProject.setCollege(studentCollegeId);
            studentProject.setCreatedAt(LocalDateTime.now());

            // Save the entity to the repository
            StudentProject savedEntity = studentProjectRepository.save(studentProject);

            // Prepare DTO to return
            Studentproject dto = new Studentproject(
                    savedEntity.getId(),
                    savedEntity.getStudent(),
                    savedEntity.getProject()
            );

            // Return success response
            return new ApiResponse<>(200, "Success", dto);
        } catch (Exception e) {
            throw new ResourceNotFoundException("Error verifying student or project: " + e.getMessage());
        }
    }
    public ApiResponse<List<Studentproject>> getProjectsForStudent(UUID studentId) {
        // Verify student exists via user service
        String token = getTokenFromContext();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        try {
            String studentUrl = userServiceUrl + "/api/users/" + studentId;
            Map<String, Object> userMap = restTemplate.exchange(
                    studentUrl,
                    HttpMethod.GET,
                    requestEntity,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            ).getBody();

            if (userMap == null || userMap.get("id") == null) {
                throw new ResourceNotFoundException("Student not found with ID: " + studentId);
            }

            List<StudentProject> studentProjects = studentProjectRepository.findByStudentAndIsDeletedFalse(studentId);

            List<Studentproject> dtoList = studentProjects.stream()
                    .map(sp -> new Studentproject(
                            sp.getId(),
                            sp.getStudent(),
                            sp.getProject()
                    ))
                    .collect(Collectors.toList());

            return new ApiResponse<>(200, "Success", dtoList);
        } catch (Exception e) {
            throw new ResourceNotFoundException("Error fetching projects for student: " + e.getMessage());
        }
    }

    @Transactional
    public ApiResponse<List<Map<String, Object>>> getUsersForProject(UUID projectId) {
        // Verify project exists
        if (!projectRepository.existsByIdAndIsDeletedFalse(projectId)) {
            throw new ResourceNotFoundException("Project not found with ID: " + projectId);
        }

        List<StudentProject> studentProjects = studentProjectRepository.findByProjectAndIsDeletedFalse(projectId);

        List<UUID> studentIds = studentProjects.stream()
                .map(StudentProject::getStudent)
                .collect(Collectors.toList());

        if (studentIds.isEmpty()) {
            return new ApiResponse<>(200, "No students assigned to this project", List.of());
        }

        // Fetch user details for each student from user service
        String token = getTokenFromContext();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<List<UUID>> requestEntity = new HttpEntity<>(studentIds, headers);

        try {
            ResponseEntity<Map<String, Object>> usersResponse = restTemplate.exchange(
                    userServiceUrl + "/api/users/batch",
                    HttpMethod.POST,
                    requestEntity,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            if (!usersResponse.getStatusCode().is2xxSuccessful() || usersResponse.getBody() == null) {
                throw new ResourceNotFoundException("Failed to fetch student details");
            }

            // Extract list of users from the response
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> usersList = (List<Map<String, Object>>) usersResponse.getBody().get("response");

            return new ApiResponse<>(200, "Success", usersList);
        } catch (Exception e) {
            throw new ResourceNotFoundException("Error fetching user details: " + e.getMessage());
        }
    }

    @Transactional
    public ApiResponse<List<ProjectDto>> getProjectsForStudents(UUID student) {
        // Verify student exists via user service
        String token = getTokenFromContext();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        try {
            String studentUrl = userServiceUrl + "/api/users/" + student;
            Map<String, Object> userMap = restTemplate.exchange(
                    studentUrl,
                    HttpMethod.GET,
                    requestEntity,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            ).getBody();

            if (userMap == null || userMap.get("id") == null) {
                throw new ResourceNotFoundException("Student not found with ID: " + student);
            }

            List<StudentProject> studentProjects = studentProjectRepository.findByStudentAndIsDeletedFalse(student);

            List<ProjectDto> projectDtos = studentProjects.stream()
                    .map(sp -> {
                        try {
                            // Fetch the Project by its UUID
                            Project project = projectRepository.findById(sp.getProject())
                                    .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + sp.getProject()));

                            ProjectDto projectDto = new ProjectDto();
                            projectDto.setId(project.getId());
                            projectDto.setName(project.getName());
                            projectDto.setDescription(project.getDescription());
                            projectDto.setScore(project.getScore());
                            projectDto.setMentorId(project.getMentor());
                            projectDto.setCollegeId(project.getCollege());
                            projectDto.setCreatedAt(project.getCreatedAt());
                            return projectDto;
                        } catch (Exception e) {
                            throw new ResourceNotFoundException("Error fetching project details: " + e.getMessage());
                        }
                    })
                    .collect(Collectors.toList());

            return new ApiResponse<>(200, "Projects fetched for student", projectDtos);
        } catch (Exception e) {
            throw new ResourceNotFoundException("Error fetching projects for student: " + e.getMessage());
        }
    }






    public ApiResponse<String> deleteStudentProject(UUID studentProjectId) {
        StudentProject studentProject = studentProjectRepository.findByIdAndIsDeletedFalse(studentProjectId)
                .orElseThrow(() -> new ResourceNotFoundException("StudentProject not found with ID: " + studentProjectId));

        studentProject.setDeleted(true);
        studentProject.setUpdatedAt(LocalDateTime.now());
        studentProjectRepository.save(studentProject);

        return new ApiResponse<>(200, "Project assignment deleted successfully.", "Deleted");
    }

    public ApiResponse<List<Studentproject>> getAllStudentProjects() {
        List<StudentProject> studentProjects = studentProjectRepository.findByIsDeletedFalse();

        List<Studentproject> dtoList = studentProjects.stream()
                .map(sp -> new Studentproject(
                        sp.getId(),
                        sp.getStudent(),
                        sp.getProject()
                ))
                .collect(Collectors.toList());

        return new ApiResponse<>(200, "Success", dtoList);
    }



    public List<UUID> getProjectIdsForStudent(UUID studentId) {
        List<StudentProject> studentProjects = studentProjectRepository.findByStudentAndIsDeletedFalse(studentId);
        return studentProjects.stream()
                .map(StudentProject::getProject)
                .collect(Collectors.toList());
    }


    private String getTokenFromContext() {
        // Get the JWT token from the security context
        return SecurityContextHolder.getContext().getAuthentication().getCredentials().toString();
    }


}