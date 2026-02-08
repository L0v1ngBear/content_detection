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
          <div class="sidebar-function-item msg-wrapper">
            <!-- 核心修改1：给按钮绑定点击事件 + .stop 阻止冒泡 -->
            <button class="msg-btn" @click.stop="toggleMsgPopup()">
              <svg viewBox="0 0 24 24" fill="#667085" class="msg-icon">
                <path
                    d="M20 2H4c-1.1 0-1.99.9-1.99 2L2 22l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm0 14H6l-2 2V4h16v12z"/>
              </svg>
              <!-- 未读消息红点（有未读时显示） -->
              <span class="msg-badge" v-if="unreadMsgCount > 0">{{ unreadMsgCount }}</span>
            </button>
            <!-- 核心修改2：给文字也绑定点击事件 + .stop -->
            <span class="function-text" v-if="!isSidebarCollapsed" @click.stop="toggleMsgPopup()">系统消息</span>
            <!-- 消息弹窗（切换显示/隐藏，适配侧边栏位置） -->
            <!-- 核心修改3：给弹窗加 @click.stop 防止点击弹窗内触发外层事件 -->
            <div class="msg-popup" v-if="isMsgPopupShow" @click.stop>
              <!-- 弹窗头部 -->
              <div class="msg-popup-header">
                <h3 class="popup-title">系统消息</h3>
                <button class="popup-clear-btn" @click="markAllAsRead()" :disabled="unreadMsgCount === 0">
                  标为已读
                </button>
              </div>
              <!-- 弹窗内容（消息列表） -->
              <div class="msg-popup-content">
                <div class="msg-empty" v-if="msgList.length === 0">
                  <p>暂无系统消息</p>
                </div>
                <div class="msg-item" v-for="(msg, index) in msgList" :key="index"
                     :class="{ 'msg-unread': !msg.isRead }">
                  <div class="msg-item-time">{{ msg.createTime }}</div>
                  <div class="msg-item-content">{{ msg.content }}</div>
                  <div class="msg-item-type" :class="msg.type">{{ getMsgTypeText(msg.type) }}</div>
                </div>
              </div>
              <!-- 弹窗底部 -->
              <div class="msg-popup-footer">
                <a href="javascript:;" class="msg-more-link" @click="viewMoreMsg()">查看更多消息</a>
              </div>
            </div>
          </div>

          <!-- 个人中心（迁移到侧边栏）- 新增点击跳转逻辑 -->
          <div
              class="sidebar-function-item user-info"
              v-if="getUserInfo.hasLogin"
              @click="handleUserInfoClick()"
              :style="{cursor: 'pointer'}"
          >
            <img src="https://picsum.photos/40/40" alt="用户头像" class="user-avatar"/>
            <span class="function-text user-name" v-if="!isSidebarCollapsed">
              {{ getUserInfo.username }}
            </span>
          </div>

          <!-- 核心修改：根据登录状态动态显示 登录/退出登录 -->
          <div
              class="sidebar-function-item"
              @click="getUserInfo.hasLogin ? handleLogout() : handleLogin()"
          >
            <button class="logout-btn">
              <svg viewBox="0 0 24 24" fill="#667085" class="logout-icon">
                <!-- 登录/退出登录 不同的图标 -->
                <path v-if="getUserInfo.hasLogin" d="M17 3v12h-4v-7H8v7H4V3h13m2-2H2v18h2V3h15v18h2V1z"/>
                <path v-else d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8zm-1-13h2v6h-2zm0 8h2v2h-2z"/>
              </svg>
            </button>
            <span class="function-text" v-if="!isSidebarCollapsed">
              {{ getUserInfo.hasLogin ? '退出登录' : '登录' }}
            </span>
          </div>
        </div>
      </aside>

      <!-- 主内容区域（头部仅保留侧边栏切换按钮，简化布局） -->
      <div class="layout-main">
        <!-- 简化版公共头部（仅保留折叠按钮） -->
        <header class="layout-header">
          <!-- 侧边栏折叠/展开按钮 -->
          <button class="header-toggle-btn" @click="toggleSidebar()">
            <svg viewBox="0 0 24 24" fill="#667085" class="toggle-icon">
              <path d="M3 18h18v-2H3v2zm0-5h18v-2H3v2zm0-7v2h18V6H3z"/>
            </svg>
          </button>
        </header>

        <!-- 路由内容容器（嵌入页面内容） -->
        <main class="layout-content">
          <router-view/>
        </main>

        <!-- 公共底部（替换为AI检测平台版权信息） -->
        <footer class="layout-footer">
          <p class="footer-text">© 2026 AI 内容检测平台 - 版权所有</p>
        </footer>
      </div>
    </div>
  </div>
