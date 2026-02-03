# BloomGuard

High-performance Bloom filter microservice for banking fraud prevention and duplicate detection.

## Overview

BloomGuard provides a REST API for managing Bloom filters with support for:

- Stolen card detection
- Duplicate transaction identification
- Invoice fraud prevention
- Suspicious account monitoring

## Prerequisites

Before running BloomGuard, ensure you have the following installed:

### Required
- **Java 17+** - Download from [Java.net](https://jdk.java.net/17) or use a package manager
  - Verify: `java -version`
- **Maven 3.8+** - Download from [Apache Maven](https://maven.apache.org/download.cgi)
  - Verify: `mvn -version`

### For Backend Services
- **Redis 7.x** - In-memory data store for Bloom filters
- **PostgreSQL 15+** - Relational database for audit logs and configuration

### Optional
- **Docker & Docker Compose** - For containerized setup (recommended for new users)
- **Postman** or **cURL** - For API testing
- **Git** - For version control

## Getting Started

### Option 1: Docker Compose (Recommended for New Users)

The easiest way to get started is using Docker Compose, which sets up all services automatically.

**Prerequisites:** Docker and Docker Compose must be installed.

#### Step 1: Clone and Navigate

```bash
git clone <repository-url>
cd bloom-filtering-guard
```

#### Step 2: Start Services

```bash
# Start all services (Redis, PostgreSQL, BloomGuard)
docker-compose up -d

# View logs to confirm startup
docker-compose logs -f bloomguard
```

#### Step 3: Test the Service

```bash
# Check health
curl http://localhost:8080/api/v1/health

# You should see:
# {"status":"UP","components":{"redis":{"status":"UP"},"db":{"status":"UP"}}}
```

#### Stop Services

```bash
docker-compose down
```

### Option 2: Local Manual Setup

For development without Docker:

#### Step 1: Install Java 17+

**macOS (using Homebrew):**
```bash
brew install openjdk@17
java -version  # Verify installation
```

**Ubuntu/Debian:**
```bash
sudo apt-get update
sudo apt-get install openjdk-17-jdk
java -version
```

**Windows:**
- Download from [java.net](https://jdk.java.net/17)
- Add Java to PATH

#### Step 2: Install Maven

**macOS:**
```bash
brew install maven
mvn -version
```

**Ubuntu/Debian:**
```bash
sudo apt-get install maven
mvn -version
```

**Windows:**
- Download from [maven.apache.org](https://maven.apache.org/download.cgi)
- Add Maven to PATH

#### Step 3: Install and Start Redis

**macOS:**
```bash
brew install redis
redis-server  # Start Redis
```

**Ubuntu/Debian:**
```bash
sudo apt-get install redis-server
sudo systemctl start redis-server
sudo systemctl status redis-server
```

**Windows:**
- Use WSL2 or download from [redis GitHub releases](https://github.com/microsoftarchive/redis/releases)

**Docker:**
```bash
docker run -d -p 6379:6379 redis:7-alpine
```

#### Step 4: Install and Start PostgreSQL

**macOS:**
```bash
brew install postgresql
brew services start postgresql
createdb bloomguard
```

**Ubuntu/Debian:**
```bash
sudo apt-get install postgresql postgresql-contrib
sudo systemctl start postgresql
sudo -u postgres createdb bloomguard
```

**Windows:**
- Download from [postgresql.org](https://www.postgresql.org/download/)

**Docker:**
```bash
docker run -d \
  -e POSTGRES_USER=bloomguard \
  -e POSTGRES_PASSWORD=bloomguard \
  -e POSTGRES_DB=bloomguard \
  -p 5432:5432 \
  postgres:15-alpine
```

#### Step 5: Configure Database User

```bash
# Create database user if not exists
psql -U postgres -c "CREATE USER bloomguard WITH PASSWORD 'bloomguard';" 2>/dev/null || true
psql -U postgres -c "GRANT ALL PRIVILEGES ON DATABASE bloomguard TO bloomguard;"
```

#### Step 6: Run the Application

```bash
# Navigate to project
cd bloom-filtering-guard

# Compile the project
mvn clean compile

# Run with development profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

The service will be available at `http://localhost:8080`

#### Step 7: Test the Service

```bash
curl http://localhost:8080/api/v1/health
```

### Option 3: Build and Run as JAR

```bash
# Navigate to project
cd bloom-filtering-guard

# Create packaged JAR
mvn clean package

# Run the JAR (requires Redis and PostgreSQL running)
java -jar target/bloomguard-1.0.0-SNAPSHOT.jar
```

## Configuration

### Profiles

BloomGuard supports multiple configuration profiles:

| Profile | Use Case | Security | Notes |
|---------|----------|----------|-------|
| `dev` | Local development | Minimal | API key auth disabled, relaxed rate limiting |
| `test` | Unit/Integration tests | Minimal | In-memory H2 database, mocked Redis |
| `prod` | Production deployment | Maximum | Full API key auth, strict rate limiting |

#### Using Profiles

```bash
# Set via environment variable
export SPRING_PROFILES_ACTIVE=dev
mvn spring-boot:run

# Set via Maven argument
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Set in Docker
docker run -e SPRING_PROFILES_ACTIVE=prod bloomguard:latest
```

### Environment Variables

Configure these environment variables for custom setups:

| Variable | Description | Default | Example |
|----------|-------------|---------|---------|
| `SPRING_PROFILES_ACTIVE` | Active profile | dev | prod |
| `SERVER_PORT` | Application port | 8080 | 9090 |
| `REDIS_HOST` | Redis server host | localhost | redis.example.com |
| `REDIS_PORT` | Redis server port | 6379 | 6380 |
| `SPRING_DATASOURCE_URL` | PostgreSQL JDBC URL | jdbc:postgresql://localhost:5432/bloomguard | jdbc:postgresql://db.example.com:5432/bloomguard |
| `SPRING_DATASOURCE_USERNAME` | Database user | bloomguard | prod_user |
| `SPRING_DATASOURCE_PASSWORD` | Database password | - | secure_password |
| `BLOOMGUARD_SECURITY_API_KEY_ENABLED` | Enable API key auth | false (dev) / true (prod) | true |
| `BLOOMGUARD_RATE_LIMIT_ENABLED` | Enable rate limiting | false (dev) / true (prod) | true |

### Application Configuration

For advanced configuration, edit `src/main/resources/application.yml`:

```yaml
spring:
  application:
    name: bloomguard
  jpa:
    hibernate:
      ddl-auto: validate
    database-platform: org.hibernate.dialect.PostgreSQLDialect
  redis:
    host: localhost
    port: 6379
    timeout: 2000
  cache:
    type: caffeine
    caffeine:
      spec: maximumSize=1000,expireAfterWrite=10m

bloomguard:
  filter:
    default-expected-insertions: 100000
    default-false-positive-rate: 0.01
  security:
    api-key-enabled: true
  rate-limit:
    enabled: true
    requests-per-minute: 1000
  backup:
    enabled: true
    schedule: "0 0 * * * ?"  # Daily at midnight
```

## Testing the API

### Health Check

Verify the service is running:

```bash
curl http://localhost:8080/api/v1/health
```

Expected response:
```json
{
  "status": "UP",
  "components": {
    "redis": {"status": "UP"},
    "db": {"status": "UP"}
  }
}
```

### Getting an API Key

For development, use the default key `dev-api-key`:

```bash
API_KEY="dev-api-key"
```

For production, generate a secure key:

```bash
# Generate a random API key
openssl rand -hex 32
```

### API Reference

#### Bloom Filter Operations

##### Check if item exists

```bash
curl -X POST http://localhost:8080/api/v1/bloom/check \
  -H "Content-Type: application/json" \
  -H "X-API-Key: dev-api-key" \
  -d '{
    "filterName": "stolen-cards",
    "item": "4111111111111111"
  }'
```

Response:
```json
{
  "success": true,
  "data": {
    "filterName": "stolen-cards",
    "item": "4111111111111111",
    "mightExist": false
  }
}
```

##### Add item to filter

```bash
curl -X POST http://localhost:8080/api/v1/bloom/add \
  -H "Content-Type: application/json" \
  -H "X-API-Key: dev-api-key" \
  -d '{
    "filterName": "stolen-cards",
    "item": "4111111111111111"
  }'
```

Response:
```json
{
  "success": true,
  "data": {
    "filterName": "stolen-cards",
    "added": true,
    "message": "Item added to filter"
  }
}
```

##### Batch check

```bash
curl -X POST http://localhost:8080/api/v1/bloom/batch-check \
  -H "Content-Type: application/json" \
  -H "X-API-Key: dev-api-key" \
  -d '{
    "filterName": "stolen-cards",
    "items": ["4111111111111111", "5555555555555555", "378282246310005"]
  }'
```

#### Fraud Detection

##### Check stolen card

```bash
curl -X POST http://localhost:8080/api/v1/fraud/stolen-card \
  -H "Content-Type: application/json" \
  -H "X-API-Key: dev-api-key" \
  -d '{
    "cardNumber": "4111111111111111"
  }'
```

Response:
```json
{
  "success": true,
  "data": {
    "flagged": false,
    "verified": false,
    "riskLevel": "LOW",
    "checkType": "STOLEN_CARD"
  }
}
```

##### Check duplicate transaction

```bash
curl -X POST http://localhost:8080/api/v1/fraud/duplicate-transaction \
  -H "Content-Type: application/json" \
  -H "X-API-Key: dev-api-key" \
  -d '{
    "accountId": "ACC123",
    "amount": "100.00",
    "currency": "USD",
    "recipientId": "REC456",
    "reference": "TXN789"
  }'
```

## Running Tests

### Unit Tests

Run all unit tests:

```bash
mvn test
```

Run specific test class:

```bash
mvn test -Dtest=BloomFilterServiceTest
```

Run with code coverage:

```bash
mvn clean test jacoco:report
# View report at: target/site/jacoco/index.html
```

### Integration Tests

Integration tests use Testcontainers (Docker required):

```bash
mvn verify
```

Run only integration tests:

```bash
mvn verify -Dit.test=*Integration*
```

### Test Coverage

Generate code coverage report:

```bash
mvn clean test jacoco:report
open target/site/jacoco/index.html  # macOS
# or view in browser: file://target/site/jacoco/index.html
```

## Development

### Project Structure

```
bloom-filtering-guard/
├── src/
│   ├── main/java/com/bloomguard/
│   │   ├── config/              # Spring configuration classes
│   │   ├── controller/          # REST controllers
│   │   ├── service/             # Business logic
│   │   ├── model/               # Entity and DTO models
│   │   ├── repository/          # JPA repositories
│   │   ├── security/            # Authentication/authorization
│   │   ├── exception/           # Custom exceptions
│   │   ├── scheduler/           # Scheduled tasks
│   │   ├── metrics/             # Prometheus metrics
│   │   └── util/                # Utility classes
│   ├── main/resources/
│   │   ├── application.yml      # Default configuration
│   │   ├── application-dev.yml  # Development settings
│   │   └── application-prod.yml # Production settings
│   └── test/
│       ├── java/com/bloomguard/ # Test classes
│       └── resources/           # Test configuration
├── docker-compose.yml           # Local development setup
├── Dockerfile                   # Container image
├── k8s/                         # Kubernetes manifests
├── helm/                        # Helm charts
├── pom.xml                      # Maven configuration
└── README.md                    # This file
```

### Building from Source

```bash
# Clone repository
git clone <repository-url>
cd bloom-filtering-guard

# Compile
mvn clean compile

# Package as JAR
mvn package

# Run JAR file
java -jar target/bloomguard-1.0.0-SNAPSHOT.jar

# Build Docker image
docker build -t bloomguard:latest .
```

### IDE Setup

#### IntelliJ IDEA
1. Open project: File → Open → select bloom-filtering-guard
2. Configure JDK: File → Project Structure → Project → SDK (select Java 17+)
3. Enable annotation processing: File → Settings → Build, Execution, Deployment → Compiler → Annotation Processors (check "Enable annotation processing")
4. Run application: Right-click BloomGuardApplication.java → Run

#### VS Code
1. Install extensions: Extension Pack for Java, Spring Boot Extension Pack
2. Open folder: File → Open Folder → select bloom-filtering-guard
3. Run application: Click "Run" on the main method or press Ctrl+F5

### Creating New Endpoints

1. Create a request/response DTO in `model/dto/`
2. Add business logic in `service/`
3. Create a controller method in `controller/`
4. Add tests in `src/test/`

Example:
```java
// 1. DTO
public class MyRequest {
    private String data;
}

// 2. Service
@Service
public class MyService {
    public String process(String data) {
        return "Processed: " + data;
    }
}

// 3. Controller
@RestController
@RequestMapping("/api/v1/my")
public class MyController {
    @PostMapping("/process")
    public ApiResponse process(@RequestBody MyRequest request) {
        return ApiResponse.success(myService.process(request.getData()));
    }
}
```

## Security

### API Authentication

BloomGuard uses API key authentication:

- Include `X-API-Key` header in all requests
- Keys are validated against database
- Different keys can have different rate limits

```bash
curl -H "X-API-Key: your-api-key" http://localhost:8080/api/v1/health
```

### Rate Limiting

Defaults (per profile):
- **dev**: 10,000 requests/minute
- **prod**: 1,000 requests/minute per API key

Exceeding limits returns 429 (Too Many Requests).

### Tenant Isolation

Each API key is associated with a tenant. All operations are automatically scoped to the caller's tenant.

### Audit Logging

All operations are logged:
- Who performed the action (API key)
- What action was performed
- When it was performed
- Operation result (success/failure)

View audit logs:
```bash
curl -H "X-API-Key: your-api-key" \
  http://localhost:8080/api/v1/admin/audit-logs?limit=100
```

## Monitoring

### Prometheus Metrics

Metrics are exposed at `/actuator/prometheus`:

- `bloomguard_checks_total` - Total filter checks
- `bloomguard_adds_total` - Total items added
- `bloomguard_false_positive_rate` - Estimated false positive rate

### Health Endpoints

- `/actuator/health` - Overall health
- `/actuator/health/readiness` - Readiness probe
- `/actuator/health/liveness` - Liveness probe

## Deployment

### Kubernetes

```bash
kubectl apply -f k8s/
```

### Helm

```bash
helm install bloomguard helm/bloomguard
```

## Troubleshooting

### Cannot connect to Redis

```
Error: Unable to connect to Redis at localhost:6379
```

Solution:
```bash
# Check Redis is running
redis-cli ping
# Should return: PONG

# Start Redis if not running
redis-server

# Or with Docker:
docker run -d -p 6379:6379 redis:7-alpine
```

### Cannot connect to PostgreSQL

```
Error: Connection refused to PostgreSQL
```

Solution:
```bash
# Check PostgreSQL is running
sudo systemctl status postgresql

# Verify connection settings in application.yml
# Start PostgreSQL if needed
sudo systemctl start postgresql

# Or with Docker:
docker run -d \
  -e POSTGRES_USER=bloomguard \
  -e POSTGRES_PASSWORD=bloomguard \
  -e POSTGRES_DB=bloomguard \
  -p 5432:5432 \
  postgres:15-alpine
```

### Maven build fails

```
Error: Maven compilation fails
```

Solution:
```bash
# Clean and rebuild
mvn clean install

# Update dependencies
mvn dependency:resolve

# Check Java version
java -version  # Should be 17+

# Clear local Maven cache if needed
rm -rf ~/.m2/repository
mvn clean install
```

### Application won't start

Check logs for errors:
```bash
# If running with Maven:
mvn spring-boot:run -Dspring-boot.run.profiles=dev -X

# If running as JAR:
java -jar bloomguard.jar

# If running with Docker:
docker logs bloomguard
```

## Performance Tuning

### Redis Configuration

For high-throughput scenarios:

```yaml
spring:
  redis:
    lettuce:
      pool:
        max-active: 20
        max-idle: 10
        min-idle: 5
        time-between-eviction-runs: 60000
```

### Database Connection Pool

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
```

### Cache Tuning

```yaml
spring:
  cache:
    caffeine:
      spec: maximumSize=5000,expireAfterWrite=30m
```

## Architecture

```
+-----------------------------------------------------------------+
|                        BloomGuard Service                        |
+-----------------------------------------------------------------+
|  Controllers         Services              Repositories          |
|  +-- BloomFilter    +-- RedisBloomFilter  +-- AuditLog          |
|  +-- FraudDetection +-- GuavaBloomFilter  +-- FilterConfig      |
|  +-- Admin          +-- FraudCheck        +-- StolenCard        |
|  +-- Statistics     +-- Audit             +-- Transaction       |
|                     +-- BackupRecovery                          |
+-----------------------------------------------------------------+
|                         Data Stores                              |
|  +--------------+  +--------------+  +--------------+           |
|  |    Redis     |  |  PostgreSQL  |  |   Caffeine   |           |
|  | (Filters)    |  | (Audit/Config)|  |   (Cache)    |           |
|  +--------------+  +--------------+  +--------------+           |
+-----------------------------------------------------------------+
```

## License

Proprietary - Internal use only

## Support

For issues or questions:
1. Check the Troubleshooting section above
2. Review application logs
3. Verify prerequisites are installed correctly
4. Contact the development team