package org.clf.springboot.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.clf.springboot.common.Result;
import org.clf.springboot.common.enums.ErrorEnum;
import org.clf.springboot.dto.HistoryPictureResDTO;
import org.clf.springboot.dto.MsgResponseDTO;
import org.clf.springboot.entity.Msg;
import org.clf.springboot.service.WebService;
import org.clf.springboot.utils.UserContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import java.util.stream.Collectors;

@Tag(name = "网页通用接口")
@RestController
@RequestMapping("/api")
public class WebController {

    private static final Logger logger = LoggerFactory.getLogger(WebController.class);

    @Resource
    private WebService webService;

    /**
     * 获取历史审核的图片
     * @return
     */
    @GetMapping("/history/review/picture")
    public Result getHistoryPicture() {
        HistoryPictureResDTO resDTO = webService.getHistoryPicture();
        return Result.success(resDTO);
    }

    @GetMapping("/msg/list")
    public Result getMsgList(@RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            if (pageSize > 50) {
                pageSize = 50;
            }
            String userId = String.valueOf(UserContextHolder.getUserId());

            if (userId == null || userId.isEmpty() || UserContextHolder.getUserId() == null) {
                return Result.error(ErrorEnum.NOT_LOGIN.msg);
            }
            List<Msg> msgList = webService.getLatestMsgList(userId, pageSize);
            List<MsgResponseDTO> msgResponseDTOS = msgList.stream().map(msg -> {
                MsgResponseDTO msgResponseDTO = new MsgResponseDTO();
                BeanUtils.copyProperties(msg, msgResponseDTO);
                msgResponseDTO.setIsRead(msg.getIsRead() == 1);
                return msgResponseDTO;
            }).collect(Collectors.toList());
            return Result.success(msgResponseDTOS);
        } catch (Exception e) {
            logger.error("获取消息失败", e);
            return Result.error("获取消息失败");
        }

    }

    @PostMapping("/msg/all-read")
    public Result setMsgAllRead() {
        try {
            String userId = String.valueOf(UserContextHolder.getUserId());
            if (userId == null || userId.isEmpty() || UserContextHolder.getUserId() == null) {
                return Result.error(ErrorEnum.NOT_LOGIN.msg);
            }
            webService.setMsgAllRead(userId);
            return Result.success();
        } catch (Exception e) {
            logger.error("一键已读失败", e);
            return Result.error("一键已读失败");
        }
    }

    @GetMapping("/msg/unread-count")
    public Result getMsgUnreadCount(@RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            String userId = String.valueOf(UserContextHolder.getUserId());
            if (userId == null || userId.isEmpty() || UserContextHolder.getUserId() == null) {
                return Result.error(ErrorEnum.NOT_LOGIN.msg);
            }
            Long count = webService.getMsgUnreadCount(userId);
            return Result.success(count);
        } catch (Exception e) {
            logger.error("读取未读数量失败");
            return Result.error("获取未读数量失败");
        }
    }

    @GetMapping("/now-count")
    public Result getNowCount() {
        return Result.success(webService.getNowCount());
    }
}
