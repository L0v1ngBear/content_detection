<template>
  <div class="app-layout">
    <!-- 全局布局容器（包含侧边栏 + 主内容区） -->
    <div class="layout-container">
      <!-- 公共侧边栏（支持折叠/展开，整合所有功能） -->
      <aside class="layout-sidebar" :class="{ 'sidebar-collapsed': isSidebarCollapsed }">
        <!-- 侧边栏Logo（替换为AI内容检测平台） -->
        <div class="sidebar-logo">
          <svg v-if="isSidebarCollapsed" viewBox="0 0 24 24" fill="#409eff" class="logo-icon-small">
            <path
                d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8zm0-14c-2.21 0-4 1.79-4 4s1.79 4 4 4 4-1.79 4-4-1.79-4-4-4zm0 6c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2z"/>
          </svg>
          <div v-else class="logo-wrapper">
            <svg viewBox="0 0 24 24" fill="#409eff" class="logo-icon">
              <path
                  d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8zm0-14c-2.21 0-4 1.79-4 4s1.79 4 4 4 4-1.79 4-4-1.79-4-4-4zm0 6c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2z"/>
            </svg>
            <span class="logo-text">AI 内容检测平台</span>
          </div>
        </div>

        <!-- 侧边栏导航菜单（替换为AI检测平台核心功能） -->
        <nav class="sidebar-nav">
          <ul class="nav-list">
            <li class="nav-item" v-for="menu in navMenus" :key="menu.path">
              <router-link
                  class="nav-link"
                  :to="menu.path"
                  exact-active-class="nav-link-active"
              >
                <svg class="nav-icon" viewBox="0 0 24 24" fill="#667085">
                  <path :d="menu.iconPath"/>
                </svg>
                <span class="nav-text" v-if="!isSidebarCollapsed">{{ menu.name }}</span>
              </router-link>
            </li>
          </ul>
        </nav>

        <!-- 侧边栏底部功能区（整合消息、个人中心、退出登录） -->
        <div class="sidebar-footer">
          <!-- 系统消息模块 - 核心修改：点击直接跳转消息中心 -->
          <div class="sidebar-function-item msg-wrapper" @click.stop="gotoMessageCenter()">
            <!-- 消息按钮 -->
            <button class="msg-btn" @click.stop>
              <svg viewBox="0 0 24 24" fill="#667085" class="msg-icon">
                <path
                    d="M20 2H4c-1.1 0-1.99.9-1.99 2L2 22l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm0 14H6l-2 2V4h16v12z"/>
              </svg>
              <!-- 未读消息红点（有未读时显示） -->
              <span class="msg-badge" v-if="unreadMsgCount > 0">{{ unreadMsgCount }}</span>
            </button>
            <!-- 消息文字 -->
            <span class="function-text" v-if="!isSidebarCollapsed">系统消息</span>
          </div>

          <!-- 个人中心 -->
          <div
              class="sidebar-function-item user-info"
              v-if="userInfo.hasLogin"
              @click.stop="handleUserInfoClick()"
              :style="{cursor: 'pointer'}"
          >
            <img src="https://picsum.photos/40/40" alt="用户头像" class="user-avatar"/>
            <span class="function-text user-name" v-if="!isSidebarCollapsed">
              {{ userInfo.username || '未知用户' }}
            </span>
          </div>

          <!-- 登录/退出登录 -->
          <div
              class="sidebar-function-item"
              @click.stop="userInfo.hasLogin ? handleLogout() : handleLogin()"
          >
            <button class="logout-btn" @click.stop>
              <svg viewBox="0 0 24 24" fill="#667085" class="logout-icon">
                <path v-if="userInfo.hasLogin" d="M17 3v12h-4v-7H8v7H4V3h13m2-2H2v18h2V3h15v18h2V1z"/>
                <path v-else d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8zm-1-13h2v6h-2zm0 8h2v2h-2z"/>
              </svg>
            </button>
            <span class="function-text" v-if="!isSidebarCollapsed">
              {{ userInfo.hasLogin ? '退出登录' : '登录' }}
            </span>
          </div>
        </div>
      </aside>

      <!-- 主内容区域 -->
      <div class="layout-main">
        <!-- 简化版公共头部 -->
        <header class="layout-header">
          <button class="header-toggle-btn" @click.stop="toggleSidebar()">
            <svg viewBox="0 0 24 24" fill="#667085" class="toggle-icon">
              <path d="M3 18h18v-2H3v2zm0-5h18v-2H3v2zm0-7v2h18V6H3z"/>
            </svg>
          </button>
        </header>

        <!-- 路由内容容器 -->
        <main class="layout-content">
          <router-view/>
        </main>

        <!-- 公共底部 -->
        <footer class="layout-footer">
          <p class="footer-text">© 2026 AI 内容检测平台 - 版权所有</p>
        </footer>
      </div>
    </div>

    <!-- 消息详情弹窗 -->
    <teleport to="body">
      <div class="msg-detail-mask" v-if="showMsgDetail" @click.stop="closeMsgDetail()">
        <div class="msg-detail-popup" @click.stop>
          <div class="msg-detail-header">
            <h3>{{ currentMsg?.type ? getMsgTypeText(currentMsg.type) : '消息详情' }}</h3>
            <button class="close-btn" @click.stop="closeMsgDetail()">×</button>
          </div>
          <div class="msg-detail-content">
            <div class="detail-item">
              <label>消息类型：</label>
              <span :class="`type-tag ${currentMsg?.type}`">{{ getMsgTypeText(currentMsg?.type) }}</span>
            </div>
            <div class="detail-item">
              <label>发送时间：</label>
              <span>{{ formatTime(currentMsg?.createTime) }}</span>
            </div>
            <div class="detail-item">
              <label>消息状态：</label>
              <span class="status-tag" :class="currentMsg?.isRead ? 'read' : 'unread'">
                {{ currentMsg?.isRead ? '已读' : '未读' }}
              </span>
            </div>
            <div class="detail-item content-item">
              <label>消息内容：</label>
              <div class="content-text">{{ currentMsg?.content || '无消息内容' }}</div>
            </div>
          </div>
          <div class="msg-detail-footer">
            <button class="btn cancel-btn" @click.stop="closeMsgDetail()">关闭</button>
            <button
                class="btn confirm-btn"
                @click.stop="markSingleMsgRead()"
                v-if="currentMsg && !currentMsg.isRead"
            >
              标记为已读
            </button>
          </div>
        </div>
      </div>
    </teleport>
  </div>
