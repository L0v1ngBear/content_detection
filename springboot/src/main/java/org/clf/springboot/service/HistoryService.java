package org.clf.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.clf.springboot.dto.HistoryRequestDTO;
import org.clf.springboot.entity.DetectHistory;
import org.clf.springboot.mapper.DetectHistoryMapper;
import org.clf.springboot.service.Impl.HistoryServiceImpl;
import org.clf.springboot.utils.UserContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class HistoryService extends ServiceImpl<DetectHistoryMapper, DetectHistory>
        implements HistoryServiceImpl {

    @Override
    public IPage<DetectHistory> getHistoryByUserId(HistoryRequestDTO queryDTO) {
        // 1. 构建分页对象
        Page<DetectHistory> page = new Page<>(
                queryDTO.getPageNum(),
                Math.min(queryDTO.getPageSize(), 100) // 限制最大页大小为100，防止性能问题
        );

        // 2. 构建动态查询条件
        LambdaQueryWrapper<DetectHistory> wrapper = new LambdaQueryWrapper<>();

        // 时间范围筛选（检测开始时间）
        if (queryDTO.getStartTime() != null) {
            wrapper.ge(DetectHistory::getDetectTime, queryDTO.getStartTime());
        }
        if (queryDTO.getEndTime() != null) {
            wrapper.le(DetectHistory::getDetectTime, queryDTO.getEndTime());
        }

        if (UserContextHolder.getUserId() != null) {
            wrapper.eq(DetectHistory::getUserId, UserContextHolder.getUserId());
        }

        // 检测状态筛选
        if (queryDTO.getStatus() != null) {
            wrapper.eq(DetectHistory::getStatus, queryDTO.getStatus());
        }

        // 检测类型筛选（模糊匹配，支持部分关键词）
        if (StringUtils.hasText(queryDTO.getDetectType())) {
            wrapper.like(DetectHistory::getDetectType, queryDTO.getDetectType());
        }

        // 排序：按检测开始时间降序
        wrapper.orderByDesc(DetectHistory::getDetectTime);

        // 3. 执行分页查询
        return baseMapper.selectPage(page, wrapper);
    }
}