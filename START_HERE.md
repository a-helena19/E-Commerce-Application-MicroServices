# 🚀 START HIER – Ihr Leitfaden

## Herzlich Willkommen zum Asynchrone Kommunikation Projekt!

Sie haben **5 neue Dokumentationen** erhalten. Diese Seite hilft Ihnen zu entscheiden, wo Sie anfangen.

---

## 👤 Wenn Sie **Person A** sind (Cart Service)

### 🎯 Ihr Aufgaben-Ordner:
```
📄 planA.md  ← ÖFFNEN SIE DIES ZUERST!
```

### 📋 Ihre Aufgaben:
- Publizieren Sie: `CartCheckoutEvent` (wenn Kunde Checkout macht)
- Empfangen Sie: `ProductReservationConfirmedEvent`, `ProductReservationFailedEvent`
- Aktualisieren Sie: Cart Status basierend auf Produktreservierungen
- **Port:** 8091
- **Aufwand:** 8-10 Stunden

### 🚀 Schritt-für-Schritt:
```
1. Öffnen Sie planA.md
2. Folgen Sie Schritt 1-9 nacheinander
3. Parallel: Person B arbeitet an planB.md
4. Nach Schritt 7: Integration Tests mit Person B
```

### ⏱️ Geschätzter Zeitplan:
- Schritt 1-2: 1 Stunde
- Schritt 3-4: 1,5 Stunden
- Schritt 5-7: 3 Stunden
- Schritt 8-9: 1,5 Stunden
- Integration Tests: 1 Stunde (mit Person B)
- **Total: 8-10 Stunden**

### 🤝 Zusammenarbeit:
- Besprechung mit Person B: Nach Schritt 4 (Events synchronisieren!)
- Integration Tests: Nach Schritt 7 (beide Seiten müssen arbeiten)
- Finale Dokumentation: Nach Integration Tests

---

## 👤 Wenn Sie **Person B** sind (Product Service)

### 🎯 Ihr Aufgaben-Ordner:
```
📄 planB.md  ← ÖFFNEN SIE DIES ZUERST!
```

### 📋 Ihre Aufgaben:
- Empfangen Sie: `CartCheckoutEvent` (vom Cart Service)
- Publizieren Sie: `ProductReservationConfirmedEvent`, `ProductReservationFailedEvent`
- Reservieren/Freigeben Sie: Produktmengen basierend auf Cart-Checkouts
- **Port:** 8092
- **Aufwand:** 8-10 Stunden

### 🚀 Schritt-für-Schritt:
```
1. Öffnen Sie planB.md
2. Folgen Sie Schritt 1-9 nacheinander
3. Parallel: Person A arbeitet an planA.md
4. Nach Schritt 7: Integration Tests mit Person A
```

### ⏱️ Geschätzter Zeitplan:
- Schritt 1-2: 1 Stunde
- Schritt 3-4: 2 Stunden (Entity-Erweiterung!)
- Schritt 5-7: 3 Stunden
- Schritt 8-9: 1,5 Stunden
- Integration Tests: 1 Stunde (mit Person A)
- **Total: 8-10 Stunden**

### 🤝 Zusammenarbeit:
- Besprechung mit Person A: Nach Schritt 4 (Events synchronisieren!)
- Integration Tests: Nach Schritt 7 (beide Seiten müssen arbeiten)
- Finale Dokumentation: Nach Integration Tests

---

## 📚 Zusätzliche Dokumentationen (als Referenz)

| Dokument | Zweck | Wann nutzen |
|----------|-------|-----------|
| **QUICK_REFERENCE.md** | Schnelle Code-Snippets & Debugging | Jederzeit, wenn Sie schnell was nachschlagen |
| **PROJECT_OVERVIEW.md** | Großbild, Event-Flows, Test-Szenarien | Nach Schritt 4, vor Integration Tests |
| **DOCUMENTATION_INDEX.md** | Alle Dokumente im Überblick | Wenn Sie sich verlaufen |
| **TASK_COMPARISON.md** | Vergleich A vs B, Sync-Punkte | Für gegenseitige Abstimmung |
| **plan.md** | Original detaillierter Plan (optional) | Nur falls Sie Hintergrund brauchen |

