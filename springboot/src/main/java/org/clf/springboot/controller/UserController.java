package org.clf.springboot.controller;

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
import org.clf.springboot.service.UserService;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户相关接口")
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    UserService userService;

//    @Operation(summary = "用户登录")
//    @PostMapping("/login")
//    public Result login(@Valid @RequestBody User user) {
//        String token = userService.login(user.getUsername(), user.getPassword());
//        if (token == null) {
//            return Result.error("用户名或密码错误");
//        }
//        return Result.success(token);
//    }

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result register(@Valid @RequestBody Account account) {
        userService.register(account);
        return Result.success();
    }

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
}
