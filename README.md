# 🛵 DeliveryApp — Backend

A microservices backend inspired by PedidosYa, built with Spring Boot and Spring Cloud. Covers the full delivery lifecycle: user authentication, restaurant catalog, order placement, driver assignment, and notifications — each as an independent deployable service communicating via REST (Feign) and asynchronous events (Kafka).

---

## 🛠️ Tech Stack

| Technology | Purpose |
|---|---|
| Java 17 | Core language |
| Spring Boot 3.5.4 | Application framework |
| Spring Cloud 2025.0.0 | Microservices infrastructure |
| Spring Cloud Gateway | API Gateway (reactive/WebFlux) |
| Spring Cloud Netflix Eureka | Service discovery and registration |
| Spring Cloud Config | Centralized configuration server |
| Spring Cloud OpenFeign | Declarative HTTP client between services |
| Resilience4j | Circuit breaker, retry, rate limiter |
| Spring Security + JWT | Stateless authentication |
| Spring Data JPA + Hibernate | ORM and database access |
| PostgreSQL 16 | Relational database (one DB per service) |
| Flyway | Database migrations |
| Apache Kafka | Asynchronous event-driven communication |
| SpringDoc / Swagger UI | API documentation |
| Lombok | Boilerplate reduction |
| Docker + Docker Compose | PostgreSQL, pgAdmin, Kafka, Zookeeper and Kafka UI |
| JUnit 5 + Mockito | Unit testing |
| Maven | Dependency management and build |
| GitHub Actions | CI/CD pipeline (build, test, release) |

---

## 🏗️ Architecture

```
Client (Postman / Frontend)
           ↓
      API Gateway :8080
           ↓  validates JWT, routes by path prefix
─────────────────────────────────────────────────────────────
              Eureka Discovery Server :8761
─────────────────────────────────────────────────────────────
    ↓              ↓              ↓               ↓
auth-service  restaurant-   order-service   delivery-service
  :8081        service         :8083             :8084
               :8082             │                  │
                           Feign →            Kafka →
                           restaurant    [delivery-status]
                                │                  │
                         Kafka →          notification-service
                       [order-confirmed]       :8085
                                │
                         delivery-service
                           (consumer)
─────────────────────────────────────────────────────────────
              Config Server :8888
       (serves application.yml to every service)
```

### Inter-service communication

Two communication patterns coexist depending on whether the call needs an immediate response:

**Synchronous (OpenFeign):** `order-service` calls `restaurant-service` to validate menu items at order creation time. A response is required before the order can be saved.

**Asynchronous (Kafka):** When an order is confirmed, `order-service` publishes an `ORDER_CONFIRMED` event. `delivery-service` consumes it and creates a delivery record independently, then publishes `DRIVER_ASSIGNED` / `ORDER_DELIVERED` events that `notification-service` consumes to send notifications. No service blocks waiting on another.

---

## 📁 Project Structure

```
delivery-app/
├── pom.xml                           ← Parent POM (BOM — manages versions for all modules)
├── docker-compose.yml                ← PostgreSQL, pgAdmin, Kafka, Zookeeper, Kafka UI
├── .env.example
├── .github/
│   └── workflows/
│       ├── ci.yml                    ← Build and test on every push/PR
│       └── release.yml               ← Release pipeline
│
├── config-server/                    ← Spring Cloud Config Server (port 8888)
│   └── src/main/resources/
│       ├── application.yml
│       └── config/
│           ├── discovery-server.yml
│           ├── api-gateway.yml
│           ├── auth-service.yml
│           ├── restaurant-service.yml
│           ├── order-service.yml
│           ├── delivery-service.yml
│           └── notification-service.yml
│
├── discovery-server/                 ← Eureka Server (port 8761)
│
├── api-gateway/                      ← Spring Cloud Gateway (port 8080)
│   └── src/main/java/.../
│       ├── filter/
│       │   ├── AuthenticationFilter.java
│       │   └── RouteValidator.java
│       ├── util/JwtUtil.java
│       └── config/GatewayConfig.java
│
├── auth-service/                     ← Authentication + Users (port 8081)
│   └── src/main/java/.../
│       ├── controller/AuthController.java
│       ├── service/AuthService.java / AuthServiceImpl.java
│       ├── service/RefreshTokenService.java / RefreshTokenServiceImpl.java
│       ├── model/User.java, RefreshToken.java
│       ├── security/JwtTokenProvider.java, JwtAuthenticationFilter.java
│       └── config/SecurityConfig.java
│
├── restaurant-service/               ← Restaurants + Menu Catalog (port 8082)
│   └── src/main/java/.../
│       ├── controller/RestaurantController.java, MenuController.java
│       ├── service/RestaurantService.java/.Impl, MenuService.java/.Impl
│       └── model/Restaurant.java, MenuItem.java, Category.java
│
├── order-service/                    ← Orders (port 8083)
│   └── src/main/java/.../
│       ├── controller/OrderController.java
│       ├── service/OrderService.java / OrderServiceImpl.java
│       ├── client/RestaurantClient.java     ← Feign + CircuitBreaker
│       ├── event/OrderConfirmedEvent.java
│       └── event/OrderEventProducer.java    ← Kafka producer
│
├── delivery-service/                 ← Deliveries + Drivers (port 8084)
│   └── src/main/java/.../
│       ├── controller/DeliveryController.java
│       ├── service/DeliveryService.java / DeliveryServiceImpl.java
│       ├── event/OrderConfirmedEvent.java
│       ├── event/DeliveryStatusEvent.java
│       ├── event/DeliveryEventConsumer.java ← Kafka consumer
│       └── event/DeliveryEventProducer.java ← Kafka producer
│
└── notification-service/             ← Notifications (port 8085)
    └── src/main/java/.../
        ├── controller/NotificationController.java
        ├── service/NotificationService.java / NotificationServiceImpl.java
        ├── event/DeliveryStatusEvent.java
        └── event/NotificationEventConsumer.java ← Kafka consumer
```

