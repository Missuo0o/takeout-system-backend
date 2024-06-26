package com.missuo.server.controller.admin;

import com.missuo.common.constant.MessageConstant;
import com.missuo.common.exception.IllegalException;
import com.missuo.common.result.PageResult;
import com.missuo.common.result.Result;
import com.missuo.pojo.dto.SetmealDTO;
import com.missuo.pojo.dto.SetmealPageQueryDTO;
import com.missuo.pojo.vo.SetmealVO;
import com.missuo.server.service.SetmealService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/setmeal")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Setmeal Management")
public class SetmealController {
  private final SetmealService setmealService;

  @PostMapping
  @Operation(summary = "Add Setmeal")
  @CacheEvict(value = "setmealCache", key = "#setmealDTO.categoryId")
  public Result save(@Validated @RequestBody SetmealDTO setmealDTO) {
    log.info("Add Setmeal：{}", setmealDTO);
    setmealService.saveWithDish(setmealDTO);
    return Result.success();
  }

  @GetMapping("/page")
  @Operation(summary = "Setmeal Page Query")
  public Result page(SetmealPageQueryDTO setmealPageQueryDTO) {
    PageResult<SetmealVO> pageResult = setmealService.pageQuery(setmealPageQueryDTO);
    return Result.success(pageResult);
  }

  @DeleteMapping
  @Operation(summary = "Delete Setmeal")
  @CacheEvict(value = "setmealCache", allEntries = true)
  public Result delete(@RequestParam List<Long> ids) {
    setmealService.deleteBatch(ids);
    return Result.success();
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get Setmeal by ID")
  public Result getById(@PathVariable Long id) {
    SetmealVO setmealVO = setmealService.getByIdWithDish(id);
    return Result.success(setmealVO);
  }

  @PutMapping
  @Operation(summary = "Update Setmeal")
  @CacheEvict(value = "setmealCache", allEntries = true)
  public Result update(@Validated(SetmealDTO.Update.class) @RequestBody SetmealDTO setmealDTO) {

    log.info("Update Setmeal：{}", setmealDTO);
    setmealService.update(setmealDTO);
    return Result.success();
  }

  @PutMapping("/status/{status}")
  @Operation(summary = "Start or Stop Setmeal")
  @CacheEvict(value = "setmealCache", allEntries = true)
  public Result startOrStop(@PathVariable Integer status, @RequestParam Long id) {
    if (status != 1 && status != 0) {
      throw new IllegalException(MessageConstant.ILLEGAL_OPERATION);
    }
    setmealService.startOrStop(status, id);
    return Result.success();
  }
}
