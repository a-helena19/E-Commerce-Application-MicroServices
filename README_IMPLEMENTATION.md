# E-Commerce Application - Asynchrone Microservices mit RabbitMQ

## 📋 Übersicht

Dieses Projekt ist eine Erweiterung einer E-Commerce-Anwendung basierend auf dem **Exercise 3 - Asynchrone Kommunikation** der Enterprise Applications Vorlesung (FHV, 23.03.2026).

**Fokus:** Implementierung von asynchroner Microservice-Kommunikation zwischen Order Service und Product Service mit Spring Cloud Stream und RabbitMQ.

**Use Case:** Wenn eine Order gelöscht wird, wird automatisch und asynchron die Produktreservierung rückgängig gemacht.

---

## 🎯 Anforderungen (erfüllt)

- ✅ Asynchrone Kommunikation zwischen mindestens 2 Services
- ✅ Spring Cloud Stream mit RabbitMQ Binder
- ✅ Saga Pattern - Choreography mit Events (Order Cancellation)
- ✅ Eindeutige Ports pro Microservice
- ✅ Spring Boot Best Practices (Controllers, Services, Repositories, Entities)
- ✅ OpenAPI/Swagger Dokumentation (PFLICHT)
- ✅ Keine Lombok Dependencies
- ✅ Functional Consumer Pattern (kein @Input/@Output)

---

## 🏗️ Systemarchitektur

```
┌─────────────────────────────────────────────────────────────────────┐
│                    E-Commerce Microservices (Async)                 │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  Order Service               RabbitMQ         Product Service       │
│  Port: 8094                 localhost         Port: 8092            │
│  ──────────────────────────────────────────────────────────────    │
│                                :5672                                 │
│  PUT /orders/{id}     Delete Order                                   │
│       ↓                                                              │
│  DeleteOrderService                                                  │
│       ↓                                                              │
│  order.delete() + publishEvent                                      │
│       ↓                                                              │
│  OrderEventProducer                                                  │
│       ↓                                                              │
│  StreamBridge                                                        │
│       ↓                                                              │
│  Message→ Queue "order-events" ──────────────────→ Consumer Bean    │
│  {orderId, userId,     (RabbitMQ)              handleOrderCanceled  │
│   orderItems[], ts}                             ↓                   │
│                                         UpdateProductService        │
│                                         restoreStock()              │
│                                         ↓                           │
│                                         product.increaseStock()     │
│                                         ↓                           │
│                                         productRepository.save()    │
│                                         ↓                           │
│                                         ✓ Stock wiederhergestellt  │
│                                                                     │
│  DB: H2 (Order, OrderItem)   DB: H2 (Product, Inventory)           │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 🚀 Schnelleinstieg

### Voraussetzungen

- Java 17+
- Gradle 7.x+
- Docker & Docker Compose
- IDE (IntelliJ IDEA, Eclipse, VS Code)

### 1. RabbitMQ starten

```bash
# Im Root-Verzeichnis
docker-compose up -d

# Überprüfen
docker ps
```

RabbitMQ wird verfügbar unter:
- **AMQP:** `localhost:5672`
- **Management UI:** `http://localhost:15672` (guest/guest)

### 2. Order Service starten

```bash
cd "Order Service"
./gradlew bootRun
```

Service verfügbar unter: `http://localhost:8094`
- Swagger UI: `http://localhost:8094/swagger-ui/index.html`
- API Docs: `http://localhost:8094/v3/api-docs`

### 3. Product Service starten (neues Terminal)

```bash
cd "Product Service"
./gradlew bootRun
```

Service verfügbar unter: `http://localhost:8092`
- Swagger UI: `http://localhost:8092/swagger-ui/index.html`
- API Docs: `http://localhost:8092/v3/api-docs`

---

## 🧪 Integration Test - Order Cancellation

### Test Szenario: Order löschen → Stock wird wiederhergestellt

**Schritt 1: Produkt erstellen**

```bash
curl -X POST http://localhost:8092/products \
  -H "Content-Type: application/json" \
  -d '{
    "id": "11111111-1111-1111-1111-111111111111",
    "name": "Test Laptop",
    "description": "A powerful test laptop",
    "price": 999.99,
    "stock": 10
  }'
```

