# 🛵 DeliveryApp — Backend

A microservices backend inspired by PedidosYa, built with Spring Boot and Spring Cloud. Covers the full delivery lifecycle: user authentication, restaurant catalog, order placement, payment processing, driver assignment, and notifications — each as an independent deployable service communicating via REST (Feign) and asynchronous events (Kafka).

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
| MercadoPago Checkout Pro | Payment processing |
| SpringDoc / Swagger UI | API documentation |
| Lombok | Boilerplate reduction |
| Docker + Docker Compose | PostgreSQL, pgAdmin, Kafka, Zookeeper and Kafka UI |
| JUnit 5 + Mockito | Unit testing |
| Maven | Dependency management and build |
| GitHub Actions | CI/CD pipeline (build, test, release) |
| ngrok | Expose local webhook endpoint for MercadoPago |

---

## 🏗️ Architecture

```
Client (Postman / Frontend)
           ↓
      API Gateway :8080
           ↓  validates JWT, routes by path prefix
─────────────────────────────────────────────────────────────────
                  Eureka Discovery Server :8761
─────────────────────────────────────────────────────────────────
    ↓           ↓           ↓            ↓            ↓
auth-service  restaurant  order-service  payment-    delivery-
  :8081        -service     :8083        service      service
               :8082          │           :8086        :8084
                         Feign →            │             │
                         restaurant    Kafka →       Kafka →
                                    [payment-     [delivery-
                                     approved]      status]
                                         │             │
                                    order-service  notification
                                    (consumer)      -service
                                         │           :8085
                                    Kafka →
                                  [order-confirmed]
                                         │
                                  delivery-service
                                    (consumer)
─────────────────────────────────────────────────────────────────
                    Config Server :8888
           (serves application.yml to every service)
```

### Inter-service communication

**Synchronous (OpenFeign):** `order-service` calls `restaurant-service` to validate menu items at order creation time. A response is required before the order can be saved.

**Asynchronous (Kafka):** Everything else flows through events. When a payment is approved, `payment-service` publishes a `PAYMENT_APPROVED` event. `order-service` consumes it and confirms the order, then publishes `ORDER_CONFIRMED`. `delivery-service` consumes that and creates a delivery record, then publishes `DRIVER_ASSIGNED` / `ORDER_DELIVERED` events that `notification-service` consumes to send notifications.

---

## 📁 Project Structure

```
delivery-app/
├── pom.xml                           ← Parent POM (BOM)
├── docker-compose.yml                ← PostgreSQL, pgAdmin, Kafka, Zookeeper, Kafka UI
├── .env.example
├── .github/
│   └── workflows/
│       ├── ci.yml
│       └── release.yml
│
├── config-server/                    ← Spring Cloud Config Server (port 8888)
│   └── src/main/resources/config/
│       ├── discovery-server.yml
│       ├── api-gateway.yml
│       ├── auth-service.yml
│       ├── restaurant-service.yml
│       ├── order-service.yml
│       ├── payment-service.yml
│       ├── delivery-service.yml
│       └── notification-service.yml
│
├── discovery-server/                 ← Eureka Server (port 8761)
├── api-gateway/                      ← Spring Cloud Gateway (port 8080)
├── auth-service/                     ← Authentication + Users (port 8081)
├── restaurant-service/               ← Restaurants + Menu Catalog (port 8082)
│
├── order-service/                    ← Orders (port 8083)
│   └── event/
│       ├── OrderConfirmedEvent.java
│       ├── PaymentApprovedEvent.java
│       ├── PaymentRejectedEvent.java
│       └── PaymentEventConsumer.java
│
├── payment-service/                  ← MercadoPago Payments (port 8086)
│   └── src/main/java/.../
│       ├── controller/PaymentController.java
│       ├── service/PaymentService.java / PaymentServiceImpl.java
│       ├── model/Payment.java
│       ├── event/PaymentApprovedEvent.java
│       ├── event/PaymentRejectedEvent.java
│       ├── event/PaymentEventProducer.java
│       └── config/MercadoPagoConfig.java
│
├── delivery-service/                 ← Deliveries + Drivers (port 8084)
└── notification-service/             ← Notifications (port 8085)
```

