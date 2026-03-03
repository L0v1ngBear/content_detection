<template>
  <div class="ai-image-detect-container sidebar-layout">
    <!-- 左侧功能侧边栏 -->
    <div class="sidebar">
      <div class="sidebar-header">
        <h3>AI图片检测</h3>
        <p>YOLO合规校验工具（批量版）</p>
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
          <div class="upload-icon-wrapper" v-if="imageFiles.length === 0">
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

          <!-- 批量图片预览 -->
          <div class="batch-preview" v-if="imageFiles.length > 0">
            <div class="preview-list">
              <div class="preview-item" v-for="(file, index) in imageFiles" :key="index">
                <div class="preview-img-wrapper">
                  <img :src="file.previewUrl" alt="预览" class="preview-img" @click="viewOriginalImage(file.previewUrl)"/>
                  <button class="close-preview" @click="removeFile(index)">×</button>
                  <div class="image-size-tip">{{ `${(file.file.size / 1024).toFixed(1)}KB` }}</div>
                  <!-- 单个文件检测状态 -->
                  <div class="file-status" :class="file.detectStatus">
                    <span v-if="file.detectStatus === 'submitting' || file.detectStatus === 'polling'">
                      <i class="loading-icon">◯</i> {{ file.detectMsg }}
                    </span>
                    <span v-if="file.detectStatus === 'success'">
                      {{ file.yoloResult.isPass ? '✅ 合规' : '⚠️ 违规' }}
                    </span>
                    <span v-if="file.detectStatus === 'error'">
                      ❌ {{ file.detectMsg }}
                    </span>
                  </div>
                </div>
                <p class="file-name">{{ file.file.name }}</p>
              </div>
            </div>
            <div class="batch-stats">
              <span>已选择 {{ imageFiles.length }} 张图片</span>
              <span v-if="completedCount > 0">已完成 {{ completedCount }}/{{ imageFiles.length }}</span>
            </div>
          </div>

          <!-- 上传按钮容器 -->
          <div class="upload-btn-container">
            <!-- 修复：文件输入框单独定位，避免按钮点击冒泡触发 -->
            <input
                ref="fileInputRef"
                type="file"
                accept="image/jpg,image/jpeg,image/png,image/webp"
                class="file-input"
                @change="handleImageChange"
                multiple
            style="position: absolute; opacity: 0; width: 0; height: 0; z-index: -1;"
            />
            <!-- 上传/添加图片按钮 -->
            <button class="upload-btn" @click="triggerFileInput" :disabled="isDetectingFlag || !isLogin || imageFiles.length >= maxFileCount">
              <span class="btn-icon">📤</span>
              <span class="btn-text">{{ imageFiles.length > 0 ? '添加更多图片' : '上传图片检测' }}</span>
            </button>
            <!-- 批量检测按钮 -->
            <button class="batch-detect-btn" @click="handleBatchDetectClick"
                    :disabled="!isLogin || imageFiles.length === 0 || isDetectingFlag || completedCount === imageFiles.length">
              <span class="btn-icon">🚀</span>
              <span class="btn-text">开始批量检测</span>
            </button>
            <!-- 清空按钮 -->
            <button class="clear-btn" @click="clearAllFiles" :disabled="isDetectingFlag">
              <span class="btn-icon">🗑️</span>
              <span class="btn-text">清空</span>
            </button>
          </div>

          <p class="tips">{{ dragOver ? '释放上传' : '支持拖拽/点击批量上传' }}</p>
          <p class="format-tips">支持 JPG/PNG/WEBP 格式 | 单张最大 5MB | 最多上传20张</p>
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
        <div class="empty-state initial-empty" v-else-if="imageFiles.length === 0">
          <div class="empty-illustration">
            <svg viewBox="0 0 24 24" width="120" height="120" fill="#f0f2f5">
              <path
                  d="M21 19V5c0-1.1-.9-2-2-2H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2zM8.5 13.5l2.5 3.01L14.5 12l4.5 6H5l3.5-4.5z"/>
            </svg>
          </div>
          <h3>批量上传图片开始YOLO检测</h3>
          <p>AI智能检测图片合规性，快速识别违规内容，支持批量处理</p>
        </div>

        <!-- 批量检测汇总结果 -->
        <div class="batch-result-state" v-else>
          <div class="batch-summary">
            <h3>批量检测进度</h3>
            <div class="progress-bar">
              <div class="progress-fill" :style="{ width: `${(completedCount / imageFiles.length) * 100}%` }"></div>
            </div>
            <p class="progress-text">{{ completedCount }}/{{ imageFiles.length }} 张图片已检测</p>

            <!-- 统计信息 -->
            <div class="stats-grid" v-if="completedCount > 0">
              <div class="stat-item">
                <span class="stat-label">合规</span>
                <span class="stat-value safe">{{ passCount }}</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">违规</span>
                <span class="stat-value high">{{ failCount }}</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">失败</span>
                <span class="stat-value error">{{ errorCount }}</span>
              </div>
            </div>
          </div>

          <!-- 详细结果列表 -->
          <div class="detailed-results">
            <h4>检测结果详情</h4>
            <div class="result-table">
              <div class="table-header">
                <div class="col-name">文件名</div>
                <div class="col-status">检测状态</div>
                <div class="col-result">检测结果</div>
                <div class="col-confidence">置信度</div>
              </div>
              <div class="table-body">
                <div class="table-row" v-for="(file, index) in imageFiles" :key="index">
                  <div class="col-name">{{ file.file.name }}</div>
                  <div class="col-status" :class="file.detectStatus">
                    <span v-if="file.detectStatus === 'idle'">未开始</span>
                    <span v-if="file.detectStatus === 'submitting'">提交中</span>
                    <span v-if="file.detectStatus === 'polling'">检测中</span>
                    <span v-if="file.detectStatus === 'success'" class="success">已完成</span>
                    <span v-if="file.detectStatus === 'error'" class="error">失败</span>
                  </div>
                  <div class="col-result">
                    <span v-if="file.detectStatus === 'success'" :class="file.yoloResult.isPass ? 'safe' : 'high'">
                      {{ file.yoloResult.isPass ? '合规' : file.yoloResult.violationType }}
                    </span>
                    <span v-else>-</span>
                  </div>
                  <div class="col-confidence">
                    <span v-if="file.detectStatus === 'success'">
                      {{ (file.yoloResult.confidence * 100).toFixed(2) }}%
                    </span>
                    <span v-else>-</span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 批量操作按钮 -->
          <div class="batch-actions">
            <button class="btn-primary" @click="handleBatchDetectClick"
                    :disabled="isDetectingFlag || completedCount === imageFiles.length">
              重新检测全部
            </button>
            <button class="btn-secondary" @click="clearAllFiles" :disabled="isDetectingFlag">
              清空重新上传
            </button>
            <button class="btn-export" @click="exportResults" :disabled="completedCount === 0">
              导出检测结果
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive, onUnmounted, onMounted, watch, computed } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import request from '../../utils/request';

