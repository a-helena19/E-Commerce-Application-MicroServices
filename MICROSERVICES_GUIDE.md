# E-Commerce Microservices Migration Guide

**Datum:** 17.03.2026  
**Ziel:** Umwandlung der monolithischen E-Commerce-Anwendung in Microservices  
**Deadline:** Exercise 2 Submission

---

## 📋 Übersicht der Architektur

### Zielstruktur: 4 Spring Boot Projekte

```
E-Commerce-Microservices/
├── api-gateway/                    (Port 8080 - User-facing Entry Point)
├── user-service/                   (Port 8081)
├── product-service/                (Port 8082)
├── order-service/                  (Port 8083)
└── README.md
```

### Komponentenübersicht

| Service | Port | Verantwortung | DB | Kommunikation |
|---------|------|---------------|----|----|
| **API Gateway** | 8080 | Single Entry Point, OpenAPI UI, Request Routing | - | REST (zu allen Services) |
| **User Service** | 8081 | Benutzerprofile, Authentifizierung | H2/Postgres | REST |
| **Product Service** | 8082 | Produkte, Lagerbestände, Preise | H2/Postgres | REST |
| **Order Service** | 8083 | Bestellungen, Checkout-Logik | H2/Postgres | REST (zu User/Product Services) |

---

## 🚀 Schritt-für-Schritt Implementierung

### **Schritt 1: Projektstruktur vorbereiten**

#### 1.1 Hauptverzeichnis erstellen
```bash
cd C:\Users\Hla Aldlol\Desktop\FHV\4. Semester\Enterprise Applications\backend
mkdir E-Commerce-Microservices
cd E-Commerce-Microservices
```

#### 1.2 Vier neue Spring Boot Projekte mit Gradle erstellen

Verwende Spring Boot Initializr oder IntelliJ IDEA:

**Gemeinsame Dependencies für alle Services:**
```gradle
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.2.0'
    id 'io.spring.dependency-management' version '1.1.0'
}

group = 'at.fhv.e_commerce'
version = '0.0.1-SNAPSHOT'
sourceCompatibility = '17'

dependencies {
    // Spring Boot Starter Web (REST)
    implementation 'org.springframework.boot:spring-boot-starter-web'
    
    // Spring Boot Starter Data JPA
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    
    // H2 Database (für Entwicklung)
    runtimeOnly 'com.h2database:h2'
    
    // Lombok
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
    
    // OpenAPI / Springdoc (für API-Dokumentation)
    implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.0.2'
    
    // Testing
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}
```

---

### **Schritt 2: API Gateway implementieren**

#### 2.1 Projektstruktur
```
api-gateway/
├── src/main/java/at/fhv/e_commerce/gateway/
│   ├── ApiGatewayApplication.java
│   ├── controller/
│   │   ├── CartController.java
│   │   ├── OrderController.java
│   │   ├── ProductController.java
│   │   └── UserController.java
│   └── config/
│       └── RestClientConfig.java
├── src/main/resources/
│   └── application.properties
└── build.gradle
```

#### 2.2 ApiGatewayApplication.java
```java
package at.fhv.e_commerce.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
```

#### 2.3 application.properties (API Gateway)
```properties
server.port=8080
spring.application.name=api-gateway

# OpenAPI Configuration
springdoc.api-docs.path=/v3/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.swagger-ui.enable=true
springdoc.swagger-ui.operations-sorter=method

# Service URLs (intern)
service.user.url=http://localhost:8081
service.product.url=http://localhost:8082
service.order.url=http://localhost:8083
```

#### 2.4 RestClientConfig.java
```java
package at.fhv.e_commerce.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RestClientConfig {

    @Value("${service.user.url}")
    public String userServiceUrl;

    @Value("${service.product.url}")
    public String productServiceUrl;

    @Value("${service.order.url}")
    public String orderServiceUrl;
}
```

#### 2.5 Beispiel-Controller: ProductController.java (API Gateway)
```java
package at.fhv.e_commerce.gateway.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import at.fhv.e_commerce.gateway.config.RestClientConfig;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private RestClientConfig restClientConfig;

    @GetMapping
    public ResponseEntity<?> getAllProducts() {
        String url = restClientConfig.productServiceUrl + "/products";
        return restTemplate.getForEntity(url, Object.class);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable String id) {
        String url = restClientConfig.productServiceUrl + "/products/" + id;
        return restTemplate.getForEntity(url, Object.class);
    }
}
```

