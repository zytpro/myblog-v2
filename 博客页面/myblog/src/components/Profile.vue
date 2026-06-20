<template>
  <el-card class="profile-card">
    <div class="profile-header">
      <div @click="uploadImageHandler" class="avatar-container">
        <el-avatar :src="userInfo.avatar" size="large" />
      </div>
    </div>

    <el-form :model="userInfo" ref="userForm" :rules="rules" label-width="120px" class="profile-form">
      <el-form-item label="用户名" prop="username">
        <el-input v-model="userInfo.username" :disabled="!isEditing" />
      </el-form-item>
      <el-form-item label="邮箱" prop="email">
        <el-input v-model="userInfo.email" :disabled="!isEditing" />
      </el-form-item>
      <el-form-item label="电话" prop="phone">
        <el-input v-model="userInfo.phone" :disabled="!isEditing" />
      </el-form-item>
      <el-form-item label="个人简介" prop="description">
        <el-input type="textarea" v-model="userInfo.description" :disabled="!isEditing" />
      </el-form-item>

      <el-form-item class="form-item-center">
        <el-button @click="saveProfile" type="success" class="btn-save">保存</el-button>
        <el-button @click="cancelEdit" type="default" class="btn-cancel">取消</el-button>
        <el-button @click="showChangePasswordDialog" type="primary" class="btn-change-password">修改密码</el-button>
      </el-form-item>
    </el-form>
  </el-card>

  <!-- 修改密码对话框 -->
  <el-dialog
    v-model="passwordDialogVisible"
    title="修改密码"
    width="30%"
    :close-on-click-modal="false"
  >
    <el-form
      :model="passwordForm"
      :rules="passwordRules"
      ref="passwordFormRef"
      label-width="100px"
    >
      <el-form-item label="原密码" prop="oldPassword">
        <el-input
          v-model="passwordForm.oldPassword"
          type="password"
          show-password
          placeholder="请输入原密码"
        />
      </el-form-item>
      <el-form-item label="新密码" prop="newPassword">
        <el-input
          v-model="passwordForm.newPassword"
          type="password"
          show-password
          placeholder="请输入新密码"
        />
      </el-form-item>
      <el-form-item label="确认密码" prop="confirmPassword">
        <el-input
          v-model="passwordForm.confirmPassword"
          type="password"
          show-password
          placeholder="请再次输入新密码"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="passwordDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleChangePassword">确认</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import request from "../utills/request.js";

const newUserInfo = ref({
  username: '',
  avatar: '',
  email: '',
  phone: '',
  description: ''
});

// 校验规则
const rules = {
  username: [{ required: true, message: "请输入用户名", trigger: "blur" }],
  email: [
    { required: true, message: "请输入邮箱", trigger: "blur" },
    { type: "email", message: "请输入正确的邮箱地址", trigger: "blur" }
  ],
  phone: [
    { required: true, message: "请输入电话", trigger: "change" },
    { pattern: /^[0-9]{11}$/, message: "请输入有效的电话号码", trigger: "blur" }
  ],
  description: []
};

const uploadedImageUrl = ref(""); 
const router = useRouter();
const isEditing = ref(true); 
const userInfo = ref({
  username: '',
  avatar: '',
  email: '',
  phone: '',
  description: ''
});


const originalUserInfo = ref({ ...userInfo.value });


const userForm = ref<any>(null);

const saveProfile = async () => {
  console.log('保存个人资料:', userInfo.value);

  if (userForm.value) {
    const valid = await userForm.value.validate();
    if (!valid) {
      ElMessage.error("请检查表单是否填写完整");
      return;
    }
  }
  try {
    newUserInfo.value = userInfo.value;
    const response = await request.post('/user/updatePersonalInfo', userInfo.value);
    if (response.code === 0) {
      ElMessage.success(response.message);
    }
  } catch (error: any) {
    ElMessage.error(error.message);
  }
};

const cancelEdit = () => {
  userInfo.value = { ...originalUserInfo.value };
};

const getUserInfo = async () => {
  try {
    const response = await request.post('/user/getPersonalInfo');
    if (response.code === 0) {
      userInfo.value = response.data;
      originalUserInfo.value = { ...userInfo.value };
    } else {
      ElMessage.error(response.message);
    }
  } catch (error: any) {
    ElMessage.error(error.message);
  }
};

