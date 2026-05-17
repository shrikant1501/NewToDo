package com.example.NewToDo.controller;

import com.example.NewToDo.dto.PageResponse;
import com.example.NewToDo.dto.TodoCreateRequest;
import com.example.NewToDo.dto.TodoResponse;
import com.example.NewToDo.dto.TodoUpdateRequest;
import com.example.NewToDo.entity.Todo;
import com.example.NewToDo.mapper.TodoMapper;
import com.example.NewToDo.service.TodoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/todos")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TodoController {
    
    private final TodoService todoService;
    private final TodoMapper todoMapper;
    
    /**
     * GET /api/todos - Get all todos
     */
    @GetMapping
    public ResponseEntity<List<TodoResponse>> getAllTodos() {
        List<Todo> todos = todoService.getAllTodos();
        List<TodoResponse> responses = todoMapper.toResponseList(todos);
        return ResponseEntity.ok(responses);
    }
    
    /**
     * GET /api/todos/{id} - Get todo by ID
     * Returns 404 if todo not found (handled by GlobalExceptionHandler)
     */
    @GetMapping("/{id}")
    public ResponseEntity<TodoResponse> getTodoById(@PathVariable Long id) {
        Todo todo = todoService.getTodoById(id); // Throws TodoNotFoundException if not found
        TodoResponse response = todoMapper.toResponse(todo);
        return ResponseEntity.ok(response);
    }
    
    /**
     * POST /api/todos - Create a new todo
     */
    @PostMapping
    public ResponseEntity<TodoResponse> createTodo(@Valid @RequestBody TodoCreateRequest request) {
        Todo todo = todoMapper.toEntity(request);
        Todo createdTodo = todoService.createTodo(todo);
        TodoResponse response = todoMapper.toResponse(createdTodo);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * PUT /api/todos/{id} - Update an existing todo
     * Returns 404 if todo not found (handled by GlobalExceptionHandler)
     */
    @PutMapping("/{id}")
    public ResponseEntity<TodoResponse> updateTodo(
            @PathVariable Long id,
            @Valid @RequestBody TodoUpdateRequest request) {
        
        Todo existingTodo = todoService.getTodoById(id); // Throws TodoNotFoundException if not found
        todoMapper.updateEntity(existingTodo, request);
        Todo updatedTodo = todoService.updateTodo(existingTodo);
        TodoResponse response = todoMapper.toResponse(updatedTodo);
        return ResponseEntity.ok(response);
    }
    
    /**
     * PATCH /api/todos/{id}/toggle - Toggle todo completion status
     * Returns 404 if todo not found (handled by GlobalExceptionHandler)
     */
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<TodoResponse> toggleTodoCompletion(@PathVariable Long id) {
        Todo todo = todoService.toggleTodoCompletion(id); // Throws TodoNotFoundException if not found
        TodoResponse response = todoMapper.toResponse(todo);
        return ResponseEntity.ok(response);
    }
    
    /**
     * DELETE /api/todos/{id} - Delete a todo
     * Returns 404 if todo not found (handled by GlobalExceptionHandler)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTodo(@PathVariable Long id) {
        todoService.deleteTodo(id); // Throws TodoNotFoundException if not found
        return ResponseEntity.noContent().build();
    }
    
    /**
     * GET /api/todos/status/{completed} - Get todos by completion status
     */
    @GetMapping("/status/{completed}")
    public ResponseEntity<List<TodoResponse>> getTodosByStatus(@PathVariable Boolean completed) {
        List<Todo> todos = todoService.getTodosByStatus(completed);
        List<TodoResponse> responses = todoMapper.toResponseList(todos);
        return ResponseEntity.ok(responses);
    }
    
    /**
     * GET /api/todos/search?keyword={keyword} - Search todos by title
     */
    @GetMapping("/search")
    public ResponseEntity<List<TodoResponse>> searchTodos(@RequestParam String keyword) {
        List<Todo> todos = todoService.searchTodosByTitle(keyword);
        List<TodoResponse> responses = todoMapper.toResponseList(todos);
        return ResponseEntity.ok(responses);
    }
    
    /**
     * DELETE /api/todos/completed - Delete all completed todos
     */
    @DeleteMapping("/completed")
    public ResponseEntity<Void> deleteCompletedTodos() {
        todoService.deleteCompletedTodos();
        return ResponseEntity.noContent().build();
    }
    
    /**
     * GET /api/todos/paginated - Get paginated and sorted todos
     *
     * Query Parameters:
     * - page: Page number (0-indexed, default: 0)
     * - size: Number of items per page (default: 10)
     * - sortBy: Field to sort by (default: createdAt)
     * - sortDir: Sort direction - asc or desc (default: desc)
     *
     * Example: GET /api/todos/paginated?page=0&size=10&sortBy=title&sortDir=asc
     */
    @GetMapping("/paginated")
    public ResponseEntity<PageResponse<TodoResponse>> getAllTodosPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        // Create sort direction
        Sort.Direction direction = sortDir.equalsIgnoreCase("asc")
            ? Sort.Direction.ASC
            : Sort.Direction.DESC;
        
        // Create Pageable object
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        // Get paginated todos from service (already mapped to DTOs)
        Page<TodoResponse> responsePage = todoService.getAllTodosPaginated(pageable);
        
        // Create custom page response
        PageResponse<TodoResponse> pageResponse = PageResponse.of(responsePage);
        
        return ResponseEntity.ok(pageResponse);
    }
}

// Made with Bob
