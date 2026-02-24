<template>
  <div class="ai-image-detect-container sidebar-layout">
    <!-- 左侧功能侧边栏 -->
    <div class="sidebar">
      <div class="sidebar-header">
        <h3>AI图片检测</h3>
        <p>YOLO合规校验工具</p>
      </div>
      <div class="sidebar-content">
        <!-- 美化后的上传区域 -->
        <div
            class="upload-area"
            @dragover.prevent="handleDragOver"
            @dragleave.prevent="handleDragLeave"
            @drop.prevent="handleDrop"
            :class="{ 'drag-over': dragOver }"
        >
          <!-- 精美上传图标 -->
          <div class="upload-icon-wrapper" v-if="!previewUrl">
            <svg class="upload-icon" viewBox="0 0 24 24" width="60" height="60">
              <defs>
                <linearGradient id="uploadGradient" x1="0%" y1="0%" x2="100%" y2="100%">
                  <stop offset="0%" stop-color="#4f46e5"/>
                  <stop offset="100%" stop-color="#7c3aed"/>
                </linearGradient>
              </defs>
              <path fill="url(#uploadGradient)"
                    d="M12 16q1.25 0 2.125-.875T15 13q0-1.25-.875-2.125T12 10q-1.25 0-2.125.875T9 13q0 1.25.875 2.125T12 16Zm0-6q.412 0 .707-.294T13 9q0-.412-.293-.706T12 8q-.412 0-.707.294T11 9q0 .412.293.706T12 10Zm0 7q-2.075 0-3.537-1.463T7 18q0-.825.437-1.512T9 15.5q.412-.175.65-.55t.237-.75q0-.412-.293-.706T9 13q-.825 0-1.512.437T7 15q.825 0 1.512-.437T12 16Zm0-11q-2.5 0-4.25 1.75T6 11v6q0 1.25.875 2.125T9 21h6q1.25 0 2.125-.875T18 18v-6q0-2.5-1.75-4.25T12 6Zm0 2q1.5 0 2.5 1t1 2.5v6q0 .412-.293.706T14 17h-4q-.412 0-.707-.294T9 16v-6q0-1.5 1-2.5t2.5-1Z"/>
            </svg>
          </div>

          <!-- 图片预览 -->
          <div class="image-preview" v-if="previewUrl">
            <img :src="previewUrl" alt="预览" class="preview-img"/>
            <button class="close-preview" @click="clearPreview">×</button>
          </div>

          <!-- 上传按钮容器 -->
          <div class="upload-btn-container">
            <input
                ref="fileInputRef"
                type="file"
                accept="image/jpg,image/jpeg,image/png,image/webp"
                class="file-input"
                @change="handleImageChange"
            />
            <button class="upload-btn" @click="triggerFileInput" :disabled="isDetectingFlag || !isLogin">
              <span class="btn-icon">📤</span>
              <span class="btn-text">{{ previewUrl ? '重新上传图片' : '上传图片检测' }}</span>
            </button>
          </div>

          <p class="tips">{{ dragOver ? '释放上传' : '支持拖拽/点击上传' }}</p>
          <p class="format-tips">支持 JPG/PNG/WEBP 格式 | 最大 5MB</p>
          <p class="login-tips" v-if="!isLogin">请先登录后再使用检测功能</p>
        </div>
      </div>
    </div>

    <!-- 右侧内容区域 -->
    <div class="main-content">
      <div class="status-container" ref="statusContainerRef">
        <!-- 未登录 -->
        <div class="empty-state login-empty" v-if="!isLogin">
          <svg class="empty-icon" viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg">
            <path fill="#c0c4cc"
                  d="M512 64C264.6 64 64 264.6 64 512s200.6 448 448 448 448-200.6 448-448S759.4 64 512 64zm0 820c-205.4 0-372-166.6-372-372s166.6-372 372-372 372 166.6 372 372-166.6 372-372 372z"/>
            <path fill="#c0c4cc"
                  d="M512 336c-48.7 0-88 39.3-88 88s39.3 88 88 88 88-39.3 88-88-39.3-88-88-88zm0 136c-26.5 0-48-21.5-48-48s21.5-48 48-48 48 21.5 48 48-21.5 48-48 48zm184 208H328c-17.7 0-32-14.3-32-32v-32c0-17.7 14.3-32 32-32h368c17.7 0 32 14.3 32 32v32c0 17.7-14.3 32-32 32z"/>
          </svg>
          <p>请先登录账号，才能使用AI图片检测功能</p>
          <button class="login-btn" @click="goToLogin">立即登录</button>
        </div>

        <!-- 初始空状态 -->
        <div class="empty-state initial-empty"
             v-else-if="detectResult.detectStatus === 'idle' && !previewUrl">
          <div class="empty-illustration">
            <svg viewBox="0 0 24 24" width="120" height="120" fill="#f0f2f5">
              <path
                  d="M21 19V5c0-1.1-.9-2-2-2H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2zM8.5 13.5l2.5 3.01L14.5 12l4.5 6H5l3.5-4.5z"/>
            </svg>
          </div>
          <h3>上传图片开始YOLO检测</h3>
          <p>AI智能检测图片合规性，快速识别违规内容</p>
        </div>

        <!-- 检测中/轮询中 -->
        <div class="detecting-state" v-else-if="detectResult.detectStatus === 'submitting' || detectResult.detectStatus === 'polling'">
          <div class="loading-spinner">
            <div class="spinner"></div>
          </div>
          <p class="detecting-text">{{ detectResult.detectMsg }}</p>
        </div>

        <!-- 错误 -->
        <div class="error-state" v-else-if="detectResult.detectStatus === 'error'">
          <svg class="error-icon" viewBox="0 0 24 24" width="80" height="80">
            <path fill="#ef4444" d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-2h2v2zm0-4h-2V7h2v6z"/>
          </svg>
          <p class="error-text">{{ detectResult.detectMsg }}</p>
          <button class="retry-btn" @click="triggerFileInput" :disabled="isDetectingFlag">重新上传</button>
        </div>

        <!-- YOLO检测结果（适配后端返回的Normal/Adult/Violent + 置信度） -->
        <div class="result-state" v-else-if="detectResult.detectStatus === 'success'">
          <div class="result-header">
            <div class="result-icon">
              {{ detectResult.yoloResult.isPass ? '✅' : '⚠️' }}
            </div>
            <h3>{{ detectResult.yoloResult.isPass ? '检测合规' : '检测到违规内容' }}</h3>
          </div>

          <div class="result-details">
            <div class="result-item">
              <span class="label">文件名称：</span>
              <span class="value">{{ detectResult.yoloResult.filename }}</span>
            </div>
            <div class="result-item">
              <span class="label">检测时间：</span>
              <span class="value">{{ detectResult.yoloResult.detectTime }}</span>
            </div>
            <div class="result-item">
              <span class="label">检测结果：</span>
              <span class="value" :class="!detectResult.yoloResult.isPass ? 'high' : 'safe'">
                {{ detectResult.yoloResult.isPass ? '合规' : detectResult.yoloResult.violationType }}
              </span>
            </div>
            <div class="result-item">
              <span class="label">置信度：</span>
              <span class="value">{{ (detectResult.yoloResult.confidence * 100).toFixed(2) }}%</span>
            </div>
          </div>

          <div class="result-actions">
            <button class="btn-primary" @click="clearDetection">重新检测</button>
            <button class="btn-secondary" @click="goToHistoryPage">查看检测记录</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive, onUnmounted, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import request from '../../utils/request';

