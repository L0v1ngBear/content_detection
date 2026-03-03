package org.clf.springboot.utils;

import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Bucket;
import io.minio.messages.Item;
import jakarta.annotation.Resource;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * MinIO 工具类，封装常用文件操作（优化分片上传/合并逻辑）
 */
@Component
public class MinIOUtils {

    @Resource
    private MinioClient minioClient;

    // 从配置文件中获取存储桶名称
    @Value("${minio.bucketName.defaultBucket}")
    private String defaultBucketName;

    // 视频成品桶（存储合并后的完整视频）
    @Value("${minio.bucketName.videoBucket:video-bucket}")
    private String videoBucketName;

    // 分片存储桶（单独配置，与成品桶分离）
    @Value("${minio.bucketName.chunkBucket:video-chunks}")
    private String chunkBucketName;

    @Value("${minio.endpoint}")
    private String minioEndpoint;

    private static boolean pathStyleAccess = true;

    /**
     * 检查存储桶是否存在
     * @param bucketName 存储桶名称
     * @return 存在返回 true，否则 false
     */
    public boolean bucketExists(String bucketName) {
        try {
            return minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        } catch (Exception e) {
            throw new RuntimeException("检查存储桶是否存在失败：" + e.getMessage(), e);
        }
    }

    /**
     * 创建存储桶（支持公有读配置）
     * @param bucketName 存储桶名称
     * @param isPublic 是否设置为公有读
     */
    public void createBucket(String bucketName, boolean isPublic) {
        try {
            if (!bucketExists(bucketName)) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                System.out.println("存储桶[" + bucketName + "]创建成功");
                if (isPublic) {
                    setBucketPublic(bucketName);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("创建存储桶[" + bucketName + "]失败：" + e.getMessage(), e);
        }
    }

    /**
     * 创建存储桶（默认私有）
     * @param bucketName 存储桶名称
     */
    public void createBucket(String bucketName) {
        createBucket(bucketName, false);
    }

    /**
     * 设置存储桶为公有读权限（关键：确保公开链接可访问）
     * @param bucketName 存储桶名称
     */
    private void setBucketPublic(String bucketName) {
        try {
            // 公有读策略JSON（MinIO官方标准策略）
            String publicReadPolicy = """
                    {
                        "Version": "2012-10-17",
                        "Statement": [
                            {
                                "Effect": "Allow",
                                "Principal": "*",
                                "Action": [
                                    "s3:GetObject"
                                ],
                                "Resource": [
                                    "arn:aws:s3:::%s/*"
                                ]
                            }
                        ]
                    }
                    """.formatted(bucketName);

            // 应用桶策略
            minioClient.setBucketPolicy(
                    SetBucketPolicyArgs.builder()
                            .bucket(bucketName)
                            .config(publicReadPolicy)
                            .build()
            );
            System.out.println("存储桶[" + bucketName + "]已设置为公有读权限");
        } catch (Exception e) {
            throw new RuntimeException("设置桶[" + bucketName + "]公有读权限失败：" + e.getMessage(), e);
        }
    }

    /**
     * 上传文件（默认存储桶）
     * @param file 上传的文件
     * @param objectName 存储在 MinIO 中的文件名（可包含路径，如 "images/xxx.jpg"）
     * @return 上传成功的文件名
     */
    public String uploadFile(MultipartFile file, String objectName) {
        return uploadFile(defaultBucketName, file, objectName, false);
    }

    /**
     * 上传文件（指定存储桶）
     * @param bucketName 存储桶名称
     * @param file 上传的文件
     * @param objectName 存储在 MinIO 中的文件名
     * @param isPublic 是否设置为公有读
     * @return 上传成功的文件名
     */
    public String uploadFile(String bucketName, MultipartFile file, String objectName, Boolean isPublic) {
        try {
            // 检查存储桶是否存在，不存在则创建
            createBucket(bucketName, isPublic);

            // 上传文件
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
            return objectName;
        } catch (Exception e) {
            throw new RuntimeException("文件上传失败：" + e.getMessage(), e);
        }
    }

    /**
     * 下载文件（获取输入流）
     * @param objectName 存储在 MinIO 中的文件名
     * @return 文件输入流
     */
    public InputStream downloadFile(String objectName) {
        return downloadFile(defaultBucketName, objectName);
    }

    /**
     * 下载文件（指定存储桶，获取输入流）
     * @param bucketName 存储桶名称
     * @param objectName 存储在 MinIO 中的文件名
     * @return 文件输入流
     */
    public InputStream downloadFile(String bucketName, String objectName) {
        try {
            // 校验桶是否存在
            if (!bucketExists(bucketName)) {
                throw new RuntimeException("存储桶[" + bucketName + "]不存在");
            }
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("文件下载失败：" + e.getMessage(), e);
        }
    }

    /**
     * 删除文件
     * @param objectName 存储在 MinIO 中的文件名
     */
    public void deleteFile(String objectName) {
        deleteFile(defaultBucketName, objectName);
    }

    /**
     * 删除文件（指定存储桶）
     * @param bucketName 存储桶名称
     * @param objectName 存储在 MinIO 中的文件名
     */
    public void deleteFile(String bucketName, String objectName) {
        try {
            if (!bucketExists(bucketName)) {
                return; // 桶不存在，直接返回
            }
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("文件删除失败：" + e.getMessage(), e);
        }
    }

    /**
     * 获取文件的临时访问链接（默认有效期7天）
     * @param objectName 存储在 MinIO 中的文件名
     * @return 临时访问 URL
     */
    public String getPresignedUrl(String objectName) {
        return getPresignedUrl(defaultBucketName, objectName, 7, TimeUnit.DAYS);
    }

    /**
     * 获取文件的临时访问链接（指定有效期）
     * @param bucketName 存储桶名称
     * @param objectName 存储在 MinIO 中的文件名
     * @param duration 有效期
     * @param unit 时间单位
     * @return 临时访问 URL
     */
    public String getPresignedUrl(String bucketName, String objectName, int duration, TimeUnit unit) {
        try {
            // 校验桶是否存在
            if (!bucketExists(bucketName)) {
                throw new RuntimeException("存储桶[" + bucketName + "]不存在");
            }
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .method(Method.GET)
                            .expiry(duration, unit)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("获取临时访问链接失败：" + e.getMessage(), e);
        }
    }

    /**
     * 列出存储桶中的所有文件
     * @param bucketName 存储桶名称
     * @return 文件名称列表
     */
    public List<String> listFiles(String bucketName) {
        List<String> fileNames = new ArrayList<>();
        try {
            // 校验桶是否存在
            if (!bucketExists(bucketName)) {
                throw new RuntimeException("存储桶[" + bucketName + "]不存在");
            }
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucketName) // 修复：原代码错误使用videoBucketName
                            .recursive(true)
                            .build()
            );
            for (Result<Item> result : results) {
                Item item = result.get();
                fileNames.add(item.objectName());
            }
            return fileNames;
        } catch (Exception e) {
            throw new RuntimeException("列出文件失败：" + e.getMessage(), e);
        }
    }

    /**
     * 上传字节数组（适用于非 MultipartFile 场景，如本地文件）
     * @param data 字节数组
     * @param objectName 存储在 MinIO 中的文件名
     * @param contentType 文件类型（如 "image/jpeg"）
     */
    public void uploadBytes(byte[] data, String objectName, String contentType) {
        uploadBytes(defaultBucketName, data, objectName, contentType, false);
    }

    /**
     * 上传字节数组（指定存储桶）
     * @param bucketName 存储桶名称
     * @param data 字节数组
     * @param objectName 存储在 MinIO 中的文件名
     * @param contentType 文件类型
     * @param isPublic 是否公有读
     */
    public void uploadBytes(String bucketName, byte[] data, String objectName, String contentType, boolean isPublic) {
        try {
            // 校验并创建桶
            createBucket(bucketName, isPublic);
            try (InputStream stream = new ByteArrayInputStream(data)) {
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucketName)
                                .object(objectName)
                                .stream(stream, data.length, -1)
                                .contentType(contentType)
                                .build()
                );
            }
        } catch (Exception e) {
            throw new RuntimeException("字节数组上传失败：" + e.getMessage(), e);
        }
    }

    /**
     * 获取文件的永久公开访问链接（需确保桶为公有读权限）
     * @param objectName 存储在 MinIO 中的文件名
     * @return 永久公开访问 URL
     */
    public String getPublicUrl(String objectName) {
        return getPublicUrl(defaultBucketName, objectName);
    }

    /**
     * 获取文件的永久公开访问链接（指定存储桶）
     * @param bucketName 存储桶名称
     * @param objectName 存储在 MinIO 中的文件名
     * @return 永久公开访问 URL
     */
    public String getPublicUrl(String bucketName, String objectName) {
        try {
            // 校验存储桶是否存在
            if (!bucketExists(bucketName)) {
                throw new RuntimeException("存储桶[" + bucketName + "]不存在");
            }

            // 处理endpoint格式（移除末尾的/，避免URL拼接错误）
            String endpoint = minioEndpoint.trim();
            if (endpoint.endsWith("/")) {
                endpoint = endpoint.substring(0, endpoint.length() - 1);
            }

            // 拼接公开URL（区分路径风格/虚拟主机风格）
            String publicUrl;
            if (pathStyleAccess) {
                // 路径风格：http://minio:9000/bucketName/objectName
                publicUrl = endpoint + "/" + bucketName + "/" + objectName;
            } else {
                // 虚拟主机风格：http://bucketName.minio:9000/objectName
                publicUrl = endpoint.replace("://", "://" + bucketName + ".") + "/" + objectName;
            }

            // 提示：如果桶不是公有读，公开链接无法访问
            try {
                Bucket bucket = minioClient.listBuckets().stream()
                        .filter(b -> b.name().equals(bucketName))
                        .findFirst()
                        .orElse(null);
                if (bucket != null) {
                    System.out.println("提示：请确保存储桶[" + bucketName + "]已设置为公有读权限，否则公开链接无法访问");
                }
            } catch (Exception e) {
                // 非核心异常，不影响URL生成，仅打印日志
                System.err.println("校验桶权限时发生异常：" + e.getMessage());
            }

            return publicUrl;
        } catch (Exception e) {
            throw new RuntimeException("生成公开访问链接失败：" + e.getMessage(), e);
        }
    }

    // ====================== 分片上传相关方法（核心优化）======================

    /**
     * 查询已上传的分片索引
     * @param fileHash 文件唯一标识
     * @return 已上传的分片索引列表
     */
    public List<Integer> listUploadedChunks(String fileHash) {
        List<Integer> uploadedChunks = new ArrayList<>();

        try {
            // 1. 先创建分片桶（避免NoSuchBucket错误）
            createBucket(chunkBucketName);

            // 2. 列出指定前缀的所有分片（使用分片桶，而非视频成品桶）
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(chunkBucketName) // 修复：使用分片桶
                            .prefix(fileHash + "/")
                            .recursive(false)
                            .build()
            );

            // 3. 解析分片索引（增加异常处理）
            for (Result<Item> result : results) {
                Item item = result.get();
                String objectName = item.objectName();
                try {
                    // 提取分片索引：fileHash/0_filename.mp4 -> 0
                    String[] parts = objectName.split("/");
                    if (parts.length > 1) {
                        String[] chunkParts = parts[1].split("_");
                        if (chunkParts.length > 0) {
                            uploadedChunks.add(Integer.parseInt(chunkParts[0]));
                        }
                    }
                } catch (NumberFormatException e) {
                    System.err.println("解析分片索引失败，文件名：" + objectName + "，错误：" + e.getMessage());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("查询已上传分片失败：" + e.getMessage(), e);
        }
        return uploadedChunks;
    }

    /**
     * 上传单个分片
     * @param file 分片文件
     * @param fileHash 文件唯一标识
     * @param chunkIndex 分片索引
     * @param fileName 原文件名
     * @throws Exception 上传异常
     */
    public void uploadChunk(MultipartFile file, String fileHash, int chunkIndex, String fileName) throws Exception {
        // 1. 先创建分片桶
        createBucket(chunkBucketName);

        // 2. 分片存储路径：{fileHash}/{chunkIndex}_{fileName}
        String objectName = fileHash + "/" + chunkIndex + "_" + fileName;

        // 3. 上传分片到分片桶
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(chunkBucketName) // 修复：使用分片桶
                        .object(objectName)
                        .stream(file.getInputStream(), file.getSize(), -1)
                        .contentType(file.getContentType())
                        .build()
        );
    }

    /**
     * 合并分片为完整文件
     * @param fileHash 文件唯一标识
     * @param fileName 原文件名
     * @param totalChunks 总分片数
     * @param originalFileMd5 原始文件MD5（用于校验完整性）
     * @return 合并后文件的访问链接
     * @throws Exception 合并异常
     */
    public String mergeChunks(String fileHash, String fileName, int totalChunks, String originalFileMd5) throws Exception {
        // ========== 1. 前置校验 ==========
        // 创建视频成品桶
        createBucket(videoBucketName);
        // 创建分片桶
        createBucket(chunkBucketName);

        // ========== 2. 校验分片数量是否完整 ==========
        List<String> existingChunks = new ArrayList<>();
        minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(chunkBucketName) // 修复：使用分片桶
                        .prefix(fileHash + "/")
                        .recursive(false)
                        .build()
        ).forEach(item -> {
            try {
                existingChunks.add(item.get().objectName());
            } catch (Exception e) {
                throw new RuntimeException("读取分片列表失败：" + e.getMessage(), e);
            }
        });

        // 校验分片数量
        if (existingChunks.size() != totalChunks) {
            throw new Exception("分片数量不完整！期望：" + totalChunks + "，实际：" + existingChunks.size());
        }

        // ========== 3. 合并分片（并校验完整性） ==========
        String finalObjectName = "videos/" + UUID.randomUUID() + "_" + fileName;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MessageDigest md5Digest = MessageDigest.getInstance("MD5");

        try {
            for (int i = 0; i < totalChunks; i++) {
                String chunkObjectName = fileHash + "/" + i + "_" + fileName;

                // 读取分片内容
                try (InputStream inputStream = minioClient.getObject(
                        GetObjectArgs.builder()
                                .bucket(chunkBucketName) // 修复：使用分片桶
                                .object(chunkObjectName)
                                .build()
                )) {
                    byte[] buffer = new byte[1024 * 1024]; // 1MB缓冲区
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                        md5Digest.update(buffer, 0, bytesRead); // 累加更新MD5
                    }
                }

                // 删除已合并的分片（可选：也可保留用于断点续传）
                deleteChunk(fileHash, i, fileName);
            }

            // ========== 4. 校验合并后文件的MD5 ==========
            byte[] mergedBytes = outputStream.toByteArray();
            String mergedFileMd5 = DigestUtils.md5Hex(mergedBytes);
            if (originalFileMd5 != null && !originalFileMd5.isEmpty() && !originalFileMd5.equals(mergedFileMd5)) {
                throw new Exception("文件合并后MD5校验失败！前端MD5：" + originalFileMd5 + "，实际MD5：" + mergedFileMd5);
            }

            // ========== 5. 上传合并后的完整文件到视频成品桶 ==========
            uploadBytes(
                    videoBucketName,
                    mergedBytes,
                    finalObjectName,
                    "video/mp4", // 根据实际文件类型调整
                    false // 成品视频默认私有
            );

            // ========== 6. 删除所有分片（可选） ==========
            deleteAllChunks(fileHash);

            // ========== 7. 返回访问链接 ==========
            return finalObjectName;
        } finally {
            outputStream.close(); // 确保流关闭
        }
    }

    /**
     * 删除单个分片
     * @param fileHash 文件唯一标识
     * @param chunkIndex 分片索引
     * @param fileName 原文件名
     */
    public void deleteChunk(String fileHash, int chunkIndex, String fileName) {
        try {
            if (!bucketExists(chunkBucketName)) {
                return;
            }
            String objectName = fileHash + "/" + chunkIndex + "_" + fileName;
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(chunkBucketName) // 修复：使用分片桶
                            .object(objectName)
                            .build()
            );
        } catch (Exception e) {
            System.err.println("删除分片失败：" + e.getMessage());
        }
    }

    /**
     * 删除文件Hash对应的所有分片
     * @param fileHash 文件唯一标识
     */
    public void deleteAllChunks(String fileHash) {
        try {
            if (!bucketExists(chunkBucketName)) {
                return;
            }
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(chunkBucketName) // 修复：使用分片桶
                            .prefix(fileHash + "/")
                            .recursive(false)
                            .build()
            );

            for (Result<Item> result : results) {
                Item item = result.get();
                minioClient.removeObject(
                        RemoveObjectArgs.builder()
                                .bucket(chunkBucketName)
                                .object(item.objectName())
                                .build()
                );
            }
        } catch (Exception e) {
            System.err.println("删除所有分片失败：" + e.getMessage());
        }
    }
}