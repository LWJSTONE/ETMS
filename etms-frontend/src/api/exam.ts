import request from '@/utils/request'
import type {
  ApiResponse,
  PageResult,
  PageParams,
  Question,
  QuestionCreateParams,
  QuestionUpdateParams,
  QuestionRandomParams,
  Paper,
  PaperCreateParams,
  PaperUpdateParams,
  ExamRecord,
  ExamSubmitParams,
  ExamResult
} from './types'

/**
 * 题库管理API
 */

// 获取题目列表
export function getQuestionList(params: PageParams): Promise<ApiResponse<PageResult<Question>>> {
  return request.get('/exam/questions', params)
}

// 获取题目详情
export function getQuestionDetail(id: number): Promise<ApiResponse<Question>> {
  return request.get(`/exam/questions/${id}`)
}

// 新增题目
export function createQuestion(data: QuestionCreateParams): Promise<ApiResponse<void>> {
  return request.post('/exam/questions', data)
}

// 更新题目
export function updateQuestion(id: number, data: QuestionUpdateParams): Promise<ApiResponse<void>> {
  return request.put(`/exam/questions/${id}`, data)
}

// 删除题目
export function deleteQuestion(id: number): Promise<ApiResponse<void>> {
  return request.delete(`/exam/questions/${id}`)
}

// 随机抽取题目
export function randomQuestions(params: QuestionRandomParams): Promise<ApiResponse<Question[]>> {
  return request.get('/exam/questions/random', params)
}

/**
 * 试卷管理API
 */

// 获取试卷列表
export function getPaperList(params: PageParams): Promise<ApiResponse<PageResult<Paper>>> {
  return request.get('/exam/papers', params)
}

// 获取试卷详情
export function getPaperDetail(id: number): Promise<ApiResponse<Paper>> {
  return request.get(`/exam/papers/${id}`)
}

// 新增试卷
export function createPaper(data: PaperCreateParams): Promise<ApiResponse<void>> {
  return request.post('/exam/papers', data)
}

// 更新试卷
export function updatePaper(id: number, data: PaperUpdateParams): Promise<ApiResponse<void>> {
  return request.put(`/exam/papers/${id}`, data)
}

// 删除试卷
export function deletePaper(id: number): Promise<ApiResponse<void>> {
  return request.delete(`/exam/papers/${id}`)
}

// 发布试卷
export function publishPaper(id: number): Promise<ApiResponse<void>> {
  return request.post(`/exam/papers/${id}/publish`)
}

// 停用试卷
export function disablePaper(id: number): Promise<ApiResponse<void>> {
  return request.post(`/exam/papers/${id}/disable`)
}

/**
 * 考试记录API
 */

// 获取考试记录列表
export function getExamRecordList(params: PageParams): Promise<ApiResponse<PageResult<ExamRecord>>> {
  return request.get('/exam/records', params)
}

// 获取我的考试记录
export function getMyExamRecordList(params: PageParams): Promise<ApiResponse<PageResult<ExamRecord>>> {
  return request.get('/exam/records/my', params)
}

// 开始考试
export function startExam(paperId: number, planId?: number): Promise<ApiResponse<ExamRecord>> {
  const params = planId ? { planId } : undefined
  return request.post(`/exam/records/start/${paperId}`, null, { params })
}

// 提交试卷
export function submitExam(data: ExamSubmitParams): Promise<ApiResponse<void>> {
  return request.post('/exam/records/submit', data)
}

// 获取考试记录详情
export function getExamRecordDetail(id: number): Promise<ApiResponse<ExamRecord>> {
  return request.get(`/exam/records/${id}`)
}

/**
 * 成绩管理API
 */

// 获取成绩列表
export function getResultList(params: PageParams): Promise<ApiResponse<PageResult<ExamResult>>> {
  return request.get('/exam/results', params)
}

// 获取个人成绩
export function getMyResults(params: PageParams): Promise<ApiResponse<PageResult<ExamResult>>> {
  return request.get('/exam/results/my', params)
}

// 获取成绩详情
export function getResultDetail(id: number): Promise<ApiResponse<ExamResult>> {
  return request.get(`/exam/results/${id}`)
}

// 获取成绩统计
export function getResultStats(params?: PageParams): Promise<ApiResponse<ResultStats>> {
  return request.get('/exam/results/stats', params)
}

// 成绩统计类型
export interface ResultStats {
  totalCount: number
  passCount: number
  failCount: number
  passRate: number
  avgScore: number
}
