<template>
  <div class="life-time-manage">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>生活时光管理</span>
          <el-button type="primary" @click="handleAdd">添加记录</el-button>
        </div>
      </template>

      <!-- 生活时光列表 -->
      <el-table :data="lifeTimeList" style="width: 100%">
        <el-table-column label="照片" width="180">
          <template #default="scope">
            <el-image 
              :src="scope.row.url.startsWith('http') ? scope.row.url : `${baseURL}${scope.row.url}`" 
              :preview-src-list="getPreviewSrcList(scope.row.url)"
              :initial-index="0"
              fit="cover"
              style="width: 100px; height: 100px"
              preview-teleported
            />
          </template>
        </el-table-column>
        <el-table-column prop="content" label="描述" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="100">
          <template #default="scope">
            <el-button 
              type="danger" 
              size="small" 
              @click="handleDelete(scope.row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加对话框 -->
    <el-dialog 
      v-model="dialogVisible" 
      title="添加记录"
      width="600px"
      destroy-on-close
      @close="handleDialogClose"
      class="life-time-dialog"
    >
      <el-form :model="form" label-width="80px">
        <el-form-item label="照片" class="upload-item">
          <el-upload
            class="upload-demo"
            :action="`${baseURL}/life-time`"
            :on-success="handleUploadSuccess"
            :before-upload="beforeUpload"
            :data="{ content: form.content }"
            :auto-upload="false"
            ref="uploadRef"
            drag
            :show-file-list="false"
            :on-change="handleFileChange"
          >
            <div v-if="!previewUrl" class="upload-area">
              <el-icon class="el-icon--upload"><upload-filled /></el-icon>
              <div class="el-upload__text">
                将文件拖到此处，或 <em>点击上传</em>
              </div>
            </div>
            <div v-else class="preview-container">
              <el-image 
                :src="previewUrl" 
                fit="cover"
                class="preview-image"
              />
              <div class="preview-mask">
                <el-icon class="preview-icon"><upload-filled /></el-icon>
                <span>点击更换图片</span>
              </div>
            </div>
            <template #tip>
              <div class="el-upload__tip">
                只能上传jpg/png/gif文件
              </div>
            </template>
          </el-upload>
        </el-form-item>
        <el-form-item label="描述" class="content-item">
          <el-input 
            v-model="form.content" 
            type="textarea" 
            :rows="4"
            placeholder="记录下这个美好的时刻..."
            resize="none"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import request from '../utills/request'

const lifeTimeList = ref([])
const dialogVisible = ref(false)
const form = ref({
  content: '',
  url: ''
})
const uploadRef = ref()
const baseURL = import.meta.env.VITE_BASE_URL
const previewUrl = ref('')

// 计算所有图片的完整URL列表
const allImageUrls = computed(() => {
  return lifeTimeList.value.map(item => 
    item.url.startsWith('http') ? item.url : `${baseURL}${item.url}`
  )
})

// 获取指定图片的预览列表（包含所有图片）
const getPreviewSrcList = (currentUrl) => {
  const fullCurrentUrl = currentUrl.startsWith('http') ? currentUrl : `${baseURL}${currentUrl}`
  // 将当前图片移到列表第一位
  const currentIndex = allImageUrls.value.findIndex(url => url === fullCurrentUrl)
  if (currentIndex === -1) return allImageUrls.value
  
  const reorderedList = [...allImageUrls.value]
  reorderedList.splice(currentIndex, 1)
  reorderedList.unshift(fullCurrentUrl)
  return reorderedList
}

// 获取所有记录
const fetchLifeTimeList = async () => {
  try {
    const response = await request.get('/life-time')
    if (response.code === 0) {
      lifeTimeList.value = response.data
    }
  } catch (error) {
    ElMessage.error('获取记录失败')
  }
}

// 上传前验证
const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    ElMessage.error('只能上传图片文件！')
    return false
  }
  return true
}

// 文件改变时的处理函数
const handleFileChange = (file) => {
  if (file && file.raw) {
    previewUrl.value = URL.createObjectURL(file.raw)
  }
}

// 处理添加按钮点击
const handleAdd = () => {
  form.value = {
    content: '',
    url: ''
  }
  previewUrl.value = ''  // 清空预览图片
  if (uploadRef.value) {
    uploadRef.value.clearFiles()  // 清空上传组件的文件列表
  }
  dialogVisible.value = true  // 最后设置对话框可见
}

