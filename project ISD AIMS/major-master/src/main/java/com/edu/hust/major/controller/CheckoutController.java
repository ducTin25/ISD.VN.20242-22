package com.edu.hust.major.controller;

import com.edu.hust.major.global.GlobalData;
import com.edu.hust.major.model.Product;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CheckoutController {

    @GetMapping("/checkout")
    public String checkout(Model model) {
        model.addAttribute("cartCount", GlobalData.cart.size());
        model.addAttribute("total",
                GlobalData.cart.stream().mapToDouble(pair -> pair.getKey().getPrice() * pair.getValue()).sum());
        return "checkout";
    }
}
