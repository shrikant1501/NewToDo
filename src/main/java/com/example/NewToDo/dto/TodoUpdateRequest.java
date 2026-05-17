package com.example.NewToDo.dto;

import com.example.NewToDo.entity.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO for updating an existing todo
 * Contains all fields that can be modified
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TodoUpdateRequest {
    
    @NotBlank(message = "Title is required")
    @Size(min = 1, max = 100, message = "Title must be between 1 and 100 characters")
    private String title;
    
    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;
    
    @NotNull(message = "Completed status is required")
    private Boolean completed;
    
    /**
     * Priority level (LOW, MEDIUM, HIGH)
     * Required for update
     */
    @NotNull(message = "Priority is required")
    private Priority priority;
    
    /**
     * Due date for the todo
     * Can be null to remove due date
     */
    private LocalDate dueDate;
    
    /**
     * Comma-separated tags
     * Can be null or empty to remove tags
     */
    @Size(max = 200, message = "Tags cannot exceed 200 characters")
    private String tags;
    
    /**
     * Category ID to assign this todo to
     * Can be null to remove category
     */
    private Long categoryId;
    
    // No id, createdAt, or updatedAt fields
    // These are managed by the system
}

// Made with Bob
