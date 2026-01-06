package org.clf.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import jakarta.annotation.Resource;
import org.clf.springboot.dto.HistoryPictureResDTO;
import org.clf.springboot.entity.Msg;
import org.clf.springboot.entity.Picture;
import org.clf.springboot.mapper.MsgMapper;
import org.clf.springboot.utils.UserContextHolder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class WebService {

    @Value("${minio.redisKey}")
    private String redisKey;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private MsgMapper msgMapper;

    public HistoryPictureResDTO getHistoryPicture() {
        String userId = String.valueOf(UserContextHolder.getUserId());
        HistoryPictureResDTO resDTO = new HistoryPictureResDTO();
        Set<String> imageList = stringRedisTemplate.opsForZSet().range(redisKey + userId, 0, -1);
        String detailKey = redisKey + userId;
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

    public Long getNowCount() {
        Long count = Long.valueOf(stringRedisTemplate.opsForValue().get("now_count"));
        return count;
    }
}
