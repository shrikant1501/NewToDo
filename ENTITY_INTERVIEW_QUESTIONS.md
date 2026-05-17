# 🎯 JPA Entity Interview Questions & Answers

Complete guide covering all entity concepts with detailed answers for interview preparation.

---

## 📚 Quick Reference

**Topics Covered:**
1. Basic Entity Concepts (Q1-Q3)
2. Annotations (Q4-Q6)
3. Relationships (Q7-Q10)
4. Enums (Q11-Q12)
5. Column Customization (Q13-Q15)
6. Lifecycle Callbacks (Q16-Q17)
7. Fetch Strategies (Q18-Q19)
8. Cascade Operations (Q20-Q21)
9. Advanced Topics (Q22-Q25)
10. Scenario-Based Questions (Q26-Q30)

---

## 1. Basic Entity Concepts

### Q1: What is a JPA Entity?

**Answer:**
A JPA Entity is a Java class that represents a table in a relational database. Each instance corresponds to a row.

**Requirements:**
- `@Entity` annotation
- No-argument constructor
- Primary key with `@Id`
- Top-level class (not nested)

**Example:**
```java
@Entity
public class Todo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
}
```

---

### Q2: Difference between `@Entity` and `@Table`?

| Aspect | @Entity | @Table |
|--------|---------|--------|
| Purpose | Marks as JPA entity | Customizes table |
| Required | Yes | No |
| Default | Class name = table name | N/A |

**Example:**
```java
@Entity
@Table(name = "todos", indexes = @Index(columnList = "completed"))
public class Todo { }
```

---

### Q3: Explain `@GeneratedValue` strategies

**1. IDENTITY** (Most Common)
```java
@GeneratedValue(strategy = GenerationType.IDENTITY)
```
- Database auto-increment
- Best for MySQL, PostgreSQL

**2. SEQUENCE**
```java
@GeneratedValue(strategy = GenerationType.SEQUENCE)
```
- Database sequence
- Best for Oracle

**3. AUTO**
```java
@GeneratedValue(strategy = GenerationType.AUTO)
```
- JPA chooses based on database

**4. TABLE**
```java
@GeneratedValue(strategy = GenerationType.TABLE)
```
- Separate table for IDs (rarely used)

---

## 2. Annotations

### Q4: What is `@Column` used for?

**Answer:** Customizes column mapping.

```java
@Column(
    name = "todo_title",      // Custom name
    nullable = false,         // NOT NULL
    unique = true,            // UNIQUE
    length = 100,             // VARCHAR(100)
    insertable = true,        // Include in INSERT
    updatable = true          // Include in UPDATE
)
private String title;
```

**Use Cases:**
- Custom column names
- Add constraints
- Optimize storage
- Read-only fields

---

### Q5: What is `@Transient`?

**Answer:** Marks fields NOT persisted to database.

**Use Cases:**

1. **Computed Fields:**
```java
@Transient
public boolean isOverdue() {
    return dueDate.isBefore(LocalDateTime.now());
}
```

2. **Temporary Data:**
```java
@Transient
private String temporaryToken;
```

3. **Sensitive Data:**
```java
@Transient
private String decryptedPassword;
```

---

### Q6: `@Temporal` vs Java 8 Date/Time API?

**Old Way (Date + @Temporal):**
```java
@Temporal(TemporalType.DATE)
private Date birthDate;

@Temporal(TemporalType.TIMESTAMP)
private Date createdAt;
```

**Modern Way (Java 8+):**
```java
private LocalDate birthDate;        // No @Temporal needed
private LocalDateTime createdAt;    // No @Temporal needed
```

**Interview Tip:** Use Java 8 date/time API!

---

## 3. Relationships

### Q7: Types of JPA relationships?

**1. One-to-One**
```java
@OneToOne
@JoinColumn(name = "profile_id")
private UserProfile profile;
```
Example: User ↔ Profile

**2. Many-to-One / One-to-Many**
```java
// Many side
@ManyToOne
@JoinColumn(name = "category_id")
private Category category;

// One side
@OneToMany(mappedBy = "category")
private List<Todo> todos;
```
Example: Category ↔ Todos

