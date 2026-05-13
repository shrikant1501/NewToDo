package com.example.NewToDo.repository;

import com.example.NewToDo.entity.Todo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TodoRepository extends JpaRepository<Todo, Long> {
    
    // Find all todos by completion status
    List<Todo> findByCompleted(Boolean completed);
    
    // Find todos by title containing a keyword (case-insensitive)
    List<Todo> findByTitleContainingIgnoreCase(String keyword);
    
    // Find all todos ordered by creation date descending
    List<Todo> findAllByOrderByCreatedAtDesc();
}

// Made with Bob