---

## ⚙️ Configuration

Every service reads its configuration from Config Server at startup:

```yaml
spring:
  application:
    name: payment-service
  config:
    import: optional:configserver:http://localhost:8888
```

### Environment variables

Copy `.env.example` to `.env`:

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
| `MERCADOPAGO_ACCESS_TOKEN` | MercadoPago sandbox Access Token (APP_USR-...) |

---

## 🚀 Running the Project

### Prerequisites

- Java 17
- Maven 3.8+
- Docker Desktop
- ngrok (for MercadoPago webhooks)

### 1. Start infrastructure containers

```bash
docker compose up -d
```

Creates: PostgreSQL, pgAdmin, Zookeeper, Kafka, Kafka UI and all databases:

```
auth_db / restaurant_db / order_db / payment_db / delivery_db / notification_db
```

### 2. Start ngrok (required for MercadoPago webhooks)

```bash
ngrok http 8086
```

Copy the generated URL (e.g. `https://abc123.ngrok-free.app`) and set it in `config-server/src/main/resources/config/payment-service.yml`:

```yaml
app:
  payment:
    base-url: https://abc123.ngrok-free.app
```

Also configure the webhook URL in your MercadoPago Developers dashboard:
- **URL:** `https://abc123.ngrok-free.app/api/v1/payments/webhook`
- **Events:** Pagos (legacy)

### 3. Start services in order

```bash
# Terminal 1 — Config Server (must be first)
cd config-server && mvn spring-boot:run

# Terminal 2 — Discovery Server
cd discovery-server && mvn spring-boot:run

# Terminal 3 — API Gateway
cd api-gateway && mvn spring-boot:run

# Terminals 4-9 — Business services
cd auth-service && mvn spring-boot:run
cd restaurant-service && mvn spring-boot:run
cd order-service && mvn spring-boot:run
cd payment-service && mvn spring-boot:run
cd delivery-service && mvn spring-boot:run
cd notification-service && mvn spring-boot:run
```

> ⚠️ If `MERCADOPAGO_ACCESS_TOKEN` is set as a system environment variable, run `payment-service` in the same terminal where the variable is set.

### Stop containers

```bash
docker compose down        # stop and remove containers
docker compose down -v     # also remove volumes (wipes all data)
```

---

## 🗄️ Database

Each microservice owns its own PostgreSQL database. No shared tables, no cross-database joins.

| Service | Database |
|---|---|
| auth-service | auth_db |
| restaurant-service | restaurant_db |
| order-service | order_db |
| payment-service | payment_db |
| delivery-service | delivery_db |
| notification-service | notification_db |

Access pgAdmin at `http://localhost:5050`

| Field | Value |
|---|---|
| Email | admin@deliveryapp.com |
| Password | admin123 |
| PostgreSQL host | `postgres` (Docker service name, not localhost) |
| Port | 5432 |

---

## 💳 Payment Flow (MercadoPago Checkout Pro)

```
POST /api/v1/payments/create  { orderId, userId, amount, description }
        ↓
payment-service creates a MercadoPago preference
        ↓
Returns { initPoint: "https://www.mercadopago.com.ar/checkout/..." }
        ↓
Customer pays at that URL
        ↓
MercadoPago calls POST /api/v1/payments/webhook (via ngrok)
        ↓
payment-service updates payment record in payment_db
        ↓
Publishes PaymentApprovedEvent or PaymentRejectedEvent → Kafka
        ↓
order-service consumer → order status: CONFIRMED or CANCELLED
        ↓
order-service publishes OrderConfirmedEvent → Kafka
        ↓
delivery-service → creates delivery record automatically
        ↓
delivery-service publishes DeliveryStatusEvent → Kafka
        ↓
notification-service → sends notification to the user
```

