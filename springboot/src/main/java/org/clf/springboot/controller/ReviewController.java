package org.clf.springboot.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.clf.springboot.common.Result;
import org.clf.springboot.common.ReviewResult;
import org.clf.springboot.common.enums.ResultCodeEnum;
import org.clf.springboot.exception.CustomException;
import org.clf.springboot.service.ReviewService;
import org.clf.springboot.service.UserService;
import org.clf.springboot.utils.UserContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.concurrent.ThreadPoolExecutor;

@Tag(name = "机器审核接口")
@RestController
@RequestMapping("/review")
public class ReviewController {

    @Resource
    ReviewService reviewService;

    @Resource(name = "statThreadPool")
    private ThreadPoolExecutor statThreadPool;

    @Operation(summary = "图片审核")
    @PostMapping("/picture")
    public Result reviewPicture(@RequestParam("file") MultipartFile file) {
        String taskId = reviewService.pictureView(file);
        Long userId = UserContextHolder.getUserId();
        if (userId != null) {
            statThreadPool.execute(() -> {
                reviewService.savePicReview(userId);
            });
        }
        return Result.success(taskId);
    }

    @PostMapping("/video")
    public Result reviewVideo(@RequestParam("file") MultipartFile file) {
        String taskId = reviewService.videoView(file);
        return Result.success(taskId);
    }

    @GetMapping("/picture/result")
    public Result getPictureResult(String taskId) {
        if (taskId == null) {
            throw new CustomException(ResultCodeEnum.PARAM_ERROR);
        }
        Long userId = UserContextHolder.getUserId();
        if (userId == null) {
            throw new CustomException(ResultCodeEnum.USER_NOT_LOGIN);
        }
        return Result.success(reviewService.getPictureResult(taskId, userId));
    }

}
