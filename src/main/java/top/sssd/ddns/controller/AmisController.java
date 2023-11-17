package top.sssd.ddns.controller;

import lombok.Data;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import top.sssd.ddns.common.AmisResult;
import top.sssd.ddns.common.utils.AmisPageUtils;
import top.sssd.ddns.common.valid.ValidGroup;
import top.sssd.ddns.model.entity.ChangedLog;
import top.sssd.ddns.model.entity.ParsingRecord;
import top.sssd.ddns.model.response.NetWorkSelectResponse;
import top.sssd.ddns.service.ChangedLogService;
import top.sssd.ddns.service.IParsingRecordService;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static top.sssd.ddns.common.constant.DDNSConstant.publicAccessDisabledKey;
import static top.sssd.ddns.common.constant.DDNSConstant.publicAccessDisabledMap;

/**
 * @author sssd
 * @careate 2023-10-25-15:01
 */
@RestController
@RequestMapping("amis")
public class AmisController {

    @Resource
    private IParsingRecordService parsingRecordService;
    
    @Resource
    private ChangedLogService changedLogService;

    @Data
    static class PublicAccessDisabled{
        private Boolean value;
    }

    @PostMapping("publicAccessDisabled")
    public AmisResult UpdatePublicAccessDisabled(@RequestBody PublicAccessDisabled publicAccessDisabled){
        publicAccessDisabledMap.put(publicAccessDisabledKey,publicAccessDisabled.getValue());
        return AmisResult.ok();
    }
    

    @GetMapping("publicAccessDisabled")
    public AmisResult publicAccessDisabled(){
        Boolean publicAccessDisabled = publicAccessDisabledMap.get(publicAccessDisabledKey);
        HashMap<String, Boolean> resultMap = new HashMap<>();
        resultMap.put("publicAccessDisabled",publicAccessDisabled);
        return AmisResult.ok(resultMap);
    }
    
    @GetMapping("page")
    public AmisResult<AmisPageUtils<ParsingRecord>> queryPage(ParsingRecord parsingRecord){
        AmisPageUtils<ParsingRecord> pageResult = parsingRecordService.queryPage(parsingRecord);
        return AmisResult.ok(pageResult);
    }

    @PostMapping("add")
    public AmisResult<String> add(@RequestBody
                              @Validated(ValidGroup.SaveGroup.class) ParsingRecord parsingRecord) throws Exception {
        parsingRecordService.add(parsingRecord);
        return AmisResult.ok();
    }

    @PostMapping("modify")
    public AmisResult<String> modify(@RequestBody
                                 @Validated(ValidGroup.UpdateGroup.class) ParsingRecord parsingRecord) throws Exception {
        parsingRecordService.modify(parsingRecord);
        return AmisResult.ok();
    }

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

    @DeleteMapping("delete/{id}")
    public AmisResult<String> delete(@PathVariable Long id) throws Exception {
        parsingRecordService.delete(id);
        return AmisResult.ok();
    }


    @GetMapping("getIpModeValue")
    public AmisResult<List<NetWorkSelectResponse>> getModeValue(@RequestParam Integer getIpMode,@RequestParam Integer recordType) throws Exception {
        List<NetWorkSelectResponse> list = parsingRecordService.getModeIpValue(getIpMode,recordType);
        return AmisResult.ok(list);
    }

}
