<template>
  <div class="register-container">
    <!-- 背景装饰（和登录页一致） -->
    <div class="register-bg-decoration left"></div>
    <div class="register-bg-decoration right"></div>

    <!-- 注册卡片 -->
    <div class="register-card">
      <h2 class="register-title">用户注册</h2>
      <div class="register-icon">
        <svg viewBox="0 0 24 24" fill="#409eff">
          <path d="M15 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm-9-2c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm9 10c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm-9-2c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4z" />
        </svg>
      </div>

      <!-- 注册表单 -->
      <form class="register-form" @submit.prevent="handleRegister">
        <!-- 用户名 -->
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
              v-model="registerForm.username"
              placeholder="请输入用户名（3-16位字母/数字/下划线）"
              required
              maxlength="16"
          />
        </div>

        <!-- 手机号 -->
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
              v-model="registerForm.phone"
              placeholder="请输入11位手机号"
              required
              maxlength="11"
          />
        </div>

        <!-- 验证码 -->
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
                v-model="registerForm.verifyCode"
                placeholder="请输入6位验证码"
                required
                maxlength="6"
            />
            <button
                type="button"
                class="get-code-btn"
                @click="getSmsCode"
                :disabled="isCodeSending || registerForm.phone.length !== 11"
            >
              {{ isCodeSending ? `${countDown}s后重新获取` : '获取验证码' }}
            </button>
          </div>
        </div>

        <!-- 密码 -->
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
                v-model="registerForm.password"
                placeholder="请输入密码（6-16位，含字母+数字）"
                required
                minlength="6"
                maxlength="16"
                @input="checkPasswordStrength"
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
          <!-- 密码强度提示 -->
          <div class="password-strength" v-if="registerForm.password">
            <div class="strength-label">密码强度：</div>
            <div class="strength-bars">
              <div class="strength-bar" :class="strengthClass[0]"></div>
              <div class="strength-bar" :class="strengthClass[1]"></div>
              <div class="strength-bar" :class="strengthClass[2]"></div>
            </div>
            <div class="strength-text">{{ strengthText }}</div>
          </div>
        </div>

        <!-- 确认密码 -->
        <div class="form-item">
          <label for="confirmPassword" class="form-label">
            <svg class="label-icon" viewBox="0 0 24 24" fill="#666">
              <path d="M18 8h-1V6c0-2.76-2.24-5-5-5S7 3.24 7 6v2H6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V10c0-1.1-.9-2-2-2zM9 6c0-1.66 1.34-3 3-3s3 1.34 3 3v2H9V6zm9 14H6V10h12v10zm-6-3c1.1 0 2-.9 2-2s-.9-2-2-2-2 .9-2 2 .9 2 2 2z" />
            </svg>
            确认密码
          </label>
          <input
              :type="showPassword ? 'text' : 'password'"
              id="confirmPassword"
              class="form-input"
              v-model="registerForm.confirmPassword"
              placeholder="请再次输入密码"
              required
              minlength="6"
              maxlength="16"
          />
        </div>

        <!-- 协议勾选 -->
        <div class="form-extra">
          <label class="agreement-label">
            <input type="checkbox" v-model="registerForm.agreement" class="agreement-checkbox" required />
            <span class="agreement-text">
              我已阅读并同意
              <a href="#" class="agreement-link">《用户注册协议》</a>
              和
              <a href="#" class="agreement-link">《隐私政策》</a>
            </span>
          </label>
        </div>

        <!-- 注册按钮 -->
        <button type="submit" class="register-btn" :disabled="isSubmitting">
          <span v-if="!isSubmitting" class="btn-text">立即注册</span>
          <span v-else class="btn-loading">
            <svg class="loading-spinner" viewBox="0 0 24 24">
              <circle cx="12" cy="12" r="10" stroke="#fff" stroke-width="2" fill="none" stroke-dasharray="56.548" stroke-dashoffset="0" />
            </svg>
            注册中...
          </span>
        </button>

        <!-- 登录跳转 -->
        <div class="form-footer">
          已有账号？<a href="/login" class="login-link">立即登录</a>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onUnmounted } from 'vue';
