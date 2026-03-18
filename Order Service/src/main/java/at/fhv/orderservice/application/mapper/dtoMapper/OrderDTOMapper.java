package at.fhv.orderservice.application.mapper.dtoMapper;

import at.fhv.orderservice.domain.model.Order;
import at.fhv.orderservice.rest.dtos.GetOrderDTO;

public interface OrderDTOMapper {
    GetOrderDTO toGetOrderDTO(Order order);
}
