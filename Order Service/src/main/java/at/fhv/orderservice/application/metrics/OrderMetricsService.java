package at.fhv.orderservice.application.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

@Service
public class OrderMetricsService {

    private final Counter ordersCreatedCounter;
    private final Counter ordersCancelledCounter;

    public OrderMetricsService(MeterRegistry registry) {
        this.ordersCreatedCounter = Counter.builder("orders_created_total")
                .description("Total number of orders created")
                .register(registry);

        this.ordersCancelledCounter = Counter.builder("orders_cancelled_total")
                .description("Total number of orders cancelled")
                .register(registry);
    }

    public void incrementOrdersCreated() {
        ordersCreatedCounter.increment();
    }

    public void incrementOrdersCancelled() {
        ordersCancelledCounter.increment();
    }
}