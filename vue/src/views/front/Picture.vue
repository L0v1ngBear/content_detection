<template>
  <div class="ai-image-detect-container sidebar-layout">
    <!-- 左侧功能侧边栏 -->
    <div class="sidebar">
      <div class="sidebar-header">
        <h3>AI图片检测</h3>
        <p>合规校验工具</p>
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
          <div class="upload-icon-wrapper">
            <svg class="upload-icon" viewBox="0 0 24 24" width="60" height="60">
              <defs>
                <linearGradient id="uploadGradient" x1="0%" y1="0%" x2="100%" y2="100%">
                  <stop offset="0%" stop-color="#4f46e5" />
                  <stop offset="100%" stop-color="#7c3aed" />
                </linearGradient>
              </defs>
              <path fill="url(#uploadGradient)" d="M12 16q1.25 0 2.125-.875T15 13q0-1.25-.875-2.125T12 10q-1.25 0-2.125.875T9 13q0 1.25.875 2.125T12 16Zm0-6q.412 0 .707-.294T13 9q0-.412-.293-.706T12 8q-.412 0-.707.294T11 9q0 .412.293.706T12 10Zm0 7q-2.075 0-3.537-1.463T7 18q0-.825.437-1.512T9 15.5q.412-.175.65-.55t.237-.75q0-.412-.293-.706T9 13q-.825 0-1.512.437T7 15q.825 0 1.512-.437T12 16Zm0-11q-2.5 0-4.25 1.75T6 11v6q0 1.25.875 2.125T9 21h6q1.25 0 2.125-.875T18 18v-6q0-2.5-1.75-4.25T12 6Zm0 2q1.5 0 2.5 1t1 2.5v6q0 .412-.293.706T14 17h-4q-.412 0-.707-.294T9 16v-6q0-1.5 1-2.5t2.5-1Z"/>
            </svg>
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
              <span class="btn-text">上传图片检测</span>
            </button>
          </div>

          <p class="tips">{{ dragOver ? '释放上传' : '支持拖拽/点击上传' }}</p>
          <p class="format-tips">支持 JPG/PNG/WEBP 格式 | 最大 5MB</p>
          <p class="login-tips" v-if="!isLogin">请先登录后再使用检测功能</p>

          <!-- 上传成功提示（含历史记录跳转） -->
          <div class="upload-success-tip" v-if="uploadSuccess">
            <svg class="success-icon" viewBox="0 0 24 24" width="20" height="20">
              <path fill="#10b981" d="M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z"/>
            </svg>
            <span>图片检测已提交！</span>
            <button class="history-btn" @click="goToHistoryPage">查看检测记录</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 右侧内容区域 -->
    <div class="main-content">
      <!-- 状态展示区域 -->
      <div class="status-container" ref="statusContainerRef">
        <!-- 未登录状态 -->
        <div class="empty-state login-empty" v-if="!isLogin">
          <svg class="empty-icon" viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg">
            <path fill="#c0c4cc" d="M512 64C264.6 64 64 264.6 64 512s200.6 448 448 448 448-200.6 448-448S759.4 64 512 64zm0 820c-205.4 0-372-166.6-372-372s166.6-372 372-372 372 166.6 372 372-166.6 372-372 372z"/>
            <path fill="#c0c4cc" d="M512 336c-48.7 0-88 39.3-88 88s39.3 88 88 88 88-39.3 88-88-39.3-88-88-88zm0 136c-26.5 0-48-21.5-48-48s21.5-48 48-48 48 21.5 48 48-21.5 48-48 48zm184 208H328c-17.7 0-32-14.3-32-32v-32c0-17.7 14.3-32 32-32h368c17.7 0 32 14.3 32 32v32c0 17.7-14.3 32-32 32z"/>
          </svg>
          <p>请先登录账号，才能使用AI图片检测功能</p>
          <button class="login-btn" @click="goToLogin">立即登录</button>
        </div>

        <!-- 初始空状态（已登录） -->
        <div class="empty-state initial-empty" v-else-if="detectResult.detectStatus === 'idle' && !uploadSuccess">
          <div class="empty-illustration">
            <svg viewBox="0 0 24 24" width="120" height="120" fill="#f0f2f5">
              <path d="M21 19V5c0-1.1-.9-2-2-2H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2zM8.5 13.5l2.5 3.01L14.5 12l4.5 6H5l3.5-4.5z"/>
              <path d="M8.5 13.5l2.5 3.01L14.5 12l4.5 6H5l3.5-4.5z" fill="#e8e8e8"/>
            </svg>
          </div>
          <h3>上传图片开始检测</h3>
          <p>AI智能检测图片合规性，快速识别违规内容</p>
        </div>

        <!-- 检测中状态 -->
        <div class="detecting-state" v-else-if="detectResult.detectStatus === 'submitting'">
          <div class="loading-spinner">
            <div class="spinner"></div>
          </div>
          <p class="detecting-text">{{ detectResult.detectMsg }}</p>
        </div>

        <!-- 错误状态 -->
        <div class="error-state" v-else-if="detectResult.detectStatus === 'error'">
          <svg class="error-icon" viewBox="0 0 24 24" width="80" height="80">
            <path fill="#ef4444" d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-2h2v2zm0-4h-2V7h2v6z"/>
          </svg>
          <p class="error-text">{{ detectResult.detectMsg }}</p>
          <button class="retry-btn" @click="triggerFileInput" :disabled="isDetectingFlag">重新上传</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive, onUnmounted, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import request from '../../utils/request';

