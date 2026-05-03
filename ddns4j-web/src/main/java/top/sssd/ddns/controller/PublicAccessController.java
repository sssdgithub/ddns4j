package top.sssd.ddns.controller;

import lombok.Data;
import org.springframework.web.bind.annotation.*;
import top.sssd.ddns.common.AmisResult;

import java.util.HashMap;

import static top.sssd.ddns.common.constant.DDNSConstant.PUBLIC_ACCESS_DISABLED_KEY;
import static top.sssd.ddns.common.constant.DDNSConstant.publicAccessDisabledMap;

/**
 * @author sssd
 * @careate 2024-01-01-18:08
 */
@RestController
@RequestMapping("publicAccess")
public class PublicAccessController {

    // 禁止公网访问开关相关接口-start
    @Data
    static class PublicAccessDisabled{
        private Boolean value;
    }

    @PostMapping("publicAccessDisabled")
    public AmisResult<String> UpdatePublicAccessDisabled(@RequestBody PublicAccessDisabled publicAccessDisabled){
        publicAccessDisabledMap.put(PUBLIC_ACCESS_DISABLED_KEY,publicAccessDisabled.getValue());
        return AmisResult.ok();
    }


    @GetMapping("publicAccessDisabled")
    public AmisResult<HashMap<String, Boolean>> publicAccessDisabled(){
        Boolean publicAccessDisabled = publicAccessDisabledMap.get(PUBLIC_ACCESS_DISABLED_KEY);
        HashMap<String, Boolean> resultMap = new HashMap<>();
        resultMap.put("publicAccessDisabled",publicAccessDisabled);
        return AmisResult.ok(resultMap);
    }
    // 禁止公网访问开关相关接口-end
}
