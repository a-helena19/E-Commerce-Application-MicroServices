package at.fhv.productservice.domain.service;

import at.fhv.productservice.domain.model.exception.InvalidProductDataException;
import at.fhv.productservice.domain.model.exception.ProductReservationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ProductService {
    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    public void releaseReservation(String productId, Integer quantity) {
        logger.info("Starting reservation release: productId={}, quantity={}", productId, quantity);
        validateInput(productId, quantity);

        try {
            logger.debug("Reservation released: productId={}, quantity={}", productId, quantity);
            logger.info("Product availability updated: productId={}", productId);

        } catch (Exception e) {
            logger.error("Error while updating reservation", e);
            throw new ProductReservationException("Failed to release reservation", productId, quantity, e
            );
        }
    }

    private void validateInput(String productId, Integer quantity) {
        if (productId == null || productId.isBlank()) {
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
