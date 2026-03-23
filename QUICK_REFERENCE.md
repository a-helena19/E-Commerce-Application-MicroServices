# ⚡ Quick Reference – Asynchrone Kommunikation Setup

## 🚀 Start in 5 Minuten

### 1️⃣ RabbitMQ starten (Terminal)
```bash
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 \
  -e RABBITMQ_DEFAULT_USER=guest \
  -e RABBITMQ_DEFAULT_PASS=guest \
  rabbitmq:3.13-management
```

Überprüfen: `http://localhost:15672` (guest/guest)

---

### 2️⃣ Person A & B: Dependencies hinzufügen
**build.gradle bearbeiten:**
```groovy
dependencies {
    implementation 'org.springframework.cloud:spring-cloud-starter-stream-rabbit'
    testImplementation 'org.springframework.cloud:spring-cloud-stream-test-support'
}

dependencyManagement {
    imports {
        mavenBom 'org.springframework.cloud:spring-cloud-dependencies:2023.0.1'
    }
}
```

---

### 3️⃣ application.properties konfigurieren
```properties
# RabbitMQ Verbindung
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest

# Debug Logging
logging.level.org.springframework.cloud.stream=DEBUG
```

---

## 📝 Schnelle Code-Snippets

### Event-Klasse
```java
public class OrderCreatedEvent {
    private UUID orderId;
    private int quantity;
    // + Getter/Setter
}
```

### Messaging Config
```java
@Configuration
public class OrderMessagingConfig {
    public static final String ORDER_CREATED_CHANNEL = "orderCreated-out";
    
    public interface OrderEventPublishingChannel {
        @Output(ORDER_CREATED_CHANNEL)
        MessageChannel orderCreatedChannel();
    }
}
```

### Event Publishing
```java
@Component
@EnableBinding(OrderMessagingConfig.OrderEventPublishingChannel.class)
public class OrderEventPublisher {
    @Autowired
    private OrderMessagingConfig.OrderEventPublishingChannel channel;
    
    public void publish(OrderCreatedEvent event) {
        channel.orderCreatedChannel().send(
            MessageBuilder.withPayload(event).build()
        );
    }
}
```

### Event Listening
```java
@Component
@EnableBinding(ProductMessagingConfig.OrderEventListeningChannel.class)
public class OrderEventListener {
    @StreamListener(ProductMessagingConfig.ORDER_CREATED_CHANNEL)
    public void handle(OrderCreatedEvent event) {
        // Process event
    }
}
```

---

## 🔗 Message Binding Übersicht

### Person A (Order Service)
```properties
# OUT: Events die wir publizieren
spring.cloud.stream.bindings.orderCreated-out.destination=order-events
spring.cloud.stream.bindings.orderCanceled-out.destination=order-events

# IN: Events die wir empfangen
spring.cloud.stream.bindings.productReservationUpdated-in.destination=product-reservation-events
spring.cloud.stream.bindings.productReservationUpdated-in.group=order-service-group
```

### Person B (Product Service)
```properties
# IN: Events die wir empfangen
spring.cloud.stream.bindings.orderCreated-in.destination=order-events
spring.cloud.stream.bindings.orderCreated-in.group=product-service-group

# OUT: Events die wir publizieren
spring.cloud.stream.bindings.productReservationUpdated-out.destination=product-reservation-events
```

---

## 🧪 Schneller Test

### Test 1: Event Publishing (Person A)
```bash
# Bestellt 5 Stück des Produkts prod-123
curl -X POST http://localhost:8094/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user-456",
    "productId": "prod-123",
    "quantity": 5,
    "price": 99.99
  }'
```

Erwartet:
- Response: `{"id": "order-789", "status": "PENDING"}`
- RabbitMQ Console: 1 Message in `order-events` Queue
- Person B Logs: `Received OrderCreatedEvent`

### Test 2: Event Processing (Person B)
- Person B sollte automatisch das Event verarbeiten
- Product.reservedQuantity sollte auf 5 erhöht sein
- Response-Event sollte in `product-reservation-events` Queue sein

### Test 3: Order Confirmation (Person A)
```bash
# Nach ~1 Sekunde Order überprüfen
curl http://localhost:8094/api/orders/order-789
```

Erwartet:
- `"status": "CONFIRMED"` (wenn Stock verfügbar)
- oder `"status": "CANCELED"` (wenn Stock nicht verfügbar)

---

## 🐛 Debugging-Befehle

### RabbitMQ Queues überprüfen
```bash
# Mit Docker
docker exec rabbitmq rabbitmqctl list_queues

# Mit Management UI
http://localhost:15672/
```

### Logs filtern
```bash
# In IDE: Filter nach "Cloud Stream"
logging.level.org.springframework.cloud.stream=DEBUG

# Oder: Filter nach Service-Package
logging.level.at.fhv.orderservice=DEBUG
logging.level.at.fhv.productservice=DEBUG
```

