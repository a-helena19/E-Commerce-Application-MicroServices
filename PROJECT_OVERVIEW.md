# 🚀 Projekt-Übersicht: Asynchrone Kommunikation mit RabbitMQ
## Zusammenarbeit Person A + Person B

**Deadline:** 23.03.2026  
**Messaging Broker:** RabbitMQ (lokal auf Port 5672)  
**Services:** Cart Service + Product Service

---

## 📊 Arbeitsverteilung

### Person A: Cart Service
- **Port:** 8091
- **Aufgaben:**
  - ✅ Event Publishing: `CartCheckoutEvent`
  - ✅ Event Listening: `ProductReservationConfirmedEvent`, `ProductReservationFailedEvent`
  - ✅ Cart Status Management basierend auf Events
  - ✅ RabbitMQ Connection & Spring Cloud Stream Binding

### Person B: Product Service
- **Port:** 8092
- **Aufgaben:**
  - ✅ Event Listening: `CartCheckoutEvent`
  - ✅ Event Publishing: `ProductReservationConfirmedEvent`, `ProductReservationFailedEvent`
  - ✅ Produktreservierungslogik
  - ✅ RabbitMQ Connection & Spring Cloud Stream Binding

---

## 🔄 Event-Fluss (Choreography Pattern)

```
┌─────────────────────────────────────────────────────────────────────┐
│                    SAGA: Cart Checkout Flow                         │
└─────────────────────────────────────────────────────────────────────┘

┌──────────────┐
│ User/Client  │
└──────┬───────┘
       │
       │ POST /api/carts/{id}/checkout
       ↓
┌──────────────────────┐         ┌────────────────────┐
│  PERSON A            │         │  RabbitMQ          │
│  Cart Service        │         │  (Message Broker)  │
│  ──────────────      │         └────────────────────┘
│ - Checkout Cart      │                    ▲
│ - Status: PENDING    │                    │
│ - Publish Event ────────────→ cart-checkout-events queue
└──────────────────────┘                    │
                                             │
                                             ↓ (Consumer Group: product-service-group)
                                    ┌──────────────────────┐
                                    │  PERSON B            │
                                    │  Product Service     │
                                    │  ──────────────────  │
                                    │ - Receive Event      │
                                    │ - Check Stock        │
                                    │ - Reserve Quantity   │
                                    │ - Publish Response   │
                                    └──────────┬───────────┘
                                               │
                    ┌──────────────────────────┴──────────────────────┐
                    │                                                  │
      SUCCESS CASE              │             FAILURE CASE           │
                    │                                                  │
      ProductReservation    │      ProductReservation                │
      ConfirmedEvent        │      FailedEvent                       │
      (CONFIRMED)           │      (INSUFFICIENT_STOCK)              │
                    │                                                  │
                    ↓                                                  ↓
      ┌─────────────────────────┐           ┌──────────────────────────┐
      │ product-reservation-    │           │ product-reservation-     │
      │ events queue            │           │ events queue             │
      │ (cart-service-group)    │           │ (cart-service-group)     │
      └───────────┬─────────────┘           └───────────┬──────────────┘
                  │                                     │
                  ↓                                     ↓
      ┌──────────────────────┐           ┌──────────────────────┐
      │  PERSON A (Listen)   │           │  PERSON A (Listen)   │
      │  Update Cart         │           │  Update Cart         │
      │  Status: CHECKED_OUT │           │  Status: CHECKOUT_FAILED │
      └──────────────────────┘           └──────────────────────┘
```

---

## 🔌 RabbitMQ Queues & Exchanges

| Queue Name | Publisher | Consumer(s) | Description |
|-----------|-----------|-----------|-----------|
| `cart-checkout-events` | Cart Service (Person A) | Product Service (Person B) | Cart Checkouts |
| `product-reservation-events` | Product Service (Person B) | Cart Service (Person A) | Reservierungsresultate |

---

## 📦 Events Dokumentation

