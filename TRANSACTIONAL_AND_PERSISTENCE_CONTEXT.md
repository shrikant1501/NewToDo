# 🔄 @Transactional and Persistence Context in Our Todo Application

Complete explanation of how transactions and persistence context work in our code.

---

## 📍 Where We Use @Transactional

### In Our TodoService.java (Line 14):

```java
@Service
@RequiredArgsConstructor
@Transactional  // ← Applied at CLASS level
public class TodoService {
    
    private final TodoRepository todoRepository;
    
    public List<Todo> getAllTodos() {
        return todoRepository.findAllByOrderByCreatedAtDesc();
    }
    
    public Todo getTodoById(Long id) {
        return todoRepository.findById(id)
                .orElseThrow(() -> new TodoNotFoundException(id));
    }
    
    public Todo createTodo(Todo todo) {
        todo.setId(null);
        if (todo.getCompleted() == null) {
            todo.setCompleted(false);
        }
        return todoRepository.save(todo);
    }
    
    public Todo toggleTodoCompletion(Long id) {
        Todo todo = getTodoById(id);
        todo.setCompleted(!todo.getCompleted());
        return todoRepository.save(todo);
    }
    
    public void deleteTodo(Long id) {
        if (!todoRepository.existsById(id)) {
            throw new TodoNotFoundException(id);
        }
        todoRepository.deleteById(id);
    }
}
```

**Key Point:** We applied `@Transactional` at the **class level**, which means **ALL methods** in TodoService run within a transaction.

---

## 🎯 What is @Transactional?

### Definition:
`@Transactional` is a Spring annotation that manages database transactions automatically.

### What It Does:
1. **Starts a transaction** when method begins
2. **Opens a Persistence Context** (entity manager session)
3. **Commits transaction** if method completes successfully
4. **Rolls back transaction** if exception occurs
5. **Closes Persistence Context** when method ends

---

## 🔄 How @Transactional Works in Our Code

### Example 1: Creating a Todo

```java
@Transactional
public Todo createTodo(Todo todo) {
    todo.setId(null);
    if (todo.getCompleted() == null) {
        todo.setCompleted(false);
    }
    return todoRepository.save(todo);
}
```

**Step-by-Step Execution:**

```
1. Client calls createTodo()
   ↓
2. @Transactional intercepts the call
   ↓
3. Spring starts a database transaction
   BEGIN TRANSACTION
   ↓
4. Spring opens a Persistence Context (EntityManager)
   ↓
5. Method executes:
   - todo.setId(null)
   - Check and set completed flag
   - todoRepository.save(todo)
   ↓
6. JPA adds todo to Persistence Context
   (Not yet in database!)
   ↓
7. Method completes successfully
   ↓
8. @Transactional commits the transaction
   COMMIT TRANSACTION
   ↓
9. SQL INSERT is executed
   INSERT INTO todos (title, completed, ...) VALUES (?, ?, ...)
   ↓
10. Persistence Context is closed
    ↓
11. Todo object returned to client
```

---

### Example 2: Toggle Todo Completion

```java
@Transactional
public Todo toggleTodoCompletion(Long id) {
    Todo todo = getTodoById(id);           // Step 1: Load from DB
    todo.setCompleted(!todo.getCompleted()); // Step 2: Modify in memory
    return todoRepository.save(todo);      // Step 3: Save to DB
}
```

**Detailed Flow:**

```
1. Transaction starts
   BEGIN TRANSACTION
   ↓
2. Persistence Context opens
   ↓
3. getTodoById(id) executes
   SQL: SELECT * FROM todos WHERE id = ?
   ↓
4. Todo entity loaded into Persistence Context
   [Persistence Context]
   Todo(id=1, title="Buy milk", completed=false)
   ↓
5. todo.setCompleted(!todo.getCompleted())
   [Persistence Context - MODIFIED]
   Todo(id=1, title="Buy milk", completed=true)
   ↓
6. todoRepository.save(todo)
   JPA detects changes (dirty checking)
   ↓
7. Method completes
   ↓
8. Transaction commits
   COMMIT TRANSACTION
   ↓
9. SQL UPDATE executed
   UPDATE todos SET completed = true WHERE id = 1
   ↓
10. Persistence Context closed
```

