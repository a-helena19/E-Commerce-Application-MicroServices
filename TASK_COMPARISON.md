# 🔄 Vergleich: Person A vs Person B
## Detaillierte Aufgabenverteilung

---

## 📋 Aufgaben-Matrix

| Aufgabe | Person A (Cart Service) | Person B (Product Service) | Zusammenarbeit |
|---------|----------------------|------------------------|-----------------|
| **Setup** | | | |
| Dependencies hinzufügen | ✅ Schritt 1 | ✅ Schritt 1 | 🔄 Parallel |
| application.properties | ✅ Schritt 2 | ✅ Schritt 2 | 🔄 Parallel |
| | | | |
| **Event-Klassen** | | | |
| CartCheckoutEvent.java | ✅ Schritt 3 | ✅ Schritt 3 | ⚠️ **IDENTISCH!** |
| ProductReservationConfirmedEvent.java | ✅ Schritt 3 | ✅ Schritt 3 | ⚠️ **IDENTISCH!** |
| ProductReservationFailedEvent.java | ✅ Schritt 3 | ✅ Schritt 3 | ⚠️ **IDENTISCH!** |
| | | | |
| **Entity-Erweiterung** | | | |
| Cart Entity anpassen | ✅ (vorhanden) | - | nur Person A |
| Product Entity anpassen | - | ✅ Schritt 4 | nur Person B |
| | | | |
| **Messaging Config** | | | |
| CartMessagingConfig.java | ✅ Schritt 4 | - | nur Person A |
| ProductMessagingConfig.java | - | ✅ Schritt 5 | nur Person B |
| | | | |
| **Event Publishing** | | | |
| CartEventPublisher | ✅ Schritt 5 | - | nur Person A |
| ProductReservationEventPublisher | - | ✅ Schritt 6 | nur Person B |
| | | | |
| **Event Listening** | | | |
| ProductReservationEventListener | ✅ Schritt 6 | - | nur Person A |
| CartEventListener | - | ✅ Schritt 7 | nur Person B |
| | | | |
| **Service Integration** | | | |
| CartService anpassen | ✅ Schritt 7 | - | nur Person A |
| ProductService anpassen | - | ✅ (optional) | nur Person B |
| | | | |
| **OpenAPI & Testen** | | | |
| OpenAPI Annotations | ✅ Schritt 8 | ✅ Schritt 8 | 🔄 Parallel |
| Unit Tests | ✅ Schritt 9 | ✅ Schritt 9 | 🔄 Parallel |
| | | | |
| **Integration Testing** | | | |
| Szenario 1: Happy Path | 🔄 Zusammen | 🔄 Zusammen | ⚠️ **MUSS KLAPPT!** |
| Szenario 2: Error Case | 🔄 Zusammen | 🔄 Zusammen | ⚠️ **MUSS KLAPPT!** |

---

## 📁 Datei-Ownership

### Person A – Cart Service

```
Cart Service/src/main/java/at/fhv/cartservice/
│
├── 🔴 config/
│   └── CartMessagingConfig.java            [NEU - Person A]
│
├── 🟡 controller/
│   └── CartController.java                  [ÄNDERN - OpenAPI]
│
├── 🟢 events/                              [NEU - Person A & B]
│   ├── CartCheckoutEvent.java              [NEU - IDENTISCH mit B!]
│   ├── ProductReservationConfirmedEvent.java [NEU - IDENTISCH mit B!]
│   └── ProductReservationFailedEvent.java  [NEU - IDENTISCH mit B!]
│
├── 🟣 messaging/                           [NEU - Person A]
│   ├── CartEventPublisher.java             [NEU]
│   └── ProductReservationEventListener.java [NEU]
│
├── 🟠 service/
│   └── CartService.java                    [ÄNDERN - Event Publishing]
│
└── 📄 application.properties                [ÄNDERN - RabbitMQ Config]
```

---

### Person B – Product Service

```
Product Service/src/main/java/at/fhv/productservice/
│
├── 🔴 config/
│   └── ProductMessagingConfig.java         [NEU - Person B]
│
├── 🟡 controller/
│   └── ProductController.java              [ÄNDERN - OpenAPI]
│
├── 🟢 events/                              [NEU - Person A & B]
│   ├── CartCheckoutEvent.java              [NEU - IDENTISCH mit A!]
│   ├── ProductReservationConfirmedEvent.java [NEU - IDENTISCH mit A!]
│   └── ProductReservationFailedEvent.java  [NEU - IDENTISCH mit A!]
│
├── 🟣 messaging/                           [NEU - Person B]
│   ├── CartEventListener.java              [NEU]
│   └── ProductReservationEventPublisher.java [NEU]
│
├── 🟠 model/
│   └── Product.java                        [ÄNDERN - reservedQuantity]
│
├── 🟠 service/
│   └── ProductService.java                 [OPTIONAL]
│
└── 📄 application.properties                [ÄNDERN - RabbitMQ Config]
```

