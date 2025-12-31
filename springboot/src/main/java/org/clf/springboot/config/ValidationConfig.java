package org.clf.springboot.config;

import jakarta.validation.Validator;
import org.clf.springboot.utils.ValidationUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@Configuration
public class ValidationConfig {
    /**
     * 注册校验器Bean（Spring自动管理）
     */
    @Bean
    public Validator validator() {
        return new LocalValidatorFactoryBean();
    }

    /**
     * 初始化通用校验工具类（将Spring管理的校验器注入工具类）
     */
    @Bean
    public void initValidationUtils(Validator validator) {
        ValidationUtils.initValidator(validator);
    }
}