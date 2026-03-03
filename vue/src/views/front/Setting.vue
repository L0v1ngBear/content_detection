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
        <button
            class="tab-nav-item"
            :class="{ active: activeTab === 'apiKey' }"
            @click="activeTab = 'apiKey'"
        >
          API访问密钥
        </button>
      </div>

      <!-- 个人信息面板 -->
      <div class="tab-panel" v-if="activeTab === 'userInfo'">
        <div class="setting-form-box">
          <div class="form-item">
            <label class="form-label">用户名</label>
            <input
                v-model="userInfoForm.username"
                type="text"
                class="form-input"
                placeholder="请输入用户名"
                maxlength="20"
            />
            <p class="form-tip">用户名长度为2-20个字符</p>
          </div>
          <div class="form-item">
            <label class="form-label">手机号码</label>
            <input
                v-model="userInfoForm.phone"
                type="tel"
                class="form-input"
                placeholder="请输入手机号码"
                maxlength="11"
            />
            <p class="form-tip">选填，用于接收重要通知</p>
          </div>
          <div class="form-item">
            <label class="form-label">电子邮箱</label>
            <input
                v-model="userInfoForm.email"
                type="email"
                class="form-input"
                placeholder="请输入电子邮箱"
            />
            <p class="form-tip">选填，用于密码找回和密钥通知</p>
          </div>
          <div class="form-item">
            <label class="form-label">用户头像</label>
            <div class="avatar-upload-box">
              <img
                  v-if="userInfoForm.avatar"
                  :src="userInfoForm.avatar"
                  alt="用户头像"
                  class="avatar-preview"
              />
              <div v-else class="avatar-placeholder">
                <span class="placeholder-text">暂无头像</span>
              </div>
              <input
                  type="file"
                  class="avatar-input"
                  accept="image/jpeg,image/png"
                  @change="handleAvatarUpload"
                  id="avatar-upload"
              />
              <label for="avatar-upload" class="avatar-upload-btn">
                上传头像
              </label>
            </div>
            <p class="form-tip">支持JPG/PNG格式，大小不超过2MB</p>
          </div>
          <div class="form-actions">
            <button
                class="btn submit-btn"
                @click="submitUserInfo"
                :disabled="loading"
            >
              {{ loading ? '保存中...' : '保存修改' }}
            </button>
          </div>
        </div>
      </div>

      <!-- 密码修改面板 -->
      <div class="tab-panel" v-if="activeTab === 'password'">
        <div class="setting-form-box">
          <div class="form-item">
            <label class="form-label">原密码</label>
            <input
                v-model="passwordForm.oldPassword"
                type="password"
                class="form-input"
                placeholder="请输入原密码"
            />
          </div>
          <div class="form-item">
            <label class="form-label">新密码</label>
            <input
                v-model="passwordForm.newPassword"
                type="password"
                class="form-input"
                placeholder="请输入新密码"
                maxlength="20"
            />
            <p class="form-tip">密码长度为6-20个字符，建议包含字母和数字</p>
          </div>
          <div class="form-item">
            <label class="form-label">确认新密码</label>
            <input
                v-model="passwordForm.confirmPassword"
                type="password"
                class="form-input"
                placeholder="请再次输入新密码"
                maxlength="20"
            />
          </div>
          <div class="form-actions">
            <button
                class="btn submit-btn"
                @click="submitPassword"
                :disabled="loading"
            >
              {{ loading ? '修改中...' : '修改密码' }}
            </button>
          </div>
        </div>
      </div>

      <!-- API访问密钥面板 -->
      <div class="tab-panel" v-if="activeTab === 'apiKey'">
        <!-- 密钥生成区域 -->
        <div class="api-key-generate-box">
          <div class="generate-header">
            <h3>生成新的API密钥</h3>
            <p class="generate-tip">
              🔒 密钥生成后请立即保存，Secret Key仅显示一次，丢失后无法找回
            </p>
          </div>
          <div class="form-item">
            <label class="form-label">密钥名称</label>
            <input
                v-model="apiKeyForm.keyName"
                type="text"
                class="form-input"
                placeholder="请输入密钥名称（选填）"
                maxlength="100"
            />
            <p class="form-tip">用于标识密钥用途，方便管理</p>
          </div>
          <div class="form-item">
            <label class="form-label">有效期</label>
            <select
                v-model="apiKeyForm.expireDays"
                class="form-select"
            >
              <option value="7">7天</option>
              <option value="30">30天（默认）</option>
              <option value="90">90天</option>
              <option value="180">180天</option>
              <option value="365">1年</option>
              <option value="0">永久有效</option>
            </select>
            <p class="form-tip">设置密钥的有效期限，到期后自动失效</p>
          </div>
          <div class="form-actions">
            <button
                class="btn submit-btn"
                @click="generateApiKey"
                :disabled="generating"
            >
              {{ generating ? '生成中...' : '生成API密钥' }}
            </button>
          </div>
        </div>

        <!-- 密钥使用说明区域（核心更新：适配双Key+服务端自动MD5校验） -->
        <div class="api-key-guide-box" style="margin-top: 32px;">
          <div class="guide-header">
            <h3>API密钥使用说明</h3>
            <p class="guide-tip">使用以下方式调用NSFW图片分类接口，<strong>必须同时传递Api-Key和Secret-Key</strong>（直接传原始Secret Key即可）</p>
          </div>
          <div class="guide-content">
            <!-- 基础调用规则 -->
            <div class="guide-section">
              <h4>1. 基础调用规则</h4>
              <ul class="guide-list">
                <li>请求方式：POST</li>
                <li>接口域名：<code>http://localhost:8000</code></li>
                <li>请求头（<strong>必须</strong>）：
                  <ul class="sub-guide-list">
                    <li><code>Api-Key</code>：Access Key</li>
                    <li><code>Secret-Key</code>：Secret Key</li>
                    <li><code>Content-Type</code>：application/json</li>
                  </ul>
                </li>
                <li>接口列表：
                  <ul class="sub-guide-list">
                    <li>单张图片分类：<code>/classify</code></li>
                    <li>批量图片分类：<code>/classify/batch</code></li>
                  </ul>
                </li>
              </ul>
            </div>

            <!-- 接口调用示例 - 单张图片分类（简化版，无需手动MD5） -->
            <div class="guide-section">
              <h4>2. 单张图片分类接口示例</h4>
              <p class="guide-desc">请求示例（Python）</p>
              <div class="code-block">
                <pre>
