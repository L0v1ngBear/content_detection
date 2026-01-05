<script>
import { ref, reactive, onUnmounted } from 'vue';
import request from '../../utils/request';

export default {
  name: "ImageAIDetect",
  setup() {
    // 1. 响应式变量
    const imageFile = ref(null);
    const imagePreviewUrl = ref('');
    const uploadLoading = ref(false);
    const fileInputRef = ref(null);
    // 新增：拖拽相关状态
    const dragOver = ref(false);

    const detectResult = reactive({
      isPass: false,
      detectStatus: 'idle',
      violationType: '',
      violationScore: 0,
      violationArea: [],
      detectMsg: ''
    });

    // 历史记录相关
    const historyDialogVisible = ref(false);
    const historyLoading = ref(false);
    const detectHistoryList = ref([]);

    const maxImageSize = 5 * 1024 * 1024;
    const allowImageTypes = ['image/jpg', 'image/jpeg', 'image/png', 'image/webp'];

    // 2. 核心：处理图片文件（抽取为公共方法，供点击/拖拽调用）
    const handleImageFile = (file) => {
      resetDetectState();
      if (!file) return;

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

      // 生成预览（使用 FileReader 替代 URL.createObjectURL，兼容性更好）
      const reader = new FileReader();
      reader.onload = (e) => {
        imagePreviewUrl.value = e.target.result;
      };
      reader.readAsDataURL(file);

      imageFile.value = file;
      detectResult.detectStatus = 'idle';
      detectResult.detectMsg = '已选择图片，点击「开始AI检测」进行校验';
    };

    // 3. 点击上传：监听 input change 事件
    const handleImageChange = (e) => {
      const file = e.target.files[0];
      handleImageFile(file);
      // 立即清空输入框，确保同一张图片可重复上传
      if (fileInputRef.value) {
        fileInputRef.value.value = '';
      }
    };

    // 4. 拖拽上传：新增拖拽事件（备选上传方式）
    const handleDragOver = (e) => {
      e.preventDefault();
      e.stopPropagation();
      dragOver.value = true;
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
      const file = e.dataTransfer.files[0];
      if (file && allowImageTypes.includes(file.type)) {
        handleImageFile(file);
      } else {
        detectResult.detectStatus = 'error';
        detectResult.detectMsg = '请拖拽有效的图片文件！';
      }
    };

    const resetDetectState = () => {
      // 重置检测结果
      detectResult.isPass = false;
      detectResult.detectStatus = 'idle';
      detectResult.violationType = '';
      detectResult.violationScore = 0;
      detectResult.violationArea = [];
      detectResult.detectMsg = '';

      // 重置图片相关
      imageFile.value = null;
      if (imagePreviewUrl.value) {
        imagePreviewUrl.value = '';
      }

      // 强制重置文件输入框（核心修复）
      if (fileInputRef.value) {
        // 方案1：直接清空 value
        fileInputRef.value.value = '';
      }
    };

    // 6. AI
    const handleImageAIDetect = async () => {
      if (!imageFile.value) {
        detectResult.detectStatus = 'error';
        detectResult.detectMsg = '请先选择需要检测的图片';
        return;
      }

      try {
        uploadLoading.value = true;
        detectResult.detectStatus = 'detecting';
        detectResult.detectMsg = '正在进行AI图片检测，请稍候...';

        const formData = new FormData();
        formData.append('file', imageFile.value);

        const response = await request({
          url: '/api/ai/detect/image',
          method: 'post',
          data: formData,
          headers: { 'Content-Type': 'multipart/form-data' },
          timeout: 30000
        });

        const backendDetectData = response.data || response;
        detectResult.detectStatus = 'success';
        detectResult.isPass = backendDetectData.isPass;
        detectResult.violationType = backendDetectData.violationType || '';
        detectResult.violationScore = backendDetectData.violationScore || 0;
        detectResult.violationArea = backendDetectData.violationArea || [];

        detectResult.detectMsg = detectResult.isPass
            ? `检测合规，置信度：${detectResult.violationScore}/100`
            : `检测违规【${detectResult.violationType}】，置信度：${detectResult.violationScore}/100`;

      } catch (error) {
        console.error('检测失败：', error);
        detectResult.detectStatus = 'error';
        detectResult.detectMsg = error.message || '检测失败，请检查网络或后端服务！';
      } finally {
        uploadLoading.value = false;
      }
    };

    // 7. 历史记录逻辑（不变）
    const getDetectHistory = async () => {
      try {
        historyDialogVisible.value = true;
        historyLoading.value = true;
        const response = await request({
          url: '/api/ai/detect/history',
          method: 'get',
          timeout: 10000
        });
        detectHistoryList.value = response.data || [];
      } catch (error) {
        detectHistoryList.value = [];
        alert('获取历史记录失败：' + error.message);
      } finally {
        historyLoading.value = false;
      }
    };

    // 8. 工具方法
    const getResultCardClass = () => {
      switch (detectResult.detectStatus) {
        case 'detecting': return 'result-detecting';
        case 'success': return detectResult.isPass ? 'result-pass' : 'result-fail';
        case 'error': return 'result-error';
        default: return '';
      }
    };

    const getResultStatusText = () => {
      switch (detectResult.detectStatus) {
        case 'idle': return '未检测';
        case 'detecting': return '检测中';
        case 'success': return detectResult.isPass ? '检测合规' : '检测违规';
        case 'error': return '检测失败';
        default: return '未知状态';
      }
    };

    onUnmounted(() => {
      imagePreviewUrl.value = '';
    });

    return {
      imagePreviewUrl,
      uploadLoading,
      detectResult,
      fileInputRef,
      dragOver,
      historyDialogVisible,
      historyLoading,
      detectHistoryList,
      handleImageChange,
      handleDragOver,
      handleDragLeave,
      handleDrop,
      handleImageAIDetect,
      resetDetectState,
      getDetectHistory,
      getResultCardClass,
      getResultStatusText
    };
  }
};
</script>

