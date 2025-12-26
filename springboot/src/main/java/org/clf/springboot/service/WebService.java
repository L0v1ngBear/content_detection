package org.clf.springboot.service;

import jakarta.annotation.Resource;
import org.clf.springboot.dto.HistoryPictureResDTO;
import org.clf.springboot.entity.Picture;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
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
        Picture picture = new Picture();
        picture.setId(String.valueOf(stringRedisTemplate.opsForValue().get("id")));
        picture.setObjectName(String.valueOf(stringRedisTemplate.opsForHash().get(redisKey,  "objectName")));
        picture.setStatus(String.valueOf(stringRedisTemplate.opsForHash().get(redisKey,  "status")));
        picture.setPreSignedUrl(String.valueOf(stringRedisTemplate.opsForHash().get(redisKey,  "preSignedUrl")));
        picture.setImageId(String.valueOf(stringRedisTemplate.opsForHash().get(redisKey,  "imageId")));
        return picture;
    }
}
