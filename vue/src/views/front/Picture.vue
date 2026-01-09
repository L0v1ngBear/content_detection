<template>
  <div class="ai-image-detect-container sidebar-layout">
    <!-- 左侧功能侧边栏 -->
    <div class="sidebar">
      <div class="sidebar-header">
        <h3>AI图片检测</h3>
        <p>合规校验工具</p>
      </div>
      <div class="sidebar-content">
        <div
            class="upload-area"
            @dragover.prevent="handleDragOver"
            @dragleave.prevent="handleDragLeave"
            @drop.prevent="handleDrop"
            :class="{ 'drag-over': dragOver }"
        >
          <input
              ref="fileInputRef"
              type="file"
              accept="image/jpg,image/jpeg,image/png,image/webp"
              class="file-input"
              @change="handleImageChange"
          />
          <div class="upload-icon-wrapper">
            <svg class="upload-icon" viewBox="0 0 24 24" width="40" height="40">
              <path fill="#4f46e5" d="M12 16q1.25 0 2.125-.875T15 13q0-1.25-.875-2.125T12 10q-1.25 0-2.125.875T9 13q0 1.25.875 2.125T12 16Zm0-6q.412 0 .707-.294T13 9q0-.412-.293-.706T12 8q-.412 0-.707.294T11 9q0 .412.293.706T12 10Zm0 7q-2.075 0-3.537-1.463T7 18q0-.825.437-1.512T9 15.5q.412-.175.65-.55t.237-.75q0-.412-.293-.706T9 13q-.825 0-1.512.437T7 15q.825 0 1.512-.437T12 16Zm0-11q-2.5 0-4.25 1.75T6 11v6q0 1.25.875 2.125T9 21h6q1.25 0 2.125-.875T18 18v-6q0-2.5-1.75-4.25T12 6Zm0 2q1.5 0 2.5 1t1 2.5v6q0 .412-.293.706T14 17h-4q-.412 0-.707-.294T9 16v-6q0-1.5 1-2.5t2.5-1Z"/>
            </svg>
          </div>
          <button class="upload-btn" @click="triggerFileInput" :disabled="isDetecting">
            上传图片检测
          </button>
          <p class="tips">{{ dragOver ? '释放上传' : '支持拖拽/点击' }}</p>
        </div>
        <button class="clear-btn" @click="clearAllRecords" :disabled="isDetecting || chatRecords.length === 0">
          清空检测记录
        </button>
      </div>
    </div>

    <!-- 右侧内容区域 -->
    <div class="main-content">
      <!-- 顶部说明 -->
      <div class="content-header">
        <p>支持 JPG/PNG/WEBP 格式，最大 5MB</p>
      </div>
      <!-- 检测记录容器 -->
      <div class="chat-container" ref="chatContainerRef">
        <!-- 空状态 -->
        <div class="empty-state" v-if="chatRecords.length === 0 && detectResult.detectStatus === 'idle'">
          <svg class="empty-icon" viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg">
            <path fill="#c0c4cc" d="M864 256H736v-64c0-35.3-28.7-64-64-64H352c-35.3 0-64 28.7-64 64v64H160c-17.7 0-32 14.3-32 32v640c0 17.7 14.3 32 32 32h704c17.7 0 32-14.3 32-32V288c0-17.7-14.3-32-32-32zM352 208h320v48H352v-48zm464 664H208V352h240c17.7 0 32-14.3 32-32v-48h192v48c0 17.7 14.3 32 32 32h240v520zM512 486.4V736c0 4.4-3.6 8-8 8h-48c-4.4 0-8-3.6-8-8V486.4c0-18.7-11.4-35.5-28.3-42.3l-128-42.7c-16.2-5.4-34.2 2.3-40.2 18.3l-64 192c-2.2 6.7 .2 14.1 6 18.7s12.1 6 18.8 2.2l107.9-35.9c16.2-5.4 34.2 2.3 40.2 18.3l80 240c2.2 6.7 .2 14.1-6 18.7s-12.1 6-18.8 2.2l-128-42.7c-16.2-5.4-34.2 2.3-40.2 18.3l-64 192c-2.2 6.7 .2 14.1 6 18.7s12.1 6 18.8 2.2l224-74.7c18.7-6.2 30-23.6 30-42.3V486.4c0-18.7-11.4-35.5-28.3-42.3l-128-42.7c-16.2-5.4-34.2 2.3-40.2 18.3z"/>
          </svg>
          <p>暂无检测记录，上传图片开始检测</p>
        </div>

        <!-- 对话记录 -->
        <div class="chat-record" v-for="(record, index) in chatRecords" :key="index">
          <!-- 用户上传的图片 -->
          <div class="user-message">
            <div class="avatar user-avatar">
              <span>👤</span>
            </div>
            <div class="message-content">
              <img :src="record.imageUrl" alt="检测图片" class="detect-image" />
              <p class="time">{{ record.time }}</p>
            </div>
          </div>

          <!-- AI 检测结果 -->
          <div class="ai-message" :class="record.result.isPass ? 'pass' : 'fail'">
            <div class="avatar ai-avatar">
              <span>🤖</span>
            </div>
            <div class="message-content">
              <div class="result-header">
                <span class="result-tag" :class="record.result.isPass ? 'pass-tag' : 'fail-tag'">
                  {{ record.result.isPass ? '检测合规' : '检测违规' }}
                </span>
                <span class="confidence">置信度：{{ record.result.violationScore }}/100</span>
              </div>
              <div class="result-detail" v-if="!record.result.isPass">
                <p><strong>违规类型：</strong>{{ record.result.violationType || '未知类型' }}</p>
                <p v-if="record.result.violationArea && record.result.violationArea.length > 0"><strong>违规区域：</strong>共 {{ record.result.violationArea.length }} 处疑似违规区域</p>
              </div>
              <p class="time">{{ record.time }}</p>
            </div>
          </div>
        </div>

        <!-- 检测中状态 -->
        <div class="ai-message detecting"
             v-if="detectResult.detectStatus === 'submitting' || detectResult.detectStatus === 'waiting'">
          <div class="avatar ai-avatar">
            <span>🤖</span>
          </div>
          <div class="message-content">
            <p class="detecting-text">
              {{ detectResult.detectMsg }}
              <span class="loading-dots">
                <span>.</span><span>.</span><span>.</span>
              </span>
            </p>
          </div>
        </div>

        <!-- 错误提示 -->
        <div class="ai-message error" v-if="detectResult.detectStatus === 'error'">
          <div class="avatar ai-avatar">
            <span>🤖</span>
          </div>
          <div class="message-content">
            <p class="error-text">{{ detectResult.detectMsg }}</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive, onUnmounted } from 'vue';
