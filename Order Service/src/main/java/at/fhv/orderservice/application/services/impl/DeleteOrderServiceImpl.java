package at.fhv.orderservice.application.services.impl;

import at.fhv.orderservice.application.services.DeleteOrderService;
import at.fhv.orderservice.domain.events.OrderCanceledEvent;
import at.fhv.orderservice.domain.events.OrderItemEvent;
import at.fhv.orderservice.domain.exception.OrderNotFoundException;
import at.fhv.orderservice.domain.model.Order;
import at.fhv.orderservice.domain.model.OrderItem;
import at.fhv.orderservice.domain.model.OrderRepository;
import at.fhv.orderservice.messaging.OrderEventProducer;
import at.fhv.orderservice.rest.client.ProductServiceClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DeleteOrderServiceImpl implements DeleteOrderService {

    private final OrderRepository orderRepository;
    private final ProductServiceClient productServiceClient;
    private final OrderEventProducer orderEventProducer;

    public DeleteOrderServiceImpl(OrderRepository orderRepository, ProductServiceClient productServiceClient
                                    , OrderEventProducer orderEventProducer) {
        this.orderRepository = orderRepository;
        this.productServiceClient = productServiceClient;
        this.orderEventProducer = orderEventProducer;
    }

    @Override
    @Transactional
    public void deleteOrderById(UUID orderId) {
        Order order = orderRepository.getOrderById(orderId);
        if (order == null) {
            throw new OrderNotFoundException(orderId);
        }

        // Restore stock for all products in the order by calling Product Service
        for (OrderItem item : order.getOrderItems()) {
            try {
                productServiceClient.restoreStock(item.getProductId(), item.getQuantity());
            } catch (Exception e) {
                throw new RuntimeException("Failed to restore stock for product " + item.getProductId() + ": " + e.getMessage(), e);
            }
        }

        order.delete();
        orderRepository.save(order);

        OrderCanceledEvent event = new OrderCanceledEvent(
                order.getId(),
                order.getUserId(),
                order.getOrderItems().stream()
                        .map(item -> new OrderItemEvent(item.getProductId(), item.getQuantity()))
                        .collect(Collectors.toList())
        );

        orderEventProducer.publishOrderCanceledEvent(event);
    }
}
