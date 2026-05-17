package com.example.NewToDo.dto;

import com.example.NewToDo.entity.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO for creating a new todo
 * Contains only fields that client can set
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TodoCreateRequest {
    
    @NotBlank(message = "Title is required")
    @Size(min = 1, max = 100, message = "Title must be between 1 and 100 characters")
    private String title;
    
    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;
    
    /**
     * Priority level (LOW, MEDIUM, HIGH)
     * Optional - defaults to MEDIUM if not provided
     */
    private Priority priority;
    
    /**
     * Due date for the todo
     * Optional - can be null
     */
    private LocalDate dueDate;
    
    /**
     * Comma-separated tags
     * Example: "urgent, work, important"
     */
    @Size(max = 200, message = "Tags cannot exceed 200 characters")
    private String tags;
    
    /**
     * Category ID to assign this todo to
     * Optional - todo can exist without category
     */
    private Long categoryId;
    
    // No id, createdAt, updatedAt, or completed fields
    // These are managed by the system
}

// Made with Bob
