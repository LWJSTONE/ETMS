<template>
  <div class="training-report">
    <!-- 搜索区域 -->
    <el-card shadow="never" class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            :shortcuts="dateShortcuts"
            style="width: 260px"
          />
        </el-form-item>
        <el-form-item label="部门">
          <el-select
            v-model="searchForm.deptId"
            placeholder="请选择部门"
            clearable
            filterable
            style="width: 180px"
          >
            <el-option
              v-for="dept in deptList"
              :key="dept.id"
              :label="dept.deptName"
              :value="dept.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="培训类型">
          <el-select
            v-model="searchForm.planType"
            placeholder="请选择培训类型"
            clearable
            style="width: 140px"
          >
            <el-option label="必修" :value="1" />
            <el-option label="选修" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>查询
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>重置
          </el-button>
          <el-button type="success" @click="handleExport">
            <el-icon><Download /></el-icon>导出报表
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 统计概览 -->
    <el-row :gutter="20" class="stats-section">
      <el-col :xs="12" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">
            <el-icon size="28"><Calendar /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-number">{{ overview.planTotal }}</span>
            <span class="stat-title">培训计划总数</span>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);">
            <el-icon size="28"><CircleCheck /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-number">{{ overview.completedTotal }}</span>
            <span class="stat-title">已完成培训</span>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);">
            <el-icon size="28"><Timer /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-number">{{ overview.totalHours }}</span>
            <span class="stat-title">累计培训时长(h)</span>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);">
            <el-icon size="28"><TrendCharts /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-number">{{ overview.completionRate }}%</span>
            <span class="stat-title">培训完成率</span>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="chart-section">
      <el-col :xs="24" :lg="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>培训计划完成情况</span>
              <el-radio-group v-model="planChartType" size="small">
                <el-radio-button value="bar">柱状图</el-radio-button>
                <el-radio-button value="pie">饼图</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div ref="planChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>各部门培训进度</span>
            </div>
          </template>
          <div ref="deptProgressChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-section">
      <el-col :xs="24" :lg="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>培训时长统计</span>
            </div>
          </template>
          <div ref="hoursChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>培训类型分布</span>
            </div>
          </template>
          <div ref="typeChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 数据表格 -->
    <el-card shadow="never" class="table-card">
      <template #header>
        <div class="card-header">
          <span>培训详情数据</span>
        </div>
      </template>
      <el-table :data="tableData" v-loading="loading" stripe border show-summary :summary-method="getSummary">
        <el-table-column prop="planName" label="培训计划名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="deptName" label="所属部门" width="120" />
        <el-table-column prop="planType" label="培训类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.planType === 1 ? 'danger' : 'success'" size="small">
              {{ row.planType === 1 ? '必修' : '选修' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="totalPerson" label="应参加人数" width="100" align="center" />
        <el-table-column prop="completedPerson" label="已完成人数" width="100" align="center" />
        <el-table-column prop="completionRate" label="完成率" width="100" align="center">
          <template #default="{ row }">
            <el-progress
              :percentage="row.completionRate"
              :color="getProgressColor(row.completionRate)"
              :stroke-width="12"
              :text-inside="true"
            />
          </template>
        </el-table-column>
        <el-table-column prop="totalHours" label="培训时长(h)" width="100" align="center" />
        <el-table-column prop="avgScore" label="平均分数" width="100" align="center">
          <template #default="{ row }">
            <span :class="getScoreClass(row.avgScore)">{{ row.avgScore || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusName(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleViewDetail(row)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="getTableList"
        @current-change="getTableList"
      />
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="培训详情"
      width="800px"
      destroy-on-close
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="培训计划名称">{{ currentDetail.planName }}</el-descriptions-item>
        <el-descriptions-item label="所属部门">{{ currentDetail.deptName }}</el-descriptions-item>
        <el-descriptions-item label="培训类型">
          <el-tag :type="currentDetail.planType === 1 ? 'danger' : 'success'" size="small">
            {{ currentDetail.planType === 1 ? '必修' : '选修' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="培训状态">
          <el-tag :type="getStatusType(currentDetail.status)" size="small">
            {{ getStatusName(currentDetail.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="培训时间">{{ currentDetail.startDate }} 至 {{ currentDetail.endDate }}</el-descriptions-item>
        <el-descriptions-item label="培训时长">{{ currentDetail.totalHours }} 小时</el-descriptions-item>
        <el-descriptions-item label="应参加人数">{{ currentDetail.totalPerson }} 人</el-descriptions-item>
        <el-descriptions-item label="已完成人数">{{ currentDetail.completedPerson }} 人</el-descriptions-item>
        <el-descriptions-item label="完成率">
          <el-progress
            :percentage="currentDetail.completionRate"
            :color="getProgressColor(currentDetail.completionRate)"
          />
        </el-descriptions-item>
        <el-descriptions-item label="平均分数">
          <span :class="getScoreClass(currentDetail.avgScore)">{{ currentDetail.avgScore || '-' }}</span>
        </el-descriptions-item>
      </el-descriptions>

      <el-divider content-position="left">参与人员明细</el-divider>
      <el-table :data="currentDetail.participants" max-height="300" stripe border>
        <el-table-column prop="userName" label="姓名" width="100" />
        <el-table-column prop="deptName" label="部门" width="120" />
        <el-table-column prop="progress" label="学习进度" width="120">
          <template #default="{ row }">
            <el-progress
              :percentage="row.progress"
              :color="getProgressColor(row.progress)"
              :stroke-width="10"
            />
          </template>
        </el-table-column>
        <el-table-column prop="studyHours" label="学习时长(h)" width="100" align="center" />
        <el-table-column prop="score" label="考试成绩" width="100" align="center">
          <template #default="{ row }">
            <span :class="getScoreClass(row.score)">{{ row.score || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="completeTime" label="完成时间" width="160" />
        <el-table-column prop="status" label="状态">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'warning'" size="small">
              {{ row.status === 1 ? '已完成' : '进行中' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Download, Calendar, CircleCheck, Timer, TrendCharts } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import dayjs from 'dayjs'
import { getPublicDeptList } from '@/api/dept'
import { getPlanList, getProgressList } from '@/api/training'
import type { TrainingPlan, LearningProgress } from '@/api/types'

// 日期快捷选项
const dateShortcuts = [
  {
    text: '最近一周',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 7)
      return [start, end]
    }
  },
  {
    text: '最近一个月',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 30)
      return [start, end]
    }
  },
  {
    text: '最近三个月',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 90)
      return [start, end]
    }
  },
  {
    text: '最近半年',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 180)
      return [start, end]
    }
  },
  {
    text: '今年',
    value: () => {
      const end = new Date()
      const start = new Date(new Date().getFullYear(), 0, 1)
      return [start, end]
    }
  }
]

// 搜索表单
const searchForm = reactive({
  dateRange: [] as string[],
  deptId: null as number | null,
  planType: null as number | null
})

// 部门列表
const deptList = ref<any[]>([])

// 统计概览
const overview = reactive({
  planTotal: 0,
  completedTotal: 0,
  totalHours: 0,
  completionRate: 0
})

// 表格数据
const tableData = ref<any[]>([])
const loading = ref(false)
const statsLoading = ref(false)
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 原始数据存储（用于统计计算）
const allPlans = ref<TrainingPlan[]>([])
const allProgress = ref<LearningProgress[]>([])

// 图表类型
const planChartType = ref<'bar' | 'pie'>('bar')

// 图表引用
const planChartRef = ref<HTMLElement>()
const deptProgressChartRef = ref<HTMLElement>()
const hoursChartRef = ref<HTMLElement>()
const typeChartRef = ref<HTMLElement>()

// 图表实例
let planChart: echarts.ECharts | null = null
let deptProgressChart: echarts.ECharts | null = null
let hoursChart: echarts.ECharts | null = null
let typeChart: echarts.ECharts | null = null

// 详情对话框
const detailDialogVisible = ref(false)
const currentDetail = ref<any>({})

// 获取部门列表
const getDeptData = async () => {
  try {
    const res = await getPublicDeptList()
    deptList.value = res || []
  } catch (error: any) {
    console.error('获取部门列表失败:', error)
    // 部门列表获取失败不影响主功能，静默处理
  }
}

// 获取统计数据
const getStatistics = async () => {
  statsLoading.value = true
  try {
    // 构建查询参数，支持时间范围筛选
    const params: any = { current: 1, size: 500 }
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      params.startDate = searchForm.dateRange[0]
      params.endDate = searchForm.dateRange[1]
    }
    if (searchForm.deptId !== null) {
      params.deptId = searchForm.deptId
    }
    if (searchForm.planType !== null) {
      params.planType = searchForm.planType
    }
    
    // 获取培训计划数据（限制数量以提高性能）
    const planRes = await getPlanList(params)
    allPlans.value = planRes.data?.records || []
    
    // 如果数据量达到上限，给出提示
    if (allPlans.value.length >= 500) {
      ElMessage.warning('数据量较大，当前仅显示前500条培训计划。建议缩小查询范围获取更精确的统计数据。')
    }
    
    // 获取学习进度数据（限制数量以提高性能）
    // 注意：后端应提供专门的培训统计聚合接口，避免传输大量数据
    const progressParams: any = { current: 1, size: 2000 }
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      progressParams.startDate = searchForm.dateRange[0]
      progressParams.endDate = searchForm.dateRange[1]
    }
    if (searchForm.deptId !== null) {
      progressParams.deptId = searchForm.deptId
    }
    
    const progressRes = await getProgressList(progressParams)
    allProgress.value = progressRes.data?.records || []
    
    // 如果数据量达到上限，给出提示
    if (allProgress.value.length >= 2000) {
      ElMessage.warning('学习进度数据量较大，当前仅显示前2000条记录。建议缩小查询范围获取更精确的统计数据。')
    }
    
    // 计算统计数据
    const stats = calculateStatistics(allPlans.value, allProgress.value)
    
    // 更新概览数据
    Object.assign(overview, stats.overview)

    // 初始化图表
    await nextTick()
    initPlanChart(stats.planStatus)
    initDeptProgressChart(stats.deptProgress)
    initHoursChart(stats.monthlyHours)
    initTypeChart(stats.typeDistribution)
  } catch (error: any) {
    console.error('获取统计数据失败:', error)
    ElMessage.error(error.message || '获取统计数据失败')
  } finally {
    statsLoading.value = false
  }
}

// 计算统计数据
const calculateStatistics = (plans: TrainingPlan[], progressList: LearningProgress[]) => {
  // 概览统计
  const planTotal = plans.length
  const completedPlans = plans.filter(p => p.status === 3 || p.status === 4) // 已结束或已归档
  
  // 计算总培训时长（从学习进度中统计，使用 studyTime 字段）
  const totalHours = Math.round(progressList.reduce((sum, p) => sum + (p.studyTime || 0), 0) / 60) || 0
  
  // 计算完成率
  const completedProgress = progressList.filter(p => p.status === 2) // 已完成状态
  const completionRate = progressList.length > 0 
    ? Math.round((completedProgress.length / progressList.length) * 100) 
    : 0
  
  // 培训计划状态分布
  const statusMap: Record<number, { name: string; value: number }> = {
    0: { name: '草稿', value: 0 },
    1: { name: '已发布', value: 0 },
    2: { name: '进行中', value: 0 },
    3: { name: '已结束', value: 0 },
    4: { name: '已归档', value: 0 }
  }
  plans.forEach(p => {
    if (statusMap[p.status]) {
      statusMap[p.status].value++
    }
  })
  const planStatus = [
    { name: '已完成', value: (statusMap[3].value + statusMap[4].value) },
    { name: '进行中', value: statusMap[2].value },
    { name: '未开始', value: (statusMap[0].value + statusMap[1].value) }
  ].filter(s => s.value > 0)
  
  // 部门培训进度
  const deptMap = new Map<string, { total: number; completed: number }>()
  progressList.forEach(p => {
    const deptName = p.deptName || '未分配部门'
    if (!deptMap.has(deptName)) {
      deptMap.set(deptName, { total: 0, completed: 0 })
    }
    const dept = deptMap.get(deptName)!
    dept.total++
    if (p.status === 2) {
      dept.completed++
    }
  })
  const deptProgress = Array.from(deptMap.entries())
    .map(([deptName, data]) => ({
      deptName,
      total: data.total,
      completed: data.completed,
      rate: data.total > 0 ? Math.round((data.completed / data.total) * 1000) / 10 : 0
    }))
    .sort((a, b) => b.total - a.total)
    .slice(0, 10) // 取前10个部门
  
  // 月度培训时长统计（基于学习进度的创建时间）
  const monthMap = new Map<string, number>()
  for (let i = 1; i <= 12; i++) {
    monthMap.set(`${i}月`, 0)
  }
  progressList.forEach(p => {
    if (p.createTime) {
      const month = dayjs(p.createTime).month() + 1
      const key = `${month}月`
      monthMap.set(key, (monthMap.get(key) || 0) + (p.studyTime || 0))
    }
  })
  const monthlyHours = Array.from(monthMap.entries())
    .map(([month, minutes]) => ({ month, hours: Math.round(minutes / 60) }))
  
  // 培训类型分布
  const typeMap = new Map<string, number>()
  plans.forEach(p => {
    const typeName = p.planType === 1 ? '必修培训' : '选修培训'
    typeMap.set(typeName, (typeMap.get(typeName) || 0) + 1)
  })
  const typeDistribution = Array.from(typeMap.entries())
    .map(([name, value]) => ({ name, value }))
    .sort((a, b) => b.value - a.value)
  
  // 如果没有数据，添加默认分类
  if (typeDistribution.length === 0) {
    typeDistribution.push({ name: '暂无数据', value: 1 })
  }
  
  return {
    overview: {
      planTotal,
      completedTotal: completedPlans.length,
      totalHours,
      completionRate
    },
    planStatus: planStatus.length > 0 ? planStatus : [{ name: '暂无数据', value: 1 }],
    deptProgress: deptProgress.length > 0 ? deptProgress : [{ deptName: '暂无数据', total: 1, completed: 0, rate: 0 }],
    monthlyHours,
    typeDistribution
  }
}

// 获取表格数据
const getTableList = async () => {
  loading.value = true
  try {
    // 构建查询参数
    const params: any = {
      current: pagination.current,
      size: pagination.size
    }
    
    // 添加日期范围筛选
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      params.startDate = searchForm.dateRange[0]
      params.endDate = searchForm.dateRange[1]
    }
    
    // 添加部门筛选
    if (searchForm.deptId !== null) {
      params.deptId = searchForm.deptId
    }
    
    // 添加培训类型筛选
    if (searchForm.planType !== null) {
      params.planType = searchForm.planType
    }
    
    // 调用API获取培训计划列表
    const planRes = await getPlanList(params)
    const plans = planRes.data?.records || []
    pagination.total = planRes.data?.total || 0
    
    // 如果当前页没有数据，不再请求学习进度
    if (plans.length === 0) {
      tableData.value = []
      return
    }
    
    // 获取当前页培训计划的学习进度数据
    // 限制查询数量以提高性能
    const planIds = plans.map((p: TrainingPlan) => p.id)
    const progressParams: any = {
      current: 1,
      size: 1000, // 限制每次最多获取1000条进度记录
      planIds: planIds.join(',')
    }
    
    // 添加筛选条件
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      progressParams.startDate = searchForm.dateRange[0]
      progressParams.endDate = searchForm.dateRange[1]
    }
    if (searchForm.deptId !== null) {
      progressParams.deptId = searchForm.deptId
    }
    
    const progressRes = await getProgressList(progressParams)
    const allProgressData = progressRes.data?.records || []
    
    // 为每个培训计划计算统计数据
    tableData.value = plans.map((plan: TrainingPlan) => {
      // 筛选该培训计划的学习进度
      const planProgress = allProgressData.filter((p: LearningProgress) => p.planId === plan.id)
      
      // 计算统计数据
      const totalPerson = planProgress.length
      const completedPerson = planProgress.filter((p: LearningProgress) => p.status === 2).length
      const completionRate = totalPerson > 0 ? Math.round((completedPerson / totalPerson) * 100) : 0
      const totalHours = Math.round(planProgress.reduce((sum: number, p: LearningProgress) => sum + (p.studyTime || 0), 0) / 60)
      
      // 计算平均分数（如果有考试成绩的话）
      const scores = planProgress.filter((p: any) => p.score !== null && p.score !== undefined).map((p: any) => p.score)
      const avgScore = scores.length > 0 ? Math.round(scores.reduce((a: number, b: number) => a + b, 0) / scores.length * 10) / 10 : null
      
      // 获取部门名称（从第一个学习进度记录中获取，或者使用空字符串）
      const deptName = planProgress.length > 0 ? (planProgress[0] as LearningProgress).deptName || '-' : '-'
      
      return {
        id: plan.id,
        planName: plan.planName,
        deptName,
        planType: plan.planType,
        totalPerson,
        completedPerson,
        completionRate,
        totalHours,
        avgScore,
        status: plan.status,
        startDate: plan.startDate,
        endDate: plan.endDate
      }
    })
  } catch (error: any) {
    console.error('获取表格数据失败:', error)
    ElMessage.error(error.message || '获取培训数据失败')
  } finally {
    loading.value = false
  }
}

// 初始化培训计划完成情况图表
const initPlanChart = (data: any[]) => {
  if (!planChartRef.value) return
  
  if (!planChart) {
    planChart = echarts.init(planChartRef.value)
  }
  
  let option: echarts.EChartsOption = {}
  
  if (planChartType.value === 'bar') {
    option = {
      tooltip: {
        trigger: 'axis',
        axisPointer: { type: 'shadow' }
      },
      legend: {
        data: ['数量'],
        bottom: 0
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '15%',
        top: '10%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        data: data.map(item => item.name),
        axisLabel: { color: '#666' }
      },
      yAxis: {
        type: 'value',
        axisLabel: { color: '#666' }
      },
      series: [
        {
          name: '数量',
          type: 'bar',
          barWidth: '50%',
          data: data.map((item, index) => ({
            value: item.value,
            itemStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: index === 0 ? '#67c23a' : index === 1 ? '#409eff' : '#e6a23c' },
                { offset: 1, color: index === 0 ? '#95d475' : index === 1 ? '#79bbff' : '#eebe77' }
              ])
            }
          })),
          label: {
            show: true,
            position: 'top',
            formatter: '{c}个'
          }
        }
      ]
    }
  } else {
    option = {
      tooltip: {
        trigger: 'item',
        formatter: '{b}: {c}个 ({d}%)'
      },
      legend: {
        orient: 'vertical',
        left: 'left',
        top: 'center'
      },
      series: [
        {
          name: '培训状态',
          type: 'pie',
          radius: ['40%', '70%'],
          center: ['60%', '50%'],
          avoidLabelOverlap: false,
          itemStyle: {
            borderRadius: 10,
            borderColor: '#fff',
            borderWidth: 2
          },
          label: {
            show: true,
            formatter: '{b}\n{d}%'
          },
          emphasis: {
            label: {
              show: true,
              fontSize: 14,
              fontWeight: 'bold'
            }
          },
          data: data.map((item, index) => ({
            value: item.value,
            name: item.name,
            itemStyle: {
              color: index === 0 ? '#67c23a' : index === 1 ? '#409eff' : '#e6a23c'
            }
          }))
        }
      ]
    }
  }
  
  planChart.setOption(option)
}