### Test Cards (MercadoPago sandbox)

| Card | Number | CVV | Expiry | Result |
|---|---|---|---|---|
| Visa | 4509 9535 6623 3704 | 123 | 11/25 | Approved |
| Mastercard | 5031 7557 3453 0604 | 123 | 11/25 | Approved |
| Visa | 4000 0000 0000 0002 | 123 | 11/25 | Rejected |

> Use `APRO` as cardholder name to simulate approval, `OTHE` for rejection.
> Must pay with a MercadoPago test buyer account (create one in the Developers dashboard → Cuentas de prueba).

---

## 📨 Kafka Topics

| Topic | Producer | Consumer | Trigger |
|---|---|---|---|
| `payment-approved` | payment-service | order-service | MercadoPago payment approved |
| `payment-rejected` | payment-service | order-service | MercadoPago payment rejected |
| `order-confirmed` | order-service | delivery-service | Order status → CONFIRMED |
| `delivery-status` | delivery-service | notification-service | Driver assigned or delivery completed |

Access Kafka UI at `http://localhost:8090` to inspect topics, messages and consumer groups.

---

## 🔐 Security Flow

```
POST /api/v1/auth/register  →  201 { accessToken, refreshToken, user }
POST /api/v1/auth/login     →  200 { accessToken, refreshToken, user }

GET  /api/v1/orders         →  Authorization: Bearer <accessToken>
                               API Gateway validates JWT → forwards downstream
                               Services trust X-Auth-User header

POST /api/v1/auth/refresh   →  body: { refreshToken }  →  200 { new tokens }
POST /api/v1/auth/logout    →  body: { refreshToken }  →  204

POST /api/v1/payments/webhook → Public (called by MercadoPago, no JWT)
```

### User Roles

| Role | Description |
|---|---|
| `ROLE_CUSTOMER` | Places orders and makes payments |
| `ROLE_RESTAURANT_OWNER` | Manages restaurant and menu |
| `ROLE_DRIVER` | Receives delivery assignments |
| `ROLE_ADMIN` | Full access |

---

## 🌐 API Endpoints

All requests go through the API Gateway at `http://localhost:8080`.

### Authentication

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `POST` | `/api/v1/auth/register` | Public | Register a new user |
| `POST` | `/api/v1/auth/login` | Public | Login and get tokens |
| `POST` | `/api/v1/auth/refresh` | Public | Refresh access token |
| `POST` | `/api/v1/auth/logout` | Public | Invalidate refresh token |

### Restaurants

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `POST` | `/api/v1/restaurants` | 🔒 | Create a restaurant |
| `GET` | `/api/v1/restaurants` | 🔒 | List all restaurants |
| `GET` | `/api/v1/restaurants/{id}` | 🔒 | Get restaurant by ID |
| `GET` | `/api/v1/restaurants/owner/{ownerId}` | 🔒 | Get restaurants by owner |
| `PUT` | `/api/v1/restaurants/{id}` | 🔒 | Update a restaurant |
| `DELETE` | `/api/v1/restaurants/{id}` | 🔒 | Delete a restaurant |

### Menu

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `POST` | `/api/v1/menu` | 🔒 | Create a menu item |
| `GET` | `/api/v1/menu/{id}` | 🔒 | Get menu item by ID |
| `GET` | `/api/v1/menu/restaurant/{restaurantId}` | 🔒 | List menu for a restaurant |
| `PUT` | `/api/v1/menu/{id}` | 🔒 | Update a menu item |
| `PATCH` | `/api/v1/menu/{id}/availability` | 🔒 | Toggle item availability |
| `DELETE` | `/api/v1/menu/{id}` | 🔒 | Delete a menu item |

