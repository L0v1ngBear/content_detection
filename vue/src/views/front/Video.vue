<template>
  <div class="video-upload-container">
    <!-- 上传区域 -->
    <div class="upload-area" @click="triggerFileInput" :class="{ dragging: isDragging }">
      <input
          type="file"
          ref="fileInput"
          accept="video/mp4,video/avi,video/mov"
          @change="handleFileChange"
          style="display: none"
      />
      <svg class="upload-icon" viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg">
        <path fill="#409eff" d="M896 480H544V128h-64v352H128c-35.3 0-64 28.7-64 64v320c0 35.3 28.7 64 64 64h768c35.3 0 64-28.7 64-64V544c0-35.3-28.7-64-64-64zM512 832c-44.2 0-80-35.8-80-80s35.8-80 80-80 80 35.8 80 80-35.8 80-80 80zm256-256H256V544h512v128z"></path>
      </svg>
      <p class="upload-text">{{ uploadText }}</p>
    </div>

    <!-- 视频预览 -->
    <div class="video-preview" v-if="videoUrl">
      <video
          ref="videoPlayer"
          :src="videoUrl"
          controls
          width="100%"
          height="auto"
          class="video-player"
      ></video>
      <div class="upload-progress" v-if="showProgress">
        <div class="progress-bar" :style="{ width: progressPercent + '%' }"></div>
        <span class="progress-text">{{ progressText }}</span>
      </div>
    </div>

    <!-- 审核结果 -->
    <div class="audit-result" v-if="auditResult">
      <div class="result-tag" :class="auditResult.isPass ? 'tag-pass' : 'tag-fail'">
        {{ auditResult.isPass ? '视频审核通过' : '视频审核不通过' }}
      </div>
      <div class="result-detail" v-if="!auditResult.isPass">
        <p>违规类型：{{ auditResult.violationType }}</p>
        <p>置信度：{{ auditResult.violationScore }}/100</p>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive, onUnmounted } from 'vue';
import axios from 'axios';

