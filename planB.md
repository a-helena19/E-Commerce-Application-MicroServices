# Person B – Aufgabenplan für Product Service
## Asynchronous Communication mit Spring Cloud Stream

**Datum:** 23.03.2026  
**Fokus:** Product Service - Event Listening und Publishing  
**Partner:** Person A (Order Service)  
**Messaging Broker:** RabbitMQ

---

## 🎯 Überblick Ihrer Aufgaben

Sie sind verantwortlich für den **Product Service**. Ihre Aufgabe ist es, folgende Events zu **empfangen** und zu **publizieren**:

### Events die Sie empfangen:
- `OrderCreatedEvent` - Von Order Service (neue Bestellung)
- `OrderCanceledEvent` - Von Order Service (Bestellung storniert)

### Events die Sie publizieren:
- `ProductReservationUpdatedEvent` - Reservierung erfolgreich
- `ProductReservationFailedEvent` - Reservierung fehlgeschlagen

---

## 📋 Schritt-für-Schritt Implementierung

### Schritt 1: Dependencies zu build.gradle hinzufügen
**Datei:** `Product Service/build.gradle`

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
**Datei:** `Product Service/src/main/resources/application.properties`

Fügen Sie folgende Konfiguration hinzu:

```properties
# Behalte bestehende Konfiguration

# ===== RabbitMQ Konfiguration =====
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest

# ===== Spring Cloud Stream Bindings =====
# Inbound: Events die Product Service von Order Service empfängt
spring.cloud.stream.bindings.orderCreated-in.destination=order-events
spring.cloud.stream.bindings.orderCreated-in.group=product-service-group
spring.cloud.stream.bindings.orderCreated-in.contentType=application/json

spring.cloud.stream.bindings.orderCanceled-in.destination=order-events
spring.cloud.stream.bindings.orderCanceled-in.group=product-service-group
spring.cloud.stream.bindings.orderCanceled-in.contentType=application/json

# Outbound: Events die Product Service publiziert
spring.cloud.stream.bindings.productReservationUpdated-out.destination=product-reservation-events
spring.cloud.stream.bindings.productReservationUpdated-out.contentType=application/json

spring.cloud.stream.bindings.productReservationFailed-out.destination=product-reservation-events
spring.cloud.stream.bindings.productReservationFailed-out.contentType=application/json

# Optional: Logging für Message Tracing
logging.level.org.springframework.cloud.stream=DEBUG
logging.level.org.springframework.amqp=DEBUG
```

---

### Schritt 3: Event-Klassen erstellen
**Ordner:** `Product Service/src/main/java/at/fhv/productservice/events/`

Erstellen Sie folgende 4 Event-Klassen:

#### 3.1 OrderCreatedEvent.java
```java
package at.fhv.productservice.events;

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
package at.fhv.productservice.events;

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
package at.fhv.productservice.events;

import java.time.LocalDateTime;
import java.util.UUID;

public class ProductReservationUpdatedEvent {
    private UUID orderId;
    private UUID productId;
    private int reservedQuantity;
    private String status; // "RESERVED"
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
package at.fhv.productservice.events;

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

### Schritt 4: Product Entity erweitern
**Datei:** `Product Service/src/main/java/at/fhv/productservice/model/Product.java`

Sie müssen das `Product` Entity um ein Feld für reservierte Menge erweitern:

```java
import jakarta.persistence.*;

@Entity
@Table(name = "products")
public class Product {
    // ...existing code...

    @Column(name = "reserved_quantity", nullable = false)
    private int reservedQuantity = 0;

    // Getter und Setter
    public int getReservedQuantity() {
        return reservedQuantity;
    }

    public void setReservedQuantity(int reservedQuantity) {
        this.reservedQuantity = reservedQuantity;
    }

    /**
     * Erhöht die reservierte Menge
     * 
     * @param quantity Die zu reservierende Menge
     */
    public void reserveQuantity(int quantity) {
        this.reservedQuantity += quantity;
    }

    /**
     * Reduziert die reservierte Menge
     * 
     * @param quantity Die freizugebende Menge
     */
    public void releaseReservation(int quantity) {
        this.reservedQuantity = Math.max(0, this.reservedQuantity - quantity);
    }

    /**
     * Gibt die verfügbare Menge zurück (Menge - Reservierungen)
     * 
     * @return die verfügbare Menge
     */
    public int getAvailableQuantity() {
        return this.quantity - this.reservedQuantity;
    }

