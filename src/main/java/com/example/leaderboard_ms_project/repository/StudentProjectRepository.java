package com.example.leaderboard_ms_project.repository;

import com.example.leaderboard_ms_project.entity.Project;
import com.example.leaderboard_ms_project.entity.StudentProject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StudentProjectRepository extends JpaRepository<StudentProject, UUID> {
    Optional<StudentProject> findByStudentAndProjectAndIsDeletedFalse(UUID student, UUID project);

    // Changed from projectId to project
    List<StudentProject> findByProjectAndIsDeletedFalse(UUID project);

    Optional<StudentProject> findByIdAndIsDeletedFalse(UUID id);
    List<StudentProject> findByStudentAndIsDeletedFalse(UUID studentId);

//    Optional<StudentProject> findByStudentAndProjectAndIsDeletedFalse(UUID student, Project project);


    // Changed from studentId and projectId to student and project
    boolean existsByStudentAndProjectAndIsDeletedFalse(UUID student, UUID project);

    List<StudentProject> findByIsDeletedFalse();




    @Query("SELECT sp.project FROM StudentProject sp WHERE sp.student = :student")
    List<UUID> findProjectIdsByStudentId(@Param("student") UUID student);

    @Query("SELECT sp.project FROM StudentProject sp WHERE sp.student = :student")
    List<Project> findProjectsByStudentId(@Param("student") UUID student);


}