// 初始化部门培训进度图表
const initDeptProgressChart = (data: any[]) => {
  if (!deptProgressChartRef.value) return
  
  if (!deptProgressChart) {
    deptProgressChart = echarts.init(deptProgressChartRef.value)
  }
  
  const option: echarts.EChartsOption = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter: (params: any) => {
        const data = params[0]
        return `${data.name}<br/>计划数: ${data.value}个<br/>完成数: ${params[1].value}个`
      }
    },
    legend: {
      data: ['计划数', '完成数'],
      bottom: 0
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '15%',
      top: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: data.map(item => item.deptName),
      axisLabel: {
        color: '#666',
        rotate: 30
      }
    },
    yAxis: {
      type: 'value',
      axisLabel: { color: '#666' }
    },
    series: [
      {
        name: '计划数',
        type: 'bar',
        barGap: '0%',
        data: data.map(item => item.total),
        itemStyle: { color: '#409eff' }
      },
      {
        name: '完成数',
        type: 'bar',
        data: data.map(item => item.completed),
        itemStyle: { color: '#67c23a' }
      }
    ]
  }
  
  deptProgressChart.setOption(option)
}

// 初始化培训时长图表
const initHoursChart = (data: any[]) => {
  if (!hoursChartRef.value) return
  
  if (!hoursChart) {
    hoursChart = echarts.init(hoursChartRef.value)
  }
  
  const option: echarts.EChartsOption = {
    tooltip: {
      trigger: 'axis',
      formatter: '{b}<br/>培训时长: {c}小时'
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: data.map(item => item.month),
      axisLabel: { color: '#666' }
    },
    yAxis: {
      type: 'value',
      axisLabel: { color: '#666', formatter: '{value}h' }
    },
    series: [
      {
        name: '培训时长',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 8,
        data: data.map(item => item.hours),
        lineStyle: {
          width: 3,
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: '#667eea' },
            { offset: 1, color: '#764ba2' }
          ])
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(102, 126, 234, 0.4)' },
            { offset: 1, color: 'rgba(102, 126, 234, 0.05)' }
          ])
        },
        itemStyle: { color: '#667eea' }
      }
    ]
  }
  
  hoursChart.setOption(option)
}

