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

      <!-- 上传区域 -->
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
            <div class="el-upload__tip">
              支持 mp4, avi, mkv 等格式视频文件
            </div>
          </template>
        </el-upload>

        <!-- 上传进度 -->
        <el-progress
            v-if="uploading"
            :percentage="uploadProgress"
            :format="progressFormat"
            class="upload-progress"
        />
      </div>

      <!-- 视频列表 -->
      <el-table
          :data="filteredVideos"
          border
          style="width: 100%; margin-top: 20px;"
          v-loading="loading"
          :height="500"
      >
        <el-table-column label="缩略图" width="120" align="center">
          <template #default="scope">
            <el-image
                :src="scope.row.thumbnailUrl || defaultThumbnail"
                :preview-src-list="[scope.row.thumbnailUrl || defaultThumbnail]"
                fit="cover"
                lazy
                class="thumbnail-image"
            >
              <template #placeholder>
                <div class="image-placeholder">
                  <el-icon><Loading /></el-icon>
                </div>
              </template>
              <template #error>
                <div class="image-error">
                  <el-icon><Picture /></el-icon>
                </div>
              </template>
            </el-image>
          </template>
        </el-table-column>

        <el-table-column prop="name" label="文件名称" show-overflow-tooltip/>

        <el-table-column label="操作" align="center" width="200">
          <template #default="scope">
            <div class="action-buttons">
              <el-button
                  size="small"
                  type="success"
                  @click="previewVideo(scope.row)"
                  class="action-button"
              >
                预览
              </el-button>
              <el-button
                  size="small"
                  type="danger"
                  @click="confirmDelete(scope.row)"
                  class="action-button"
              >
                删除
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 视频预览对话框 -->
      <el-dialog
          v-model="previewDialogVisible"
          width="80%"
          @close="stopVideo"
          destroy-on-close
          center
      >
        <template #header>
          <div class="dialog-header">
            <span>视频预览</span>
            <span class="video-name" v-if="currentVideo">- {{ currentVideo.name }}</span>
          </div>
        </template>

        <div class="video-container">
          <video
              ref="videoPlayer"
              controls
              preload="metadata"
              class="preview-video"
          >
            <source :src="previewUrl" type="video/mp4" />
            您的浏览器不支持 video 标签。
          </video>
        </div>

        <template #footer>
          <el-button @click="stopVideo">关闭</el-button>
        </template>
      </el-dialog>
    </el-card>
  </div>
</template>

