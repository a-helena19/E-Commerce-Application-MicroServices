package at.fhv.orderservice.messaging;

import at.fhv.orderservice.domain.events.OrderCanceledEvent;

public interface OrderEventProducer  {
    void publishOrderCanceledEvent(OrderCanceledEvent event);
}
