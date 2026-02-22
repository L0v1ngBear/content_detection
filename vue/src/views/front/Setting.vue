<template>
  <div class="setting-container">
    <!-- 页面标题 -->
    <div class="setting-page-title">
      <h2>系统设置</h2>
      <p>管理个人信息、检测规则与存储配置，个性化你的使用体验</p>
    </div>

    <!-- 选项卡切换 -->
    <div class="setting-tabs">
      <div class="tabs-nav">
        <button
            class="tab-nav-item"
            :class="{ active: activeTab === 'userInfo' }"
            @click="activeTab = 'userInfo'"
        >
          个人信息
        </button>
        <button
            class="tab-nav-item"
            :class="{ active: activeTab === 'password' }"
            @click="activeTab = 'password'"
        >
          密码修改
        </button>
        <!-- 移除存储配置选项卡 -->
        <!-- 新增API访问密钥选项卡 -->
        <button
            class="tab-nav-item"
            :class="{ active: activeTab === 'apiKey' }"
            @click="activeTab = 'apiKey'"
        >
          API访问密钥
        </button>
      </div>

      <!-- 个人信息面板 - 已移除用户ID -->
      <div class="tab-panel" v-if="activeTab === 'userInfo'">
        <div class="setting-form-box">
          <div class="form-item">
            <label class="form-label">用户名：</label>
            <input
                type="text"
                v-model="userInfoForm.username"
                class="form-input"
                placeholder="请输入用户名（2-20个字符）"
            />
          </div>

          <div class="form-item">
            <label class="form-label">手机号：</label>
            <input
                type="tel"
                v-model="userInfoForm.phone"
                class="form-input"
                placeholder="请输入11位手机号"
            />
          </div>

          <div class="form-item">
            <label class="form-label">邮箱：</label>
            <input
                type="email"
                v-model="userInfoForm.email"
                class="form-input"
                placeholder="请输入有效邮箱地址"
            />
          </div>

          <div class="form-item">
            <label class="form-label">头像：</label>
            <div class="avatar-upload-box">
              <img
                  v-if="userInfoForm.avatar"
                  :src="userInfoForm.avatar"
                  class="avatar-preview"
                  alt="头像"
              />
              <div class="avatar-placeholder" v-else>
                <span class="placeholder-text">上传头像</span>
              </div>
              <input
                  type="file"
                  class="avatar-input"
                  accept="image/jpeg,image/png"
                  @change="handleAvatarUpload"
              />
              <label class="avatar-upload-btn" for="avatar-upload">选择图片</label>
            </div>
          </div>

          <div class="form-actions">
            <button class="btn submit-btn" @click="submitUserInfo">保存修改</button>
          </div>
        </div>
      </div>

      <!-- 密码修改面板 -->
      <div class="tab-panel" v-if="activeTab === 'password'">
        <div class="setting-form-box">
          <div class="form-item">
            <label class="form-label">原密码：</label>
            <input
                type="password"
                v-model="passwordForm.oldPassword"
                class="form-input"
                placeholder="请输入当前登录密码"
            />
          </div>

          <div class="form-item">
            <label class="form-label">新密码：</label>
            <input
                type="password"
                v-model="passwordForm.newPassword"
                class="form-input"
                placeholder="请输入6-20位新密码"
            />
          </div>

          <div class="form-item">
            <label class="form-label">确认新密码：</label>
            <input
                type="password"
                v-model="passwordForm.confirmPassword"
                class="form-input"
                placeholder="请再次输入新密码"
            />
          </div>

          <div class="form-actions">
            <button class="btn submit-btn" @click="submitPassword">修改密码</button>
          </div>
        </div>
      </div>

      <!-- API访问密钥面板 -->
      <div class="tab-panel" v-if="activeTab === 'apiKey'">
        <!-- 密钥生成区域 -->
        <div class="api-key-generate-box">
          <div class="generate-header">
            <h3>生成新的API密钥</h3>
            <p class="generate-tip">API密钥用于调用系统检测接口，生成后请妥善保管，仅显示一次！</p>
          </div>

          <div class="form-item">
            <label class="form-label">密钥名称：</label>
            <input
                type="text"
                v-model="apiKeyForm.keyName"
                class="form-input"
                placeholder="请输入密钥名称（如：业务系统对接）"
            />
          </div>

          <div class="form-item">
            <label class="form-label">密钥有效期：</label>
            <select v-model="apiKeyForm.expireDays" class="form-select">
              <option value="7">7天</option>
              <option value="30">30天</option>
              <option value="90">90天</option>
              <option value="0">永久有效</option>
            </select>
          </div>

          <div class="form-actions">
            <button class="btn submit-btn" @click="generateApiKey" :disabled="generating">
              <span v-if="!generating">生成密钥</span>
              <span v-if="generating">生成中...</span>
            </button>
          </div>

          <!-- 密钥生成成功弹窗 -->
          <div class="api-key-modal" v-if="showKeyModal">
            <div class="modal-content">
              <div class="modal-header">
                <h4>密钥生成成功</h4>
                <button class="close-btn" @click="showKeyModal = false">×</button>
              </div>
              <div class="modal-body">
                <div class="key-item">
                  <label>API Key：</label>
                  <div class="key-value">
                    {{ newApiKey.accessKey }}
                    <button class="copy-btn" @click="copyToClipboard(newApiKey.accessKey)">复制</button>
                  </div>
                </div>
                <div class="key-item">
                  <label>Secret Key：</label>
                  <div class="key-value">
                    {{ newApiKey.secretKey }}
                    <button class="copy-btn" @click="copyToClipboard(newApiKey.secretKey)">复制</button>
                  </div>
                </div>
                <p class="modal-tip">⚠️ 请妥善保管你的Secret Key，系统仅显示一次，丢失无法找回！</p>
              </div>
              <div class="modal-footer">
                <button class="btn confirm-btn" @click="showKeyModal = false">我已知晓</button>
              </div>
            </div>
          </div>
        </div>

        <!-- 密钥列表区域 -->
        <div class="api-key-list-box" style="margin-top: 32px;">
          <div class="list-header">
            <h3>已生成的API密钥</h3>
            <p class="list-tip">当前共有 {{ apiKeyList.length }} 个API密钥</p>
          </div>

          <!-- 空状态 -->
          <div class="api-key-empty" v-if="apiKeyList.length === 0 && !loading">
            <svg class="empty-icon" viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg">
              <path fill="#c0c4cc"
                    d="M864 160H160c-17.7 0-32 14.3-32 32v736c0 17.7 14.3 32 32 32h704c17.7 0 32-14.3 32-32V192c0-17.7-14.3-32-32-32zM640 736c0 4.4-3.6 8-8 8H416c-4.4 0-8-3.6-8-8v-48c0-4.4 3.6-8 8-8h216c4.4 0 8 3.6 8 8v48zm0-160c0 4.4-3.6 8-8 8H416c-4.4 0-8-3.6-8-8v-48c0-4.4 3.6-8 8-8h216c4.4 0 8 3.6 8 8v48zm0-160c0 4.4-3.6 8-8 8H416c-4.4 0-8-3.6-8-8v-48c0-4.4 3.6-8 8-8h216c4.4 0 8 3.6 8 8v48zm192-304H224v-64h608v64z"></path>
            </svg>
            <p class="empty-text">暂无API密钥，请点击上方按钮生成</p>
          </div>

          <!-- 列表内容 -->
          <div class="api-key-list" v-if="apiKeyList.length > 0 && !loading">
            <div class="list-header-row">
              <div class="list-col col-name">密钥名称</div>
              <div class="list-col col-access-key">Access Key</div>
              <div class="list-col col-status">状态</div>
              <div class="list-col col-create-time">创建时间</div>
              <div class="list-col col-expire-time">过期时间</div>
              <div class="list-col col-operation">操作</div>
            </div>
            <div class="list-body">
              <div class="list-row" v-for="(key, index) in apiKeyList" :key="index">
                <div class="list-col col-name">{{ key.keyName || '未命名' }}</div>
                <div class="list-col col-access-key">{{ maskAccessKey(key.accessKey) }}</div>
                <div class="list-col col-status">
                  <span class="status-tag" :class="key.status === 'active' ? 'tag-active' : 'tag-disabled'">
                    {{ key.status === 'active' ? '启用中' : '已禁用' }}
                  </span>
                </div>
                <div class="list-col col-create-time">{{ formatTime(key.createTime) }}</div>
                <div class="list-col col-expire-time">
                  {{ key.expireDays === 0 ? '永久有效' : formatTime(key.expireTime) }}
                </div>
                <div class="list-col col-operation">
                  <button
                      class="oper-btn toggle-btn"
                      @click="toggleKeyStatus(key.id)"
                      :disabled="loading"
                  >
                    {{ key.status === 'active' ? '禁用' : '启用' }}
                  </button>
                  <button
                      class="oper-btn reset-btn"
                      @click="resetApiKey(key.id)"
                      :disabled="loading || key.status === 'disabled'"
                  >
                    重置
                  </button>
                  <button
                      class="oper-btn delete-btn"
                      @click="deleteApiKey(key.id)"
                      :disabled="loading"
                  >
                    删除
                  </button>
                </div>
              </div>
            </div>
          </div>

          <!-- 加载状态 -->
          <div class="history-loading" v-if="loading">
            <span class="loading-text">正在加载API密钥列表...</span>
            <div class="loading-spinner"></div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue';
