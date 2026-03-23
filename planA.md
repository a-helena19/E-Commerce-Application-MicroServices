# Person A – Aufgabenplan für Order Service
## Asynchronous Communication mit Spring Cloud Stream

**Datum:** 23.03.2026  
**Fokus:** Order Service - Event Publishing und Listening  
**Partner:** Person B (Product Service)  
**Messaging Broker:** RabbitMQ

---

## 🎯 Überblick Ihrer Aufgaben

Sie sind verantwortlich für den **Order Service**. Ihre Aufgabe ist es, folgende Events zu **publizieren** und zu **empfangen**:

### Events die Sie publizieren:
- `OrderCreatedEvent` - Wenn eine neue Bestellung erstellt wird
- `OrderCanceledEvent` - Wenn eine Bestellung storniert wird

### Events die Sie empfangen:
- `ProductReservationUpdatedEvent` - Von Product Service (Bestätigung der Reservierung)
- `ProductReservationFailedEvent` - Von Product Service (Reservierung fehlgeschlagen)

---

## 📋 Schritt-für-Schritt Implementierung

### Schritt 1: Dependencies zu build.gradle hinzufügen
**Datei:** `Order Service/build.gradle`

1. Öffnen Sie die `build.gradle` Datei
2. Fügen Sie folgende Dependencies in den `dependencies` Block hinzu:

```groovy
// Spring Cloud Stream für RabbitMQ
implementation 'org.springframework.cloud:spring-cloud-starter-stream-rabbit'

// Spring Cloud Stream Test Support
testImplementation 'org.springframework.cloud:spring-cloud-stream-test-support'
```

3. Überprüfen Sie, ob folgende Dependency Management vorhanden ist (falls nicht, hinzufügen):

```groovy
dependencyManagement {
    imports {
        mavenBom 'org.springframework.cloud:spring-cloud-dependencies:2023.0.1'
    }
}
```

**Hinweis:** Diese Version ist kompatibel mit Spring Boot 3.3.5

---

### Schritt 2: Applikations-Konfiguration in application.properties
**Datei:** `Order Service/src/main/resources/application.properties`

Fügen Sie folgende Konfiguration hinzu:

```properties
# Behalte bestehende Konfiguration

# ===== RabbitMQ Konfiguration =====
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest

# ===== Spring Cloud Stream Bindings =====
# Outbound: Events die Order Service publiziert
spring.cloud.stream.bindings.orderCreated-out.destination=order-events
spring.cloud.stream.bindings.orderCreated-out.contentType=application/json

spring.cloud.stream.bindings.orderCanceled-out.destination=order-events
spring.cloud.stream.bindings.orderCanceled-out.contentType=application/json

# Inbound: Events die Order Service von Product Service empfängt
spring.cloud.stream.bindings.productReservationUpdated-in.destination=product-reservation-events
spring.cloud.stream.bindings.productReservationUpdated-in.group=order-service-group
spring.cloud.stream.bindings.productReservationUpdated-in.contentType=application/json

spring.cloud.stream.bindings.productReservationFailed-in.destination=product-reservation-events
spring.cloud.stream.bindings.productReservationFailed-in.group=order-service-group
spring.cloud.stream.bindings.productReservationFailed-in.contentType=application/json

# Optional: Logging für Message Tracing
logging.level.org.springframework.cloud.stream=DEBUG
logging.level.org.springframework.amqp=DEBUG
```

---

### Schritt 3: Event-Klassen erstellen
**Ordner:** `Order Service/src/main/java/at/fhv/orderservice/events/`

Erstellen Sie folgende 4 Event-Klassen:

#### 3.1 OrderCreatedEvent.java
```java
package at.fhv.orderservice.events;

import java.time.LocalDateTime;
import java.util.UUID;

public class OrderCreatedEvent {
    private UUID orderId;
    private UUID userId;
    private UUID productId;
    private int quantity;
    private double price;
    private LocalDateTime timestamp;

    // Konstruktor
    public OrderCreatedEvent() {
    }

    public OrderCreatedEvent(UUID orderId, UUID userId, UUID productId, int quantity, double price) {
        this.orderId = orderId;
        this.userId = userId;
        this.productId = productId;
        this.quantity = quantity;
        this.price = price;
        this.timestamp = LocalDateTime.now();
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

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
```

