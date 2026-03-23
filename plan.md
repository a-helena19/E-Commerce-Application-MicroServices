# Enterprise Applications Exercise 3 – Asynchronous Communication Plan
**Date:** 23.03.2026  
**Objective:** Extend the e-commerce microservice application with asynchronous messaging using Spring Cloud Stream

---

## Overview
This plan outlines the implementation of event-driven architecture using Spring Cloud Stream with a focus on decoupling microservices through asynchronous messaging. A choreography-based saga pattern will be implemented between Order and Product/Inventory services.

---

## Requirements Analysis

### Primary Requirements
1. **Asynchronous Communication:** Refactor/extend communication between at least 2 services to use Spring Cloud Stream
2. **Messaging Broker:** Use RabbitMQ or Kafka as the binder
3. **Saga Pattern:** Implement choreography-based saga (e.g., Order Canceled → Update Product Reservations)
4. **Unique Ports:** Ensure each microservice has a unique port
5. **Code Structure:** Follow Spring Boot best practices (controllers, services, repositories, entities)
6. **API Documentation:** Use OpenAPI/Springdoc for Swagger UI at `http://localhost:8080/swagger-ui/index.html`
7. **Deliverable:** Executable .zip archive with README.md documentation

---

## Current Project State

### Existing Microservices
- **Api Gateway** (Port: TBD)
- **Cart Service** (Port: TBD)
- **Order Service** (Port: 8094)
- **Product Service** (Port: 8092)
- **User Service** (Port: TBD)

### Current Dependencies
- Spring Boot 3.3.5
- Spring Data JPA
- H2 Database (in-memory)
- Springdoc OpenAPI 2.5.0
- Java 17

### Current Status
- Services use synchronous REST communication (e.g., ProductServiceClient)
- OpenAPI documentation is already partially implemented
- Services have unique ports configured

---

## Implementation Plan

### Phase 1: Dependency Management & Configuration

#### 1.1 Add Spring Cloud Stream Dependencies
**Services Affected:** All microservices

**Actions:**
- [ ] Add Spring Cloud Stream starter to all `build.gradle` files
- [ ] Add RabbitMQ binder dependency (or Kafka as alternative)
- [ ] Update Spring Cloud version to compatible release (2023.x for Spring Boot 3.3.5)
- [ ] Add Spring Cloud Stream Test Support for testing

**Dependencies to Add:**
```groovy
implementation 'org.springframework.cloud:spring-cloud-starter-stream-rabbit'  // or kafka
testImplementation 'org.springframework.cloud:spring-cloud-stream-test-support'
```

**Configuration in Parent build.gradle (if applicable):**
```groovy
ext {
    set('springCloudVersion', "2023.0.1")
}

dependencyManagement {
    imports {
        mavenBom "org.springframework.cloud:spring-cloud-dependencies:${springCloudVersion}"
    }
}
```

#### 1.2 Configure Application Properties
**Services Affected:** Order Service, Product Service, Cart Service

**Actions:**
- [ ] Configure RabbitMQ connection settings (host, port, username, password)
- [ ] Define Spring Cloud Stream bindings for each service
- [ ] Ensure unique ports for all services
- [ ] Add logging configuration for message tracing

**Example Configuration:**
```properties
# RabbitMQ Configuration
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest

# Spring Cloud Stream Bindings
spring.cloud.stream.bindings.orderCreated-out.destination=order-events
spring.cloud.stream.bindings.orderCreated-out.contentType=application/json

spring.cloud.stream.bindings.productReservationUpdated-in.destination=order-events
spring.cloud.stream.bindings.productReservationUpdated-in.group=product-service-group
spring.cloud.stream.bindings.productReservationUpdated-in.contentType=application/json
```

---

### Phase 2: Event Models & DTOs

#### 2.1 Create Event Classes
**Location:** Create `events` package in each service

**Events to Implement:**

**OrderService Events:**
- [ ] `OrderCreatedEvent` - Triggered when order is placed
- [ ] `OrderCanceledEvent` - Triggered when order is canceled
- [ ] `OrderConfirmedEvent` - Triggered when order is confirmed

**ProductService Events:**
- [ ] `ProductReservationUpdatedEvent` - Triggered when reservations are updated
- [ ] `ProductReservationFailedEvent` - Triggered when reservation fails