export default {
  name: "AIImageDetect",
  setup() {
    const router = useRouter();

    // 响应式变量
    const imageFile = ref(null);
    const fileInputRef = ref(null);
    const statusContainerRef = ref(null);
    const dragOver = ref(false);
    const isLogin = ref(true); // 模拟已登录
    const isDetectingFlag = ref(false);
    const previewUrl = ref('');
    let pollTimer = null;

    // 违规类型映射（严格匹配后端返回的大小写：Normal/Adult/Violent）
    const violationTypeMap = {
      Normal: '正常',
      Adult: '色情',
      Violent: '暴力'
    };

    // 检测结果（适配后端返回的Normal/Adult/Violent + 置信度）
    const detectResult = reactive({
      detectStatus: 'idle', // idle / submitting / polling / error / success
      detectMsg: '',
      taskId: '',
      yoloResult: {
        filename: '',
        detectTime: '',
        isPass: true,       // 仅Normal为true，其他均为false
        violationType: '',
        confidence: 0       // 后端返回的置信度（0-1）
      }
    });

    // 配置项
    const maxImageSize = 5 * 1024 * 1024;
    const allowImageTypes = ['image/jpg', 'image/jpeg', 'image/png', 'image/webp'];

    // 登录校验（模拟）
    const checkLoginStatus = () => {
      isLogin.value = true; // 实际项目中替换为真实登录校验
    };

    // 跳转登录
    const goToLogin = () => {
      router.push({path: '/login', query: {redirect: router.currentRoute.fullPath}});
    };

    // 跳转历史记录
    const goToHistoryPage = () => {
      router.push('/front/history');
    };

    // 更新检测状态
    const updateDetectingStatus = () => {
      isDetectingFlag.value = ['submitting', 'polling'].includes(detectResult.detectStatus);
    };

    // 生成图片预览
    const createPreview = (file) => {
      if (previewUrl.value) URL.revokeObjectURL(previewUrl.value);
      previewUrl.value = URL.createObjectURL(file);
    };

    // 清空预览
    const clearPreview = () => {
      if (previewUrl.value) URL.revokeObjectURL(previewUrl.value);
      previewUrl.value = '';
      imageFile.value = null;
      detectResult.detectStatus = 'idle';
      detectResult.detectMsg = '';
      detectResult.taskId = '';
      // 清空轮询定时器
      if (pollTimer) {
        clearInterval(pollTimer);
        pollTimer = null;
      }
    };

    // 清空本次检测
    const clearDetection = () => {
      clearPreview();
    };

    // 触发文件选择
    const triggerFileInput = () => {
      if (!isLogin.value) {
        ElMessage.warning('请先登录');
        goToLogin();
        return;
      }
      if (fileInputRef.value && !isDetectingFlag.value) {
        fileInputRef.value.click();
      }
    };

    // 处理文件选择
    const handleImageFile = (file) => {
      if (!isLogin.value) {
        ElMessage.warning('请先登录');
        goToLogin();
        return;
      }
      if (!file) return;

      // 重置状态
      detectResult.detectStatus = 'idle';
      detectResult.detectMsg = '';
      detectResult.taskId = '';

      // 格式校验
      if (!allowImageTypes.includes(file.type)) {
        detectResult.detectStatus = 'error';
        detectResult.detectMsg = '仅支持 JPG/PNG/WEBP 格式！';
        return;
      }

      // 大小校验
      if (file.size > maxImageSize) {
        detectResult.detectStatus = 'error';
        detectResult.detectMsg = `图片大小超过 5MB 限制，当前：${(file.size / 1024 / 1024).toFixed(2)}MB`;
        return;
      }

      // 生成预览
      createPreview(file);
      imageFile.value = file;
      // 开始检测
      handleImageAIDetect();
    };

    // 点击上传
    const handleImageChange = (e) => {
      const file = e.target.files?.[0];
      if (file) handleImageFile(file);
      // 重置input值
      setTimeout(() => {
        if (fileInputRef.value) fileInputRef.value.value = '';
      }, 100);
    };

    // 拖拽上传
    const handleDragOver = (e) => {
      e.preventDefault();
      if (!isDetectingFlag.value && isLogin.value) dragOver.value = true;
    };

    const handleDragLeave = (e) => {
      e.preventDefault();
      dragOver.value = false;
    };

    const handleDrop = (e) => {
      e.preventDefault();
      dragOver.value = false;
      if (!isLogin.value) {
        ElMessage.warning('请先登录');
        goToLogin();
        return;
      }
      if (isDetectingFlag.value) return;
      const file = e.dataTransfer.files?.[0];
      if (file) handleImageFile(file);
    };

    // 轮询获取YOLO检测结果（核心：严格按Normal判定合规）
    const startPollResult = () => {
      // 防止重复轮询
      if (pollTimer) clearInterval(pollTimer);

      // 每1秒轮询一次
      pollTimer = setInterval(async () => {
        try {
          const res = await request({
            url: '/review/picture/result',
            method: 'get',
            params: {taskId: detectResult.taskId}
          });

          if (res.code !== 200) {
            throw new Error(res.msg || '获取检测结果失败');
          }

          const data = res.data;
          // 任务状态：0=排队中 1=检测中 2=完成 3=失败
          switch (data.status) {
            case 0:
              detectResult.detectMsg = '排队等待YOLO检测...';
              break;
            case 1:
              detectResult.detectMsg = 'YOLO正在检测中...';
              break;
            case 2:
              // 检测完成 - 核心逻辑：仅Normal为合规
              clearInterval(pollTimer);
              pollTimer = null;
              detectResult.detectStatus = 'success';

              // 1. 获取后端原始违规类型（严格匹配大小写）
              const rawViolationType = data.violationType || 'Normal';
              // 2. 映射为中文展示
              const showViolationType = violationTypeMap[rawViolationType] || '未知违规类型';
              // 3. 判定是否合规（仅Normal为true）
              const isPass = rawViolationType === 'Normal';

              // 4. 赋值最终结果
              detectResult.yoloResult = {
                filename: imageFile.value.name,
                detectTime: new Date().toLocaleString(),
                isPass: isPass,          // 核心：仅Normal为true
                violationType: showViolationType,
                confidence: data.confidence || 0 // 读取后端置信度
              };
              ElMessage.success('YOLO检测完成');
              break;
            case 3:
              // 检测失败
              clearInterval(pollTimer);
              pollTimer = null;
              detectResult.detectStatus = 'error';
              detectResult.detectMsg = data.msg || 'YOLO检测失败';
              break;
          }
        } catch (err) {
          console.error('轮询失败：', err);
          // 轮询失败不立即终止，继续尝试
        }
      }, 1000);
    };

    // 提交图片到后端（修复taskId取值错误）
    const handleImageAIDetect = async () => {
      if (!imageFile.value || isDetectingFlag.value) return;

      try {
        detectResult.detectStatus = 'submitting';
        detectResult.detectMsg = '图片上传中...';
        updateDetectingStatus();

        const formData = new FormData();
        formData.append('file', imageFile.value);

        // 调用上传接口
        const res = await request({
          url: '/review/picture',
          method: 'post',
          data: formData,
          headers: {'Content-Type': 'multipart/form-data'},
          timeout: 30000
        });

        if (res?.code === 200) {
          detectResult.taskId = res.data;
          detectResult.detectStatus = 'polling';
          detectResult.detectMsg = '提交成功，等待YOLO检测...';
          // 启动轮询
          startPollResult();
        } else {
          throw new Error(res?.msg || '图片提交失败');
        }
      } catch (err) {
        detectResult.detectStatus = 'error';
        detectResult.detectMsg = err.message || '图片提交失败，请检查网络！';
      } finally {
        updateDetectingStatus();
      }
    };

    // 生命周期
    onMounted(() => {
      checkLoginStatus();
      updateDetectingStatus();
    });

    onUnmounted(() => {
      // 清理资源
      if (previewUrl.value) URL.revokeObjectURL(previewUrl.value);
      if (pollTimer) clearInterval(pollTimer);
      imageFile.value = null;
    });

    return {
      fileInputRef,
      statusContainerRef,
      dragOver,
      detectResult,
      isDetectingFlag,
      isLogin,
      previewUrl,
      triggerFileInput,
      handleImageChange,
      handleDragOver,
      handleDragLeave,
      handleDrop,
      clearPreview,
      clearDetection,
      goToLogin,
      goToHistoryPage
    };
  }
};
</script>