**3. Many-to-Many**
```java
@ManyToMany
@JoinTable(name = "student_course")
private List<Course> courses;
```
Example: Students ↔ Courses

---

### Q8: What is `mappedBy`?

**Answer:** Indicates non-owning side of bidirectional relationship.

```java
// Non-owning side (no foreign key)
@OneToMany(mappedBy = "category")
private List<Todo> todos;

// Owning side (has foreign key)
@ManyToOne
@JoinColumn(name = "category_id")
private Category category;
```

**Key Points:**
- Prevents duplicate foreign keys
- Only owning side changes persist
- Value = field name in owning entity

**Common Mistake:**
```java
// ❌ Wrong - both have @JoinColumn
@OneToMany
@JoinColumn(name = "category_id")
private List<Todo> todos;

// ✅ Correct
@OneToMany(mappedBy = "category")
private List<Todo> todos;
```

---

### Q9: Unidirectional vs Bidirectional?

**Unidirectional:** One-way navigation
```java
@Entity
public class Todo {
    @ManyToOne
    private Category category;  // Todo → Category ✅
}

@Entity
public class Category {
    // No reference to Todo
    // Category → Todo ❌
}
```

**Bidirectional:** Two-way navigation
```java
@Entity
public class Todo {
    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;  // Todo → Category ✅
}

@Entity
public class Category {
    @OneToMany(mappedBy = "category")
    private List<Todo> todos;  // Category → Todo ✅
}
```

**When to use:**
- Unidirectional: Simpler, less memory
- Bidirectional: More flexible, both-way navigation

---

### Q10: `@JoinColumn` vs `@JoinTable`?

**@JoinColumn:** For One-to-One, Many-to-One
```java
@ManyToOne
@JoinColumn(name = "category_id")
private Category category;
```
Creates foreign key in same table.

**@JoinTable:** For Many-to-Many
```java
@ManyToMany
@JoinTable(
    name = "student_course",
    joinColumns = @JoinColumn(name = "student_id"),
    inverseJoinColumns = @JoinColumn(name = "course_id")
)
private List<Course> courses;
```
Creates separate join table.

---

## 4. Enums

### Q11: How to map Enums? ORDINAL vs STRING?

