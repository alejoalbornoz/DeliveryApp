# 🛵 DeliveryApp — Backend

A microservices backend inspired by PedidosYa, built with Spring Boot and Spring Cloud. Covers the full delivery lifecycle: user authentication, restaurant catalog, order placement, driver assignment, and notifications — each as an independent deployable service.

---

## 🛠️ Tech Stack

| Technology | Purpose |
|---|---|
| Java 17 | Core language |
| Spring Boot 4.0.6 | Application framework |
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
| SpringDoc / Swagger UI | API documentation |
| MapStruct | DTO mapping |
| Lombok | Boilerplate reduction |
| Docker + Docker Compose | PostgreSQL and pgAdmin containers |
| JUnit 5 + Mockito | Unit testing |
| Testcontainers | Integration testing with real PostgreSQL |
| Maven | Dependency management and build |

---

## 🏗️ Architecture

```
Client (Postman / Frontend)
           ↓
      API Gateway :8080
           ↓  validates JWT, routes by path prefix
─────────────────────────────────────────────────────
         Eureka Discovery Server :8761
─────────────────────────────────────────────────────
    ↓           ↓           ↓            ↓
auth-service  restaurant  order-service  delivery-service
  :8081       -service      :8083           :8084
              :8082           ↓               ↓
                        (Feign →        (Feign →
                        restaurant)     notification)
                                            ↓
                                  notification-service
                                        :8085
─────────────────────────────────────────────────────
         Config Server :8888
         (serves application.yml to every service)
```

### Inter-service communication

`order-service` calls `restaurant-service` via OpenFeign to validate menu items at order creation time. `delivery-service` calls `notification-service` via OpenFeign on driver assignment and delivery status changes. Both Feign clients are protected by Resilience4j circuit breakers and retry policies.

---

## 📁 Project Structure

```
delivery-app/
├── pom.xml                          ← Parent POM (BOM — manages versions for all modules)
├── docker-compose.yml               ← PostgreSQL + pgAdmin
├── .env.example
├── scripts/
│   └── init-multiple-databases.sh   ← Creates one DB per service on first run
│
├── config-server/                   ← Spring Cloud Config Server (port 8888)
│   └── src/main/resources/
│       ├── application.yml
│       └── config/                  ← One .yml file per downstream service
│           ├── discovery-server.yml
│           ├── api-gateway.yml
│           ├── auth-service.yml
│           ├── restaurant-service.yml
│           ├── order-service.yml
│           ├── delivery-service.yml
│           └── notification-service.yml
│
├── discovery-server/                ← Eureka Server (port 8761)
│
├── api-gateway/                     ← Spring Cloud Gateway (port 8080)
│   └── src/main/java/.../
│       ├── filter/
│       │   ├── AuthenticationFilter.java   ← JWT validation before routing
│       │   └── RouteValidator.java         ← Public vs secured routes
│       ├── util/JwtUtil.java
│       └── config/GatewayConfig.java       ← Route definitions (lb:// via Eureka)
│
├── auth-service/                    ← Authentication + Users (port 8081)
│   └── src/main/java/.../
│       ├── controller/AuthController.java
│       ├── service/
│       │   ├── AuthService.java / AuthServiceImpl.java
│       │   └── RefreshTokenService.java / RefreshTokenServiceImpl.java
│       ├── model/User.java, RefreshToken.java
│       ├── repository/UserRepository.java, RefreshTokenRepository.java
│       ├── dto/request/ + dto/response/
│       ├── security/
│       │   ├── JwtTokenProvider.java
│       │   ├── JwtAuthenticationFilter.java
│       │   └── UserDetailsServiceImpl.java
│       └── config/SecurityConfig.java
│
├── restaurant-service/              ← Restaurants + Menu Catalog (port 8082)
│   └── src/main/java/.../
│       ├── controller/RestaurantController.java, MenuController.java
│       ├── service/RestaurantService.java/.Impl, MenuService.java/.Impl
│       ├── model/Restaurant.java, MenuItem.java, Category.java
│       └── repository/ + dto/
│
├── order-service/                   ← Orders / Pedidos (port 8083)
│   └── src/main/java/.../
│       ├── controller/OrderController.java
│       ├── service/OrderService.java / OrderServiceImpl.java
│       ├── client/RestaurantClient.java    ← @FeignClient + @CircuitBreaker
│       ├── model/Order.java, OrderItem.java
│       └── repository/ + dto/
│
├── delivery-service/                ← Deliveries + Drivers (port 8084)
│   └── src/main/java/.../
│       ├── controller/DeliveryController.java
│       ├── service/DeliveryService.java / DeliveryServiceImpl.java
│       ├── client/NotificationClient.java  ← @FeignClient + @CircuitBreaker
│       ├── model/Delivery.java, Driver.java
│       └── repository/ + dto/
│
└── notification-service/            ← Notifications (port 8085)
    └── src/main/java/.../
        ├── controller/NotificationController.java
        ├── service/NotificationService.java / NotificationServiceImpl.java
        └── model/Notification.java
```

