<script setup>
import request from "../utills/request";
import { ref, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';

// 定义数据
const apiList = ref([]);
const dialogVisible = ref(false);
const loading = ref(false);

// 热搜数据相关的响应式变量
const hotSearchData = ref([]);
const hotSearchDialogVisible = ref(false);
const currentHotSearchName = ref('');

// 新增热搜的表单数据
const formData = ref({
  name: '',
  api: ''
});

// 格式化热度数字
const formatHot = (hot) => {
  if (!hot) return '0';
  const num = parseInt(hot);
  if (isNaN(num)) return hot;
  if (num < 10000) return num.toString();
  return (num / 10000).toFixed(1) + '万';
};

// 获取热搜列表
const getApiList = async () => {
  try {
    loading.value = true;
    const res = await request.get('/api');
    if (res.code === 0) {
      apiList.value = res.data;
    }
  } catch (error) {
    ElMessage.error('获取数据失败');
  } finally {
    loading.value = false;
  }
};

// 添加热搜
const addHotSearch = async () => {
  if (!formData.value.name || !formData.value.api) {
    ElMessage.warning('请填写完整信息');
    return;
  }
  
  try {
    // 修改为使用 URLSearchParams 来发送请求参数
    const params = new URLSearchParams();
    params.append('name', formData.value.name);
    params.append('link', formData.value.api);
    
    const res = await request.post('/api/addlink?' + params.toString());
    
    if (res.code === 0) { // 注意：这里改为 0，因为后端成功时返回的是 0
      ElMessage.success('添加成功');
      dialogVisible.value = false;
      getApiList();
      formData.value = { name: '', api: '' };
    } else {
      ElMessage.error(res.msg || '添加失败');
    }
  } catch (error) {
    console.error('Add hot search error:', error);
    ElMessage.error('添加失败：' + (error.response?.data?.msg || error.message || '未知错误'));
  }
};

// 删除热搜
const deleteHotSearch = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这条热搜吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });
    
    const res = await request.delete('/api/deletelink', {
      params: { id }
    });
    
    if (res.code === 0) {
      ElMessage.success('删除成功');
      getApiList();
    } else {
      ElMessage.error(res.msg || '删除失败');
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败');
    }
  }
};

// 获取热搜数据
const getHotSearchData = async (api, name) => {
  try {
    loading.value = true;
    const res = await request.get('/api/getHotSearch', {
      params: { url: api }
    });
    
    if (res.code === 0 && res.data) {
      try {
        // 尝试解析数据
        let jsonStr = res.data;
        if (typeof jsonStr === 'string') {
          jsonStr = jsonStr.replace(/[\u0000-\u001F\u007F-\u009F]/g, '')
                          .replace(/\\/g, '\\\\')
                          .trim();
        }

        const parsedData = JSON.parse(jsonStr);
        
        // 处理不同平台的数据格式
        if (parsedData.word_list) {
          // 处理抖音热搜格式
          hotSearchData.value = parsedData.word_list.map(item => ({
            title: item.word || '',
            hot: item.hot_value?.toString() || '',
            url: '',
            desc: '',
            pic: ''
          }));
          currentHotSearchName.value = name;
        } else if (parsedData.data && Array.isArray(parsedData.data)) {
          // 检查是否是知乎格式
          if (parsedData.data[0]?.target?.title) {
            hotSearchData.value = parsedData.data.map(item => ({
              title: item.target?.title || '',
              hot: item.detail_text || '',
              url: item.target?.url || '',
              desc: item.target?.excerpt || '',
              pic: item.children?.[0]?.thumbnail || ''
            }));
          } else {
            // 处理微博和百度热搜格式
            hotSearchData.value = parsedData.data.map(item => ({
              title: item.title || '',
              hot: item.hot?.toString() || '',
              url: item.url || item.mobilUrl || '',
              desc: item.desc || '',
              pic: item.pic || ''
            }));
          }
          currentHotSearchName.value = parsedData.title || name;
        } else {
          throw new Error('数据格式不正确');
        }

        hotSearchDialogVisible.value = true;
        ElMessage.success('获取热搜数据成功');
      } catch (parseError) {
        console.error('Parse error:', parseError);
        // 解析失败时直接显示原始数据
        hotSearchData.value = [{
          title: '原始数据',
          hot: '',
          url: '',
          desc: JSON.stringify(res.data, null, 2),
          pic: ''
        }];
        currentHotSearchName.value = name + ' (原始数据)';
        hotSearchDialogVisible.value = true;
        ElMessage.warning('数据解析失败，显示原始数据');
      }
    } else {
      ElMessage.error(res.message || '获取热搜数据失败');
    }
  } catch (error) {
    console.error('Hot search data error:', error);
    ElMessage.error('获取热搜数据失败：' + (error.message || '未知错误'));
    hotSearchData.value = [];
  } finally {
    loading.value = false;
  }
};

