package top.sssd.ddns.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;
/**
 * @author sssd
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Result<T>{
    /**
     * 返回码
     */
    private Integer code;

    /**
     * 返回消息
     */
    private String message;

    /**
     * 返回数据
     */
    private T data;

    protected static <T > Result<T> build(T data) {
        Result<T> result = new Result<>();
        if (Objects.nonNull(data)) {
            result.setData(data);
        }
        return result;
    }

    public static <T > Result<T> build(T body, Integer code, String message) {
        Result<T> result = build(body);
        result.setCode(code);
        result.setMessage(message);
        return result;
    }

    public static <T > Result<T> build(T body, ResultCodeEnum resultCodeEnum) {
        Result<T> result = build(body);
        result.setCode(resultCodeEnum.getCode());
        result.setMessage(resultCodeEnum.getMessage());
        return result;
    }

    public static<T > Result<T> ok(T data){
        return build(data, ResultCodeEnum.SUCCESS);
    }

    public static<T > Result<T> ok(){
        return Result.ok(null);
    }

    public static<T > Result<T> fail(T data){
        return build(data, ResultCodeEnum.FAIL);
    }

    public static<T > Result<T> fail(){
        return Result.fail(null);
    }

    public Result<T> message(String msg){
        this.setMessage(msg);
        return this;
    }

    public Result<T> code(Integer code){
        this.setCode(code);
        return this;
    }
}
