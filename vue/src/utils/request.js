import axios from "axios";
import { ElMessage } from "element-plus";
import router from "../router/index.js";

// 创建请求实例
const request = axios.create({
    baseURL: 'http://localhost:8080',
    timeout: 30000
});

// 请求拦截器（保持原有逻辑，无需修改）
request.interceptors.request.use(
    async (config) => {
        const tokenExpireTimestamp = localStorage.getItem("tokenExpireTimestamp");
        const accessToken = localStorage.getItem("accessToken");
        const refreshToken = localStorage.getItem("refreshToken");

        if (accessToken) {
            config.headers['Authorization'] = `Bearer ${accessToken}`;
        }

        if (tokenExpireTimestamp && refreshToken) {
            const expireTime = Number(tokenExpireTimestamp);
            if (Date.now() > expireTime) {
                try {
                    const refreshRes = await request.post('/auth/refreshToken',
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
                    // 刷新Token失败（401），直接跳转登录
                    ElMessage.error("登录已过期，请重新登录");
                    localStorage.removeItem("accessToken");
                    localStorage.removeItem("refreshToken");
                    localStorage.removeItem("tokenExpireTimestamp");
                    router.push('/login');
                    return Promise.reject(refreshErr);
                }
            }
        }
        return config;
    },
    (error) => Promise.reject(error)
);

// 响应拦截器（核心优化：401移到error分支处理）
request.interceptors.response.use(
    response => {
        let res = response.data;
        if (response.config.responseType === 'blob') {
            return res;
        }
        if (typeof res === 'string') {
            res = res ? JSON.parse(res) : res;
        }

        // 仅处理「HTTP 200但业务码非200」的场景（非401）
        if (res.code !== '200' && res.code !== 200 && res.code !== 401) {
            // 业务失败：只抛错，提示交给业务层
            return Promise.reject(new Error(res.msg || res.message || "请求失败"));
        }

        // 业务成功（包括HTTP 200+业务码200），返回数据
        return res;
    },
    error => {
        // HTTP 错误处理（核心：处理401）
        let errMsg = "";
        const status = error.response?.status;

        // 1. 优先处理401（未登录/Token失效）
        if (status === 401) {
            errMsg = error.response?.data?.msg || "登录已过期，请重新登录";
            // 避免重复弹窗/跳转（加防抖）
            if (!window.isLoginExpired) {
                window.isLoginExpired = true;
                ElMessage.error(errMsg);
                // 清理本地Token
                localStorage.removeItem("accessToken");
                localStorage.removeItem("refreshToken");
                localStorage.removeItem("tokenExpireTimestamp");
                // // 跳转登录页（保留当前页面路径，登录后可返回）
                // router.push({
                //     path: '/login',
                //     query: { redirect: router.currentRoute.fullPath }
                // });
                // 重置防抖标记
                setTimeout(() => {
                    window.isLoginExpired = false;
                }, 1000);
            }
        }
        // 2. 处理其他HTTP错误
        else if (status === 404) {
            errMsg = '未找到请求接口';
        } else if (status === 500) {
            errMsg = '系统异常';
        } else if (error.message.includes('timeout')) {
            errMsg = '请求超时，请稍后重试';
        } else {
            errMsg = error.message || "网络异常，请检查网络";
        }

        // 抛出包含自定义信息的 Error 实例
        return Promise.reject(new Error(errMsg));
    }
);

export default request;