### Von Person A (Cart Service) publiziert:

#### CartCheckoutEvent
```json
{
  "cartId": "550e8400-e29b-41d4-a716-446655440000",
  "userId": "550e8400-e29b-41d4-a716-446655440001",
  "productId": "550e8400-e29b-41d4-a716-446655440002",
  "quantity": 5,
  "totalPrice": 99.99,
  "timestamp": "2026-03-23T10:30:00"
}
```

---

### Von Person B (Product Service) publiziert:

#### ProductReservationConfirmedEvent (SUCCESS)
```json
{
  "cartId": "550e8400-e29b-41d4-a716-446655440000",
  "productId": "550e8400-e29b-41d4-a716-446655440002",
  "reservedQuantity": 45,
  "status": "CONFIRMED",
  "timestamp": "2026-03-23T10:30:05"
}
```

#### ProductReservationFailedEvent (FAILURE)
```json
{
  "cartId": "550e8400-e29b-41d4-a716-446655440000",
  "productId": "550e8400-e29b-41d4-a716-446655440002",
  "requestedQuantity": 100,
  "reason": "INSUFFICIENT_STOCK",
  "timestamp": "2026-03-23T10:30:05"
}
```

---

## ⚙️ Konfiguration & Dependencies

### Gemeinsame Dependencies (beide Services)
```groovy
implementation 'org.springframework.cloud:spring-cloud-starter-stream-rabbit'
testImplementation 'org.springframework.cloud:spring-cloud-stream-test-support'
```

### Gemeinsame Dependency Management
```groovy
dependencyManagement {
    imports {
        mavenBom 'org.springframework.cloud:spring-cloud-dependencies:2023.0.1'
    }
}
```

### RabbitMQ Verbindung (Identisch für beide Services)
```properties
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest
```

---

## 🧪 Integrations-Test Szenarios

### Szenario 1: Happy Path - Cart Checkout mit ausreichendem Bestand

**Schritte:**
1. Person B: Produkt mit 100 Stück erstellen
   ```bash
   POST http://localhost:8092/api/products
   {
     "name": "Laptop",
     "quantity": 100,
     "price": 999.99
   }
   # Response: productId = "prod-123"
   ```

2. Person A: Cart erstellen
   ```bash
   POST http://localhost:8091/api/carts
   {
     "userId": "user-456",
     "productId": "prod-123",
     "quantity": 10,
     "totalPrice": 999.99
   }
   # Response: cartId = "cart-789"
   ```

3. Person A: Cart Checkout
   ```bash
   POST http://localhost:8091/api/carts/cart-789/checkout
   # Response: status = PENDING
   ```

4. **Automatisch (via RabbitMQ):**
   - CartCheckoutEvent wird publiziert
   - Product Service empfängt Event
   - Product Service reserviert 10 Stück
   - Product Service publiziert ProductReservationConfirmedEvent mit status=CONFIRMED

5. Person A: Cart Status überprüfen
   ```bash
   GET http://localhost:8091/api/carts/cart-789
   # Expected: status = CHECKED_OUT
   ```

6. Person B: Produkt überprüfen
   ```bash
   GET http://localhost:8092/api/products/prod-123
   # Expected: quantity = 100, reservedQuantity = 10, availableQuantity = 90
   ```

---

### Szenario 2: Fehlerfall - Cart Checkout mit unzureichendem Bestand

**Schritte:**
1. Person B: Produkt mit 5 Stück erstellen
   ```bash
   POST http://localhost:8092/api/products
   {
     "name": "Seltenes Item",
     "quantity": 5,
     "price": 499.99
   }
   # Response: productId = "prod-rare"
   ```

2. Person A: Cart mit 20 Stück erstellen
   ```bash
   POST http://localhost:8091/api/carts
   {
     "userId": "user-456",
     "productId": "prod-rare",
     "quantity": 20,
     "totalPrice": 9999.80
   }
   # Response: cartId = "cart-failed"
   ```

