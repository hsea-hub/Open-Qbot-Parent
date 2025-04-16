package com.qbot.cq.starter.web.exception;

import com.google.common.collect.Sets;
import com.qbot.cq.framework.common.enums.MessageEnum;
import com.qbot.cq.framework.common.enums.ResultCodeEnum;
import com.qbot.cq.framework.common.model.ResultVO;
import jakarta.security.auth.message.AuthException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.yaml.snakeyaml.constructor.DuplicateKeyException;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 处理其他异常
     *
     * @param req
     * @param e
     * @return
     */
    @ExceptionHandler(value = Exception.class)
    public ResultVO<?> exceptionHandler(HttpServletRequest req, Exception e) {
        Map<String, String[]> parameterMap = req.getParameterMap();
        e.printStackTrace();
        // 国际化处理
        return ResultVO.builder()
                .code(ResultCodeEnum.UNKNOWN_ERROR.getCode())
                .messageKey(MessageEnum.UNLAWFUL_OPERATIONS.getKey())
                .message(MessageEnum.UNLAWFUL_OPERATIONS.getMessage())
                .localDateTime(LocalDateTime.now())
                .build();
    }


    /**
     * 处理请求方式异常
     *
     * @param req
     * @param e
     * @return
     */
    @ExceptionHandler(value = HttpRequestMethodNotSupportedException.class)
    public ResultVO<?> exceptionHandler(HttpServletRequest req, HttpRequestMethodNotSupportedException e) {
        return ResultVO.builder()
                .code(ResultCodeEnum.METHOD_NOT_SUPPORT_ERROR.getCode())
                .messageKey(ResultCodeEnum.METHOD_NOT_SUPPORT_ERROR.getMessage())
                .message(ResultCodeEnum.METHOD_NOT_SUPPORT_ERROR.getMessage())
                .localDateTime(LocalDateTime.now())
                .build();
    }


    /**
     * Http消息不可读异常
     *
     * @param req
     * @param e
     * @return
     */
    @ExceptionHandler(value = {HttpMessageNotReadableException.class,IllegalStateException.class})
    public ResultVO<?> exceptionHandler(HttpServletRequest req, HttpMessageNotReadableException e) {
        return ResultVO.builder()
                .code(ResultCodeEnum.MESSAGE_IS_NOT_READABLE.getCode())
                .messageKey(ResultCodeEnum.MESSAGE_IS_NOT_READABLE.getMessage())
                .message(ResultCodeEnum.MESSAGE_IS_NOT_READABLE.getMessage())
                .localDateTime(LocalDateTime.now())
                .build();
    }


    /**
     * HTTP_MEDIA_TYPE_NOT_SUPPORTED异常
     *
     * @param req
     * @param e
     * @return
     */
    @ExceptionHandler(value = {HttpMediaTypeNotSupportedException.class})
    public ResultVO<?> exceptionHandler(HttpServletRequest req, HttpMediaTypeNotSupportedException e) {
        return ResultVO.builder()
                .code(ResultCodeEnum.HTTP_MEDIA_TYPE_NOT_SUPPORTED.getCode())
                .messageKey(ResultCodeEnum.HTTP_MEDIA_TYPE_NOT_SUPPORTED.getMessage())
                .message(ResultCodeEnum.HTTP_MEDIA_TYPE_NOT_SUPPORTED.getMessage())
                .localDateTime(LocalDateTime.now())
                .build();
    }

    /**
     * 约束违例异常
     *
     * @param req
     * @param e
     * @return
     */
    @ExceptionHandler(value = ConstraintViolationException.class)
    public ResultVO<?> exceptionHandler(HttpServletRequest req, ConstraintViolationException e) {
        Set<ConstraintViolation<?>> constraintViolations = e.getConstraintViolations();
        Set messages = Sets.newHashSet();
        messages.addAll(constraintViolations.stream()
                .map(constraintViolation -> String.format("%s value '%s' %s", constraintViolation.getPropertyPath(),
                        constraintViolation.getInvalidValue(), constraintViolation.getMessage()))
                .collect(Collectors.toList()));
        return ResultVO.builder()
                .code(ResultCodeEnum.PARAM_ERROR.getCode())
                .messageKey(ResultCodeEnum.PARAM_ERROR.getMessage())
                .message(ResultCodeEnum.PARAM_ERROR.getMessage())
                .data(messages)
                .localDateTime(LocalDateTime.now())
                .build();
    }

    /**
     * 方法参数无效异常
     *
     * @param req
     * @param e
     * @return
     */
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResultVO<?> exceptionHandler(HttpServletRequest req, MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();
        FieldError fieldError = bindingResult.getFieldError();
        return ResultVO.builder()
                .code(ResultCodeEnum.PARAM_ERROR.getCode())
                .messageKey(ResultCodeEnum.PARAM_ERROR.getMessage())
                .message(fieldError.getField() + fieldError.getDefaultMessage())
                .localDateTime(LocalDateTime.now())
                .build();
    }


    /**
     * 重复密钥异常
     *
     * @param req
     * @param e
     * @return
     */
    @ExceptionHandler(value = DuplicateKeyException.class)
    public ResultVO<?> exceptionHandler(HttpServletRequest req, DuplicateKeyException e) {
        return ResultVO.builder()
                .code(ResultCodeEnum.DUPLICATE_KEY_ERROR.getCode())
                .messageKey(ResultCodeEnum.DUPLICATE_KEY_ERROR.getMessage())
                .message(ResultCodeEnum.DUPLICATE_KEY_ERROR.getMessage())
                .localDateTime(LocalDateTime.now())
                .build();
    }

    /**
     * Servlet请求绑定异常
     *
     * @param req
     * @param e
     * @return
     */
    @ExceptionHandler(value = ServletRequestBindingException.class)
    public ResultVO<?> exceptionHandler(HttpServletRequest req, ServletRequestBindingException e) {
        return ResultVO.builder()
                .code(ResultCodeEnum.SERVLET_REQUEST_BINDING_ERROR.getCode())
                .messageKey(ResultCodeEnum.SERVLET_REQUEST_BINDING_ERROR.getMessage())
                .message(e.getMessage())
                .localDateTime(LocalDateTime.now())
                .build();
    }
//    /**
//     * Servlet请求绑定异常
//     *
//     * @param req
//     * @param e
//     * @return
//     */
//    @ExceptionHandler(value = MissingServletRequestParameterException.class)
//    public ResultVO<?> exceptionHandler(HttpServletRequest req, MissingServletRequestParameterException e) {
//        Map<String, String[]> parameterMap = req.getParameterMap();
//        log.error("missing servlet request parameter exception，req: {}，message: {}",JSON.toJSONString(parameterMap), e);
//        return ResultVO.builder()
//                .code(ResultCodeEnum.SERVLET_REQUEST_BINDING_ERROR.getCode())
//                .messageKey(ResultCodeEnum.SERVLET_REQUEST_BINDING_ERROR.getMessage())
//                .message(ResultCodeEnum.SERVLET_REQUEST_BINDING_ERROR.getMessage())
//                .localDateTime(LocalDateTime.now())
//                .build();
//    }

}
