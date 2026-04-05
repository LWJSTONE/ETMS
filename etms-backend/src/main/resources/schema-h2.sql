-- H2 Database Schema for ETMS

-- 1. 部门表
CREATE TABLE IF NOT EXISTS sys_dept (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    parent_id BIGINT DEFAULT 0,
    dept_name VARCHAR(50) NOT NULL,
    dept_code VARCHAR(50) DEFAULT NULL,
    leader_id BIGINT DEFAULT NULL,
    sort_order INT DEFAULT 0,
    level INT DEFAULT 1,
    ancestors VARCHAR(200) DEFAULT '',
    status TINYINT DEFAULT 1,
    create_by BIGINT DEFAULT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_by BIGINT DEFAULT NULL,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    remark VARCHAR(500) DEFAULT NULL,
    deleted TINYINT DEFAULT 0
);

-- 2. 岗位表
CREATE TABLE IF NOT EXISTS sys_position (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    position_name VARCHAR(50) NOT NULL,
    position_code VARCHAR(50) DEFAULT NULL,
    position_level VARCHAR(20) DEFAULT '初级',
    position_desc VARCHAR(500) DEFAULT NULL,
    requirement VARCHAR(1000) DEFAULT NULL,
    dept_id BIGINT DEFAULT NULL,
    sort_order INT DEFAULT 0,
    status TINYINT DEFAULT 1,
    create_by BIGINT DEFAULT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_by BIGINT DEFAULT NULL,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
);

-- 3. 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    real_name VARCHAR(50) NOT NULL,
    gender TINYINT DEFAULT 0,
    email VARCHAR(100) DEFAULT NULL UNIQUE,
    phone VARCHAR(20) DEFAULT NULL UNIQUE,
    avatar VARCHAR(255) DEFAULT NULL,
    dept_id BIGINT DEFAULT NULL,
    position_id BIGINT DEFAULT NULL,
    status TINYINT DEFAULT 1,
    login_ip VARCHAR(64) DEFAULT NULL,
    login_time TIMESTAMP DEFAULT NULL,
    pwd_expire_time TIMESTAMP DEFAULT NULL,
    lock_time TIMESTAMP DEFAULT NULL,
    lock_count INT DEFAULT 0,
    create_by BIGINT DEFAULT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_by BIGINT DEFAULT NULL,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    remark VARCHAR(500) DEFAULT NULL,
    deleted TINYINT DEFAULT 0
);

-- 4. 角色表
CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_code VARCHAR(50) NOT NULL UNIQUE,
    role_name VARCHAR(50) NOT NULL,
    role_desc VARCHAR(200) DEFAULT NULL,
    data_scope TINYINT DEFAULT 1,
    sort_order INT DEFAULT 0,
    status TINYINT DEFAULT 1,
    create_by BIGINT DEFAULT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_by BIGINT DEFAULT NULL,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    remark VARCHAR(500) DEFAULT NULL,
    deleted TINYINT DEFAULT 0
);

-- 5. 权限表
CREATE TABLE IF NOT EXISTS sys_permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    perm_code VARCHAR(100) NOT NULL UNIQUE,
    perm_name VARCHAR(50) NOT NULL,
    perm_type TINYINT NOT NULL,
    parent_id BIGINT DEFAULT 0,
    path VARCHAR(200) DEFAULT NULL,
    icon VARCHAR(50) DEFAULT NULL,
    component VARCHAR(100) DEFAULT NULL,
    sort_order INT DEFAULT 0,
    visible TINYINT DEFAULT 1,
    status TINYINT DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
);

-- 6. 用户角色关联表
CREATE TABLE IF NOT EXISTS sys_user_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 7. 角色权限关联表
CREATE TABLE IF NOT EXISTS sys_role_permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 9. 课程表
CREATE TABLE IF NOT EXISTS training_course (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_name VARCHAR(100) NOT NULL,
    course_code VARCHAR(50) DEFAULT NULL,
    course_type TINYINT DEFAULT 1,
    course_desc VARCHAR(1000) DEFAULT NULL,
    course_objective VARCHAR(500) DEFAULT NULL,
    category_id BIGINT DEFAULT NULL,
    duration INT NOT NULL,
    credit INT DEFAULT 0,
    difficulty TINYINT DEFAULT 1,
    cover_image VARCHAR(255) DEFAULT NULL,
    video_url VARCHAR(500) DEFAULT NULL,
    document_url VARCHAR(500) DEFAULT NULL,
    ppt_url VARCHAR(500) DEFAULT NULL,
    tag_ids VARCHAR(200) DEFAULT NULL,
    target_audience VARCHAR(200) DEFAULT NULL,
    prerequisite VARCHAR(500) DEFAULT NULL,
    status TINYINT DEFAULT 0,
    audit_remark VARCHAR(500) DEFAULT NULL,
    audit_by BIGINT DEFAULT NULL,
    audit_time TIMESTAMP DEFAULT NULL,
    view_count INT DEFAULT 0,
    collect_count INT DEFAULT 0,
    rating_avg DECIMAL(3,2) DEFAULT 0.00,
    rating_count INT DEFAULT 0,
    create_by BIGINT DEFAULT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_by BIGINT DEFAULT NULL,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    remark VARCHAR(500) DEFAULT NULL,
    deleted TINYINT DEFAULT 0
);

