package com.example.NewToDo.mapper;

import com.example.NewToDo.dto.TodoCreateRequest;
import com.example.NewToDo.dto.TodoResponse;
import com.example.NewToDo.dto.TodoUpdateRequest;
import com.example.NewToDo.entity.Category;
import com.example.NewToDo.entity.Priority;
import com.example.NewToDo.entity.Todo;
import com.example.NewToDo.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Enhanced TodoMapper with support for:
 * - Priority enum
 * - Category relationship
 * - Due dates and tags
 */
@Component
@RequiredArgsConstructor
public class TodoMapper {
    
    private final CategoryRepository categoryRepository;
    
    /**
     * Convert TodoCreateRequest to Todo entity
     * 
     * WHAT IT DOES:
     * 1. Maps basic fields (title, description)
     * 2. Sets priority (defaults to MEDIUM if not provided)
     * 3. Sets due date and tags
     * 4. Looks up and sets category if categoryId provided
     */
    public Todo toEntity(TodoCreateRequest request) {
        Todo todo = new Todo();
        todo.setTitle(request.getTitle());
        todo.setDescription(request.getDescription());
        todo.setCompleted(false); // Default value for new todos
        
        // Set priority (will default to MEDIUM in @PrePersist if null)
        todo.setPriority(request.getPriority() != null ? request.getPriority() : Priority.MEDIUM);
        
        // Set optional fields
        todo.setDueDate(request.getDueDate());
        todo.setTags(request.getTags());
        
        // Set category if provided
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElse(null); // If category not found, todo will have no category
            todo.setCategory(category);
        }
        
        return todo;
    }
    
    /**
     * Update existing Todo entity with TodoUpdateRequest data
     * 
     * WHAT IT DOES:
     * 1. Updates all modifiable fields
     * 2. Handles category change (can set or remove)
     * 3. Preserves system-managed fields (id, timestamps)
     */
    public void updateEntity(Todo todo, TodoUpdateRequest request) {
        todo.setTitle(request.getTitle());
        todo.setDescription(request.getDescription());
        todo.setCompleted(request.getCompleted());
        todo.setPriority(request.getPriority());
        todo.setDueDate(request.getDueDate());
        todo.setTags(request.getTags());
        
        // Update category
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElse(null);
            todo.setCategory(category);
        } else {
            // Remove category if categoryId is null
            todo.setCategory(null);
        }
        
        // Don't touch id, createdAt, updatedAt - they are managed by JPA
    }
    
    /**
     * Convert Todo entity to TodoResponse
     * 
     * WHAT IT DOES:
     * 1. Maps all entity fields to response
     * 2. Extracts category info (id and name only, not full object)
     * 3. Calculates computed fields (overdue status)
     * 
     * WHY NOT SEND FULL CATEGORY?
     * - Avoid circular reference (Category has todos list)
     * - Client usually only needs category name
     * - Smaller response size
     */
    public TodoResponse toResponse(Todo todo) {
        TodoResponse response = new TodoResponse();
        response.setId(todo.getId());
        response.setTitle(todo.getTitle());
        response.setDescription(todo.getDescription());
        response.setCompleted(todo.getCompleted());
        response.setPriority(todo.getPriority());
        response.setDueDate(todo.getDueDate());
        response.setTags(todo.getTags());
        response.setCreatedAt(todo.getCreatedAt());
        response.setUpdatedAt(todo.getUpdatedAt());
        
        // Set category info (simplified)
        if (todo.getCategory() != null) {
            response.setCategoryId(todo.getCategory().getId());
            response.setCategoryName(todo.getCategory().getName());
        }
        
        // Calculate overdue status
        response.setOverdue(todo.isOverdue());
        
        return response;
    }
    
    /**
     * Convert list of Todo entities to list of TodoResponse
     * Used when returning multiple todos
     */
    public List<TodoResponse> toResponseList(List<Todo> todos) {
        return todos.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}

// Made with Bob
