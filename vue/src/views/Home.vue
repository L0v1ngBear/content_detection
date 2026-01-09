<template>
  <div class="ai-detect-home">
    <!-- 顶部Banner区（聚焦三类检测模态，添加装饰元素） -->
    <div class="home-banner">
      <div class="banner-bg-decoration"></div>
      <div class="banner-container">
        <h1 class="banner-title">违规内容智能检测平台</h1>
        <p class="banner-subtitle">精准识别文本、图片、视频中的血腥暴力、色情场景等违规内容，筑牢内容安全防线</p>
        <div class="banner-btns">
          <button class="btn-primary" @click="handleNavigate('text-detect')">立即检测</button>
          <!-- 新增：API开放平台按钮 -->
          <button class="btn-secondary" @click="handleNavigate('api-platform')">API开放平台</button>
        </div>
      </div>
    </div>

    <!-- 新增：实时检测数量展示区 -->
    <div class="home-realtime">
      <div class="realtime-container">
        <div class="realtime-title">
          <svg viewBox="0 0 24 24" fill="#409eff" class="realtime-icon">
            <path d="M12 2C6.5 2 2 6.5 2 12s4.5 10 10 10 10-4.5 10-10S17.5 2 12 2zm0 18c-4.4 0-8-3.6-8-8s3.6-8 8-8 8 3.6 8 8-3.6 8-8 8zm-1-13h2v6h-2zm0 8h2v2h-2z" />
          </svg>
          <h3>实时检测数据</h3>
        </div>
        <div class="realtime-content">
          <div class="realtime-count-item">
            <p class="realtime-count-label">累计实时检测量</p>
            <p class="realtime-count-value">{{ realtimeDetectCount }}</p>
          </div>
          <div class="realtime-rate-item">
            <p class="realtime-rate-label">当前检测速率</p>
            <p class="realtime-rate-value">{{ realtimeDetectRate }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 核心数据指标区（更新支持模态数量，优化卡片样式） -->
    <div class="home-stats">
      <div class="stats-container">
        <div class="stat-item" v-for="stat in statsData" :key="stat.id">
          <p class="stat-value">{{ stat.value }}</p>
          <p class="stat-label">{{ stat.label }}</p>
          <p class="stat-desc">{{ stat.desc }}</p>
          <div class="stat-line"></div>
        </div>
      </div>
    </div>

    <!-- 核心功能区（移除文本检测卡片，仅保留图片、视频检测） -->
    <div class="home-features">
      <div class="section-header">
        <h2 class="section-title">核心违规检测能力</h2>
        <p class="section-desc">覆盖多场景、多模态的违规内容识别，满足各类内容安全审核需求</p>
      </div>
      <div class="features-container">
        <!-- 移除：文本违规检测卡片 -->

        <!-- 图片违规检测卡片 -->
        <div class="feature-card" @click="handleNavigate('image-detect')">
          <div class="feature-icon image-icon">
            <svg viewBox="0 0 24 24" fill="#409eff" class="icon-svg">
              <path d="M21 19V5c0-1.1-.9-2-2-2H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2zM8.5 13.5l2.5 3.01L14.5 12l4.5 6H5l3.5-4.5z" />
            </svg>
          </div>
          <h3 class="feature-title">图片违规检测</h3>
          <p class="feature-desc">识别图片中的血腥暴力、色情场景、敏感画面、违禁图案等，支持主流图片格式，精准判断内容合规性</p>
          <div class="feature-tags">
            <span class="tag">像素级分析</span>
            <span class="tag">快速检测</span>
            <span class="tag">多格式支持</span>
          </div>
          <div class="feature-arrow">
            <svg viewBox="0 0 24 24" fill="#409eff" class="arrow-svg">
              <path d="M12 2L10.59 3.41 16.17 9H2v2h14.17l-5.58 5.59L12 22l8-8z" />
            </svg>
          </div>
        </div>

        <!-- 视频违规检测卡片 -->
        <div class="feature-card" @click="handleNavigate('video-detect')">
          <div class="feature-icon video-icon">
            <svg viewBox="0 0 24 24" fill="#409eff" class="icon-svg">
              <path d="M17 10.5V7c0-.55-.45-1-1-1H4c-.55 0-1 .45-1 1v10c0 .55.45 1 1 1h12c.55 0 1-.45 1-1v-3.5l4 4v-11l-4 4z" />
            </svg>
          </div>
          <h3 class="feature-title">视频违规检测</h3>
          <p class="feature-desc">逐帧分析视频画面中的血腥暴力、色情场景，全面排查视频合规性，支持长视频批量检测</p>
          <div class="feature-tags">
            <span class="tag">帧级分析</span>
            <span class="tag">实时检测</span>
            <span class="tag">高准确率</span>
          </div>
          <div class="feature-arrow">
            <svg viewBox="0 0 24 24" fill="#409eff" class="arrow-svg">
              <path d="M12 2L10.59 3.41 16.17 9H2v2h14.17l-5.58 5.59L12 22l8-8z" />
            </svg>
          </div>
        </div>
      </div>
    </div>

    <!-- 产品优势区（更新多模态描述，优化图标和卡片） -->
    <div class="home-advantages">
      <div class="section-header">
        <h2 class="section-title">平台核心优势</h2>
        <p class="section-desc">依托先进算法与海量违规样本数据，打造行业领先的内容安全检测能力</p>
      </div>
      <div class="advantages-container">
        <div class="advantage-item" v-for="adv in advantagesData" :key="adv.id">
          <div class="advantage-icon" :style="{ backgroundColor: adv.color }">
            <svg viewBox="0 0 24 24" fill="#ffffff" class="adv-icon-svg">
              <path :d="adv.iconPath" />
            </svg>
          </div>
          <h3 class="advantage-title">{{ adv.title }}</h3>
          <p class="advantage-desc">{{ adv.desc }}</p>
        </div>
      </div>
    </div>

    <!-- 底部CTA区（优化渐变和按钮质感） -->
    <div class="home-cta">
      <div class="cta-bg-decoration"></div>
      <div class="cta-container">
        <h2 class="cta-title">开启你的内容安全检测之旅</h2>
        <p class="cta-desc">简单操作，即刻获取精准违规检测结果，守护平台内容安全与合规</p>
        <button class="btn-primary btn-large" @click="handleNavigate('text-detect')">立即免费检测</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import {ref, onMounted, onUnmounted} from 'vue';
import { useRouter } from 'vue-router';
import request from "../utils/request.js";

// 路由实例
const router = useRouter();

// TODO
// 新增：实时检测数量（模拟数据，后续可替换为接口/Redis获取）
const realtimeDetectCount = ref(320000); // 实时检测总数
const realtimeDetectRate = ref("3.2/秒"); // 实时检测速率（增强实时感）

// 定义定时任务变量，方便后续销毁
let countTimer = null;

// 封装接口请求函数（复用性更强，便于处理异常）
const fetchRealtimeCount = async () => {
  try {
    // 发起 GET 请求获取最新数据
    const res = await request.get("/api/now-count");
    // 接口返回成功，更新数据（优先使用接口返回值，兜底为 0）
    realtimeDetectCount.value = res.data || 0;
  } catch (error) {
    // 捕获接口请求异常（网络错误、接口报错等）
    console.error("获取实时检测数量失败：", error);
    // 可选：异常时保持当前数据，或重置为 0
    // realtimeDetectCount.value = 0;
  }
};

onMounted(() => {
  // 1. 初始化时立即请求一次接口（避免页面加载初期数据为 0）
  // fetchRealtimeCount();

  // 2. 设置定时任务，每隔 N 秒请求一次接口（此处设置 5 秒，可根据需求调整）
  countTimer = setInterval(() => {
    // fetchRealtimeCount();

    // 若不需要本地增长，仅保留接口真实数据，删除以下这行即可
    realtimeDetectCount.value += Math.floor(Math.random() * 10) + 5;

  }, 5000); // 5000 毫秒 = 5 秒，可修改为你需要的间隔时间（如 3000 = 3 秒）
});

// 3. 组件销毁时清除定时任务
onUnmounted(() => {
  if (countTimer) {
    clearInterval(countTimer);
  }
});
// 核心数据指标（更新支持模态数量，若需同步修改为2种，可调整此处id=3的内容）
const statsData = ref([
  {
    id: 1,
    value: "99.2%",
    label: "违规识别准确率",
    desc: "多维度特征分析，精准识别各类违规内容，误判率低"
  },
  {
    id: 2,
    value: "3000万+",
    label: "累计违规检测量",
    desc: "海量违规样本训练，模型持续优化，覆盖场景更全面"
  },
  {
    id: 3,
    value: "2种", // 若需严格匹配当前检测能力（图片、视频），可改为"2种"
    label: "支持检测模态",
    desc: "图片、视频违规内容全覆盖" // 若需同步修改，可改为"图片、视频违规内容全覆盖"
  },
  {
    id: 4,
    value: "秒级",
    label: "检测响应速度",
    desc: "实时反馈检测结果，高效处理内容安全审核需求"
  }
]);

// 核心优势数据
const advantagesData = ref([
  {
    id: 1,
    title: "精准识别",
    desc: "基于大模型技术与海量违规标注样本，有效识别血腥暴力、色情低俗等各类违规内容，误判率低",
    iconPath: "M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z",
    color: "#409eff"
  },
  {
    id: 2,
    title: "多模态支持",
    desc: "全面覆盖文本、图片、视频等多种内容形式，满足社区、电商、媒体等多场景违规检测需求",
    iconPath: "M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-4.44 15.21c-.24.14-.54.14-.78 0l-2.74-1.69c-.36-.21-.58-.57-.58-.96V10.2c0-.39.22-.75.58-.96l2.74-1.69c.24-.14.54-.14.78 0l2.74 1.69c.36.21.58.57.58.96v3.46c0 .39-.22.75-.58.96l-2.74 1.69zM12 13.4l1.73-1.07V10.2l-1.73 1.07V13.4z",
    color: "#67c23a"
  },
  {
    id: 3,
    title: "高效便捷",
    desc: "操作流程简单，支持批量上传与API集成，适配企业级内容安全审核与个人快速检测场景",
    iconPath: "M19 3h-4.18C14.4 1.84 13.3 1 12 1c-1.3 0-2.4.84-2.82 2H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-7 0c.55 0 1 .45 1 1s-.45 1-1 1-1-.45-1-1 .45-1 1-1zm2 14H7v-2h7v2zm3-4H7v-2h10v2zm0-4H7V7h10v2z",
    color: "#f7ba1e"
  },
  {
    id: 4,
    title: "安全合规",
    desc: "数据加密传输与存储，严格遵循隐私合规要求，保障用户检测数据安全与平台内容合规性",
    iconPath: "M12 15c1.66 0 2.99-1.34 2.99-3L15 6c0-1.66-1.34-3-3-3S9 4.34 9 6v6c0 1.66 1.34 3 3 3zm5.3-3c0 3-2.54 5.1-5.3 5.1S6.7 15 6.7 12H5c0 3.42 2.72 6.23 6 6.72V21h2v-2.28c3.28-.48 6-3.3 6-6.72h-1.7z",
    color: "#f56c6c"
  }
]);

// 跳转功能页面（新增api-platform路由支持）
const handleNavigate = (path) => {
  router.push(`/front/${path}`);
};
</script>

<style scoped>
/* 全局样式重置 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
  list-style: none;
}

.ai-detect-home {
  width: 100%;
  font-family: "PingFang SC", "Microsoft YaHei", sans-serif;
  color: #2c3e50;
  background-color: #ffffff;
  overflow-x: hidden;
}

/* 顶部Banner区 - 美化核心：渐变增强+装饰元素+层次感 */
.home-banner {
  width: 100%;
  background: linear-gradient(135deg, #e6f0ff 0%, #d4e4ff 50%, #bdd8ff 100%);
  padding: 100px 0;
  text-align: center;
  position: relative;
  overflow: hidden;
}

.banner-bg-decoration {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: radial-gradient(circle at 20% 30%, rgba(64, 158, 255, 0.1) 0%, transparent 20%),
  radial-gradient(circle at 80% 70%, rgba(51, 126, 204, 0.1) 0%, transparent 20%);
  z-index: 0;
}

.banner-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  position: relative;
  z-index: 1;
}

.banner-title {
  font-size: 48px;
  font-weight: 700;
  color: #1a2b48;
  margin-bottom: 24px;
  line-height: 1.2;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.banner-subtitle {
  font-size: 20px;
  color: #4e5d78;
  max-width: 800px;
  margin: 0 auto 48px;
  line-height: 1.8;
}

.banner-btns {
  display: flex;
  justify-content: center;
  gap: 24px;
}

.btn-primary {
  padding: 16px 36px;
  background-color: #409eff;
  color: #ffffff;
  border: none;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 6px 16px rgba(64, 158, 255, 0.25);
}

.btn-primary:hover {
  background-color: #337ecc;
  transform: translateY(-3px);
  box-shadow: 0 8px 24px rgba(64, 158, 255, 0.35);
}

.btn-secondary {
  padding: 16px 36px;
  background-color: #ffffff;
  color: #409eff;
  border: 2px solid #409eff;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.05);
}

.btn-secondary:hover {
  background-color: #f0f7ff;
  border-color: #337ecc;
  transform: translateY(-3px);
  box-shadow: 0 8px 24px rgba(64, 158, 255, 0.15);
}

/* 新增：实时检测数量展示区 - 贴合整体风格（配色、圆角、阴影、动画） */
.home-realtime {
  width: 100%;
  padding: 40px 0;
  background-color: #ffffff;
  border-bottom: 1px solid #f0f2f5;
}

.realtime-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.realtime-title {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
}

.realtime-icon {
  width: 24px;
  height: 24px;
}

.realtime-title h3 {
  font-size: 22px;
  font-weight: 600;
  color: #1a2b48;
}

.realtime-content {
  display: flex;
  gap: 60px;
  justify-content: center;
  flex-wrap: wrap;
  width: 100%;
  max-width: 800px;
}

.realtime-count-item, .realtime-rate-item {
  text-align: center;
  padding: 24px 36px;
  border-radius: 16px;
  background-color: #fafbfc;
  border: 1px solid #f0f2f5;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  min-width: 280px;
}

.realtime-count-item:hover, .realtime-rate-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 20px rgba(64, 158, 255, 0.08);
  border-color: #c6e2ff;
  background-color: #f0f7ff;
}

.realtime-count-label, .realtime-rate-label {
  font-size: 16px;
  color: #4e5d78;
  margin-bottom: 12px;
}

.realtime-count-value {
  font-size: 36px;
  font-weight: 700;
  color: #409eff;
  line-height: 1;
  /* 数字换行处理，适配小屏幕 */
  word-break: break-all;
}

.realtime-rate-value {
  font-size: 32px;
  font-weight: 600;
  color: #409eff;
  line-height: 1;
}

/* 核心数据指标区 - 美化核心：卡片增强+线条装饰+hover动画升级 */
.home-stats {
  width: 100%;
  padding: 80px 0;
  background-color: #fafbfc;
}

.stats-container {
  max-width: 1200px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 36px;
  padding: 0 20px;
}

.stat-item {
  text-align: center;
  padding: 32px 24px;
  border-radius: 16px;
  background-color: #ffffff;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.04);
  position: relative;
  overflow: hidden;
}

.stat-item:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 28px rgba(0, 0, 0, 0.08);
}