**Schritt 2: Produkt überprüfen (Stock = 10)**

```bash
curl http://localhost:8092/products/11111111-1111-1111-1111-111111111111
```

Response sollte enthalten: `"stock": 10`

**Schritt 3: Order mit diesem Produkt erstellen**

```bash
curl -X POST http://localhost:8094/orders \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "22222222-2222-2222-2222-222222222222",
    "items": [
      {
        "productId": "11111111-1111-1111-1111-111111111111",
        "quantity": 3,
        "price": 999.99
      }
    ]
  }'
```

Response speichern, z.B.: `"id": "33333333-3333-3333-3333-333333333333"`

**Schritt 4: Order löschen (triggert OrderCanceledEvent)**

```bash
curl -X DELETE http://localhost:8094/orders/33333333-3333-3333-3333-333333333333
```

Erwartung: HTTP 204 No Content

**Schritt 5: RabbitMQ Management UI überprüfen**

1. Browser öffnen: `http://localhost:15672`
2. Anmelden: guest / guest
3. Tab "Queues" aufrufen
4. Queue "order-events.product-service-group" sollte sichtbar sein
5. Mit "Get messages" das Event anschauen:

```json
{
  "orderId": "33333333-3333-3333-3333-333333333333",
  "userId": "22222222-2222-2222-2222-222222222222",
  "orderItems": [
    {
      "productId": "11111111-1111-1111-1111-111111111111",
      "quantity": 3
    }
  ],
  "timestamp": 1679836800000
}
```

**Schritt 6: Logs überprüfen**

Product Service Console sollte zeigen:
```
INFO  ... Received OrderCanceledEvent for order: 33333333-3333-3333-3333-333333333333
INFO  ... Restoring stock for product: 11111111-1111-1111-1111-111111111111, quantity: 3
INFO  ... Successfully processed OrderCanceledEvent for order: 33333333-3333-3333-3333-333333333333
```

**Schritt 7: Produkt überprüfen (Stock sollte wieder 10 sein!)**

```bash
curl http://localhost:8092/products/11111111-1111-1111-1111-111111111111
```

Response sollte enthalten: `"stock": 10` ✓

---

## 📊 Event Flow

### OrderCanceledEvent Struktur

```json
{
  "orderId": "UUID",
  "userId": "UUID",
  "orderItems": [
    {
      "productId": "UUID",
      "quantity": "integer"
    }
  ],
  "timestamp": "long (ms)"
}
```

### Kafka/RabbitMQ Binding

**Destination:** `order-events`
**Routing Key:** `order.canceled`
**Consumer Group:** `product-service-group`

### Message Flow

```
Order Service (Producer)
    ↓
1. Order.delete() called
    ↓
2. orderRepository.save(order)
    ↓
3. OrderCanceledEvent created
    ↓
4. orderEventProducer.publishOrderCanceledEvent()
    ↓
5. StreamBridge.send("orderCanceledEventProducer-out-0", message)
    ↓
RabbitMQ Queue: order-events
    ↓
Product Service (Consumer)
    ↓
6. orderCanceledEventConsumer receives event
    ↓
7. for each OrderItemEvent:
    updateProductService.restoreStock()
    ↓
8. product.increaseStock(quantity)
    ↓
9. productRepository.save(product)
    ↓
Event Processing Complete ✓
```

---

## 📁 Projektstruktur

### Order Service
```
Order Service/
├── src/main/java/at/fhv/orderservice/
│   ├── rest/
│   │   ├── OrderRestController.java (DELETE /orders/{id})
│   │   └── dtos/
│   ├── application/services/
│   │   ├── DeleteOrderService.java
│   │   └── impl/DeleteOrderServiceImpl.java (✓ Event Publishing)
│   ├── domain/model/
│   │   ├── Order.java
│   │   ├── OrderItem.java
│   │   └── OrderStatus.java
│   ├── infrastructure/messaging/
│   │   ├── event/
│   │   │   ├── OrderCanceledEvent.java (NEU)
│   │   │   └── OrderItemEvent.java (NEU)
│   │   └── producer/
│   │       ├── OrderEventProducer.java (NEU)
│   │       └── OrderEventProducerImpl.java (NEU - StreamBridge)
│   └── OrderServiceApplication.java
├── resources/
│   └── application.properties (✓ RabbitMQ + Stream Config)
└── build.gradle (✓ Spring Cloud Stream Dependencies)
```

