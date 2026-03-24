import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse, AxiosError } from 'axios'
import { ElMessage } from 'element-plus'
import NProgress from 'nprogress'

// 401错误处理标志位，防止并发请求时重复跳转登录页
let isRedirecting = false

/**
 * 错误消息映射表 - HTTP 状态码
 */
const HTTP_ERROR_MESSAGES: Record<number, string> = {
  400: '请求参数错误，请检查输入内容',
  401: '登录已过期，请重新登录',
  403: '没有权限访问该资源',
  404: '请求的资源不存在',
  405: '请求方法不允许',
  408: '请求超时，请稍后重试',
  409: '资源冲突，请刷新后重试',
  422: '请求参数验证失败',
  429: '请求过于频繁，请稍后重试',
  500: '服务器内部错误，请稍后重试',
  502: '网关错误，请稍后重试',
  503: '服务暂不可用，请稍后重试',
  504: '网关超时，请稍后重试'
}

/**
 * 业务错误码映射表
 */
const BUSINESS_ERROR_MESSAGES: Record<number, string> = {
  40001: '用户名或密码错误',
  40002: '验证码错误或已过期',
  40003: '用户已被禁用',
  40004: '用户不存在',
  40005: '原密码错误',
  40011: '该用户名已被使用',
  40012: '该邮箱已被使用',
  40013: '该手机号已被使用',
  40021: '密码强度不足',
  50001: '数据不存在',
  50002: '数据已存在',
  50003: '数据关联存在，无法删除',
  50004: '状态不允许此操作',
  50011: '课程未发布',
  50012: '考试未开始',
  50013: '考试已结束',
  50014: '考试次数已用尽',
  50015: '考试正在进行中',
  50021: '培训计划未开始',
  50022: '培训计划已结束',
  50023: '未达到学习时长要求',
  60001: '文件上传失败',
  60002: '文件格式不支持',
  60003: '文件大小超出限制',
  60004: '文件不存在'
}

/**
 * 重置401重定向标志位
 * 用于登录成功后重置，允许后续的401错误能够正常处理
 */
export const resetRedirectFlag = () => {
  isRedirecting = false
}

/**
 * 获取友好的错误消息
 * @param error 错误对象
 * @returns 友好的错误消息
 */
function getErrorMessage(error: AxiosError<any>): string {
  // 优先使用后端返回的业务错误消息
  if (error.response?.data?.message) {
    return error.response.data.message
  }
  
  // 业务错误码消息
  const businessCode = error.response?.data?.code
  if (businessCode && BUSINESS_ERROR_MESSAGES[businessCode]) {
    return BUSINESS_ERROR_MESSAGES[businessCode]
  }
  
  // HTTP 状态码消息
  const status = error.response?.status
  if (status && HTTP_ERROR_MESSAGES[status]) {
    return HTTP_ERROR_MESSAGES[status]
  }
  
  // 特殊错误类型
  if (error.code === 'ECONNABORTED' || error.message.includes('timeout')) {
    return '请求超时，请检查网络连接后重试'
  }
  
  if (error.message === 'Network Error') {
    return '网络连接失败，请检查网络设置'
  }
  
  // 默认错误消息
  return error.message || '请求失败，请稍后重试'
}

/**
 * 处理未授权错误
 */
function handleUnauthorized(): void {
  if (!isRedirecting) {
    isRedirecting = true
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    ElMessage.error('登录已过期，请重新登录')
    
    // 跳转登录页并传递当前路径作为重定向目标
    const currentPath = window.location.pathname + window.location.search
    const redirectPath = currentPath !== '/login' ? encodeURIComponent(currentPath) : ''
    window.location.href = redirectPath ? `/login?redirect=${redirectPath}` : '/login'
  }
}

// 创建axios实例
const service: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json;charset=UTF-8'
  }
})

// 请求拦截器
service.interceptors.request.use(
  (config) => {
    NProgress.start()
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    NProgress.done()
    ElMessage.error('请求发送失败')
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  (response: AxiosResponse) => {
    NProgress.done()
    
    // 文件下载响应处理 - 直接返回 Blob 数据
    if (response.config.responseType === 'blob') {
      return response.data
    }
    
    const res = response.data
    
    // 业务状态码判断
    if (res.code !== undefined && res.code !== 200) {
      // 401 未授权
      if (res.code === 401) {
        handleUnauthorized()
        return Promise.reject(new Error('未授权'))
      }
      
      // 获取业务错误消息
      const message = BUSINESS_ERROR_MESSAGES[res.code] || res.message || '操作失败'
      ElMessage.error(message)
      
      return Promise.reject(new Error(message))
    }
    
    return res
  },
  (error: AxiosError<any>) => {
    NProgress.done()
    
    // 处理 HTTP 错误
    if (error.response) {
      const status = error.response.status
      
      // 401 未授权
      if (status === 401) {
        handleUnauthorized()
        return Promise.reject(error)
      }
      
      // 获取并显示友好的错误消息
      const message = getErrorMessage(error)
      ElMessage.error(message)
    } else {
      // 网络错误或请求被取消
      if (error.message === 'Network Error') {
        ElMessage.error('网络连接失败，请检查网络设置')
      } else if (error.code === 'ECONNABORTED') {
        ElMessage.error('请求超时，请稍后重试')
      } else if (error.message !== 'canceled') {
        ElMessage.error('请求失败，请稍后重试')
      }
    }
    
    return Promise.reject(error)
  }
)

// 封装请求方法
const request = {
  get<T>(url: string, params?: object, config?: AxiosRequestConfig): Promise<T> {
    return service.get(url, { params, ...config })
  },
  
  post<T>(url: string, data?: object, config?: AxiosRequestConfig): Promise<T> {
    return service.post(url, data, config)
  },
  
  put<T>(url: string, data?: object, config?: AxiosRequestConfig): Promise<T> {
    return service.put(url, data, config)
  },
  
  delete<T>(url: string, params?: object, config?: AxiosRequestConfig): Promise<T> {
    return service.delete(url, { params, ...config })
  },
  
  /**
   * 下载文件
   * @param url 请求地址
   * @param params 查询参数
   * @param filename 文件名（可选，不传则从响应头获取）
   */
  async download(url: string, params?: object, filename?: string): Promise<void> {
    try {
      const response = await service.get(url, {
        params,
        responseType: 'blob'
      })
      
      // 从响应头获取文件名
      const disposition = response.headers['content-disposition']
      let downloadFilename = filename
      if (!downloadFilename && disposition) {
        const filenameMatch = disposition.match(/filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/)
        if (filenameMatch && filenameMatch[1]) {
          downloadFilename = decodeURIComponent(filenameMatch[1].replace(/['"]/g, ''))
        }
      }
      
      // 创建下载链接
      const blob = new Blob([response.data])
      const link = document.createElement('a')
      link.href = URL.createObjectURL(blob)
      link.download = downloadFilename || 'download'
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      URL.revokeObjectURL(link.href)
    } catch (error) {
      // 错误已在拦截器中处理
      throw error
    }
  },
  
  /**
   * 获取 Blob 响应（用于导出等场景）
   * @param url 请求地址
   * @param params 查询参数
   * @returns Blob 对象
   */
  async getBlob(url: string, params?: object): Promise<Blob> {
    try {
      const response = await service.get(url, {
        params,
        responseType: 'blob'
      })
      return response.data
    } catch (error) {
      // 错误已在拦截器中处理
      throw error
    }
  }
}

export default request
