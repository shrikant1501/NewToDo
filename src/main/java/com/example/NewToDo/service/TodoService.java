package com.example.NewToDo.service;

import com.example.NewToDo.entity.Todo;
import com.example.NewToDo.repository.TodoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class TodoService {
    
    private final TodoRepository todoRepository;
    
    /**
     * Get all todos
     */
    public List<Todo> getAllTodos() {
        return todoRepository.findAllByOrderByCreatedAtDesc();
    }
    
    /**
     * Get todo by ID
     */
    public Optional<Todo> getTodoById(Long id) {
        return todoRepository.findById(id);
    }
    
    /**
     * Create a new todo
     */
    public Todo createTodo(Todo todo) {
        todo.setId(null); // Ensure it's a new entity
        if (todo.getCompleted() == null) {
            todo.setCompleted(false);
        }
        return todoRepository.save(todo);
    }
    
    /**
     * Update an existing todo
     */
    public Todo updateTodo(Todo todo) {
        return todoRepository.save(todo);
    }
    
    /**
     * Toggle todo completion status
     */
    public Optional<Todo> toggleTodoCompletion(Long id) {
        return todoRepository.findById(id)
                .map(todo -> {
                    todo.setCompleted(!todo.getCompleted());
                    return todoRepository.save(todo);
                });
    }
    
    /**
     * Delete a todo
     */
    public boolean deleteTodo(Long id) {
        if (todoRepository.existsById(id)) {
            todoRepository.deleteById(id);
            return true;
        }
        return false;
    }
    
    /**
     * Get todos by completion status
     */
    public List<Todo> getTodosByStatus(Boolean completed) {
        return todoRepository.findByCompleted(completed);
    }
    
    /**
     * Search todos by title
     */
    public List<Todo> searchTodosByTitle(String keyword) {
        return todoRepository.findByTitleContainingIgnoreCase(keyword);
    }
    
    /**
     * Delete all completed todos
     */
    public void deleteCompletedTodos() {
        List<Todo> completedTodos = todoRepository.findByCompleted(true);
        todoRepository.deleteAll(completedTodos);
    }
}

// Made with Bob