---

### **Schritt 3: User Service implementieren**

#### 3.1 Projektstruktur
```
user-service/
├── src/main/java/at/fhv/e_commerce/user/
│   ├── UserServiceApplication.java
│   ├── controller/
│   │   └── UserRestController.java
│   ├── service/
│   │   ├── UserService.java
│   │   └── UserServiceImpl.java
│   ├── repository/
│   │   └── UserRepository.java
│   ├── entity/
│   │   └── User.java
│   └── dto/
│       ├── CreateUserDTO.java
│       ├── UpdateUserDTO.java
│       └── UserResponseDTO.java
├── src/main/resources/
│   └── application.properties
└── build.gradle
```

#### 3.2 User.java (Entity)
```java
package at.fhv.e_commerce.user.entity;

import lombok.*;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User {
    
    @Id
    private String id = UUID.randomUUID().toString();
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @Column(nullable = false)
    private String firstName;
    
    @Column(nullable = false)
    private String lastName;
    
    @Column(nullable = false)
    private String password; // In Produktion: verschlüsselt!
    
    private String address;
    private String city;
    private String postalCode;
}
```

#### 3.3 UserResponseDTO.java
```java
package at.fhv.e_commerce.user.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserResponseDTO {
    private String id;
    private String email;
    private String firstName;
    private String lastName;
    private String address;
    private String city;
    private String postalCode;
}
```

#### 3.4 UserRepository.java
```java
package at.fhv.e_commerce.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import at.fhv.e_commerce.user.entity.User;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);
}
```

#### 3.5 UserService.java (Interface)
```java
package at.fhv.e_commerce.user.service;

import at.fhv.e_commerce.user.dto.CreateUserDTO;
import at.fhv.e_commerce.user.dto.UpdateUserDTO;
import at.fhv.e_commerce.user.dto.UserResponseDTO;

public interface UserService {
    UserResponseDTO createUser(CreateUserDTO dto);
    UserResponseDTO getUserById(String id);
    UserResponseDTO updateUser(String id, UpdateUserDTO dto);
    void deleteUser(String id);
}
```

#### 3.6 UserServiceImpl.java
```java
package at.fhv.e_commerce.user.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import at.fhv.e_commerce.user.dto.CreateUserDTO;
import at.fhv.e_commerce.user.dto.UpdateUserDTO;
import at.fhv.e_commerce.user.dto.UserResponseDTO;
import at.fhv.e_commerce.user.entity.User;
import at.fhv.e_commerce.user.repository.UserRepository;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserResponseDTO createUser(CreateUserDTO dto) {
        User user = User.builder()
            .email(dto.getEmail())
            .firstName(dto.getFirstName())
            .lastName(dto.getLastName())
            .password(dto.getPassword()) // TODO: Verschlüsseln!
            .build();
        
        User saved = userRepository.save(user);
        return mapToDTO(saved);
    }

    @Override
    public UserResponseDTO getUserById(String id) {
        Optional<User> user = userRepository.findById(id);
        return user.map(this::mapToDTO).orElse(null);
    }

    @Override
    public UserResponseDTO updateUser(String id, UpdateUserDTO dto) {
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            User u = user.get();
            u.setFirstName(dto.getFirstName());
            u.setLastName(dto.getLastName());
            u.setAddress(dto.getAddress());
            u.setCity(dto.getCity());
            u.setPostalCode(dto.getPostalCode());
            userRepository.save(u);
            return mapToDTO(u);
        }
        return null;
    }

    @Override
    public void deleteUser(String id) {
        userRepository.deleteById(id);
    }

    private UserResponseDTO mapToDTO(User user) {
        return UserResponseDTO.builder()
            .id(user.getId())
            .email(user.getEmail())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .address(user.getAddress())
            .city(user.getCity())
            .postalCode(user.getPostalCode())
            .build();
    }
}
```

