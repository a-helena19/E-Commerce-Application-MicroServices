# Plan B - Person B: Product Service (Consumer/Event Listener)

## Übersicht
Person B ist verantwortlich für die Product Service Seite der asynchronen Kommunikation. Der Fokus liegt auf dem Konsumieren und Verarbeiten des `OrderCanceledEvent` von der Order Service. Wenn ein Event empfangen wird, wird die Reservierung für die Produkte wieder hergestellt.

**Usecase:** OrderCanceledEvent wird von RabbitMQ empfangen → Product Service konsumiert das Event → Stock für alle Produkte in der Order wird erhöht.

---

## Phase 1: Dependencies & Konfiguration hinzufügen

### 1.1 Product Service build.gradle Update
**Datei:** `Product Service/build.gradle`

**Was zu tun ist:**
- Spring Cloud Stream Dependencies für RabbitMQ hinzufügen
- Spring Cloud Config und weitere notwendige Dependencies

**Zu ändernde Dependencies-Sektion:**

```groovy
// Füge nach den bestehenden Spring Boot Dependencies folgende hinzu:
implementation 'org.springframework.cloud:spring-cloud-stream:4.1.0'
implementation 'org.springframework.cloud:spring-cloud-stream-binder-rabbit:4.1.0'
implementation 'org.springframework.boot:spring-boot-starter-amqp'
```

**Zusätzlich:** Noch vor dem `tasks.named('test')` Block folgende Dependency-Management Section hinzufügen:

```groovy
dependencyManagement {
    imports {
        mavenBom 'org.springframework.cloud:spring-cloud-dependencies:2023.0.0'
    }
}
```

---

### 1.2 application.properties für Product Service Update
**Datei:** `Product Service/src/main/resources/application.properties`

**Zu ändernde/hinzufügende Properties:**

```properties
# RabbitMQ Konfiguration
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest

# Spring Cloud Stream Konfiguration für Product Service (Consumer)
spring.cloud.stream.bindings.orderCanceledEventConsumer-in-0.destination=order-events
spring.cloud.stream.bindings.orderCanceledEventConsumer-in-0.content-type=application/json
spring.cloud.stream.bindings.orderCanceledEventConsumer-in-0.group=product-service-group

# RabbitMQ Binder Settings - Consumer Group für Failover/Skalierung
spring.cloud.stream.rabbit.bindings.orderCanceledEventConsumer-in-0.consumer.binding-routing-key=order.*
spring.cloud.stream.rabbit.bindings.orderCanceledEventConsumer-in-0.consumer.queue-name-group-only=true
```

---

## Phase 2: Event-Klasse erstellen (gleich wie Person A)

### 2.1 OrderCanceledEvent DTO Klasse
**Datei:** `Product Service/src/main/java/at/fhv/productservice/infrastructure/messaging/event/OrderCanceledEvent.java`

**Zu erstellende Klasse:**

```java
package at.fhv.productservice.infrastructure.messaging.event;

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
**Datei:** `Product Service/src/main/java/at/fhv/productservice/infrastructure/messaging/event/OrderItemEvent.java`

**Zu erstellende Klasse:**

```java
package at.fhv.productservice.infrastructure.messaging.event;

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

## Phase 3: Consumer/Listener Interface & Implementierung

### 3.1 OrderEventConsumer Functional Interface
**Datei:** `Product Service/src/main/java/at/fhv/productservice/infrastructure/messaging/consumer/OrderEventConsumer.java`

**Zu erstellende Interface-Klasse:**

Hinweis: Mit Spring Cloud Stream 4.x und Functional Programming Model wird das Consumer Function als Bean registriert, kein Interface nötig. Stattdessen erstellen wir direkt die Consumer-Bean:

```java
package at.fhv.productservice.infrastructure.messaging.consumer;

import java.util.function.Consumer;
import at.fhv.productservice.infrastructure.messaging.event.OrderCanceledEvent;

public interface OrderEventConsumer extends Consumer<OrderCanceledEvent> {
}
```

### 3.2 OrderEventConsumerImpl Implementierung
**Datei:** `Product Service/src/main/java/at/fhv/productservice/infrastructure/messaging/consumer/OrderEventConsumerImpl.java`

**Zu erstellende Implementierungs-Klasse:**

