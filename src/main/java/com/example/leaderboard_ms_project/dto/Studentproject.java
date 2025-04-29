package com.example.leaderboard_ms_project.dto;

import lombok.AllArgsConstructor;

import lombok.NoArgsConstructor;

import java.util.UUID;


@NoArgsConstructor
@AllArgsConstructor
public class Studentproject {
    private UUID id;
    private UUID student;
    private UUID project;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getStudent() {
        return student;
    }

    public void setStudentId(UUID student) { // Should be setStudent
        this.student = student;
    }

    public UUID getProjectId() {
        return project;
    }

    public void setProjectId(UUID project) {
        this.project = project;
    }

}