.stat-value {
  font-size: 42px;
  font-weight: 700;
  color: #409eff;
  margin-bottom: 12px;
  line-height: 1;
}

.stat-label {
  font-size: 18px;
  font-weight: 500;
  color: #1a2b48;
  margin-bottom: 8px;
}

.stat-desc {
  font-size: 14px;
  color: #667085;
  line-height: 1.6;
}

.stat-line {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 60px;
  height: 4px;
  background-color: #409eff;
  border-radius: 2px;
  transition: all 0.3s ease;
}

.stat-item:hover .stat-line {
  width: 100px;
}

/* 通用section头部样式 - 美化核心：文字间距+下划线装饰 */
.section-header {
  text-align: center;
  margin-bottom: 56px;
  position: relative;
}

.section-title {
  font-size: 36px;
  font-weight: 700;
  color: #1a2b48;
  margin-bottom: 16px;
  letter-spacing: 1px;
}

.section-title::after {
  content: "";
  display: block;
  width: 80px;
  height: 4px;
  background-color: #409eff;
  border-radius: 2px;
  margin: 16px auto 0;
}

.section-desc {
  font-size: 18px;
  color: #4e5d78;
  max-width: 800px;
  margin: 0 auto;
  line-height: 1.8;
}

/* 核心功能区 - 美化核心：卡片质感+箭头装饰+标签优化（已移除文本相关样式，无需额外修改） */
.home-features {
  width: 100%;
  padding: 100px 0;
  background-color: #ffffff;
}

