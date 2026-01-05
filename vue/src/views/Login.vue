<template>
  <div class="login-container">
    <!-- 背景装饰 -->
    <div class="login-bg-decoration left"></div>
    <div class="login-bg-decoration right"></div>

    <!-- 登录卡片 -->
    <div class="login-card">
      <h2 class="login-title">用户登录</h2>
      <div class="login-icon">
        <svg viewBox="0 0 24 24" fill="#409eff">
          <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8zm0-14c-2.21 0-4 1.79-4 4s1.79 4 4 4 4-1.79 4-4-1.79-4-4-4zm0 6c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2z" />
        </svg>
      </div>

      <!-- 登录方式切换标签 -->
      <div class="login-tab-wrapper">
        <button
            class="login-tab"
            :class="{ 'login-tab-active': loginType === 'PASSWORD' }"
            @click="switchLoginType('PASSWORD')"
        >
          密码登录
        </button>
        <button
            class="login-tab"
            :class="{ 'login-tab-active': loginType === 'SMS_CODE' }"
            @click="switchLoginType('SMS_CODE')"
        >
          短信登录
        </button>
      </div>

      <!-- 登录表单 -->
      <form class="login-form" @submit.prevent="handleLogin">
        <!-- 密码登录表单（对应后端 username/password） -->
        <div v-if="loginType === 'PASSWORD'">
          <div class="form-item">
            <label for="username" class="form-label">
              <svg class="label-icon" viewBox="0 0 24 24" fill="#666">
                <path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z" />
              </svg>
              用户名
            </label>
            <input
                type="text"
                id="username"
                class="form-input"
                v-model="loginForm.username"
                placeholder="请输入用户名"
                required
            />
          </div>

          <div class="form-item">
            <label for="password" class="form-label">
              <svg class="label-icon" viewBox="0 0 24 24" fill="#666">
                <path d="M18 8h-1V6c0-2.76-2.24-5-5-5S7 3.24 7 6v2H6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V10c0-1.1-.9-2-2-2zM9 6c0-1.66 1.34-3 3-3s3 1.34 3 3v2H9V6zm9 14H6V10h12v10zm-6-3c1.1 0 2-.9 2-2s-.9-2-2-2-2 .9-2 2 .9 2 2 2z" />
              </svg>
              密码
            </label>
            <div class="password-input-wrapper">
              <input
                  :type="showPassword ? 'text' : 'password'"
                  id="password"
                  class="form-input"
                  v-model="loginForm.password"
                  placeholder="请输入密码"
                  required
                  minlength="6"
              />
              <button
                  type="button"
                  class="password-toggle-btn"
                  @click="showPassword = !showPassword"
              >
                <svg viewBox="0 0 24 24" :fill="showPassword ? '#409eff' : '#999'">
                  <path d="M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z" />
                </svg>
              </button>
            </div>
          </div>
        </div>

        <!-- 短信登录表单（对应后端 phone/verifyCode） -->
        <div v-if="loginType === 'SMS_CODE'">
          <div class="form-item">
            <label for="phone" class="form-label">
              <svg class="label-icon" viewBox="0 0 24 24" fill="#666">
                <path d="M20 15.5c-1.25 0-2.45-.2-3.57-.57-.35-.11-.74-.03-1.01.24l-2.2 2.2c-2.83-1.44-5.15-3.75-6.59-6.59l2.2-2.2c.28-.28.36-.67.25-1.01C8.7 6.45 8.5 5.25 8.5 4c0-.55-.45-1-1-1H4c-.55 0-1 .45-1 1 0 9.39 7.61 17 17 17 .55 0 1-.45 1-1v-3.5c0-.55-.45-1-1-1zM5.92 5.92C4.72 4.72 3.31 4 2 4v2c1.31 0 2.72.72 3.92 1.92zM20 20c-1.28 0-2.55-.47-3.64-1.36l-2.67-2.67c.94-1.1 1.7-2.37 2.22-3.74.18-.46.52-.64.96-.64.26 0 .52.1.74.3l2.67 2.67c.89-1.09 1.36-2.36 1.36-3.64V4c-1.31 0-2.72.72-3.92 1.92C18.72 6.28 18 7.69 18 9c0 .55.45 1 1 1h2c0-1.31-.72-2.72-1.92-3.92z" />
              </svg>
              手机号
            </label>
            <input
                type="tel"
                id="phone"
                class="form-input"
                v-model="loginForm.phone"
                placeholder="请输入11位手机号"
                required
                maxlength="11"
            />
          </div>

          <div class="form-item">
            <label for="verifyCode" class="form-label">
              <svg class="label-icon" viewBox="0 0 24 24" fill="#666">
                <path d="M18 2H6c-1.1 0-2 .9-2 2v16c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zM6 4h5v8l-2.5-1.5L6 12V4z" />
              </svg>
              验证码
            </label>
            <div class="sms-code-wrapper">
              <input
                  type="text"
                  id="verifyCode"
                  class="form-input"
                  v-model="loginForm.verifyCode"
                  placeholder="请输入6位验证码"
                  required
                  maxlength="6"
              />
              <button
                  type="button"
                  class="get-code-btn"
                  @click="getSmsCode"
                  :disabled="isCodeSending || loginForm.phone.length !== 11"
              >
                {{ isCodeSending ? `${countDown}s后重新获取` : '获取验证码' }}
              </button>
            </div>
          </div>
        </div>

        <!-- 记住我（仅密码登录显示） -->
        <div class="form-extra" v-if="loginType === 'PASSWORD'">
          <label class="remember-label">
            <input type="checkbox" v-model="loginForm.rememberMe" class="remember-checkbox" />
            <span class="remember-text">记住我</span>
          </label>
          <a href="#" class="forget-link">忘记密码？</a>
        </div>

        <!-- 登录按钮 -->
        <button type="submit" class="login-btn" :disabled="isSubmitting">
          <span v-if="!isSubmitting" class="btn-text">
            {{ loginType === 'PASSWORD' ? '立即登录' : '短信验证登录' }}
          </span>
          <span v-else class="btn-loading">
            <svg class="loading-spinner" viewBox="0 0 24 24">
              <circle cx="12" cy="12" r="10" stroke="#fff" stroke-width="2" fill="none" stroke-dasharray="56.548" stroke-dashoffset="0" />
            </svg>
            登录中...
          </span>
        </button>

        <!-- 其他登录方式（微信登录，对应后端 authCode） -->
        <div class="other-login-wrapper">
          <span class="other-login-tip">其他登录方式</span>
          <div class="other-login-btns">
            <button type="button" class="wechat-login-btn" @click="handleWechatLogin">
              <svg class="wechat-icon" viewBox="0 0 24 24" fill="#07c160">
                <path d="M18.5 0h-13C4.12 0 3 1.12 3 2.5v19C3 22.88 4.12 24 5.5 24h13c1.38 0 2.5-1.12 2.5-2.5v-19C21 1.12 19.88 0 18.5 0zm-13 1h13c.83 0 1.5.67 1.5 1.5v19c0 .83-.67 1.5-1.5 1.5h-13c-.83 0-1.5-.67-1.5-1.5v-19c0-.83.67-1.5 1.5-1.5zm6.99 4.37c-1.57 0-2.86 1.24-2.98 2.78-.15 1.88.49 3.3 1.39 4.21.83.83 1.9 1.3 3.22 1.49.46.07.91.1 1.34.1.9 0 1.73-.19 2.47-.53.81-.38 1.49-.9 2.02-1.53.53-.63.93-1.36 1.19-2.17.26-.81.38-1.67.38-2.56 0-2.28-1.85-4.13-4.13-4.13zm-.01 1.29c1.3 0 2.36 1.05 2.36 2.36 0 .65-.26 1.25-.68 1.71-.42.46-.98.73-1.63.82-.13.02-.26.03-.39.03-.57 0-1.12-.13-1.62-.37-.5-.24-.93-.59-1.27-1.04-.34-.45-.54-.99-.54-1.55 0-1.31 1.06-2.36 2.36-2.36z" />
              </svg>
              微信登录
            </button>
          </div>
        </div>

        <!-- 底部注册提示 -->
        <div class="form-footer">
          还没有账号？<a href="#" class="register-link">立即注册</a>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue';
