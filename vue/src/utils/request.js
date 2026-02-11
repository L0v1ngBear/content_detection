import axios from "axios";
import { ElMessage, ElMessageBox } from "element-plus";
import router from "../router/index.js";

// ===================== 基础配置 =====================
// 1. 创建「无拦截器」的基础实例（仅用于刷新Token，避免循环拦截）
const baseRequest = axios.create({
    baseURL: 'http://localhost:8080', // 后端接口基础地址
    timeout: 30000, // 请求超时时间
    headers: {
        'Content-Type': 'application/json;charset=utf-8'
    }
});

// 2. 创建业务请求实例（带拦截器）
const request = axios.create({
    baseURL: 'http://localhost:8080',
    timeout: 30000,
    headers: {
        'Content-Type': 'application/json;charset=utf-8'
    }
});

// ===================== 状态管理 =====================
// 防抖/锁标记：避免重复刷新Token、重复弹窗
let isRefreshingToken = false; // Token刷新中标记
let requestQueue = []; // 等待Token刷新的请求队列
let isLoginExpired = false; // 登录过期跳转锁
let lastErrorTime = 0; // 最后一次错误提示时间
const ERROR_DEBOUNCE_TIME = 1000; // 1秒内同一类型错误只提示一次

// ===================== 辅助函数 =====================
/**
 * 重试队列中的请求（Token刷新成功后调用）
 * @param {string} newToken 新的accessToken
 */
const retryQueuedRequests = (newToken) => {
    requestQueue.forEach(callback => callback(newToken));
    requestQueue = []; // 清空队列
};

/**
 * 清理本地Token（复用函数）
 */
const clearToken = () => {
    localStorage.removeItem("accessToken");
    localStorage.removeItem("refreshToken");
    localStorage.removeItem("tokenExpireTimestamp");
};

/**
 * 精准解析后端错误信息
 * @param {object} error axios错误对象
 * @returns {string} 精准的错误提示信息
 */
const parseBackendErrorMsg = (error) => {
    const response = error.response;
    const responseData = response?.data || {};
    // 优先级：后端msg > 后端message > axios错误信息 > 兜底提示
    return responseData.msg || responseData.message || error.message || "请求失败，请稍后重试";
};

// ===================== 请求拦截器 =====================
request.interceptors.request.use(
    async (config) => {
        // 跳过刷新Token接口的拦截器（避免循环请求）
        if (config.url?.includes('/auth/refreshToken')) {
            return config;
        }

        // 读取本地Token相关信息
        const tokenExpireTimestamp = localStorage.getItem("tokenExpireTimestamp");
        const accessToken = localStorage.getItem("accessToken");
        const refreshToken = localStorage.getItem("refreshToken");

        // 1. 设置请求头Token
        if (accessToken) {
            config.headers['Authorization'] = `Bearer ${accessToken}`;
        }

        // 2. Token过期检测（提前30秒刷新）
        const expireTime = Number(tokenExpireTimestamp);
        const isExpireTimeValid = !isNaN(expireTime) && expireTime > 0; // 校验过期时间有效性
        const needRefresh = isExpireTimeValid
            && refreshToken
            && !isRefreshingToken
            && Date.now() > (expireTime - 30 * 1000);

        if (needRefresh) {
            isRefreshingToken = true; // 加锁，避免重复刷新
            // 返回Promise，让当前请求等待Token刷新完成
            return new Promise((resolve, reject) => {
                // 将当前请求的resolve/reject加入队列
                requestQueue.push((newToken) => {
                    if (newToken) {
                        // 刷新成功：更新请求头Token
                        config.headers['Authorization'] = `Bearer ${newToken}`;
                        resolve(config);
                    } else {
                        // 刷新失败：拒绝请求
                        reject(new Error(parseBackendErrorMsg({ message: 'Token刷新失败' })));
                    }
                });

                // 执行Token刷新请求（使用无拦截器的baseRequest）
                baseRequest.post('/auth/refreshToken', { refreshToken }, { headers: { Authorization: '' } })
                    .then(refreshRes => {
                        const { newAccessToken, newTokenExpireTimestamp } = refreshRes.data || {};
                        if (newAccessToken && newTokenExpireTimestamp) {
                            // 刷新成功：更新本地Token
                            localStorage.setItem("accessToken", newAccessToken);
                            localStorage.setItem("tokenExpireTimestamp", newTokenExpireTimestamp);
                            retryQueuedRequests(newAccessToken);
                        } else {
                            throw new Error('Token刷新返回数据异常');
                        }
                    })
                    .catch(refreshErr => {
                        console.error('Token刷新失败：', refreshErr);
                        retryQueuedRequests(null);
                        clearToken(); // 刷新失败，清理本地Token
                    })
                    .finally(() => {
                        isRefreshingToken = false; // 解锁
                    });
            });
        }

        return config;
    },
    (error) => {
        isRefreshingToken = false; // 异常时解锁
        return Promise.reject(new Error(parseBackendErrorMsg(error)));
    }
);

