/**
 * 公共类型定义
 */

// 通用API响应结构
export interface ApiResponse<T> {
  code: number
  message: string
  data: T
  timestamp: string
}

// 分页结果
export interface PageResult<T> {
  records: T[]
  total: number
  current: number
  size: number
  pages: number
}

// 分页参数
export interface PageParams {
  current?: number
  size?: number
  [key: string]: any
}

/**
 * 用户相关类型
 */
export interface User {
  id: number
  username: string
  realName: string
  avatar?: string
  email?: string
  phone?: string
  gender?: number
  status: number
  deptId?: number
  deptName?: string
  positionId?: number
  positionName?: string
  roleIds?: number[]
  roleNames?: string[]
  roles?: { id: number; roleName: string }[]  // 用户详情中的角色列表
  loginIp?: string
  loginTime?: string
  createTime?: string
  updateTime?: string
}

export interface UserCreateParams {
  username: string
  password: string
  realName: string
  avatar?: string
  email?: string
  phone?: string
  gender?: number
  status?: number
  deptId?: number
  positionId?: number
  roleIds?: number[]
}

export interface UserUpdateParams {
  realName?: string
  avatar?: string
  email?: string
  phone?: string
  gender?: number
  status?: number
  deptId?: number
  positionId?: number
  roleIds?: number[]
}

/**
 * 角色相关类型 - 已修复与后端字段匹配
 */
export interface Role {
  id: number
  roleCode: string
  roleName: string
  roleDesc?: string
  dataScope?: number
  sortOrder?: number
  status: number
  createTime?: string
  updateTime?: string
  permissionIds?: number[]
}

export interface RoleCreateParams {
  roleCode: string
  roleName: string
  roleDesc?: string
  dataScope?: number
  sortOrder?: number
  status?: number
}

export interface RoleUpdateParams {
  roleCode?: string
  roleName?: string
  roleDesc?: string
  dataScope?: number
  sortOrder?: number
  status?: number
}

/**
 * 部门相关类型 - 已修复与后端字段匹配
 */
export interface Dept {
  id: number
  deptName: string
  deptCode?: string
  parentId?: number
  parentName?: string
  leaderId?: number
  leaderName?: string
  sortOrder?: number
  level?: number
  ancestors?: string
  status: number
  children?: Dept[]
  createTime?: string
  updateTime?: string
}

export interface DeptCreateParams {
  deptName: string
  deptCode?: string
  parentId?: number
  leaderId?: number
  sortOrder?: number
  status?: number
}

export interface DeptUpdateParams {
  deptName?: string
  deptCode?: string
  parentId?: number
  leaderId?: number
  sortOrder?: number
  status?: number
}

/**
 * 课程相关类型 - 已修复与后端字段匹配
 */
export interface Course {
  id: number
  courseName: string
  courseCode?: string
  courseDesc?: string
  courseObjective?: string
  coverImage?: string
  videoUrl?: string
  documentUrl?: string
  pptUrl?: string
  courseType?: number  // 课程类型(1视频 2文档 3直播)
  duration?: number
  credit?: number
  difficulty?: number
  targetAudience?: string
  prerequisite?: string
  tagIds?: string
  status: number
  auditRemark?: string
  auditBy?: number
  auditTime?: string
  viewCount?: number
  collectCount?: number
  ratingAvg?: number
  ratingCount?: number
  createTime?: string
  updateTime?: string
}

export interface CourseCreateParams {
  courseName: string
  courseCode?: string
  courseDesc?: string
  courseObjective?: string
  coverImage?: string
  videoUrl?: string
  documentUrl?: string
  pptUrl?: string
  courseType?: number  // 课程类型(1视频 2文档 3直播)
  duration?: number
  credit?: number
  difficulty?: number
  targetAudience?: string
  prerequisite?: string
  tagIds?: string
}

export interface CourseUpdateParams {
  courseName?: string
  courseCode?: string
  courseDesc?: string
  courseObjective?: string
  coverImage?: string
  videoUrl?: string
  documentUrl?: string
  pptUrl?: string
  courseType?: number  // 课程类型(1视频 2文档 3直播)
  duration?: number
  credit?: number
  difficulty?: number
  targetAudience?: string
  prerequisite?: string
  tagIds?: string
}

export interface CourseAuditParams {
  status: number
  auditRemark?: string
}

/**
 * 题目相关类型 - 已修复与后端字段匹配
 */
export interface Question {
  id: number
  questionCode?: string
  questionType: number
  questionContent: string
  optionA?: string
  optionB?: string
  optionC?: string
  optionD?: string
  optionE?: string
  answer: string
  answerAnalysis?: string
  score: number
  difficulty?: number
  categoryId?: number
  courseId?: number
  tagIds?: string
  status: number
  createTime?: string
  updateTime?: string
}

