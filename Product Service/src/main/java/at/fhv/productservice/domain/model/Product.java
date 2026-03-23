package at.fhv.productservice.domain.model;

import at.fhv.productservice.domain.model.exception.InvalidProductDataException;
import at.fhv.productservice.domain.model.exception.ProductOutOfStockException;

import java.math.BigDecimal;
import java.util.UUID;

public class Product {
    private UUID id;
    private String name;
    private String description;
    private BigDecimal price;
    private int stock;
    private ProductStatus status;

    private static final int MINIMUM_NAME_LENGTH = 3;
    private static final int MAXIMUM_NAME_LENGTH = 20;
    private static final int MAXIMUM_DESCRIPTION_LENGTH = 200;
    private static final String NAME_REGEX = "^[\\p{L} ,.'-]+$";
    private static final String DESCRIPTION_REGEX = "^[\\p{L}0-9 ,.!?'()%-]*$";

    private Product(UUID id, String name, String description, BigDecimal price, int stock, ProductStatus status) {
        validateName(name);
        validateDescription(description);
        validatePrice(price);
        validateStock(stock);

        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.status = status;
    }

    public static Product reconstruct(UUID id, String name, String description, BigDecimal price, int stock, ProductStatus status) {
        return new Product(id, name, description, price, stock, status);
    }

    public static Product create(UUID id, String name, String description, BigDecimal price, int stock) {
        return new Product(id, name, description, price, stock, ProductStatus.ACTIVE);
    }



    public void update(String name, String description, BigDecimal price, int stock) {
        if (status == ProductStatus.INACTIVE) {
            throw new InvalidProductDataException("status", status, "Can't update an inactive product.");
        }

        validateName(name);
        validateDescription(description);
        validatePrice(price);
        validateStock(stock);
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
    }

    public void delete() {
        status = ProductStatus.INACTIVE;
    }

    public boolean isAvailable() {
        return stock > 0 && status == ProductStatus.ACTIVE;
    }

    public void reduceStock(int quantity) {
        if (quantity <= 0) {
            throw new InvalidProductDataException("quantity", quantity, "Quantity must be greater than zero.");
        }
        if (quantity > stock) {
            throw new ProductOutOfStockException(id, quantity, stock);
        }
        stock -= quantity;
    }

    public void increaseStock(int quantity) {
        if (quantity <= 0) {
            throw new InvalidProductDataException("quantity", quantity, "Quantity must be greater than zero.");
        }
        stock += quantity;
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidProductDataException("name", name, "Name can't be null or blank.");
        }

        if (name.length() < MINIMUM_NAME_LENGTH || name.length() > MAXIMUM_NAME_LENGTH) {
            throw new InvalidProductDataException("name", name, "Name must be between " + MINIMUM_NAME_LENGTH + " and " + MAXIMUM_NAME_LENGTH + " characters long."
            );
        }

        if (!name.matches(NAME_REGEX)) {
            throw new InvalidProductDataException("name", name, "Name contains invalid characters."
            );
        }
    }

    private void validateDescription(String description) {
        if (description == null) {
            return;
        }
        if (description.length() > MAXIMUM_DESCRIPTION_LENGTH) {
            throw new InvalidProductDataException("description", description, "Description must be at most " + MAXIMUM_DESCRIPTION_LENGTH + " characters long.");
        }
        if (!description.matches(DESCRIPTION_REGEX)) {
            throw new InvalidProductDataException("description", description, "Description contains invalid characters.");
        }
    }

    private void validatePrice(BigDecimal price) {
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidProductDataException("price", price, "Price can't be null or negative.");
        }
        if (price.scale() > 2) {
            throw new InvalidProductDataException("price", price, "Price must have at most 2 decimal places.");
        }
    }

    private void validateStock(int stock) {
        if (stock < 0) {
            throw new InvalidProductDataException("stock", stock, "Stock can't be negative.");
        }
    }

    public String getDescription() {
        return description;
    }

    public ProductStatus getStatus() {
        return status;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public int getStock() {
        return stock;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
