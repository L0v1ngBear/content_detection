package org.clf.springboot.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.clf.springboot.common.Result;
import org.clf.springboot.common.enums.ResultCodeEnum;
import org.clf.springboot.dto.MergeChunksDTO;
import org.clf.springboot.dto.MergeResultDTO;
import org.clf.springboot.dto.UploadedChunksDTO;
import org.clf.springboot.exception.CustomException;
import org.clf.springboot.service.ReviewService;
import org.clf.springboot.utils.MinIOUtils;
import org.clf.springboot.utils.UserContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

@Tag(name = "机器审核接口")
@RestController
@RequestMapping("/review")
public class ReviewController {

    private static final Logger logger = LoggerFactory.getLogger(ReviewController.class);

    @Resource
    ReviewService reviewService;

    @Resource(name = "statThreadPool")
    private ThreadPoolExecutor statThreadPool;

    @Resource
    private MinIOUtils minIOUtils;

    @Operation(summary = "图片审核")
    @PostMapping("/picture")
    public Result reviewPicture(@RequestParam("file") MultipartFile file) {
        String taskId = reviewService.pictureView(file);
        Long userId = UserContextHolder.getUserId();
        if (userId != null) {
            statThreadPool.execute(() -> {
                reviewService.savePicReview(userId, "image");
            });
        }
        return Result.success(taskId);
    }

    @GetMapping("/result")
    public Result getResult(String taskId) {
        if (taskId == null) {
            throw new CustomException(ResultCodeEnum.PARAM_ERROR);
        }
        Long userId = UserContextHolder.getUserId();
        if (userId == null) {
            throw new CustomException(ResultCodeEnum.USER_NOT_LOGIN);
        }
        return Result.success(reviewService.getResult(taskId, userId));
    }

    @GetMapping("/video/check")
    public Result checkUploadedVideo(@RequestParam String fileHash, @RequestParam String fileName) {
        UploadedChunksDTO dto = new UploadedChunksDTO();
        List<Integer> uploadedChunks = minIOUtils.listUploadedChunks(fileHash);
        dto.setUploadedChunks(uploadedChunks);
        return Result.success(dto);
    }

    @PostMapping("/chunk")
    public Result uploadChunk(@RequestParam("file") MultipartFile file,
                              @RequestParam String fileHash,
                              @RequestParam String fileName,
                              @RequestParam int chunkIndex,
                              @RequestParam int totalChunks) {
        try {
            minIOUtils.uploadChunk(file, fileHash, chunkIndex, fileName);
        } catch (Exception e) {
            return Result.error("分片上传失败");
        }
        return Result.success();
    }

    @PostMapping("/merge")
    public Result mergeChunks(@RequestBody MergeChunksDTO request) {

        try {
            String objectName = minIOUtils.mergeChunks(request.getFileHash(), request.getFileName(), request.getTotalChunks(), request.getMd5());
            String taskId = reviewService.videoReview(objectName, request.getFileName());
            Long userId = UserContextHolder.getUserId();
            if (userId != null) {
                statThreadPool.execute(() -> {
                    reviewService.savePicReview(userId, "video");
                });
            }
            MergeResultDTO dto = new MergeResultDTO();
            dto.setTaskId(taskId);
            return Result.success(dto);
        } catch (Exception e) {
            minIOUtils.deleteAllChunks(request.getFileHash());
            logger.error("合并分片失败", e);
            return Result.error("合并分片失败");
        }
    }
}
