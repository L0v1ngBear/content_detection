package org.clf.springboot.utils;

import jakarta.annotation.Resource;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.Set;
import java.util.StringJoiner;

/**
 * 通用必填字段校验工具类
 * 支持任意实体类的注解式校验，复用性强，无需重复编写校验逻辑
 */
public class ValidationUtils {
    // 日志记录（便于排查校验失败问题）
    private static final Logger logger = LoggerFactory.getLogger(ValidationUtils.class);

    // 静态持有校验器（Spring环境下可通过注入获取，非Spring环境需手动初始化）
    private static Validator validator;

    /**
     * 初始化校验器（非Spring环境调用，Spring环境无需手动调用，直接注入Validator即可）
     */
    public static void initValidator(Validator validator) {
        ValidationUtils.validator = validator;
    }

    /**
     * 核心方法：校验实体类的必填字段（返回校验结果是否通过）
     * @param obj 待校验的实体对象（不能为空）
     * @param <T> 实体对象泛型
     * @return true=校验通过，false=校验失败（存在必填字段为空等问题）
     */
    public static <T> boolean validate(T obj) {
        return validate(obj, false) == null;
    }

    /**
     * 核心方法：校验实体类的必填字段（返回详细错误信息）
     * @param obj 待校验的实体对象（不能为空）
     * @param <T> 实体对象泛型
     * @return 校验失败时返回拼接后的错误信息，校验通过时返回null
     */
    public static <T> String validateWithMsg(T obj) {
        return validate(obj, false);
    }

    /**
     * 增强方法：校验实体类（支持分组校验，适配多场景校验需求）
     * @param obj 待校验的实体对象
     * @param groups 校验分组（如新增场景、更新场景）
     * @param <T> 实体对象泛型
     * @return 校验失败时返回拼接后的错误信息，校验通过时返回null
     */
    public static <T> String validateWithMsg(T obj, Class<?>... groups) {
        return validate(obj, true, groups);
    }

    /**
     * 底层统一校验逻辑（封装重复代码，提高可维护性）
     * @param obj 待校验的实体对象
     * @param returnMsg 是否返回详细错误信息
     * @param groups 校验分组
     * @param <T> 实体对象泛型
     * @return 按需返回错误信息或null
     */
    private static <T> String validate(T obj, boolean returnMsg, Class<?>... groups) {
        // 1. 入参校验：待校验对象不能为空
        if (obj == null) {
            String errorMsg = "待校验对象不能为空！";
            logger.error(errorMsg);
            return returnMsg ? errorMsg : null;
        }

        // 2. 校验器判空（避免空指针）
        if (validator == null) {
            String errorMsg = "校验器未初始化！请先调用initValidator()方法初始化或在Spring环境下注入Validator！";
            logger.error(errorMsg);
            return returnMsg ? errorMsg : null;
        }

        // 3. 执行字段校验
        Set<ConstraintViolation<T>> violationSet = validator.validate(obj, groups);

        // 4. 处理校验结果
        if (CollectionUtils.isEmpty(violationSet)) {
            // 校验通过：无错误信息
            return null;
        } else {
            // 校验失败：拼接详细错误信息
            StringJoiner errorMsgJoiner = new StringJoiner("; ", "字段校验失败：", "");
            for (ConstraintViolation<T> violation : violationSet) {
                // violation.getMessage()：获取@NotNull/@NotBlank注解中配置的错误信息
                // violation.getPropertyPath()：获取校验失败的字段名（可选，便于精准定位）
                errorMsgJoiner.add(violation.getPropertyPath() + "：" + violation.getMessage());
            }
            String errorMsg = errorMsgJoiner.toString();

            // 记录错误日志（便于后续排查问题）
            logger.warn(errorMsg);

            // 按需返回错误信息
            return returnMsg ? errorMsg : null;
        }
    }
}