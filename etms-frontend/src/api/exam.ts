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
  ExamResult,
  ResultStats,
  PaperQuestionItem,
  PaperQuestionDetail
} from './types'

/**
 * 题库管理API
 * 注意：由于响应拦截器已解包返回response.data，所以API函数返回的实际数据类型为T，而非ApiResponse<T>
 */

// 获取题目列表
export function getQuestionList(params: PageParams): Promise<PageResult<Question>> {
  return request.get('/exam/questions', params)
}

// 获取题目详情
export function getQuestionDetail(id: number): Promise<Question> {
  return request.get(`/exam/questions/${id}`)
}

// 新增题目
export function createQuestion(data: QuestionCreateParams): Promise<void> {
  return request.post('/exam/questions', data)
}

// 更新题目
export function updateQuestion(id: number, data: QuestionUpdateParams): Promise<void> {
  return request.put(`/exam/questions/${id}`, data)
}

// 删除题目
export function deleteQuestion(id: number): Promise<void> {
  return request.delete(`/exam/questions/${id}`)
}

// 随机抽取题目
export function randomQuestions(params: QuestionRandomParams): Promise<Question[]> {
  return request.get('/exam/questions/random', params)
}

/**
 * 试卷管理API
 */

// 获取试卷列表
export function getPaperList(params: PageParams): Promise<PageResult<Paper>> {
  return request.get('/exam/papers', params)
}

// 获取可参加的考试列表（普通用户可用）
export function getAvailableExams(params: PageParams): Promise<PageResult<Paper>> {
  return request.get('/exam/papers/available', params)
}

// 获取试卷详情
export function getPaperDetail(id: number, forExam?: boolean, planId?: number): Promise<Paper> {
  const params: Record<string, any> = {}
  if (forExam !== undefined) params.forExam = forExam
  if (planId !== undefined) params.planId = planId
  return request.get(`/exam/papers/${id}`, Object.keys(params).length > 0 ? params : undefined)
}

// 新增试卷
export function createPaper(data: PaperCreateParams): Promise<void> {
  return request.post('/exam/papers', data)
}

// 更新试卷
export function updatePaper(id: number, data: PaperUpdateParams): Promise<void> {
  return request.put(`/exam/papers/${id}`, data)
}

// 删除试卷
export function deletePaper(id: number): Promise<void> {
  return request.delete(`/exam/papers/${id}`)
}

// 发布试卷
export function publishPaper(id: number): Promise<void> {
  return request.post(`/exam/papers/${id}/publish`)
}

// 停用试卷
export function disablePaper(id: number): Promise<void> {
  return request.post(`/exam/papers/${id}/disable`)
}

/**
 * 考试记录API
 */

// 获取考试记录列表
export function getExamRecordList(params: PageParams): Promise<PageResult<ExamRecord>> {
  return request.get('/exam/records', params)
}

// 获取我的考试记录
export function getMyExamRecordList(params: PageParams): Promise<PageResult<ExamRecord>> {
  return request.get('/exam/records/my', params)
}

// 开始考试
export function startExam(paperId: number, planId?: number): Promise<ExamRecord> {
  if (planId) {
    return request.post(`/exam/records/start/${paperId}?planId=${planId}`)
  }
  return request.post(`/exam/records/start/${paperId}`)
}

// 提交试卷
export function submitExam(data: ExamSubmitParams): Promise<void> {
  const payload = {
    recordId: data.recordId,
    answers: JSON.stringify(data.answers)
  }
  return request.post('/exam/records/submit', payload)
}

// 放弃考试
export function giveUpExam(recordId: number): Promise<void> {
  return request.post(`/exam/records/giveup/${recordId}`)
}

// 获取考试记录详情
export function getExamRecordDetail(id: number): Promise<ExamRecord> {
  return request.get(`/exam/records/${id}`)
}

// 导出考试记录
export function exportExamRecords(params?: {
  paperId?: number
  userId?: number
  status?: number
  userName?: string
  paperName?: string
}): Promise<Blob> {
  return request.getBlob('/exam/records/export', params)
}

/**
 * 成绩管理API
 */

// 获取成绩列表
export function getResultList(params: PageParams): Promise<PageResult<ExamResult>> {
  return request.get('/exam/results', params)
}

// 获取个人成绩
export function getMyResults(params: PageParams): Promise<PageResult<ExamResult>> {
  return request.get('/exam/results/my', params)
}

// 获取成绩详情
export function getResultDetail(id: number): Promise<ExamResult> {
  return request.get(`/exam/results/${id}`)
}

// 获取成绩统计
export function getResultStats(params?: { startTime?: string; endTime?: string }): Promise<ResultStats> {
  return request.get('/exam/results/stats', params)
}

// 导出成绩
export function exportResults(params?: {
  paperId?: number
  userId?: number
  passed?: number
  userName?: string
  paperName?: string
  startTime?: string
  endTime?: string
}): Promise<Blob> {
  return request.getBlob('/exam/results/export', params)
}

/**
 * 组卷管理API
 */

// 获取试卷题目列表
export function getPaperQuestions(paperId: number): Promise<PaperQuestionDetail[]> {
  return request.get(`/exam/papers/${paperId}/questions`)
}

// 批量添加题目到试卷
export function batchAddQuestionsToPaper(paperId: number, questions: PaperQuestionItem[]): Promise<void> {
  return request.post(`/exam/papers/${paperId}/questions`, questions)
}

// 从试卷移除单个题目
export function removeQuestionFromPaper(paperId: number, questionId: number): Promise<void> {
  return request.delete(`/exam/papers/${paperId}/questions/${questionId}`)
}

// 清空试卷所有题目
export function clearPaperQuestions(paperId: number): Promise<void> {
  return request.delete(`/exam/papers/${paperId}/questions`)
}
