package com.example.NewToDo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Enhanced Todo entity with:
 * 1. Enum mapping (Priority)
 * 2. Relationship (Many-to-One with Category)
 * 3. Column customization (indexes, constraints)
 * 4. Additional useful fields (dueDate, tags)
 */
@Entity
@Table(name = "todos", indexes = {
        @Index(name = "idx_completed", columnList = "completed"),
        @Index(name = "idx_priority", columnList = "priority"),
        @Index(name = "idx_due_date", columnList = "due_date")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Todo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Title of the todo
     * 
     * COLUMN CUSTOMIZATION:
     * - nullable = false: Must have a title
     * - length = 100: Max 100 characters (default is 255)
     * 
     * WHY: Prevents empty todos, saves database space
     */
    @Column(nullable = false, length = 100)
    private String title;
    
    /**
     * Detailed description
     * 
     * COLUMN CUSTOMIZATION:
     * - length = 1000: Longer text allowed
     * - columnDefinition = "TEXT": For very long descriptions (optional)
     * 
     * WHY: Some todos need detailed instructions
     */
    @Column(length = 1000)
    private String description;
    
    /**
     * Completion status
     * 
     * COLUMN CUSTOMIZATION:
     * - nullable = false: Must be true or false
     * - columnDefinition: Specifies exact database type
     * 
     * WHY: Indexed for fast filtering (show completed/pending)
     */
    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean completed = false;
    
    /**
     * Priority level using ENUM
     * 
     * ENUM MAPPING:
     * @Enumerated(EnumType.STRING): Stores "LOW", "MEDIUM", "HIGH" in database
     * Alternative: EnumType.ORDINAL stores 0, 1, 2 (not recommended - breaks if enum order changes)
     * 
     * WHY ENUM?
     * - Type-safe: Can't set invalid priority
     * - No typos: Priority.HIGH vs "hihg"
     * - Easy to change: Add URGENT priority later
     * 
     * PROBLEM IT SOLVES:
     * Without enum: todo.setPriority("urgent"); // What if we type "urgnet"?
     * With enum: todo.setPriority(Priority.HIGH); // Compiler checks!
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Priority priority = Priority.MEDIUM;
    
    /**
     * Due date for the todo
     * 
     * WHY LocalDate not LocalDateTime?
     * - LocalDate: Just the date (2024-01-15)
     * - LocalDateTime: Date + time (2024-01-15 10:30:00)
     * 
     * For todos, usually just the date matters
     * 
     * COLUMN CUSTOMIZATION:
     * - name = "due_date": Database column name (snake_case)
     * - Java field: dueDate (camelCase)
     */
    @Column(name = "due_date")
    private LocalDate dueDate;
    
    /**
     * Tags for additional categorization
     * Example: "urgent, client-meeting, important"
     * 
     * WHY String not List<String>?
     * - Simple: Easy to store and query
     * - Flexible: Can search with LIKE '%urgent%'
     * 
     * Alternative: Create separate Tag entity (more complex)
     */
    @Column(length = 200)
    private String tags;
    
    /**
     * Category relationship (Many-to-One)
     * 
     * RELATIONSHIP EXPLANATION:
     * @ManyToOne: Many todos belong to one category
     * Example: 10 todos in "Work" category, 5 todos in "Personal"
     * 
     * @JoinColumn: Specifies foreign key column
     * - name = "category_id": Column name in todos table
     * - nullable = true: Todo can exist without category
     * 
     * @JsonIgnore: Prevents infinite loop when converting to JSON
     * (Category has todos list, Todo has category - circular reference)
     * 
     * WHY RELATIONSHIP?
     * - Organization: Group related todos
     * - Filtering: "Show all Work todos"
     * - Cascading: Delete category → optionally delete todos
     * 
     * DATABASE STRUCTURE:
     * todos table will have:
     * - id (primary key)
     * - title
     * - category_id (foreign key to categories table)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    @JsonIgnore
    private Category category;
    
    /**
     * Creation timestamp
     * 
     * COLUMN CUSTOMIZATION:
     * - name = "created_at": Database column name
     * - nullable = false: Must have creation time
     * - updatable = false: Can't change after creation
     * 
     * WHY updatable = false?
     * - Creation time should never change
     * - Even if you try to update it, JPA ignores it
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * Last update timestamp
     * 
     * COLUMN CUSTOMIZATION:
     * - name = "updated_at": Database column name
     * - No updatable = false: This SHOULD change on updates
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * JPA Lifecycle callback - runs before INSERT
     * 
     * WHY @PrePersist?
     * - Automatically sets timestamps
     * - No need to manually set in service layer
     * - Consistent across all creates
     * 
     * WHEN IT RUNS:
     * todoRepository.save(newTodo) → @PrePersist → INSERT into database
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        
        // Set default priority if not set
        if (priority == null) {
            priority = Priority.MEDIUM;
        }
    }
    
    /**
     * JPA Lifecycle callback - runs before UPDATE
     * 
     * WHY @PreUpdate?
     * - Automatically updates timestamp
     * - No need to remember to set it
     * 
     * WHEN IT RUNS:
     * todo.setTitle("New"); todoRepository.save(todo) → @PreUpdate → UPDATE database
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * Helper method to check if todo is overdue
     * Not stored in database - calculated on the fly
     * 
     * WHY NOT @Column?
     * - Derived value, changes based on current date
     * - No need to store it
     */
    @Transient
    public boolean isOverdue() {
        if (dueDate == null || completed) {
            return false;
        }
        return dueDate.isBefore(LocalDate.now());
    }
}

// Made with Bob