</template>

<script setup>
import {ref, computed, onMounted, onUnmounted} from 'vue';
import {useRouter} from 'vue-router';
import {ElMessage} from 'element-plus';
// 注：request 请确保你的项目中存在该工具类，若不存在可注释或替换为真实请求逻辑
import request from '../utils/request';

// --------------- 路由实例 ---------------
const router = useRouter();

// --------------- 响应式数据 ---------------
// 侧边栏折叠/展开状态
const isSidebarCollapsed = ref(false);
// 窗口宽度（用于响应式适配）
const windowWidth = ref(window.innerWidth);

// 新增：消息相关响应式数据
const isMsgPopupShow = ref(false); // 消息弹窗显示/隐藏
const msgList = ref([]); // 消息列表
const unreadMsgCount = ref(0); // 未读消息数量
// 新增：存储从接口获取的用户名
const apiUsername = ref('');

// --------------- 侧边栏导航菜单配置（替换为AI检测平台核心功能） ---------------
const navMenus = ref([
  {
    path: "/front/home", // 对应首页路由（完整路径）
    name: "检测首页",
    iconPath: "M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8h5z" // 首页图标
  },
  {
    path: "/front/picture", // 图片检测路由
    name: "图片AI检测",
    iconPath: "M21 19V5c0-1.1-.9-2-2-2H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2zM8.5 13.5l2.5 3.01L14.5 12l4.5 6H5l3.5-4.5z" // 图片图标
  },
  {
    path: "/front/video", // 视频检测路由
    name: "视频AI检测",
    iconPath: "M17 10.5V7c0-.55-.45-1-1-1H4c-.55 0-1 .45-1 1v10c0 .55.45 1 1 1h12c.55 0 1-.45 1-1v-3.5l4 4v-11l-4 4z" // 视频图标
  },
  {
    path: "/front/history", // 历史记录路由
    name: "检测历史记录",
    iconPath: "M13 3c-4.97 0-9 4.03-9 9H1l3.89 3.89.07.14L9 21l4-4 4 4v-6.17l-2-2V13l2.81-2.81c.39-.39.39-1.02 0-1.41l-2.59-2.59c.39-.39 1.02-.39 1.41 0L12 10.59 9.19 7.79c-.39-.39-1.02-.39-1.41 0L5 10.59c-.39.39-.39 1.02 0 1.41L7.81 14 5 16.81c-.39.39-.39 1.02 0 1.41l2.59 2.59c.39.39 1.02.39 1.41 0L12 17.41l2.81 2.81c.39.39 1.02.39 1.41 0l2.59-2.59c.39.39.39 1.02 0 1.41L14.19 14 17 11.19V13c0 .55.45 1 1 1h4c.55 0 1-.45 1-1v-6c0-.55-.45-1-1-1h-4c-.55 0-1 .45-1 1v1.59L13 7.41 10.19 10.21c-.39-.39-1.02-.39-1.41 0L6 7.41c-.39.39-.39 1.02 0 1.41l2.59 2.59c.39.39 1.02.39 1.41 0L12 9.41 14.81 6.6c.39-.39 1.02-.39 1.41 0l2.59 2.59c.39.39.39 1.02 0 1.41L16.81 9 19 11.19V5h-2v1.59L13 9.41 10.19 6.6c-.39-.39-1.02-.39-1.41 0L6 9.41c-.39.39-.39 1.02 0 1.41l2.59 2.59c.39.39 1.02.39 1.41 0L12 16.59 14.81 19.4c.39.39 1.02.39 1.41 0l2.59-2.59c.39.39.39 1.02 0-1.41L16.81 13H19v6h-2v-1.59L13 10.59 10.19 13.4c-.39.39-1.02.39-1.41 0L6 10.59c-.39.39-.39 1.02 0 1.41l2.59 2.59c.39.39 1.02.39 1.41 0L12 9.41 14.81 6.6c.39-.39 1.02-.39 1.41 0l2.59 2.59c.39.39.39 1.02 0 1.41L16.81 12H19v-6h-2v1.59L13 7.41z" // 历史记录图标
  },
  {
    path: "/front/setting", // 系统设置路由
    name: "平台设置",
    iconPath: "M19.14 12.94c.04-.3.06-.61.06-.94s-.02-.64-.07-.94l2.03-1.58c.18-.14.23-.41.12-.61l-1.92-3.32c-.12-.22-.37-.29-.59-.22l-2.39.96c-.5-.38-1.03-.7-1.62-.94L14 2.81c-.04-.24-.24-.41-.48-.41h-4c-.24 0-.43.17-.47.41L9.25 5.35c-.59.24-1.13.57-1.62.94l-2.39-.96c-.22-.08-.47 0-.59.22L.74 10.4c-.12.22-.07.47.12.61l2.03 1.58c-.05.3-.07.63-.07.94s.02.64.07.94l-2.03 1.58c-.18.14-.23.41-.12.61l1.92 3.32c.12.22.37.29.59.22l2.39-.96c.5.38 1.03.7 1.62.94L10 21.19c.04.24.24.41.48.41h4c.24 0 .43-.17.47-.41l.75-2.54c.59-.24 1.13-.57 1.62-.94l2.39.96c.22.08.47 0 .59-.22l1.92-3.32c.12-.22.07-.47-.12-.61l-2.03-1.58zM12 15.6c-1.98 0-3.6-1.62-3.6-3.6s1.62-3.6 3.6-3.6 3.6 1.62 3.6 3.6-1.62 3.6-3.6 3.6z" // 设置图标
  }
]);