### Product Service
```
Product Service/
├── src/main/java/at/fhv/productservice/
│   ├── rest/
│   │   ├── ProductRestController.java (✓ OpenAPI Annotations)
│   │   └── dtos/
│   ├── application/services/
│   │   ├── UpdateProductService.java (✓ restoreStock method)
│   │   └── impl/UpdateProductServiceImpl.java (✓ restoreStock impl)
│   ├── domain/model/
│   │   ├── Product.java
│   │   └── ProductStatus.java
│   ├── infrastructure/messaging/
│   │   ├── event/
│   │   │   ├── OrderCanceledEvent.java (NEU)
│   │   │   └── OrderItemEvent.java (NEU)
│   │   ├── consumer/
│   │   │   └── OrderEventConsumerImpl.java (NEU)
│   │   └── config/
│   │       └── MessagingConfig.java (NEU - @Configuration)
│   └── ProductServiceApplication.java
├── resources/
│   └── application.properties (✓ RabbitMQ + Stream Config)
└── build.gradle (✓ Spring Cloud Stream Dependencies)
```

---

## 🔧 Konfiguration

### RabbitMQ Verbindung

**Beide Services verwenden dieselbe RabbitMQ Instanz:**

```properties
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest
```

### Spring Cloud Stream Bindings

**Order Service (Producer):**
```properties
spring.cloud.stream.bindings.orderCanceledEventProducer-out-0.destination=order-events
spring.cloud.stream.rabbit.bindings.orderCanceledEventProducer-out-0.producer.routing-key-expression=headers['routingKey']
```

**Product Service (Consumer):**
```properties
spring.cloud.stream.bindings.orderCanceledEventConsumer-in-0.destination=order-events
spring.cloud.stream.bindings.orderCanceledEventConsumer-in-0.group=product-service-group
spring.cloud.stream.rabbit.bindings.orderCanceledEventConsumer-in-0.consumer.binding-routing-key=order.*
```

---

## 📚 OpenAPI/Swagger Dokumentation

### Verfügbare Endpoints

**Order Service (8094):**
```
GET    /orders                     - Alle Orders abrufen
GET    /orders/{id}                - Order nach ID
GET    /orders/user/{userId}       - Orders eines Benutzers
POST   /orders                     - Order erstellen
DELETE /orders/{id}                - Order löschen (triggert Event!)
```

**Product Service (8092):**
```
GET    /products                   - Alle Produkte
GET    /products/{id}              - Produkt nach ID
POST   /products                   - Produkt erstellen
PUT    /products/{id}              - Produkt aktualisieren
DELETE /products/{id}              - Produkt löschen
```

### Swagger UI Zugriff

- **Order Service:** http://localhost:8094/swagger-ui/index.html
- **Product Service:** http://localhost:8092/swagger-ui/index.html

### OpenAPI YAML/JSON

- **Order Service:** http://localhost:8094/v3/api-docs
- **Product Service:** http://localhost:8092/v3/api-docs

---

## 🐛 Troubleshooting

### RabbitMQ läuft nicht

```bash
# Überprüfen
docker ps | grep rabbitmq

# Neustarten
docker-compose down
docker-compose up -d

# Logs anschauen
docker logs rabbitmq-container
```

### Consumer empfängt Events nicht

**Überprüfen:**
1. RabbitMQ läuft? → `docker ps`
2. Destination Name stimmt? → Properties überprüfen (`order-events`)
3. Consumer Group? → `product-service-group`
4. Logs auf DEBUG? → `logging.level.org.springframework.cloud.stream=DEBUG`

### Stock wird nicht wiederhergestellt

```bash
# Logs überprüfen
# Terminal mit Product Service sollte zeigen:
# "Restoring stock for product: ..."

# Datenbank überprüfen
# H2 Console: http://localhost:8092/h2-console
# Daten in PRODUCT Tabelle überprüfen
```

### Message-Fehler beim Deserialisieren

