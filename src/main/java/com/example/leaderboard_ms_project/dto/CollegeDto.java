package com.example.leaderboard_ms_project.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CollegeDto extends BaseResponse {
    private UUID id;
    private String name;
    private String location;
    private String about;
    private String email;
    private String roleName;
    private int score;

    public CollegeDto(UUID id, String name, String email, String location, String about, String roleName, int score) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.location = location;
        this.about = about;
        this.roleName = roleName;
        this.score =score;
    }


    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }
}
