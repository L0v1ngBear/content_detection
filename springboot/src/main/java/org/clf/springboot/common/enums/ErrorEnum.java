package org.clf.springboot.common.enums;

public enum ErrorEnum {
    NOT_LOGIN("获取当前用户信息失败,请重新登录");

    public String msg;

    ErrorEnum(String s) {
        this.msg = s;
    }
}
