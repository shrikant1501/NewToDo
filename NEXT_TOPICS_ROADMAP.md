# 🚀 Next Topics to Add to Your Todo Project

Based on your current progress, here are the next topics you can implement to make your project production-ready and interview-impressive!

---

## 📊 Current Project Status

### ✅ What We Have:
- ✅ Basic CRUD operations
- ✅ Entity relationships (Category ↔ Todo)
- ✅ DTOs for clean API design
- ✅ Exception handling
- ✅ Bean validation
- ✅ Enums (Priority)
- ✅ Column customization (indexes, constraints)
- ✅ Lifecycle callbacks (@PrePersist, @PreUpdate)
- ✅ @Transactional for transaction management

---

## 🎯 Recommended Next Topics (Prioritized)

### 🔥 **TIER 1: Essential Production Features** (Implement First)

#### 1. **Spring Security & Authentication** 🔐
**Why:** Every production app needs security!

**What to Add:**
- User registration and login
- JWT token-based authentication
- Password encryption (BCrypt)
- Role-based access control (USER, ADMIN)
- Secure endpoints

**Implementation:**
```java
@Entity
public class User {
    @Id
    private Long id;
    private String username;
    private String password;  // BCrypt encrypted
    private String email;
    
    @Enumerated(EnumType.STRING)
    private Role role;  // USER, ADMIN
    
    @OneToMany(mappedBy = "user")
    private List<Todo> todos;
}

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request);
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request);
}
```

**Benefits:**
- ✅ Secure your API
- ✅ User-specific todos
- ✅ Learn JWT tokens
- ✅ Interview favorite topic!

**Difficulty:** Medium
**Time:** 3-4 hours
**Interview Value:** ⭐⭐⭐⭐⭐

---

#### 2. **Pagination & Sorting** 📄
**Why:** Handle large datasets efficiently!

**What to Add:**
- Pageable support in repositories
- Sorting by multiple fields
- Custom page response DTOs

**Implementation:**
```java
@RestController
@RequestMapping("/api/todos")
public class TodoController {
    
    @GetMapping
    public ResponseEntity<Page<TodoResponse>> getAllTodos(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "createdAt,desc") String[] sort
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(orders));
        Page<Todo> todos = todoService.getAllTodos(pageable);
        return ResponseEntity.ok(todos.map(todoMapper::toResponse));
    }
}

@Service
public class TodoService {
    public Page<Todo> getAllTodos(Pageable pageable) {
        return todoRepository.findAll(pageable);
    }
}
```

**Benefits:**
- ✅ Handle thousands of todos
- ✅ Better performance
- ✅ Professional API design
- ✅ Common interview question

**Difficulty:** Easy
**Time:** 1-2 hours
**Interview Value:** ⭐⭐⭐⭐

---

#### 3. **Advanced Search & Filtering** 🔍
**Why:** Users need to find specific todos quickly!

**What to Add:**
- Search by multiple criteria
- Filter by priority, category, status, date range
- Specification pattern for dynamic queries

**Implementation:**
```java
@RestController
@RequestMapping("/api/todos")
public class TodoController {
    
    @GetMapping("/search")
    public ResponseEntity<List<TodoResponse>> searchTodos(
        @RequestParam(required = false) String title,
        @RequestParam(required = false) Priority priority,
        @RequestParam(required = false) Long categoryId,
        @RequestParam(required = false) Boolean completed,
        @RequestParam(required = false) LocalDate dueDateFrom,
        @RequestParam(required = false) LocalDate dueDateTo
    ) {
        List<Todo> todos = todoService.searchTodos(
            title, priority, categoryId, completed, dueDateFrom, dueDateTo
        );
        return ResponseEntity.ok(todos.stream()
            .map(todoMapper::toResponse)
            .collect(Collectors.toList()));
    }
}

// Using Specification pattern
public class TodoSpecification {
    public static Specification<Todo> hasTitle(String title) {
        return (root, query, cb) -> 
            title == null ? null : cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase() + "%");
    }
    
    public static Specification<Todo> hasPriority(Priority priority) {
        return (root, query, cb) -> 
            priority == null ? null : cb.equal(root.get("priority"), priority);
    }
    
    public static Specification<Todo> hasCategory(Long categoryId) {
        return (root, query, cb) -> 
            categoryId == null ? null : cb.equal(root.get("category").get("id"), categoryId);
    }
}
```

**Benefits:**
- ✅ Powerful search capabilities
- ✅ Learn JPA Specifications
- ✅ Dynamic query building
- ✅ Great for interviews