import request from '../../utils/request';
import { ElMessage, ElMessageBox } from 'element-plus';
import { useRouter } from 'vue-router';

export default {
  name: "Setting",
  setup() {
    // 路由实例
    const router = useRouter();

    // 选项卡激活状态
    const activeTab = ref('userInfo');

    // 加载状态
    const loading = ref(false);
    const generating = ref(false); // 密钥生成中状态

    // 个人信息表单 - 已移除userId字段
    const userInfoForm = reactive({
      username: '',
      phone: '',
      email: '',
      avatar: ''
    });

    // 密码修改表单
    const passwordForm = reactive({
      oldPassword: '',
      newPassword: '',
      confirmPassword: ''
    });

    // 移除存储配置相关代码

    // API密钥相关
    const apiKeyForm = reactive({
      keyName: '',
      expireDays: 30 // 默认30天有效期
    });
    const apiKeyList = ref([]); // 密钥列表
    const showKeyModal = ref(false); // 密钥生成成功弹窗
    const newApiKey = ref({ accessKey: '', secretKey: '' }); // 新生成的密钥

    // 初始化页面数据
    const initData = async () => {
      try {
        loading.value = true;

        // 获取个人信息
        const userRes = await request({
          url: 'user/info',
          method: 'get',
          timeout: 10000
        });
        // 赋值时排除userId，仅赋值需要的字段
        const {username, phone, email, avatar} = userRes.data;
        Object.assign(userInfoForm, {username, phone, email, avatar});

        // 移除存储配置相关请求

        // 加载API密钥列表
        await loadApiKeyList();

      } catch (error) {
        console.error('初始化设置页失败：', error);
        const errorCode = error.response?.status || error.code;
        if (errorCode === 401) {
          ElMessage.warning('您的登录状态已失效，请重新登录');
          localStorage.removeItem('accessToken');
          router.push('/login');
        } else {
          ElMessage.error('加载配置失败，请刷新页面重试');
        }
      } finally {
        loading.value = false;
      }
    };

    // 加载API密钥列表
    const loadApiKeyList = async () => {
      try {
        const res = await request({
          url: '/api/user/api-key/list',
          method: 'get',
          timeout: 10000
        });
        apiKeyList.value = res.data || [];
      } catch (error) {
        console.error('加载API密钥列表失败：', error);
        ElMessage.error('加载API密钥列表失败，请重试');
      }
    };

    // 生成API密钥
    const generateApiKey = async () => {
      if (!apiKeyForm.keyName) {
        ElMessage.warning('请输入密钥名称');
        return;
      }

      try {
        generating.value = true;
        const res = await request({
          url: '/api/user/api-key/generate',
          method: 'post',
          data: apiKeyForm,
          timeout: 15000
        });
        if (res.code === 200) {
          newApiKey.value = res.data;
          showKeyModal.value = true;
          // 清空表单
          apiKeyForm.keyName = '';
          apiKeyForm.expireDays = 30;
          // 重新加载列表
          await loadApiKeyList();
        } else {
          ElMessage.error(res.msg || '生成API密钥失败');
        }
      } catch (error) {
        console.error('生成API密钥失败：', error);
        ElMessage.error('生成失败，请重试');
      } finally {
        generating.value = false;
      }
    };

    // 复制到剪贴板
    const copyToClipboard = (text) => {
      navigator.clipboard.writeText(text).then(() => {
        ElMessage.success('复制成功！');
      }).catch(() => {
        // 降级方案：创建临时input复制
        const input = document.createElement('input');
        input.value = text;
        document.body.appendChild(input);
        input.select();
        document.execCommand('copy');
        document.body.removeChild(input);
        ElMessage.success('复制成功！');
      });
    };

    // 掩码处理AccessKey（只显示前8位和后4位）
    const maskAccessKey = (accessKey) => {
      if (!accessKey) return '';
      return accessKey.substring(0, 8) + '****************' + accessKey.substring(accessKey.length - 4);
    };

    // 格式化时间
    const formatTime = (timestamp) => {
      if (!timestamp) return '未知时间';
      const date = new Date(timestamp);
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
      });
    };

    // 切换密钥状态（启用/禁用）
    const toggleKeyStatus = async (keyId) => {
      try {
        loading.value = true;
        const res = await request({
          url: `/api/user/api-key/toggle/${keyId}`,
          method: 'post',
          timeout: 10000
        });
        if (res.code === 200) {
          ElMessage.success('密钥状态修改成功');
          await loadApiKeyList();
        } else {
          ElMessage.error(res.msg || '修改失败');
        }
      } catch (error) {
        console.error('切换密钥状态失败：', error);
        ElMessage.error('修改失败，请重试');
      } finally {
        loading.value = false;
      }
    };

    // 重置API密钥
    const resetApiKey = async (keyId) => {
      ElMessageBox.confirm(
          '重置密钥将生成新的Secret Key，原密钥将立即失效，是否确认？',
          '确认重置',
          {
            confirmButtonText: '确认',
            cancelButtonText: '取消',
            type: 'warning'
          }
      ).then(async () => {
        try {
          loading.value = true;
          const res = await request({
            url: `/api/user/api-key/reset/${keyId}`,
            method: 'post',
            timeout: 10000
          });
          if (res.code === 200) {
            ElMessage.success('密钥重置成功，新的Secret Key已发送至你的邮箱');
            await loadApiKeyList();
          } else {
            ElMessage.error(res.msg || '重置失败');
          }
        } catch (error) {
          console.error('重置密钥失败：', error);
          ElMessage.error('重置失败，请重试');
        } finally {
          loading.value = false;
        }
      }).catch(() => {
        ElMessage.info('已取消重置');
      });
    };

    // 删除API密钥
    const deleteApiKey = async (keyId) => {
      ElMessageBox.confirm(
          '删除密钥后将无法恢复，相关接口调用将失效，是否确认删除？',
          '确认删除',
          {
            confirmButtonText: '删除',
            cancelButtonText: '取消',
            type: 'error'
          }
      ).then(async () => {
        try {
          loading.value = true;
          const res = await request({
            url: `/api/user/api-key/delete/${keyId}`,
            method: 'post',
            timeout: 10000
          });
          if (res.code === 200) {
            ElMessage.success('密钥删除成功');
            await loadApiKeyList();
          } else {
            ElMessage.error(res.msg || '删除失败');
          }
        } catch (error) {
          console.error('删除密钥失败：', error);
          ElMessage.error('删除失败，请重试');
        } finally {
          loading.value = false;
        }
      }).catch(() => {
        ElMessage.info('已取消删除');
      });
    };

    // 头像上传处理
    const handleAvatarUpload = (e) => {
      const file = e.target.files[0];
      if (!file) return;

      // 校验文件类型和大小
      const isImage = file.type === 'image/jpeg' || file.type === 'image/png';
      const isLt2M = file.size / 1024 / 1024 < 2;

      if (!isImage) {
        ElMessage.error('头像只能是 JPG/PNG 格式！');
        return;
      }
      if (!isLt2M) {
        ElMessage.error('头像大小不能超过 2MB！');
        return;
      }

      // 构建FormData上传
      const formData = new FormData();
      formData.append('file', file);

      request({
        url: '/api/upload/avatar',
        method: 'post',
        data: formData,
        headers: {
          'Content-Type': 'multipart/form-data'
        },
        timeout: 15000
      }).then(res => {
        userInfoForm.avatar = res.data.url;
        ElMessage.success('头像上传成功！');
        // 清空input值，支持重复上传同一文件
        e.target.value = '';
      }).catch(error => {
        console.error('头像上传失败：', error);
        ElMessage.error('头像上传失败，请重试');
      });
    };

    // 提交个人信息修改
    const submitUserInfo = async () => {
      // 简单表单校验
      if (!userInfoForm.username || userInfoForm.username.length < 2 || userInfoForm.username.length > 20) {
        ElMessage.warning('请输入2-20个字符的用户名');
        return;
      }
      if (userInfoForm.phone && !/^1[3-9]\d{9}$/.test(userInfoForm.phone)) {
        ElMessage.warning('请输入正确的手机号');
        return;
      }
      if (userInfoForm.email && !/^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/.test(userInfoForm.email)) {
        ElMessage.warning('请输入正确的邮箱地址');
        return;
      }

      try {
        loading.value = true;
        const res = await request({
          url: '/api/user/update',
          method: 'post',
          data: userInfoForm,
          timeout: 10000
        });
        if (res.code === 200) {
          ElMessage.success('个人信息修改成功！');
        } else {
          ElMessage.error(res.msg || '修改失败');
        }
      } catch (error) {
        console.error('修改个人信息失败：', error);
        ElMessage.error('修改失败，请重试');
      } finally {
        loading.value = false;
      }
    };

    // 提交密码修改
    const submitPassword = async () => {
      // 表单校验
      if (!passwordForm.oldPassword) {
        ElMessage.warning('请输入原密码');
        return;
      }
      if (!passwordForm.newPassword || passwordForm.newPassword.length < 6 || passwordForm.newPassword.length > 20) {
        ElMessage.warning('请输入6-20位的新密码');
        return;
      }
      if (passwordForm.newPassword !== passwordForm.confirmPassword) {
        ElMessage.warning('两次输入的密码不一致');
        return;
      }

      try {
        loading.value = true;
        const res = await request({
          url: '/api/user/change-password',
          method: 'post',
          data: passwordForm,
          timeout: 10000
        });
        if (res.code === 200) {
          ElMessage.success('密码修改成功，请重新登录');
          // 清空表单
          passwordForm.oldPassword = '';
          passwordForm.newPassword = '';
          passwordForm.confirmPassword = '';
          // 跳转到登录页
          setTimeout(() => {
            localStorage.removeItem('accessToken');
            router.push('/login');
          }, 1500);
        } else {
          ElMessage.error(res.msg || '密码修改失败');
        }
      } catch (error) {
        console.error('修改密码失败：', error);
        ElMessage.error('修改失败，原密码错误或服务器异常');
      } finally {
        loading.value = false;
      }
    };

    // 页面挂载初始化
    onMounted(() => {
      initData();
    });

    // 暴露变量和方法
    return {
      activeTab,
      loading,
      generating,
      userInfoForm,
      passwordForm,
      apiKeyForm,
      apiKeyList,
      showKeyModal,
      newApiKey,
      handleAvatarUpload,
      submitUserInfo,
      submitPassword,
      generateApiKey,
      copyToClipboard,
      maskAccessKey,
      formatTime,
      toggleKeyStatus,
      resetApiKey,
      deleteApiKey
    };
  }
};
</script>

