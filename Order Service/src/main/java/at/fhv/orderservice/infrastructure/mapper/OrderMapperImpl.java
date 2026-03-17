package at.fhv.orderservice.infrastructure.mapper;

import at.fhv.orderservice.application.mapper.OrderMapper;
import at.fhv.orderservice.domain.model.Order;
import at.fhv.orderservice.domain.model.OrderItem;
import at.fhv.orderservice.domain.model.OrderStatus;
import at.fhv.orderservice.infrastructure.persistence.model.OrderEntity;
import at.fhv.orderservice.infrastructure.persistence.model.OrderItemEntity;
import at.fhv.orderservice.infrastructure.persistence.model.OrderStatusEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderMapperImpl implements OrderMapper {

    @Override
    public Order toDomain(OrderEntity orderEntity) {
        List<OrderItem> items = orderEntity.getOrderItems()
                .stream()
                .map(this::toDomainItem)
                .toList();

        return new Order(
                orderEntity.getId(),
                orderEntity.getUserId(),
                OrderStatus.valueOf(orderEntity.getStatus().name()),
                items,
                orderEntity.getTotalPrice(),
                orderEntity.getOrderDate()
        );
    }

    @Override
    public OrderEntity toEntity(Order order) {
            List<OrderItemEntity> itemEntities = order.getOrderItems()
                    .stream()
                    .map(this::toEntityItem)
                    .toList();

            OrderEntity orderEntity = new OrderEntity(
                    order.getId(),
                    order.getUserId(),
                    OrderStatusEntity.valueOf(order.getStatus().name()),
                    itemEntities,
                    order.getTotalPrice(),
                    order.getOrderDate()
            );

            for (OrderItemEntity item : itemEntities) {
                item.setOrder(orderEntity);
            }

            return orderEntity;
    }

    @Override
    public OrderItem toDomainItem(OrderItemEntity orderItemEntity) {
        return new OrderItem(

                orderItemEntity.getId(),
                orderItemEntity.getProductId(),
                orderItemEntity.getQuantity(),
                orderItemEntity.getPrice()
        );
    }

    @Override
    public OrderItemEntity toEntityItem(OrderItem orderItem) {
        return new OrderItemEntity(
                orderItem.getId(),
                orderItem.getProductId(),
                orderItem.getQuantity(),
                orderItem.getPrice()
        );
    }
}
