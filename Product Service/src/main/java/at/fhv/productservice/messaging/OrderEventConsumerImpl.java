package at.fhv.productservice.messaging;

import at.fhv.productservice.domain.model.event.OrderCanceledEvent;
import at.fhv.productservice.domain.model.event.OrderItemEvent;
import at.fhv.productservice.domain.service.ProductService;
import at.fhv.productservice.domain.model.exception.InvalidEventException;
import at.fhv.productservice.domain.model.exception.OrderEventProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.function.Consumer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OrderEventConsumerImpl {
    private static final Logger logger = LoggerFactory.getLogger(OrderEventConsumerImpl.class);
    private final ProductService productService;

    public OrderEventConsumerImpl(ProductService productService) {
        this.productService = productService;
    }

    @Bean
    public Consumer<OrderCanceledEvent> orderCanceledEventConsumer() {
        return event -> {
            try {
                logger.info("Received OrderCanceledEvent: orderId={}, itemCount={}",
                        event.getOrderId(), event.getOrderItems().size());
                validateEvent(event);

                for (OrderItemEvent item : event.getOrderItems()) {
                    logger.info("Restoring stock: productId={}, quantity={}",
                            item.getProductId(), item.getQuantity());
                    productService.restoreStock(item.getProductId(), item.getQuantity());
                }

                logger.info("OrderCanceledEvent processed successfully: orderId={}", event.getOrderId());

            } catch (InvalidEventException e) {
                logger.warn("Invalid event received: {}", e.getMessage());
                throw e;

            } catch (OrderEventProcessingException e) {
                logger.error("Error while processing event: {}", e.getMessage(), e);
                throw e;

            } catch (Exception e) {
                logger.error("Unexpected error while processing event", e);
                throw new OrderEventProcessingException(
                        "Unexpected error while processing order cancellation",
                        event != null ? event.getOrderId().toString() : "UNKNOWN",
                        "Stock restoration failed",
                        e
                );
            }
        };
    }

    private void validateEvent(OrderCanceledEvent event) {
        if (event == null) {
            throw new InvalidEventException("OrderCanceledEvent", "Event is null");
        }

        if (event.getOrderId() == null) {
            throw new InvalidEventException("OrderCanceledEvent", "Order ID is null");
        }

        if (event.getOrderItems() == null || event.getOrderItems().isEmpty()) {
            throw new InvalidEventException("OrderCanceledEvent", "Order items are null or empty");
        }

        for (OrderItemEvent item : event.getOrderItems()) {
            if (item.getProductId() == null) {
                throw new InvalidEventException("OrderCanceledEvent", "Product ID is null");
            }
            if (item.getQuantity() <= 0) {
                throw new InvalidEventException("OrderCanceledEvent", "Quantity must be greater than zero");
            }
        }
        logger.debug("Event validation successful for {} items", event.getOrderItems().size());
    }
}
