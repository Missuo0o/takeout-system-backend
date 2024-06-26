package com.missuo.server.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.missuo.common.constant.MessageConstant;
import com.missuo.common.constant.StatusConstant;
import com.missuo.common.exception.DeletionNotAllowedException;
import com.missuo.common.result.PageResult;
import com.missuo.pojo.dto.DishDTO;
import com.missuo.pojo.dto.DishPageQueryDTO;
import com.missuo.pojo.entity.Dish;
import com.missuo.pojo.entity.DishFlavor;
import com.missuo.pojo.vo.DishVO;
import com.missuo.server.mapper.DishFlavorMapper;
import com.missuo.server.mapper.DishMapper;
import com.missuo.server.mapper.SetmealDishMapper;
import com.missuo.server.service.DishService;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DishServiceImpl implements DishService {
  private final DishMapper dishMapper;
  private final DishFlavorMapper dishFlavorMapper;
  private final SetmealDishMapper setmealDishMapper;

  @Override
  @Transactional
  public void saveWithFlavor(DishDTO dishDTO) {
    Dish dish = new Dish();
    BeanUtils.copyProperties(dishDTO, dish);
    dishMapper.insert(dish);

    List<DishFlavor> flavors = dishDTO.getFlavors();
    if (flavors != null && !flavors.isEmpty()) {
      flavors.forEach(flavor -> flavor.setDishId(dish.getId()));

      dishFlavorMapper.insertBatch(flavors);
    }
  }

  @Override
  public PageResult<DishVO> pageQuery(DishPageQueryDTO dishPageQueryDTO) {
    try {
      PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
      Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);

      if (dishPageQueryDTO.getPage() > page.getPages()) {
        PageHelper.startPage(page.getPages(), dishPageQueryDTO.getPageSize());
        page = dishMapper.pageQuery(dishPageQueryDTO);
      }

      return new PageResult<>(page.getTotal(), page.getResult());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  @Transactional
  public void deleteBatch(List<Long> ids) {
    // Presence of dishes on sale

    List<Dish> byIds = dishMapper.getByIds(ids);

    if (byIds.stream().anyMatch(dish -> Objects.equals(dish.getStatus(), StatusConstant.ENABLE))) {
      throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
    }
    // Determine if a dish is in a combo
    List<Long> setmealIdsByDishIds = setmealDishMapper.getSetmealIdsByDishIds(ids);
    if (!setmealIdsByDishIds.isEmpty()) {
      throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
    }
    // Delete dish
    dishMapper.deleteBatch(ids);
    // Delete dish flavor
    dishFlavorMapper.deleteBatchByDishIds(ids);
  }

  @Override
  public DishVO getByIdWithFlavor(Long id) {
    Dish dish = dishMapper.getById(id);

    List<DishFlavor> dishFlavors = dishFlavorMapper.getByDishId(id);

    DishVO dishVO = new DishVO();
    BeanUtils.copyProperties(dish, dishVO);
    dishVO.setFlavors(dishFlavors);

    return dishVO;
  }

  @Override
  @Transactional
  public void updateWithFlavor(DishDTO dishDTO) {
    Dish dish = new Dish();
    BeanUtils.copyProperties(dishDTO, dish);
    dishMapper.update(dish);

    dishFlavorMapper.deleteByDishId(dishDTO.getId());

    List<DishFlavor> flavors = dishDTO.getFlavors();
    if (flavors != null && !flavors.isEmpty()) {
      flavors.forEach(flavor -> flavor.setDishId(dish.getId()));

      dishFlavorMapper.insertBatch(flavors);
    }
  }

  @Override
  public void startOrStop(Integer status, Long id) {
    Dish dish = Dish.builder().id(id).status(status).build();
    dishMapper.update(dish);
  }

  @Override
  public List<Dish> list(Long categoryId) {
    Dish dish = Dish.builder().categoryId(categoryId).status(StatusConstant.ENABLE).build();
    return dishMapper.list(dish);
  }

  @Override
  public List<DishVO> listWithFlavor(Dish dish) {
    List<Dish> dishList = dishMapper.list(dish);

    return dishList.stream()
        .map(
            d -> {
              DishVO dishVO = new DishVO();
              BeanUtils.copyProperties(d, dishVO);

              List<DishFlavor> flavors = dishFlavorMapper.getByDishId(d.getId());
              dishVO.setFlavors(flavors);

              return dishVO;
            })
        .collect(Collectors.toList());

    //    List<DishVO> dishVOList = new ArrayList<>();
    //
    //    for (Dish d : dishList) {
    //      DishVO dishVO = new DishVO();
    //      BeanUtils.copyProperties(d, dishVO);
    //
    //      List<DishFlavor> flavors = dishFlavorMapper.getByDishId(d.getId());
    //
    //      dishVO.setFlavors(flavors);
    //      dishVOList.add(dishVO);
    //    }
    //
    //
    //    return dishVOList;
  }
}
