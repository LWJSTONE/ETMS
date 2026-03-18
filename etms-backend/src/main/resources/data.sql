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
(2, 'train_admin', '培训管理员', '管理培训相关业务', 1, 2, 1),
(3, 'dept_manager', '部门主管', '管理本部门培训事务', 2, 3, 1),
(4, 'employee', '普通员工', '参与培训学习', 3, 4, 1);

-- =============================================
-- 初始化用户数据 (密码为: 123456，BCrypt加密)
-- =============================================
INSERT INTO sys_user (id, username, password, real_name, gender, email, phone, dept_id, position_id, status) VALUES
(1, 'admin', '$2a$10$3be60RRk3AJV8cY5oVkaqOlag3ISilX/bOu55z/b9VwXkPO0.QzSm', '系统管理员', 1, 'admin@etms.com', '13800138000', 1, 1, 1),
(2, 'zhangsan', '$2a$10$3be60RRk3AJV8cY5oVkaqOlag3ISilX/bOu55z/b9VwXkPO0.QzSm', '张三', 1, 'zhangsan@etms.com', '13800138001', 2, 3, 1),
(3, 'lisi', '$2a$10$3be60RRk3AJV8cY5oVkaqOlag3ISilX/bOu55z/b9VwXkPO0.QzSm', '李四', 1, 'lisi@etms.com', '13800138002', 2, 4, 1),
(4, 'wangwu', '$2a$10$3be60RRk3AJV8cY5oVkaqOlag3ISilX/bOu55z/b9VwXkPO0.QzSm', '王五', 2, 'wangwu@etms.com', '13800138003', 3, 4, 1),
(5, 'trainadmin', '$2a$10$3be60RRk3AJV8cY5oVkaqOlag3ISilX/bOu55z/b9VwXkPO0.QzSm', '培训管理员', 1, 'train@etms.com', '13800138004', 4, 7, 1);

-- =============================================
-- 初始化用户角色关联
-- =============================================
INSERT INTO sys_user_role (user_id, role_id) VALUES
(1, 1),
(2, 4),
(3, 4),
(4, 4),
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
(7, 'system:config', '系统配置', 1, 1, '/system/config', 'Tools', 'system/config/index', 6, 1, 1),
(8, 'system:dict', '字典管理', 1, 1, '/system/dict', 'Collection', 'system/dict/index', 7, 1, 1),
(9, 'system:log', '日志管理', 1, 1, '/system/log', 'Document', 'system/log/index', 8, 1, 1),
(10, 'system:notice', '通知公告', 1, 1, '/system/notice', 'Bell', 'system/notice/index', 9, 1, 1),
-- 培训管理
(20, 'training', '培训管理', 1, 0, '/training', 'Reading', NULL, 2, 1, 1),
(21, 'training:course', '课程管理', 1, 20, '/training/course', 'Notebook', 'training/course/index', 1, 1, 1),
(22, 'training:category', '课程分类', 1, 20, '/training/category', 'Files', 'training/category/index', 2, 1, 1),
(23, 'training:plan', '培训计划', 1, 20, '/training/plan', 'Calendar', 'training/plan/index', 3, 1, 1),
(24, 'training:progress', '学习进度', 1, 20, '/training/progress', 'DataLine', 'training/progress/index', 4, 1, 1),
-- 签到管理
(30, 'attendance', '签到管理', 1, 0, '/attendance', 'Clock', NULL, 3, 1, 1),
(31, 'attendance:record', '签到记录', 1, 30, '/attendance/record', 'List', 'attendance/record/index', 1, 1, 1),
(32, 'attendance:apply', '补签申请', 1, 30, '/attendance/apply', 'Edit', 'attendance/apply/index', 2, 1, 1),
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

-- 培训管理员权限
INSERT INTO sys_role_permission (role_id, permission_id) VALUES
(2, 20), (2, 21), (2, 22), (2, 23), (2, 24),
(2, 30), (2, 31), (2, 32),
(2, 40), (2, 41), (2, 42), (2, 43), (2, 44),
(2, 50), (2, 51), (2, 52);

-- 部门主管权限
INSERT INTO sys_role_permission (role_id, permission_id) VALUES
(3, 20), (3, 21), (3, 23), (3, 24),
(3, 30), (3, 31),
(3, 40), (3, 43), (3, 44),
(3, 50), (3, 51),
(3, 60), (3, 61), (3, 62), (3, 63), (3, 64);

-- 普通员工权限
INSERT INTO sys_role_permission (role_id, permission_id) VALUES
(4, 60), (4, 61), (4, 62), (4, 63), (4, 64);