**Difficulty:** Medium
**Time:** 2-3 hours
**Interview Value:** ⭐⭐⭐⭐⭐

---

#### 4. **API Documentation with Swagger/OpenAPI** 📚
**Why:** Professional APIs need documentation!

**What to Add:**
- Swagger UI for interactive API testing
- Auto-generated API documentation
- Request/response examples

**Implementation:**
```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.2.0</version>
</dependency>
```

```java
@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI todoOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Todo API")
                .description("Todo Management REST API")
                .version("1.0.0"));
    }
}

@RestController
@RequestMapping("/api/todos")
@Tag(name = "Todo", description = "Todo management APIs")
public class TodoController {
    
    @Operation(summary = "Get all todos", description = "Returns list of all todos")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public ResponseEntity<List<TodoResponse>> getAllTodos() {
        // ...
    }
}
```

**Access:** http://localhost:8080/swagger-ui.html

**Benefits:**
- ✅ Interactive API testing
- ✅ Auto-generated docs
- ✅ Professional presentation
- ✅ Easy for frontend developers

**Difficulty:** Easy
**Time:** 1 hour
**Interview Value:** ⭐⭐⭐

---

### 🔥 **TIER 2: Advanced Features** (Implement After Tier 1)

#### 5. **Caching with Redis/Spring Cache** ⚡
**Why:** Improve performance dramatically!

**What to Add:**
- Cache frequently accessed data
- Cache eviction strategies
- Redis integration (optional)

**Implementation:**
```java
@Configuration
@EnableCaching
public class CacheConfig {
    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("todos", "categories");
    }
}

@Service
@Transactional
public class TodoService {
    
    @Cacheable(value = "todos", key = "#id")
    public Todo getTodoById(Long id) {
        return todoRepository.findById(id)
            .orElseThrow(() -> new TodoNotFoundException(id));
    }
    
    @CacheEvict(value = "todos", key = "#result.id")
    public Todo updateTodo(Todo todo) {
        return todoRepository.save(todo);
    }
    
    @CacheEvict(value = "todos", allEntries = true)
    public Todo createTodo(Todo todo) {
        return todoRepository.save(todo);
    }
}
```

**Benefits:**
- ✅ 10-100x faster reads
- ✅ Reduce database load
- ✅ Learn caching strategies
- ✅ Production-ready optimization

**Difficulty:** Medium
**Time:** 2-3 hours
**Interview Value:** ⭐⭐⭐⭐⭐

---

#### 6. **Logging with SLF4J & Logback** 📝
**Why:** Debug and monitor your application!

**What to Add:**
- Structured logging
- Different log levels (DEBUG, INFO, WARN, ERROR)
- Log to file and console
- Request/response logging

**Implementation:**
```java
@Service
@Slf4j  // Lombok annotation
@Transactional
public class TodoService {
    
    public Todo createTodo(Todo todo) {
        log.info("Creating new todo: {}", todo.getTitle());
        
        try {
            Todo savedTodo = todoRepository.save(todo);
            log.info("Todo created successfully with ID: {}", savedTodo.getId());
            return savedTodo;
        } catch (Exception e) {
            log.error("Error creating todo: {}", todo.getTitle(), e);
            throw e;
        }
    }
    
    public Todo getTodoById(Long id) {
        log.debug("Fetching todo with ID: {}", id);
        return todoRepository.findById(id)
            .orElseThrow(() -> {
                log.warn("Todo not found with ID: {}", id);
                return new TodoNotFoundException(id);
            });
    }
}

// Request logging interceptor
@Component
@Slf4j
public class RequestLoggingInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        log.info("Request: {} {}", request.getMethod(), request.getRequestURI());
        return true;
    }
}
```

**Benefits:**
- ✅ Debug issues easily
- ✅ Monitor application health
- ✅ Production troubleshooting
- ✅ Professional practice

**Difficulty:** Easy
**Time:** 1-2 hours
**Interview Value:** ⭐⭐⭐

---

#### 7. **Unit & Integration Testing** 🧪
**Why:** Ensure code quality and prevent bugs!

**What to Add:**
- JUnit 5 tests
- Mockito for mocking
- Integration tests with @SpringBootTest
- Test coverage

