# 企业员工培训管理系统 - API接口文档

## 📋 目录

1. [接口概述](#接口概述)
2. [认证授权](#认证授权)
3. [公共参数](#公共参数)
4. [用户模块](#用户模块)
5. [课程模块](#课程模块)
6. [培训计划模块](#培训计划模块)
7. [签到模块](#签到模块)
8. [考核模块](#考核模块)
9. [系统管理模块](#系统管理模块)
10. [错误码说明](#错误码说明)

---

## 接口概述

### 基础信息

| 项目 | 说明 |
|------|------|
| 接口地址 | http://localhost:8080/api |
| 协议 | HTTP/HTTPS |
| 数据格式 | JSON |
| 字符编码 | UTF-8 |
| 接口文档 | http://localhost:8080/api/swagger-ui.html |

### 后端技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 2.7.18 | 核心框架 |
| Spring Security | - | 安全认证 |
| JWT | 0.11.5 | Token认证 |
| MyBatis-Plus | 3.5.3.1 | ORM框架 |
| Knife4j | 3.0.3 | API文档 |

### 请求方式

| 方法 | 说明 |
|------|------|
| GET | 查询资源 |
| POST | 创建资源 |
| PUT | 更新资源 |
| DELETE | 删除资源 |

### 响应格式

所有接口统一返回以下格式（对应 `com.etms.common.Result`）：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {},
  "timestamp": "2024-01-01T12:00:00"
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| code | Integer | 状态码，200表示成功 |
| message | String | 提示信息 |
| data | Object | 返回数据 |
| timestamp | String | 时间戳 |

### 分页格式

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [],
    "total": 100,
    "current": 1,
    "size": 10,
    "pages": 10
  },
  "timestamp": "2024-01-01T12:00:00"
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| records | Array | 数据列表 |
| total | Long | 总记录数 |
| current | Long | 当前页码 |
| size | Long | 每页大小 |
| pages | Long | 总页数 |

---

## 认证授权

### 登录

**接口地址：** `POST /api/auth/login`

**对应控制器：** `com.etms.controller.AuthController`

**请求参数：**（对应 `com.etms.dto.LoginDTO`）

```json
{
  "username": "admin",
  "password": "123456"
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | String | 是 | 用户名 |
| password | String | 是 | 密码 |

**响应示例：**（对应 `com.etms.vo.LoginVO`）

```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400,
    "userId": 1,
    "username": "admin",
    "realName": "系统管理员",
    "avatar": null,
    "deptName": "总公司",
    "roles": ["admin"],
    "permissions": ["system:user:add", "system:user:edit"]
  }
}
```

### 登出

**接口地址：** `POST /api/auth/logout`

**对应控制器：** `com.etms.controller.AuthController`

**请求头：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| Authorization | String | 是 | Bearer {token} |

**响应示例：**

```json
{
  "code": 200,
  "message": "登出成功",
  "data": null
}
```

### 获取当前用户信息

**接口地址：** `GET /api/auth/info`

**对应控制器：** `com.etms.controller.AuthController`

**请求头：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| Authorization | String | 是 | Bearer {token} |

**响应示例：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "username": "admin",
    "realName": "系统管理员",
    "gender": 1,
    "email": "admin@etms.com",
    "phone": "13800138000",
    "avatar": null,
    "deptId": 1,
    "deptName": "总公司",
    "positionId": 1,
    "positionName": "总经理",
    "status": 1,
    "createTime": "2024-01-01T00:00:00"
  }
}
```

---

## 公共参数

### 请求头

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| Authorization | String | 是 | JWT Token，格式：Bearer {token} |
| Content-Type | String | 是 | application/json |

### 分页参数

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| current | Long | 否 | 1 | 当前页码 |
| size | Long | 否 | 10 | 每页大小 |

---

## 用户模块

**对应控制器：** `com.etms.controller.UserController`

### 获取用户列表

**接口地址：** `GET /api/users`

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| current | Long | 否 | 当前页码，默认1 |
| size | Long | 否 | 每页大小，默认10 |
| username | String | 否 | 用户名（模糊查询） |
| realName | String | 否 | 真实姓名（模糊查询） |
| phone | String | 否 | 手机号（模糊查询） |
| deptId | Long | 否 | 部门ID |
| status | Integer | 否 | 状态 |

**响应示例：**（对应 `com.etms.vo.UserVO`）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "id": 1,
        "username": "admin",
        "realName": "系统管理员",
        "gender": 1,
        "genderName": "男",
        "email": "admin@etms.com",
        "phone": "13800138000",
        "deptId": 1,
        "deptName": "总公司",
        "positionId": 1,
        "positionName": "总经理",
        "status": 1,
        "statusName": "正常",
        "createTime": "2024-01-01T00:00:00",
        "roles": [
          {
            "id": 1,
            "roleCode": "admin",
            "roleName": "超级管理员"
          }
        ]
      }
    ],
    "total": 1,
    "current": 1,
    "size": 10,
    "pages": 1
  }
}
```

### 获取用户详情

**接口地址：** `GET /api/users/{id}`

**路径参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 用户ID |

**响应示例：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "username": "admin",
    "realName": "系统管理员",
    "gender": 1,
    "email": "admin@etms.com",
    "phone": "13800138000",
    "avatar": null,
    "deptId": 1,
    "deptName": "总公司",
    "positionId": 1,
    "positionName": "总经理",
    "status": 1,
    "statusName": "正常"
  }
}
```

### 新增用户

**接口地址：** `POST /api/users`

**请求参数：**（对应 `com.etms.dto.UserDTO`）

```json
{
  "username": "testuser",
  "realName": "测试用户",
  "gender": 1,
  "email": "test@etms.com",
  "phone": "13800138001",
  "deptId": 2,
  "positionId": 4,
  "status": 1,
  "remark": "测试账号"
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | String | 是 | 用户名（唯一） |
| realName | String | 是 | 真实姓名 |
| gender | Integer | 否 | 性别(0未知/1男/2女) |
| email | String | 否 | 邮箱（唯一） |
| phone | String | 否 | 手机号（唯一） |
| deptId | Long | 否 | 部门ID |
| positionId | Long | 否 | 岗位ID |
| status | Integer | 否 | 状态，默认1 |
| remark | String | 否 | 备注 |

**响应示例：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": null
}
```

### 更新用户

**接口地址：** `PUT /api/users/{id}`

**路径参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 用户ID |

**请求参数：**

```json
{
  "realName": "新名字",
  "email": "newemail@etms.com",
  "phone": "13800138002",
  "deptId": 3,
  "status": 1
}
```

### 删除用户

**接口地址：** `DELETE /api/users/{id}`

**路径参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 用户ID |

### 重置密码

**接口地址：** `PUT /api/users/{id}/reset-password`

**路径参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 用户ID |

**响应示例：**

```json
{
  "code": 200,
  "message": "密码已重置为: 123456",
  "data": null
}
```

### 分配角色

**接口地址：** `PUT /api/users/{id}/roles`

**路径参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 用户ID |

**请求参数：**

```json
[1, 2, 3]
```

---

## 课程模块

**对应控制器：** `com.etms.controller.CourseController`

### 获取课程列表

**接口地址：** `GET /api/courses`

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| current | Long | 否 | 当前页码 |
| size | Long | 否 | 每页大小 |
| courseName | String | 否 | 课程名称（模糊查询） |
| categoryId | Long | 否 | 分类ID |
| status | Integer | 否 | 状态 |
| difficulty | Integer | 否 | 难度等级 |

**响应示例：**（对应 `com.etms.vo.CourseVO`）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "id": 1,
        "courseName": "Java编程基础",
        "courseCode": "JAVA_BASIC",
        "courseDesc": "Java语言基础语法...",
        "categoryId": 4,
        "categoryName": "编程语言",
        "duration": 120,
        "difficulty": 2,
        "difficultyName": "初级",
        "coverImage": null,
        "status": 2,
        "statusName": "已上架",
        "viewCount": 100,
        "ratingAvg": 4.5,
        "createTime": "2024-01-01T00:00:00"
      }
    ],
    "total": 7,
    "current": 1,
    "size": 10,
    "pages": 1
  }
}
```

### 获取课程详情

**接口地址：** `GET /api/courses/{id}`

### 新增课程

**接口地址：** `POST /api/courses`

**请求参数：**

```json
{
  "courseName": "Python数据分析",
  "courseCode": "PYTHON_DATA",
  "courseDesc": "Python数据分析入门课程",
  "courseObjective": "掌握数据分析基本方法",
  "categoryId": 4,
  "duration": 90,
  "difficulty": 2,
  "coverImage": "http://xxx.com/cover.jpg",
  "videoUrl": "http://xxx.com/video.mp4",
  "documentUrl": "http://xxx.com/doc.pdf",
  "targetAudience": "数据分析初学者",
  "prerequisite": "Python基础知识"
}
```

### 更新课程

**接口地址：** `PUT /api/courses/{id}`

### 删除课程

**接口地址：** `DELETE /api/courses/{id}`

### 提交审核

**接口地址：** `POST /api/courses/{id}/submit`

### 审核课程

**接口地址：** `POST /api/courses/{id}/audit`

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| status | Integer | 是 | 审核状态(2通过/4驳回) |
| auditRemark | String | 否 | 审核意见 |

### 上架课程

**接口地址：** `POST /api/courses/{id}/publish`

### 下架课程

**接口地址：** `POST /api/courses/{id}/unpublish`

---

## 培训计划模块

**对应控制器：** `com.etms.controller.TrainingPlanController`

### 获取培训计划列表

**接口地址：** `GET /api/plans`

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| current | Long | 否 | 当前页码 |
| size | Long | 否 | 每页大小 |
| planName | String | 否 | 计划名称 |
| status | Integer | 否 | 状态 |
| planType | Integer | 否 | 类型(1必修/2选修) |

**响应示例：**（对应 `com.etms.vo.TrainingPlanVO`）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "id": 1,
        "planName": "2024年度Java技术培训",
        "planCode": "PLAN_2024_JAVA",
        "planDesc": "面向技术部全员",
        "courseId": 1,
        "courseName": "Java编程基础",
        "planType": 1,
        "planTypeName": "必修",
        "startDate": "2024-01-15",
        "endDate": "2024-02-15",
        "status": 2,
        "statusName": "进行中",
        "createTime": "2024-01-01T00:00:00"
      }
    ],
    "total": 1,
    "current": 1,
    "size": 10,
    "pages": 1
  }
}
```

