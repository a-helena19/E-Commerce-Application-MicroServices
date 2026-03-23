# Plan A - Person A: Order Service (Producer/Event Publisher)

## Übersicht
Person A ist verantwortlich für die Order Service Seite der asynchronen Kommunikation. Der Fokus liegt auf dem Versand von `OrderCanceledEvent` wenn eine Order gelöscht wird. Anschließend wird das Event von der Product Service konsumiert und verarbeitet.

**Usecase:** Wenn ein Order gelöscht wird → `OrderCanceledEvent` wird publisht → Product Service konsumiert das Event und stellt die Reservierungen wieder her.

---

## Phase 1: Dependencies & Konfiguration hinzufügen

### 1.1 Order Service build.gradle Update
**Datei:** `Order Service/build.gradle`

**Was zu tun ist:**
- Spring Cloud Stream Dependencies für RabbitMQ hinzufügen
- OpenAPI/Swagger Dependencies hinzufügen (PFLICHT!)
- Spring Cloud Config und weitere notwendige Dependencies

**Zu ändernde Dependencies-Sektion:**

```groovy
// Füge nach den bestehenden Spring Boot Dependencies folgende hinzu:
implementation 'org.springframework.cloud:spring-cloud-stream:4.1.0'
implementation 'org.springframework.cloud:spring-cloud-stream-binder-rabbit:4.1.0'
implementation 'org.springframework.boot:spring-boot-starter-amqp'
implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.5.0'
```

**WICHTIG:** Die `springdoc-openapi-starter-webmvc-ui` Dependency war bereits in der ursprünglichen build.gradle, also nur überprüfen, dass sie vorhanden ist!

**Zusätzlich:** Noch vor dem `tasks.named('test')` Block folgende Dependency-Management Section hinzufügen:

```groovy
dependencyManagement {
    imports {
        mavenBom 'org.springframework.cloud:spring-cloud-dependencies:2023.0.0'
    }
}
```

---

### 1.2 application.properties für Order Service Update
**Datei:** `Order Service/src/main/resources/application.properties`

**Zu ändernde/hinzufügende Properties:**

```properties
# RabbitMQ Konfiguration
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest

# Spring Cloud Stream Konfiguration für Order Service (Producer)
spring.cloud.stream.bindings.orderCanceledEventProducer-out-0.destination=order-events
spring.cloud.stream.bindings.orderCanceledEventProducer-out-0.content-type=application/json

# RabbitMQ Binder Settings
spring.cloud.stream.rabbit.bindings.orderCanceledEventProducer-out-0.producer.routing-key-expression=headers['routingKey']
```

---

## Phase 2: Event-Klasse erstellen

### 2.1 OrderCanceledEvent DTO Klasse
**Datei:** `Order Service/src/main/java/at/fhv/orderservice/infrastructure/messaging/event/OrderCanceledEvent.java`

**Zu erstellende Klasse:**

```java
package at.fhv.orderservice.infrastructure.messaging.event;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

public class OrderCanceledEvent implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private UUID orderId;
    private UUID userId;
    private List<OrderItemEvent> orderItems;
    private long timestamp;
    
    // Konstruktoren
    public OrderCanceledEvent() {
    }
    
    public OrderCanceledEvent(UUID orderId, UUID userId, List<OrderItemEvent> orderItems) {
        this.orderId = orderId;
        this.userId = userId;
        this.orderItems = orderItems;
        this.timestamp = System.currentTimeMillis();
    }
    
    // Getter und Setter
    public UUID getOrderId() {
        return orderId;
    }
    
    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }
    
    public UUID getUserId() {
        return userId;
    }
    
    public void setUserId(UUID userId) {
        this.userId = userId;
    }
    
    public List<OrderItemEvent> getOrderItems() {
        return orderItems;
    }
    
    public void setOrderItems(List<OrderItemEvent> orderItems) {
        this.orderItems = orderItems;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
```

### 2.2 OrderItemEvent Klasse
**Datei:** `Order Service/src/main/java/at/fhv/orderservice/infrastructure/messaging/event/OrderItemEvent.java`

**Zu erstellende Klasse:**

```java
package at.fhv.orderservice.infrastructure.messaging.event;

import java.io.Serializable;
import java.util.UUID;

public class OrderItemEvent implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private UUID productId;
    private int quantity;
    
    // Konstruktoren
    public OrderItemEvent() {
    }
    
    public OrderItemEvent(UUID productId, int quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }
    
    // Getter und Setter
    public UUID getProductId() {
        return productId;
    }
    
    public void setProductId(UUID productId) {
        this.productId = productId;
    }
    
    public int getQuantity() {
        return quantity;
    }
    
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
```

---

## Phase 3: Producer/Publisher Interface & Implementierung

### 3.1 OrderEventProducer Interface
**Datei:** `Order Service/src/main/java/at/fhv/orderservice/infrastructure/messaging/producer/OrderEventProducer.java`

**Zu erstellende Interface-Klasse:**

```java
package at.fhv.orderservice.infrastructure.messaging.producer;

import at.fhv.orderservice.infrastructure.messaging.event.OrderCanceledEvent;

public interface OrderEventProducer {
    void publishOrderCanceledEvent(OrderCanceledEvent event);
}
```

