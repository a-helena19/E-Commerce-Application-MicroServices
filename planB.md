# Person B – Aufgabenplan für Product Service
## Asynchronous Communication mit Spring Cloud Stream

**Datum:** 23.03.2026  
**Fokus:** Product Service - Event Listening und Publishing  
**Partner:** Person A (Cart Service)  
**Messaging Broker:** RabbitMQ

---

## 🎯 Überblick Ihrer Aufgaben

Sie sind verantwortlich für den **Product Service**. Ihre Aufgabe ist es, folgende Events zu **empfangen** und zu **publizieren**:

### Events die Sie empfangen:
- `CartCheckoutEvent` - Von Cart Service (Checkout eines Carts)

### Events die Sie publizieren:
- `ProductReservationConfirmedEvent` - Reservierung erfolgreich
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
# Inbound: Events die Product Service von Cart Service empfängt
spring.cloud.stream.bindings.cartCheckout-in.destination=cart-checkout-events
spring.cloud.stream.bindings.cartCheckout-in.group=product-service-group
spring.cloud.stream.bindings.cartCheckout-in.contentType=application/json

# Outbound: Events die Product Service publiziert
spring.cloud.stream.bindings.productReservationConfirmed-out.destination=product-reservation-events
spring.cloud.stream.bindings.productReservationConfirmed-out.contentType=application/json

spring.cloud.stream.bindings.productReservationFailed-out.destination=product-reservation-events
spring.cloud.stream.bindings.productReservationFailed-out.contentType=application/json

# Optional: Logging für Message Tracing
logging.level.org.springframework.cloud.stream=DEBUG
logging.level.org.springframework.amqp=DEBUG
```

---

### Schritt 3: Event-Klassen erstellen
**Ordner:** `Product Service/src/main/java/at/fhv/productservice/events/`

Erstellen Sie folgende 3 Event-Klassen (IDENTISCH mit Person A!):

#### 3.1 CartCheckoutEvent.java
```java
package at.fhv.productservice.events;

import java.time.LocalDateTime;
import java.util.UUID;

public class CartCheckoutEvent {
    private UUID cartId;
    private UUID userId;
    private UUID productId;
    private int quantity;
    private double totalPrice;
    private LocalDateTime timestamp;

    // Konstruktor
    public CartCheckoutEvent() {
    }

    public CartCheckoutEvent(UUID cartId, UUID userId, UUID productId, int quantity, double totalPrice) {
        this.cartId = cartId;
        this.userId = userId;
        this.productId = productId;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
        this.timestamp = LocalDateTime.now();
    }

    // Getter und Setter
    public UUID getCartId() {
        return cartId;
    }

