<template>
  <span v-html="sanitizedHtml"></span>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import DOMPurify from 'dompurify'

/**
 * 安全的 HTML 渲染组件
 * 使用 DOMPurify 过滤危险标签，防止 XSS 攻击
 */

interface Props {
  html: string
  /** 允许的标签列表 */
  allowedTags?: string[]
  /** 允许的属性列表 */
  allowedAttributes?: string[]
}

const props = withDefaults(defineProps<Props>(), {
  html: '',
  allowedTags: () => [
    'b', 'i', 'u', 'strong', 'em', 'br', 'p', 'span', 'div',
    'h1', 'h2', 'h3', 'h4', 'h5', 'h6',
    'ul', 'ol', 'li', 'dl', 'dt', 'dd',
    'table', 'thead', 'tbody', 'tr', 'th', 'td',
    'a', 'img', 'sub', 'sup', 'code', 'pre', 'blockquote'
  ],
  allowedAttributes: () => [
    'href', 'title', 'target', 'rel',
    'src', 'alt', 'width', 'height',
    'class', 'style'
  ]
})

// 清理 HTML - 在 sanitize 调用时配置，避免在组件级别设置全局配置
const sanitizedHtml = computed(() => {
  if (!props.html) return ''
  return DOMPurify.sanitize(props.html, {
    ALLOWED_TAGS: props.allowedTags,
    ALLOWED_ATTR: props.allowedAttributes,
    ALLOW_DATA_ATTR: false
  })
})
</script>
