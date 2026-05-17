# Entity Enhancements Summary

## What We Added to Todo Entity

Successfully enhanced the Todo entity with **Enum Mapping**, **Relationships**, and **Column Customization** following enterprise best practices.

---

## 🎯 Three Main Enhancements

### 1. **Enum Mapping (Priority)**
### 2. **Relationships (Category)**
### 3. **Column Customization (Indexes, Constraints)**

---

## 1️⃣ Enum Mapping - Priority

### **What is an Enum?**
An enum (enumeration) is a special data type that represents a fixed set of constants.

### **The Problem Without Enum**

```java
// ❌ BAD: Using String
todo.setPriority("high");
todo.setPriority("hihg");  // Typo! No error until runtime
todo.setPriority("urgent"); // Invalid value, no error
```

**Issues:**
- Typos cause bugs
- No validation
- Can set any string value
- Hard to maintain

### **The Solution With Enum**

```java
// ✅ GOOD: Using Enum
public enum Priority {
    LOW, MEDIUM, HIGH
}

todo.setPriority(Priority.HIGH);
todo.setPriority(Priority.HIHG);  // ❌ Compiler error!
```

**Benefits:**
- ✅ Type-safe (compiler checks)
- ✅ No typos possible
- ✅ Only valid values allowed
- ✅ Easy to add new priorities
- ✅ Auto-complete in IDE

### **How It's Stored in Database**

```java
@Enumerated(EnumType.STRING)
@Column(nullable = false, length = 10)
private Priority priority = Priority.MEDIUM;
```

**Two Storage Options:**

| Type | Database Value | Pros | Cons |
|------|---------------|------|------|
| `EnumType.STRING` | "LOW", "MEDIUM", "HIGH" | Readable, safe if order changes | Takes more space |
| `EnumType.ORDINAL` | 0, 1, 2 | Saves space | Breaks if you reorder enum! |

**We use STRING** because it's safer!

### **Database Table**

```sql
CREATE TABLE todos (
    id BIGINT PRIMARY KEY,
    title VARCHAR(100),
    priority VARCHAR(10) NOT NULL,  -- Stores "LOW", "MEDIUM", "HIGH"
    ...
);
```

### **Example Usage**

```java
// Create todo with HIGH priority
Todo todo = new Todo();
todo.setTitle("Fix critical bug");
todo.setPriority(Priority.HIGH);

// Query by priority
List<Todo> highPriorityTodos = todoRepository.findByPriority(Priority.HIGH);
```

---

## 2️⃣ Relationships - Category

### **What is a Relationship?**
A relationship connects two entities (tables) in the database.

### **The Problem Without Relationships**

```java
// ❌ BAD: Storing category as String
todo.setCategory("Work");
todo.setCategory("work");  // Duplicate! (different case)
todo.setCategory("Wrk");   // Typo!
```

**Issues:**
- Duplicates (Work, work, WORK)
- Typos
- Can't store category details (color, description)
- Hard to query all todos in a category

### **The Solution With Relationships**

```java
// Category entity
@Entity
public class Category {
    @Id
    private Long id;
    private String name;
    private String color;
    
    @OneToMany(mappedBy = "category")
    private List<Todo> todos;  // All todos in this category
}

// Todo entity
@Entity
public class Todo {
    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;  // Reference to category
}
```

**Benefits:**
- ✅ No duplicates (one "Work" category)
- ✅ Can store category details
- ✅ Easy to query: "Show all Work todos"
- ✅ Referential integrity (database enforces)

### **Relationship Types**

#### **Many-to-One (What We Used)**

```
Many Todos → One Category

Example:
- "Fix bug" todo → Work category
- "Write code" todo → Work category
- "Buy milk" todo → Personal category
```

**In Database:**

```sql
-- categories table
id | name     | color
1  | Work     | #FF5733
2  | Personal | #33FF57

-- todos table
id | title      | category_id (foreign key)
1  | Fix bug    | 1  (points to Work)
2  | Write code | 1  (points to Work)
3  | Buy milk   | 2  (points to Personal)
```

#### **Other Relationship Types (For Reference)**

| Type | Example | Description |
|------|---------|-------------|
| **One-to-One** | User ↔ Profile | One user has one profile |
| **One-to-Many** | Category → Todos | One category has many todos |
| **Many-to-One** | Todos → Category | Many todos belong to one category |
| **Many-to-Many** | Students ↔ Courses | Many students take many courses |

