package com.missuo.server.controller.admin;

import com.missuo.common.constant.MessageConstant;
import com.missuo.common.exception.IllegalException;
import com.missuo.common.result.PageResult;
import com.missuo.common.result.Result;
import com.missuo.pojo.dto.DishDTO;
import com.missuo.pojo.dto.DishPageQueryDTO;
import com.missuo.pojo.entity.Dish;
import com.missuo.pojo.vo.DishVO;
import com.missuo.server.service.DishService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/dish")
@Tag(name = "Dish Management")
@Slf4j
@RequiredArgsConstructor
public class DishController {
  private final DishService dishService;
  private final RedisTemplate<Object, Object> redisTemplate;

  @PostMapping
  @Operation(summary = "Add Dish")
  public Result save(@Validated @RequestBody DishDTO dishDTO) {
    log.info("Add Dish：{}", dishDTO);
    dishService.saveWithFlavor(dishDTO);
    cleanCache("dish_" + dishDTO.getCategoryId());
    return Result.success();
  }

  @GetMapping("/page")
  @Operation(summary = "Get Dish")
  public Result page(DishPageQueryDTO dishPageQueryDTO) {
    log.info("Get Dish：{}", dishPageQueryDTO);
    PageResult<DishVO> pageResult = dishService.pageQuery(dishPageQueryDTO);
    return Result.success(pageResult);
  }

  @DeleteMapping
  @Operation(summary = "Delete Dish")
  public Result delete(@RequestParam("ids") List<Long> ids) {
    log.info("Delete Dish：{}", ids);
    dishService.deleteBatch(ids);
    cleanCache("dish_*");
    return Result.success();
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get Dish By Id")
  public Result getById(@PathVariable Long id) {
    log.info("Get Dish By Id：{}", id);
    DishVO dishVO = dishService.getByIdWithFlavor(id);
    return Result.success(dishVO);
  }

  @PutMapping
  @Operation(summary = "Update Dish")
  public Result update(@Validated @RequestBody DishDTO dishDTO) {
    log.info("Update Dish：{}", dishDTO);
    dishService.updateWithFlavor(dishDTO);
    cleanCache("dish_*");
    return Result.success();
  }

  @PutMapping("/status/{status}")
  @Operation(summary = "Start or Stop Dish")
  public Result startOrStop(@PathVariable Integer status, @RequestParam Long id) {
    if (status != 1 && status != 0) {
      throw new IllegalException(MessageConstant.ILLEGAL_OPERATION);
    }
    log.info("Start or Stop Dish：{},{}", status, id);
    cleanCache("dish_*");
    dishService.startOrStop(status, id);
    return Result.success();
  }

  @GetMapping("/list")
  @Operation(summary = "List Dish")
  public Result list(Long categoryId) {
    List<Dish> list = dishService.list(categoryId);
    return Result.success(list);
  }

  private void cleanCache(String pattern) {
    Set<Object> keys = redisTemplate.keys(pattern);
    if (keys != null && !keys.isEmpty()) {
      redisTemplate.delete(keys);
    }
  }
}
