/**
 * localStorage 存储键名常量
 * 统一管理所有localStorage键名，避免硬编码
 */
export const STORAGE_KEYS = {
  /** 用户令牌 */
  TOKEN: 'token',
  /** 用户信息 */
  USER_INFO: 'userInfo'
} as const