#### 3.7 UserRestController.java
```java
package at.fhv.e_commerce.user.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import at.fhv.e_commerce.user.dto.CreateUserDTO;
import at.fhv.e_commerce.user.dto.UpdateUserDTO;
import at.fhv.e_commerce.user.dto.UserResponseDTO;
import at.fhv.e_commerce.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/users")
@Tag(name = "User Management", description = "APIs for user management")
public class UserRestController {

    @Autowired
    private UserService userService;

    @PostMapping
    @Operation(summary = "Create a new user")
    public ResponseEntity<UserResponseDTO> createUser(@RequestBody CreateUserDTO dto) {
        UserResponseDTO result = userService.createUser(dto);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable String id) {
        UserResponseDTO result = userService.getUserById(id);
        if (result != null) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user")
    public ResponseEntity<UserResponseDTO> updateUser(
            @PathVariable String id,
            @RequestBody UpdateUserDTO dto) {
        UserResponseDTO result = userService.updateUser(id, dto);
        if (result != null) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
```

#### 3.8 application.properties (User Service)
```properties
server.port=8081
spring.application.name=user-service

# Database Configuration
spring.datasource.url=jdbc:h2:mem:userdb;DB_CLOSE_DELAY=-1
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=true

# OpenAPI Configuration
springdoc.api-docs.path=/v3/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
```

---

### **Schritt 4: Product Service implementieren**

#### 4.1 Projektstruktur
```
product-service/
├── src/main/java/at/fhv/e_commerce/product/
│   ├── ProductServiceApplication.java
│   ├── controller/
│   │   └── ProductRestController.java
│   ├── service/
│   │   ├── ProductService.java
│   │   └── ProductServiceImpl.java
│   ├── repository/
│   │   └── ProductRepository.java
│   ├── entity/
│   │   └── Product.java
│   └── dto/
│       ├── CreateProductDTO.java
│       ├── ProductResponseDTO.java
│       └── UpdateStockDTO.java
├── src/main/resources/
│   └── application.properties
└── build.gradle
```

#### 4.2 Product.java (Entity)
```java
package at.fhv.e_commerce.product.entity;

import lombok.*;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "products")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Product {
    
    @Id
    private String id = UUID.randomUUID().toString();
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false, length = 1000)
    private String description;
    
    @Column(nullable = false)
    private BigDecimal price;
    
    @Column(nullable = false)
    private Integer stock;
    
    private String category;
}
```

#### 4.3 ProductResponseDTO.java
```java
package at.fhv.e_commerce.product.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProductResponseDTO {
    private String id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private String category;
}
```

#### 4.4 ProductRepository.java
```java
package at.fhv.e_commerce.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import at.fhv.e_commerce.product.entity.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {
}
```

#### 4.5 ProductService.java (Interface)
```java
package at.fhv.e_commerce.product.service;

import at.fhv.e_commerce.product.dto.CreateProductDTO;
import at.fhv.e_commerce.product.dto.ProductResponseDTO;
import at.fhv.e_commerce.product.dto.UpdateStockDTO;
import java.util.List;

public interface ProductService {
    ProductResponseDTO createProduct(CreateProductDTO dto);
    ProductResponseDTO getProductById(String id);
    List<ProductResponseDTO> getAllProducts();
    ProductResponseDTO updateProduct(String id, CreateProductDTO dto);
    ProductResponseDTO updateStock(String id, UpdateStockDTO dto);
    void deleteProduct(String id);
}
```

