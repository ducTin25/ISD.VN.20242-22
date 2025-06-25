package com.edu.hust.major.controller;

import com.edu.hust.major.global.GlobalData;
import com.edu.hust.major.service.CategoryService;
import com.edu.hust.major.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/shop")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    // View all products
    @GetMapping("")
    public String shop(Model model) {
        model.addAttribute("cartCount", GlobalData.cart.size());
        model.addAttribute("categories", categoryService.getAllCategory());
        model.addAttribute("products", productService.getAllProduct());
        return "shop";
    }

    // View by category
    @GetMapping("/category/{id}")
    public String shopByCategory(@PathVariable int id, Model model) {
        model.addAttribute("cartCount", GlobalData.cart.size());
        model.addAttribute("categories", categoryService.getAllCategory());
        model.addAttribute("products", productService.getAllProductByCategoryId(id));
        return "shop";
    }

    // View product detail
    @GetMapping("/viewproduct/{id}")
    public String viewProduct(@PathVariable long id,
            @RequestParam(name = "itemAmount", required = false) Integer amount, Model model) {
        model.addAttribute("product", productService.getProductById(id).orElse(null));
        model.addAttribute("amount", amount);
        return "viewProduct";
    }

    // Search product
    @GetMapping("/search")
    public String searchProducts(@RequestParam("keyword") String keyword, Model model) {
        model.addAttribute("cartCount", GlobalData.cart.size());
        model.addAttribute("categories", categoryService.getAllCategory());
        model.addAttribute("products", productService.searchProductsByName(keyword));
        model.addAttribute("keyword", keyword);
        return "shop";
    }
}
