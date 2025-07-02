package com.edu.hust.major.model;

import lombok.Data;
import javax.persistence.*;

@Entity
@Data
@Table(name = "cart_items")
@IdClass(CartItemId.class)
public class Cart {
    @Id
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Id
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    public Cart(int userId, Long productId, int quantity) {
        User user = new User();
        user.setId(userId);
        this.user = user;
        Product product = new Product();
        product.setId(productId);
        this.product = product;
        this.quantity = quantity;
    }

    public Cart() {
    }
}
