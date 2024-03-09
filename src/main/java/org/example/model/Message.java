package org.example.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Message {
    private String role;
    private String content;

    // Default constructor
    public Message() {
    }

    @JsonCreator
    public Message(@JsonProperty("role") String role, @JsonProperty("content") String content) {
        this.role = role;
        this.content = content;
    }

    // Getters and setters
    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

}