<style scoped>
/* 全局容器样式 - 与历史页保持一致 */
.setting-container {
  width: 100%;
  max-width: 1400px;
  margin: 0 auto;
  padding: 40px 20px;
  box-sizing: border-box;
  font-family: "Helvetica Neue", Helvetica, Arial, sans-serif;
  color: #1a2b48;
  background-color: #ffffff;
}

/* 页面标题 - 复用历史页样式 */
.setting-page-title {
  text-align: center;
  margin-bottom: 32px;
}

.setting-page-title h2 {
  font-size: 28px;
  font-weight: 600;
  margin-bottom: 8px;
  color: #1a2b48;
}

.setting-page-title p {
  font-size: 16px;
  color: #4e5d78;
  margin: 0;
}

/* 选项卡样式 */
.setting-tabs {
  background-color: #fafbfc;
  border: 1px solid #e6e9ed;
  border-radius: 12px;
  overflow: hidden;
}

/* 选项卡导航 */
.tabs-nav {
  display: flex;
  background-color: #e6f7ff;
  border-bottom: 1px solid #409eff;
  flex-wrap: wrap; /* 适配选项卡，自动换行 */
}

.tab-nav-item {
  flex: 1;
  min-width: 120px; /* 最小宽度，避免挤压 */
  padding: 16px 0;
  background: transparent;
  border: none;
  font-size: 16px;
  font-weight: 500;
  color: #4e5d78;
  cursor: pointer;
  transition: all 0.3s ease;
}

