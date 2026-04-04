package at.fhv.compositionservice.application.services;

import at.fhv.compositionservice.rest.client.OrderServiceClient;
import at.fhv.compositionservice.rest.client.ProductServiceClient;
import at.fhv.compositionservice.rest.client.UserServiceClient;
import at.fhv.compositionservice.rest.dtos.CustomerDTO;
import at.fhv.compositionservice.rest.dtos.OrderDetailsDTO;
import at.fhv.compositionservice.rest.dtos.OrderItemDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class CompositionService {

    private final OrderServiceClient orderServiceClient;
    private final ProductServiceClient productServiceClient;
    private final UserServiceClient userServiceClient;

    public CompositionService(OrderServiceClient orderServiceClient,
                              ProductServiceClient productServiceClient,
                              UserServiceClient userServiceClient) {
        this.orderServiceClient = orderServiceClient;
        this.productServiceClient = productServiceClient;
        this.userServiceClient = userServiceClient;
    }

    public Mono<OrderDetailsDTO> getOrderDetails(String orderId) {
        log.info("=== START: Composing order details for orderId: {} ===", orderId);

        return orderServiceClient.getOrder(orderId)
                .flatMap(remoteOrder -> {
                    log.info("Order fetched successfully: {}", remoteOrder.getId());
                    log.info("Step 2: Fetching user data...");

                    return userServiceClient.getUser(remoteOrder.getUserId())
                            .flatMap(remoteUser -> {
                                log.info("User fetched successfully: {}", remoteUser.getId());

                                CustomerDTO customer = new CustomerDTO(
                                        remoteUser.getId(),
                                        remoteUser.getFullName(),
                                        remoteUser.getEmail(),
                                        remoteUser.getAddress()
                                );

                                log.info("Step 3: Fetching product data for {} items...",
                                        remoteOrder.getItems().size());

                                // Fetch all products in parallel
                                return Flux.fromIterable(remoteOrder.getItems())
                                        .flatMap(remoteItem -> {
                                            log.debug("Fetching product for item: {}",
                                                    remoteItem.getProductId());
                                            return productServiceClient.getProduct(remoteItem.getProductId())
                                                    .map(product -> {
                                                        Double unitPrice = remoteItem.getUnitPrice() != null ?
                                                            remoteItem.getUnitPrice() : 0.0;
                                                        double totalPrice = remoteItem.getQuantity() * unitPrice;

                                                        return new OrderItemDTO(
                                                                product.getId(),
                                                                product.getName(),
                                                                product.getDescription(),
                                                                remoteItem.getQuantity(),
                                                                unitPrice,
                                                                totalPrice
                                                        );
                                                    });
                                        })
                                        .collectList()
                                        .map(items -> {
                                            log.info("Products fetched successfully: {} items", items.size());

                                            OrderDetailsDTO orderDetails = new OrderDetailsDTO(
                                                    remoteOrder.getId(),
                                                    remoteOrder.getStatus(),
                                                    remoteOrder.getTotalPrice(),
                                                    remoteOrder.getCreatedAt(),
                                                    customer,
                                                    items
                                            );

                                            log.info("=== END: Order composition completed successfully ===");
                                            return orderDetails;
                                        });
                            });
                })
                .doOnError(e -> log.error("Error during order composition: {}", e.getMessage(), e))
                .onErrorMap(e -> new RuntimeException("Failed to compose order details for ID: " + orderId, e));
    }
}