import request from '../utils/request.js';
import router from '../router/index.js';
import { ElMessage } from 'element-plus';

// 基础响应式数据
const isSubmitting = ref(false);
const showPassword = ref(false);
const isCodeSending = ref(false);
const countDown = ref(60);
let countDownTimer = null;

// 密码强度相关
const strengthClass = ref(['', '', '']);
const strengthText = ref('');

// 注册表单数据
const registerForm = reactive({
  username: '',
  phone: '',
  verifyCode: '',
  password: '',
  confirmPassword: '',
  agreement: false // 协议勾选
});

// --------------- 短信验证码逻辑 ---------------
const getSmsCode = () => {
  // 手机号格式校验
  const phoneReg = /^1[3-9]\d{9}$/;
  if (!phoneReg.test(registerForm.phone)) {
    ElMessage.warning("请输入有效的11位手机号！");
    return;
  }

  // 标记发送状态，启动倒计时
  isCodeSending.value = true;
  startCountDown();

  // 调用注册验证码接口
  request.post('/auth/sendRegisSmsCode', { phone: registerForm.phone })
      .then(() => {
        ElMessage.success("验证码已发送，请注意查收！");
      })
      .catch(err => {
        ElMessage.error(err.message || "获取验证码失败，请重试！");
        console.error("获取注册验证码错误：", err);
        clearCountDown();
      });
};

// 倒计时逻辑
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

// --------------- 密码强度校验 ---------------
const checkPasswordStrength = () => {
  const password = registerForm.password;
  let level = 0;

  // 强度规则：1. 长度≥6 2. 含字母 3. 含数字
  if (password.length >= 6) level++;
  if (/[a-zA-Z]/.test(password)) level++;
  if (/[0-9]/.test(password)) level++;

  // 更新强度样式和文本
  switch (level) {
    case 0:
    case 1:
      strengthClass.value = ['weak', '', ''];
      strengthText.value = '弱（需包含字母+数字，长度≥6）';
      break;
    case 2:
      strengthClass.value = ['weak', 'medium', ''];
      strengthText.value = '中（建议增加复杂度）';
      break;
    case 3:
      strengthClass.value = ['weak', 'medium', 'strong'];
      strengthText.value = '强（密码安全）';
      break;
  }
};

// --------------- 表单校验 ---------------
const validateForm = () => {
  // 用户名校验（3-16位字母/数字/下划线）
  const usernameReg = /^[a-zA-Z0-9_]{3,16}$/;
  if (!usernameReg.test(registerForm.username)) {
    ElMessage.warning("用户名需为3-16位字母、数字或下划线！");
    return false;
  }

  // 手机号校验
  const phoneReg = /^1[3-9]\d{9}$/;
  if (!phoneReg.test(registerForm.phone)) {
    ElMessage.warning("请输入有效的11位手机号！");
    return false;
  }

  // 验证码校验
  if (!/^\d{6}$/.test(registerForm.verifyCode)) {
    ElMessage.warning("请输入有效的6位数字验证码！");
    return false;
  }

  // 密码校验
  const pwdReg = /^(?=.*[a-zA-Z])(?=.*\d)[a-zA-Z0-9]{6,16}$/;
  if (!pwdReg.test(registerForm.password)) {
    ElMessage.warning("密码需为6-16位，且包含字母和数字！");
    return false;
  }

  // 确认密码校验
  if (registerForm.password !== registerForm.confirmPassword) {
    ElMessage.warning("两次输入的密码不一致！");
    return false;
  }

  // 协议勾选校验
  if (!registerForm.agreement) {
    ElMessage.warning("请阅读并同意用户注册协议和隐私政策！");
    return false;
  }

  return true;
};