</template>

<script setup>
import {ref, onMounted, onUnmounted, watch} from 'vue';
import {useRouter} from 'vue-router';
import {ElMessage, ElMessageBox} from 'element-plus';
import request from '../utils/request';

// 路由实例
const router = useRouter();

// 响应式数据
const isSidebarCollapsed = ref(false);
const windowWidth = ref(window.innerWidth);
const msgList = ref([]); // 所有消息列表
const unreadMsgCount = ref(0); // 未读消息数量
const isLoadingMsg = ref(false); // 消息列表加载状态，防止重复请求

// WebSocket核心配置
const ws = ref(null); // WebSocket实例
const wsReconnectTimer = ref(null); // 重连定时器
const heartBeatTimer = ref(null); // 心跳定时器
const WS_RECONNECT_INTERVAL = 5000; // 重连间隔5秒
const WS_HEARTBEAT_INTERVAL = 15000; // 缩短心跳间隔为15秒，提升保活稳定性
const currentUserId = ref(''); // 用户ID
const wsUrl = ref(''); // WebSocket连接地址
const isWsConnected = ref(false); // 标记WebSocket是否已成功连接

// 消息详情相关
const showMsgDetail = ref(false); // 是否显示消息详情弹窗
const currentMsg = ref(null); // 当前选中的消息

// 用户信息
const userInfo = ref({
  username: '',
  hasLogin: false,
  tokenExpired: false
});

