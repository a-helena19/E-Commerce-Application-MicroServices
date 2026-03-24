package at.fhv.orderservice.messaging;

import at.fhv.orderservice.domain.events.OrderCanceledEvent;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Component
public class OrderEventProducerImpl implements OrderEventProducer {

    private final StreamBridge streamBridge;

    public OrderEventProducerImpl(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    @Override
    public void publishOrderCanceledEvent(OrderCanceledEvent event) {
        Message<OrderCanceledEvent> message = MessageBuilder
                .withPayload(event)
                .setHeader("routingKey", "order.canceled")
                .setHeader("eventType", "OrderCanceledEvent")
                .build();

        streamBridge.send("orderCanceledEventProducer-out-0", message);
    }
}