    public void setCartId(UUID cartId) {
        this.cartId = cartId;
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

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
```

#### 3.2 ProductReservationConfirmedEvent.java
```java
package at.fhv.productservice.events;

import java.time.LocalDateTime;
import java.util.UUID;

public class ProductReservationConfirmedEvent {
    private UUID cartId;
    private UUID productId;
    private int reservedQuantity;
    private String status; // "CONFIRMED"
    private LocalDateTime timestamp;

    // Konstruktor
    public ProductReservationConfirmedEvent() {
    }

    public ProductReservationConfirmedEvent(UUID cartId, UUID productId, int reservedQuantity, String status) {
        this.cartId = cartId;
        this.productId = productId;
        this.reservedQuantity = reservedQuantity;
        this.status = status;
        this.timestamp = LocalDateTime.now();
    }

    // Getter und Setter
    public UUID getCartId() {
        return cartId;
    }

    public void setCartId(UUID cartId) {
        this.cartId = cartId;
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

#### 3.3 ProductReservationFailedEvent.java
```java
package at.fhv.productservice.events;

import java.time.LocalDateTime;
import java.util.UUID;

public class ProductReservationFailedEvent {
    private UUID cartId;
    private UUID productId;
    private int requestedQuantity;
    private String reason; // z.B. "INSUFFICIENT_STOCK"
    private LocalDateTime timestamp;

    // Konstruktor
    public ProductReservationFailedEvent() {
    }

    public ProductReservationFailedEvent(UUID cartId, UUID productId, int requestedQuantity, String reason) {
        this.cartId = cartId;
        this.productId = productId;
        this.requestedQuantity = requestedQuantity;
        this.reason = reason;
        this.timestamp = LocalDateTime.now();
    }

    // Getter und Setter
    public UUID getCartId() {
        return cartId;
    }

    public void setCartId(UUID cartId) {
        this.cartId = cartId;
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

### Schritt 4: Product Entity und Domain Service erstellen

#### 4.1 Product Entity anpassen
**Datei:** `Product Service/src/main/java/at/fhv/productservice/domain/model/Product.java`

Das `Product` Entity sollte nur Daten halten, **keine Business-Logik**:

```java
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "products")
public class Product {
    @Id
    private UUID id;
    
    @Column(name = "name")
    private String name;
    
    @Column(name = "quantity")
    private int quantity;
    
    @Column(name = "reserved_quantity", nullable = false)
    private int reservedQuantity = 0;
    
    @Column(name = "price")
    private double price;

    // Konstruktor
    public Product() {}

    // Getter und Setter (KEINE Business-Logik!)
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    
    public int getReservedQuantity() { return reservedQuantity; }
    public void setReservedQuantity(int reservedQuantity) { 
        this.reservedQuantity = reservedQuantity; 
    }
    
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
}
```

**Wichtig:** Sie müssen eine Datenbankmigrationsscript erstellen, um die neue Spalte `reserved_quantity` hinzuzufügen (falls Sie Liquibase/Flyway verwenden) oder die H2 Datenbank wird sie automatisch erstellen.

#### 4.2 ProductReservationService erstellen (Domain Service)
**Datei:** `Product Service/src/main/java/at/fhv/productservice/domain/service/ProductReservationService.java`

Die **Business-Logik gehört hier rein**, nicht in die Entity:

```java
package at.fhv.productservice.domain.service;

import at.fhv.productservice.domain.model.Product;
import at.fhv.productservice.domain.repository.ProductRepository;
import at.fhv.productservice.events.ProductReservationConfirmedEvent;
import at.fhv.productservice.events.ProductReservationFailedEvent;
import at.fhv.productservice.infrastructure.messaging.ProductReservationEventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Domain Service für Produktreservierungen
 * Enthält die Business-Logik für Reservierungen
 */
@Slf4j
@Service
public class ProductReservationService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductReservationEventPublisher eventPublisher;

    /**
     * Reserviert eine Produktmenge
     * Business-Logik: Prüfe Verfügbarkeit, reserviere, publiziere Event
     * 
     * @param cartId Die Cart-ID
     * @param productId Die Produkt-ID
     * @param requestedQuantity Die angeforderte Menge
     */
    @Transactional
    public void reserveProduct(UUID cartId, UUID productId, int requestedQuantity) {
        try {
            log.info("Reservierungsanfrage: CartID={}, ProductID={}, Menge={}", 
                    cartId, productId, requestedQuantity);
            
            // 1. Finde das Produkt
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Produkt nicht gefunden: " + productId));
            
            log.info("Produkt gefunden: {}. Bestand: {}, Reserviert: {}, Verfügbar: {}", 
                    product.getName(), 
                    product.getQuantity(), 
                    product.getReservedQuantity(),
                    getAvailableQuantity(product));
            
            // 2. Prüfe ob genug verfügbar ist (Business-Logik!)
            if (hasAvailableQuantity(product, requestedQuantity)) {
                // 3. Reserviere die Menge
                product.setReservedQuantity(product.getReservedQuantity() + requestedQuantity);
                productRepository.save(product);
                
                log.info("Reservierung erfolgreich! Neue Reservierung: {}", 
                        product.getReservedQuantity());
                
                // 4. Publiziere Success-Event
                ProductReservationConfirmedEvent event = new ProductReservationConfirmedEvent(
                    cartId,
                    productId,
                    product.getReservedQuantity(),
                    "CONFIRMED"
                );
                eventPublisher.publishProductReservationConfirmed(event);
                
            } else {
                // Nicht genug verfügbar - publiziere Fehler-Event
                log.warn("Nicht genug Bestand! Angefordert: {}, Verfügbar: {}", 
                        requestedQuantity, getAvailableQuantity(product));
                
                ProductReservationFailedEvent event = new ProductReservationFailedEvent(
                    cartId,
                    productId,
                    requestedQuantity,
                    "INSUFFICIENT_STOCK"
                );
                eventPublisher.publishProductReservationFailed(event);
            }
        } catch (Exception e) {
            log.error("Fehler bei Reservierung: {}", e.getMessage(), e);
            
            // Publiziere Fehler-Event als Fallback
            try {
                ProductReservationFailedEvent event = new ProductReservationFailedEvent(
                    cartId,
                    productId,
                    requestedQuantity,
                    "PROCESSING_ERROR: " + e.getMessage()
                );
                eventPublisher.publishProductReservationFailed(event);
            } catch (Exception publishError) {
                log.error("Fehler beim Publizieren des Fehler-Events: {}", publishError.getMessage());
            }
        }
    }

    /**
     * Helper-Methode: Gibt verfügbare Menge zurück
     * Business-Logik: available = total - reserved
     */
    private int getAvailableQuantity(Product product) {
        return product.getQuantity() - product.getReservedQuantity();
    }

    /**
     * Helper-Methode: Prüft ob genug verfügbar ist
     * Business-Logik für Reservierung
     */
    private boolean hasAvailableQuantity(Product product, int requestedQuantity) {
        return getAvailableQuantity(product) >= requestedQuantity;
    }

    /**
     * Gibt eine Reservierung frei (falls nötig)
     * 
     * @param productId Die Produkt-ID
     * @param quantity Die freizugebende Menge
     */
    @Transactional
    public void releaseReservation(UUID productId, int quantity) {
        try {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Produkt nicht gefunden: " + productId));
            
            product.setReservedQuantity(Math.max(0, product.getReservedQuantity() - quantity));
            productRepository.save(product);
            
            log.info("Reservierung freigegeben. Neue Reservierung: {}", 
                    product.getReservedQuantity());
        } catch (Exception e) {
            log.error("Fehler beim Freigeben der Reservierung: {}", e.getMessage());
        }
    }
}
```

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
    
    public static final String CART_CHECKOUT_CHANNEL = "cartCheckout-in";
    
    // ===== Outbound Channels (Events die wir publizieren) =====
    
    public static final String PRODUCT_RESERVATION_CONFIRMED_CHANNEL = "productReservationConfirmed-out";
    public static final String PRODUCT_RESERVATION_FAILED_CHANNEL = "productReservationFailed-out";

    /**
     * Interface für Cart Event Listening
     */
    public interface CartEventListeningChannel {
        
        @Input(CART_CHECKOUT_CHANNEL)
        SubscribableChannel cartCheckoutChannel();
    }

    /**
     * Interface für Product Reservation Event Publishing
     */
    public interface ProductReservationPublishingChannel {
        
        @Output(PRODUCT_RESERVATION_CONFIRMED_CHANNEL)
        MessageChannel productReservationConfirmedChannel();
        
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
import at.fhv.productservice.events.ProductReservationConfirmedEvent;
import at.fhv.productservice.events.ProductReservationFailedEvent;
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
     * Publiziert ein ProductReservationConfirmedEvent
     * Wird aufgerufen, wenn die Reservierung erfolgreich war
     * 
     * @param event Das zu publizierende Event
     */
    public void publishProductReservationConfirmed(ProductReservationConfirmedEvent event) {
        try {
            log.info("Publishing ProductReservationConfirmedEvent für Cart ID: {}", event.getCartId());
            
            Message<ProductReservationConfirmedEvent> message = MessageBuilder
                    .withPayload(event)
                    .setHeader(org.springframework.messaging.MessageHeaders.CONTENT_TYPE, 
                               MimeTypeUtils.APPLICATION_JSON)
                    .build();
            
            boolean sent = channel.productReservationConfirmedChannel().send(message);
            if (sent) {
                log.info("ProductReservationConfirmedEvent erfolgreich publiziert für Cart ID: {}", 
                        event.getCartId());
            } else {
                log.error("Fehler beim Publizieren von ProductReservationConfirmedEvent für Cart ID: {}", 
                        event.getCartId());
            }
        } catch (Exception e) {
            log.error("Fehler beim Publizieren von ProductReservationConfirmedEvent: {}", 
                    e.getMessage(), e);
            throw new RuntimeException("Fehler beim Publizieren von ProductReservationConfirmedEvent", e);
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
            log.info("Publishing ProductReservationFailedEvent für Cart ID: {}", event.getCartId());
            
            Message<ProductReservationFailedEvent> message = MessageBuilder
                    .withPayload(event)
                    .setHeader(org.springframework.messaging.MessageHeaders.CONTENT_TYPE, 
                               MimeTypeUtils.APPLICATION_JSON)
                    .build();
            
            boolean sent = channel.productReservationFailedChannel().send(message);
            if (sent) {
                log.info("ProductReservationFailedEvent erfolgreich publiziert für Cart ID: {}", 
                        event.getCartId());
            } else {
                log.error("Fehler beim Publizieren von ProductReservationFailedEvent für Cart ID: {}", 
                        event.getCartId());
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

### Schritt 7: Cart Event Listener erstellen
**Datei:** `Product Service/src/main/java/at/fhv/productservice/infrastructure/messaging/CartEventListener.java`

```java
package at.fhv.productservice.infrastructure.messaging;

import at.fhv.productservice.config.ProductMessagingConfig;
import at.fhv.productservice.events.CartCheckoutEvent;
import at.fhv.productservice.domain.service.ProductReservationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.stereotype.Component;

/**
 * Listener für Cart Checkout Events
 * Delegiert die Business-Logik an ProductReservationService
 */
@Slf4j
@Component
@EnableBinding(ProductMessagingConfig.CartEventListeningChannel.class)
public class CartEventListener {

    @Autowired
    private ProductReservationService reservationService;  // Domain Service!

    /**
     * Verarbeitet CartCheckoutEvent
     * Delegiert die Reservierungslogik an ProductReservationService
     * 
     * @param event Das empfangene Event
     */
    @StreamListener(ProductMessagingConfig.CART_CHECKOUT_CHANNEL)
    public void handleCartCheckout(CartCheckoutEvent event) {
        try {
            log.info("Received CartCheckoutEvent für Cart ID: {}, Product ID: {}, Quantity: {}", 
                    event.getCartId(), event.getProductId(), event.getQuantity());
            
            // ✅ Delegiere an Domain Service (wo die Business-Logik ist!)
            reservationService.reserveProduct(
                event.getCartId(),
                event.getProductId(),
                event.getQuantity()
            );
            
        } catch (Exception e) {
            log.error("Fehler beim Verarbeiten von CartCheckoutEvent: {}", e.getMessage(), e);
        }
    }
}
```

**Wichtig:** Der Listener ist nur eine **dünne Schicht**, die das Event empfängt und an die **Domain Service** delegiert. Die gesamte **Business-Logik** (Verfügbarkeitsprüfung, Reservierung, Event-Publishing) ist in `ProductReservationService`!

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
**Datei:** `Product Service/src/test/java/at/fhv/productservice/messaging/CartEventListenerTest.java`

```java
package at.fhv.productservice.messaging;

import at.fhv.productservice.config.ProductMessagingConfig;
import at.fhv.productservice.events.CartCheckoutEvent;
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
class CartEventListenerTest {

    @Autowired
    private CartEventListener cartEventListener;

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
    void testHandleCartCheckout_Success() {
        UUID cartId = UUID.randomUUID();
        CartCheckoutEvent event = new CartCheckoutEvent(
            cartId, UUID.randomUUID(), productId, 10, 99.99
        );

        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        cartEventListener.handleCartCheckout(event);

        // Überprüfe, dass die Menge reserviert wurde
        verify(productRepository, times(1)).save(testProduct);
        verify(reservationPublisher, times(1))
            .publishProductReservationConfirmed(any());
    }

    @Test
    void testHandleCartCheckout_InsufficientStock() {
        testProduct.setQuantity(5);
        
        UUID cartId = UUID.randomUUID();
        CartCheckoutEvent event = new CartCheckoutEvent(
            cartId, UUID.randomUUID(), productId, 10, 99.99
        );

        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));

        cartEventListener.handleCartCheckout(event);

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
- [ ] Step 3: Alle 3 Event-Klassen erstellt (IDENTISCH mit A!)
- [ ] Step 4.1: Product Entity um reserved_quantity erweitert
- [ ] Step 4.2: ProductReservationService erstellt (Domain Service mit Business-Logik!)
- [ ] Step 5: ProductMessagingConfig erstellt
- [ ] Step 6: ProductReservationEventPublisher erstellt
- [ ] Step 7: CartEventListener erstellt (delegiert an Service)
- [ ] Step 8: OpenAPI-Annotationen hinzugefügt
- [ ] Step 9: Unit Tests geschrieben und ausgeführt
- [ ] Mit Person A: Integration testen nach deren Completion

---

## 🔄 Saga Flow - Was passiert:

```
1. Person A (CartService): Cart Checkout
   ↓
2. RabbitMQ: CartCheckoutEvent in "cart-checkout-events" Queue
   ↓
3. Sie (ProductService): CartEventListener empfängt Event
   - Überprüfe Produktmenge
   - Wenn genug: reservieren → ProductReservationConfirmedEvent ("CONFIRMED")
   - Wenn nicht: ProductReservationFailedEvent ("INSUFFICIENT_STOCK")
   ↓
4. RabbitMQ: Response-Event in "product-reservation-events" Queue
   ↓
5. Person A (CartService): Empfängt Response
   - CONFIRMED: Cart Status → CHECKED_OUT
   - FAILED: Cart Status → CHECKOUT_FAILED
```

---

## 📝 Wichtige Notizen:

- **Lokales RabbitMQ:** Stellen Sie sicher, dass RabbitMQ lokal läuft (Port 5672)
- **Datenbankmigration:** Achten Sie auf die neue Spalte `reserved_quantity`
- **Idempotenz:** Events können doppelt ankommen - nutzen Sie cartId zur Deduplizierung falls nötig
- **Error Handling:** Fangen Sie Exceptions ab und publizieren Sie Fehler-Events
- **Logging:** Nutzen Sie die Logs zum Debuggen
- **Zusammenarbeit mit Person A:** Testen Sie zusammen die End-to-End Flow

---

## 🧪 Manuelle Test-Szenarien:

### Szenario 1: Erfolgreiche Reservierung
1. Produkt mit 100 Stück erstellen
2. Cart mit 10 Stück von Person A checken out
3. Überprüfen: Product.reservedQuantity = 10
4. Überprüfen: Cart.status = CHECKED_OUT

### Szenario 2: Unzureichender Bestand
1. Produkt mit 5 Stück erstellen
2. Cart mit 10 Stück von Person A checken out
3. Überprüfen: Product.reservedQuantity = 0
4. Überprüfen: Cart.status = CHECKOUT_FAILED

---

**Status:** Bereit zur Implementierung  
**Geschätzter Aufwand:** 8-10 Stunden  
**Abhängig von:** Person A (Cart Service)

