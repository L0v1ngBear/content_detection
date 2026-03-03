package org.clf.springboot.dto;

import lombok.Data;

import java.util.List;

@Data
public class UploadedChunksDTO {
    private List<Integer> uploadedChunks;
}
