# Complete POST Request Flow Explanation

Let me walk you through **EXACTLY** what happens when a client creates a todo, step by step.

---

## 🎯 The Request Journey

### **Step 1: Client Sends JSON**
```bash
POST http://localhost:8080/api/todos
Content-Type: application/json

{
  "title": "Buy groceries",
  "description": "Milk, eggs, bread"
}
```

---

### **Step 2: Spring Receives Request**

Spring Boot's **Jackson** library automatically converts JSON to Java object:

```
JSON String → TodoCreateRequest object
```

**What happens:**
```java
// Spring creates this object from JSON
TodoCreateRequest request = new TodoCreateRequest();
request.setTitle("Buy groceries");
request.setDescription("Milk, eggs, bread");
```

**Why TodoCreateRequest?** Because controller method signature says:
```java
public ResponseEntity<TodoResponse> createTodo(@RequestBody TodoCreateRequest request)
                                                    ↑
                                    Spring knows to convert JSON to this type
```

---

### **Step 3: Validation Happens (@Valid)**

Before entering the controller method, Spring validates the object:

```java
@PostMapping
public ResponseEntity<TodoResponse> createTodo(@Valid @RequestBody TodoCreateRequest request) {
                                                  ↑
                                    This triggers validation
```

**Validation checks (from TodoCreateRequest.java):**
```java
@NotBlank(message = "Title is required")
@Size(min = 1, max = 100, message = "Title must be between 1 and 100 characters")
private String title;

@Size(max = 1000, message = "Description cannot exceed 1000 characters")
private String description;
```

**If validation FAILS:**
```json
// Client sent: {"title": "", "description": "Test"}
Response: 400 Bad Request
{
  "title": "Title is required"
}
```

**If validation PASSES:** Continue to Step 4

---

### **Step 4: Controller Receives Valid DTO**

Now we're inside the controller method:

```java
@PostMapping
public ResponseEntity<TodoResponse> createTodo(@Valid @RequestBody TodoCreateRequest request) {
    // request object is now validated and ready to use
    // request.title = "Buy groceries"
    // request.description = "Milk, eggs, bread"
```

**Current state:**
```
TodoCreateRequest {
    title: "Buy groceries"
    description: "Milk, eggs, bread"
}
```

**Notice:** No `id`, `createdAt`, `updatedAt`, or `completed` fields!
Client **cannot** send these fields because `TodoCreateRequest` doesn't have them.

---

### **Step 5: Mapper Converts DTO → Entity**

```java
Todo todo = todoMapper.toEntity(request);
```

**Inside TodoMapper.toEntity():**
```java
public Todo toEntity(TodoCreateRequest request) {
    Todo todo = new Todo();                          // Create empty entity
    todo.setTitle(request.getTitle());               // Copy title
    todo.setDescription(request.getDescription());   // Copy description
    todo.setCompleted(false);                        // Set default value
    return todo;                                     // Return entity
}
```

**After mapping:**
```
Todo Entity {
    id: null                           ← Not set yet (database will generate)
    title: "Buy groceries"             ← From DTO
    description: "Milk, eggs, bread"   ← From DTO
    completed: false                   ← Default value from mapper
    createdAt: null                    ← Not set yet (JPA will set)
    updatedAt: null                    ← Not set yet (JPA will set)
}
```

**Why mapping?**
- DTO has only 2 fields (title, description)
- Entity has 6 fields (id, title, description, completed, createdAt, updatedAt)
- Mapper fills in the missing fields with defaults

---

### **Step 6: Service Saves Entity**

```java
Todo createdTodo = todoService.createTodo(todo);
```

**Inside TodoService.createTodo():**
```java
public Todo createTodo(Todo todo) {
    todo.setId(null);                    // Ensure it's new (safety check)
    if (todo.getCompleted() == null) {   // Double-check completed
        todo.setCompleted(false);
    }
    return todoRepository.save(todo);    // Save to database
}
```

---