```java
package at.fhv.productservice.infrastructure.messaging.consumer;

import at.fhv.productservice.infrastructure.messaging.event.OrderCanceledEvent;
import at.fhv.productservice.infrastructure.messaging.event.OrderItemEvent;
import at.fhv.productservice.application.services.UpdateProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrderEventConsumerImpl {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderEventConsumerImpl.class);
    
    private final UpdateProductService updateProductService;
    
    public OrderEventConsumerImpl(UpdateProductService updateProductService) {
        this.updateProductService = updateProductService;
    }
    
    public void handleOrderCanceledEvent(OrderCanceledEvent event) {
        logger.info("Received OrderCanceledEvent for order: {}", event.getOrderId());
        
        try {
            // Für jedes Produkt im Order: Stock wiederherstellen
            for (OrderItemEvent item : event.getOrderItems()) {
                logger.info("Restoring stock for product: {}, quantity: {}", 
                           item.getProductId(), item.getQuantity());
                
                updateProductService.restoreStock(item.getProductId(), item.getQuantity());
            }
            
            logger.info("Successfully processed OrderCanceledEvent for order: {}", event.getOrderId());
        } catch (Exception e) {
            logger.error("Error processing OrderCanceledEvent for order: {}", 
                       event.getOrderId(), e);
            // Exception werfen → Message bleibt in Queue für Retry
            throw new RuntimeException("Failed to restore stock for order: " + event.getOrderId(), e);
        }
    }
}
```

### 3.3 MessagingConfig - Configuration Klasse für Consumer Bean
**Datei:** `Product Service/src/main/java/at/fhv/productservice/infrastructure/messaging/config/MessagingConfig.java`

**Zu erstellende Configuration-Klasse (IDIOMATISCH!):**

```java
package at.fhv.productservice.infrastructure.messaging.config;

import at.fhv.productservice.infrastructure.messaging.consumer.OrderEventConsumerImpl;
import at.fhv.productservice.infrastructure.messaging.event.OrderCanceledEvent;
import at.fhv.productservice.application.services.UpdateProductService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
public class MessagingConfig {
    
    @Bean
    public OrderEventConsumerImpl orderEventConsumerImpl(UpdateProductService updateProductService) {
        return new OrderEventConsumerImpl(updateProductService);
    }
    
    @Bean
    public Consumer<OrderCanceledEvent> orderCanceledEventConsumer(OrderEventConsumerImpl consumer) {
        return consumer::handleOrderCanceledEvent;
    }
}
```

---

## Phase 4: UpdateProductService erweitern

### 4.1 UpdateProductService Interface überprüfen/erweitern
**Datei:** `Product Service/src/main/java/at/fhv/productservice/application/services/UpdateProductService.java`

**Was zu tun ist:**
- Überprüfen, ob die Methode `restoreStock(UUID productId, int quantity)` existiert
- Falls nicht, diese Methode hinzufügen

**Zu erstellende/erweiternde Interface-Methode:**

```java
// Falls noch nicht vorhanden, hinzufügen:
public void restoreStock(UUID productId, int quantity);
```

### 4.2 UpdateProductServiceImpl Update
**Datei:** `Product Service/src/main/java/at/fhv/productservice/application/services/impl/UpdateProductServiceImpl.java`

**Was zu tun ist:**
- Die Methode `restoreStock` implementieren
- Diese Methode soll den Stock eines Produktes erhöhen

**Zu ändernde/hinzufügende Methode:**

```java
// Füge diese Methode zur UpdateProductServiceImpl Klasse hinzu:

@Override
@Transactional
public void restoreStock(UUID productId, int quantity) {
    Product product = productRepository.getProductById(productId);
    
    if (product == null) {
        throw new ProductNotFoundException(productId);
    }
    
    // Stock erhöhen (Reservierung rückgängig machen)
    product.increaseStock(quantity);
    productRepository.save(product);
    
    logger.info("Stock restored for product: {}, new quantity: {}", 
               productId, product.getStock());
}
```

---

## Phase 5: Logging & Error Handling

### 5.1 SLF4J Logger hinzufügen
**In OrderEventConsumerImpl und UpdateProductServiceImpl:**

```java
// Bereits im Code oben enthalten:
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

private static final Logger logger = LoggerFactory.getLogger(KlassenName.class);
```

### 5.2 application.properties - Logging erweitern
**Datei:** `Product Service/src/main/resources/application.properties`

