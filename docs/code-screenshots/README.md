# 代码截图索引

> 本目录包含ETMS项目核心代码的语法高亮截图，供手动插入到毕业论文中使用。
> 截图按编号和类别组织，方便与论文各章节对应。

---

## 📁 截图分类与论文对应关系

### 一、系统架构与启动类
| 编号 | 文件名 | 说明 | 论文章节参考 |
|------|--------|------|-------------|
| 72 | EtmsApplication-启动类.png | Spring Boot启动类 | 第3章 系统设计/3.1 总体架构 |
| 37 | pom.xml-Maven依赖配置.png | Maven项目依赖 | 第3章 系统设计/3.2 技术栈 |
| 36 | application.yml-应用配置.png | Spring Boot应用配置 | 第3章 系统设计/3.3 数据库与中间件配置 |

### 二、安全与认证模块（第5章 核心模块实现）
| 编号 | 文件名 | 说明 | 论文章节参考 |
|------|--------|------|-------------|
| 01 | SecurityConfig-Spring安全配置.png | Spring Security配置 | 5.1 用户认证与权限管理 |
| 02 | JwtTokenProvider-JWT令牌工具.png | JWT令牌生成与验证 | 5.1 用户认证与权限管理 |
| 03 | JwtAuthenticationFilter-JWT认证过滤器.png | JWT认证过滤器 | 5.1 用户认证与权限管理 |
| 68 | LoginUser-用户详情实现.png | UserDetails实现 | 5.1 用户认证与权限管理 |
| 69 | UserDetailsServiceImpl-用户加载服务.png | 用户认证加载服务 | 5.1 用户认证与权限管理 |
| 70 | CustomPermissionEvaluator-权限评估器.png | 自定义权限评估器 | 5.1 用户认证与权限管理 |
| 71 | MethodSecurityConfig-方法安全配置.png | 方法级安全配置 | 5.1 用户认证与权限管理 |
| 04 | AuthController-登录认证控制器.png | 登录控制器 | 5.1 用户认证与权限管理 |

### 三、后端控制器层
| 编号 | 文件名 | 说明 | 论文章节参考 |
|------|--------|------|-------------|
| 05 | UserController-用户管理控制器.png | 用户CRUD接口 | 5.2 用户管理模块 |
| 06 | CourseController-课程管理控制器.png | 课程管理接口 | 5.3 培训管理模块 |
| 07 | TrainingPlanController-培训计划控制器.png | 培训计划接口 | 5.3 培训管理模块 |
| 08 | PaperController-试卷管理控制器.png | 试卷管理接口 | 5.4 考试管理模块 |
| 09 | ExamRecordController-考试记录控制器.png | 考试记录接口 | 5.4 考试管理模块 |
| 10 | ExamResultController-考试结果控制器.png | 考试结果统计接口 | 5.4 考试管理模块 |
| 11 | RoleController-角色管理控制器.png | 角色管理接口 | 5.5 系统管理模块 |
| 12 | LearningProgressController-学习进度控制器.png | 学习进度接口 | 5.3 培训管理模块 |

### 四、后端服务实现层
| 编号 | 文件名 | 说明 | 论文章节参考 |
|------|--------|------|-------------|
| 13 | UserServiceImpl-用户服务实现.png | 用户管理业务逻辑 | 5.2 用户管理模块 |
| 14 | CourseServiceImpl-课程服务实现.png | 课程管理业务逻辑 | 5.3 培训管理模块 |
| 15 | TrainingPlanServiceImpl-培训计划服务实现.png | 培训计划业务逻辑 | 5.3 培训管理模块 |
| 16 | PaperServiceImpl-试卷服务实现.png | 试卷管理业务逻辑 | 5.4 考试管理模块 |
| 17 | ExamRecordServiceImpl-考试记录服务实现.png | 考试记录业务逻辑 | 5.4 考试管理模块 |
| 18 | LearningProgressServiceImpl-学习进度服务实现.png | 学习进度业务逻辑 | 5.3 培训管理模块 |
| 19 | RoleServiceImpl-角色服务实现.png | 角色管理业务逻辑 | 5.5 系统管理模块 |
| 20 | QuestionServiceImpl-题库服务实现.png | 题库管理业务逻辑 | 5.4 考试管理模块 |
| 77 | LogServiceImpl-日志服务实现.png | 操作日志服务 | 5.6 日志管理 |
| 78 | DeptServiceImpl-部门服务实现.png | 部门管理业务逻辑 | 5.5 系统管理模块 |
| 79 | PositionServiceImpl-岗位服务实现.png | 岗位管理业务逻辑 | 5.5 系统管理模块 |