<style scoped>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
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
  width: 350px;
  background: #fff;
  border-right: 1px solid #e2e8f0;
  display: flex;
  flex-direction: column;
  padding: 32px 24px;
}

.sidebar-header {
  text-align: center;
  margin-bottom: 40px;
}

.sidebar-header h3 {
  font-size: 24px;
  color: #1e293b;
  font-weight: 600;
  margin-bottom: 8px;
}

.sidebar-header p {
  font-size: 14px;
  color: #94a3b8;
}

.sidebar-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

/* 上传区域 */
.upload-area {
  border: 2px dashed #e2e8f0;
  border-radius: 20px;
  padding: 48px 24px;
  text-align: center;
  transition: all 0.3s;
  background: #fff;
  position: relative;
}

.upload-area:hover:not(.drag-over) {
  border-color: #818cf8;
}

.upload-area.drag-over {
  border-color: #4f46e5;
  background: #f5f3ff;
}

.upload-icon-wrapper {
  margin-bottom: 28px;
}

.image-preview {
  position: relative;
  margin: 0 auto 24px;
  max-width: 240px;
  max-height: 180px;
  border-radius: 12px;
  overflow: hidden;
}

.preview-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.close-preview {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.4);
  color: #fff;
  border: none;
  font-size: 16px;
  cursor: pointer;
}