---

## ⚙️ Configuration

Every service reads its configuration from Config Server at startup. Each service's local `application.yml` contains only its name and where to find Config Server:

```yaml
spring:
  application:
    name: auth-service
  config:
    import: optional:configserver:http://localhost:8888
```

All other configuration (database URL, JWT secret, Kafka brokers, Resilience4j settings) lives in `config-server/src/main/resources/config/<service-name>.yml`.

### Environment variables

Copy `.env.example` to `.env` and fill in your values:

```bash
cp .env.example .env
```

| Variable | Description |
|---|---|
| `POSTGRES_USER` | PostgreSQL username |
| `POSTGRES_PASSWORD` | PostgreSQL password |
| `JWT_SECRET` | HS256 signing secret (min 64 chars) |
| `JWT_ACCESS_TOKEN_EXPIRATION_MS` | Access token TTL (default: 900000 = 15 min) |
| `JWT_REFRESH_TOKEN_EXPIRATION_MS` | Refresh token TTL (default: 604800000 = 7 days) |
| `PGADMIN_DEFAULT_EMAIL` | pgAdmin login email |
| `PGADMIN_DEFAULT_PASSWORD` | pgAdmin login password |

---

## 🚀 Running the Project

### Prerequisites

- Java 17
- Maven 3.8+
- Docker Desktop

### 1. Start infrastructure containers

```bash
docker compose up -d
```

This starts PostgreSQL, pgAdmin, Zookeeper, Kafka and Kafka UI. The init container also creates one database per service automatically:

```
auth_db  /  restaurant_db  /  order_db  /  delivery_db  /  notification_db
```

### 2. Start services in order

Each service must be started in a separate terminal. Order matters because of dependencies:

```bash
# Terminal 1 — Config Server (must be first)
cd config-server && mvn spring-boot:run

# Terminal 2 — Discovery Server
cd discovery-server && mvn spring-boot:run

# Terminal 3 — API Gateway
cd api-gateway && mvn spring-boot:run

# Terminal 4 — Auth Service
cd auth-service && mvn spring-boot:run

# Terminal 5 — Restaurant Service
cd restaurant-service && mvn spring-boot:run

# Terminal 6 — Order Service
cd order-service && mvn spring-boot:run

# Terminal 7 — Delivery Service
cd delivery-service && mvn spring-boot:run

# Terminal 8 — Notification Service
cd notification-service && mvn spring-boot:run
```

> ⚠️ Config Server must be running before any other service. Kafka warnings in delivery-service and notification-service logs are normal until the Kafka container is fully healthy — the consumers reconnect automatically.

### Stop containers

```bash
# Stop but keep data
docker compose stop

# Stop and remove containers
docker compose down

# Stop and remove everything including volumes
docker compose down -v
```

---

## 🗄️ Database

Each microservice owns its own PostgreSQL database. No shared tables, no cross-database joins. Cross-service data is referenced by ID only.

