<template>
  <div class="music-manager">
    <el-card>
      <div class="header">
        <el-input
            v-model="searchQuery"
            placeholder="搜索音乐"
            clearable
            @input="handleSearch"
            style="width: 300px"
        />
        <el-button type="primary" @click="fetchMusicList">刷新</el-button>
      </div>

      <!-- 修改上传音乐部分 -->
      <div class="upload-section">
        <el-upload
            ref="musicUploadRef"
            :action="uploadUrl"
            :headers="uploadHeaders"
            :data="uploadParams"
            :on-success="handleUploadSuccess"
            :on-error="handleUploadError"
            :before-upload="beforeUpload"
            :auto-upload="false"
            accept=".mp3, .wav, .flac"
            :file-list="musicFileList"
            :on-change="(file, fileList) => handleFileChange(file, fileList, 'music')"
            class="upload-item"
        >
          <el-button type="primary">选择音乐文件</el-button>
        </el-upload>

        <el-upload
            ref="lrcUploadRef"
            :action="uploadUrl"
            :headers="uploadHeaders"
            :auto-upload="false"
            accept=".lrc"
            :file-list="lrcFileList"
            :on-change="(file, fileList) => handleFileChange(file, fileList, 'lrc')"
            class="upload-item"
        >
          <el-button>选择歌词文件</el-button>
        </el-upload>

        <el-button 
          type="success" 
          @click="handleUploadFiles"
          :disabled="!canUpload"
        >
          开始上传
        </el-button>
      </div>

      <el-table
          :data="filteredMusic"
          border
          style="width: 100%; margin-top: 20px"
          v-loading="loading"
      >
        <!-- 第一列：封面图 -->
        <el-table-column label="封面" width="120" align="center">
          <template #default="scope">
            <img
                :src="scope.row.coverUrl || defaultCover"
                alt="封面"
                class="cover-image"
            />
          </template>
        </el-table-column>

        <!-- 第二列：音乐名称 -->
        <el-table-column prop="name" label="文件名称" />

        <!-- 第三列：操作按钮 -->
        <el-table-column label="操作" align="center" width="200px">
          <template #default="scope">
            <div class="action-buttons">
              <el-button
                  size="small"
                  type="success"
                  @click="openMusicPlayer(scope.row)"
                  class="action-button"
              >
                播放
              </el-button>
              <el-button
                  size="small"
                  type="danger"
                  @click="deleteMusic(scope.row)"
                  class="action-button"
              >
                删除
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script>
import { ref, computed, onMounted } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import request from "../utills/request.js";