**Structure Example:**
```java
// OrderService - events/OrderCreatedEvent.java
public class OrderCreatedEvent {
    private UUID orderId;
    private UUID productId;
    private int quantity;
    private LocalDateTime timestamp;
    // getters, setters, constructors
}

// OrderService - events/OrderCanceledEvent.java
public class OrderCanceledEvent {
    private UUID orderId;
    private UUID productId;
    private int quantity;
    private LocalDateTime timestamp;
    // getters, setters, constructors
}

// ProductService - events/ProductReservationUpdatedEvent.java
public class ProductReservationUpdatedEvent {
    private UUID productId;
    private int reservedQuantity;
    private String status;
    private UUID orderId;
    private LocalDateTime timestamp;
    // getters, setters, constructors
}
```

---

### Phase 3: Messaging Configuration

#### 3.1 Create Message Binding Interfaces
**Location:** Create `messaging` or `config` package in each service

**Actions:**
- [ ] Create Spring Cloud Stream binding interfaces using `@Input` and `@Output`
- [ ] Define channels for events
- [ ] Configure message converters

**Example - OrderService:**
```java
@Configuration
public class OrderMessagingBindings {
    public static final String ORDER_CREATED_CHANNEL = "order-created";
    public static final String ORDER_CANCELED_CHANNEL = "order-canceled";
    public static final String PRODUCT_RESERVATION_CHANNEL = "product-reservation";
    
    @FunctionalInterface
    public interface OrderEventPublisher {
        @Output(ORDER_CREATED_CHANNEL)
        MessageChannel orderCreated();
        
        @Output(ORDER_CANCELED_CHANNEL)
        MessageChannel orderCanceled();
    }
}
```

**Example - ProductService:**
```java
@Configuration
public class ProductMessagingBindings {
    public static final String ORDER_EVENTS_CHANNEL = "order-events";
    
    @FunctionalInterface
    public interface OrderEventListener {
        @Input(ORDER_EVENTS_CHANNEL)
        SubscribableChannel orderEvents();
    }
}
```

#### 3.2 Implement Message Publishing
**Location:** Create `messaging/publisher` or `event` package

**Actions:**
- [ ] Create Publisher interfaces/classes for each service
- [ ] Implement message publishing logic in services
- [ ] Handle serialization/deserialization

**Example - OrderEventPublisher:**
```java
@Component
public class OrderEventPublisher {
    private final MessageChannel orderCreatedChannel;
    private final MessageChannel orderCanceledChannel;
    
    public void publishOrderCreated(OrderCreatedEvent event) {
        Message<OrderCreatedEvent> message = 
            MessageBuilder.withPayload(event)
                .setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON)
                .build();
        orderCreatedChannel.send(message);
    }
    
    public void publishOrderCanceled(OrderCanceledEvent event) {
        Message<OrderCanceledEvent> message = 
            MessageBuilder.withPayload(event)
                .setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON)
                .build();
        orderCanceledChannel.send(message);
    }
}
```

#### 3.3 Implement Message Listeners
**Location:** Create `messaging/listener` package

**Actions:**
- [ ] Create listener classes for each service
- [ ] Implement message handling logic
- [ ] Handle errors and retries

**Example - OrderEventListener (in ProductService):**
```java
@Component
public class OrderEventListener {
    private final ProductService productService;
    private final ProductRepository productRepository;
    
    @StreamListener("order-created")
    public void handleOrderCreated(OrderCreatedEvent event) {
        try {
            // Update product reservations
            Product product = productRepository.findById(event.getProductId())
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));
            
            product.increaseReservation(event.getQuantity());
            productRepository.save(product);
            
            // Publish confirmation event (optional)
        } catch (Exception e) {
            // Handle error and potentially publish failure event
        }
    }
    
    @StreamListener("order-canceled")
    public void handleOrderCanceled(OrderCanceledEvent event) {
        try {
            Product product = productRepository.findById(event.getProductId())
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));
            
            product.decreaseReservation(event.getQuantity());
            productRepository.save(product);
        } catch (Exception e) {
            // Handle error
        }
    }
}
```

---

### Phase 4: Saga Implementation (Choreography Pattern)

#### 4.1 Design Saga Workflow
**Scenario: Order Placement and Product Reservation**

**Flow:**
1. User places order → `OrderCreatedEvent` published
2. Product Service listens to `OrderCreatedEvent`
3. Product Service updates reservations and publishes `ProductReservationUpdatedEvent`
4. Order Service listens to `ProductReservationUpdatedEvent` and confirms order
5. If reservation fails, Order Service is notified and cancels order

**Saga Sequence Diagram:**
```
User -> Order Service: Create Order
Order Service -> RabbitMQ: Publish OrderCreatedEvent
RabbitMQ -> Product Service: OrderCreatedEvent
Product Service: Reserve Products
Product Service -> RabbitMQ: Publish ProductReservationUpdatedEvent
RabbitMQ -> Order Service: ProductReservationUpdatedEvent
Order Service: Confirm/Cancel Order
```

