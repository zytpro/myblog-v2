<template>
  <div ref="chartRef" style="width: 100%; height: 100%"></div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue'
import * as echarts from 'echarts'

const props = defineProps<{
  data: any
}>()

const chartRef = ref<HTMLElement | null>(null)
let chart: echarts.ECharts | null = null

// 初始化图表
const initChart = () => {
  if (chart) {
    chart.dispose()
  }
  if (chartRef.value) {
    chart = echarts.init(chartRef.value, null, {
      renderer: 'canvas',  // 使用 canvas 渲染器以获得更好的性能
    })
    chart.setOption(props.data)
  }
}

// 监听数据变化
watch(() => props.data, (newValue) => {
  if (chart && newValue) {
    chart.setOption(newValue)
  }
}, { deep: true })

// 监听窗口大小变化
const handleResize = () => {
  if (chart) {
    chart.resize({
      animation: {
        duration: 300
      }
    })
  }
}

onMounted(() => {
  initChart()
  window.addEventListener('resize', handleResize)
})

// 组件卸载时清理
onUnmounted(() => {
  if (chart) {
    chart.dispose()
    chart = null
  }
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.chart {
  width: 100%;
  height: 100%;
}
</style> 