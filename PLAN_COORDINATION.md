# Übersicht: Plan A & Plan B - Koordination & Synchronisierung

## Zusammenfassung des Gesamtprojekts

Dieses Projekt implementiert **asynchrone Microservice-Kommunikation** zwischen Order Service und Product Service über **RabbitMQ** und **Spring Cloud Stream**.

**Use Case:** Wenn ein Order gelöscht wird, wird automatisch die Produktreservierung rückgängig gemacht durch ein asynchrones Event.

---

## Zeitliche Abfolge & Koordination

### Woche 1-2: Vorbereitung (Beide zusammen)
- [ ] Einen gemeinsamen Termin festlegen um Fragen zu klären
- [ ] `docker-compose.yml` zusammen erstellen (Person A macht es)
- [ ] RabbitMQ lokal starten und testen
- [ ] Dependencies in beiden Services übereinstimmen

### Woche 2-3: Implementierung (Parallel möglich)

**Person A startet mit:**
1. Dependencies in `Order Service/build.gradle` hinzufügen
2. RabbitMQ Properties in `Order Service/application.properties`
3. Event-Klassen erstellen: `OrderCanceledEvent`, `OrderItemEvent`
4. Producer implementieren: `OrderEventProducer`, `OrderEventProducerImpl`
5. `DeleteOrderServiceImpl` anpassen

**Person B startet mit (parallel):**
1. Dependencies in `Product Service/build.gradle` hinzufügen
2. RabbitMQ Properties in `Product Service/application.properties`
3. Event-Klassen erstellen: `OrderCanceledEvent`, `OrderItemEvent` (identisch wie Person A!)
4. Consumer implementieren: `OrderEventConsumer`, `OrderEventConsumerImpl`
5. `UpdateProductServiceImpl` erweitern mit `restoreStock()` Methode

### Woche 3-4: Testing & Debugging (Zusammen)

**Einzelne Tests:**
- Person A: Testet ob Order löschen das Event publisht
- Person B: Testet ob Consumer das Event empfängt und verarbeitet

**Integrationstests:**
- Beide zusammen: Full-End-to-End Test durchführen
- RabbitMQ Queues überwachen
- Logs analysieren

---

## Kritische Synchronisationspunkte

### 1. Event-Klassen MÜSSEN identisch sein!
**WICHTIG:** Die `OrderCanceledEvent` und `OrderItemEvent` Klassen müssen in beiden Services exakt gleich sein!

```
Order Service:
├─ at.fhv.orderservice.infrastructure.messaging.event.OrderCanceledEvent
├─ at.fhv.orderservice.infrastructure.messaging.event.OrderItemEvent

Product Service:
├─ at.fhv.productservice.infrastructure.messaging.event.OrderCanceledEvent
├─ at.fhv.productservice.infrastructure.messaging.event.OrderItemEvent
```

⚠️ **Die Paketnamen sind verschieden, der Inhalt MUSS aber identisch sein!**

### 2. RabbitMQ Destination Names
Beide Services MÜSSEN den gleichen Destination Namen verwenden:

```properties
# Order Service (Producer)
spring.cloud.stream.bindings.orderCanceledEventProducer-out-0.destination=order-events

# Product Service (Consumer)
spring.cloud.stream.bindings.orderCanceledEventConsumer-in-0.destination=order-events
```

✓ Beide nutzen `order-events` als Destination!

### 3. Event Structure Absprache
Die JSON-Struktur des Events MUSS zwischen beiden Services vereinbart sein:

```json
{
  "orderId": "UUID",
  "userId": "UUID",
  "orderItems": [
    {
      "productId": "UUID",
      "quantity": "int"
    }
  ],
  "timestamp": "long"
}
```

---

## Checkliste für beide Personen

### Person A - Order Service Producer

**Schritt 1: Setup**
- [ ] `Order Service/build.gradle` - Dependencies hinzufügen
- [ ] `Order Service/src/main/resources/application.properties` - RabbitMQ Config
- [ ] Folder `Order Service/src/main/java/at/fhv/orderservice/infrastructure/messaging/` erstellen

**Schritt 2: Event-Klassen**
- [ ] `OrderCanceledEvent.java` erstellen
- [ ] `OrderItemEvent.java` erstellen

**Schritt 3: Producer**
- [ ] `OrderEventProducer.java` Interface erstellen
- [ ] `OrderEventProducerImpl.java` erstellen
- [ ] Tests durchführen: `publishOrderCanceledEvent()` funktioniert?