3. Person A: Cart Checkout
   ```bash
   POST http://localhost:8091/api/carts/cart-failed/checkout
   # Response: status = PENDING
   ```

4. **Automatisch (via RabbitMQ):**
   - CartCheckoutEvent wird publiziert
   - Product Service empfängt Event
   - Product Service prüft: 5 < 20 ❌
   - Product Service publiziert ProductReservationFailedEvent mit reason=INSUFFICIENT_STOCK

5. Person A: Cart Status überprüfen
   ```bash
   GET http://localhost:8091/api/carts/cart-failed
   # Expected: status = CHECKOUT_FAILED
   ```

6. Person B: Produkt überprüfen
   ```bash
   GET http://localhost:8092/api/products/prod-rare
   # Expected: reservedQuantity = 0 (keine Reservierung)
   ```

---


## 🚀 Startup-Anleitung

### Step 0: RabbitMQ starten

**Option A: Mit Docker Compose**
```bash
# Datei: docker-compose.yml (erstellen)
version: '3.8'
services:
  rabbitmq:
    image: rabbitmq:3.13-management
    ports:
      - "5672:5672"      # AMQP Port
      - "15672:15672"    # Management UI
    environment:
      RABBITMQ_DEFAULT_USER: guest
      RABBITMQ_DEFAULT_PASS: guest
    volumes:
      - rabbitmq_data:/var/lib/rabbitmq

volumes:
  rabbitmq_data:
```

```bash
cd project-root
docker-compose up -d
```

Zugriff auf Management UI: `http://localhost:15672` (guest/guest)

**Option B: Lokale Installation**
- Download: https://www.rabbitmq.com/download.html
- Installation durchführen
- RabbitMQ Service starten

---

### Step 1: Services bauen & starten

**Person A: Order Service**
```bash
cd "Order Service"
./gradlew clean build
java -jar build/libs/Order-Service-0.0.1-SNAPSHOT.jar
# oder über IDE starten
```

**Person B: Product Service**
```bash
cd "Product Service"
./gradlew clean build
java -jar build/libs/Product-Service-0.0.1-SNAPSHOT.jar
# oder über IDE starten
```

---

### Step 2: Logs überprüfen

Beide Services sollten folgende Log-Meldungen zeigen:

```
...
Establishing SSL connection to RabbitMQ
...
Successfully initialized Spring Cloud Stream with RabbitMQ binder
...
Binding org.springframework.cloud.stream.config.OrderEventPublishingChannel
Binding org.springframework.cloud.stream.config.OrderEventListeningChannel
```

---

### Step 3: Endpoints testen

**Swagger UI zugänglich?**
- Person A: `http://localhost:8094/swagger-ui/index.html`
- Person B: `http://localhost:8092/swagger-ui/index.html`

---

## 🔍 Debugging-Tipps

### RabbitMQ Management Console
- URL: `http://localhost:15672`
- Credentials: `guest` / `guest`
- Überprüfen Sie hier:
  - Queues: `order-events`, `product-reservation-events`
  - Messages in den Queues
  - Bindings zwischen Exchanges und Queues

### Logs in der IDE anschauen
```properties
# Ändern Sie das Logging-Level in application.properties
logging.level.org.springframework.cloud.stream=DEBUG
logging.level.org.springframework.amqp=DEBUG
logging.level.at.fhv.orderservice=DEBUG
logging.level.at.fhv.productservice=DEBUG
```

### Message-Fluss verfolgen
1. Event publizieren (z.B. Order erstellen)
2. RabbitMQ Management UI öffnen
3. Queues überprüfen → sollten temporär Messages enthalten
4. Logs überprüfen → sollten "Received" und "Publishing" Messages zeigen

### Häufige Fehler

