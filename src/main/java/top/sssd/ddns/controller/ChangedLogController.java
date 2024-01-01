package top.sssd.ddns.controller;

import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.sssd.ddns.model.entity.ChangedLog;
import top.sssd.ddns.service.ChangedLogService;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author sssd
 * @careate 2024-01-01-18:10
 */
@RestController
@RequestMapping("changedLog")
public class ChangedLogController {

    @Resource
    private ChangedLogService changedLogService;
    @PostMapping("logs")
    public String logs() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime dayAog = now.minusDays(1);
        List<ChangedLog> list = changedLogService.lambdaQuery()
                .le(ChangedLog::getInsertDate, now)
                .ge(ChangedLog::getInsertDate, dayAog)
                .orderByDesc(ChangedLog::getInsertDate)
                .list();
        if(CollectionUtils.isEmpty(list)){
            return null;
        }
        List<String> result = list.stream().map(item -> {
            StringBuilder builder = new StringBuilder();
            builder.append(item.getInsertDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            builder.append(":");
            builder.append(item.getContent());
            return builder.toString();
        }).collect(Collectors.toList());

        return String.join("\r\n",result);
    }
}
