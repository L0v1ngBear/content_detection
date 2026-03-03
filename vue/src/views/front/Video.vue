<template>
  <div class="video-upload-container sidebar-layout">
    <div class="sidebar">
      <div class="sidebar-header">
        <h3>AI视频检测</h3>
        <p>合规校验工具</p> <!-- 移除断点续传描述 -->
      </div>
      <div class="sidebar-content">
        <div
            class="upload-area"
            @click="triggerFileInput"
            @dragover.prevent="handleDragEnter"
            @dragleave.prevent="handleDragLeave"
            @drop.prevent="handleDrop"
            :class="{ 'drag-over': isDragging, 'has-file': videoFile }"
        >
          <div class="upload-icon-wrapper" v-if="!videoFile">
            <svg class="upload-icon" viewBox="0 0 24 24" width="60" height="60">
              <defs>
                <linearGradient id="videoUploadGradient" x1="0%" y1="0%" x2="100%" y2="100%">
                  <stop offset="0%" stop-color="#4f46e5"/>
                  <stop offset="100%" stop-color="#7c3aed"/>
                </linearGradient>
              </defs>
              <path fill="url(#videoUploadGradient)"
                    d="M8 4v12l6-6-6-6zm10 2h-2v8h2V6zm-2-4c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H6c-1.1 0-2-.9-2-2V4c0-1.1.9-2 2-2h10zm0 2H6v12h10V4z"/>
            </svg>
          </div>

          <div class="video-preview-wrapper" v-if="videoFile">
            <div class="preview-content">
              <video
                  ref="videoPlayer"
                  :src="videoUrl"
                  controls
                  class="preview-video"
              ></video>
              <div class="video-info">
                <p class="file-name">{{ videoFile.name }}</p>
                <p class="file-size">{{ (videoFile.size / 1024 / 1024).toFixed(2) }}MB</p>
              </div>
              <!-- 优化后的进度条 -->
              <div class="upload-progress" v-if="showProgress">
                <div class="progress-bg">
                  <div class="progress-bar" :style="{ width: progressPercent + '%' }">
                    <div class="progress-glow"></div>
                  </div>
                </div>
                <span class="progress-text">{{ progressText }}</span>
              </div>
              <div class="upload-controls" v-if="showProgress">
                <button
                    class="control-btn"
                    :class="{ pause: uploadStatus === 'uploading' }"
                    @click.stop="toggleUpload"
                >
                  {{ uploadStatus === 'uploading' ? '暂停上传' : '继续上传' }}
                </button>
                <button class="control-btn cancel-btn" @click.stop="cancelUpload">
                  取消上传
                </button>
              </div>
            </div>
          </div>

          <div class="upload-btn-container" :class="{ 'hidden': videoFile }">
            <input
                ref="fileInput"
                type="file"
                accept="video/mp4,video/avi,video/mov"
                @change="handleFileChange"
                class="file-input"
            />
            <button class="upload-btn" @click.stop="triggerFileInput" :disabled="isDetectingFlag || !isLogin">
              <span class="btn-icon">📤</span>
              <span class="btn-text">上传视频检测</span>
            </button>
          </div>

          <p class="tips">{{ isDragging ? '释放上传' : '支持拖拽/点击上传视频' }}</p>
          <!-- 移除断点续传提示 -->
          <p class="format-tips">支持 MP4/AVI/MOV 格式 | 最大 500MB</p>
          <p class="login-tips" v-if="!isLogin">请先登录后再使用检测功能</p>
        </div>
      </div>
    </div>

    <div class="main-content">
      <div class="status-container" ref="statusContainerRef">
        <div class="empty-state login-empty" v-if="!isLogin">
          <svg class="empty-icon" viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg">
            <path fill="#c0c4cc"
                  d="M512 64C264.6 64 64 264.6 64 512s200.6 448 448 448 448-200.6 448-448S759.4 64 512 64zm0 820c-205.4 0-372-166.6-372-372s166.6-372 372-372 372 166.6 372 372-166.6 372-372 372z"/>
            <path fill="#c0c4cc"
                  d="M512 336c-48.7 0-88 39.3-88 88s39.3 88 88 88 88-39.3 88-88-39.3-88-88-88zm0 136c-26.5 0-48-21.5-48-48s21.5-48 48-48 48 21.5 48 48-21.5 48-48 48zm184 208H328c-17.7 0-32-14.3-32-32v-32c0-17.7 14.3-32 32-32h368c17.7 0 32 14.3 32 32v32c0 17.7-14.3 32-32 32z"/>
          </svg>
          <p>请先登录账号，才能使用AI视频检测功能</p>
          <button class="login-btn" @click="goToLogin">立即登录</button>
        </div>

        <div class="empty-state initial-empty" v-else-if="!videoFile">
          <div class="empty-illustration">
            <svg viewBox="0 0 24 24" width="120" height="120" fill="#f0f2f5">
              <path
                  d="M21 3H3c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h18c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm0 16H3V5h18v14zM8 15c0-1.66 1.34-3 3-3 .35 0 .69.07 1 .18V6h5v2h-3v7.03c-.02 1.64-1.35 2.97-3 2.97-1.66 0-3-1.34-3-3z"/>
            </svg>
          </div>
          <h3>上传视频开始AI合规检测</h3>
          <p>智能检测视频违规内容，快速获取审核结果</p> <!-- 移除断点续传描述 -->
        </div>

        <div class="audit-result-state" v-else-if="auditResult">
          <div class="result-card">
            <div class="result-header">
              <h3>视频审核结果</h3>
              <div class="result-tag" :class="auditResult.isPass ? 'tag-pass' : 'tag-fail'">
                {{ auditResult.isPass ? '视频审核通过' : '视频审核不通过' }}
              </div>
            </div>

            <div class="video-info-summary">
              <div class="info-item">
                <span class="label">文件名</span>
                <span class="value">{{ videoFile.name }}</span>
              </div>
              <div class="info-item">
                <span class="label">文件大小</span>
                <span class="value">{{ (videoFile.size / 1024 / 1024).toFixed(2) }}MB</span>
              </div>
              <div class="info-item">
                <span class="label">检测时间</span>
                <span class="value">{{ auditResult.detectTime }}</span>
              </div>
              <!-- 新增：展示违规类型（即使通过也显示） -->
              <div class="info-item">
                <span class="label">违规类型</span>
                <span class="value" :class="{ 'text-danger': !auditResult.isPass }">
                  {{ auditResult.violationType }}
                </span>
              </div>
              <!-- 新增：展示置信度 -->
              <div class="info-item">
                <span class="label">置信度</span>
                <span class="value" :class="{ 'text-danger': !auditResult.isPass }">
                  {{ (auditResult.confidence * 100).toFixed(2) }}%
                </span>
              </div>
            </div>

            <!-- 优化违规详情展示：仅在不通过时显示，且内容更清晰 -->
            <div class="violation-details" v-if="!auditResult.isPass">
              <h4>违规详情</h4>
              <div class="details-grid">
                <div class="detail-item">
                  <span class="detail-label">违规类型</span>
                  <span class="detail-value high">{{ auditResult.violationType }}</span>
                </div>
                <div class="detail-item">
                  <span class="detail-label">置信度</span>
                  <span class="detail-value">{{ (auditResult.confidence * 100).toFixed(2) }}%</span>
                </div>
                <!-- 可选：添加违规描述 -->
                <div class="detail-item" v-if="auditResult.violationDesc">
                  <span class="detail-label">违规描述</span>
                  <span class="detail-value">{{ auditResult.violationDesc }}</span>
                </div>
              </div>
            </div>

            <div class="result-actions">
              <button class="btn-primary" @click.stop="reUploadVideo" :disabled="isDetectingFlag">
                重新检测
              </button>
              <button class="btn-secondary" @click.stop="clearVideo" :disabled="isDetectingFlag">
                上传新视频
              </button>
              <button class="btn-export" @click.stop="exportResult" :disabled="!auditResult">
                导出检测报告
              </button>
            </div>
          </div>
        </div>

        <div class="empty-state processing-state" v-else>
          <div class="processing-icon">
            <svg class="loading-icon" viewBox="0 0 24 24" width="80" height="80">
              <circle cx="12" cy="12" r="10" fill="none" stroke="url(#videoUploadGradient)" stroke-width="2"
                      stroke-dasharray="50,100" transform="rotate(0 12 12)">
                <animateTransform attributeName="transform" type="rotate" from="0 12 12" to="360 12 12" dur="1.5s"
                                  repeatCount="indefinite"/>
              </circle>
            </svg>
            <defs>
              <linearGradient id="videoUploadGradient" x1="0%" y1="0%" x2="100%" y2="100%">
                <stop offset="0%" stop-color="#4f46e5"/>
                <stop offset="100%" stop-color="#7c3aed"/>
              </linearGradient>
            </defs>
          </div>
          <h3>{{ progressText }}</h3>
          <!-- 移除断点续传提示 -->
          <p>{{ uploadStatus === 'uploading' ? '请勿刷新页面，上传中...' : '' }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import {ref, reactive, onUnmounted, onMounted, watch} from 'vue';
import request from '../../utils/request';
import SparkMD5 from 'spark-md5';
import {ElMessage} from 'element-plus';

export default {
  setup() {
    // 响应式变量
    const fileInput = ref(null);
    const videoPlayer = ref(null);
    const statusContainerRef = ref(null);
    const isDragging = ref(false);
    const videoUrl = ref('');
    const videoFile = ref(null);
    const showProgress = ref(false);
    const progressPercent = ref(0);
    const progressText = ref('');
    const auditResult = ref(null);
    const isLogin = ref(true);
    const isDetectingFlag = ref(false);
    let pollTimer = null;

    // 分片上传相关变量
    const chunkSize = ref(5 * 1024 * 1024);
    const fileHash = ref('');
    const chunks = ref([]);
    const uploadedChunks = ref([]);
    const currentChunkIndex = ref(0);
    const uploadStatus = ref('');
    const uploadAbortController = ref(null);

    // 配置项
    const maxVideoSize = 500 * 1024 * 1024;
    const allowVideoTypes = ['video/mp4', 'video/avi', 'video/mov'];
    const maxPollTimes = 60;
    const pollInterval = 1000;

    // 登录校验
    const checkLoginStatus = () => {
      isLogin.value = true;
    };

    // 跳转登录
    const goToLogin = () => {
      ElMessage.info('跳转到登录页面');
    };

    // 更新检测状态
    const updateDetectingStatus = () => {
      isDetectingFlag.value = uploadStatus.value === 'uploading' ||
          uploadStatus.value === 'polling';
    };

    // 监听上传状态变化
    watch([uploadStatus], () => {
      updateDetectingStatus();
    }, {deep: true, immediate: true});

    // 生成文件Hash
    const generateFileHash = (file) => {
      return new Promise((resolve) => {
        const spark = new SparkMD5.ArrayBuffer();
        const fileReader = new FileReader();
        const chunkSize = 2 * 1024 * 1024;
        const chunks = Math.ceil(file.size / chunkSize);
        let currentChunk = 0;

        fileReader.onload = (e) => {
          spark.append(e.target.result);
          currentChunk++;
          progressText.value = `正在计算文件哈希：${Math.floor((currentChunk / chunks) * 100)}%`;

          if (currentChunk < chunks) {
            loadNextChunk();
          } else {
            const hash = spark.end();
            fileHash.value = hash;
            resolve(hash);
          }
        };

        const loadNextChunk = () => {
          const start = currentChunk * chunkSize;
          const end = Math.min(start + chunkSize, file.size);
          fileReader.readAsArrayBuffer(file.slice(start, end));
        };

        loadNextChunk();
      });
    };

    // 分割文件为分片
    const splitFileIntoChunks = (file) => {
      const chunkList = [];
      let index = 0;
      while (index * chunkSize.value < file.size) {
        const start = index * chunkSize.value;
        const end = Math.min(start + chunkSize.value, file.size);
        chunkList.push({
          file: file.slice(start, end),
          index,
          size: end - start
        });
        index++;
      }
      chunks.value = chunkList;
      return chunkList;
    };

    // 查询已上传的分片
    const checkUploadedChunks = async (fileHash, fileName) => {
      try {
        const response = await request.get('/review/video/check', {
          params: {
            fileHash,
            fileName
          }
        });
        uploadedChunks.value = response.data.uploadedChunks || [];
        progressText.value = `已上传 ${uploadedChunks.value.length}/${chunks.value.length} 分片`;
      } catch (error) {
        console.error('查询已上传分片失败：', error);
        uploadedChunks.value = [];
      }
    };

    // 上传单个分片
    const uploadChunk = async (chunk, fileHash, fileName) => {
      if (uploadStatus.value === 'paused' || uploadStatus.value === 'canceled') return;

      try {
        uploadAbortController.value = new AbortController();
        const formData = new FormData();
        formData.append('file', chunk.file);
        formData.append('fileHash', fileHash);
        formData.append('fileName', fileName);
        formData.append('chunkIndex', chunk.index);
        formData.append('totalChunks', chunks.value.length);

        const response = await request({
          url: '/review/chunk',
          method: 'post',
          data: formData,
          headers: {'Content-Type': 'multipart/form-data'},
          signal: uploadAbortController.value.signal,
          timeout: 30000
        });

        if (response.code === 200) {
          uploadedChunks.value.push(chunk.index);
          currentChunkIndex.value++;

          progressPercent.value = Math.floor((uploadedChunks.value.length / chunks.value.length) * 100);
          progressText.value = `上传中：${progressPercent.value}% (${uploadedChunks.value.length}/${chunks.value.length})`;

          if (currentChunkIndex.value < chunks.value.length) {
            await uploadChunk(chunks.value[currentChunkIndex.value], fileHash, fileName);
          } else {
            await mergeChunks(fileHash, fileName);
          }
        } else {
          throw new Error(`分片 ${chunk.index} 上传失败：${response.data.msg}`);
        }
      } catch (error) {
        if (error.name !== 'AbortError') {
          console.error(`分片 ${chunk.index} 上传失败：`, error);
          if (chunk.retryCount === undefined) chunk.retryCount = 0;
          if (chunk.retryCount < 3) {
            chunk.retryCount++;
            progressText.value = `分片 ${chunk.index} 上传失败，正在重试(${chunk.retryCount}/3)...`;
            setTimeout(() => {
              uploadChunk(chunk, fileHash, fileName);
            }, 2000);
          } else {
            throw new Error(`分片 ${chunk.index} 上传失败，重试次数已用尽`);
          }
        }
      }
    };

    // 合并分片
    const mergeChunks = async (fileHash, fileName) => {
      try {
        progressText.value = '所有分片上传完成，正在合并文件...';

        const response = await request.post('/review/merge', {
          fileHash,
          fileName,
          totalChunks: chunks.value.length
        });

        if (response.code === 200) {
          progressText.value = '文件合并成功，等待审核...';
          const taskId = response.data.taskId;
          startPollAuditResult(taskId);
        } else {
          throw new Error('文件合并失败：' + response.data.msg);
        }
      } catch (error) {
        ElMessage.error('文件合并失败：' + error.message);
        showProgress.value = false;
        uploadStatus.value = 'failed';
        updateDetectingStatus();
      }
    };

    // 开始分片上传
    const startChunkUpload = async (file) => {
      try {
        showProgress.value = true;
        progressPercent.value = 0;
        progressText.value = '正在准备上传...';
        uploadStatus.value = 'uploading';
        updateDetectingStatus();

        await generateFileHash(file);
        splitFileIntoChunks(file);
        await checkUploadedChunks(fileHash.value, file.name);

        currentChunkIndex.value = 0;
        while (currentChunkIndex.value < chunks.value.length) {
          if (!uploadedChunks.value.includes(currentChunkIndex.value)) {
            break;
          }
          currentChunkIndex.value++;
        }

        if (currentChunkIndex.value < chunks.value.length) {
          await uploadChunk(chunks.value[currentChunkIndex.value], fileHash.value, file.name);
        } else {
          await mergeChunks(fileHash.value, file.name);
        }
      } catch (error) {
        ElMessage.error('上传失败：' + error.message);
        showProgress.value = false;
        uploadStatus.value = 'failed';
        updateDetectingStatus();
      }
    };

    // 暂停/继续上传
    const toggleUpload = () => {
      if (uploadStatus.value === 'uploading') {
        uploadStatus.value = 'paused';
        progressText.value = `上传已暂停 (${progressPercent.value}%)`;
        if (uploadAbortController.value) {
          uploadAbortController.value.abort();
        }
      } else if (uploadStatus.value === 'paused') {
        uploadStatus.value = 'uploading';
        progressText.value = `继续上传：${progressPercent.value}%`;
        uploadChunk(chunks.value[currentChunkIndex.value], fileHash.value, videoFile.value.name);
      }
      updateDetectingStatus();
    };

    // 取消上传
    const cancelUpload = () => {
      if (uploadAbortController.value) {
        uploadAbortController.value.abort();
      }

      if (pollTimer) {
        clearInterval(pollTimer);
        pollTimer = null;
      }

      uploadStatus.value = 'canceled';
      progressText.value = '上传已取消';
      showProgress.value = false;
      progressPercent.value = 0;
      uploadedChunks.value = [];
      currentChunkIndex.value = 0;
      fileHash.value = '';
      chunks.value = [];
      uploadStatus.value = '';

      ElMessage.info('上传已取消');
      updateDetectingStatus();
    };

    // 处理文件选择
    const handleFileChange = (e) => {
      const file = e.target.files[0];
      if (!file) return;

      clearVideo();

      if (!allowVideoTypes.includes(file.type)) {
        ElMessage.error('仅支持MP4/AVI/MOV格式的视频');
        return;
      }

      if (file.size > maxVideoSize) {
        ElMessage.error('视频大小不能超过500MB');
        return;
      }

      videoFile.value = file;
      videoUrl.value = URL.createObjectURL(file);
      startChunkUpload(file);
    };

    // 轮询逻辑
    const startPollAuditResult = (taskId) => {
      if (pollTimer) {
        clearInterval(pollTimer);
      }

      uploadStatus.value = 'polling';
      updateDetectingStatus();
      let pollCount = 0;

      pollTimer = setInterval(async () => {
        pollCount++;

        if (pollCount >= maxPollTimes) {
          clearInterval(pollTimer);
          pollTimer = null;
          progressText.value = '审核结果获取超时';
          uploadStatus.value = 'failed';
          updateDetectingStatus();
          ElMessage.error('检测超时，请重新上传');
          return;
        }

        try {
          const res = await request({
            url: '/review/result',
            method: 'get',
            params: {taskId: taskId},
            timeout: 5000
          });

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
              progressText.value = '排队等待检测...';
              break;
            case 1:
              progressText.value = '视频检测中...';
              break;
            case 2:
              // 检测完成
              clearInterval(pollTimer);
              pollTimer = null;
              uploadStatus.value = '';
              updateDetectingStatus();

              // 格式化检测结果
              const rawViolationType = data.violationType || 'Normal';
              // 映射违规类型为中文展示
              const violationTypeMap = {
                'Normal': '无违规',
                'Porn': '色情内容',
                'Violence': '暴力内容',
                'Politics': '政治敏感',
                'Advertising': '广告内容',
                'Other': '其他违规'
              };

              const isPass = rawViolationType === 'Normal';
              auditResult.value = {
                filename: videoFile.value.name,
                detectTime: new Date().toLocaleString(),
                isPass: isPass,
                // 使用映射后的中文展示
                violationType: violationTypeMap[rawViolationType] || rawViolationType,
                confidence: typeof data.confidence === 'number' ? data.confidence : 0,
                violationScore: (data.confidence || 0) * 100,
                // 可选：添加违规描述
                violationDesc: data.violationDesc || ''
              };

              showProgress.value = false;
              progressText.value = '';
              ElMessage.success(isPass ? '视频审核通过' : `视频审核不通过：${auditResult.value.violationType}`);
              break;
            case 3:
              // 检测失败
              clearInterval(pollTimer);
              pollTimer = null;
              progressText.value = data.msg || '检测失败';
              uploadStatus.value = 'failed';
              updateDetectingStatus();
              ElMessage.error(`视频检测失败：${data.msg || '未知错误'}`);
              break;
            default:
              throw new Error(`未知的任务状态：${data.status}`);
          }
        } catch (err) {
          console.error('轮询失败：', err);
          if (pollCount % 3 === 0) {
            ElMessage.warning(`轮询失败：${err.message}，将继续尝试`);
          }
        }
      }, pollInterval);
    };

    // 触发文件选择
    const triggerFileInput = () => {
      if (!isLogin.value) {
        ElMessage.warning('请先登录');
        goToLogin();
        return;
      }
      if (fileInput.value && !isDetectingFlag.value) {
        fileInput.value.click();
      }
    };

    // 拖拽事件
    const handleDragEnter = (e) => {
      e.preventDefault();
      if (!isDetectingFlag.value && isLogin.value) {
        isDragging.value = true;
      }
    };

    const handleDragLeave = (e) => {
      e.preventDefault();
      isDragging.value = false;
    };

    const handleDrop = (e) => {
      e.preventDefault();
      isDragging.value = false;

      if (!isLogin.value) {
        ElMessage.warning('请先登录');
        goToLogin();
        return;
      }

      if (isDetectingFlag.value) return;

      const file = e.dataTransfer.files[0];
      if (file) {
        const dataTransfer = new DataTransfer();
        dataTransfer.items.add(file);
        fileInput.value.files = dataTransfer.files;
        handleFileChange({target: fileInput.value});
      }
    };

    // 重新上传视频
    const reUploadVideo = () => {
      if (pollTimer) {
        clearInterval(pollTimer);
        pollTimer = null;
      }

      if (videoFile.value) {
        startChunkUpload(videoFile.value);
        auditResult.value = null;
      }
    };

    // 清空视频
    const clearVideo = () => {
      if (uploadAbortController.value) {
        uploadAbortController.value.abort();
      }

      if (pollTimer) {
        clearInterval(pollTimer);
        pollTimer = null;
      }

      if (videoUrl.value) {
        URL.revokeObjectURL(videoUrl.value);
      }

      videoFile.value = null;
      videoUrl.value = '';
      showProgress.value = false;
      progressPercent.value = 0;
      progressText.value = '';
      auditResult.value = null;
      fileHash.value = '';
      chunks.value = [];
      uploadedChunks.value = [];
      currentChunkIndex.value = 0;
      uploadStatus.value = '';
      updateDetectingStatus();
    };

    // 导出检测结果（优化导出字段）
    const exportResult = () => {
      if (!auditResult.value) {
        ElMessage.warning('暂无检测结果可导出');
        return;
      }

      let csvContent = "文件名,文件大小(MB),检测时间,审核结果,违规类型,置信度(%)\n";
      const sizeMB = (videoFile.value.size / 1024 / 1024).toFixed(2);
      const result = auditResult.value.isPass ? '通过' : '不通过';
      const violationType = auditResult.value.violationType;
      const confidence = (auditResult.value.confidence * 100).toFixed(2);

      csvContent += `${videoFile.value.name},${sizeMB},${auditResult.value.detectTime},${result},${violationType},${confidence}\n`;

      const blob = new Blob([csvContent], {type: 'text/csv;charset=utf-8;'});
      const url = URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.setAttribute('href', url);
      link.setAttribute('download', `AI视频检测结果_${new Date().getTime()}.csv`);
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
      if (pollTimer) {
        clearInterval(pollTimer);
      }
      if (videoUrl.value) {
        URL.revokeObjectURL(videoUrl.value);
      }
      if (uploadAbortController.value) {
        uploadAbortController.value.abort();
      }
    });

    return {
      fileInput,
      videoPlayer,
      statusContainerRef,
      isDragging,
      videoUrl,
      videoFile,
      showProgress,
      progressPercent,
      progressText,
      auditResult,
      isLogin,
      isDetectingFlag,
      uploadStatus,
      triggerFileInput,
      handleFileChange,
      handleDragEnter,
      handleDragLeave,
      handleDrop,
      toggleUpload,
      cancelUpload,
      goToLogin,
      reUploadVideo,
      clearVideo,
      exportResult
    };
  }
};
</script>

