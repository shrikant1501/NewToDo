package com.example.NewToDo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Category entity for organizing todos
 * 
 * WHY WE NEED THIS:
 * - Group related todos together (Work, Personal, Shopping, etc.)
 * - Better organization and filtering
 * - One category can have many todos
 * 
 * PROBLEM IT SOLVES:
 * Without categories: All todos mixed together, hard to find
 * With categories: "Show me all Work todos" - Easy filtering!
 * 
 * RELATIONSHIP:
 * One Category → Many Todos
 * Example: "Work" category has 10 todos, "Personal" has 5 todos
 */
@Entity
@Table(name = "categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Category {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Category name (e.g., "Work", "Personal", "Shopping")
     * 
     * COLUMN CUSTOMIZATION:
     * - unique = true: No duplicate category names
     * - nullable = false: Must have a name
     * - length = 50: Max 50 characters
     */
    @Column(unique = true, nullable = false, length = 50)
    private String name;
    
    /**
     * Optional description of the category
     */
    @Column(length = 200)
    private String description;
    
    /**
     * Color code for UI display (e.g., "#FF5733")
     * Helps visually distinguish categories
     */
    @Column(length = 7)
    private String color;
    
    /**
     * List of todos in this category
     * 
     * RELATIONSHIP EXPLANATION:
     * @OneToMany: One category has many todos
     * mappedBy = "category": The "category" field in Todo entity owns this relationship
     * cascade = CascadeType.ALL: If category is deleted, delete its todos too
     * orphanRemoval = true: If todo is removed from list, delete it from database
     * 
     * WHY mappedBy?
     * - Tells JPA that Todo entity has the foreign key (category_id column)
     * - Category doesn't have a foreign key, it just references the todos
     */
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Todo> todos = new ArrayList<>();
    
    /**
     * Helper method to add a todo to this category
     * Also sets the category reference in the todo (bidirectional relationship)
     */
    public void addTodo(Todo todo) {
        todos.add(todo);
        todo.setCategory(this);
    }
    
    /**
     * Helper method to remove a todo from this category
     */
    public void removeTodo(Todo todo) {
        todos.remove(todo);
        todo.setCategory(null);
    }
}

// Made with Bob
