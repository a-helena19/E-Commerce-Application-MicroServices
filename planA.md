# Person A – Aufgabenplan für Cart Service
## Asynchronous Communication mit Spring Cloud Stream

**Datum:** 23.03.2026  
**Fokus:** Cart Service - Event Publishing und Listening  
**Partner:** Person B (Product Service)  
**Messaging Broker:** RabbitMQ

---

## 🎯 Überblick Ihrer Aufgaben

Sie sind verantwortlich für den **Cart Service**. Ihre Aufgabe ist es, folgende Events zu **publizieren** und zu **empfangen**:

### Events die Sie publizieren:
- `CartCheckoutEvent` - Wenn ein Kunde seinen Cart checkt out

### Events die Sie empfangen:
- `ProductReservationConfirmedEvent` - Von Product Service (Reservierung bestätigt)
- `ProductReservationFailedEvent` - Von Product Service (Reservierung fehlgeschlagen)

---

## 📋 Schritt-für-Schritt Implementierung

### Schritt 1: Dependencies zu build.gradle hinzufügen
**Datei:** `Cart Service/build.gradle`

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
**Datei:** `Cart Service/src/main/resources/application.properties`

Fügen Sie folgende Konfiguration hinzu:

```properties
# Behalte bestehende Konfiguration

# ===== RabbitMQ Konfiguration =====
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest

# ===== Spring Cloud Stream Bindings =====
# Outbound: Events die Cart Service publiziert
spring.cloud.stream.bindings.cartCheckout-out.destination=cart-checkout-events
spring.cloud.stream.bindings.cartCheckout-out.contentType=application/json

# Inbound: Events die Cart Service von Product Service empfängt
spring.cloud.stream.bindings.productReservationConfirmed-in.destination=product-reservation-events
spring.cloud.stream.bindings.productReservationConfirmed-in.group=cart-service-group
spring.cloud.stream.bindings.productReservationConfirmed-in.contentType=application/json

spring.cloud.stream.bindings.productReservationFailed-in.destination=product-reservation-events
spring.cloud.stream.bindings.productReservationFailed-in.group=cart-service-group
spring.cloud.stream.bindings.productReservationFailed-in.contentType=application/json

# Optional: Logging für Message Tracing
logging.level.org.springframework.cloud.stream=DEBUG
logging.level.org.springframework.amqp=DEBUG
```

---

### Schritt 3: Event-Klassen erstellen
**Ordner:** `Cart Service/src/main/java/at/fhv/cartservice/events/`

Erstellen Sie folgende 3 Event-Klassen:

#### 3.1 CartCheckoutEvent.java
```java
package at.fhv.cartservice.events;

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
package at.fhv.cartservice.events;

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
package at.fhv.cartservice.events;

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

### Schritt 4: Messaging-Konfiguration (Binding Interfaces)
**Datei:** `Cart Service/src/main/java/at/fhv/cartservice/config/CartMessagingConfig.java`

```java
package at.fhv.cartservice.config;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.context.annotation.Configuration;

/**
 * Messaging-Konfiguration für Cart Service
 * Definiert Input (empfangen) und Output (publizieren) Kanäle
 */
@Configuration
public class CartMessagingConfig {

    // ===== Outbound Channels (Events die wir publizieren) =====
    
    public static final String CART_CHECKOUT_CHANNEL = "cartCheckout-out";
    
    // ===== Inbound Channels (Events die wir empfangen) =====
    
    public static final String PRODUCT_RESERVATION_CONFIRMED_CHANNEL = "productReservationConfirmed-in";
    public static final String PRODUCT_RESERVATION_FAILED_CHANNEL = "productReservationFailed-in";

    /**
     * Interface für Cart Event Publishing
     */
    public interface CartEventPublishingChannel {
        
        @Output(CART_CHECKOUT_CHANNEL)
        MessageChannel cartCheckoutChannel();
    }