**Implementation:**
```java
@ExtendWith(MockitoExtension.class)
class TodoServiceTest {
    
    @Mock
    private TodoRepository todoRepository;
    
    @InjectMocks
    private TodoService todoService;
    
    @Test
    void createTodo_ShouldReturnSavedTodo() {
        // Given
        Todo todo = new Todo();
        todo.setTitle("Test Todo");
        
        when(todoRepository.save(any(Todo.class))).thenReturn(todo);
        
        // When
        Todo result = todoService.createTodo(todo);
        
        // Then
        assertNotNull(result);
        assertEquals("Test Todo", result.getTitle());
        verify(todoRepository, times(1)).save(todo);
    }
    
    @Test
    void getTodoById_WhenNotFound_ShouldThrowException() {
        // Given
        when(todoRepository.findById(1L)).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(TodoNotFoundException.class, () -> {
            todoService.getTodoById(1L);
        });
    }
}

@SpringBootTest
@AutoConfigureMockMvc
class TodoControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    void createTodo_ShouldReturn201() throws Exception {
        TodoCreateRequest request = new TodoCreateRequest();
        request.setTitle("Test Todo");
        request.setPriority(Priority.HIGH);
        
        mockMvc.perform(post("/api/todos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.title").value("Test Todo"));
    }
}
```

**Benefits:**
- ✅ Catch bugs early
- ✅ Refactor with confidence
- ✅ Professional development
- ✅ Required in most companies

**Difficulty:** Medium
**Time:** 4-5 hours
**Interview Value:** ⭐⭐⭐⭐⭐

---

#### 8. **File Upload/Download** 📁
**Why:** Handle attachments for todos!

**What to Add:**
- Upload files (images, documents)
- Store file metadata
- Download files
- File validation

**Implementation:**
```java
@Entity
public class TodoAttachment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String fileName;
    private String fileType;
    private Long fileSize;
    private String filePath;
    
    @ManyToOne
    @JoinColumn(name = "todo_id")
    private Todo todo;
    
    private LocalDateTime uploadedAt;
}

@RestController
@RequestMapping("/api/todos/{todoId}/attachments")
public class AttachmentController {
    
    @PostMapping
    public ResponseEntity<AttachmentResponse> uploadFile(
        @PathVariable Long todoId,
        @RequestParam("file") MultipartFile file
    ) {
        AttachmentResponse response = attachmentService.uploadFile(todoId, file);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{attachmentId}")
    public ResponseEntity<Resource> downloadFile(
        @PathVariable Long todoId,
        @PathVariable Long attachmentId
    ) {
        Resource resource = attachmentService.downloadFile(attachmentId);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
            .body(resource);
    }
}
```

**Benefits:**
- ✅ Handle file uploads
- ✅ Learn MultipartFile
- ✅ File storage strategies
- ✅ Common requirement

**Difficulty:** Medium
**Time:** 3-4 hours
**Interview Value:** ⭐⭐⭐⭐

---

### 🔥 **TIER 3: Expert Features** (Advanced Topics)

#### 9. **Scheduled Tasks & Background Jobs** ⏰
**Why:** Automate recurring tasks!

**What to Add:**
- Delete old completed todos
- Send reminder notifications
- Generate reports
- Cleanup tasks

**Implementation:**
```java
@Configuration
@EnableScheduling
public class SchedulingConfig {
}

@Component
@Slf4j
public class TodoScheduledTasks {
    
    @Autowired
    private TodoService todoService;
    
    // Run every day at midnight
    @Scheduled(cron = "0 0 0 * * *")
    public void deleteOldCompletedTodos() {
        log.info("Starting scheduled task: Delete old completed todos");
        
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<Todo> oldTodos = todoRepository.findCompletedBefore(thirtyDaysAgo);
        
        todoRepository.deleteAll(oldTodos);
        log.info("Deleted {} old completed todos", oldTodos.size());
    }
    
    // Run every hour
    @Scheduled(fixedRate = 3600000)
    public void sendOverdueReminders() {
        log.info("Checking for overdue todos");
        
        List<Todo> overdueTodos = todoRepository.findOverdueTodos(LocalDateTime.now());
        // Send notifications
    }
}
```

**Benefits:**
- ✅ Automate maintenance
- ✅ Background processing
- ✅ Learn scheduling
- ✅ Production feature

**Difficulty:** Easy-Medium
**Time:** 2-3 hours
**Interview Value:** ⭐⭐⭐⭐

---

#### 10. **Email Notifications** 📧
**Why:** Keep users informed!

**What to Add:**
- Send email on todo creation
- Reminder emails for due dates
- Email templates
- Async email sending

**Implementation:**
```java
@Service
@Slf4j
public class EmailService {
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Async
    public void sendTodoCreatedEmail(User user, Todo todo) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(user.getEmail());
            message.setSubject("New Todo Created: " + todo.getTitle());
            message.setText("Your todo has been created successfully.\n\n" +
                "Title: " + todo.getTitle() + "\n" +
                "Priority: " + todo.getPriority() + "\n" +
                "Due Date: " + todo.getDueDate());
            
            mailSender.send(message);
            log.info("Email sent to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send email", e);
        }
    }
    
    @Async
    public void sendDueReminderEmail(User user, Todo todo) {
        // Send reminder email
    }
}

@Configuration
@EnableAsync
public class AsyncConfig {
    @Bean
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("email-");
        executor.initialize();
        return executor;
    }
}
```