// --------------- 计算属性：分层获取用户信息 ---------------
const getUserInfo = computed(() => {
  const accessToken = localStorage.getItem("accessToken");
  if (!accessToken) {
    return {
      username: "",
      hasLogin: false
    };
  }

  // 优先级1：本地存储的rememberedUsername
  const rememberedUsername = localStorage.getItem("rememberedUsername");
  if (rememberedUsername) {
    return {
      username: rememberedUsername,
      hasLogin: true
    };
  }

  // 优先级2：从接口获取的用户名
  if (apiUsername.value) {
    return {
      username: apiUsername.value,
      hasLogin: true
    };
  }

  // 最终兜底：未知用户
  return {
    username: '未知用户',
    hasLogin: true
  };
});

// --------------- 新增：从接口获取用户名 ---------------
const fetchUsername = async () => {
  try {
    const res = await request({
      url: "/api/getUserName", // 你的获取用户名接口
      method: "get"
    });
    // 可根据实际接口返回格式调整解析逻辑
    if (res.code === 200 && res.data) {
      apiUsername.value = res.data;
      // 可选：将接口获取的用户名存入本地，下次无需重复请求
      // localStorage.setItem("rememberedUsername", res.data);
      ElMessage.success(`欢迎回来，${res.data}！`);
    }
  } catch (error) {
    console.error("获取用户名失败：", error);
    apiUsername.value = '';
    ElMessage.error('获取用户信息失败，请刷新页面重试');
  }
};