.features-container {
  max-width: 1200px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 40px;
  padding: 0 20px;
}

.feature-card {
  background-color: #fafbfc;
  border-radius: 16px;
  padding: 40px 32px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
  border: 1px solid #f0f2f5;
  position: relative;
  overflow: hidden;
}

.feature-card:hover {
  transform: translateY(-10px);
  box-shadow: 0 16px 32px rgba(64, 158, 255, 0.1);
  border-color: #c6e2ff;
  background-color: #f0f7ff;
}

.feature-icon {
  width: 72px;
  height: 72px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 24px;
  transition: all 0.3s ease;
}

.feature-card:hover .feature-icon {
  transform: scale(1.05);
}

/* 移除：text-icon 样式（无需保留） */
.image-icon {
  background-color: rgba(103, 194, 58, 0.1);
}

.video-icon {
  background-color: rgba(245, 108, 108, 0.1);
}

.icon-svg {
  width: 36px;
  height: 36px;
}

.feature-title {
  font-size: 22px;
  font-weight: 600;
  color: #1a2b48;
  margin-bottom: 16px;
}

.feature-desc {
  font-size: 15px;
  color: #4e5d78;
  line-height: 1.8;
  margin-bottom: 24px;
}

.feature-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 24px;
}

.tag {
  padding: 6px 14px;
  background-color: #ffffff;
  color: #4e5d78;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.04);
  transition: all 0.3s ease;
}

