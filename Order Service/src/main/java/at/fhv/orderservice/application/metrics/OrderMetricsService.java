package at.fhv.orderservice.application.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

@Service
public class OrderMetricsService {

    private final Counter ordersCreatedCounter;
    private final Counter ordersCancelledCounter;

    public OrderMetricsService(MeterRegistry registry) {
        this.ordersCreatedCounter = Counter.builder("orders_added")
                .description("Total number of orders created")
                .register(registry);

        this.ordersCancelledCounter = Counter.builder("orders_cancelled")
                .description("Total number of orders cancelled")
                .register(registry);

        ordersCreatedCounter.increment(0);
        ordersCancelledCounter.increment(0);
    }

    public void incrementOrdersCreated() {
        ordersCreatedCounter.increment();
    }

    public void incrementOrdersCancelled() {
        ordersCancelledCounter.increment();
    }
}