| Fehler | Ursache | Lösung |
|--------|--------|--------|
| `Connection refused: localhost:5672` | RabbitMQ läuft nicht | RabbitMQ starten (Docker oder lokal) |
| `Queue not found` | Binding nicht korrekt | Spring Boot Logs überprüfen, Service neu starten |
| `Timeout` | Event hängt in Queue | RabbitMQ Management UI überprüfen, Consumer prüfen |
| `Jackson serialization error` | Event-Klasse nicht serialisierbar | Getter/Setter überprüfen, `@JsonProperty` verwenden |

---

## 📋 Gemeinsame Checkliste

### Pre-Implementation
- [ ] RabbitMQ lokal verfügbar (Port 5672)
- [ ] Docker oder lokale RabbitMQ Installation
- [ ] Beiden haben ihre jeweiligen Plans (planA.md, planB.md)
- [ ] Git Repository ist eingerichtet (optional)

### Implementation Phase
- [ ] Person A: Steps 1-4 abgeschlossen
- [ ] Person B: Steps 1-4 abgeschlossen
- [ ] Synchronisierung: Events haben identische Struktur ✅
- [ ] Person A: Steps 5-7 abgeschlossen
- [ ] Person B: Steps 5-7 abgeschlossen
- [ ] Beide Services starten ohne Fehler

### Testing Phase
- [ ] Szenario 1 durchführen (Happy Path)
- [ ] Szenario 2 durchführen (Fehlerfall)
- [ ] Szenario 3 durchführen (Stornierung)
- [ ] Logs überprüfen → keine Fehler
- [ ] RabbitMQ Management UI überprüfen
- [ ] Swagger UI ist erreichbar & dokumentiert ✅

### Documentation Phase
- [ ] README.md aktualisiert
- [ ] Event-Dokumentation vollständig
- [ ] API-Dokumentation in Swagger ✅
- [ ] Setup-Instructions dokumentiert
- [ ] Troubleshooting-Sektion ergänzt

### Final Phase
- [ ] Code-Review durchführen
- [ ] Alle Tests bestanden
- [ ] keine DEBUG Logs mehr im Code
- [ ] Ziparchiiv erstellen & testen
- [ ] Submission vorbereiten

---

## 📅 Zeitplan-Empfehlung

| Phase | Dauer | Wer |
|-------|-------|-----|
| Setup & Dependencies | 1-2h | Beide |
| Event-Klassen & Config | 2-3h | Beide (parallel) |
| Publishing/Listening | 2-3h | Beide (parallel) |
| Service-Integration | 1-2h | Beide (parallel) |
| Testing & Debugging | 2-3h | Beide (sequenziell) |
| Documentation | 1-2h | Beide |
| **Total** | **9-15h** | |

---

## 💡 Best Practices während der Implementierung

✅ **Machen Sie:**
- Commits nach jedem abgeschlossenen Step
- Testen Sie lokal vor dem Merge
- Schreiben Sie Unit Tests
- Dokumentieren Sie Ihre Änderungen
- Kommunizieren Sie bei Blockern
- Verwenden Sie aussagekräftige Lognachrichten

❌ **Vermeiden Sie:**
- Hartcodierte UUIDs oder Port-Nummern
- Fehlende Error-Handling in Event-Listenern
- Synchrone Calls statt async Messaging
- Unvollständige Event-Dokumentation
- Services mit identischen Ports

---

## 🎯 Erfolgs-Kriterien

✅ Beide Services laufen fehlerfrei auf unterschiedlichen Ports
✅ RabbitMQ-Verbindung ist stabil
✅ Events werden korrekt publiziert und empfangen
✅ Order & Product Status werden korrekt aktualisiert
✅ Alle 3 Test-Szenarien bestanden
✅ OpenAPI/Swagger Dokumentation ist vollständig
✅ Logs zeigen erwartete Message-Flows
✅ README.md ist aussagekräftig und vollständig
✅ ZIP-Archive ist lauffähig auf anderen Maschinen

---

**Gutes Gelingen! 🚀**  
Bei Fragen oder Blockern: Gegenseitig unterstützen, Logs überprüfen, RabbitMQ Management UI nutzen!

