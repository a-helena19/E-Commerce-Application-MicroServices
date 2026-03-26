package at.fhv.productservice.application.services;

import java.util.UUID;

public interface RestoreStockService {
    void restoreStock(UUID productId, Integer quantity);

}