// ===================== 响应拦截器 =====================
request.interceptors.response.use(
    response => {
        let res = response.data;

        // 1. 处理blob类型响应（文件下载场景）
        if (response.config.responseType === 'blob') {
            // 兼容：后端返回错误信息的blob（如权限不足时返回json格式的blob）
            if (res.type === 'application/json') {
                return new Response(res).text().then(text => {
                    const errData = JSON.parse(text);
                    return Promise.reject(new Error(errData.msg || '文件下载失败'));
                });
            }
            return res;
        }

        // 2. 处理字符串类型响应（后端直接返回字符串的情况）
        if (typeof res === 'string') {
            try {
                res = JSON.parse(res);
            } catch (e) {
                return Promise.reject(new Error('响应数据解析失败'));
            }
        }

        // 3. 业务码校验：非200则抛错（使用后端原始msg）
        const successCode = ['200', 200];
        if (!successCode.includes(res.code)) {
            const errMsg = res.msg || res.message || "请求失败";
            return Promise.reject(new Error(errMsg));
        }

        return res;
    },
    error => {
        // 防抖：避免短时间内重复弹出错误提示
        const now = Date.now();
        if (now - lastErrorTime < ERROR_DEBOUNCE_TIME) {
            return Promise.reject(new Error(parseBackendErrorMsg(error)));
        }
        lastErrorTime = now;

        const response = error.response;
        const status = response?.status;
        const errMsg = parseBackendErrorMsg(error); // 精准解析后端错误信息

        // 按HTTP状态码分类处理
        switch (status) {
            // 401：未授权（登录过期/Token无效/密码错误）
            case 401:
                if (!isLoginExpired) {
                    isLoginExpired = true;
                    ElMessageBox.confirm(errMsg, '登录状态异常', {
                        confirmButtonText: '重新登录',
                        cancelButtonText: '取消',
                        type: 'warning',
                        closeOnClickModal: false // 点击遮罩不关闭弹窗
                    }).then(() => {
                        clearToken(); // 清理Token
                        // 跳转登录页，携带当前页面路径（登录后可返回）
                        router.push({
                            path: '/login',
                            query: { redirect: router.currentRoute?.fullPath || '/' }
                        });
                    }).catch(() => {
                        isLoginExpired = false; // 取消跳转，重置标记
                    });
                }
                break;

            // 400：参数错误（如账号密码为空）
            case 400:
                ElMessage.warning(errMsg);
                break;

            // 403：权限拒绝（账号被锁定/无操作权限）
            case 403:
                ElMessage.error(errMsg);
                break;

            // 404：接口不存在
            case 404:
                ElMessage.error('请求接口不存在：' + errMsg);
                break;

            // 500：服务器内部错误（开发环境显示原始信息，生产环境隐藏）
            case 500:
                const showMsg = import.meta.env.DEV ? errMsg : '系统异常，请稍后重试';
                ElMessage.error(showMsg);
                // 开发环境打印详细错误日志
                if (import.meta.env.DEV) {
                    console.error('服务器内部错误详情：', error);
                }
                break;

            // 无状态码（网络超时/断网）
            case undefined:
                const netErrMsg = error.message.includes('timeout')
                    ? '请求超时，请检查网络或稍后重试'
                    : (error.message.includes('Network Error')
                        ? '网络异常，请检查网络连接'
                        : errMsg);
                ElMessage.error(netErrMsg);
                break;

            // 其他状态码（如405/408等）
            default:
                ElMessage.error(errMsg);
                break;
        }

        // 重置登录过期标记（非401场景）
        if (status !== 401) {
            isLoginExpired = false;
        }

        // 抛出自定义错误（携带精准msg）
        return Promise.reject(new Error(errMsg));
    }
);

export default request;