---

## 🔗 Dependencies zwischen den Aufgaben

### Sequenziell (nacheinander):
```
Person A & B
├── Schritt 1-2: Setup (🔄 Parallel)
│   ├── A: Schritt 1: Dependencies
│   └── B: Schritt 1: Dependencies
│   
├── Schritt 3-4: Events & Config (🔄 Parallel)
│   ├── A: Schritt 3-4: Events + Config
│   └── B: Schritt 3-4: Events + Config + Entity
│   
├── Schritt 5-7: Pub/Sub (🔄 Parallel)
│   ├── A: Publisher + Listener
│   └── B: Listener + Publisher
│   
├── Schritt 8-9: API & Tests (🔄 Parallel)
│   ├── A: OpenAPI + Unit Tests
│   └── B: OpenAPI + Unit Tests
│   
└── 🧪 Integration Testing (⚠️ SEQUENZIELL!)
    └── Beide zusammen: Szenario 1, 2, 3
```

### Kritische Abhängigkeiten (MUSS IDENTISCH sein):
```
⚠️ Event-Struktur:
   Person A definiert OrderCreatedEvent
   → Person B MUSS identische Fields haben!
   → Kopieren Sie direkt von QUICK_REFERENCE.md

⚠️ RabbitMQ Queues:
   Person A: spring.cloud.stream.bindings.orderCreated-out.destination=order-events
   → Person B: spring.cloud.stream.bindings.orderCreated-in.destination=order-events
   → MUSS identisch sein!

⚠️ Consumer Group:
   Person B: spring.cloud.stream.bindings.orderCreated-in.group=product-service-group
   → Consumer Group MUSS für Parallelverarbeitung gesetzt sein!
```

---

## ⏱️ Zeitplan-Übersicht

### Optimistisches Szenario (Parallel maximal)
```
Tag 1 (4h):
├── 30min: Beide lesen QUICK_REFERENCE.md + PROJECT_OVERVIEW.md
├── 30min: RabbitMQ Setup
└── 3h: Schritt 1-4 (🔄 Parallel)

Tag 2 (5h):
├── 3h: Schritt 5-7 (🔄 Parallel)
└── 2h: Schritt 8-9 (🔄 Parallel)

Tag 3 (3h):
├── 2h: Integration Testing (⚠️ Sequenziell!)
└── 1h: Dokumentation & Finalisierung
```

### Realistisches Szenario
```
Tag 1 (6h):
├── 1h: Setup & Planung
├── 2h: Dependencies + Config + Events
└── 3h: Messaging Config + Publishing

Tag 2 (6h):
├── 2h: Listening & Fehlerbehandlung
├── 2h: Service Integration & Tests
└── 2h: OpenAPI & Documentation

Tag 3 (4h):
├── 3h: Integration Testing
└── 1h: Finalisierung & ZIP

Total: 16 Stunden (vs. 9 Stunden optimal)
```

---

## 🔄 Synchronisierungs-Punkte

| Punkt | Vor Arbeit | Während Arbeit | Nach Arbeit |
|-------|-----------|----------------|------------|
| **Event-Klassen** | ⚠️ Abstimmung | 🔒 Identisch halten | ✅ Verifizieren |
| **RabbitMQ Config** | 🔄 Gleich definieren | 🔒 Nicht ändern | ✅ Testen |
| **OpenAPI Docs** | 📋 Plan | 🔄 Parallel | ✅ Review |
| **Integration Tests** | 📋 Szenarien kennen | 🔄 Zusammen | ✅ Alle bestanden |
| **Finale Doku** | 📋 Struktur | 🔄 Contributions | ✅ Review |

---

## 🎯 Checkpoint Matrix

### Checkpoint 1: Setup abgeschlossen
```
Person A:
├── [ ] Dependencies hinzugefügt
├── [ ] build.gradle compiliert
├── [ ] application.properties aktualisiert
└── [ ] Service startet ohne Fehler

Person B:
├── [ ] Dependencies hinzugefügt
├── [ ] build.gradle compiliert
├── [ ] application.properties aktualisiert
└── [ ] Service startet ohne Fehler

Zusammen:
└── [ ] RabbitMQ läuft (Port 5672, 15672)
```

### Checkpoint 2: Events definiert
```
Person A:
├── [ ] Alle 4 Event-Klassen erstellt
├── [ ] OrderMessagingConfig erstellt
├── [ ] Classes compilieren
└── [ ] Classes sind serialisierbar (POJO)

Person B:
├── [ ] Alle 4 Event-Klassen erstellt (IDENTISCH!)
├── [ ] ProductMessagingConfig erstellt
├── [ ] Classes compilieren
└── [ ] Classes sind serialisierbar (POJO)

Zusammen:
└── [ ] Event-Klassen verifizieren (MUST BE IDENTICAL)
```

