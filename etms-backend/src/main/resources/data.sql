-- =============================================
-- 企业员工培训管理系统 初始化数据
-- ETMS (Enterprise Training Management System)
-- =============================================

USE etms;

-- =============================================
-- 初始化部门数据
-- =============================================
INSERT INTO sys_dept (id, parent_id, dept_name, dept_code, sort_order, level, ancestors, status) VALUES
(1, 0, '总公司', 'HQ', 1, 1, '0', 1),
(2, 1, '技术研发部', 'TECH', 1, 2, '0,1', 1),
(3, 1, '市场营销部', 'MARKET', 2, 2, '0,1', 1),
(4, 1, '人力资源部', 'HR', 3, 2, '0,1', 1),
(5, 1, '财务管理部', 'FINANCE', 4, 2, '0,1', 1),
(6, 1, '运营管理部', 'OPERATION', 5, 2, '0,1', 1),
(7, 2, '前端开发组', 'FE_DEV', 1, 3, '0,1,2', 1),
(8, 2, '后端开发组', 'BE_DEV', 2, 3, '0,1,2', 1),
(9, 2, '测试组', 'QA', 3, 3, '0,1,2', 1);

-- =============================================
-- 初始化岗位数据
-- =============================================
INSERT INTO sys_position (id, position_name, position_code, position_level, position_desc, dept_id, sort_order, status) VALUES
(1, '总经理', 'GM', '专家', '公司总经理，负责公司整体运营管理', 1, 1, 1),
(2, '部门经理', 'DM', '高级', '部门负责人，负责部门日常管理', NULL, 2, 1),
(3, '高级工程师', 'SE', '高级', '技术骨干，负责核心技术攻关', 2, 3, 1),
(4, '中级工程师', 'ME', '中级', '技术骨干，负责模块开发', 2, 4, 1),
(5, '初级工程师', 'JE', '初级', '技术开发人员，负责功能实现', 2, 5, 1),
(6, '人事专员', 'HR_SPEC', '初级', '人力资源专员，负责招聘培训', 4, 6, 1),
(7, '培训专员', 'TRAIN_SPEC', '初级', '培训专员，负责员工培训管理', 4, 7, 1);

-- =============================================
-- 初始化角色数据
-- =============================================
INSERT INTO sys_role (id, role_code, role_name, role_desc, data_scope, sort_order, status) VALUES
(1, 'admin', '超级管理员', '拥有系统全部权限', 1, 1, 1),
(2, 'employee', '普通员工', '参与培训学习', 3, 2, 1);

-- =============================================
-- 初始化用户数据 (密码为: 123456，BCrypt加密)
-- =============================================
INSERT INTO sys_user (id, username, password, real_name, gender, email, phone, dept_id, position_id, status) VALUES
(1, 'admin', '$2a$10$3be60RRk3AJV8cY5oVkaqOlag3ISilX/bOu55z/b9VwXkPO0.QzSm', '系统管理员', 1, 'admin@etms.com', '13800138000', 1, 1, 1),
(2, 'zhangsan', '$2a$10$3be60RRk3AJV8cY5oVkaqOlag3ISilX/bOu55z/b9VwXkPO0.QzSm', '张三', 1, 'zhangsan@etms.com', '13800138001', 2, 3, 1),
(3, 'lisi', '$2a$10$3be60RRk3AJV8cY5oVkaqOlag3ISilX/bOu55z/b9VwXkPO0.QzSm', '李四', 1, 'lisi@etms.com', '13800138002', 2, 4, 1),
(4, 'wangwu', '$2a$10$3be60RRk3AJV8cY5oVkaqOlag3ISilX/bOu55z/b9VwXkPO0.QzSm', '王五', 2, 'wangwu@etms.com', '13800138003', 3, 4, 1),
(5, 'trainadmin', '$2a$10$3be60RRk3AJV8cY5oVkaqOlag3ISilX/bOu55z/b9VwXkPO0.QzSm', '培训专员', 1, 'train@etms.com', '13800138004', 4, 7, 1);