export default {
  name: "AIImageDetect",
  setup() {
    // 路由实例
    const router = useRouter();

    // 响应式变量
    const imageFile = ref(null);
    const fileInputRef = ref(null);
    const statusContainerRef = ref(null);
    const dragOver = ref(false);
    const isLogin = ref(false);
    const isDetectingFlag = ref(false);
    const uploadSuccess = ref(false); // 上传成功标识

    // 检测结果
    const detectResult = reactive({
      detectStatus: 'idle', // idle/submitting/error
      detectMsg: ''
    });

    // 配置项
    const maxImageSize = 5 * 1024 * 1024;
    const allowImageTypes = ['image/jpg', 'image/jpeg', 'image/png', 'image/webp'];

    // 校验登录状态
    const checkLoginStatus = () => {
      const accessToken = localStorage.getItem("accessToken");
      const tokenExpireTimestamp = localStorage.getItem("tokenExpireTimestamp");
      const isTokenValid = accessToken && tokenExpireTimestamp && new Date().getTime() < Number(tokenExpireTimestamp);
      isLogin.value = isTokenValid;
      if (!isLogin.value) {
        ElMessage.warning('请先登录账号，才能使用AI图片检测功能');
      }
    };

    // 跳转到登录页
    const goToLogin = () => {
      router.push({
        path: '/login',
        query: { redirect: router.currentRoute.fullPath }
      });
    };

    // 跳转到检测历史记录页面
    const goToHistoryPage = () => {
      // 请根据实际路由路径修改，这里默认使用 /detect-history
      router.push('/front/history');
    };

    // 更新检测状态
    const updateDetectingStatus = () => {
      isDetectingFlag.value = detectResult.detectStatus === 'submitting';
    };

    // 触发文件选择框
    const triggerFileInput = () => {
      if (!isLogin.value) {
        ElMessage.warning('请先登录后再上传图片');
        goToLogin();
        return;
      }
      if (fileInputRef.value && !isDetectingFlag.value) {
        uploadSuccess.value = false; // 重置上传成功状态
        fileInputRef.value.click();
      }
    };

    // 处理图片文件
    const handleImageFile = (file) => {
      if (!isLogin.value) {
        ElMessage.warning('请先登录后再上传图片');
        goToLogin();
        return;
      }
      if (!file) return;

      // 重置状态
      detectResult.detectStatus = 'idle';
      detectResult.detectMsg = '';
      uploadSuccess.value = false;
      updateDetectingStatus();

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

      imageFile.value = file;
      // 开始检测
      handleImageAIDetect();
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
      if (!isDetectingFlag.value && isLogin.value) {
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

      if (!isLogin.value) {
        ElMessage.warning('请先登录后再上传图片');
        goToLogin();
        return;
      }

      if (isDetectingFlag.value) {
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

    // AI检测核心逻辑（简化版：仅判断接口返回200）
    const handleImageAIDetect = async () => {
      if (!isLogin.value) {
        ElMessage.warning('请先登录后再进行检测');
        goToLogin();
        return;
      }

      if (!imageFile.value || isDetectingFlag.value) return;

      try {
        detectResult.detectStatus = 'submitting';
        detectResult.detectMsg = '正在提交图片至AI审核';
        updateDetectingStatus();

        const formData = new FormData();
        formData.append('file', imageFile.value);

        // 调用后端接口
        const response = await request({
          url: '/review/picture',
          method: 'post',
          data: formData,
          headers: { 'Content-Type': 'multipart/form-data' },
          timeout: 30000
        });

        // 仅判断接口返回200即为成功
        if (response && response.code === 200) {
          // 标记上传成功
          uploadSuccess.value = true;
          detectResult.detectStatus = 'idle'; // 重置为初始状态
          ElMessage.success('图片检测已提交，可前往历史记录查看详情');
          updateDetectingStatus();
        } else {
          throw new Error(response?.msg || '图片提交失败');
        }

      } catch (error) {
        console.error('图片提交失败：', error);
        detectResult.detectStatus = 'error';
        detectResult.detectMsg = error.message || '图片提交失败，请检查网络或图片格式！';
        updateDetectingStatus();
      }
    };

    // 组件生命周期
    onMounted(() => {
      checkLoginStatus();
      updateDetectingStatus();
      watch([() => detectResult.detectStatus], () => {
        updateDetectingStatus();
      }, { immediate: true });
    });

    onUnmounted(() => {
      imageFile.value = null;
    });

    return {
      fileInputRef,
      statusContainerRef,
      dragOver,
      detectResult,
      isDetectingFlag,
      isLogin,
      uploadSuccess,
      triggerFileInput,
      handleImageChange,
      handleDragOver,
      handleDragLeave,
      handleDrop,
      goToLogin,
      goToHistoryPage
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
  width: 350px;
  height: 100%;
  background-color: #ffffff;
  border-right: 1px solid #e2e8f0;
  display: flex;
  flex-direction: column;
  padding: 32px 24px;
  box-shadow: 2px 0 12px rgba(0, 0, 0, 0.04);
}

.sidebar-header {
  text-align: center;
  margin-bottom: 40px;
}

.sidebar-header h3 {
  font-size: 24px;
  color: #1e293b;
  margin-bottom: 8px;
  font-weight: 600;
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

/* 精美上传区域样式 */
.upload-area {
  border: 2px dashed #e2e8f0;
  border-radius: 20px;
  padding: 56px 24px;
  text-align: center;
  transition: all 0.3s ease;
  cursor: pointer;
  background-color: #ffffff;
  position: relative;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.02);
}

.upload-area:hover:not(.drag-over):not(:disabled) {
  border-color: #818cf8;
  box-shadow: 0 6px 20px rgba(79, 70, 229, 0.06);
  transform: translateY(-2px);
}

.upload-area.drag-over {
  border-color: #4f46e5;
  background-color: #f5f3ff;
  box-shadow: 0 8px 25px rgba(79, 70, 229, 0.12);
  transform: translateY(-4px);
}

.upload-icon-wrapper {
  margin-bottom: 28px;
  position: relative;
}

.upload-icon-wrapper::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 80px;
  height: 80px;
  background: radial-gradient(circle, rgba(79,70,229,0.1) 0%, rgba(79,70,229,0) 70%);
  border-radius: 50%;
  z-index: -1;
}

.upload-btn-container {
  position: relative;
  display: inline-block;
  margin-bottom: 16px;
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

.upload-btn {
  background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%);
  color: white;
  border: none;
  padding: 14px 32px;
  border-radius: 12px;
  cursor: pointer;
  font-size: 16px;
  font-weight: 500;
  transition: all 0.3s ease;
  position: relative;
  z-index: 2;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  box-shadow: 0 4px 15px rgba(79, 70, 229, 0.2);
}

.upload-btn:disabled {
  background: linear-gradient(135deg, #94a3b8 0%, #cbd5e1 100%);
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.upload-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(79, 70, 229, 0.3);
}

.btn-icon {
  font-size: 18px;
}

.tips {
  font-size: 14px;
  color: #64748b;
  margin-bottom: 8px;
}

.format-tips {
  font-size: 12px;
  color: #94a3b8;
  margin-bottom: 12px;
}

.login-tips {
  font-size: 13px;
  color: #ef4444;
  margin-top: 8px;
}

/* 上传成功提示 */
.upload-success-tip {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-top: 16px;
  padding: 10px;
  background-color: #ecfdf5;
  border-radius: 8px;
  color: #065f46;
  font-size: 14px;
  animation: fadeIn 0.5s ease;
}

.success-icon {
  flex-shrink: 0;
}

.history-btn {
  background-color: #10b981;
  color: white;
  border: none;
  padding: 4px 12px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 12px;
  margin-left: 8px;
  transition: all 0.2s ease;
}

.history-btn:hover {
  background-color: #059669;
}

/* 主内容区域 */
.main-content {
  flex: 1;
  height: 100%;
  display: flex;
  flex-direction: column;
  background-color: #f8fafc;
}

.status-container {
  flex: 1;
  padding: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #94a3b8;
  text-align: center;
}

.login-empty {
  gap: 20px;
}

.initial-empty {
  gap: 16px;
}

.empty-icon {
  width: 100px;
  height: 100px;
  margin-bottom: 20px;
  opacity: 0.8;
}

.empty-illustration {
  margin-bottom: 24px;
}

.empty-state h3 {
  font-size: 22px;
  color: #334155;
  margin-bottom: 8px;
  font-weight: 500;
}

.empty-state p {
  font-size: 16px;
  color: #64748b;
  max-width: 400px;
}

.login-btn {
  background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%);
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 10px;
  cursor: pointer;
  font-size: 15px;
  font-weight: 500;
  margin-top: 12px;
  transition: all 0.2s ease;
  box-shadow: 0 4px 15px rgba(79, 70, 229, 0.2);
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(79, 70, 229, 0.3);
}

/* 检测中状态 */
.detecting-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 24px;
}

.loading-spinner {
  width: 80px;
  height: 80px;
  position: relative;
}

.spinner {
  width: 100%;
  height: 100%;
  border: 4px solid rgba(79, 70, 229, 0.1);
  border-radius: 50%;
  border-top-color: #4f46e5;
  animation: spin 1s ease-in-out infinite;
}

.detecting-text {
  font-size: 18px;
  color: #475569;
  font-weight: 500;
}

/* 错误状态 */
.error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 20px;
  text-align: center;
}

.error-icon {
  margin-bottom: 8px;
}

.error-text {
  font-size: 16px;
  color: #dc2626;
  max-width: 400px;
}

.retry-btn {
  background-color: #4f46e5;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s ease;
}

.retry-btn:disabled {
  background-color: #94a3b8;
  cursor: not-allowed;
}

.retry-btn:hover:not(:disabled) {
  background-color: #4338ca;
}

/* 动画效果 */
@keyframes spin {
  to { transform: rotate(360deg); }
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
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
    padding: 24px 16px;
  }

  .sidebar-header {
    margin-bottom: 24px;
  }

  .upload-area {
    padding: 40px 16px;
  }

  .main-content {
    height: calc(100vh - 300px);
  }

  .status-container {
    padding: 20px;
  }
}
</style>