| Service | Database |
|---|---|
| auth-service | auth_db |
| restaurant-service | restaurant_db |
| order-service | order_db |
| delivery-service | delivery_db |
| notification-service | notification_db |

Access pgAdmin at `http://localhost:5050`

| Field | Value |
|---|---|
| Email | admin@deliveryapp.com (or your .env value) |
| Password | admin123 (or your .env value) |
| PostgreSQL host | `postgres` (Docker service name, not localhost) |
| Port | 5432 |

---

## 📨 Kafka Event Flow

```
PATCH /api/v1/orders/{id}/status?status=CONFIRMED
        ↓
  order-service saves status change
        ↓
  publishes ──► [order-confirmed] ──► Kafka
                                          ↓
                              delivery-service consumer
                                          ↓
                              creates delivery record in DB
                                          ↓
                              publishes ──► [delivery-status] ──► Kafka
                                                                      ↓
                                                        notification-service consumer
                                                                      ↓
                                                        persists and sends notification
```

### Kafka Topics

| Topic | Producer | Consumer | Trigger |
|---|---|---|---|
| `order-confirmed` | order-service | delivery-service | Order status → CONFIRMED |
| `delivery-status` | delivery-service | notification-service | Driver assigned or delivery completed |

Access Kafka UI at `http://localhost:8090` to inspect topics, messages and consumer groups in real time.

---

## 🔐 Security Flow

```
POST /api/v1/auth/register  →  201 { accessToken, refreshToken, user }
POST /api/v1/auth/login     →  200 { accessToken, refreshToken, user }

GET  /api/v1/orders         →  Authorization: Bearer <accessToken>
                               API Gateway validates JWT → forwards to order-service
                               Downstream services trust X-Auth-User header

POST /api/v1/auth/refresh   →  body: { refreshToken }  →  200 { new tokens }
POST /api/v1/auth/logout    →  body: { refreshToken }  →  204
```

Access tokens expire in 15 minutes. Refresh tokens expire in 7 days and rotate on every use.

### User Roles

| Role | Description |
|---|---|
| `ROLE_CUSTOMER` | Places orders |
| `ROLE_RESTAURANT_OWNER` | Manages restaurant and menu |
| `ROLE_DRIVER` | Receives delivery assignments |
| `ROLE_ADMIN` | Full access |

---

## 🌐 API Endpoints

All requests go through the API Gateway at `http://localhost:8080`.

### Authentication (`/api/v1/auth`)

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `POST` | `/api/v1/auth/register` | Public | Register a new user |
| `POST` | `/api/v1/auth/login` | Public | Login and get tokens |
| `POST` | `/api/v1/auth/refresh` | Public | Refresh access token |
| `POST` | `/api/v1/auth/logout` | Public | Invalidate refresh token |

### Restaurants (`/api/v1/restaurants`)

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `POST` | `/api/v1/restaurants` | 🔒 | Create a restaurant |
| `GET` | `/api/v1/restaurants` | 🔒 | List all restaurants |
| `GET` | `/api/v1/restaurants/{id}` | 🔒 | Get restaurant by ID |
| `GET` | `/api/v1/restaurants/owner/{ownerId}` | 🔒 | Get restaurants by owner |
| `PUT` | `/api/v1/restaurants/{id}` | 🔒 | Update a restaurant |
| `DELETE` | `/api/v1/restaurants/{id}` | 🔒 | Delete a restaurant |

### Menu (`/api/v1/menu`)

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `POST` | `/api/v1/menu` | 🔒 | Create a menu item |
| `GET` | `/api/v1/menu/{id}` | 🔒 | Get menu item by ID |
| `GET` | `/api/v1/menu/restaurant/{restaurantId}` | 🔒 | List menu for a restaurant |
| `PUT` | `/api/v1/menu/{id}` | 🔒 | Update a menu item |
| `PATCH` | `/api/v1/menu/{id}/availability` | 🔒 | Toggle item availability |
| `DELETE` | `/api/v1/menu/{id}` | 🔒 | Delete a menu item |

### Orders (`/api/v1/orders`)

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `POST` | `/api/v1/orders` | 🔒 | Create an order (validates items via Feign) |
| `GET` | `/api/v1/orders/{id}` | 🔒 | Get order by ID |
| `GET` | `/api/v1/orders/user/{userId}` | 🔒 | List orders by user |
| `PATCH` | `/api/v1/orders/{id}/status` | 🔒 | Update order status (triggers Kafka event on CONFIRMED) |
| `PATCH` | `/api/v1/orders/{id}/cancel` | 🔒 | Cancel an order |