#### 4.6 ProductServiceImpl.java
```java
package at.fhv.e_commerce.product.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import at.fhv.e_commerce.product.dto.CreateProductDTO;
import at.fhv.e_commerce.product.dto.ProductResponseDTO;
import at.fhv.e_commerce.product.dto.UpdateStockDTO;
import at.fhv.e_commerce.product.entity.Product;
import at.fhv.e_commerce.product.repository.ProductRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Override
    public ProductResponseDTO createProduct(CreateProductDTO dto) {
        Product product = Product.builder()
            .name(dto.getName())
            .description(dto.getDescription())
            .price(dto.getPrice())
            .stock(dto.getStock())
            .category(dto.getCategory())
            .build();
        
        Product saved = productRepository.save(product);
        return mapToDTO(saved);
    }

    @Override
    public ProductResponseDTO getProductById(String id) {
        Optional<Product> product = productRepository.findById(id);
        return product.map(this::mapToDTO).orElse(null);
    }

    @Override
    public List<ProductResponseDTO> getAllProducts() {
        return productRepository.findAll().stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    @Override
    public ProductResponseDTO updateProduct(String id, CreateProductDTO dto) {
        Optional<Product> product = productRepository.findById(id);
        if (product.isPresent()) {
            Product p = product.get();
            p.setName(dto.getName());
            p.setDescription(dto.getDescription());
            p.setPrice(dto.getPrice());
            p.setCategory(dto.getCategory());
            productRepository.save(p);
            return mapToDTO(p);
        }
        return null;
    }

    @Override
    public ProductResponseDTO updateStock(String id, UpdateStockDTO dto) {
        Optional<Product> product = productRepository.findById(id);
        if (product.isPresent()) {
            Product p = product.get();
            p.setStock(dto.getStock());
            productRepository.save(p);
            return mapToDTO(p);
        }
        return null;
    }

    @Override
    public void deleteProduct(String id) {
        productRepository.deleteById(id);
    }

    private ProductResponseDTO mapToDTO(Product product) {
        return ProductResponseDTO.builder()
            .id(product.getId())
            .name(product.getName())
            .description(product.getDescription())
            .price(product.getPrice())
            .stock(product.getStock())
            .category(product.getCategory())
            .build();
    }
}
```

#### 4.7 ProductRestController.java
```java
package at.fhv.e_commerce.product.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import at.fhv.e_commerce.product.dto.CreateProductDTO;
import at.fhv.e_commerce.product.dto.ProductResponseDTO;
import at.fhv.e_commerce.product.dto.UpdateStockDTO;
import at.fhv.e_commerce.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;

@RestController
@RequestMapping("/products")
@Tag(name = "Product Management", description = "APIs for product management")
public class ProductRestController {

    @Autowired
    private ProductService productService;

    @PostMapping
    @Operation(summary = "Create a new product")
    public ResponseEntity<ProductResponseDTO> createProduct(@RequestBody CreateProductDTO dto) {
        ProductResponseDTO result = productService.createProduct(dto);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID")
    public ResponseEntity<ProductResponseDTO> getProductById(@PathVariable String id) {
        ProductResponseDTO result = productService.getProductById(id);
        if (result != null) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping
    @Operation(summary = "Get all products")
    public ResponseEntity<List<ProductResponseDTO>> getAllProducts() {
        List<ProductResponseDTO> result = productService.getAllProducts();
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update product")
    public ResponseEntity<ProductResponseDTO> updateProduct(
            @PathVariable String id,
            @RequestBody CreateProductDTO dto) {
        ProductResponseDTO result = productService.updateProduct(id, dto);
        if (result != null) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.notFound().build();
    }

    @PatchMapping("/{id}/stock")
    @Operation(summary = "Update product stock")
    public ResponseEntity<ProductResponseDTO> updateStock(
            @PathVariable String id,
            @RequestBody UpdateStockDTO dto) {
        ProductResponseDTO result = productService.updateStock(id, dto);
        if (result != null) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete product")
    public ResponseEntity<Void> deleteProduct(@PathVariable String id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
```

#### 4.8 application.properties (Product Service)
```properties
server.port=8082
spring.application.name=product-service

# Database Configuration
spring.datasource.url=jdbc:h2:mem:productdb;DB_CLOSE_DELAY=-1
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false

# OpenAPI Configuration
springdoc.api-docs.path=/v3/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
```

---

### **Schritt 5: Order Service implementieren**

#### 5.1 Projektstruktur
```
order-service/
├── src/main/java/at/fhv/e_commerce/order/
│   ├── OrderServiceApplication.java
│   ├── controller/
│   │   └── OrderRestController.java
│   ├── service/
│   │   ├── OrderService.java
│   │   ├── OrderServiceImpl.java
│   │   └── ProductServiceClient.java
│   ├── repository/
│   │   └── OrderRepository.java
│   ├── entity/
│   │   ├── Order.java
│   │   └── OrderItem.java
│   └── dto/
│       ├── CreateOrderDTO.java
│       ├── OrderResponseDTO.java
│       ├── OrderItemDTO.java
│       └── ProductDTO.java
├── src/main/resources/
│   └── application.properties
└── build.gradle
```

