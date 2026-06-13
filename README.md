# Interview Flash — 面试刷题系统后端

基于 Spring Boot 3.2 的全栈面试刷题系统后端服务，提供题目管理、在线刷题、社区文章、实时通知等完整功能。

## 技术栈

| 技术 | 说明 |
|------|------|
| **Spring Boot 3.2** | Java 17，RESTful API 框架 |
| **Spring Security + JWT** | 身份认证与权限控制 |
| **Spring Data JPA** | 数据持久化，Hibernate ORM |
| **PostgreSQL 15** | 关系型数据库 |
| **Flyway** | 数据库版本迁移（9 个迁移脚本） |
| **Caffeine** | 本地缓存（热点数据 TTL 3~30 分钟） |
| **SSE (SseEmitter)** | 服务端推送实时通知 |
| **Gradle 8.7+** | 构建工具（非 Maven） |
| **SpringDoc OpenAPI** | Swagger UI 接口文档 |
| **Apache POI** | Excel 题目批量导入 |
| **Lombok** | 减少样板代码 |

## 快速启动

### 方式一：Docker Compose（推荐）

```bash
# 从项目根目录启动（PostgreSQL + 后端 + 前端）
docker compose up -d --build

# 访问
# 前端: http://localhost:3000
# API:  http://localhost:8080
# Swagger: http://localhost:8080/swagger-ui.html
```

### 方式二：本地开发

```bash
# 1. 启动 PostgreSQL
docker run -d --name interview-flash-db \
  -e POSTGRES_DB=interview_flash \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres123 \
  -p 5432:5432 \
  postgres:15-alpine

# 2. 启动后端
cd backend
./gradlew bootRun
```

## 项目结构

```
src/main/java/com/flash/
├── InterviewFlashApplication.java          # 启动类
├── auth/                                   # 认证模块
│   ├── config/                             #   SecurityConfig, RateLimitFilter
│   ├── controller/AuthController.java      #   注册/登录/个人信息
│   ├── dto/                                #   LoginRequest, RegisterRequest, AuthResponse
│   ├── entity/                             #   User, Role
│   ├── jwt/JwtTokenProvider.java           #   JWT 令牌生成与验证
│   ├── repository/                         #   UserRepository, RoleRepository
│   └── service/AuthService.java            #   认证业务逻辑
├── community/                              # 社区模块
│   ├── controller/                         #   Article, Comment, Follow, Like, Series...
│   ├── dto/                                #   ArticleDTO, CommentDTO, NotificationDTO...
│   ├── entity/                             #   Article, Comment, Series, Topic, Tag...
│   ├── repository/                         #   数据访问层（含原子 SQL 更新方法）
│   └── service/                            #   业务逻辑（含 SSE 推送）
├── admin/controller/                       # 管理后台 API
├── common/                                 # 公共模块
│   ├── dto/ApiResponse.java               #   统一响应格式 {code, msg, data}
│   ├── entity/BaseEntity.java             #   基础实体（id, createdAt, updatedAt）
│   └── exception/                          #   BusinessException, GlobalExceptionHandler
├── config/                                 # 配置
│   ├── SpaController.java                 #   SPA 路由转发 + SEO 爬虫支持
│   └── RobotsController.java             #   robots.txt + sitemap.xml
├── controller/                             # 题库核心 API
├── dto/                                    # 题目相关 DTO
├── entity/                                 # 题目、分类、进度实体
├── repository/                             # 数据访问
└── service/                                # 业务逻辑
```

## API 端点总览

### 认证 (`/api/auth`)
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/register` | 用户注册 |
| POST | `/login` | 用户登录（返回 JWT） |
| GET | `/me` | 获取当前用户信息 |
| PUT | `/profile` | 更新个人资料 |
| PUT | `/password` | 修改密码 |

### 题库 (`/api/questions`)
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/` | 题目列表（分页 + 分类/难度/题型筛选） |
| GET | `/{id}` | 题目详情 |
| GET | `/random` | 随机一题 |
| GET | `/random/batch` | 随机批量出题 |
| GET | `/search` | 关键词搜索 |
| GET | `/count` | 题目总数 |
| GET | `/hot` | 热门题目 |