---

## ⚙️ Configuration

Every service reads its configuration from Config Server at startup. The only thing each service's local `application.yml` contains is its name and where to find Config Server:

```yaml
spring:
  application:
    name: auth-service
  config:
    import: optional:configserver:http://localhost:8888
```

The actual configuration (database URL, JWT secret, Eureka address, Resilience4j settings) lives in `config-server/src/main/resources/config/<service-name>.yml`.

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

This starts PostgreSQL (port 5432) and pgAdmin (port 5050). The init script creates one database per service automatically:

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

> ⚠️ Config Server must be running before any other service starts. Discovery Server must be running before the Gateway and business services register.

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

Each microservice owns its own PostgreSQL database — they never share tables or join across databases. Cross-service data is referenced by ID only, and fetched via Feign calls when needed.

| Service | Database |
|---|---|
| auth-service | auth_db |
| restaurant-service | restaurant_db |
| order-service | order_db |
| delivery-service | delivery_db |
| notification-service | notification_db |

Flyway runs automatically on startup and applies migrations from `src/main/resources/db/migration/`.

Access pgAdmin at `http://localhost:5050`

| Field | Value |
|---|---|
| Email | admin@deliveryapp.com (or your .env value) |
| Password | admin123 (or your .env value) |
| PostgreSQL host | `postgres` (Docker service name, not localhost) |
| Port | 5432 |

---

## 🔐 Security Flow

```
POST /api/v1/auth/register  →  201 { accessToken, refreshToken, user }
POST /api/v1/auth/login     →  200 { accessToken, refreshToken, user }

GET  /api/v1/orders         →  Authorization: Bearer <accessToken>
                               API Gateway validates JWT → forwards to order-service
                               Downstream services trust X-Auth-User header

POST /api/v1/auth/refresh   →  body: { refreshToken }
                           →   200 { new accessToken, new refreshToken }

POST /api/v1/auth/logout    →  body: { refreshToken }  →  204
```

Access tokens expire in 15 minutes. Refresh tokens expire in 7 days and are rotated on every use (each refresh call invalidates the old token and issues a new one).

The API Gateway validates the JWT before forwarding any secured request. If valid, it injects the resolved username as an `X-Auth-User` header so downstream services don't need to re-parse the token.

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
| `POST` | `/api/v1/orders` | 🔒 | Create an order (validates items against restaurant-service) |
| `GET` | `/api/v1/orders/{id}` | 🔒 | Get order by ID |
| `GET` | `/api/v1/orders/user/{userId}` | 🔒 | List orders by user |
| `PATCH` | `/api/v1/orders/{id}/status` | 🔒 | Update order status |
| `PATCH` | `/api/v1/orders/{id}/cancel` | 🔒 | Cancel an order |

### Delivery (`/api/v1/delivery`)

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `POST` | `/api/v1/delivery` | 🔒 | Create a delivery record for an order |
| `GET` | `/api/v1/delivery/{id}` | 🔒 | Get delivery by ID |
| `GET` | `/api/v1/delivery/order/{orderId}` | 🔒 | Get delivery by order |
| `PATCH` | `/api/v1/delivery/{id}/assign` | 🔒 | Assign next available driver |
| `PATCH` | `/api/v1/delivery/{id}/status` | 🔒 | Update delivery status |

