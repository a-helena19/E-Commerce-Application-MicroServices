package at.fhv.orderservice.application.services.impl;

import at.fhv.orderservice.application.services.DeleteOrderService;
import at.fhv.orderservice.domain.events.OrderCanceledEvent;
import at.fhv.orderservice.domain.events.OrderItemEvent;
import at.fhv.orderservice.domain.exception.OrderNotFoundException;
import at.fhv.orderservice.domain.model.Order;
import at.fhv.orderservice.domain.model.OrderRepository;
import at.fhv.orderservice.messaging.OrderEventProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DeleteOrderServiceImpl implements DeleteOrderService {

    private static final Logger logger = LoggerFactory.getLogger(DeleteOrderServiceImpl.class);
    private final OrderRepository orderRepository;
    private final OrderEventProducer orderEventProducer;

    public DeleteOrderServiceImpl(OrderRepository orderRepository, OrderEventProducer orderEventProducer) {
        this.orderRepository = orderRepository;
        this.orderEventProducer = orderEventProducer;
    }

    @Override
    @Transactional
    public void deleteOrderById(UUID orderId) {
        try {
            logger.info("Starting order deletion/cancellation: orderId={}", orderId);

            Order order = orderRepository.getOrderById(orderId);
            if (order == null) {
                logger.warn("Order not found for deletion: orderId={}", orderId);
                throw new OrderNotFoundException(orderId);
            }

            logger.debug("Order found: orderId={}, userId={}, itemCount={}",
                orderId, order.getUserId(), order.getOrderItems().size());

            order.delete();
            orderRepository.save(order);
            logger.debug("Order status updated to DELETED: orderId={}", orderId);

            OrderCanceledEvent event = new OrderCanceledEvent(
                    order.getId(),
                    order.getUserId(),
                    order.getOrderItems().stream()
                            .map(item -> new OrderItemEvent(item.getProductId(), item.getQuantity()))
                            .collect(Collectors.toList())
            );

            logger.debug("OrderCanceledEvent created: orderId={}, items={}",
                orderId, event.getOrderItems().size());

            orderEventProducer.publishOrderCanceledEvent(event);

            logger.info("Order successfully canceled and event published: orderId={}", orderId);

        } catch (OrderNotFoundException e) {
            logger.error("Order not found: orderId={}", orderId);
            throw e;

        } catch (Exception e) {
            logger.error("Error while deleting/canceling order: orderId={}, error={}",
                orderId, e.getMessage(), e);
            throw e;
        }
    }
}
