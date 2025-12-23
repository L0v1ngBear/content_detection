package org.clf.springboot.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.clf.springboot.common.Result;
import org.clf.springboot.dto.LoginRequestDTO;
import org.clf.springboot.dto.LoginResponseDTO;
import org.clf.springboot.service.AuthService;
import org.springframework.web.bind.annotation.*;

@Tag(name = "多种登录方式接口")
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Resource
    private AuthService authService;

    @Operation(summary = "登录")
    @PostMapping("/login")
    public Result login (@Valid @RequestBody LoginRequestDTO loginRequestDTO) {
        LoginResponseDTO loginResponseDTO = authService.login(loginRequestDTO);
        return Result.success(loginResponseDTO);
    }

    @PostMapping("/sendCode")
    public Result sendSmsCode(@RequestParam String phone) {
        authService.sendSmsCode(phone);
        return Result.success();
    }

    @PostMapping("/refreshToken")
    public Result refreshToken(@RequestParam String refreshToken) {
        LoginResponseDTO loginResponseDTO = authService.refreshToken(refreshToken);
        return Result.success(loginResponseDTO);
    }

    @DeleteMapping("/logout")
    public Result logout(@RequestParam String refreshToken) {
        authService.logout(refreshToken);
        return Result.success();
    }

// TODO 暂时无法实现微信登录，受限于网站网址
//    @GetMapping("/wechat/auth")
//    public Result wechat() {
//        String url = justAuthUtils.build
//    }
}
