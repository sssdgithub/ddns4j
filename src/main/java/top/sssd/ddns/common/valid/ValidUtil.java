package top.sssd.ddns.common.valid;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import javax.validation.Validation;
import java.util.List;
import java.util.stream.Collectors;


/**
 * @author sssd
 */
@Slf4j
public class ValidUtil {
    private ValidUtil(){}
    public static void serviceValid(Object o) {
        List<InvalidField> errorList = Validation
                .buildDefaultValidatorFactory()
                .getValidator().validate(o).stream()
                .map(item -> new InvalidField(item.getPropertyPath().toString(), item.getMessage(), item.getInvalidValue()))
                .collect(Collectors.toList());
        if(CollectionUtils.isEmpty(errorList)){
            return;
        }
        throw new ValidException(JSONUtil.toJsonStr(errorList));
    }


}
