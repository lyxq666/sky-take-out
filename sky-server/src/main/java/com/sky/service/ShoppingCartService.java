package com.sky.service;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import org.springframework.stereotype.Service;

import java.security.PrivateKey;
import java.util.List;


public interface ShoppingCartService {


    /**
     * 添加购物车
     * @param shoppingCartDTO
     */
    void addShoppingCart(ShoppingCartDTO shoppingCartDTO);

    /**
     * 展示购物车
     * @return
     */
    List<ShoppingCart> showShoppingCart();

}