### 获取培训计划详情

**接口地址：** `GET /api/plans/{id}`

### 新增培训计划

**接口地址：** `POST /api/plans`

**请求参数：**

```json
{
  "planName": "2024年度新员工培训",
  "planCode": "PLAN_2024_NEW",
  "planDesc": "面向新入职员工",
  "planObjective": "帮助新员工快速融入公司",
  "courseId": 1,
  "planType": 1,
  "startDate": "2024-02-01",
  "endDate": "2024-02-28",
  "targetType": 1,
  "targetDeptIds": "[2, 3]",
  "minStudyTime": 60,
  "minProgress": 80,
  "needExam": 1,
  "passScore": 60
}
```

### 更新培训计划

**接口地址：** `PUT /api/plans/{id}`

### 删除培训计划

**接口地址：** `DELETE /api/plans/{id}`

### 发布培训计划

**接口地址：** `POST /api/plans/{id}/publish`

### 归档培训计划

**接口地址：** `POST /api/plans/{id}/archive`

---

## 签到模块

**对应控制器：** `com.etms.controller.AttendanceRecordController`

### 获取签到记录列表

**接口地址：** `GET /api/attendance/records`

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| current | Long | 否 | 当前页码 |
| size | Long | 否 | 每页大小 |
| planId | Long | 否 | 计划ID |
| userId | Long | 否 | 用户ID |
| status | Integer | 否 | 签到状态 |

