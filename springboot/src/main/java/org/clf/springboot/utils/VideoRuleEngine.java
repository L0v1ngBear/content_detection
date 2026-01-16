package org.clf.springboot.utils;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class VideoRuleEngine {

    // 违规置信度阈值：≥70分才判定为有效违规帧
    private static final int VIOLATION_SCORE_THRESHOLD = 70;
    // 违规帧占比阈值：违规帧占比≥20%才判定视频违规
    private static final double VIOLATION_FRAME_RATIO = 0.2;

    /**
     * 执行结果判定
     * @param frameResults 所有帧的检测结果
     * @return 最终判定结果
     */
    public VideoAuditDecision decide(List<Map<String, Object>> frameResults) {
        VideoAuditDecision decision = new VideoAuditDecision();
        int totalFrame = frameResults.size();
        int violationFrameCount = 0;
        String mainViolationType = "";
        int maxScore = 0;

        // 1. 遍历所有帧结果
        for (Map<String, Object> frameResult : frameResults) {
            boolean isPass = (boolean) frameResult.get("isPass");
            int score = (int) frameResult.get("score");
            String violationType = (String) frameResult.get("violationType");

            // 2. 判定有效违规帧（未通过 + 分数≥阈值）
            if (!isPass && score >= VIOLATION_SCORE_THRESHOLD) {
                violationFrameCount++;
                // 记录最高置信度的违规类型
                if (score > maxScore) {
                    maxScore = score;
                    mainViolationType = violationType;
                }
            }
        }

        // 3. 计算违规帧占比
        double violationRatio = totalFrame == 0 ? 0 : (double) violationFrameCount / totalFrame;

        // 4. 最终判定
        boolean isVideoPass = violationRatio < VIOLATION_FRAME_RATIO;
        decision.setPass(isVideoPass);
        decision.setViolationType(isVideoPass ? "" : mainViolationType);
        decision.setViolationScore(maxScore);
        decision.setViolationFrameCount(violationFrameCount);
        decision.setTotalFrameCount(totalFrame);
        decision.setViolationRatio(violationRatio);

        return decision;
    }

    // 判定结果封装
    @Data
    public static class VideoAuditDecision {
        private boolean isPass; // 视频是否合规
        private String violationType; // 主要违规类型
        private int violationScore; // 最高违规置信度
        private int violationFrameCount; // 违规帧数
        private int totalFrameCount; // 总抽帧数
        private double violationRatio; // 违规帧占比
    }
}
