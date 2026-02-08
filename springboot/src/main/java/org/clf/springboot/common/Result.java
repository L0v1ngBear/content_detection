package org.clf.springboot.common;

import org.clf.springboot.common.enums.ResultCodeEnum;
import lombok.Getter;
import lombok.Setter;

/**
 * 通用返回结果类
 * 新增 httpStatus 字段：存储HTTP状态码（如200、401、403、500）
 * code：业务码（如200、5001、40101）
 * msg：提示信息
 * data：数据体
 */
@Setter
@Getter
public class Result {
    // HTTP状态码（如200成功、401未授权、403禁止访问、500服务器异常）
    private int httpStatus;
    // 业务码（区分具体业务异常，比如40101=Token过期、5001=数据库异常）
    private int code;
    // 提示信息
    private String msg;
    // 数据体
    private Object data;

    // ========== 原有方法兼容（默认HTTP状态码200/500） ==========
    /**
     * 成功返回（默认HTTP 200 + 业务码200）
     */
    public static Result success() {
        Result result = new Result();
        result.setHttpStatus(200); // 默认HTTP成功状态码
        result.setCode(200);
        result.setMsg("success");
        return result;
    }

    /**
     * 成功返回（带数据，默认HTTP 200 + 业务码200）
     */
    public static Result success(Object data) {
        Result result = success();
        result.setData(data);
        return result;
    }

    /**
     * 通用异常（默认HTTP 500 + 业务码500）
     */
    public static Result error() {
        Result result = new Result();
        result.setHttpStatus(500); // 默认HTTP服务器错误
        result.setCode(500);
        result.setMsg("系统异常");
        return result;
    }

    /**
     * 自定义业务异常（默认HTTP 500）
     */
    public static Result error(int code, String msg) {
        Result result = new Result();
        result.setHttpStatus(500); // 默认HTTP 500
        result.setCode(code);
        result.setMsg(msg);
        return result;
    }

    /**
     * 自定义提示信息（默认HTTP 500 + 业务码500）
     */
    public static Result error(String msg) {
        Result result = new Result();
        result.setHttpStatus(500);
        result.setCode(500);
        result.setMsg(msg);
        return result;
    }

    public static Result success(int code, String msg, Object data) {
        Result result = new Result();
        result.setHttpStatus(200);
        result.setCode(code);
        result.setMsg(msg);
        result.setData(data);
        return result;
    }

    // ========== 新增：支持HTTP状态码的核心方法 ==========
    /**
     * 成功返回（自定义HTTP状态码 + 业务码 + 数据）
     * 场景：如201创建成功、204无内容等特殊成功场景
     */
    public static Result success(int httpStatus, int code, String msg, Object data) {
        Result result = new Result();
        result.setHttpStatus(httpStatus);
        result.setCode(code);
        result.setMsg(msg);
        result.setData(data);
        return result;
    }

    /**
     * 异常返回（自定义HTTP状态码 + 业务码 + 提示信息）
     */
    public static Result error(int httpStatus, int code, String msg) {
        Result result = new Result();
        result.setHttpStatus(httpStatus);
        result.setCode(code);
        result.setMsg(msg);
        result.setData(null);
        return result;
    }

    /**
     * 异常返回（自定义HTTP状态码 + 业务码 + 提示信息 + 数据）
     * 场景：如400参数错误时返回错误字段明细
     */
    public static Result error(int httpStatus, int code, String msg, Object data) {
        Result result = new Result();
        result.setHttpStatus(httpStatus);
        result.setCode(code);
        result.setMsg(msg);
        result.setData(data);
        return result;
    }

    // ========== 适配枚举：支持HTTP状态码 ==========
    /**
     * 通过枚举构建异常结果（默认HTTP状态码：枚举code>=400则设为400，否则500）
     * 可根据实际枚举规则调整HTTP状态码映射
     */
    public static Result error(ResultCodeEnum resultCode) {
        Result result = new Result();
        // 枚举业务码映射HTTP状态码（示例规则）：
        // - 4xx业务码 → HTTP 400（客户端错误）
        // - 5xx业务码 → HTTP 500（服务器错误）
        // - 其他 → 对应业务码或默认500
        int httpStatus = switch (resultCode.getCode() / 100) {
            case 4 -> 400; // 4xx业务码 → HTTP 400
            case 5 -> 500; // 5xx业务码 → HTTP 500
            case 2 -> 200; // 2xx业务码 → HTTP 200（极少用，枚举一般是异常）
            default -> 500; // 默认服务器错误
        };
        result.setHttpStatus(httpStatus);
        result.setCode(resultCode.getCode());
        result.setMsg(resultCode.getMsg());
        result.setData(null);
        return result;
    }

    /**
     * 重载：枚举 + 自定义HTTP状态码 + 数据
     */
    public static Result error(ResultCodeEnum resultCode, int httpStatus, Object data) {
        Result result = error(resultCode);
        result.setHttpStatus(httpStatus); // 覆盖默认HTTP状态码
        result.setData(data);
        return result;
    }

    // ========== 快捷方法（常用HTTP状态码） ==========
    /**
     * 401未授权（HTTP 401 + 自定义业务码 + 提示）
     * 场景：Token过期、未登录
     */
    public static Result unauthorized(int code, String msg) {
        return error(401, code, msg);
    }

    /**
     * 403禁止访问（HTTP 403 + 自定义业务码 + 提示）
     * 场景：权限不足
     */
    public static Result forbidden(int code, String msg) {
        return error(403, code, msg);
    }

    /**
     * 400参数错误（HTTP 400 + 自定义业务码 + 提示 + 错误数据）
     */
    public static Result badRequest(int code, String msg, Object data) {
        return error(400, code, msg, data);
    }

    /**
     * 404资源不存在（HTTP 404 + 自定义业务码 + 提示）
     */
    public static Result notFound(int code, String msg) {
        return error(404, code, msg);
    }
}