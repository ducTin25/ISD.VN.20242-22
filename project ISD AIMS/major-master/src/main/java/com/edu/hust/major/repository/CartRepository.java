package com.edu.hust.major.repository;

import com.edu.hust.major.model.Cart;
import com.edu.hust.major.model.CartItemId;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartRepository extends JpaRepository<Cart, CartItemId> {
    List<Cart> findAllByUser_Id(Integer id);
}