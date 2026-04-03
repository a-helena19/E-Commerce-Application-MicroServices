package at.fhv.springcloudgateway.rest;

import at.fhv.springcloudgateway.rest.dtos.GatewayInfo;
import at.fhv.springcloudgateway.rest.dtos.HealthResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/gateway")
public class HealthRestController {

    @GetMapping("/health")
    public HealthResponse getHealth() {
        return new HealthResponse("Gateway is healthy", "UP");
    }

    @GetMapping("/info")
    public GatewayInfo getInfo() {
        return new GatewayInfo(
                "Spring Cloud Gateway",
                "8090",
                "Routes requests to microservices using Eureka service discovery",
                "1.0.0"
        );
    }
}