.tab-nav-item:hover {
  color: #409eff;
  background-color: #f0f7ff;
}

.tab-nav-item.active {
  color: #409eff;
  background-color: #ffffff;
  border-bottom: 2px solid #409eff;
}

/* 选项卡面板 */
.tab-panel {
  padding: 32px;
}

/* 表单容器 */
.setting-form-box {
  max-width: 800px;
  margin: 0 auto;
}

/* 表单项 */
.form-item {
  display: flex;
  flex-direction: column;
  margin-bottom: 24px;
}

.form-label {
  font-size: 14px;
  color: #1a2b48;
  font-weight: 500;
  margin-bottom: 8px;
}

.form-input {
  padding: 10px 12px;
  border: 1px solid #e6e9ed;
  border-radius: 8px;
  font-size: 14px;
  color: #1a2b48;
  outline: none;
  transition: border-color 0.3s ease;
}

.form-input:focus {
  border-color: #409eff;
}

.form-input:disabled {
  background-color: #f5f7fa;
  color: #909399;
}

/* 下拉选择框 */
.form-select {
  padding: 10px 12px;
  border: 1px solid #e6e9ed;
  border-radius: 8px;
  font-size: 14px;
  color: #1a2b48;
  outline: none;
  transition: border-color 0.3s ease;
  background-color: #ffffff;
}