import request from '../utils/request.js';

// --------------- 基础响应式数据 ---------------
const isSubmitting = ref(false);
const showPassword = ref(false);
// 登录类型与后端保持一致：PASSWORD/SMS_CODE/WECHAT
const loginType = ref('PASSWORD');
const isCodeSending = ref(false);
const countDown = ref(60);
let countDownTimer = null;

// --------------- 表单数据：完全匹配后端 LoginRequestDTO ---------------
const loginForm = reactive({
  // 必传字段：登录类型
  loginType: 'PASSWORD',
  // 密码登录字段
  username: '',
  password: '',
  // 短信登录字段（后端字段名：verifyCode，注意对应）
  phone: '',
  verifyCode: '',
  // 第三方登录字段（微信/支付宝）
  authCode: '',
  // 前端自用：记住我（不传递给后端）
  rememberMe: false
});

// --------------- 登录方式切换 ---------------
const switchLoginType = (type) => {
  loginType.value = type;
  // 更新表单中的登录类型（同步传给后端）
  loginForm.loginType = type;

  // 重置对应表单状态
  if (type === 'PASSWORD') {
    showPassword.value = false;
  } else if (type === 'SMS_CODE') {
    loginForm.verifyCode = '';
    clearCountDown();
  }
};

// --------------- 短信验证码相关逻辑 ---------------
const getSmsCode = () => {
  // 手机号格式校验
  const phoneReg = /^1[3-9]\d{9}$/;
  if (!phoneReg.test(loginForm.phone)) {
    alert("请输入有效的11位手机号！");
    return;
  }

  // 标记发送状态，启动倒计时
  isCodeSending.value = true;
  startCountDown();

  // 模拟获取验证码接口（实际项目替换为真实后端接口）
  request().post('/auth/sendCode', { phone: loginForm.phone })
      .then(res => {
        alert("验证码已发送，请注意查收！");
      })
      .catch(err => {
        alert("获取验证码失败，请重试！");
        console.error("获取验证码错误：", err);
        clearCountDown();
      });
};

