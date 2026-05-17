package com.example.NewToDo.dto;

import com.example.NewToDo.entity.Priority;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for todo responses
 * Contains all fields that client should see
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TodoResponse {
    
    private Long id;
    private String title;
    private String description;
    private Boolean completed;
    private Priority priority;
    private LocalDate dueDate;
    private String tags;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * Category information (simplified)
     * We don't send the entire Category object to avoid circular references
     */
    private Long categoryId;
    private String categoryName;
    
    /**
     * Computed field - is this todo overdue?
     * Not stored in database, calculated when creating response
     */
    private boolean overdue;
    
    /**
     * Helper constructor without computed fields
     * Computed fields (overdue) will be set separately
     */
    public TodoResponse(Long id, String title, String description, Boolean completed, 
                       Priority priority, LocalDate dueDate, String tags,
                       LocalDateTime createdAt, LocalDateTime updatedAt,
                       Long categoryId, String categoryName) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.completed = completed;
        this.priority = priority;
        this.dueDate = dueDate;
        this.tags = tags;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        
        // Calculate overdue status
        this.overdue = dueDate != null && !completed && dueDate.isBefore(LocalDate.now());
    }
}

// Made with Bob