const uploadImageHandler = async () => {
  const input = document.createElement("input");
  input.setAttribute("type", "file");
  input.setAttribute("accept", "image/*");
  input.click();

  input.onchange = async () => {
    const files = input.files;
    if (files && files[0]) {
      const file = files[0];
      try {
        const url = await uploadImage(file);
        uploadedImageUrl.value = url;
        userInfo.value.avatar = url; 
      } catch (error) {
        console.error("图片上传失败", error);
      }
    }
  };
};

const uploadImage = async (file: File) => {
  const formData = new FormData();
  formData.append("file", file);
  formData.append("type", "images");

  const response = await fetch("http://localhost:4000/upload", {
    method: "POST",
    body: formData,
  });

  if (!response.ok) {
    throw new Error("图片上传失败");
  }

  const data = await response.json();
  return data.url;
};

// 修改密码相关
const passwordDialogVisible = ref(false);
const passwordFormRef = ref<any>(null);
const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
});

const handleChangePassword = async () => {
  try {
    if (passwordFormRef.value) {
      await passwordFormRef.value.validate();
    }
    
    const params = {
      username: userInfo.value.username,
      oldPassword: passwordForm.value.oldPassword,
      newPassword: passwordForm.value.newPassword
    };

    const response = await request.post('/user/updatePassword', null, {
      params: params
    });
    
    if (response.code === 0) {
      ElMessage({
        type: 'success',
        message: response.message || '密码修改成功',
        duration: 2000
      });
      passwordDialogVisible.value = false;
      setTimeout(() => {
        router.push('/login');
      }, 2000);
    } else {
      let errorMsg = '修改密码失败';
      if (response.code === 1 && response.message === '原密码错误') {
        errorMsg = '原密码输入错误，请重新输入';
      } else if (response.message === '无此用户') {
        errorMsg = '用户信息异常，请重新登录';
      } else {
        errorMsg = response.message || '修改密码失败，请稍后重试';
      }
      ElMessage({
        type: 'error',
        message: errorMsg,
        duration: 3000
      });
    }
  } catch (error: any) {
    if (error.message) {
      ElMessage({
        type: 'error',
        message: error.message,
        duration: 3000
      });
    } else {
      ElMessage({
        type: 'error',
        message: '表单验证失败，请检查输入',
        duration: 3000
      });
    }
    return;
  }
};

const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能小于6位', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能小于6位', trigger: 'blur' },
    { 
      validator: (rule: any, value: string, callback: any) => {
        if (value === passwordForm.value.oldPassword) {
          callback(new Error('新密码不能与原密码相同'));
        } else {
          callback();
        }
      }, 
      trigger: 'blur' 
    }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule: any, value: string, callback: any) => {
        if (value !== passwordForm.value.newPassword) {
          callback(new Error('两次输入的密码不一致'));
        } else {
          callback();
        }
      },
      trigger: 'blur'
    }
  ]
};

const showChangePasswordDialog = () => {
  passwordDialogVisible.value = true;
  passwordForm.value = {
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
  };
};

getUserInfo();
</script>

<style scoped>
.profile-card {
  width: 600px;
  margin: 20px auto;
  padding: 20px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
  border-radius: 10px;
  background-color: #fff; 
}

.profile-header {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20px;
}


.profile-header .user-info {
  margin-left: 20px;
}

.profile-form {
  margin-top: 20px;
}

.el-avatar {
  border-radius: 50%;
  cursor: pointer; 
  width: 200px;
  height: 200px;
  border: 3px solid #e4e4e4; 
  transition: transform 0.3s ease-in-out;
}

.el-avatar:hover {
  transform: scale(1.1); 
}

.avatar-container {
  cursor: pointer;
}

.form-item-center {
  display: flex;
  justify-content: center;
  margin-top: 40px;
  margin-left: 50px;
}

.btn-save {
  width: 100px;
  margin-right: 10px;
  border-radius: 5px;
  background-color: #4CAF50;
  transition: background-color 0.3s;
}

.btn-save:hover {
  background-color: #45a049; 
}

.btn-cancel {
  width: 100px;
  border-radius: 5px;
  background-color: #f44336; 
  transition: background-color 0.3s;
}

.btn-cancel:hover {
  background-color: #e53935; 
}

.btn-change-password {
  width: 100px;
  margin-left: 10px;
  border-radius: 5px;
  background-color: #409EFF;
  transition: background-color 0.3s;
}

.btn-change-password:hover {
  background-color: #66b1ff;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.el-input,
.el-input__inner {
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1); 
}

.el-form-item__label {
  color: #333;
  font-weight: bold;
}

</style>