export interface QuestionCreateParams {
  questionCode?: string
  questionType: number
  questionContent: string
  optionA?: string
  optionB?: string
  optionC?: string
  optionD?: string
  optionE?: string
  answer: string
  answerAnalysis?: string
  score: number
  difficulty?: number
  categoryId?: number
  courseId?: number
  tagIds?: string
}

export interface QuestionUpdateParams {
  questionCode?: string
  /** 题目类型(1单选 2多选 3判断 4填空 5简答) - 更新时建议传递 */
  questionType?: number
  questionContent?: string
  optionA?: string
  optionB?: string
  optionC?: string
  optionD?: string
  optionE?: string
  answer?: string
  answerAnalysis?: string
  score?: number
  difficulty?: number
  categoryId?: number
  courseId?: number
  tagIds?: string
}

export interface QuestionRandomParams {
  courseId?: number  // 修复：与后端参数名一致，原来是categoryId
  questionType?: number
  difficulty?: number
  count: number
}

/**
 * 试卷相关类型 - 已修复与后端字段匹配
 */
export interface Paper {
  id: number
  paperName: string
  paperCode?: string
  planId?: number
  planName?: string
  courseId?: number
  courseName?: string
  paperType?: number
  totalScore: number
  passScore: number
  duration: number  // 考试时长(分钟)，后端通过@JsonProperty("duration")映射examDuration字段
  questionCount?: number
  questionConfig?: string
  shuffleOption?: number
  shuffleQuestion?: number
  antiCheat?: number
  maxSwitch?: number
  status: number
  startTime?: string
  endTime?: string
  description?: string  // 试卷说明
  createTime?: string
  updateTime?: string
  questions?: PaperQuestion[]
}

export interface PaperQuestion {
  id: number
  paperId: number
  questionId: number
  score: number
  sortOrder: number
  question?: Question
}

export interface PaperCreateParams {
  paperName: string
  paperCode?: string
  planId?: number
  courseId?: number
  paperType?: number
  totalScore: number
  passScore: number
  duration: number  // 考试时长(分钟)，后端通过@JsonProperty("duration")映射examDuration字段
  startTime?: string
  endTime?: string
  description?: string  // 试卷说明
  questionCount?: number
  questionConfig?: string
  shuffleOption?: number
  shuffleQuestion?: number
  antiCheat?: number
  maxSwitch?: number
  questions?: { questionId: number; score: number; sortOrder: number }[]
}

export interface PaperUpdateParams {
  paperName?: string
  paperCode?: string
  planId?: number
  courseId?: number
  paperType?: number
  totalScore?: number
  passScore?: number
  duration?: number  // 考试时长(分钟)，后端通过@JsonProperty("duration")映射examDuration字段
  startTime?: string
  endTime?: string
  description?: string  // 试卷说明
  questionCount?: number
  questionConfig?: string
  shuffleOption?: number
  shuffleQuestion?: number
  antiCheat?: number
  maxSwitch?: number
  questions?: { questionId: number; score: number; sortOrder: number }[]
}

/**
 * 考试记录相关类型 - 已修复与后端字段匹配
 */
export interface ExamRecord {
  id: number
  paperId: number
  paperName?: string
  planId?: number
  userId: number
  userName?: string
  realName?: string
  deptName?: string
  startTime: string
  submitTime?: string
  totalScore?: number
  userScore?: number
  passScore?: number  // 及格分数
  passed?: number
  status: number
  duration?: number  // 考试时长(分钟)
  durationUsed?: number  // 实际用时(分钟)
  correctCount?: number  // 正确题数
  totalCount?: number  // 总题数
  answers?: ExamAnswer[]
  questions?: ExamRecordQuestion[]  // 题目列表
  paperQuestions?: ExamRecordQuestion[]  // 试卷题目列表
  createTime?: string
  updateTime?: string
}

export interface ExamAnswer {
  id: number
  recordId: number
  questionId: number
  userAnswer: string
  isCorrect?: boolean
  score?: number
}

/**
 * 考试记录中的题目类型（用于questions和paperQuestions字段）
 */
export interface ExamRecordQuestion {
  questionId: number
  questionType: number
  questionContent: string
  optionA?: string
  optionB?: string
  optionC?: string
  optionD?: string
  optionE?: string
  answer?: string
  answerAnalysis?: string
  difficulty?: number
  score: number
  sortOrder?: number
  userAnswer?: string
  isCorrect?: boolean
  userScore?: number
}

export interface ExamSubmitParams {
  recordId: number
  answers: { questionId: number; userAnswer: string }[]
}

