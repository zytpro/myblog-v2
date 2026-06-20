<template>
  <div ref="chartRef" style="width: 100%; height: 100%"></div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue'
import * as echarts from 'echarts'

interface ChartData {
  [key: string]: any
}

const props = defineProps<{
  data: ChartData
}>()

const chartRef = ref<HTMLElement | null>(null)
let chart: echarts.ECharts | null = null

const initChart = () => {
  if (chart) {
    chart.dispose()
  }
  if (chartRef.value) {
    chart = echarts.init(chartRef.value)
    chart.setOption(props.data)
  }
}

watch(() => props.data, (newValue) => {
  if (chart && newValue) {
    chart.setOption(newValue, true)
  }
}, { deep: true })

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

onUnmounted(() => {
  if (chart) {
    chart.dispose()
    chart = null
  }
  window.removeEventListener('resize', handleResize)
})
</script> 