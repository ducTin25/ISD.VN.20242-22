package com.edu.hust.major.service;

import com.edu.hust.major.model.Cart;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface CartService {
    List<Cart> getAllItemInCart(int id);

    void addItemToCart(int userId, Long productId, int quantity);

    void updateItemInCart(int userId, Long productId, int quantity);

    void removeItemInCart(int userId, Long productId);
}
