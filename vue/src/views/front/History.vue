<script>
import {ref, reactive, onMounted} from 'vue';
import request from '../../utils/request'; // 适配你的request路径
import {ElMessage} from 'element-plus';
import {useRouter} from 'vue-router'; // 引入路由，用于跳转登录页

export default {
  name: "History",
  setup() {
    // 1. 引入路由实例
    const router = useRouter();

    // 1. 响应式变量定义
    // 列表加载与空状态
    const loading = ref(false);
    const empty = ref(false);
    // 历史记录列表
    const historyList = ref([]);
    // 分页参数：适配后端HistoryRequestDTO的pageNum/pageSize
    const pagination = reactive({
      pageNum: 1,       // 前端使用的当前页（对应后端pageNum）
      pageSize: 10,     // 每页条数（对应后端pageSize）
      total: 0,         // 总记录数（后端返回的total）
      totalPages: 0     // 总页数（后端返回的pages）
    });
    // 筛选参数：适配后端HistoryRequestDTO字段
    const filterForm = reactive({
      startTime: '',    // 开始时间（前端日期选择器值）
      endTime: '',      // 结束时间（前端日期选择器值）
      status: '',       // 检测状态（all:全部, 2:检测成功, 3:检测失败）
      detectType: ''    // 检测类型（all:全部, picture:图片, video:视频）
    });

    // 2. 核心方法：获取历史记录列表（适配后端参数）
    const getHistoryList = async () => {
      try {
        // 开启加载状态
        loading.value = true;
        empty.value = false;

        // 构建请求参数：严格适配后端HistoryRequestDTO
        const params = {
          pageNum: pagination.pageNum,    // 对应后端pageNum
          pageSize: pagination.pageSize,  // 对应后端pageSize
          // 日期格式转换：前端日期字符串转后端LocalDateTime格式（yyyy-MM-dd HH:mm:ss）
          startTime: filterForm.startTime ? `${filterForm.startTime} 00:00:00` : '',
          endTime: filterForm.endTime ? `${filterForm.endTime} 23:59:59` : '',
          // 状态映射：前端筛选值转后端Integer类型状态
          status: filterForm.status === '' ? '' : parseInt(filterForm.status),
          // 检测类型映射：前端值转后端detectType（picture/video）
          detectType: filterForm.detectType === '' ? '' : filterForm.detectType
        };

        // 过滤空参数（避免传递空字符串给后端）
        Object.keys(params).forEach(key => {
          if (params[key] === '' || params[key] === null || params[key] === undefined) {
            delete params[key];
          }
        });

        // 调用后端接口
        const response = await request({
          url: '/api/history/review/picture',
          method: 'get',
          params: params,
          timeout: 10000
        });

        // 处理后端返回结果
        const responseData = response;
        // 校验后端返回的code
        if (responseData.code === 200) {
          // 列表数据：从data.records获取
          historyList.value = responseData.data?.records || [];
          // 分页数据：同步后端返回的参数
          pagination.total = responseData.data?.total || 0;
          pagination.pageSize = responseData.data?.size || 10;
          pagination.totalPages = responseData.data?.pages || 0;
          pagination.pageNum = responseData.data?.current || 1;

          // 强制触发空状态判断
          empty.value = historyList.value.length === 0;

          // 成功提示
          if (historyList.value.length > 0) {
            ElMessage.success(`成功加载 ${historyList.value.length} 条检测记录`);
          }
        } else {
          // 后端返回非200状态码
          ElMessage.error('获取历史记录失败：' + (responseData.msg || '查询异常'));
          historyList.value = [];
          empty.value = true;
        }

      } catch (error) {
        console.error('获取检测历史记录失败：', error);
        historyList.value = [];
        empty.value = true;

        // 401未登录处理
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
        // 关闭加载状态
        loading.value = false;
        // 最终确认空状态
        if (!loading.value) {
          empty.value = historyList.value.length === 0;
        }
      }
    };

    // 3. 分页切换方法
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

    // 4. 筛选重置方法
    const handleResetFilter = () => {
      filterForm.startTime = '';
      filterForm.endTime = '';
      filterForm.status = '';
      filterForm.detectType = '';
      pagination.pageNum = 1;
      ElMessage.info('筛选条件已重置，将加载全部检测记录');
      getHistoryList();
    };

    // 5. 页面挂载时加载数据
    onMounted(() => {
      getHistoryList();
    });

    // 暴露变量与方法
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

    <!-- 筛选区域：适配后端参数 -->
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
          <option value="picture">图片检测</option> <!-- 适配后端picture -->
          <option value="video">视频检测</option>   <!-- 适配后端video -->
        </select>
      </div>

      <div class="filter-item">
        <label>检测状态：</label>
        <select v-model="filterForm.status" class="filter-select">
          <option value="">全部状态</option>
          <option value="2">检测成功</option> <!-- 对应后端2-检测成功 -->
          <option value="3">检测失败</option> <!-- 对应后端3-检测失败 -->
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

      <!-- 列表容器：始终显示表头 -->
      <div class="history-list" v-else>
        <!-- 列表表头 -->
        <div class="history-list-header">
          <div class="list-col col-time">检测时间</div>
          <div class="list-col col-type-item">检测类型</div>
          <div class="list-col col-name">文件名称</div>
          <div class="list-col col-status">检测状态</div>
          <div class="list-col col-violation-type">违规类型</div>
        </div>

        <!-- 列表内容 -->
        <div class="history-list-body">
          <!-- 有数据时显示列表 -->
          <div class="history-list-item" v-for="(item, index) in historyList" :key="index">
            <div class="list-col col-time">{{ item.detectTime || '未知时间' }}</div>
            <div class="list-col col-type-item">
              <span class="type-tag" :class="item.detectType === 'picture' ? 'tag-image' : 'tag-video'">
                {{ item.detectType === 'picture' ? '图片' : '视频' }}
              </span>
            </div>
            <div class="list-col col-name">{{ item.fileName || '未命名文件' }}</div>
            <div class="list-col col-status">
              <!-- 适配后端状态码：2-检测成功 3-检测失败 -->
              <span class="status-tag" :class="item.status === 2 ? 'tag-pass' : 'tag-fail'">
                {{ item.status === 2 ? '检测成功' : item.status === 3 ? '检测失败' : '未知状态' }}
              </span>
            </div>
            <div class="list-col col-violation-type">
              {{ item.status === 2 ? '—' : (item.violationType || '未知违规类型') }}
            </div>
          </div>

          <!-- 无数据提示 -->
          <div class="history-list-empty" v-if="empty">
            <div class="empty-content">
              <svg class="empty-icon-small" viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg">
                <path fill="#c0c4cc"
                      d="M864 160H160c-17.7 0-32 14.3-32 32v736c0 17.7 14.3 32 32 32h704c17.7 0 32-14.3 32-32V192c0-17.7-14.3-32-32-32zM640 736c0 4.4-3.6 8-8 8H416c-4.4 0-8-3.6-8-8v-48c0-4.4 3.6-8 8-8h216c4.4 0 8 3.6 8 8v48zm0-160c0 4.4-3.6 8-8 8H416c-4.4 0-8-3.6-8-8v-48c0-4.4 3.6-8 8-8h216c4.4 0 8 3.6 8 8v48zm0-160c0 4.4-3.6 8-8 8H416c-4.4 0-8-3.6-8-8v-48c0-4.4 3.6-8 8-8h216c4.4 0 8 3.6 8 8v48zm192-304H224v-64h608v64z"></path>
              </svg>
              <p class="empty-text-small">
                暂无{{
                  filterForm.detectType === 'picture' ? '图片' : filterForm.detectType === 'video' ? '视频' : '内容'
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
</template>

<style scoped>
/* 全局容器样式 */
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

/* 列表样式 */
.history-list-header {
  display: flex;
  background-color: #e6f7ff;
  border-bottom: 1px solid #409eff;
}

.history-list-body {
  max-height: 600px;
  overflow-y: auto;
  min-height: 200px; /* 保证表格最小高度 */
}

.history-list-item {
  display: flex;
  border-bottom: 1px solid #e6e9ed;
  transition: background-color 0.3s ease;
}

.history-list-item:hover {
  background-color: #f0f7ff;
}

/* 表格内无数据提示样式 */
.history-list-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 200px; /* 与表格最小高度匹配 */
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

/* 列宽调整 */
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

/* 检测类型标签 */
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

/* 响应式适配 */
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

  /* 移动端列标签 */
  .col-time::before { content: '检测时间：'; }
  .col-type-item::before { content: '检测类型：'; }
  .col-name::before { content: '文件名称：'; }
  .col-status::before { content: '检测状态：'; }
  .col-violation-type::before { content: '违规类型：'; }

  /* 移动端空数据提示适配 */
  .history-list-empty { height: 150px; }
  .empty-icon-small { width: 40px; height: 40px; }
}
</style>