const startCountDown = () => {
  if (countDownTimer) clearInterval(countDownTimer);
  countDown.value = 60;

  countDownTimer = setInterval(() => {
    countDown.value--;
    if (countDown.value <= 0) {
      clearCountDown();
    }
  }, 1000);
};

const clearCountDown = () => {
  if (countDownTimer) {
    clearInterval(countDownTimer);
    countDownTimer = null;
  }
  isCodeSending.value = false;
  countDown.value = 60;
};

// --------------- 微信登录逻辑（对应后端 authCode） ---------------
const handleWechatLogin = () => {
  isSubmitting.value = true;

  // 模拟：获取微信授权码（实际项目需对接微信开放平台，获取真实 authCode）
  const wechatAuthCode = 'mock_wechat_auth_code_123456';
  loginForm.authCode = wechatAuthCode;
  loginForm.loginType = 'WECHAT';

  // TODO 暂时还无法实现
  // 调用后端登录接口（微信登录）
  request.post('/api/login', loginForm)
      .then(res => {
        alert("微信登录成功！");
        console.log("微信登录返回数据：", res.data);
        handleLoginSuccess(res.data);
      })
      .catch(err => {
        alert("微信登录失败，请重试！");
        console.error("微信登录错误：", err);
      })
      .finally(() => {
        isSubmitting.value = false;
        // 重置微信登录相关字段
        loginForm.authCode = '';
        loginForm.loginType = 'PASSWORD';
      });
};

// --------------- 核心：对接后端登录接口 ---------------
const handleLogin = async () => {
  // 1. 前端基础校验
  if (!validateForm()) {
    return;
  }

  // 2. 标记提交状态
  isSubmitting.value = true;

  try {
    const response = await request().post('/auth/login', loginForm);

    // 4. 登录成功处理
    alert("登录成功！");
    console.log("后端返回登录数据：", response.data);
    handleLoginSuccess(response.data);

  } catch (error) {
    // 5. 登录失败处理（捕获后端校验/业务错误）
    console.error("登录请求失败：", error);
    const errorMsg = error.response?.data?.message || "登录失败，请检查信息后重试！";
    alert(errorMsg);

  } finally {
    // 6. 重置提交状态
    isSubmitting.value = false;
  }
};

// --------------- 辅助函数 ---------------
/**
 * 前端表单校验（匹配后端 @NotBlank 等校验）
 */
const validateForm = () => {
  // 校验登录类型（后端必传）
  if (!loginForm.loginType) {
    alert("登录类型不能为空！");
    return false;
  }

  // 密码登录额外校验
  if (loginForm.loginType === 'PASSWORD') {
    if (!loginForm.username.trim()) {
      alert("请输入用户名！");
      return false;
    }
    if (loginForm.password.length < 6) {
      alert("密码长度不能少于6位！");
      return false;
    }
  }

  // 短信登录额外校验
  if (loginForm.loginType === 'SMS_CODE') {
    const phoneReg = /^1[3-9]\d{9}$/;
    if (!phoneReg.test(loginForm.phone)) {
      alert("请输入有效的11位手机号！");
      return false;
    }
    if (!/^\d{6}$/.test(loginForm.verifyCode)) {
      alert("请输入有效的6位数字验证码！");
      return false;
    }
  }

  return true;
};

/**
 * 登录成功后续处理（存储 token、跳转首页等）
 * @param {Object} loginResponse 后端返回的完整响应数据
 */
