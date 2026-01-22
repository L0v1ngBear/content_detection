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
                    // 仅401场景保留提示（因为是全局跳转，非业务错误）
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

// 响应拦截器（核心修改：移除业务错误的 ElMessage 调用）
request.interceptors.response.use(
    response => {
        let res = response.data;
        if (response.config.responseType === 'blob') {
            return res;
        }
        if (typeof res === 'string') {
            res = res ? JSON.parse(res) : res;
        }

        // 业务失败时：只抛错，不弹提示
        if (res.code !== '200' && res.code !== 200) {
            // 401 单独处理（跳转登录，保留提示）
            if (res.code === '401' || res.code === 401) {
                ElMessage.error(res.msg || res.message || "登录已过期，请重新登录");
                localStorage.removeItem("accessToken");
                localStorage.removeItem("refreshToken");
                localStorage.removeItem("tokenExpireTimestamp");
                router.push('/login');
            }
            // 仅抛出错误，提示交给业务层
            return Promise.reject(new Error(res.msg || res.message || "请求失败"));
        }

        // 业务成功，返回数据
        return res;
    },
    error => {
        // HTTP 错误处理：只解析错误信息，不弹提示
        let errMsg = "";
        if (error.response?.status === 404) {
            errMsg = '未找到请求接口';
        } else if (error.response?.status === 500) {
            errMsg = '系统异常，请查看后端控制台报错';
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