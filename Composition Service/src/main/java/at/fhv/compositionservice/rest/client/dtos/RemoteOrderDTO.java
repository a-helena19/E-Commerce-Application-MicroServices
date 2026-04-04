package at.fhv.compositionservice.rest.client.dtos;

import java.math.BigDecimal;
import java.util.List;

public class RemoteOrderDTO {
    private String id;
    private String status;
    private BigDecimal totalPrice;
    private String createdAt;
    private String userId;
    private List<RemoteOrderItemDTO> items;

    public RemoteOrderDTO() {
    }

    public RemoteOrderDTO(String id, String status, BigDecimal totalPrice, String createdAt, String userId, List<RemoteOrderItemDTO> items) {
        this.id = id;
        this.status = status;
        this.totalPrice = totalPrice;
        this.createdAt = createdAt;
        this.userId = userId;
        this.items = items;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<RemoteOrderItemDTO> getItems() {
        return items;
    }

    public void setItems(List<RemoteOrderItemDTO> items) {
        this.items = items;
    }

    public static class RemoteOrderItemDTO {
        private String productId;
        private Integer quantity;
        private BigDecimal price;

        public RemoteOrderItemDTO() {
        }

        public RemoteOrderItemDTO(String productId, Integer quantity, BigDecimal price) {
            this.productId = productId;
            this.quantity = quantity;
            this.price = price;
        }

        public String getProductId() {
            return productId;
        }

        public void setProductId(String productId) {
            this.productId = productId;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }
    }
}