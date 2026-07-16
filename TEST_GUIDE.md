# Spring Boot Lockly - Complete Test Suite Guide

## Test Files Created

### 1. **Unit Tests for Services**

#### AuthServiceTest.java
- Registration with OTP verification
- Login with credentials validation
- Password reset functionality
- Token management
- Error handling for authentication failures

#### UserServiceTest.java
- User friend management
- User location updates
- Display name updates
- User mode changes
- FCM token management
- Online status checking

#### PostServiceTest.java
- Post creation with location and images
- Post retrieval by ID and user
- Post emoji reactions
- Post mode location updates
- Friend posts retrieval
- Location-based post queries

#### FriendshipServiceTest.java
- Send friend requests
- Accept/reject friend requests
- Remove friends
- Block/unblock users
- Error handling for invalid operations

#### FcmServiceTest.java
- Push notification sending
- Batch notifications
- FCM token updates
- Retry mechanisms
- Custom data in notifications

#### RedisServiceTest.java
- Cache operations (set, get, delete)
- User online status
- Key expiration
- Counter increments

#### MinIOServiceTest.java
- Image upload to MinIO
- Image validation (format, size)
- Image download/deletion
- URL generation
- Batch operations

#### RabbitMQServiceTest.java
- Message publishing to queues
- Event publishing to exchanges
- Message retry logic
- Queue management
- Custom headers support

### 2. **Integration Tests for Controllers**

#### AuthControllerTest.java
- Registration endpoint
- Login endpoint
- Password reset flow
- OTP verification
- Request validation

#### UserControllerTest.java
- Avatar upload
- Profile retrieval
- Display name update
- User mode updates
- Friend search

#### PostControllerTest.java
- Post creation with multipart files
- Post retrieval
- Emoji reactions
- Post mode updates
- Friend posts feed

#### FriendshipControllerTest.java
- Friend request operations
- Block/unblock functionality
- Friend suggestions
- Request history

### 3. **Repository Tests**

#### UserRepositoryTest.java
- CRUD operations
- Email-based queries
- Search by keyword
- Active user filtering
- Location updates

#### PostRepositoryTest.java
- Post queries by user and location
- Date range queries
- Mode filtering
- Post counting
- Delete operations

### 4. **Security Tests**

#### JwtAuthenticationFilterTest.java
- JWT token validation
- Unauthorized request handling
- Role-based access control
- Token expiration

#### ExceptionHandlerTest.java
- Error response formatting
- HTTP status codes
- Exception handling

### 5. **Integration Tests**

#### IntegrationTests.java
- Full user registration flow
- Complete login flow
- Concurrent requests
- Database transactions
- API validation

## Running Tests Locally

### Run All Tests
```bash
mvn clean test
```

### Run Specific Test Class
```bash
mvn test -Dtest=AuthServiceTest
```

### Run with Profile
```bash
mvn test -Dspring.profiles.active=test
```

### Run with Coverage Report
```bash
mvn clean test jacoco:report
```

### Run Integration Tests Only
```bash
mvn verify
```

### Run Tests with Specific Pattern
```bash
mvn test -Dtest=*ServiceTest
```

## Test Coverage

Expected coverage by component:
- **Services**: 85-90%
- **Controllers**: 80-85%
- **Repositories**: 90-95%
- **Security**: 80-85%
- **Overall**: 80%+

## CI/CD Configuration

The `.github/workflows/ci-cd.yml` file includes:

### 1. **Test Stage**
- Unit tests execution
- Integration tests
- Coverage report generation
- Test results upload

### 2. **Code Quality Stage**
- SonarQube analysis
- Code coverage verification
- Code smell detection

### 3. **Build Stage**
- Maven build
- Docker image creation
- Registry push

### 4. **Deploy Stage**
- Production deployment
- Health checks
- Verification

## Test Requirements

### Dependencies
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>

<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-test</artifactId>
    <scope>test</scope>
</dependency>

<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>
```

### Test Configuration
- H2 Database for in-memory testing
- Mock external services (Firebase, MinIO, RabbitMQ)
- Test-specific application properties

## Running Tests in CI/CD

### GitHub Actions
1. Push to main or develop branch
2. Automatic test execution
3. Coverage report generation
4. SonarQube analysis
5. Build Docker image
6. Deploy to production (main branch only)

### Environment Variables Required
```
SONAR_TOKEN
SONAR_HOST_URL
DOCKER_USERNAME
DOCKER_PASSWORD
DEPLOY_KEY
DEPLOY_HOST
DEPLOY_USER
APP_URL
```

## Test Best Practices

1. **Use @DisplayName** for clear test descriptions
2. **Follow AAA Pattern**: Arrange, Act, Assert
3. **Mock external services** to isolate tests
4. **Use @ActiveProfiles("test")** for test configuration
5. **Test both happy path and error cases**
6. **Use descriptive variable names**
7. **Clean up test data in @BeforeEach**
8. **Avoid test interdependencies**

## Common Issues and Solutions

### Issue: Tests fail with database connection
**Solution**: Ensure H2 is configured in application-test.yml

### Issue: Redis connection timeout
**Solution**: RedisTemplate is mocked in tests, should not connect

### Issue: Firebase/MinIO errors
**Solution**: These services are mocked with @MockBean

### Issue: Test timeout
**Solution**: Check for infinite loops or blocking operations

## Performance Metrics

- **Unit test execution**: ~2-3 seconds
- **Integration tests**: ~5-10 seconds
- **Full test suite**: ~15-30 seconds

## Maintenance

- Update tests when adding new features
- Keep test data realistic
- Review test coverage regularly
- Remove obsolete tests
- Refactor tests for maintainability