### **Step 7: JPA Lifecycle Hook Triggers**

**Before saving to database**, JPA calls `@PrePersist` method:

```java
@PrePersist
protected void onCreate() {
    createdAt = LocalDateTime.now();   // Set creation time
    updatedAt = LocalDateTime.now();   // Set update time
}
```

**Entity now has:**
```
Todo Entity {
    id: null                           ← Still null
    title: "Buy groceries"
    description: "Milk, eggs, bread"
    completed: false
    createdAt: 2024-01-15T10:30:00     ← JPA set this
    updatedAt: 2024-01-15T10:30:00     ← JPA set this
}
```

---

### **Step 8: Database Saves Record**

```java
todoRepository.save(todo);
```

**JPA generates SQL:**
```sql
INSERT INTO todos (title, description, completed, created_at, updated_at)
VALUES ('Buy groceries', 'Milk, eggs, bread', false, '2024-01-15 10:30:00', '2024-01-15 10:30:00');
```

**Database auto-generates ID:**
```
Todo Entity {
    id: 1                              ← Database generated this!
    title: "Buy groceries"
    description: "Milk, eggs, bread"
    completed: false
    createdAt: 2024-01-15T10:30:00
    updatedAt: 2024-01-15T10:30:00
}
```

**This entity is returned to the service, then to the controller.**

---

### **Step 9: Mapper Converts Entity → Response DTO**

```java
TodoResponse response = todoMapper.toResponse(createdTodo);
```

**Inside TodoMapper.toResponse():**
```java
public TodoResponse toResponse(Todo todo) {
    TodoResponse response = new TodoResponse();
    response.setId(todo.getId());                     // Copy id
    response.setTitle(todo.getTitle());               // Copy title
    response.setDescription(todo.getDescription());   // Copy description
    response.setCompleted(todo.getCompleted());       // Copy completed
    response.setCreatedAt(todo.getCreatedAt());       // Copy createdAt
    response.setUpdatedAt(todo.getUpdatedAt());       // Copy updatedAt
    return response;
}
```

**After mapping:**
```
TodoResponse {
    id: 1
    title: "Buy groceries"
    description: "Milk, eggs, bread"
    completed: false
    createdAt: 2024-01-15T10:30:00
    updatedAt: 2024-01-15T10:30:00
}
```

**Why convert to Response DTO?**
- We control exactly what client sees
- Can hide sensitive fields if needed
- Can add computed fields (e.g., `isOverdue`)
- API structure independent from database

---

### **Step 10: Controller Returns Response**

```java
return ResponseEntity.status(HttpStatus.CREATED).body(response);
```

**Spring converts TodoResponse → JSON:**
```
TodoResponse object → JSON String
```

**HTTP Response:**
```
HTTP/1.1 201 Created
Content-Type: application/json

{
  "id": 1,
  "title": "Buy groceries",
  "description": "Milk, eggs, bread",
  "completed": false,
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

---

## 📊 Visual Flow Diagram

```
┌─────────────────────────────────────────────────────────────┐
│ 1. CLIENT                                                   │
│    POST /api/todos                                          │
│    {"title": "Buy groceries", "description": "..."}         │
└────────────────────────┬────────────────────────────────────┘
                         │ JSON
                         ↓
┌─────────────────────────────────────────────────────────────┐
│ 2. SPRING FRAMEWORK                                         │
│    JSON → TodoCreateRequest object                          │
└────────────────────────┬────────────────────────────────────┘
                         │ TodoCreateRequest
                         ↓
┌─────────────────────────────────────────────────────────────┐
│ 3. VALIDATION (@Valid)                                      │
│    Check @NotBlank, @Size annotations                       │
│    ✅ Pass → Continue   ❌ Fail → Return 400 error          │
└────────────────────────┬────────────────────────────────────┘
                         │ Validated TodoCreateRequest
                         ↓
