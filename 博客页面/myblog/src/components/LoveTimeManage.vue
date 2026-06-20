<template>
  <div class="love-time-manage">
    <el-card class="love-time-card">
      <template #header>
        <div class="card-header">
          <h2>恋爱时光</h2>
          <el-button type="primary" @click="handleAdd">添加记录</el-button>
        </div>
      </template>

      <!-- 筛选区域 -->
      <div class="filter-section">
        <el-radio-group v-model="filterCreatPeople" @change="handleFilterChange">
          <el-radio-button label="">全部</el-radio-button>
          <el-radio-button label="1">男生</el-radio-button>
          <el-radio-button label="0">女生</el-radio-button>
        </el-radio-group>
      </div>

      <!-- 表格区域 -->
      <el-table :data="loveTimeList" style="width: 100%" v-loading="loading">
        <el-table-column prop="content" label="内容" show-overflow-tooltip />
        <el-table-column prop="creatPeople" label="发起人" width="100">
          <template #default="scope">
            {{ scope.row.creatPeople === '1' ? '男生' : '女生' }}
          </template>
        </el-table-column>
        <el-table-column prop="creatTime" label="时间" width="120">
          <template #default="scope">
            {{ formatDate(scope.row.creatTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
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
      title="添加恋爱时光"
      width="50%"
      @close="handleDialogClose"
    >
      <el-form :model="formData" label-width="120px" ref="formRef">
        <el-form-item label="内容" prop="content" :rules="[{ required: true, message: '请输入内容' }]">
          <el-input
            v-model="formData.content"
            type="textarea"
            :rows="4"
            placeholder="写下这个时刻的故事..."
          />
        </el-form-item>
        <el-form-item label="发起人" prop="creatPeople" :rules="[{ required: true, message: '请选择发起人' }]">
          <el-radio-group v-model="formData.creatPeople">
            <el-radio label="1">男生</el-radio>
            <el-radio label="0">女生</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="时间" prop="creatTime" :rules="[{ required: true, message: '请选择时间' }]">
          <el-date-picker
            v-model="formData.creatTime"
            type="date"
            placeholder="选择日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            :default-time="new Date(2000, 0, 1, 0, 0, 0)"
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

<script>
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../utills/request'
import dayjs from 'dayjs'

export default {
  name: 'LoveTimeManage',
  data() {
    return {
      loading: false,
      loveTimeList: [],
      filterCreatPeople: '', // 筛选条件
      dialogVisible: false,
      formData: {
        content: '',
        creatPeople: '1',
        creatTime: dayjs().format('YYYY-MM-DD')
      }
    }
  },
  created() {
    this.fetchLoveTimeList()
  },
  methods: {
    // 获取恋爱时光列表
    async fetchLoveTimeList() {
      this.loading = true
      try {
        const url = this.filterCreatPeople
          ? `/love-time/${this.filterCreatPeople}`
          : '/love-time'
        const response = await request.get(url)
        if (response.code === 0) {
          this.loveTimeList = response.data
        } else {
          ElMessage.error('获取列表失败')
        }
      } catch (error) {
        console.error('获取列表失败:', error)
        ElMessage.error('获取列表失败')
      } finally {
        this.loading = false
      }
    },

    // 处理筛选变化
    handleFilterChange() {
      this.fetchLoveTimeList()
    },

    // 格式化日期
    formatDate(date) {
      return date ? dayjs(date).format('YYYY-MM-DD') : ''
    },

    // 打开添加对话框
    handleAdd() {
      this.dialogVisible = true
      this.formData = {
        content: '',
        creatPeople: '1',
        creatTime: dayjs().format('YYYY-MM-DD')
      }
    },

    // 关闭对话框
    handleDialogClose() {
      this.formData = {
        content: '',
        creatPeople: '1',
        creatTime: null
      }
    },

    // 提交表单
    async handleSubmit() {
      const formRef = this.$refs.formRef
      if (!formRef) return

      await formRef.validate(async (valid) => {
        if (valid) {
          try {
            // 获取当前时间的时分秒
            const now = new Date()
            const timeString = dayjs(now).format('HH:mm:ss')
            const saveData = {
              ...this.formData,
              // 使用选择的日期和当前的时分秒
              creatTime: this.formData.creatTime + ' ' + timeString
            }
            const response = await request.post('/love-time', saveData)
            if (response.code === 0) {
              ElMessage.success('添加成功')
              this.dialogVisible = false
              this.fetchLoveTimeList()
            } else {
              ElMessage.error(response.msg || '添加失败')
            }
          } catch (error) {
            console.error('添加失败:', error)
            ElMessage.error('添加失败')
          }
        }
      })
    },

    // 处理删除
    handleDelete(row) {
      ElMessageBox.confirm('确定要删除这条记录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const response = await request.delete(`/love-time/${row.id}`)
          if (response.code === 0) {
            ElMessage.success('删除成功')
            this.fetchLoveTimeList()
          } else {
            ElMessage.error(response.msg || '删除失败')
          }
        } catch (error) {
          console.error('删除失败:', error)
          ElMessage.error('删除失败')
        }
      })
    }
  }
}
</script>

<style scoped>
.love-time-manage {
  padding: 20px;
}

.love-time-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.filter-section {
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

:deep(.el-form-item) {
  margin-bottom: 22px;
}
</style> 