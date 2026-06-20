<template>
  <div class="video-management">
    <el-card class="video-card">
      <div class="header">
        <el-input
            v-model="searchQuery"
            placeholder="搜索视频"
            clearable
            @input="handleSearch"
            style="width: 300px"
        />
        <el-button type="primary" @click="fetchVideoList">刷新</el-button>
      </div>

      <div class="upload-section">
        <el-upload
            :action="uploadUrl"
            :headers="uploadHeaders"
            :data="uploadParams"
            :on-success="handleUploadSuccess"
            :on-error="handleUploadError"
            :before-upload="beforeUpload"
            :on-progress="handleUploadProgress"
            accept="video/*"
            class="upload-button"
            :show-file-list="false"
        >
          <template #trigger>
            <el-button type="primary" :loading="uploading">
              {{ uploading ? '上传中...' : '上传视频' }}
            </el-button>
          </template>
          <template #tip>
            <div class="el-upload__tip">支持 mp4, avi, mkv 等格式视频文件</div>
          </template>
        </el-upload>

        <el-progress v-if="uploading" :percentage="uploadProgress" :format="progressFormat" class="upload-progress" />
      </div>

      <el-table :data="filteredVideos" border style="width: 100%; margin-top: 20px;" v-loading="loading" :height="500">
        <el-table-column label="缩略图" width="120" align="center">
          <template #default="scope">
            <el-image
                :src="scope.row.thumbnailUrl || defaultThumbnail"
                :preview-src-list="[scope.row.thumbnailUrl || defaultThumbnail]"
                fit="cover" lazy class="thumbnail-image"
            >
              <template #placeholder><div class="image-placeholder"><el-icon><Loading /></el-icon></div></template>
              <template #error><div class="image-error"><el-icon><Picture /></el-icon></div></template>
            </el-image>
          </template>
        </el-table-column>

        <el-table-column prop="name" label="文件名称" show-overflow-tooltip/>

        <el-table-column label="操作" align="center" width="200">
          <template #default="scope">
            <div class="action-buttons">
              <el-button size="small" type="success" @click="previewVideo(scope.row)" class="action-button">预览</el-button>
              <el-button size="small" type="danger" @click="confirmDelete(scope.row)" class="action-button">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 视频预览弹窗 - 用 v-show 保证 video 元素始终在 DOM -->
      <div class="preview-overlay" v-show="previewDialogVisible" @click.self="stopVideo">
        <div class="preview-modal">
          <div class="preview-topbar">
            <div class="preview-title-wrap">
              <svg viewBox="0 0 24 24" width="16" height="16" fill="#e91e63">
                <path d="M8 5v14l11-7z"/>
              </svg>
              <span class="preview-title">{{ currentVideo?.name || '视频预览' }}</span>
            </div>
            <button class="preview-close-btn" @click="stopVideo">&#x2715;</button>
          </div>

          <div class="preview-player-wrap">
            <video
              ref="videoPlayer"
              class="video-js vjs-big-play-centered"
              controls
              preload="auto"
              playsinline
            ></video>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script>
import { ref, computed, onMounted, nextTick, onBeforeUnmount } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { Loading, Picture } from '@element-plus/icons-vue';
import videojs from 'video.js';
import 'video.js/dist/video-js.css';
import request from "../utills/request.js";