#### 3.2 OrderCanceledEvent.java
```java
package at.fhv.orderservice.events;

import java.time.LocalDateTime;
import java.util.UUID;

public class OrderCanceledEvent {
    private UUID orderId;
    private UUID userId;
    private UUID productId;
    private int quantity;
    private String reason;
    private LocalDateTime timestamp;

    // Konstruktor
    public OrderCanceledEvent() {
    }

    public OrderCanceledEvent(UUID orderId, UUID userId, UUID productId, int quantity, String reason) {
        this.orderId = orderId;
        this.userId = userId;
        this.productId = productId;
        this.quantity = quantity;
        this.reason = reason;
        this.timestamp = LocalDateTime.now();
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

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
```

#### 3.3 ProductReservationUpdatedEvent.java
```java
package at.fhv.orderservice.events;

import java.time.LocalDateTime;
import java.util.UUID;

public class ProductReservationUpdatedEvent {
    private UUID orderId;
    private UUID productId;
    private int reservedQuantity;
    private String status; // "RESERVED" oder "FAILED"
    private LocalDateTime timestamp;

    // Konstruktor
    public ProductReservationUpdatedEvent() {
    }

    public ProductReservationUpdatedEvent(UUID orderId, UUID productId, int reservedQuantity, String status) {
        this.orderId = orderId;
        this.productId = productId;
        this.reservedQuantity = reservedQuantity;
        this.status = status;
        this.timestamp = LocalDateTime.now();
    }

    // Getter und Setter
    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public int getReservedQuantity() {
        return reservedQuantity;
    }

    public void setReservedQuantity(int reservedQuantity) {
        this.reservedQuantity = reservedQuantity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
```

#### 3.4 ProductReservationFailedEvent.java
```java
package at.fhv.orderservice.events;

import java.time.LocalDateTime;
import java.util.UUID;

public class ProductReservationFailedEvent {
    private UUID orderId;
    private UUID productId;
    private int requestedQuantity;
    private String reason; // z.B. "INSUFFICIENT_STOCK"
    private LocalDateTime timestamp;

    // Konstruktor
    public ProductReservationFailedEvent() {
    }

    public ProductReservationFailedEvent(UUID orderId, UUID productId, int requestedQuantity, String reason) {
        this.orderId = orderId;
        this.productId = productId;
        this.requestedQuantity = requestedQuantity;
        this.reason = reason;
        this.timestamp = LocalDateTime.now();
    }

    // Getter und Setter
    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public int getRequestedQuantity() {
        return requestedQuantity;
    }

    public void setRequestedQuantity(int requestedQuantity) {
        this.requestedQuantity = requestedQuantity;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
```

---

### Schritt 4: Messaging-Konfiguration (Binding Interfaces)
**Datei:** `Order Service/src/main/java/at/fhv/orderservice/config/OrderMessagingConfig.java`

```java
package at.fhv.orderservice.config;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.context.annotation.Configuration;

/**
 * Messaging-Konfiguration für Order Service
 * Definiert Input (empfangen) und Output (publizieren) Kanäle
 */
@Configuration
public class OrderMessagingConfig {

    // ===== Outbound Channels (Events die wir publizieren) =====
    
    public static final String ORDER_CREATED_CHANNEL = "orderCreated-out";
    public static final String ORDER_CANCELED_CHANNEL = "orderCanceled-out";
    
    // ===== Inbound Channels (Events die wir empfangen) =====
    
    public static final String PRODUCT_RESERVATION_UPDATED_CHANNEL = "productReservationUpdated-in";
    public static final String PRODUCT_RESERVATION_FAILED_CHANNEL = "productReservationFailed-in";

    /**
     * Interface für Order Event Publishing
     */
    public interface OrderEventPublishingChannel {
        
        @Output(ORDER_CREATED_CHANNEL)
        MessageChannel orderCreatedChannel();
        
        @Output(ORDER_CANCELED_CHANNEL)
        MessageChannel orderCanceledChannel();
    }

    /**
     * Interface für Product Reservation Event Listening
     */
    public interface ProductReservationListeningChannel {
        
        @Input(PRODUCT_RESERVATION_UPDATED_CHANNEL)
        SubscribableChannel productReservationUpdatedChannel();
        
        @Input(PRODUCT_RESERVATION_FAILED_CHANNEL)
        SubscribableChannel productReservationFailedChannel();
    }
}
```

---

### Schritt 5: Event Publisher erstellen
**Datei:** `Order Service/src/main/java/at/fhv/orderservice/messaging/OrderEventPublisher.java`

