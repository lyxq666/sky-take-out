package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * 菜品管理
 */
@RestController
@RequestMapping("/admin/dish")
@Api(tags = "菜品管理")
@Slf4j
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 新增菜品
     *
     * @param dishDTO
     * @return
     */
    @PostMapping
    @ApiOperation("新增菜品")
    public Result save(@RequestBody DishDTO dishDTO) {
        log.info("新增菜品：{}", dishDTO);
        dishService.saveWithFlavor(dishDTO);
        //构造分类id
        String key = "*dish_" + dishDTO.getCategoryId();
        //清理缓存数据（只清理新增菜品的分类）
        cleanCatch(key);
        return Result.success();
    }

    /**
     * 菜品分页查询
     *
     * @param dishPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("菜品分页查询")
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO) {
        log.info("菜品分页查询：{}", dishPageQueryDTO);
        PageResult pageResult = dishService.pageQuery(dishPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 批量删除菜品
     *
     * @param ids
     * @return
     */
    @DeleteMapping
    @ApiOperation("批量删除菜品")
    public Result delete(@RequestParam List<Long> ids)//@RequestParam注解用于获取HTTP请求中的参数，并把这些参数传递给方法中的变量。
    {
        //eg：GET /delete?ids=1&ids=2&ids=3
        //ids=[1,2,3]
        log.info("批量删除菜品：{}", ids);
        dishService.deleteBatch(ids);

        //因为存在删除的多个菜品属于不同分类,所以直接删除所有菜品分类的redis缓存
        cleanCatch("*dish_*");
        return Result.success();
    }

    /**
     * 根据id查询菜品
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询菜品")
    public Result<DishVO> getById(@PathVariable Long id) {
        log.info("根据id查询菜品:{}", id);
        DishVO dishVO = dishService.getByIdWithFlavor(id);
        return Result.success(dishVO);
    }

    /**
     * 根据分类id查询菜品
     *
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<Dish>> getDishesByCategoryId(Long categoryId) {
        log.info("根据分类id查询菜品:{}", categoryId);
        List<Dish> list = dishService.list(categoryId);
        return Result.success(list);
    }

    /**
     * 修改菜品
     *
     * @return
     */
    @PutMapping //put为更新现有资源
    @ApiOperation("修改菜品")
    public Result update(@RequestBody DishDTO dishDTO) {
        log.info("修改菜品：{}", dishDTO);
        dishService.updateWithFlavor(dishDTO);

        //因为存在修改菜品分类的情况，所以如果再单独判断，情况较为复杂，直接全删即可
        cleanCatch("*dish_*");
        return Result.success();
    }

    /**
     * 菜品起售、禁售
     * 规则：如果执行停售操作，则包含此菜品的套餐也需要停售
     *
     * @param status
     * @param id
     * @return
     */
    @PostMapping("/status/{status}")
    @ApiOperation("菜品起售、停售")
    public Result startOrStop(@PathVariable Integer status, Long id) {
        log.info("启售或停售菜品");
        dishService.startOrStop(status, id);

        //清理所有缓存数据
        cleanCatch("*dish_*");

        return Result.success();
    }

    /**
     * 清理缓存数据
     *
     * @param pattern
     */
    private void cleanCatch(String pattern) {
        Set keys = redisTemplate.keys(pattern);//返回集合(set) ,所有dish_ 相关的数据
        redisTemplate.delete(keys);//删除集合
    }

}