    /**
     * Überprüft, ob genug Menge verfügbar ist
     * 
     * @param requestedQuantity die angeforderte Menge
     * @return true wenn genug Menge verfügbar ist
     */
    public boolean hasAvailableQuantity(int requestedQuantity) {
        return getAvailableQuantity() >= requestedQuantity;
    }
}
```

**Wichtig:** Sie müssen eine Datenbankmigrationsscript erstellen, um die neue Spalte hinzuzufügen (falls Sie Liquibase/Flyway verwenden) oder die H2 Datenbank wird sie automatisch erstellen.

---

### Schritt 5: Messaging-Konfiguration (Binding Interfaces)
**Datei:** `Product Service/src/main/java/at/fhv/productservice/config/ProductMessagingConfig.java`

```java
package at.fhv.productservice.config;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.context.annotation.Configuration;

/**
 * Messaging-Konfiguration für Product Service
 * Definiert Input (empfangen) und Output (publizieren) Kanäle
 */
@Configuration
public class ProductMessagingConfig {

    // ===== Inbound Channels (Events die wir empfangen) =====
    
    public static final String ORDER_CREATED_CHANNEL = "orderCreated-in";
    public static final String ORDER_CANCELED_CHANNEL = "orderCanceled-in";
    
    // ===== Outbound Channels (Events die wir publizieren) =====
    
    public static final String PRODUCT_RESERVATION_UPDATED_CHANNEL = "productReservationUpdated-out";
    public static final String PRODUCT_RESERVATION_FAILED_CHANNEL = "productReservationFailed-out";

    /**
     * Interface für Order Event Listening
     */
    public interface OrderEventListeningChannel {
        
        @Input(ORDER_CREATED_CHANNEL)
        SubscribableChannel orderCreatedChannel();
        
        @Input(ORDER_CANCELED_CHANNEL)
        SubscribableChannel orderCanceledChannel();
    }

    /**
     * Interface für Product Reservation Event Publishing
     */
    public interface ProductReservationPublishingChannel {
        
        @Output(PRODUCT_RESERVATION_UPDATED_CHANNEL)
        MessageChannel productReservationUpdatedChannel();
        
        @Output(PRODUCT_RESERVATION_FAILED_CHANNEL)
        MessageChannel productReservationFailedChannel();
    }
}
```

---

### Schritt 6: Event Publisher für Responses erstellen
**Datei:** `Product Service/src/main/java/at/fhv/productservice/messaging/ProductReservationEventPublisher.java`

```java
package at.fhv.productservice.messaging;

import at.fhv.productservice.config.ProductMessagingConfig;
import at.fhv.productservice.events.ProductReservationFailedEvent;
import at.fhv.productservice.events.ProductReservationUpdatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeTypeUtils;

/**
 * Publisher für Product Reservation Events
 * Publiziert Responses nach erfolgreichter/fehlgeschlagener Reservierung
 */
@Slf4j
@Component
@EnableBinding(ProductMessagingConfig.ProductReservationPublishingChannel.class)
public class ProductReservationEventPublisher {

    @Autowired
    private ProductMessagingConfig.ProductReservationPublishingChannel channel;

    /**
     * Publiziert ein ProductReservationUpdatedEvent
     * Wird aufgerufen, wenn die Reservierung erfolgreich war
     * 
     * @param event Das zu publizierende Event
     */
    public void publishProductReservationUpdated(ProductReservationUpdatedEvent event) {
        try {
            log.info("Publishing ProductReservationUpdatedEvent für Order ID: {}", event.getOrderId());
            
            Message<ProductReservationUpdatedEvent> message = MessageBuilder
                    .withPayload(event)
                    .setHeader(org.springframework.messaging.MessageHeaders.CONTENT_TYPE, 
                               MimeTypeUtils.APPLICATION_JSON)
                    .build();
            
            boolean sent = channel.productReservationUpdatedChannel().send(message);
            if (sent) {
                log.info("ProductReservationUpdatedEvent erfolgreich publiziert für Order ID: {}", 
                        event.getOrderId());
            } else {
                log.error("Fehler beim Publizieren von ProductReservationUpdatedEvent für Order ID: {}", 
                        event.getOrderId());
            }
        } catch (Exception e) {
            log.error("Fehler beim Publizieren von ProductReservationUpdatedEvent: {}", 
                    e.getMessage(), e);
            throw new RuntimeException("Fehler beim Publizieren von ProductReservationUpdatedEvent", e);
        }
    }