### 3.2 OrderEventProducerImpl Implementierung
**Datei:** `Order Service/src/main/java/at/fhv/orderservice/infrastructure/messaging/producer/OrderEventProducerImpl.java`

**Zu erstellende Implementierungs-Klasse:**

```java
package at.fhv.orderservice.infrastructure.messaging.producer;

import at.fhv.orderservice.infrastructure.messaging.event.OrderCanceledEvent;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Component
public class OrderEventProducerImpl implements OrderEventProducer {
    
    private final StreamBridge streamBridge;
    
    public OrderEventProducerImpl(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }
    
    @Override
    public void publishOrderCanceledEvent(OrderCanceledEvent event) {
        Message<OrderCanceledEvent> message = MessageBuilder
                .withPayload(event)
                .setHeader("routingKey", "order.canceled")
                .setHeader("eventType", "OrderCanceledEvent")
                .build();
        
        streamBridge.send("orderCanceledEventProducer-out-0", message);
    }
}
```

---

## Phase 4: DeleteOrderService anpassen

### 4.1 DeleteOrderService Interface Update
**Datei:** `Order Service/src/main/java/at/fhv/orderservice/application/services/DeleteOrderService.java`

**Kein direkter Edit notwendig** - Das Interface bleibt gleich, aber die Implementierung wird angepasst.

### 4.2 DeleteOrderServiceImpl Update
**Datei:** `Order Service/src/main/java/at/fhv/orderservice/application/services/impl/DeleteOrderServiceImpl.java`

**Was zu tun ist:**
- OrderEventProducer injizieren
- Nach dem Löschen der Order (nach `order.delete()`) das `OrderCanceledEvent` publishen

**Zu ändernde Zeilen:**

```java
// Import hinzufügen (nach bestehenden Imports)
import at.fhv.orderservice.infrastructure.messaging.producer.OrderEventProducer;
import at.fhv.orderservice.infrastructure.messaging.event.OrderCanceledEvent;
import at.fhv.orderservice.infrastructure.messaging.event.OrderItemEvent;
import java.util.stream.Collectors;

// ... (bestehender Code)

@Service
public class DeleteOrderServiceImpl implements DeleteOrderService {

    private final OrderRepository orderRepository;
    private final ProductServiceClient productServiceClient;
    private final OrderEventProducer orderEventProducer;  // NEU hinzufügen

    public DeleteOrderServiceImpl(OrderRepository orderRepository, 
                                 ProductServiceClient productServiceClient,
                                 OrderEventProducer orderEventProducer) {  // NEU in Konstruktor
        this.orderRepository = orderRepository;
        this.productServiceClient = productServiceClient;
        this.orderEventProducer = orderEventProducer;  // NEU hinzufügen
    }

    @Override
    @Transactional
    public void deleteOrderById(UUID orderId) {
        Order order = orderRepository.getOrderById(orderId);
        if (order == null) {
            throw new OrderNotFoundException(orderId);
        }

        order.delete();
        orderRepository.save(order);
        
        // Publish OrderCanceledEvent für asynchrone Stock-Wiederherstellung
        OrderCanceledEvent event = new OrderCanceledEvent(
                order.getId(),
                order.getUserId(),
                order.getOrderItems().stream()
                        .map(item -> new OrderItemEvent(item.getProductId(), item.getQuantity()))
                        .collect(Collectors.toList())
        );
        
        orderEventProducer.publishOrderCanceledEvent(event);
    }
}
```

---

## Phase 5: OpenAPI/Swagger Dokumentation (PFLICHT!)

### 5.1 OrderRestController mit OpenAPI Annotations aktualisieren
**Datei:** `Order Service/src/main/java/at/fhv/orderservice/rest/OrderRestController.java`

**Was zu tun ist:**
- OpenAPI/Swagger Annotations hinzufügen
- Jede API-Methode dokumentieren

**Zu ändernde/hinzufügende Imports und Annotations:**

```java
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/orders")
@Tag(name = "Order Management", description = "APIs for managing orders and order lifecycle")
public class OrderRestController {
    
    // ...existing code...
    
    @Operation(
        summary = "Get all orders",
        description = "Retrieve a list of all orders in the system"
    )
    @ApiResponse(responseCode = "200", description = "List of orders retrieved successfully")
    @GetMapping
    public ResponseEntity<List<GetOrderDTO>> getAllOrders() {
        // ...existing implementation...
    }
    
    @Operation(
        summary = "Get order by ID",
        description = "Retrieve a specific order by its UUID"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Order found"),
        @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<GetOrderDTO> getOrder(@PathVariable UUID id) {
        // ...existing implementation...
    }
    
    @Operation(
        summary = "Get orders by user ID",
        description = "Retrieve all orders for a specific user"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User orders retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<GetOrderDTO>> getOrdersByUserId(@PathVariable UUID userId) {
        // ...existing implementation...
    }
    
    @Operation(
        summary = "Create new order",
        description = "Create a new order with items"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Order created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid order data")
    })
    @PostMapping
    public ResponseEntity<GetOrderDTO> createOrder(@Valid @RequestBody CreateOrderDTO dto) {
        // ...existing implementation...
    }
    
    @Operation(
        summary = "Cancel/Delete order",
        description = "Cancel an order by marking it as CANCELLED. Triggers OrderCanceledEvent for async stock restoration in Product Service."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Order cancelled successfully and event published to RabbitMQ"),
        @ApiResponse(responseCode = "404", description = "Order not found"),
        @ApiResponse(responseCode = "400", description = "Cannot cancel order in current status")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable UUID id) {
        // ...existing implementation...
    }
}
```

