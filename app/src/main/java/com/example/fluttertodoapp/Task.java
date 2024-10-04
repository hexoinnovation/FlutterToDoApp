package com.example.fluttertodoapp; // Ensure this is correct

public class Task {
    private String id; // Document ID
    private String title;
    private String description;

    // Constructor
    public Task(String id, String title, String description) {
        this.id = id;
        this.title = title;
        this.description = description;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }
}