### 五、AOP切面与配置类
| 编号 | 文件名 | 说明 | 论文章节参考 |
|------|--------|------|-------------|
| 21 | GlobalExceptionHandler-全局异常处理.png | 全局异常处理器 | 第4章/4.x 异常处理设计 |
| 22 | ApiLogAspect-操作日志AOP.png | 操作日志切面 | 第4章/4.x AOP切面设计 |
| 23 | RateLimiterAspect-限流AOP.png | Redis限流切面 | 第4章/4.x 安全设计 |
| 24 | RedisConfig-Redis配置.png | Redis缓存配置 | 第4章/4.x 缓存设计 |
| 25 | MyBatisPlusConfig-MyBatis配置.png | MyBatis-Plus分页配置 | 第4章/4.x 持久层设计 |
| 26 | Result-统一响应封装.png | 统一API响应 | 第4章/4.x 接口设计 |
| 27 | RateLimiter-限流注解.png | 自定义限流注解 | 第4章/4.x 安全设计 |

### 六、定时任务
| 编号 | 文件名 | 说明 | 论文章节参考 |
|------|--------|------|-------------|
| 28 | ExamTimeoutTask-考试超时任务.png | 考试超时自动提交 | 5.4 考试管理模块 |
| 29 | TrainingPlanStatusTask-计划状态任务.png | 培训计划状态流转 | 5.3 培训管理模块 |

### 七、实体与数据传输对象
| 编号 | 文件名 | 说明 | 论文章节参考 |
|------|--------|------|-------------|
| 30 | Entity-User-用户实体.png | 用户实体类 | 第4章/4.x 数据库设计 |
| 31 | Entity-Course-课程实体.png | 课程实体类 | 第4章/4.x 数据库设计 |
| 32 | Entity-TrainingPlan-培训计划实体.png | 培训计划实体类 | 第4章/4.x 数据库设计 |
| 33 | Entity-Paper-试卷实体.png | 试卷实体类 | 第4章/4.x 数据库设计 |
| 34 | Entity-ExamRecord-考试记录实体.png | 考试记录实体类 | 第4章/4.x 数据库设计 |
| 35 | Entity-Question-题目实体.png | 题目实体类 | 第4章/4.x 数据库设计 |
| 73 | DTO-LoginDTO.png | 登录DTO | 第4章/4.x 数据传输设计 |
| 74 | DTO-SubmitExamDTO.png | 提交考试DTO | 5.4 考试管理模块 |
| 75 | VO-LoginVO.png | 登录响应VO | 5.1 用户认证与权限管理 |

### 八、数据访问层（Mapper）
| 编号 | 文件名 | 说明 | 论文章节参考 |
|------|--------|------|-------------|
| 38 | UserMapper-用户Mapper.png | 用户数据访问接口 | 第4章/4.x 持久层设计 |
| 39 | CourseMapper-课程Mapper.png | 课程数据访问接口 | 第4章/4.x 持久层设计 |
| 40 | UserMapper.xml-自定义SQL.png | 自定义SQL映射 | 第4章/4.x 持久层设计 |
| 80 | UserMapper.xml-SQL映射.png | 用户自定义SQL映射 | 第4章/4.x 持久层设计 |