// 页面加载时获取列表数据
onMounted(() => {
  getApiList();
});
</script>

<template>
  <div class="hot-search-manage">
    <div class="header">
      <h2>热搜管理</h2>
      <el-button type="primary" @click="dialogVisible = true">添加热搜</el-button>
    </div>

    <el-table 
      v-loading="loading"
      :data="apiList" 
      border 
      style="width: 100%"
    >
      <el-table-column prop="name" label="热搜名称" />
      <el-table-column prop="api" label="API地址" show-overflow-tooltip />
      <el-table-column label="操作" width="200">
        <template #default="scope">
          <el-button 
            type="primary" 
            size="small"
            @click="getHotSearchData(scope.row.api)"
          >
            获取数据
          </el-button>
          <el-button 
            type="danger" 
            size="small"
            @click="deleteHotSearch(scope.row.id)"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 添加热搜对话框 -->
    <el-dialog 
      v-model="dialogVisible" 
      title="添加热搜" 
      width="500px"
    >
      <el-form :model="formData" label-width="100px">
        <el-form-item label="热搜名称">
          <el-input v-model="formData.name" placeholder="请输入热搜名称" />
        </el-form-item>
        <el-form-item label="API地址">
          <el-input v-model="formData.api" placeholder="请输入API地址" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="addHotSearch">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 热搜数据展示对话框 -->
    <el-dialog 
      v-model="hotSearchDialogVisible" 
      :title="`${currentHotSearchName}热搜数据`"
      width="900px"
    >
      <el-table 
        :data="hotSearchData" 
        border 
        style="width: 100%"
        max-height="600"
      >
        <el-table-column type="index" label="排名" width="60" />
        <el-table-column prop="title" label="标题" min-width="200">
          <template #default="scope">
            <a 
              :href="scope.row.url" 
              target="_blank" 
              class="hot-search-link"
            >
              {{ scope.row.title }}
            </a>
          </template>
        </el-table-column>
        <el-table-column prop="hot" label="热度" width="100">
          <template #default="scope">
            {{ formatHot(scope.row.hot) }}
          </template>
        </el-table-column>
        <el-table-column prop="desc" label="描述" min-width="300">
          <template #default="scope">
            <div class="desc-content" :class="{ 'raw-data': scope.row.title === '原始数据' }">
              {{ scope.row.desc || '暂无描述' }}
            </div>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="hotSearchDialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.hot-search-manage {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header h2 {
  margin: 0;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.hot-search-link {
  color: #409EFF;
  text-decoration: none;
  display: block;
  width: 100%;
}

.hot-search-link:hover {
  text-decoration: underline;
  color: #66b1ff;
}

.desc-content {
  white-space: pre-wrap;
  word-break: break-all;
}

.raw-data {
  font-family: monospace;
  font-size: 12px;
  max-height: 500px;
  overflow-y: auto;
}
</style>