---

### Example 3: Delete Todo with Validation

```java
@Transactional
public void deleteTodo(Long id) {
    if (!todoRepository.existsById(id)) {
        throw new TodoNotFoundException(id);
    }
    todoRepository.deleteById(id);
}
```

**Success Scenario:**

```
1. Transaction starts
   ↓
2. existsById(id) checks database
   SQL: SELECT COUNT(*) FROM todos WHERE id = ?
   Result: 1 (exists)
   ↓
3. deleteById(id) executes
   ↓
4. Method completes successfully
   ↓
5. Transaction commits
   SQL: DELETE FROM todos WHERE id = ?
   ↓
6. Persistence Context closed
```

**Failure Scenario:**

```
1. Transaction starts
   ↓
2. existsById(id) checks database
   SQL: SELECT COUNT(*) FROM todos WHERE id = ?
   Result: 0 (doesn't exist)
   ↓
3. throw new TodoNotFoundException(id)
   ↓
4. Exception thrown!
   ↓
5. @Transactional catches exception
   ↓
6. Transaction ROLLS BACK
   ROLLBACK TRANSACTION
   ↓
7. No changes made to database
   ↓
8. Persistence Context closed
   ↓
9. Exception propagated to controller
```

---

## 🗄️ What is Persistence Context?

### Definition:
The Persistence Context is a **cache** that holds entity objects during a transaction. It's managed by the EntityManager.

### Think of it as:
```
┌─────────────────────────────────────────┐
│        Persistence Context              │
│         (EntityManager)                 │
│                                         │
│  ┌─────────────────────────────────┐   │
│  │  Managed Entities (Cache)       │   │
│  │                                 │   │
│  │  Todo(id=1, title="Buy milk")   │   │
│  │  Todo(id=2, title="Fix bug")    │   │
│  │  Category(id=1, name="Work")    │   │
│  └─────────────────────────────────┘   │
│                                         │
│  - Tracks entity changes                │
│  - Manages entity lifecycle             │
│  - Handles lazy loading                 │
└─────────────────────────────────────────┘
           ↕
    Database
```

---

## 🔍 How Persistence Context Works in Our Code

### Example: Loading and Modifying Todo

```java
@Transactional
public Todo toggleTodoCompletion(Long id) {
    // Step 1: Load entity
    Todo todo = getTodoById(id);
    
    // Step 2: Modify entity
    todo.setCompleted(!todo.getCompleted());
    
    // Step 3: Save entity
    return todoRepository.save(todo);
}
```

**Persistence Context States:**

```
┌──────────────────────────────────────────────────────────┐
│ STEP 1: Load Entity                                      │
├──────────────────────────────────────────────────────────┤
│ SQL: SELECT * FROM todos WHERE id = 1                    │
│                                                          │
│ [Persistence Context]                                    │
│ Todo(id=1, title="Buy milk", completed=false) ← MANAGED │
│                                                          │
│ Entity State: MANAGED (tracked by Persistence Context)  │
└──────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────┐
│ STEP 2: Modify Entity                                    │
├──────────────────────────────────────────────────────────┤
│ todo.setCompleted(true)                                  │
│                                                          │
│ [Persistence Context]                                    │
│ Todo(id=1, title="Buy milk", completed=true) ← DIRTY    │
│                                                          │
│ Entity State: MANAGED + DIRTY (changes detected)        │
│ No SQL executed yet!                                     │
└──────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────┐
│ STEP 3: Save Entity                                      │
├──────────────────────────────────────────────────────────┤
│ todoRepository.save(todo)                                │
│                                                          │
│ JPA performs "Dirty Checking":                           │
│ - Compares current state with original state            │
│ - Detects: completed changed from false to true         │
│                                                          │
│ [Persistence Context]                                    │
│ Todo(id=1, title="Buy milk", completed=true) ← MANAGED  │
│                                                          │
│ Entity State: MANAGED (changes marked for flush)        │
└──────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────┐
│ TRANSACTION COMMIT                                       │
├──────────────────────────────────────────────────────────┤
│ @Transactional commits transaction                       │
│                                                          │
│ SQL: UPDATE todos SET completed = true WHERE id = 1     │
│                                                          │
│ [Persistence Context] - CLOSED                           │
│                                                          │
│ Entity State: DETACHED (no longer tracked)              │
└──────────────────────────────────────────────────────────┘
```

