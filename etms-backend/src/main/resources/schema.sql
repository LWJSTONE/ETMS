-- =============================================
-- 企业员工培训管理系统 数据库脚本
-- ETMS (Enterprise Training Management System)
-- 数据库: MySQL 8.0
-- 字符集: utf8mb4
-- =============================================

-- 删除已存在的同名数据库（避免重复创建导致的问题）
DROP DATABASE IF EXISTS etms;

-- 创建数据库
CREATE DATABASE etms DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE etms;

-- =============================================
-- 系统基础表
-- =============================================

-- 1. 部门表
DROP TABLE IF EXISTS sys_dept;
CREATE TABLE sys_dept (
    id BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '部门ID',
    parent_id BIGINT(20) DEFAULT 0 COMMENT '父部门ID',
    dept_name VARCHAR(50) NOT NULL COMMENT '部门名称',
    dept_code VARCHAR(50) DEFAULT NULL COMMENT '部门编码',
    leader_id BIGINT(20) DEFAULT NULL COMMENT '部门负责人ID',
    sort_order INT(11) DEFAULT 0 COMMENT '排序顺序',
    level INT(11) DEFAULT 1 COMMENT '部门层级',
    ancestors VARCHAR(200) DEFAULT '' COMMENT '祖级列表',
    status TINYINT(1) DEFAULT 1 COMMENT '状态(0禁用 1正常)',
    create_by BIGINT(20) DEFAULT NULL COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT(20) DEFAULT NULL COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_dept_code (dept_code),
    KEY idx_parent_id (parent_id),
    KEY idx_level (level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门表';

-- 2. 岗位表
DROP TABLE IF EXISTS sys_position;
CREATE TABLE sys_position (
    id BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '岗位ID',
    position_name VARCHAR(50) NOT NULL COMMENT '岗位名称',
    position_code VARCHAR(50) DEFAULT NULL COMMENT '岗位编码',
    position_level VARCHAR(20) DEFAULT '初级' COMMENT '职级',
    position_desc VARCHAR(500) DEFAULT NULL COMMENT '岗位职责描述',
    requirement VARCHAR(1000) DEFAULT NULL COMMENT '任职要求',
    dept_id BIGINT(20) DEFAULT NULL COMMENT '所属部门ID',
    sort_order INT(11) DEFAULT 0 COMMENT '排序顺序',
    status TINYINT(1) DEFAULT 1 COMMENT '状态(0禁用 1正常)',
    create_by BIGINT(20) DEFAULT NULL COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT(20) DEFAULT NULL COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_position_code (position_code),
    KEY idx_dept_id (dept_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='岗位表';

-- 3. 用户表
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码',
    real_name VARCHAR(50) NOT NULL COMMENT '真实姓名',
    gender TINYINT(1) DEFAULT 0 COMMENT '性别(0未知 1男 2女)',
    email VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    phone VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    avatar VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    dept_id BIGINT(20) DEFAULT NULL COMMENT '部门ID',
    position_id BIGINT(20) DEFAULT NULL COMMENT '岗位ID',
    status TINYINT(1) DEFAULT 1 COMMENT '状态(0禁用 1正常 2离职 3休假)',
    login_ip VARCHAR(50) DEFAULT NULL COMMENT '最后登录IP',
    login_time DATETIME DEFAULT NULL COMMENT '最后登录时间',
    pwd_expire_time DATETIME DEFAULT NULL COMMENT '密码过期时间',
    lock_time DATETIME DEFAULT NULL COMMENT '锁定时间',
    lock_count INT(11) DEFAULT 0 COMMENT '连续登录失败次数',
    create_by BIGINT(20) DEFAULT NULL COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT(20) DEFAULT NULL COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username),
    UNIQUE KEY uk_email (email),
    UNIQUE KEY uk_phone (phone),
    KEY idx_dept_id (dept_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 4. 角色表
DROP TABLE IF EXISTS sys_role;
CREATE TABLE sys_role (
    id BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '角色ID',
    role_code VARCHAR(50) NOT NULL COMMENT '角色编码',
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
    role_desc VARCHAR(200) DEFAULT NULL COMMENT '角色描述',
    data_scope TINYINT(1) DEFAULT 1 COMMENT '数据范围(1全部 2本部门 3本人)',
    sort_order INT(11) DEFAULT 0 COMMENT '排序顺序',
    status TINYINT(1) DEFAULT 1 COMMENT '状态(0禁用 1正常)',
    create_by BIGINT(20) DEFAULT NULL COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT(20) DEFAULT NULL COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 5. 权限表
DROP TABLE IF EXISTS sys_permission;
CREATE TABLE sys_permission (
    id BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '权限ID',
    perm_code VARCHAR(100) NOT NULL COMMENT '权限编码',
    perm_name VARCHAR(50) NOT NULL COMMENT '权限名称',
    perm_type TINYINT(1) NOT NULL COMMENT '权限类型(1菜单 2按钮 3接口)',
    parent_id BIGINT(20) DEFAULT 0 COMMENT '父权限ID',
    path VARCHAR(200) DEFAULT NULL COMMENT '路由路径',
    icon VARCHAR(50) DEFAULT NULL COMMENT '菜单图标',
    component VARCHAR(100) DEFAULT NULL COMMENT '前端组件路径',
    sort_order INT(11) DEFAULT 0 COMMENT '排序顺序',
    visible TINYINT(1) DEFAULT 1 COMMENT '是否可见(0隐藏 1显示)',
    status TINYINT(1) DEFAULT 1 COMMENT '状态(0禁用 1正常)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_perm_code (perm_code),
    KEY idx_parent_id (parent_id),
    KEY idx_perm_type (perm_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- 6. 用户角色关联表
DROP TABLE IF EXISTS sys_user_role;
CREATE TABLE sys_user_role (
    id BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
    user_id BIGINT(20) NOT NULL COMMENT '用户ID',
    role_id BIGINT(20) NOT NULL COMMENT '角色ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_role (user_id, role_id),
    KEY idx_user_id (user_id),
    KEY idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 7. 角色权限关联表
DROP TABLE IF EXISTS sys_role_permission;
CREATE TABLE sys_role_permission (
    id BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
    role_id BIGINT(20) NOT NULL COMMENT '角色ID',
    permission_id BIGINT(20) NOT NULL COMMENT '权限ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_perm (role_id, permission_id),
    KEY idx_role_id (role_id),
    KEY idx_permission_id (permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- =============================================
-- 培训业务表
-- =============================================

-- 8. 课程分类表
DROP TABLE IF EXISTS training_category;
CREATE TABLE training_category (
    id BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    parent_id BIGINT(20) DEFAULT 0 COMMENT '父分类ID',
    category_name VARCHAR(50) NOT NULL COMMENT '分类名称',
    category_code VARCHAR(50) DEFAULT NULL COMMENT '分类编码',
    level INT(11) DEFAULT 1 COMMENT '分类层级',
    sort_order INT(11) DEFAULT 0 COMMENT '排序顺序',
    icon VARCHAR(50) DEFAULT NULL COMMENT '分类图标',
    status TINYINT(1) DEFAULT 1 COMMENT '状态(0禁用 1正常)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_category_code (category_code),
    KEY idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程分类表';

-- 9. 课程表
DROP TABLE IF EXISTS training_course;
CREATE TABLE training_course (
    id BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '课程ID',
    course_name VARCHAR(100) NOT NULL COMMENT '课程名称',
    course_code VARCHAR(50) DEFAULT NULL COMMENT '课程编码',
    course_desc VARCHAR(1000) DEFAULT NULL COMMENT '课程描述',
    course_objective VARCHAR(500) DEFAULT NULL COMMENT '课程目标',
    category_id BIGINT(20) DEFAULT NULL COMMENT '分类ID',
    duration INT(11) NOT NULL COMMENT '课程时长(分钟)',
    difficulty TINYINT(1) DEFAULT 1 COMMENT '难度等级(1入门 2初级 3中级 4高级 5专家)',
    cover_image VARCHAR(255) DEFAULT NULL COMMENT '封面图片URL',
    video_url VARCHAR(500) DEFAULT NULL COMMENT '视频资源URL',
    document_url VARCHAR(500) DEFAULT NULL COMMENT '文档资源URL',
    ppt_url VARCHAR(500) DEFAULT NULL COMMENT 'PPT资源URL',
    tag_ids VARCHAR(200) DEFAULT NULL COMMENT '标签ID集合(JSON)',
    target_audience VARCHAR(200) DEFAULT NULL COMMENT '适用对象',
    prerequisite VARCHAR(500) DEFAULT NULL COMMENT '前置要求',
    status TINYINT(1) DEFAULT 0 COMMENT '状态(0草稿 1待审核 2已上架 3已下架 4审核驳回)',
    audit_remark VARCHAR(500) DEFAULT NULL COMMENT '审核意见',
    audit_by BIGINT(20) DEFAULT NULL COMMENT '审核人ID',
    audit_time DATETIME DEFAULT NULL COMMENT '审核时间',
    view_count INT(11) DEFAULT 0 COMMENT '浏览次数',
    collect_count INT(11) DEFAULT 0 COMMENT '收藏次数',
    rating_avg DECIMAL(3,2) DEFAULT 0.00 COMMENT '平均评分',
    rating_count INT(11) DEFAULT 0 COMMENT '评分人数',
    create_by BIGINT(20) DEFAULT NULL COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT(20) DEFAULT NULL COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_course_code (course_code),
    KEY idx_category_id (category_id),
    KEY idx_status (status),
    KEY idx_difficulty (difficulty)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程表';

-- 10. 课程资源表
DROP TABLE IF EXISTS training_course_resource;
CREATE TABLE training_course_resource (
    id BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '资源ID',
    course_id BIGINT(20) NOT NULL COMMENT '课程ID',
    resource_name VARCHAR(100) NOT NULL COMMENT '资源名称',
    resource_type TINYINT(1) NOT NULL COMMENT '资源类型(1视频 2文档 3图片 4其他)',
    resource_url VARCHAR(500) NOT NULL COMMENT '资源URL',
    file_size BIGINT(20) DEFAULT NULL COMMENT '文件大小(字节)',
    file_format VARCHAR(20) DEFAULT NULL COMMENT '文件格式',
    version VARCHAR(20) DEFAULT NULL COMMENT '版本号',
    is_preview TINYINT(1) DEFAULT 0 COMMENT '是否可预览',
    sort_order INT(11) DEFAULT 0 COMMENT '排序顺序',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_course_id (course_id),
    KEY idx_resource_type (resource_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程资源表';

-- 11. 课程评价表
DROP TABLE IF EXISTS training_course_rating;
CREATE TABLE training_course_rating (
    id BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '评价ID',
    course_id BIGINT(20) NOT NULL COMMENT '课程ID',
    user_id BIGINT(20) NOT NULL COMMENT '用户ID',
    rating TINYINT(1) NOT NULL COMMENT '评分(1-5星)',
    content VARCHAR(500) DEFAULT NULL COMMENT '评价内容',
    reply_content VARCHAR(500) DEFAULT NULL COMMENT '回复内容',
    reply_by BIGINT(20) DEFAULT NULL COMMENT '回复人ID',
    reply_time DATETIME DEFAULT NULL COMMENT '回复时间',
    is_anonymous TINYINT(1) DEFAULT 0 COMMENT '是否匿名',
    status TINYINT(1) DEFAULT 1 COMMENT '状态(0隐藏 1显示)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_course_user (course_id, user_id),
    KEY idx_course_id (course_id),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程评价表';

-- 12. 培训计划表
DROP TABLE IF EXISTS training_plan;
CREATE TABLE training_plan (
    id BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '计划ID',
    plan_name VARCHAR(100) NOT NULL COMMENT '计划名称',
    plan_code VARCHAR(50) DEFAULT NULL COMMENT '计划编码',
    plan_desc VARCHAR(500) DEFAULT NULL COMMENT '计划描述',
    plan_objective VARCHAR(500) DEFAULT NULL COMMENT '培训目标',
    course_id BIGINT(20) DEFAULT NULL COMMENT '课程ID',
    plan_type TINYINT(1) DEFAULT 1 COMMENT '计划类型(1必修 2选修)',
    start_date DATE NOT NULL COMMENT '开始日期',
    end_date DATE NOT NULL COMMENT '结束日期',
    sign_start_time DATETIME DEFAULT NULL COMMENT '签到开始时间',
    sign_end_time DATETIME DEFAULT NULL COMMENT '签到结束时间',
    target_type TINYINT(1) NOT NULL COMMENT '目标类型(1部门 2岗位 3个人)',
    target_dept_ids VARCHAR(500) DEFAULT NULL COMMENT '目标部门ID集合(JSON)',
    target_position_ids VARCHAR(500) DEFAULT NULL COMMENT '目标岗位ID集合(JSON)',
    target_user_ids VARCHAR(500) DEFAULT NULL COMMENT '目标用户ID集合(JSON)',
    min_study_time INT(11) DEFAULT NULL COMMENT '最低学习时长(分钟)',
    min_progress INT(11) DEFAULT NULL COMMENT '最低完成进度(%)',
    need_exam TINYINT(1) DEFAULT 0 COMMENT '是否需要考试',
    pass_score INT(11) DEFAULT 60 COMMENT '及格分数',
    max_retake INT(11) DEFAULT 3 COMMENT '最大补考次数',
    status TINYINT(1) DEFAULT 0 COMMENT '状态(0草稿 1已发布 2进行中 3已结束 4已归档)',
    create_by BIGINT(20) DEFAULT NULL COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT(20) DEFAULT NULL COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_plan_code (plan_code),
    KEY idx_course_id (course_id),
    KEY idx_status (status),
    KEY idx_start_date (start_date),
    KEY idx_end_date (end_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='培训计划表';

-- 13. 学习进度表
DROP TABLE IF EXISTS learning_progress;
CREATE TABLE learning_progress (
    id BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '进度ID',
    user_id BIGINT(20) NOT NULL COMMENT '用户ID',
    plan_id BIGINT(20) NOT NULL COMMENT '计划ID',
    course_id BIGINT(20) NOT NULL COMMENT '课程ID',
    progress INT(11) DEFAULT 0 COMMENT '完成进度(0-100)',
    study_time INT(11) DEFAULT 0 COMMENT '学习时长(分钟)',
    status TINYINT(1) DEFAULT 0 COMMENT '状态(0未开始 1学习中 2已完成 3已超时)',
    last_study_time DATETIME DEFAULT NULL COMMENT '最后学习时间',
    complete_time DATETIME DEFAULT NULL COMMENT '完成时间',
    is_late TINYINT(1) DEFAULT 0 COMMENT '是否进度滞后',
    warning_sent TINYINT(1) DEFAULT 0 COMMENT '预警通知是否发送',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_plan (user_id, plan_id),
    KEY idx_user_id (user_id),
    KEY idx_plan_id (plan_id),
    KEY idx_course_id (course_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习进度表';

-- =============================================
-- 签到考核表
-- =============================================

-- 14. 签到记录表
DROP TABLE IF EXISTS attendance_record;
CREATE TABLE attendance_record (
    id BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '签到ID',
    user_id BIGINT(20) NOT NULL COMMENT '用户ID',
    plan_id BIGINT(20) NOT NULL COMMENT '计划ID',
    sign_time DATETIME NOT NULL COMMENT '签到时间',
    sign_type TINYINT(1) NOT NULL COMMENT '签到类型(1二维码 2GPS定位 3人脸识别)',
    sign_category TINYINT(1) DEFAULT 1 COMMENT '签到类别(1签到 2签退)',
    location VARCHAR(100) DEFAULT NULL COMMENT '签到位置',
    ip_address VARCHAR(50) DEFAULT NULL COMMENT '签到IP地址',
    device_info VARCHAR(100) DEFAULT NULL COMMENT '设备信息',
    status TINYINT(1) DEFAULT 1 COMMENT '状态(1正常 2迟到 3早退 4缺勤 5补签)',
    late_minutes INT(11) DEFAULT 0 COMMENT '迟到分钟数',
    early_minutes INT(11) DEFAULT 0 COMMENT '早退分钟数',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    reason VARCHAR(500) DEFAULT NULL COMMENT '补签原因',
    audit_remark VARCHAR(500) DEFAULT NULL COMMENT '审核备注',
    audit_status TINYINT(1) DEFAULT 0 COMMENT '审核状态(0待审核 1通过 2驳回)',
    audit_by BIGINT(20) DEFAULT NULL COMMENT '审核人ID',
    audit_time DATETIME DEFAULT NULL COMMENT '审核时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_plan_id (plan_id),
    KEY idx_sign_time (sign_time),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='签到记录表';

-- 15. 补签申请表
DROP TABLE IF EXISTS attendance_apply;
CREATE TABLE attendance_apply (
    id BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '申请ID',
    user_id BIGINT(20) NOT NULL COMMENT '用户ID',
    plan_id BIGINT(20) NOT NULL COMMENT '计划ID',
    apply_reason VARCHAR(500) NOT NULL COMMENT '申请原因',
    apply_time DATETIME NOT NULL COMMENT '申请时间',
    sign_date DATE NOT NULL COMMENT '需补签日期',
    proof_image VARCHAR(500) DEFAULT NULL COMMENT '证明材料图片URL',
    audit_status TINYINT(1) DEFAULT 0 COMMENT '审核状态(0待审核 1通过 2驳回)',
    audit_remark VARCHAR(500) DEFAULT NULL COMMENT '审核意见',
    audit_by BIGINT(20) DEFAULT NULL COMMENT '审核人ID',
    audit_time DATETIME DEFAULT NULL COMMENT '审核时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_plan_id (plan_id),
    KEY idx_audit_status (audit_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='补签申请表';

-- 16. 题库表
DROP TABLE IF EXISTS exam_question;
CREATE TABLE exam_question (
    id BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '题目ID',
    question_code VARCHAR(50) DEFAULT NULL COMMENT '题目编码',
    question_type TINYINT(1) NOT NULL COMMENT '题目类型(1单选 2多选 3判断 4填空 5简答)',
    question_content TEXT NOT NULL COMMENT '题目内容',
    option_a VARCHAR(500) DEFAULT NULL COMMENT '选项A',
    option_b VARCHAR(500) DEFAULT NULL COMMENT '选项B',
    option_c VARCHAR(500) DEFAULT NULL COMMENT '选项C',
    option_d VARCHAR(500) DEFAULT NULL COMMENT '选项D',
    option_e VARCHAR(500) DEFAULT NULL COMMENT '选项E',
    answer VARCHAR(500) NOT NULL COMMENT '正确答案',
    answer_analysis VARCHAR(1000) DEFAULT NULL COMMENT '答案解析',
    difficulty TINYINT(1) DEFAULT 2 COMMENT '难度(1简单 2中等 3困难)',
    score INT(11) DEFAULT 1 COMMENT '题目分值',
    category_id BIGINT(20) DEFAULT NULL COMMENT '题目分类ID',
    tag_ids VARCHAR(200) DEFAULT NULL COMMENT '标签ID集合',
    course_id BIGINT(20) DEFAULT NULL COMMENT '关联课程ID',
    status TINYINT(1) DEFAULT 1 COMMENT '状态(0禁用 1启用)',
    create_by BIGINT(20) DEFAULT NULL COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT(20) DEFAULT NULL COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_question_code (question_code),
    KEY idx_question_type (question_type),
    KEY idx_difficulty (difficulty),
    KEY idx_category_id (category_id),
    KEY idx_course_id (course_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题库表';

-- 17. 试卷表
DROP TABLE IF EXISTS exam_paper;
CREATE TABLE exam_paper (
    id BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '试卷ID',
    paper_name VARCHAR(100) NOT NULL COMMENT '试卷名称',
    paper_code VARCHAR(50) DEFAULT NULL COMMENT '试卷编码',
    plan_id BIGINT(20) DEFAULT NULL COMMENT '关联培训计划ID',
    course_id BIGINT(20) DEFAULT NULL COMMENT '关联课程ID',
    paper_type TINYINT(1) DEFAULT 1 COMMENT '试卷类型(1手动组卷 2随机组卷)',
    total_score INT(11) NOT NULL COMMENT '总分',
    pass_score INT(11) DEFAULT 60 COMMENT '及格分数',
    exam_duration INT(11) NOT NULL COMMENT '考试时长(分钟)',
    start_time DATETIME DEFAULT NULL COMMENT '考试开始时间',
    end_time DATETIME DEFAULT NULL COMMENT '考试结束时间',
    question_count INT(11) NOT NULL COMMENT '题目数量',
    question_config TEXT DEFAULT NULL COMMENT '题目配置(JSON)',
    shuffle_option TINYINT(1) DEFAULT 1 COMMENT '是否打乱选项',
    shuffle_question TINYINT(1) DEFAULT 1 COMMENT '是否打乱题目',
    anti_cheat TINYINT(1) DEFAULT 1 COMMENT '防作弊开关',
    max_switch INT(11) DEFAULT 3 COMMENT '最大切屏次数',
    status TINYINT(1) DEFAULT 0 COMMENT '状态(0草稿 1已发布 2已使用 3已停用)',
    create_by BIGINT(20) DEFAULT NULL COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT(20) DEFAULT NULL COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_paper_code (paper_code),
    KEY idx_plan_id (plan_id),
    KEY idx_course_id (course_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='试卷表';

-- 18. 试卷题目关联表
DROP TABLE IF EXISTS exam_paper_question;
CREATE TABLE exam_paper_question (
    id BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
    paper_id BIGINT(20) NOT NULL COMMENT '试卷ID',
    question_id BIGINT(20) NOT NULL COMMENT '题目ID',
    question_score INT(11) NOT NULL COMMENT '题目分值',
    sort_order INT(11) DEFAULT 0 COMMENT '题目顺序',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_paper_id (paper_id),
    KEY idx_question_id (question_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='试卷题目关联表';

-- 19. 考试记录表
DROP TABLE IF EXISTS exam_record;
CREATE TABLE exam_record (
    id BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    user_id BIGINT(20) NOT NULL COMMENT '用户ID',
    plan_id BIGINT(20) NOT NULL COMMENT '计划ID',
    paper_id BIGINT(20) NOT NULL COMMENT '试卷ID',
    exam_start_time DATETIME DEFAULT NULL COMMENT '考试开始时间',
    exam_end_time DATETIME DEFAULT NULL COMMENT '考试结束时间',
    submit_time DATETIME DEFAULT NULL COMMENT '提交时间',
    duration_used INT(11) DEFAULT NULL COMMENT '实际用时(分钟)',
    switch_count INT(11) DEFAULT 0 COMMENT '切屏次数',
    status TINYINT(1) DEFAULT 0 COMMENT '状态(0考试中 1已提交 2超时 3已批阅)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_plan_id (plan_id),
    KEY idx_paper_id (paper_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考试记录表';

-- 20. 考核成绩表
DROP TABLE IF EXISTS exam_result;
CREATE TABLE exam_result (
    id BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '成绩ID',
    user_id BIGINT(20) NOT NULL COMMENT '用户ID',
    plan_id BIGINT(20) NOT NULL COMMENT '计划ID',
    exam_record_id BIGINT(20) DEFAULT NULL COMMENT '考试记录ID',
    exam_score INT(11) NOT NULL COMMENT '考试分数',
    total_score INT(11) NOT NULL COMMENT '总分',
    pass_status TINYINT(1) NOT NULL COMMENT '通过状态(0未通过 1通过)',
    objective_score INT(11) DEFAULT NULL COMMENT '客观题得分',
    subjective_score INT(11) DEFAULT NULL COMMENT '主观题得分',
    answer_detail TEXT DEFAULT NULL COMMENT '答题详情(JSON)',
    review_status TINYINT(1) DEFAULT 0 COMMENT '复核状态(0无 1申请中 2已完成)',
    review_remark VARCHAR(500) DEFAULT NULL COMMENT '复核意见',
    retake_count INT(11) DEFAULT 0 COMMENT '补考次数',
    exam_time DATETIME NOT NULL COMMENT '考试时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_plan_id (plan_id),
    KEY idx_pass_status (pass_status),
    KEY idx_exam_time (exam_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考核成绩表';

-- 21. 补训记录表
DROP TABLE IF EXISTS retraining_record;
CREATE TABLE retraining_record (
    id BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '补训ID',
    user_id BIGINT(20) NOT NULL COMMENT '用户ID',
    original_plan_id BIGINT(20) NOT NULL COMMENT '原计划ID',
    retrain_plan_id BIGINT(20) DEFAULT NULL COMMENT '补训计划ID',
    reason VARCHAR(500) NOT NULL COMMENT '补训原因',
    trigger_type TINYINT(1) NOT NULL COMMENT '触发类型(1考试不合格 2进度未完成 3手动指定)',
    status TINYINT(1) DEFAULT 0 COMMENT '状态(0待补训 1补训中 2已完成 3已超时)',
    deadline DATETIME DEFAULT NULL COMMENT '补训截止时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    complete_time DATETIME DEFAULT NULL COMMENT '完成时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_original_plan_id (original_plan_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='补训记录表';

-- =============================================
-- 系统管理表
-- =============================================

-- 22. 操作日志表
DROP TABLE IF EXISTS sys_operation_log;
CREATE TABLE sys_operation_log (
    id BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    user_id BIGINT(20) DEFAULT NULL COMMENT '操作用户ID',
    username VARCHAR(50) DEFAULT NULL COMMENT '操作用户名',
    module VARCHAR(50) DEFAULT NULL COMMENT '操作模块',
    operation_type VARCHAR(50) DEFAULT NULL COMMENT '操作类型',
    operation_desc VARCHAR(500) DEFAULT NULL COMMENT '操作描述',
    request_method VARCHAR(10) DEFAULT NULL COMMENT '请求方法',
    request_url VARCHAR(200) DEFAULT NULL COMMENT '请求URL',
    request_params TEXT DEFAULT NULL COMMENT '请求参数',
    ip_address VARCHAR(50) DEFAULT NULL COMMENT '操作IP',
    ip_location VARCHAR(100) DEFAULT NULL COMMENT 'IP归属地',
    browser VARCHAR(100) DEFAULT NULL COMMENT '浏览器信息',
    os VARCHAR(50) DEFAULT NULL COMMENT '操作系统',
    status TINYINT(1) DEFAULT 1 COMMENT '操作状态(0失败 1成功)',
    error_msg VARCHAR(1000) DEFAULT NULL COMMENT '错误信息',
    cost_time INT(11) DEFAULT NULL COMMENT '执行耗时(毫秒)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_module (module),
    KEY idx_operation_type (operation_type),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- 23. 登录日志表
DROP TABLE IF EXISTS sys_login_log;
CREATE TABLE sys_login_log (
    id BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    user_id BIGINT(20) DEFAULT NULL COMMENT '用户ID',
    username VARCHAR(50) DEFAULT NULL COMMENT '用户名',
    login_type TINYINT(1) DEFAULT 1 COMMENT '登录类型(1账号密码 2短信 3邮箱)',
    status TINYINT(1) NOT NULL COMMENT '登录状态(0失败 1成功)',
    ip_address VARCHAR(50) DEFAULT NULL COMMENT '登录IP',
    ip_location VARCHAR(100) DEFAULT NULL COMMENT 'IP归属地',
    browser VARCHAR(100) DEFAULT NULL COMMENT '浏览器信息',
    os VARCHAR(50) DEFAULT NULL COMMENT '操作系统',
    device_info VARCHAR(100) DEFAULT NULL COMMENT '设备信息',
    msg VARCHAR(500) DEFAULT NULL COMMENT '登录消息',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_status (status),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='登录日志表';

-- 24. 系统配置表
DROP TABLE IF EXISTS sys_config;
CREATE TABLE sys_config (
    id BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '配置ID',
    config_name VARCHAR(100) NOT NULL COMMENT '配置名称',
    config_key VARCHAR(100) NOT NULL COMMENT '配置键',
    config_value VARCHAR(500) DEFAULT NULL COMMENT '配置值',
    config_type TINYINT(1) DEFAULT 1 COMMENT '配置类型(1系统 2业务)',
    is_editable TINYINT(1) DEFAULT 1 COMMENT '是否可编辑',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- 25. 数据字典表
DROP TABLE IF EXISTS sys_dict;
CREATE TABLE sys_dict (
    id BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '字典ID',
    dict_type VARCHAR(50) NOT NULL COMMENT '字典类型',
    dict_code VARCHAR(50) NOT NULL COMMENT '字典编码',
    dict_label VARCHAR(100) NOT NULL COMMENT '字典标签',
    dict_value VARCHAR(100) NOT NULL COMMENT '字典值',
    sort_order INT(11) DEFAULT 0 COMMENT '排序顺序',
    status TINYINT(1) DEFAULT 1 COMMENT '状态(0禁用 1正常)',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_dict_type (dict_type),
    UNIQUE KEY uk_dict_type_code (dict_type, dict_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据字典表';

-- 26. 通知公告表
DROP TABLE IF EXISTS sys_notice;
CREATE TABLE sys_notice (
    id BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '公告ID',
    notice_title VARCHAR(100) NOT NULL COMMENT '公告标题',
    notice_type TINYINT(1) NOT NULL COMMENT '公告类型(1通知 2公告 3消息)',
    notice_content TEXT NOT NULL COMMENT '公告内容',
    target_type TINYINT(1) DEFAULT 1 COMMENT '目标类型(1全部 2部门 3角色 4个人)',
    target_ids VARCHAR(500) DEFAULT NULL COMMENT '目标ID集合(JSON)',
    priority TINYINT(1) DEFAULT 1 COMMENT '优先级(1普通 2重要 3紧急)',
    is_top TINYINT(1) DEFAULT 0 COMMENT '是否置顶',
    status TINYINT(1) DEFAULT 1 COMMENT '状态(0草稿 1已发布 2已撤回)',
    publish_time DATETIME DEFAULT NULL COMMENT '发布时间',
    expire_time DATETIME DEFAULT NULL COMMENT '过期时间',
    create_by BIGINT(20) DEFAULT NULL COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_notice_type (notice_type),
    KEY idx_status (status),
    KEY idx_publish_time (publish_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知公告表';

-- 27. 通知记录表
DROP TABLE IF EXISTS sys_notice_record;
CREATE TABLE sys_notice_record (
    id BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    notice_id BIGINT(20) NOT NULL COMMENT '公告ID',
    user_id BIGINT(20) NOT NULL COMMENT '用户ID',
    is_read TINYINT(1) DEFAULT 0 COMMENT '是否已读(0未读 1已读)',
    read_time DATETIME DEFAULT NULL COMMENT '阅读时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_notice_id (notice_id),
    KEY idx_user_id (user_id),
    KEY idx_is_read (is_read)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知记录表';

-- 28. 文件管理表
DROP TABLE IF EXISTS sys_file;
CREATE TABLE sys_file (
    id BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '文件ID',
    file_name VARCHAR(100) NOT NULL COMMENT '文件名称',
    file_original_name VARCHAR(200) DEFAULT NULL COMMENT '原始文件名',
    file_path VARCHAR(500) NOT NULL COMMENT '文件路径',
    file_url VARCHAR(500) DEFAULT NULL COMMENT '文件访问URL',
    file_size BIGINT(20) DEFAULT NULL COMMENT '文件大小(字节)',
    file_type VARCHAR(50) DEFAULT NULL COMMENT '文件类型',
    file_extension VARCHAR(20) DEFAULT NULL COMMENT '文件扩展名',
    upload_user_id BIGINT(20) DEFAULT NULL COMMENT '上传用户ID',
    module VARCHAR(50) DEFAULT NULL COMMENT '所属模块',
    status TINYINT(1) DEFAULT 1 COMMENT '状态(0禁用 1正常)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_upload_user_id (upload_user_id),
    KEY idx_module (module),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件管理表';

-- 29. 定时任务表
DROP TABLE IF EXISTS sys_job;
CREATE TABLE sys_job (
    id BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '任务ID',
    job_name VARCHAR(100) NOT NULL COMMENT '任务名称',
    job_group VARCHAR(50) DEFAULT 'DEFAULT' COMMENT '任务组名',
    job_type TINYINT(1) DEFAULT 1 COMMENT '任务类型(1同步 2异步)',
    cron_expression VARCHAR(50) DEFAULT NULL COMMENT 'cron表达式',
    invoke_target VARCHAR(500) NOT NULL COMMENT '调用目标字符串',
    method_name VARCHAR(100) DEFAULT NULL COMMENT '方法名称',
    method_params VARCHAR(500) DEFAULT NULL COMMENT '方法参数',
    status TINYINT(1) DEFAULT 1 COMMENT '状态(0暂停 1正常)',
    error_policy TINYINT(1) DEFAULT 1 COMMENT '错误策略(1继续 2暂停)',
    max_retry INT(11) DEFAULT 3 COMMENT '最大重试次数',
    last_execute_time DATETIME DEFAULT NULL COMMENT '最后执行时间',
    next_execute_time DATETIME DEFAULT NULL COMMENT '下次执行时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_job_group (job_group),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='定时任务表';

-- 30. 定时任务日志表
DROP TABLE IF EXISTS sys_job_log;
CREATE TABLE sys_job_log (
    id BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    job_id BIGINT(20) NOT NULL COMMENT '任务ID',
    job_name VARCHAR(100) DEFAULT NULL COMMENT '任务名称',
    job_group VARCHAR(50) DEFAULT NULL COMMENT '任务组名',
    execute_time DATETIME DEFAULT NULL COMMENT '执行时间',
    duration INT(11) DEFAULT NULL COMMENT '执行耗时(毫秒)',
    status TINYINT(1) NOT NULL COMMENT '执行状态(0失败 1成功)',
    error_msg TEXT DEFAULT NULL COMMENT '错误信息',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_job_id (job_id),
    KEY idx_status (status),
    KEY idx_execute_time (execute_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='定时任务日志表';
