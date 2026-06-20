## 我的博客（Vue 3 + Vite）

一个基于 Vue 3 的个人博客与后台管理前端项目，使用 Vite 构建，集成 Vuetify 与 Element Plus 作为 UI 组件库，配合 Pinia 与 Vue Router 完成状态管理与路由。

### 功能概览
- **前台页面**：
  - **首页**：`/`
  - **热搜**：`/hotsearch`
  - **我的小家**：`/myhome`
  - **留言墙**：`/message`
  - **音乐播放器（独立页）**：`/music-player`（不显示全局导航与页脚）
  - **文章详情**：`/article/:id`
- **后台管理（需登录）**：`/admin`
  - 后台首页、个人中心、图片管理、视频管理、音乐管理、文章管理、分类管理、标签管理、评论管理、热搜管理、公告管理、轮播图管理、导航栏管理、恋爱小屋、恋爱时光、生活时光
  - 登录页：`/admin/login`（已登录访问将跳转后台）
- **导航栏**：
  - 静态项：主页
  - 动态项：从后端接口 `/navbar/list` 加载（仅展示 `state` 为 1 的项）
  - 「后台管理」在新标签页打开，未登录跳转登录页

### 技术栈
- **框架**：Vue 3
- **构建**：Vite
- **路由**：Vue Router 4
- **状态管理**：Pinia
- **UI 组件**：Vuetify 3、Element Plus
- **数据请求**：Axios（封装于 `src/utills/request.js`）
- **日期处理**：dayjs（中文本地化）
- **图表**：ECharts（用于图表组件）
- **富文本**：TinyMCE（`public/tinymce_7.7.0/` 与 `@tinymce/tinymce-vue`）

### 目录结构
- `src/`
  - `main.js`：应用入口，注册 Router、Pinia、Vuetify、Element Plus
  - `App.vue`：全局布局（导航栏、主内容、页脚）与动态导航逻辑
  - `router/index.js`：路由与路由守卫
  - `stores/`：Pinia 状态
  - `components/`、`views/`：页面与组件
  - `config/env.js`：后端与资源基础地址配置
- `public/`：静态资源与 TinyMCE 资源
- `vite.config.js`：Vite 与 Element Plus 自动导入配置

### 环境与配置
- 接口与资源基础地址在 `src/config/env.js` 中配置：
  - `API_BASE_URL`
  - `IMAGE_BASE_URL`
  - `MUSIC_BASE_URL`
- 默认启用中文本地化（Element Plus、dayjs）。

### 开发与构建
```bash
# 安装依赖
npm install

# 本地开发
npm run dev

# 生产构建
npm run build

# 本地预览构建产物
npm run preview
```

### 路由说明（当前生效）
```text
/                      → Home
/hotsearch             → HotSearch
/myhome                → MyHome
/message               → MessageBoard
/music-player          → MusicPlay
/article/:id           → ArticleDetailPage
/admin/login           → AdminLogin
/admin                 → AdminHome
  ├─ ''                → Dashboard
  ├─ profile           → Profile
  ├─ picture           → PictureManage
  ├─ video             → VideoManage
  ├─ music             → MucicManage
  ├─ article           → ArticleManage
  ├─ category          → CategoryManage
  ├─ tags              → TagsManage
  ├─ commentManage     → CommentManage
  ├─ hotSearch         → HotSearchManage
  ├─ announcement      → AnnouncementManage
  ├─ carouseManage     → CarouseManage
  ├─ navbar            → NavbarManage
  ├─ love-room         → LoveRoomManage
  ├─ love-time-manage  → LoveTimeManage
  └─ life-time-manage  → LifeTimeManage
```

### 开发提示
- 导航数据从后台 `/navbar/list` 获取，需后端配合返回 `state` 为 1 的条目。
- 「后台管理」始终在新标签打开：已登录跳 `'/admin'`，未登录跳 `'/admin/login'`。
- 提示文案与页面文本以中文为主。

 ## 后台管理
 账号： jiujiu
 密码： 654321

 体验地址： www.jiujiutzy.cn