package org.clf.springboot.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.clf.springboot.common.Result;
import org.clf.springboot.service.WebService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "网页通用接口")
@RestController
@RequestMapping("/api")
public class WebController {

    @Resource
    private WebService webService;

    /**
     * 获取历史审核的图片
     * @param userId
     * @return
     */
//    @GetMapping("/history/review/picture/#{userId}")
//    public Result getHistoryPicture(@PathVariable("userId") String userId) {
//        webService.getHistoryPicture(userId);
//        return Result.success();
//    }
}