export default {
  setup() {
    // 响应式变量
    const fileInput = ref(null);
    const videoPlayer = ref(null);
    const isDragging = ref(false);
    const videoUrl = ref('');
    const showProgress = ref(false);
    const progressPercent = ref(0);
    const progressText = ref('');
    const uploadText = ref('点击或拖拽上传视频（支持MP4/AVI/MOV）');
    const auditResult = ref(null);
    let ws = null; // WebSocket实例

    // 处理文件选择
    const handleFileChange = (e) => {
      const file = e.target.files[0];
      if (!file) return;

      // 校验文件类型
      const videoTypes = ['video/mp4', 'video/avi', 'video/mov'];
      if (!videoTypes.includes(file.type)) {
        alert('仅支持MP4/AVI/MOV格式的视频');
        return;
      }

      // 校验文件大小（限制500MB）
      const maxSize = 500 * 1024 * 1024;
      if (file.size > maxSize) {
        alert('视频大小不能超过500MB');
        return;
      }

      // 预览视频
      videoUrl.value = URL.createObjectURL(file);
      uploadText.value = `已选择：${file.name}`;

      // 上传视频
      uploadVideo(file);
    };

    // 上传视频
    const uploadVideo = async (file) => {
      try {
        showProgress.value = true;
        progressPercent.value = 0;
        progressText.value = '正在上传视频...';

        // 构建FormData
        const formData = new FormData();
        formData.append('file', file);

        // 上传视频（分片上传可参考WebUploader，这里简化为普通上传）
        const response = await axios({
          url: '/api/ai/upload/video',
          method: 'post',
          data: formData,
          headers: { 'Content-Type': 'multipart/form-data' },
          onUploadProgress: (e) => {
            // 上传进度
            if (e.total > 0) {
              progressPercent.value = Math.round((e.loaded / e.total) * 100);
              progressText.value = `上传中：${progressPercent.value}%`;
            }
          },
          timeout: 60000 // 超时时间60秒
        });

        if (response.data.code === 200) {
          // 上传成功，开始监听审核结果
          progressText.value = '视频上传成功，等待审核...';
          const taskId = response.data.taskId;
          listenAuditResult(taskId);
        } else {
          throw new Error(response.data.msg || '视频上传失败');
        }
      } catch (error) {
        alert('上传失败：' + error.message);
        showProgress.value = false;
      }
    };

    // 监听审核结果（WebSocket）
    const listenAuditResult = (taskId) => {
      // 关闭原有连接
      if (ws) {
        ws.close();
      }

      // 建立WebSocket连接
      const wsProtocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
      ws = new WebSocket(`${wsProtocol}//${window.location.host}/ai-detect/result/${taskId}`);

      // 连接成功
      ws.onopen = () => {
        console.log('WebSocket连接成功，监听审核结果');
      };

      // 接收消息
      ws.onmessage = (e) => {
        const result = JSON.parse(e.data);
        auditResult.value = result;
        showProgress.value = false;
        progressText.value = '';

        // 关闭连接
        ws.close();
      };

      // 连接错误
      ws.onerror = (error) => {
        console.error('WebSocket错误：', error);
        progressText.value = 'WebSocket连接失败，将轮询结果';
        pollAuditResult(taskId);
      };

      // 连接关闭
      ws.onclose = (e) => {
        console.log('WebSocket连接关闭：', e.code);
      };
    };

    // 轮询审核结果（降级方案）
    const pollAuditResult = (taskId) => {
      const interval = setInterval(async () => {
        try {
          const response = await axios.get('/api/ai/detect/result', {
            params: { taskId }
          });
          if (response.data.status === 'completed' || response.data.status === 'failed') {
            auditResult.value = response.data;
            showProgress.value = false;
            clearInterval(interval);
          }
        } catch (error) {
          console.error('轮询失败：', error);
        }
      }, 3000);
    };

    // 触发文件选择
    const triggerFileInput = () => {
      fileInput.value.click();
    };

    // 拖拽事件
    const handleDragEnter = (e) => {
      e.preventDefault();
      isDragging.value = true;
    };

    const handleDragLeave = (e) => {
      e.preventDefault();
      isDragging.value = false;
    };

    const handleDrop = (e) => {
      e.preventDefault();
      isDragging.value = false;
      const file = e.dataTransfer.files[0];
      if (file) {
        // 模拟文件选择
        const dataTransfer = new DataTransfer();
        dataTransfer.items.add(file);
        fileInput.value.files = dataTransfer.files;
        handleFileChange({ target: fileInput.value });
      }
    };

    // 组件销毁时清理
    onUnmounted(() => {
      if (ws) {
        ws.close();
      }
      if (videoUrl.value) {
        URL.revokeObjectURL(videoUrl.value);
      }
    });

    return {
      fileInput,
      videoPlayer,
      isDragging,
      videoUrl,
      showProgress,
      progressPercent,
      progressText,
      uploadText,
      auditResult,
      triggerFileInput,
      handleFileChange,
      handleDragEnter,
      handleDragLeave,
      handleDrop
    };
  }
};
</script>

<style scoped>
.video-upload-container {
  width: 100%;
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.upload-area {
  border: 2px dashed #e6e9ed;
  border-radius: 12px;
  padding: 60px 20px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
}

.upload-area.dragging {
  border-color: #409eff;
  background-color: #f0f7ff;
}

.upload-icon {
  width: 64px;
  height: 64px;
  margin-bottom: 16px;
}

.upload-text {
  font-size: 16px;
  color: #4e5d78;
  margin: 0;
}

.video-preview {
  margin-top: 24px;
  border: 1px solid #e6e9ed;
  border-radius: 8px;
  overflow: hidden;
}

.video-player {
  max-height: 400px;
}

.upload-progress {
  position: relative;
  height: 30px;
  background-color: #f5f5f5;
}

.progress-bar {
  position: absolute;
  left: 0;
  top: 0;
  height: 100%;
  background-color: #409eff;
  transition: width 0.3s ease;
}

.progress-text {
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
  font-size: 12px;
  color: #1a2b48;
}

.audit-result {
  margin-top: 24px;
  padding: 16px;
  border-radius: 8px;
  background-color: #fafbfc;
}

.result-tag {
  padding: 8px 16px;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 500;
  text-align: center;
}

.tag-pass {
  background-color: #f0fff4;
  color: #67c23a;
}

.tag-fail {
  background-color: #fff2f0;
  color: #f56c6c;
}

.result-detail {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #e6e9ed;
}

.result-detail p {
  margin: 4px 0;
  font-size: 14px;
  color: #4e5d78;
}
</style>