<style scoped>
/* 基础样式重置 */
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
  cursor: pointer;
}

.upload-area:hover:not(.drag-over) {
  border-color: #818cf8;
}

.upload-area.drag-over {
  border-color: #4f46e5;
  background: #f5f3ff;
}

.upload-area.has-file {
  border-style: solid;
  border-color: #e2e8f0;
}

.upload-icon-wrapper {
  margin-bottom: 28px;
}

/* 视频预览 */
.video-preview-wrapper {
  margin-bottom: 24px;
}

.preview-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.preview-video {
  width: 100%;
  border-radius: 12px;
  max-height: 240px;
  background: #000;
}

.video-info {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px solid #f1f5f9;
}

.file-name {
  font-size: 14px;
  color: #1e293b;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 70%;
}

.file-size {
  font-size: 12px;
  color: #64748b;
}

/* 优化后的进度条样式 */
.upload-progress {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: 8px;
}

.progress-bg {
  height: 8px;
  background-color: #f1f5f9;
  border-radius: 4px;
  overflow: hidden;
  position: relative;
}

.progress-bar {
  position: absolute;
  left: 0;
  top: 0;
  height: 100%;
  background: linear-gradient(90deg, #4f46e5 0%, #7c3aed 100%);
  border-radius: 4px;
  transition: width 0.4s cubic-bezier(0.39, 0.575, 0.565, 1);
}

.progress-glow {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(90deg, rgba(255,255,255,0.2) 0%, rgba(255,255,255,0) 100%);
  animation: progressGlow 1.5s infinite linear;
}

@keyframes progressGlow {
  0% { transform: translateX(-100%); }
  100% { transform: translateX(100%); }
}

.progress-text {
  font-size: 12px;
  color: #64748b;
  text-align: center;
  font-weight: 500;
}

/* 控制按钮 */
.upload-controls {
  display: flex;
  gap: 12px;
  justify-content: center;
}

.control-btn {
  padding: 8px 16px;
  border: none;
  border-radius: 8px;
  background: linear-gradient(135deg, #4f46e5, #7c3aed);
  color: white;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.control-btn.pause {
  background: linear-gradient(135deg, #e6a23c, #f59e0b);
}

.cancel-btn {
  background: #f8fafc;
  color: #64748b;
  border: 1px solid #e2e8f0;
}

.control-btn:disabled {
  background: #cbd5e1;
  cursor: not-allowed;
}

/* 上传按钮容器 */
.upload-btn-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 16px;
  position: relative;
}

.upload-btn-container.hidden {
  display: none;
}

.file-input {
  position: absolute !important;
  opacity: 0 !important;
  width: 0 !important;
  height: 0 !important;
  z-index: -1 !important;
}

.upload-btn {
  padding: 12px 20px;
  border-radius: 12px;
  font-size: 14px;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  border: none;
  background: linear-gradient(135deg, #4f46e5, #7c3aed);
  color: #fff;
}

.upload-btn:disabled {
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
  cursor: pointer;
}

/* 处理中状态 */
.processing-state h3 {
  font-size: 20px;
  color: #4f46e5;
  margin: 20px 0 8px;
}

.processing-state p {
  font-size: 14px;
  color: #64748b;
}

.loading-icon {
  animation: spin 1.5s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

/* 审核结果样式 */
.audit-result-state {
  width: 100%;
}

.result-card {
  background: #fff;
  border-radius: 20px;
  padding: 40px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  animation: fadeIn 0.3s;
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f1f5f9;
}

.result-header h3 {
  font-size: 24px;
  color: #1e293b;
}

.result-tag {
  padding: 8px 16px;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 500;
}

.tag-pass {
  background-color: #f0fff4;
  color: #67c23a;
}

.tag-fail {
  background-color: #fff2f0;
  color: #f56c6c;
}

/* 优化结果信息展示 */
.video-info-summary {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-item .label {
  font-size: 14px;
  color: #64748b;
}

.info-item .value {
  font-size: 16px;
  color: #1e293b;
  font-weight: 500;
}

.info-item .value.text-danger {
  color: #dc2626;
}

/* 违规详情 */
.violation-details {
  background: #f8fafc;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 32px;
}

.violation-details h4 {
  font-size: 18px;
  color: #1e293b;
  margin-bottom: 16px;
}

.details-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 16px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.detail-label {
  font-size: 14px;
  color: #64748b;
}

.detail-value {
  font-size: 16px;
  color: #1e293b;
  font-weight: 500;
}

.detail-value.high {
  color: #dc2626;
}

/* 结果操作按钮 */
.result-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
  margin-top: 24px;
}

.btn-primary {
  background: linear-gradient(135deg, #4f46e5, #7c3aed);
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

  .result-actions {
    flex-direction: column;
  }

  .video-info-summary, .details-grid {
    grid-template-columns: 1fr;
  }
}
</style>