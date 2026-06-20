<template>
  <div class="comment-manage">
    <div class="header">
      <div class="title">评论管理</div>
      <div class="filter">
        <el-select 
          v-model="currentArticleId" 
          placeholder="请选择文章" 
          @change="getComments"
          style="width: 300px"
        >
          <el-option
            v-for="article in articles"
            :key="article.id"
            :label="article.title"
            :value="article.id"
          />
        </el-select>
      </div>
    </div>

    <el-card class="table-card">
      <el-table
        v-loading="loading"
        :data="comments"
        border
        style="width: 100%"
        :empty-text="'暂无评论数据'"
      >
        <el-table-column prop="id" label="序号" width="80" align="center" />
        <el-table-column prop="nickname" label="昵称" width="180" align="center" />
        <el-table-column prop="content" label="评论内容" min-width="300" show-overflow-tooltip>
          <template #default="scope">
            <div class="comment-content">{{ scope.row.content }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="评论时间" width="250" align="center">
          <template #default="scope">
            {{ formatDate(scope.row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="scope">
            <el-button 
              v-if="scope.row.status === 0"
              type="success" 
              size="small" 
              @click="approveComment(scope.row.id)"
            >
              通过
            </el-button>
            <el-button 
              v-if="scope.row.status === 0"
              type="danger" 
              size="small" 
              @click="rejectComment(scope.row.id)"
            >
              拒绝
            </el-button>
            <el-button 
              type="danger" 
              size="small" 
              @click="deleteComment(scope.row.id)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          background
          small
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import request from "../utills/request";

// 数据定义
const loading = ref(false);
const comments = ref([]);
const currentArticleId = ref('');
const articles = ref([]);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);

// 获取文章列表
const getArticles = async () => {
  try {
    const res = await request.get('/article', {
      params: {
        pageNum: 1,
        pageSize: 100  // 获取足够多的文章供选择
      }
    });
    if (res.articles) {
      articles.value = res.articles;
      if (articles.value.length > 0) {
        currentArticleId.value = articles.value[0].id;
        getComments();
      }
    }
  } catch (error) {
    ElMessage.error('获取文章列表失败');
  }
};

// 获取评论列表
const getComments = async () => {
  if (!currentArticleId.value) return;
  
  try {
    loading.value = true;
    const res = await request.get(`/comments/all/${currentArticleId.value}`);
    // 直接使用返回的数组
    comments.value = res || [];
    total.value = res?.length || 0;
  } catch (error) {
    console.error('Error fetching comments:', error);
    ElMessage.error('获取评论列表失败');
  } finally {
    loading.value = false;
  }
};

// 删除评论
const deleteComment = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这条评论吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });
    
    const res = await request.delete(`/comments/${id}`);
    ElMessage.success('删除成功');
    getComments();
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Error deleting comment:', error);
      ElMessage.error('删除失败');
    }
  }
};

// 审核评论
const approveComment = async (id) => {
  try {
    const res = await request.put(`/comments/${id}/approve`);
    ElMessage.success('审核通过');
    getComments();
  } catch (error) {
    console.error('Error approving comment:', error);
    ElMessage.error('操作失败');
  }
};

const rejectComment = async (id) => {
  try {
    const res = await request.put(`/comments/${id}/reject`);
    ElMessage.success('已拒绝');
    getComments();
  } catch (error) {
    console.error('Error rejecting comment:', error);
    ElMessage.error('操作失败');
  }
};

// 格式化日期
const formatDate = (date) => {
  if (!date) return '';
  const d = new Date(date);
  return `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日 ${d.getHours()}:${d.getMinutes()}:${d.getSeconds()}`;
};

// 获取状态类型
const getStatusType = (status) => {
  const types = {
    0: 'warning',   // 待审核
    1: 'success',   // 已通过
    2: 'danger'     // 已拒绝
  };
  return types[status] || 'info';
};

// 获取状态文本
const getStatusText = (status) => {
  const texts = {
    0: '待审核',
    1: '已通过',
    2: '已拒绝'
  };
  return texts[status] || '未知';
};

// 分页处理
const handleSizeChange = (val) => {
  pageSize.value = val;
  getComments();
};

const handleCurrentChange = (val) => {
  currentPage.value = val;
  getComments();
};

// 页面加载时获取文章列表
onMounted(() => {
  getArticles();
});
</script>

<style scoped>
.comment-manage {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.header {
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title {
  font-size: 22px;
  font-weight: bold;
  color: #303133;
}

.filter {
  display: flex;
  gap: 15px;
}

.table-card {
  background-color: #fff;
  border-radius: 4px;
}

.comment-content {
  white-space: pre-wrap;
  word-break: break-all;
  line-height: 1.5;
  padding: 8px 0;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
  padding: 10px 0;
}

:deep(.el-card__body) {
  padding: 20px;
}

:deep(.el-table) {
  margin-bottom: 20px;
}

:deep(.el-table th) {
  background-color: #f5f7fa;
  color: #606266;
  font-weight: bold;
}

:deep(.el-button--small) {
  padding: 6px 12px;
}

:deep(.el-tag) {
  padding: 0 8px;
  height: 24px;
  line-height: 24px;
}

:deep(.el-pagination) {
  padding: 0;
  margin: 0;
}
</style>