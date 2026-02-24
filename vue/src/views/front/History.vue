<script>
import {ref, reactive, onMounted} from 'vue';
import request from '../../utils/request';
import {ElMessage} from 'element-plus';
import {useRouter} from 'vue-router';

export default {
  name: "History",
  setup() {
    const router = useRouter();

    const loading = ref(false);
    const empty = ref(false);
    const historyList = ref([]);
    const pagination = reactive({
      pageNum: 1,
      pageSize: 10,
      total: 0,
      totalPages: 0
    });
    const filterForm = reactive({
      startTime: '',
      endTime: '',
      status: '',
      detectType: ''
    });

    // 新增：图片预览相关状态
    const previewVisible = ref(false); // 预览弹窗是否显示
    const previewImage = ref(''); // 预览图片的URL
    const previewLoading = ref(false); // 图片加载状态
    const currentRecord = ref(null); // 当前点击的记录

    // 新增：时间格式化函数（处理ISO时间，转换为本地时区）
    const formatDetectTime = (timeStr) => {
      if (!timeStr) return '未知时间';

      try {
        const date = new Date(timeStr);
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        const hours = String(date.getHours()).padStart(2, '0');
        const minutes = String(date.getMinutes()).padStart(2, '0');
        const seconds = String(date.getSeconds()).padStart(2, '0');

        return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
      } catch (error) {
        console.error('时间格式化失败：', error);
        return '未知时间';
      }
    };

    // 违规类型映射表
    const violationTypeMap = {
      Normal: '合规',
      adult: '色情',
      violate: '暴力',
    };

    // 格式化违规类型
    const formatViolationType = (status, violationType) => {
      if (status === 3) {
        return '—';
      }
      if (status === 2) {
        return violationTypeMap[violationType] || '未知违规类型';
      }
      return '未知状态';
    };

    // 新增：图片预览方法（仅保留方式1）
    const handleImagePreview = async (item) => {

      if (item.detectType !== 'image') {
        ElMessage.info('仅支持图片类型的预览');
        return;
      }

      // 检测失败的记录不预览
      if (item.status === 3) {
        ElMessage.info('检测失败的记录无法预览图片');
        return;
      }

      // 没有图片URL的情况
      if (!item.presignedUrl) {
        ElMessage.warning('该记录暂无图片可预览');
        return;
      }

      try {
        currentRecord.value = item;
        previewLoading.value = true;

        // 直接使用后端返回的imageUrl
        previewImage.value = item.presignedUrl;
        previewVisible.value = true;
      } catch (error) {
        console.error('图片预览失败：', error);
        ElMessage.error('图片预览失败：' + (error.message || '获取图片失败'));
      } finally {
        previewLoading.value = false;
      }
    };

    // 新增：关闭预览弹窗
    const closePreview = () => {
      previewVisible.value = false;
      previewImage.value = '';
      currentRecord.value = null;
      // 移除blob URL相关代码（方式2已删除）
    };

    // 获取历史记录列表
    const getHistoryList = async () => {
      try {
        loading.value = true;
        empty.value = false;

        const params = {
          pageNum: pagination.pageNum,
          pageSize: pagination.pageSize,
          startTime: filterForm.startTime ? `${filterForm.startTime} 00:00:00` : '',
          endTime: filterForm.endTime ? `${filterForm.endTime} 23:59:59` : '',
          status: filterForm.status === '' ? '' : parseInt(filterForm.status),
          detectType: filterForm.detectType === '' ? '' : filterForm.detectType
        };

        Object.keys(params).forEach(key => {
          if (params[key] === '' || params[key] === null || params[key] === undefined) {
            delete params[key];
          }
        });

        const response = await request({
          url: '/api/history/review/picture',
          method: 'get',
          params: params,
          timeout: 10000
        });

        const responseData = response;
        if (responseData.code === 200) {
          historyList.value = responseData.data?.records || [];
          pagination.total = responseData.data?.total || 0;
          pagination.pageSize = responseData.data?.size || 10;
          pagination.totalPages = responseData.data?.pages || 0;
          pagination.pageNum = responseData.data?.current || 1;

          empty.value = historyList.value.length === 0;

          if (historyList.value.length > 0) {
            ElMessage.success(`成功加载 ${historyList.value.length} 条检测记录`);
          }
        } else {
          ElMessage.error('获取历史记录失败：' + (responseData.msg || '查询异常'));
          historyList.value = [];
          empty.value = true;
        }

      } catch (error) {
        console.error('获取检测历史记录失败：', error);
        historyList.value = [];
        empty.value = true;

        const errorCode = error.response?.status || error.code;
        const errorMsg = error.response?.data?.msg || error.message;

        if (errorCode === 401) {
          ElMessage.warning(errorMsg || '您尚未登录，请先登录');
          localStorage.removeItem('accessToken');
          router.push('/login');
        } else {
          ElMessage.error('获取历史记录失败：' + (errorMsg || '服务器内部异常'));
        }
      } finally {
        loading.value = false;
        if (!loading.value) {
          empty.value = historyList.value.length === 0;
        }
      }
    };

    // 分页方法
    const handlePrevPage = () => {
      if (pagination.pageNum <= 1) return;
      pagination.pageNum--;
      getHistoryList();
    };

    const handleNextPage = () => {
      if (pagination.pageNum >= pagination.totalPages) return;
      pagination.pageNum++;
      getHistoryList();
    };

    const handleJumpPage = (page) => {
      if (page < 1 || page > pagination.totalPages) {
        ElMessage.warning(`请输入有效的页码（1-${pagination.totalPages || 1}）`);
        return;
      }
      pagination.pageNum = page;
      getHistoryList();
    };

    // 重置筛选
    const handleResetFilter = () => {
      filterForm.startTime = '';
      filterForm.endTime = '';
      filterForm.status = '';
      filterForm.detectType = '';
      pagination.pageNum = 1;
      ElMessage.info('筛选条件已重置，将加载全部检测记录');
      getHistoryList();
    };

    // 挂载加载
    onMounted(() => {
      getHistoryList();
    });

    // 暴露变量和方法
    return {
      loading,
      empty,
      historyList,
      pagination,
      filterForm,
      formatDetectTime,
      formatViolationType,
      // 新增预览相关
      previewVisible,
      previewImage,
      previewLoading,
      currentRecord,
      handleImagePreview,
      closePreview,
      // 原有方法
      getHistoryList,
      handlePrevPage,
      handleNextPage,
      handleJumpPage,
      handleResetFilter
    };
  }
};
</script>

