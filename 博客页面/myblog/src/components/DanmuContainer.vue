<template>
  <div class="danmu-container">
    <!-- 循环渲染每一列 -->
    <div v-for="(column, columnIndex) in totalColumns" :key="columnIndex" class="danmu-column">
      <div v-for="(item, index) in getColumnDanmu(columnIndex)" :key="index"
           class="danmu-item" :style="getDanmuStyles(index)">
        <div class="danmu-box">{{ item }}</div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  props: {
    // 传递的弹幕数据
    danmuList: {
      type: Array,
      required: true
    },
    // 设置的列数
    totalColumns: {
      type: Number,
      default: 4
    },
    // 设置的每列最多弹幕数
    maxMessagesPerColumn: {
      type: Number,
      default: 8
    },
    // 设置的垂直间距
    verticalSpacing: {
      type: Number,
      default: 12
    },
    // 设置的水平间距
    horizontalSpacing: {
      type: Number,
      default: 25
    }
  },
  computed: {
    // 根据列数，获取每列的弹幕
    getColumnDanmu() {
      return (columnIndex) => {
        return this.danmuList.filter((_, index) => index % this.totalColumns === columnIndex);
      };
    }
  },
  methods: {
    // 计算每条弹幕的样式
    getDanmuStyles(index) {
      const column = index % this.totalColumns; // 弹幕所在列
      const row = Math.floor(index / this.totalColumns); // 弹幕所在行
      const top = row * this.verticalSpacing; // 垂直位置

      const animationDuration = 20; // 动画持续时间
      const animationDelay = (index % this.danmuList.length) * 1; // 动画延迟

      return {
        top: `${top}%`,
        // 移除 left 定位，让 CSS 动画控制水平位置
        animationDuration: `${animationDuration}s`,
        animationDelay: `${animationDelay}s`
      };
    }
  }
};
</script>

<style scoped>
.danmu-container {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 1;
  overflow: hidden;
  border-radius: 20px;
}

.danmu-column {
  position: relative;
  width: 25%;
  min-width: 200px;
  height: 100%;
  padding-top: 5%;
}

.danmu-item {
  position: absolute;
  left: 100vw; /* 从视口最右边开始 */
  font-size: 18px;
  white-space: nowrap;
  pointer-events: none;
  animation: moveDanmu linear infinite;
  font-family: 'Comic Sans MS', sans-serif;
  font-weight: bold;
  z-index: 2;
}

.danmu-box {
  background: linear-gradient(45deg, rgba(102, 126, 234, 0.9), rgba(118, 75, 162, 0.9));
  padding: 10px 18px;
  border-radius: 25px;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.4), 0 0 30px rgba(102, 126, 234, 0.5);
  display: inline-block;
  max-width: 300px;
  word-wrap: break-word;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.7), 0 0 20px rgba(255, 255, 255, 0.3);
  color: white;
  font-size: 16px;
  border: 1px solid rgba(255, 255, 255, 0.3);
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;
}

.danmu-box:hover {
  transform: scale(1.05);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.5), 0 0 40px rgba(102, 126, 234, 0.6);
}

@keyframes moveDanmu {
  0% {
    left: 100vw; /* 从视口最右边开始 */
    opacity: 0;
    transform: translateY(0);
  }
  5% {
    opacity: 1;
  }
  95% {
    opacity: 1;
  }
  100% {
    left: -100%; /* 移动到屏幕左边消失 */
    opacity: 0;
    transform: translateY(-10px);
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .danmu-column {
    width: 50%;
    min-width: 150px;
  }
  
  .danmu-box {
    font-size: 14px;
    padding: 8px 15px;
    max-width: 250px;
  }
}

@media (max-width: 480px) {
  .danmu-column {
    width: 100%;
    min-width: 100px;
  }
  
  .danmu-box {
    font-size: 13px;
    padding: 6px 12px;
    max-width: 200px;
  }
}

/* 移除所有可能的焦点黑边 */
.danmu-container *:focus,
.danmu-container *:focus-visible {
  outline: none !important;
  box-shadow: none !important;
}

/* 确保弹幕项目没有焦点样式 */
.danmu-item:focus,
.danmu-item:focus-visible {
  outline: none !important;
  box-shadow: none !important;
}
</style>