### Delivery (`/api/v1/delivery`)

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `POST` | `/api/v1/delivery` | 🔒 | Create a delivery record |
| `GET` | `/api/v1/delivery/{id}` | 🔒 | Get delivery by ID |
| `GET` | `/api/v1/delivery/order/{orderId}` | 🔒 | Get delivery by order |
| `PATCH` | `/api/v1/delivery/{id}/assign` | 🔒 | Assign next available driver |
| `PATCH` | `/api/v1/delivery/{id}/status` | 🔒 | Update delivery status |

### Notifications (`/api/v1/notifications`)

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `POST` | `/api/v1/notifications` | 🔒 | Send a notification |
| `GET` | `/api/v1/notifications/user/{userId}` | 🔒 | Get notifications for a user |

---

## ⚡ Resilience4j

Feign calls between services are protected with circuit breakers and retry policies.

```
order-service → restaurant-service
  @CircuitBreaker: opens after 50% failure rate in a 10-call window
  @Retry: 3 attempts with exponential backoff (1s → 2s → 4s)
  Fallback: throws MenuItemUnavailableException (400)
```

The fallback philosophy: a missing menu item is a hard blocker for order creation. Notification failures are best-effort — the delivery proceeds regardless. This is now reinforced by the Kafka pattern: notification-service processes events independently, so a notification failure never rolls back a delivery.

---

## 🔁 CI/CD — GitHub Actions

Two workflows run automatically on every push and pull request:

**`ci.yml`** — Continuous Integration
- Triggered on push to any branch and on pull requests
- Compiles the full multi-module project with `mvn clean install`
- Runs all unit tests across every service
- Fails the pipeline if any test fails, preventing broken code from merging

**`release.yml`** — Release pipeline
- Triggered on version tags or manual dispatch
- Builds and packages all services
- Creates a GitHub Release with the compiled artifacts

---

## 🔍 Useful URLs

| URL | Description |
|---|---|
| `http://localhost:8761` | Eureka dashboard — all registered services |
| `http://localhost:5050` | pgAdmin — inspect databases and tables |
| `http://localhost:8090` | Kafka UI — topics, messages and consumer groups |
| `http://localhost:8081/swagger-ui.html` | Auth Service Swagger UI |
| `http://localhost:8082/swagger-ui.html` | Restaurant Service Swagger UI |
| `http://localhost:8083/swagger-ui.html` | Order Service Swagger UI |
| `http://localhost:8084/swagger-ui.html` | Delivery Service Swagger UI |
| `http://localhost:8085/swagger-ui.html` | Notification Service Swagger UI |
| `http://localhost:8888/auth-service/default` | Verify Config Server is serving auth config |

---

## 🧠 Design Decisions

**One database per service** — Each microservice owns its schema exclusively. No shared tables, no cross-database joins. Cross-service data is referenced by ID only. This enforces true independence and lets each service evolve its schema without coordinating with others.

**Synchronous vs asynchronous communication by use case** — Feign is used when an immediate response is required (validating a menu item before saving an order). Kafka is used for downstream side effects that don't need to block the caller (creating a delivery after an order is confirmed, sending a notification after a driver is assigned). The choice is driven by whether the caller needs the result, not by preference.

**Independent event contracts** — Each service defines its own copy of the events it produces or consumes (e.g. `OrderConfirmedEvent` in both `order-service` and `delivery-service`). No shared JAR. This mirrors the same principle applied to Feign DTOs: services evolve their contracts independently, and Jackson matches fields by name at runtime.

**Cross-service references as plain Longs** — `order-service` stores `restaurantId` as a `Long`, not a JPA `@ManyToOne`. The `Restaurant` entity lives in a different database — there is no foreign key to reference across service boundaries.

**Price snapshot at order time** — `OrderItem` stores `menuItemName` and `unitPrice` as snapshots at order creation. If the restaurant changes prices tomorrow, historical orders still show what the customer actually paid.

**Interface + Implementation on every service** — Every service class is defined as an interface and implemented separately. Controllers inject the interface, making every service independently mockable in unit tests without loading the Spring context.

**Gateway-level JWT validation** — The API Gateway validates the JWT once before forwarding any secured request. Downstream services receive the resolved username via `X-Auth-User` and trust it without re-parsing the token.

**Config Server with native profile** — Configuration is centralized in `config-server/src/main/resources/config/`, one file per service. Using the `native` profile keeps the setup self-contained for development. In production, swap to `git` profile to get versioned configuration history.

---

## 📄 License

This project is for educational purposes.
