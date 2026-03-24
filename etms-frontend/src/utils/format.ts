/**
 * 公共格式化工具函数
 * 统一管理重复的工具函数，提高代码复用性
 */

/**
 * 日期时间格式化
 * @param dateTime 日期时间字符串
 * @param defaultValue 默认值，默认为 '-'
 * @returns 格式化后的日期时间字符串 (YYYY-MM-DD HH:mm)
 */
export function formatDateTime(dateTime: string | null | undefined, defaultValue = '-'): string {
  if (!dateTime) return defaultValue
  return dateTime.replace('T', ' ').substring(0, 16)
}

/**
 * 时长格式化
 * @param minutes 分钟数
 * @param defaultValue 默认值，默认为 '-'
 * @returns 格式化后的时长字符串 (X小时Y分钟)
 */
export function formatDuration(minutes: number | null | undefined, defaultValue = '-'): string {
  if (minutes === null || minutes === undefined) return defaultValue
  if (minutes === 0) return '0分钟'
  
  const hours = Math.floor(minutes / 60)
  const mins = minutes % 60
  
  if (hours > 0 && mins > 0) {
    return `${hours}小时${mins}分钟`
  } else if (hours > 0) {
    return `${hours}小时`
  } else {
    return `${mins}分钟`
  }
}

/**
 * 获取状态类型 (Element Plus Tag 组件的 type 属性)
 * @param status 状态值
 * @param type 状态类型: 'progress' | 'exam' | 'audit' | 'attendance' | 'common'
 * @returns Tag 组件的 type 属性值
 */
export function getStatusType(
  status: number, 
  type: 'progress' | 'exam' | 'audit' | 'attendance' | 'common' = 'progress'
): string {
  const typeMaps = {
    // 学习进度状态: 0-未开始, 1-进行中, 2-已完成
    progress: {
      0: 'info',
      1: 'warning',
      2: 'success'
    },
    // 考试状态: 0-未开始, 1-进行中, 2-已结束, 3-已批阅
    exam: {
      0: 'info',
      1: 'primary',
      2: 'warning',
      3: 'success'
    },
    // 审核状态: 0-待审核, 1-已通过, 2-已拒绝
    audit: {
      0: 'warning',
      1: 'success',
      2: 'danger'
    },
    // 签到状态: 0-正常, 1-迟到, 2-早退, 3-缺勤
    attendance: {
      0: 'success',
      1: 'warning',
      2: 'warning',
      3: 'danger'
    },
    // 通用状态: 0-禁用, 1-正常
    common: {
      0: 'danger',
      1: 'success'
    }
  }
  
  const map = typeMaps[type]
  return map[status as keyof typeof map] || 'info'
}

/**
 * 获取进度颜色
 * @param percentage 进度百分比
 * @param status 可选的状态值，用于判断是否完成
 * @returns 颜色值
 */
export function getProgressColor(percentage: number, status?: number): string {
  // 如果已完成，返回成功色
  if (status === 2 || percentage >= 100) return '#67c23a'
  // 进度较高
  if (percentage >= 80) return '#409eff'
  // 进度中等
  if (percentage >= 50) return '#e6a23c'
  // 进度较低
  if (percentage >= 20) return '#909399'
  // 未开始
  return '#c0c4cc'
}

/**
 * 获取状态名称
 * @param status 状态值
 * @param type 状态类型
 * @returns 状态名称
 */
export function getStatusName(
  status: number,
  type: 'progress' | 'exam' | 'audit' | 'attendance' | 'common' = 'progress'
): string {
  const nameMaps = {
    // 学习进度状态
    progress: {
      0: '未开始',
      1: '进行中',
      2: '已完成'
    },
    // 考试状态
    exam: {
      0: '未开始',
      1: '进行中',
      2: '已结束',
      3: '已批阅'
    },
    // 审核状态
    audit: {
      0: '待审核',
      1: '已通过',
      2: '已拒绝'
    },
    // 签到状态
    attendance: {
      0: '正常',
      1: '迟到',
      2: '早退',
      3: '缺勤'
    },
    // 通用状态
    common: {
      0: '禁用',
      1: '正常'
    }
  }
  
  const map = nameMaps[type]
  return map[status as keyof typeof map] || '未知'
}

/**
 * 格式化文件大小
 * @param bytes 字节数
 * @returns 格式化后的文件大小字符串
 */
export function formatFileSize(bytes: number): string {
  if (bytes === 0) return '0 B'
  
  const units = ['B', 'KB', 'MB', 'GB', 'TB']
  const k = 1024
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + units[i]
}

/**
 * 格式化百分比
 * @param value 数值
 * @param decimals 小数位数，默认为 0
 * @returns 格式化后的百分比字符串
 */
export function formatPercentage(value: number, decimals = 0): string {
  if (value === null || value === undefined) return '0%'
  return value.toFixed(decimals) + '%'
}
