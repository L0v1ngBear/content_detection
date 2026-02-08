package org.clf.springboot.service.Impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.clf.springboot.dto.HistoryRequestDTO;
import org.clf.springboot.entity.DetectHistory;

public interface HistoryServiceImpl extends IService<DetectHistory> {

    IPage<DetectHistory> getHistoryByUserId(HistoryRequestDTO historyRequestDTO);
}
