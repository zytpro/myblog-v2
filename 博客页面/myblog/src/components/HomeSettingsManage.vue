<template>
  <div class="home-settings-manage">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>首页设置</span>
        </div>
      </template>
      
      <el-form :model="homeSettings" label-width="120px">
        <el-form-item label="网站标题">
          <el-input v-model="homeSettings.webTitle" placeholder="请输入网站标题" />
        </el-form-item>
        
        <el-form-item label="背景图片">
          <div class="image-uploader">
            <el-upload
              class="avatar-uploader"
              :http-request="handleCustomUpload"
              :show-file-list="false"
              :before-upload="beforeUpload"
            >
              <img v-if="homeSettings.backgroundImage" :src="homeSettings.backgroundImage" class="avatar" />
              <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
            </el-upload>
            <el-input v-model="homeSettings.backgroundImage" placeholder="图片路径" class="mt-2" />
          </div>
        </el-form-item>
        
        <el-form-item label="打字机文本">
          <el-input
            v-model="typewriterTextsStr"
            type="textarea"
            rows="5"
            placeholder="请输入打字机文本，每行一个"
          />
          <el-button type="primary" size="small" @click="addText" class="mt-2">添加文本</el-button>
          <el-tag
            v-for="(text, index) in typewriterTextsArray"
            :key="index"
            closable
            @close="removeText(index)"
            class="mt-2 mr-2"
          >
            {{ text }}
          </el-tag>
        </el-form-item>
        
        <el-form-item label="底部引用">
          <el-input v-model="homeSettings.footerQuote" placeholder="请输入底部引用文本" />
        </el-form-item>
        
        <el-form-item label="ICP备案号">
          <el-input v-model="homeSettings.icpLicense" placeholder="请输入ICP备案号" />
        </el-form-item>
        
        <el-form-item label="联系邮箱">
          <el-input v-model="homeSettings.contactEmail" placeholder="请输入联系邮箱" />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="saveSettings">保存设置</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import request from '../utills/request.js'
import { Plus } from '@element-plus/icons-vue'

const homeSettings = ref({
  id: null,
  webTitle: '我的博客',
  backgroundImage: '/img/back1.png',
  typewriterTexts: '',
  footerQuote: '云想衣裳花想容,春风拂槛露华浓。',
  icpLicense: '滇ICP备2025053841号-1',
  contactEmail: '1363269065@qq.com'
})

const typewriterTextsStr = ref('')
const typewriterTextsArray = ref([])

const fetchHomeSettings = async () => {
  try {
    const response = await request.get('/home-settings/get')
    if (response.code === 0 && response.data) {
      homeSettings.value = response.data
      typewriterTextsArray.value = response.data.typewriterTexts.split(',')
    }
  } catch (error) {
    console.error('获取首页设置失败:', error)
  }
}

const handleCustomUpload = async (options) => {
  const { file, onSuccess, onError } = options
  try {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('type', 'images') // 添加文件类型参数
    formData.append('preserveName', 'false') // 是否保留原始文件名
    
    const response = await request({
      url: '/upload',
      method: 'post',
      data: formData
    })
    
    if (response && response.url) {
      homeSettings.value.backgroundImage = response.url
      console.log('上传成功，原始文件名:', response.originalFilename)
      console.log('文件大小:', response.fileSize)
      console.log('文件类型:', response.fileType)
      onSuccess(response)
    } else {
      onError(new Error('上传失败'))
    }
  } catch (error) {
    console.error('上传失败:', error)
    onError(error)
  }
}

const handleImageSuccess = (response, file) => {
  // 这个方法现在不再使用，因为我们使用了自定义上传
}

const beforeUpload = (file) => {
  const isJPG = file.type === 'image/jpeg' || file.type === 'image/png' || file.type === 'image/webp'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isJPG) {
    ElMessage.error('只能上传 JPG/PNG/WebP 格式的图片!')
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
  }
  return isJPG && isLt2M
}

const addText = () => {
  if (typewriterTextsStr.value.trim()) {
    typewriterTextsArray.value.push(typewriterTextsStr.value.trim())
    typewriterTextsStr.value = ''
  }
}

const removeText = (index) => {
  typewriterTextsArray.value.splice(index, 1)
}

const saveSettings = async () => {
  try {
    homeSettings.value.typewriterTexts = typewriterTextsArray.value.join(',')
    const response = await request.post('/home-settings/update', homeSettings.value)
    if (response.code === 0) {
      ElMessage.success('保存成功')
    } else {
      ElMessage.error('保存失败: ' + response.message)
    }
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败，请稍后重试')
  }
}

const resetForm = () => {
  fetchHomeSettings()
  typewriterTextsStr.value = ''
}

onMounted(fetchHomeSettings)
</script>

<style scoped>
.home-settings-manage {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.image-uploader {
  width: 100%;
}

.avatar-uploader {
  width: 200px;
  height: 100px;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-uploader:hover {
  border-color: #409eff;
}

.avatar {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 200px;
  height: 100px;
  text-align: center;
  line-height: 100px;
}

.mt-2 {
  margin-top: 10px;
}

.mr-2 {
  margin-right: 10px;
}
</style>
