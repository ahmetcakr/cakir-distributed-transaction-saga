# Cakir Distributed Transaction Saga

A demonstration of the **Orchestration-based Saga Pattern** for managing distributed transactions across multiple microservices. This project simulates an e-commerce order flow (order → stock reservation → payment) with full compensating transaction support.

---

## Architecture

The system consists of **4 independent Spring Boot microservices**, each with its own PostgreSQL database. Services communicate exclusively via **Apache Kafka** — there are no direct HTTP calls between them.

```
Client
  │
  ▼ REST POST /orders
Order Service (6001)
  │ publishes ORDER_CREATED
  ▼
[Kafka: order-events]
  │
  ▼
Saga Orchestrator (6003)  ◄──── coordinates the entire flow
  │                    │
  │ RESERVE_STOCK      │ PROCESS_PAYMENT
  ▼                    ▼
Stock Service (6004)  Payment Service (6002)
  │ STOCK_RESERVED/    │ PAYMENT_SUCCESS/
  │ STOCK_RELEASED     │ PAYMENT_FAILED
  └────────┬───────────┘
           │
           ▼
    Saga Orchestrator
           │ ORDER_COMPLETED / ORDER_CANCELLED / ORDER_FAILED
           ▼
    [Kafka: order-status-updates]
           │
           ▼
    Order Service (finalizes order status)
```

### Kafka Topics

| Topic                  | Producer           | Consumer           |
|------------------------|--------------------|--------------------|
| `order-events`         | order-service      | saga-orchestrator  |
| `stock-commands`       | saga-orchestrator  | stock-service      |
| `stock-events`         | stock-service      | saga-orchestrator  |
| `payment-commands`     | saga-orchestrator  | payment-service    |
| `payment-events`       | payment-service    | saga-orchestrator  |
| `order-status-updates` | saga-orchestrator  | order-service      |

### Retry and DLQ Topics

Consumer bindings use retry with exponential backoff (`maxAttempts=4`, initial `1s`, max `10s`, multiplier `2.0`).
If processing still fails after retries, message is sent to a Dead Letter Queue topic:

| Main Topic             | DLQ Topic                    |
|------------------------|------------------------------|
| `order-events`         | `order-events.dlq`           |
| `stock-events`         | `stock-events.dlq`           |
| `payment-events`       | `payment-events.dlq`         |
| `stock-commands`       | `stock-commands.dlq`         |
| `payment-commands`     | `payment-commands.dlq`       |
| `order-status-updates` | `order-status-updates.dlq`   |

---

## Services

### Order Service — port `6001`
- Exposes `POST /orders` to create a new order
- Saves the order in `PENDING` state to `order_db`
- Publishes `ORDER_CREATED` to `order-events`
- Listens on `order-status-updates` and finalizes the order as `COMPLETED`, `CANCELLED`, or `FAILED`

### Saga Orchestrator — port `6003`
- The central coordinator — drives the saga state machine without owning any business logic
- Persists saga progress in `saga_instances` table (`orderId`, `lastState`, `sagaStatus`)
- Guards against duplicate/stale messages: once a saga reaches `SUCCESS` or `FAILED`, further events are ignored
- Triggers **compensating transactions** on failure (stock release → order cancel)

### Payment Service — port `6002`
- Listens on `payment-commands`
- **processPayment**: deducts balance from `payment_db` if sufficient → publishes `PAYMENT_SUCCESS`; otherwise `PAYMENT_FAILED`
- **refundPayment**: restores balance (compensating transaction) when the saga rolls back

### Stock Service — port `6004`
- Listens on `stock-commands`
- **handleStockReserve**: checks availability, deducts stock, calculates `totalPrice = quantity × unitPrice` → publishes `STOCK_RESERVED`
- **handleStockRelease**: restores stock (compensating transaction) on payment failure

---

## Saga Flow

### Happy Path
```
ORDER_CREATED → RESERVE_STOCK → STOCK_RESERVED → PROCESS_PAYMENT → PAYMENT_SUCCESS → ORDER_COMPLETED
```

### Compensation (Rollback)
If payment fails, the orchestrator initiates compensating transactions in reverse:
```
PAYMENT_FAILED → RELEASE_STOCK → STOCK_RELEASED → ORDER_CANCELLED
```
If stock is unavailable:
```
STOCK_NOT_FOUND / STOCK_INSUFFICIENT → ORDER_FAILED (no compensation needed)
```

---

## Tech Stack

| Technology | Role |
|---|---|
| Java 21 | Language |
| Spring Boot 3.4.1 | Microservice framework |
| Spring Cloud Stream 2024.0.0 | Kafka abstraction (function-based bindings) |
| Apache Kafka | Async inter-service messaging |
| Zookeeper | Kafka coordination |
| PostgreSQL 15 | Per-service relational databases |
| Spring Data JPA / Hibernate | ORM and database access |
| Lombok | Boilerplate reduction |
| Maven | Build tool |
| Docker Compose | Local infrastructure (Kafka + Zookeeper + Postgres) |

---

## Project Structure

```
cakir-distributed-transaction-saga/
├── docker-compose.yml          # Kafka, Zookeeper, PostgreSQL
├── order-service/
├── payment-service/
├── stock-service/
└── saga-orchestrator/
```

Each service follows the same internal layout:

```
src/main/java/cakir/<service>/
├── config/         # Spring Cloud Stream Consumer/Supplier function beans
├── controller/     # REST endpoints (order-service only)
├── messaging/      # StreamBridge publishers (outbound Kafka messages)
├── model/
│   ├── dto/        # Event and Command objects for Kafka messages
│   ├── entity/     # JPA database entities
│   └── enums/      # Status enums
├── repository/     # Spring Data JPA interfaces
└── service/        # Business logic (interface + impl)
```

---

## Getting Started

### Prerequisites
- Docker & Docker Compose
- Java 21
- Maven

### 1. Start Infrastructure

```bash
docker-compose up -d
```

This starts:
- Kafka on `localhost:9092`
- Zookeeper on `localhost:2181`
- PostgreSQL on `localhost:5432` (user: `user`, password: `password`)

### 2. Create Databases

Connect to PostgreSQL and create the required databases:

```sql
CREATE DATABASE order_db;
CREATE DATABASE payment_db;
CREATE DATABASE stock_db;
CREATE DATABASE saga_db;
```

### 3. Run the Services

Start each service in a separate terminal (order matters — start the orchestrator before the others):

```bash
cd saga-orchestrator && ./mvnw spring-boot:run
cd order-service    && ./mvnw spring-boot:run
cd stock-service    && ./mvnw spring-boot:run
cd payment-service  && ./mvnw spring-boot:run
```

### 4. Create an Order

```bash
curl -X POST http://localhost:6001/orders \
  -H "Content-Type: application/json" \
  -d '{"productId": "PROD-1", "quantity": 2, "userId": 1}'
```

---

## Database Schema (auto-managed by Hibernate)

| Service | Table | Key Columns |
|---|---|---|
| order-service | `orders` | `id`, `product_id`, `quantity`, `price`, `status`, `user_id` |
| payment-service | `payments` | `id`, `user_id`, `balance` |
| stock-service | `stocks` | `id`, `product_id`, `remaining_stock`, `single_unit_price` |
| saga-orchestrator | `saga_instances` | `order_id`, `last_state`, `saga_status` |

---

## Order Status Values

| Status | Meaning |
|---|---|
| `PENDING` | Order created, saga in progress |
| `COMPLETED` | Stock reserved and payment successful |
| `CANCELLED` | Payment failed and stock was released |
| `FAILED` | Stock not found or insufficient |
