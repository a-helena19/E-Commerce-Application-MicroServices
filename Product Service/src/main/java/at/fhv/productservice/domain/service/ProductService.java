package at.fhv.productservice.domain.service;

import at.fhv.productservice.domain.model.Product;
import at.fhv.productservice.domain.model.ProductRepository;
import at.fhv.productservice.domain.model.exception.InvalidProductDataException;
import at.fhv.productservice.domain.model.exception.ProductNotFoundException;
import at.fhv.productservice.domain.model.exception.ProductReservationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ProductService {
    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public void restoreStock(UUID productId, Integer quantity) {
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

        } catch (ProductReservationException e) {
            logger.error("Error while updating reservation: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error while updating reservation: productId={}, error={}", productId, e.getMessage(), e);
            throw new ProductReservationException("Failed to release reservation", productId.toString(), quantity, e);
        }
    }

    private void validateInput(UUID productId, Integer quantity) {
        if (productId == null) {
            logger.debug("Validation failed: productId is null or empty");
            throw new InvalidProductDataException("productId", productId, "must not be null or empty");
        }

        if (quantity == null || quantity <= 0) {
            logger.debug("Validation failed: invalid quantity={}", quantity);
            throw new InvalidProductDataException("quantity", quantity, "must be greater than zero");
        }

        logger.debug("Input validation successful");
    }
}