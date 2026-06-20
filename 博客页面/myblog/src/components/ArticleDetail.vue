<template>
  <div class="article-detail">
    <div class="article-header">
      <h1 class="article-title">{{ article.title }}</h1>
    </div>

    <div class="article-content" v-html="processContent(article.content)" @click="handleMusicClick"></div>

    <!-- 全局音乐播放器（浮动按钮+侧边弹窗，组件内 teleport 至 body） -->
    <MusicPlayer ref="playerRef" />
  </div>
</template>

<script lang="ts">
import {ref} from "vue";
import MusicPlayer from './MusicPlayer.vue';
import { resolveMusicUrl } from '../utills/imageUtils.js';
import { MUSIC_BASE_URL } from '../config/env.js';

// 扩展环境变量类型
declare global {
  interface ImportMeta {
    env: Record<string, string>;
  }
}

export default {
  name: 'ArticleDetail',
  components: {
    MusicPlayer
  },
  props: {
    article: {
      type: Object,
      default: () => ({title: '', description: '', content: ''}),
    },
  },
  setup() {
    const playerRef = ref<InstanceType<typeof MusicPlayer> | null>(null)
    
    const processContent = (content: string) => {
      const decodeHtml = (str: string) => {
        if (!str) return ''
        return str
          .replace(/&lt;/g, '<')
          .replace(/&gt;/g, '>')
          .replace(/&amp;/g, '&')
          .replace(/&quot;/g, '"')
          .replace(/&#39;/g, "'")
      }
      const html = decodeHtml(content || '')
      // 将 <audio src> 渲染为音乐卡片
      const withCards = html.replace(/<audio[^>]*src=["']([^"']+)["'][^>]*>(?:<\/audio>)?/gi, (_m, src) => {
        const fileName = String(src).split('/').pop() || ''
        const musicFileName = fileName.split('.')[0]
        const fileExtension = fileName.split('.').pop() || 'mp3'
        const coverUrl = resolveMusicUrl(`${musicFileName}.${fileExtension}`, 'cover')
        
        // 构建完整的音频预览URL
        const previewUrl = resolveMusicUrl(fileName, 'preview')
        
        const name = musicFileName
        const generatedHtml = `
          <div class="music-card" data-preview-url="${previewUrl}" data-file-name="${fileName}" data-music-file-name="${musicFileName}">
            <div class="music-cover">
              <div class="image-placeholder"></div>
              <img src="${coverUrl}" alt="音乐封面" loading="lazy" onerror="this.onerror=null; this.src='/musicdefault.png'; this.classList.add('loaded'); this.previousElementSibling.style.display='none';" onload="this.classList.add('loaded'); this.previousElementSibling.style.display='none';">
              <div class="play-icon">▶</div>
            </div>
            <div class="music-info">
              <div class="music-name">${name}</div>
              <div class="music-duration">3:07</div>
            </div>
          </div>
        `
        
        return generatedHtml
      })
      // 将独立段落内仅含 <code> 的内容规范为 <pre><code>
      const normalized = withCards.replace(/<p>\s*<code>([\s\S]*?)<\/code>\s*<\/p>/gi, (_m, code) => {
        return `<pre><code>${code}</code></pre>`
      })
      return normalized
    }

    const handleMusicClick = (event: MouseEvent) => {
      const target = event.target as HTMLElement
      const card = target.closest('.music-card') as HTMLElement | null
      
      if (!card) return
      
      // 获取所有数据属性
      const previewUrl = card.getAttribute('data-preview-url') || ''
      const fileName = card.getAttribute('data-file-name') || ''
      const musicFileName = card.getAttribute('data-music-file-name') || ''
      const title = (card.querySelector('.music-name')?.textContent || '').trim()
      
      // 直接使用音乐卡片上显示的封面URL
      const coverImg = card.querySelector('.music-cover img')
      const cover = coverImg ? coverImg.src : ''
      
      // 通过暴露的方法把歌曲信息传给 MusicPlayer
      const player: any = playerRef.value as any
      if (player && typeof player.setTrack === 'function') {
        player.setTrack({ src: previewUrl, cover, title, artist: '' })
      }
    }

    return {
      processContent,
      handleMusicClick,
      playerRef,
    };
  }
};
</script>

<style>
.article-detail {
  background-color: #ffffff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  padding: 32px;
  width: 100%;
}

.article-title {
  font-size: 2rem;
  margin-bottom: 1rem;
  font-weight: bold;
  color: #333;
}

.article-content {
  font-size: 1rem;
  line-height: 1.8;
  color: #444;
}

/* 代码块与行内代码样式 */
.article-content pre,
.article-content code {
  background-color: #1e1e1e;
  color: #d4d4d4;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", "Courier New", monospace;
}

.article-content pre {
  padding: 12px 14px;
  border-radius: 8px;
  overflow: auto;
  white-space: pre;
}

.article-content code {
  padding: 2px 6px;
  border-radius: 4px;
}

/* 音乐卡片样式（点击后调用 MusicPlayer） */
.music-card {
  width: 180px !important;
  margin: 10px 0;
  cursor: pointer;
  border-radius: 8px;
  overflow: hidden;
  background: #fff;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  display: inline-block;
  margin-right: 15px;
  margin-bottom: 15px;
}

.music-card:hover { transform: translateY(-2px); box-shadow: 0 4px 15px rgba(0, 0, 0, 0.15); }

.music-cover { position: relative; width: 180px !important; height: 180px !important; overflow: hidden; background: #f5f5f5; display: flex; align-items: center; justify-content: center; }
.music-cover img { width: 100% !important; height: 100% !important; object-fit: cover !important; opacity: 0; transition: opacity 0.5s ease; max-width: none !important; }
.music-cover img.loaded { opacity: 1; }
.image-placeholder { position: absolute; top: 0; left: 0; width: 100%; height: 100%; background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%); background-size: 200% 100%; animation: shimmer 1.5s infinite; z-index: 1; }
@keyframes shimmer { 0% { background-position: -200% 0; } 100% { background-position: 200% 0; } }
.play-icon { position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%); width: 50px; height: 50px; background: rgba(255, 255, 255, 0.9); border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 24px; color: #333; opacity: 0; transition: opacity 0.3s ease; z-index: 2; }
.music-card:hover .play-icon { opacity: 1; }
.music-info { padding: 12px; background: #fff; }
.music-name { font-size: 14px; font-weight: 500; color: #333; margin-bottom: 4px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.music-duration { font-size: 12px; color: #666; }
</style>