<template>
  <div class="image-ai-detect-container">
    <div class="page-title">
      <h2>图片AI检测</h2>
      <p>支持点击/拖拽上传 | 格式：JPG/JPEG/PNG/WEBP | 最大：5MB</p>
    </div>

    <div class="detect-main-content">
      <!-- 上传预览区域：新增拖拽事件监听 -->
      <div
          class="image-upload-preview"
          @dragover.prevent="handleDragOver"
          @dragleave.prevent="handleDragLeave"
          @drop.prevent="handleDrop"
      >
        <div class="image-preview-box" v-if="imagePreviewUrl">
          <img :src="imagePreviewUrl" alt="预览图" class="preview-image" />
          <div
              class="violation-area-marker"
              v-for="(area, index) in detectResult.violationArea"
              :key="index"
              :style="{ left: `${area.x}%`, top: `${area.y}%`, width: `${area.width}%`, height: `${area.height}%` }"
              v-if="detectResult.detectStatus === 'success' && !detectResult.isPass"
          >
            <span class="violation-tag">{{ detectResult.violationType }}</span>
          </div>
        </div>

        <!-- 上传占位：根据拖拽状态切换样式 -->
        <div
            class="image-upload-placeholder"
            v-else
            :class="{ 'drag-over': dragOver }"
        >
          <svg class="upload-icon" viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg">
            <path fill="#c0c4cc" d="M864 256H736v-64c0-35.3-28.7-64-64-64H352c-35.3 0-64 28.7-64 64v64H160c-17.7 0-32 14.3-32 32v640c0 17.7 14.3 32 32 32h704c17.7 0 32-14.3 32-32V288c0-17.7-14.3-32-32-32zM352 208h320v48H352v-48zm464 664H208V352h240c17.7 0 32-14.3 32-32v-48h192v48c0 17.7 14.3 32 32 32h240v520zM512 486.4V736c0 4.4-3.6 8-8 8h-48c-4.4 0-8-3.6-8-8V486.4c0-18.7-11.4-35.5-28.3-42.3l-128-42.7c-16.2-5.4-34.2 2.3-40.2 18.3l-64 192c-2.2 6.7 .2 14.1 6 18.7s12.1 6 18.8 2.2l107.9-35.9c16.2-5.4 34.2 2.3 40.2 18.3l80 240c2.2 6.7 .2 14.1-6 18.7s-12.1 6-18.8 2.2l-128-42.7c-16.2-5.4-34.2 2.3-40.2 18.3l-64 192c-2.2 6.7 .2 14.1 6 18.7s12.1 6 18.8 2.2l224-74.7c18.7-6.2 30-23.6 30-42.3V486.4c0-18.7-11.4-35.5-28.3-42.3l-128-42.7c-16.2-5.4-34.2 2.3-40.2 18.3z"></path>
          </svg>
          <p>{{ dragOver ? '释放图片开始上传' : '点击上传或拖拽图片至此处' }}</p>
          <span class="tips">支持JPG/JPEG/PNG/WEBP格式，最大5MB</span>
        </div>

        <input
            ref="fileInputRef"
            type="file"
            accept="image/jpg,image/jpeg,image/png,image/webp"
            class="image-upload-input"
            @change="handleImageChange"
        />
      </div>

      <div class="detect-operation-result">
        <div class="operation-buttons">
          <button
              class="btn detect-btn"
              @click="handleImageAIDetect"
              :disabled="uploadLoading || !imagePreviewUrl"
          >
            <span v-if="!uploadLoading">开始AI检测</span>
            <span v-else>检测中...</span>
            <i class="loading-icon" v-if="uploadLoading"></i>
          </button>
          <button
              class="btn reset-btn"
              @click="resetDetectState"
              :disabled="uploadLoading"
          >
            重置
          </button>
          <button
              class="btn history-btn"
              @click="getDetectHistory"
              :disabled="uploadLoading"
          >
            查看历史记录
          </button>
        </div>

        <div class="detect-result-card" :class="getResultCardClass()">
          <div class="result-title">
            <h3>检测结果</h3>
            <span
                class="result-status-tag"
                :class="{
                'status-idle': detectResult.detectStatus === 'idle',
                'status-detecting': detectResult.detectStatus === 'detecting',
                'status-pass': detectResult.detectStatus === 'success' && detectResult.isPass,
                'status-fail': detectResult.detectStatus === 'success' && !detectResult.isPass,
                'status-error': detectResult.detectStatus === 'error'
              }"
            >
              {{ getResultStatusText() }}
            </span>
          </div>
          <div class="result-content">
            <p class="result-msg">{{ detectResult.detectMsg || '请上传图片并进行检测' }}</p>
            <div class="result-detail" v-if="detectResult.detectStatus === 'success'">
              <div class="detail-item">
                <label>合规状态：</label>
                <span :class="detectResult.isPass ? 'text-pass' : 'text-fail'">
                  {{ detectResult.isPass ? '合规' : '违规' }}
                </span>
              </div>
              <div class="detail-item" v-if="!detectResult.isPass">
                <label>违规类型：</label>
                <span class="text-fail">{{ detectResult.violationType || '未知违规类型' }}</span>
              </div>
              <div class="detail-item">
                <label>置信度分数：</label>
                <span>{{ detectResult.violationScore }}/100</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 历史记录弹窗 -->
    <div class="history-dialog-mask" v-if="historyDialogVisible" @click="historyDialogVisible = false">
      <div class="history-dialog" @click.stop>
        <div class="history-dialog-header">
          <h3>检测历史记录</h3>
          <button class="dialog-close-btn" @click="historyDialogVisible = false">×</button>
        </div>
        <div class="history-dialog-body">
          <div class="history-loading" v-if="historyLoading">正在加载历史记录...</div>
          <div class="history-empty" v-else-if="!detectHistoryList.length">暂无检测历史记录</div>
          <div class="history-list" v-else>
            <div class="history-item" v-for="(item, index) in detectHistoryList" :key="index">
              <div class="history-item-left">
                <p class="history-detect-time">{{ item.detectTime || '未知时间' }}</p>
                <p class="history-image-name">{{ item.fileName || '未命名图片' }}</p>
              </div>
              <div class="history-item-right">
                <span class="history-result-tag" :class="item.isPass ? 'tag-pass' : 'tag-fail'">
                  {{ item.isPass ? '合规' : '违规' }}
                </span>
                <p class="history-violation-type" v-if="!item.isPass">
                  违规类型：{{ item.violationType || '未知' }}
                </p>
              </div>
            </div>
          </div>
        </div>
        <div class="history-dialog-footer">
          <button class="btn dialog-confirm-btn" @click="historyDialogVisible = false">关闭</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.image-ai-detect-container {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 20px;
  box-sizing: border-box;
  font-family: "Helvetica Neue", Helvetica, Arial, sans-serif;
  color: #1a2b48;
  background-color: #ffffff;
  position: relative;
}