    /**
     * Publiziert ein ProductReservationFailedEvent
     * Wird aufgerufen, wenn die Reservierung fehlgeschlagen ist
     * 
     * @param event Das zu publizierende Event
     */
    public void publishProductReservationFailed(ProductReservationFailedEvent event) {
        try {
            log.info("Publishing ProductReservationFailedEvent für Order ID: {}", event.getOrderId());
            
            Message<ProductReservationFailedEvent> message = MessageBuilder
                    .withPayload(event)
                    .setHeader(org.springframework.messaging.MessageHeaders.CONTENT_TYPE, 
                               MimeTypeUtils.APPLICATION_JSON)
                    .build();
            
            boolean sent = channel.productReservationFailedChannel().send(message);
            if (sent) {
                log.info("ProductReservationFailedEvent erfolgreich publiziert für Order ID: {}", 
                        event.getOrderId());
            } else {
                log.error("Fehler beim Publizieren von ProductReservationFailedEvent für Order ID: {}", 
                        event.getOrderId());
            }
        } catch (Exception e) {
            log.error("Fehler beim Publizieren von ProductReservationFailedEvent: {}", 
                    e.getMessage(), e);
            throw new RuntimeException("Fehler beim Publizieren von ProductReservationFailedEvent", e);
        }
    }
}
```

---

### Schritt 7: Order Event Listener erstellen
**Datei:** `Product Service/src/main/java/at/fhv/productservice/messaging/OrderEventListener.java`

```java
package at.fhv.productservice.messaging;

import at.fhv.productservice.config.ProductMessagingConfig;
import at.fhv.productservice.events.OrderCanceledEvent;
import at.fhv.productservice.events.OrderCreatedEvent;
import at.fhv.productservice.events.ProductReservationFailedEvent;
import at.fhv.productservice.events.ProductReservationUpdatedEvent;
import at.fhv.productservice.model.Product;
import at.fhv.productservice.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.stereotype.Component;

/**
 * Listener für Order Events
 * Verarbeitet Bestellungen und aktualisiert Produktreservierungen
 */
@Slf4j
@Component
@EnableBinding(ProductMessagingConfig.OrderEventListeningChannel.class)
public class OrderEventListener {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductReservationEventPublisher reservationPublisher;

    /**
     * Verarbeitet OrderCreatedEvent
     * Reserviert Produktmenge und publiziert Success/Failed Event
     * 
     * @param event Das empfangene Event
     */
    @StreamListener(ProductMessagingConfig.ORDER_CREATED_CHANNEL)
    public void handleOrderCreated(OrderCreatedEvent event) {
        try {
            log.info("Received OrderCreatedEvent für Order ID: {}, Product ID: {}, Quantity: {}", 
                    event.getOrderId(), event.getProductId(), event.getQuantity());
            
            Product product = productRepository.findById(event.getProductId())
                    .orElseThrow(() -> new RuntimeException(
                            "Produkt nicht gefunden: " + event.getProductId()));
            
            log.info("Produkt gefunden: {}. Verfügbare Menge: {}", 
                    product.getId(), product.getAvailableQuantity());
            
            // Überprüfe, ob genug Menge verfügbar ist
            if (product.hasAvailableQuantity(event.getQuantity())) {
                // Reserviere die Menge
                product.reserveQuantity(event.getQuantity());
                productRepository.save(product);
                
                log.info("Produktmenge erfolgreich reserviert. Neue Reservierung: {}", 
                        product.getReservedQuantity());
                
                // Publiziere Success Event
                ProductReservationUpdatedEvent successEvent = new ProductReservationUpdatedEvent(
                    event.getOrderId(),
                    event.getProductId(),
                    product.getReservedQuantity(),
                    "RESERVED"
                );
                reservationPublisher.publishProductReservationUpdated(successEvent);
            } else {
                // Nicht genug Menge verfügbar
                log.warn("Nicht genug Produktmenge verfügbar. Angefordert: {}, Verfügbar: {}", 
                        event.getQuantity(), product.getAvailableQuantity());
                
                // Publiziere Failed Event
                ProductReservationFailedEvent failedEvent = new ProductReservationFailedEvent(
                    event.getOrderId(),
                    event.getProductId(),
                    event.getQuantity(),
                    "INSUFFICIENT_STOCK"
                );
                reservationPublisher.publishProductReservationFailed(failedEvent);
            }
        } catch (Exception e) {
            log.error("Fehler beim Verarbeiten von OrderCreatedEvent: {}", e.getMessage(), e);
            
            // Publiziere Failed Event als Fallback
            try {
                ProductReservationFailedEvent failedEvent = new ProductReservationFailedEvent(
                    event.getOrderId(),
                    event.getProductId(),
                    event.getQuantity(),
                    "PROCESSING_ERROR: " + e.getMessage()
                );
                reservationPublisher.publishProductReservationFailed(failedEvent);
            } catch (Exception publishError) {
                log.error("Fehler beim Publizieren von Fehler-Event: {}", publishError.getMessage());
            }
        }
    }

