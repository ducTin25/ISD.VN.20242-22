package com.edu.hust.major.controller;

import com.edu.hust.major.dto.UserDTO;
import com.edu.hust.major.global.GlobalData;
import com.edu.hust.major.model.Cart;
import com.edu.hust.major.model.Product;
import com.edu.hust.major.model.Role;
import com.edu.hust.major.model.User;
import com.edu.hust.major.model.CustomUserDetail;
import com.edu.hust.major.service.CartService;
import com.edu.hust.major.service.CategoryService;
import com.edu.hust.major.service.ProductService;
import com.edu.hust.major.service.RoleService;
import com.edu.hust.major.service.UserService;

import javafx.util.Pair;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
public class HomeController {
    @Autowired
    private PasswordEncoder bCryptPasswordEncoder;

    @Autowired
    UserService userService;

    @Autowired
    RoleService roleService;

    @Autowired
    CategoryService categoryService;

    @Autowired
    ProductService productService;

    @Autowired
    CartService cartService;

    @GetMapping({ "/", "/home" })
    public String home(Model model) {
        GlobalData.cart.clear();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof CustomUserDetail) {
            CustomUserDetail userDetails = (CustomUserDetail) auth.getPrincipal();
            Integer userId = userDetails.getId();
            System.out.println("ID=" + userId);
            List<Cart> cart = userId != null ? cartService.getAllItemInCart(userId) : null;
            if (cart != null) {
                for (Cart item : cart) {
                    GlobalData.cart.add(new Pair<Product, Integer>(item.getProduct(), item.getQuantity()));
                }
            }
        }
        model.addAttribute("cartCount", GlobalData.cart.size());
        return "index";
    } // index

    @GetMapping("/users/add")
    public String updateUser(Model model) {
        UserDTO currentUser = new UserDTO();
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails && ((UserDetails) principal).getUsername() != null) {
            String currentUsername = ((UserDetails) principal).getUsername();
            User user = userService.getUserByEmail(currentUsername).get();
            currentUser.setId(user.getId());
            currentUser.setEmail(user.getEmail());
            currentUser.setPassword("");
            currentUser.setFirstName(user.getFirstName());
            currentUser.setLastName(user.getLastName());
            List<Integer> roleIds = new ArrayList<>();
            for (Role item : user.getRoles()) {
                roleIds.add(item.getId());
            }
            currentUser.setRoleIds(roleIds);
        } // get current User runtime

        model.addAttribute("userDTO", currentUser);
        return "userRoleAdd";
    }

    @PostMapping("/users/add")
    public String postUserAdd(@ModelAttribute("userDTO") UserDTO userDTO) {
        // convert dto > entity
        User user = new User();
        user.setId(userDTO.getId());
        user.setEmail(userDTO.getEmail());
        user.setPassword(bCryptPasswordEncoder.encode(userDTO.getPassword()));
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        List<Role> roles = userService.getUserById(user.getId()).get().getRoles();
        user.setRoles(roles);

        userService.updateUser(user);
        return "index";
    }

}
