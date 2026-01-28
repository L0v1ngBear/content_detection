import axios from "axios";
import { ElMessage } from "element-plus";
import router from "../router/index.js";

// 1. 创建「无拦截器」的基础实例（仅用于刷新Token，避免循环）
const baseRequest = axios.create({
    baseURL: 'http://localhost:8080',
    timeout: 30000
});

// 2. 创建业务请求实例（带拦截器）
const request = axios.create({
    baseURL: 'http://localhost:8080',
    timeout: 30000
});

// 3. 防抖标记（用闭包，避免污染window全局变量）
let isRefreshingToken = false;
let isLoginExpired = false;

// 4. 请求拦截器（核心：移除循环请求，简化逻辑）
request.interceptors.request.use(
    async (config) => {
        // 跳过刷新Token接口的拦截器（避免循环）
        if (config.url.includes('/auth/refreshToken')) {
            return config;
        }

        const tokenExpireTimestamp = localStorage.getItem("tokenExpireTimestamp");
        const accessToken = localStorage.getItem("accessToken");
        const refreshToken = localStorage.getItem("refreshToken");

        // 1. 基础Token设置
        if (accessToken) {
            config.headers['Authorization'] = `Bearer ${accessToken}`;
        }

        // 2. Token过期检测（仅检测，不在这里刷新，避免阻塞）
        if (tokenExpireTimestamp && refreshToken && !isRefreshingToken) {
            const expireTime = Number(tokenExpireTimestamp);
            // 提前30秒刷新（避免刚好过期时请求失败）
            const needRefresh = Date.now() > (expireTime - 30 * 1000);

            if (needRefresh) {
                isRefreshingToken = true; // 加锁，避免重复刷新
                try {
                    // 用baseRequest（无拦截器）刷新Token，避免循环
                    const refreshRes = await baseRequest.post(
                        '/auth/refreshToken',
                        { refreshToken },
                        { headers: { Authorization: '' } }
                    );

                    const { newAccessToken, newTokenExpireTimestamp } = refreshRes.data;
                    if (newAccessToken) {
                        localStorage.setItem("accessToken", newAccessToken);
                        localStorage.setItem("tokenExpireTimestamp", newTokenExpireTimestamp);
                        config.headers['Authorization'] = `Bearer ${newAccessToken}`;
                    }
                } catch (refreshErr) {
                    // 刷新失败：只标记，不在这里跳转（交给响应拦截器处理）
                    console.error('Token刷新失败：', refreshErr);
                } finally {
                    isRefreshingToken = false; // 解锁
                }
            }
        }

        return config;
    },
    (error) => {
        isRefreshingToken = false; // 异常时解锁
        return Promise.reject(error);
    }
);

// 5. 响应拦截器（简化逻辑，避免同步阻塞）
request.interceptors.response.use(
    response => {
        let res = response.data;
        // 处理blob类型响应
        if (response.config.responseType === 'blob') {
            return res;
        }
        // 处理字符串类型响应
        if (typeof res === 'string') {
            res = res ? JSON.parse(res) : res;
        }
        // 业务码非200时抛错（交给业务层处理）
        if (res.code !== '200' && res.code !== 200) {
            return Promise.reject(new Error(res.msg || res.message || "请求失败"));
        }
        return res;
    },
    error => {
        let errMsg = "";
        const status = error.response?.status;

        // 处理401（登录过期）
        if (status === 401 && !isLoginExpired) {
            isLoginExpired = true;
            errMsg = error.response?.data?.msg || "登录已过期，请重新登录";
            ElMessage.error(errMsg);
            // 清理Token
            localStorage.removeItem("accessToken");
            localStorage.removeItem("refreshToken");
            localStorage.removeItem("tokenExpireTimestamp");
            // 延迟跳转（避免同步阻塞）
            setTimeout(() => {
                router.push({
                    path: '/login',
                    query: { redirect: router.currentRoute?.fullPath || '/' }
                });
                isLoginExpired = false;
            }, 500);
        }
        // 其他错误类型
        else if (status === 404) {
            errMsg = '未找到请求接口';
        } else if (status === 500) {
            errMsg = '系统异常';
        } else if (error.message.includes('timeout')) {
            errMsg = '请求超时，请稍后重试';
        } else {
            errMsg = error.message || "网络异常，请检查网络";
        }

        // 非401错误提示（避免重复弹窗）
        if (status !== 401) {
            ElMessage.error(errMsg);
        }

        return Promise.reject(new Error(errMsg));
    }
);

export default request;