package org.clf.springboot.common.enums;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

/**
 * 结果状态枚举（新增HTTP状态码属性）
 */
@Getter
public enum ResultCodeEnum {

    // 成功（业务码200，HTTP状态码200）
    SUCCESS(200, "成功", HttpStatus.OK),
    // 参数异常（业务码400，HTTP状态码400）
    PARAM_ERROR(400, "参数异常", HttpStatus.BAD_REQUEST),

    TOKEN_INVALID_ERROR(401, "请登录", HttpStatus.UNAUTHORIZED),
    // 验证失败（业务码401，HTTP状态码401）
    TOKEN_CHECK_ERROR(401, "验证失败，请重新登录", HttpStatus.UNAUTHORIZED),
    // 参数缺失（业务码4001，HTTP状态码400）
    PARAM_LOST_ERROR(4001, "参数缺失", HttpStatus.BAD_REQUEST),

    // 系统异常（业务码500，HTTP状态码500）
    SYSTEM_ERROR(500, "系统异常", HttpStatus.INTERNAL_SERVER_ERROR),
    // 用户名已存在（业务码5001，HTTP状态码400）
    USER_EXIST_ERROR(5001, "用户名已存在", HttpStatus.BAD_REQUEST),
    // 用户未登录（业务码5002，HTTP状态码401）
    USER_NOT_LOGIN(5002, "用户未登录", HttpStatus.UNAUTHORIZED),
    // 账号或密码错误（业务码5003，HTTP状态码400）
    USER_ACCOUNT_ERROR(5003, "账号或密码错误", HttpStatus.BAD_REQUEST),
    // 用户不存在（业务码5004，HTTP状态码400）
    USER_NOT_EXIST_ERROR(5004, "用户不存在", HttpStatus.BAD_REQUEST),
    // 原密码输入错误（业务码5005，HTTP状态码400）
    PARAM_PASSWORD_ERROR(5005, "原密码输入错误", HttpStatus.BAD_REQUEST),
    // 无权操作（业务码5006，HTTP状态码403）
    NO_PERMISSION(5006, "无权操作", HttpStatus.FORBIDDEN),
    ;

    // 原有属性：业务码、提示信息
    public int code;
    public String msg;
    // 新增属性：HTTP状态码（基于Spring的HttpStatus枚举，规范且不易出错）
    public HttpStatus httpStatus;

    // 1. 保留原有构造器（兼容旧代码）
    ResultCodeEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    // 2. 新增构造器（关联HTTP状态码）
    ResultCodeEnum(int code, String msg, HttpStatus httpStatus) {
        this.code = code;
        this.msg = msg;
        this.httpStatus = httpStatus;
    }

    // 新增getter方法（推荐用getter，而非直接访问public属性，符合封装规范）
    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    // 可选：获取HTTP状态码的数值（比如401、200），方便直接设置响应码
    public int getHttpStatusCode() {
        return httpStatus != null ? httpStatus.value() : HttpStatus.INTERNAL_SERVER_ERROR.value();
    }
}