// 侧边栏导航菜单配置
const navMenus = ref([
  {
    path: "/front/home",
    name: "检测首页",
    iconPath: "M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8h5z"
  },
  {
    path: "/front/picture",
    name: "图片AI检测",
    iconPath: "M21 19V5c0-1.1-.9-2-2-2H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2zM8.5 13.5l2.5 3.01L14.5 12l4.5 6H5l3.5-4.5z"
  },
  {
    path: "/front/video",
    name: "视频AI检测",
    iconPath: "M17 10.5V7c0-.55-.45-1-1-1H4c-.55 0-1 .45-1 1v10c0 .55.45 1 1 1h12c.55 0 1-.45 1-1v-3.5l4 4v-11l-4 4z"
  },
  {
    path: "/front/history",
    name: "检测历史记录",
    iconPath: "M13 3c-4.97 0-9 4.03-9 9H1l3.89 3.89.07.14L9 21l4-4 4 4v-6.17l-2-2V13l2.81-2.81c.39-.39.39-1.02 0-1.41l-2.59-2.59c.39-.39 1.02-.39 1.41 0L12 10.59 9.19 7.79c-.39-.39-1.02-.39-1.41 0L5 10.59c-.39.39-.39 1.02 0 1.41l2.59 2.59c.39.39 1.02.39 1.41 0L12 17.41l2.81 2.81c.39.39 1.02.39 1.41 0l2.59-2.59c.39.39.39 1.02 0 1.41L14.19 14 17 11.19V13c0 .55.45 1 1 1h4c.55 0 1-.45 1-1v-6c0-.55-.45-1-1-1h-4c-.55 0-1 .45-1 1v1.59L13 7.41 10.19 10.21c-.39-.39-1.02-.39-1.41 0L6 7.41c-.39.39-.39 1.02 0 1.41l2.59 2.59c.39.39 1.02.39 1.41 0L12 9.41 14.81 6.6c.39-.39 1.02-.39 1.41 0l2.59 2.59c.39.39.39 1.02 0 1.41L16.81 9 19 11.19V5h-2v1.59L13 9.41 10.19 6.6c-.39-.39-1.02-.39-1.41 0L6 9.41c-.39.39-.39 1.02 0 1.41l2.59 2.59c.39.39 1.02.39 1.41 0L12 16.59 14.81 19.4c.39.39 1.02.39 1.41 0l2.59-2.59c.39.39.39 1.02 0-1.41L16.81 13H19v6h-2v-1.59L13 10.59 10.19 13.4c-.39.39-1.02.39-1.41 0L6 10.59c-.39.39-.39 1.02 0 1.41l2.59 2.59c.39.39 1.02.39 1.41 0L12 9.41 14.81 6.6c.39-.39 1.02-.39 1.41 0l2.59 2.59c.39.39.39 1.02 0 1.41L16.81 12H19v-6h-2v1.59L13 7.41z"
  },
  {
    path: "/front/setting",
    name: "平台设置",
    iconPath: "M19.14 12.94c.04-.3.06-.61.06-.94s-.02-.64-.07-.94l2.03-1.58c.18-.14.23-.41.12-.61l-1.92-3.32c-.12-.22-.37-.29-.59-.22l-2.39.96c-.5-.38-1.03-.7-1.62-.94L14 2.81c-.04-.24-.24-.41-.48-.41h-4c-.24 0-.43.17-.47.41L9.25 5.35c-.59.24-1.13.57-1.62.94l-2.39-.96c-.22-.08-.47 0-.59.22L.74 10.4c-.12.22-.07.47.12.61l2.03 1.58c-.05.3-.07.63-.07.94s.02.64.07.94l-2.03 1.58c-.18.14-.23.41-.12.61l1.92 3.32c.12.22.37.29.59.22l2.39-.96c.5.38 1.03.7 1.62.94L10 21.19c.04.24.24.41.48.41h4c.24 0 .43-.17.47-.41l.75-2.54c.59-.24 1.13-.57 1.62-.94l2.39.96c.22.08.47 0 .59-.22l1.92-3.32c.12-.22.07-.47-.12-.61l-2.03-1.58zM12 15.6c-1.98 0-3.6-1.62-3.6-3.6s1.62-3.6 3.6-3.6 3.6 1.62 3.6 3.6-1.62 3.6-3.6 3.6z"
  }
]);

// 生成用户ID
const generateUserId = () => {
  const savedUserId = localStorage.getItem("userId");
  if (savedUserId) {
    return savedUserId;
  }
  // 如果没有保存的ID，生成一个临时ID并保存
  const newUserId = `user_${Date.now()}_${Math.random().toString(36).substr(2, 8)}`;
  localStorage.setItem("userId", newUserId);
  return newUserId;
};

// 初始化用户信息
const initUserInfo = async () => {
  userInfo.value = {username: '', hasLogin: false, tokenExpired: false};
  const accessToken = localStorage.getItem("accessToken");
  const localUsername = localStorage.getItem("username") || localStorage.getItem("rememberedUsername");

  if (!accessToken) return;

  const tokenExpireTimestamp = localStorage.getItem("tokenExpireTimestamp");
  if (tokenExpireTimestamp && new Date().getTime() > Number(tokenExpireTimestamp)) {
    userInfo.value.tokenExpired = true;
    ElMessage.warning('登录状态已过期，请重新登录');
    handleLogout(false);
    return;
  }

  if (localUsername) {
    userInfo.value = {
      username: localUsername,
      hasLogin: true,
      tokenExpired: false
    };
    await fetchUsername();
    currentUserId.value = generateUserId();
    initWebSocket();
    return;
  }

  await fetchUsername();
  currentUserId.value = generateUserId();
  initWebSocket();
};

