package at.fhv.orderservice.application.mapper.dtoMapper;

import at.fhv.orderservice.domain.model.Order;
import at.fhv.orderservice.domain.model.OrderItem;
import at.fhv.orderservice.rest.dtos.GetOrderDTO;
import at.fhv.orderservice.rest.dtos.GetOrderItemDTO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderDTOMapperImpl implements OrderDTOMapper {
    @Override
    public GetOrderDTO toGetOrderDTO(Order order) {
        List<GetOrderItemDTO> items = order.getOrderItems()
                .stream()
                .map(this::toGetOrderItemDTO)
                .toList();

        return new GetOrderDTO(
                order.getId(),
                order.getUserId(),
                order.getStatus().name(),
                items,
                order.getTotalPrice(),
                order.getOrderDate()
        );
    }


    private GetOrderItemDTO toGetOrderItemDTO(OrderItem item) {
        return new GetOrderItemDTO(
                item.getId(),
                item.getProductId(),
                item.getQuantity(),
                item.getPrice()
        );
    }
}