// --------------- 注册核心逻辑 ---------------
const handleRegister = async () => {
  // 前端表单校验
  if (!validateForm()) {
    return;
  }

  // 标记提交状态
  isSubmitting.value = true;

  try {
    // 调用注册接口
    const response = await request.post('/auth/register', {
      username: registerForm.username,
      phone: registerForm.phone,
      verifyCode: registerForm.verifyCode,
      password: registerForm.password
    });

    // 注册成功处理
    ElMessage.success("注册成功！即将跳转到登录页");
    console.log("注册返回数据：", response);

    // 3秒后跳转到登录页
    setTimeout(() => {
      router.push('/login');
    }, 3000);

  } catch (error) {
    // 注册失败处理
    console.error("注册请求失败：", error);
    ElMessage.error(error.message || "注册失败，请重试");

  } finally {
    // 重置提交状态
    isSubmitting.value = false;
  }
};

// --------------- 生命周期 ---------------
onUnmounted(() => {
  // 清除倒计时定时器，防止内存泄漏
  clearCountDown();
});
</script>

<style scoped>
/* 整体布局（和登录页风格统一） */
.register-container {
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

/* 背景装饰 */
.register-bg-decoration {
  position: absolute;
  width: 600px;
  height: 600px;
  border-radius: 50%;
  background: rgba(64, 158, 255, 0.05);
  z-index: 0;
}

.register-bg-decoration.left {
  top: -300px;
  left: -300px;
}

.register-bg-decoration.right {
  bottom: -300px;
  right: -300px;
}

/* 注册卡片 */
.register-card {
  width: 100%;
  max-width: 450px;
  padding: 40px 35px;
  background-color: #ffffff;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.06), 0 2px 8px rgba(0, 0, 0, 0.04);
  box-sizing: border-box;
  position: relative;
  z-index: 1;
  transition: all 0.3s ease;
}

.register-card:hover {
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.08), 0 4px 12px rgba(0, 0, 0, 0.06);
}

.register-title {
  text-align: center;
  color: #2c3e50;
  margin: 0 0 10px 0;
  font-size: 26px;
  font-weight: 700;
  letter-spacing: 1px;
}

.register-icon {
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

/* 表单样式 */
.register-form {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 20px;
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

/* 密码输入框 */
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

/* 密码强度提示 */
.password-strength {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-top: 4px;
}

.strength-label {
  font-size: 12px;
  color: #667085;
}

.strength-bars {
  display: flex;
  gap: 4px;
  height: 6px;
}

.strength-bar {
  flex: 1;
  border-radius: 3px;
  background-color: #e1e5eb;
}

.strength-bar.weak {
  background-color: #f53f3f;
}

.strength-bar.medium {
  background-color: #ff7d00;
}

.strength-bar.strong {
  background-color: #07c160;
}

.strength-text {
  font-size: 12px;
  color: #667085;
}

/* 验证码输入框 */
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

/* 协议勾选 */
.form-extra {
  width: 100%;
  margin-top: 5px;
}

.agreement-label {
  font-size: 13px;
  color: #667085;
  cursor: pointer;
  display: flex;
  align-items: flex-start;
  gap: 6px;
  transition: color 0.2s ease;
}

.agreement-label:hover {
  color: #409eff;
}

.agreement-checkbox {
  width: 14px;
  height: 14px;
  border-radius: 4px;
  border: 1px solid #e1e5eb;
  accent-color: #409eff;
  cursor: pointer;
  margin-top: 2px;
  flex-shrink: 0;
}

.agreement-text {
  user-select: none;
  line-height: 1.4;
}

.agreement-link {
  color: #409eff;
  text-decoration: none;
}

.agreement-link:hover {
  text-decoration: underline;
}

/* 注册按钮 */
.register-btn {
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

.register-btn:disabled {
  background-color: #a0cfff;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.register-btn:hover:not(:disabled) {
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

/* 底部跳转 */
.form-footer {
  text-align: center;
  font-size: 13px;
  color: #667085;
  margin-top: 10px;
}

.login-link {
  color: #409eff;
  text-decoration: none;
  font-weight: 500;
  transition: color 0.2s ease;
}

.login-link:hover {
  color: #337ecc;
  text-decoration: underline;
}

/* 响应式适配 */
@media (max-width: 480px) {
  .register-card {
    padding: 30px 25px;
    margin: 0 20px;
  }

  .register-title {
    font-size: 22px;
  }

  .register-icon {
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