### Checkpoint 3: Publisher & Listener arbeiten
```
Person A:
├── [ ] OrderEventPublisher erstellt
├── [ ] ProductReservationEventListener erstellt
├── [ ] Services sind @Component annotiert
├── [ ] @EnableBinding ist gesetzt
└── [ ] Unit Tests für Publisher bestehen

Person B:
├── [ ] OrderEventListener erstellt
├── [ ] ProductReservationEventPublisher erstellt
├── [ ] Services sind @Component annotiert
├── [ ] @EnableBinding ist gesetzt
└── [ ] Unit Tests für Listener bestehen

Zusammen:
└── [ ] Manueller Test: Order erstellen → Event fließt
```

### Checkpoint 4: Integriert in Services
```
Person A:
├── [ ] OrderService.createOrder() publiziert Event
├── [ ] OrderService.cancelOrder() publiziert Event
├── [ ] Listener aktualisiert Order Status
└── [ ] Unit Tests für Integration bestehen

Person B:
├── [ ] OrderEventListener verarbeitet Bestellungen
├── [ ] Product.reservedQuantity wird korrekt aktualisiert
├── [ ] Response-Events werden publiziert
└── [ ] Unit Tests für Integration bestehen
```

### Checkpoint 5: APIs dokumentiert
```
Person A:
├── [ ] @Operation auf allen Endpoints
├── [ ] @ApiResponse auf allen Endpoints
├── [ ] @Schema auf allen DTOs
└── [ ] Swagger UI ist erreichbar & vollständig

Person B:
├── [ ] @Operation auf allen Endpoints
├── [ ] @ApiResponse auf allen Endpoints
├── [ ] @Schema auf allen DTOs
└── [ ] Swagger UI ist erreichbar & vollständig
```

### Checkpoint 6: Integration Tests bestanden
```
Zusammen:
├── [ ] Szenario 1: Happy Path ✅
├── [ ] Szenario 2: Error Case ✅
├── [ ] Szenario 3: Cancellation ✅
├── [ ] Logs zeigen alle Events ✅
└── [ ] RabbitMQ Queues sind leer nach Tests ✅
```

---

## 🤝 Communication Protocol

### Täglich:
- 📝 Fortschritt-Updates (welcher Schritt abgeschlossen)
- 🐛 Block-Meldungen (sofort kommunizieren!)
- 🔄 Event-Klassen-Sync (nachdem Schritt 3 beide abgeschlossen)

### Bei Blockern:
1. **Lokal debuggen:** 15 Minuten
2. **Andere Person fragen:** sofort
3. **QUICK_REFERENCE.md** prüfen
4. **RabbitMQ Management UI** überprüfen
5. **Gemeinsam debuggen**

### Kritische Sync-Points:
```
MUSS abgestimmt sein BEVOR weitermachen:
├── Event-Klasse Fields (IDENTISCH)
├── RabbitMQ Queue Names (IDENTISCH)
├── Consumer Group Names (Eindeutig)
├── application.properties (Korrekt)
└── Messaging Config Channels (Korrekt gemappt)
```

---

## 📊 Erfolgsmetriken

### Person A (Order Service)
- ✅ Publiziert OrderCreatedEvent nach create()
- ✅ Publiziert OrderCanceledEvent nach cancel()
- ✅ Empfängt und verarbeitet ProductReservationUpdatedEvent
- ✅ Empfängt und verarbeitet ProductReservationFailedEvent
- ✅ Order Status wird korrekt aktualisiert
- ✅ OpenAPI Dokumentation vollständig
- ✅ Unit Tests für Publisher & Listener

### Person B (Product Service)
- ✅ Empfängt und verarbeitet OrderCreatedEvent
- ✅ Empfängt und verarbeitet OrderCanceledEvent
- ✅ Reserviert Produkte korrekt
- ✅ Publiziert ProductReservationUpdatedEvent bei Success
- ✅ Publiziert ProductReservationFailedEvent bei Error
- ✅ OpenAPI Dokumentation vollständig
- ✅ Unit Tests für Listener & Publisher

### Zusammen
- ✅ End-to-End Flow funktioniert
- ✅ Alle 3 Szenarien bestanden
- ✅ Keine Fehler in Logs
- ✅ RabbitMQ Messages fließen korrekt
- ✅ README.md dokumentiert alles
- ✅ ZIP-Archive ist lauffähig

---

**Viel Erfolg bei der Umsetzung! 🚀**

Person A: Starten Sie mit `planA.md`!  
Person B: Starten Sie mit `planB.md`!