### 学习进度 (`/api/progress`)
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/` | 获取用户进度 |
| GET | `/question` | 获取指定题目进度 |
| GET | `/wrong` | 错题本 |
| GET | `/favorites` | 收藏列表 |
| GET | `/statistics` | 学习统计 |
| POST | `/` | 更新进度（答题提交） |
| DELETE | `/reset` | 重置题目进度 |

### 社区文章 (`/api/articles`)
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/` | 文章列表（分页 + 话题筛选） |
| GET | `/{id}` | 文章详情（含浏览量递增） |
| GET | `/my` | 我的文章 |
| GET | `/my/drafts` | 我的草稿 |
| GET | `/hot` | 热门文章 |
| GET | `/search` | 文章搜索 |
| POST | `/` | 发布文章 |
| PUT | `/{id}` | 编辑文章 |
| DELETE | `/{id}` | 删除文章 |
| POST | `/{id}/like` | 点赞/取消点赞（原子计数） |
| GET | `/{id}/like-status` | 查询点赞状态 |

### 评论 (`/api/articles/{articleId}/comments`)
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/` | 评论列表（树形结构） |
| POST | `/` | 发表评论（原子计数递增） |
| PUT | `/{commentId}` | 编辑评论 |
| DELETE | `/{commentId}` | 删除评论（原子计数递减） |
| POST | `/{commentId}/like` | 评论点赞（原子计数） |

### 关注 (`/api/follow`)
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/{userId}` | 关注/取消关注 |
| GET | `/{userId}/followers` | 粉丝列表 |
| GET | `/{userId}/following` | 关注列表 |
| GET | `/{userId}/status` | 关注状态 |

### 系列 (`/api/series`)
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/` | 系列列表 |
| GET | `/{id}` | 系列详情（含文章分页） |
| POST | `/` | 创建系列 |
| PUT | `/{id}` | 编辑系列 |
| DELETE | `/{id}` | 删除系列 |
| PUT | `/articles/{articleId}` | 文章加入系列 |

### 通知 (`/api/notifications`)
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/` | 通知列表（分页） |
| GET | `/unread-count` | 未读数量 |
| PUT | `/{id}/read` | 标记已读（原子 SQL + 用户归属验证） |
| PUT | `/read-all` | 全部标记已读 |
| GET | `/subscribe` | SSE 实时推送订阅 |

### 其他
| 模块 | 路径 | 说明 |
|------|------|------|
| 收藏 | `/api/bookmarks` | 文章收藏管理 |
| 黑名单 | `/api/block` | 用户屏蔽管理 |
| 标签 | `/api/tags` | 文章标签列表 |
| 话题 | `/api/topics` | 文章话题列表 |
| 统计 | `/api/statistics` | 答题/文章/粉丝趋势数据 |
| 上传 | `/api/upload` | 图片上传 |
| 管理 | `/api/admin/*` | 后台管理（需 ROLE_ADMIN） |

## 安全机制

- **JWT 认证**: 登录返回 JWT，前端通过 `Authorization: Bearer` 头传递
- **Spring Security**: 公开端点（login/register/SSE/subscribe/Swagger）免认证；`/api/admin/**` 需 `ROLE_ADMIN`
- **速率限制**: 登录端点 5 次/分钟/IP（`RateLimitFilter`）
- **原子计数器**: `viewCount`/`thumbsUpCount`/`commentCount`/`likeCount` 使用 `@Modifying @Query` 原子 SQL 更新，避免并发竞态
- **用户归属验证**: `markAsRead` 同时校验通知 ID 和用户 ID，防止越权操作

## 实时通知 (SSE)

`SseEmitterManager` 管理用户 SSE 连接（每用户最多 10 个），支持：
- 评论通知、点赞通知、关注通知
- 30 分钟超时自动重连
- payload 包含: id, type, summary, fromUserId, fromUserNickname, fromUserAvatar, targetId, createdAt

## 数据库迁移

使用 Flyway 管理 9 个版本化迁移脚本：

| 版本 | 说明 |
|------|------|
| V1 | 认证表（users, roles） |
| V2 | 社区表（articles, comments, follows, topics, tags） |
| V3 | 文章点赞表 |
| V4 | 社区种子数据 |
| V5 | 评论点赞表 |
| V6 | 文章收藏表 |
| V7 | 黑名单表 |
| V8 | 文章日浏览量表 |
| V9 | 文章系列表 |

## 测试

```bash
# 运行全部测试（21 个测试文件，含并发行为测试）
./gradlew test

# 仅运行并发行为测试
./gradlew test --tests "com.flash.concurrent.*"

# 构建可部署 JAR
./gradlew bootJar
```

## 统一响应格式

所有 API 返回统一格式：
```json
{
  "code": 200,
  "msg": "success",
  "data": {}
}
```

错误响应：
```json
{
  "code": 404,
  "msg": "文章不存在",
  "data": null
}
```