#### 5.2 Order.java (Entity)
```java
package at.fhv.e_commerce.order.entity;

import lombok.*;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Order {
    
    @Id
    private String id = UUID.randomUUID().toString();
    
    @Column(nullable = false)
    private String userId;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.PENDING;
    
    @Column(nullable = false)
    private BigDecimal totalPrice;
    
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "order_id")
    private List<OrderItem> items = new ArrayList<>();
    
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    private LocalDateTime updatedAt;
    
    public enum OrderStatus {
        PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED
    }
}
```

#### 5.3 OrderItem.java (Entity)
```java
package at.fhv.e_commerce.order.entity;

import lombok.*;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "order_items")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OrderItem {
    
    @Id
    private String id = UUID.randomUUID().toString();
    
    @Column(nullable = false)
    private String productId;
    
    @Column(nullable = false)
    private Integer quantity;
    
    @Column(nullable = false)
    private BigDecimal priceSnapshot; // Preis zum Zeitpunkt der Bestellung
}
```

#### 5.4 ProductDTO.java (für externe Kommunikation)
```java
package at.fhv.e_commerce.order.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProductDTO {
    private String id;
    private String name;
    private BigDecimal price;
    private Integer stock;
}
```

#### 5.5 ProductServiceClient.java (Service-zu-Service Kommunikation)
```java
package at.fhv.e_commerce.order.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import at.fhv.e_commerce.order.dto.ProductDTO;

@Component
public class ProductServiceClient {
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Value("${service.product.url}")
    private String productServiceUrl;
    
    public ProductDTO getProductById(String productId) {
        try {
            String url = productServiceUrl + "/products/" + productId;
            return restTemplate.getForObject(url, ProductDTO.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch product from Product Service", e);
        }
    }
}
```

#### 5.6 OrderService.java (Interface)
```java
package at.fhv.e_commerce.order.service;

import at.fhv.e_commerce.order.dto.CreateOrderDTO;
import at.fhv.e_commerce.order.dto.OrderResponseDTO;
import java.util.List;

public interface OrderService {
    OrderResponseDTO createOrder(CreateOrderDTO dto);
    OrderResponseDTO getOrderById(String id);
    List<OrderResponseDTO> getOrdersByUserId(String userId);
    OrderResponseDTO updateOrderStatus(String id, String status);
    void cancelOrder(String id);
}
```

#### 5.7 OrderServiceImpl.java
```java
package at.fhv.e_commerce.order.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import at.fhv.e_commerce.order.dto.CreateOrderDTO;
import at.fhv.e_commerce.order.dto.OrderItemDTO;
import at.fhv.e_commerce.order.dto.OrderResponseDTO;
import at.fhv.e_commerce.order.dto.ProductDTO;
import at.fhv.e_commerce.order.entity.Order;
import at.fhv.e_commerce.order.entity.OrderItem;
import at.fhv.e_commerce.order.repository.OrderRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private ProductServiceClient productServiceClient;

    @Override
    public OrderResponseDTO createOrder(CreateOrderDTO dto) {
        // Validierung: Produkte abrufen und Verfügbarkeit prüfen
        BigDecimal totalPrice = BigDecimal.ZERO;
        List<OrderItem> items = new ArrayList<>();
        
        for (OrderItemDTO itemDto : dto.getItems()) {
            ProductDTO product = productServiceClient.getProductById(itemDto.getProductId());
            
            if (product == null || product.getStock() < itemDto.getQuantity()) {
                throw new RuntimeException("Product not available or insufficient stock");
            }
            
            OrderItem item = OrderItem.builder()
                .productId(itemDto.getProductId())
                .quantity(itemDto.getQuantity())
                .priceSnapshot(product.getPrice())
                .build();
            
            items.add(item);
            totalPrice = totalPrice.add(product.getPrice().multiply(BigDecimal.valueOf(itemDto.getQuantity())));
        }
        
        Order order = Order.builder()
            .userId(dto.getUserId())
            .status(Order.OrderStatus.PENDING)
            .totalPrice(totalPrice)
            .items(items)
            .build();
        
        Order saved = orderRepository.save(order);
        return mapToDTO(saved);
    }

    @Override
    public OrderResponseDTO getOrderById(String id) {
        Optional<Order> order = orderRepository.findById(id);
        return order.map(this::mapToDTO).orElse(null);
    }

    @Override
    public List<OrderResponseDTO> getOrdersByUserId(String userId) {
        return orderRepository.findByUserId(userId).stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    @Override
    public OrderResponseDTO updateOrderStatus(String id, String status) {
        Optional<Order> order = orderRepository.findById(id);
        if (order.isPresent()) {
            Order o = order.get();
            o.setStatus(Order.OrderStatus.valueOf(status));
            o.setUpdatedAt(LocalDateTime.now());
            orderRepository.save(o);
            return mapToDTO(o);
        }
        return null;
    }

    @Override
    public void cancelOrder(String id) {
        Optional<Order> order = orderRepository.findById(id);
        if (order.isPresent()) {
            Order o = order.get();
            o.setStatus(Order.OrderStatus.CANCELLED);
            o.setUpdatedAt(LocalDateTime.now());
            orderRepository.save(o);
        }
    }

    private OrderResponseDTO mapToDTO(Order order) {
        List<OrderItemDTO> itemDtos = order.getItems().stream()
            .map(item -> OrderItemDTO.builder()
                .productId(item.getProductId())
                .quantity(item.getQuantity())
                .priceSnapshot(item.getPriceSnapshot())
                .build())
            .collect(Collectors.toList());
        
        return OrderResponseDTO.builder()
            .id(order.getId())
            .userId(order.getUserId())
            .status(order.getStatus().toString())
            .totalPrice(order.getTotalPrice())
            .items(itemDtos)
            .createdAt(order.getCreatedAt())
            .updatedAt(order.getUpdatedAt())
            .build();
    }
}
```