export default {
  name: "MusicManager",
  setup() {
    const loading = ref(false);
    const musicList = ref([]);
    const searchQuery = ref("");
    const musicFileList = ref([]);
    const lrcFileList = ref([]);
    const defaultCover = "/assets/default-cover.png";
    // 上传相关配置
    const uploadUrl = request.defaults.baseURL + "/upload"; // 后端接口路径
    const uploadHeaders = { Authorization: "Bearer your-token" }; 
    const uploadParams = { type: "music" };

    const musicUploadRef = ref(null);
    const lrcUploadRef = ref(null);

    // 判断是否可以上传
    const canUpload = computed(() => {
      return musicFileList.value.length > 0;
    });

    // 获取音乐列表
    const fetchMusicList = async () => {
      loading.value = true;
      try {
        const response = await request.get("/file/list", {
          params: { type: "music" },
        });

        // 处理空列表情况
        if (response.code === 1 && response.message === "未找到文件") {
          musicList.value = []; 
          return; 
        }

        if (response.data && Array.isArray(response.data)) {
          musicList.value = response.data.map((file) => ({
            name: file.fileName,
            storedFileName: file.storedFileName,
            coverUrl: `${request.defaults.baseURL}/file/cover/music/${file.storedFileName}`,
            musicUrl: `${request.defaults.baseURL}/file/preview/music/${file.storedFileName}`,
            artist: "",
          }));
        } else {
          musicList.value = []; 
        }
      } catch (error) {
        console.error("Error fetching music list:", error);
        ElMessage.error("获取音乐列表失败");
      } finally {
        loading.value = false;
      }
    };

    // 删除音乐
    const deleteMusic = async (music) => {
      try {
        await ElMessageBox.confirm(`确认删除音乐 "${music.name}" 吗？`, "提示", {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning",
        });

        // 执行删除操作
        await request.delete(`/file/delete/music/${music.storedFileName}`);
        ElMessage.success("音乐删除成功！");
        fetchMusicList(); 
      } catch (error) {
        if (error === 'cancel') return;
        ElMessage.error("音乐删除失败！");
      }
    };

    // 打开音乐播放器
    const openMusicPlayer = (music) => {
      const playerUrl = `/music-player?coverUrl=${encodeURIComponent(music.coverUrl)}&musicUrl=${encodeURIComponent(music.musicUrl)}&name=${encodeURIComponent(music.name)}&artist=${encodeURIComponent(music.artist)}`;
      window.open(playerUrl, "_blank");
    };

    // 处理搜索
    const handleSearch = () => {};

    // 过滤音乐
    const filteredMusic = computed(() => {
      return musicList.value.filter((file) => {
        return file.name.toLowerCase().includes(searchQuery.value.toLowerCase());
      });
    });

    // 处理文件上传
    const handleUploadFiles = async () => {
      if (!musicFileList.value.length) {
        ElMessage.warning('请选择音乐文件');
        return;
      }

      const formData = new FormData();

      // 处理音乐文件名，移除空格
      const musicFile = musicFileList.value[0].raw;
      const cleanMusicFileName = musicFile.name.replace(/\s+/g, '_');
      const musicBlob = new Blob([musicFile], { type: musicFile.type });
      const newMusicFile = new File([musicBlob], cleanMusicFileName, { type: musicFile.type });
      formData.append('file', newMusicFile);
      formData.append('type', 'music');

      // 如果有歌词文件，同样处理文件名
      if (lrcFileList.value.length > 0) {
        const lrcFile = lrcFileList.value[0].raw;
        const cleanLrcFileName = lrcFile.name.replace(/\s+/g, '_');
        const lrcBlob = new Blob([lrcFile], { type: lrcFile.type });
        const newLrcFile = new File([lrcBlob], cleanLrcFileName, { type: lrcFile.type });
        formData.append('lyricsFile', newLrcFile);
      }

      try {
        // 添加preserveName参数以保留原始文件名
        formData.append('preserveName', 'true');
        const response = await request.post('/upload', formData);

        if (response && response.url) { 
          ElMessage.success('上传成功！');
          // 清空文件列表
          musicFileList.value = [];
          lrcFileList.value = [];
          // 刷新音乐列表
          fetchMusicList();
        } else {

          throw new Error(response.message || '上传失败');
        }
      } catch (error) {
        console.error('Upload error:', error);
        ElMessage.error('上传失败：' + (error.response?.data || error.message || '未知错误'));
      }
    };

    // 修改文件选择处理，添加文件名验证
    const handleFileChange = (file, fileList, type) => {
      // 检查文件名是否包含特殊字符
      const hasSpecialChars = /[^a-zA-Z0-9._-]/.test(file.name);
      if (hasSpecialChars) {
        ElMessage.warning('文件名请不要包含空格和特殊字符');
      }

      if (type === 'music') {
        musicFileList.value = fileList.slice(-1);
      } else {
        lrcFileList.value = fileList.slice(-1);
      }
    };

    // 上传前的文件类型校验
    const beforeUpload = (file) => {
      const isMusic = file.type.startsWith("audio/");
      if (!isMusic) {
        ElMessage.error("只能上传音频文件！");
        return false;
      }
      return true;
    };

    // 上传成功回调
    const handleUploadSuccess = (response) => {
      if (response && response.url) {
        ElMessage.success("音乐上传成功！");
        fetchMusicList();
        // 清空文件列表
        musicFileList.value = [];
        lrcFileList.value = [];
      } else {
        ElMessage.error("上传失败：未获取到文件URL");
      }
    };

    // 上传失败的回调
    const handleUploadError = (response) => {
      const errrMessage = response?.message || "音乐上传失败";
      ElMessage.error(errrMessage);
    };

    onMounted(() => {
      fetchMusicList();
    });

    return {
      loading,
      musicList,
      searchQuery,
      defaultCover,
      fetchMusicList,
      deleteMusic,
      openMusicPlayer,
      filteredMusic,
      uploadHeaders,
      uploadParams,
      handleUploadSuccess,
      handleUploadError,
      beforeUpload,
      uploadUrl,
      handleSearch,
      musicFileList,
      lrcFileList,
      canUpload,
      handleUploadFiles,
      handleFileChange,
      musicUploadRef,
      lrcUploadRef,
    };
  },
};
</script>

<style scoped>
.music-manager {
  padding: 20px;
}
.header {
  display: flex;
  align-items: center;
  gap: 10px;
}
.cover-image {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 4px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.action-buttons {
  display: flex;
  gap: 10px;
  justify-content: center;
}

.action-button {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 80px;
  height: 32px;
  padding: 0;
  line-height: 1px;
}

.el-table {
  max-height: 500px;
  overflow-y: auto;
}

.el-table::-webkit-scrollbar {
  display: none;
}

.upload-info {
  margin-top: 10px;
  font-size: 12px;
  color: #999;
}

.upload-section {
  padding: 20px 0;
  display: flex;
  gap: 16px;
  align-items: center;
}

.upload-item {
  flex: 1;
}

.upload-item :deep(.el-upload) {
  width: 100%;
}

.upload-item :deep(.el-upload-list) {
  margin-top: 8px;
}

.upload-item :deep(.el-button) {
  width: 100%;
}
</style>
