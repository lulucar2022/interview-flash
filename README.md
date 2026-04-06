# Interview Flash - 面试刷题系统

## 快速部署

### 方式一：Docker Compose 一键部署（推荐）

```bash
cd /root/my-project/interview-flash

# 启动所有服务（PostgreSQL + 后端应用）
docker-compose up -d --build

# 查看日志
docker-compose logs -f

# 停止服务
docker-compose down
```

部署后访问：
- 应用：http://localhost:8080
- 接口文档：http://localhost:8080/swagger-ui.html

### 方式二：本地运行

#### 前置条件
- Java 17
- Maven 3.9+
- PostgreSQL 15

#### 步骤

1. 启动 PostgreSQL
```bash
docker run -d --name interview-flash-db \
  -e POSTGRES_DB=interview_flash \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres123 \
  -p 5432:5432 \
  postgres:15-alpine
```

2. 构建并运行应用
```bash
cd /root/my-project/interview-flash
mvn clean package -DskipTests
java -jar target/interview-flash-0.0.1-SNAPSHOT.jar
```

## API 端点

### 分类管理
- `GET /api/categories` - 获取所有分类
- `POST /api/categories` - 创建分类
- `PUT /api/categories/{id}` - 更新分类
- `DELETE /api/categories/{id}` - 删除分类

### 题目管理
- `GET /api/questions` - 获取题目列表
- `GET /api/questions/random` - 随机一题
- `GET /api/questions/{id}` - 获取题目详情
- `POST /api/questions` - 创建题目
- `PUT /api/questions/{id}` - 更新题目
- `DELETE /api/questions/{id}` - 删除题目

### 用户管理
- `GET /api/users` - 获取所有用户
- `GET /api/users/{id}` - 根据ID获取用户
- `GET /api/users/username/{username}` - 根据用户名获取用户
- `POST /api/users` - 创建用户
- `PUT /api/users/{id}` - 更新用户
- `DELETE /api/users/{id}` - 删除用户

### 学习进度
- `GET /api/progress?userId=1` - 获取学习进度
- `GET /api/progress/question?userId=1&questionId=1` - 获取指定题目进度
- `GET /api/progress/wrong?userId=1` - 获取错题本
- `GET /api/progress/favorites?userId=1` - 获取收藏
- `GET /api/progress/statistics?userId=1` - 获取统计信息
- `POST /api/progress?userId=1` - 更新学习进度
- `DELETE /api/progress/reset?userId=1&questionId=1` - 重置题目进度

## 项目结构

```
interview-flash/
├── src/main/java/com/flash/
│   ├── InterviewFlashApplication.java    # 启动类
│   ├── controller/                       # REST API 控制器
│   ├── service/                          # 业务逻辑
│   ├── repository/                       # 数据访问
│   ├── entity/                           # JPA 实体
│   └── dto/                              # 数据传输对象
├── src/main/resources/
│   ├── application.yml                  # 配置文件
│   └── schema.sql                       # 数据库表结构SQL
├── Dockerfile                            # Docker 镜像构建
├── docker-compose.yml                    # Docker Compose 配置
└── pom.xml                               # Maven 配置
```

## 技术栈

- **后端**: Spring Boot 3.2.x, Spring Data JPA, PostgreSQL
- **工具**: Lombok, Maven, SpringDoc OpenAPI
- **部署**: Docker, Docker Compose

## 数据库表结构

项目使用 PostgreSQL 数据库，包含以下表：

### users - 用户表
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGSERIAL | 主键，自增 |
| username | VARCHAR(50) | 用户名，唯一 |
| email | VARCHAR(100) | 邮箱，唯一 |
| password | VARCHAR(255) | 密码 |
| display_name | VARCHAR(100) | 显示名称 |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |

### categories - 分类表
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGSERIAL | 主键，自增 |
| name | VARCHAR(100) | 分类名称 |
| description | TEXT | 分类描述 |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |

### questions - 题目表
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGSERIAL | 主键，自增 |
| title | VARCHAR(255) | 题目标题 |
| content | TEXT | 题目内容 |
| answer | TEXT | 答案 |
| category_id | BIGINT | 分类ID |
| difficulty | VARCHAR(20) | 难度(LOW/MEDIUM/HIGH) |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |

### user_progress - 学习进度表
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGSERIAL | 主键，自增 |
| user_id | INTEGER | 用户ID |
| question_id | BIGINT | 题目ID |
| status | VARCHAR(20) | 状态(NEW/LEARNING/MASTERED/REVIEW) |
| is_correct | BOOLEAN | 是否答对 |
| is_favorite | BOOLEAN | 是否收藏 |
| review_count | INTEGER | 复习次数 |
| last_reviewed_at | TIMESTAMP | 最后复习时间 |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |

## 开发说明

### 创建测试数据
```bash
# 通过API创建用户
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"username":"test","email":"test@example.com","password":"123456","displayName":"测试用户"}'

# 创建分类
curl -X POST http://localhost:8080/api/categories \
  -H "Content-Type: application/json" \
  -d '{"name":"Java","description":"Java面试题"}'

# 创建题目
curl -X POST http://localhost:8080/api/questions \
  -H "Content-Type: application/json" \
  -d '{"title":"什么是多态？","content":"请解释Java多态的概念","answer":"多态是指...","categoryId":1,"difficulty":"MEDIUM"}'
```

### 运行测试
```bash
mvn test
```

### 构建镜像
```bash
mvn clean package -DskipTests
docker build -t interview-flash .
```
