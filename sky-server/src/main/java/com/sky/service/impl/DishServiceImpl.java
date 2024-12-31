package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Service
@Slf4j
public class DishServiceImpl implements DishService {


    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMqapper;
    /**
     * 新增菜品和对应的口味数据
     * @param dishDTO
     */
    @Transactional
    public void saveWithFlavor(DishDTO dishDTO) {

        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);//将dto中的数据拷贝到实体类中

        //向菜品表插入一条数据
        dishMapper.insert(dish);

        //获取dishMapper.xml中 useGeneratedKeys="true" keyProperty="id" ，insert语句生成的主键值
        long dishID = dish.getId();

        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && flavors.size() > 0)
        {
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dishID);//将菜品的口味id设置为菜品的id
            });
            //向口味表插入数据
            dishFlavorMqapper.insertBatch(flavors);//批量插入
        }


    }

    /**
     * 菜品分页查询
     *
     * @param dishPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuary(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());//分页参数：第几页，每页显示多少条
        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);//

        return new PageResult(page.getTotal(),page.getResult());
    }
}
