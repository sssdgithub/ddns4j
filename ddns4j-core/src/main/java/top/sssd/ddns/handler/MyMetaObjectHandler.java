package top.sssd.ddns.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

import static top.sssd.ddns.common.constant.DDNSConstant.*;

/**
 * @author sssd
 */
@Component
@Slf4j
public class MyMetaObjectHandler implements MetaObjectHandler {

    /**
     * @author sssd
     */


    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, CREATE_DATE, LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, UPDATE_DATE, LocalDateTime.class, LocalDateTime.now());

        this.strictInsertFill(metaObject, CREATOR, Long.class, 0L);
        this.strictInsertFill(metaObject, UPDATER, Long.class, 0L);

    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject,UPDATE_DATE,LocalDateTime.class, LocalDateTime.now());
        this.strictUpdateFill(metaObject,UPDATER,Long.class, 0L);
    }
}