// 初始化培训类型分布图表
const initTypeChart = (data: any[]) => {
  if (!typeChartRef.value) return
  
  if (!typeChart) {
    typeChart = echarts.init(typeChartRef.value)
  }
  
  const colors = ['#409eff', '#67c23a', '#e6a23c', '#f56c6c', '#909399']
  
  const option: echarts.EChartsOption = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c}个 ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left',
      top: 'center'
    },
    series: [
      {
        name: '培训类型',
        type: 'pie',
        radius: ['35%', '65%'],
        center: ['60%', '50%'],
        roseType: 'radius',
        itemStyle: {
          borderRadius: 5,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: true,
          formatter: '{b}\n{d}%'
        },
        data: data.map((item, index) => ({
          value: item.value,
          name: item.name,
          itemStyle: { color: colors[index % colors.length] }
        }))
      }
    ]
  }
  
  typeChart.setOption(option)
}

// 监听图表类型变化 - 只需要重新渲染图表，不需要重新获取数据
watch(planChartType, () => {
  // 计算统计数据
  const stats = calculateStatistics(allPlans.value, allProgress.value)
  // 只更新计划完成情况图表
  initPlanChart(stats.planStatus)
})

// 获取状态类型
const getStatusType = (status: number) => {
  const types: Record<number, string> = {
    0: 'info',
    1: 'success',
    2: 'primary',
    3: 'warning',
    4: ''
  }
  return types[status] || 'info'
}

