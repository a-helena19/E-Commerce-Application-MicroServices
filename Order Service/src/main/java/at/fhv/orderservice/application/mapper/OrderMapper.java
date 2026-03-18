package at.fhv.orderservice.application.mapper;

import at.fhv.orderservice.domain.model.Order;
import at.fhv.orderservice.domain.model.OrderItem;
import at.fhv.orderservice.infrastructure.persistence.model.OrderEntity;
import at.fhv.orderservice.infrastructure.persistence.model.OrderItemEntity;

public interface OrderMapper {
    Order toDomain(OrderEntity orderEntity);
    OrderEntity toEntity(Order order);
    OrderItem toDomainItem(OrderItemEntity orderItemEntity);
    OrderItemEntity toEntityItem(OrderItem orderItem);
}
