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
  roleIds?: number[]
  roleNames?: string[]
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
  roleIds?: number[]
}

/**
 * 角色相关类型
 */
export interface Role {
  id: number
  name: string
  code: string
  description?: string
  status: number
  sort?: number
  createTime?: string
  updateTime?: string
}

export interface RoleCreateParams {
  name: string
  code: string
  description?: string
  status?: number
  sort?: number
}

export interface RoleUpdateParams {
  name?: string
  code?: string
  description?: string
  status?: number
  sort?: number
}

/**
 * 部门相关类型
 */
export interface Dept {
  id: number
  name: string
  parentId?: number
  parentName?: string
  code?: string
  leader?: string
  phone?: string
  email?: string
  status: number
  sort?: number
  children?: Dept[]
  createTime?: string
  updateTime?: string
}

export interface DeptCreateParams {
  name: string
  parentId?: number
  code?: string
  leader?: string
  phone?: string
  email?: string
  status?: number
  sort?: number
}

export interface DeptUpdateParams {
  name?: string
  parentId?: number
  code?: string
  leader?: string
  phone?: string
  email?: string
  status?: number
  sort?: number
}

/**
 * 课程相关类型
 */
export interface Course {
  id: number
  title: string
  description?: string
  coverImage?: string
  categoryId?: number
  categoryName?: string
  creatorId?: number
  creatorName?: string
  duration?: number
  credit?: number
  difficulty?: number
  status: number
  auditStatus?: number
  auditRemark?: string
  auditorId?: number
  auditorName?: string
  auditTime?: string
  createTime?: string
  updateTime?: string
}

export interface CourseCreateParams {
  title: string
  description?: string
  coverImage?: string
  categoryId?: number
  duration?: number
  credit?: number
  difficulty?: number
}

export interface CourseUpdateParams {
  title?: string
  description?: string
  coverImage?: string
  categoryId?: number
  duration?: number
  credit?: number
  difficulty?: number
}

export interface CourseAuditParams {
  status: number
  auditRemark?: string
}

/**
 * 题目相关类型
 */
export interface Question {
  id: number
  content: string
  type: number
  options?: string
  answer: string
  analysis?: string
  score: number
  difficulty?: number
  categoryId?: number
  categoryName?: string
  creatorId?: number
  creatorName?: string
  status: number
  createTime?: string
  updateTime?: string
}

export interface QuestionCreateParams {
  content: string
  type: number
  options?: string
  answer: string
  analysis?: string
  score: number
  difficulty?: number
  categoryId?: number
}

export interface QuestionUpdateParams {
  content?: string
  type?: number
  options?: string
  answer?: string
  analysis?: string
  score?: number
  difficulty?: number
  categoryId?: number
}

export interface QuestionRandomParams {
  categoryId?: number
  type?: number
  difficulty?: number
  count: number
}

/**
 * 试卷相关类型
 */
export interface Paper {
  id: number
  title: string
  description?: string
  totalScore: number
  passScore: number
  duration: number
  questionCount?: number
  status: number
  creatorId?: number
  creatorName?: string
  createTime?: string
  updateTime?: string
  questions?: PaperQuestion[]
}

export interface PaperQuestion {
  id: number
  paperId: number
  questionId: number
  score: number
  sort: number
  question?: Question
}

export interface PaperCreateParams {
  title: string
  description?: string
  totalScore: number
  passScore: number
  duration: number
  questions?: { questionId: number; score: number; sort: number }[]
}

export interface PaperUpdateParams {
  title?: string
  description?: string
  totalScore?: number
  passScore?: number
  duration?: number
  questions?: { questionId: number; score: number; sort: number }[]
}

/**
 * 考试记录相关类型
 */
export interface ExamRecord {
  id: number
  paperId: number
  paperTitle?: string
  userId: number
  userName?: string
  startTime: string
  submitTime?: string
  totalScore?: number
  userScore?: number
  status: number
  answers?: ExamAnswer[]
  createTime?: string
}

export interface ExamAnswer {
  id: number
  recordId: number
  questionId: number
  userAnswer: string
  isCorrect?: boolean
  score?: number
}

export interface ExamSubmitParams {
  recordId: number
  answers: { questionId: number; userAnswer: string }[]
}

/**
 * 培训计划相关类型
 */
export interface TrainingPlan {
  id: number
  title: string
  description?: string
  startDate: string
  endDate: string
  location?: string
  lecturer?: string
  credit?: number
  status: number
  deptId?: number
  deptName?: string
  creatorId?: number
  creatorName?: string
  createTime?: string
  updateTime?: string
  courses?: TrainingPlanCourse[]
}

export interface TrainingPlanCourse {
  id: number
  planId: number
  courseId: number
  courseName?: string
  sort: number
  required: boolean
}

export interface TrainingPlanCreateParams {
  title: string
  description?: string
  startDate: string
  endDate: string
  location?: string
  lecturer?: string
  credit?: number
  deptId?: number
  courses?: { courseId: number; sort: number; required: boolean }[]
}

export interface TrainingPlanUpdateParams {
  title?: string
  description?: string
  startDate?: string
  endDate?: string
  location?: string
  lecturer?: string
  credit?: number
  deptId?: number
  courses?: { courseId: number; sort: number; required: boolean }[]
}

/**
 * 学习进度相关类型
 */
export interface LearningProgress {
  id: number
  planId: number
  planTitle?: string
  userId: number
  userName?: string
  courseId: number
  courseName?: string
  progress: number
  status: number
  startTime?: string
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
 * 签到记录相关类型
 */
export interface AttendanceRecord {
  id: number
  planId: number
  planTitle?: string
  userId: number
  userName?: string
  signType: number
  signTime: string
  location?: string
  status: number
  isSupplementary: boolean
  supplementaryStatus?: number
  supplementaryReason?: string
  auditStatus?: number
  auditRemark?: string
  auditorId?: number
  auditorName?: string
  auditTime?: string
  createTime?: string
}

export interface AttendanceSignInParams {
  planId: number
  signType: number
  location?: string
}

export interface AttendanceSupplementaryParams {
  planId: number
  signType: number
  signTime: string
  reason: string
}

export interface AttendanceAuditParams {
  auditStatus: number
  auditRemark?: string
}

export interface AttendanceStats {
  totalDays: number
  signedDays: number
  lateTimes: number
  earlyLeaveTimes: number
  absentDays: number
  supplementaryCount: number
  approvedCount: number
}

/**
 * 成绩相关类型
 */
export interface ExamResult {
  id: number
  recordId: number
  paperId: number
  paperTitle?: string
  userId: number
  userName?: string
  deptName?: string
  totalScore: number
  userScore: number
  passScore: number
  isPass: boolean
  submitTime: string
  duration: number
  createTime?: string
}

/**
 * 分类相关类型
 */
export interface Category {
  id: number
  name: string
  type: number
  parentId?: number
  parentName?: string
  status: number
  sort?: number
  children?: Category[]
  createTime?: string
  updateTime?: string
}

export interface CategoryCreateParams {
  name: string
  type: number
  parentId?: number
  status?: number
  sort?: number
}

export interface CategoryUpdateParams {
  name?: string
  type?: number
  parentId?: number
  status?: number
  sort?: number
}

/**
 * 登录相关类型
 */
export interface LoginParams {
  username: string
  password: string
  captcha?: string
  captchaKey?: string
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
  roles: string[]
  permissions: string[]
}