.upload-btn-container {
  position: relative;
  display: inline-block;
  margin-bottom: 16px;
}

.file-input {
  position: absolute;
  inset: 0;
  opacity: 0;
  cursor: pointer;
  z-index: 1;
}

.upload-btn {
  background: linear-gradient(135deg, #4f46e5, #7c3aed);
  color: #fff;
  border: none;
  padding: 14px 28px;
  border-radius: 12px;
  font-size: 15px;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.upload-btn:disabled {
  background: #cbd5e1;
  cursor: not-allowed;
}

.tips {
  font-size: 14px;
  color: #64748b;
  margin-bottom: 6px;
}

.format-tips {
  font-size: 12px;
  color: #94a3b8;
}

.login-tips {
  font-size: 13px;
  color: #ef4444;
  margin-top: 10px;
}

/* 主内容区域 */
.main-content {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
}

.status-container {
  width: 100%;
  max-width: 600px;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  color: #94a3b8;
  text-align: center;
}

.empty-icon {
  width: 100px;
  height: 100px;
  margin-bottom: 20px;
}

.empty-illustration {
  margin-bottom: 24px;
}

.initial-empty h3 {
  font-size: 22px;
  color: #334155;
  margin-bottom: 8px;
}

.initial-empty p {
  font-size: 16px;
  color: #64748b;
}

.login-btn {
  background: linear-gradient(135deg, #4f46e5, #7c3aed);
  color: #fff;
  border: none;
  padding: 12px 24px;
  border-radius: 10px;
  margin-top: 16px;
  font-size: 15px;
}

/* 检测中状态 */
.detecting-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 24px;
}

.loading-spinner {
  width: 70px;
  height: 70px;
}

.spinner {
  width: 100%;
  height: 100%;
  border: 4px solid #f0f0f0;
  border-top-color: #4f46e5;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

.detecting-text {
  font-size: 18px;
  color: #475569;
}

/* 错误状态 */
.error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
  text-align: center;
}

.error-text {
  font-size: 16px;
  color: #dc2626;
}

.retry-btn {
  background: #4f46e5;
  color: #fff;
  border: none;
  padding: 10px 20px;
  border-radius: 8px;
}

/* 检测结果面板 */
.result-state {
  background: #fff;
  border-radius: 20px;
  padding: 40px 32px;
  width: 100%;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  text-align: center;
  animation: fadeIn 0.3s;
}

.result-header {
  margin-bottom: 28px;
}

.result-icon {
  font-size: 60px;
  margin-bottom: 12px;
}

.result-header h3 {
  font-size: 24px;
  color: #1e293b;
}

.result-details {
  text-align: left;
  margin-bottom: 32px;
  padding: 20px 0;
  border-top: 1px solid #f1f5f9;
  border-bottom: 1px solid #f1f5f9;
}

.result-item {
  display: flex;
  margin-bottom: 14px;
  font-size: 15px;
}

.result-item:last-child {
  margin-bottom: 0;
}

.label {
  width: 100px;
  color: #64748b;
  font-weight: 500;
}

.value {
  color: #1e293b;
}

.value.safe {
  color: #059669;
}

.value.high {
  color: #dc2626;
  font-weight: 500;
}

.result-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
}

.btn-primary {
  background: #4f46e5;
  color: #fff;
  border: none;
  padding: 12px 24px;
  border-radius: 10px;
  font-size: 15px;
  cursor: pointer;
}

.btn-secondary {
  background: #f1f5f9;
  color: #334155;
  border: none;
  padding: 12px 24px;
  border-radius: 10px;
  font-size: 15px;
  cursor: pointer;
}

/* 动画 */
@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 移动端适配 */
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
}
</style>