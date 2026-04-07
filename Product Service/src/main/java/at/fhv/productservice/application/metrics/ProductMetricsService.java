package at.fhv.productservice.application.metrics;

import at.fhv.productservice.domain.model.Product;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class ProductMetricsService {
    private final Counter productsViewedCounter;
    private final AtomicInteger currentStockLevel = new AtomicInteger(0);

    public ProductMetricsService(MeterRegistry registry) {
        this.productsViewedCounter = Counter.builder("products_viewed_total")
                .description("Total number of product detail views")
                .register(registry);

        Gauge.builder("product_stock_level", currentStockLevel, AtomicInteger::get)
                .description("Current total stock level across all products")
                .register(registry);
    }

    public void incrementProductsViewed() {
        productsViewedCounter.increment();
    }

    public void updateStockLevel(int total) {
        currentStockLevel.set(total);
    }

}