import request from '../../utils/request';

export default {
  name: "AIImageDetect",
  setup() {
    // 响应式变量
    const imageFile = ref(null);
    const imagePreviewUrl = ref('');
    const fileInputRef = ref(null);
    const chatContainerRef = ref(null);
    const dragOver = ref(false);
    const messageQueueListener = ref(null);
    const taskId = ref('');

    // 检测结果
    const detectResult = reactive({
      isPass: false,
      detectStatus: 'idle', // idle/submitting/waiting/success/error
      violationType: '',
      violationScore: 0,
      violationArea: [],
      detectMsg: ''
    });

    // 对话记录
    const chatRecords = ref([]);

    // 配置项
    const maxImageSize = 5 * 1024 * 1024;
    const allowImageTypes = ['image/jpg', 'image/jpeg', 'image/png', 'image/webp'];

    // 计算属性：是否正在检测
    const isDetecting = () => {
      return detectResult.detectStatus === 'submitting' || detectResult.detectStatus === 'waiting';
    };

    // 触发文件选择框
    const triggerFileInput = () => {
      if (fileInputRef.value && !isDetecting()) {
        fileInputRef.value.click();
      }
    };

    // 格式化时间
    const formatTime = () => {
      const now = new Date();
      const date = `${now.getFullYear()}-${(now.getMonth() + 1).toString().padStart(2, '0')}-${now.getDate().toString().padStart(2, '0')}`;
      const time = `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}:${now.getSeconds().toString().padStart(2, '0')}`;
      return `${date} ${time}`;
    };

    // 滚动到底部
    const scrollToChatBottom = () => {
      if (chatContainerRef.value) {
        const container = chatContainerRef.value;
        container.scrollTop = container.scrollHeight;
      }
    };

    // 处理图片文件
    const handleImageFile = (file) => {
      if (!file) return;

      // 重置上一次检测错误状态
      detectResult.detectStatus = 'idle';
      detectResult.detectMsg = '';

      // 格式校验
      if (!allowImageTypes.includes(file.type)) {
        detectResult.detectStatus = 'error';
        detectResult.detectMsg = '仅支持 JPG/JPEG/PNG/WEBP 格式！';
        return;
      }

      // 大小校验
      if (file.size > maxImageSize) {
        detectResult.detectStatus = 'error';
        detectResult.detectMsg = `图片大小超过 5MB 限制，当前：${(file.size / 1024 / 1024).toFixed(2)}MB`;
        return;
      }

      // 生成预览
      const reader = new FileReader();
      reader.onload = (e) => {
        imagePreviewUrl.value = e.target.result;
        // 开始检测
        handleImageAIDetect();
      };
      reader.onerror = () => {
        detectResult.detectStatus = 'error';
        detectResult.detectMsg = '图片预览生成失败，请更换图片重试！';
      };
      reader.readAsDataURL(file);
      imageFile.value = file;
    };

    // 点击上传
    const handleImageChange = (e) => {
      const file = e.target.files[0];
      if (file) {
        handleImageFile(file);
      }
      // 延迟重置，确保文件选择事件完成
      setTimeout(() => {
        if (fileInputRef.value) {
          fileInputRef.value.value = '';
        }
      }, 100);
    };

    // 拖拽上传
    const handleDragOver = (e) => {
      e.preventDefault();
      e.stopPropagation();
      if (!isDetecting()) {
        dragOver.value = true;
      }
    };

    const handleDragLeave = (e) => {
      e.preventDefault();
      e.stopPropagation();
      dragOver.value = false;
    };

    const handleDrop = (e) => {
      e.preventDefault();
      e.stopPropagation();
      dragOver.value = false;

      if (isDetecting()) {
        detectResult.detectStatus = 'error';
        detectResult.detectMsg = '当前正在检测中，请等待完成后再上传！';
        return;
      }

      const file = e.dataTransfer.files[0];
      if (file) {
        handleImageFile(file);
      } else {
        detectResult.detectStatus = 'error';
        detectResult.detectMsg = '请拖拽有效的图片文件！';
      }
    };

    // 消息队列结果处理
    const handleMessageQueueResult = (aiResult) => {
      // 校验返回结果的合法性
      if (!aiResult || typeof aiResult !== 'object') {
        detectResult.detectStatus = 'error';
        detectResult.detectMsg = '检测结果格式异常，请重试！';
        return;
      }

      // 更新检测结果
      detectResult.detectStatus = 'success';
      detectResult.isPass = Boolean(aiResult.isPass);
      detectResult.violationType = aiResult.violationType || '未识别违规类型';
      detectResult.violationScore = Math.max(0, Math.min(100, Number(aiResult.violationScore) || 0));
      detectResult.violationArea = Array.isArray(aiResult.violationArea) ? aiResult.violationArea : [];

      // 添加到对话记录
      chatRecords.value.push({
        imageUrl: imagePreviewUrl.value,
        time: formatTime(),
        result: { ...JSON.parse(JSON.stringify(detectResult)) }
      });

      // 滚动到底部
      scrollToChatBottom();

      // 重置任务ID和预览图
      taskId.value = '';
      imagePreviewUrl.value = '';
    };

    // 停止消息队列监听
    const stopMessageQueueListening = () => {
      if (messageQueueListener.value) {
        // WebSocket 实例处理
        if (messageQueueListener.value.close) {
          try {
            messageQueueListener.value.close(1000, '检测完成，关闭连接');
          } catch (error) {
            console.warn('WebSocket 关闭失败：', error);
          }
        }
        // 轮询定时器处理
        else if (typeof messageQueueListener.value === 'number') {
          clearInterval(messageQueueListener.value);
        }
        messageQueueListener.value = null;
      }
    };

    // AI检测
    const handleImageAIDetect = async () => {
      if (!imageFile.value || isDetecting()) return;

      try {
        // 1. 初始化检测状态
        detectResult.detectStatus = 'submitting';
        detectResult.detectMsg = '正在提交图片至AI审核';
        stopMessageQueueListening();

        // 2. 构建表单数据
        const formData = new FormData();
        formData.append('file', imageFile.value);

        // 3. 调用后端接口
        const response = await request({
          url: '/review/picture',
          method: 'post',
          data: formData,
          headers: { 'Content-Type': 'multipart/form-data' },
          timeout: 30000
        });

        // 校验后端返回结果
        if (!response || !response.data || !response.data.taskId) {
          throw new Error('未获取到检测任务ID，提交失败');
        }
        taskId.value = response.data.taskId;

        // 4. 更新状态
        detectResult.detectStatus = 'waiting';
        detectResult.detectMsg = '正在等待检测结果（消息队列处理中）';

        // 5. 消息队列监听
        /************************** 方案1：WebSocket **************************/
        const wsProtocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
        const wsUrl = `${wsProtocol}//${window.location.host}/ai-picture/result?taskId=${taskId.value}`;
        const ws = new WebSocket(wsUrl);

        ws.onopen = () => {
          console.log('WebSocket 连接成功，等待检测结果回调');
        };

        ws.onmessage = (event) => {
          try {
            const aiResult = JSON.parse(event.data);
            if (aiResult.taskId === taskId.value) {
              handleMessageQueueResult(aiResult);
              stopMessageQueueListening();
            }
          } catch (error) {
            console.error('检测结果解析失败：', error);
            detectResult.detectStatus = 'error';
            detectResult.detectMsg = '检测结果解析失败，请重试！';
            stopMessageQueueListening();
          }
        };

        ws.onerror = (error) => {
          console.error('WebSocket 连接错误：', error);
          detectResult.detectStatus = 'error';
          detectResult.detectMsg = '消息队列连接失败，将切换为轮询查询';
          startPollingResult();
        };

        ws.onclose = (event) => {
          if (event.code !== 1000 && detectResult.detectStatus === 'waiting') {
            console.warn('WebSocket 异常关闭：', event.reason);
            detectResult.detectMsg = '消息队列连接异常关闭，将切换为轮询查询';
            startPollingResult();
          }
        };

        messageQueueListener.value = ws;

      } catch (error) {
        console.error('图片提交失败：', error);
        detectResult.detectStatus = 'error';
        detectResult.detectMsg = error.message || '图片提交失败，请检查网络或图片格式！';
        stopMessageQueueListening();
      }
    };

    // 清空所有记录
    const clearAllRecords = () => {
      if (isDetecting()) return;

      // 重置所有状态
      chatRecords.value = [];
      detectResult.detectStatus = 'idle';
      detectResult.detectMsg = '';
      detectResult.isPass = false;
      detectResult.violationScore = 0;
      detectResult.violationType = '';
      detectResult.violationArea = [];
      imagePreviewUrl.value = '';
      imageFile.value = null;
      taskId.value = '';
      stopMessageQueueListening();
    };

    // 组件销毁
    onUnmounted(() => {
      stopMessageQueueListening();
      imageFile.value = null;
    });

    return {
      imagePreviewUrl,
      fileInputRef,
      chatContainerRef,
      dragOver,
      detectResult,
      chatRecords,
      isDetecting,
      triggerFileInput,
      handleImageChange,
      handleDragOver,
      handleDragLeave,
      handleDrop,
      clearAllRecords
    };
  }
};
</script>

