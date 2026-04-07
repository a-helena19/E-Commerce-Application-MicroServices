package at.fhv.orderservice.application.services.impl;

import at.fhv.orderservice.application.mapper.dtoMapper.OrderDTOMapper;
import at.fhv.orderservice.application.metrics.OrderMetricsService;
import at.fhv.orderservice.application.services.CreateOrderService;
import at.fhv.orderservice.domain.model.Order;
import at.fhv.orderservice.domain.model.OrderItem;
import at.fhv.orderservice.domain.model.OrderRepository;
import at.fhv.orderservice.rest.client.CartServiceClient;
import at.fhv.orderservice.rest.client.ProductServiceClient;
import at.fhv.orderservice.rest.client.GetCartResponseDTO;
import at.fhv.orderservice.rest.client.GetProductResponseDTO;
import at.fhv.orderservice.rest.dtos.CreateOrderDTO;
import at.fhv.orderservice.rest.dtos.GetOrderDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
public class CreateOrderServiceImpl implements CreateOrderService {
    private final OrderRepository orderRepository;
    private final CartServiceClient cartServiceClient;
    private final ProductServiceClient productServiceClient;
    private final OrderDTOMapper orderDTOMapper;
    private final OrderMetricsService orderMetricsService;

    public CreateOrderServiceImpl(OrderRepository orderRepository, CartServiceClient cartServiceClient,
                                ProductServiceClient productServiceClient, OrderDTOMapper orderDTOMapper,
                                OrderMetricsService orderMetricsService) {
        this.orderRepository = orderRepository;
        this.cartServiceClient = cartServiceClient;
        this.productServiceClient = productServiceClient;
        this.orderDTOMapper = orderDTOMapper;
        this.orderMetricsService = orderMetricsService;
    }

    @Override
    @Transactional
    public GetOrderDTO createOrder(CreateOrderDTO dto) {
        if (dto == null || dto.getUserId() == null) {
            throw new RuntimeException("User ID cannot be null");
        }

        // Get cart from Cart Service
        GetCartResponseDTO cart = cartServiceClient.getCartByUserId(dto.getUserId());
        if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty. Add items to cart before placing an order.");
        }

        if ("INACTIVE".equalsIgnoreCase(cart.getStatus())) {
            throw new RuntimeException("Cart is inactive. Cannot place order.");
        }

        // Validate stock availability for all products
        Map<UUID, Integer> productQuantities = cart.getItemsAsMap();
        Map<UUID, GetProductResponseDTO> products = new HashMap<>();

        for (Map.Entry<UUID, Integer> entry : productQuantities.entrySet()) {
            UUID productId = entry.getKey();
            int quantity = entry.getValue();

            GetProductResponseDTO product = productServiceClient.getProductById(productId);

            if (!"ACTIVE".equalsIgnoreCase(product.getStatus())) {
                throw new RuntimeException("Product with ID " + productId + " is not available for ordering");
            }

            if (product.getStock() < quantity) {
                throw new RuntimeException("Product out of stock. Requested: " + quantity + ", Available: " + product.getStock());
            }

            products.put(productId, product);
        }

        // Create order items from cart
        List<OrderItem> orderItems = new ArrayList<>();
        for (Map.Entry<UUID, Integer> entry : productQuantities.entrySet()) {
            GetProductResponseDTO product = products.get(entry.getKey());
            BigDecimal unitPrice = BigDecimal.valueOf(product.getPrice()).setScale(2, RoundingMode.HALF_UP);
            OrderItem orderItem = new OrderItem(null, entry.getKey(), entry.getValue(), unitPrice);
            orderItems.add(orderItem);
        }

        // Create and save order
        Order order = Order.create(dto.getUserId(), orderItems);
        Order savedOrder = orderRepository.save(order);

        // Reduce stock for all products in the order (call Product Service)
        for (Map.Entry<UUID, Integer> entry : productQuantities.entrySet()) {
            try {
                productServiceClient.reduceStock(entry.getKey(), entry.getValue());
            } catch (Exception e) {
                throw new RuntimeException("Failed to reduce stock for product " + entry.getKey() + ": " + e.getMessage(), e);
            }
        }

        // Clear cart after successful order creation (call Cart Service)
        cartServiceClient.clearCartByUserId(dto.getUserId());

        orderMetricsService.incrementOrdersCreated();

        return orderDTOMapper.toGetOrderDTO(savedOrder);
    }
}
