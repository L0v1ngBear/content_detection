<script>
// 引入ECharts核心模块及所需图表
import { ref, onMounted, onUnmounted } from 'vue';
import * as echarts from 'echarts/core';
import { PieChart, BarChart } from 'echarts/charts';
import { TitleComponent, TooltipComponent, LegendComponent, GridComponent } from 'echarts/components';
import { CanvasRenderer } from 'echarts/renderers';

import request from '../../utils/request.js';

// 注册ECharts所需组件
echarts.use([
  PieChart,
  BarChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
  CanvasRenderer
]);

export default {
  name: "Home",
  setup() {
    // 1. 定义图表容器引用（对应模板中的DOM元素）
    const pieChartRef = ref(null); // 饼图（检测种类分布）
    const barChartRef = ref(null); // 柱状图（检测数量统计）
    let pieChartInstance = null;
    let barChartInstance = null;

    // 2. 定义数据相关响应式变量（预留后端数据存储）
    const detectTypeData = ref([]); // 检测种类分布数据（饼图+本月柱状图）
    const lastMonthDetectData = ref([]); // 上月检测数据（柱状图专用）
    const chartLoading = ref(false); // 加载状态
    const chartError = ref(''); // 异常信息

    /**
     * 从后端获取检测统计图表数据（适配封装的 request 工具类）
     * @returns {Promise<void>}
     */
    // const fetchChartDataFromBackend = async () => {
    //   try {
    //     // ① 开启加载状态
    //     chartLoading.value = true;
    //     chartError.value = '';
    //
    //     const backendData = await request({
    //       url: '/api/detect/chart/statistics', // 你的后端接口路径（相对路径）
    //       method: 'get', // 请求方法：get/post/put/delete 等
    //       params: {
    //         // 可选：请求参数（如时间范围、筛选条件等）
    //         // startDate: '2026-01-01',
    //         // endDate: '2026-01-31'
    //       }
    //     });
    //     detectTypeData.value = backendData.detectTypeList || [];
    //     lastMonthDetectData.value = backendData.lastMonthData || [];
    //
    //     if (!detectTypeData.value.length) {
    //       throw new Error('后端返回检测种类数据为空');
    //     }
    //     if (detectTypeData.value.length !== lastMonthDetectData.value.length) {
    //       throw new Error('本月与上月数据长度不匹配');
    //     }
    //
    //   } catch (error) {
    //     // ⑥ 异常处理（捕获 request 工具类抛出的错误或数据格式错误）
    //     console.error('获取图表数据失败：', error);
    //     // 优先使用 request 工具类拦截器返回的错误信息，无则使用默认信息
    //     chartError.value = error.message || '获取统计数据失败，请稍后重试';
    //
    //     detectTypeData.value = [
    //       { name: '文本检测', value: 0, color: '#409eff' },
    //       { name: '图片检测', value: 0, color: '#67c23a' },
    //       { name: '视频检测', value: 0, color: '#f56c6c' }
    //     ];
    //     lastMonthDetectData.value = [0, 0, 0];
    //
    //   } finally {
    //     // ⑧ 关闭加载状态（无论成功/失败都执行）
    //     chartLoading.value = false;
    //   }
    // };

    // 4. 初始化图表方法（依赖后端获取的数据，无耦合）
    const initCharts = () => {
      // 数据为空时不初始化图表
      if (!detectTypeData.value.length) return;

      // 初始化饼图（检测种类分布）
      if (pieChartRef.value) {
        // 销毁旧实例，防止重复渲染
        if (pieChartInstance) pieChartInstance.dispose();
        pieChartInstance = echarts.init(pieChartRef.value);

        const pieOption = {
          title: {
            text: '检测种类分布',
            left: 'center',
            textStyle: {
              fontSize: 18,
              color: '#1a2b48'
            }
          },
          tooltip: {
            trigger: 'item',
            formatter: '{b}<br/>检测数量：{c}（{d}%）'
          },
          legend: {
            orient: 'vertical',
            left: 'left',
            top: 'center',
            textStyle: {
              color: '#4e5d78'
            }
          },
          series: [
            {
              name: '检测数量',
              type: 'pie',
              radius: ['40%', '70%'],
              center: ['55%', '50%'],
              avoidLabelOverlap: false,
              label: {
                show: false,
                position: 'center'
              },
              emphasis: {
                label: {
                  show: true,
                  fontSize: 16,
                  fontWeight: 'bold'
                }
              },
              labelLine: {
                show: false
              },
              data: detectTypeData.value,
              itemStyle: {
                color: (params) => params.data.color,
                borderRadius: 4,
                shadowBlur: 10,
                shadowColor: 'rgba(0, 0, 0, 0.05)'
              }
            }
          ]
        };
        pieChartInstance.setOption(pieOption);
      }

      // 初始化柱状图（检测数量统计）
      if (barChartRef.value) {
        // 销毁旧实例，防止重复渲染
        if (barChartInstance) barChartInstance.dispose();
        barChartInstance = echarts.init(barChartRef.value);

        const barOption = {
          title: {
            text: '检测数量统计',
            left: 'center',
            textStyle: {
              fontSize: 18,
              color: '#1a2b48'
            }
          },
          tooltip: {
            trigger: 'axis',
            axisPointer: {
              type: 'shadow'
            }
          },
          legend: {
            data: [
              {
                name: '本月检测',
                icon: 'rect',
                iconStyle: { color: '#409eff' }
              },
              {
                name: '上月检测',
                icon: 'rect',
                iconStyle: {
                  color: '#409eff',
                  opacity: 0.3
                }
              }],
            left: 'center',
            bottom: 0
          },
          grid: {
            left: '3%',
            right: '4%',
            bottom: '15%',
            top: '15%',
            containLabel: true
          },
          xAxis: [
            {
              type: 'category',
              data: detectTypeData.value.map(item => item.name),
              axisLine: {
                lineStyle: {
                  color: '#e6e9ed'
                }
              },
              axisLabel: {
                color: '#4e5d78'
              }
            }
          ],
          yAxis: [
            {
              type: 'value',
              name: '检测数量（条/张/个）',
              nameTextStyle: {
                color: '#4e5d78'
              },
              axisLine: {
                lineStyle: {
                  color: '#e6e9ed'
                }
              },
              axisLabel: {
                color: '#4e5d78'
              },
              splitLine: {
                lineStyle: {
                  color: '#f0f2f5'
                }
              }
            }
          ],
          series: [
            {
              name: '本月检测',
              type: 'bar',
              data: detectTypeData.value.map(item => item.value),
              barWidth: '30%',
              itemStyle: {
                color: (params) => detectTypeData.value[params.dataIndex].color,
                borderRadius: [4, 4, 0, 0]
              },
              emphasis: {
                itemStyle: {
                  shadowBlur: 10,
                  shadowColor: 'rgba(0, 0, 0, 0.1)'
                }
              }
            },
            {
              name: '上月检测',
              type: 'bar',
              data: lastMonthDetectData.value,
              barWidth: '30%',
              itemStyle: {
                color: (params) => {
                  let color = detectTypeData.value[params.dataIndex].color;
                  if (color.startsWith('#')) {
                    const hex = color.slice(1);
                    const r = parseInt(hex.slice(0, 2), 16);
                    const g = parseInt(hex.slice(2, 4), 16);
                    const b = parseInt(hex.slice(4, 6), 16);
                    return `rgba(${r}, ${g}, ${b}, 0.3)`;
                  }
                  if (color.startsWith('rgb')) {
                    return color.replace('rgb', 'rgba').replace(')', ', 0.3)');
                  }
                  return 'rgba(150, 150, 150, 0.3)';
                },
                borderRadius: [4, 4, 0, 0]
              },
              emphasis: {
                itemStyle: {
                  shadowBlur: 10,
                  shadowColor: 'rgba(0, 0, 0, 0.1)'
                }
              }
            }
          ]
        };
        barChartInstance.setOption(barOption);
      }
    };

    // 5. 窗口大小变化时，重置图表尺寸（保证响应式）
    const resizeCharts = () => {
      if (pieChartInstance) pieChartInstance.resize();
      if (barChartInstance) barChartInstance.resize();
    };

    // 6. 数据更新后重新渲染图表（解耦请求与渲染）
    const renderChartAfterDataUpdate = async () => {
      await fetchChartDataFromBackend();
      initCharts();
    };

    // 7. 生命周期钩子：组件挂载后获取数据并渲染图表
    onMounted(() => {
      renderChartAfterDataUpdate();
      window.addEventListener('resize', resizeCharts);
    });

    // 8. 组件卸载前，销毁图表实例（释放内存）
    onUnmounted(() => {
      window.removeEventListener('resize', resizeCharts);
      if (pieChartInstance) pieChartInstance.dispose();
      if (barChartInstance) barChartInstance.dispose();
    });

    // 9. 暴露给模板的变量
    return {
      pieChartRef,
      barChartRef,
      chartLoading,
      chartError
    };
  }
};
</script>

