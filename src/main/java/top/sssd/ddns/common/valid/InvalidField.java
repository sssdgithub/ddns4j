package top.sssd.ddns.common.valid;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author sssd
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class InvalidField {
    private String name;
    private String message;
    private Object value;
}
