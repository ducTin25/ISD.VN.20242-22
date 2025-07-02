package com.edu.hust.major.model;

import java.io.Serializable;
import java.util.Objects;

import lombok.Data;

@Data
public class CartItemId implements Serializable {
    private Integer user;
    private Long product;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof CartItemId))
            return false;
        CartItemId that = (CartItemId) o;
        return Objects.equals(user, that.user) && Objects.equals(product, that.product);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, product);
    }

    public CartItemId(int user, Long product) {
        this.user = user;
        this.product = product;
    }

    public CartItemId() {

    }
}