### **Key Annotations Explained**

```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "category_id")
@JsonIgnore
private Category category;
```

**@ManyToOne:**
- Defines the relationship type
- Many todos belong to one category

**fetch = FetchType.LAZY:**
- Don't load category immediately
- Load only when accessed (performance optimization)
- Alternative: EAGER (load immediately)

**@JoinColumn(name = "category_id"):**
- Specifies foreign key column name
- Creates `category_id` column in `todos` table

**@JsonIgnore:**
- Don't include in JSON response
- Prevents infinite loop (Category has todos, Todo has category)

### **Bidirectional Relationship**

```java
// Category side (One)
@OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
private List<Todo> todos = new ArrayList<>();

// Todo side (Many)
@ManyToOne
@JoinColumn(name = "category_id")
private Category category;
```

**mappedBy = "category":**
- Tells JPA that Todo entity owns the relationship
- Category doesn't have foreign key, Todo does

**cascade = CascadeType.ALL:**
- Operations on Category cascade to Todos
- Delete category → Delete all its todos
- Save category → Save all its todos

---

## 3️⃣ Column Customization

### **Why Customize Columns?**
- Performance (indexes for fast queries)
- Data integrity (constraints)
- Storage optimization (appropriate lengths)
- Database naming conventions

### **Table-Level Customization**

```java
@Entity
@Table(name = "todos", indexes = {
        @Index(name = "idx_completed", columnList = "completed"),
        @Index(name = "idx_priority", columnList = "priority"),
        @Index(name = "idx_due_date", columnList = "due_date")
})
public class Todo {
    // ...
}
```

**@Table(name = "todos"):**
- Custom table name
- Default would be "todo" (class name)

**@Index:**
- Creates database index for fast queries
- Example: Finding all completed todos is now super fast!

**Why Indexes?**
```sql
-- Without index: Scans ALL rows (slow)
SELECT * FROM todos WHERE completed = true;  -- Scans 1,000,000 rows

-- With index: Uses index (fast)
SELECT * FROM todos WHERE completed = true;  -- Finds instantly
```

### **Column-Level Customization**

#### **Basic Constraints**

```java
@Column(nullable = false, length = 100)
private String title;
```

- `nullable = false`: Must have a value (NOT NULL in SQL)
- `length = 100`: Max 100 characters (VARCHAR(100) in SQL)

#### **Unique Constraint**

```java
@Column(unique = true, nullable = false, length = 50)
private String name;  // In Category entity
```

- `unique = true`: No duplicates allowed
- Database enforces: Can't have two "Work" categories

#### **Custom Column Name**

```java
@Column(name = "due_date")
private LocalDate dueDate;
```

- Java: `dueDate` (camelCase)
- Database: `due_date` (snake_case)
- Follows database naming conventions

#### **Updatable Control**

```java
@Column(name = "created_at", nullable = false, updatable = false)
private LocalDateTime createdAt;
```

- `updatable = false`: Can't change after creation
- Even if you try: `todo.setCreatedAt(newDate)`, JPA ignores it

#### **Column Definition**

```java
@Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
private Boolean completed = false;
```

- `columnDefinition`: Exact SQL for column
- Sets default value in database
- Useful for database-specific types

---

## 🔄 JPA Lifecycle Callbacks

### **What Are Lifecycle Callbacks?**
Methods that run automatically at specific times in an entity's lifecycle.

### **@PrePersist - Before INSERT**

```java
@PrePersist
protected void onCreate() {
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();
    
    if (priority == null) {
        priority = Priority.MEDIUM;
    }
}
```

**When it runs:**
```java
Todo todo = new Todo();
todo.setTitle("Test");
todoRepository.save(todo);  // ← @PrePersist runs HERE (before INSERT)
```

**What it does:**
- Sets timestamps automatically
- Sets default values
- Runs validation

### **@PreUpdate - Before UPDATE**

```java
@PreUpdate
protected void onUpdate() {
    updatedAt = LocalDateTime.now();
}
```

**When it runs:**
```java
Todo todo = todoRepository.findById(1L).get();
todo.setTitle("Updated");
todoRepository.save(todo);  // ← @PreUpdate runs HERE (before UPDATE)
```

### **Other Lifecycle Callbacks**