### Orders

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `POST` | `/api/v1/orders` | 🔒 | Create an order (validates items via Feign) |
| `GET` | `/api/v1/orders/{id}` | 🔒 | Get order by ID |
| `GET` | `/api/v1/orders/user/{userId}` | 🔒 | List orders by user |
| `PATCH` | `/api/v1/orders/{id}/status` | 🔒 | Update order status |
| `PATCH` | `/api/v1/orders/{id}/cancel` | 🔒 | Cancel an order |

### Payments

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `POST` | `/api/v1/payments/create` | 🔒 | Create MercadoPago preference and get payment URL |
| `POST` | `/api/v1/payments/webhook` | Public | Webhook called by MercadoPago with payment result |
| `GET` | `/api/v1/payments/order/{orderId}` | 🔒 | Get payment status by order |

### Delivery

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `POST` | `/api/v1/delivery` | 🔒 | Create a delivery record |
| `GET` | `/api/v1/delivery/{id}` | 🔒 | Get delivery by ID |
| `GET` | `/api/v1/delivery/order/{orderId}` | 🔒 | Get delivery by order |
| `PATCH` | `/api/v1/delivery/{id}/assign` | 🔒 | Assign next available driver |
| `PATCH` | `/api/v1/delivery/{id}/status` | 🔒 | Update delivery status |

### Notifications

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `POST` | `/api/v1/notifications` | 🔒 | Send a notification |
| `GET` | `/api/v1/notifications/user/{userId}` | 🔒 | Get notifications for a user |

---

## ⚡ Resilience4j

```
order-service → restaurant-service
  @CircuitBreaker: opens after 50% failure rate in a 10-call window
  @Retry: 3 attempts with exponential backoff (1s → 2s → 4s)
  Fallback: throws MenuItemUnavailableException (400)
```

---

## 🔁 CI/CD — GitHub Actions

**`ci.yml`** — Triggered on every push and pull request. Compiles the full project and runs all unit tests across every service.

**`release.yml`** — Triggered on version tags. Builds, packages and creates a GitHub Release.

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
| `http://localhost:8086/swagger-ui.html` | Payment Service Swagger UI |
| `http://localhost:8084/swagger-ui.html` | Delivery Service Swagger UI |
| `http://localhost:8085/swagger-ui.html` | Notification Service Swagger UI |
| `http://localhost:8888/payment-service/default` | Verify Config Server serving payment config |

---

## 🧠 Design Decisions

**One database per service** — Each microservice owns its schema exclusively. No shared tables, no cross-database joins. Cross-service data is referenced by ID only.

**Synchronous vs asynchronous by use case** — Feign is used when an immediate response is required (validating menu items before saving an order). Kafka is used for downstream side effects that don't need to block the caller (payment result → order confirmation → delivery creation → notifications).

**MercadoPago webhook pattern** — The payment result arrives asynchronously via webhook. `payment-service` persists the payment record before calling MercadoPago, guaranteeing a local record exists even if the webhook is delayed. The webhook handler updates the status and publishes a Kafka event — `order-service` never polls for payment status, it just reacts to the event.

**Independent event contracts** — Each service defines its own copy of the events it produces or consumes. No shared JAR between services. Jackson matches fields by name at runtime.

**Cross-service references as plain Longs** — No JPA `@ManyToOne` relationships across service boundaries. Each service stores only the ID of entities owned by other services.

**Price snapshot at order time** — `OrderItem` stores `menuItemName` and `unitPrice` at purchase time. Historical orders always show what the customer actually paid, regardless of future price changes.

**Interface + Implementation on every service** — Controllers inject interfaces, making every service layer independently mockable in unit tests with Mockito.

**Gateway-level JWT validation** — JWT validated once at the API Gateway before forwarding any secured request. Downstream services receive the resolved username via `X-Auth-User` header and trust it without re-parsing the token.

**Config Server with native profile** — All configuration centralized in `config-server/src/main/resources/config/`. Swap to `git` profile in production for versioned configuration history.

---

## 📄 License

This project is for educational purposes.
