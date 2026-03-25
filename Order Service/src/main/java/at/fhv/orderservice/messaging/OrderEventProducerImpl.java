package at.fhv.orderservice.messaging;

import at.fhv.orderservice.domain.events.OrderCanceledEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Component
public class OrderEventProducerImpl implements OrderEventProducer {

    private static final Logger logger = LoggerFactory.getLogger(OrderEventProducerImpl.class);
    private final StreamBridge streamBridge;

    public OrderEventProducerImpl(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    @Override
    public void publishOrderCanceledEvent(OrderCanceledEvent event) {
        try {
            logger.info("Publishing OrderCanceledEvent: orderId={}, userId={}, itemCount={}",
                event.getOrderId(), event.getUserId(), event.getOrderItems().size());

            logger.debug("OrderCanceledEvent details: items={}", event.getOrderItems());

            sendViaStreamBridge(event);

        } catch (Exception e) {
            logger.error("Failed to publish OrderCanceledEvent: orderId={}, error={}",
                event.getOrderId(), e.getMessage(), e);
            throw e;
        }
    }

    private void sendViaStreamBridge(OrderCanceledEvent event) {
        try {
            logger.debug("Sending OrderCanceledEvent via StreamBridge: orderId={}", event.getOrderId());

            Message<OrderCanceledEvent> message = MessageBuilder
                    .withPayload(event)
                    .setHeader("routingKey", "order.canceled")
                    .setHeader("eventType", "OrderCanceledEvent")
                    .build();

            streamBridge.send("orderCanceledEventProducer-out-0", message);

            logger.info("OrderCanceledEvent sent successfully via StreamBridge: orderId={}", event.getOrderId());

        } catch (Exception e) {
            logger.error("Failed to send OrderCanceledEvent via StreamBridge: orderId={}, error={}",
                event.getOrderId(), e.getMessage(), e);
            throw e;
        }
    }
}