### 九、数据库设计
| 编号 | 文件名 | 说明 | 论文章节参考 |
|------|--------|------|-------------|
| 66 | schema-数据库表结构.png | 数据库表结构DDL | 第4章 数据库设计 |

### 十、前端：路由与状态管理
| 编号 | 文件名 | 说明 | 论文章节参考 |
|------|--------|------|-------------|
| 41 | router-路由配置.png | Vue Router路由配置 | 5.x 前端实现 |
| 42 | stores-user-用户状态管理.png | Pinia用户状态 | 5.1 用户认证与权限管理 |
| 43 | request-axios封装.png | Axios请求封装 | 5.x 前端实现 |
| 44 | permission-directive-权限指令.png | v-permission权限指令 | 5.1 用户认证与权限管理 |

### 十一、前端：API接口层
| 编号 | 文件名 | 说明 | 论文章节参考 |
|------|--------|------|-------------|
| 45 | api-auth-登录API.png | 登录认证API | 5.1 用户认证与权限管理 |
| 46 | api-course-课程API.png | 课程管理API | 5.3 培训管理模块 |
| 47 | api-training-培训API.png | 培训管理API | 5.3 培训管理模块 |
| 48 | api-exam-考试API.png | 考试管理API | 5.4 考试管理模块 |
| 49 | api-user-用户API.png | 用户管理API | 5.2 用户管理模块 |
| 67 | types-接口类型定义.png | TypeScript类型定义 | 5.x 前端实现 |

### 十二、前端：页面组件
| 编号 | 文件名 | 说明 | 论文章节参考 |
|------|--------|------|-------------|
| 50 | vue-login-登录页面.png | 登录页面组件 | 5.1 用户认证与权限管理 |
| 51 | vue-MainLayout-主布局.png | 系统主布局组件 | 5.x 前端实现 |
| 52 | vue-dashboard-仪表盘.png | 仪表盘页面 | 5.x 系统首页 |
| 53 | vue-training-plan-培训计划管理.png | 培训计划管理 | 5.3 培训管理模块 |
| 54 | vue-training-course-课程管理.png | 课程管理页面 | 5.3 培训管理模块 |
| 55 | vue-exam-paper-试卷管理.png | 试卷管理页面 | 5.4 考试管理模块 |
| 56 | vue-exam-question-题库管理.png | 题库管理页面 | 5.4 考试管理模块 |
| 57 | vue-exam-taking-在线考试.png | 在线考试页面 | 5.4 考试管理模块 |
| 58 | vue-system-user-用户管理.png | 用户管理页面 | 5.2 用户管理模块 |
| 59 | vue-system-role-角色管理.png | 角色管理页面 | 5.5 系统管理模块 |
| 60 | vue-system-dept-部门管理.png | 部门管理页面 | 5.5 系统管理模块 |
| 61 | vue-report-exam-考试报表.png | 考试统计报表 | 5.6 统计报表 |
| 62 | vue-report-training-培训报表.png | 培训统计报表 | 5.6 统计报表 |
| 63 | vue-my-learning-我的学习.png | 我的学习页面 | 5.x 员工端 |
| 64 | vue-my-course-我的课程.png | 我的课程页面 | 5.x 员工端 |
| 65 | vue-training-progress-培训进度.png | 培训进度页面 | 5.3 培训管理模块 |
| 76 | utils-format-格式化工具.png | 数据格式化工具 | 5.x 前端实现 |

---

## 📝 使用说明

1. 论文中需插入代码截图时，从 `docs/code-screenshots/` 目录中选取对应的 `.png` 文件
2. 每张截图顶部有蓝色标题栏，标注了源文件名和功能说明
3. 截图采用VS Code Dark+配色方案，语法高亮清晰易读
4. 编号格式说明：`编号-类名-功能说明.png`
5. 建议在论文中插入时使用"图X-X 文件名 — 功能说明"的图题格式