**Schritt 4: Integration**
- [ ] `DeleteOrderServiceImpl.java` anpassen
- [ ] OrderEventProducer injizieren
- [ ] `publishOrderCanceledEvent()` nach `order.delete()` aufrufen

**Schritt 5: Testing**
- [ ] Order erstellen und löschen
- [ ] RabbitMQ Management UI öffnen
- [ ] Message in Queue überprüfen
- [ ] Event-Struktur validieren

---

### Person B - Product Service Consumer

**Schritt 1: Setup**
- [ ] `Product Service/build.gradle` - Dependencies hinzufügen
- [ ] `Product Service/src/main/resources/application.properties` - RabbitMQ Config
- [ ] Folder `Product Service/src/main/java/at/fhv/productservice/infrastructure/messaging/` erstellen

**Schritt 2: Event-Klassen** (IDENTISCH mit Person A!)
- [ ] `OrderCanceledEvent.java` erstellen
- [ ] `OrderItemEvent.java` erstellen

**Schritt 3: Consumer**
- [ ] `OrderEventConsumer.java` Interface erstellen
- [ ] `OrderEventConsumerImpl.java` mit `@Bean Consumer<>` erstellen
- [ ] Tests durchführen: Consumer empfängt Events?

**Schritt 4: Integration**
- [ ] `UpdateProductService.java` überprüfen/erweitern
- [ ] `restoreStock()` Methode hinzufügen falls nicht vorhanden
- [ ] `UpdateProductServiceImpl.java` - `restoreStock()` implementieren

**Schritt 5: Testing**
- [ ] Produkt erstellen mit Stock=10
- [ ] Order mit diesem Produkt erstellen (Stock reduzieren)
- [ ] Order löschen (Event publishen)
- [ ] Warten auf asynchrone Verarbeitung (5-10 Sekunden)
- [ ] Produkt überprüfen: Stock sollte wieder 10 sein!

---

## Kommunikationsmatrix zwischen Services

```
┌─────────────────────────────────────────────────────────────────┐
│                        RabbitMQ (rabbitmq:5672)                │
│                                                                 │
│  Exchange: order-events                                         │
│  Routing Key: order.*                                           │
│                                                                 │
│  Queue: order-events.product-service-group                      │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
         ▲                                          ▼
         │                                          │
         │                                          │
  Person A (Order Service)              Person B (Product Service)
  Port: 8094                            Port: 8092
  
  Publisher (SendEvent)                 Consumer (ReceiveEvent)
  - DeleteOrderServiceImpl               - OrderEventConsumerImpl
    publishes OrderCanceledEvent          consumes OrderCanceledEvent
    
  Sends to:                             Receives from:
  "orderCanceledEventProducer-out-0"   "orderCanceledEventConsumer-in-0"
```

---

## Gemeinsame Ressourcen

Diese Dateien/Ressourcen sind GEMEINSAM zu koordinieren:

1. **docker-compose.yml** (Person A erstellt, beide nutzen)
   - RabbitMQ Service Definition
   - Start-Befehl: `docker-compose up -d`

2. **Event-Klassen** (Inhalt MUSS identisch sein!)
   - `OrderCanceledEvent.java`
   - `OrderItemEvent.java`
   - Unterschiedliche Package-Namen aber identische Struktur

3. **Spring Cloud Stream Bindings** (Destination Names MÜSSEN matched sein!)
   - `order-events` = Gemeinsamer Queue Name
   - `order.canceled` = Routing Key

---

## Troubleshooting Guide

### Problem: Consumer empfängt Events nicht
**Wahrscheinliche Ursachen:**
- [ ] RabbitMQ läuft nicht? → `docker ps` überprüfen
- [ ] Destination Name unterschiedlich? → Properties überprüfen
- [ ] Consumer Group Name falsch? → `product-service-group` verwenden
- [ ] Spring Cloud Stream nicht aktiviert? → Dependencies überprüfen

**Lösung:**
1. RabbitMQ Logs anschauen: `docker logs rabbitmq-container`
2. RabbitMQ Management UI: `http://localhost:15672`
3. Spring Boot Logs auf DEBUG Level: `logging.level.org.springframework.cloud.stream=DEBUG`

### Problem: Event-Struktur passt nicht
**Symptom:** Consumer wirft Deserialisierungsfehler