import requests
import json

API_URL = "http://localhost:8000/classify"
ACCESS_KEY = "你的Access Key"
RAW_SECRET_KEY = "你的Secret Key"
IMAGE_URL = "https://example.com/test.jpg"

headers = {
    "Content-Type": "application/json",
    "Api-Key": ACCESS_KEY,
    "Secret-Key": RAW_SECRET_KEY
}

data = {
    "image_url": IMAGE_URL
}

try:
    response = requests.post(
        API_URL,
        headers=headers,
        data=json.dumps(data),
        timeout=30
    )
    result = response.json()
    print("识别结果：", json.dumps(result, indent=2, ensure_ascii=False))
except Exception as e:
    print("调用失败：", str(e))
                </pre>
              </div>
              <p class="guide-desc">响应示例：</p>
              <div class="code-block">
                <pre>
{
  "code": 200,
  "msg": "识别成功",
  "data": {
    "image_url": "https://example.com/test.jpg",
    "image_size": "720x1080",
    "final_class": "Normal",
    "final_prob": 99.85,
    "detail_probs": {
      "Adult": 0.05,
      "Normal": 99.85,
      "Violent": 0.1
    },
    "api_key_used": "abcd1234****"
  }
}
                </pre>
              </div>
            </div>

            <!-- 接口调用示例 - 批量图片分类（简化版，无需手动MD5） -->
            <div class="guide-section">
              <h4>3. 批量图片分类接口示例</h4>
              <p class="guide-desc">请求示例（Python）：</p>
              <div class="code-block">
                <pre>
import requests
import json

API_URL = "http://localhost:8000/classify/batch"
ACCESS_KEY = "你的Access Key"
RAW_SECRET_KEY = "你的Secret Key"
IMAGE_URLS = [
    "https://example.com/test1.jpg",
    "https://example.com/test2.jpg",
    "https://example.com/test3.jpg"
]

headers = {
    "Content-Type": "application/json",
    "Api-Key": ACCESS_KEY,
    "Secret-Key": RAW_SECRET_KEY
}

data = {
    "image_urls": IMAGE_URLS
}

try:
    response = requests.post(
        API_URL,
        headers=headers,
        data=json.dumps(data),
        timeout=60
    )
    result = response.json()
    print("批量识别结果：", json.dumps(result, indent=2, ensure_ascii=False))
