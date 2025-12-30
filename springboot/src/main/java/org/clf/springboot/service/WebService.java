package org.clf.springboot.service;

import jakarta.annotation.Resource;
import org.clf.springboot.dto.HistoryPictureResDTO;
import org.clf.springboot.entity.Picture;
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

    public HistoryPictureResDTO getHistoryPicture(String userId) {
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

    private Picture buildPicture(String redisKey) {

        Map<Object, Object> hashEntries = stringRedisTemplate.opsForHash().entries(redisKey);

        Picture picture = new Picture();

        picture.setId((Long) hashEntries.get("id"));
        picture.setUpdateTime((Long) hashEntries.get("uploadTime"));

        picture.setObjectName((String) hashEntries.get("objectName"));
        picture.setStatus((String) hashEntries.get("status"));
        picture.setPreSignedUrl((String) hashEntries.get("preSignedUrl"));
        picture.setImageId((String) hashEntries.get("imageId"));

        picture.setYoloScore((Double) hashEntries.get("yoloScore"));
        return picture;
    }
}