// --------------- 侧边栏折叠/展开切换 ---------------
const toggleSidebar = () => {
  isSidebarCollapsed.value = !isSidebarCollapsed.value;
  ElMessage.info(isSidebarCollapsed.value ? '侧边栏已折叠' : '侧边栏已展开');
};

// --------------- 退出登录逻辑 ---------------
const handleLogout = async () => {
  try {
    // 替换原生confirm为Element Plus的确认弹窗
    await ElMessageBox.confirm(
        '确定要退出登录吗？', // 提示内容
        '退出确认', // 弹窗标题
        {
          confirmButtonText: '确认退出',
          cancelButtonText: '取消',
          type: 'warning', // 警告类型，显示黄色图标
          center: true // 内容居中显示
        }
    );

    // 1. 清除本地存储的所有登录相关数据
    localStorage.removeItem("accessToken");
    localStorage.removeItem("refreshToken");
    localStorage.removeItem("tokenExpireTime");
    localStorage.removeItem("tokenExpireTimestamp");
    localStorage.removeItem("rememberedUsername");
    // 清空接口获取的用户名
    apiUsername.value = '';

    // 2. 调用后端退出登录接口（可选，根据项目需求保留）
    const refreshToken = localStorage.getItem("refreshToken");
    if (refreshToken) {
      try {
        await request.post("/auth/logout", { refreshToken });
        console.log("后端退出登录成功");
        ElMessage.success('退出登录成功');
      } catch (err) {
        console.error("后端退出登录失败：", err);
        ElMessage.warning('退出登录成功（后端状态未同步）');
      }
    } else {
      ElMessage.success('退出登录成功');
    }

    // 3. 跳转到登录页
    router.push("/login");

  } catch (error) {
    console.log("用户取消退出登录");
  }
};

