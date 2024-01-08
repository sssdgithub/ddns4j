package top.sssd.ddns.controller;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import top.sssd.ddns.common.AmisResult;
import top.sssd.ddns.common.utils.AmisPageUtils;
import top.sssd.ddns.common.valid.ValidGroup;
import top.sssd.ddns.model.entity.ParsingRecord;
import top.sssd.ddns.model.response.NetWorkSelectResponse;
import top.sssd.ddns.service.IParsingRecordService;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author sssd
 * @careate 2023-10-25-15:01
 */
@RestController
@RequestMapping("parsingRecord")
public class ParsingRecordController {

    @Resource
    private IParsingRecordService parsingRecordService;
    
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

    @PostMapping("copy")
    public AmisResult<String> copy(@RequestBody
                                  @Validated(ValidGroup.CopyGroup.class) ParsingRecord parsingRecord) throws Exception {
        parsingRecordService.copy(parsingRecord);
        return AmisResult.ok();
    }

    @PostMapping("modify")
    public AmisResult<String> modify(@RequestBody
                                 @Validated(ValidGroup.UpdateGroup.class) ParsingRecord parsingRecord) throws Exception {
        parsingRecordService.modify(parsingRecord);
        return AmisResult.ok();
    }

    @DeleteMapping("delete/{id}")
    public AmisResult<String> delete(@PathVariable Long id) throws Exception {
        parsingRecordService.delete(id);
        return AmisResult.ok();
    }

    /**
     * 获取ip模式
     * @param getIpMode
     * @param recordType
     * @return
     * @throws Exception
     */
    @GetMapping("getIpModeValue")
    public AmisResult<List<NetWorkSelectResponse>> getModeValue(@RequestParam Integer getIpMode,@RequestParam Integer recordType) throws Exception {
        List<NetWorkSelectResponse> list = parsingRecordService.getModeIpValue(getIpMode,recordType);
        return AmisResult.ok(list);
    }

}
