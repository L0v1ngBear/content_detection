package org.clf.springboot.exception;

import lombok.Getter;
import lombok.Setter;
import org.clf.springboot.common.enums.ResultCodeEnum;

@Getter
@Setter
public class CustomException extends RuntimeException {

    private String code;
    private String msg;

    public CustomException(String msg) {
        super(msg);
        this.code = "400";
        this.msg = msg;
    }

    public CustomException(String code, String msg) {
        super(msg);
        this.code = code;
        this.msg = msg;
    }

    public CustomException(String code, String msg, Throwable cause) {
        super(msg, cause);
        this.code = code;
        this.msg = msg;
    }

    public CustomException(ResultCodeEnum resultCodeEnum) {
        this.code = resultCodeEnum.code;
        this.msg = resultCodeEnum.msg;
    }
}