---

## ⚡ Schnellstart (5 Minuten)

### Schritt 1: Lesen Sie QUICK_REFERENCE.md
```
⏱️ Zeit: 5 Minuten
📝 Was Sie lernen: 
   - RabbitMQ aufstarten
   - Dependencies hinzufügen
   - Erste Event-Klasse erstellen
```

### Schritt 2: Schauen Sie PROJECT_OVERVIEW.md
```
⏱️ Zeit: 10 Minuten
📝 Was Sie lernen:
   - Event-Fluss zwischen Services
   - Wie RabbitMQ funktioniert
   - Test-Szenarien
```

### Schritt 3: Öffnen Sie Ihren Plan
```
Person A: planA.md
Person B: planB.md

⏱️ Zeit: Folgen Sie den 9 Schritten
📝 Was Sie tun:
   - Implementieren Sie Ihren Service
   - Schreiben Sie Unit Tests
   - Dokumentieren Sie APIs
```

### Schritt 4: Integrations-Tests
```
⏱️ Zeit: 1 Stunde (mit der anderen Person)
📝 Was Sie tun:
   - Starten Sie RabbitMQ
   - Starten Sie beide Services
   - Testen Sie 3 Szenarien zusammen
```

---

## 🔑 Wichtigste Punkte zum Merken

### ⚠️ KRITISCH – Muss identisch sein:
```
□ Event-Klassen: CartCheckoutEvent, 
                 ProductReservationConfirmedEvent, 
                 ProductReservationFailedEvent
□ RabbitMQ Queue Names: cart-checkout-events, product-reservation-events
□ Event Fields: UUID cartId, UUID productId, int quantity, etc.
```

### ✅ Empfohlen – Folgt euch gegenseitig:
```
□ RabbitMQ Konfiguration (spring.rabbitmq.*)
□ Spring Cloud Stream Binding Naming
□ Consumer Group Names
□ Port-Nummern (Person A: 8091, Person B: 8092)
```

### 🔄 Parallel möglich – Unabhängig voneinander:
```
□ Schritt 1-3: Setup & Events
□ Schritt 5-7: Publishing/Listening
□ Schritt 8-9: OpenAPI & Unit Tests
```

### 🚫 Nicht parallel – Muss sequenziell:
```
□ Schritt 4: Messaging Config (basiert auf Events)
□ Integration Tests: Szenario 1-3 (braucht beide Services)
```

---

## 🆘 Häufige Anfangs-Fehler

| Fehler | Symptom | Fix |
|--------|---------|-----|
| RabbitMQ läuft nicht | `Connection refused: localhost:5672` | `docker run ... rabbitmq:3.13-management` |
| Event-Klassen unterschiedlich | Spring Serialisierung fehlt | Events EXAKT kopieren (siehe QUICK_REFERENCE.md) |
| Falsche Queue Names | Messages kommen nicht an | `application.properties` überprüfen (siehe PROJECT_OVERVIEW.md) |
| @EnableBinding vergessen | Listener wird nicht aufgerufen | Überprüfen Sie die Class-Annotation |
| Consumer Group fehlt | Duplikate/Fehler | `spring.cloud.stream.bindings.xxx.group=` hinzufügen |

---

## 📋 Pre-Implementierungs-Checklist

Bevor Sie anfangen:

- [ ] **RabbitMQ vorbereitet**
  ```bash
  docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 \
    -e RABBITMQ_DEFAULT_USER=guest \
    -e RABBITMQ_DEFAULT_PASS=guest \
    rabbitmq:3.13-management
  ```
  Überprüfen: `http://localhost:15672` (guest/guest)

- [ ] **QUICK_REFERENCE.md gelesen** (5 min)

