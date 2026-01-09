<script>
import { ref, reactive, onMounted } from 'vue';
import request from '../../utils/request'; // 适配你的request路径

export default {
  name: "History",
  setup() {
    // 1. 响应式变量定义
    // 列表加载与空状态
    const loading = ref(false);
    const empty = ref(false);
    // 历史记录列表
    const historyList = ref([]);
    // 分页参数
    const pagination = reactive({
      pageNum: 1,
      pageSize: 10,
      total: 0, // 总记录数
      totalPages: 0 // 总页数
    });
    // 筛选参数（新增detectType：all/all, image/图片, video/视频）
    const filterForm = reactive({
      startTime: '',
      endTime: '',
      status: '', // all: 全部, pass: 合规, fail: 违规
      detectType: '' // all: 全部, image: 图片, video: 视频
    });

    // 2. 核心方法：获取历史记录列表（支持分页+筛选）
    const getHistoryList = async () => {
      try {
        // 开启加载状态
        loading.value = true;
        empty.value = false;

        // 构建请求参数（GET请求拼接在url后）
        const params = {
          pageNum: pagination.pageNum,
          pageSize: pagination.pageSize,
          startTime: filterForm.startTime,
          endTime: filterForm.endTime,
          status: filterForm.status,
          detectType: filterForm.detectType // 新增检测类型筛选
        };

        // 调用后端历史记录接口（适配分页的接口）
        const response = await request({
          url: '/api/ai/detect/history',
          method: 'get',
          params: params, // 拼接查询参数
          timeout: 10000
        });

        // 解析响应数据
        const resData = response.data || [];
        pagination.total = response.total || 0;
        pagination.totalPages = Math.ceil(pagination.total / pagination.pageSize);

        // 处理列表与空状态
        historyList.value = resData;
        empty.value = historyList.value.length === 0 && !loading.value;

      } catch (error) {
        console.error('获取检测历史记录失败：', error);
        historyList.value = [];
        empty.value = true;
        alert('获取历史记录失败：' + (error.message || '服务器内部异常'));
      } finally {
        // 关闭加载状态
        loading.value = false;
      }
    };

    // 3. 分页切换方法
    // 上一页
    const handlePrevPage = () => {
      if (pagination.pageNum <= 1) return;
      pagination.pageNum--;
      getHistoryList();
    };

    // 下一页
    const handleNextPage = () => {
      if (pagination.pageNum >= pagination.totalPages) return;
      pagination.pageNum++;
      getHistoryList();
    };

    // 跳转到指定页码（可选，增强实用性）
    const handleJumpPage = (page) => {
      if (page < 1 || page > pagination.totalPages) return;
      pagination.pageNum = page;
      getHistoryList();
    };

    // 4. 筛选重置方法（新增detectType重置）
    const handleResetFilter = () => {
      filterForm.startTime = '';
      filterForm.endTime = '';
      filterForm.status = '';
      filterForm.detectType = ''; // 重置检测类型
      pagination.pageNum = 1; // 重置页码为第一页
      getHistoryList();
    };

    // 5. 页面挂载时加载第一页数据
    onMounted(() => {
      getHistoryList();
    });

    // 暴露给模板的变量与方法
    return {
      loading,
      empty,
      historyList,
      pagination,
      filterForm,
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

    <!-- 筛选区域（新增检测类型筛选） -->
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
          <option value="pass">检测合规</option>
          <option value="fail">检测违规</option>
        </select>
      </div>

      <div class="filter-actions">
        <button class="btn filter-btn" @click="getHistoryList">查询</button>
        <button class="btn reset-btn" @click="handleResetFilter">重置</button>
      </div>
    </div>

    <!-- 历史记录列表区域 -->
    <div class="history-list-box">
      <!-- 加载状态 -->
      <div class="history-loading" v-if="loading">
        <span class="loading-text">正在加载历史记录...</span>
        <div class="loading-spinner"></div>
      </div>

      <!-- 空状态 -->
      <div class="history-empty" v-else-if="empty">
        <svg class="empty-icon" viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg">
          <path fill="#c0c4cc" d="M864 160H160c-17.7 0-32 14.3-32 32v736c0 17.7 14.3 32 32 32h704c17.7 0 32-14.3 32-32V192c0-17.7-14.3-32-32-32zM640 736c0 4.4-3.6 8-8 8H416c-4.4 0-8-3.6-8-8v-48c0-4.4 3.6-8 8-8h216c4.4 0 8 3.6 8 8v48zm0-160c0 4.4-3.6 8-8 8H416c-4.4 0-8-3.6-8-8v-48c0-4.4 3.6-8 8-8h216c4.4 0 8 3.6 8 8v48zm0-160c0 4.4-3.6 8-8 8H416c-4.4 0-8-3.6-8-8v-48c0-4.4 3.6-8 8-8h216c4.4 0 8 3.6 8 8v48zm192-304H224v-64h608v64z"></path>
        </svg>
        <p class="empty-text">暂无{{ filterForm.detectType === 'image' ? '图片' : filterForm.detectType === 'video' ? '视频' : '内容' }}检测历史记录</p>
      </div>

      <!-- 列表数据 -->
      <div class="history-list" v-else>
        <!-- 列表表头（新增检测类型列） -->
        <div class="history-list-header">
          <div class="list-col col-time">检测时间</div>
          <div class="list-col col-type-item">检测类型</div> <!-- 新增 -->
          <div class="list-col col-name">文件名称</div>
          <div class="list-col col-status">检测状态</div>
          <div class="list-col col-violation-type">违规类型</div>
          <div class="list-col col-score">置信度分数</div>
        </div>

        <!-- 列表内容 -->
        <div class="history-list-body">
          <div class="history-list-item" v-for="(item, index) in historyList" :key="index">
            <div class="list-col col-time">{{ item.detectTime || '未知时间' }}</div>
            <!-- 新增检测类型列 -->
            <div class="list-col col-type-item">
              <span class="type-tag" :class="item.detectType === 'image' ? 'tag-image' : 'tag-video'">
                {{ item.detectType === 'image' ? '图片' : item.detectType === 'video' ? '视频' : '未知' }}
              </span>
            </div>
            <div class="list-col col-name">{{ item.fileName || '未命名文件' }}</div>
            <div class="list-col col-status">
              <span class="status-tag" :class="item.isPass ? 'tag-pass' : 'tag-fail'">
                {{ item.isPass ? '检测合规' : '检测违规' }}
              </span>
            </div>
            <div class="list-col col-violation-type">
              {{ item.isPass ? '—' : (item.violationType || '未知违规类型') }}
            </div>
            <div class="list-col col-score">{{ item.violationScore || 0 }}/100</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 分页区域 -->
    <div class="history-pagination" v-if="!empty && pagination.total > 0">
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
  </div>
</template>

<style scoped>
/* 全局容器样式 */
.history-container {
  width: 100%;
  max-width: 1400px; /* 加宽容器适配新增列 */
  margin: 0 auto;
  padding: 40px 20px;
  box-sizing: border-box;
  font-family: "Helvetica Neue", Helvetica, Arial, sans-serif;
  color: #1a2b48;
  background-color: #ffffff;
}

/* 页面标题 */
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

/* 筛选区域 */
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

/* 按钮通用样式 */
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

/* 列表区域 */
.history-list-box {
  background-color: #fafbfc;
  border: 1px solid #e6e9ed;
  border-radius: 12px;
  overflow: hidden;
  margin-bottom: 24px;
}

/* 加载状态 */
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

/* 空状态 */
.history-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
}

.empty-icon {
  width: 64px;
  height: 64px;
  fill: #c0c4cc;
  margin-bottom: 16px;
}

.empty-text {
  font-size: 16px;
  color: #909399;
  margin: 0;
}

/* 列表样式 */
.history-list-header {
  display: flex;
  background-color: #e6f7ff;
  border-bottom: 1px solid #409eff;
}

.history-list-body {
  max-height: 600px;
  overflow-y: auto;
}

.history-list-item {
  display: flex;
  border-bottom: 1px solid #e6e9ed;
  transition: background-color 0.3s ease;
}

.history-list-item:hover {
  background-color: #f0f7ff;
}

.list-col {
  padding: 16px;
  flex: 1;
  text-align: center;
  font-size: 14px;
}

/* 调整列宽适配新增的检测类型列 */
.col-time {
  flex: 2;
}

.col-type-item { /* 新增检测类型列 */
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

.col-violation-type { /* 重命名避免冲突 */
  flex: 3;
}

.col-score {
  flex: 2;
}

/* 检测类型标签（新增） */
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

/* 状态标签 */
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

/* 分页区域 */
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

/* 响应式适配（适配新增列） */
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

  /* 移动端列标签自定义 */
  .col-time::before { content: '检测时间：'; }
  .col-type-item::before { content: '检测类型：'; }
  .col-name::before { content: '文件名称：'; }
  .col-status::before { content: '检测状态：'; }
  .col-violation-type::before { content: '违规类型：'; }
  .col-score::before { content: '置信度：'; }
}
</style>