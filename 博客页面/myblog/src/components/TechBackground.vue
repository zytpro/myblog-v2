<template>
  <canvas ref="canvasRef" class="tech-bg-canvas"></canvas>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

const canvasRef = ref(null)
let ctx = null
let animId = null
let particles = []
let mouseX = -1000
let mouseY = -1000
let width = 0
let height = 0

const CONFIG = {
  particleCount: 80,
  connectDist: 150,
  mouseRadius: 180,
  speed: 0.3,
  glowColor: '100, 200, 255',
  accentColor: '140, 100, 255',
}

class Particle {
  constructor() {
    this.reset()
  }

  reset() {
    this.x = Math.random() * width
    this.y = Math.random() * height
    this.vx = (Math.random() - 0.5) * CONFIG.speed
    this.vy = (Math.random() - 0.5) * CONFIG.speed
    this.radius = 1.2 + Math.random() * 2
    this.flicker = Math.random() * Math.PI * 2
  }

  update() {
    this.flicker += 0.02

    // 鼠标吸引
    const dx = mouseX - this.x
    const dy = mouseY - this.y
    const dist = Math.sqrt(dx * dx + dy * dy)
    if (dist < CONFIG.mouseRadius && dist > 0) {
      const force = (CONFIG.mouseRadius - dist) / CONFIG.mouseRadius * 0.04
      this.vx += dx / dist * force
      this.vy += dy / dist * force
    }

    // 阻尼
    this.vx *= 0.995
    this.vy *= 0.995

    this.x += this.vx
    this.y += this.vy

    // 边界回弹
    if (this.x < 0 || this.x > width) this.vx *= -1
    if (this.y < 0 || this.y > height) this.vy *= -1
  }

  draw(ctx) {
    const flickerAlpha = 0.6 + Math.sin(this.flicker) * 0.3
    ctx.beginPath()
    ctx.arc(this.x, this.y, this.radius, 0, Math.PI * 2)
    ctx.fillStyle = `rgba(${CONFIG.glowColor}, ${flickerAlpha})`
    ctx.fill()

    // 外发光
    ctx.beginPath()
    ctx.arc(this.x, this.y, this.radius * 3, 0, Math.PI * 2)
    ctx.fillStyle = `rgba(${CONFIG.glowColor}, ${flickerAlpha * 0.15})`
    ctx.fill()
  }
}

const initParticles = () => {
  particles = []
  for (let i = 0; i < CONFIG.particleCount; i++) {
    particles.push(new Particle())
  }
}

const animate = () => {
  if (!ctx || !canvasRef.value) return
  ctx.clearRect(0, 0, width, height)

  // 更新和绘制粒子
  for (const p of particles) {
    p.update()
    p.draw(ctx)
  }

  // 绘制连线
  for (let i = 0; i < particles.length; i++) {
    for (let j = i + 1; j < particles.length; j++) {
      const dx = particles[i].x - particles[j].x
      const dy = particles[i].y - particles[j].y
      const dist = Math.sqrt(dx * dx + dy * dy)
      if (dist < CONFIG.connectDist) {
        const alpha = (1 - dist / CONFIG.connectDist) * 0.35
        const midX = (particles[i].x + particles[j].x) / 2
        const midY = (particles[i].y + particles[j].y) / 2
        const distToMouse = Math.sqrt((midX - mouseX) ** 2 + (midY - mouseY) ** 2)

        // 靠近鼠标的连线用强调色
        const color = distToMouse < CONFIG.mouseRadius
          ? CONFIG.accentColor
          : CONFIG.glowColor

        ctx.beginPath()
        ctx.moveTo(particles[i].x, particles[i].y)
        ctx.lineTo(particles[j].x, particles[j].y)
        ctx.strokeStyle = `rgba(${color}, ${alpha})`
        ctx.lineWidth = 0.5
        ctx.stroke()
      }
    }
  }

  // 鼠标光标光晕
  if (mouseX > 0 && mouseY > 0) {
    const grad = ctx.createRadialGradient(mouseX, mouseY, 0, mouseX, mouseY, 120)
    grad.addColorStop(0, `rgba(${CONFIG.accentColor}, 0.15)`)
    grad.addColorStop(0.5, `rgba(${CONFIG.glowColor}, 0.06)`)
    grad.addColorStop(1, 'rgba(0,0,0,0)')
    ctx.fillStyle = grad
    ctx.beginPath()
    ctx.arc(mouseX, mouseY, 120, 0, Math.PI * 2)
    ctx.fill()
  }

  animId = requestAnimationFrame(animate)
}

const onMouseMove = (e) => {
  mouseX = e.clientX
  mouseY = e.clientY
}

const onResize = () => {
  if (!canvasRef.value) return
  const dpr = window.devicePixelRatio || 1
  width = window.innerWidth
  height = window.innerHeight
  canvasRef.value.width = width * dpr
  canvasRef.value.height = height * dpr
  canvasRef.value.style.width = width + 'px'
  canvasRef.value.style.height = height + 'px'
  ctx.setTransform(dpr, 0, 0, dpr, 0, 0)
  initParticles()
}

onMounted(() => {
  ctx = canvasRef.value.getContext('2d')
  onResize()
  window.addEventListener('mousemove', onMouseMove, { passive: true })
  window.addEventListener('resize', onResize)
  animate()
})

onUnmounted(() => {
  cancelAnimationFrame(animId)
  window.removeEventListener('mousemove', onMouseMove)
  window.removeEventListener('resize', onResize)
})
</script>

<style scoped>
.tech-bg-canvas {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
  background: radial-gradient(ellipse at 50% 50%, #0c1119 0%, #060a10 100%);
}
</style>
