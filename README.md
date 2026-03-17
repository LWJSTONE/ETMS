# 企业员工培训管理系统 (ETMS)

## 项目简介

企业员工培训管理系统（Enterprise Training Management System，简称ETMS）是一个功能完善的企业级培训管理平台，旨在帮助企业高效管理员工培训、考试考核、学习进度等业务。

## 技术栈

### 后端
- **框架**: Spring Boot 3.2.0
- **安全**: Spring Security + JWT
- **ORM**: MyBatis-Plus 3.5.5
- **数据库**: MySQL 8.0
- **缓存**: Redis
- **API文档**: Swagger/OpenAPI 3.0

### 前端
- **框架**: Vue 3.4 + TypeScript
- **构建工具**: Vite 5
- **UI组件**: Element Plus 2.4
- **状态管理**: Pinia
- **路由**: Vue Router 4
- **图表**: ECharts 5

## 功能模块

### 系统管理
- 用户管理：用户增删改查、导入导出、密码重置
- 角色管理：角色定义、权限分配
- 部门管理：组织架构维护
- 岗位管理：岗位信息维护
- 权限管理：菜单权限、按钮权限、数据权限
- 字典管理：数据字典维护
- 系统配置：系统参数配置
- 日志管理：操作日志、登录日志

### 培训管理
- 课程管理：课程信息维护、资源管理、审核流程
- 课程分类：分类树形结构管理
- 培训计划：计划制定、分配、发布
- 学习进度：进度跟踪、统计报表

### 签到管理
- 签到记录：多种签到方式支持
- 补签申请：补签申请与审批

### 考核管理
- 题库管理：多种题型支持
- 试卷管理：手动组卷、随机组卷
- 考试记录：在线考试、防作弊
- 成绩管理：成绩统计、分析

### 报表分析
- 培训报表：多维度培训数据分析
- 考核报表：考试通过率、成绩分布

### 我的培训（员工端）
- 我的课程：课程学习
- 我的考试：在线考试
- 学习记录：学习进度查看
- 我的成绩：成绩查询

## 项目结构

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
│       ├── schema.sql           # 数据库脚本
│       └── data.sql             # 初始数据
│
└── etms-frontend/               # 前端项目
    ├── src/
    │   ├── api/                 # API接口
    │   ├── assets/              # 静态资源
    │   ├── components/          # 公共组件
    │   ├── layouts/             # 布局组件
    │   ├── router/              # 路由配置
    │   ├── stores/              # 状态管理
    │   ├── styles/              # 样式文件
    │   ├── utils/               # 工具函数
    │   └── views/               # 页面组件
    ├── index.html
    ├── vite.config.ts
    └── package.json
```

## 快速开始

### 环境要求
- JDK 17+
- Node.js 18+
- MySQL 8.0+
- Redis 6.0+
- Maven 3.8+

### 后端启动

1. 创建数据库
```sql
CREATE DATABASE etms DEFAULT CHARACTER SET utf8mb4;
```

2. 修改配置文件
编辑 `etms-backend/src/main/resources/application.yml`，修改数据库和Redis连接信息。

3. 执行数据库脚本
```bash
mysql -u root -p etms < etms-backend/src/main/resources/schema.sql
mysql -u root -p etms < etms-backend/src/main/resources/data.sql
```

4. 启动后端
```bash
cd etms-backend
mvn spring-boot:run
```

后端服务将在 http://localhost:8080 启动

### 前端启动

```bash
cd etms-frontend
npm install
npm run dev
```

前端服务将在 http://localhost:3000 启动

### 默认账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | 123456 | 超级管理员 |
| trainadmin | 123456 | 培训管理员 |
| zhangsan | 123456 | 普通员工 |

## API文档

启动后端后访问：http://localhost:8080/api/swagger-ui.html

## 许可证

MIT License
