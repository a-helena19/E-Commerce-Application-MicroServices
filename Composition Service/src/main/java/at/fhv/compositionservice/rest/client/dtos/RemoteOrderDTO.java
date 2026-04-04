package at.fhv.compositionservice.rest.client.dtos;

import java.util.List;

public class RemoteOrderDTO {
    private String id;
    private String status;
    private Double totalPrice;
    private String createdAt;
    private String userId;
    private List<RemoteOrderItemDTO> items;

    public RemoteOrderDTO() {
    }

    public RemoteOrderDTO(String id, String status, Double totalPrice, String createdAt, String userId, List<RemoteOrderItemDTO> items) {
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

    public Double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Double totalPrice) {
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
        private Double unitPrice;

        public RemoteOrderItemDTO() {
        }

        public RemoteOrderItemDTO(String productId, Integer quantity, Double unitPrice) {
            this.productId = productId;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
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

        public Double getUnitPrice() {
            return unitPrice;
        }

        public void setUnitPrice(Double unitPrice) {
            this.unitPrice = unitPrice;
        }
    }
}