---

## 🎭 Entity Lifecycle States

### In Our Todo Application:

```java
@Transactional
public Todo createTodo(Todo todo) {
    // 1. TRANSIENT STATE
    // Entity exists in memory but not in database or Persistence Context
    todo.setId(null);
    
    // 2. MANAGED STATE
    // After save(), entity is in Persistence Context and tracked
    Todo savedTodo = todoRepository.save(todo);
    
    // 3. Still MANAGED
    // Any changes are automatically detected
    savedTodo.setTitle("Updated title");
    // No need to call save() again! Changes will be flushed on commit
    
    return savedTodo;
    // 4. DETACHED STATE (after transaction commits)
    // Entity no longer tracked by Persistence Context
}
```

**Visual Representation:**

```
┌─────────────┐
│  TRANSIENT  │  New entity, not in DB or Persistence Context
└──────┬──────┘
       │ save()
       ↓
┌─────────────┐
│   MANAGED   │  In Persistence Context, changes tracked
└──────┬──────┘
       │ transaction commit
       ↓
┌─────────────┐
│  DETACHED   │  No longer tracked, but has ID from DB
└──────┬──────┘
       │ merge()
       ↓
┌─────────────┐
│   MANAGED   │  Back in Persistence Context
└─────────────┘
```

---

## 🔄 Dirty Checking in Our Code

### What is Dirty Checking?

JPA automatically detects changes to managed entities and generates UPDATE statements.

### Example from Our Code:

```java
@Transactional
public Todo toggleTodoCompletion(Long id) {
    Todo todo = getTodoById(id);           // Entity becomes MANAGED
    todo.setCompleted(!todo.getCompleted()); // Change detected by dirty checking
    return todoRepository.save(todo);      // Actually, save() is optional here!
}
```

**We could simplify to:**

```java
@Transactional
public Todo toggleTodoCompletion(Long id) {
    Todo todo = getTodoById(id);
    todo.setCompleted(!todo.getCompleted());
    return todo;  // No save() needed! JPA will auto-update on commit
}
```

**Why?** Because the entity is MANAGED, JPA tracks changes automatically!

---

## 🚀 Benefits of @Transactional in Our Code

### 1. Automatic Transaction Management

**Without @Transactional:**
```java
public Todo createTodo(Todo todo) {
    EntityManager em = entityManagerFactory.createEntityManager();
    EntityTransaction tx = em.getTransaction();
    try {
        tx.begin();
        em.persist(todo);
        tx.commit();
        return todo;
    } catch (Exception e) {
        tx.rollback();
        throw e;
    } finally {
        em.close();
    }
}
```

**With @Transactional:**
```java
@Transactional
public Todo createTodo(Todo todo) {
    return todoRepository.save(todo);  // So much simpler!
}
```

---

### 2. Automatic Rollback on Exceptions

```java
@Transactional
public void deleteTodo(Long id) {
    if (!todoRepository.existsById(id)) {
        throw new TodoNotFoundException(id);  // Transaction auto-rolls back
    }
    todoRepository.deleteById(id);
}
```

**What Happens:**
- Exception thrown → Transaction rolls back → No changes to database
- No manual rollback code needed!

---

### 3. Lazy Loading Support

