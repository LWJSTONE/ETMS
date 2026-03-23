<template>
  <div class="training-progress">
    <!-- 搜索区域 -->
    <el-card shadow="never" class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="用户名">
          <el-input
            v-model="searchForm.userName"
            placeholder="请输入用户名"
            clearable
            style="width: 180px"
          />
        </el-form-item>
        <el-form-item label="培训计划">
          <el-select
            v-model="searchForm.planId"
            placeholder="请选择培训计划"
            clearable
            filterable
            style="width: 200px"
          >
            <el-option
              v-for="plan in planList"
              :key="plan.id"
              :label="plan.planName"
              :value="plan.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="进度状态">
          <el-select
            v-model="searchForm.status"
            placeholder="请选择状态"
            clearable
            style="width: 140px"
          >
            <el-option label="未开始" :value="0" />
            <el-option label="进行中" :value="1" />
            <el-option label="已完成" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格区域 -->
    <el-card shadow="never" class="table-card">
      <template #header>
        <div class="card-header">
          <span>学习进度列表</span>
          <el-button type="success" @click="handleExport">
            <el-icon><Download /></el-icon>导出
          </el-button>
        </div>
      </template>

      <el-table
        ref="tableRef"
        :data="tableData"
        v-loading="loading"
        stripe
        border
        @sort-change="handleSortChange"
      >
        <el-table-column prop="userName" label="用户名" min-width="100" show-overflow-tooltip />
        <el-table-column prop="planName" label="培训计划" min-width="150" show-overflow-tooltip />
        <el-table-column prop="courseName" label="课程名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="progress" label="学习进度" width="200" sortable="custom">
          <template #default="{ row }">
            <div class="progress-cell">
              <el-progress
                :percentage="row.progress || 0"
                :status="getProgressStatus(row.progress, row.status)"
                :stroke-width="12"
              />
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="studyTime" label="学习时长" width="120" sortable="custom">
          <template #default="{ row }">
            <span>{{ formatDuration(row.studyTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="完成状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" effect="light">
              {{ getStatusName(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastStudyTime" label="最后学习时间" width="170" sortable="custom">
          <template #default="{ row }">
            <span>{{ row.lastStudyTime || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleViewDetail(row)">
              <el-icon><View /></el-icon>详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="getList"
        @current-change="getList"
      />
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailVisible"
      title="学习进度详情"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="用户名">
          {{ detailData.userName }}
        </el-descriptions-item>
        <el-descriptions-item label="培训计划">
          {{ detailData.planName }}
        </el-descriptions-item>
        <el-descriptions-item label="课程名称" :span="2">
          {{ detailData.courseName }}
        </el-descriptions-item>
        <el-descriptions-item label="学习进度" :span="2">
          <div class="progress-detail">
            <el-progress
              :percentage="detailData.progress || 0"
              :status="getProgressStatus(detailData.progress, detailData.status)"
              :stroke-width="16"
              style="flex: 1"
            />
            <span class="progress-text">{{ detailData.progress || 0 }}%</span>
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="学习时长">
          {{ formatDuration(detailData.studyTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="完成状态">
          <el-tag :type="getStatusType(detailData.status)" effect="light">
            {{ getStatusName(detailData.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="最后学习时间" :span="2">
          {{ detailData.lastStudyTime || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">
          {{ detailData.createTime || '-' }}
        </el-descriptions-item>
      </el-descriptions>

      <div class="study-record" v-if="studyRecords.length > 0">
        <div class="record-title">学习记录</div>
        <el-timeline>
          <el-timeline-item
            v-for="(record, index) in studyRecords"
            :key="index"
            :timestamp="record.time"
            placement="top"
          >
            <el-card shadow="never">
              <div class="record-content">
                <span>学习时长: {{ formatDuration(record.duration) }}</span>
                <span>进度: {{ record.progress }}%</span>
              </div>
            </el-card>
          </el-timeline-item>
        </el-timeline>
      </div>

      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Download, View } from '@element-plus/icons-vue'
import { getProgressList } from '@/api/training'
import { getPlanList } from '@/api/training'

// 定义进度数据类型
interface ProgressItem {
  id: number
  userId: number
  userName: string
  planId: number
  planName: string
  courseId: number
  courseName: string
  progress: number
  studyTime: number
  status: number
  lastStudyTime: string
  createTime: string
}

// 定义学习记录类型
interface StudyRecord {
  time: string
  duration: number
  progress: number
}

// 搜索表单
const searchForm = reactive({
  userName: '',
  planId: null as number | null,
  status: null as number | null
})

// 排序
const sortInfo = reactive({
  prop: '',
  order: ''
})

// 表格数据
const tableData = ref<ProgressItem[]>([])
const loading = ref(false)
const tableRef = ref()

// 分页
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 培训计划列表
const planList = ref<any[]>([])

// 详情对话框
const detailVisible = ref(false)
const detailData = ref<ProgressItem>({} as ProgressItem)
const studyRecords = ref<StudyRecord[]>([])

// 获取状态类型
const getStatusType = (status: number) => {
  const types: Record<number, string> = {
    0: 'info',
    1: 'warning',
    2: 'success'
  }
  return types[status] || 'info'
}

// 获取状态名称
const getStatusName = (status: number) => {
  const names: Record<number, string> = {
    0: '未开始',
    1: '进行中',
    2: '已完成'
  }
  return names[status] || '未知'
}

// 获取进度条状态
const getProgressStatus = (progress: number, status: number) => {
  if (status === 2 || progress >= 100) return 'success'
  if (status === 1 && progress > 0) return undefined
  return undefined
}

// 格式化学习时长
const formatDuration = (minutes: number) => {
  if (!minutes || minutes === 0) return '0分钟'
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

// 获取列表数据
const getList = async () => {
  loading.value = true
  try {
    const params: any = {
      current: pagination.current,
      size: pagination.size,
      ...searchForm
    }
    
    // 添加排序参数
    if (sortInfo.prop && sortInfo.order) {
      params.sortField = sortInfo.prop
      params.sortOrder = sortInfo.order === 'ascending' ? 'asc' : 'desc'
    }
    
    const res = await getProgressList(params)
    tableData.value = res.data?.records || []
    pagination.total = res.data?.total || 0
  } catch (error) {
    console.error('获取进度列表失败:', error)
    ElMessage.error('获取进度列表失败')
  } finally {
    loading.value = false
  }
}

// 获取培训计划列表
const getPlanOptions = async () => {
  try {
    const res = await getPlanList({ current: 1, size: 1000 })
    planList.value = res.data?.records || []
  } catch (error) {
    console.error('获取培训计划列表失败:', error)
  }
}

// 搜索
const handleSearch = () => {
  pagination.current = 1
  getList()
}

// 重置
const handleReset = () => {
  Object.assign(searchForm, {
    userName: '',
    planId: null,
    status: null
  })
  sortInfo.prop = ''
  sortInfo.order = ''
  handleSearch()
}

// 排序变化
const handleSortChange = ({ prop, order }: { prop: string; order: string }) => {
  sortInfo.prop = prop
  sortInfo.order = order
  getList()
}

// 查看详情
const handleViewDetail = (row: ProgressItem) => {
  detailData.value = { ...row }
  
  // 模拟学习记录数据（实际项目中应从后端获取）
  studyRecords.value = generateStudyRecords(row)
  
  detailVisible.value = true
}

// 生成模拟学习记录
const generateStudyRecords = (data: ProgressItem): StudyRecord[] => {
  const records: StudyRecord[] = []
  if (data.status === 0) return records
  
  // 生成最近几条学习记录
  const recordCount = Math.min(5, Math.ceil(data.progress / 20))
  let accumulatedProgress = 0
  let accumulatedDuration = 0
  
  for (let i = 0; i < recordCount; i++) {
    const progressIncrement = Math.min(20, data.progress - accumulatedProgress)
    const durationIncrement = Math.floor(progressIncrement * 1.5 + Math.random() * 10)
    
    accumulatedProgress += progressIncrement
    accumulatedDuration += durationIncrement
    
    // 生成时间（最近的记录往前推）
    const daysAgo = recordCount - i - 1
    const date = new Date()
    date.setDate(date.getDate() - daysAgo)
    date.setHours(9 + Math.floor(Math.random() * 8), Math.floor(Math.random() * 60))
    
    records.push({
      time: date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      }),
      duration: durationIncrement,
      progress: accumulatedProgress
    })
  }
  
  return records.reverse()
}

// 导出
const handleExport = async () => {
  try {
    // 构建导出数据
    const exportData = tableData.value.map(item => ({
      '用户名': item.userName,
      '培训计划': item.planName,
      '课程名称': item.courseName,
      '学习进度': `${item.progress}%`,
      '学习时长': formatDuration(item.studyTime),
      '完成状态': getStatusName(item.status),
      '最后学习时间': item.lastStudyTime || '-'
    }))
    
    // 使用第三方库或原生方式导出Excel
    // 这里使用简单的CSV导出
    const headers = Object.keys(exportData[0] || {})
    const csvContent = [
      headers.join(','),
      ...exportData.map(row => headers.map(h => `"${row[h as keyof typeof row] || ''}"`).join(','))
    ].join('\n')
    
    // 添加BOM以支持中文
    const BOM = '\uFEFF'
    const blob = new Blob([BOM + csvContent], { type: 'text/csv;charset=utf-8;' })
    const link = document.createElement('a')
    const url = URL.createObjectURL(blob)
    link.setAttribute('href', url)
    link.setAttribute('download', `学习进度_${new Date().toLocaleDateString()}.csv`)
    link.style.visibility = 'hidden'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  }
}

// 初始化
onMounted(() => {
  getPlanOptions()
  getList()
})
</script>

<style lang="scss" scoped>
.training-progress {
  .search-card {
    margin-bottom: 20px;
  }

  .table-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .progress-cell {
      padding: 0 10px;
    }

    .el-pagination {
      margin-top: 20px;
      justify-content: flex-end;
    }
  }

  .progress-detail {
    display: flex;
    align-items: center;
    gap: 12px;

    .progress-text {
      font-weight: 600;
      color: #409eff;
    }
  }

  .study-record {
    margin-top: 24px;

    .record-title {
      font-size: 16px;
      font-weight: 600;
      margin-bottom: 16px;
      padding-left: 12px;
      border-left: 3px solid #409eff;
    }

    .record-content {
      display: flex;
      gap: 24px;
      font-size: 14px;
      color: #606266;
    }

    :deep(.el-timeline-item__timestamp) {
      color: #909399;
    }

    :deep(.el-card__body) {
      padding: 12px 16px;
    }
  }
}
</style>