### Spring Boot Build & Run
```bash
# Build
./gradlew clean build

# Run
./gradlew bootRun

# Oder JAR
java -jar build/libs/Order-Service-0.0.1-SNAPSHOT.jar
```

---

## 📊 Event Kommunikations-Matrix

```
Person A (Order Service)          RabbitMQ              Person B (Product Service)
─────────────────────────         ────────              ──────────────────────────

Publiziert:                    Queue: order-events
├─ OrderCreatedEvent    ──────→ (auto-ack)        ──→ Empfängt
├─ OrderCanceledEvent   ──────→                   ──→ Verarbeitet
                                                      └─ Reserviert/Gibt frei
                                                      └─ Publiziert Response
                                
                            Queue: product-reservation-events
                            (UPDATED Event)
Empfängt ←──────────────────────────── Publiziert
└─ Aktualisiert                      (FAILED Event)
   Order Status
```

---

## ✅ Checkpoint-Checkliste

### Vor dem Start
- [ ] RabbitMQ läuft auf Port 5672
- [ ] RabbitMQ Management UI erreichbar (15672)
- [ ] Beide Services compilieren ohne Fehler

### Nach Dependencies
- [ ] Beide Services bauen erfolgreich
- [ ] Keine Spring Cloud Stream Fehler beim Start
- [ ] Logs zeigen "Binding ... Channel"

### Nach Implementation
- [ ] Services starten ohne Fehler
- [ ] "Successfully initialized Spring Cloud Stream" in Logs
- [ ] Swagger UI erreichbar
- [ ] RabbitMQ Queues sichtbar in Management UI

### Nach erstem Test
- [ ] Order erstellen funktioniert
- [ ] Event in Queue sichtbar
- [ ] Product Service empfängt Event (in Logs)
- [ ] Order Status wird aktualisiert

---

## 🎯 Minimales Working Example (MWE)

### Nur die essentiellen Files:

**Datei 1: OrderCreatedEvent.java**
```java
@Data
public class OrderCreatedEvent {
    UUID orderId;
    UUID productId;
    int quantity;
}
```

**Datei 2: OrderMessagingConfig.java**
```java
@Configuration
public class OrderMessagingConfig {
    public static final String CHANNEL = "orderCreated-out";
    public interface Channel {
        @Output(CHANNEL)
        MessageChannel channel();
    }
}
```

**Datei 3: OrderEventPublisher.java**
```java
@Component
@EnableBinding(OrderMessagingConfig.Channel.class)
public class OrderEventPublisher {
    @Autowired OrderMessagingConfig.Channel ch;
    
    public void send(OrderCreatedEvent e) {
        ch.channel().send(MessageBuilder.withPayload(e).build());
    }
}
```

**Datei 4: OrderEventListener.java**
```java
@Component
@EnableBinding(ProductMessagingConfig.Channel.class)
public class OrderEventListener {
    @StreamListener(ProductMessagingConfig.CHANNEL)
    public void handle(OrderCreatedEvent e) {
        System.out.println("Received: " + e.orderId);
    }
}
```

**Datei 5: application.properties**
```properties
spring.rabbitmq.host=localhost
spring.cloud.stream.bindings.orderCreated-out.destination=order-events
spring.cloud.stream.bindings.orderCreated-in.destination=order-events
spring.cloud.stream.bindings.orderCreated-in.group=my-group
```

Das ist alles! 🎉

---

## 🆘 Häufige Fehler & Fixes

| Error | Ursache | Fix |
|-------|--------|-----|
| `Connection refused` | RabbitMQ nicht aktiv | `docker ps` überprüfen, Container starten |
| `Binding not found` | Event-Klasse falsch | Getter/Setter überprüfen, `@Data` Annotation |
| `Queue not declared` | Destination falsch | `application.properties` überprüfen, Service neu starten |
| `Jackson error` | Serialisierung fehlt | `@JsonProperty`, `@JsonIgnore` verwenden |
| `Timeout receiving` | Listener blockiert | Listener muss async sein, keine blocking ops |
| `No consumer` | Group nicht gesetzt | `spring.cloud.stream.bindings.xxx.group` ergänzen |

---

## 📞 Support bei Blockern

1. **RabbitMQ Probleme**: Management UI überprüfen (15672)
2. **Event kommt nicht an**: Logs auf "Received" oder "Published" filtern
3. **Order Status aktualisiert sich nicht**: Product Service Logs überprüfen
4. **Port-Konflikt**: `netstat -ano | findstr 8094` (Windows) oder `lsof -i :8094` (Mac/Linux)
5. **Build-Fehler**: `./gradlew clean build --refresh-dependencies`

---

**Viel Erfolg! 🚀**