**响应示例：**（对应 `com.etms.vo.AttendanceRecordVO`）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "id": 1,
        "userId": 2,
        "userName": "zhangsan",
        "realName": "张三",
        "planId": 1,
        "planName": "Java技术培训",
        "signTime": "2024-01-15T09:00:00",
        "signType": 1,
        "signTypeName": "二维码",
        "location": "北京市朝阳区",
        "status": 1,
        "statusName": "正常",
        "createTime": "2024-01-15T09:00:00"
      }
    ],
    "total": 1,
    "current": 1,
    "size": 10,
    "pages": 1
  }
}
```

### 签到

**接口地址：** `POST /api/attendance/sign`

**请求参数：**

```json
{
  "planId": 1,
  "signType": 1,
  "location": "北京市朝阳区xxx"
}
```

### 获取签到统计

**接口地址：** `GET /api/attendance/stats`

**响应示例：**（对应 `com.etms.vo.AttendanceStatsVO`）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "totalCount": 100,
    "normalCount": 85,
    "lateCount": 10,
    "earlyCount": 3,
    "absentCount": 2,
    "normalRate": 85.0
  }
}
```

---

## 考核模块

**对应控制器：**
- 题库管理：`com.etms.controller.QuestionController`
- 试卷管理：`com.etms.controller.PaperController`

