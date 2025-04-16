package com.example.leaderboard_ms_project.repository;


import com.example.leaderboard_ms_project.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {
    List<Project> findByIsDeletedFalse();
    List<Project> findByMentorId(UUID mentorId);
    Optional<Project> findByIdAndIsDeletedFalse(UUID id);

    List<Project> findByMentorIdAndIsDeletedFalse(UUID mentorId);

    List<Project> findByCollegeIdAndIsDeletedFalse(UUID collegeId);

    boolean existsByIdAndCollegeIdAndIsDeletedFalse(UUID projectId, UUID colllegeId);

    @Query("SELECT COALESCE(SUM(p.score), 0) FROM Project p WHERE p.college.id = :collegeId AND p.isDeleted = false")
    int sumScoresByCollegeId(@Param("collegeId") UUID collegeId);

    List<Project> findByScoreGreaterThanOrderByScoreDesc(int score);


    @Query("SELECT COUNT(p) FROM Project p WHERE p.college.id = :collegeId AND p.isDeleted = false")
    int countByCollegeId(@Param("collegeId") UUID collegeId);

    @Query("SELECT p FROM Project p WHERE p.college.id = :collegeId")
    List<Project> findByCollegeId(@Param("collegeId") UUID collegeId);


}
