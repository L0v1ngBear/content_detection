package org.clf.springboot.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.clf.springboot.common.Result;
import org.clf.springboot.common.enums.ResultCodeEnum;
import org.clf.springboot.entity.Account;
import org.clf.springboot.entity.User;
import org.clf.springboot.exception.CustomException;
import org.clf.springboot.mapper.UserMapper;
import org.clf.springboot.service.UserService;
import org.clf.springboot.utils.MinIOUtils;
import org.clf.springboot.utils.UserContextHolder;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "用户相关接口")
@RestController
@RequestMapping("/user")
public class UserController {

    @Value("${minio.bucketName.avatarBucket}")
    private String avatarBucket;

    @Resource
    UserService userService;

    @Resource
    private MinIOUtils minIOUtils;
    @Autowired
    private UserMapper userMapper;

    @Operation(summary = "修改密码")
    @PutMapping("/{userId}/password")
    public Result password(@Valid @PathVariable("userId") Long userId,
                           @RequestBody Account account,
                           @RequestAttribute("userId") Long accountId) {
        if (!userId.equals(accountId)) {
            throw new CustomException(ResultCodeEnum.NO_PERMISSION);
        }
        if (StrUtil.isBlank(account.getPassword())
                || StrUtil.isBlank(account.getNewPassword())) {
            throw new CustomException(ResultCodeEnum.PARAM_ERROR);
        }
        account.setId(userId);
        userService.changePassword(account);
        return Result.success();
    }

    @GetMapping("/info")
    public Result info() {
        if (UserContextHolder.getUser() == null) {
            throw new CustomException(ResultCodeEnum.NO_PERMISSION);
        }
        User resUser = new User();
        User user = UserContextHolder.getUser();
        resUser.setUsername(user.getUsername());
        resUser.setPhone(user.getPhone());
        resUser.setEmail(user.getEmail());
        resUser.setAvatar(user.getAvatar());
        return Result.success(resUser);
    }

    @PostMapping("/upload/avatar")
    public Result uploadAvatar(@RequestParam("file") MultipartFile file) {
        User user = UserContextHolder.getUser();
        if (user == null) {
            throw new CustomException(ResultCodeEnum.NO_PERMISSION);
        }
        if (file.isEmpty()) {
            throw new CustomException(ResultCodeEnum.PARAM_ERROR);
        }
        // 最大2mb
        long maxSize = 2 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            throw new CustomException(ResultCodeEnum.PARAM_ERROR);
        }

        String contentType = file.getContentType();
        boolean isAllowed = false;
        String[] allowTypes = {"image/jpeg", "image/png"};
        for (String type : allowTypes) {
            if (type.equalsIgnoreCase(contentType)) {
                isAllowed = true;
                break;
            }
        }
        if (!isAllowed) {
            throw new CustomException(ResultCodeEnum.TYPE_ERROR);
        }

        String avatarUrl;

        try {
            Long userId = UserContextHolder.getUserId();
            String originalFilename = file.getOriginalFilename();
            String suffix = FileUtil.extName(originalFilename);
            String fileName = userId + '-' + suffix;

            minIOUtils.uploadFile(avatarBucket, file, fileName, true);
            // 生成访问URL（MinIO配置了公开访问/临时签名）

            avatarUrl = minIOUtils.getPublicUrl(avatarBucket, fileName);

            userService.updateUserAvatar(userId, avatarUrl);

            return Result.success();
        } catch (Exception e) {
            throw new RuntimeException("更新用户头像信息失败", e);
        }
    }
}

