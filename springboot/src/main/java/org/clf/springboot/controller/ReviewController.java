package org.clf.springboot.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.clf.springboot.common.Result;
import org.clf.springboot.common.ReviewResult;
import org.clf.springboot.service.ReviewService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "机器审核接口")
@RestController
@RequestMapping("/review")
public class ReviewController {

    @Resource
    ReviewService reviewService;

    @Operation(summary = "图片审核")
    @PostMapping("/picture")
    public Result reviewPicture(@RequestParam("file") MultipartFile file, @RequestHeader (value = "RequestId") String requestId) {
        reviewService.pictureView(file, requestId);
        return Result.success();
    }

}