-- 10. 培训计划表
CREATE TABLE IF NOT EXISTS training_plan (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    plan_name VARCHAR(100) NOT NULL,
    plan_code VARCHAR(50) DEFAULT NULL,
    plan_desc VARCHAR(500) DEFAULT NULL,
    plan_objective VARCHAR(500) DEFAULT NULL,
    course_id BIGINT DEFAULT NULL,
    plan_type TINYINT DEFAULT 1,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    sign_start_time TIMESTAMP DEFAULT NULL,
    sign_end_time TIMESTAMP DEFAULT NULL,
    target_type TINYINT NOT NULL,
    target_dept_ids VARCHAR(500) DEFAULT NULL,
    target_position_ids VARCHAR(500) DEFAULT NULL,
    target_user_ids VARCHAR(500) DEFAULT NULL,
    min_study_time INT DEFAULT NULL,
    min_progress INT DEFAULT NULL,
    need_exam TINYINT DEFAULT 0,
    pass_score INT DEFAULT 60,
    max_retake INT DEFAULT 3,
    status TINYINT DEFAULT 0,
    create_by BIGINT DEFAULT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_by BIGINT DEFAULT NULL,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    remark VARCHAR(500) DEFAULT NULL,
    deleted TINYINT DEFAULT 0
);

-- 11. 培训计划课程关联表
CREATE TABLE IF NOT EXISTS etms_plan_course (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    plan_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    sort_order INT DEFAULT 0,
    required TINYINT DEFAULT 1,
    create_by BIGINT DEFAULT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_by BIGINT DEFAULT NULL,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    remark VARCHAR(500) DEFAULT NULL
);