<template>
  <div class="history-container">
    <!-- 页面标题 -->
    <div class="history-page-title">
      <h2>内容检测历史记录</h2>
      <p>查看所有图片/视频的AI检测记录，支持分页与筛选</p>
    </div>

    <!-- 筛选区域 -->
    <div class="history-filter-box">
      <div class="filter-item">
        <label>检测时间：</label>
        <input
            type="date"
            v-model="filterForm.startTime"
            class="filter-input"
            placeholder="开始时间"
        />
        <span class="filter-split">至</span>
        <input
            type="date"
            v-model="filterForm.endTime"
            class="filter-input"
            placeholder="结束时间"
        />
      </div>
      <div class="filter-item">
        <label>检测类型：</label>
        <select v-model="filterForm.detectType" class="filter-select">
          <option value="">全部类型</option>
          <option value="image">图片检测</option>
          <option value="video">视频检测</option>
        </select>
      </div>
      <div class="filter-item">
        <label>检测状态：</label>
        <select v-model="filterForm.status" class="filter-select">
          <option value="">全部状态</option>
          <option value="2">检测成功</option>
          <option value="3">检测失败</option>
        </select>
      </div>
      <div class="filter-actions">
        <button class="btn filter-btn" @click="getHistoryList">查询</button>
        <button class="btn reset-btn" @click="handleResetFilter">重置</button>
      </div>
    </div>

    <!-- 列表区域 -->
    <div class="history-list-box">
      <div class="history-loading" v-if="loading">
        <span class="loading-text">正在加载历史记录...</span>
        <div class="loading-spinner"></div>
      </div>
      <div class="history-list" v-else>
        <div class="history-list-header">
          <div class="list-col col-time">检测时间</div>
          <div class="list-col col-type-item">检测类型</div>
          <div class="list-col col-name">文件名称</div>
          <div class="list-col col-status">检测状态</div>
          <div class="list-col col-violation-type">违规类型</div>
        </div>
        <div class="history-list-body">
          <!-- 新增点击事件 @click="handleImagePreview(item)" -->
          <div
              class="history-list-item"
              v-for="(item, index) in historyList"
              :key="index"
              @click="handleImagePreview(item)"
              :class="{ 'cursor-pointer': item.detectType === 'image' && item.status === 2 }"
          >
            <div class="list-col col-time">{{ formatDetectTime(item.detectTime) }}</div>
            <div class="list-col col-type-item">
              <span class="type-tag" :class="item.detectType === 'image' ? 'tag-image' : 'tag-video'">
                {{ item.detectType === 'image' ? '图片' : '视频' }}
              </span>
            </div>
            <div class="list-col col-name">{{ item.fileName || '未命名文件' }}</div>
            <div class="list-col col-status">
              <span class="status-tag" :class="item.status === 2 ? 'tag-pass' : 'tag-fail'">
                {{ item.status === 2 ? '检测成功' : item.status === 3 ? '检测失败' : '未知状态' }}
              </span>
            </div>
            <div class="list-col col-violation-type">
              {{ formatViolationType(item.status, item.violationType) }}
            </div>
          </div>
          <div class="history-list-empty" v-if="empty">
            <div class="empty-content">
              <svg class="empty-icon-small" viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg">
                <path fill="#c0c4cc"
                      d="M864 160H160c-17.7 0-32 14.3-32 32v736c0 17.7 14.3 32 32 32h704c17.7 0 32-14.3 32-32V192c0-17.7-14.3-32-32-32zM640 736c0 4.4-3.6 8-8 8H416c-4.4 0-8-3.6-8-8v-48c0-4.4 3.6-8 8-8h216c4.4 0 8 3.6 8 8v48zm0-160c0 4.4-3.6 8-8 8H416c-4.4 0-8-3.6-8-8v-48c0-4.4 3.6-8 8-8h216c4.4 0 8 3.6 8 8v48zm0-160c0 4.4-3.6 8-8 8H416c-4.4 0-8-3.6-8-8v-48c0-4.4 3.6-8 8-8h216c4.4 0 8 3.6 8 8v48zm192-304H224v-64h608v64z"></path>
              </svg>
              <p class="empty-text-small">
                暂无{{
                  filterForm.detectType === 'image' ? '图片' : filterForm.detectType === 'video' ? '视频' : '内容'
                }}检测历史记录
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>

  <!-- 分页区域 -->
  <div class="history-pagination" v-if="!loading && pagination.total > 0">
    <button class="pagination-btn" @click="handlePrevPage" :disabled="pagination.pageNum <= 1">
      上一页
    </button>
    <span class="pagination-info">
      第 {{ pagination.pageNum }} 页 / 共 {{ pagination.totalPages }} 页
      （总 {{ pagination.total }} 条记录）
    </span>
    <button class="pagination-btn" @click="handleNextPage" :disabled="pagination.pageNum >= pagination.totalPages">
      下一页
    </button>
  </div>

  <!-- 新增：图片预览弹窗 -->
  <div class="image-preview-modal" v-if="previewVisible">
    <div class="preview-mask" @click="closePreview"></div>
    <div class="preview-content">
      <!-- 关闭按钮 -->
      <button class="preview-close-btn" @click="closePreview">×</button>

      <!-- 图片信息 -->
      <div class="preview-header" v-if="currentRecord">
        <p class="preview-filename">{{ currentRecord.fileName || '未命名图片' }}</p>
        <p class="preview-status">
          检测状态：{{ currentRecord.status === 2 ? '检测成功' : '检测失败' }} |
          违规类型：{{ formatViolationType(currentRecord.status, currentRecord.violationType) }}
        </p>
      </div>

      <!-- 图片加载状态 -->
      <div class="preview-loading" v-if="previewLoading">
        <span>图片加载中...</span>
        <div class="loading-spinner"></div>
      </div>

      <!-- 预览图片 -->
      <img
          v-if="previewImage && !previewLoading"
          :src="previewImage"
          class="preview-image"
          alt="图片预览"
          @error="() => { ElMessage.error('图片加载失败'); previewLoading = false; }"
      />

      <!-- 图片加载失败 -->
      <div class="preview-error" v-if="!previewLoading && !previewImage">
        <svg class="empty-icon-small" viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg">
          <path fill="#c0c4cc"
                d="M864 160H160c-17.7 0-32 14.3-32 32v736c0 17.7 14.3 32 32 32h704c17.7 0 32-14.3 32-32V192c0-17.7-14.3-32-32-32zM640 736c0 4.4-3.6 8-8 8H416c-4.4 0-8-3.6-8-8v-48c0-4.4 3.6-8 8-8h216c4.4 0 8 3.6 8 8v48zm0-160c0 4.4-3.6 8-8 8H416c-4.4 0-8-3.6-8-8v-48c0-4.4 3.6-8 8-8h216c4.4 0 8 3.6 8 8v48zm0-160c0 4.4-3.6 8-8 8H416c-4.4 0-8-3.6-8-8v-48c0-4.4 3.6-8 8-8h216c4.4 0 8 3.6 8 8v48zm192-304H224v-64h608v64z"></path>
        </svg>
        <p>图片加载失败，请重试</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* 原有样式保持不变 */
