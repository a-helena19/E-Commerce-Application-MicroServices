package at.fhv.api_gateway.application.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RestClientConfig {

    @Value("${service.user.url}")
    public String userServiceUrl;

    @Value("${service.product.url}")
    public String productServiceUrl;

    @Value("${service.order.url}")
    public String orderServiceUrl;

    @Value("${service.cart.url}")
    public String cartServiceUrl;


}
