package org.clf.springboot.loginStrategy;

import org.clf.springboot.exception.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class LoginStrategyFactory {

    private final Map<String, LoginStrategy> loginStrategies = new HashMap<>();

    @Autowired
    public LoginStrategyFactory(List<LoginStrategy> strategies) {
        for (LoginStrategy strategy : strategies) {
            loginStrategies.put(strategy.getSupportType(), strategy);
        }
    }

    public LoginStrategy getStrategy(String supportType) {
        LoginStrategy strategy = loginStrategies.get(supportType);
        if (strategy == null) {
            throw new CustomException("不支持的登录类型");
        }
        return strategy;
    }

}