```java
package at.fhv.orderservice.messaging;

import at.fhv.orderservice.config.OrderMessagingConfig;
import at.fhv.orderservice.events.OrderCanceledEvent;
import at.fhv.orderservice.events.OrderCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeTypeUtils;

/**
 * Publisher für Order-Events
 * Publiziert OrderCreatedEvent und OrderCanceledEvent zu RabbitMQ
 */
@Slf4j
@Component
@EnableBinding(OrderMessagingConfig.OrderEventPublishingChannel.class)
public class OrderEventPublisher {

    @Autowired
    private OrderMessagingConfig.OrderEventPublishingChannel channel;

    /**
     * Publiziert ein OrderCreatedEvent
     * Wird aufgerufen, wenn eine neue Bestellung erstellt wird
     * 
     * @param event Das zu publizierende Event
     */
    public void publishOrderCreated(OrderCreatedEvent event) {
        try {
            log.info("Publishing OrderCreatedEvent für Order ID: {}", event.getOrderId());
            
            Message<OrderCreatedEvent> message = MessageBuilder
                    .withPayload(event)
                    .setHeader(org.springframework.messaging.MessageHeaders.CONTENT_TYPE, 
                               MimeTypeUtils.APPLICATION_JSON)
                    .build();
            
            boolean sent = channel.orderCreatedChannel().send(message);
            if (sent) {
                log.info("OrderCreatedEvent erfolgreich publiziert für Order ID: {}", event.getOrderId());
            } else {
                log.error("Fehler beim Publizieren von OrderCreatedEvent für Order ID: {}", event.getOrderId());
            }
        } catch (Exception e) {
            log.error("Fehler beim Publizieren von OrderCreatedEvent: {}", e.getMessage(), e);
            throw new RuntimeException("Fehler beim Publizieren von OrderCreatedEvent", e);
        }
    }

    /**
     * Publiziert ein OrderCanceledEvent
     * Wird aufgerufen, wenn eine Bestellung storniert wird
     * 
     * @param event Das zu publizierende Event
     */
    public void publishOrderCanceled(OrderCanceledEvent event) {
        try {
            log.info("Publishing OrderCanceledEvent für Order ID: {}", event.getOrderId());
            
            Message<OrderCanceledEvent> message = MessageBuilder
                    .withPayload(event)
                    .setHeader(org.springframework.messaging.MessageHeaders.CONTENT_TYPE, 
                               MimeTypeUtils.APPLICATION_JSON)
                    .build();
            
            boolean sent = channel.orderCanceledChannel().send(message);
            if (sent) {
                log.info("OrderCanceledEvent erfolgreich publiziert für Order ID: {}", event.getOrderId());
            } else {
                log.error("Fehler beim Publizieren von OrderCanceledEvent für Order ID: {}", event.getOrderId());
            }
        } catch (Exception e) {
            log.error("Fehler beim Publizieren von OrderCanceledEvent: {}", e.getMessage(), e);
            throw new RuntimeException("Fehler beim Publizieren von OrderCanceledEvent", e);
        }
    }
}
```

---

### Schritt 6: Event Listener erstellen
**Datei:** `Order Service/src/main/java/at/fhv/orderservice/messaging/ProductReservationEventListener.java`

