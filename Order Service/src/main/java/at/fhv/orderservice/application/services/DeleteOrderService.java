package at.fhv.orderservice.application.services;

import java.util.UUID;

public interface DeleteOrderService {

    void deleteOrderById(UUID orderId);
}