const handleLoginSuccess = (loginResponse) => {
  // 1. 提取后端 LoginResponseDTO 中的核心数据（解构赋值，更清晰）
  const { accessToken, refreshToken, expireTime } = loginResponse.data || {};

  // 2. 存储 JWT Access Token（必要，后续接口请求需携带该令牌）
  if (accessToken) {
    localStorage.setItem("accessToken", accessToken);
    console.log("Access Token 已存储：", accessToken);
  }

  // 3. 存储 Refresh Token（用于后续刷新过期的 Access Token）
  if (refreshToken) {
    localStorage.setItem("refreshToken", refreshToken);
    console.log("Refresh Token 已存储：", refreshToken);
  }

  // 4. 存储 Access Token 过期时间
  if (expireTime) {
    const expireTimestamp = Date.now() + (expireTime * 1000);
    localStorage.setItem("tokenExpireTimestamp", expireTimestamp);
    console.log("Access Token 过期时间戳：", new Date(expireTimestamp));
  }

  // 5. 处理「记住我」逻辑（仅密码登录有效，存储用户名）
  // 仅当勾选「记住我」且是密码登录时，才存储用户名（保持原有逻辑合理性）
  if (loginForm.rememberMe && loginForm.loginType === 'PASSWORD') {
    localStorage.setItem("rememberedUsername", loginForm.username);
    console.log("已记住用户名：", loginForm.username);
  } else {
    // 未勾选「记住我」或非密码登录，清除本地存储的用户名
    localStorage.removeItem("rememberedUsername");
    console.log("已清除记住的用户名");
  }

  // 提取登录页跳转时携带的 redirect 参数
  const redirectPath = router.currentRoute.query.redirect;
  if (redirectPath) {
    // 跳转回原目标页面
    router.push(redirectPath);
  } else {
    // 默认跳首页
    router.push("/front/home");
  }
};
// --------------- 组件生命周期 ---------------
onMounted(() => {
  // 恢复记住的用户名
  const rememberedUsername = localStorage.getItem("rememberedUsername");
  if (rememberedUsername) {
    loginForm.username = rememberedUsername;
    loginForm.rememberMe = true;
  }
});

onUnmounted(() => {
  // 清除倒计时定时器，防止内存泄漏
  clearCountDown();
});
</script>

<style scoped>
/* 样式部分与上一版一致，无额外修改，此处省略重复代码 */
.login-container {
  width: 100%;
  height: 100vh;
  margin: 0;
  padding: 0;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4eaf5 100%);
  display: flex;
  justify-content: center;
  align-items: center;
  box-sizing: border-box;
  position: relative;
  overflow: hidden;
}

.login-bg-decoration {
  position: absolute;
  width: 600px;
  height: 600px;
  border-radius: 50%;
  background: rgba(64, 158, 255, 0.05);
  z-index: 0;
}

.login-bg-decoration.left {
  top: -300px;
  left: -300px;
}

.login-bg-decoration.right {
  bottom: -300px;
  right: -300px;
}

.login-card {
  width: 100%;
  max-width: 420px;
  padding: 40px 35px;
  background-color: #ffffff;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.06), 0 2px 8px rgba(0, 0, 0, 0.04);
  box-sizing: border-box;
  position: relative;
  z-index: 1;
  transition: all 0.3s ease;
}

.login-card:hover {
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.08), 0 4px 12px rgba(0, 0, 0, 0.06);
}

.login-title {
  text-align: center;
  color: #2c3e50;
  margin: 0 0 10px 0;
  font-size: 26px;
  font-weight: 700;
  letter-spacing: 1px;
}

.login-icon {
  width: 60px;
  height: 60px;
  margin: 0 auto 25px auto;
  display: flex;
  justify-content: center;
  align-items: center;
  background: rgba(64, 158, 255, 0.1);
  border-radius: 50%;
  svg {
    width: 32px;
    height: 32px;
  }
}

.login-tab-wrapper {
  display: flex;
  justify-content: center;
  gap: 30px;
  margin-bottom: 25px;
}

.login-tab {
  background: none;
  border: none;
  font-size: 16px;
  font-weight: 500;
  color: #667085;
  cursor: pointer;
  padding: 5px 0;
  border-bottom: 2px solid transparent;
  transition: all 0.2s ease;
}

.login-tab-active {
  color: #409eff;
  border-bottom: 2px solid #409eff;
}

.login-tab:hover:not(.login-tab-active) {
  color: #337ecc;
  border-bottom: 2px solid #e1e5eb;
}

.login-form {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 22px;
}