```java
package at.fhv.orderservice.messaging;

import at.fhv.orderservice.config.OrderMessagingConfig;
import at.fhv.orderservice.events.ProductReservationFailedEvent;
import at.fhv.orderservice.events.ProductReservationUpdatedEvent;
import at.fhv.orderservice.model.Order;
import at.fhv.orderservice.model.OrderStatus;
import at.fhv.orderservice.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.stereotype.Component;

/**
 * Listener für Product Reservation Events
 * Verarbeitet Responses vom Product Service
 */
@Slf4j
@Component
@EnableBinding(OrderMessagingConfig.ProductReservationListeningChannel.class)
public class ProductReservationEventListener {

    @Autowired
    private OrderRepository orderRepository;

    /**
     * Verarbeitet ProductReservationUpdatedEvent
     * Wird aufgerufen, wenn die Reservierung erfolgreich war
     * 
     * @param event Das empfangene Event
     */
    @StreamListener(OrderMessagingConfig.PRODUCT_RESERVATION_UPDATED_CHANNEL)
    public void handleProductReservationUpdated(ProductReservationUpdatedEvent event) {
        try {
            log.info("Received ProductReservationUpdatedEvent für Order ID: {}", event.getOrderId());
            
            Order order = orderRepository.findById(event.getOrderId())
                    .orElseThrow(() -> new RuntimeException("Order nicht gefunden: " + event.getOrderId()));
            
            if ("RESERVED".equals(event.getStatus())) {
                log.info("Produkt erfolgreich reserviert für Order ID: {}", event.getOrderId());
                order.setStatus(OrderStatus.CONFIRMED);
                orderRepository.save(order);
                log.info("Order Status zu CONFIRMED aktualisiert für Order ID: {}", event.getOrderId());
            } else {
                log.warn("Unerwarteter Status in ProductReservationUpdatedEvent: {}", event.getStatus());
            }
        } catch (Exception e) {
            log.error("Fehler beim Verarbeiten von ProductReservationUpdatedEvent: {}", e.getMessage(), e);
        }
    }

    /**
     * Verarbeitet ProductReservationFailedEvent
     * Wird aufgerufen, wenn die Reservierung fehlgeschlagen ist
     * 
     * @param event Das empfangene Event
     */
    @StreamListener(OrderMessagingConfig.PRODUCT_RESERVATION_FAILED_CHANNEL)
    public void handleProductReservationFailed(ProductReservationFailedEvent event) {
        try {
            log.info("Received ProductReservationFailedEvent für Order ID: {}", event.getOrderId());
            
            Order order = orderRepository.findById(event.getOrderId())
                    .orElseThrow(() -> new RuntimeException("Order nicht gefunden: " + event.getOrderId()));
            
            log.warn("Reservierung fehlgeschlagen für Order ID: {}. Grund: {}", 
                    event.getOrderId(), event.getReason());
            
            order.setStatus(OrderStatus.CANCELED);
            orderRepository.save(order);
            log.info("Order Status zu CANCELED aktualisiert für Order ID: {}", event.getOrderId());
        } catch (Exception e) {
            log.error("Fehler beim Verarbeiten von ProductReservationFailedEvent: {}", e.getMessage(), e);
        }
    }
}
```

---

### Schritt 7: Order Service anpassen
**Datei:** `Order Service/src/main/java/at/fhv/orderservice/service/OrderService.java`

Sie müssen die bestehende `OrderService` Klasse anpassen:

1. Injizieren Sie den `OrderEventPublisher`:
```java
@Autowired
private OrderEventPublisher orderEventPublisher;
```

2. In der `createOrder()` Methode, nach dem Speichern der Order, das Event publizieren:
```java
public Order createOrder(OrderCreateRequest request) {
    // ... Validierung und Order erstellen ...
    
    Order order = new Order();
    // ... Felder setzen ...
    order = orderRepository.save(order);
    
    // 👇 NEU: Event publizieren
    OrderCreatedEvent event = new OrderCreatedEvent(
        order.getId(),
        order.getUserId(),
        order.getProductId(),
        order.getQuantity(),
        order.getPrice()
    );
    orderEventPublisher.publishOrderCreated(event);
    
    return order;
}
```

3. In der `cancelOrder()` Methode, nach dem Aktualisieren der Order:
```java
public void cancelOrder(UUID orderId) {
    Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException("Order nicht gefunden"));
    
    order.setStatus(OrderStatus.CANCELED);
    order = orderRepository.save(order);
    
    // 👇 NEU: Event publizieren
    OrderCanceledEvent event = new OrderCanceledEvent(
        order.getId(),
        order.getUserId(),
        order.getProductId(),
        order.getQuantity(),
        "Bestellung storniert"
    );
    orderEventPublisher.publishOrderCanceled(event);
}
```

---

### Schritt 8: OpenAPI-Dokumentation hinzufügen
**Datei:** `Order Service/src/main/java/at/fhv/orderservice/controller/OrderController.java`

Fügen Sie OpenAPI-Annotationen zu Ihren Endpoints hinzu:

```java
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @PostMapping
    @Operation(summary = "Create a new order", 
               description = "Creates a new order and publishes OrderCreatedEvent")
    @ApiResponse(responseCode = "201", description = "Order created successfully",
                 content = @Content(mediaType = "application/json", 
                                   schema = @Schema(implementation = OrderDTO.class)))
    @ApiResponse(responseCode = "400", description = "Invalid request")
    public ResponseEntity<OrderDTO> createOrder(@RequestBody @Valid OrderCreateRequest request) {
        // Implementation
    }

    @DeleteMapping("/{id}/cancel")
    @Operation(summary = "Cancel an order",
               description = "Cancels an existing order and publishes OrderCanceledEvent")
    @ApiResponse(responseCode = "200", description = "Order cancelled successfully")
    @ApiResponse(responseCode = "404", description = "Order not found")
    public ResponseEntity<Void> cancelOrder(@PathVariable UUID id) {
        // Implementation
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID")
    @ApiResponse(responseCode = "200", description = "Order found")
    @ApiResponse(responseCode = "404", description = "Order not found")
    public ResponseEntity<OrderDTO> getOrder(@PathVariable UUID id) {
        // Implementation
    }
}
```

---

### Schritt 9: Unit Tests schreiben
**Datei:** `Order Service/src/test/java/at/fhv/orderservice/messaging/OrderEventPublisherTest.java`

```java
package at.fhv.orderservice.messaging;

import at.fhv.orderservice.config.OrderMessagingConfig;
import at.fhv.orderservice.events.OrderCreatedEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.messaging.Message;
import org.springframework.test.context.TestPropertySource;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.cloud.stream.default.binder=test"
})
class OrderEventPublisherTest {

    @Autowired
    private OrderEventPublisher orderEventPublisher;

    @Autowired
    private OrderMessagingConfig.OrderEventPublishingChannel publishingChannel;

    @Autowired
    private MessageCollector messageCollector;

    @Test
    void testPublishOrderCreatedEvent() {
        UUID orderId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        OrderCreatedEvent event = new OrderCreatedEvent(
            orderId, userId, productId, 5, 99.99
        );

        orderEventPublisher.publishOrderCreated(event);

        Message<?> message = messageCollector.forChannel(publishingChannel.orderCreatedChannel()).poll();
        
        assertNotNull(message);
        assertTrue(message.getPayload() instanceof OrderCreatedEvent);
        
        OrderCreatedEvent receivedEvent = (OrderCreatedEvent) message.getPayload();
        assertEquals(orderId, receivedEvent.getOrderId());
        assertEquals(userId, receivedEvent.getUserId());
        assertEquals(5, receivedEvent.getQuantity());
    }
}
```

---

## ✅ Checkliste für Person A

- [ ] Step 1: Dependencies zu build.gradle hinzufügen
- [ ] Step 2: application.properties konfiguriert
- [ ] Step 3: Alle 4 Event-Klassen erstellt
- [ ] Step 4: OrderMessagingConfig erstellt
- [ ] Step 5: OrderEventPublisher erstellt und getestet
- [ ] Step 6: ProductReservationEventListener erstellt
- [ ] Step 7: OrderService angepasst (createOrder + cancelOrder)
- [ ] Step 8: OpenAPI-Annotationen hinzugefügt
- [ ] Step 9: Unit Tests geschrieben und ausgeführt
- [ ] Mit Person B: Integration testen nach deren Completion

---

## 🔄 Saga Flow - Was passiert:

```
1. USER: Bestellung erstellen
   ↓
2. OrderService.createOrder()
   - Order speichern (Status: PENDING)
   - OrderCreatedEvent publizieren
   ↓
3. RabbitMQ: Nachricht in "order-events" Queue
   ↓
4. ProductService: Empfängt OrderCreatedEvent
   - Produkt reservieren
   - ProductReservationUpdatedEvent oder ProductReservationFailedEvent publizieren
   ↓
5. RabbitMQ: Nachricht in "product-reservation-events" Queue
   ↓
6. OrderService: ProductReservationEventListener empfängt Event
   - Wenn SUCCESS: Order Status → CONFIRMED
   - Wenn FAILED: Order Status → CANCELED
```

---

## 📝 Wichtige Notizen:

- **Lokales RabbitMQ:** Stellen Sie sicher, dass RabbitMQ lokal läuft (Port 5672)
- **Idempotenz:** Events können doppelt ankommen - bauen Sie Idempotenz ein
- **Logging:** Nutzen Sie die Logs zum Debuggen
- **Order Status:** Achten Sie auf die korrekten Status-Übergänge
- **Zusammenarbeit mit Person B:** Testen Sie zusammen die End-to-End Flow

---

**Status:** Bereit zur Implementierung  
**Geschätzter Aufwand:** 8-10 Stunden

