# 📊 Visuelle Übersicht der Umstrukturierung

## 🔄 Von Order-centric zu Cart-centric

```
┌─────────────────────────────────────────────────────────────────────┐
│                           ALT (Order)                                │
├─────────────────────────────────────────────────────────────────────┤
│                                                                       │
│  Order Service (Port 8094)          Product Service (Port 8092)     │
│  ┌──────────────────────────┐       ┌──────────────────────────┐   │
│  │ Events Publiziert:       │       │ Events Empfangen:        │   │
│  │ • OrderCreatedEvent      │       │ • OrderCreatedEvent      │   │
│  │ • OrderCanceledEvent     │       │ • OrderCanceledEvent     │   │
│  │                          │       │                          │   │
│  │ Events Empfangen:        │       │ Events Publiziert:       │   │
│  │ • ProductReservationUpd… │       │ • ProductReservationUpd… │   │
│  │ • ProductReservationFai… │       │ • ProductReservationFai… │   │
│  └────────┬─────────────────┘       └────────┬─────────────────┘   │
│           │                                   │                      │
│           └──────→ order-events ←─────────────┘                      │
│                    product-reservation-events                        │
│                                                                       │
└─────────────────────────────────────────────────────────────────────┘


┌─────────────────────────────────────────────────────────────────────┐
│                           NEU (Cart)                                  │
├─────────────────────────────────────────────────────────────────────┤
│                                                                       │
│  Cart Service (Port 8091)           Product Service (Port 8092)     │
│  ┌──────────────────────────┐       ┌──────────────────────────┐   │
│  │ Events Publiziert:       │       │ Events Empfangen:        │   │
│  │ • CartCheckoutEvent      │       │ • CartCheckoutEvent      │   │
│  │                          │       │                          │   │
│  │ Events Empfangen:        │       │ Events Publiziert:       │   │
│  │ • ProductReservationCo…  │       │ • ProductReservationCo…  │   │
│  │ • ProductReservationFai… │       │ • ProductReservationFai… │   │
│  └────────┬─────────────────┘       └────────┬─────────────────┘   │
│           │                                   │                      │
│           └─→ cart-checkout-events ←──────────┘                      │
│              product-reservation-events                              │
│                                                                       │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 📈 Event-Anzahl Reduzierung

```
Alt (Order-centric):              Neu (Cart-centric):
├─ OrderCreatedEvent          └─ CartCheckoutEvent
├─ OrderCanceledEvent              (gestrichen!)
├─ ProductReservationUpdated... ├─ ProductReservationConfirmed...
└─ ProductReservationFailed...  └─ ProductReservationFailed...

TOTAL: 4 Events                TOTAL: 3 Events (-25%)
```

---

## 🔌 Queue-Name Änderungen

```
ALT                          NEU
────────────────────────────────────
order-events             → cart-checkout-events
product-reservation-events (BLEIBT GLEICH)

Consumer Groups:
ALT                      NEU
────────────────────────────────────
order-service-group      → cart-service-group
product-service-group       (BLEIBT GLEICH)
```

---

## 🔄 Saga Flow Vergleich

### Alt: Order-Saga (3 Szenarien)
```
1. ORDER CREATION
   OrderCreatedEvent → Reserve → [SUCCESS/FAILED]
   
2. ORDER CONFIRMATION
   ProductReservationUpdated → Order.status = CONFIRMED
   
3. ORDER CANCELLATION
   OrderCanceledEvent → Release Reservation
```

### Neu: Cart-Saga (2 Szenarien)
```
1. CART CHECKOUT
   CartCheckoutEvent → Reserve → [SUCCESS/FAILED]
   
2. [Result Only]
   ProductReservationConfirmed → Cart.status = CHECKED_OUT
   ProductReservationFailed → Cart.status = CHECKOUT_FAILED
   
   (Kein Cancellation-Szenario!)
```

---

## 📊 Dateistruktur Vergleich

### Person A (Alt: Order Service)
```
OrderService/
├── config/
│   └── OrderMessagingConfig.java
├── controller/
│   └── OrderController.java
├── events/
│   ├── OrderCreatedEvent.java
│   ├── OrderCanceledEvent.java          ❌ ENTFERNT
│   ├── ProductReservationUpdatedEvent.java
│   └── ProductReservationFailedEvent.java
├── messaging/
│   ├── OrderEventPublisher.java
│   └── ProductReservationEventListener.java
└── service/
    └── OrderService.java
