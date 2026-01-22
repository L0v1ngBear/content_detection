package org.clf.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import jakarta.annotation.Resource;
import org.clf.springboot.dto.HistoryPictureResDTO;
import org.clf.springboot.dto.StaticsResponseDTO;
import org.clf.springboot.entity.Msg;
import org.clf.springboot.entity.Picture;
import org.clf.springboot.entity.PictureStat;
import org.clf.springboot.mapper.MsgMapper;
import org.clf.springboot.mapper.PictureStatMapper;
import org.clf.springboot.utils.UserContextHolder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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

    public HistoryPictureResDTO getHistoryPicture() {
        String userId = String.valueOf(UserContextHolder.getUserId());
        HistoryPictureResDTO resDTO = new HistoryPictureResDTO();
        Set<String> imageList = stringRedisTemplate.opsForZSet().range(REDISKEY + userId, 0, -1);
        String detailKey = REDISKEY + userId;
        if (imageList == null || imageList.isEmpty()) {
            resDTO.setTotal(0);
            resDTO.setPicture(null);
            return resDTO;
        }
        List<Picture> pictures = new ArrayList<>();
        for (String image : imageList) {
            Picture picture = buildPicture(detailKey + image);
            pictures.add(picture);
        }
        resDTO.setPicture(pictures);
        resDTO.setTotal(imageList.size());
        return resDTO;
    }

    public List<Msg> getLatestMsgList(String userId, Integer pageSize) {
        LambdaQueryWrapper<Msg> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Msg::getUserId, userId) // 条件：用户ID匹配
                .orderByDesc(Msg::getCreateTime) // 倒序：按创建时间
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

    private Picture buildPicture(String redisKey) {

        Map<Object, Object> hashEntries = stringRedisTemplate.opsForHash().entries(redisKey);

        Picture picture = new Picture();

        picture.setId((Long) hashEntries.get("id"));
        picture.setUploadTime((Long) hashEntries.get("uploadTime"));

        picture.setObjectName((String) hashEntries.get("objectName"));
        picture.setStatus((String) hashEntries.get("status"));
        picture.setImageId((String) hashEntries.get("imageId"));

        picture.setYoloScore((Double) hashEntries.get("yoloScore"));
        return picture;
    }

    // 获取实时统计数量
    public Long getNowCount() {
        Long count = Long.valueOf(stringRedisTemplate.opsForValue().get("now_count"));
        return count;
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
        Object nowValue = stringRedisTemplate.opsForValue().get(redisKey);
        resDTO.setCurrentMonth(nowValue == null ? null : Long.parseLong(nowValue.toString()));
        QueryWrapper<PictureStat> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(userId != null, "user_id", userId)
                .eq(lastMonth != null && !lastMonth.isEmpty(), "stat_month", lastMonth)
                // 只查询需要的字段，减少数据库IO
                .select("stat_value");
        PictureStat pictureStat = pictureStatMapper.selectOne(queryWrapper);
        if (pictureStat == null) {
            return resDTO;
        }
        resDTO.setLastMonth(pictureStat.getStatValue());
        return resDTO;
    }
}
