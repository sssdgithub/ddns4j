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
@RequestMapping("amis")
public class AmisController {

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


    @GetMapping("getIpModeValue")
    public AmisResult<List> getModeValue(@RequestParam Integer getIpMode,@RequestParam Integer recordType) throws Exception {
        List<NetWorkSelectResponse> list = parsingRecordService.getModeIpValue(getIpMode,recordType);
        return AmisResult.ok(list);
    }

}