.feature-card:hover .tag {
  background-color: rgba(255, 255, 255, 0.8);
  color: #409eff;
}

.feature-arrow {
  position: absolute;
  bottom: 32px;
  right: 32px;
  opacity: 0.5;
  transition: all 0.3s ease;
}

.feature-card:hover .feature-arrow {
  opacity: 1;
  transform: translateX(5px);
}

.arrow-svg {
  width: 20px;
  height: 20px;
}

/* 产品优势区 - 美化核心：图标放大+卡片间距+色彩增强 */
.home-advantages {
  width: 100%;
  padding: 100px 0;
  background-color: #fafbfc;
}

.advantages-container {
  max-width: 1200px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 48px;
  padding: 0 20px;
}

.advantage-item {
  text-align: center;
  padding: 24px;
  transition: all 0.3s ease;
}

.advantage-item:hover {
  transform: translateY(-5px);
}

.advantage-icon {
  width: 96px;
  height: 96px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 28px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
}

.advantage-item:hover .advantage-icon {
  transform: scale(1.05);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.12);
}

.adv-icon-svg {
  width: 40px;
  height: 40px;
}

.advantage-title {
  font-size: 22px;
  font-weight: 600;
  color: #1a2b48;
  margin-bottom: 16px;
}

.advantage-desc {
  font-size: 15px;
  color: #4e5d78;
  line-height: 1.8;
}