// 对话框关闭时的处理
const handleDialogClose = () => {
  dialogVisible.value = false  // 确保对话框关闭
  form.value = {
    content: '',
    url: ''
  }
  previewUrl.value = ''  // 清空预览图片
  if (uploadRef.value) {
    uploadRef.value.clearFiles()  // 清空上传组件的文件列表
  }
}

// 上传成功回调
const handleUploadSuccess = async (response) => {
  if (response.code === 0) {
    ElMessage.success('添加成功')
    dialogVisible.value = false  // 关闭对话框
    form.value = { content: '', url: '' }  // 重置表单
    previewUrl.value = ''  // 清空预览
    if (uploadRef.value) {
      uploadRef.value.clearFiles()  // 清空上传组件的文件列表
    }
    fetchLifeTimeList()  // 刷新列表
  } else {
    ElMessage.error('上传失败')
  }
}

// 提交表单
const handleSubmit = async () => {
  try {
    if (!uploadRef.value) return
    uploadRef.value.submit()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

// 删除记录
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除这条记录吗？', '提示', {
      type: 'warning'
    })
    const response = await request.delete('/life-time', {
      params: { url: row.url }
    })
    if (response.code === 0) {
      ElMessage.success('删除成功')
      fetchLifeTimeList()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  fetchLifeTimeList()
})
</script>

<style scoped>
.life-time-manage {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.life-time-dialog :deep(.el-dialog__body) {
  padding: 30px;
}

.upload-item :deep(.el-upload) {
  width: 100%;
}

.upload-item :deep(.el-upload-dragger) {
  width: 100%;
  height: 200px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background-color: #f8f9fa;
  border: 2px dashed #e4e7ed;
  border-radius: 8px;
  transition: all 0.3s;
}

.upload-item :deep(.el-upload-dragger:hover) {
  border-color: #409eff;
  background-color: #f5f7fa;
}

.upload-area {
  text-align: center;
}

.el-icon--upload {
  font-size: 48px;
  color: #909399;
  margin-bottom: 16px;
}

.el-upload__text {
  color: #606266;
  font-size: 14px;
  margin-bottom: 8px;
}

.el-upload__text em {
  color: #409eff;
  font-style: normal;
  cursor: pointer;
}

.el-upload__tip {
  color: #909399;
  font-size: 12px;
}

.content-item {
  margin-top: 20px;
}

.content-item :deep(.el-textarea__inner) {
  min-height: 120px;
  font-size: 14px;
  line-height: 1.6;
  padding: 12px;
  background-color: #f8f9fa;
  border-radius: 8px;
  resize: none;
  transition: all 0.3s;
}

.content-item :deep(.el-textarea__inner:focus) {
  background-color: #fff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.1);
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding-top: 20px;
}

.preview-container {
  position: relative;
  width: 100%;
  height: 200px;
  border-radius: 8px;
  overflow: hidden;
}

.preview-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.preview-mask {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #fff;
  opacity: 0;
  transition: opacity 0.3s;
}

.preview-container:hover .preview-mask {
  opacity: 1;
}

.preview-icon {
  font-size: 24px;
  margin-bottom: 8px;
}

/* 图片预览样式 */
:deep(.el-image-viewer__wrapper) {
  position: fixed;
  top: 0;
  right: 0;
  bottom: 0;
  left: 0;
  z-index: 2000;
}

:deep(.el-image-viewer__mask) {
  position: fixed;
  top: 0;
  right: 0;
  bottom: 0;
  left: 0;
  z-index: 1999;
  background-color: rgba(0, 0, 0, 0.9);
}

:deep(.el-image-viewer__btn) {
  z-index: 2001;
}

:deep(.el-image-viewer__close) {
  top: 40px;
  right: 40px;
  width: 40px;
  height: 40px;
  font-size: 24px;
}

:deep(.el-image-viewer__canvas) {
  z-index: 2000;
}

:deep(.el-image-viewer__prev), 
:deep(.el-image-viewer__next) {
  z-index: 2001;
}

:deep(.el-image-viewer__actions) {
  z-index: 2001;
}

/* 禁用预览时的页面滚动 */
:deep(body.el-popup-parent--hidden) {
  overflow: hidden !important;
  padding-right: 0 !important;
}
</style>