**Optional hinzufügen:**

```properties
# Logging für Messaging
logging.level.at.fhv.productservice.infrastructure.messaging=INFO
logging.level.org.springframework.cloud.stream=DEBUG
logging.level.org.springframework.amqp=INFO
```

---

## Phase 6: Testen

### 6.1 Voraussetzungen
- RabbitMQ muss laufen (docker-compose up -d)
- Order Service läuft auf Port 8094
- Product Service läuft auf Port 8092

### 6.2 Manuelle Integration-Tests

**Test 1: Gesamter Workflow testen**

```bash
# 1. Produkt erstellen (auf Port 8092)
curl -X POST http://localhost:8092/products \
  -H "Content-Type: application/json" \
  -d '{
    "id": "660e8400-e29b-41d4-a716-446655440000",
    "name": "Test Product",
    "description": "A test product",
    "price": 19.99,
    "stock": 10
  }'

# 2. Überprüfen: Stock sollte 10 sein
curl http://localhost:8092/products/660e8400-e29b-41d4-a716-446655440000

# 3. Order erstellen mit diesem Produkt (auf Port 8094)
curl -X POST http://localhost:8094/orders \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "items": [
      {
        "productId": "660e8400-e29b-41d4-a716-446655440000",
        "quantity": 3,
        "price": 19.99
      }
    ]
  }'

# 4. Order-ID aus Response nehmen und Order löschen
# (Das triggert das OrderCanceledEvent)
curl -X DELETE http://localhost:8094/orders/<ORDER_ID>

# 5. Kurz warten (5-10 Sekunden für asynchrone Verarbeitung)

# 6. Produkt überprüfen: Stock sollte wieder 10 sein!
curl http://localhost:8092/products/660e8400-e29b-41d4-a716-446655440000
# Erwartet: "stock": 10

# 7. RabbitMQ Management UI überprüfen: http://localhost:15672
# - Username: guest, Passwort: guest
# - Unter "Queues" sollte "order-events.product-service-group" sichtbar sein
```

**Test 2: Logs überprüfen**

In der Konsole der Product Service sollte folgende Log-Ausgabe sichtbar sein:

```
INFO  ... Received OrderCanceledEvent for order: <ORDER_ID>
INFO  ... Restoring stock for product: 660e8400-e29b-41d4-a716-446655440000, quantity: 3
INFO  ... Successfully processed OrderCanceledEvent for order: <ORDER_ID>
```

**Test 3: Event in RabbitMQ überprüfen**

1. RabbitMQ Management UI öffnen: `http://localhost:15672`
2. Tab "Queues" aufrufen
3. Queue `order-events.product-service-group` auswählen
4. Button "Get messages" klicken
5. Das Event sollte in JSON-Format sichtbar sein:

```json
{
  "orderId": "...",
  "userId": "...",
  "orderItems": [
    {
      "productId": "660e8400-e29b-41d4-a716-446655440000",
      "quantity": 3
    }
  ],
  "timestamp": ...
}
```

---

## Phase 7: OpenAPI/Swagger Dokumentation (PFLICHT!)

### 7.1 ProductRestController mit OpenAPI Annotations aktualisieren
**Datei:** `Product Service/src/main/java/at/fhv/productservice/rest/ProductRestController.java`

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
@RequestMapping("/products")
@Tag(name = "Product Management", description = "APIs for managing products and inventory")
public class ProductRestController {
    
    // ...existing code...
    
    @Operation(
        summary = "Get all products",
        description = "Retrieve a list of all available products"
    )
    @ApiResponse(responseCode = "200", description = "List of products retrieved successfully")
    @GetMapping
    public ResponseEntity<List<GetProductDTO>> getAllProducts() {
        // ...existing implementation...
    }
    
    @Operation(
        summary = "Get product by ID",
        description = "Retrieve a product by its UUID"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Product found"),
        @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<GetProductDTO> getProduct(@PathVariable UUID id) {
        // ...existing implementation...
    }
    
    @Operation(
        summary = "Create new product",
        description = "Create a new product in the inventory"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Product created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid product data")
    })
    @PostMapping
    public ResponseEntity<GetProductDTO> createProduct(@Valid @RequestBody CreateProductDTO dto) {
        // ...existing implementation...
    }
    
