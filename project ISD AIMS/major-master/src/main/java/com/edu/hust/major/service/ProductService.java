package com.edu.hust.major.service;

import com.edu.hust.major.model.Product;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface ProductService {
     List<Product> getAllProduct();
     void updateProduct(Product product);
     void removeProductById(long id);
     Optional<Product> getProductById(long id);
     List<Product> getAllProductByCategoryId(int id);
     List<Product> searchProductsByName(String keyword);
}
