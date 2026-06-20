<template>
  <div class="announcement-manage">
    <!-- 顶部操作区 -->
    <div class="header-actions">
      <el-button type="primary" @click="openDialog('add')">新增公告</el-button>
    </div>

    <!-- 公告列表 -->
    <el-table :data="announcements" style="width: 100%; margin-top: 20px">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="content" label="公告内容" show-overflow-tooltip>
        <template #default="scope">
          <div style="white-space: pre-wrap;">{{ scope.row.content }}</div>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180">
        <template #default="scope">
          {{ formatDate(scope.row.createTime) }}
        </template>
      </el-table-column>
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
      :title="dialogType === 'add' ? '新增公告' : '编辑公告'"
      width="50%"
    >
      <el-form 
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="公告内容" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="4"
            placeholder="请输入公告内容"
          />
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
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from "../utills/request"

// 数据定义
const announcements = ref([])
const dialogVisible = ref(false)
const dialogType = ref('add')
const formRef = ref(null)

const form = ref({
  id: null,
  content: '',
  status: 1
})

// 表单验证规则
const rules = {
  content: [
    { required: true, message: '请输入公告内容', trigger: 'blur' },
    { min: 1, max: 500, message: '长度在 1 到 500 个字符', trigger: 'blur' }
  ]
}

// 获取公告列表
const fetchAnnouncements = async () => {
  try {
    const res = await request.get('/announcement/list')
    if (res.code === 0) {
      announcements.value = res.data
    }
  } catch (error) {
    ElMessage.error('获取公告列表失败')
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
      content: '',
      status: 1
    }
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const isEdit = dialogType.value === 'edit'
        const url = isEdit ? '/announcement/update' : '/announcement/add'
        const method = isEdit ? 'put' : 'post'
        
        const res = await request[method](url, form.value)
        
        if (res.code === 0) {
          ElMessage.success(isEdit ? '更新成功' : '添加成功')
          dialogVisible.value = false
          fetchAnnouncements()
        } else {
          ElMessage.error(res.message || '操作失败')
        }
      } catch (error) {
        ElMessage.error('操作失败')
      }
    }
  })
}

// 删除公告
const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除该公告吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const res = await request.delete(`/announcement/delete/${id}`)
    if (res.code === 0) {
      ElMessage.success('删除成功')
      fetchAnnouncements()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 简化日期格式化函数
const formatDate = (date) => {
  if (!date) return '暂无'
  return date.split(' ')[0] // 如果日期中包含时间，只取日期部分
}

// 页面加载时获取数据
onMounted(() => {
  fetchAnnouncements()
})
</script>

<style scoped>
.announcement-manage {
  padding: 20px;
}

.header-actions {
  margin-bottom: 20px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

:deep(.el-table) {
  margin-top: 20px;
}

:deep(.el-dialog__body) {
  padding: 20px 40px;
}
</style>