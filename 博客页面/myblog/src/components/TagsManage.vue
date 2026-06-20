<template>
  <div>
    <!-- 搜索区域 -->
    <el-row :gutter="20" style="margin-bottom: 20px;">
      <el-col :span="8">
        <el-input
            v-model="search.name"
            placeholder="请输入标签名称"
            clearable
        />
      </el-col>
      <el-col :span="8">
        <el-select
            v-model="search.status"
            placeholder="请选择状态"
            clearable
        >
          <el-option label="启用" value="1" />
          <el-option label="禁用" value="0" />
        </el-select>
      </el-col>
      <el-col :span="8">
<!--        <el-button type="primary" @click="applyFilter">搜索</el-button>-->
        <el-button @click="resetFilter" type="primary">重置</el-button>
      </el-col>
    </el-row>

    <!-- 新增标签按钮 -->
    <el-button type="success" plain @click="openAddDialog">
      新增标签
    </el-button>

    <!-- 表格展示 -->
    <el-table :data="filteredTags" border style="margin-top: 20px; width: 600px">
      <el-table-column prop="name" label="标签名称" width="200" />
      <el-table-column label="状态" width="200">
        <template #default="scope">
          <el-tag :type="scope.row.status === '1' ? 'success' : 'danger'">
            {{ scope.row.status === '1' ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column label="操作" width="200">
        <template #default="scope">
          <el-button type="primary" size="small" @click="openEditDialog(scope.row)">
            编辑
          </el-button>
          <el-button type="danger" size="small" @click="deleteCategory(scope.row)">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="form">
        <el-form-item label="标签名称" :label-width="formLabelWidth">
          <el-input v-model="form.name" autocomplete="off" />
        </el-form-item>
        <el-form-item label="状态" :label-width="formLabelWidth">
          <el-select v-model="form.status" placeholder="请选择状态">
            <el-option label="启用" value="1" />
            <el-option label="禁用" value="0" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveCategory">确认</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, computed, onMounted } from "vue";
import request from "../utills/request";
import { ElMessageBox, ElMessage } from "element-plus";

// 初始化标签数据
const tags = reactive([]);

// 搜索条件
const search = reactive({
  name: "", // 标签名称
  status: null, // 标签状态
});

// 弹窗控制
const dialogVisible = ref(false);
const dialogTitle = ref("");

// 表单数据
const form = reactive({
  id: null,
  name: "",
  status: "1", // 默认启用状态
});

const formLabelWidth = "120px";

// 过滤后的标签数据
const filteredTags = computed(() => {
  return tags.filter((tag) => {
    const matchName = !search.name || tag.name.includes(search.name.trim());
    const matchStatus = search.status === null || tag.status === search.status;
    return matchName && matchStatus;
  });
});

// 应用过滤条件
const applyFilter = () => {
  // Vue 的响应式特性会实时更新 filteredTags，无需额外逻辑
};

// 重置过滤条件
const resetFilter = () => {
  search.name = "";
  search.status = null;
};

// 打开新增标签弹窗
const openAddDialog = () => {
  dialogTitle.value = "新增标签";
  Object.assign(form, { id: null, name: "", status: "1" });
  dialogVisible.value = true;
};

// 打开编辑标签弹窗
const openEditDialog = (category) => {
  dialogTitle.value = "编辑标签";
  Object.assign(form, category);
  dialogVisible.value = true;
};

// 保存标签（新增或编辑）
const saveCategory = async () => {
  try {
    if (form.id === null) {
      const response = await request.post("/tags", form);
      if (response.code === 0) {
        ElMessage.success("添加成功");
        await fetchtags();
      }
    } else {
      const response = await request.put(`/tags`, form);
      if (response.code === 0) {
        ElMessage.success("更新成功");
        await fetchtags();
      }
    }
    dialogVisible.value = false;
  } catch (error) {
    console.error("保存标签失败:", error);
    ElMessage.error("操作失败，请稍后再试");
  }
};

// 获取标签数据
const fetchtags = async () => {
  try {
    const response = await request.get("/tags");
    if (Array.isArray(response)) {
      tags.splice(0, tags.length, ...response);
    }
  } catch (error) {
    console.error("获取标签数据失败:", error);
  }
};

// 删除标签
const deleteCategory = async (category) => {
  try {
    await ElMessageBox.confirm(
        `确定要删除标签 "${category.name}" 吗？`,
        "删除确认",
        {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning",
        }
    );

    const response = await request.delete(`/tags/${category.id}`);
    if (response.code === 0) {
      ElMessage.success("删除成功");
      const index = tags.findIndex((cat) => cat.id === category.id);
      if (index !== -1) {
        tags.splice(index, 1);
      }
    } else {
      ElMessage.error("删除失败");
    }
  } catch (error) {
    if (error !== "cancel") {
      console.error("删除出错:", error);
      ElMessage.error("系统出错，请稍后再试");
    }
  }
};

onMounted(fetchtags);
</script>

<style scoped>
.dialog-footer {
  text-align: right;
}
</style>