**Swagger UI Zugriff:**
- Order Service: `http://localhost:8094/swagger-ui/index.html`
- API Docs: `http://localhost:8094/v3/api-docs`

---

## Phase 6: RabbitMQ Container/Setup

### 6.1 Docker Compose Setup (Optional - für lokale Entwicklung)
**Datei:** `E-Commerce-Application-MicroServices/docker-compose.yml` (neu erstellen)

**Inhalt:**

```yaml
version: '3.8'

services:
  rabbitmq:
    image: rabbitmq:3.12-management
    container_name: rabbitmq-container
    ports:
      - "5672:5672"    # AMQP port
      - "15672:15672"  # Management UI
    environment:
      RABBITMQ_DEFAULT_USER: guest
      RABBITMQ_DEFAULT_PASS: guest
    volumes:
      - rabbitmq_data:/var/lib/rabbitmq
    healthcheck:
      test: rabbitmq-diagnostics -q ping
      interval: 30s
      timeout: 10s
      retries: 5

volumes:
  rabbitmq_data:
```

**Anweisungen zum Starten:**
```bash
docker-compose up -d
```

RabbitMQ UI wird dann verfügbar unter: `http://localhost:15672` (guest/guest)

---

## Phase 7: Testen

### 7.1 Manuelle Tests durchführen

**Test 1: Order erstellen und löschen**

```bash
# 1. Order erstellen (POST)
curl -X POST http://localhost:8094/orders \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "items": [
      {
        "productId": "660e8400-e29b-41d4-a716-446655440000",
        "quantity": 2,
        "price": 19.99
      }
    ]
  }'

# 2. Response notieren (die Order ID)
# Example: {"id": "770e8400-e29b-41d4-a716-446655440000", ...}

# 3. Order löschen (DELETE)
curl -X DELETE http://localhost:8094/orders/770e8400-e29b-41d4-a716-446655440000

# 4. RabbitMQ Management UI öffnen: http://localhost:15672
# 5. Unter "Queues" sollte man die "order-events" Queue sehen
# 6. Mit "Get messages" kann man das publizierte Event sehen
```

**Test 2: Event-Struktur überprüfen**

Das Event sollte im RabbitMQ Message folgende Struktur haben:
```json
{
  "orderId": "770e8400-e29b-41d4-a716-446655440000",
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "orderItems": [
    {
      "productId": "660e8400-e29b-41d4-a716-446655440000",
      "quantity": 2
    }
  ],
  "timestamp": 1679836800000
}
```

---

## Zusammenfassung der Dateien für Person A

**Zu erstellende Dateien:**
1. `Order Service/src/main/java/at/fhv/orderservice/infrastructure/messaging/event/OrderCanceledEvent.java`
2. `Order Service/src/main/java/at/fhv/orderservice/infrastructure/messaging/event/OrderItemEvent.java`
3. `Order Service/src/main/java/at/fhv/orderservice/infrastructure/messaging/producer/OrderEventProducer.java`
4. `Order Service/src/main/java/at/fhv/orderservice/infrastructure/messaging/producer/OrderEventProducerImpl.java`
5. `E-Commerce-Application-MicroServices/docker-compose.yml`

**Zu modifizierende Dateien:**
1. `Order Service/build.gradle` (Dependencies + dependencyManagement)
2. `Order Service/src/main/resources/application.properties` (RabbitMQ & Stream Config)
3. `Order Service/src/main/java/at/fhv/orderservice/rest/OrderRestController.java` (Swagger Annotations - PFLICHT!)
4. `Order Service/src/main/java/at/fhv/orderservice/application/services/impl/DeleteOrderServiceImpl.java` (Producer injizieren + Event publishen)

---

## Geplante Abhängigkeiten mit Person B

Person B wird:
- Den Consumer in Product Service implementieren
- Das Event konsumieren und verarbeiten
- Stock für Produkte wiederherstellen

Die Kommunikation erfolgt über RabbitMQ und Spring Cloud Stream - komplett asynchron!

---

## Tipps & Hinweise

- **Keine Lombrok**: Alle Getter/Setter manuell schreiben ✓
- **StreamBridge statt Input/Output**: Neue Spring Cloud Stream Funktionalität verwenden ✓
- **Fehlerbehandlung**: Im Producer sollte man auch Error-Handler überlegen
- **Tracing**: Optional: Spring Cloud Sleuth für Distributed Tracing hinzufügen
- **Deadletter-Queue**: Optional später für Fehlerbehandlung implementieren