export default {
  name: "AIImageDetectBatch",
  setup() {
    const router = useRouter();
    const route = useRoute();

    // 响应式变量 - 改为批量模式
    const imageFiles = ref([]); // 批量文件列表：[{ file, previewUrl, detectStatus, detectMsg, taskId, yoloResult, pollTimer, pollCount }]
    const fileInputRef = ref(null);
    const statusContainerRef = ref(null);
    const dragOver = ref(false);
    const isLogin = ref(true); // 模拟已登录
    const isDetectingFlag = ref(false);
    const maxPollTimes = 60; // 最大轮询次数（60秒）
    const maxFileCount = 20; // 最大上传文件数

    // 违规类型映射
    const violationTypeMap = {
      Normal: '正常',
      Adult: '色情',
      Violent: '暴力'
    };

    // 配置项
    const maxImageSize = 5 * 1024 * 1024;
    const allowImageTypes = ['image/jpg', 'image/jpeg', 'image/png', 'image/webp'];

    // 计算属性 - 批量统计
    const completedCount = computed(() => {
      return imageFiles.value.filter(file =>
          file.detectStatus === 'success' || file.detectStatus === 'error'
      ).length;
    });

    const passCount = computed(() => {
      return imageFiles.value.filter(file =>
          file.detectStatus === 'success' && file.yoloResult.isPass
      ).length;
    });

    const failCount = computed(() => {
      return imageFiles.value.filter(file =>
          file.detectStatus === 'success' && !file.yoloResult.isPass
      ).length;
    });

    const errorCount = computed(() => {
      return imageFiles.value.filter(file =>
          file.detectStatus === 'error'
      ).length;
    });

    // 登录校验
    const checkLoginStatus = () => {
      isLogin.value = true; // 实际项目中替换为真实登录校验
    };

    // 跳转登录
    const goToLogin = () => {
      router.push({
        path: '/login',
        query: { redirect: route.fullPath }
      });
    };

    // 跳转历史记录
    const goToHistoryPage = () => {
      router.push('/front/history');
    };

    // 更新批量检测状态
    const updateDetectingStatus = () => {
      isDetectingFlag.value = imageFiles.value.some(file =>
          ['submitting', 'polling'].includes(file.detectStatus)
      );
    };

    // 监听文件列表变化，更新检测状态
    watch([() => imageFiles.value], () => {
      updateDetectingStatus();
    }, { deep: true, immediate: true });

    // 生成图片预览
    const createPreview = (file) => {
      return URL.createObjectURL(file);
    };

    // 释放预览URL
    const revokePreviewUrl = (previewUrl) => {
      if (previewUrl) {
        try {
          URL.revokeObjectURL(previewUrl);
        } catch (e) {
          console.warn('释放预览URL失败:', e);
        }
      }
    };

    // 查看原图
    const viewOriginalImage = (previewUrl) => {
      if (previewUrl) {
        window.open(previewUrl, '_blank');
      }
    };

    // 移除单个文件
    const removeFile = (index) => {
      if (isDetectingFlag.value) {
        ElMessage.warning('检测中无法移除文件');
        return;
      }

      // 清理资源
      const fileItem = imageFiles.value[index];
      revokePreviewUrl(fileItem.previewUrl);
      if (fileItem.pollTimer) {
        clearInterval(fileItem.pollTimer);
      }

      // 移除文件
      imageFiles.value.splice(index, 1);
    };

    // 清空所有文件
    const clearAllFiles = () => {
      if (isDetectingFlag.value) {
        ElMessage.warning('检测中无法清空文件');
        return;
      }

      // 清理所有资源
      imageFiles.value.forEach(fileItem => {
        revokePreviewUrl(fileItem.previewUrl);
        if (fileItem.pollTimer) {
          clearInterval(fileItem.pollTimer);
        }
      });

      // 清空列表
      imageFiles.value = [];
    };

    // 触发文件选择（仅用于上传/添加图片）
    const triggerFileInput = () => {
      if (!isLogin.value) {
        ElMessage.warning('请先登录');
        goToLogin();
        return;
      }
      if (fileInputRef.value && !isDetectingFlag.value && imageFiles.value.length < maxFileCount) {
        fileInputRef.value.click();
      }
    };

    // 验证单个文件
    const validateImageFile = (file) => {
      if (!file) return { valid: false, message: '未选择文件' };

      // 格式校验
      if (!allowImageTypes.includes(file.type)) {
        return {
          valid: false,
          message: `文件 ${file.name}：仅支持 JPG/PNG/WEBP 格式！`
        };
      }

      // 大小校验
      if (file.size > maxImageSize) {
        return {
          valid: false,
          message: `文件 ${file.name}：大小超过 5MB 限制，当前：${(file.size / 1024 / 1024).toFixed(2)}MB`
        };
      }

      return { valid: true };
    };

    // 添加文件到列表
    const addFilesToList = (files) => {
      const newFiles = Array.from(files);

      // 数量校验
      if (imageFiles.value.length + newFiles.length > maxFileCount) {
        ElMessage.error(`最多只能上传${maxFileCount}张图片`);
        return;
      }

      // 逐个验证并添加
      newFiles.forEach(file => {
        const validation = validateImageFile(file);
        if (!validation.valid) {
          ElMessage.error(validation.message);
          return;
        }

        // 检查是否已存在相同文件
        const isDuplicate = imageFiles.value.some(item =>
            item.file.name === file.name && item.file.size === file.size
        );
        if (isDuplicate) {
          ElMessage.warning(`文件 ${file.name} 已存在`);
          return;
        }

        // 添加到列表
        imageFiles.value.push({
          file,
          previewUrl: createPreview(file),
          detectStatus: 'idle', // idle / submitting / polling / error / success
          detectMsg: '',
          taskId: '',
          pollTimer: null,
          pollCount: 0,
          yoloResult: {
            filename: file.name,
            detectTime: '',
            isPass: true,
            violationType: '',
            confidence: 0
          }
        });
      });
    };

    // 处理文件选择（仅上传图片，不触发检测）
    const handleImageChange = (e) => {
      const files = e.target.files;
      if (files && files.length > 0) {
        addFilesToList(files);
      }

      // 重置input值
      setTimeout(() => {
        if (fileInputRef.value) {
          fileInputRef.value.value = '';
        }
      }, 100);
    };

    // 拖拽上传
    const handleDragOver = (e) => {
      e.preventDefault();
      if (!isDetectingFlag.value && isLogin.value) {
        dragOver.value = true;
      }
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

      const files = e.dataTransfer.files;
      if (files && files.length > 0) {
        addFilesToList(files);
      }
    };

    // 停止单个文件的轮询
    const stopFilePolling = (fileItem, errorMsg = '') => {
      if (fileItem.pollTimer) {
        clearInterval(fileItem.pollTimer);
        fileItem.pollTimer = null;
      }
      fileItem.pollCount = 0;

      if (errorMsg) {
        fileItem.detectStatus = 'error';
        fileItem.detectMsg = errorMsg;
        updateDetectingStatus();
      }
    };

    // 轮询获取单个文件的检测结果
    const startFilePollResult = (fileItem, index) => {
      // 防止重复轮询
      stopFilePolling(fileItem);

      fileItem.pollCount = 0;
      fileItem.detectStatus = 'polling';
      fileItem.detectMsg = '等待YOLO检测...';

      // 每1秒轮询一次
      fileItem.pollTimer = setInterval(async () => {
        fileItem.pollCount++;

        // 轮询超时处理
        if (fileItem.pollCount >= maxPollTimes) {
          stopFilePolling(fileItem, '检测超时');
          ElMessage.error(`文件 ${fileItem.file.name} 检测超时`);
          return;
        }

        try {
          const res = await request({
            url: '/review/result',
            method: 'get',
            params: { taskId: fileItem.taskId },
            timeout: 5000
          });

          if (!res) {
            throw new Error('获取检测结果为空');
          }

          if (res.code !== 200) {
            throw new Error(res.msg || '获取检测结果失败');
          }

          const data = res.data;
          if (!data || typeof data.status !== 'number') {
            throw new Error('检测结果格式异常');
          }

          // 任务状态：0=排队中 1=检测中 2=完成 3=失败
          switch (data.status) {
            case 0:
              fileItem.detectMsg = '排队等待...';
              break;
            case 1:
              fileItem.detectMsg = '检测中...';
              break;
            case 2:
              // 检测完成
              stopFilePolling(fileItem);
              fileItem.detectStatus = 'success';

              const rawViolationType = data.violationType || 'Normal';
              const showViolationType = violationTypeMap[rawViolationType] || rawViolationType;
              const isPass = rawViolationType === 'Normal';

              fileItem.yoloResult = {
                filename: fileItem.file.name,
                detectTime: new Date().toLocaleString(),
                isPass: isPass,
                violationType: showViolationType,
                confidence: typeof data.confidence === 'number' ? data.confidence : 0
              };
              break;
            case 3:
              // 检测失败
              stopFilePolling(fileItem, data.msg || '检测失败');
              break;
            default:
              throw new Error(`未知的任务状态：${data.status}`);
          }
        } catch (err) {
          console.error(`轮询失败 [${fileItem.file.name}]：`, err);
          // 轮询失败超过3次则停止
          if (fileItem.pollCount % 3 === 0) {
            ElMessage.warning(`文件 ${fileItem.file.name} 轮询失败：${err.message}`);
          }
        }
      }, 1000);

      updateDetectingStatus();
    };

    // 提交单个文件检测
    const submitFileDetect = async (fileItem, index) => {
      if (!fileItem || fileItem.detectStatus !== 'idle') return;

      try {
        fileItem.detectStatus = 'submitting';
        fileItem.detectMsg = '上传中...';
        updateDetectingStatus();

        const formData = new FormData();
        formData.append('file', fileItem.file);

        // 调用上传接口（仅提交检测，不再触发文件选择）
        const res = await request({
          url: '/review/picture',
          method: 'post',
          data: formData,
          headers: { 'Content-Type': 'multipart/form-data' },
          timeout: 30000
        });

        if (!res) {
          throw new Error('上传响应为空');
        }

        if (res?.code === 200) {
          if (!res.data || typeof res.data !== 'string') {
            throw new Error('获取的任务ID无效');
          }

          fileItem.taskId = res.data;
          // 启动轮询
          startFilePollResult(fileItem, index);
        } else {
          throw new Error(res?.msg || '图片提交失败');
        }
      } catch (err) {
        console.error(`提交失败 [${fileItem.file.name}]：`, err);
        fileItem.detectStatus = 'error';

        if (err.message.includes('timeout')) {
          fileItem.detectMsg = '上传超时';
        } else {
          fileItem.detectMsg = err.message || '提交失败';
        }
      } finally {
        updateDetectingStatus();
      }
    };

    // 批量检测核心逻辑（独立方法，避免和上传混淆）
    const startBatchDetect = async () => {
      if (!isLogin.value) {
        ElMessage.warning('请先登录');
        goToLogin();
        return;
      }

      if (imageFiles.value.length === 0) {
        ElMessage.warning('请先上传图片');
        return;
      }

      // 重置未完成的文件状态
      imageFiles.value.forEach(fileItem => {
        if (fileItem.detectStatus !== 'success' && fileItem.detectStatus !== 'error') {
          fileItem.detectStatus = 'idle';
          fileItem.detectMsg = '';
          fileItem.taskId = '';
          if (fileItem.pollTimer) {
            clearInterval(fileItem.pollTimer);
            fileItem.pollTimer = null;
          }
          fileItem.pollCount = 0;
        }
      });

      ElMessage.info(`开始批量检测 ${imageFiles.value.length} 张图片`);

      // 串行提交检测（避免同时提交过多请求）
      for (let i = 0; i < imageFiles.value.length; i++) {
        const fileItem = imageFiles.value[i];
        if (fileItem.detectStatus === 'idle') {
          await submitFileDetect(fileItem, i);
          // 间隔500ms提交下一个，避免后端压力过大
          await new Promise(resolve => setTimeout(resolve, 500));
        }
      }
    };

    // 批量检测按钮点击处理（独立方法，防止事件冒泡）
    const handleBatchDetectClick = async (e) => {
      // 阻止事件冒泡，防止触发文件选择
      e.stopPropagation();
      await startBatchDetect();
    };

    // 导出检测结果
    const exportResults = () => {
      if (completedCount.value === 0) {
        ElMessage.warning('暂无检测结果可导出');
        return;
      }

      // 构建CSV内容
      let csvContent = "文件名,文件大小(KB),检测时间,检测结果,违规类型,置信度(%)\n";

      imageFiles.value.forEach(fileItem => {
        if (fileItem.detectStatus === 'success') {
          const sizeKB = (fileItem.file.size / 1024).toFixed(1);
          const confidence = (fileItem.yoloResult.confidence * 100).toFixed(2);
          const result = fileItem.yoloResult.isPass ? '合规' : '违规';

          csvContent += `${fileItem.file.name},${sizeKB},${fileItem.yoloResult.detectTime},${result},${fileItem.yoloResult.violationType},${confidence}\n`;
        }
      });

      // 创建并下载CSV文件
      const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
      const url = URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.setAttribute('href', url);
      link.setAttribute('download', `AI图片检测结果_${new Date().getTime()}.csv`);
      link.style.visibility = 'hidden';
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      URL.revokeObjectURL(url);

      ElMessage.success('检测结果已导出');
    };

    // 生命周期
    onMounted(() => {
      checkLoginStatus();
      updateDetectingStatus();
    });

    onUnmounted(() => {
      // 清理所有资源
      imageFiles.value.forEach(fileItem => {
        revokePreviewUrl(fileItem.previewUrl);
        if (fileItem.pollTimer) {
          clearInterval(fileItem.pollTimer);
        }
      });
    });

    return {
      fileInputRef,
      statusContainerRef,
      dragOver,
      imageFiles,
      isDetectingFlag,
      isLogin,
      maxFileCount,
      completedCount,
      passCount,
      failCount,
      errorCount,
      triggerFileInput,
      handleImageChange,
      handleDragOver,
      handleDragLeave,
      handleDrop,
      removeFile,
      clearAllFiles,
      goToLogin,
      goToHistoryPage,
      viewOriginalImage,
      handleBatchDetectClick, // 暴露独立的批量检测点击方法
      exportResults
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
  overflow: hidden;
}

/* 侧边栏样式 */
.sidebar {
  width: 400px;
  background: #fff;
  border-right: 1px solid #e2e8f0;
  display: flex;
  flex-direction: column;
  padding: 32px 24px;
  overflow-y: auto;
}

.sidebar-header {
  text-align: center;
  margin-bottom: 24px;
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
}

/* 上传区域 */
.upload-area {
  border: 2px dashed #e2e8f0;
  border-radius: 20px;
  padding: 24px;
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

/* 批量预览样式 */
.batch-preview {
  margin-bottom: 24px;
  max-height: 400px;
  overflow-y: auto;
}

.preview-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  justify-content: center;
  margin-bottom: 16px;
}

.preview-item {
  width: 100px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.preview-img-wrapper {
  position: relative;
  width: 90px;
  height: 90px;
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 4px;
}

.preview-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.close-preview {
  position: absolute;
  top: 2px;
  right: 2px;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.6);
  color: #fff;
  border: none;
  font-size: 12px;
  cursor: pointer;
  z-index: 10;
}

.image-size-tip {
  position: absolute;
  bottom: 2px;
  left: 2px;
  font-size: 10px;
  color: #fff;
  background: rgba(0, 0, 0, 0.4);
  padding: 1px 4px;
  border-radius: 2px;
}

.file-status {
  position: absolute;
  bottom: 20px;
  left: 0;
  width: 100%;
  font-size: 10px;
  color: #fff;
  background: rgba(0, 0, 0, 0.6);
  padding: 2px;
  text-align: center;
}

.file-name {
  font-size: 12px;
  color: #64748b;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  width: 100%;
}

.batch-stats {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: #64748b;
  padding: 8px;
  background: #f8fafc;
  border-radius: 8px;
}

/* 上传按钮容器 */
.upload-btn-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 16px;
  position: relative;
}

/* 隐藏的文件输入框，避免布局干扰 */
.file-input {
  position: absolute !important;
  opacity: 0 !important;
  width: 0 !important;
  height: 0 !important;
  z-index: -1 !important;
}

.upload-btn, .batch-detect-btn, .clear-btn {
  padding: 12px 20px;
  border-radius: 12px;
  font-size: 14px;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  border: none;
}

.upload-btn {
  background: linear-gradient(135deg, #4f46e5, #7c3aed);
  color: #fff;
}

.batch-detect-btn {
  background: linear-gradient(135deg, #2563eb, #3b82f6);
  color: #fff;
}

.clear-btn {
  background: #f8fafc;
  color: #64748b;
  border: 1px solid #e2e8f0;
}

.upload-btn:disabled, .batch-detect-btn:disabled, .clear-btn:disabled {
  background: #cbd5e1;
  color: #94a3b8;
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
  overflow-y: auto;
}

.status-container {
  width: 100%;
  max-width: 800px;
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

/* 批量结果样式 */
.batch-result-state {
  background: #fff;
  border-radius: 20px;
  padding: 40px;
  width: 100%;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  animation: fadeIn 0.3s;
}

.batch-summary {
  text-align: center;
  margin-bottom: 32px;
}

.batch-summary h3 {
  font-size: 24px;
  color: #1e293b;
  margin-bottom: 16px;
}

.progress-bar {
  height: 12px;
  background: #f1f5f9;
  border-radius: 6px;
  overflow: hidden;
  margin-bottom: 8px;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(135deg, #4f46e5, #7c3aed);
  transition: width 0.3s ease;
}

.progress-text {
  font-size: 14px;
  color: #64748b;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-top: 24px;
}

.stat-item {
  background: #f8fafc;
  padding: 16px;
  border-radius: 12px;
}

.stat-label {
  font-size: 14px;
  color: #64748b;
  margin-bottom: 4px;
  display: block;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
}

.stat-value.safe {
  color: #059669;
}

.stat-value.high {
  color: #dc2626;
}

.stat-value.error {
  color: #f59e0b;
}

.detailed-results {
  margin-bottom: 32px;
}

.detailed-results h4 {
  font-size: 18px;
  color: #1e293b;
  margin-bottom: 16px;
}

.result-table {
  width: 100%;
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid #e2e8f0;
}

.table-header {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr 1fr;
  background: #f8fafc;
  padding: 12px 16px;
  font-weight: 600;
  color: #334155;
  border-bottom: 1px solid #e2e8f0;
}

.table-body {
  max-height: 400px;
  overflow-y: auto;
}

.table-row {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr 1fr;
  padding: 12px 16px;
  border-bottom: 1px solid #f1f5f9;
  font-size: 14px;
}

.table-row:last-child {
  border-bottom: none;
}

.col-status .success {
  color: #059669;
}

.col-status .error {
  color: #dc2626;
}

.col-result .safe {
  color: #059669;
}

.col-result .high {
  color: #dc2626;
}

.batch-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
  margin-top: 24px;
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

.btn-export {
  background: #2563eb;
  color: #fff;
  border: none;
  padding: 12px 24px;
  border-radius: 10px;
  font-size: 15px;
  cursor: pointer;
}

.btn-primary:disabled, .btn-secondary:disabled, .btn-export:disabled {
  background: #cbd5e1;
  cursor: not-allowed;
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
    height: auto;
  }

  .sidebar {
    width: 100%;
    height: auto;
    border-right: none;
    border-bottom: 1px solid #e2e8f0;
    max-height: 50vh;
  }

  .main-content {
    padding: 20px;
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }

  .table-header, .table-row {
    grid-template-columns: 1fr 1fr;
  }

  .table-header .col-confidence, .table-row .col-confidence,
  .table-header .col-status, .table-row .col-status {
    display: none;
  }

  .batch-actions {
    flex-direction: column;
  }
}
</style>