-- =============================================
-- 初始化用户角色关联
-- =============================================
INSERT INTO sys_user_role (user_id, role_id) VALUES
(1, 1),
(2, 2),
(3, 2),
(4, 2),
(5, 2);

-- =============================================
-- 初始化权限数据
-- =============================================
INSERT INTO sys_permission (id, perm_code, perm_name, perm_type, parent_id, path, icon, component, sort_order, visible, status) VALUES
-- 系统管理
(1, 'system', '系统管理', 1, 0, '/system', 'Setting', NULL, 1, 1, 1),
(2, 'system:user', '用户管理', 1, 1, '/system/user', 'User', 'system/user/index', 1, 1, 1),
(3, 'system:role', '角色管理', 1, 1, '/system/role', 'UserFilled', 'system/role/index', 2, 1, 1),
(4, 'system:dept', '部门管理', 1, 1, '/system/dept', 'OfficeBuilding', 'system/dept/index', 3, 1, 1),
(5, 'system:position', '岗位管理', 1, 1, '/system/position', 'Briefcase', 'system/position/index', 4, 1, 1),
(6, 'system:permission', '权限管理', 1, 1, '/system/permission', 'Lock', 'system/permission/index', 5, 1, 1),
-- 培训管理
(20, 'training', '培训管理', 1, 0, '/training', 'Reading', NULL, 2, 1, 1),
(21, 'training:course', '课程管理', 1, 20, '/training/course', 'Notebook', 'training/course/index', 1, 1, 1),
(23, 'training:plan', '培训计划', 1, 20, '/training/plan', 'Calendar', 'training/plan/index', 3, 1, 1),
(24, 'training:progress', '学习进度', 1, 20, '/training/progress', 'DataLine', 'training/progress/index', 4, 1, 1),
-- 考核管理
(40, 'exam', '考核管理', 1, 0, '/exam', 'Edit', NULL, 4, 1, 1),
(41, 'exam:question', '题库管理', 1, 40, '/exam/question', 'Collection', 'exam/question/index', 1, 1, 1),
(42, 'exam:paper', '试卷管理', 1, 40, '/exam/paper', 'Document', 'exam/paper/index', 2, 1, 1),
(43, 'exam:record', '考试记录', 1, 40, '/exam/record', 'Tickets', 'exam/record/index', 3, 1, 1),
(44, 'exam:result', '成绩管理', 1, 40, '/exam/result', 'TrendCharts', 'exam/result/index', 4, 1, 1),
-- 报表分析
(50, 'report', '报表分析', 1, 0, '/report', 'DataAnalysis', NULL, 5, 1, 1),
(51, 'report:training', '培训报表', 1, 50, '/report/training', 'DataBoard', 'report/training/index', 1, 1, 1),
(52, 'report:exam', '考核报表', 1, 50, '/report/exam', 'Histogram', 'report/exam/index', 2, 1, 1),
-- 我的培训(员工端)
(60, 'my', '我的培训', 1, 0, '/my', 'UserFilled', NULL, 6, 1, 1),
(61, 'my:course', '我的课程', 1, 60, '/my/course', 'Reading', 'my/course/index', 1, 1, 1),
(62, 'my:exam', '我的考试', 1, 60, '/my/exam', 'Edit', 'my/exam/index', 2, 1, 1),
(63, 'my:progress', '学习记录', 1, 60, '/my/progress', 'DataLine', 'my/progress/index', 3, 1, 1),
(64, 'my:result', '我的成绩', 1, 60, '/my/result', 'TrendCharts', 'my/result/index', 4, 1, 1);

-- =============================================
-- 初始化角色权限关联
-- =============================================
-- 超级管理员拥有所有权限
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT 1, id FROM sys_permission;

-- 普通员工权限（只有我的培训模块）
INSERT INTO sys_role_permission (role_id, permission_id) VALUES
(2, 60), (2, 61), (2, 62), (2, 63), (2, 64);

