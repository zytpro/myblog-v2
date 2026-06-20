<template>
  <div class="file-manager">
    <el-card>
      <div class="header">
        <el-input
            v-model="searchQuery"
            placeholder="搜索图片"
            clearable
            @input="handleSearch"
            class="search-bar"
        />
        <el-button
            type="primary"
            @click="fetchFileList(true)"
            class="refresh-btn"
        >
          刷新
        </el-button>
      </div>

      <!-- 上传图片按钮 -->
      <el-upload
          :action="uploadUrl"
          :headers="uploadHeaders"
          :data="uploadParams"
          :on-success="handleUploadSuccess"
          :on-error="handleUploadError"
          :before-upload="beforeUpload"
          accept="image/*"
          class="upload-button"
      >
        <el-button type="primary">上传图片</el-button>
      </el-upload>

      <el-table
          :data="filteredFiles"
          border
          style="width: 100%; margin-top: 20px"
          v-loading="loading"
      >
        <!-- 图片列 -->
        <el-table-column label="图片" width="180">
          <template #default="scope">
            <el-image
                :src="getImageUrl(scope.row)"
                fit="cover"
                class="image-cell enhanced"
                lazy
                @click="openViewer(scope.$index)"
            >
              <template #placeholder>
                <div class="image-placeholder">加载中...</div>
              </template>
              <template #error>
                <div class="image-error">加载失败</div>
              </template>
            </el-image>
          </template>
        </el-table-column>

        <!-- 文件名称列 -->
        <el-table-column prop="name" label="文件名称">
          <template #default="scope">
            <span class="file-name">{{ scope.row.name }}</span>
          </template>
        </el-table-column>

        <!-- 操作列 -->
        <el-table-column label="操作" width="180" align="center">
          <template #default="scope">
            <el-button
                size="small"
                type="danger"
                @click.stop="deleteFile(scope.row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 全屏图片预览（支持左右切换） -->
    <teleport to="body">
      <el-image-viewer
        v-if="viewerVisible"
        :url-list="previewList"
        :initial-index="currentIndex"
        @close="viewerVisible = false"
        hide-on-click-modal
      />
    </teleport>
  </div>
</template>

<script>
import { ref, onMounted, computed } from "vue";
import { ElMessage, ElMessageBox, ElImageViewer } from "element-plus";
import request from "../utills/request.js";

export default {
  name: "FileManager",
  components: { ElImageViewer },
  setup() {
    const loading = ref(false);
    const files = ref([]);
    const searchQuery = ref("");

    // 预览状态
    const viewerVisible = ref(false);
    const currentIndex = ref(0);

    const openViewer = (index) => {
      currentIndex.value = index;
      viewerVisible.value = true;
    };

    // 上传相关配置
    const uploadUrl = request.defaults.baseURL + "/upload"; // 后端接口路径
    const uploadHeaders = { Authorization: "Bearer your-token" }; // 根据需要设置
    const uploadParams = { type: "images" };

    const fetchFileList = async (clearCache = false) => {
      loading.value = true;
      try {
        if (clearCache) {
          localStorage.removeItem('imageFiles');
          localStorage.removeItem('cacheTime');
        }
        const cachedData = localStorage.getItem('imageFiles');
        const cacheTime = localStorage.getItem('cacheTime');
        const now = Date.now();

        if (!clearCache && cachedData && cacheTime && (now - cacheTime < 10 * 60 * 1000)) {
          files.value = JSON.parse(cachedData);
        } else {
          const response = await request.get("/file/list", { params: { type: "images" } });
          if (response.code === 1 && response.message === "未找到文件") {
            files.value = [];
          } else if (response.code === 0 && Array.isArray(response.data)) {
            files.value = response.data.map((file) => ({
              name: file.fileName,
              storedName: file.storedFileName
            }));
            localStorage.setItem('imageFiles', JSON.stringify(files.value));
            localStorage.setItem('cacheTime', now);
          } else {
            throw new Error(response.message || "获取文件列表失败");
          }
        }
      } catch (error) {
        console.error("Error fetching file list:", error);
        ElMessage.error(error.message || "获取文件列表失败！");
        files.value = [];
      } finally {
        loading.value = false;
      }
    };

    const deleteFile = async (file) => {
      try {
        await ElMessageBox.confirm(
            `确认删除文件 "${file.name}" 吗？`,
            "提示",
            { confirmButtonText: "确定", cancelButtonText: "取消", type: "warning" }
        );
        const response = await request.delete(`/file/delete/images/${file.storedName}`);
        if (response === "File deleted successfully.") {
          ElMessage.success("文件删除成功！");
          fetchFileList(true);
        } else {
          throw new Error(response || "删除失败");
        }
      } catch (error) {
        if (error === 'cancel') return;
        ElMessage.error(error.message || "文件删除失败！");
      }
    };

    const handleSearch = () => {};

    const filteredFiles = computed(() =>
        files.value.filter((file) =>
            file.name.toLowerCase().includes(searchQuery.value.toLowerCase())
        )
    );

    const getImageUrl = (file) => `${request.defaults.baseURL}/file/preview/image/${file.storedName}`;
    const previewList = computed(() => filteredFiles.value.map(f => getImageUrl(f)));

    const handleUploadSuccess = (response) => {
      if (response && response.url) {
        ElMessage.success("图片上传成功！");
        fetchFileList(true);
      } else {
        ElMessage.error("上传失败：未获取到文件URL");
      }
    };

    const handleUploadError = (response) => {
      const errrMessage = response?.message || "图片上传失败";
      ElMessage.error(errrMessage);
    };

    const beforeUpload = (file) => {
      const isImage = file.type.startsWith("image/");
      if (!isImage) {
        ElMessage.error("只能上传图片文件！");
      }
      return isImage;
    };

    onMounted(() => fetchFileList());

    return {
      loading,
      files,
      searchQuery,
      fetchFileList,
      deleteFile,
      handleSearch,
      filteredFiles,
      getImageUrl,
      uploadHeaders,
      uploadParams,
      handleUploadSuccess,
      handleUploadError,
      beforeUpload,
      uploadUrl,
      // 预览
      viewerVisible,
      currentIndex,
      openViewer,
      previewList,
    };
  },
};
</script>

<style scoped>
.file-manager { padding: 20px; }
.header { align-items: center; justify-content: space-between; margin-bottom: 20px; }

.image-cell { width: 120px; height: 120px; object-fit: cover; border-radius: 8px; }
.image-cell.enhanced { transition: transform .2s ease, box-shadow .2s ease; cursor: zoom-in; }
.image-cell.enhanced:hover { transform: scale(1.05); box-shadow: 0 6px 18px rgba(0,0,0,.15); }

.el-table { max-height: 500px; overflow-y: auto; }
.el-table::-webkit-scrollbar { display: none; }

.search-bar { margin-right: 10px; width: 300px; }
.refresh-btn { white-space: nowrap; }

.image-placeholder, .image-error { width: 120px; height: 120px; display:flex; align-items:center; justify-content:center; color:#909399; background:#f5f7fa; border-radius:8px; }

.file-name { font-size: 14px; color: #606266; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
</style>
