package com.sky.service;

import com.sky.dto.SetmealDTO;
import org.springframework.stereotype.Service;

@Service
public interface SetmealService {


    /**
     * 新增套餐和对应的菜品
     * @param setmealDTO
     */
    void saveWithDish(SetmealDTO setmealDTO);
}