/* 底部CTA区 - 美化核心：渐变增强+装饰元素+按钮放大 */
.home-cta {
  width: 100%;
  padding: 120px 0;
  background: linear-gradient(135deg, #337ecc 0%, #409eff 50%, #5ba6ff 100%);
  text-align: center;
  color: #ffffff;
  position: relative;
  overflow: hidden;
}

.cta-bg-decoration {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: radial-gradient(circle at 30% 20%, rgba(255, 255, 255, 0.1) 0%, transparent 20%),
  radial-gradient(circle at 70% 80%, rgba(255, 255, 255, 0.1) 0%, transparent 20%);
  z-index: 0;
}

.cta-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 0 20px;
  position: relative;
  z-index: 1;
}

.cta-title {
  font-size: 38px;
  font-weight: 700;
  margin-bottom: 20px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.cta-desc {
  font-size: 20px;
  opacity: 0.9;
  margin-bottom: 56px;
  line-height: 1.8;
}

.btn-large {
  padding: 20px 48px;
  font-size: 18px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}

/* 响应式适配 - 保持美观度不降级 */
@media (max-width: 768px) {
  .banner-title {
    font-size: 36px;
  }

  .banner-subtitle {
    font-size: 18px;
  }

  .section-title {
    font-size: 30px;
  }

  .section-desc {
    font-size: 16px;
  }

  .stat-value {
    font-size: 36px;
  }

  .feature-card {
    padding: 32px 24px;
  }

  .cta-title {
    font-size: 30px;
  }

  .cta-desc {
    font-size: 18px;
  }

  .realtime-count-value {
    font-size: 32px;
  }

  .realtime-rate-value {
    font-size: 28px;
  }
}

@media (max-width: 480px) {
  .banner-btns {
    flex-direction: column;
    gap: 16px;
  }

  .btn-primary,
  .btn-secondary {
    width: 100%;
  }

  .stats-container {
    grid-template-columns: 1fr;
  }

  .features-container {
    grid-template-columns: 1fr;
  }

  .advantages-container {
    grid-template-columns: 1fr;
  }

  .banner-title {
    font-size: 28px;
  }

  .cta-title {
    font-size: 26px;
  }

  .realtime-content {
    gap: 20px;
  }

  .realtime-count-item, .realtime-rate-item {
    min-width: 100%;
    padding: 20px 24px;
  }
}
</style>