// 获取状态名称
const getStatusName = (status: number) => {
  const names: Record<number, string> = {
    0: '草稿',
    1: '已发布',
    2: '进行中',
    3: '已结束',
    4: '已归档'
  }
  return names[status] || '未知'
}

// 获取进度颜色
const getProgressColor = (percentage: number) => {
  if (percentage >= 80) return '#67c23a'
  if (percentage >= 60) return '#e6a23c'
  return '#f56c6c'
}

// 获取分数样式
const getScoreClass = (score: number | null) => {
  if (!score) return ''
  if (score >= 90) return 'score-excellent'
  if (score >= 80) return 'score-good'
  if (score >= 60) return 'score-pass'
  return 'score-fail'
}

// 获取合计
const getSummary = (param: any) => {
  const { columns, data } = param
  const sums: string[] = []
  columns.forEach((column: any, index: number) => {
    if (index === 0) {
      sums[index] = '合计'
      return
    }
    if (['totalPerson', 'completedPerson', 'totalHours'].includes(column.property)) {
      const values = data.map((item: any) => Number(item[column.property]))
      sums[index] = values.reduce((prev: number, curr: number) => {
        const value = Number(curr)
        if (!isNaN(value)) {
          return prev + curr
        }
        return prev
      }, 0).toString()
    } else if (column.property === 'completionRate') {
      const totalPerson = data.reduce((prev: number, item: any) => prev + Number(item.totalPerson), 0)
      const completedPerson = data.reduce((prev: number, item: any) => prev + Number(item.completedPerson), 0)
      const rate = totalPerson > 0 ? Math.round((completedPerson / totalPerson) * 100) : 0
      sums[index] = `${rate}%`
    } else {
      sums[index] = ''
    }
  })
  return sums
}

