package top.sssd.ddns.common.valid;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Delegate;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

/**
 * @author sssd
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ValidList<E> implements List<E> {

    @Delegate
    @Valid
    public List<E> list = new ArrayList<>();

    @Override
    public String toString() {
        return list.toString();
    }


}