<style scoped>
/* 基础重置 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
  overflow-x: hidden;
}

.sidebar-layout {
  display: flex;
  width: 100%;
  height: 100vh;
  background-color: #f8fafc;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
}

/* 侧边栏样式 */
.sidebar {
  width: 280px;
  height: 100%;
  background-color: #ffffff;
  border-right: 1px solid #e2e8f0;
  display: flex;
  flex-direction: column;
  padding: 24px;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.03);
}

.sidebar-header h3 {
  font-size: 18px;
  color: #1e293b;
  margin-bottom: 4px;
  font-weight: 600;
}

.sidebar-header p {
  font-size: 12px;
  color: #94a3b8;
}

.sidebar-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 20px;
  margin-top: 20px;
}

.upload-area {
  border: 2px dashed #e2e8f0;
  border-radius: 12px;
  padding: 30px 16px;
  text-align: center;
  transition: all 0.2s;
  cursor: pointer;
  background-color: #ffffff;
}

.upload-area.drag-over {
  border-color: #4f46e5;
  background-color: #f5f3ff;
}

.file-input {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  opacity: 0;
  cursor: pointer;
  z-index: 1;
}

.upload-icon-wrapper {
  margin-bottom: 16px;
}

.upload-btn {
  background-color: #4f46e5;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  margin-bottom: 8px;
  font-weight: 500;
  transition: all 0.2s;
}

