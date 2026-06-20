<template>
  <div class="navbar-manage">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>导航栏管理</span>
          <el-button type="primary" @click="handleAdd">添加导航</el-button>
        </div>
      </template>

      <el-table :data="navbarList" style="width: 100%">
        <el-table-column prop="name" label="导航名称" />
        <el-table-column prop="state" label="状态" width="120">
          <template #default="scope">
            <el-switch
              v-model="scope.row.state"
              :active-value="'1'"
              :inactive-value="'0'"
              @change="(val) => handleStateChange(scope.row, val)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="scope">
            <el-button
              size="small"
              type="danger"
              @click="handleDelete(scope.row.name)"
            >删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="添加导航"
      width="500px"
    >
      <el-form :model="navbarForm" label-width="100px">
        <el-form-item label="导航名称">
          <el-input v-model="navbarForm.name" placeholder="请输入导航名称" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch
            v-model="navbarForm.state"
            :active-value="'1'"
            :inactive-value="'0'"
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
import request from '../utills/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const navbarList = ref([])
const dialogVisible = ref(false)
const navbarForm = ref({
  name: '',
  state: '1'
})

// 获取导航栏列表
const getNavbarList = async () => {
  try {
    const res = await request.get('/navbar/list')
    if (res && Array.isArray(res)) {
      navbarList.value = res
    } else {
      ElMessage.error('获取导航栏列表失败：数据格式错误')
    }
  } catch (error) {
    ElMessage.error('获取导航栏列表失败')
  }
}

// 添加按钮点击
const handleAdd = () => {
  navbarForm.value = {
    name: '',
    state: '1'
  }
  dialogVisible.value = true
}

// 切换导航状态
const handleStateChange = async (row, newState) => {
  try {
    const res = await request.put('/navbar/update', {
      name: row.name,
      state: newState
    })
    if (res?.code === 0 || res?.code === 200) {
      ElMessage.success('状态更新成功')
      getNavbarList()
    }
  } catch (error) {
    ElMessage.error('状态更新失败')
    // 如果更新失败，恢复原状态
    row.state = row.state === '1' ? '0' : '1'
  }
}

// 删除按钮点击
const handleDelete = async (name) => {
  try {
    await ElMessageBox.confirm('确定要删除该导航吗？', '提示', {
      type: 'warning'
    })
    const res = await request.delete(`/navbar/${name}`)
    if (res?.code === 0 || res?.code === 200) {
      ElMessage.success('删除成功')
      getNavbarList()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 提交表单
const handleSubmit = async () => {
  try {
    const { name, state } = navbarForm.value
    if (!name) {
      return ElMessage.warning('请填写导航名称')
    }

    const res = await request.post('/navbar/add', navbarForm.value)
    if (res?.code === 0 || res?.code === 200) {
      ElMessage.success('添加成功')
      dialogVisible.value = false
      getNavbarList()
    }
  } catch (error) {
    ElMessage.error('添加失败')
  }
}

// 组件挂载时获取列表
onMounted(() => {
  getNavbarList()
})
</script>

<style scoped>
.navbar-manage {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

:deep(.el-button:focus),
:deep(.el-button:focus-visible),
:deep(.el-button.is-focus),
:deep(.el-button:active) {
  outline: none !important;
  box-shadow: none !important;
}
</style>
