<template>
  <div class="carousel-manage">
    <!-- 顶部操作区 -->
    <div class="header-actions">
      <el-button type="primary" @click="openDialog('add')">新增轮播图</el-button>
    </div>

    <!-- 轮播图列表 -->
    <el-table :data="carouselList" style="width: 100%; margin-top: 20px">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="title" label="标题" width="200" />
      <el-table-column label="图片" width="200">
        <template #default="scope">
          <el-image 
            :src="scope.row.imageURL" 
            :preview-src-list="[scope.row.imageURL]"
            fit="cover"
            style="width: 100px; height: 60px"
          />
        </template>
      </el-table-column>
      <el-table-column prop="articleId" label="关联文章ID" width="120" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
            {{ scope.row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="scope">
          <el-button 
            type="primary" 
            size="small" 
            @click="openDialog('edit', scope.row)"
          >
            编辑
          </el-button>
          <el-button 
            type="danger" 
            size="small" 
            @click="handleDelete(scope.row.id)"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新增/编辑对话框 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="dialogType === 'add' ? '新增轮播图' : '编辑轮播图'"
      width="50%"
    >
      <el-form 
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入标题" />
        </el-form-item>
        <el-form-item label="图片" prop="imageFile">
          <el-upload
            class="carousel-uploader"
            :action="uploadAction"
            :show-file-list="false"
            :before-upload="beforeUpload"
            :on-success="handleUploadSuccess"
            :on-error="handleUploadError"
            :http-request="customUpload"
          >
            <img v-if="form.imageURL" :src="form.imageURL" class="uploaded-image" />
            <el-icon v-else class="carousel-uploader-icon"><Plus /></el-icon>
          </el-upload>
          <div class="el-upload__tip">
            只能上传 jpg/png/gif 文件，且不超过 5MB
          </div>
        </el-form-item>
        <el-form-item label="关联文章" prop="articleId">
          <el-input v-model="form.articleId" type="number" placeholder="请输入关联文章ID（选填）" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch
            v-model="form.status"
            :active-value="1"
            :inactive-value="0"
            active-text="启用"
            inactive-text="禁用"
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
import { Plus } from '@element-plus/icons-vue'
import request from "../utills/request"

// 数据定义
const carouselList = ref([])
const dialogVisible = ref(false)
const dialogType = ref('add')
const formRef = ref(null)
const uploadAction = `${import.meta.env.VITE_BASE_URL}/carousel/upload`

const form = ref({
  id: null,
  title: '',
  imageURL: '',
  articleId: '',
  status: 1
})

// 表单验证规则
const rules = {
  title: [
    { required: true, message: '请输入标题', trigger: 'blur' },
    { min: 1, max: 50, message: '长度在 1 到 50 个字符', trigger: 'blur' }
  ]
}

// 获取轮播图列表
const fetchCarousels = async () => {
  try {
    const res = await request.get('/carousel')
    carouselList.value = res
  } catch (error) {
    ElMessage.error('获取轮播图列表失败')
  }
}

// 打开对话框
const openDialog = (type, row = null) => {
  dialogType.value = type
  dialogVisible.value = true
  if (type === 'edit' && row) {
    form.value = { ...row }
  } else {
    form.value = {
      id: null,
      title: '',
      imageURL: '',
      articleId: '',
      status: 1
    }
  }
}

// 上传相关方法
const beforeUpload = (file) => {
  if (!form.value.title) {
    ElMessage.error('请先填写标题!')
    return false
  }

  const isImage = /^image\/(jpeg|png|gif)$/.test(file.type)
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB!')
    return false
  }
  return true
}

// 自定义上传方法
const customUpload = async (options) => {
  try {
    console.log('Starting upload with file:', options.file)
    const imageFormData = new FormData()
    imageFormData.append('file', options.file)
    
    const response = await request.post('/carousel/upload', imageFormData)

    console.log('Upload response:', response)

    // 检查响应格式
    if (typeof response === 'string') {
      // 如果响应直接是字符串（URL）
      form.value.imageURL = response
      options.onSuccess(response)
      ElMessage.success('图片上传成功')
    } else if (response && response.data) {
      // 如果响应是对象，尝试获取 data 属性
      form.value.imageURL = response.data
      options.onSuccess(response.data)
      ElMessage.success('图片上传成功')
    } else {
      throw new Error('未获取到有效的图片URL')
    }
  } catch (error) {
    console.error('Upload error details:', error)
    if (error.response) {
      console.error('Error response:', error.response)
    }
    options.onError(error)
    ElMessage.error('上传失败：' + (error.response?.data || error.message || '未知错误'))
  }
}

const handleUploadError = (error) => {
  console.error('Upload error in handler:', error)
  let errorMessage = '上传失败'
  if (error.response) {
    console.error('Error response in handler:', error.response)
    errorMessage += ': ' + (error.response.data || error.message)
  } else if (error.message) {
    errorMessage += ': ' + error.message
  }
  ElMessage.error(errorMessage)
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  if (!form.value.imageURL) {
    ElMessage.error('请上传图片')
    return
  }
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const isEdit = dialogType.value === 'edit'
        const method = isEdit ? 'put' : 'post'
        const url = isEdit ? `/carousel/${form.value.id}` : '/carousel'
        
        if (isEdit) {
          // 编辑模式使用 JSON
          const data = {
            id: form.value.id,
            title: form.value.title,
            status: form.value.status,
            imageURL: form.value.imageURL,
            articleId: form.value.articleId || null
          }
          const res = await request[method](url, data)
          console.log('Submit response:', res)
        } else {
          // 新增模式使用 FormData
          const formData = new FormData()
          formData.append('title', form.value.title)
          formData.append('status', form.value.status)
          formData.append('imageURL', form.value.imageURL)
          if (form.value.articleId) {
            formData.append('articleId', form.value.articleId)
          }
          
          const res = await request.post(url, formData)
          console.log('Submit response:', res)
        }
        
        ElMessage.success(isEdit ? '更新成功' : '添加成功')
        dialogVisible.value = false
        fetchCarousels()
      } catch (error) {
        console.error('Submit error:', error)
        ElMessage.error('操作失败: ' + (error.response?.data || error.message || '未知错误'))
      }
    }
  })
}

// 删除轮播图
const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除该轮播图吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await request.delete(`/carousel/${id}`)
    ElMessage.success('删除成功')
    fetchCarousels()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 页面加载时获取数据
onMounted(() => {
  fetchCarousels()
})
</script>

<style scoped>
.carousel-manage {
  padding: 20px;
}

.header-actions {
  margin-bottom: 20px;
}

.carousel-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 178px;
  height: 178px;
}

.carousel-uploader:hover {
  border-color: #409EFF;
}

.carousel-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 178px;
  height: 178px;
  text-align: center;
  line-height: 178px;
}

.uploaded-image {
  width: 178px;
  height: 178px;
  display: block;
  object-fit: cover;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

:deep(.el-upload) {
  width: 178px;
  height: 178px;
}
</style>