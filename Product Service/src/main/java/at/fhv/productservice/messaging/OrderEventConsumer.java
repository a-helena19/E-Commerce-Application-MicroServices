package at.fhv.productservice.messaging;

import at.fhv.productservice.domain.model.event.OrderCanceledEvent;
import at.fhv.productservice.domain.service.ProductService;
import at.fhv.productservice.domain.model.exception.InvalidEventException;
import at.fhv.productservice.domain.model.exception.OrderEventProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.function.Consumer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OrderEventConsumer {
    private static final Logger logger = LoggerFactory.getLogger(OrderEventConsumer.class);
    private final ProductService productService;

    public OrderEventConsumer(ProductService productService) {
        this.productService = productService;
    }

    @Bean
    public Consumer<OrderCanceledEvent> orderCanceledIn() {
        return event -> {
            try {
                logger.info("Received event: {}", event);
                validateEvent(event);
                productService.releaseReservation(event.getProductId(), event.getQuantity());
                logger.info("Reservation released successfully: productId={}, quantity={}", event.getProductId(), event.getQuantity());

            } catch (InvalidEventException e) {
                logger.warn("Invalid event received: {}", e.getMessage());
                throw e;

            } catch (OrderEventProcessingException e) {
                logger.error("Error while processing event: {}", e.getMessage(), e);
                throw e;

            } catch (Exception e) {
                logger.error("Unexpected error while processing event", e);
                throw new OrderEventProcessingException("Unexpected error while processing event", event != null ? event.getOrderId() : "UNKNOWN", "Unknown error type", e
                );
            }
        };
    }

    private void validateEvent(OrderCanceledEvent event) {
        if (event == null) {
            throw new InvalidEventException("OrderCanceledEvent", "Event is null");
        }

        if (event.getOrderId() == null || event.getOrderId().isEmpty()) {
            throw new InvalidEventException("OrderCanceledEvent", "Order ID is null or empty");
        }

        if (event.getProductId() == null || event.getProductId().isEmpty()) {
            throw new InvalidEventException("OrderCanceledEvent", "Product ID is null or empty");
        }

        if (event.getQuantity() == null || event.getQuantity() <= 0) {
            throw new InvalidEventException("OrderCanceledEvent", "Quantity must be greater than zero");
        }
        logger.debug("Event validation successful: orderId={}, productId={}, quantity={}", event.getOrderId(), event.getProductId(), event.getQuantity());
    }
}