.page-title {
  text-align: center;
  margin-bottom: 40px;
}

.page-title h2 {
  font-size: 28px;
  font-weight: 600;
  margin-bottom: 8px;
}

.page-title p {
  font-size: 16px;
  color: #4e5d78;
}

.detect-main-content {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(500px, 1fr));
  gap: 40px;
  align-items: flex-start;
}

.image-upload-preview {
  position: relative;
  width: 100%;
}

.image-preview-box {
  width: 100%;
  height: 450px;
  border: 2px dashed #e6e9ed;
  border-radius: 16px;
  overflow: hidden;
  background-color: #fafbfc;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.preview-image {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
}

.violation-area-marker {
  position: absolute;
  border: 2px solid #f56c6c;
  background-color: rgba(245, 108, 108, 0.2);
  border-radius: 8px;
  z-index: 10;
}

.violation-tag {
  position: absolute;
  top: 0;
  left: 0;
  background-color: #f56c6c;
  color: #ffffff;
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 4px 0 4px 0;
}

.image-upload-placeholder {
  width: 100%;
  height: 450px;
  border: 2px dashed #e6e9ed;
  border-radius: 16px;
  background-color: #fafbfc;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
}
/* 拖拽悬浮样式 */
.image-upload-placeholder.drag-over {
  border-color: #409eff;
  background-color: #e6f7ff;
}

