<template>
  <div class="page-container">
    <!-- 搜索栏 -->
    <div class="search-bar-section">
      <div class="search-bar">
        <el-input
            v-model="searchQuery.title"
            placeholder="文章名称"
            class="search-input"
        ></el-input>

        <el-select
            v-model="searchQuery.categorySelected"
            placeholder="请选择分类"
            class="search-input"
        >
          <el-option
              v-for="category in categories"
              :key="category.value"
              :label="category.label"
              :value="category.value"
          ></el-option>
        </el-select>

        <el-select
            v-model="searchQuery.status"
            placeholder="发布状态"
            class="search-input"
        >
          <el-option label="已发布" value="1"></el-option>
          <el-option label="未发布" value="0"></el-option>
        </el-select>

        <el-button type="primary" @click="search">搜索</el-button>
        <el-button @click="reset">重置</el-button>
      </div>
    </div>

    <!-- 表格和新增按钮区域 -->
    <div class="table-section">
      <div class="table-header">
        <el-button
            type="success"
            @click="openModal"
            class="add-button"
        >新增</el-button>
      </div>

      <el-table :data="tableData" style="border: none;">
        <el-table-column
            type="selection"
            width="50"
            label="选择"
            align="center"
        ></el-table-column>

        <el-table-column
            prop="cover"
            label="文章封面"
            width="120"
            align="center"
        >
          <template #default="scope">
            <el-image
                :src="scope.row.cover + '?t=' + new Date().getTime()"
                fit="cover"
                style="width: 100%; height: 80px; border-radius: 4px"
            ></el-image>
          </template>
        </el-table-column>

        <el-table-column
            prop="title"
            label="文章名称"
            width="250"
            align="center"
        ></el-table-column>

        <el-table-column
            prop="category"
            label="分类"
            width="200"
            align="center"
        >
          <template #default="scope">
            <el-tag type="warning">{{ scope.row.category }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column
            prop="tags"
            label="标签"
            width="200"
            align="center"
        >
          <template #default="scope">
            <el-tag
                v-for="tag in scope.row.tags"
                :key="tag"
                type="info"
                style="margin-right: 4px"
            >{{ tag }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="340" align="center">
          <template #default="scope">
            <div style="display: flex; justify-content: center; gap: 10px">
              <el-button
                  type="success"
                  size="small"
                  @click="editArticle(scope.row)"
              >编辑</el-button>
              <el-button
                  type="danger"
                  size="small"
                  @click="deleteArticle(scope.row.id)"
              >删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 新增文章弹窗 -->
    <el-dialog
        v-model="isModalVisible"
        width="70%"
        @close="closeModal"
        fullscreen
        style="justify-content: center; align-items: center"
        :z-index="dialogZIndex"
    >
      <template #header>
        <span
            style="
            display: block;
            text-align: center;
            font-weight: bold;
            font-size: 18px;
          "
        >{{ isEditing ? '编辑文章' : '新增文章' }}</span>
      </template>
      <el-form
          ref="articleForm"
          :model="newArticle"
          :rules="rules"
          label-width="100px"
          class="article-form"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="文章名称" prop="title">
              <el-input
                  v-model="newArticle.title"
                  placeholder="请输入文章名称"
              ></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="分类" prop="category">
              <el-select
                  v-model="newArticle.category"
                  placeholder="请选择分类"
              >
                <el-option
                    v-for="item in categories"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value"
                ></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="文章简介">
              <el-input
                  v-model="newArticle.description"
                  placeholder="请输入文章简介"
              ></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="标签" prop="tags">
              <el-select
                  v-model="newArticle.tags"
                  multiple
                  placeholder="请选择标签"
              >
                <el-option
                    v-for="tag in tags"
                    :key="tag.value"
                    :label="tag.label"
                    :value="tag.value"
                ></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="封面图片" prop="img">
          <div class="simple-image-uploader">
            <button type="button" @click="uploadImageHandler">上传图片</button>
            <div v-if="uploadedImageUrl" class="preview">
              <img :src="uploadedImageUrl" alt="预览图" />
            </div>
          </div>
        </el-form-item>

        <el-form-item label="内容" prop="content">
          <RichTextEditor
              ref="richTextEditorRef"
              v-model="newArticle.content"
              placeholder="请输入文章内容..."
              style="width: 100%; height: 300px;"
          ></RichTextEditor>
        </el-form-item>

        <el-row justify="center" style="padding: 20px 0">
          <el-button
              @click="closeModal"
              style="margin-right: 10px; margin-top: 40px"
          >取消</el-button>
          <el-button
              type="primary"
              @click="submitArticle"
              style="margin-top: 40px"
          >{{ isEditing ? '保存' : '提交' }}</el-button>
        </el-row>
      </el-form>
    </el-dialog>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, reactive, nextTick } from 'vue';
import type { FormInstance } from 'element-plus';
import RichTextEditor from './RichTextEditor.vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import request from '../utills/request';
import type { Article, Category, Tag, SearchQuery, NewArticle } from '../types';


interface TableData {
  id: number;
  cover: string;
  title: string;
  content: string;
  description: string;
  author: string;
  type: string;
  category: string;
  categoryId?: string | number;
  tags: string[];
  pinned: number;
  views: string;
  likes: string;
  comments: string;
  updatedAt: string;
  createdAt: string;
}

interface ArticleResponse {
  total: number;
  articles: {
    id: number;
    title: string;
    description: string;
    views: string;
    comments: string;
    likes: string;
    updatedAt: string;
    createdAt: string;
    image: string;
    content: string;
    tags: string;
    category: string;
    pinned: string;
  }[];
}

const validateForm = async (form: FormInstance | undefined): Promise<boolean> => {
  if (!form) return false;
  return await form.validate().catch(() => false);
};

export default defineComponent({
  name: 'ArticleList',
  components: { RichTextEditor },
  setup() {
    const articleForm = ref<FormInstance>();
    
    const uploadedImageUrl = ref("");
    const isModalVisible = ref(false);
    const isEditing = ref(false); 
    const searchQuery = reactive({
      title: "",
      tag: "",
      categorySelected: null,
      status: null,
    });

    const tableData = ref<TableData[]>([]);
    const originalData = ref<TableData[]>([]);
    const categories = ref<Category[]>([]);
    const tags = ref<Tag[]>([]); 

    const newArticle = reactive<NewArticle>({
      title: "",
      content: "",
      tags: [],
      category: null,
      description: "",
      image: null,
      pinned: 0,
      views: "0",
      likes: "0",
      comments: "0"
    });

    const rules = {
      title: [
        { required: true, message: "请输入文章名称", trigger: "blur" },
        { min: 2, max: 100, message: "标题长度应在2-100个字符之间", trigger: "blur" }
      ],
      content: [
        { required: true, message: "请输入文章内容", trigger: "blur" }
      ],
      category: [
        { required: true, message: "请选择分类", trigger: "change" }
      ]
    };

    const richTextEditorRef = ref<InstanceType<typeof RichTextEditor> | null>(null);

    const uploadImageHandler = async () => {
      try {
        const input = document.createElement("input");
        input.setAttribute("type", "file");
        input.setAttribute("accept", "image/*");
        input.click();

        input.onchange = async (event) => {
          const file = event.target.files?.[0];
          if (!file) return;

          try {
            const formData = new FormData();
            formData.append('file', file);
            formData.append('type', 'images');

            const response = await request.post('/upload', formData);
            if (response && response.url) {
              uploadedImageUrl.value = response.url;
              newArticle.image = response.url;
              ElMessage.success('封面上传成功！');
            } else {
              throw new Error('上传失败：未获取到图片URL');
            }
          } catch (error) {
            console.error('封面上传失败:', error);
            ElMessage.error(error instanceof Error ? error.message : '封面上传失败，请重试');
          }
        };
      } catch (error) {
        console.error('选择文件失败:', error);
        ElMessage.error('选择文件失败，请重试');
      }
    };

    const uploadImage = async (file: File): Promise<string> => {
      try {
        const maxSize = 100 * 1024 * 1024;
        if (file.size > maxSize) {
          throw new Error('文件大小不能超过100MB');
        }

        const formData = new FormData();
        formData.append('file', file);
        formData.append('type', 'images');

        const response = await request.post('/upload', formData);
        
        if (response.code === 0 && response.data?.url) {
          return response.data.url;
        } else {
          throw new Error('上传失败：' + (response.message || '未知错误'));
        }
      } catch (error) {
        console.error('上传失败:', error);
        throw new Error(error instanceof Error ? error.message : '文件上传失败');
      }
    };

    const getAllArticle = async () => {
      try {
        const response = await request.get("/article");
        console.log('Response:', response); 

        if (response && Array.isArray(response.articles)) {
          originalData.value = response.articles.map(article => ({
            id: article.id,
            cover: article.image || '',
            title: article.title || '',
            content: article.content || '',
            description: article.description || '',
            author: "未知",
            type: "博客",
            category: article.category || "未分类",
            categoryId: article.category,
            tags: article.tags ? article.tags.split(',') : [],
            pinned: Number(article.pinned || 0),
            views: article.views || "0",
            likes: article.likes || "0",
            comments: article.comments || "0",
            updatedAt: article.updatedAt || '',
            createdAt: article.createdAt || ''
          }));
          
          tableData.value = [...originalData.value];
          console.log('处理后的文章数据:', tableData.value); 
        } else {
          throw new Error('获取文章列表失败：无效的响应数据');
        }
      } catch (error) {
        console.error("获取文章列表失败:", error);
        ElMessage.error("获取文章列表失败，请刷新页面重试");
      }
    };

    const getTagsAndCategories = async () => {
      try {
        const tagsResponse = await request.get("/article/tags");
        if (tagsResponse && Array.isArray(tagsResponse.data)) {
          tags.value = tagsResponse.data.map(tag => ({ 
            value: tag, 
            label: tag 
          }));
        }
        const categoriesResponse = await request.get("/categories");
        if (categoriesResponse && Array.isArray(categoriesResponse)) {
          categories.value = categoriesResponse.map(category => ({
            value: category.id || category.value,
            label: category.name || category.label,
          }));
        }
      } catch (error) {
        console.error("获取标签和分类失败:", error);
        ElMessage.error("获取标签和分类失败，请刷新页面重试");
      }
    };

    const search = () => {
      const filteredData = originalData.value.filter((article: TableData) => {
        const matchesTitle =
          !searchQuery.title || article.title.toLowerCase().includes(searchQuery.title.toLowerCase());

        const selectedCategory = categories.value.find(
          (cat) => cat.value === searchQuery.categorySelected
        );
        const matchesCategory =
          !searchQuery.categorySelected ||
          article.category === selectedCategory?.label;

        const matchesStatus =
          searchQuery.status === null || article.status === searchQuery.status;

        return matchesTitle && matchesCategory && matchesStatus;
      });

      tableData.value = filteredData;
    };

    const reset = () => {
      searchQuery.title = "";
      searchQuery.tag = "";
      searchQuery.categorySelected = null;
      searchQuery.status = null;
      tableData.value = [...originalData.value];
    };

    const resetNewArticle = () => {
      richTextEditorRef.value?.reset();

      Object.assign(newArticle, {
        id: undefined,
        title: "",
        content: "",
        tags: [],
        category: null,
        description: "",
        image: null,
        pinned: 0,
        views: "0",
        likes: "0",
        comments: "0"
      });
      
      uploadedImageUrl.value = "";
      isEditing.value = false;
    };

    const closeModal = () => {
      isModalVisible.value = false;
      nextTick(() => {
        resetNewArticle();
      });
    };

    const submitArticle = async () => {
      try {
        const valid = await validateForm(articleForm.value);
        if (!valid) {
          ElMessage.warning('请填写必填项');
          return;
        }
        const submitData = {
          id: isEditing.value ? newArticle.id : undefined,
          title: newArticle.title.trim(),
          content: newArticle.content,
          description: newArticle.description?.trim() || '',
          category: newArticle.category || '',
          tags: Array.isArray(newArticle.tags) ? newArticle.tags.join(',') : newArticle.tags || '',
          image: uploadedImageUrl.value || newArticle.image || '', // 使用 uploadedImageUrl
          pinned: Number(newArticle.pinned || 0),
          views: newArticle.views || "0",
          likes: newArticle.likes || "0",
          comments: newArticle.comments || "0"
        };

        console.log('提交的数据:', submitData); 

        let response;
        if (isEditing.value && newArticle.id) {
          response = await request.put(`/article/${newArticle.id}`, submitData);
          console.log('更新响应:', response); 
        } else {
          const { id, ...createData } = submitData;
          response = await request.post("/article/save", createData);
        }

        if (!response) {
          throw new Error('服务器响应异常');
        }

        ElMessage.success(isEditing.value ? "文章编辑成功" : "文章新增成功");
        closeModal();
        await getAllArticle();
      } catch (error: unknown) {
        console.error('保存文章失败:', error);
        const errorMessage = error instanceof Error 
          ? error.message 
          : (isEditing.value ? "编辑失败" : "新增失败");
        ElMessage.error(errorMessage);
      }
    };

    const editArticle = (article: TableData) => {
      resetNewArticle();
      
      nextTick(() => {
        isEditing.value = true;
        isModalVisible.value = true;
        nextTick(() => {
          const articleCopy = JSON.parse(JSON.stringify(article));
          const category = categories.value.find(
            cat => cat.label === articleCopy.category
          );
          
          Object.assign(newArticle, {
            id: articleCopy.id,
            title: articleCopy.title || '',
            content: articleCopy.content || '',
            tags: articleCopy.tags ? (
              Array.isArray(articleCopy.tags) 
                ? articleCopy.tags 
                : articleCopy.tags.split(',')
            ) : [],
            category: category ? category.value : null,
            description: articleCopy.description || '',
            image: articleCopy.image || articleCopy.cover || '',
            pinned: Number(articleCopy.pinned || 0),
            views: articleCopy.views || "0",
            likes: articleCopy.likes || "0",
            comments: articleCopy.comments || "0"
          });
          
          uploadedImageUrl.value = articleCopy.image || articleCopy.cover || '';
        });
      });
    };


    const deleteArticle = async (id: number) => {
      try {
        const confirmDelete = await ElMessageBox.confirm(
            '确定要删除这篇文章吗？',
            '删除文章',
            {
              confirmButtonText: '删除',
              cancelButtonText: '取消',
              type: 'warning'
            }
        );
        if (confirmDelete === 'confirm') {
          await request.delete(`/article/${id}`);
          ElMessage.success("文章删除成功");
          getAllArticle();
        }
      } catch (error: any) {
        if (error !== 'cancel' && error !== 'close') {
          ElMessage.error("删除失败，请重试");
          console.error(error);
        }
      }
    };

    const openModal = () => {
      resetNewArticle(); 
      nextTick(() => {
        isModalVisible.value = true;
      });
    };
    const dialogZIndex = 1000;

    getAllArticle();
    getTagsAndCategories();

    return {
      articleForm,
      searchQuery,
      tableData,
      categories,
      tags,
      originalData,
      isModalVisible,
      isEditing,
      search,
      reset,
      newArticle,
      rules,
      resetNewArticle,
      submitArticle,
      openModal,
      closeModal,
      editArticle,
      deleteArticle,
      uploadImageHandler,
      uploadedImageUrl,
      dialogZIndex,
      richTextEditorRef,
    };
  }
});
</script>


<style scoped>
.page-container {
  padding: 24px;
  background-color: #f5f5f5; 
  border-radius: 8px;
  min-height: 100vh;
  box-sizing: border-box;
}

.search-bar-section {
  margin-bottom: 24px;
  background-color: #ffffff;
  border-radius: 8px;
  padding: 20px 24px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.search-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  align-items: center;
}

.search-input {
  width: 200px;
}


.table-section {
  background-color: #ffffff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.el-table {
  width: 100%;
  border-radius: 8px;
  overflow: auto;
  max-height: 500px;
  display: block; 
}

.el-table th {
  background-color: #fafafa;
  font-weight: bold;
  text-align: center;
}

.el-table td {
  text-align: center;
  vertical-align: middle;
}

.el-image {
  border-radius: 4px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

/* 按钮样式 */
.add-button {
  background-color: #67c23a !important;
  color: white !important;
  font-weight: bold;
  margin-bottom: 16px;
}

.add-button:hover {
  background-color: #5daf32 !important;
}

.el-button[type="danger"] {
  background-color: #f56c6c !important;
  color: white !important;
}

.el-button[type="danger"]:hover {
  background-color: #ee5c5c !important;
}


.upload-demo {
  width: 20px;
  height: 20px;
  display: flex;
  justify-content: center;
  align-items: center;
  flex-direction: column;
  border: 1px dashed #d9d9d9;
  border-radius: 4px;
  cursor: pointer;
  background-color: #f8f8f8;
  transition: border-color 0.3s, background-color 0.3s;
  margin-left: 30px;
}

.upload-demo:hover {
  border-color: #409eff;
  background-color: #eef5ff;
}

.upload-demo i {
  font-size: 32px;
  color: #409eff;
}

.upload-demo div {
  font-size: 14px;
  color: #666;
}


.simple-image-uploader button {
  padding: 8px 12px;
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.preview img {
  margin-top: 10px;
  max-width: 100px;
  max-height: 120px;
  height: auto;
  border: 1px solid #ddd;
  border-radius: 4px;
  padding: 5px;
}




</style>
