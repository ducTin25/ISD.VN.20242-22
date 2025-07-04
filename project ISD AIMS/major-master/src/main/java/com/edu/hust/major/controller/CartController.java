package com.edu.hust.major.controller;

import com.edu.hust.major.global.GlobalData;
import com.edu.hust.major.model.Product;
import com.edu.hust.major.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import org.apache.commons.lang3.tuple.Pair;


@Controller
public class CartController {
    @Autowired
    ProductService productService;

    @GetMapping("/cart")
    public String cartGet(Model model) {
        model.addAttribute("cartCount", GlobalData.cart.size());
        model.addAttribute("total",
                GlobalData.cart.stream().mapToDouble(pair -> pair.getKey().getPrice() * pair.getValue()).sum());
        model.addAttribute("cart", GlobalData.cart);

        return "cart";
    }

    @GetMapping("/addToCart/{id}")
    public String addToCart(@PathVariable int id, @RequestParam("itemAmount") Integer amount,
            @RequestParam("from") Boolean from) {
        int i;
        for (i = 0; i < GlobalData.cart.size(); i++) {
            if (id == GlobalData.cart.get(i).getKey().getId()) {
                break;
            }
        }
        if (from) {
            GlobalData.cart.remove(i);
            GlobalData.cart.add(Pair.of(productService.getProductById(id).get(), amount));
            return "redirect:/cart";
        } else if (i < GlobalData.cart.size()) {
            int inCart = GlobalData.cart.get(i).getValue();
            GlobalData.cart.remove(i);
            GlobalData.cart.add(Pair.of(productService.getProductById(id).get(), amount + inCart));
            return "redirect:/shop";
        } else {
            GlobalData.cart.add(Pair.of(productService.getProductById(id).get(), amount));
            return "redirect:/shop";
        }
    }

    @GetMapping("/cart/removeItem/{index}")
    public String cartItemRemove(@PathVariable int index) {
        GlobalData.cart.remove(index);
        return "redirect:/cart";
    }

}