export default {
  name: "VideoManagement",
  components: { Loading, Picture },

  setup() {
    const loading = ref(false);
    const videoList = ref([]);
    const searchQuery = ref("");
    const previewDialogVisible = ref(false);
    const previewUrl = ref("");
    const defaultThumbnail = "/assets/default-thumbnail.png";
    const videoPlayer = ref(null);
    const currentVideo = ref(null);
    let player = null;

    const uploading = ref(false);
    const uploadProgress = ref(0);
    const uploadUrl = request.defaults.baseURL + "/upload";
    const uploadHeaders = {};
    const uploadParams = {type: "video"};

    const fetchVideoList = async () => {
      loading.value = true;
      try {
        const response = await request.get("/file/list", { params: {type: "video"} });
        if (response.code === 0 && response.data && Array.isArray(response.data)) {
          videoList.value = response.data.map(file => ({
            name: file.fileName,
            storedName: file.storedFileName,
            thumbnailUrl: file.thumbnailUrl || `${request.defaults.baseURL}/file/cover/video/${file.fileName}`,
            videoUrl: file.url,
          }));
        } else {
          videoList.value = [];
        }
      } catch (error) {
        ElMessage.error("获取视频列表失败");
      } finally {
        loading.value = false;
      }
    };

    const handleUploadProgress = (event) => {
      uploadProgress.value = Math.round((event.loaded / event.total) * 100);
    };

    const progressFormat = (percentage) => {
      return percentage === 100 ? '处理中...' : `${percentage}%`;
    };

    const handleUploadSuccess = async (response) => {
      uploading.value = false;
      uploadProgress.value = 0;
      try {
        if (typeof response === 'string') response = JSON.parse(response);
        if (response && response.url && response.originalFilename) {
          ElMessage.success("视频上传成功！");
          await generateVideoThumbnail(response.url, response.originalFilename);
          fetchVideoList();
        } else {
          throw new Error('未获取到文件URL或原始文件名');
        }
      } catch (error) {
        ElMessage.error(`上传失败：${error.message}`);
      }
    };

    const generateVideoThumbnail = async (videoUrl, originalFilename) => {
      try {
        const video = document.createElement('video');
        video.crossOrigin = 'anonymous';
        video.src = videoUrl;
        await new Promise((resolve, reject) => {
          video.addEventListener('loadedmetadata', resolve);
          video.addEventListener('error', reject);
        });
        video.currentTime = 1;
        await new Promise((resolve) => { video.addEventListener('seeked', resolve); });
        const canvas = document.createElement('canvas');
        canvas.width = video.videoWidth;
        canvas.height = video.videoHeight;
        canvas.getContext('2d').drawImage(video, 0, 0, canvas.width, canvas.height);
        const thumbnailFilename = originalFilename.replace(/\.[^/.]+$/, '') + '.jpg';
        canvas.toBlob(async (blob) => {
          if (blob) {
            const formData = new FormData();
            formData.append('file', blob, thumbnailFilename);
            formData.append('type', 'images');
            formData.append('preserveName', 'true');
            await request({ url: '/upload', method: 'post', data: formData });
          }
        }, 'image/jpeg', 0.8);
      } catch (error) {
        console.error('生成视频缩略图时出错:', error);
      }
    };

    const handleUploadError = (error) => {
      uploading.value = false;
      uploadProgress.value = 0;
      ElMessage.error(error?.message || "视频上传失败");
    };

    const beforeUpload = (file) => {
      if (!file.type.startsWith("video/")) { ElMessage.error("只能上传视频文件！"); return false; }
      if (file.size > 500 * 1024 * 1024) { ElMessage.error("文件大小不能超过 500MB！"); return false; }
      uploading.value = true;
      uploadProgress.value = 0;
      return true;
    };

    const confirmDelete = async (video) => {
      try {
        await ElMessageBox.confirm(`确认删除视频 "${video.name}" 吗？`, "提示", {
          confirmButtonText: "确定", cancelButtonText: "取消", type: "warning",
        });
        await deleteVideo(video);
      } catch {}
    };

    const deleteVideo = async (video) => {
      try {
        const response = await request.delete(`/file/delete/video/${video.storedName}`);
        if (response === "File deleted successfully.") {
          ElMessage.success("视频删除成功！");
          videoList.value = videoList.value.filter(v => v.storedName !== video.storedName);
        } else {
          throw new Error(response || "删除失败");
        }
      } catch (error) {
        ElMessage.error(error.message || "视频删除失败！");
      }
    };

    const previewVideo = async (video) => {
      currentVideo.value = video;
      previewUrl.value = `${video.videoUrl}?t=${new Date().getTime()}`;
      previewDialogVisible.value = true;

      // v-show 下元素已经在 DOM，nextTick 后直接初始化
      await nextTick();
      if (!videoPlayer.value) return;

      // 先销毁旧实例再初始化新的
      if (player) player.dispose();

      player = videojs(videoPlayer.value, {
        controls: true,
        autoplay: true,
        preload: 'auto',
        fluid: true,
        playbackRates: [0.5, 1, 1.5, 2],
        sources: [{ src: previewUrl.value, type: 'video/mp4' }],
      });
    };

    const stopVideo = () => {
      if (player) {
        player.dispose();
        player = null;
      }
      previewUrl.value = "";
      currentVideo.value = null;
      previewDialogVisible.value = false;
    };

    const filteredVideos = computed(() => {
      if (!searchQuery.value) return videoList.value;
      const query = searchQuery.value.toLowerCase();
      return videoList.value.filter(video => video.name.toLowerCase().includes(query));
    });

    onMounted(() => { fetchVideoList(); });
    onBeforeUnmount(() => { stopVideo(); });

    return {
      loading, videoList, searchQuery, previewDialogVisible, previewUrl,
      defaultThumbnail, videoPlayer, uploading, uploadProgress,
      uploadUrl, uploadHeaders, uploadParams, currentVideo,
      filteredVideos, fetchVideoList, handleUploadSuccess, handleUploadError,
      handleUploadProgress, beforeUpload, progressFormat,
      confirmDelete, previewVideo, stopVideo,
      handleSearch: () => {},
    };
  },
};
</script>