// 从接口获取用户名
const fetchUsername = async () => {
  try {
    const res = await request({
      url: "/api/getUserName",
      method: "get"
    });
    if (res.code === 200 && res.data) {
      userInfo.value.username = res.data;
      userInfo.value.hasLogin = true;
      localStorage.setItem("username", res.data);
      ElMessage.success(`欢迎回来，${res.data}！`);
    } else {
      userInfo.value.hasLogin = false;
      ElMessage.error('获取用户信息失败：无用户数据');
    }
  } catch (error) {
    console.error("获取用户名失败：", error);
    userInfo.value.hasLogin = false;
    ElMessage.error('获取用户信息失败，请重新登录');
    handleLogout(false);
  }
};

// 侧边栏折叠/展开切换
const toggleSidebar = () => {
  isSidebarCollapsed.value = !isSidebarCollapsed.value;
  ElMessage.info(isSidebarCollapsed.value ? '侧边栏已折叠' : '侧边栏已展开');
};

// 退出登录逻辑
const handleLogout = async (needConfirm = true) => {
  if (needConfirm) {
    try {
      await ElMessageBox.confirm(
          '确定要退出登录吗？',
          '退出确认',
          {
            confirmButtonText: '确认退出',
            cancelButtonText: '取消',
            type: 'warning',
            center: true
          }
      );
    } catch (error) {
      console.log("用户取消退出登录");
      return;
    }
  }

  // 关闭WebSocket连接
  closeWebSocket();

  // 清除登录信息
  localStorage.removeItem("accessToken");
  localStorage.removeItem("refreshToken");
  localStorage.removeItem("tokenExpireTime");
  localStorage.removeItem("tokenExpireTimestamp");
  localStorage.removeItem("username");
  localStorage.removeItem("rememberedUsername");

  userInfo.value = {
    username: '',
    hasLogin: false,
    tokenExpired: false
  };

  const refreshToken = localStorage.getItem("refreshToken");
  if (refreshToken) {
    try {
      await request.post("/auth/logout", {refreshToken});
      console.log("后端退出登录成功");
    } catch (err) {
      console.error("后端退出登录失败：", err);
    }
  }

  ElMessage.success('退出登录成功');
  router.push("/login");
};

// 登录按钮点击逻辑
const handleLogin = () => {
  router.push("/login")
      .then(() => {
        console.log("✅ 跳转到登录页面成功");
        ElMessage.info('请先完成登录操作');
      })
      .catch(err => {
        console.error("❌ 跳转到登录页面失败：", err);
        ElMessage.error('登录页面不存在，请检查路由配置');
      });
};

// 个人中心点击跳转逻辑
const handleUserInfoClick = () => {
  if (!userInfo.value.hasLogin) {
    ElMessage.warning('请先登录');
    handleLogin();
    return;
  }
  router.push("/front/setting")
      .then(() => {
        console.log("✅ 跳转到平台设置页面成功");
        ElMessage.info('已进入个人设置页面');
      })
      .catch(err => {
        console.error("❌ 跳转到平台设置页面失败：", err);
        ElMessage.error('设置页面不存在，请检查路由配置');
      });
};

// 跳转到消息中心页面
const gotoMessageCenter = () => {
  if (!userInfo.value.hasLogin) {
    ElMessage.warning('请先登录查看消息');
    handleLogin();
    return;
  }

  if (msgList.value.length === 0 && !isLoadingMsg.value) {
    loadMsgList().then(() => {
      router.push("/front/message-center")
          .then(() => {
            ElMessage.info(unreadMsgCount.value > 0
                ? `您有${unreadMsgCount.value}条未读消息`
                : '已进入消息中心');
          })
          .catch(err => {
            console.error("❌ 跳转到消息中心失败：", err);
            ElMessage.error('消息中心页面不存在，请检查路由配置');
          });
    });
  } else {
    router.push("/front/message-center")
        .then(() => {
          ElMessage.info(unreadMsgCount.value > 0
              ? `您有${unreadMsgCount.value}条未读消息`
              : '已进入消息中心');
        })
        .catch(err => {
          console.error("❌ 跳转到消息中心失败：", err);
          ElMessage.error('消息中心页面不存在，请检查路由配置');
        });
  }
};

// 加载消息列表
const loadMsgList = async () => {
  if (isLoadingMsg.value) return;

  try {
    isLoadingMsg.value = true;
    const response = await request({
      url: "/api/msg/list",
      method: "get",
      params: {
        pageSize: 10,
        userId: currentUserId.value
      }
    });
    const resData = response.data.records || [];
    msgList.value = resData;
    unreadMsgCount.value = resData.filter(msg => !msg.isRead).length;

    if (unreadMsgCount.value > 0) {
      ElMessage.info(`您有${unreadMsgCount.value}条未读消息`);
    }
  } catch (error) {
    console.error("加载消息列表失败：", error);
    msgList.value = [];
    unreadMsgCount.value = 0;
    ElMessage.error('加载消息失败，请稍后重试');
  } finally {
    isLoadingMsg.value = false;
  }
};

