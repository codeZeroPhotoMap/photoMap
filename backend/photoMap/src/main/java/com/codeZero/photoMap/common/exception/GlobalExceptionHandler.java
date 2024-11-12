package com.codeZero.photoMap.common.exception;

import com.codeZero.photoMap.common.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice //전역 예외 처리기
public class GlobalExceptionHandler {

    // 400 Bad Request error: 잘못된 요청 매개변수나 유효하지 않은 값이 전달된 경우 사용 (예 - 필수 파라미터 누락, 잘못된 데이터 형식 등)
    @ExceptionHandler(IllegalArgumentException.class)
    public ApiResponse<String> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ApiResponse.of(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // 401 Unauthorized error: 인증되지 않은 요청에 사용
    @ExceptionHandler(UnauthorizedException.class)
    public ApiResponse<String> handleUnauthorizedException(UnauthorizedException ex) {
        return ApiResponse.of(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    // 403 forbidden error: 해당 엔티티에 속해 있지 않는 경우 사용 (예 - 찾은 그룹이 사용자가 속한 그룹이 아닌 경우)
    @ExceptionHandler(ForbiddenException.class)
    public ApiResponse<String> handleForbiddenException(ForbiddenException ex) {
        return ApiResponse.of(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    //TODO : test용
//    // 403 Forbidden error: 탈퇴한 회원 접근 시 사용
//    @ExceptionHandler(ForbiddenException.class)
//    public ResponseEntity<ApiResponse<String>> handleForbiddenException(ForbiddenException ex) {
//        ApiResponse<String> response = ApiResponse.of(HttpStatus.FORBIDDEN, ex.getMessage());
//        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
//    }

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

    //500 Internal Server Error 중 메일 전송 에러
    @ExceptionHandler(EmailSendException.class)
    public ApiResponse<String> handleEmailSendException(EmailSendException ex) {
        return ApiResponse.of(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }

    // 500 Internal Server Error 중 일반 런타임 에러
    @ExceptionHandler(RuntimeException.class)
    public ApiResponse<String> handleRuntimeException(RuntimeException ex) {
        return ApiResponse.of(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }
}
