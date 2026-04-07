package at.fhv.productservice.application.impl;

import at.fhv.productservice.application.metrics.ProductMetricsService;
import at.fhv.productservice.application.services.RestoreStockService;
import at.fhv.productservice.domain.model.Product;
import at.fhv.productservice.domain.model.ProductRepository;
import at.fhv.productservice.domain.model.exception.InvalidProductDataException;
import at.fhv.productservice.domain.model.exception.ProductNotFoundException;
import at.fhv.productservice.domain.model.exception.ProductReservationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class RestoreStockServiceImpl implements RestoreStockService {
    private static final Logger logger = LoggerFactory.getLogger(RestoreStockServiceImpl.class);
    private final ProductRepository productRepository;
    private final ProductMetricsService productMetricsService;

    public RestoreStockServiceImpl(ProductRepository productRepository, ProductMetricsService productMetricsService) {
        this.productRepository = productRepository;
        this.productMetricsService = productMetricsService;
    }

    @Override
    @Transactional
    public void restoreStock(UUID productId, int quantity) {
        logger.info("Starting reservation release: productId={}, quantity={}", productId, quantity);
        validateInput(productId, quantity);

        try {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> {
                        logger.error("Product not found: productId={}", productId);
                        return new ProductReservationException("Product not found", productId.toString(), quantity, new ProductNotFoundException(productId)
                        );
                    });

            logger.debug("Found product: productId={}, currentStock={}", productId, product.getStock());
            product.increaseStock(quantity);
            logger.debug("Stock increased: productId={}, quantity={}, newStock={}", productId, quantity, product.getStock());
            productRepository.save(product);
            logger.info("Product availability updated: productId={}, newStock={}", productId, product.getStock());
            int totalStock = productRepository.findAll().stream().mapToInt(Product::getStock).sum();
            productMetricsService.updateStockLevel(totalStock);

        } catch (ProductReservationException e) {
            logger.error("Error while updating reservation: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error while updating reservation: productId={}, error={}", productId, e.getMessage(), e);
            throw new ProductReservationException("Failed to release reservation", productId.toString(), quantity, e);
        }
    }

    private void validateInput(UUID productId, int quantity) {
        if (productId == null) {
            logger.debug("Validation failed: productId is null or empty");
            throw new InvalidProductDataException("productId", productId, "must not be null or empty");
        }

        if (quantity <= 0) {
            logger.debug("Validation failed: invalid quantity={}", quantity);
            throw new InvalidProductDataException("quantity", quantity, "must be greater than zero");
        }

        logger.debug("Input validation successful");
    }
}