**Lösung:**
- Event-Klassen exakt vergleichen (Person A ↔ Person B)
- Feldnamen überprüfen (case-sensitive!)
- Typen überprüfen (UUID, int, long, etc.)

### Problem: Stock wird nicht wiederhergestellt
**Symptom:** Product Service verarbeitet Event aber Stock ändert sich nicht

**Lösung:**
1. Logs überprüfen: "Restoring stock for product..."?
2. `UpdateProductService.restoreStock()` Implementierung überprüfen
3. Database überprüfen: Werden Änderungen persistiert?
4. `@Transactional` Annotation überprüfen

---

## Abhängigkeitsmatrix

```
Order Service Dependencies:
├─ spring-boot-starter-web
├─ spring-boot-starter-data-jpa
├─ spring-cloud-stream ◄─── SHARED
├─ spring-cloud-stream-binder-rabbit ◄─── SHARED
├─ spring-boot-starter-amqp ◄─── SHARED
├─ springdoc-openapi-starter-webmvc-ui
└─ h2database

Product Service Dependencies:
├─ spring-boot-starter-web
├─ spring-boot-starter-data-jpa
├─ spring-cloud-stream ◄─── SHARED (gleiche Version!)
├─ spring-cloud-stream-binder-rabbit ◄─── SHARED (gleiche Version!)
├─ spring-boot-starter-amqp ◄─── SHARED (gleiche Version!)
├─ springdoc-openapi-starter-webmvc-ui
└─ h2database

⚠️ WICHTIG: Alle SHARED Dependencies MÜSSEN die gleiche Version haben!
```

---

## Parallel Development Tipps

✓ **Person A kann starten ohne auf Person B zu warten:**
- Events publizieren und in RabbitMQ überprüfen
- Manuell mit RabbitMQ Management UI testen

✓ **Person B kann starten ohne auf Person A zu warten:**
- Event-Klassen lokal erstellen
- Consumer Bean vorbereiten
- Mit Mock-Events testen

✓ **Erst zusammen testen wenn:**
- Beide grundlegende Implementierung fertig
- RabbitMQ läuft
- Dependencies stimmen überein
- Event-Struktur definiert ist

---

## Finale Checkliste vor Submission

**Code Quality:**
- [ ] Keine Lombok Annotations (manuell Getter/Setter)
- [ ] Keine @Input/@Output (StreamBridge & Function<> Consumer)
- [ ] Keine Fehler im IDE
- [ ] Code formatiert und sauber

**Tests:**
- [ ] Order erstellen → löschen → Event published
- [ ] Event in RabbitMQ sichtbar
- [ ] Product Service empfängt Event
- [ ] Stock wird wiederhergestellt
- [ ] Logs zeigen korrekten Workflow

**Documentation:**
- [ ] README.md aktualisiert
- [ ] OpenAPI/Swagger dokumentiert
- [ ] RabbitMQ Setup dokumentiert
- [ ] Event-Struktur dokumentiert

**Deployment:**
- [ ] Docker-compose.yml funktioniert
- [ ] Beide Services starten problemlos
- [ ] RabbitMQ verbindet sich
- [ ] Full-End-to-End Test erfolgreich

---

## Terminalkommandos (für beide)

```bash
# RabbitMQ starten
docker-compose up -d

# RabbitMQ Management UI
# Browser: http://localhost:15672
# Benutzername: guest
# Passwort: guest

# Order Service starten
cd "Order Service"
./gradlew bootRun

# Product Service starten (in neuem Terminal)
cd "Product Service"
./gradlew bootRun

# Logs anschauen
# Terminal 1: Order Service - sollte "Received OrderCanceledEvent" zeigen bei DELETE
# Terminal 2: Product Service - sollte "Restoring stock" zeigen

# RabbitMQ Status
docker ps  # RabbitMQ sollte laufen
docker logs rabbitmq-container  # Logs anschauen

# Alles stoppen
docker-compose down
```

---

## Viel Erfolg bei der Implementierung! 🚀

Falls Fragen oder Probleme auftreten:
1. Zuerst die entsprechende planA.md oder planB.md konsultieren
2. RabbitMQ Management UI überprüfen
3. Spring Boot Logs analysieren
4. Debug-Mode aktivieren: `logging.level.org.springframework.cloud.stream=DEBUG`
5. Mit dem Teampartner absprechen

**Deadline Reminder:** Sicherstellen, dass die .zip Archive alle notwendigen Dateien enthalten!


