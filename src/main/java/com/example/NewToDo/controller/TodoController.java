package com.example.NewToDo.controller;

import com.example.NewToDo.entity.Todo;
import com.example.NewToDo.service.TodoService;
import lombok.RequiredArgsConstructor;
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
    
    /**
     * GET /api/todos - Get all todos
     */
    @GetMapping
    public ResponseEntity<List<Todo>> getAllTodos() {
        List<Todo> todos = todoService.getAllTodos();
        return ResponseEntity.ok(todos);
    }
    
    /**
     * GET /api/todos/{id} - Get todo by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Todo> getTodoById(@PathVariable Long id) {
        return todoService.getTodoById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * POST /api/todos - Create a new todo
     */
    @PostMapping
    public ResponseEntity<Todo> createTodo(@RequestBody Todo todo) {
        Todo createdTodo = todoService.createTodo(todo);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTodo);
    }
    
    /**
     * PUT /api/todos/{id} - Update an existing todo
     */
    @PutMapping("/{id}")
    public ResponseEntity<Todo> updateTodo(@PathVariable Long id, @RequestBody Todo todoDetails) {
        return todoService.updateTodo(id, todoDetails)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * PATCH /api/todos/{id}/toggle - Toggle todo completion status
     */
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<Todo> toggleTodoCompletion(@PathVariable Long id) {
        return todoService.toggleTodoCompletion(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * DELETE /api/todos/{id} - Delete a todo
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTodo(@PathVariable Long id) {
        boolean deleted = todoService.deleteTodo(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
    
    /**
     * GET /api/todos/status/{completed} - Get todos by completion status
     */
    @GetMapping("/status/{completed}")
    public ResponseEntity<List<Todo>> getTodosByStatus(@PathVariable Boolean completed) {
        List<Todo> todos = todoService.getTodosByStatus(completed);
        return ResponseEntity.ok(todos);
    }
    
    /**
     * GET /api/todos/search?keyword={keyword} - Search todos by title
     */
    @GetMapping("/search")
    public ResponseEntity<List<Todo>> searchTodos(@RequestParam String keyword) {
        List<Todo> todos = todoService.searchTodosByTitle(keyword);
        return ResponseEntity.ok(todos);
    }
    
    /**
     * DELETE /api/todos/completed - Delete all completed todos
     */
    @DeleteMapping("/completed")
    public ResponseEntity<Void> deleteCompletedTodos() {
        todoService.deleteCompletedTodos();
        return ResponseEntity.noContent().build();
    }
}

// Made with Bob