    @Operation(
        summary = "Update product",
        description = "Update an existing product by its UUID"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Product updated successfully"),
        @ApiResponse(responseCode = "404", description = "Product not found"),
        @ApiResponse(responseCode = "400", description = "Invalid product data")
    })
    @PutMapping("/{id}")
    public ResponseEntity<GetProductDTO> updateProduct(@PathVariable UUID id, @Valid @RequestBody CreateProductDTO dto) {
        // ...existing implementation...
    }
    
    @Operation(
        summary = "Delete product",
        description = "Delete (mark as inactive) a product by its UUID"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Product deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable UUID id) {
        // ...existing implementation...
    }
}
```

**Swagger UI Zugriff:**
- Product Service: `http://localhost:8092/swagger-ui/index.html`
- Order Service: `http://localhost:8094/swagger-ui/index.html`

---

## Zusammenfassung der Dateien für Person B

**Zu erstellende Dateien:**
1. `Product Service/src/main/java/at/fhv/productservice/infrastructure/messaging/event/OrderCanceledEvent.java`
2. `Product Service/src/main/java/at/fhv/productservice/infrastructure/messaging/event/OrderItemEvent.java`
3. `Product Service/src/main/java/at/fhv/productservice/infrastructure/messaging/consumer/OrderEventConsumer.java`
4. `Product Service/src/main/java/at/fhv/productservice/infrastructure/messaging/consumer/OrderEventConsumerImpl.java`
5. `Product Service/src/main/java/at/fhv/productservice/infrastructure/messaging/config/MessagingConfig.java`

**Zu modifizierende Dateien:**
1. `Product Service/build.gradle` (Dependencies + dependencyManagement)
2. `Product Service/src/main/resources/application.properties` (RabbitMQ & Stream Config)
3. `Product Service/src/main/java/at/fhv/productservice/application/services/UpdateProductService.java` (evtl. restoreStock Methode hinzufügen)
4. `Product Service/src/main/java/at/fhv/productservice/application/services/impl/UpdateProductServiceImpl.java` (restoreStock Implementierung)
5. `Product Service/src/main/java/at/fhv/productservice/rest/ProductRestController.java` (Swagger Annotations - PFLICHT!)

---

## Geplante Abhängigkeiten mit Person A

Person A wird:
- Das Event publishen über OrderEventProducer
- DeleteOrderServiceImpl anpassen um das Event zu senden
- Den Docker Compose Setup für RabbitMQ bereitstellen

Die Kommunikation erfolgt über RabbitMQ und Spring Cloud Stream - komplett asynchron!

---

## Architektur Diagram

```
Order Service                          RabbitMQ                         Product Service
═══════════════════                   ════════════                      ═══════════════════

OrderRestController                                          
    │                          
    ├─ DELETE /orders/{id}                                                       
    │                          
DeleteOrderServiceImpl          
    │                          
    ├─ order.delete()          
    │                          
OrderEventProducerImpl          
    │                          
    └─> publishOrderCanceledEvent()      order-events      orderCanceledEventConsumer()
                                         queue              │
                                         │ (message)        │
                                         │                  ├─ Event.getOrderItems()
                                         │                  │
                                         └────────────────→ UpdateProductServiceImpl
                                                            │
                                                            ├─ restoreStock()
                                                            │
                                                            ProductRepository
                                                            │
                                                            └─> Product.increaseStock()
```

---

## Tipps & Hinweise

- **Keine Lombok**: Alle Getter/Setter sind manuell geschrieben ✓
- **Functional Consumer**: Statt @Input/@Output nutzen wir Function<> Beans
- **Consumer Group**: Wichtig für Failover und mehrere Instanzen
- **Error Handling**: Bei Fehler wird Exception geworfen → Message bleibt in Queue
- **Idempotenz**: Consumer sollte idempotent sein (mehrfaches Verarbeiten erlaubt)
- **Logging**: Mit Logger kann man den Event-Flow nachverfolgbar machen

---

## Nächste Schritte nach Implementierung

1. **Integration Test schreiben** für den gesamten Saga-Flow
2. **Error Scenarios** testen (z.B. wenn Produkt nicht existiert)
3. **Performance testen** mit mehreren Orders gleichzeitig
4. **Dead Letter Queue** implementieren für fehlerhafte Events
5. **Spring Cloud Sleuth** hinzufügen für Distributed Tracing (Optional)


