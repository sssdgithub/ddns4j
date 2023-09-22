package top.sssd.ddns.controller;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import top.sssd.ddns.common.Result;
import top.sssd.ddns.common.utils.PageUtils;
import top.sssd.ddns.common.valid.ValidGroup;
import top.sssd.ddns.model.entity.ParsingRecord;
import top.sssd.ddns.model.response.NetWorkSelectResponse;
import top.sssd.ddns.service.IParsingRecordService;
import top.sssd.ddns.service.NetWorkService;

import javax.annotation.Resource;
import java.net.SocketException;
import java.util.List;

/**
 * @author sssd
 * @created 2023-05-02-5:09
 */
@RestController
@RequestMapping("parsingRecord")
@Validated
public class ParsingRecordController {

    @Resource
    private IParsingRecordService parsingRecordService;

    @Resource
    private NetWorkService netWorkService;


    @GetMapping("network/{recordType}")
    public Result<List<NetWorkSelectResponse>> network(@PathVariable Integer recordType) throws SocketException {
        return Result.ok(netWorkService.networks(recordType));
    }

    @PostMapping("page")
    public Result<PageUtils<ParsingRecord>> queryPage(@RequestBody ParsingRecord parsingRecord) {
        PageUtils<ParsingRecord> page = parsingRecordService.queryPage(parsingRecord);
        return Result.ok(page);
    }


    @GetMapping("{id}")
    public Result<ParsingRecord> getId(@PathVariable("id") Long id){
        return Result.ok(parsingRecordService.getById(id));
    }

    @PostMapping("add")
    public Result<String> add(@RequestBody
                      @Validated(ValidGroup.SaveGroup.class) ParsingRecord parsingRecord) throws Exception {
        parsingRecordService.add(parsingRecord);
        return Result.ok();
    }

    @PostMapping("modify")
    public Result<String> modify(@RequestBody
                         @Validated(ValidGroup.UpdateGroup.class) ParsingRecord parsingRecord) throws Exception {
        parsingRecordService.modify(parsingRecord);
        return Result.ok();
    }

    @DeleteMapping("delete/{id}")
    public Result<String> delete(@PathVariable Long id) throws Exception {
        parsingRecordService.delete(id);
        return Result.ok();
    }

}
