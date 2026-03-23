import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'
import NProgress from 'nprogress'

// 401错误处理标志位，防止并发请求时重复跳转登录页
let isRedirecting = false

/**
 * 重置401重定向标志位
 * 用于登录成功后重置，允许后续的401错误能够正常处理
 */
export const resetRedirectFlag = () => {
  isRedirecting = false
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
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  (response: AxiosResponse) => {
    NProgress.done()
    const res = response.data
    
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      
      // 401 未授权
      if (res.code === 401) {
        if (!isRedirecting) {
          isRedirecting = true
          localStorage.removeItem('token')
          ElMessage.error('登录已过期，请重新登录')
          // 跳转登录页并传递当前路径作为重定向目标
          const currentPath = window.location.pathname + window.location.search
          const redirectPath = currentPath !== '/login' ? encodeURIComponent(currentPath) : ''
          window.location.href = redirectPath ? `/login?redirect=${redirectPath}` : '/login'
        }
      }
      
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    
    return res
  },
  (error) => {
    NProgress.done()
    
    if (error.response) {
      switch (error.response.status) {
        case 401:
          if (!isRedirecting) {
            isRedirecting = true
            ElMessage.error('未授权，请重新登录')
            localStorage.removeItem('token')
            // 跳转登录页并传递当前路径作为重定向目标
            const currentPath = window.location.pathname + window.location.search
            const redirectPath = currentPath !== '/login' ? encodeURIComponent(currentPath) : ''
            window.location.href = redirectPath ? `/login?redirect=${redirectPath}` : '/login'
          }
          break
        case 403:
          ElMessage.error('拒绝访问')
          break
        case 404:
          ElMessage.error('请求地址不存在')
          break
        case 500:
          ElMessage.error('服务器内部错误')
          break
        default:
          ElMessage.error(error.message || '请求失败')
      }
    } else {
      ElMessage.error('网络错误，请检查网络连接')
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
  }
}

export default request
