package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.entity.Setmeal;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.CategoryService;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class DishServiceImpl implements DishService {


    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMqapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private SetmealMapper setmealMapper;

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

    /**
     * 菜品批量删除
     * 业务规则：可以一次删除一个菜品，也可以批量删除菜品
     * 起售中的菜品不能删除
     * 被套餐关联的菜品不能删除
     * 删除菜品后，关联的口味数据也需要删除掉
     * @param ids
     */
    @Transactional
    public void deleteBatch(List<Long> ids) {
        //判断当前菜品是否能够删除--起售中的菜品不能删除（查看是否存在起售中的菜品）
        for (Long id : ids) {
           Dish dish = dishMapper.getById(id);
            if(dish.getStatus() == StatusConstant.ENABLE)
            {
                //MessageConstant.DISH_ON_SALE:当前菜品处于起售中，不能删除
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }

        //判断当前菜品是否能够删除--被套餐关联的菜品不能删除（查看是否存在被套餐关联的菜品）

        List<Long> setmealIds = setmealDishMapper.getSetmealIdsByDishIds(ids);
        if(setmealIds != null && setmealIds.size()>0)//存在被套餐关联的菜品
        {
            //当前菜品被套餐关联，不能删除
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }

        //删除菜品表中的菜品数据  此方法sql语句太多次
//        for (Long id : ids) {
//            dishMapper.deleteById(id);
//            //删除菜品关联的口味数据
//            dishFlavorMqapper.deleteBydishId(id);
//        }

        //优化后如下：性能得到提升。
        //根据菜品id集合批量删除菜品数据
        //sql:delete from dish where id in (?,?,?)
        dishMapper.deleteByIds(ids);

        //根据菜品id集合批量删除关联的口味数据
        //sql:delete from dish_flavor where id in (?,?,?)
        dishFlavorMqapper.deleteBydishIds(ids);

    }

    /**
     * 根据id查询菜品和对应口味数据
     *
     * @return
     */
    @Override
    public DishVO getByIdWithFlavor(Long id) {
        //根据id查询菜品数据
        Dish dish = dishMapper.getById(id);

        //根据菜品id查询口味数据
        List<DishFlavor> dishFlavors = dishFlavorMqapper.getByDishId(id);

        //将查询到的数据封装到VO
        DishVO dishVO = new DishVO();
        //将 dish 对象的属性值拷贝到 dishVO 对象中，用于将数据实体转换为视图对象
        BeanUtils.copyProperties(dish,dishVO);
        dishVO.setFlavors(dishFlavors);

        return dishVO;

    }

    /**
     * 根据id修改菜品基本信息和对应的口味信息
     *
     * @param dishDTO
     */
    @Override
    public void updateWithFlavor(DishDTO dishDTO) {
        Dish dish = new Dish();//需要使用dish进行数据库修改
        BeanUtils.copyProperties(dishDTO,dish);
        //*技术层面为先删除再插入
        //修改菜品表基本信息
        dishMapper.update(dish);

        //删除原有的口味数据
        dishFlavorMqapper.deleteBydishId(dishDTO.getId());//删除原来菜品关联的口味数据

        //插入新的口味数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && flavors.size() > 0)
        {
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dishDTO.getId());//将菜品的口味id设置为菜品的id
            });
            //向口味表插入数据
            dishFlavorMqapper.insertBatch(flavors);//借用批量插入
        }
    }

    /**
     * 根据id修改菜品起售、禁售状态
     * @param status
     * @param id
     */
    @Override
    public void startOrStop(Integer status, Long id) {
        Dish dish = Dish.builder()
                .id(id)
                .status(status)
                .build();
        dishMapper.update(dish);

        //规则：如果执行停售操作，则包含此菜品的套餐也需要停售
        if(status == StatusConstant.DISABLE)
        {
            //如果停售，包含当前菜品的套餐也需要停售
            List<Long> dishIds = new ArrayList<>();
            dishIds.add(id);

            List<Long> setmealIds = setmealDishMapper.getSetmealIdsByDishIds(dishIds);//根据菜品id查询对应的套餐
            if(setmealIds != null && setmealIds.size()>0)//当前菜品关联的套餐存在
            {
                log.info("检查发现存在包含该菜品的套餐");
                for(Long setmealId : setmealIds)
                {
                    Setmeal setmeal = Setmeal.builder()
                            .id(setmealId)
                            .status(StatusConstant.DISABLE)//将含义菜品id的套餐禁用
                            .build();
                    setmealMapper.update(setmeal);
                }
            }
            else {
                log.info("不存在包含该菜品的套餐");
            }
      
      
        }

    }

    /**
     * 根据分类id查询菜品
     *
     * @param categoryId
     * @return
     */
    @Override
    public List<Dish> list(Long categoryId) {
        Dish dish = Dish.builder()
                .categoryId(categoryId)
                .status(StatusConstant.ENABLE)
                .build();
        return dishMapper.list(dish);
    }

}
