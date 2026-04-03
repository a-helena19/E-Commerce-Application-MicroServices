package at.fhv.compositionservice.rest.dtos;

import java.util.List;

public class OrderDetailsDTO {
    private String orderId;
    private String status;
    private Double totalPrice;
    private String createdAt;
    private CustomerDTO customer;
    private List<OrderItemDTO> items;

    public OrderDetailsDTO() {}

    public OrderDetailsDTO(String orderId, String status, Double totalPrice, String createdAt, CustomerDTO customer, List<OrderItemDTO> items) {
        this.orderId = orderId;
        this.status = status;
        this.totalPrice = totalPrice;
        this.createdAt = createdAt;
        this.customer = customer;
        this.items = items;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
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


    public CustomerDTO getCustomer() {
        return customer;
    }

    public void setCustomer(CustomerDTO customer) {
        this.customer = customer;
    }

    public List<OrderItemDTO> getItems() {
        return items;
    }

    public void setItems(List<OrderItemDTO> items) {
        this.items = items;
    }
}
