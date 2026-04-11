package at.fhv.productservice.application.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;

@Service
public class ProductMetricsService {
    private final Counter productsAddedCounter;
    private final AtomicInteger currentStockLevel = new AtomicInteger(0);

    public ProductMetricsService(MeterRegistry registry) {
        this.productsAddedCounter = Counter.builder("products_added_total")
                .description("Total number of products created")
                .register(registry);

        Gauge.builder("product_stock_level", currentStockLevel, AtomicInteger::get)
                .description("Current total stock level across all products")
                .register(registry);
    }

    public void incrementProductsAdded() {
        productsAddedCounter.increment();
    }

    public void updateStockLevel(int total) {
        currentStockLevel.set(total);
    }

}
