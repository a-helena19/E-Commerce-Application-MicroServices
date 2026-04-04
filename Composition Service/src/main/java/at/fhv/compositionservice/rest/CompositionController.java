package at.fhv.compositionservice.rest;

import at.fhv.compositionservice.application.services.CompositionService;
import at.fhv.compositionservice.rest.dtos.OrderDetailsDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

@Slf4j
@Controller
@RequiredArgsConstructor
public class CompositionController {

    private final CompositionService compositionService;

    @QueryMapping
    public Mono<OrderDetailsDTO> orderDetails(@Argument String orderId) {
        log.info("GraphQL Query: orderDetails(orderId={})", orderId);
        return compositionService.getOrderDetails(orderId)
                .doOnError(e -> log.error("Error in GraphQL orderDetails query: {}", e.getMessage(), e));
    }
}