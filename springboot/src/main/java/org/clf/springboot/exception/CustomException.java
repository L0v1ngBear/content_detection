package org.clf.springboot.exception;

import lombok.Getter;
import lombok.Setter;
import org.clf.springboot.common.enums.ResultCodeEnum;

/**
 * 自定义业务异常（适配ResultCodeEnum，支持获取枚举对象）
 */
@Getter
@Setter
public class CustomException extends RuntimeException {

    private int code;          // 业务码
    private String msg;        // 提示信息
    private ResultCodeEnum resultCodeEnum; // 新增：关联的枚举对象（关键）

    // ========== 原有构造器（保留，兼容旧代码） ==========
    public CustomException(String msg) {
        super(msg);
        this.code = 400;       // 参数类异常默认400，合理
        this.msg = msg;
        this.resultCodeEnum = ResultCodeEnum.PARAM_ERROR;
    }

    public CustomException(int code, String msg) {
        super(msg);
        this.code = code;
        this.msg = msg;
        this.resultCodeEnum = null;
    }

    public CustomException(int code, String msg, Throwable cause) {
        super(msg, cause);
        this.code = code;
        this.msg = msg;
        this.resultCodeEnum = null;
    }

    // ========== 核心构造器（优化：保存枚举对象） ==========
    public CustomException(ResultCodeEnum resultCodeEnum) {
        super(resultCodeEnum.getMsg()); // 父类异常消息
        this.code = resultCodeEnum.getCode(); // 枚举业务码
        this.msg = resultCodeEnum.getMsg();   // 枚举提示信息
        this.resultCodeEnum = resultCodeEnum; // 保存枚举对象（关键）
    }

    // 重载：支持传入异常根因（便于排查问题）
    public CustomException(ResultCodeEnum resultCodeEnum, Throwable cause) {
        super(resultCodeEnum.getMsg(), cause);
        this.code = resultCodeEnum.getCode();
        this.msg = resultCodeEnum.getMsg();
        this.resultCodeEnum = resultCodeEnum;
    }

    // ========== 新增方法：获取枚举对象（全局异常处理器需要） ==========
    /**
     * 获取关联的ResultCodeEnum枚举（为了取HTTP状态码）
     * 若未关联枚举，返回SYSTEM_ERROR兜底
     */
    public ResultCodeEnum getResultCode() {
        if (this.resultCodeEnum != null) {
            return this.resultCodeEnum;
        }
        // 兜底：无枚举时默认返回系统异常
        return ResultCodeEnum.SYSTEM_ERROR;
    }
}