### Notifications (`/api/v1/notifications`)

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `POST` | `/api/v1/notifications` | 🔒 | Send a notification (called by other services) |
| `GET` | `/api/v1/notifications/user/{userId}` | 🔒 | Get notifications for a user |

---

## ⚡ Resilience4j

`order-service` and `delivery-service` protect their outbound Feign calls with circuit breakers and retry policies.

```
order-service → restaurant-service
  @CircuitBreaker: opens after 50% failure rate in 10-call window
  @Retry: 3 attempts with exponential backoff (1s, 2s, 4s)
  Fallback: throws MenuItemUnavailableException (400)

delivery-service → notification-service
  @CircuitBreaker: opens after 50% failure rate in 10-call window
  @Retry: 2 attempts, 500ms wait
  Fallback: silently swallowed — notifications are best-effort
```

The fallback philosophy differs intentionally: a missing menu item is a hard blocker for order creation. A failed "your driver is on the way" notification is not — the delivery proceeds regardless.

Health check endpoints expose circuit breaker state:
```
GET http://localhost:8083/actuator/health  ← order-service
GET http://localhost:8084/actuator/health  ← delivery-service
```

---

## 🔍 Useful URLs

| URL | Description |
|---|---|
| `http://localhost:8761` | Eureka dashboard — see all registered services |
| `http://localhost:5050` | pgAdmin — inspect databases and tables |
| `http://localhost:8081/swagger-ui.html` | Auth Service Swagger UI |
| `http://localhost:8082/swagger-ui.html` | Restaurant Service Swagger UI |
| `http://localhost:8083/swagger-ui.html` | Order Service Swagger UI |
| `http://localhost:8084/swagger-ui.html` | Delivery Service Swagger UI |
| `http://localhost:8085/swagger-ui.html` | Notification Service Swagger UI |
| `http://localhost:8888/auth-service/default` | Verify Config Server is serving auth config |

---

## 🧠 Design Decisions

**One database per service** — Each microservice owns its schema exclusively. No shared tables, no cross-database JOINs. When a service needs data from another, it calls it via Feign. This enforces true independence and lets each service evolve its schema without coordinating with others.

**Cross-service references as plain Longs** — `order-service` stores `restaurantId` as a `Long`, not a JPA `@ManyToOne`. The `Restaurant` entity lives in a different database; there is no foreign key to reference. This is the most visible rule of microservices data modeling in this codebase.

**Price snapshot at order time** — `OrderItem` stores `menuItemName` and `unitPrice` as snapshots copied from `restaurant-service` at the moment the order is created. If the restaurant changes its prices tomorrow, historical orders still show what the customer actually paid.

**Interface + Implementation on every service** — Every service class is defined as an interface and implemented separately. Controllers inject the interface. This makes every service layer independently mockable in unit tests with Mockito, and makes the implementation swappable without touching the controller.

**Gateway-level JWT validation** — The API Gateway validates the JWT once, centrally, before forwarding any secured request. Downstream services receive the resolved username via the `X-Auth-User` header and trust it without re-parsing the token. This avoids duplicating JWT validation logic across 5 services.

**Fallback philosophy by criticality** — `RestaurantClient` in `order-service` throws a domain exception on fallback because a missing menu item is a hard blocker: the order genuinely cannot be created. `NotificationClient` in `delivery-service` silently swallows the fallback because a notification failure should never block a delivery from being assigned or marked complete.

**Config Server with native profile** — Configuration is centralized in `config-server/src/main/resources/config/`, one file per service. Using the `native` profile (local filesystem) keeps the setup self-contained for development. In production, swap to `git` profile to get versioned configuration history.

**Minimal local `application.yml`** — Each microservice's local `application.yml` contains only its name and the Config Server URL. Everything else (port, database, JWT secret, Resilience4j settings) comes from Config Server. This makes environment-specific overrides a single-file change.

---

## 📄 License

This project is for educational purposes.