// 查看详情
const handleViewDetail = async (row: any) => {
  try {
    // 获取该培训计划的学习进度（参与者）数据
    const res = await getProgressList({ 
      planId: row.id,
      current: 1, 
      size: 1000 
    })
    const participants = (res.records || []).map((p: LearningProgress) => ({
      userName: p.realName || p.userName,
      deptName: p.deptName || '-',
      progress: p.progress || 0,
      studyHours: p.studyTime ? Math.round(p.studyTime / 60 * 10) / 10 : 0, // 转换为小时
      score: (p as any).score || null,
      completeTime: p.completeTime ? dayjs(p.completeTime).format('YYYY-MM-DD HH:mm') : null,
      status: p.status === 2 ? 1 : 0 // 2=已完成转为1，其他转为0
    }))
    
    currentDetail.value = {
      ...row,
      participants: participants.length > 0 ? participants : []
    }
    detailDialogVisible.value = true
  } catch (error: any) {
    console.error('获取培训详情失败:', error)
    ElMessage.error(error.message || '获取培训详情失败')
  }
}

// 搜索
const handleSearch = () => {
  pagination.current = 1
  getStatistics()
  getTableList()
}

// 重置
const handleReset = () => {
  Object.assign(searchForm, {
    dateRange: [],
    deptId: null,
    planType: null
  })
  handleSearch()
}