- [ ] **PROJECT_OVERVIEW.md überflogen** (10 min)

- [ ] **Mit der anderen Person abgestimmt:**
  - Wer macht Person A?
  - Wer macht Person B?
  - Wann treffen wir uns für Integration Tests?

- [ ] **Ihr Plan geöffnet:**
  - Person A: planA.md
  - Person B: planB.md

---

## 🎯 Die 3 wichtigsten Erfolgsfaktoren

### 1️⃣ Event-Klassen müssen identisch sein!
```
Wenn Person A das schreibt:
    public UUID getOrderId() { return orderId; }

Muss Person B das EXAKT gleich haben!
```

### 2️⃣ RabbitMQ muss laufen
```
Fehler beim Starten?
→ Docker/RabbitMQ überprüfen
→ Logs ansehen: docker logs rabbitmq
→ Port 5672 sollte erreichbar sein
```

### 3️⃣ Kommunikation ist key
```
Wenn Sie bei Schritt 3 sind, Person B auch?
→ Abstimmen!

Wenn Sie bei Schritt 7 sind, Person B auch?
→ Integration Tests planen!
```

---

## 🗺️ Navigation durch die Dokumente

```
Sie sind hier? → Was Sie tun sollten:

📍 Gerade gestartet
   → QUICK_REFERENCE.md (5 min) 
   → Ihr persönlicher Plan (planA.md oder planB.md)

📍 Bei Schritt 1-4
   → Folgen Sie Ihrem Plan
   → QUICK_REFERENCE.md bei Fragen

📍 Bei Schritt 5-7
   → Folgen Sie Ihrem Plan
   → PROJECT_OVERVIEW.md für Event-Fluss-Verständnis

📍 Bei Schritt 8-9
   → Folgen Sie Ihrem Plan
   → QUICK_REFERENCE.md für Code-Beispiele

📍 Bei Integration Tests
   → PROJECT_OVERVIEW.md (Szenarien 1-3)
   → QUICK_REFERENCE.md (Debugging)
   → TASK_COMPARISON.md (Sync-Punkte)

📍 Bei Blockern
   → QUICK_REFERENCE.md (🆘 Häufige Fehler & Fixes)
   → RabbitMQ Management UI überprüfen
   → Andere Person fragen
```

---

## ⏱️ Zeitplan-Beispiel

### Tag 1 (6 Stunden)
```
10:00 - 10:05:  Alle: RabbitMQ starten & testen
10:05 - 10:15:  Alle: QUICK_REFERENCE.md lesen
10:15 - 10:30:  Alle: PROJECT_OVERVIEW.md überblick
10:30 - 12:00:  Person A: Schritt 1-2 (parallel mit B)
                Person B: Schritt 1-2 (parallel mit A)
12:00 - 13:00:  Mittagspause
13:00 - 14:30:  Person A: Schritt 3-4 (parallel mit B)
                Person B: Schritt 3-4 (parallel mit A)
14:30 - 15:30:  Besprechung A+B: Events synchronisieren!
15:30 - 16:00:  Buffer für Fehlerbehandlung
```

### Tag 2 (6 Stunden)
```
10:00 - 12:30:  Person A: Schritt 5-7 (parallel mit B)
                Person B: Schritt 5-7 (parallel mit A)
12:30 - 13:30:  Mittagspause
13:30 - 15:30:  Person A: Schritt 8-9 (parallel mit B)
                Person B: Schritt 8-9 (parallel mit A)
15:30 - 16:00:  Besprechung A+B: Bereitschaft für Integration Tests
```

### Tag 3 (4 Stunden)
```
10:00 - 12:00:  A+B zusammen: Integration Testing
                - Szenario 1: Happy Path
                - Szenario 2: Error Case
                - Szenario 3: Cancellation
12:00 - 13:00:  Mittagspause
13:00 - 14:00:  Dokumentation & Finalisierung
14:00 - 14:30:  Finale Tests & Fehlerbehandlung
14:30 - 15:00:  ZIP-Archive vorbereiten & Submission
```

