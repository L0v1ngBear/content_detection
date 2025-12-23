package org.clf.springboot.utils;

import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Item;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * MinIO 工具类，封装常用文件操作
 */
@Component
public class MinIOUtils {

    @Autowired
    private MinioClient minioClient;

    // 从配置文件中获取存储桶名称
    @Value("${minio.bucketName}")
    private String defaultBucketName;

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
     * 创建存储桶
     * @param bucketName 存储桶名称
     */
    public void createBucket(String bucketName) {
        try {
            if (!bucketExists(bucketName)) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }
        } catch (Exception e) {
            throw new RuntimeException("创建存储桶失败：" + e.getMessage(), e);
        }
    }

    /**
     * 上传文件（默认存储桶）
     * @param file 上传的文件
     * @param objectName 存储在 MinIO 中的文件名（可包含路径，如 "images/xxx.jpg"）
     * @return 上传成功的文件名
     */
    public String uploadFile(MultipartFile file, String objectName) {
        return uploadFile(defaultBucketName, file, objectName);
    }

    /**
     * 上传文件（指定存储桶）
     * @param bucketName 存储桶名称
     * @param file 上传的文件
     * @param objectName 存储在 MinIO 中的文件名
     * @return 上传成功的文件名
     */
    public String uploadFile(String bucketName, MultipartFile file, String objectName) {
        try {
            // 检查存储桶是否存在，不存在则创建
            createBucket(bucketName);

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
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucketName)
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
        try (InputStream stream = new ByteArrayInputStream(data)) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(defaultBucketName)
                            .object(objectName)
                            .stream(stream, data.length, -1)
                            .contentType(contentType)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("字节数组上传失败：" + e.getMessage(), e);
        }
    }
}