```

### Person A (Neu: Cart Service)
```
CartService/
├── config/
│   └── CartMessagingConfig.java
├── controller/
│   └── CartController.java
├── events/
│   ├── CartCheckoutEvent.java
│   ├── ProductReservationConfirmedEvent.java
│   └── ProductReservationFailedEvent.java
├── messaging/
│   ├── CartEventPublisher.java
│   └── ProductReservationEventListener.java
└── service/
    └── CartService.java
```

---

## 🎯 Was bleibt gleich (Person B)

```
ProductService/
├── config/
│   └── ProductMessagingConfig.java       ✅ Config-Namen gleich
├── controller/
│   └── ProductController.java
├── events/
│   ├── CartCheckoutEvent.java            ⚠️  (alt: OrderCreatedEvent)
│   ├── ProductReservationConfirmedEvent.java
│   └── ProductReservationFailedEvent.java
├── messaging/
│   ├── CartEventListener.java            ⚠️  (alt: OrderEventListener)
│   └── ProductReservationEventPublisher.java
├── model/
│   └── Product.java
└── service/
    └── ProductService.java
```

---

## 🗂️ Vereinfachter Message Flow

### Alt: Komplex (4 Events)
```
Order Service                 RabbitMQ                 Product Service
     │                            │                            │
     ├─ OrderCreatedEvent ────────┼────→ Empfangen & Reserve   │
     │                            │                            │
     │         ┌─────────────────────── ProductReservationUpdated/Failed
     │         │                   │                            │
     ├─────────┘ (Empfangen)       │                            │
     │ Order Status Update         │                            │
     │                            │                            │
     ├─ OrderCanceledEvent ───────┼────→ Release Reserve       │
```

### Neu: Simpel (3 Events)
```
Cart Service                  RabbitMQ                 Product Service
     │                            │                            │
     ├─ CartCheckoutEvent ────────┼────→ Empfangen & Reserve   │
     │                            │                            │
     │         ┌─────────────────────── ProductReservationConfirmed/Failed
     │         │                   │                            │
     ├─────────┘ (Empfangen)       │                            │
     │ Cart Status Update          │                            │
```

---

## 📋 Implementation-Komplexität

```
ALT (Order):
├─ 4 Event-Klassen
├─ OrderService mit createOrder() + cancelOrder()
├─ 2 separate Saga Flows (Creation & Cancellation)
├─ 3 Test-Szenarien
└─ ~12 Methods zu implementieren

NEU (Cart):
├─ 3 Event-Klassen (-1)
├─ CartService mit checkout() (einfacher)
├─ 1 Saga Flow (nur Checkout)
├─ 2 Test-Szenarien (-1)
└─ ~9 Methods zu implementieren (-25%)
```

---

## ✅ Vollständigkeitscheckliste

| Item | Status |
|------|--------|
| START_HERE.md aktualisiert | ✅ |
| planA.md (Cart Service) neu | ✅ |
| planB.md (Product Service) neu | ✅ |
| PROJECT_OVERVIEW.md aktualisiert | ✅ |
| TASK_COMPARISON.md aktualisiert | ✅ |
| QUICK_REFERENCE.md vorhanden | ✅ (alt) |
| DOCUMENTATION_INDEX.md vorhanden | ✅ (alt) |
| Event-Struktur geklärt | ✅ |
| Queue-Namen dokumentiert | ✅ |
| Ports definiert (A=8091, B=8092) | ✅ |
| Test-Szenarien angepasst | ✅ |

---

## 🎓 Learning Outcome

### Person A (Cart Service) lernt:
- ✅ Spring Cloud Stream Publishing
- ✅ Event-Publishing im Service-Layer
- ✅ RabbitMQ Binding Configuration
- ✅ Async Event Listening
- ✅ Status Management basierend auf Events

### Person B (Product Service) lernt:
- ✅ Spring Cloud Stream Listening
- ✅ Event-Processing im Service-Layer
- ✅ RabbitMQ Binding Configuration
- ✅ Async Event Publishing
- ✅ Business Logic (Reservierung) mit Events

### Beide zusammen:
- ✅ Saga Pattern (Choreography)
- ✅ Event-Driven Architecture
- ✅ Asynchrone Kommunikation
- ✅ Microservice-Entkopplung
- ✅ OpenAPI/Swagger Dokumentation

---

## 🚀 Ready to Start!

Alle Dateien sind aktualisiert. Sie können jetzt sofort anfangen!

**Person A:** Öffne planA.md  
**Person B:** Öffne planB.md

Viel Erfolg! 🎉