#### 4.2 Implement Saga Participants

**OrderService:**
- [ ] Implement `OrderCreatedEvent` publishing in order creation
- [ ] Implement `OrderCanceledEvent` publishing in order cancellation
- [ ] Listen to `ProductReservationUpdatedEvent`
- [ ] Update order status based on reservation results

**ProductService:**
- [ ] Listen to `OrderCreatedEvent`
- [ ] Update product reservations (increase)
- [ ] Publish `ProductReservationUpdatedEvent` with status
- [ ] Listen to `OrderCanceledEvent`
- [ ] Update product reservations (decrease)

---

### Phase 5: Service Updates & Refactoring

#### 5.1 OrderService Updates
**Location:** `Order Service/src/main/java/at/fhv/orderservice/`

**Actions:**
- [ ] Update `OrderService` class to use event publishing
  - Inject `OrderEventPublisher`
  - Publish `OrderCreatedEvent` after creating order
  - Publish `OrderCanceledEvent` after canceling order
  - Handle confirmation events from Product Service

**Example:**
```java
@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderEventPublisher eventPublisher;
    
    public Order createOrder(OrderCreateRequest request) {
        Order order = new Order(...);
        order = orderRepository.save(order);
        
        // Publish event
        OrderCreatedEvent event = new OrderCreatedEvent(
            order.getId(), 
            request.getProductId(), 
            request.getQuantity()
        );
        eventPublisher.publishOrderCreated(event);
        
        return order;
    }
    
    public void cancelOrder(UUID orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException());
        
        order.setStatus(OrderStatus.CANCELED);
        order = orderRepository.save(order);
        
        // Publish cancellation event
        OrderCanceledEvent event = new OrderCanceledEvent(
            order.getId(),
            order.getProductId(),
            order.getQuantity()
        );
        eventPublisher.publishOrderCanceled(event);
    }
}
```

#### 5.2 ProductService Updates
**Location:** `Product Service/src/main/java/at/fhv/productservice/`

**Actions:**
- [ ] Update `ProductService` to handle reservations
- [ ] Add reservation counter to `Product` entity
- [ ] Implement event listener for order events
- [ ] Update product availability calculation

**Example - Product Entity:**
```java
@Entity
public class Product {
    // ...existing fields...
    
    @Column(nullable = false)
    private int reservedQuantity = 0;
    
    public void increaseReservation(int quantity) {
        this.reservedQuantity += quantity;
    }
    
    public void decreaseReservation(int quantity) {
        this.reservedQuantity = Math.max(0, this.reservedQuantity - quantity);
    }
    
    public int getAvailableQuantity() {
        return quantity - reservedQuantity;
    }
}
```

#### 5.3 Remove Synchronous Dependencies
**Location:** Services affected by synchronous calls

**Actions:**
- [ ] Refactor `ProductServiceClient` usage in CartService (if applicable)
- [ ] Replace REST calls with event-based communication where appropriate
- [ ] Keep REST calls for read-only operations (getting product details)

---

### Phase 6: Testing

#### 6.1 Unit Tests for Event Publishing
**Location:** Each service `/src/test/java/`

**Actions:**
- [ ] Create tests for event publishing
- [ ] Mock message channels
- [ ] Verify correct events are published

#### 6.2 Unit Tests for Event Listeners
**Actions:**
- [ ] Create tests for event listeners
- [ ] Mock repositories and services
- [ ] Verify correct handling of events

#### 6.3 Integration Tests
**Actions:**
- [ ] Use `@SpringBootTest` with Spring Cloud Stream Test Support
- [ ] Test end-to-end saga flows
- [ ] Test error scenarios and compensation

**Example Test:**
```java
@SpringBootTest
@TestPropertySource(properties = {
    "spring.cloud.stream.bindings.order-created.destination=test.orders"
})
class OrderEventPublisherTest {
    
    @Autowired
    private OrderEventPublisher publisher;
    
    @Autowired
    @Output("order-created")
    private MessageChannel messageChannel;
    
    @Test
    void testPublishOrderCreated() {
        OrderCreatedEvent event = new OrderCreatedEvent(...);
        publisher.publishOrderCreated(event);
        
        // Verify message was sent
    }
}
```

---

### Phase 7: Configuration & Port Management

#### 7.1 Configure Unique Ports
**File:** Each service's `application.properties`

**Current Ports:**
- Order Service: 8094
- Product Service: 8092
- Cart Service: 8093 (to be set)
- User Service: 8091 (to be set)
- Api Gateway: 8080 (to be set)

**Actions:**
- [ ] Verify all services have unique ports
- [ ] Update gateway routing if needed