// --------------- 新增：登录按钮点击逻辑 ---------------
const handleLogin = () => {
  // 跳转到登录页面
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

// --------------- 个人中心点击跳转逻辑 ---------------
const handleUserInfoClick = () => {
  // 跳转到平台设置页面（对应导航菜单中的 /front/setting 路由）
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

// --------------- 新增：消息相关核心方法 ---------------
// 1. 切换消息弹窗显示/隐藏
const toggleMsgPopup = (e) => {
  // 阻止事件冒泡，避免弹窗意外关闭
  if (e) e.stopPropagation();
  isMsgPopupShow.value = !isMsgPopupShow.value;
  // 弹窗显示时，加载消息列表（避免重复请求）
  if (isMsgPopupShow.value && msgList.value.length === 0) {
    loadMsgList();
  }
};

// 2. 加载消息列表（调用后端接口）
const loadMsgList = async () => {
  try {
    ElMessage.loading({
      message: '正在加载消息列表...',
      duration: 0,
      id: 'msg-loading'
    });
    const response = await request({
      url: "/api/msg/list",
      method: "get",
      params: {pageSize: 10} // 只加载最新10条消息
    });
    ElMessage.close('msg-loading');
    const resData = response.data || [];
    msgList.value = resData;
    // 计算未读消息数量
    unreadMsgCount.value = resData.filter(msg => !msg.isRead).length;

    if (unreadMsgCount.value > 0) {
      ElMessage.info(`您有${unreadMsgCount.value}条未读消息`);
    } else if (resData.length === 0) {
      ElMessage.info('暂无系统消息');
    }
  } catch (error) {
    ElMessage.close('msg-loading');
    console.error("加载消息列表失败：", error);
    msgList.value = [];
    ElMessage.error('加载消息失败，请稍后重试');
  }
};

// 3. 标记所有消息为已读
const markAllAsRead = async (e) => {
  if (e) e.stopPropagation();
  if (unreadMsgCount.value === 0) {
    ElMessage.info('暂无未读消息');
    return;
  }
  try {
    ElMessage.loading({
      message: '正在标记已读...',
      duration: 0,
      id: 'msg-read-loading'
    });
    await request({
      url: "/api/msg/all-read",
      method: "post"
    });
    ElMessage.close('msg-read-loading');
    // 前端更新状态
    msgList.value.forEach(msg => {
      msg.isRead = true;
    });
    unreadMsgCount.value = 0;
    ElMessage.success('所有消息已标记为已读');
  } catch (error) {
    ElMessage.close('msg-read-loading');
    console.error("标记所有消息为已读失败：", error);
    ElMessage.error('标记已读失败，请稍后重试');
  }
};

// 4. 获取消息类型文本（格式化显示）
const getMsgTypeText = (type) => {
  switch (type) {
    case "system":
      return "系统通知";
    case "detect":
      return "检测结果";
    case "warning":
      return "预警提示";
    case "error":
      return "错误通知";
    default:
      return "未知消息";
  }
};

// 5. 查看更多消息
const viewMoreMsg = () => {
  ElMessage.info('即将跳转到消息中心页面');
  // 这里可以添加跳转到完整消息列表页面的逻辑
  // router.push("/front/message-center");
};

// --------------- 响应式窗口适配（移动端自动折叠侧边栏） ---------------
const handleWindowResize = () => {
  windowWidth.value = window.innerWidth;
  // 移动端（小于768px）自动折叠侧边栏
  if (windowWidth.value < 768) {
    isSidebarCollapsed.value = true;
  }
};

// --------------- 点击页面空白处关闭消息弹窗（优化体验） ---------------
const handleClickOutside = (e) => {
  if (isMsgPopupShow.value && !e.target.closest(".msg-wrapper")) {
    isMsgPopupShow.value = false;
  }
};

// --------------- 组件生命周期 ---------------
onMounted(() => {
  // 初始化窗口适配
  handleWindowResize();
  // 监听窗口大小变化
  window.addEventListener("resize", handleWindowResize);
  // 监听点击空白处关闭弹窗
  document.addEventListener("click", handleClickOutside);

  // 登录状态下，先检查本地用户名，为空则调用接口
  const accessToken = localStorage.getItem("accessToken");
  const rememberedUsername = localStorage.getItem("rememberedUsername");
  if (accessToken && !rememberedUsername) {
    fetchUsername(); // 调用/api/getusername获取用户名
  }

  // 初始化加载未读消息数量（不加载完整列表，提升性能）
  if (getUserInfo.value.hasLogin) {
    try {
      request.get("/api/msg/unread-count").then(res => {
        unreadMsgCount.value = res.data || 0;
        if (unreadMsgCount.value > 0) {
          ElMessage.info(`您有${unreadMsgCount.value}条未读系统消息`);
        }
      });
    } catch (error) {
      console.error("加载未读消息数量失败：", error);
    }
  }
});

onUnmounted(() => {
  // 移除窗口大小变化监听
  window.removeEventListener("resize", handleWindowResize);
  // 移除点击空白处监听
  document.removeEventListener("click", handleClickOutside);
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

/* 侧边栏样式（优化视觉，贴合AI检测平台） */
.layout-sidebar {
  width: 220px;
  height: 100%;
  background-color: #ffffff;
  border-right: 1px solid #e1e5eb;
  transition: all 0.3s ease;
  overflow: hidden;
  z-index: 10;
  display: flex;
  flex-direction: column;
  justify-content: space-between; /* 让底部功能区固定在侧边栏底部 */
}

/* 侧边栏折叠状态 */
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
  background-color: #f8fafc; /* 轻微背景色，提升品牌感 */
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
  color: #409eff; /* 主色调，突出平台名称 */
  white-space: nowrap;
}

/* 侧边栏导航（优化hover样式，贴合AI检测平台） */
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
  margin-bottom: 4px; /* 缩小间距，更紧凑 */
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
  background-color: #f0f7ff; /* 主色调浅背景，贴合平台风格 */
  color: #409eff;
}

.nav-link-active {
  background-color: #e6f0ff; /* 激活态加深，突出选中 */
  color: #409eff;
  font-weight: 500;
  border-left: 2px solid #409eff; /* 左侧竖线，强化激活态 */
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

/* 侧边栏底部功能区（核心：整合所有迁移过来的功能） */
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
  position: relative; /* 为消息弹窗提供定位上下文 */
}

.sidebar-function-item:hover {
  background-color: #f0f7ff;
  color: #409eff;
}

.function-text {
  font-size: 14px;
  margin-left: 12px;
}

/* 消息模块样式（适配侧边栏） */
.msg-wrapper {
  display: flex;
  align-items: center;
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

/* 消息弹窗（适配侧边栏，向右展开） */
.msg-popup {
  position: absolute;
  top: 0;
  left: 100%; /* 从侧边栏右侧展开 */
  margin-left: 8px;
  width: 380px;
  background-color: #ffffff;
  border: 1px solid #e1e5eb;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  z-index: 100;
  overflow: hidden;
}

/* 弹窗头部 */
.msg-popup-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-bottom: 1px solid #e1e5eb;
}

.popup-title {
  font-size: 14px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0;
}

.popup-clear-btn {
  background-color: #f0f7ff;
  color: #409eff;
  border: none;
  border-radius: 4px;
  padding: 4px 8px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.popup-clear-btn:hover:not(:disabled) {
  background-color: #e6f0ff;
}

.popup-clear-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 弹窗内容 */
.msg-popup-content {
  max-height: 300px;
  overflow-y: auto;
}

/* 空消息状态 */
.msg-empty {
  padding: 32px 16px;
  text-align: center;
}

.msg-empty p {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

/* 消息项 */
.msg-item {
  padding: 12px 16px;
  border-bottom: 1px solid #f5f7fa;
  transition: background-color 0.2s ease;
}

.msg-item:hover {
  background-color: #fafbfc;
}

/* 未读消息样式 */
.msg-unread {
  background-color: #f0f7ff;
  border-left: 2px solid #409eff;
}

.msg-item-time {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

.msg-item-content {
  font-size: 13px;
  color: #667085;
  margin-bottom: 4px;
  line-height: 1.4;
}

.msg-item-type {
  display: inline-block;
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 4px;
  color: #ffffff;
}

/* 消息类型配色 */
.msg-item-type.system {
  background-color: #409eff;
}

.msg-item-type.detect {
  background-color: #67c23a;
}

.msg-item-type.warning {
  background-color: #e6a23c;
}

.msg-item-type.error {
  background-color: #f56c6c;
}

/* 弹窗底部 */
.msg-popup-footer {
  padding: 12px 16px;
  text-align: center;
  border-top: 1px solid #e1e5eb;
}

.msg-more-link {
  font-size: 13px;
  color: #409eff;
  transition: all 0.2s ease;
  cursor: pointer;
}

.msg-more-link:hover {
  color: #337ecc;
  text-decoration: underline;
}

/* 个人信息样式（适配侧边栏）- 新增hover样式 */
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

/* 退出登录/登录按钮（适配侧边栏） */
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

/* 简化版公共头部（仅保留折叠按钮，不占用多余空间） */
.layout-header {
  height: 60px;
  background-color: #ffffff;
  border-bottom: 1px solid #e1e5eb;
  display: flex;
  align-items: center;
  padding: 0 20px;
  flex-shrink: 0;
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
  background-color: #f0f7ff; /* 与侧边栏hover风格统一 */
}

.toggle-icon {
  width: 20px;
  height: 20px;
}

/* 路由内容容器（优化内边距，更舒适） */
.layout-content {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
  background-color: #f5f7fa;
}

/* 公共底部（优化背景色，与头部统一） */
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

/* 响应式适配（移动端，优化显示） */
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

  /* 移动端消息弹窗适配 */
  .msg-popup {
    width: 300px;
    left: 64px;
    top: 0;
    margin-left: 0;
  }
}
</style>