// 导出报表
const handleExport = () => {
  if (tableData.value.length === 0) {
    ElMessage.warning('暂无数据可导出')
    return
  }
  
  try {
    // 构建CSV内容
    const headers = ['培训计划名称', '所属部门', '培训类型', '应参加人数', '已完成人数', '完成率(%)', '培训时长(h)', '平均分数', '状态', '开始日期', '结束日期']
    const rows = tableData.value.map((row: any) => [
      row.planName,
      row.deptName,
      row.planType === 1 ? '必修' : '选修',
      row.totalPerson,
      row.completedPerson,
      row.completionRate,
      row.totalHours,
      row.avgScore || '-',
      getStatusName(row.status),
      row.startDate,
      row.endDate
    ])
    
    // 添加BOM以支持中文
    const BOM = '\uFEFF'
    const csvContent = BOM + [headers, ...rows].map((row: string[]) => row.map((cell: string) => `"${cell}"`).join(',')).join('\n')
    
    // 创建Blob并下载
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
    const link = document.createElement('a')
    const url = URL.createObjectURL(blob)
    link.setAttribute('href', url)
    link.setAttribute('download', `培训报表_${dayjs().format('YYYY-MM-DD_HHmmss')}.csv`)
    link.style.visibility = 'hidden'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(url)
    
    ElMessage.success('报表导出成功')
  } catch (error: any) {
    console.error('导出报表失败:', error)
    ElMessage.error(error.message || '导出报表失败')
  }
}