#### 7.2 Configure Spring Cloud Stream Bindings
**File:** Each service's `application.properties`

**Actions:**
- [ ] Define input/output bindings for each service
- [ ] Configure content-type headers
- [ ] Set consumer groups for scalability

**Example Configuration:**
```properties
# Order Service
spring.cloud.stream.bindings.order-created.destination=order-events
spring.cloud.stream.bindings.order-created.contentType=application/json
spring.cloud.stream.bindings.product-reservation.destination=product-reservation-events
spring.cloud.stream.bindings.product-reservation.group=order-service-group

# Product Service
spring.cloud.stream.bindings.order-events.destination=order-events
spring.cloud.stream.bindings.order-events.group=product-service-group
spring.cloud.stream.bindings.order-events.contentType=application/json
```

---

### Phase 8: API Documentation

#### 8.1 Update OpenAPI Annotations
**Actions:**
- [ ] Add `@Operation` annotations to all endpoints
- [ ] Add `@Schema` annotations to DTOs
- [ ] Document event structures in OpenAPI
- [ ] Add examples to API documentation

**Example:**
```java
@PostMapping
@Operation(summary = "Create a new order")
@ApiResponse(responseCode = "201", description = "Order created successfully")
public ResponseEntity<OrderDTO> createOrder(
    @RequestBody @Valid OrderCreateRequest request) {
    // Implementation
}
```

#### 8.2 Configure Swagger UI
**Actions:**
- [ ] Ensure Swagger UI is accessible at configured port
- [ ] Add API title and description
- [ ] Configure API groups if needed

**Configuration:**
```properties
springdoc.swagger-ui.enabled=true
springdoc.api-docs.enabled=true
springdoc.api-docs.path=/v3/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.api-docs.title=Order Service API
springdoc.api-docs.version=1.0.0
```

---

### Phase 9: Infrastructure Setup

#### 9.1 Docker/RabbitMQ Setup (Optional but Recommended)
**Actions:**
- [ ] Create `docker-compose.yml` for RabbitMQ
- [ ] Document how to start RabbitMQ
- [ ] Provide configuration for local development

**Example docker-compose.yml:**
```yaml
version: '3.8'
services:
  rabbitmq:
    image: rabbitmq:3.13-management
    ports:
      - "5672:5672"
      - "15672:15672"
    environment:
      RABBITMQ_DEFAULT_USER: guest
      RABBITMQ_DEFAULT_PASS: guest
    volumes:
      - rabbitmq_data:/var/lib/rabbitmq

volumes:
  rabbitmq_data:
```

---

### Phase 10: Documentation & Packaging

#### 10.1 Create/Update README.md
**Location:** Project root

**Contents to Include:**
- [ ] Project overview
- [ ] Architecture diagram (ASCII art)
- [ ] Setup instructions
- [ ] How to run the application
- [ ] How to access APIs (Swagger UI)
- [ ] Saga flow explanation
- [ ] Event descriptions
- [ ] Testing instructions
- [ ] Troubleshooting guide

**Structure:**
```markdown
# E-Commerce Microservices - Asynchronous Communication

## Overview
- Project description
- Technologies used

## Architecture
- Microservices overview
- Event-driven communication diagram

## Setup Instructions
- Prerequisites
- Installation steps

## Running the Application
- Start RabbitMQ
- Start each microservice
- Access Swagger UI

## API Documentation
- Service endpoints
- Event flows

## Event Catalog
- OrderCreatedEvent
- OrderCanceledEvent
- ProductReservationUpdatedEvent
- ... (all events)

## Saga Implementation
- Choreography pattern explanation
- Sequence diagrams

## Testing
- Unit tests
- Integration tests
- Manual testing guide

## Troubleshooting
- Common issues
- Solutions
```

#### 10.2 Code Organization & Documentation
**Actions:**
- [ ] Add JavaDoc comments to all classes
- [ ] Add inline comments for complex logic
- [ ] Ensure consistent code formatting
- [ ] Follow Spring Boot conventions

---

### Phase 11: Final Testing & Validation

#### 11.1 End-to-End Testing
**Actions:**
- [ ] Create order via Order Service API
- [ ] Verify event is published to RabbitMQ
- [ ] Verify Product Service receives event
- [ ] Verify product reservations are updated
- [ ] Test cancellation flow

#### 11.2 Performance & Reliability Testing
**Actions:**
- [ ] Test message ordering
- [ ] Test error handling and retries
- [ ] Test with multiple instances (optional)
- [ ] Monitor RabbitMQ message flow