<script>
import { ref, computed, onMounted, nextTick, onBeforeUnmount } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { Loading, Picture } from '@element-plus/icons-vue';
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

    // 上传相关状态
    const uploading = ref(false);
    const uploadProgress = ref(0);
    const uploadUrl = request.defaults.baseURL + "/upload";
    const uploadHeaders = {

    };
    const uploadParams = {type: "video"};

    // 获取视频列表
    const fetchVideoList = async () => {
      loading.value = true;
      try {
        const response = await request.get("/file/list", {
          params: {type: "video"},
        });

        if (response.code === 0) {
          if (response.data && Array.isArray(response.data)) {
            videoList.value = response.data.map(file => ({
              name: file.fileName,
              storedName: file.storedFileName,
              thumbnailUrl: file.thumbnailUrl || `${request.defaults.baseURL}/file/cover/video/${file.fileName}`,
              videoUrl: file.url,
            }));
          } else {
            videoList.value = [];
          }
        } else {
          ElMessage.error(response.message || "获取视频列表失败");
        }
      } catch (error) {
        ElMessage.error("获取视频列表失败");
        console.error("Error fetching video list:", error);
      } finally {
        loading.value = false;
      }
    };

    // 上传相关方法
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
        if (typeof response === 'string') {
          response = JSON.parse(response);
        }

        if (response && response.url && response.originalFilename) {
          ElMessage.success("视频上传成功！");
          
          // 生成视频缩略图
          await generateVideoThumbnail(response.url, response.originalFilename);
          
          fetchVideoList();
        } else {
          throw new Error('未获取到文件URL或原始文件名');
        }
      } catch (error) {
        console.error('Upload response error:', error);
        ElMessage.error(`上传失败：${error.message}`);
      }
    };

    // 生成视频缩略图
    const generateVideoThumbnail = async (videoUrl, originalFilename) => {
      try {
        // 创建视频元素
        const video = document.createElement('video');
        video.crossOrigin = 'anonymous';
        video.src = videoUrl;
        
        // 等待视频元数据加载完成
        await new Promise((resolve, reject) => {
          video.addEventListener('loadedmetadata', resolve);
          video.addEventListener('error', reject);
        });
        
        // 设置视频时间为1秒（可以根据需要调整）
        video.currentTime = 1;
        
        // 等待视频帧加载完成
        await new Promise((resolve) => {
          video.addEventListener('seeked', resolve);
        });
        
        // 创建Canvas元素
        const canvas = document.createElement('canvas');
        const width = video.videoWidth;
        const height = video.videoHeight;
        canvas.width = width;
        canvas.height = height;
        
        // 绘制视频帧到Canvas
        const ctx = canvas.getContext('2d');
        ctx.drawImage(video, 0, 0, width, height);
        
        // 使用原始文件名生成缩略图文件名
        const thumbnailFilename = originalFilename.replace(/\.[^/.]+$/, '') + '.jpg';
        
        // 将Canvas转换为Blob
        canvas.toBlob(async (blob) => {
          if (blob) {
            // 创建FormData对象
            const formData = new FormData();
            formData.append('file', blob, thumbnailFilename);
            formData.append('type', 'images');
            formData.append('preserveName', 'true'); // 保留原始文件名
            
            try {
              // 上传缩略图
              const thumbResponse = await request({
                url: '/upload',
                method: 'post',
                data: formData
              });
              
              if (thumbResponse && thumbResponse.url) {
                console.log('缩略图上传成功:', thumbResponse.url);
              } else {
                console.error('缩略图上传失败:', thumbResponse);
              }
            } catch (error) {
              console.error('上传缩略图时出错:', error);
            }
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
      const isVideo = file.type.startsWith("video/");
      if (!isVideo) {
        ElMessage.error("只能上传视频文件！");
        return false;
      }

      const maxSize = 500 * 1024 * 1024; // 500MB
      if (file.size > maxSize) {
        ElMessage.error("文件大小不能超过 500MB！");
        return false;
      }

      uploading.value = true;
      uploadProgress.value = 0;
      return true;
    };

    // 删除相关方法
    const confirmDelete = async (video) => {
      try {
        await ElMessageBox.confirm(
            `确认删除视频 "${video.name}" 吗？`,
            "提示",
            {
              confirmButtonText: "确定",
              cancelButtonText: "取消",
              type: "warning",
            }
        );
        await deleteVideo(video);
      } catch {
        // 用户取消删除
      }
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
        console.error("Error deleting video:", error);
      }
    };

    // 预览相关方法
    const previewVideo = async (video) => {
      currentVideo.value = video;
      if (videoPlayer.value) {
        videoPlayer.value.pause();
        videoPlayer.value.currentTime = 0;
      }

      previewUrl.value = `${video.videoUrl}?t=${new Date().getTime()}`;
      previewDialogVisible.value = true;

      await nextTick();
      if (videoPlayer.value) {
        try {
          await videoPlayer.value.play();
        } catch (error) {
          console.error('Error playing video:', error);
        }
      }
    };

    const stopVideo = () => {
      if (videoPlayer.value) {
        videoPlayer.value.pause();
        videoPlayer.value.currentTime = 0;
      }
      previewUrl.value = "";
      currentVideo.value = null;
      previewDialogVisible.value = false;
    };

    // 搜索过滤
    const filteredVideos = computed(() => {
      if (!searchQuery.value) return videoList.value;
      const query = searchQuery.value.toLowerCase();
      return videoList.value.filter(video =>
          video.name.toLowerCase().includes(query)
      );
    });

    // 生命周期钩子
    onMounted(() => {
      fetchVideoList();
    });

    onBeforeUnmount(() => {
      stopVideo();
    });

    return {
      // 状态
      loading,
      videoList,
      searchQuery,
      previewDialogVisible,
      previewUrl,
      defaultThumbnail,
      videoPlayer,
      uploading,
      uploadProgress,
      uploadUrl,
      uploadHeaders,
      uploadParams,
      currentVideo,

      // 计算属性
      filteredVideos,

      // 方法
      fetchVideoList,
      handleUploadSuccess,
      handleUploadError,
      handleUploadProgress,
      beforeUpload,
      progressFormat,
      confirmDelete,
      previewVideo,
      stopVideo,
      handleSearch: () => {
      },
    };
  },
};
</script>

<style scoped>
.video-management {
  padding: 20px;
}

.header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
}

.upload-section {
  padding: 20px 0;
  border-bottom: 1px solid #ebeef5;
}

.upload-progress {
  margin-top: 10px;
  width: 300px;
}

.upload-button {
  display: inline-block;
}

.el-upload__tip {
  color: #909399;
  font-size: 12px;
  margin-top: 8px;
}

.thumbnail-image {
  width: 80px;
  height: 80px;
  border-radius: 4px;
  transition: transform 0.3s ease;
}

.thumbnail-image:hover {
  transform: scale(1.05);
}

.image-placeholder,
.image-error {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 80px;
  height: 80px;
  background: #f5f7fa;
  color: #909399;
}

.action-buttons {
  display: flex;
  gap: 10px;
  justify-content: center;
}

.action-button {
  padding: 8px 16px;
}

.video-container {
  position: relative;
  width: 100%;
  padding-top: 56.25%;
  background: #000;
}

.preview-video {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.dialog-header {
  display: flex;
  align-items: center;
  gap: 8px;
}

.video-name {
  color: #909399;
  font-size: 14px;
}

.el-table {
  --el-scrollbar-width: 6px;
  --el-scrollbar-height: 6px;
}

.el-table ::-webkit-scrollbar {
  width: var(--el-scrollbar-width);
  height: var(--el-scrollbar-height);
}

.el-table ::-webkit-scrollbar-thumb {
  background-color: var(--el-border-color-lighter);
  border-radius: 3px;
}

.el-table ::-webkit-scrollbar-track {
  background-color: var(--el-fill-color-lighter);
}
</style>