// 窗口大小变化时重新渲染图表
const handleResize = () => {
  planChart?.resize()
  deptProgressChart?.resize()
  hoursChart?.resize()
  typeChart?.resize()
}

onMounted(() => {
  getDeptData()
  getStatistics()
  getTableList()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  planChart?.dispose()
  deptProgressChart?.dispose()
  hoursChart?.dispose()
  typeChart?.dispose()
})
</script>

<style lang="scss" scoped>
.training-report {
  .search-card {
    margin-bottom: 20px;
  }

  .stats-section {
    margin-bottom: 20px;

    .stat-card {
      :deep(.el-card__body) {
        display: flex;
        align-items: center;
        padding: 20px;
      }

      .stat-icon {
        width: 56px;
        height: 56px;
        border-radius: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
        color: #fff;
        margin-right: 15px;
      }

      .stat-info {
        display: flex;
        flex-direction: column;

        .stat-number {
          font-size: 26px;
          font-weight: bold;
          color: #333;
        }

        .stat-title {
          font-size: 13px;
          color: #999;
          margin-top: 4px;
        }
      }
    }
  }

  .chart-section {
    margin-bottom: 20px;

    .chart-container {
      height: 320px;
    }
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .table-card {
    .el-pagination {
      margin-top: 20px;
      justify-content: flex-end;
    }
  }

  .score-excellent {
    color: #67c23a;
    font-weight: bold;
  }

  .score-good {
    color: #409eff;
    font-weight: bold;
  }

  .score-pass {
    color: #e6a23c;
  }

  .score-fail {
    color: #f56c6c;
  }
}
</style>