#### 5.8 OrderRepository.java
```java
package at.fhv.e_commerce.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import at.fhv.e_commerce.order.entity.Order;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
    List<Order> findByUserId(String userId);
}
```

#### 5.9 OrderRestController.java
```java
package at.fhv.e_commerce.order.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import at.fhv.e_commerce.order.dto.CreateOrderDTO;
import at.fhv.e_commerce.order.dto.OrderResponseDTO;
import at.fhv.e_commerce.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;

@RestController
@RequestMapping("/orders")
@Tag(name = "Order Management", description = "APIs for order management")
public class OrderRestController {

    @Autowired
    private OrderService orderService;

    @PostMapping
    @Operation(summary = "Create a new order")
    public ResponseEntity<OrderResponseDTO> createOrder(@RequestBody CreateOrderDTO dto) {
        OrderResponseDTO result = orderService.createOrder(dto);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID")
    public ResponseEntity<OrderResponseDTO> getOrderById(@PathVariable String id) {
        OrderResponseDTO result = orderService.getOrderById(id);
        if (result != null) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get orders by user ID")
    public ResponseEntity<List<OrderResponseDTO>> getOrdersByUserId(@PathVariable String userId) {
        List<OrderResponseDTO> result = orderService.getOrdersByUserId(userId);
        return ResponseEntity.ok(result);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update order status")
    public ResponseEntity<OrderResponseDTO> updateOrderStatus(
            @PathVariable String id,
            @RequestParam String status) {
        OrderResponseDTO result = orderService.updateOrderStatus(id, status);
        if (result != null) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancel order")
    public ResponseEntity<Void> cancelOrder(@PathVariable String id) {
        orderService.cancelOrder(id);
        return ResponseEntity.noContent().build();
    }
}
```

#### 5.10 application.properties (Order Service)
```properties
server.port=8083
spring.application.name=order-service

# Database Configuration
spring.datasource.url=jdbc:h2:mem:orderdb;DB_CLOSE_DELAY=-1
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false

# Service URLs (intern)
service.product.url=http://localhost:8082

# OpenAPI Configuration
springdoc.api-docs.path=/v3/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
```

---

### **Schritt 6: API Gateway erweitern (Routing & Swagger-UI)**

#### 6.1 Gateway-Controller vervollständigen

Füge ähnliche Controller für User, Order und Cart hinzu:
- `UserController` → routed zu `http://localhost:8081/users`
- `OrderController` → routed zu `http://localhost:8083/orders`
- `CartController` → lokale Implementierung oder Forwarding