    /**
     * Interface für Product Reservation Event Listening
     */
    public interface ProductReservationListeningChannel {
        
        @Input(PRODUCT_RESERVATION_CONFIRMED_CHANNEL)
        SubscribableChannel productReservationConfirmedChannel();
        
        @Input(PRODUCT_RESERVATION_FAILED_CHANNEL)
        SubscribableChannel productReservationFailedChannel();
    }
}
```

---

### Schritt 5: Event Publisher erstellen
**Datei:** `Cart Service/src/main/java/at/fhv/cartservice/messaging/CartEventPublisher.java`

```java
package at.fhv.cartservice.messaging;

import at.fhv.cartservice.config.CartMessagingConfig;
import at.fhv.cartservice.events.CartCheckoutEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeTypeUtils;

/**
 * Publisher für Cart-Events
 * Publiziert CartCheckoutEvent zu RabbitMQ
 */
@Slf4j
@Component
@EnableBinding(CartMessagingConfig.CartEventPublishingChannel.class)
public class CartEventPublisher {

    @Autowired
    private CartMessagingConfig.CartEventPublishingChannel channel;

    /**
     * Publiziert ein CartCheckoutEvent
     * Wird aufgerufen, wenn ein Kunde seinen Cart checkt out
     * 
     * @param event Das zu publizierende Event
     */
    public void publishCartCheckout(CartCheckoutEvent event) {
        try {
            log.info("Publishing CartCheckoutEvent für Cart ID: {}", event.getCartId());
            
            Message<CartCheckoutEvent> message = MessageBuilder
                    .withPayload(event)
                    .setHeader(org.springframework.messaging.MessageHeaders.CONTENT_TYPE, 
                               MimeTypeUtils.APPLICATION_JSON)
                    .build();
            
            boolean sent = channel.cartCheckoutChannel().send(message);
            if (sent) {
                log.info("CartCheckoutEvent erfolgreich publiziert für Cart ID: {}", event.getCartId());
            } else {
                log.error("Fehler beim Publizieren von CartCheckoutEvent für Cart ID: {}", event.getCartId());
            }
        } catch (Exception e) {
            log.error("Fehler beim Publizieren von CartCheckoutEvent: {}", e.getMessage(), e);
            throw new RuntimeException("Fehler beim Publizieren von CartCheckoutEvent", e);
        }
    }
}
```

---

### Schritt 6: Event Listener erstellen
**Datei:** `Cart Service/src/main/java/at/fhv/cartservice/messaging/ProductReservationEventListener.java`

```java
package at.fhv.cartservice.messaging;

import at.fhv.cartservice.config.CartMessagingConfig;
import at.fhv.cartservice.events.ProductReservationConfirmedEvent;
import at.fhv.cartservice.events.ProductReservationFailedEvent;
import at.fhv.cartservice.model.Cart;
import at.fhv.cartservice.model.CartStatus;
import at.fhv.cartservice.repository.CartRepository;
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
@EnableBinding(CartMessagingConfig.ProductReservationListeningChannel.class)
public class ProductReservationEventListener {

    @Autowired
    private CartRepository cartRepository;

    /**
     * Verarbeitet ProductReservationConfirmedEvent
     * Wird aufgerufen, wenn die Reservierung erfolgreich war
     * 
     * @param event Das empfangene Event
     */
    @StreamListener(CartMessagingConfig.PRODUCT_RESERVATION_CONFIRMED_CHANNEL)
    public void handleProductReservationConfirmed(ProductReservationConfirmedEvent event) {
        try {
            log.info("Received ProductReservationConfirmedEvent für Cart ID: {}", event.getCartId());
            
            Cart cart = cartRepository.findById(event.getCartId())
                    .orElseThrow(() -> new RuntimeException("Cart nicht gefunden: " + event.getCartId()));
            
            if ("CONFIRMED".equals(event.getStatus())) {
                log.info("Produkt erfolgreich reserviert für Cart ID: {}", event.getCartId());
                cart.setStatus(CartStatus.CHECKED_OUT);
                cartRepository.save(cart);
                log.info("Cart Status zu CHECKED_OUT aktualisiert für Cart ID: {}", event.getCartId());
            } else {
                log.warn("Unerwarteter Status in ProductReservationConfirmedEvent: {}", event.getStatus());
            }
        } catch (Exception e) {
            log.error("Fehler beim Verarbeiten von ProductReservationConfirmedEvent: {}", e.getMessage(), e);
        }
    }