| Annotation | When It Runs |
|-----------|--------------|
| `@PrePersist` | Before INSERT |
| `@PostPersist` | After INSERT |
| `@PreUpdate` | Before UPDATE |
| `@PostUpdate` | After UPDATE |
| `@PreRemove` | Before DELETE |
| `@PostRemove` | After DELETE |
| `@PostLoad` | After loading from database |

---

## 📊 Complete Database Schema

### **Generated SQL**

```sql
-- Categories table
CREATE TABLE categories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(200),
    color VARCHAR(7)
);

-- Todos table
CREATE TABLE todos (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(100) NOT NULL,
    description VARCHAR(1000),
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    priority VARCHAR(10) NOT NULL,
    due_date DATE,
    tags VARCHAR(200),
    category_id BIGINT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    
    -- Foreign key
    CONSTRAINT fk_category FOREIGN KEY (category_id) 
        REFERENCES categories(id),
    
    -- Indexes
    INDEX idx_completed (completed),
    INDEX idx_priority (priority),
    INDEX idx_due_date (due_date)
);
```

---

## 🎯 Real-World Example

### **Creating a Todo with All Features**

```json
POST /api/todos
{
  "title": "Prepare presentation",
  "description": "Create slides for client meeting",
  "priority": "HIGH",
  "dueDate": "2024-01-20",
  "tags": "urgent, client, important",
  "categoryId": 1
}
```

### **What Happens Behind the Scenes**

1. **Controller** receives TodoCreateRequest
2. **Mapper** converts to Todo entity:
   - Sets title, description
   - Maps priority enum: "HIGH" → Priority.HIGH
   - Looks up Category by ID
   - Sets due date and tags
3. **@PrePersist** runs:
   - Sets createdAt = now()
   - Sets updatedAt = now()
   - Ensures priority is set
4. **Database** INSERT:
   ```sql
   INSERT INTO todos (title, description, completed, priority, 
                      due_date, tags, category_id, created_at, updated_at)
   VALUES ('Prepare presentation', 'Create slides...', false, 'HIGH',
           '2024-01-20', 'urgent, client, important', 1, NOW(), NOW());
   ```
5. **Mapper** converts to TodoResponse:
   - Includes all fields
   - Adds category name
   - Calculates overdue status
6. **Client** receives:
   ```json
   {
     "id": 1,
     "title": "Prepare presentation",
     "description": "Create slides for client meeting",
     "completed": false,
     "priority": "HIGH",
     "dueDate": "2024-01-20",
     "tags": "urgent, client, important",
     "categoryId": 1,
     "categoryName": "Work",
     "createdAt": "2024-01-15T10:30:00",
     "updatedAt": "2024-01-15T10:30:00",
     "overdue": false
   }
   ```

---

## ✅ Compilation Status

**BUILD SUCCESS** - All 15 source files compiled successfully!

### **What Was Created:**

1. ✅ **Priority.java** - Enum for priority levels
2. ✅ **Category.java** - Entity with One-to-Many relationship
3. ✅ **Enhanced Todo.java** - With enum, relationship, column customization
4. ✅ **Updated DTOs** - Support for new fields
5. ✅ **Updated TodoMapper** - Handles enum and relationships
6. ✅ **CategoryRepository** - CRUD for categories

---

## 🎓 Key Learnings

### **1. Enum Mapping**
- **Problem:** String values allow typos and invalid data
- **Solution:** Enum provides type-safety and validation
- **Use:** `@Enumerated(EnumType.STRING)`

### **2. Relationships**
- **Problem:** Storing related data as strings causes duplicates
- **Solution:** Foreign key relationships ensure data integrity
- **Use:** `@ManyToOne`, `@OneToMany`, `@JoinColumn`

### **3. Column Customization**
- **Problem:** Default columns may not be optimized
- **Solution:** Custom constraints, indexes, and naming
- **Use:** `@Column`, `@Table`, `@Index`

### **4. Lifecycle Callbacks**
- **Problem:** Manual timestamp management is error-prone
- **Solution:** Automatic callbacks handle it
- **Use:** `@PrePersist`, `@PreUpdate`

---

## 🚀 Next Steps

You can now:
1. Create categories and assign todos to them
2. Filter todos by priority
3. Query overdue todos
4. Use tags for additional organization

**Test it:**
```bash
./mvnw spring-boot:run
```

This is exactly how enterprise applications structure their entities!