**Benefits:**
- ✅ User engagement
- ✅ Learn async processing
- ✅ Email integration
- ✅ Professional feature

**Difficulty:** Medium
**Time:** 3-4 hours
**Interview Value:** ⭐⭐⭐⭐

---

#### 11. **Audit Trail & History** 📜
**Why:** Track who changed what and when!

**What to Add:**
- Track all changes to todos
- Store old values
- User who made changes
- Timestamp of changes

**Implementation:**
```java
@Entity
public class TodoAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long todoId;
    private String action;  // CREATE, UPDATE, DELETE
    private String fieldName;
    private String oldValue;
    private String newValue;
    private String changedBy;
    private LocalDateTime changedAt;
}

@Component
public class TodoAuditListener {
    
    @Autowired
    private TodoAuditRepository auditRepository;
    
    @PostPersist
    public void afterCreate(Todo todo) {
        TodoAudit audit = new TodoAudit();
        audit.setTodoId(todo.getId());
        audit.setAction("CREATE");
        audit.setChangedBy(getCurrentUser());
        audit.setChangedAt(LocalDateTime.now());
        auditRepository.save(audit);
    }
    
    @PostUpdate
    public void afterUpdate(Todo todo) {
        // Track changes
    }
}

@RestController
@RequestMapping("/api/todos/{todoId}/history")
public class TodoHistoryController {
    
    @GetMapping
    public ResponseEntity<List<TodoAuditResponse>> getHistory(@PathVariable Long todoId) {
        List<TodoAudit> history = auditService.getTodoHistory(todoId);
        return ResponseEntity.ok(history.stream()
            .map(auditMapper::toResponse)
            .collect(Collectors.toList()));
    }
}
```

**Benefits:**
- ✅ Complete audit trail
- ✅ Compliance requirement
- ✅ Debug changes
- ✅ Enterprise feature

**Difficulty:** Hard
**Time:** 4-5 hours
**Interview Value:** ⭐⭐⭐⭐⭐

---

#### 12. **Rate Limiting** 🚦
**Why:** Prevent API abuse!

**What to Add:**
- Limit requests per user
- Different limits for different endpoints
- Rate limit headers
- 429 Too Many Requests response

**Implementation:**
```java
@Component
public class RateLimitInterceptor implements HandlerInterceptor {
    
    private final Map<String, List<Long>> requestCounts = new ConcurrentHashMap<>();
    private static final int MAX_REQUESTS = 100;
    private static final long TIME_WINDOW = 60000; // 1 minute
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String clientId = getClientId(request);
        
        if (isRateLimitExceeded(clientId)) {
            response.setStatus(429);
            return false;
        }
        
        recordRequest(clientId);
        return true;
    }
    
    private boolean isRateLimitExceeded(String clientId) {
        List<Long> timestamps = requestCounts.getOrDefault(clientId, new ArrayList<>());
        long now = System.currentTimeMillis();
        
        // Remove old timestamps
        timestamps.removeIf(timestamp -> now - timestamp > TIME_WINDOW);
        
        return timestamps.size() >= MAX_REQUESTS;
    }
}
```

**Benefits:**
- ✅ Prevent abuse
- ✅ Protect resources
- ✅ Fair usage
- ✅ Production security

**Difficulty:** Medium
**Time:** 2-3 hours
**Interview Value:** ⭐⭐⭐⭐

---

## 📋 Implementation Priority Roadmap

### **Phase 1: Core Features** (Week 1-2)
1. ✅ Pagination & Sorting (Easy, High Value)
2. ✅ API Documentation (Easy, High Value)
3. ✅ Logging (Easy, Essential)

### **Phase 2: Security & Search** (Week 3-4)
4. ✅ Spring Security & JWT (Medium, Critical)
5. ✅ Advanced Search & Filtering (Medium, High Value)

### **Phase 3: Performance & Quality** (Week 5-6)
6. ✅ Caching (Medium, High Performance)
7. ✅ Unit & Integration Testing (Medium, Essential)

### **Phase 4: Advanced Features** (Week 7-8)
8. ✅ File Upload/Download (Medium, Useful)
9. ✅ Scheduled Tasks (Easy-Medium, Automation)
10. ✅ Email Notifications (Medium, Engagement)

