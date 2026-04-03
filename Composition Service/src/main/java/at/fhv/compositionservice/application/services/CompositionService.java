package at.fhv.compositionservice.application.services;

import at.fhv.compositionservice.rest.client.OrderServiceClient;
import at.fhv.compositionservice.rest.client.ProductServiceClient;
import at.fhv.compositionservice.rest.client.UserServiceClient;
import at.fhv.compositionservice.rest.client.dtos.RemoteOrderDTO;
import at.fhv.compositionservice.rest.client.dtos.RemoteProductDTO;
import at.fhv.compositionservice.rest.client.dtos.RemoteUserDTO;
import at.fhv.compositionservice.rest.dtos.CustomerDTO;
import at.fhv.compositionservice.rest.dtos.OrderDetailsDTO;
import at.fhv.compositionservice.rest.dtos.OrderItemDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CompositionService {

    private final OrderServiceClient orderServiceClient;
    private final ProductServiceClient productServiceClient;
    private final UserServiceClient userServiceClient;

    public CompositionService(OrderServiceClient orderServiceClient, ProductServiceClient productServiceClient, UserServiceClient userServiceClient) {
        this.orderServiceClient = orderServiceClient;
        this.productServiceClient = productServiceClient;
        this.userServiceClient = userServiceClient;
    }

    public OrderDetailsDTO getOrderDetails(String orderId) {
        log.info("=== START: Composing order details for orderId: {} ===", orderId);

        try {
            log.info("Step 1: Fetching order data...");
            RemoteOrderDTO remoteOrder = orderServiceClient.getOrder(orderId);
            log.info("Order fetched successfully: {}", remoteOrder.getId());

            log.info("Step 2: Fetching user data...");
            RemoteUserDTO remoteUser = userServiceClient.getUser(remoteOrder.getUserId());
            log.info("User fetched successfully: {}", remoteUser.getId());

            CustomerDTO customer = new CustomerDTO(
                    remoteUser.getId(),
                    remoteUser.getFullName(),
                    remoteUser.getEmail(),
                    remoteUser.getAddress()
            );

            log.info("Step 3: Fetching product data for {} items...", remoteOrder.getItems().size());
            List<OrderItemDTO> items = remoteOrder.getItems().stream()
                    .map(remoteItem -> {
                        log.debug("Fetching product for item: {}", remoteItem.getProductId());
                        RemoteProductDTO product = productServiceClient.getProduct(remoteItem.getProductId());

                        double totalPrice = remoteItem.getQuantity() * remoteItem.getUnitPrice();

                        return new OrderItemDTO(
                                product.getId(),
                                product.getName(),
                                product.getDescription(),
                                remoteItem.getQuantity(),
                                remoteItem.getUnitPrice(),
                                totalPrice
                        );
                    })
                    .collect(Collectors.toList());

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

        } catch (Exception e) {
            log.error("Error during order composition: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to compose order details for ID: " + orderId, e);
        }
    }

}