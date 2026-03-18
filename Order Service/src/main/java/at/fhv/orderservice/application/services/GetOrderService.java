package at.fhv.orderservice.application.services;

import at.fhv.orderservice.rest.dtos.GetOrderDTO;

import java.util.List;
import java.util.UUID;

public interface GetOrderService {
    GetOrderDTO getOrderById(UUID orderId);

    List<GetOrderDTO> getOrdersByUserId(UUID userId);

    List<GetOrderDTO> getAllOrders();
}
