package com.example.NewToDo.repository;

import com.example.NewToDo.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Category entity
 * 
 * WHY WE NEED THIS:
 * - CRUD operations for categories
 * - Find category by name
 * - Check if category exists
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    /**
     * Find category by name (case-insensitive)
     * Useful for checking if category already exists
     */
    Optional<Category> findByNameIgnoreCase(String name);
    
    /**
     * Check if category with given name exists
     * Useful for validation before creating
     */
    boolean existsByNameIgnoreCase(String name);
}

// Made with Bob
