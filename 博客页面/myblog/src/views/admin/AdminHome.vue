<template>
  <div class="admin-layout">
    <Header />
    <div class="main">
      <Sidebar />
      <div class="content">
        <!-- 面包屑导航 -->
        <Breadcrumb />
        <!-- 路由视图 -->
        <router-view />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import Header from '../../components/Header.vue'
import Sidebar from '../../components/Sidebar.vue'
import Breadcrumb from '../../components/Breadcrumb.vue'
</script>

<style scoped>
.admin-layout {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #f5f5f5;
}

.main {
  display: flex;
  flex: 1;
  overflow: hidden;
}

.content {
  flex: 1;
  padding: 16px;
  overflow: auto;
  background: #f7f7f7;
}
</style>
