# 企业员工培训管理系统 (ETMS)

<div align="center">
  <img src="etms-frontend/src/assets/logo.svg" alt="ETMS Logo" width="120" height="120">
  
  <h3>Enterprise Training Management System</h3>
  <p>一套功能完善的企业级培训管理平台</p>
  
  [![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
  [![Vue](https://img.shields.io/badge/Vue-3.4.0-4fc08d.svg)](https://vuejs.org/)
  [![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)](https://www.mysql.com/)
  [![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
</div>

---

## 📖 项目简介

企业员工培训管理系统（Enterprise Training Management System，简称ETMS）是一个功能完善的企业级培训管理平台，旨在帮助企业高效管理员工培训、考试考核、学习进度等业务。

### 主要特点

- 🚀 **前后端分离架构**：Spring Boot 3 + Vue 3
- 🔐 **安全认证**：Spring Security + JWT
- 📊 **数据可视化**：ECharts图表展示
- 📱 **响应式设计**：支持多终端访问
- 📦 **开箱即用**：完整的业务功能模块

---

## 🛠️ 技术栈

### 后端技术

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.2.0 | 核心框架 |
| Spring Security | - | 安全认证 |
| JWT | 0.12.3 | Token认证 |
| MyBatis-Plus | 3.5.5 | ORM框架 |
| MySQL | 8.0 | 数据库 |
| Redis | 6.0+ | 缓存 |
| Swagger | 2.3.0 | API文档 |

### 前端技术

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.4.0 | 核心框架 |
| TypeScript | 5.3.3 | 类型支持 |
| Element Plus | 2.4.4 | UI组件库 |
| Vite | 5.0.10 | 构建工具 |
| Pinia | 2.1.7 | 状态管理 |
| Vue Router | 4.2.5 | 路由管理 |
| ECharts | 5.4.3 | 图表库 |

---

## 📁 项目结构

```
ETMS/
├── etms-backend/                 # 后端项目
│   ├── src/main/java/com/etms/
│   │   ├── config/              # 配置类
│   │   ├── controller/          # 控制器
│   │   ├── service/             # 服务层
│   │   ├── mapper/              # 数据访问层
│   │   ├── entity/              # 实体类
│   │   ├── dto/                 # 数据传输对象
│   │   ├── vo/                  # 视图对象
│   │   ├── common/              # 公共类
│   │   ├── security/            # 安全相关
│   │   └── utils/               # 工具类
│   └── src/main/resources/
│       ├── mapper/              # MyBatis XML
│       ├── application.yml      # 配置文件
│       ├── schema.sql           # 数据库建表脚本
│       └── data.sql             # 初始数据脚本
│
├── etms-frontend/               # 前端项目
│   ├── src/
│   │   ├── api/                 # API接口
│   │   ├── assets/              # 静态资源
│   │   ├── components/          # 公共组件
│   │   ├── layouts/             # 布局组件
│   │   ├── router/              # 路由配置
│   │   ├── stores/              # 状态管理
│   │   ├── styles/              # 样式文件
│   │   ├── utils/               # 工具函数
│   │   └── views/               # 页面组件
│   ├── index.html
│   ├── vite.config.ts
│   └── package.json
│
├── docs/                         # 文档目录
│   ├── 本地部署指南.md           # 部署指南
│   ├── 数据库设计文档.md         # 数据库文档
│   ├── API接口文档.md            # API文档
│   └── 系统使用说明书.md         # 使用手册
│
└── README.md                     # 项目说明
```

---

## 🎯 功能模块

### 系统管理
- ✅ 用户管理：用户增删改查、导入导出、密码重置
- ✅ 角色管理：角色定义、权限分配
- ✅ 部门管理：组织架构维护（支持无限级嵌套）
- ✅ 岗位管理：岗位信息维护
- ✅ 权限管理：菜单权限、按钮权限、数据权限
- ✅ 字典管理：数据字典维护
- ✅ 系统配置：系统参数配置
- ✅ 日志管理：操作日志、登录日志

### 培训管理
- ✅ 课程管理：课程信息维护、资源管理、审核流程
- ✅ 课程分类：分类树形结构管理
- ✅ 培训计划：计划制定、分配、发布
- ✅ 学习进度：进度跟踪、统计报表

### 签到管理
- ✅ 签到记录：多种签到方式支持（二维码/GPS/人脸）
- ✅ 补签申请：补签申请与审批

### 考核管理
- ✅ 题库管理：支持单选、多选、判断、填空、简答多种题型
- ✅ 试卷管理：手动组卷、随机组卷
- ✅ 考试记录：在线考试、防作弊机制
- ✅ 成绩管理：成绩统计、分析

### 报表分析
- ✅ 培训报表：多维度培训数据分析
- ✅ 考核报表：考试通过率、成绩分布

### 我的培训（员工端）
- ✅ 我的课程：课程学习
- ✅ 我的考试：在线考试
- ✅ 学习记录：学习进度查看
- ✅ 我的成绩：成绩查询

---

## 🚀 快速开始

### 环境要求

| 软件 | 版本要求 |
|------|---------|
| JDK | 17+ |
| MySQL | 8.0+ |
| Redis | 6.0+ |
| Node.js | 18+ |
| Maven | 3.8+ |

### 后端启动

```bash
# 1. 创建数据库
mysql -u root -p
CREATE DATABASE etms DEFAULT CHARACTER SET utf8mb4;

# 2. 执行数据库脚本
cd etms-backend/src/main/resources
mysql -u root -p etms < schema.sql
mysql -u root -p etms < data.sql

# 3. 修改配置文件
# 编辑 application.yml，修改数据库和Redis连接信息

# 4. 启动后端
cd etms-backend
mvn spring-boot:run
```

### 前端启动

```bash
# 1. 安装依赖
cd etms-frontend
npm install

# 2. 启动开发服务器
npm run dev
```

### 访问系统

| 服务 | 地址 |
|------|------|
| 前端系统 | http://localhost:3000 |
| 后端API | http://localhost:8080/api |
| Swagger文档 | http://localhost:8080/api/swagger-ui.html |

### 默认账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | 123456 | 超级管理员 |
| trainadmin | 123456 | 培训管理员 |
| zhangsan | 123456 | 普通员工 |

---

## 📚 文档说明

| 文档 | 路径 | 说明 |
|------|------|------|
| 本地部署指南 | [docs/本地部署指南.md](docs/本地部署指南.md) | 零基础部署教程 |
| 数据库设计文档 | [docs/数据库设计文档.md](docs/数据库设计文档.md) | 表结构设计说明 |
| API接口文档 | [docs/API接口文档.md](docs/API接口文档.md) | 接口说明文档 |
| 系统使用说明书 | [docs/系统使用说明书.md](docs/系统使用说明书.md) | 功能操作指南 |

---

## 📸 系统截图

### 登录页面
![登录页面](docs/screenshots/login.png)

### 系统首页
![系统首页](docs/screenshots/dashboard.png)

### 用户管理
![用户管理](docs/screenshots/user.png)

### 课程管理
![课程管理](docs/screenshots/course.png)

---

## 🔧 开发指南

### 后端开发

```bash
# 编译项目
mvn clean package -DskipTests

# 运行测试
mvn test

# 生成API文档
mvn swagger2markup:convertSwagger2markup
```

### 前端开发

```bash
# 开发模式
npm run dev

# 构建生产版本
npm run build

# 代码检查
npm run lint
```

---

## 🤝 参与贡献

1. Fork 本仓库
2. 新建分支 (`git checkout -b feature/xxx`)
3. 提交更改 (`git commit -m 'Add some feature'`)
4. 推送分支 (`git push origin feature/xxx`)
5. 提交 Pull Request

---

## 📄 许可证

本项目采用 [MIT](LICENSE) 许可证。

---

## 📧 联系方式

如有问题或建议，欢迎提交 Issue 或 Pull Request。

---

<div align="center">
  <p>⭐ 如果这个项目对你有帮助，请给一个 Star ⭐</p>
</div>