### **Phase 5: Enterprise Features** (Week 9-10)
11. ✅ Audit Trail (Hard, Enterprise)
12. ✅ Rate Limiting (Medium, Security)

---

## 🎯 Quick Wins (Implement Today!)

### 1. **Pagination** (1-2 hours)
- Easiest to implement
- Immediate value
- Professional API

### 2. **Swagger Documentation** (1 hour)
- Just add dependency
- Auto-generated docs
- Great for testing

### 3. **Logging** (1-2 hours)
- Add @Slf4j
- Log important operations
- Debug easily

---

## 💡 Interview-Focused Topics

### **Must-Have for Interviews:**
1. ⭐⭐⭐⭐⭐ Spring Security & JWT
2. ⭐⭐⭐⭐⭐ Unit & Integration Testing
3. ⭐⭐⭐⭐⭐ Caching
4. ⭐⭐⭐⭐⭐ Advanced Search (Specifications)
5. ⭐⭐⭐⭐⭐ Audit Trail

### **Good to Have:**
- ⭐⭐⭐⭐ Pagination & Sorting
- ⭐⭐⭐⭐ File Upload
- ⭐⭐⭐⭐ Scheduled Tasks
- ⭐⭐⭐⭐ Email Notifications
- ⭐⭐⭐⭐ Rate Limiting

### **Nice to Have:**
- ⭐⭐⭐ API Documentation
- ⭐⭐⭐ Logging

---

## 🚀 My Recommendation

### **Start with these 3 (This Weekend!):**

1. **Pagination & Sorting** (Saturday Morning - 2 hours)
   - Easy to implement
   - Immediate professional look
   - Common interview question

2. **Swagger Documentation** (Saturday Afternoon - 1 hour)
   - Just add dependency
   - Makes testing easier
   - Impresses interviewers

3. **Logging** (Sunday Morning - 2 hours)
   - Add @Slf4j everywhere
   - Log important operations
   - Professional practice

### **Then move to (Next Week):**

4. **Spring Security & JWT** (3-4 hours)
   - Most important for interviews
   - Secure your API
   - Learn authentication

5. **Advanced Search** (2-3 hours)
   - Powerful feature
   - Learn JPA Specifications
   - Great for interviews

---

## 📚 Learning Resources

### **For Each Topic:**

1. **Spring Security:**
   - Spring Security Official Docs
   - JWT.io for token understanding
   - Baeldung Spring Security tutorials

2. **Testing:**
   - JUnit 5 User Guide
   - Mockito Documentation
   - Spring Boot Testing Guide

3. **Caching:**
   - Spring Cache Abstraction
   - Redis Documentation
   - Caching Best Practices

4. **JPA Specifications:**
   - Spring Data JPA Specifications
   - Criteria API Guide
   - Dynamic Query Building

---

## 🎓 Interview Preparation

### **Topics Interviewers Love:**

1. **"How do you secure your REST API?"**
   → Spring Security + JWT

2. **"How do you handle large datasets?"**
   → Pagination + Caching

3. **"How do you test your code?"**
   → Unit Tests + Integration Tests

4. **"How do you implement search functionality?"**
   → JPA Specifications + Dynamic Queries

5. **"How do you optimize performance?"**
   → Caching + Indexes + Lazy Loading

---

## 🎯 Final Recommendation

### **Best Learning Path:**

**Week 1-2: Quick Wins**
- Pagination
- Swagger
- Logging

**Week 3-4: Security**
- Spring Security
- JWT Authentication
- Role-based access

**Week 5-6: Advanced Features**
- Advanced Search
- Caching
- Testing

**Week 7-8: Production Ready**
- File Upload
- Scheduled Tasks
- Email Notifications

**Week 9-10: Enterprise**
- Audit Trail
- Rate Limiting
- Performance Tuning

---

## 📊 Complexity vs Value Matrix

```
High Value, Low Complexity (DO FIRST):
- Pagination & Sorting
- API Documentation
- Logging

High Value, Medium Complexity (DO NEXT):
- Spring Security & JWT
- Advanced Search
- Caching
- Testing

High Value, High Complexity (DO LATER):
- Audit Trail
- File Upload with Cloud Storage

Medium Value, Low Complexity (NICE TO HAVE):
- Scheduled Tasks
- Email Notifications
- Rate Limiting
```

---

## 🚀 Ready to Start?

Pick one topic from Tier 1 and let me know! I'll help you implement it step-by-step with:
- Complete code examples
- Best practices
- Interview tips
- Testing strategies

**Which topic would you like to implement first?** 🎯