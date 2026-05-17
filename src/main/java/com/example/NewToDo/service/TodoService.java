package com.example.NewToDo.service;

import com.example.NewToDo.dto.TodoResponse;
import com.example.NewToDo.entity.Todo;
import com.example.NewToDo.exception.TodoNotFoundException;
import com.example.NewToDo.mapper.TodoMapper;
import com.example.NewToDo.repository.TodoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TodoService {
    
    private final TodoRepository todoRepository;
    private final TodoMapper todoMapper;
    
    /**
     * Get all todos
     */
    public List<Todo> getAllTodos() {
        return todoRepository.findAllByOrderByCreatedAtDesc();
    }
    
    /**
     * Get todo by ID
     * @throws TodoNotFoundException if todo with given id doesn't exist
     */
    public Todo getTodoById(Long id) {
        return todoRepository.findById(id)
                .orElseThrow(() -> new TodoNotFoundException(id));
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
     * @throws TodoNotFoundException if todo with given id doesn't exist
     */
    public Todo toggleTodoCompletion(Long id) {
        Todo todo = getTodoById(id); // This will throw TodoNotFoundException if not found
        todo.setCompleted(!todo.getCompleted());
        return todoRepository.save(todo);
    }
    
    /**
     * Delete a todo
     * @throws TodoNotFoundException if todo with given id doesn't exist
     */
    public void deleteTodo(Long id) {
        if (!todoRepository.existsById(id)) {
            throw new TodoNotFoundException(id);
        }
        todoRepository.deleteById(id);
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
    
    /**
     * Get paginated and sorted todos
     * Maps entities to DTOs within transaction to avoid LazyInitializationException
     * 
     * @param pageable Contains page number, size, and sort information
     * @return Page of TodoResponse DTOs
     */
    @Transactional(readOnly = true)
    public Page<TodoResponse> getAllTodosPaginated(Pageable pageable) {
        // Fetch paginated entities from database
        Page<Todo> todoPage = todoRepository.findAll(pageable);
        
        // Map entities to DTOs within transaction (important for lazy loading)
        return todoPage.map(todoMapper::toResponse);
    }
}

// Made with Bob