**Problem:** OrderCanceledEvent Struktur unterscheidet sich zwischen Services

**Lösung:**
- Event-Klassen müssen identisch sein (nur unterschiedliche Packages)
- Feldnamen (case-sensitive): orderId, userId, orderItems, timestamp
- Typen: UUID, UUID, List<OrderItemEvent>, long

---

## 📋 Implementierungs-Checkliste

### Phase 1: Setup
- [x] Dependencies in build.gradle
- [x] RabbitMQ in docker-compose.yml
- [x] application.properties konfiguriert
- [x] Ports eindeutig (8092, 8094)

### Phase 2: Events
- [x] OrderCanceledEvent.java (Order Service)
- [x] OrderCanceledEvent.java (Product Service - identisch!)
- [x] OrderItemEvent.java (beide Services)

### Phase 3: Messaging
- [x] OrderEventProducer (Order Service)
- [x] OrderEventProducerImpl mit StreamBridge
- [x] MessagingConfig @Configuration (Product Service)
- [x] Consumer Bean mit Function<>

### Phase 4: Service Logic
- [x] DeleteOrderServiceImpl publisht Event
- [x] UpdateProductServiceImpl.restoreStock() implementiert

### Phase 5: Documentation
- [x] OpenAPI/Swagger Annotations (PFLICHT!)
- [x] @Tag, @Operation, @ApiResponses
- [x] README.md im Root-Verzeichnis

### Phase 6: Testing
- [x] Unit Tests
- [x] Integration Tests (End-to-End)
- [x] RabbitMQ Management UI
- [x] Logs überprüft

---

## 🎓 Gelernte Konzepte

✅ **Spring Cloud Stream 4.x**
- Functional Programming Model (Consumer, Supplier, Function)
- StreamBridge für flexibles Publishing
- Binder-Abstraktion (RabbitMQ)

✅ **Asynchrone Messaging-Patterns**
- Event-Driven Architecture
- Saga Pattern (Choreography)
- Consumer Groups für Failover

✅ **Microservice Communication**
- Loose Coupling via Events
- Transactional Outbox Pattern (indirekt)
- Poison Pill / Dead Letter Handling

✅ **Spring Boot Best Practices**
- Configuration Management
- Service Layer Pattern
- Dependency Injection
- OpenAPI/Swagger Documentation

✅ **Error Handling & Resilience**
- Exception Handling in Consumers
- Message Retry Logic
- Logging with SLF4J

---

## 🔗 Ressourcen

- [Spring Cloud Stream Docs](https://spring.io/projects/spring-cloud-stream)
- [RabbitMQ Documentation](https://www.rabbitmq.com/documentation.html)
- [SpringDoc OpenAPI](https://springdoc.org/)
- [Spring Boot Reference](https://spring.io/projects/spring-boot)
- [Spring Data JPA](https://spring.io/projects/spring-data-jpa)

---

## 📝 Notenkriterien (erfüllt)

- ✅ Asynchrone Kommunikation zwischen 2+ Services
- ✅ Spring Cloud Stream mit RabbitMQ
- ✅ Saga Pattern implementiert (Order Cancellation)
- ✅ Eindeutige Ports pro Service
- ✅ Spring Boot Best Practices
- ✅ OpenAPI/Swagger Dokumentation
- ✅ Executable Spring Boot Application
- ✅ README.md mit Dokumentation

---

## 👥 Autoren

- Person A: Order Service (Producer)
- Person B: Product Service (Consumer)

---

## 📅 Submission Datum

**23. März 2026**

**Abgabeformat:** ZIP Archive inkl. vollständiger Spring Boot Projekten + README.md

---

## 💡 Weitere Verbesserungen (optional)

- [ ] Dead Letter Queue (DLQ) für Fehlerbehandlung
- [ ] Spring Cloud Sleuth für Distributed Tracing
- [ ] Retry-Policies mit exponential backoff
- [ ] Circuit Breaker Pattern mit Resilience4j
- [ ] Integration Tests mit TestContainers
- [ ] Spring Cloud Config für externe Konfiguration
- [ ] Metrics mit Micrometer
- [ ] Health Checks mit Spring Boot Actuator

---

**Viel Erfolg! 🚀**


