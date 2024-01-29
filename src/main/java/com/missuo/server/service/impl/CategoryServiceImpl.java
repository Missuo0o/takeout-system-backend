package com.missuo.server.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.missuo.common.constant.MessageConstant;
import com.missuo.common.constant.StatusConstant;
import com.missuo.common.context.BaseContext;
import com.missuo.common.exception.DeletionNotAllowedException;
import com.missuo.common.result.PageResult;
import com.missuo.pojo.dto.CategoryDTO;
import com.missuo.pojo.dto.CategoryPageQueryDTO;
import com.missuo.pojo.entity.Category;
import com.missuo.server.mapper.CategoryMapper;
import com.missuo.server.mapper.DishMapper;
import com.missuo.server.mapper.SetmealMapper;
import com.missuo.server.service.CategoryService;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl implements CategoryService {

  @Autowired private CategoryMapper categoryMapper;
  @Autowired private DishMapper dishMapper;
  @Autowired private SetmealMapper setmealMapper;

  public void save(CategoryDTO categoryDTO) {
    Category category = new Category();
    BeanUtils.copyProperties(categoryDTO, category);

    category.setStatus(StatusConstant.DISABLE);

    //    category.setCreateTime(LocalDateTime.now());
    //    category.setUpdateTime(LocalDateTime.now());
    //    category.setCreateUser(BaseContext.getCurrentId());
    //    category.setUpdateUser(BaseContext.getCurrentId());

    categoryMapper.insert(category);
  }

  public PageResult<Category> pageQuery(CategoryPageQueryDTO categoryPageQueryDTO) {
    PageHelper.startPage(categoryPageQueryDTO.getPage(), categoryPageQueryDTO.getPageSize());
    Page<Category> page = categoryMapper.pageQuery(categoryPageQueryDTO);

    // If the current page number value is greater than the total page number value, re-execute the
    // query operation and use the maximum page number value as the current page number value.
    if (categoryPageQueryDTO.getPage() > page.getPages()) {
      PageHelper.startPage(page.getPages(), categoryPageQueryDTO.getPageSize());
      page = categoryMapper.pageQuery(categoryPageQueryDTO);
    }

    return new PageResult<>(page.getTotal(), page.getResult());
  }

  public void deleteById(Long id) {
    // Check whether the current category is associated with dishes
    Integer count = dishMapper.countByCategoryId(id);
    if (count > 0) {
      throw new DeletionNotAllowedException(MessageConstant.CATEGORY_BE_RELATED_BY_DISH);
    }

    // Check whether the current category is associated with a combo
    count = setmealMapper.countByCategoryId(id);
    if (count > 0) {
      throw new DeletionNotAllowedException(MessageConstant.CATEGORY_BE_RELATED_BY_SETMEAL);
    }

    categoryMapper.deleteById(id);
  }

  public void update(CategoryDTO categoryDTO) {
    Category category = new Category();
    BeanUtils.copyProperties(categoryDTO, category);

    //    category.setUpdateTime(LocalDateTime.now());
    //    category.setUpdateUser(BaseContext.getCurrentId());

    categoryMapper.update(category);
  }

  public void startOrStop(Integer status, Long id) {
    Category category =
        Category.builder()
            .id(id)
            .status(status)
            .updateTime(LocalDateTime.now())
            .updateUser(BaseContext.getCurrentId())
            .build();
    categoryMapper.update(category);
  }

  public List<Category> list(Integer type) {
    return categoryMapper.list(type);
  }
}