### 获取题库列表

**接口地址：** `GET /api/exam/questions`

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| current | Long | 否 | 当前页码 |
| size | Long | 否 | 每页大小 |
| questionContent | String | 否 | 题目内容 |
| questionType | Integer | 否 | 题型(1单选/2多选/3判断/4填空/5简答) |
| difficulty | Integer | 否 | 难度(1简单/2中等/3困难) |
| courseId | Long | 否 | 关联课程ID |

**响应示例：**（对应 `com.etms.vo.QuestionVO`）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "id": 1,
        "questionCode": "Q001",
        "questionType": 1,
        "questionTypeName": "单选题",
        "questionContent": "Java中，哪个关键字用于定义类？",
        "optionA": "class",
        "optionB": "struct",
        "optionC": "object",
        "optionD": "define",
        "answer": "A",
        "answerAnalysis": "Java使用class关键字定义类",
        "difficulty": 1,
        "difficultyName": "简单",
        "score": 2,
        "courseId": 1,
        "createTime": "2024-01-01T00:00:00"
      }
    ],
    "total": 8,
    "current": 1,
    "size": 10,
    "pages": 1
  }
}
```

### 新增题目

**接口地址：** `POST /api/exam/questions`

**请求参数：**

```json
{
  "questionType": 1,
  "questionContent": "Spring Boot默认使用什么作为日志框架？",
  "optionA": "Log4j",
  "optionB": "Logback",
  "optionC": "SLF4J",
  "optionD": "JUL",
  "answer": "B",
  "answerAnalysis": "Spring Boot默认使用Logback作为日志框架",
  "difficulty": 1,
  "score": 2,
  "courseId": 2
}
```

### 获取试卷列表

**接口地址：** `GET /api/exam/papers`

### 新增试卷

**接口地址：** `POST /api/exam/papers`

**请求参数：**

```json
{
  "paperName": "Java基础测试卷",
  "paperType": 1,
  "courseId": 1,
  "totalScore": 100,
  "passScore": 60,
  "examDuration": 60,
  "questionCount": 50,
  "shuffleOption": 1,
  "shuffleQuestion": 1,
  "antiCheat": 1,
  "maxSwitch": 3
}
```

### 获取考试记录

**接口地址：** `GET /api/exam/records`

### 获取成绩列表

**接口地址：** `GET /api/exam/results`

---

## 系统管理模块

### 角色管理

**对应控制器：** `com.etms.controller.RoleController`

### 获取角色列表

**接口地址：** `GET /api/system/roles`

### 新增角色

**接口地址：** `POST /api/system/roles`

**请求参数：**

```json
{
  "roleCode": "teacher",
  "roleName": "讲师",
  "roleDesc": "课程讲师角色",
  "dataScope": 2,
  "sortOrder": 5,
  "status": 1
}
```

### 部门管理

**对应控制器：** `com.etms.controller.DeptController`

### 获取部门树

**接口地址：** `GET /api/system/depts/tree`

**响应示例：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "deptName": "总公司",
      "parentId": 0,
      "children": [
        {
          "id": 2,
          "deptName": "技术研发部",
          "parentId": 1,
          "children": []
        }
      ]
    }
  ]
}
```