-- =============================================
-- 初始化课程分类数据
-- =============================================
INSERT INTO training_category (id, parent_id, category_name, category_code, level, sort_order, status) VALUES
(1, 0, '技术类', 'TECH', 1, 1, 1),
(2, 0, '管理类', 'MANAGE', 1, 2, 1),
(3, 0, '通用类', 'GENERAL', 1, 3, 1),
(4, 1, '编程语言', 'TECH_LANG', 2, 1, 1),
(5, 1, '数据库', 'TECH_DB', 2, 2, 1),
(6, 1, '框架技术', 'TECH_FRAME', 2, 3, 1),
(7, 2, '领导力', 'MANAGE_LEADER', 2, 1, 1),
(8, 2, '团队管理', 'MANAGE_TEAM', 2, 2, 1),
(9, 3, '办公软件', 'GENERAL_OFFICE', 2, 1, 1),
(10, 3, '职业素养', 'GENERAL_CAREER', 2, 2, 1);

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

-- =============================================
-- 初始化数据字典
-- =============================================
INSERT INTO sys_dict (dict_type, dict_code, dict_label, dict_value, sort_order, status) VALUES
-- 用户状态
('user_status', '0', '禁用', '0', 1, 1),
('user_status', '1', '正常', '1', 2, 1),
('user_status', '2', '离职', '2', 3, 1),
('user_status', '3', '休假', '3', 4, 1),
-- 性别
('gender', '0', '未知', '0', 1, 1),
('gender', '1', '男', '1', 2, 1),
('gender', '2', '女', '2', 3, 1),
-- 课程难度
('course_difficulty', '1', '入门', '1', 1, 1),
('course_difficulty', '2', '初级', '2', 2, 1),
('course_difficulty', '3', '中级', '3', 3, 1),
('course_difficulty', '4', '高级', '4', 4, 1),
('course_difficulty', '5', '专家', '5', 5, 1),
-- 课程状态
('course_status', '0', '草稿', '0', 1, 1),
('course_status', '1', '待审核', '1', 2, 1),
('course_status', '2', '已上架', '2', 3, 1),
('course_status', '3', '已下架', '3', 4, 1),
('course_status', '4', '审核驳回', '4', 5, 1),
-- 培训计划状态
('plan_status', '0', '草稿', '0', 1, 1),
('plan_status', '1', '已发布', '1', 2, 1),
('plan_status', '2', '进行中', '2', 3, 1),
('plan_status', '3', '已结束', '3', 4, 1),
('plan_status', '4', '已归档', '4', 5, 1),
-- 签到状态
('attendance_status', '1', '正常', '1', 1, 1),
('attendance_status', '2', '迟到', '2', 2, 1),
('attendance_status', '3', '早退', '3', 3, 1),
('attendance_status', '4', '缺勤', '4', 4, 1),
('attendance_status', '5', '补签', '5', 5, 1),
-- 题目类型
('question_type', '1', '单选题', '1', 1, 1),
('question_type', '2', '多选题', '2', 2, 1),
('question_type', '3', '判断题', '3', 3, 1),
('question_type', '4', '填空题', '4', 4, 1),
('question_type', '5', '简答题', '5', 5, 1),
-- 考试状态
('exam_status', '0', '考试中', '0', 1, 1),
('exam_status', '1', '已提交', '1', 2, 1),
('exam_status', '2', '超时', '2', 3, 1),
('exam_status', '3', '已批阅', '3', 4, 1);

-- =============================================
-- 初始化系统配置
-- =============================================
INSERT INTO sys_config (config_name, config_key, config_value, config_type, is_editable, remark) VALUES
('密码最小长度', 'sys.password.min.length', '6', 1, 1, '密码最小长度'),
('密码最大长度', 'sys.password.max.length', '20', 1, 1, '密码最大长度'),
('密码是否需要特殊字符', 'sys.password.special.char', 'true', 1, 1, '密码是否需要特殊字符'),
('登录失败锁定次数', 'sys.login.lock.count', '5', 1, 1, '登录失败锁定次数'),
('会话超时时间(分钟)', 'sys.session.timeout', '30', 1, 1, '会话超时时间'),
('验证码过期时间(秒)', 'sys.captcha.expire', '60', 1, 1, '验证码过期时间'),
('文件上传最大大小(MB)', 'sys.file.max.size', '50', 1, 1, '文件上传最大大小'),
('允许上传的文件类型', 'sys.file.allow.types', 'jpg,png,pdf,doc,docx,xls,xlsx,ppt,pptx,mp4', 1, 1, '允许上传的文件类型'),
('培训预警提前天数', 'train.warning.days', '3', 2, 1, '培训预警提前天数'),
('考试切屏最大次数', 'exam.switch.max.count', '3', 2, 1, '考试切屏最大次数');

-- =============================================
-- 初始化通知公告
-- =============================================
INSERT INTO sys_notice (notice_title, notice_type, notice_content, target_type, priority, is_top, status, publish_time, create_by) VALUES
('系统上线通知', 1, '企业员工培训管理系统已正式上线，请各部门积极配合使用。如有问题请联系管理员。', 1, 2, 1, 1, NOW(), 1),
('新员工培训须知', 2, '新入职员工需在入职后一周内完成《办公软件使用培训》和《公司规章制度培训》。', 1, 1, 0, 1, NOW(), 5);