.history-container {
  width: 100%;
  max-width: 1400px;
  margin: 0 auto;
  padding: 40px 20px;
  box-sizing: border-box;
  font-family: "Helvetica Neue", Helvetica, Arial, sans-serif;
  color: #1a2b48;
  background-color: #ffffff;
}
.history-page-title {
  text-align: center;
  margin-bottom: 32px;
}
.history-page-title h2 {
  font-size: 28px;
  font-weight: 600;
  margin-bottom: 8px;
  color: #1a2b48;
}
.history-page-title p {
  font-size: 16px;
  color: #4e5d78;
  margin: 0;
}
.history-filter-box {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background-color: #fafbfc;
  border: 1px solid #e6e9ed;
  border-radius: 12px;
  margin-bottom: 24px;
}
.filter-item {
  display: flex;
  align-items: center;
  gap: 8px;
}
.filter-item label {
  font-size: 14px;
  color: #1a2b48;
  font-weight: 500;
}
.filter-input {
  padding: 8px 12px;
  border: 1px solid #e6e9ed;
  border-radius: 8px;
  font-size: 14px;
  color: #1a2b48;
  outline: none;
  transition: border-color 0.3s ease;
}
.filter-input:focus {
  border-color: #409eff;
}
.filter-split {
  font-size: 14px;
  color: #4e5d78;
}
.filter-select {
  padding: 8px 12px;
  border: 1px solid #e6e9ed;
  border-radius: 8px;
  font-size: 14px;
  color: #1a2b48;
  outline: none;
  transition: border-color 0.3s ease;
  background-color: #ffffff;
}
.filter-select:focus {
  border-color: #409eff;
}
.filter-actions {
  display: flex;
  gap: 12px;
  margin-left: auto;
}
.btn {
  padding: 8px 16px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  border: none;
}
.filter-btn {
  background-color: #409eff;
  color: #ffffff;
}
.filter-btn:hover {
  background-color: #337ecc;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.3);
}
.reset-btn {
  background-color: #f0f2f5;
  color: #4e5d78;
}
.reset-btn:hover {
  background-color: #e6e9ed;
  color: #1a2b48;
}
.history-list-box {
  background-color: #fafbfc;
  border: 1px solid #e6e9ed;
  border-radius: 12px;
  overflow: hidden;
  margin-bottom: 24px;
}
.history-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
}
.loading-text {
  font-size: 16px;
  color: #409eff;
  margin-bottom: 16px;
}
.loading-spinner {
  width: 32px;
  height: 32px;
  border: 3px solid rgba(64, 158, 255, 0.2);
  border-top-color: #409eff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}
@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}
.history-list-header {
  display: flex;
  background-color: #e6f7ff;
  border-bottom: 1px solid #409eff;
}
.history-list-body {
  max-height: 600px;
  overflow-y: auto;
  min-height: 200px;
}
.history-list-item {
  display: flex;
  border-bottom: 1px solid #e6e9ed;
  transition: background-color 0.3s ease;
}
/* 新增：可点击的记录添加鼠标样式 */
.history-list-item.cursor-pointer {
  cursor: pointer;
}
.history-list-item:hover {
  background-color: #f0f7ff;
}
.history-list-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 200px;
  width: 100%;
}
.empty-content {
  text-align: center;
}
.empty-icon-small {
  width: 48px;
  height: 48px;
  fill: #c0c4cc;
  margin-bottom: 12px;
}
.empty-text-small {
  font-size: 14px;
  color: #909399;
  margin: 0;
}
.list-col {
  padding: 16px;
  flex: 1;
  text-align: center;
  font-size: 14px;
}
.col-time {
  flex: 2;
}
.col-type-item {
  flex: 1.5;
}
.col-name {
  flex: 3;
  text-align: left;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.col-status {
  flex: 2;
}
.col-violation-type {
  flex: 3;
}
.type-tag {
  padding: 4px 12px;
  border-radius: 16px;
  font-size: 12px;
  font-weight: 500;
}
.tag-image {
  background-color: #e8f4f8;
  color: #4299e1;
}
.tag-video {
  background-color: #fdf2f8;
  color: #9f7aea;
}
.status-tag {
  padding: 4px 12px;
  border-radius: 16px;
  font-size: 12px;
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
.history-pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 24px;
  padding: 16px 0;
  font-size: 14px;
  color: #4e5d78;
}
.pagination-btn {
  padding: 8px 16px;
  border-radius: 8px;
  font-size: 14px;
  color: #1a2b48;
  background-color: #f0f2f5;
  border: none;
  cursor: pointer;
  transition: all 0.3s ease;
}
.pagination-btn:hover:not(:disabled) {
  background-color: #e6e9ed;
  color: #409eff;
}
.pagination-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
.pagination-info {
  color: #4e5d78;
}

/* 新增：图片预览弹窗样式 */
.image-preview-modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1000;
}
.preview-mask {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.7);
  cursor: pointer;
}
.preview-content {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background-color: #ffffff;
  border-radius: 12px;
  padding: 20px;
  max-width: 90%;
  max-height: 90%;
  overflow: auto;
  z-index: 1001;
}
.preview-close-btn {
  position: absolute;
  top: 16px;
  right: 16px;
  width: 32px;
  height: 32px;
  border: none;
  background-color: transparent;
  color: #666;
  font-size: 24px;
  cursor: pointer;
  transition: color 0.3s ease;
}
.preview-close-btn:hover {
  color: #f56c6c;
}
.preview-header {
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #e6e9ed;
}
.preview-filename {
  font-size: 16px;
  font-weight: 600;
  color: #1a2b48;
  margin: 0 0 8px 0;
}
.preview-status {
  font-size: 14px;
  color: #4e5d78;
  margin: 0;
}
.preview-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
}
.preview-loading span {
  font-size: 14px;
  color: #409eff;
  margin-bottom: 16px;
}
.preview-image {
  max-width: 100%;
  max-height: 70vh;
  display: block;
  margin: 0 auto;
  border-radius: 8px;
}
.preview-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
  text-align: center;
}
.preview-error p {
  font-size: 14px;
  color: #f56c6c;
  margin-top: 16px;
}