<style scoped>
.video-management { padding: 20px; }
.header { display: flex; align-items: center; gap: 10px; margin-bottom: 20px; }
.upload-section { padding: 20px 0; border-bottom: 1px solid #ebeef5; }
.upload-progress { margin-top: 10px; width: 300px; }
.el-upload__tip { color: #909399; font-size: 12px; margin-top: 8px; }
.thumbnail-image { width: 80px; height: 80px; border-radius: 4px; transition: transform 0.3s ease; }
.thumbnail-image:hover { transform: scale(1.05); }
.image-placeholder, .image-error { display: flex; justify-content: center; align-items: center; width: 80px; height: 80px; background: #f5f7fa; color: #909399; }
.action-buttons { display: flex; gap: 10px; justify-content: center; }
.action-button { padding: 8px 16px; }

/* 预览弹窗 */
.preview-overlay {
  position: fixed; inset: 0; z-index: 3000;
  background: rgba(0, 0, 0, 0.75);
  backdrop-filter: blur(12px);
  display: flex; align-items: center; justify-content: center;
  padding: 24px;
}
.preview-modal {
  width: 100%; max-width: 1100px; max-height: 90vh;
  background: #1a1a2e; border-radius: 16px; overflow: hidden;
  box-shadow: 0 24px 80px rgba(0,0,0,0.6), 0 0 0 1px rgba(255,255,255,0.06) inset;
  display: flex; flex-direction: column;
}
.preview-topbar {
  display: flex; align-items: center; justify-content: space-between;
  padding: 14px 20px; border-bottom: 1px solid rgba(255,255,255,0.06); flex-shrink: 0;
}
.preview-title-wrap { display: flex; align-items: center; gap: 10px; }
.preview-title {
  font-size: 15px; font-weight: 600; color: rgba(255,255,255,0.85);
  max-width: 500px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}
.preview-close-btn {
  width: 36px; height: 36px; border-radius: 10px;
  border: 1px solid rgba(255,255,255,0.15);
  background: rgba(255,255,255,0.1); color: rgba(255,255,255,0.8);
  cursor: pointer; font-size: 18px; line-height: 1;
  display: flex; align-items: center; justify-content: center;
  transition: all 0.25s; outline: none;
}
.preview-close-btn:hover {
  background: rgba(255,255,255,0.18); border-color: rgba(255,255,255,0.25); color: #fff;
}
.preview-player-wrap {
  flex: 1; background: #000; display: flex; align-items: center; justify-content: center;
  min-height: 400px;
}
</style>

<style>
/* Video.js 非 scoped */
.preview-overlay .video-js {
  width: 100% !important;
  max-height: 80vh;
  font-family: inherit;
}
.preview-overlay .vjs-control-bar {
  display: flex !important;
  background: rgba(0,0,0,0.65);
}
.preview-overlay .vjs-big-play-button {
  top: 50% !important; left: 50% !important;
  transform: translate(-50%, -50%);
  border-radius: 50% !important;
  width: 72px !important; height: 72px !important;
  line-height: 72px !important;
  border: 2px solid rgba(255,255,255,0.5) !important;
  background: rgba(0,0,0,0.45) !important;
}
.preview-overlay .vjs-big-play-button .vjs-icon-placeholder::before {
  font-size: 36px; line-height: 68px;
}
</style>
