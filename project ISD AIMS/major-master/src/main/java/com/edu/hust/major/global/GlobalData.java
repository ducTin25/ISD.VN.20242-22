package com.edu.hust.major.global;

import com.edu.hust.major.model.Product;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;


public class GlobalData {
    // tao bien toan cuc
    public static List<Pair<Product, Integer>> cart;

    static {
        cart = new ArrayList<>();
    }

}
