package com.example.NewToDo.mapper;

import com.example.NewToDo.dto.TodoCreateRequest;
import com.example.NewToDo.dto.TodoResponse;
import com.example.NewToDo.dto.TodoUpdateRequest;
import com.example.NewToDo.entity.Todo;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class TodoMapper {
    
    /**
     * Convert TodoCreateRequest to Todo entity
     * Used when creating a new todo
     */
    public Todo toEntity(TodoCreateRequest request) {
        Todo todo = new Todo();
        todo.setTitle(request.getTitle());
        todo.setDescription(request.getDescription());
        todo.setCompleted(false); // Default value for new todos
        return todo;
    }
    
    /**
     * Update existing Todo entity with TodoUpdateRequest data
     * Used when updating an existing todo
     */
    public void updateEntity(Todo todo, TodoUpdateRequest request) {
        todo.setTitle(request.getTitle());
        todo.setDescription(request.getDescription());
        todo.setCompleted(request.getCompleted());
        // Don't touch id, createdAt, updatedAt - they are managed by JPA
    }
    
    /**
     * Convert Todo entity to TodoResponse
     * Used when returning data to the client
     */
    public TodoResponse toResponse(Todo todo) {
        TodoResponse response = new TodoResponse();
        response.setId(todo.getId());
        response.setTitle(todo.getTitle());
        response.setDescription(todo.getDescription());
        response.setCompleted(todo.getCompleted());
        response.setCreatedAt(todo.getCreatedAt());
        response.setUpdatedAt(todo.getUpdatedAt());
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
