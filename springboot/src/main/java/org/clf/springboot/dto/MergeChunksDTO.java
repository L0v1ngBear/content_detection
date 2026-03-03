package org.clf.springboot.dto;

import lombok.Data;

/**
 * 合并分片参数接收类
 * 对应前端传递的JSON参数：fileHash、fileName、totalChunks、md5
 */
@Data  // lombok注解，自动生成get/set/toString等方法
public class MergeChunksDTO {
    // 字段名必须和前端传递的参数名完全一致（大小写、拼写）
    private String fileHash;
    private String fileName;
    private int totalChunks;
    private String md5;
}