---

## 📞 Kontakt & Support während Arbeit

**Bei technischen Fragen:**
1. Schauen Sie in Ihrem Plan nach
2. Nutzen Sie QUICK_REFERENCE.md
3. Fragen Sie die andere Person
4. Schauen Sie in RabbitMQ Management UI

**Bei Blockern:**
1. 5 Minuten selbst debuggen
2. Logs überprüfen
3. Andere Person fragen
4. Gemeinsam debuggen

**Bei Prozess-Fragen:**
1. Schauen Sie in TASK_COMPARISON.md
2. Schauen Sie in PROJECT_OVERVIEW.md
3. Fragen Sie die andere Person

---

## ✨ Spezial-Tipps für Erfolg

### 💡 Tipp 1: Nutzen Sie Git
```bash
# Nach jedem Schritt commiten
git add .
git commit -m "Person A: Schritt 7 abgeschlossen"
```

### 💡 Tipp 2: Loggen Sie großzügig
```properties
# In application.properties
logging.level.org.springframework.cloud.stream=DEBUG
logging.level.at.fhv.orderservice=DEBUG
logging.level.at.fhv.productservice=DEBUG
```

### 💡 Tipp 3: Nutzen Sie RabbitMQ Management
```
http://localhost:15672
Credentials: guest / guest
- Überprüfen Sie Queues
- Sehen Sie Messages live
- Debuggen Sie mit Leichtigkeit!
```

### 💡 Tipp 4: Testen Sie lokal
```bash
# Bevor Sie zur nächsten Person gehen
./gradlew test

# Vor Integration Tests
./gradlew clean build
```

### 💡 Tipp 5: Dokumentieren Sie unterwegs
```
# Nicht nur Code schreiben, auch dokumentieren!
- JavaDoc hinzufügen
- Logs aussagekräftig machen
- README.md aktualisieren
```

---

## 🎓 Learning Goals

Nach Completion sollten Sie verstehen:

✅ Spring Cloud Stream Grundlagen  
✅ RabbitMQ Message-Broker Konzept  
✅ Event-Driven Architecture  
✅ Saga Pattern (Choreography)  
✅ Asynchrone Kommunikation zwischen Microservices  
✅ OpenAPI/Swagger Dokumentation  
✅ Unit & Integration Testing  
✅ Fehlerbehandlung in ereignisgesteuerten Systemen  

---

## 🏁 Sie sind bereit!

### Person A:
```
👉 ÖFFNEN: planA.md
👉 STARTEN: Schritt 1
👉 ZEIT: 8-10 Stunden
👉 ERGEBNIS: Funktionierender Cart Service mit Event Publishing
```

### Person B:
```
👉 ÖFFNEN: planB.md
👉 STARTEN: Schritt 1
👉 ZEIT: 8-10 Stunden
👉 ERGEBNIS: Funktionierender Product Service mit Event Listening
```

### Dann zusammen:
```
👉 ÖFFNEN: PROJECT_OVERVIEW.md (Szenarien)
👉 TESTEN: Integration Tests
👉 DOKUMENTIEREN: README.md & finalisieren
👉 EINREICHEN: ZIP-Archive mit vollständiger Lösung
```

---

## 🚀 Viel Erfolg!

Sie haben alles was Sie brauchen:
- ✅ Detaillierte Schritt-für-Schritt Anleitungen
- ✅ Code-Beispiele und Snippets
- ✅ Debugging-Tipps und FAQs
- ✅ Test-Szenarien
- ✅ Best Practices

**Beginnen Sie jetzt! 🚀**

Bei Fragen: Schauen Sie auf das entsprechende Dokument oben → oder fragen Sie die andere Person!

---

**Ihr Projektteam**
- Person A (Cart Service) & Person B (Product Service)
- Zusammenarbeit mit RabbitMQ & Spring Cloud Stream
- Deadline: 23.03.2026

