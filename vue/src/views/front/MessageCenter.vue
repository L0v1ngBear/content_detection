<template>
  <div class="message-center-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2 class="page-title">消息中心</h2>
      <div class="header-actions">
        <el-button
            type="primary"
            size="small"
            @click="markAllRead"
            :disabled="unreadCount === 0"
        >
          <el-icon><Check /></el-icon> 标记全部已读
        </el-button>
        <el-button
            type="default"
            size="small"
            @click="refreshMessages"
        >
          <el-icon><Refresh /></el-icon> 刷新
        </el-button>
      </div>
    </div>

    <!-- 筛选工具栏 -->
    <div class="filter-toolbar">
      <el-form :inline="true" :model="filterForm" class="filter-form">
        <el-form-item label="消息类型" style="width: 180px;">
          <el-select
              v-model="filterForm.type"
              placeholder="全部类型"
              size="small"
              clearable
              style="width: 100%;"
          >
          <!-- 关键修改：value改为大写，和后端一致 -->
          <el-option label="系统通知" value="SYSTEM"></el-option>
          <el-option label="检测结果" value="DETECT"></el-option>
          <el-option label="预警提示" value="WARNING"></el-option>
          <el-option label="错误通知" value="ERROR"></el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="消息状态" style="width: 180px;">
          <el-select
              v-model="filterForm.isRead"
              placeholder="全部状态"
              size="small"
              clearable
              style="width: 100%;"
          >
          <!-- 0未读，1已读 -->
          <el-option label="未读" value="0"></el-option>
          <el-option label="已读" value="1"></el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="时间范围" style="width: 300px;">
          <el-date-picker
              v-model="filterForm.dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              size="small"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              style="width: 100%;"
          ></el-date-picker>
        </el-form-item>
        <el-form-item>
          <el-button
              type="primary"
              size="small"
              @click="handleSearch"
          >
            查询
          </el-button>
          <el-button
              size="small"
              @click="resetFilter"
          >
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 消息列表 -->
    <div class="message-list-container">
      <!-- 空状态 -->
      <div class="empty-state" v-if="messageList.length === 0 && !loading">
        <el-empty description="暂无消息数据">
          <el-button type="primary" size="small" @click="refreshMessages">
            重新加载
          </el-button>
        </el-empty>
      </div>

      <!-- 加载状态 -->
      <div class="loading-state" v-if="loading">
        <el-skeleton :rows="8" animated />
      </div>

      <!-- 消息列表 -->
      <div class="message-list" v-else>
        <div
            class="message-item"
            v-for="(msg, index) in messageList"
            :key="msg.id || index"
            :class="{ 'unread': !msg.isRead }"
            @click="viewMessageDetail(msg)"
        >
          <!-- 消息类型图标 -->
          <div class="msg-type-icon">
            <el-icon v-if="msg.type === 'SYSTEM'"><Bell /></el-icon>
            <el-icon v-else-if="msg.type === 'DETECT'"><DocumentChecked /></el-icon>
            <el-icon v-else-if="msg.type === 'WARNING'"><Warning /></el-icon>
            <el-icon v-else-if="msg.type === 'ERROR'"><CircleClose /></el-icon>
            <el-icon v-else><InfoFilled /></el-icon>
          </div>

          <!-- 消息内容 -->
          <div class="msg-content">
            <div class="msg-header">
              <span class="msg-title">{{ getMsgTypeText(msg.type) }}</span>
              <span class="msg-time">{{ formatTime(msg.createTime) }}</span>
            </div>
            <div class="msg-body">
              <p class="msg-text">{{ msg.content }}</p>
            </div>
          </div>

          <!-- 操作按钮 -->
          <div class="msg-actions">
            <el-button
                type="text"
                size="small"
                @click.stop="markSingleRead(msg)"
                v-if="!msg.isRead"
            >
              标为已读
            </el-button>
            <el-tag size="small" type="success" v-if="msg.isRead">已读</el-tag>
            <el-tag size="small" type="primary" v-else>未读</el-tag>
          </div>
        </div>
      </div>
    </div>

    <!-- 分页控件 -->
    <div class="pagination-container" v-if="messageList.length > 0 && !loading">
      <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="pagination.pageNum"
          :page-sizes="[10, 20, 50]"
      :page-size="pagination.pageSize"
      layout="total, sizes, prev, pager, next, jumper"
      :total="pagination.total"
      background
      size="small"
      >
      </el-pagination>
    </div>

    <!-- 消息详情弹窗 -->
    <el-dialog
        v-model="detailVisible"
        title="消息详情"
        width="600px"
        destroy-on-close
        @close="clearCurrentMessage"
    >
      <div class="message-detail" v-if="currentMessage">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="消息类型">
            <el-tag
                :type="getTagType(currentMessage.type)"
                size="small"
            >
              {{ getMsgTypeText(currentMessage.type) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="发送时间">
            {{ formatTime(currentMessage.createTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="消息状态">
            <el-tag
                :type="currentMessage.isRead ? 'success' : 'primary'"
                size="small"
            >
              {{ currentMessage.isRead ? '已读' : '未读' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="消息内容">
            <div class="msg-content-detail">
              {{ currentMessage.content }}
            </div>
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="detailVisible = false">关闭</el-button>
          <el-button
              type="primary"
              @click="markCurrentRead"
              v-if="currentMessage && !currentMessage.isRead"
          >
            标记为已读
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import request from '../../utils/request';

// 响应式数据
const loading = ref(false);
const unreadCount = ref(0);
const messageList = ref([]);
const detailVisible = ref(false);
const currentMessage = ref(null);

// 筛选表单
const filterForm = reactive({
  type: '',
  isRead: '',
  dateRange: []
});

// 分页配置
const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
});

// 页面加载时获取消息列表
onMounted(() => {
  fetchMessageList();
});

// 获取消息列表（核心修改）
const fetchMessageList = async () => {
  loading.value = true;
  try {
    // 构造查询参数（直接使用大写type，无需转换）
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      type: filterForm.type,          // 已为大写，直接传递
      isRead: filterForm.isRead,      // 后端接收参数名是status，不是isRead
      startDate: filterForm.dateRange[0] || '',
      endDate: filterForm.dateRange[1] || ''
    };

    const response = await request({
      url: '/api/msg/list',
      method: 'get',
      params
    });

    if (response.code === 200) {
      // 关键修改：后端返回列表是records，不是list
      const rawList = response.data.records || [];
      // 转换数据格式：isRead数字转布尔值
      messageList.value = rawList.map(item => ({
        ...item,
        isRead: item.isRead === 1  // 后端1=已读，0=未读，转布尔值
      }));
      // 总条数取后端的total
      pagination.total = response.data.total || 0;
      // 计算未读数量
      unreadCount.value = messageList.value.filter(msg => !msg.isRead).length;
    } else {
      ElMessage.error('获取消息列表失败：' + (response.msg || '未知错误'));
      messageList.value = [];
    }
  } catch (error) {
    console.error('获取消息列表出错：', error);
    ElMessage.error('获取消息列表失败，请稍后重试');
    messageList.value = [];
  } finally {
    loading.value = false;
  }
};

// 刷新消息列表
const refreshMessages = () => {
  pagination.pageNum = 1;
  fetchMessageList();
};

// 处理查询
const handleSearch = () => {
  pagination.pageNum = 1;
  fetchMessageList();
};

// 重置筛选条件
const resetFilter = () => {
  filterForm.type = '';
  filterForm.isRead = '';
  filterForm.dateRange = [];
  pagination.pageNum = 1;
  fetchMessageList();
};

// 分页大小改变
const handleSizeChange = (val) => {
  pagination.pageSize = val;
  fetchMessageList();
};

// 当前页改变
const handleCurrentChange = (val) => {
  pagination.pageNum = val;
  fetchMessageList();
};

// 标记全部已读
const markAllRead = async () => {
  try {
    await ElMessageBox.confirm(
        '确定要将所有消息标记为已读吗？',
        '确认操作',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
    );

    await request({
      url: '/api/msg/all-read',
      method: 'post'
    });

    // 更新本地数据
    messageList.value.forEach(msg => {
      msg.isRead = true;
    });
    unreadCount.value = 0;

    ElMessage.success('所有消息已标记为已读');
  } catch (error) {
    if (error !== 'cancel') {
      console.error('标记全部已读失败：', error);
      ElMessage.error('标记失败，请稍后重试');
    }
  }
};

// 标记单条消息已读
const markSingleRead = async (msg) => {
  try {
    await request({
      url: `/api/msg/read/${msg.id}`,
      method: 'post'
    });

    // 更新本地数据
    msg.isRead = true;
    unreadCount.value = Math.max(0, unreadCount.value - 1);

    ElMessage.success('消息已标记为已读');
  } catch (error) {
    console.error('标记消息已读失败：', error);
    ElMessage.error('标记失败，请稍后重试');
  }
};

// 查看消息详情
const viewMessageDetail = (msg) => {
  currentMessage.value = { ...msg };
  detailVisible.value = true;

  // 如果消息未读，自动标记为已读
  if (!msg.isRead) {
    markSingleRead(msg);
  }
};

// 标记当前弹窗中的消息为已读
const markCurrentRead = () => {
  if (currentMessage.value) {
    markSingleRead(currentMessage.value);
    // 更新弹窗内的状态
    currentMessage.value.isRead = true;
  }
};

// 清空当前消息
const clearCurrentMessage = () => {
  currentMessage.value = null;
};

// 获取消息类型文本（适配大写type）
const getMsgTypeText = (type) => {
  switch (type) {
    case 'SYSTEM':
      return '系统通知';
    case 'DETECT':
      return '检测结果';
    case 'WARNING':
      return '预警提示';
    case 'ERROR':
      return '错误通知';
    default:
      return '未知消息';
  }
};

// 获取标签类型（适配大写type）
const getTagType = (type) => {
  switch (type) {
    case 'SYSTEM':
      return 'info';
    case 'DETECT':
      return 'success';
    case 'WARNING':
      return 'warning';
    case 'ERROR':
      return 'danger';
    default:
      return '';
  }
};

// 格式化时间（兼容带T的格式）
const formatTime = (timeStr) => {
  if (!timeStr) return '未知时间';
  // 替换T为空格，兼容后端时间格式 2026-02-10T15:40:24
  const formatStr = timeStr.replace('T', ' ');
  const date = new Date(formatStr);
  // 处理无效日期
  if (isNaN(date.getTime())) return '未知时间';
  return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}:${date.getSeconds().toString().padStart(2, '0')}`;
};
</script>

<style scoped>
/* 页面容器 */
.message-center-container {
  width: 100%;
  height: 100%;
  padding: 20px;
  background-color: #fff;
  border-radius: 8px;
}

/* 页面头部 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #e5e7eb;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 10px;
}

/* 筛选工具栏 */
.filter-toolbar {
  margin-bottom: 20px;
  padding: 16px;
  background-color: #f9fafb;
  border-radius: 6px;
}

.filter-form {
  display: flex;
  align-items: center;
}

/* 消息列表容器 */
.message-list-container {
  margin-bottom: 20px;
  min-height: 400px;
}

.empty-state, .loading-state {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 400px;
}

/* 消息列表 */
.message-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

/* 消息项 */
.message-item {
  display: flex;
  align-items: flex-start;
  padding: 16px;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.message-item:hover {
  border-color: #93c5fd;
  background-color: #f0f9ff;
}

/* 未读消息样式 */
.message-item.unread {
  border-left: 4px solid #3b82f6;
  background-color: #eff6ff;
}

/* 消息类型图标 */
.msg-type-icon {
  margin-right: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background-color: #f3f4f6;
  color: #4b5563;
  flex-shrink: 0;
}

/* 消息内容 */
.msg-content {
  flex: 1;
  overflow: hidden;
}

.msg-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.msg-title {
  font-weight: 500;
  color: #1f2937;
  font-size: 14px;
}

.msg-time {
  font-size: 12px;
  color: #6b7280;
}

.msg-body {
  overflow: hidden;
}

.msg-text {
  margin: 0;
  font-size: 14px;
  color: #4b5563;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* 消息操作按钮 */
.msg-actions {
  display: flex;
  align-items: center;
  margin-left: 16px;
  flex-shrink: 0;
}

/* 分页容器 */
.pagination-container {
  display: flex;
  justify-content: center;
  padding: 10px;
}

/* 消息详情 */
.message-detail {
  padding: 10px 0;
}

.msg-content-detail {
  padding: 12px;
  background-color: #f9fafb;
  border-radius: 6px;
  min-height: 100px;
  white-space: pre-wrap;
  word-wrap: break-word;
  line-height: 1.8;
}

/* 弹窗底部 */
.dialog-footer {
  text-align: right;
}

/* 响应式适配 */
@media (max-width: 768px) {
  .message-center-container {
    padding: 10px;
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .header-actions {
    width: 100%;
    justify-content: flex-end;
  }

  .filter-toolbar {
    padding: 10px;
  }

  .filter-form {
    flex-wrap: wrap;
  }

  .message-item {
    flex-direction: column;
    gap: 10px;
  }

  .msg-type-icon {
    margin-right: 0;
    margin-bottom: 8px;
  }

  .msg-actions {
    margin-left: 0;
    justify-content: flex-end;
  }

  /* 筛选表单整体样式 - 加到<style scoped>中 */
  .filter-form {
    display: flex;
    align-items: center;
    gap: 20px; /* 增加筛选项之间的间距 */
    flex-wrap: wrap; /* 小屏幕自动换行 */
  }

}
</style>