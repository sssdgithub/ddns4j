package top.sssd.ddns.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * @author sssd
 * @careate 2023-10-25-15:04
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AmisResult<T> {

    private Integer status;

    private String msg;

    private T data;

    protected static <T > AmisResult<T> build(T data) {
        AmisResult<T> result = new AmisResult<>();
        if (Objects.nonNull(data)) {
            result.setData(data);
        }
        return result;
    }

    public static <T > AmisResult<T> build(T body, Integer code, String message) {
        AmisResult<T> result = build(body);
        result.setStatus(code);
        result.setMsg(message);
        return result;
    }

    public static <T > AmisResult<T> build(T body, ResultCodeEnum resultCodeEnum) {
        AmisResult<T> result = build(body);
        result.setStatus(resultCodeEnum.getCode());
        result.setMsg(resultCodeEnum.getMessage());
        return result;
    }

    public static <T > AmisResult<T> build(ResultCodeEnum resultCodeEnum,String message) {
        AmisResult<T> result = new AmisResult<>();
        result.setStatus(resultCodeEnum.getCode());
        result.setMsg(message);
        return result;
    }

    public static<T > AmisResult<T> ok(T data){
        return build(data, ResultCodeEnum.AMIS_SUCCESS);
    }

    public static<T > AmisResult<T> ok(){
        return AmisResult.ok(null);
    }


    public static<T > AmisResult<T> fail(String message){
        return build(ResultCodeEnum.FAIL, message);
    }

    public static<T > AmisResult<T> fail(){
        return AmisResult.fail(null);
    }

    public AmisResult<T> message(String msg){
        this.setMsg(msg);
        return this;
    }

    public AmisResult<T> code(Integer code){
        this.setStatus(code);
        return this;
    }

}
