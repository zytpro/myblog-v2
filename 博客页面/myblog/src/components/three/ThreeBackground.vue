<template>
  <div ref="container" class="three-bg"></div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue'
import * as THREE from 'three'

const container = ref<HTMLElement | null>(null)
let renderer: THREE.WebGLRenderer | null = null
let scene: THREE.Scene | null = null
let camera: THREE.PerspectiveCamera | null = null
let animationId = 0
let particleSystem: THREE.Points | null = null

const init = () => {
  if (!container.value) return

  const width = container.value.clientWidth
  const height = container.value.clientHeight || 300

  renderer = new THREE.WebGLRenderer({ antialias: true, alpha: true })
  renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2))
  renderer.setSize(width, height)
  container.value.appendChild(renderer.domElement)

  scene = new THREE.Scene()

  camera = new THREE.PerspectiveCamera(60, width / height, 0.1, 1000)
  camera.position.set(0, 0, 120)

  // 粒子球
  const particles = 1500
  const radius = 60
  const geometry = new THREE.BufferGeometry()
  const positions = new Float32Array(particles * 3)

  for (let i = 0; i < particles; i++) {
    // 黄金点分布在球面上
    const phi = Math.acos(1 - 2 * (i + 0.5) / particles)
    const theta = Math.PI * (1 + Math.sqrt(5)) * (i + 0.5)
    const x = radius * Math.cos(theta) * Math.sin(phi)
    const y = radius * Math.sin(theta) * Math.sin(phi)
    const z = radius * Math.cos(phi)
    positions[i * 3] = x
    positions[i * 3 + 1] = y
    positions[i * 3 + 2] = z
  }

  geometry.setAttribute('position', new THREE.BufferAttribute(positions, 3))

  const material = new THREE.PointsMaterial({
    size: 1.8,
    color: 0x91cc75,
    transparent: true,
    opacity: 0.9
  })

  particleSystem = new THREE.Points(geometry, material)
  scene.add(particleSystem)

  const onResize = () => {
    if (!container.value || !renderer || !camera) return
    const w = container.value.clientWidth
    const h = container.value.clientHeight || 300
    renderer.setSize(w, h)
    camera.aspect = w / h
    camera.updateProjectionMatrix()
  }
  window.addEventListener('resize', onResize)

  const animate = () => {
    animationId = requestAnimationFrame(animate)
    if (particleSystem) {
      particleSystem.rotation.y += 0.0018
      particleSystem.rotation.x += 0.0006
    }
    renderer?.render(scene!, camera!)
  }
  animate()

  // 清理函数
  onBeforeUnmount(() => {
    window.removeEventListener('resize', onResize)
    cancelAnimationFrame(animationId)
    if (renderer) {
      renderer.dispose()
      if (renderer.domElement && renderer.domElement.parentNode) {
        renderer.domElement.parentNode.removeChild(renderer.domElement)
      }
    }
    scene?.clear()
    particleSystem?.geometry.dispose()
    ;(particleSystem?.material as THREE.Material)?.dispose()
    renderer = null
    scene = null
    camera = null
    particleSystem = null
  })
}

onMounted(() => init())
</script>

<style scoped>
.three-bg {
  position: absolute;
  inset: 0;
  z-index: 0;
  pointer-events: none;
}
</style> 