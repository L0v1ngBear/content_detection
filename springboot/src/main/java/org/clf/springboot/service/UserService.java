package org.clf.springboot.service;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import jakarta.annotation.Resource;
import org.clf.springboot.common.enums.ResultCodeEnum;
import org.clf.springboot.entity.Account;
import org.clf.springboot.exception.CustomException;
import org.clf.springboot.entity.User;
import org.clf.springboot.mapper.UserMapper;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


@Service
public class UserService {

    private final Logger logger = LoggerFactory.getLogger(UserService.class);

    private static final int BCRYPT_LOG_ROUNDS = 11;

    private static final int SHARED_COUNT = 16;

    private static final String REDIS_KEY_PREFIX = "stat_";

    private static final String STAT_TYPE_PICTURE_REVIEW = "picture_review_count";

    private static final long REDIS_KEY_EXPIRE_SECONDS = 32 * 24 * 3600;

    @Resource
    private UserMapper userMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    public void register(Account account) {
        User user = new User();
        BeanUtils.copyProperties(account, user);
        addUser(user);
        logger.info("用户注册成功，用户名:{}",user.getUsername() );
    }

    private void addUser(User user) throws CustomException {
        if (user == null) {
            throw new CustomException(ResultCodeEnum.PARAM_ERROR);
        }
        String username = user.getUsername();
        User dbUser = userMapper.selectByUsername(username);
        if (ObjectUtil.isNotNull(dbUser)) {
            logger.error("dbuser不为空");
            throw new CustomException(ResultCodeEnum.USER_EXIST_ERROR);
        }
        if (StrUtil.isBlank(username)) {
            user.setUsername("user_" + System.currentTimeMillis());
        }
        String password = user.getPassword();
        if (StrUtil.isEmpty(password)) {
            logger.error("密码为空");
            throw new CustomException(ResultCodeEnum.PARAM_ERROR);
        }
        String encrypedPassword = BCrypt.hashpw(password, BCrypt.gensalt(BCRYPT_LOG_ROUNDS));
        user.setPassword(encrypedPassword);
        user.setNickname(username);
        user.setStatus(1);
        try {
            userMapper.insert(user);
        } catch (Exception e) {
            throw new CustomException("500", "新增用户失败", e);
        }
    }

    public void changePassword(Account account) throws CustomException {
        User user = userMapper.selectById(account.getId());
        if (user == null) {
            throw new CustomException("400", "用户id不正确");
        }
        String oldPassword = account.getPassword();
        String dbPassword = user.getPassword();
        String newPassword = account.getNewPassword();
        if (!BCrypt.checkpw(oldPassword, dbPassword)) {
            throw new CustomException("400", "旧密码错误");
        }
        user.setPassword(BCrypt.hashpw(newPassword, BCrypt.gensalt(BCRYPT_LOG_ROUNDS)));
        try {
            userMapper.updateById(user);
        } catch (Exception e) {
            throw new CustomException("500", "修改密码失败", e);
        }
    }

    public void countPicReview(Long userId) {
        try {
            if (userId == null) {
                throw new CustomException(ResultCodeEnum.USER_NOT_LOGIN);
            }
            String currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
            int sharedIndex = Math.abs(userId.hashCode() % SHARED_COUNT);
            String redisKey = String.format("%s%s_%s_%d", REDIS_KEY_PREFIX, STAT_TYPE_PICTURE_REVIEW, currentMonth, sharedIndex);
            String redisField = userId.toString();

            stringRedisTemplate.opsForHash().increment(redisKey, redisField, 1);

            // 3. 仅首次设置过期时间（避免重复调用EXPIRE）
            if (Boolean.FALSE.equals(stringRedisTemplate.hasKey(redisKey))) {
                stringRedisTemplate.expire(redisKey, Duration.ofDays(REDIS_KEY_EXPIRE_SECONDS));
            }
        } catch (Exception e) {
            logger.error("统计用户图片审核数量失败");
        }
    }
}
