package at.fhv.orderservice.domain.model;

import java.util.List;
import java.util.UUID;

public interface OrderRepository {
    Order save(Order order);
    Order getOrderById(UUID orderId);
    List<Order> getOrdersByUserId(UUID userId);
    List<Order> getAllOrders();
}