.form-select:focus {
  border-color: #409eff;
}

/* 表单提示文字 */
.form-tip {
  margin: 8px 0 0 0;
  font-size: 12px;
  color: #909399;
}

/* 表单值展示 */
.form-value {
  font-size: 14px;
  color: #1a2b48;
  line-height: 38px;
}

/* 头像上传区域 */
.avatar-upload-box {
  display: flex;
  align-items: center;
  gap: 16px;
}

.avatar-preview {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  object-fit: cover;
  border: 1px solid #e6e9ed;
}

.avatar-placeholder {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background-color: #f5f7fa;
  border: 1px dashed #e6e9ed;
  display: flex;
  align-items: center;
  justify-content: center;
}

.placeholder-text {
  font-size: 12px;
  color: #909399;
}

.avatar-input {
  display: none;
}

.avatar-upload-btn {
  padding: 8px 16px;
  border-radius: 8px;
  font-size: 14px;
  color: #409eff;
  background-color: #e8f4f8;
  border: 1px solid #409eff;
  cursor: pointer;
  transition: all 0.3s ease;
}

.avatar-upload-btn:hover {
  background-color: #409eff;
  color: #ffffff;
}

/* 移除存储配置相关样式 */

/* 表单操作按钮 */
.form-actions {
  margin-top: 32px;
  text-align: center;
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

.submit-btn {
  background-color: #409eff;
  color: #ffffff;
  padding: 10px 24px;
  font-size: 16px;
}

.submit-btn:hover {
  background-color: #337ecc;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.3);
}

.submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* API密钥相关样式 */
.api-key-generate-box {
  background-color: #ffffff;
  padding: 24px;
  border-radius: 8px;
  border: 1px solid #e6e9ed;
}

.generate-header h3 {
  font-size: 18px;
  color: #1a2b48;
  margin: 0 0 8px 0;
}

.generate-tip {
  font-size: 12px;
  color: #f56c6c;
  margin: 0 0 24px 0;
}

/* 密钥弹窗样式 */
.api-key-modal {
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

.modal-content {
  width: 500px;
  background-color: #ffffff;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.modal-header {
  padding: 16px 24px;
  border-bottom: 1px solid #e6e9ed;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-header h4 {
  margin: 0;
  font-size: 16px;
  color: #1a2b48;
}

.close-btn {
  background: transparent;
  border: none;
  font-size: 20px;
  color: #909399;
  cursor: pointer;
  width: 32px;
  height: 32px;
  line-height: 32px;
  text-align: center;
}

.close-btn:hover {
  color: #f56c6c;
}

.modal-body {
  padding: 24px;
}

.key-item {
  margin-bottom: 16px;
}

.key-item label {
  font-size: 14px;
  color: #4e5d78;
  display: block;
  margin-bottom: 8px;
}

.key-value {
  display: flex;
  align-items: center;
  gap: 12px;
  background-color: #f5f7fa;
  padding: 12px;
  border-radius: 4px;
  font-family: monospace;
  font-size: 14px;
  word-break: break-all;
}

.copy-btn {
  padding: 4px 8px;
  background-color: #409eff;
  color: #ffffff;
  border: none;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
  white-space: nowrap;
}

.copy-btn:hover {
  background-color: #337ecc;
}

.modal-tip {
  font-size: 12px;
  color: #f56c6c;
  margin-top: 16px;
}

.modal-footer {
  padding: 16px 24px;
  border-top: 1px solid #e6e9ed;
  text-align: right;
}

.confirm-btn {
  background-color: #409eff;
  color: #ffffff;
}

/* API密钥列表样式 */
.api-key-list-box {
  background-color: #ffffff;
  padding: 24px;
  border-radius: 8px;
  border: 1px solid #e6e9ed;
}

.list-header h3 {
  font-size: 18px;
  color: #1a2b48;
  margin: 0 0 8px 0;
}

.list-tip {
  font-size: 12px;
  color: #909399;
  margin: 0 0 16px 0;
}

.api-key-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 0;
}

.empty-icon {
  width: 64px;
  height: 64px;
  fill: #c0c4cc;
  margin-bottom: 16px;
}

.empty-text {
  font-size: 14px;
  color: #909399;
}

/* 密钥列表样式 */
.api-key-list {
  border: 1px solid #e6e9ed;
  border-radius: 8px;
  overflow: hidden;
}

.list-header-row {
  display: flex;
  background-color: #e6f7ff;
  font-weight: 500;
}

.list-row {
  display: flex;
  border-bottom: 1px solid #e6e9ed;
  transition: background-color 0.3s ease;
}

.list-row:hover {
  background-color: #f0f7ff;
}

.list-col {
  padding: 16px;
  flex: 1;
  text-align: center;
  font-size: 14px;
}

/* 列表列宽调整 */
.col-name {
  flex: 1.5;
}

.col-access-key {
  flex: 2.5;
  font-family: monospace;
}

.col-status {
  flex: 1;
}

.col-create-time, .col-expire-time {
  flex: 2;
}

.col-operation {
  flex: 2;
  display: flex;
  gap: 8px;
  justify-content: center;
}

/* 操作按钮样式 */
.oper-btn {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
  border: none;
}

.toggle-btn {
  background-color: #e8f4f8;
  color: #4299e1;
}

.reset-btn {
  background-color: #fff7e6;
  color: #faad14;
}

.delete-btn {
  background-color: #fff2f0;
  color: #f56c6c;
}

.oper-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 密钥状态标签 */
.status-tag {
  padding: 4px 12px;
  border-radius: 16px;
  font-size: 12px;
  font-weight: 500;
}

.tag-active {
  background-color: #f0fff4;
  color: #67c23a;
}

.tag-disabled {
  background-color: #f5f5f5;
  color: #909399;
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

/* 响应式适配 */
@media (max-width: 1024px) {
  .setting-container {
    max-width: 100%;
  }

  .tab-panel {
    padding: 24px;
  }

  /* API密钥列表响应式 */
  .col-name {
    flex: 1.2;
  }

  .col-access-key {
    flex: 2;
  }

  .col-operation {
    flex: 2.5;
    flex-wrap: wrap;
  }
}

@media (max-width: 768px) {
  .setting-page-title h2 {
    font-size: 24px;
  }

  .tabs-nav {
    flex-wrap: wrap;
  }

  .tab-nav-item {
    flex: 50%;
    padding: 12px 0;
    font-size: 14px;
  }

  .tab-panel {
    padding: 16px;
  }

  .avatar-upload-box {
    flex-direction: column;
    align-items: flex-start;
  }

  /* API密钥弹窗响应式 */
  .modal-content {
    width: 90%;
  }

  /* API密钥列表响应式 */
  .list-header-row {
    display: none;
  }

  .list-row {
    flex-direction: column;
    padding: 16px;
    border-bottom: 1px solid #e6e9ed;
  }

  .list-col {
    padding: 8px 0;
    text-align: left;
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

  .col-name::before {
    content: '密钥名称：';
  }

  .col-access-key::before {
    content: 'Access Key：';
  }

  .col-status::before {
    content: '状态：';
  }

  .col-create-time::before {
    content: '创建时间：';
  }

  .col-expire-time::before {
    content: '过期时间：';
  }

  .col-operation::before {
    content: '操作：';
  }

  .col-operation {
    justify-content: flex-start;
    margin-top: 8px;
  }
}

@media (max-width: 480px) {
  .setting-container {
    padding: 20px 16px;
  }

  .tab-nav-item {
    flex: 100%;
  }

  .submit-btn {
    width: 100%;
  }
}
</style>