.image-upload-placeholder:hover {
  border-color: #409eff;
  background-color: #f0f7ff;
}

.upload-icon {
  width: 64px;
  height: 64px;
  margin-bottom: 16px;
  transition: all 0.3s ease;
}

.image-upload-placeholder:hover .upload-icon {
  fill: #409eff;
  transform: scale(1.1);
}

.image-upload-placeholder p {
  font-size: 16px;
  color: #4e5d78;
  margin-bottom: 8px;
}

.image-upload-placeholder .tips {
  font-size: 12px;
  color: #909399;
}

.image-upload-input {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  opacity: 0;
  cursor: pointer;
  z-index: 20;
}

.detect-operation-result {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.operation-buttons {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.btn {
  padding: 12px 32px;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.detect-btn {
  background-color: #409eff;
  color: #ffffff;
}

.detect-btn:disabled {
  background-color: #b3d8ff;
  cursor: not-allowed;
}

.detect-btn:not(:disabled):hover {
  background-color: #337ecc;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.reset-btn {
  background-color: #f0f2f5;
  color: #4e5d78;
}

.reset-btn:disabled {
  background-color: #fafbfc;
  color: #c0c4cc;
  cursor: not-allowed;
}

.reset-btn:not(:disabled):hover {
  background-color: #e6e9ed;
  color: #1a2b48;
}

.history-btn {
  background-color: #67c23a;
  color: #ffffff;
}

.history-btn:disabled {
  background-color: #b3e19d;
  cursor: not-allowed;
}

.history-btn:not(:disabled):hover {
  background-color: #529e2d;
  box-shadow: 0 4px 12px rgba(103, 194, 58, 0.3);
}

.loading-icon {
  width: 16px;
  height: 16px;
  border: 2px solid transparent;
  border-top: 2px solid #ffffff;
  border-radius: 50%;
  animation: loading 1s linear infinite;
}

@keyframes loading {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.detect-result-card {
  padding: 24px;
  border-radius: 16px;
  background-color: #fafbfc;
  border: 1px solid #e6e9ed;
  transition: all 0.3s ease;
}

.result-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e6e9ed;
}

.result-title h3 {
  font-size: 18px;
  font-weight: 600;
}

.result-status-tag {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.status-idle { background-color: #f0f2f5; color: #4e5d78; }
.status-detecting { background-color: #e6f7ff; color: #409eff; }
.status-pass { background-color: #f0fff4; color: #67c23a; }
.status-fail { background-color: #fff2f0; color: #f56c6c; }
.status-error { background-color: #fff2f0; color: #f56c6c; }

.result-content {
  font-size: 14px;
  color: #4e5d78;
}

.result-msg {
  margin-bottom: 16px;
  line-height: 1.6;
}

.result-detail {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.detail-item label {
  font-weight: 500;
  color: #1a2b48;
  width: 100px;
  text-align: right;
}

.text-pass { color: #67c23a; font-weight: 500; }
.text-fail { color: #f56c6c; font-weight: 500; }

@media (max-width: 768px) {
  .detect-main-content {
    grid-template-columns: 1fr;
    gap: 24px;
  }
  .image-preview-box, .image-upload-placeholder { height: 350px; }
  .operation-buttons { flex-direction: column; gap: 12px; }
  .btn { width: 100%; }
}

@media (max-width: 480px) {
  .image-ai-detect-container { padding: 20px 16px; }
  .page-title h2 { font-size: 24px; }
  .image-preview-box, .image-upload-placeholder { height: 280px; }
  .detect-result-card { padding: 16px; }
}
</style>

<style>
.image-ai-detect-container .detect-result-card.result-pass {
  border-color: #67c23a;
  background-color: #f0fff4;
}
.image-ai-detect-container .detect-result-card.result-fail {
  border-color: #f56c6c;
  background-color: #fff2f0;
}
.image-ai-detect-container .detect-result-card.result-error {
  border-color: #f56c6c;
  background-color: #fff2f0;
}
.image-ai-detect-container .detect-result-card.result-detecting {
  border-color: #409eff;
  background-color: #f0f7ff;
}

/* 历史记录弹窗样式 */
.history-dialog-mask {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.history-dialog {
  width: 90%;
  max-width: 600px;
  background-color: #ffffff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2);
}

.history-dialog-header {
  padding: 16px 24px;
  border-bottom: 1px solid #e6e9ed;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.history-dialog-header h3 {
  font-size: 18px;
  font-weight: 600;
  margin: 0;
}

.dialog-close-btn {
  background: none;
  border: none;
  font-size: 20px;
  color: #909399;
  cursor: pointer;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  transition: all 0.3s ease;
}

.dialog-close-btn:hover {
  background-color: #f0f2f5;
  color: #f56c6c;
}

.history-dialog-body {
  padding: 24px;
  max-height: 400px;
  overflow-y: auto;
}

.history-loading, .history-empty {
  text-align: center;
  font-size: 16px;
  padding: 40px 0;
}

.history-loading { color: #409eff; }
.history-empty { color: #909399; }

.history-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.history-item {
  padding: 16px;
  border-radius: 8px;
  background-color: #fafbfc;
  border: 1px solid #e6e9ed;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.history-item-left {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.history-detect-time {
  font-size: 12px;
  color: #909399;
  margin: 0;
}

.history-image-name {
  font-size: 14px;
  color: #1a2b48;
  margin: 0;
  font-weight: 500;
}

.history-item-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
}

.history-result-tag {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.tag-pass { background-color: #f0fff4; color: #67c23a; }
.tag-fail { background-color: #fff2f0; color: #f56c6c; }

.history-violation-type {
  font-size: 12px;
  color: #f56c6c;
  margin: 0;
}

.history-dialog-footer {
  padding: 16px 24px;
  border-top: 1px solid #e6e9ed;
  text-align: right;
}

.dialog-confirm-btn {
  background-color: #409eff;
  color: #ffffff;
  border: none;
  border-radius: 8px;
  padding: 8px 16px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.dialog-confirm-btn:hover {
  background-color: #337ecc;
}
</style>