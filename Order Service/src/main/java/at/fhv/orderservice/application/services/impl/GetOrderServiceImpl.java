package at.fhv.orderservice.application.services.impl;

import at.fhv.orderservice.application.mapper.dtoMapper.OrderDTOMapper;
import at.fhv.orderservice.application.services.GetOrderService;
import at.fhv.orderservice.domain.exception.OrderNotFoundException;
import at.fhv.orderservice.domain.model.Order;
import at.fhv.orderservice.domain.model.OrderRepository;
import at.fhv.orderservice.rest.dtos.GetOrderDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class GetOrderServiceImpl implements GetOrderService {

    private final OrderRepository orderRepository;
    private final OrderDTOMapper orderDTOMapper;

    public GetOrderServiceImpl(OrderRepository orderRepository, OrderDTOMapper orderDTOMapper) {
        this.orderRepository = orderRepository;
        this.orderDTOMapper = orderDTOMapper;
    }

    @Override
    public GetOrderDTO getOrderById(UUID orderId) {
        Order order = orderRepository.getOrderById(orderId);
        if (order == null) {
            throw new OrderNotFoundException(orderId);
        }
        return orderDTOMapper.toGetOrderDTO(order);
    }

    @Override
    public List<GetOrderDTO> getOrdersByUserId(UUID userId) {
        return orderRepository.getOrdersByUserId(userId)
                .stream()
                .map(orderDTOMapper::toGetOrderDTO)
                .toList();
    }

    @Override
    public List<GetOrderDTO> getAllOrders() {
        return orderRepository.getAllOrders()
                .stream()
                .map(orderDTOMapper::toGetOrderDTO)
                .toList();
    }
}
