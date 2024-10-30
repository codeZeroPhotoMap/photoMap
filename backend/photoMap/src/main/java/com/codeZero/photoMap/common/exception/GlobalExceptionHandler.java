package com.codeZero.photoMap.common.exception;

import com.codeZero.photoMap.common.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

//@ControllerAdvice
@RestControllerAdvice   //전역 예외 처리기
public class GlobalExceptionHandler {

    // 403 forbidden error: 해당 엔티티에 속해 있지 않는 경우 사용 (예 - 찾은 그룹이 사용자가 속한 그룹이 아닌 경우)
    @ExceptionHandler(ForbiddenException.class)
    public ApiResponse<String> handleForbiddenException(ForbiddenException ex) {
        return ApiResponse.of(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    // 404 not found error: 특정 엔티티를 찾을 수 없을 때 사용
    @ExceptionHandler(NotFoundException.class)
    public ApiResponse<String> handleNotFoundException(NotFoundException ex) {
        return ApiResponse.of(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // 409 Conflict error: 충돌이 발생 하는 경우 사용 (예 - 동일한 위치에 위치 폴더가 이미 존재하는 경우)
    @ExceptionHandler(DuplicateException.class)
    public ApiResponse<String> handleDuplicateException(DuplicateException ex) {
        return ApiResponse.of(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred");
    }
}