**ORDINAL (Default - Don't Use!):**
```java
@Enumerated(EnumType.ORDINAL)
private Priority priority;

enum Priority { LOW, MEDIUM, HIGH }
// Stored as: 0, 1, 2
```

**Problems:**
- ❌ Adding enum at start breaks data
- ❌ Not readable in database
- ❌ Reordering breaks data

**STRING (Recommended):**
```java
@Enumerated(EnumType.STRING)
private Priority priority;

enum Priority { LOW, MEDIUM, HIGH }
// Stored as: "LOW", "MEDIUM", "HIGH"
```

**Benefits:**
- ✅ Readable
- ✅ Safe to add/reorder
- ✅ Self-documenting

**Interview Tip:** Always use `EnumType.STRING`!

---

### Q12: What happens when adding new enum value?

**With ORDINAL:**
```java
// Before
enum Priority { LOW, MEDIUM, HIGH }  // 0, 1, 2

// Adding at end - Safe
enum Priority { LOW, MEDIUM, HIGH, URGENT }  // 0, 1, 2, 3 ✅

// Adding at start - BREAKS DATA!
enum Priority { URGENT, LOW, MEDIUM, HIGH }  // 0, 1, 2, 3 ❌
// LOW changed from 0 to 1!
```

**With STRING:**
```java
// Before
enum Priority { LOW, MEDIUM, HIGH }

// Can add anywhere - Always safe
enum Priority { URGENT, LOW, MEDIUM, HIGH, CRITICAL }  // ✅
```

---

## 5. Column Customization

### Q13: Why use database indexes?

**Answer:** Dramatically improve query performance.

**Adding Indexes:**
```java
@Entity
@Table(indexes = {
    @Index(name = "idx_completed", columnList = "completed"),
    @Index(name = "idx_priority", columnList = "priority")
})
public class Todo { }
```

**Performance Impact:**

| Rows | Without Index | With Index | Speedup |
|------|---------------|------------|---------|
| 1,000 | 50ms | 1ms | 50x |
| 100,000 | 500ms | 5ms | 100x |
| 1,000,000 | 5000ms | 10ms | 500x |

**When to Add:**
- WHERE clauses
- JOIN conditions
- ORDER BY columns
- Foreign keys

**When NOT to Add:**
- Rarely queried columns
- Frequently updated columns
- Small tables

---

### Q14: What are composite indexes?

**Answer:** Index on multiple columns together.

```java
@Table(indexes = @Index(
    name = "idx_category_priority",
    columnList = "category_id, priority"
))
```

**Usage Rules:**
```java
// Index: (category_id, priority)

WHERE category_id = 1 AND priority = 'HIGH'  // ✅ Uses index
WHERE category_id = 1                        // ✅ Uses index (leftmost)
WHERE priority = 'HIGH'                      // ❌ Doesn't use index
```

**Rule:** Composite index (A, B, C) works for:
- WHERE A
- WHERE A AND B
- WHERE A AND B AND C
- NOT for WHERE B or WHERE C alone

---

### Q15: Explain `nullable`, `unique`, `length`

**nullable:**
```java
@Column(nullable = false)
private String title;  // NOT NULL constraint
```

**unique:**
```java
@Column(unique = true)
private String email;  // UNIQUE constraint
```

**length:**
```java
@Column(length = 100)
private String title;  // VARCHAR(100)
```

**Benefits:**
- Database-level validation
- Saves storage
- Improves performance
- Self-documenting

---

## 6. Lifecycle Callbacks

### Q16: What are JPA lifecycle callbacks?

**Answer:** Methods that execute automatically at specific points.

```java
@Entity
public class Todo {
    
    @PrePersist    // Before INSERT
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    @PostPersist   // After INSERT
    protected void afterCreate() { }
    
    @PreUpdate     // Before UPDATE
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    @PostUpdate    // After UPDATE
    protected void afterUpdate() { }
    
    @PreRemove     // Before DELETE
    protected void onDelete() { }
    
    @PostRemove    // After DELETE
    protected void afterDelete() { }
    
    @PostLoad      // After loading from DB
    protected void afterLoad() { }
}
```

**Execution Order:**
- Create: `@PrePersist → INSERT → @PostPersist`
- Update: `@PreUpdate → UPDATE → @PostUpdate`
- Delete: `@PreRemove → DELETE → @PostRemove`
- Load: `SELECT → @PostLoad`

---

### Q17: Common use cases for lifecycle callbacks?

**1. Automatic Timestamps:**
```java
@PrePersist
protected void onCreate() {
    createdAt = LocalDateTime.now();
}

@PreUpdate
protected void onUpdate() {
    updatedAt = LocalDateTime.now();
}
```

**2. Audit Logging:**
```java
@PostPersist
protected void logCreation() {
    auditService.log("Created: " + title);
}
```

**3. Data Validation:**
```java
@PrePersist
@PreUpdate
protected void validate() {
    if (dueDate.isBefore(LocalDateTime.now())) {
        throw new IllegalStateException("Due date in past");
    }
}
```

**4. Default Values:**
```java
@PrePersist
protected void setDefaults() {
    if (priority == null) priority = Priority.MEDIUM;
}
```

---

## 7. Fetch Strategies

### Q18: LAZY vs EAGER fetching?

**EAGER:** Loads immediately
```java
@ManyToOne(fetch = FetchType.EAGER)
private Category category;

Todo todo = todoRepository.findById(1L).get();
// SQL: SELECT * FROM todos JOIN categories
// Category loaded immediately
```

**LAZY:** Loads on-demand
```java
@ManyToOne(fetch = FetchType.LAZY)
private Category category;

Todo todo = todoRepository.findById(1L).get();
// SQL: SELECT * FROM todos
// Category NOT loaded

String name = todo.getCategory().getName();
// SQL: SELECT * FROM categories
// Category loaded now
```

**Comparison:**

| Aspect | EAGER | LAZY |
|--------|-------|------|
| Loading | Immediate | On-demand |
| Performance | Slower | Faster |
| Memory | More | Less |
| Default @ManyToOne | Yes | No |
| Default @OneToMany | No | Yes |

**Interview Tip:** LAZY is preferred for performance.

---

### Q19: What is N+1 problem? How to solve?

**Problem:**
```java
List<Todo> todos = todoRepository.findAll();  // 1 query

for (Todo todo : todos) {
    todo.getCategory().getName();  // N queries (one per todo)
}
// Total: 1 + N queries (101 for 100 todos!)
```

**Solution 1: JOIN FETCH**
```java
@Query("SELECT t FROM Todo t JOIN FETCH t.category")
List<Todo> findAllWithCategory();
// Only 1 query!
```

**Solution 2: Entity Graph**
```java
@EntityGraph(attributePaths = {"category"})
List<Todo> findAll();
// Only 1 query!
```

**Solution 3: Batch Fetching**
```java
@ManyToOne(fetch = FetchType.LAZY)
@BatchSize(size = 10)
private Category category;
// 11 queries for 100 todos (instead of 101)
```

**Performance:**
| Approach | Queries | Time |
|----------|---------|------|
| N+1 Problem | 101 | 1010ms |
| JOIN FETCH | 1 | 10ms |
| Batch Fetching | 11 | 110ms |

---

## 8. Cascade Operations

### Q20: Explain cascade types

**CascadeType.PERSIST:** Save children with parent
```java
@OneToMany(cascade = CascadeType.PERSIST)
private List<Todo> todos;

categoryRepository.save(category);  // Saves todos too
```

**CascadeType.MERGE:** Update children with parent
```java
@OneToMany(cascade = CascadeType.MERGE)
private List<Todo> todos;

categoryRepository.save(category);  // Updates todos too
```

**CascadeType.REMOVE:** Delete children with parent
```java
@OneToMany(cascade = CascadeType.REMOVE)
private List<Todo> todos;

categoryRepository.delete(category);  // Deletes todos too
```

**CascadeType.ALL:** All operations
```java
@OneToMany(cascade = CascadeType.ALL)
private List<Todo> todos;
```

**Warning:** Be careful with REMOVE - can delete lots of data!

---

### Q21: orphanRemoval vs CascadeType.REMOVE?

**CascadeType.REMOVE:** Deletes when parent deleted
```java
@OneToMany(cascade = CascadeType.REMOVE)
private List<Todo> todos;

categoryRepository.delete(category);  // ✅ Deletes todos
category.getTodos().remove(todo);     // ❌ Todo still exists
```

**orphanRemoval:** Deletes when removed from collection
```java
@OneToMany(orphanRemoval = true)
private List<Todo> todos;

categoryRepository.delete(category);  // ❌ Doesn't delete todos
category.getTodos().remove(todo);     // ✅ Deletes todo
```

**Both:**
```java
@OneToMany(cascade = CascadeType.REMOVE, orphanRemoval = true)
private List<Todo> todos;

categoryRepository.delete(category);  // ✅ Deletes todos
category.getTodos().remove(todo);     // ✅ Deletes todo
```

**Comparison:**

| Action | CascadeType.REMOVE | orphanRemoval |
|--------|-------------------|---------------|
| Delete parent | Deletes children | No effect |
| Remove from collection | No effect | Deletes child |

---

## 9. Advanced Topics

### Q22: `@Embeddable` vs `@Entity`?

**@Embeddable:** No separate table
```java
@Embeddable
public class Address {
    private String street;
    private String city;
}

@Entity
public class User {
    @Embedded
    private Address address;  // Embedded in users table
}

// Database: Only users table with street, city columns
```

**@Entity:** Separate table
```java
@Entity
public class Address {
    @Id
    private Long id;
    private String street;
}

@Entity
public class User {
    @OneToOne
    private Address address;  // Foreign key to addresses table
}

// Database: users and addresses tables
```

**When to Use:**
| Use @Embeddable | Use @Entity |
|-----------------|-------------|
| No identity | Has identity |
| Part of parent | Independent |
| Example: Address, Money | Example: User, Order |

---

### Q23: What is `@MappedSuperclass`?

**Answer:** Base class with common fields inherited by entities.

```java
@MappedSuperclass
public abstract class BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
}

@Entity
public class Todo extends BaseEntity {
    private String title;
    // Inherits: id, createdAt, updatedAt
}

@Entity
public class Category extends BaseEntity {
    private String name;
    // Inherits: id, createdAt, updatedAt
}
```

**Benefits:**
- ✅ DRY (Don't Repeat Yourself)
- ✅ Consistent fields
- ✅ Centralized callbacks
- ✅ Easy maintenance

---

### Q24: Explain `@Version` and optimistic locking

**Answer:** Prevents lost updates in concurrent scenarios.

**Problem Without @Version:**
```java
// User 1 reads
Todo todo1 = repo.findById(1L).get();  // title = "Buy milk"

// User 2 reads
Todo todo2 = repo.findById(1L).get();  // title = "Buy milk"

// User 1 updates
todo1.setTitle("Buy milk and eggs");
repo.save(todo1);  // title = "Buy milk and eggs"

// User 2 updates (overwrites User 1's change!)
todo2.setTitle("Buy milk and bread");
repo.save(todo2);  // title = "Buy milk and bread"
// User 1's "eggs" is lost!
```

**Solution With @Version:**
```java
@Entity
public class Todo {
    @Id
    private Long id;
    
    @Version
    private Long version;  // Managed by JPA
    
    private String title;
}

// User 1 updates
todo1.setTitle("Buy milk and eggs");
repo.save(todo1);  // Success! version = 2

// User 2 updates
todo2.setTitle("Buy milk and bread");
repo.save(todo2);  // Throws OptimisticLockException
// version mismatch (expected 1, found 2)
```

**How It Works:**
1. JPA includes version in WHERE clause
2. If version doesn't match, update fails
3. Throws `OptimisticLockException`
4. Application can retry or notify user

---

### Q25: `@JoinColumn` attributes explained

```java
@ManyToOne
@JoinColumn(
    name = "category_id",              // FK column name
    nullable = false,                  // NOT NULL
    referencedColumnName = "id",       // Referenced column
    foreignKey = @ForeignKey(name = "fk_todo_category")
)
private Category category;
```

**Database Result:**
```sql
CREATE TABLE todos (
    id BIGINT PRIMARY KEY,
    category_id BIGINT NOT NULL,
    CONSTRAINT fk_todo_category 
        FOREIGN KEY (category_id) 
        REFERENCES categories(id)
);
```

---

## 10. Scenario-Based Questions

### Q26: Design a blog system (Posts, Comments, Tags)

```java
@Entity
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String title;
    
    // One Post has Many Comments
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();
    
    // Many Posts have Many Tags
    @ManyToMany
    @JoinTable(
        name = "post_tags",
        joinColumns = @JoinColumn(name = "post_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();
}

@Entity
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String content;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;
}

@Entity
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String name;
    
    @ManyToMany(mappedBy = "tags")
    private Set<Post> posts = new HashSet<>();
}
```

---

### Q27: Implement soft delete

```java
@Entity
@SQLDelete(sql = "UPDATE todos SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class Todo {
    @Id
    private Long id;
    
    private String title;
    
    @Column(nullable = false)
    private boolean deleted = false;
    
    private LocalDateTime deletedAt;
}

// Usage
todoRepository.delete(todo);
// SQL: UPDATE todos SET deleted = true WHERE id = 1
// Record still exists in database

List<Todo> todos = todoRepository.findAll();
// SQL: SELECT * FROM todos WHERE deleted = false
// Only returns non-deleted todos
```

**Benefits:**
- ✅ Can recover deleted data
- ✅ Audit trail
- ✅ Referential integrity maintained

---

### Q28: Handle multi-tenancy

```java
@Entity
@Table(indexes = @Index(columnList = "tenant_id"))
public class Todo {
    @Id
    private Long id;
    
    @Column(nullable = false)
    private String tenantId;
    
    private String title;
}

@Repository
public interface TodoRepository extends JpaRepository<Todo, Long> {
    List<Todo> findByTenantId(String tenantId);
}

@Service
public class TodoService {
    public List<Todo> getAllTodos() {
        String tenantId = TenantContext.getCurrentTenantId();
        return todoRepository.findByTenantId(tenantId);
    }
}
```

---

### Q29: Best practices for JPA entities

**1. Use EnumType.STRING**
```java
@Enumerated(EnumType.STRING)
private Priority priority;
```

**2. Add indexes on queried columns**
```java
@Table(indexes = @Index(columnList = "completed"))
```

**3. Use LAZY fetching**
```java
@ManyToOne(fetch = FetchType.LAZY)
private Category category;
```

**4. Implement equals/hashCode**
```java
@Override
public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Todo)) return false;
    return Objects.equals(id, ((Todo) o).id);
}
```

**5. Use @MappedSuperclass for common fields**
```java
@MappedSuperclass
public abstract class BaseEntity {
    @Id
    private Long id;
    private LocalDateTime createdAt;
}
```

**6. Add proper constraints**
```java
@Column(nullable = false, length = 100)
private String title;
```

**7. Use lifecycle callbacks**
```java
@PrePersist
protected void onCreate() {
    createdAt = LocalDateTime.now();
}
```

---

### Q30: Common mistakes to avoid

**1. Using ORDINAL for enums**
```java
// ❌ Wrong
@Enumerated(EnumType.ORDINAL)

// ✅ Correct
@Enumerated(EnumType.STRING)
```

**2. Not handling N+1 problem**
```java
// ❌ Wrong
List<Todo> todos = repo.findAll();
todos.forEach(t -> t.getCategory().getName());

// ✅ Correct
@Query("SELECT t FROM Todo t JOIN FETCH t.category")
List<Todo> findAllWithCategory();
```

**3. Using @JoinColumn on both sides**
```java
// ❌ Wrong
@OneToMany
@JoinColumn(name = "category_id")

// ✅ Correct
@OneToMany(mappedBy = "category")
```

**4. Not using @Transactional**
```java
// ❌ Wrong - lazy loading fails
public Todo getTodo(Long id) {
    return repo.findById(id).get();
}

// ✅ Correct
@Transactional
public Todo getTodo(Long id) {
    return repo.findById(id).get();
}
```

**5. Forgetting cascade operations**
```java
// ❌ Wrong - children not saved
@OneToMany(mappedBy = "category")

// ✅ Correct
@OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
```

---

## 📝 Interview Preparation Tips

**1. Understand the "Why"**
- Don't just memorize syntax
- Understand why each annotation exists
- Know the problems they solve

**2. Practice Common Scenarios**
- One-to-Many relationships
- N+1 problem solutions
- Enum mapping
- Cascade operations

**3. Know Performance Implications**
- LAZY vs EAGER
- Index usage
- N+1 problem
- Batch fetching

**4. Be Ready for Follow-ups**
- "What happens if...?"
- "How would you optimize...?"
- "What's the difference between...?"

**5. Use Real Examples**
- Relate to projects you've worked on
- Explain with concrete use cases
- Show problem-solving approach

---

## 🎯 Quick Review Checklist

Before your interview, make sure you can explain:

- [ ] What is a JPA Entity
- [ ] @Entity vs @Table
- [ ] @Id and generation strategies
- [ ] @Column attributes
- [ ] @Transient use cases
- [ ] All relationship types
- [ ] mappedBy concept
- [ ] Unidirectional vs Bidirectional
- [ ] EnumType.STRING vs ORDINAL
- [ ] Database indexes
- [ ] Lifecycle callbacks
- [ ] LAZY vs EAGER fetching
- [ ] N+1 problem and solutions
- [ ] Cascade types
- [ ] orphanRemoval
- [ ] @Embeddable vs @Entity
- [ ] @MappedSuperclass
- [ ] @Version and optimistic locking
- [ ] Best practices
- [ ] Common mistakes

---

**Good luck with your interview! 🚀**