┌─────────────────────────────────────────────────────────────┐
│ 4. CONTROLLER (TodoController.createTodo)                   │
│    Receives validated DTO                                   │
└────────────────────────┬────────────────────────────────────┘
                         │ TodoCreateRequest
                         ↓
┌─────────────────────────────────────────────────────────────┐
│ 5. MAPPER (TodoMapper.toEntity)                             │
│    TodoCreateRequest → Todo Entity                          │
│    - Copy title, description                                │
│    - Set completed = false                                  │
└────────────────────────┬────────────────────────────────────┘
                         │ Todo Entity (incomplete)
                         ↓
┌─────────────────────────────────────────────────────────────┐
│ 6. SERVICE (TodoService.createTodo)                         │
│    - Ensure id = null                                       │
│    - Call repository.save()                                 │
└────────────────────────┬────────────────────────────────────┘
                         │ Todo Entity
                         ↓
┌─────────────────────────────────────────────────────────────┐
│ 7. JPA LIFECYCLE (@PrePersist)                              │
│    - Set createdAt = now()                                  │
│    - Set updatedAt = now()                                  │
└────────────────────────┬────────────────────────────────────┘
                         │ Todo Entity (complete)
                         ↓
┌─────────────────────────────────────────────────────────────┐
│ 8. DATABASE                                                 │
│    INSERT INTO todos (...)                                  │
│    Returns entity with generated ID                         │
└────────────────────────┬────────────────────────────────────┘
                         │ Todo Entity (with ID)
                         ↓
┌─────────────────────────────────────────────────────────────┐
│ 9. MAPPER (TodoMapper.toResponse)                           │
│    Todo Entity → TodoResponse                               │
│    - Copy all fields                                        │
└────────────────────────┬────────────────────────────────────┘
                         │ TodoResponse
                         ↓
┌─────────────────────────────────────────────────────────────┐
│ 10. CONTROLLER                                              │
│     Return ResponseEntity with TodoResponse                 │
└────────────────────────┬────────────────────────────────────┘
                         │ TodoResponse
                         ↓
┌─────────────────────────────────────────────────────────────┐
│ 11. SPRING FRAMEWORK                                        │
│     TodoResponse → JSON                                     │
└────────────────────────┬────────────────────────────────────┘
                         │ JSON
                         ↓
┌─────────────────────────────────────────────────────────────┐
│ 12. CLIENT                                                  │
│     Receives: 201 Created                                   │
│     {"id": 1, "title": "...", ...}                          │
└─────────────────────────────────────────────────────────────┘
```

---

## 🔑 Key Concepts

### **Why 3 Different Objects?**

1. **TodoCreateRequest** (Input DTO)
   - What client sends
   - Only fields client should provide
   - Has validation rules

2. **Todo** (Entity)
   - Internal representation
   - Maps to database table
   - Has all fields including system-managed ones

3. **TodoResponse** (Output DTO)
   - What client receives
   - Can be different from entity
   - Controls what data is exposed

### **Why Mapping?**

**Without Mapper:**
```java
// ❌ Bad: Exposing entity directly
@PostMapping
public Todo create(@RequestBody Todo todo) {
    return todoRepository.save(todo);
}
// Problem: Client can set id, createdAt, etc.
```

**With Mapper:**
```java
// ✅ Good: Using DTOs
@PostMapping
public TodoResponse create(@Valid @RequestBody TodoCreateRequest request) {
    Todo todo = mapper.toEntity(request);      // Control what goes in
    Todo saved = service.save(todo);
    return mapper.toResponse(saved);           // Control what goes out
}
// Benefit: Full control over input/output
```

---

## 🎓 Summary

**The flow is:**
```
Client JSON 
  → TodoCreateRequest (validated)
  → Todo Entity (mapped)
  → Database (saved)
  → Todo Entity (with ID)
  → TodoResponse (mapped)
  → Client JSON
```

**Each layer has a purpose:**
- **DTO**: API contract with client
- **Entity**: Database representation
- **Mapper**: Translation between layers

This separation allows you to change the API without changing the database, or vice versa!