#### 6.2 OpenAPI Aggregation (optional, für Unity-UI)

Nutze Springdoc-OpenAPI für die Aggregation:

```gradle
// In api-gateway/build.gradle
dependencies {
    // ... weitere dependencies ...
    implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.0.2'
    // Optional: Für aggregierte API-Dokumentation
    implementation 'org.springdoc:springdoc-openapi-springdoc-openapi-kotlin:2.0.2'
}
```

---

### **Schritt 7: Testen der Microservices**

#### 7.1 Alle Services starten

```bash
# Terminal 1 - API Gateway
cd api-gateway
./gradlew bootRun

# Terminal 2 - User Service
cd user-service
./gradlew bootRun

# Terminal 3 - Product Service
cd product-service
./gradlew bootRun

# Terminal 4 - Order Service
cd order-service
./gradlew bootRun
```

#### 7.2 OpenAPI Dokumentation aufrufen

```
http://localhost:8080/swagger-ui/index.html        (API Gateway)
http://localhost:8081/swagger-ui/index.html        (User Service)
http://localhost:8082/swagger-ui/index.html        (Product Service)
http://localhost:8083/swagger-ui/index.html        (Order Service)
```

#### 7.3 Beispiel-Requests mit cURL

```bash
# Benutzer erstellen
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "password": "password123"
  }'

# Produkt erstellen
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Laptop",
    "description": "High-performance laptop",
    "price": 999.99,
    "stock": 50,
    "category": "Electronics"
  }'

# Bestellung erstellen
curl -X POST http://localhost:8080/api/checkout \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "<user-id>",
    "items": [
      {
        "productId": "<product-id>",
        "quantity": 2
      }
    ]
  }'
```

---

### **Schritt 8: Integration Tests**

#### 8.1 Test-Struktur für Order Service

```java
package at.fhv.e_commerce.order;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import at.fhv.e_commerce.order.dto.CreateOrderDTO;
import at.fhv.e_commerce.order.dto.OrderResponseDTO;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class OrderServiceIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void testCreateOrder() {
        // Arrange
        CreateOrderDTO dto = CreateOrderDTO.builder()
            .userId("test-user")
            .items(List.of())
            .build();

        // Act
        ResponseEntity<OrderResponseDTO> response = restTemplate.postForEntity(
            "/orders", dto, OrderResponseDTO.class);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getId());
    }
}
```

---

### **Schritt 9: Deployment & Packaging**

#### 9.1 Verzeichnisstruktur für Abgabe

```
E-Commerce-Microservices/
├── api-gateway/
│   ├── src/
│   ├── build.gradle
│   └── ...
├── user-service/
│   ├── src/
│   ├── build.gradle
│   └── ...
├── product-service/
│   ├── src/
│   ├── build.gradle
│   └── ...
├── order-service/
│   ├── src/
│   ├── build.gradle
│   └── ...
├── settings.gradle          (Root Gradle Multi-Module)
├── README.md                (Dokumentation)
└── DEPLOYMENT.md            (Anweisungen zum Starten)
```

#### 9.2 Root settings.gradle

```gradle
rootProject.name = 'e-commerce-microservices'

include 'api-gateway'
include 'user-service'
include 'product-service'
include 'order-service'
```

#### 9.3 Als ZIP-Archiv verpacken

```bash
cd E-Commerce-Microservices
7z a -r E-Commerce-Microservices.zip .
# oder
Compress-Archive -Path . -DestinationPath E-Commerce-Microservices.zip
```

---

### **Schritt 10: Dokumentation (README.md)**

