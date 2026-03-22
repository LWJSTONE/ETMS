import request from '@/utils/request'

/**
 * 题库管理API
 */

// 获取题目列表
export function getQuestionList(params: any) {
  return request.get('/exam/questions', params)
}

// 获取题目详情
export function getQuestionDetail(id: number) {
  return request.get(`/exam/questions/${id}`)
}

// 新增题目
export function addQuestion(data: any) {
  return request.post('/exam/questions', data)
}

// 更新题目
export function updateQuestion(id: number, data: any) {
  return request.put(`/exam/questions/${id}`, data)
}

// 删除题目
export function deleteQuestion(id: number) {
  return request.delete(`/exam/questions/${id}`)
}

// 随机抽取题目
export function randomQuestions(params: any) {
  return request.get('/exam/questions/random', params)
}

/**
 * 试卷管理API
 */

// 获取试卷列表
export function getPaperList(params: any) {
  return request.get('/exam/papers', params)
}

// 获取试卷详情
export function getPaperDetail(id: number) {
  return request.get(`/exam/papers/${id}`)
}

// 新增试卷
export function addPaper(data: any) {
  return request.post('/exam/papers', data)
}

// 更新试卷
export function updatePaper(id: number, data: any) {
  return request.put(`/exam/papers/${id}`, data)
}

// 删除试卷
export function deletePaper(id: number) {
  return request.delete(`/exam/papers/${id}`)
}

// 发布试卷
export function publishPaper(id: number) {
  return request.post(`/exam/papers/${id}/publish`)
}

// 停用试卷
export function disablePaper(id: number) {
  return request.post(`/exam/papers/${id}/disable`)
}

/**
 * 考试记录API
 */

// 获取考试记录列表
export function getExamRecordList(params: any) {
  return request.get('/exam/records', params)
}

// 开始考试
export function startExam(paperId: number) {
  return request.post(`/exam/records/start/${paperId}`)
}

// 提交试卷
export function submitExam(data: any) {
  return request.post('/exam/records/submit', data)
}

// 获取考试记录详情
export function getExamRecordDetail(id: number) {
  return request.get(`/exam/records/${id}`)
}

/**
 * 成绩管理API
 */

// 获取成绩列表
export function getResultList(params: any) {
  return request.get('/exam/results', params)
}

// 获取个人成绩
export function getMyResults(params: any) {
  return request.get('/exam/results/my', params)
}
