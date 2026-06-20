<template>
  <canvas ref="canvasRef" class="cursor-particle-canvas"></canvas>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

const canvasRef = ref(null)
let ctx = null
let particles = []
let mouseX = 0
let mouseY = 0
let animId = null

const COLORS = [
  'rgba(102, 126, 234, <a>)',
  'rgba(118, 75, 162, <a>)',
  'rgba(240, 147, 251, <a>)',
  'rgba(245, 175, 25, <a>)',
  'rgba(72, 219, 251, <a>)',
  'rgba(255, 107, 107, <a>)',
  'rgba(29, 209, 161, <a>)',
]

class Particle {
  constructor(x, y) {
    this.x = x
    this.y = y
    this.vx = (Math.random() - 0.5) * 3
    this.vy = (Math.random() - 0.5) * 3
    this.life = 1
    this.decay = 0.008 + Math.random() * 0.025
    this.size = 2 + Math.random() * 3
    this.color = COLORS[Math.floor(Math.random() * COLORS.length)]
  }

  update() {
    this.x += this.vx
    this.y += this.vy
    this.vy += 0.03
    this.life -= this.decay
  }

  draw(ctx) {
    ctx.save()
    ctx.globalAlpha = this.life
    ctx.fillStyle = this.color.replace('<a>', String(this.life))
    ctx.beginPath()
    ctx.arc(this.x, this.y, this.size, 0, Math.PI * 2)
    ctx.fill()
    ctx.restore()
  }
}

const spawnParticles = (x, y, count = 2) => {
  for (let i = 0; i < count; i++) {
    particles.push(new Particle(x, y))
  }
}

const onMouseMove = (e) => {
  mouseX = e.clientX
  mouseY = e.clientY
  spawnParticles(mouseX, mouseY, 2)
}

const onTouchMove = (e) => {
  if (e.touches.length > 0) {
    mouseX = e.touches[0].clientX
    mouseY = e.touches[0].clientY
    spawnParticles(mouseX, mouseY, 1)
  }
}

const resize = () => {
  if (!canvasRef.value) return
  const dpr = window.devicePixelRatio || 1
  canvasRef.value.width = window.innerWidth * dpr
  canvasRef.value.height = window.innerHeight * dpr
  canvasRef.value.style.width = window.innerWidth + 'px'
  canvasRef.value.style.height = window.innerHeight + 'px'
  ctx.setTransform(dpr, 0, 0, dpr, 0, 0)
}

const animate = () => {
  if (!ctx || !canvasRef.value) return
  ctx.clearRect(0, 0, window.innerWidth, window.innerHeight)

  particles = particles.filter(p => p.life > 0)
  for (const p of particles) {
    p.update()
    p.draw(ctx)
  }

  animId = requestAnimationFrame(animate)
}

onMounted(() => {
  ctx = canvasRef.value.getContext('2d')
  resize()
  window.addEventListener('mousemove', onMouseMove, { passive: true })
  window.addEventListener('touchmove', onTouchMove, { passive: true })
  window.addEventListener('resize', resize)
  animate()
})

onUnmounted(() => {
  cancelAnimationFrame(animId)
  window.removeEventListener('mousemove', onMouseMove)
  window.removeEventListener('touchmove', onTouchMove)
  window.removeEventListener('resize', resize)
})
</script>

<style scoped>
.cursor-particle-canvas {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  pointer-events: none;
  z-index: 99999;
}
</style>
