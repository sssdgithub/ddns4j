package top.sssd.ddns.common;

import lombok.Getter;

/**
 * @author sssd
 */
@Getter
public class BizException extends RuntimeException{
    private final String message;
    public BizException(String message) {
        super(message);
        this.message = message;
    }
}