/**
 * 培训计划相关类型 - 已修复与后端字段匹配
 */
export interface TrainingPlan {
  id: number
  planName: string
  planCode?: string
  planDesc?: string
  planObjective?: string
  courseId?: number
  courseName?: string
  planType?: number
  startDate: string
  endDate: string
  signStartTime?: string
  signEndTime?: string
  targetType?: number
  targetDeptIds?: string
  targetPositionIds?: string
  targetUserIds?: string
  minStudyTime?: number
  minProgress?: number
  needExam?: number
  passScore?: number
  maxRetake?: number
  status: number
  createTime?: string
  updateTime?: string
  courses?: TrainingPlanCourse[]
}

export interface TrainingPlanCourse {
  id: number
  planId: number
  courseId: number
  courseName?: string
  sortOrder: number
  required: boolean
}

export interface TrainingPlanCreateParams {
  planName: string
  planCode?: string
  planDesc?: string
  planObjective?: string
  courseId?: number
  planType?: number
  startDate: string
  endDate: string
  signStartTime?: string
  signEndTime?: string
  targetType?: number
  targetDeptIds?: string
  targetPositionIds?: string
  targetUserIds?: string
  minStudyTime?: number
  minProgress?: number
  needExam?: number
  passScore?: number
  maxRetake?: number
}

export interface TrainingPlanUpdateParams {
  planName?: string
  planCode?: string
  planDesc?: string
  planObjective?: string
  courseId?: number
  planType?: number
  startDate?: string
  endDate?: string
  signStartTime?: string
  signEndTime?: string
  targetType?: number
  targetDeptIds?: string
  targetPositionIds?: string
  targetUserIds?: string
  minStudyTime?: number
  minProgress?: number
  needExam?: number
  passScore?: number
  maxRetake?: number
}

/**
 * 学习进度相关类型
 */
export interface LearningProgress {
  id: number
  planId: number
  planName?: string
  userId: number
  userName?: string
  realName?: string
  deptName?: string
  courseId: number
  courseName?: string
  progress: number
  studyTime?: number
  status: number
  startTime?: string
  lastStudyTime?: string
  completeTime?: string
  createTime?: string
  updateTime?: string
}

export interface LearningProgressUpdateParams {
  planId: number
  courseId: number
  progress: number
}

/**
 * 成绩相关类型 - 已修复与后端字段匹配
 */
export interface ExamResult {
  id: number
  recordId: number
  paperId: number
  paperName?: string
  userId: number
  userName?: string
  realName?: string
  deptId?: number  // 部门ID
  deptName?: string
  totalScore: number
  userScore: number
  passScore: number
  passed: number
  submitTime: string
  startTime?: string
  examDuration?: number
  durationUsed?: number  // 实际用时(分钟)
  objectiveScore?: number  // 客观题得分
  subjectiveScore?: number  // 主观题得分
  answerDetail?: string  // 答题详情JSON
  answers?: any[]  // 答题详情数组
  retakeCount?: number  // 补考次数
  createTime?: string
}

/**
 * 登录相关类型
 */
export interface LoginParams {
  username: string
  password: string
}

export interface LoginResult {
  accessToken: string
  tokenType: string
  expiresIn: number
  userId: number
  username: string
  realName: string
  avatar: string
  deptName: string
  roles: string[]
  permissions: string[]
}

export interface UserInfo {
  id: number
  username: string
  realName: string
  avatar?: string
  email?: string
  phone?: string
  deptId?: number
  deptName?: string
  roleNames: string[]
  permissions: string[]
}

/**
 * 用户培训计划关联
 */
export interface UserPlan {
  id: number
  userId: number
  planId: number
  planName?: string
  courseId?: number
  courseName?: string
  courseType?: number
  coverImage?: string
  courseDesc?: string
  duration?: number
  credit?: number
  progress: number
  status: number
  startDate?: string
  endDate?: string
  createTime?: string
  updateTime?: string
}

/**
 * 成绩统计类型
 */
export interface ResultStats {
  totalCount: number   // 总考试次数
  passCount: number    // 通过次数
  failCount: number    // 未通过次数
  passRate: number     // 通过率(百分比)
  avgScore: number     // 平均分
}

/**
 * 试卷题目关联类型（用于组卷）
 */
export interface PaperQuestionItem {
  questionId: number
  score: number
  sortOrder: number
}

/**
 * 试卷题目详情类型
 */
export interface PaperQuestionDetail {
  questionId: number
  questionType: number
  questionContent: string
  optionA?: string
  optionB?: string
  optionC?: string
  optionD?: string
  optionE?: string
  answer?: string
  answerAnalysis?: string
  difficulty?: number
  score: number
  sortOrder: number
}
