package at.fhv.compositionservice.rest;

import at.fhv.compositionservice.application.services.CompositionService;
import at.fhv.compositionservice.rest.dtos.OrderDetailsDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
public class CompositionController {

    private final CompositionService compositionService;

    public CompositionController(CompositionService compositionService) {
        this.compositionService = compositionService;
    }

    @QueryMapping
    public OrderDetailsDTO orderDetails(@Argument String orderId) {
        log.info("GraphQL Query: orderDetails(orderId={})", orderId);
        try {
            return compositionService.getOrderDetails(orderId);
        } catch (Exception e) {
            log.error("Error in GraphQL orderDetails query: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch order details: " + e.getMessage());
        }
    }


}