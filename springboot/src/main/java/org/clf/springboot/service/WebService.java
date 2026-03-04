package org.clf.springboot.service;

import cn.hutool.core.date.DateTime;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.clf.springboot.common.enums.ResultCodeEnum;
import org.clf.springboot.dto.MsgRequestDTO;
import org.clf.springboot.dto.StaticsResponseDTO;
import org.clf.springboot.entity.Msg;
import org.clf.springboot.entity.PictureStatics;
import org.clf.springboot.exception.CustomException;
import org.clf.springboot.mapper.MsgMapper;
import org.clf.springboot.mapper.PictureStatMapper;
import org.clf.springboot.utils.UserContextHolder;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class WebService {

    @Value("${minio.redisKey}")
    private String REDISKEY;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private MsgMapper msgMapper;

    @Resource
    private PictureStatMapper pictureStatMapper;

    private static final int SHARED_COUNT = 16;
    private static final String REDIS_KEY_PREFIX = "stat_";
    private static final String STAT_TYPE_PICTURE_REVIEW = "picture_review_count";
    private static final String STAT_TYPE_VIDEO_REVIEW = "video_review_count";


    public List<Msg> getLatestMsgList(String userId, Integer pageSize) {
        LambdaQueryWrapper<Msg> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Msg::getUserId, userId)// 条件：用户ID匹配
                .eq(Msg::getIsRead, 0) // 条件，0未读
                .orderByDesc(Msg::getCreateTime) // 倒序：按创建时间
                .select(Msg::getType, Msg::getContent, Msg::getCreateTime, Msg::getIsRead)
                .last("LIMIT " + pageSize); // 限制返回条数
        return msgMapper.selectList(queryWrapper);

    }

    public void setMsgAllRead(String userId) {
        LambdaUpdateWrapper<Msg> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Msg::getUserId, userId)
                .set(Msg::getIsRead, 1);
        msgMapper.update(updateWrapper);
    }

    public Long getMsgUnreadCount(String userId) {
        LambdaQueryWrapper<Msg> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Msg::getUserId, userId)
                .eq(Msg::getIsRead, 0);
        return msgMapper.selectCount(queryWrapper);
    }

    // 获取实时统计数量
    public Long getNowCount() {
        Object value = stringRedisTemplate.opsForValue().get("now_count");
        return value == null ? 0 : Long.parseLong(String.valueOf(value));
    }

    // 获取图标数据
    public void getStatistics() {

    }

    // 获取用户图表数据
    public List<StaticsResponseDTO> getUserStatistics(String userId) {

        String currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        List<StaticsResponseDTO> resDTO = new ArrayList<>();
        String lastMonth = LocalDate.now().minusMonths(1).format(DateTimeFormatter.ofPattern("yyyyMM"));
        int shardIndex = Math.abs(userId.hashCode() % SHARED_COUNT);
        String picKey = String.format("%s%s_%s_%d", REDIS_KEY_PREFIX, STAT_TYPE_PICTURE_REVIEW, currentMonth, shardIndex);
        resDTO.add(buildDTO("图片检测", picKey, lastMonth, userId));
        String videoKey = String.format("%s%s_%s_%d", REDIS_KEY_PREFIX, STAT_TYPE_VIDEO_REVIEW, currentMonth, shardIndex);
        resDTO.add(buildDTO("视频检测", videoKey, lastMonth, userId));
        return resDTO;
    }

    /**
     * 构建dto
     * @return
     */
    private StaticsResponseDTO buildDTO(String typeName, String redisKey, String lastMonth, String userId) {
        StaticsResponseDTO resDTO = new StaticsResponseDTO();
        resDTO.setTypeName(typeName);
        Object nowValue = stringRedisTemplate.opsForHash().get(redisKey, userId);
        resDTO.setCurrentMonth(nowValue == null ? null : Long.parseLong(nowValue.toString()));
        QueryWrapper<PictureStatics> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(userId != null, "user_id", userId)
                .eq(lastMonth != null && !lastMonth.isEmpty(), "stat_month", lastMonth)
                // 只查询需要的字段，减少数据库IO
                .select("stat_value");
        PictureStatics pictureStatics = pictureStatMapper.selectOne(queryWrapper);
        if (pictureStatics == null) {
            return resDTO;
        }
        resDTO.setLastMonth(pictureStatics.getStatValue());
        return resDTO;
    }

    public IPage<Msg> getMsgList(MsgRequestDTO msgQueryDTO) {

        String userIdStr = String.valueOf(UserContextHolder.getUserId());
        if (userIdStr == null) {
            throw new CustomException(ResultCodeEnum.TOKEN_CHECK_ERROR);
        }
        // 2. 从DTO中获取分页参数并校验修正
        Integer pageNum = msgQueryDTO.getPageNum() == null ? 1 : msgQueryDTO.getPageNum();
        Integer pageSize = msgQueryDTO.getPageSize() == null ? 10 : msgQueryDTO.getPageSize();
        pageNum = Math.max(pageNum, 1); // 页码最小为1
        pageSize = Math.min(Math.max(pageSize, 1), 50); // 每页条数限制1-50
        LocalDateTime startTime = msgQueryDTO.getStartTime();
        LocalDateTime endTime = msgQueryDTO.getEndTime();

        // 4. 构建MyBatis-Plus分页对象
        Page<Msg> page = new Page<>(pageNum, pageSize);

        // 5. 使用LambdaQueryWrapper构建动态查询条件
        LambdaQueryWrapper<Msg> queryWrapper = new LambdaQueryWrapper<Msg>()
                // 必选条件：用户ID
                .eq(Msg::getUserId, userIdStr)
                // 可选条件：消息类型（从DTO获取）
                .eq(msgQueryDTO.getType() != null && !msgQueryDTO.getType().isEmpty(),
                Msg::getType, msgQueryDTO.getType())
        // 可选条件：阅读状态（转换为数据库的1/0）
                    .eq(msgQueryDTO.getIsRead() != null,
                Msg::getIsRead, msgQueryDTO.getIsRead())
                // 可选条件：创建时间大于等于开始时间
                .ge(startTime != null, Msg::getCreateTime, startTime)
                // 可选条件：创建时间小于等于结束时间
                .le(endTime != null, Msg::getCreateTime, endTime)
                // 排序：按创建时间降序
                .orderByDesc(Msg::getCreateTime);

        // 6. 调用服务层分页查询（传入分页对象和查询条件）
        IPage<Msg> msgPage = msgMapper.selectPage(page, queryWrapper);

        // 7. 转换为响应DTO（复用MyBatis-Plus的IPage分页对象）
        IPage<Msg> responsePage = msgPage.convert(msg -> {
            Msg dto = new Msg();
            BeanUtils.copyProperties(msg, dto);
            dto.setIsRead(msg.getIsRead()); // 转换为布尔值
            return dto;
        });
        return responsePage;
    }

    public void setMsgReadById(Integer id) {
        msgMapper.setMsgReadById(id);
    }
}

