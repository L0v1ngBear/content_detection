package org.clf.springboot.common;

import org.clf.springboot.common.enums.ResultCodeEnum;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Result {
    private int code; // 业务码（比如401、5001）
    private String msg; // 提示信息
    private Object data; // 数据体

    // ========== 原有方法（保留，兼容旧代码） ==========
    public static Result success() {
        Result result = new Result();
        result.setCode(200);
        result.setMsg("success");
        return result;
    }

    public static Result success(Object data) {
        Result result = success();
        result.setData(data);
        return result;
    }

    public static Result error() {
        Result result = new Result();
        result.setCode(500);
        result.setMsg("系统异常");
        return result;
    }

    public static Result error(int code, String msg) {
        Result result = new Result();
        result.setCode(code);
        result.setMsg(msg);
        return result;
    }

    public static Result error(String msg) {
        Result result = new Result();
        result.setCode(500);
        result.setMsg(msg);
        return result;
    }

    // ========== 新增方法（核心：适配ResultCodeEnum） ==========
    /**
     * 通过枚举快速构建返回结果（无需手动传code和msg）
     */
    public static Result error(ResultCodeEnum resultCode) {
        Result result = new Result();
        result.setCode(resultCode.getCode()); // 枚举中的业务码
        result.setMsg(resultCode.getMsg());   // 枚举中的提示信息
        result.setData(null);                 // 异常默认无数据
        return result;
    }

    /**
     * 重载：支持自定义数据（极少用，异常一般无数据）
     */
    public static Result error(ResultCodeEnum resultCode, Object data) {
        Result result = error(resultCode);
        result.setData(data);
        return result;
    }
}