```java
@Transactional
public Todo getTodoById(Long id) {
    Todo todo = todoRepository.findById(id).orElseThrow();
    
    // If Todo has lazy-loaded Category relationship:
    String categoryName = todo.getCategory().getName();  // Works!
    // Because Persistence Context is still open
    
    return todo;
}
```

**Without @Transactional:**
```java
public Todo getTodoById(Long id) {
    Todo todo = todoRepository.findById(id).orElseThrow();
    
    // Persistence Context closed after findById()
    String categoryName = todo.getCategory().getName();  // LazyInitializationException!
    
    return todo;
}
```

---

### 4. Dirty Checking (Automatic Updates)

```java
@Transactional
public Todo updateTodoTitle(Long id, String newTitle) {
    Todo todo = getTodoById(id);
    todo.setTitle(newTitle);
    // No save() needed! JPA detects change and updates on commit
    return todo;
}
```

---

## 🎯 @Transactional Attributes We Could Use

### 1. Read-Only Transactions

```java
@Transactional(readOnly = true)
public List<Todo> getAllTodos() {
    return todoRepository.findAllByOrderByCreatedAtDesc();
}
```

**Benefits:**
- Performance optimization
- Database can optimize read-only queries
- Prevents accidental modifications

---

### 2. Propagation

```java
@Transactional(propagation = Propagation.REQUIRED)  // Default
public Todo createTodo(Todo todo) {
    return todoRepository.save(todo);
}

@Transactional(propagation = Propagation.REQUIRES_NEW)
public void logActivity(String message) {
    // Always creates new transaction, even if one exists
}
```

**Propagation Types:**
- `REQUIRED` (default): Use existing transaction or create new
- `REQUIRES_NEW`: Always create new transaction
- `MANDATORY`: Must have existing transaction
- `SUPPORTS`: Use transaction if exists, otherwise non-transactional
- `NOT_SUPPORTED`: Execute non-transactionally
- `NEVER`: Throw exception if transaction exists

---

### 3. Isolation Level

```java
@Transactional(isolation = Isolation.READ_COMMITTED)
public Todo getTodoById(Long id) {
    return todoRepository.findById(id).orElseThrow();
}
```

**Isolation Levels:**
- `READ_UNCOMMITTED`: Lowest isolation, highest performance
- `READ_COMMITTED`: Default for most databases
- `REPEATABLE_READ`: Prevents non-repeatable reads
- `SERIALIZABLE`: Highest isolation, lowest performance

---

### 4. Timeout

```java
@Transactional(timeout = 30)  // 30 seconds
public void deleteCompletedTodos() {
    List<Todo> completedTodos = todoRepository.findByCompleted(true);
    todoRepository.deleteAll(completedTodos);
}
```

---

### 5. Rollback Rules

```java
@Transactional(rollbackFor = Exception.class)
public Todo createTodo(Todo todo) {
    return todoRepository.save(todo);
}

@Transactional(noRollbackFor = CustomException.class)
public Todo updateTodo(Todo todo) {
    return todoRepository.save(todo);
}
```

**Default Behavior:**
- Rolls back on **RuntimeException** and **Error**
- Does NOT roll back on **checked exceptions**

---

## 🔍 Real-World Flow in Our Application

### Complete Request Flow:

