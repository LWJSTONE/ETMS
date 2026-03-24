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

// 获取可参加的考试列表（普通用户可用）
export function getAvailableExams(params: PageParams): Promise<ApiResponse<PageResult<Paper>>> {
  return request.get('/exam/papers/available', params)
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

// 放弃考试
export function giveUpExam(recordId: number): Promise<ApiResponse<void>> {
  return request.post(`/exam/records/giveup/${recordId}`)
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
  return new Promise((resolve, reject) => {
    // 获取token
    const token = localStorage.getItem('token')
    
    // 构建查询参数
    const queryParts: string[] = []
    if (params) {
      Object.entries(params).forEach(([key, value]) => {
        if (value !== undefined && value !== null && value !== '') {
          queryParts.push(`${key}=${encodeURIComponent(value as string)}`)
        }
      })
    }
    const queryStr = queryParts.length > 0 ? `?${queryParts.join('&')}` : ''
    
    // 使用fetch API处理文件下载，支持blob响应
    fetch(`/api/exam/results/export${queryStr}`, {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
      .then(async (response) => {
        if (!response.ok) {
          // 尝试解析错误信息
          let errorMessage = '导出失败'
          try {
            const errorData = await response.json()
            errorMessage = errorData.message || errorMessage
          } catch {
            // 如果无法解析JSON，使用状态文本
            errorMessage = response.statusText || errorMessage
          }
          throw new Error(errorMessage)
        }
        return response.blob()
      })
      .then((blob) => {
        resolve(blob)
      })
      .catch((error) => {
        reject(error)
      })
  })
}

// 成绩统计类型
export interface ResultStats {
  totalCount: number
  passCount: number
  failCount: number
  passRate: number
  avgScore: number
}