    /**
     * Verarbeitet OrderCanceledEvent
     * Gibt reservierte Produktmenge frei
     * 
     * @param event Das empfangene Event
     */
    @StreamListener(ProductMessagingConfig.ORDER_CANCELED_CHANNEL)
    public void handleOrderCanceled(OrderCanceledEvent event) {
        try {
            log.info("Received OrderCanceledEvent für Order ID: {}, Product ID: {}, Quantity: {}", 
                    event.getOrderId(), event.getProductId(), event.getQuantity());
            
            Product product = productRepository.findById(event.getProductId())
                    .orElseThrow(() -> new RuntimeException(
                            "Produkt nicht gefunden: " + event.getProductId()));
            
            log.info("Gebe reservierte Menge frei. Aktuelle Reservierung: {}", 
                    product.getReservedQuantity());
            
            // Gebe die Reservierung frei
            product.releaseReservation(event.getQuantity());
            productRepository.save(product);
            
            log.info("Reservierung erfolgreich freigegeben. Neue Reservierung: {}", 
                    product.getReservedQuantity());
        } catch (Exception e) {
            log.error("Fehler beim Verarbeiten von OrderCanceledEvent: {}", e.getMessage(), e);
        }
    }
}
```

---

### Schritt 8: OpenAPI-Dokumentation hinzufügen
**Datei:** `Product Service/src/main/java/at/fhv/productservice/controller/ProductController.java`

Fügen Sie OpenAPI-Annotationen zu Ihren Endpoints hinzu:

```java
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID",
               description = "Retrieves a product by its ID including availability information")
    @ApiResponse(responseCode = "200", description = "Product found",
                 content = @Content(mediaType = "application/json",
                                   schema = @Schema(implementation = ProductDTO.class)))
    @ApiResponse(responseCode = "404", description = "Product not found")
    public ResponseEntity<ProductDTO> getProduct(@PathVariable UUID id) {
        // Implementation
    }

    @GetMapping
    @Operation(summary = "Get all products",
               description = "Retrieves all products with their availability information")
    @ApiResponse(responseCode = "200", description = "Products retrieved successfully")
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        // Implementation
    }

    @PostMapping
    @Operation(summary = "Create a new product")
    @ApiResponse(responseCode = "201", description = "Product created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request")
    public ResponseEntity<ProductDTO> createProduct(@RequestBody @Valid ProductCreateRequest request) {
        // Implementation
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update product",
               description = "Updates product information (not reserved quantities)")
    @ApiResponse(responseCode = "200", description = "Product updated successfully")
    @ApiResponse(responseCode = "404", description = "Product not found")
    public ResponseEntity<ProductDTO> updateProduct(
            @PathVariable UUID id,
            @RequestBody @Valid ProductUpdateRequest request) {
        // Implementation
    }
}
```

---

### Schritt 9: Unit Tests schreiben
**Datei:** `Product Service/src/test/java/at/fhv/productservice/messaging/OrderEventListenerTest.java`

```java
package at.fhv.productservice.messaging;

import at.fhv.productservice.config.ProductMessagingConfig;
import at.fhv.productservice.events.OrderCreatedEvent;
import at.fhv.productservice.model.Product;
import at.fhv.productservice.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.cloud.stream.default.binder=test"
})
class OrderEventListenerTest {

    @Autowired
    private OrderEventListener orderEventListener;

    @MockBean
    private ProductRepository productRepository;

    @MockBean
    private ProductReservationEventPublisher reservationPublisher;

    private UUID productId = UUID.randomUUID();
    private Product testProduct;

    @BeforeEach
    void setup() {
        testProduct = new Product();
        testProduct.setId(productId);
        testProduct.setName("Test Product");
        testProduct.setQuantity(100);
        testProduct.setReservedQuantity(0);
    }