-- 12. 用户培训计划关联表
CREATE TABLE IF NOT EXISTS etms_user_plan (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    plan_id BIGINT NOT NULL,
    status TINYINT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 13. 学习进度表
CREATE TABLE IF NOT EXISTS learning_progress (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    plan_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    progress INT DEFAULT 0,
    study_time INT DEFAULT 0,
    status TINYINT DEFAULT 0,
    last_study_time TIMESTAMP DEFAULT NULL,
    complete_time TIMESTAMP DEFAULT NULL,
    is_late TINYINT DEFAULT 0,
    warning_sent TINYINT DEFAULT 0,
    create_by BIGINT DEFAULT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_by BIGINT DEFAULT NULL,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    remark VARCHAR(500) DEFAULT NULL,
    deleted TINYINT DEFAULT 0
);

-- 16. 题库表
CREATE TABLE IF NOT EXISTS exam_question (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    question_code VARCHAR(50) DEFAULT NULL,
    question_type TINYINT NOT NULL,
    question_content CLOB NOT NULL,
    option_a VARCHAR(500) DEFAULT NULL,
    option_b VARCHAR(500) DEFAULT NULL,
    option_c VARCHAR(500) DEFAULT NULL,
    option_d VARCHAR(500) DEFAULT NULL,
    option_e VARCHAR(500) DEFAULT NULL,
    answer VARCHAR(500) NOT NULL,
    answer_analysis VARCHAR(1000) DEFAULT NULL,
    difficulty TINYINT DEFAULT 2,
    score INT DEFAULT 1,
    category_id BIGINT DEFAULT NULL,
    tag_ids VARCHAR(200) DEFAULT NULL,
    course_id BIGINT DEFAULT NULL,
    status TINYINT DEFAULT 1,
    create_by BIGINT DEFAULT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_by BIGINT DEFAULT NULL,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    remark VARCHAR(500) DEFAULT NULL,
    deleted TINYINT DEFAULT 0
);

-- 17. 试卷表
CREATE TABLE IF NOT EXISTS exam_paper (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    paper_name VARCHAR(100) NOT NULL,
    paper_code VARCHAR(50) DEFAULT NULL,
    plan_id BIGINT DEFAULT NULL,
    course_id BIGINT DEFAULT NULL,
    paper_type TINYINT DEFAULT 1,
    total_score INT NOT NULL,
    pass_score INT DEFAULT 60,
    exam_duration INT NOT NULL,
    start_time TIMESTAMP DEFAULT NULL,
    end_time TIMESTAMP DEFAULT NULL,
    question_count INT NOT NULL,
    question_config CLOB DEFAULT NULL,
    shuffle_option TINYINT DEFAULT 1,
    shuffle_question TINYINT DEFAULT 1,
    anti_cheat TINYINT DEFAULT 1,
    max_switch INT DEFAULT 3,
    status TINYINT DEFAULT 0,
    description VARCHAR(500) DEFAULT NULL,
    create_by BIGINT DEFAULT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_by BIGINT DEFAULT NULL,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    remark VARCHAR(500) DEFAULT NULL,
    deleted TINYINT DEFAULT 0
);

-- 18. 试卷题目关联表
CREATE TABLE IF NOT EXISTS exam_paper_question (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    paper_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    question_score INT NOT NULL,
    sort_order INT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 19. 考试记录表
CREATE TABLE IF NOT EXISTS exam_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    plan_id BIGINT DEFAULT NULL,
    paper_id BIGINT DEFAULT NULL,
    exam_start_time TIMESTAMP DEFAULT NULL,
    exam_end_time TIMESTAMP DEFAULT NULL,
    submit_time TIMESTAMP DEFAULT NULL,
    duration_used INT DEFAULT NULL,
    switch_count INT DEFAULT 0,
    total_score INT DEFAULT NULL,
    user_score INT DEFAULT NULL,
    passed TINYINT DEFAULT 0,
    objective_score INT DEFAULT NULL,
    subjective_score INT DEFAULT NULL,
    answer_detail CLOB DEFAULT NULL,
    retake_count INT DEFAULT 0,
    status TINYINT DEFAULT 0,
    create_by BIGINT DEFAULT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_by BIGINT DEFAULT NULL,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    remark VARCHAR(500) DEFAULT NULL,
    deleted TINYINT DEFAULT 0
);

-- 20. 考核成绩表
CREATE TABLE IF NOT EXISTS exam_result (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    plan_id BIGINT NOT NULL,
    exam_record_id BIGINT DEFAULT NULL,
    exam_score INT NOT NULL,
    total_score INT NOT NULL,
    pass_status TINYINT NOT NULL,
    objective_score INT DEFAULT NULL,
    subjective_score INT DEFAULT NULL,
    answer_detail CLOB DEFAULT NULL,
    review_status TINYINT DEFAULT 0,
    review_remark VARCHAR(500) DEFAULT NULL,
    retake_count INT DEFAULT 0,
    exam_time TIMESTAMP NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
);

-- 21. 操作日志表
CREATE TABLE IF NOT EXISTS sys_operation_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT DEFAULT NULL,
    username VARCHAR(50) DEFAULT NULL,
    module VARCHAR(50) DEFAULT NULL,
    operation_type VARCHAR(50) DEFAULT NULL,
    operation_desc VARCHAR(500) DEFAULT NULL,
    request_method VARCHAR(10) DEFAULT NULL,
    request_url VARCHAR(200) DEFAULT NULL,
    request_params CLOB DEFAULT NULL,
    ip_address VARCHAR(64) DEFAULT NULL,
    ip_location VARCHAR(100) DEFAULT NULL,
    browser VARCHAR(100) DEFAULT NULL,
    os VARCHAR(50) DEFAULT NULL,
    status TINYINT DEFAULT 1,
    error_msg VARCHAR(1000) DEFAULT NULL,
    cost_time INT DEFAULT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
);

