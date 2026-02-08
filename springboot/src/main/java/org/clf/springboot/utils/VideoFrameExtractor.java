//package org.clf.springboot.utils;
//
//import net.bramp.ffmpeg.FFmpeg;
//import net.bramp.ffmpeg.FFmpegExecutor;
//import net.bramp.ffmpeg.FFprobe;
//import net.bramp.ffmpeg.probe.FFmpegProbeResult;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//import java.io.File;
//import java.util.UUID;
//
//@Component
//public class VideoFrameExtractor {
//
//    @Value("${ffmpeg.path}")
//    private String ffmpegPath;
//
//    @Value("${ffprobe.path}")
//    private String ffprobePath;
//
//    @Value("${video.frame.temp.path}")
//    private String frameTempPath;
//
//    /**
//     * 智能抽帧（核心优化）
//     * @param videoPath 视频路径
//     * @return 抽帧图片路径列表
//     */
//    public String[] smartExtractFrames(String videoPath) {
//        try {
//            // 1. 获取视频基础信息（时长、帧率）
//            FFprobe ffprobe = new FFprobe(ffprobePath);
//            FFmpegProbeResult probeResult = ffprobe.probe(videoPath);
//            double videoDuration = probeResult.getFormat().duration; // 视频时长（秒）
//            double frameRate = probeResult.getStreams().get(0).r_frame_rate.doubleValue(); // 帧率
//
//            // 2. 动态计算抽帧数量和间隔
//            int frameCount;
//            double interval;
//            if (videoDuration <= 10) { // 短视频（≤10秒）：抽5帧，间隔2秒
//                frameCount = 5;
//                interval = 2;
//            } else if (videoDuration <= 60) { // 中视频（10-60秒）：抽8帧，间隔≈总时长/8
//                frameCount = 8;
//                interval = videoDuration / 8;
//            } else { // 长视频（>60秒）：抽10帧，且关键帧优先
//                frameCount = 10;
//                interval = videoDuration / 10;
//            }
//
//            // 3. 创建临时目录
//            String taskDir = frameTempPath + File.separator + UUID.randomUUID().toString();
//            File dir = new File(taskDir);
//            if (!dir.exists()) {
//                dir.mkdirs();
//            }
//
//            // 4. 构建智能抽帧命令（关键帧优先 + 均匀间隔）
//            String outputPattern = taskDir + File.separator + "frame_%03d.jpg";
//            FFmpeg ffmpeg = new FFmpeg(ffmpegPath);
//
//            // 核心参数说明：
//            // -skip_frame nokey：优先提取关键帧
//            // -r：输出帧率（控制抽帧间隔）
//            // -vsync vfr：可变帧率，避免重复帧
//            net.bramp.ffmpeg.builder.FFmpegBuilder builder = new net.bramp.ffmpeg.builder.FFmpegBuilder()
//                    .setInput(videoPath)
//                    .overrideOutputFiles(true)
//                    .addOutput(outputPattern)
//                    .setFrames(frameCount) // 抽帧总数
//                    .setVideoFrameRate((int) (1 / interval)) // 按间隔抽帧
//                    .setFormat("image2")
//                    .setVideoFilter("scale=640:-1") // 帧压缩为640宽（保持比例），降低检测成本
//                    .setVideoQuality(2) // 图片质量（1最高，31最低）
//                    .addExtraArgs("-skip_frame", "nokey") // 关键帧优先
//                    .addExtraArgs("-vsync", "vfr")
//                    .done();
//
//            // 5. 执行抽帧
//            FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);
//            executor.createJob(builder).run();
//
//            // 6. 获取抽帧文件列表
//            File[] frameFiles = dir.listFiles((file, name) -> name.endsWith(".jpg"));
//            if (frameFiles == null || frameFiles.length == 0) {
//                throw new RuntimeException("视频抽帧失败，未生成任何帧图片");
//            }
//
//            // 7. 转换为路径数组
//            String[] framePaths = new String[frameFiles.length];
//            for (int i = 0; i < frameFiles.length; i++) {
//                framePaths[i] = frameFiles[i].getAbsolutePath();
//            }
//
//            return framePaths;
//        } catch (Exception e) {
//            throw new RuntimeException("智能抽帧异常：" + e.getMessage(), e);
//        }
//    }
//
//    /**
//     * 清理临时帧文件
//     */
//    public void cleanTempFrames(String[] framePaths) {
//        if (framePaths == null || framePaths.length == 0) return;
//        // 先删文件，再删空目录
//        for (String path : framePaths) {
//            File file = new File(path);
//            if (file.exists()) {
//                file.delete();
//            }
//            File parentDir = file.getParentFile();
//            if (parentDir != null && parentDir.list() != null && parentDir.list().length == 0) {
//                parentDir.delete();
//            }
//        }
//    }
//}