<template>
  <div class="home-chart-container">
    <!-- 加载状态提示 -->
    <div v-if="chartLoading" class="chart-loading">正在加载统计数据...</div>

    <!-- 异常信息提示 -->
    <div v-if="chartError && !chartLoading" class="chart-error">{{ chartError }}</div>

    <!-- 饼图：检测种类分布 -->
    <div v-if="!chartLoading && !chartError" class="chart-item">
      <div ref="pieChartRef" class="chart" style="width: 100%; height: 400px;"></div>
    </div>

    <!-- 柱状图：检测数量统计 -->
    <div v-if="!chartLoading && !chartError" class="chart-item">
      <div ref="barChartRef" class="chart" style="width: 100%; height: 400px;"></div>
    </div>
  </div>
</template>

<style scoped>
/* 图表容器整体样式 */
.home-chart-container {
  width: 100%;
  padding: 60px 20px;
  background-color: #ffffff;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(450px, 1fr));
  gap: 40px;
  max-width: 1200px;
  margin: 0 auto;
  box-sizing: border-box;
  position: relative;
}

/* 单个图表项样式 */
.chart-item {
  background-color: #fafbfc;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease-in-out;
}

.chart-item:hover {
  box-shadow: 0 8px 24px rgba(64, 158, 255, 0.1);
  transform: translateY(-5px);
}

/* 加载状态样式 */
.chart-loading {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-size: 16px;
  color: #409eff;
  z-index: 10;
}

/* 异常信息样式 */
.chart-error {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-size: 16px;
  color: #f56c6c;
  z-index: 10;
  text-align: center;
}

/* 响应式适配 */
@media (max-width: 768px) {
  .home-chart-container {
    grid-template-columns: 1fr;
    gap: 24px;
    padding: 40px 16px;
  }

  .chart {
    height: 350px !important;
  }
}

@media (max-width: 480px) {
  .chart {
    height: 300px !important;
  }

  .chart-item {
    padding: 16px;
  }
}
</style>