.upload-btn:disabled {
  background-color: #94a3b8;
  cursor: not-allowed;
}

.upload-btn:hover:not(:disabled) {
  background-color: #4338ca;
}

.tips {
  font-size: 12px;
  color: #94a3b8;
}

.clear-btn {
  padding: 10px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  background-color: transparent;
  color: #64748b;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}

.clear-btn:disabled {
  color: #cbd5e1;
  border-color: #cbd5e1;
  cursor: not-allowed;
}

.clear-btn:hover:not(:disabled) {
  border-color: #4f46e5;
  color: #4f46e5;
  background-color: #f5f3ff;
}

/* 主内容区域 */
.main-content {
  flex: 1;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.content-header {
  padding: 16px 24px;
  border-bottom: 1px solid #e2e8f0;
  background-color: #ffffff;
}

.content-header p {
  font-size: 13px;
  color: #94a3b8;
  margin: 0;
}

.chat-container {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.chat-container::-webkit-scrollbar {
  display: none;
}

/* 空状态 */
.empty-state {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #94a3b8;
}

.empty-icon {
  width: 80px;
  height: 80px;
  margin-bottom: 16px;
  opacity: 0.8;
}

.empty-state p {
  font-size: 16px;
}

/* 对话记录 */
.chat-record {
  margin-bottom: 24px;
  overflow: hidden;
}

/* 用户消息 */
.user-message {
  display: flex;
  margin-bottom: 12px;
  overflow: hidden;
}

/* AI消息 */
.ai-message {
  display: flex;
  margin-bottom: 12px;
  padding: 12px;
  border-radius: 8px;
  background-color: #ffffff;
  border: 1px solid #e5e7eb;
  overflow: hidden;
}

.ai-message.pass {
  border-left: 3px solid #10b981;
}

.ai-message.fail {
  border-left: 3px solid #ef4444;
}

.ai-message.detecting {
  background-color: #f3f4f6;
  border-color: #d1d5db;
}

.ai-message.error {
  background-color: #fef2f2;
  border-color: #fecaca;
  border-left: 3px solid #ef4444;
}

/* 头像 */
.avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  margin-right: 12px;
  overflow: hidden;
}

.user-avatar {
  background-color: #4f46e5;
  color: white;
}

.ai-avatar {
  background-color: #10b981;
  color: white;
}

/* 消息内容 */
.message-content {
  flex: 1;
  max-width: calc(100% - 48px);
  overflow: hidden;
}

/* 用户上传的图片 */
.detect-image {
  max-width: 200px;
  max-height: 200px;
  border-radius: 6px;
  object-fit: contain;
  border: 1px solid #e5e7eb;
}

/* 时间 */
.time {
  font-size: 12px;
  color: #9ca3af;
  margin-top: 4px;
}

/* 检测结果 */
.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  flex-wrap: wrap;
  gap: 8px;
}

.result-tag {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 13px;
  font-weight: 500;
}

.pass-tag {
  background-color: #d1fae5;
  color: #059669;
}

.fail-tag {
  background-color: #fee2e2;
  color: #dc2626;
}

.confidence {
  font-size: 13px;
  color: #6b7280;
}

.result-detail {
  margin-bottom: 4px;
}

.error-text {
  color: #dc2626;
}

/* 检测中动画 */
.detecting-text {
  color: #4b5563;
  display: flex;
  align-items: center;
}

.loading-dots {
  margin-left: 8px;
  display: flex;
  gap: 2px;
}

.loading-dots span {
  animation: blink 1.4s infinite both;
}

.loading-dots span:nth-child(2) {
  animation-delay: 0.2s;
}

.loading-dots span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes blink {
  0%, 100% {
    opacity: 0.2;
  }
  50% {
    opacity: 1;
  }
}

/* 响应式适配 */
@media (max-width: 768px) {
  .sidebar-layout {
    flex-direction: column;
  }

  .sidebar {
    width: 100%;
    height: auto;
    border-right: none;
    border-bottom: 1px solid #e2e8f0;
  }

  .sidebar-content {
    justify-content: flex-start;
    margin-top: 10px;
  }

  .detect-image {
    max-width: 150px;
    max-height: 150px;
  }
}
</style>