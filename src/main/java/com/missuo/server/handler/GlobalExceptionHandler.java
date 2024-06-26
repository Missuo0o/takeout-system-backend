package com.missuo.server.handler;

import com.missuo.common.constant.MessageConstant;
import com.missuo.common.exception.BaseException;
import com.missuo.common.exception.IllegalException;
import com.missuo.common.exception.UserNotLoginException;
import com.missuo.common.result.Result;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  @ExceptionHandler(UserNotLoginException.class)
  public ResponseEntity<Result> userNotLoginExceptionHandler(UserNotLoginException ex) {
    getError(ex);
    return new ResponseEntity<>(Result.unAuthorized(ex.getMessage()), HttpStatus.UNAUTHORIZED);
  }

  @ExceptionHandler(IllegalException.class)
  public ResponseEntity<Result> illegalArgumentExceptionHandler(IllegalException ex) {
    getError(ex);
    return new ResponseEntity<>(Result.error(ex.getMessage()), HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(BaseException.class)
  public ResponseEntity<Result> baseExceptionHandler(BaseException ex) {
    getError(ex);
    return new ResponseEntity<>(Result.error(ex.getMessage()), HttpStatus.OK);
  }

  @ExceptionHandler(DataAccessException.class)
  public ResponseEntity<Result> exceptionHandler(DataAccessException ex) {
    getError(ex);
    String message = Objects.requireNonNull(ex.getRootCause()).getMessage();
    if (message.contains("Duplicate entry")) {
      return new ResponseEntity<>(
          Result.error(message.split(" ")[2] + MessageConstant.ALREADY_EXISTS), HttpStatus.OK);
    } else {
      return new ResponseEntity<>(Result.error(message), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  private void getError(Exception ex) {
    log.error("Exception information：{}", ex.getMessage());
  }
}