#### 11.3 Documentation Validation
**Actions:**
- [ ] Verify all APIs are documented
- [ ] Test Swagger UI accessibility
- [ ] Verify README instructions are complete
- [ ] Test on clean environment

---

### Phase 12: Packaging & Submission

#### 12.1 Prepare for Submission
**Actions:**
- [ ] Clean build artifacts: `gradlew clean`
- [ ] Remove unnecessary files
- [ ] Verify .gitignore is present
- [ ] Update version numbers if needed

#### 12.2 Create ZIP Archive
**Actions:**
- [ ] Create zip file with all source code
- [ ] Include gradle wrapper files
- [ ] Include README.md
- [ ] Include docker-compose.yml (if created)
- [ ] Exclude build/ and .gradle/ directories

**Command:**
```bash
# From project root
# On Windows:
# Use 7-Zip or built-in compression to create archive
# Or use: Compress-Archive -Path . -DestinationPath E-Commerce-Application-MicroServices.zip
```

---

## Implementation Checklist

### Pre-Implementation
- [ ] Review requirements
- [ ] Analyze current codebase
- [ ] Plan event flows
- [ ] Design message schemas

### Dependency Management
- [ ] Add Spring Cloud Stream dependencies to all services
- [ ] Add RabbitMQ binder
- [ ] Update Spring Cloud version in dependency management

### Core Implementation
- [ ] Create event classes
- [ ] Implement message bindings
- [ ] Implement event publishers
- [ ] Implement event listeners
- [ ] Update Order Service for event publishing
- [ ] Update Product Service for event handling
- [ ] Implement saga compensation logic

### Configuration
- [ ] Configure unique ports for all services
- [ ] Configure RabbitMQ connection
- [ ] Configure Spring Cloud Stream bindings
- [ ] Set up logging for message tracing

### Testing
- [ ] Write unit tests for publishers
- [ ] Write unit tests for listeners
- [ ] Write integration tests
- [ ] Test saga flows manually

### Documentation
- [ ] Update README.md
- [ ] Add OpenAPI annotations
- [ ] Create ASCII architecture diagrams
- [ ] Document all events
- [ ] Add troubleshooting section

### Quality Assurance
- [ ] Validate all APIs in Swagger UI
- [ ] Test on clean environment
- [ ] Verify message flow in RabbitMQ
- [ ] Check code style and formatting

### Submission Preparation
- [ ] Clean build artifacts
- [ ] Create ZIP archive
- [ ] Verify executability
- [ ] Final documentation review

---

## Technical Decisions

### Messaging Broker: RabbitMQ vs Kafka
**Chosen: RabbitMQ**
- Reasons:
  - Lower learning curve
  - Easier local setup
  - Suitable for this exercise
  - Good Spring Cloud Stream support

### Saga Pattern: Choreography vs Orchestration
**Chosen: Choreography**
- Reasons:
  - Decoupled services
  - Simpler for this use case
  - Services are aware of domain events
  - Follows event-driven architecture

### Event Publishing Strategy
**Chosen: Spring Cloud Stream with Functional Approach**
- Reasons:
  - Spring's recommended approach
  - Clean separation of concerns
  - Good testing support
  - Backward compatible

---

## Estimated Timeline
- Phase 1-2: Dependencies & Events (2-3 hours)
- Phase 3: Messaging Configuration (2-3 hours)
- Phase 4-5: Saga & Service Updates (4-5 hours)
- Phase 6: Testing (2-3 hours)
- Phase 7-8: Configuration & Documentation (2-3 hours)
- Phase 9-10: Infrastructure & README (2-3 hours)
- Phase 11-12: Testing & Submission (2-3 hours)

**Total Estimated Time: 16-22 hours**

---

## Success Criteria
- ✓ All services communicate asynchronously via Spring Cloud Stream
- ✓ RabbitMQ is used as the messaging broker
- ✓ Choreography-based saga is implemented and working
- ✓ All services have unique, documented ports
- ✓ Code follows Spring Boot best practices
- ✓ APIs are fully documented with OpenAPI/Swagger
- ✓ Application is executable from ZIP archive
- ✓ README.md contains complete documentation
- ✓ End-to-end saga flows work correctly
- ✓ Error handling and compensation logic are implemented

---

## Notes & Considerations
- Ensure idempotency in event listeners (events might be delivered multiple times)
- Implement proper error handling and compensation (e.g., for failed reservations)
- Use consistent message format (JSON) across all services
- Consider adding message headers for tracing and correlation
- Test with multiple instances if possible
- Document all assumptions and design decisions
- Consider adding health checks for RabbitMQ connectivity

---

**Plan Version:** 1.0  
**Created:** 23.03.2026  
**Status:** Ready for Implementation

