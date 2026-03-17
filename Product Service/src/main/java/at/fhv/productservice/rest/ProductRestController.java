package at.fhv.productservice.rest;


import at.fhv.productservice.application.services.CreateProductService;
import at.fhv.productservice.application.services.DeleteProductService;
import at.fhv.productservice.application.services.GetProductService;
import at.fhv.productservice.application.services.UpdateProductService;
import at.fhv.productservice.rest.dtos.CreateProductDTO;
import at.fhv.productservice.rest.dtos.GetProductDTO;
import at.fhv.productservice.rest.dtos.UpdateProductDTO;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/products")
public class ProductRestController {
    private final CreateProductService createProductService;
    private final GetProductService getProductService;
    private final UpdateProductService updateProductService;
    private final DeleteProductService deleteProductService;

    public ProductRestController(CreateProductService createProductService, GetProductService getProductService, UpdateProductService updateProductService, DeleteProductService deleteProductService) {
        this.createProductService = createProductService;
        this.getProductService = getProductService;
        this.updateProductService = updateProductService;
        this.deleteProductService = deleteProductService;
    }

    @PostMapping
    public ResponseEntity<GetProductDTO> createProduct(@Valid @RequestBody CreateProductDTO dto) {
        GetProductDTO createdProduct = createProductService.createProduct(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
    }

    @Operation(description="Retrieves all products, including inactive ones.")
    @GetMapping
    public ResponseEntity<List<GetProductDTO>> getAllProducts() {
        List<GetProductDTO> products = getProductService.getAllProducts(true);
        return ResponseEntity.ok(products);
    }

    @Operation(description="Retrieves all active products.")
    @GetMapping("/active")
    public ResponseEntity<List<GetProductDTO>> getActiveProducts() {
        List<GetProductDTO> products = getProductService.getAllProducts(false);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GetProductDTO> getProductById(@PathVariable UUID id) {
        GetProductDTO product = getProductService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    @PutMapping("/{id}")
    public ResponseEntity<GetProductDTO> updateProduct(@PathVariable UUID id, @Valid @RequestBody UpdateProductDTO dto) {
        GetProductDTO updatedProduct = updateProductService.updateProduct(id, dto);
        return ResponseEntity.ok(updatedProduct);
    }

    @Operation(description = "Sets the product status with the given ID to INACTIVE.")
    @DeleteMapping("/{id}")
    public ResponseEntity<GetProductDTO> deleteProduct(@PathVariable UUID id) {
        GetProductDTO deletedProduct = deleteProductService.deleteProduct(id);
        return ResponseEntity.ok(deletedProduct);
    }
}