.form-item {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-label {
  font-size: 14px;
  color: #34495e;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 6px;
}

.label-icon {
  width: 16px;
  height: 16px;
  flex-shrink: 0;
}

.form-input {
  width: 100%;
  height: 46px;
  padding: 0 15px;
  border: 1px solid #e1e5eb;
  border-radius: 8px;
  font-size: 14px;
  color: #2c3e50;
  box-sizing: border-box;
  outline: none;
  transition: all 0.3s ease;
  background-color: #fafbfe;
}

.form-input:focus {
  border-color: #409eff;
  box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.12);
  background-color: #ffffff;
}

.form-input::placeholder {
  color: #b0b7c3;
}

.form-input:disabled {
  background-color: #f5f7fa;
  cursor: not-allowed;
}

.password-input-wrapper {
  position: relative;
  width: 100%;
}

.password-toggle-btn {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  cursor: pointer;
  width: 24px;
  height: 24px;
  padding: 0;
  display: flex;
  justify-content: center;
  align-items: center;
  border-radius: 4px;
  transition: background-color 0.2s ease;
}

.password-toggle-btn:hover {
  background-color: rgba(64, 158, 255, 0.05);
}

.password-toggle-btn svg {
  width: 18px;
  height: 18px;
}

.sms-code-wrapper {
  position: relative;
  width: 100%;
  display: flex;
  align-items: center;
  gap: 10px;
}

.sms-code-wrapper .form-input {
  flex: 1;
}

.get-code-btn {
  width: 120px;
  height: 46px;
  background-color: #f0f7ff;
  color: #409eff;
  border: 1px solid #c6e2ff;
  border-radius: 8px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s ease;
  flex-shrink: 0;
}

.get-code-btn:disabled {
  background-color: #f5f7fa;
  color: #b0b7c3;
  border-color: #e1e5eb;
  cursor: not-allowed;
}

.get-code-btn:hover:not(:disabled) {
  background-color: #e6f0ff;
  border-color: #409eff;
}

.form-extra {
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 5px;
}

.remember-label {
  font-size: 13px;
  color: #667085;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: color 0.2s ease;
}

.remember-label:hover {
  color: #409eff;
}

.remember-checkbox {
  width: 14px;
  height: 14px;
  border-radius: 4px;
  border: 1px solid #e1e5eb;
  accent-color: #409eff;
  cursor: pointer;
}

.remember-text {
  user-select: none;
}

.forget-link {
  font-size: 13px;
  color: #667085;
  text-decoration: none;
  transition: all 0.2s ease;
}

.forget-link:hover {
  color: #409eff;
  text-decoration: none;
}

.login-btn {
  width: 100%;
  height: 48px;
  background-color: #409eff;
  color: #ffffff;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-top: 8px;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
  letter-spacing: 0.5px;
}

.login-btn:disabled {
  background-color: #a0cfff;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.login-btn:hover:not(:disabled) {
  background-color: #337ecc;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
  transform: translateY(-2px);
}

.btn-loading {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
}

.loading-spinner {
  width: 16px;
  height: 16px;
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

.other-login-wrapper {
  margin-top: 15px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.other-login-tip {
  font-size: 12px;
  color: #b0b7c3;
  position: relative;
  width: 100%;
  text-align: center;
}

.other-login-tip::before,
.other-login-tip::after {
  content: "";
  position: absolute;
  top: 50%;
  width: 40%;
  height: 1px;
  background-color: #e1e5eb;
}

.other-login-tip::before {
  left: 0;
}

.other-login-tip::after {
  right: 0;
}

.other-login-btns {
  display: flex;
  justify-content: center;
  gap: 15px;
}

.wechat-login-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 20px;
  background-color: #f0fdf4;
  color: #07c160;
  border: 1px solid #b7f2cd;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.wechat-login-btn:hover {
  background-color: #e6fcf0;
  border-color: #07c160;
}

.wechat-icon {
  width: 18px;
  height: 18px;
}

.form-footer {
  text-align: center;
  font-size: 13px;
  color: #667085;
  margin-top: 10px;
}

.register-link {
  color: #409eff;
  text-decoration: none;
  font-weight: 500;
  transition: color 0.2s ease;
}

.register-link:hover {
  color: #337ecc;
  text-decoration: underline;
}

@media (max-width: 480px) {
  .login-card {
    padding: 30px 25px;
    margin: 0 20px;
  }

  .login-title {
    font-size: 22px;
  }

  .login-icon {
    width: 50px;
    height: 50px;
    svg {
      width: 28px;
      height: 28px;
    }
  }

  .get-code-btn {
    width: 100px;
    font-size: 12px;
  }
}
</style>