-- 25. 课程资源表
CREATE TABLE IF NOT EXISTS training_course_resource (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_id BIGINT NOT NULL,
    resource_name VARCHAR(100) NOT NULL,
    resource_type TINYINT NOT NULL,
    resource_url VARCHAR(500) NOT NULL,
    file_size BIGINT DEFAULT NULL,
    file_format VARCHAR(20) DEFAULT NULL,
    version VARCHAR(20) DEFAULT NULL,
    is_preview TINYINT DEFAULT 0,
    sort_order INT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 27. 补训记录表
CREATE TABLE IF NOT EXISTS retraining_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    original_plan_id BIGINT NOT NULL,
    retrain_plan_id BIGINT DEFAULT NULL,
    reason VARCHAR(500) NOT NULL,
    trigger_type TINYINT NOT NULL,
    status TINYINT DEFAULT 0,
    deadline TIMESTAMP DEFAULT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    complete_time TIMESTAMP DEFAULT NULL,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 28. 登录日志表
CREATE TABLE IF NOT EXISTS sys_login_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT DEFAULT NULL,
    username VARCHAR(50) DEFAULT NULL,
    login_type TINYINT DEFAULT 1,
    status TINYINT NOT NULL,
    ip_address VARCHAR(64) DEFAULT NULL,
    ip_location VARCHAR(100) DEFAULT NULL,
    browser VARCHAR(100) DEFAULT NULL,
    os VARCHAR(50) DEFAULT NULL,
    device_info VARCHAR(100) DEFAULT NULL,
    msg VARCHAR(500) DEFAULT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 31. 文件管理表
CREATE TABLE IF NOT EXISTS sys_file (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    file_name VARCHAR(100) NOT NULL,
    file_original_name VARCHAR(200) DEFAULT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_url VARCHAR(500) DEFAULT NULL,
    file_size BIGINT DEFAULT NULL,
    file_type VARCHAR(50) DEFAULT NULL,
    file_extension VARCHAR(20) DEFAULT NULL,
    upload_user_id BIGINT DEFAULT NULL,
    module VARCHAR(50) DEFAULT NULL,
    status TINYINT DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 32. 定时任务表
CREATE TABLE IF NOT EXISTS sys_job (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    job_name VARCHAR(100) NOT NULL,
    job_group VARCHAR(50) DEFAULT 'DEFAULT',
    job_type TINYINT DEFAULT 1,
    cron_expression VARCHAR(50) DEFAULT NULL,
    invoke_target VARCHAR(500) NOT NULL,
    method_name VARCHAR(100) DEFAULT NULL,
    method_params VARCHAR(500) DEFAULT NULL,
    status TINYINT DEFAULT 1,
    error_policy TINYINT DEFAULT 1,
    max_retry INT DEFAULT 3,
    last_execute_time TIMESTAMP DEFAULT NULL,
    next_execute_time TIMESTAMP DEFAULT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 33. 定时任务日志表
CREATE TABLE IF NOT EXISTS sys_job_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    job_id BIGINT NOT NULL,
    job_name VARCHAR(100) DEFAULT NULL,
    job_group VARCHAR(50) DEFAULT NULL,
    execute_time TIMESTAMP DEFAULT NULL,
    duration INT DEFAULT NULL,
    status TINYINT NOT NULL,
    error_msg CLOB DEFAULT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_dept_parent_id ON sys_dept(parent_id);
CREATE INDEX IF NOT EXISTS idx_dept_level ON sys_dept(level);
CREATE INDEX IF NOT EXISTS idx_position_dept_id ON sys_position(dept_id);
CREATE INDEX IF NOT EXISTS idx_user_dept_id ON sys_user(dept_id);
CREATE INDEX IF NOT EXISTS idx_user_status ON sys_user(status);
CREATE INDEX IF NOT EXISTS idx_user_plan_user_id ON etms_user_plan(user_id);
CREATE INDEX IF NOT EXISTS idx_user_plan_plan_id ON etms_user_plan(plan_id);
CREATE INDEX IF NOT EXISTS idx_progress_user_id ON learning_progress(user_id);
CREATE INDEX IF NOT EXISTS idx_progress_plan_id ON learning_progress(plan_id);
CREATE INDEX IF NOT EXISTS idx_progress_course_id ON learning_progress(course_id);
CREATE INDEX IF NOT EXISTS idx_question_type ON exam_question(question_type);
CREATE INDEX IF NOT EXISTS idx_question_category ON exam_question(category_id);
CREATE INDEX IF NOT EXISTS idx_paper_plan_id ON exam_paper(plan_id);
CREATE INDEX IF NOT EXISTS idx_paper_course_id ON exam_paper(course_id);
CREATE INDEX IF NOT EXISTS idx_record_user_id ON exam_record(user_id);
CREATE INDEX IF NOT EXISTS idx_record_plan_id ON exam_record(plan_id);
CREATE INDEX IF NOT EXISTS idx_result_user_id ON exam_result(user_id);
CREATE INDEX IF NOT EXISTS idx_result_plan_id ON exam_result(plan_id);
CREATE INDEX IF NOT EXISTS idx_course_resource_course_id ON training_course_resource(course_id);