    /**
     * Verarbeitet ProductReservationFailedEvent
     * Wird aufgerufen, wenn die Reservierung fehlgeschlagen ist
     * 
     * @param event Das empfangene Event
     */
    @StreamListener(CartMessagingConfig.PRODUCT_RESERVATION_FAILED_CHANNEL)
    public void handleProductReservationFailed(ProductReservationFailedEvent event) {
        try {
            log.info("Received ProductReservationFailedEvent für Cart ID: {}", event.getCartId());
            
            Cart cart = cartRepository.findById(event.getCartId())
                    .orElseThrow(() -> new RuntimeException("Cart nicht gefunden: " + event.getCartId()));
            
            log.warn("Reservierung fehlgeschlagen für Cart ID: {}. Grund: {}", 
                    event.getCartId(), event.getReason());
            
            cart.setStatus(CartStatus.CHECKOUT_FAILED);
            cartRepository.save(cart);
            log.info("Cart Status zu CHECKOUT_FAILED aktualisiert für Cart ID: {}", event.getCartId());
        } catch (Exception e) {
            log.error("Fehler beim Verarbeiten von ProductReservationFailedEvent: {}", e.getMessage(), e);
        }
    }
}
```

---

### Schritt 7: Cart Service anpassen
**Datei:** `Cart Service/src/main/java/at/fhv/cartservice/service/CartService.java`

Sie müssen die bestehende `CartService` Klasse anpassen:

1. Injizieren Sie den `CartEventPublisher`:
```java
@Autowired
private CartEventPublisher cartEventPublisher;
```

2. In der `checkout()` Methode, nach dem Speichern des Cart mit Status PENDING, das Event publizieren:
```java
public Cart checkout(UUID cartId) {
    Cart cart = cartRepository.findById(cartId)
            .orElseThrow(() -> new CartNotFoundException("Cart nicht gefunden"));
    
    cart.setStatus(CartStatus.PENDING);
    cart = cartRepository.save(cart);
    
    // 👇 NEU: Event publizieren
    CartCheckoutEvent event = new CartCheckoutEvent(
        cart.getId(),
        cart.getUserId(),
        cart.getProductId(),  // Annahme: Cart hat ein Produkt
        cart.getQuantity(),
        cart.getTotalPrice()
    );
    cartEventPublisher.publishCartCheckout(event);
    
    return cart;
}
```

---

### Schritt 8: OpenAPI-Dokumentation hinzufügen
**Datei:** `Cart Service/src/main/java/at/fhv/cartservice/controller/CartController.java`

Fügen Sie OpenAPI-Annotationen zu Ihren Endpoints hinzu:

```java
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

@RestController
@RequestMapping("/api/carts")
public class CartController {

