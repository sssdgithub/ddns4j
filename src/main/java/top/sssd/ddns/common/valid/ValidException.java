package top.sssd.ddns.common.valid;

/**
 * @author sssd
 */
public class ValidException extends RuntimeException{
    private String message;
    public ValidException(String message) {
        super(message);
        this.message = message;
    }
}
