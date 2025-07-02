package com.edu.hust.major.service.impl;

import com.edu.hust.major.model.Cart;
import com.edu.hust.major.model.CartItemId;
import com.edu.hust.major.model.CustomUserDetail;
import com.edu.hust.major.repository.CartRepository;
import com.edu.hust.major.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CartServiceImpl implements CartService {
    @Autowired
    CartRepository cartRepository;

    @Override
    public List<Cart> getAllItemInCart(int id) {
        return cartRepository.findAllByUser_Id(id);
    }

    @Override
    public void addItemToCart(int userId, Long productId, int quantity) {
        if (userId != 0)
            cartRepository.save(new Cart(userId, productId, quantity));
    }

    @Override
    public void updateItemInCart(int userId, Long productId, int quantity) {
        if (userId != 0)
            cartRepository.save(new Cart(userId, productId, quantity));
    }

    @Override
    public void removeItemInCart(int userId, Long productId) {
        if (userId != 0)
            cartRepository.deleteById(new CartItemId(userId, productId));
    }
}