    @PostMapping("/{id}/checkout")
    @Operation(summary = "Checkout cart", 
               description = "Initiates cart checkout and publishes CartCheckoutEvent")
    @ApiResponse(responseCode = "200", description = "Checkout initiated successfully",
                 content = @Content(mediaType = "application/json", 
                                   schema = @Schema(implementation = CartDTO.class)))
    @ApiResponse(responseCode = "404", description = "Cart not found")
    public ResponseEntity<CartDTO> checkout(@PathVariable UUID id) {
        // Implementation
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get cart by ID")
    @ApiResponse(responseCode = "200", description = "Cart found")
    @ApiResponse(responseCode = "404", description = "Cart not found")
    public ResponseEntity<CartDTO> getCart(@PathVariable UUID id) {
        // Implementation
    }

    @PostMapping
    @Operation(summary = "Create a new cart")
    @ApiResponse(responseCode = "201", description = "Cart created successfully")
    public ResponseEntity<CartDTO> createCart(@RequestBody @Valid CartCreateRequest request) {
        // Implementation
    }
}
```

---

### Schritt 9: Unit Tests schreiben
**Datei:** `Cart Service/src/test/java/at/fhv/cartservice/messaging/CartEventPublisherTest.java`

```java
package at.fhv.cartservice.messaging;

import at.fhv.cartservice.config.CartMessagingConfig;
import at.fhv.cartservice.events.CartCheckoutEvent;
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
class CartEventPublisherTest {

    @Autowired
    private CartEventPublisher cartEventPublisher;

    @Autowired
    private CartMessagingConfig.CartEventPublishingChannel publishingChannel;

    @Autowired
    private MessageCollector messageCollector;

    @Test
    void testPublishCartCheckoutEvent() {
        UUID cartId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        CartCheckoutEvent event = new CartCheckoutEvent(
            cartId, userId, productId, 5, 99.99
        );

        cartEventPublisher.publishCartCheckout(event);

        Message<?> message = messageCollector.forChannel(publishingChannel.cartCheckoutChannel()).poll();
        
        assertNotNull(message);
        assertTrue(message.getPayload() instanceof CartCheckoutEvent);
        
        CartCheckoutEvent receivedEvent = (CartCheckoutEvent) message.getPayload();
        assertEquals(cartId, receivedEvent.getCartId());
        assertEquals(userId, receivedEvent.getUserId());
        assertEquals(5, receivedEvent.getQuantity());
    }
}
```

---

## ✅ Checkliste für Person A

- [ ] Step 1: Dependencies zu build.gradle hinzufügen
- [ ] Step 2: application.properties konfiguriert
- [ ] Step 3: Alle 3 Event-Klassen erstellt
- [ ] Step 4: CartMessagingConfig erstellt
- [ ] Step 5: CartEventPublisher erstellt und getestet
- [ ] Step 6: ProductReservationEventListener erstellt
- [ ] Step 7: CartService angepasst (checkout)
- [ ] Step 8: OpenAPI-Annotationen hinzugefügt
- [ ] Step 9: Unit Tests geschrieben und ausgeführt
- [ ] Mit Person B: Integration testen nach deren Completion

---

## 🔄 Saga Flow - Was passiert:

```
1. USER: Cart Checkout
   ↓
2. CartService.checkout()
   - Cart speichern (Status: PENDING)
   - CartCheckoutEvent publizieren
   ↓
3. RabbitMQ: Nachricht in "cart-checkout-events" Queue
   ↓
4. ProductService: Empfängt CartCheckoutEvent
   - Produkt reservieren
   - ProductReservationConfirmedEvent oder ProductReservationFailedEvent publizieren
   ↓
5. RabbitMQ: Nachricht in "product-reservation-events" Queue
   ↓
6. CartService: ProductReservationEventListener empfängt Event
   - Wenn SUCCESS: Cart Status → CHECKED_OUT
   - Wenn FAILED: Cart Status → CHECKOUT_FAILED
```

---

## 📝 Wichtige Notizen:

- **Lokales RabbitMQ:** Stellen Sie sicher, dass RabbitMQ lokal läuft (Port 5672)
- **Idempotenz:** Events können doppelt ankommen - bauen Sie Idempotenz ein
- **Logging:** Nutzen Sie die Logs zum Debuggen
- **Cart Status:** Achten Sie auf die korrekten Status-Übergänge
- **Zusammenarbeit mit Person B:** Testen Sie zusammen die End-to-End Flow

---

**Status:** Bereit zur Implementierung  
**Geschätzter Aufwand:** 8-10 Stunden