### 字典管理

### 获取字典列表

**接口地址：** `GET /api/system/dicts`

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| dictType | String | 是 | 字典类型 |

**响应示例：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "dictType": "user_status",
      "dictCode": "0",
      "dictLabel": "禁用",
      "dictValue": "0",
      "sortOrder": 1
    },
    {
      "dictType": "user_status",
      "dictCode": "1",
      "dictLabel": "正常",
      "dictValue": "1",
      "sortOrder": 2
    }
  ]
}
```

### 系统配置

### 获取系统配置

**接口地址：** `GET /api/system/configs`

### 更新系统配置

**接口地址：** `PUT /api/system/configs/{key}`

### 日志管理

### 获取操作日志

**接口地址：** `GET /api/system/logs/operation`

### 获取登录日志

**接口地址：** `GET /api/system/logs/login`

---

## 错误码说明

### 公共错误码

| 错误码 | 说明 |
|--------|------|
| 200 | 操作成功 |
| 400 | 请求参数错误 |
| 401 | 未授权，请先登录 |
| 403 | 权限不足 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

### 业务错误码

| 错误码 | 说明 |
|--------|------|
| 1001 | 用户名已存在 |
| 1002 | 邮箱已存在 |
| 1003 | 手机号已存在 |
| 1004 | 用户不存在 |
| 1005 | 密码错误 |
| 1006 | 账号已被禁用 |
| 1007 | 账号已被锁定 |
| 2001 | 课程不存在 |
| 2002 | 课程未上架 |
| 2003 | 课程已下架 |
| 3001 | 培训计划不存在 |
| 3002 | 培训计划未发布 |
| 3003 | 培训已结束 |
| 4001 | 签到时间已过 |
| 4002 | 签到位置不在范围内 |
| 5001 | 试卷不存在 |
| 5002 | 考试时间已结束 |
| 5003 | 考试次数已达上限 |

---

## Swagger访问

启动后端服务后，访问以下地址查看完整的API文档：

**地址：** http://localhost:8080/api/swagger-ui.html

---

## 安全配置说明

### JWT认证流程

1. 用户登录成功后，服务端生成JWT Token
2. 前端将Token存储在本地（localStorage或vuex/pinia）
3. 后续请求在Header中携带Token：`Authorization: Bearer {token}`
4. 服务端通过 `JwtAuthenticationFilter` 验证Token有效性
5. 验证通过后，将用户信息存入SecurityContext

### 安全配置类

- `SecurityConfig`：Spring Security主配置类
- `JwtTokenProvider`：JWT Token生成与验证
- `JwtAuthenticationFilter`：JWT认证过滤器
- `JwtAuthenticationEntryPoint`：认证失败处理
- `UserDetailsServiceImpl`：用户详情加载服务

### 忽略认证的路径

以下路径不需要JWT认证（在 `application.yml` 中配置）：

- `/auth/login` - 登录接口
- `/auth/captcha` - 验证码接口
- `/auth/register` - 注册接口
- `/files/**` - 文件访问路径
- `/swagger-ui/**` - Swagger UI
- `/v3/api-docs/**` - API文档
- `/doc.html` - Knife4j文档

---

**文档版本：1.0.0**