// 标记所有消息为已读
const markAllAsRead = async () => {
  if (unreadMsgCount.value === 0) {
    ElMessage.info('暂无未读消息');
    return;
  }
  try {
    await request({
      url: "/api/msg/all-read",
      method: "post",
      params: {userId: currentUserId.value}
    });

    // 同步更新本地消息状态
    msgList.value.forEach(msg => {
      msg.isRead = true;
    });

    unreadMsgCount.value = 0;
    ElMessage.success('所有未读消息已标记为已读');
  } catch (error) {
    console.error("标记所有消息为已读失败：", error);
    ElMessage.error('标记已读失败，请稍后重试');
  }
};

// 获取消息类型文本
const getMsgTypeText = (type) => {
  switch (type) {
    case "SYSTEM":
    case "system":
      return "系统通知";
    case "DETECT":
    case "detect":
      return "检测结果";
    case "IMAGE":
    case "image":
      return "图片检测";
    case "WARNING":
    case "warning":
      return "预警提示";
    case "ERROR":
    case "error":
      return "错误通知";
    default:
      return "未知消息";
  }
};

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return '未知时间';
  const timestamp = typeof timeStr === 'number' ? timeStr : new Date(timeStr).getTime();
  const date = new Date(timestamp);
  return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}:${date.getSeconds().toString().padStart(2, '0')}`;
};

// 查看消息详情
const viewMsgDetail = (msg) => {
  currentMsg.value = {...msg};
  showMsgDetail.value = true;
};

// 关闭消息详情弹窗
const closeMsgDetail = () => {
  showMsgDetail.value = false;
  currentMsg.value = null;
};

// 标记单条消息为已读
const markSingleMsgRead = async () => {
  if (!currentMsg.value) return;

  try {
    await request({
      url: `/api/msg/read/${currentMsg.value.id}`,
      method: "post",
      params: {userId: currentUserId.value}
    });

    // 同步更新本地消息状态
    currentMsg.value.isRead = true;
    const index = msgList.value.findIndex(item => item.id === currentMsg.value.id);
    if (index !== -1) {
      msgList.value[index].isRead = true;
    }

    unreadMsgCount.value = Math.max(0, unreadMsgCount.value - 1);
    ElMessage.success('消息已标记为已读');
  } catch (error) {
    console.error("标记单条消息为已读失败：", error);
    ElMessage.error('标记已读失败，请稍后重试');
  }
};

// ===================== WebSocket 核心功能（稳定保活版） =====================
// 初始化WebSocket连接（登录后保持长连接）
const initWebSocket = () => {
  // 已有有效连接，直接返回
  if (ws.value && ws.value.readyState === WebSocket.OPEN) {
    console.log('✅ WebSocket已有有效连接，无需重复初始化');
    isWsConnected.value = true;
    return;
  }

  // 未登录，不初始化
  if (!userInfo.value.hasLogin || !currentUserId.value) {
    isWsConnected.value = false;
    return;
  }

  // 关闭旧连接（如果存在）
  closeWebSocket();

  // 构建连接地址
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
  const host = "localhost:8080";
  wsUrl.value = `${protocol}//${host}/ai/result/${currentUserId.value}`;
  console.log('开始连接WebSocket:', wsUrl.value);

  try {
    ws.value = new WebSocket(wsUrl.value);

    // 连接成功
    ws.value.onopen = handleWsOpen;

    // 接收消息
    ws.value.onmessage = handleWsMessage;

    // 连接关闭
    ws.value.onclose = handleWsClose;

    // 连接错误
    ws.value.onerror = handleWsError;

  } catch (error) {
    console.error('WebSocket初始化失败:', error);
    isWsConnected.value = false;
    // 启动重连
    startWsReconnect();
  }
};

// WebSocket连接成功回调
const handleWsOpen = () => {
  console.log('✅ WebSocket连接成功', `userId=${currentUserId.value}`);
  isWsConnected.value = true;

  // 启动心跳保活（缩短为15秒，提升稳定性）
  startHeartBeat();

  // 清除重连定时器
  if (wsReconnectTimer.value) {
    clearTimeout(wsReconnectTimer.value);
    wsReconnectTimer.value = null;
  }

  // 只加载消息列表（移除离线消息接口调用）
  loadMsgList();

  ElMessage.success('消息推送已连接，将保持长连接接收实时检测结果');
};

