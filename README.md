# Order Inventory Microservices System

This project is a simplified microservices system built using **Spring Boot** and **Apache Kafka**, designed to simulate order placement and inventory validation in an e-commerce-like system.

## 🧱 Architecture

The system consists of two services:

### 1. `OrderService`

* Accepts order placement requests via REST API.
* Publishes `OrderPlacedEvent` to the `order-events` Kafka topic.
* Waits (up to 5 seconds) for an inventory validation response using `CompletableFuture`.
* Returns success or failure based on the inventory availability.

### 2. `InventoryService`

* Listens to the `order-events` topic.
* Checks availability for the given product ID and quantity.
* Publishes an `InventoryResponseEvent` to the `inventory-response` topic.

---

## 📦 Features

* **Kafka-based asynchronous communication** between services.
* **Spring Boot + Jackson** for JSON serialization/deserialization.
* **Timeout handling** for delayed responses.
* **CompletableFuture** used to wait for inventory responses.
* **Dead Letter Queue (optional)** handling support (can be enabled via `DefaultErrorHandler`).

---

## 🚀 Technologies Used

* Java 17+
* Spring Boot 3+
* Spring Kafka
* Apache Kafka
* Docker (for Kafka)
* Jackson (for JSON mapping)
* Lombok

---

## 💠 Running Locally

### Prerequisites

* Java 17+
* Maven
* Docker

### 1. Start Kafka with Docker

```bash
docker-compose up -d
```

Or manually:

```bash
docker network create kafka-net

docker run -d --name zookeeper --net kafka-net -p 2181:2181 \
  confluentinc/cp-zookeeper:7.5.0 \
  -e ZOOKEEPER_CLIENT_PORT=2181

docker run -d --name kafka --net kafka-net -p 9092:9092 \
  -e KAFKA_BROKER_ID=1 \
  -e KAFKA_ZOOKEEPER_CONNECT=zookeeper:2181 \
  -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092 \
  -e KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1 \
  confluentinc/cp-kafka:7.5.0
```

### 2. Start Services

Each service runs independently. Build and run both:

```bash
# From OrderService directory
mvn spring-boot:run

# From InventoryService directory
mvn spring-boot:run
```

---

## 📨 Sample API Usage

### Place an Order

```http
POST /api/orders
Content-Type: application/json

{
  "orderId": "od_123",
  "productId": "DEF888",
  "quantity": 2
}
```

**Response (Success):**

```
200 OK
Order placed successfully
```

**Response (Out of Stock):**

```
200 OK
Product not available in inventory
```

**Response (Timeout):**

```
408 Request Timeout
Timed out waiting for inventory response - Something went wrong
```
