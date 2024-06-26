package com.missuo.server.aop;

import com.missuo.common.utils.TrimStrUtil;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Component
@Aspect
@RequiredArgsConstructor
public class DataTrim {
  private final TrimStrUtil trimStrUtil;

  @Pointcut(
      "execution (* com.missuo.server.service.*Service.update (..)) || execution (* com.missuo.server.service.*Service.save (..))")
  private void pt() {}

  @Around("pt()")
  public Object trimUpdateStr(ProceedingJoinPoint pjp) throws Throwable {
    Object[] args = pjp.getArgs();
    if (args == null || args.length == 0) {
      return pjp.proceed();
    }
    args[0] = trimStrUtil.trimStrings(args[0]);
    return pjp.proceed(args);
  }
}
