package top.sssd.ddns.handler;

import cn.hutool.core.map.MapUtil;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;
import top.sssd.ddns.common.BizException;
import top.sssd.ddns.common.AmisResult;
import top.sssd.ddns.common.valid.ValidException;

import javax.annotation.Resource;
import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static top.sssd.ddns.common.constant.ExceptionConstant.RECORD_EXISTS;

/**
 * @author sssd
 */
@RestControllerAdvice
@Slf4j
public class GobalExceptionHandler {

    @Resource
    private ObjectMapper objectMapper;

    @ExceptionHandler({Exception.class})
    public AmisResult<String> teaExceptionHandler(Exception exception) throws JsonProcessingException {
        log.error("teaException info:{}", exception.getMessage());
        if(exception.getMessage().contains(RECORD_EXISTS)){
            return AmisResult.fail("DNS记录已存在");
        }
        return AmisResult.fail(exception.getMessage());
    }

    @ExceptionHandler({ConstraintViolationException.class})
    public AmisResult<String> constraintViolationExceptionHandler(ConstraintViolationException violationException) throws JsonProcessingException {
        log.error("valid exception info:{}", violationException.getMessage());
        return AmisResult.fail(violationException.getMessage());
    }

    @ExceptionHandler({BindException.class})
    public AmisResult<Map<String,String>> bindExceptionHandler(BindException e) {
        return AmisResult.fail(e.getBindingResult().getFieldErrors().stream().collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage)));
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public AmisResult<List<FieldError>> methodArgumentNotValidException(MethodArgumentNotValidException e) {
        return AmisResult.fail(e.getBindingResult().getFieldErrors().stream().collect(Collectors.toList()));
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public AmisResult<String> noHandlerFoundException(NoHandlerFoundException e) throws JsonProcessingException {
        Map<Object, Object> errorMap = MapUtil.builder().put("传入的头信息:", e.getHeaders().toSingleValueMap()).put("请求的方法:", e.getHttpMethod()).put("请求的url:", e.getRequestURL()).map();
        log.error(objectMapper.writeValueAsString(errorMap));
        return AmisResult.fail(e.getMessage());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public AmisResult<String> httpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) throws JsonProcessingException {
        Map<Object, Object> errorMap = MapUtil.builder().put("请求的方法:", e.getMethod()).put("支持请求的方法:", e.getSupportedMethods()).map();
        log.error(objectMapper.writeValueAsString(errorMap));
        return AmisResult.fail(e.getMessage());
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public AmisResult<String> httpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException e) throws JsonProcessingException {
        Map<Object, Object> errorMap = MapUtil.builder().put("请求的文件类型:", e.getContentType()).map();
        log.error(objectMapper.writeValueAsString(errorMap));
        return AmisResult.fail(e.getMessage());
    }

    @ExceptionHandler(MissingPathVariableException.class)
    public AmisResult<String> missingPathVariableException(MissingPathVariableException e) throws JsonProcessingException {
        Map<Object, Object> errorMap = MapUtil.builder().put("路径参数的名称:", e.getVariableName()).put("请求参数:", e.getParameter()).map();
        log.error(objectMapper.writeValueAsString(errorMap));
        return AmisResult.fail(e.getMessage());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public AmisResult<String> missingServletRequestParameterException(MissingServletRequestParameterException e) throws JsonProcessingException {
        Map<Object, Object> errorMap = MapUtil.builder().put("请求参数的名称:", e.getParameterName()).put("请求参数的类型:", e.getParameterType()).map();
        log.error(objectMapper.writeValueAsString(errorMap));
        return AmisResult.fail(e.getMessage());
    }

    @ExceptionHandler(TypeMismatchException.class)
    public AmisResult<Object> typeMismatchException(TypeMismatchException e) throws JsonProcessingException {
        Map<Object, Object> errorMap = MapUtil.builder().put("要求的类型:", e.getRequiredType()).put("传入的值:", e.getValue()).put("属性名称:", e.getPropertyName()).map();
        log.error(objectMapper.writeValueAsString(errorMap));
        return AmisResult.fail().code(Integer.parseInt(e.getErrorCode())).message(e.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public AmisResult<String> httpMessageNotReadableException(HttpMessageNotReadableException e) throws JsonProcessingException {
        Map<Object, Object> errorMap = MapUtil.builder().put("输入的消息流的堆栈信息:", e.getStackTrace()).map();
        log.error(objectMapper.writeValueAsString(errorMap));
        return AmisResult.fail(e.getMessage());
    }

    @ExceptionHandler(HttpMessageNotWritableException.class)
    public AmisResult<String> httpMessageNotWritableException(HttpMessageNotWritableException e) throws JsonProcessingException {
        Map<Object, Object> errorMap = MapUtil.builder().put("输出的消息流的堆栈信息:", e.getStackTrace()).map();
        log.error(objectMapper.writeValueAsString(errorMap));
        return AmisResult.fail(e.getMessage());
    }

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public AmisResult<String> httpMediaTypeNotAcceptableException(HttpMediaTypeNotAcceptableException e) throws JsonProcessingException {
        Map<Object, Object> errorMap = MapUtil.builder().put("媒体类型不支持的堆栈信息:", e.getStackTrace()).map();
        log.error(objectMapper.writeValueAsString(errorMap));
        return AmisResult.fail(e.getMessage());
    }


    @ExceptionHandler(ServletRequestBindingException.class)
    public AmisResult<String> servletRequestBindingException(ServletRequestBindingException e) throws JsonProcessingException {
        Map<Object, Object> errorMap = MapUtil.builder().put("请求绑定异常的堆栈信息:", e.getStackTrace()).map();
        log.error(objectMapper.writeValueAsString(errorMap));
        return AmisResult.fail(e.getMessage());
    }


    @ExceptionHandler(ConversionNotSupportedException.class)
    public AmisResult<String> conversionNotSupportedException(ConversionNotSupportedException e) throws JsonProcessingException {
        Map<Object, Object> errorMap = MapUtil.builder().put("不支持转换异常的属性名称:", e.getPropertyName()).put("不支持转换异常的所需的类型:", e.getRequiredType()).map();
        log.error(objectMapper.writeValueAsString(errorMap));
        return AmisResult.fail(e.getMessage());
    }


    @ExceptionHandler(MissingServletRequestPartException.class)
    public AmisResult<String> missingServletRequestPartException(MissingServletRequestPartException e) throws JsonProcessingException {
        Map<Object, Object> errorMap = MapUtil.builder().put("请求附件的名称:", e.getRequestPartName()).map();
        log.error(objectMapper.writeValueAsString(errorMap));
        return AmisResult.fail(e.getMessage());
    }


    @ExceptionHandler(AsyncRequestTimeoutException.class)
    public AmisResult<String> asyncRequestTimeoutException(AsyncRequestTimeoutException e) throws JsonProcessingException {
        Map<Object, Object> errorMap = MapUtil.builder().put("异步请求超时异常的堆栈信息:", e.getStackTrace()).map();
        log.error(objectMapper.writeValueAsString(errorMap));
        return AmisResult.fail(e.getMessage());
    }

    @ExceptionHandler(BizException.class)
    public AmisResult<Object> bizException(BizException e) throws JsonProcessingException {
        Map<Object, Object> errorMap = MapUtil.builder().put(e.getMessage() + ":", e.getStackTrace()).map();
        log.error(objectMapper.writeValueAsString(errorMap));
        return AmisResult.fail(e.getMessage());
    }

    @ExceptionHandler(ValidException.class)
    public AmisResult<Object> validException(ValidException e) {
        return AmisResult.fail(JSONUtil.toList(e.getMessage(), List.class));
    }
}