```markdown
# E-Commerce Microservices

## Architekturüberblick

Diese Anwendung ist in 4 unabhängige Microservices aufgeteilt:

- **API Gateway** (Port 8080): Single Entry Point für den Benutzer
- **User Service** (Port 8081): Benutzerverwaltung
- **Product Service** (Port 8082): Produktkatalog und Lagerbestände
- **Order Service** (Port 8083): Bestellungsverwaltung und Checkout

## Voraussetzungen

- Java 17+
- Gradle
- H2 Database (eingebettet)

## Starten

### Alle Services mit Gradle starten:
\`\`\`bash
./gradlew bootRun
\`\`\`

### Individuelle Services starten:
\`\`\`bash
# Terminal 1
cd api-gateway && ./gradlew bootRun

# Terminal 2
cd user-service && ./gradlew bootRun

# Terminal 3
cd product-service && ./gradlew bootRun

# Terminal 4
cd order-service && ./gradlew bootRun
\`\`\`

## API-Dokumentation

- **Unified OpenAPI UI**: http://localhost:8080/swagger-ui.html
- **User Service API**: http://localhost:8081/swagger-ui.html
- **Product Service API**: http://localhost:8082/swagger-ui.html
- **Order Service API**: http://localhost:8083/swagger-ui.html

## Technologie-Stack

- Spring Boot 3.2
- Spring Data JPA
- H2 Database
- Springdoc OpenAPI
- Lombok
- Gradle

## DDD-Architektur

Jeder Service ist als unabhängiges Bounded Context implementiert mit:
- Aggregate Roots (User, Product, Order)
- Domain Services
- Application Services
- Repository Pattern
- Data Transfer Objects (DTOs)

## Inter-Service Communication

Services kommunizieren via REST APIs:
- API Gateway orchestriert User-Requests
- Order Service ruft Product Service auf (Verfügbarkeitsprüfung)
- Jeder Service hat eigene Datenbank (H2 in-memory)

## Testen

```bash
./gradlew test
```

## Wichtige Endpunkte

### User Service
- `POST /users` - Neuen Benutzer erstellen
- `GET /users/{id}` - Benutzer abrufen
- `PUT /users/{id}` - Benutzer aktualisieren

### Product Service
- `POST /products` - Neues Produkt erstellen
- `GET /products` - Alle Produkte abrufen
- `GET /products/{id}` - Produkt abrufen
- `PATCH /products/{id}/stock` - Lagerbestand aktualisieren

### Order Service
- `POST /orders` - Neue Bestellung erstellen
- `GET /orders/{id}` - Bestellung abrufen
- `GET /orders/user/{userId}` - Bestellungen eines Benutzers
- `PATCH /orders/{id}/status` - Status aktualisieren
- `DELETE /orders/{id}` - Bestellung stornieren

## Erweiterungsmöglichkeiten

- gRPC für Order-Product-Kommunikation
- Message Broker (Kafka/RabbitMQ) für Events
- Service Discovery (Eureka)
- Load Balancing
- Circuit Breaker (Resilience4j)
- Distributed Tracing (Sleuth)
```

---

## 📊 Checkliste zur Abgabe

- [ ] **4 Spring Boot Projekte** erstellt und funktionsfähig
- [ ] **Eindeutige Ports** für jede Service (8080, 8081, 8082, 8083)
- [ ] **OpenAPI Dokumentation** für alle Services
- [ ] **API Gateway** mit unified UI unter `http://localhost:8080/swagger-ui/index.html`
- [ ] **Jede Service** hat eigene **Persistence Layer** (Datenbank)
- [ ] **Domänenlogik** in jedem Service vorhanden
- [ ] **DDD Struktur** mit Aggregates und IDs für Cross-Service-Referenzen
- [ ] **REST APIs** für alle Services dokumentiert
- [ ] **Inter-Service Communication** implementiert (Order Service ruft Product Service auf)
- [ ] **Test Cases** für kritische Funktionen
- [ ] **README.md** mit Dokumentation und Start-Anweisungen
- [ ] **ZIP-Archiv** vorbereitet und testiert

---

## ⚠️ Wichtige Hinweise

1. **Ports in application.properties nicht vergessen!** Jeder Service braucht einen eindeutigen Port.
2. **Service-URLs hardcodieren oder als Properties**, nicht Eureka/Service Discovery.
3. **DTOs für externe APIs verwenden**, nicht direkte Entity-Objekte.
4. **UUIDs als Primary Keys** für sichere Inter-Service-Referenzen.
5. **Fehlerbehandlung** bei Service-to-Service-Aufrufen (Timeouts, Retry-Logik).
6. **Database Isolation**: Jeder Service hat separate H2-Instanz.
7. **Dokumentation updaten** bei Änderungen an APIs.

---

**Viel Erfolg bei der Implementierung!** 🚀

