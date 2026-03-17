package at.fhv.orderservice.application.services;

import at.fhv.orderservice.rest.dtos.CreateOrderDTO;
import at.fhv.orderservice.rest.dtos.GetOrderDTO;

public interface CreateOrderService {
    GetOrderDTO createOrder(CreateOrderDTO dto);
}
