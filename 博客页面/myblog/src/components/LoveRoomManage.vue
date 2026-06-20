<template>
  <div class="love-room-manage">
    <el-card class="love-room-card">
      <template #header>
        <div class="card-header">
          <h2>恋爱小屋管理</h2>
          <el-button type="primary" @click="handleSave">保存修改</el-button>
        </div>
      </template>

      <el-form :model="loveRoomInfo" label-width="120px">
        <!-- 头像上传区域 -->
        <div class="avatar-section">
          <div class="avatar-upload">
            <h3>男生头像</h3>
            <el-upload
              class="avatar-uploader"
              :action="`${baseURL}/love-room/upload/avatar/boy`"
              :headers="headers"
              :show-file-list="false"
              :on-success="(res) => handleAvatarSuccess(res, 'boy')"
              :before-upload="beforeAvatarUpload"
            >
              <img v-if="loveRoomInfo.boyAvatar" :src="loveRoomInfo.boyAvatar" class="avatar" />
              <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
            </el-upload>
          </div>

          <div class="avatar-upload">
            <h3>女生头像</h3>
            <el-upload
              class="avatar-uploader"
              :action="`${baseURL}/love-room/upload/avatar/girl`"
              :headers="headers"
              :show-file-list="false"
              :on-success="(res) => handleAvatarSuccess(res, 'girl')"
              :before-upload="beforeAvatarUpload"
            >
              <img v-if="loveRoomInfo.girlAvatar" :src="loveRoomInfo.girlAvatar" class="avatar" />
              <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
            </el-upload>
          </div>
        </div>

        <!-- 基本信息表单 -->
        <el-form-item label="在一起的日子">
          <el-date-picker
            v-model="loveRoomInfo.time"
            type="date"
            placeholder="选择日期"
            format="YYYY-MM-DD"
          />
        </el-form-item>

        <el-form-item label="男生名字">
          <el-input v-model="loveRoomInfo.boyName" />
        </el-form-item>

        <el-form-item label="女生名字">
          <el-input v-model="loveRoomInfo.girlName" />
        </el-form-item>

        <el-form-item label="恋爱宣言">
          <el-input
            v-model="loveRoomInfo.declare"
            type="textarea"
            :rows="4"
            placeholder="写下你们的恋爱宣言..."
          />
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import { Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import request from '../utills/request'
import { API_BASE_URL } from '../config/env.js'

export default {
  name: 'LoveRoomManage',
  components: {
    Plus
  },
  data() {
    return {
      loveRoomInfo: {
        boyAvatar: '',
        girlAvatar: '',
        boyName: '',
        girlName: '',
        time: '',
        declare: ''
      },
      uploadUrl: API_BASE_URL + '/love-room/upload/avatar',
      headers: {
        Authorization: localStorage.getItem('token')
      },
      baseURL: API_BASE_URL
    }
  },
  created() {
    this.fetchLoveRoomInfo()
  },
  methods: {
    // 获取恋爱小屋信息
    async fetchLoveRoomInfo() {
      try {
        const response = await request.get('/love-room')
        if (response.code === 0) {
          const data = response.data || {}
          this.loveRoomInfo = {
            boyAvatar: '',
            girlAvatar: '',
            boyName: '',
            girlName: '',
            time: '',
            declare: '',
            ...data
          }
        } else {
          ElMessage.error('获取恋爱小屋信息失败')
        }
      } catch (error) {
        console.error('获取恋爱小屋信息失败:', error)
        ElMessage.error('获取恋爱小屋信息失败')
      }
    },

    // 保存修改
    async handleSave() {
      try {
        const response = await request.post('/love-room', this.loveRoomInfo)
        if (response.code === 0) {
          ElMessage.success('保存成功')
        } else {
          ElMessage.error('保存失败')
        }
      } catch (error) {
        console.error('保存失败:', error)
        ElMessage.error('保存失败')
      }
    },

    // 头像上传成功
    handleAvatarSuccess(response, type) {
      if (response.code === 0) {
        if (!this.loveRoomInfo) {
          this.loveRoomInfo = { boyAvatar: '', girlAvatar: '', boyName: '', girlName: '', time: '', declare: '' }
        }
        if (type === 'boy') {
          this.loveRoomInfo.boyAvatar = response.data
        } else {
          this.loveRoomInfo.girlAvatar = response.data
        }
        ElMessage.success('上传成功')
      } else {
        ElMessage.error('上传失败')
      }
    },

    // 头像上传前的验证
    beforeAvatarUpload(file) {
      const isJPG = file.type === 'image/jpeg' || file.type === 'image/png'
      const isLt2M = file.size / 1024 / 1024 < 2

      if (!isJPG) {
        ElMessage.error('上传头像图片只能是 JPG/PNG 格式!')
      }
      if (!isLt2M) {
        ElMessage.error('上传头像图片大小不能超过 2MB!')
      }
      return isJPG && isLt2M
    }
  }
}
</script>

<style scoped>
.love-room-manage {
  padding: 20px;
}

.love-room-card {
  max-width: 800px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.avatar-section {
  display: flex;
  justify-content: space-around;
  margin-bottom: 30px;
}

.avatar-upload {
  text-align: center;
}

.avatar-upload h3 {
  margin-bottom: 15px;
}

.avatar-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 178px;
  height: 178px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.avatar-uploader:hover {
  border-color: #409EFF;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 178px;
  height: 178px;
  text-align: center;
  display: flex;
  justify-content: center;
  align-items: center;
}

.avatar {
  width: 178px;
  height: 178px;
  display: block;
  object-fit: cover;
}
</style> 