/* 响应式样式保持不变 */
@media (max-width: 1024px) {
  .history-container {
    max-width: 100%;
  }
  .col-type-item {
    flex: 1.2;
  }
  .col-name, .col-violation-type {
    flex: 2.5;
  }
}
@media (max-width: 768px) {
  .history-filter-box {
    flex-direction: column;
    align-items: flex-start;
  }
  .filter-actions {
    margin-left: 0;
    width: 100%;
    justify-content: flex-start;
  }
  .list-col {
    padding: 12px 8px;
    font-size: 12px;
  }
  .col-type-item {
    flex: 1;
  }
  .col-name, .col-violation-type {
    flex: 2;
  }
  .history-pagination {
    flex-direction: column;
    gap: 12px;
  }
  .preview-content {
    max-width: 95%;
    max-height: 95%;
  }
}
@media (max-width: 480px) {
  .history-container {
    padding: 20px 16px;
  }
  .history-page-title h2 {
    font-size: 24px;
  }
  .history-list-header {
    display: none;
  }
  .history-list-item {
    flex-direction: column;
    align-items: flex-start;
    padding: 16px;
  }
  .list-col {
    padding: 4px 0;
    text-align: left;
    width: 100%;
    display: flex;
    align-items: center;
  }
  .list-col::before {
    content: attr(data-label);
    font-weight: 500;
    margin-right: 8px;
    color: #4e5d78;
    min-width: 80px;
  }
  .col-time::before { content: '检测时间：'; }
  .col-type-item::before { content: '检测类型：'; }
  .col-name::before { content: '文件名称：'; }
  .col-status::before { content: '检测状态：'; }
  .col-violation-type::before { content: '违规类型：'; }
  .history-list-empty { height: 150px; }
  .empty-icon-small { width: 40px; height: 40px; }
}
</style>