// WebSocket接收消息回调
const handleWsMessage = (event) => {
  try {
    console.log('📩 收到WebSocket消息:', event.data);
    const message = JSON.parse(event.data);

    // 构建统一的消息格式
    const newMsg = {
      id: message.id || `ws_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
      content: message.content || JSON.stringify(message),
      type: message.type || 'detect',
      createTime: message.createTime || new Date().getTime(),
      isRead: false,
      userId: currentUserId.value,
      rawData: message
    };

    // 处理新消息
    handleNewMessage(newMsg);

  } catch (error) {
    console.error('WebSocket消息解析失败:', error, event.data);
    // 解析失败时创建通用消息
    const errorMsg = {
      id: `ws_error_${Date.now()}`,
      content: `收到系统消息：${event.data}`,
      type: 'system',
      createTime: new Date().getTime(),
      isRead: false,
      userId: currentUserId.value,
    };

    handleNewMessage(errorMsg);
  }
};

// 处理新消息推送
const handleNewMessage = (msgData) => {
  if (!msgData) return;

  // 检查消息是否已存在
  const existsIndex = msgList.value.findIndex(item => item.id === msgData.id);

  if (existsIndex > -1) {
    // 更新已有消息
    msgList.value[existsIndex] = {...msgList.value[existsIndex], ...msgData};
  } else {
    // 添加新消息到列表头部
    msgList.value.unshift(msgData);

    // 未读消息数+1
    if (!msgData.isRead) {
      unreadMsgCount.value += 1;
      ElMessage.info(`📩 收到新消息：${msgData.content.substring(0, 20)}${msgData.content.length > 20 ? '...' : ''}`);
    }
  }

  // 限制消息列表长度
  if (msgList.value.length > 50) {
    msgList.value = msgList.value.slice(0, 50);
  }
};

// WebSocket连接关闭回调（优化：仅异常关闭时重连）
const handleWsClose = (event) => {
  console.log(`🔌 WebSocket连接关闭 [${event.code}]`, event.reason, `userId=${currentUserId.value}`);
  isWsConnected.value = false;

  // 停止心跳
  stopHeartBeat();

  // 优化：仅异常关闭（非1000码）且登录状态下才重连
  // 1000是正常关闭码，不需要重连；其他码都是异常关闭，需要重连
  if (userInfo.value.hasLogin && event.code !== 1000) {
    ElMessage.warning(`消息推送连接异常断开（码：${event.code}），正在尝试重连...`);
    startWsReconnect();
  }
};

// WebSocket错误回调
const handleWsError = (error) => {
  console.error('❌ WebSocket错误:', error, `userId=${currentUserId.value}`);
  isWsConnected.value = false;

  // 停止心跳
  stopHeartBeat();

  // 登录状态下启动重连
  if (userInfo.value.hasLogin) {
    startWsReconnect();
  }
};

// 启动心跳保活（缩短间隔，提升稳定性）
const startHeartBeat = () => {
  // 先清除旧的心跳定时器
  stopHeartBeat();

  // 15秒发送一次心跳（比30秒更稳定）
  heartBeatTimer.value = setInterval(() => {
    if (ws.value && ws.value.readyState === WebSocket.OPEN) {
      try {
        ws.value.send(JSON.stringify({type: "ping", timestamp: Date.now()}));
        console.log('💓 发送WebSocket心跳包');
      } catch (error) {
        console.error('发送心跳包失败:', error);
        // 发送失败时主动触发重连
        startWsReconnect();
      }
    }
  }, WS_HEARTBEAT_INTERVAL);
};

// 停止心跳保活
const stopHeartBeat = () => {
  if (heartBeatTimer.value) {
    clearInterval(heartBeatTimer.value);
    heartBeatTimer.value = null;
  }
};

// 启动WebSocket重连（防重复重连）
const startWsReconnect = () => {
  // 避免重复创建定时器
  if (wsReconnectTimer.value || !userInfo.value.hasLogin) return;

  console.log(`⏳ ${WS_RECONNECT_INTERVAL/1000}秒后尝试重连WebSocket`);
  wsReconnectTimer.value = setTimeout(() => {
    initWebSocket();
  }, WS_RECONNECT_INTERVAL);
};

// 关闭WebSocket连接
const closeWebSocket = () => {
  // 停止心跳和重连
  stopHeartBeat();
  if (wsReconnectTimer.value) {
    clearTimeout(wsReconnectTimer.value);
    wsReconnectTimer.value = null;
  }

  // 关闭连接
  if (ws.value) {
    try {
      // 正常关闭时不触发重连（code=1000）
      ws.value.close(1000, '客户端主动关闭');
    } catch (error) {
      console.error('关闭WebSocket失败:', error);
    }
    ws.value = null;
  }
  isWsConnected.value = false;
};

// 点击空白处关闭弹窗
const handleClickOutside = (e) => {
  if (showMsgDetail.value) {
    const detailPopup = document.querySelector('.msg-detail-popup');
    const isClickInDetail = detailPopup && (detailPopup.contains(e.target) || e.target === detailPopup);
    if (!isClickInDetail) {
      closeMsgDetail();
    }
  }
};

// 响应式窗口适配
const handleWindowResize = () => {
  windowWidth.value = window.innerWidth;
  if (windowWidth.value < 768) {
    isSidebarCollapsed.value = true;
  }
};

// ===================== 生命周期 =====================
onMounted(() => {
  handleWindowResize();
  window.addEventListener("resize", handleWindowResize);
  document.addEventListener("click", handleClickOutside);

  // 初始化用户信息
  initUserInfo();

  // 监听登录状态变化
  watch(
      () => userInfo.value.hasLogin,
      (newVal) => {
        if (newVal) {
          // 登录成功，建立WebSocket长连接
          currentUserId.value = generateUserId();
          initWebSocket();

          // 额外防护：如果连接断开，定时检查并重连
          setInterval(() => {
            if (userInfo.value.hasLogin && !isWsConnected.value) {
              initWebSocket();
            }
          }, 30000); // 30秒检查一次连接状态
        } else {
          // 登出，关闭连接
          closeWebSocket();
          msgList.value = [];
          unreadMsgCount.value = 0;
          showMsgDetail.value = false;
        }
      },
      {immediate: true}
  );
});

onUnmounted(() => {
  window.removeEventListener("resize", handleWindowResize);
  document.removeEventListener("click", handleClickOutside);

  // 组件销毁时关闭WebSocket
  closeWebSocket();
});
</script>

<style scoped>
/* 全局布局重置 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
  list-style: none;
  text-decoration: none;
}

.app-layout {
  width: 100%;
  height: 100vh;
  background-color: #f5f7fa;
  overflow: hidden;
}

.layout-container {
  display: flex;
  width: 100%;
  height: 100%;
}

/* 侧边栏样式 */
.layout-sidebar {
  width: 220px;
  height: 100%;
  background-color: #ffffff;
  border-right: 1px solid #e1e5eb;
  transition: all 0.3s ease;
  z-index: 10;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.sidebar-collapsed {
  width: 64px !important;
}

/* 侧边栏Logo */
.sidebar-logo {
  width: 100%;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid #e1e5eb;
  padding: 0 16px;
  background-color: #f8fafc;
  flex-shrink: 0;
}

.logo-wrapper {
  display: flex;
  align-items: center;
  width: 100%;
}

.logo-icon {
  width: 24px;
  height: 24px;
  margin-right: 12px;
}

.logo-icon-small {
  width: 24px;
  height: 24px;
}

.logo-text {
  font-size: 16px;
  font-weight: 700;
  color: #409eff;
  white-space: nowrap;
}

/* 侧边栏导航 */
.sidebar-nav {
  padding: 16px 0;
  flex: 1;
  overflow-y: auto;
}

.nav-list {
  width: 100%;
}

.nav-item {
  width: 100%;
  margin-bottom: 4px;
}

.nav-link {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  color: #667085;
  border-radius: 8px;
  margin: 0 8px;
  transition: all 0.2s ease;
  white-space: nowrap;
}

.nav-link:hover {
  background-color: #f0f7ff;
  color: #409eff;
}

.nav-link-active {
  background-color: #e6f0ff;
  color: #409eff;
  font-weight: 500;
  border-left: 2px solid #409eff;
  margin-left: 6px;
}

.nav-icon {
  width: 18px;
  height: 18px;
  margin-right: 12px;
  flex-shrink: 0;
}

.nav-text {
  font-size: 14px;
}

/* 侧边栏底部功能区 */
.sidebar-footer {
  width: 100%;
  padding: 16px 0;
  border-top: 1px solid #e1e5eb;
  background-color: #f8fafc;
  flex-shrink: 0;
}

.sidebar-function-item {
  width: 100%;
  display: flex;
  align-items: center;
  padding: 12px 16px;
  color: #667085;
  cursor: pointer;
  transition: all 0.2s ease;
  white-space: nowrap;
  position: relative;
}

.sidebar-function-item:hover {
  background-color: #f0f7ff;
  color: #409eff;
}

.function-text {
  font-size: 14px;
  margin-left: 12px;
}

/* 消息模块样式 */
.msg-wrapper {
  display: flex;
  align-items: center;
  position: relative;
}

.msg-btn {
  background: none;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 18px;
  height: 18px;
  position: relative;
}

.msg-icon {
  width: 18px;
  height: 18px;
}

/* 未读消息红点 */
.msg-badge {
  position: absolute;
  top: -2px;
  right: -2px;
  width: 16px;
  height: 16px;
  background-color: #f56c6c;
  color: #ffffff;
  font-size: 10px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 500;
}

/* 个人信息样式 */
.user-avatar {
  width: 18px;
  height: 18px;
  border-radius: 50%;
  border: 1px solid #e1e5eb;
}

.user-name {
  color: #2c3e50;
}

.user-info:hover {
  background-color: #f0f7ff;
  color: #409eff;
}

/* 退出登录/登录按钮 */
.logout-btn {
  background: none;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 18px;
  height: 18px;
}

.logout-icon {
  width: 18px;
  height: 18px;
}

/* 主内容区域 */
.layout-main {
  flex: 1;
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 简化版公共头部 */
.layout-header {
  height: 60px;
  background-color: #ffffff;
  border-bottom: 1px solid #e1e5eb;
  display: flex;
  align-items: center;
  padding: 0 20px;
  flex-shrink: 0;
  justify-content: space-between;
}

.header-toggle-btn {
  background: none;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 6px;
  transition: all 0.2s ease;
}

.header-toggle-btn:hover {
  background-color: #f0f7ff;
}

.toggle-icon {
  width: 20px;
  height: 20px;
}

/* 路由内容容器 */
.layout-content {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
  background-color: #f5f7fa;
}

/* 公共底部 */
.layout-footer {
  height: 40px;
  background-color: #ffffff;
  border-top: 1px solid #e1e5eb;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.footer-text {
  font-size: 12px;
  color: #667085;
}

/* 消息详情弹窗样式 */
.msg-detail-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 99999;
}

.msg-detail-popup {
  width: 500px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  overflow: hidden;
}

.msg-detail-header {
  padding: 16px;
  border-bottom: 1px solid #e1e5eb;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #f8fafc;
}

.msg-detail-header h3 {
  margin: 0;
  font-size: 16px;
  color: #2c3e50;
}

.close-btn {
  background: none;
  border: none;
  font-size: 18px;
  color: #909399;
  cursor: pointer;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all 0.2s;
}

.close-btn:hover {
  background-color: #f5f7fa;
  color: #667085;
}

.msg-detail-content {
  padding: 20px;
}

.detail-item {
  margin-bottom: 16px;
  display: flex;
  align-items: flex-start;
}

.detail-item label {
  width: 80px;
  font-size: 14px;
  color: #667085;
  font-weight: 500;
  flex-shrink: 0;
}

.detail-item span {
  font-size: 14px;
  color: #333;
}

.content-item {
  flex-direction: column;
}

.content-item label {
  margin-bottom: 8px;
}

.content-text {
  font-size: 14px;
  line-height: 1.8;
  color: #333;
  padding: 12px;
  background-color: #f8fafc;
  border-radius: 4px;
  min-height: 80px;
  white-space: pre-wrap;
  word-wrap: break-word;
}

/* 类型标签样式 */
.type-tag {
  padding: 2px 8px;
  border-radius: 4px;
  color: #fff;
  font-size: 12px;
}

.type-tag.system {
  background-color: #409eff;
}

.type-tag.detect {
  background-color: #67c23a;
}

.type-tag.image {
  background-color: #9c27b0;
}

.type-tag.warning {
  background-color: #e6a23c;
}

.type-tag.error {
  background-color: #f56c6c;
}

.type-tag.unknown {
  background-color: #909399;
}

/* 状态标签样式 */
.status-tag {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.status-tag.read {
  background-color: #e8f4ec;
  color: #67c23a;
}

.status-tag.unread {
  background-color: #f0f7ff;
  color: #409eff;
}

/* 弹窗底部按钮 */
.msg-detail-footer {
  padding: 16px;
  border-top: 1px solid #e1e5eb;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.btn {
  padding: 8px 16px;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  border: 1px solid transparent;
  transition: all 0.2s;
}

.cancel-btn {
  background-color: #f5f7fa;
  color: #667085;
  border-color: #e1e5eb;
}

.cancel-btn:hover {
  background-color: #e6e8eb;
}

.confirm-btn {
  background-color: #409eff;
  color: #fff;
}

.confirm-btn:hover {
  background-color: #337ecc;
}

/* 响应式适配（移动端） */
@media (max-width: 768px) {
  .layout-sidebar {
    width: 64px;
  }

  .logo-text, .nav-text, .function-text {
    display: none;
  }

  .layout-content {
    padding: 16px;
  }

  /* 移动端消息详情弹窗适配 */
  .msg-detail-popup {
    width: 90%;
    max-width: 400px;
  }
}
</style>