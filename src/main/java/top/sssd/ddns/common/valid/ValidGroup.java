package top.sssd.ddns.common.valid;

import javax.validation.groups.Default;

/**
 * @author sssd
 */
public class ValidGroup {
    public interface SaveGroup extends Default{}
    public interface UpdateGroup extends Default{}
    public interface ImportGroup extends Default{}
}
