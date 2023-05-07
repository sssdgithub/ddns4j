package top.sssd.ddns.controller;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import top.sssd.ddns.common.Result;
import top.sssd.ddns.common.utils.PageUtils;
import top.sssd.ddns.common.valid.ValidGroup;
import top.sssd.ddns.model.entity.ParsingRecord;
import top.sssd.ddns.service.IParsingRecordService;

import javax.annotation.Resource;

/**
 * @author sssd
 * @careate 2023-05-02-5:09
 */
@RestController
@RequestMapping("parsingRecord")
@Validated
public class ParsingRecordController {

    @Resource
    private IParsingRecordService parsingRecordService;

    @PostMapping("page")
    public Result queryPage(@RequestBody ParsingRecord parsingRecord) {
        PageUtils page = parsingRecordService.queryPage(parsingRecord);
        return Result.ok(page);
    }


    @GetMapping("{id}")
    public Result getId(@PathVariable("id") Long id){
        return Result.ok(parsingRecordService.getById(id));
    }

    @PostMapping("add")
    public Result add(@RequestBody
                      @Validated(ValidGroup.SaveGroup.class) ParsingRecord parsingRecord) throws Exception {
        parsingRecordService.add(parsingRecord);
        return Result.ok();
    }

    @PostMapping("modify")
    public Result modify(@RequestBody
                         @Validated(ValidGroup.UpdateGroup.class) ParsingRecord parsingRecord) throws Exception {
        parsingRecordService.modify(parsingRecord);
        return Result.ok();
    }

    @DeleteMapping("delete/{id}")
    public Result delete(@PathVariable Long id) throws Exception {
        parsingRecordService.delete(id);
        return Result.ok();
    }

}