except Exception as e:
    print("调用失败：", str(e))
                </pre>
              </div>
              <p class="guide-desc">响应示例：</p>
              <div class="code-block">
                <pre>
{
  "code": 200,
  "msg": "批量识别完成（成功3张，失败0张）",
  "data": {
    "total_count": 3,
    "success_count": 3,
    "fail_count": 0,
    "results": [
      {
        "index": 0,
        "image_url": "https://example.com/test1.jpg",
        "status": "success",
        "data": {
          "image_size": "720x1080",
          "final_class": "Normal",
          "final_prob": 99.85,
          "detail_probs": {
            "Adult": 0.05,
            "Normal": 99.85,
            "Violent": 0.1
          }
        },
        "error_msg": null
      },
      {
        "index": 1,
        "image_url": "https://example.com/test2.jpg",
        "status": "success",
        "data": {
          "image_size": "640x480",
          "final_class": "Adult",
          "final_prob": 98.72,
          "detail_probs": {
            "Adult": 98.72,
            "Normal": 1.15,
            "Violent": 0.13
          }
        },
        "error_msg": null
      },
      {
        "index": 2,
        "image_url": "https://example.com/test3.jpg",
        "status": "success",
        "data": {
          "image_size": "1080x1920",
          "final_class": "Violent",
          "final_prob": 97.89,
          "detail_probs": {
            "Adult": 0.25,
            "Normal": 1.86,
            "Violent": 97.89
          }
        },
        "error_msg": null
      }
    ],
    "api_key_used": "abcd1234****"
  }
}
                </pre>
              </div>
            </div>

            <!-- 注意事项（更新校验规则） -->
            <div class="guide-section">
              <h4>4. 注意事项</h4>
              <ul class="guide-list">
                <li><strong>Secret Key安全</strong>：原始Secret Key仅生成时显示一次，丢失后无法找回，需重置密钥</li>
                <li>图片URL必须是公网可访问的HTTP/HTTPS地址，不支持本地文件路径</li>
                <li>批量接口单次最多处理50张图片，建议设置合理的超时时间（60秒）</li>
                <li>响应中的<code>final_class</code>包含三种类型：Adult（成人内容）、Normal（正常内容）、Violent（暴力内容）</li>
                <li>API Key失效场景：Key不存在、已禁用、已过期（过期后会自动标记为expired状态）</li>
                <li>Secret Key校验失败场景：原始值错误、Secret Key已重置</li>
                <li>常见错误码：
                  <ul class="sub-guide-list">
                    <li>400：请求参数错误（如图片URL格式不正确）</li>
                    <li>401：Api-Key无效（不存在/已禁用/已过期）或Secret-Key错误</li>
                    <li>500：服务器内部错误（如模型加载失败、数据库异常）</li>
                  </ul>
                </li>
              </ul>
            </div>
          </div>
        </div>

        <!-- 密钥列表区域 -->
        <div class="api-key-list-box" style="margin-top: 32px;">
          <div class="list-header">
            <h3>API密钥列表</h3>
            <p class="list-tip">
              共 {{ apiKeyList.length }} 个密钥，点击操作按钮可管理密钥状态
            </p>
          </div>

          <!-- 空列表状态 -->
          <div v-if="apiKeyList.length === 0" class="api-key-empty">
            <svg class="empty-icon" viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg">
              <path d="M832 64H192c-17.7 0-32 14.3-32 32v832c0 17.7 14.3 32 32 32h640c17.7 0 32-14.3 32-32V96c0-17.7-14.3-32-32-32zM648.3 866.4L512 736.1 375.7 866.4c-10.4 10.7-27.5 10.7-37.9 0l-28.6-29.1c-10.4-10.7-10.4-28 0-38.7l149.1-153c10.4-10.7 27.5-10.7 37.9 0l149.1 153c10.4 10.7 10.4 28 0 38.7l-28.6 29.1z m117.9-322.3c-19.1 0-34.6-15.5-34.6-34.6 0-19.1 15.5-34.6 34.6-34.6s34.6 15.5 34.6 34.6c0 19.1-15.5 34.6-34.6 34.6zM384 544c0 17.7 14.3 32 32 32h192c17.7 0 32-14.3 32-32v-64c0-17.7-14.3-32-32-32H416c-17.7 0-32 14.3-32 32v64z"/>
            </svg>
            <p class="empty-text">暂无API密钥，请点击上方按钮生成</p>
          </div>

          <!-- 密钥列表 -->
          <div v-else class="api-key-list">
            <div class="list-header-row">
              <div class="list-col col-name">密钥名称</div>
              <div class="list-col col-access-key">Access Key</div>
              <div class="list-col col-status">状态</div>
              <div class="list-col col-create-time">创建时间</div>
              <div class="list-col col-expire-time">过期时间</div>
              <div class="list-col col-operation">操作</div>
            </div>
            <div
                v-for="key in apiKeyList"
                :key="key.id"
                class="list-row"
            >
              <div class="list-col col-name" data-label="密钥名称：">{{ key.keyName || '默认名称' }}</div>
              <div class="list-col col-access-key" data-label="Access Key：">{{ maskAccessKey(key.accessKey) }}</div>
              <div class="list-col col-status" data-label="状态：">
                <span
                    class="status-tag"
                    :class="key.status === 'active' ? 'tag-active' : 'tag-disabled'"
                >
                  {{ key.status === 'active' ? '已启用' : '已禁用/过期' }}
                </span>
              </div>
              <div class="list-col col-create-time" data-label="创建时间：">{{ formatTime(key.createTime) }}</div>
              <div class="list-col col-expire-time" data-label="过期时间：">
                {{ key.expireTime ? formatTime(key.expireTime) : '永久有效' }}
              </div>
              <div class="list-col col-operation" data-label="操作：">
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
                    :disabled="loading || key.status !== 'active'"
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
      </div>
    </div>

    <!-- 密钥生成成功弹窗 -->
    <div v-if="showKeyModal" class="api-key-modal">
      <div class="modal-content">
        <div class="modal-header">
          <h4>API密钥生成成功</h4>
          <button class="close-btn" @click="showKeyModal = false">×</button>
        </div>
        <div class="modal-body">
          <div class="key-item">
            <label>Access Key</label>
            <div class="key-value">
              <span>{{ newApiKey.accessKey }}</span>
              <button class="copy-btn" @click="copyToClipboard(newApiKey.accessKey)">复制</button>
            </div>
          </div>
          <div class="key-item">
            <label>Secret Key（原始值，直接使用）</label>
            <div class="key-value">
              <span>{{ newApiKey.secretKey }}</span>
              <button class="copy-btn" @click="copyToClipboard(newApiKey.secretKey)">复制</button>
            </div>
          </div>
          <p class="modal-tip">
            ⚠️ 请务必保存好Secret Key，页面关闭后将无法再次查看！调用接口时直接传递此原始值即可。
          </p>
        </div>
        <div class="modal-footer">
          <button class="btn confirm-btn" @click="showKeyModal = false">我已保存</button>
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
    const router = useRouter();
    const activeTab = ref('userInfo');
    const loading = ref(false);
    const generating = ref(false);
    const currentUserId = ref(null);

    const userInfoForm = reactive({
      username: '',
      phone: '',
      email: '',
      avatar: ''
    });

    const passwordForm = reactive({
      oldPassword: '',
      newPassword: '',
      confirmPassword: ''
    });

    const apiKeyForm = reactive({
      keyName: '',
      expireDays: 30
    });
    const apiKeyList = ref([]);
    const showKeyModal = ref(false);
    const newApiKey = ref({ accessKey: '', secretKey: '' });

    const initData = async () => {
      try {
        loading.value = true;
        const userRes = await request({
          url: 'user/info',
          method: 'get',
          timeout: 10000
        });
        const {name, phone, email, avatar, id} = userRes.data;
        Object.assign(userInfoForm, {name, phone, email, avatar});
        currentUserId.value = id;
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

    const loadApiKeyList = async () => {
      try {
        loading.value = true;
        if (!currentUserId.value) {
          apiKeyList.value = [];
          ElMessage.warning('用户信息未加载完成，无法获取密钥列表');
          return;
        }
        const res = await request({
          url: '/api/user/api-key/list',
          method: 'get',
          params: { userId: currentUserId.value },
          timeout: 10000
        });
        apiKeyList.value = Array.isArray(res.data) ? res.data : [];
      } catch (error) {
        console.error('加载API密钥列表失败：', error);
        ElMessage.error('加载API密钥列表失败，请重试');
        apiKeyList.value = [];
      } finally {
        loading.value = false;
      }
    };

    const generateApiKey = async () => {
      if (!currentUserId.value) {
        ElMessage.error('用户信息未加载完成，请刷新页面重试');
        return;
      }
      if (apiKeyForm.keyName && apiKeyForm.keyName.length > 100) {
        ElMessage.warning('密钥名称长度不能超过100个字符');
        return;
      }
      try {
        generating.value = true;
        const requestData = {
          userId: currentUserId.value,
          keyName: apiKeyForm.keyName.trim() || '',
          expireDays: Number(apiKeyForm.expireDays)
        };
        const res = await request({
          url: '/api/user/api-key/generate',
          method: 'post',
          data: requestData,
          timeout: 15000
        });
        if (res && res.code === 200 && res.data) {
          newApiKey.value = res.data;
          showKeyModal.value = true;
          apiKeyForm.keyName = '';
          apiKeyForm.expireDays = 30;
          await loadApiKeyList();
          ElMessage.success('API密钥生成成功');
        } else {
          ElMessage.error(res?.msg || '生成API密钥失败');
        }
      } catch (error) {
        console.error('生成API密钥失败：', error);
        if (error.response?.data?.msg) {
          ElMessage.error(error.response.data.msg);
        } else {
          ElMessage.error('生成失败，请检查网络或联系管理员');
        }
      } finally {
        generating.value = false;
      }
    };

    const copyToClipboard = (text) => {
      if (!text) {
        ElMessage.warning('无内容可复制');
        return;
      }
      if (navigator.clipboard && window.isSecureContext) {
        navigator.clipboard.writeText(text).then(() => {
          ElMessage.success('复制成功！');
        }).catch(() => {
          fallbackCopyTextToClipboard(text);
        });
      } else {
        fallbackCopyTextToClipboard(text);
      }
    };

    const fallbackCopyTextToClipboard = (text) => {
      const textArea = document.createElement("textarea");
      textArea.value = text;
      textArea.style.top = "0";
      textArea.style.left = "0";
      textArea.style.position = "fixed";
      document.body.appendChild(textArea);
      textArea.focus();
      textArea.select();
      try {
        const successful = document.execCommand('copy');
        const msg = successful ? '复制成功！' : '复制失败，请手动复制';
        ElMessage.success(msg);
      } catch (err) {
        console.error('Fallback: 无法复制文本: ', err);
        ElMessage.error('复制失败，请手动复制');
      }
      document.body.removeChild(textArea);
    };

    const maskAccessKey = (accessKey) => {
      if (!accessKey) return '';
      if (accessKey.length <= 12) return accessKey;
      return accessKey.substring(0, 8) + '****************' + accessKey.substring(accessKey.length - 4);
    };

    const formatTime = (timestamp) => {
      if (!timestamp) return '未知时间';
      let date;
      if (typeof timestamp === 'number') {
        date = new Date(timestamp.toString().length === 10 ? timestamp * 1000 : timestamp);
      } else {
        date = new Date(timestamp);
      }
      if (isNaN(date.getTime())) {
        return '未知时间';
      }
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
      });
    };

    const toggleKeyStatus = async (keyId) => {
      if (!keyId) {
        ElMessage.warning('密钥ID不存在，操作失败');
        return;
      }
      try {
        loading.value = true;
        const res = await request({
          url: `/api/user/api-key/toggle/${keyId}`,
          method: 'post',
          data: { userId: currentUserId.value },
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

    const resetApiKey = async (keyId) => {
      if (!keyId) {
        ElMessage.warning('密钥ID不存在，操作失败');
        return;
      }
      try {
        await ElMessageBox.confirm(
            '重置密钥将生成新的Secret Key，原密钥将立即失效，是否确认？',
            '确认重置',
            {
              confirmButtonText: '确认',
              cancelButtonText: '取消',
              type: 'warning',
              confirmButtonClass: 'el-button--warning'
            }
        );
        loading.value = true;
        const res = await request({
          url: `/api/user/api-key/reset/${keyId}`,
          method: 'post',
          data: { userId: currentUserId.value },
          timeout: 10000
        });
        if (res.code === 200) {
          ElMessage.success('密钥重置成功，新的Secret Key已发送至你的邮箱');
          await loadApiKeyList();
        } else {
          ElMessage.error(res.msg || '重置失败');
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('重置密钥失败：', error);
          ElMessage.error('重置失败，请重试');
        } else {
          ElMessage.info('已取消重置');
        }
      } finally {
        loading.value = false;
      }
    };

    const deleteApiKey = async (keyId) => {
      if (!keyId) {
        ElMessage.warning('密钥ID不存在，操作失败');
        return;
      }
      try {
        await ElMessageBox.confirm(
            '删除密钥后将无法恢复，相关接口调用将失效，是否确认删除？',
            '确认删除',
            {
              confirmButtonText: '删除',
              cancelButtonText: '取消',
              type: 'error',
              dangerMode: true
            }
        );
        loading.value = true;
        const res = await request({
          url: `/api/user/api-key/delete/${keyId}`,
          method: 'post',
          data: { userId: currentUserId.value },
          timeout: 10000
        });
        if (res.code === 200) {
          ElMessage.success('密钥删除成功');
          await loadApiKeyList();
        } else {
          ElMessage.error(res.msg || '删除失败');
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('删除密钥失败：', error);
          ElMessage.error('删除失败，请重试');
        } else {
          ElMessage.info('已取消删除');
        }
      } finally {
        loading.value = false;
      }
    };

    const handleAvatarUpload = (e) => {
      const file = e.target.files[0];
      if (!file) return;
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
      const formData = new FormData();
      formData.append('file', file);
      if (currentUserId.value) {
        formData.append('userId', currentUserId.value);
      }
      request({
        url: '/api/upload/avatar',
        method: 'post',
        data: formData,
        headers: { 'Content-Type': 'multipart/form-data' },
        timeout: 15000
      }).then(res => {
        if (res.code === 200 && res.data?.url) {
          userInfoForm.avatar = res.data.url;
          ElMessage.success('头像上传成功！');
        } else {
          ElMessage.error('头像上传失败：' + (res.msg || '服务器返回异常'));
        }
        e.target.value = '';
      }).catch(error => {
        console.error('头像上传失败：', error);
        ElMessage.error('头像上传失败，请重试');
        e.target.value = '';
      });
    };

    const submitUserInfo = async () => {
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
        const submitData = { id: currentUserId.value, ...userInfoForm };
        const res = await request({
          url: '/api/user/update',
          method: 'post',
          data: submitData,
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

    const submitPassword = async () => {
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
        const submitData = { userId: currentUserId.value, ...passwordForm };
        const res = await request({
          url: '/api/user/change-password',
          method: 'post',
          data: submitData,
          timeout: 10000
        });
        if (res.code === 200) {
          ElMessage.success('密码修改成功，请重新登录');
          passwordForm.oldPassword = '';
          passwordForm.newPassword = '';
          passwordForm.confirmPassword = '';
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

    onMounted(() => {
      initData();
    });

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

.setting-tabs {
  background-color: #fafbfc;
  border: 1px solid #e6e9ed;
  border-radius: 12px;
  overflow: hidden;
}

.tabs-nav {
  display: flex;
  background-color: #e6f7ff;
  border-bottom: 1px solid #409eff;
  flex-wrap: wrap;
}

.tab-nav-item {
  flex: 1;
  min-width: 120px;
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

.tab-panel {
  padding: 32px;
}

.setting-form-box {
  max-width: 800px;
  margin: 0 auto;
}

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

.form-tip {
  margin: 8px 0 0 0;
  font-size: 12px;
  color: #909399;
}

.form-value {
  font-size: 14px;
  color: #1a2b48;
  line-height: 38px;
}

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

.form-actions {
  margin-top: 32px;
  text-align: center;
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

/* 密钥使用说明样式 */
.api-key-guide-box {
  background-color: #ffffff;
  padding: 24px;
  border-radius: 8px;
  border: 1px solid #e6e9ed;
}

.guide-header h3 {
  font-size: 18px;
  color: #1a2b48;
  margin: 0 0 8px 0;
}

.guide-tip {
  font-size: 12px;
  color: #4e5d78;
  margin: 0 0 16px 0;
}

.guide-content {
  font-size: 14px;
  color: #1a2b48;
}

.guide-section {
  margin-bottom: 24px;
}

.guide-section h4 {
  font-size: 16px;
  color: #1a2b48;
  margin: 0 0 12px 0;
  font-weight: 500;
}

.guide-list {
  margin: 0;
  padding-left: 20px;
  line-height: 1.8;
}

.sub-guide-list {
  margin: 8px 0;
  padding-left: 20px;
}

.guide-desc {
  margin: 8px 0;
  line-height: 1.6;
}

.code-block {
  background-color: #f5f7fa;
  border-radius: 8px;
  padding: 16px;
  margin: 8px 0;
  font-family: monospace;
  font-size: 13px;
  overflow-x: auto;
  border: 1px solid #e6e9ed;
}

.code-block pre {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-all;
}

code {
  background-color: #f5f7fa;
  padding: 2px 4px;
  border-radius: 4px;
  font-family: monospace;
  font-size: 13px;
  color: #f56c6c;
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

  .modal-content {
    width: 90%;
  }

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