```
1. HTTP Request arrives
   POST /api/todos
   Body: { "title": "Buy milk", "priority": "HIGH" }
   ↓
2. Controller receives request
   TodoController.createTodo(TodoCreateRequest)
   ↓
3. DTO mapped to Entity
   TodoMapper.toEntity(request)
   ↓
4. Service method called
   TodoService.createTodo(todo)
   ↓
5. @Transactional intercepts
   - Starts transaction: BEGIN TRANSACTION
   - Opens Persistence Context
   ↓
6. Method executes
   - todo.setId(null)
   - Check completed flag
   - todoRepository.save(todo)
   ↓
7. Entity added to Persistence Context
   [Persistence Context]
   Todo(id=null, title="Buy milk", priority=HIGH) ← NEW
   ↓
8. Method completes successfully
   ↓
9. @Transactional commits
   - Flushes Persistence Context
   - SQL: INSERT INTO todos (title, priority, ...) VALUES (?, ?, ...)
   - COMMIT TRANSACTION
   - Closes Persistence Context
   ↓
10. Entity returned with generated ID
    Todo(id=1, title="Buy milk", priority=HIGH)
    ↓
11. Entity mapped to DTO
    TodoMapper.toResponse(todo)
    ↓
12. HTTP Response sent
    Status: 201 Created
    Body: { "id": 1, "title": "Buy milk", "priority": "HIGH" }
```

---

## 🎓 Interview Questions About Our Code

### Q1: Why did we put @Transactional at class level?

**Answer:**
We put `@Transactional` at class level in TodoService so that ALL methods run within a transaction. This ensures:
- Automatic transaction management for all operations
- Lazy loading works in all methods
- Consistent behavior across all service methods
- Automatic rollback on any exception

---

### Q2: What happens if we remove @Transactional?

**Answer:**
Without `@Transactional`:
1. **No automatic transaction management** - Each repository call creates its own transaction
2. **Lazy loading fails** - LazyInitializationException when accessing lazy relationships
3. **No dirty checking** - Must explicitly call save() for updates
4. **No automatic rollback** - Must handle rollback manually
5. **Performance issues** - Multiple small transactions instead of one

---

### Q3: In toggleTodoCompletion(), why do we call save()?

**Answer:**
Actually, we don't need to! Because the entity is MANAGED:

```java
@Transactional
public Todo toggleTodoCompletion(Long id) {
    Todo todo = getTodoById(id);
    todo.setCompleted(!todo.getCompleted());
    return todo;  // save() is optional!
}
```

JPA's dirty checking will automatically detect the change and generate UPDATE on commit.

We call `save()` for clarity and to ensure the entity is merged if it was detached.

---

### Q4: What happens in deleteTodo() if todo doesn't exist?

**Answer:**
```java
@Transactional
public void deleteTodo(Long id) {
    if (!todoRepository.existsById(id)) {
        throw new TodoNotFoundException(id);  // Exception thrown
    }
    todoRepository.deleteById(id);
}
```

**Flow:**
1. Transaction starts
2. existsById() returns false
3. TodoNotFoundException thrown
4. @Transactional catches exception
5. Transaction ROLLS BACK automatically
6. No changes to database
7. Exception propagated to GlobalExceptionHandler
8. Returns 404 response to client

---

### Q5: Can we have nested @Transactional methods?

**Answer:**
Yes! In our code:

```java
@Transactional
public Todo toggleTodoCompletion(Long id) {
    Todo todo = getTodoById(id);  // This is also @Transactional!
    todo.setCompleted(!todo.getCompleted());
    return todoRepository.save(todo);
}

@Transactional
public Todo getTodoById(Long id) {
    return todoRepository.findById(id)
            .orElseThrow(() -> new TodoNotFoundException(id));
}
```

**What Happens:**
- `toggleTodoCompletion()` starts a transaction
- `getTodoById()` joins the existing transaction (default propagation)
- Both methods share the same Persistence Context
- Single transaction for the entire operation

---

## 📊 Performance Implications

### With @Transactional (Our Code):

```java
@Transactional
public void updateMultipleTodos() {
    Todo todo1 = getTodoById(1L);
    todo1.setTitle("Updated 1");
    
    Todo todo2 = getTodoById(2L);
    todo2.setTitle("Updated 2");
    
    Todo todo3 = getTodoById(3L);
    todo3.setTitle("Updated 3");
    
    // All updates happen in ONE transaction
    // SQL executed on commit:
    // UPDATE todos SET title = 'Updated 1' WHERE id = 1
    // UPDATE todos SET title = 'Updated 2' WHERE id = 2
    // UPDATE todos SET title = 'Updated 3' WHERE id = 3
}
```