    @Test
    void testHandleOrderCreated_Success() {
        UUID orderId = UUID.randomUUID();
        OrderCreatedEvent event = new OrderCreatedEvent(
            orderId, UUID.randomUUID(), productId, 10, 99.99
        );

        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        orderEventListener.handleOrderCreated(event);

        // Überprüfe, dass die Menge reserviert wurde
        verify(productRepository, times(1)).save(testProduct);
        verify(reservationPublisher, times(1))
            .publishProductReservationUpdated(any());
    }

    @Test
    void testHandleOrderCreated_InsufficientStock() {
        testProduct.setQuantity(5);
        
        UUID orderId = UUID.randomUUID();
        OrderCreatedEvent event = new OrderCreatedEvent(
            orderId, UUID.randomUUID(), productId, 10, 99.99
        );

        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));

        orderEventListener.handleOrderCreated(event);

        // Überprüfe, dass Failed Event publiziert wird
        verify(reservationPublisher, times(1))
            .publishProductReservationFailed(any());
    }
}
```

---

## ✅ Checkliste für Person B

- [ ] Step 1: Dependencies zu build.gradle hinzufügen
- [ ] Step 2: application.properties konfiguriert
- [ ] Step 3: Alle 4 Event-Klassen erstellt
- [ ] Step 4: Product Entity um reserved_quantity erweitert
- [ ] Step 5: ProductMessagingConfig erstellt
- [ ] Step 6: ProductReservationEventPublisher erstellt
- [ ] Step 7: OrderEventListener erstellt und getestet
- [ ] Step 8: OpenAPI-Annotationen hinzugefügt
- [ ] Step 9: Unit Tests geschrieben und ausgeführt
- [ ] Mit Person A: Integration testen nach deren Completion

---

## 🔄 Saga Flow - Was passiert:

```
1. Person A (OrderService): Bestellung erstellen
   ↓
2. RabbitMQ: OrderCreatedEvent in "order-events" Queue
   ↓
3. Sie (ProductService): OrderEventListener empfängt Event
   - Überprüfe Produktmenge
   - Wenn genug: reservieren → ProductReservationUpdatedEvent ("RESERVED")
   - Wenn nicht: ProductReservationFailedEvent ("INSUFFICIENT_STOCK")
   ↓
4. RabbitMQ: Response-Event in "product-reservation-events" Queue
   ↓
5. Person A (OrderService): Empfängt Response
   - RESERVED: Order Status → CONFIRMED
   - FAILED: Order Status → CANCELED

==========================================

6. Bestellung wird storniert (Person A)
   ↓
7. RabbitMQ: OrderCanceledEvent in "order-events" Queue
   ↓
8. Sie (ProductService): OrderEventListener empfängt Event
   - Gebe Reservierung frei
```

---

## 📝 Wichtige Notizen:

- **Lokales RabbitMQ:** Stellen Sie sicher, dass RabbitMQ lokal läuft (Port 5672)
- **Datenbankmigration:** Achten Sie auf die neue Spalte `reserved_quantity`
- **Idempotenz:** Events können doppelt ankommen - nutzen Sie orderId zur Deduplizierung falls nötig
- **Error Handling:** Fangen Sie Exceptions ab und publizieren Sie Fehler-Events
- **Logging:** Nutzen Sie die Logs zum Debuggen
- **Zusammenarbeit mit Person A:** Testen Sie zusammen die End-to-End Flow

---

## 🧪 Manuelle Test-Szenarien:

### Szenario 1: Erfolgreiche Reservierung
1. Produkt mit 100 Stück erstellen
2. Bestellung mit 10 Stück von Person A erstellen
3. Überprüfen: Product.reservedQuantity = 10
4. Überprüfen: Order.status = CONFIRMED

### Szenario 2: Unzureichender Bestand
1. Produkt mit 5 Stück erstellen
2. Bestellung mit 10 Stück von Person A erstellen
3. Überprüfen: Product.reservedQuantity = 0
4. Überprüfen: Order.status = CANCELED

### Szenario 3: Stornierung
1. Erfolgreiche Reservierung durchführen
2. Product.reservedQuantity = 10
3. Bestellung stornieren
4. Überprüfen: Product.reservedQuantity = 0

---

**Status:** Bereit zur Implementierung  
**Geschätzter Aufwand:** 8-10 Stunden  
**Abhängig von:** Person A (Order Service)

