package com.example.leaderboard_ms_project.entity;


import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
public class StudentProject {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;




    @ManyToOne
    @JoinColumn(name = "student_id",referencedColumnName = "id", nullable = false)
    private User student; // Assuming User entity represents students

    @ManyToOne
    @JoinColumn(name = "project_id",referencedColumnName = "id", nullable = false)
    private Project project;



    private boolean isDeleted = false;

    private LocalDateTime createdAt=LocalDateTime.now();
    private LocalDateTime updatedAt;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public User getStudent() {
        return student;
    }

    public void setStudent(User student) {
        this.student = student;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