-- =============================================
-- 初始化课程数据
-- =============================================
INSERT INTO training_course (id, course_name, course_code, course_desc, course_objective, category_id, duration, difficulty, status, create_by) VALUES
(1, 'Java编程基础', 'JAVA_BASIC', 'Java语言基础语法、面向对象编程、集合框架、IO流、多线程等核心知识', '掌握Java编程基础，能够独立完成简单项目开发', 4, 120, 2, 2, 1),
(2, 'Spring Boot实战', 'SPRING_BOOT', 'Spring Boot框架核心概念、自动配置原理、Web开发、数据访问、安全控制等', '掌握Spring Boot框架，能够快速构建企业级应用', 6, 180, 3, 2, 1),
(3, 'Vue3前端开发', 'VUE3_DEV', 'Vue3 Composition API、响应式原理、组件开发、路由管理、状态管理等', '掌握Vue3前端开发技术，能够开发复杂前端应用', 4, 150, 3, 2, 1),
(4, 'MySQL数据库优化', 'MYSQL_OPT', 'MySQL索引原理、SQL优化、分库分表、主从复制、性能调优等高级技术', '掌握MySQL数据库优化技巧，提升数据库性能', 5, 100, 4, 2, 1),
(5, '高效沟通技巧', 'COMM_SKILL', '职场沟通技巧、汇报方法、会议沟通、跨部门协作等实用技能', '提升沟通效率，改善职场人际关系', 10, 60, 1, 2, 1),
(6, '团队领导力', 'TEAM_LEADER', '团队建设、目标管理、激励机制、冲突处理、绩效管理等领导力技能', '提升领导力，打造高效团队', 7, 90, 3, 2, 1),
(7, 'Office办公技巧', 'OFFICE_TIPS', 'Word排版技巧、Excel数据处理、PPT设计制作等办公软件高级技巧', '提升办公效率，掌握Office高级应用', 9, 45, 1, 2, 1);

-- =============================================
-- 初始化题库数据
-- =============================================
INSERT INTO exam_question (id, question_code, question_type, question_content, option_a, option_b, option_c, option_d, answer, answer_analysis, difficulty, score, course_id, status, create_by) VALUES
(1, 'Q001', 1, 'Java中，哪个关键字用于定义类？', 'class', 'struct', 'object', 'define', 'A', 'Java使用class关键字定义类', 1, 2, 1, 1, 1),
(2, 'Q002', 1, 'Spring Boot默认使用什么作为日志框架？', 'Log4j', 'Logback', 'SLF4J', 'JUL', 'B', 'Spring Boot默认使用Logback作为日志框架', 1, 2, 2, 1, 1),
(3, 'Q003', 2, '以下哪些是Java的基本数据类型？', 'int', 'String', 'boolean', 'double', 'ACD', 'String是引用类型，不是基本数据类型', 2, 4, 1, 1, 1),
(4, 'Q004', 3, 'Vue3使用Composition API时，ref和reactive都可以用来创建响应式数据。', NULL, NULL, NULL, NULL, '正确', 'ref用于基本类型，reactive用于对象类型', 1, 2, 3, 1, 1),
(5, 'Q005', 1, 'MySQL中，哪个存储引擎支持事务？', 'MyISAM', 'InnoDB', 'MEMORY', 'ARCHIVE', 'B', 'InnoDB支持事务，MyISAM不支持', 1, 2, 4, 1, 1),
(6, 'Q006', 2, 'Spring Boot的核心特性包括哪些？', '自动配置', '起步依赖', '内嵌服务器', 'XML配置', 'ABC', 'Spring Boot不再使用XML配置，采用Java配置', 2, 4, 2, 1, 1),
(7, 'Q007', 3, 'Vue3的Composition API相比Options API更利于代码复用。', NULL, NULL, NULL, NULL, '正确', 'Composition API通过组合函数实现逻辑复用', 1, 2, 3, 1, 1),
(8, 'Q008', 1, 'MySQL中，使用哪个命令查看表结构？', 'SHOW TABLE', 'DESC TABLE', 'DESCRIBE', 'SELECT TABLE', 'C', 'DESCRIBE或DESC命令可以查看表结构', 1, 2, 4, 1, 1);