**Database Calls:** 1 transaction, 3 SELECTs, 3 UPDATEs

---

### Without @Transactional:

```java
public void updateMultipleTodos() {
    Todo todo1 = getTodoById(1L);  // Transaction 1
    todo1.setTitle("Updated 1");
    todoRepository.save(todo1);    // Transaction 2
    
    Todo todo2 = getTodoById(2L);  // Transaction 3
    todo2.setTitle("Updated 2");
    todoRepository.save(todo2);    // Transaction 4
    
    Todo todo3 = getTodoById(3L);  // Transaction 5
    todo3.setTitle("Updated 3");
    todoRepository.save(todo3);    // Transaction 6
}
```

**Database Calls:** 6 transactions, 3 SELECTs, 3 UPDATEs

**Performance Impact:** 6x more transaction overhead!

---

## 🎯 Best Practices We Follow

### ✅ 1. Service Layer Transactions
```java
@Service
@Transactional  // ← Correct place
public class TodoService { }
```

### ✅ 2. Read-Only for Queries
```java
@Transactional(readOnly = true)
public List<Todo> getAllTodos() {
    return todoRepository.findAllByOrderByCreatedAtDesc();
}
```

### ✅ 3. Let Exceptions Rollback
```java
@Transactional
public void deleteTodo(Long id) {
    if (!todoRepository.existsById(id)) {
        throw new TodoNotFoundException(id);  // Auto-rollback
    }
    todoRepository.deleteById(id);
}
```

### ✅ 4. Keep Transactions Short
```java
@Transactional
public Todo createTodo(Todo todo) {
    // Quick operation
    return todoRepository.save(todo);
}
```

---

## 🚫 Common Mistakes to Avoid

### ❌ 1. @Transactional on Private Methods
```java
@Transactional  // Won't work!
private Todo createTodo(Todo todo) {
    return todoRepository.save(todo);
}
```

**Why:** Spring uses proxies, which can't intercept private methods.

---

### ❌ 2. Catching Exceptions Without Rethrowing
```java
@Transactional
public void deleteTodo(Long id) {
    try {
        todoRepository.deleteById(id);
    } catch (Exception e) {
        // Swallowing exception - transaction won't rollback!
        log.error("Error", e);
    }
}
```

**Fix:**
```java
@Transactional
public void deleteTodo(Long id) {
    try {
        todoRepository.deleteById(id);
    } catch (Exception e) {
        log.error("Error", e);
        throw e;  // Rethrow to trigger rollback
    }
}
```

---

### ❌ 3. Accessing Lazy Relationships Outside Transaction
```java
public Todo getTodo(Long id) {
    return todoRepository.findById(id).orElseThrow();
}

// In controller
Todo todo = todoService.getTodo(1L);
String categoryName = todo.getCategory().getName();  // LazyInitializationException!
```

**Fix:**
```java
@Transactional
public Todo getTodo(Long id) {
    return todoRepository.findById(id).orElseThrow();
}
```

---

## 📝 Summary

### In Our Todo Application:

1. **@Transactional at class level** - All TodoService methods are transactional
2. **Automatic transaction management** - No manual begin/commit/rollback
3. **Persistence Context** - Entities are tracked and changes auto-detected
4. **Dirty checking** - No need to call save() for updates to managed entities
5. **Automatic rollback** - Exceptions trigger rollback automatically
6. **Lazy loading support** - Can access lazy relationships within transaction
7. **Performance optimization** - Single transaction for multiple operations

### Key Takeaways:

- `@Transactional` = Automatic transaction + Persistence Context management
- Persistence Context = Cache that tracks entity changes
- Managed entities = Automatically updated on commit (dirty checking)
- Exception = Automatic rollback
- Class-level annotation = All methods are transactional

---

**This is how @Transactional and Persistence Context work together to make our Todo application robust, efficient, and easy to maintain!** 🚀