# My blog-serve

一个基于 Spring Boot 的博客/媒体后端服务。

### 技术栈
- Java 17
- Spring Boot 2.6.13
- MySQL 8
- Redis
- Maven

### 运行前准备
- 安装并启动 MySQL、Redis
- 创建数据库并导入初始化脚本：`sql/myblog.sql`
- 配置 `src/main/resources/application.yml` 中的数据库与 Redis 信息（如用户名、密码、主机等）
- 可选：配置 OpenRouter 等第三方 Key（如不使用相关功能，可留空）

### 关键配置（摘自 `application.yml`）
- 服务端口：`server.port=4000`
- 静态资源：`./uploads/` 会作为静态目录对外暴露
- 上传目录：
  - 视频：`./uploads/video/`
  - 图片：`./uploads/images/`
  - 音乐：`./uploads/music/`
  - 歌词：`./uploads/lrc/`

### 构建与启动
- 开发运行：
```bash
mvn spring-boot:run
```
- 生产打包：
```bash
mvn clean package -DskipTests
java -jar target/Musicplatform-serve-0.0.1-SNAPSHOT.jar
```
- 主启动类：`com.tzy.MyBlogApplication`

### 文件API
- 文件上传
  - 路径：`POST /upload`
  - 表单字段：
    - `file`：必填，文件
    - `type`：必填，取值为 `video` | `images` | `music`
    - `lyricsFile`：可选，仅当 `type=music` 时可上传 `.lrc` 歌词文件
  - 返回示例：
```json
{
  "url": "http://<host>:4000/images/xxx.png",
  "lyricsUrl": "http://<host>:4000/music/lrc/xxx.lrc"
}
```
  - 说明：
    - 服务端会对文件计算哈希并以“哈希+原扩展名”存储，重复文件会直接返回已有地址
    - 歌词文件与音乐文件同哈希，扩展名为 `.lrc`
    - 访问路径规则：
      - 图片：`/images/{filename}`
      - 视频：`/video/{filename}`
      - 音乐：`/music/{filename}`
      - 歌词：`/music/lrc/{filename}`

- 静态资源访问
  - 任何放在 `./uploads/` 下的文件，均可按上述路径直接访问

### 数据库与持久化
- 使用 MyBatis Plus、PageHelper 等依赖（Mapper、Service 已分层）
- 实体与 Mapper 位于 `src/main/java/com/tzy` 下的对应包路径

### 日志
- 日志文件位于仓库根目录下的 `logs/`

 