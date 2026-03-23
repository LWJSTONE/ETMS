<template>
  <div class="exam-report">
    <!-- 筛选区域 -->
    <el-card shadow="never" class="filter-card">
      <el-form :model="filterForm" inline>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="filterForm.dateRange"
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
            v-model="filterForm.deptId"
            placeholder="全部部门"
            clearable
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
        <el-form-item label="试卷">
          <el-select
            v-model="filterForm.paperId"
            placeholder="全部试卷"
            clearable
            filterable
            style="width: 200px"
          >
            <el-option
              v-for="paper in paperList"
              :key="paper.id"
              :label="paper.paperName"
              :value="paper.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">
            <el-icon><Search /></el-icon>查询
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :xs="12" :sm="12" :md="6">
        <el-card shadow="hover" class="stats-card">
          <div class="stats-item">
            <div class="stats-icon total">
              <el-icon><Document /></el-icon>
            </div>
            <div class="stats-info">
              <div class="stats-value">{{ reportData.totalCount }}</div>
              <div class="stats-label">考试总人次</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6">
        <el-card shadow="hover" class="stats-card">
          <div class="stats-item">
            <div class="stats-icon pass">
              <el-icon><CircleCheck /></el-icon>
            </div>
            <div class="stats-info">
              <div class="stats-value">{{ reportData.passCount }}</div>
              <div class="stats-label">通过人次</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6">
        <el-card shadow="hover" class="stats-card">
          <div class="stats-item">
            <div class="stats-icon fail">
              <el-icon><CircleClose /></el-icon>
            </div>
            <div class="stats-info">
              <div class="stats-value">{{ reportData.failCount }}</div>
              <div class="stats-label">未通过人次</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6">
        <el-card shadow="hover" class="stats-card">
          <div class="stats-item">
            <div class="stats-icon rate">
              <el-icon><TrendCharts /></el-icon>
            </div>
            <div class="stats-info">
              <div class="stats-value">{{ reportData.passRate }}%</div>
              <div class="stats-label">通过率</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :xs="24" :md="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>
                <el-icon><PieChart /></el-icon>
                考试通过率统计
              </span>
            </div>
          </template>
          <div ref="passRateChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>
                <el-icon><Histogram /></el-icon>
                各试卷成绩分布
              </span>
            </div>
          </template>
          <div ref="scoreDistChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 各试卷成绩分布详细表格 -->
    <el-card shadow="never" class="table-card">
      <template #header>
        <div class="card-header">
          <span>
            <el-icon><DataLine /></el-icon>
            各试卷成绩分布详情
          </span>
          <el-button type="success" @click="handleExport" :loading="exportLoading">
            <el-icon><Download /></el-icon>导出报表
          </el-button>
        </div>
      </template>
      <el-table :data="paperScoreData" v-loading="loading" stripe border>
        <el-table-column prop="paperName" label="试卷名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="totalExams" label="考试人次" width="100" align="center" />
        <el-table-column prop="avgScore" label="平均分" width="90" align="center">
          <template #default="{ row }">
            <span class="score-avg">{{ row.avgScore }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="maxScore" label="最高分" width="90" align="center">
          <template #default="{ row }">
            <span class="score-max">{{ row.maxScore }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="minScore" label="最低分" width="90" align="center">
          <template #default="{ row }">
            <span class="score-min">{{ row.minScore }}</span>
          </template>
        </el-table-column>
        <el-table-column label="成绩分布" min-width="300">
          <template #default="{ row }">
            <div class="score-distribution">
              <el-tooltip content="优秀(90-100分)" placement="top">
                <span class="dist-item excellent">{{ row.excellentCount || 0 }}</span>
              </el-tooltip>
              <el-tooltip content="良好(80-89分)" placement="top">
                <span class="dist-item good">{{ row.goodCount || 0 }}</span>
              </el-tooltip>
              <el-tooltip content="中等(70-79分)" placement="top">
                <span class="dist-item medium">{{ row.mediumCount || 0 }}</span>
              </el-tooltip>
              <el-tooltip content="及格(60-69分)" placement="top">
                <span class="dist-item pass">{{ row.passCount || 0 }}</span>
              </el-tooltip>
              <el-tooltip content="不及格(0-59分)" placement="top">
                <span class="dist-item fail">{{ row.failCount || 0 }}</span>
              </el-tooltip>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="passRate" label="通过率" width="100" align="center">
          <template #default="{ row }">
            <el-progress
              :percentage="row.passRate"
              :color="getProgressColor(row.passRate)"
              :stroke-width="12"
              :text-inside="true"
            />
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 各部门考试情况统计 -->
    <el-card shadow="never" class="table-card">
      <template #header>
        <div class="card-header">
          <span>
            <el-icon><OfficeBuilding /></el-icon>
            各部门考试情况统计
          </span>
        </div>
      </template>
      <el-table :data="deptExamData" v-loading="loading" stripe border>
        <el-table-column prop="deptName" label="部门名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="totalUsers" label="部门人数" width="100" align="center" />
        <el-table-column prop="examUsers" label="参考人数" width="100" align="center" />
        <el-table-column prop="examRate" label="参考率" width="120" align="center">
          <template #default="{ row }">
            <el-progress
              :percentage="row.examRate"
              :color="getProgressColor(row.examRate)"
              :stroke-width="12"
              :text-inside="true"
            />
          </template>
        </el-table-column>
        <el-table-column prop="totalExams" label="考试人次" width="100" align="center" />
        <el-table-column prop="passCount" label="通过人次" width="100" align="center">
          <template #default="{ row }">
            <span class="pass-text">{{ row.passCount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="failCount" label="未通过人次" width="110" align="center">
          <template #default="{ row }">
            <span class="fail-text">{{ row.failCount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="avgScore" label="平均分" width="90" align="center">
          <template #default="{ row }">
            <span :class="getScoreClass(row.avgScore)">{{ row.avgScore }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="passRate" label="通过率" width="120" align="center">
          <template #default="{ row }">
            <el-progress
              :percentage="row.passRate"
              :color="getProgressColor(row.passRate)"
              :stroke-width="12"
              :text-inside="true"
            />
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 考试趋势图表 -->
    <el-card shadow="never" class="chart-card">
      <template #header>
        <div class="card-header">
          <span>
            <el-icon><DataLine /></el-icon>
            考试趋势分析
          </span>
          <el-radio-group v-model="trendType" size="small" @change="initTrendChart">
            <el-radio-button label="day">按日</el-radio-button>
            <el-radio-button label="week">按周</el-radio-button>
            <el-radio-button label="month">按月</el-radio-button>
          </el-radio-group>
        </div>
      </template>
      <div ref="trendChartRef" class="chart-container-lg"></div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Search,
  Refresh,
  Document,
  CircleCheck,
  CircleClose,
  TrendCharts,
  Download,
  PieChart,
  DataLine,
  OfficeBuilding
} from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import dayjs from 'dayjs'
import { getDeptTree } from '@/api/dept'
import { getPaperList, getResultList, getResultStats } from '@/api/exam'

// Histogram 图标使用 DataLine 代替
const Histogram = DataLine

// 筛选表单
const filterForm = reactive({
  dateRange: [] as string[],
  deptId: null as number | null,
  paperId: null as number | null
})

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
    text: '最近一月',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 30)
      return [start, end]
    }
  },
  {
    text: '最近三月',
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
    text: '最近一年',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 365)
      return [start, end]
    }
  }
]

// 数据列表
const deptList = ref<any[]>([])
const paperList = ref<any[]>([])

// 报表数据
const reportData = reactive({
  totalCount: 0,
  passCount: 0,
  failCount: 0,
  passRate: '0.0',
  avgScore: '0.0'
})

// 各试卷成绩分布数据
const paperScoreData = ref<any[]>([])

// 各部门考试数据
const deptExamData = ref<any[]>([])

// 所有考试记录（用于趋势图计算）
const allExamRecords = ref<any[]>([])

// 考试趋势类型
const trendType = ref<'day' | 'week' | 'month'>('day')

// 加载状态
const loading = ref(false)
const exportLoading = ref(false)

// 图表引用
const passRateChartRef = ref<HTMLElement>()
const scoreDistChartRef = ref<HTMLElement>()
const trendChartRef = ref<HTMLElement>()

// 图表实例
let passRateChart: echarts.ECharts | null = null
let scoreDistChart: echarts.ECharts | null = null
let trendChart: echarts.ECharts | null = null

// 获取部门列表
const getDeptList = async () => {
  try {
    const res = await getDeptTree()
    const flattenDepts = (depts: any[]): any[] => {
      return depts.reduce((acc, dept) => {
        acc.push({ id: dept.id, deptName: dept.deptName })
        if (dept.children && dept.children.length > 0) {
          acc.push(...flattenDepts(dept.children))
        }
        return acc
      }, [] as any[])
    }
    deptList.value = flattenDepts(res.data || [])
  } catch (error) {
    console.error('获取部门列表失败:', error)
  }
}

// 获取试卷列表
const getPaperListData = async () => {
  try {
    const res = await getPaperList({ current: 1, size: 1000 })
    paperList.value = res.data?.records || []
  } catch (error) {
    console.error('获取试卷列表失败:', error)
  }
}

// 获取报表数据
const getReportData = async () => {
  loading.value = true
  try {
    // 注意：此处使用较大size值获取全量数据进行前端统计计算
    // 性能优化建议：后端应提供专门的统计聚合接口，避免传输大量数据
    // 当前保留此方式是因为需要计算各试卷、各部门的详细统计数据
    const params: any = {
      current: 1,
      size: 10000
    }
    
    if (filterForm.dateRange && filterForm.dateRange.length === 2) {
      params.examStartTime = filterForm.dateRange[0]
      params.examEndTime = filterForm.dateRange[1]
    }
    if (filterForm.deptId) {
      params.deptId = filterForm.deptId
    }
    if (filterForm.paperId) {
      params.paperId = filterForm.paperId
    }

    const res = await getResultList(params)
    const records = res.data?.records || []
    
    // 保存考试记录用于趋势计算
    allExamRecords.value = records
    
    // 计算汇总统计
    calculateSummary(records)
    
    // 计算各试卷成绩分布
    calculatePaperScoreDistribution(records)
    
    // 计算各部门考试情况
    calculateDeptExamStats(records)
    
    // 初始化图表
    initPassRateChart()
    initScoreDistChart()
    initTrendChart()
    
  } catch (error) {
    console.error('获取报表数据失败:', error)
    ElMessage.error('获取报表数据失败')
  } finally {
    loading.value = false
  }
}

// 计算汇总统计
const calculateSummary = (records: any[]) => {
  reportData.totalCount = records.length
  reportData.passCount = records.filter(r => r.passed === 1).length
  reportData.failCount = records.filter(r => r.passed !== 1).length
  reportData.passRate = records.length > 0 
    ? ((reportData.passCount / records.length) * 100).toFixed(1) 
    : '0.0'
  
  const totalScore = records.reduce((sum, r) => sum + (r.userScore || 0), 0)
  reportData.avgScore = records.length > 0 
    ? (totalScore / records.length).toFixed(1) 
    : '0.0'
}

// 计算各试卷成绩分布
const calculatePaperScoreDistribution = (records: any[]) => {
  const paperMap = new Map<number, any>()
  
  records.forEach(record => {
    const paperId = record.paperId
    if (!paperMap.has(paperId)) {
      paperMap.set(paperId, {
        paperId,
        paperName: record.paperName,
        scores: [],
        totalExams: 0,
        passCount: 0,
        examRecords: []
      })
    }
    const paper = paperMap.get(paperId)
    paper.scores.push(record.userScore || 0)
    paper.totalExams++
    if (record.passed === 1) paper.passCount++
    paper.examRecords.push(record)
  })
  
  paperScoreData.value = Array.from(paperMap.values()).map(paper => {
    const scores = paper.scores
    const avgScore = scores.length > 0 
      ? (scores.reduce((a: number, b: number) => a + b, 0) / scores.length).toFixed(1) 
      : '0'
    
    // 添加空数组检查，避免 Math.max/min 返回 -Infinity/Infinity
    const maxScore = scores.length > 0 ? Math.max(...scores) : 0
    const minScore = scores.length > 0 ? Math.min(...scores) : 0
    
    return {
      paperName: paper.paperName,
      totalExams: paper.totalExams,
      avgScore,
      maxScore,
      minScore,
      excellentCount: scores.filter((s: number) => s >= 90).length,
      goodCount: scores.filter((s: number) => s >= 80 && s < 90).length,
      mediumCount: scores.filter((s: number) => s >= 70 && s < 80).length,
      passCount: scores.filter((s: number) => s >= 60 && s < 70).length,
      failCount: scores.filter((s: number) => s < 60).length,
      passRate: scores.length > 0 
        ? Math.round((paper.passCount / paper.totalExams) * 100) 
        : 0
    }
  })
}

// 计算各部门考试情况
const calculateDeptExamStats = (records: any[]) => {
  const deptMap = new Map<number, any>()
  
  // 初始化部门数据
  deptList.value.forEach(dept => {
    deptMap.set(dept.id, {
      deptId: dept.id,
      deptName: dept.deptName,
      totalUsers: 0,
      examUsers: new Set(),
      totalExams: 0,
      passCount: 0,
      failCount: 0,
      totalScore: 0
    })
  })
  
  // 统计考试数据
  records.forEach(record => {
    const deptId = record.deptId
    if (deptMap.has(deptId)) {
      const dept = deptMap.get(deptId)
      dept.examUsers.add(record.userId)
      dept.totalExams++
      if (record.passed === 1) {
        dept.passCount++
      } else {
        dept.failCount++
      }
      dept.totalScore += record.userScore || 0
    }
  })
  
  deptExamData.value = Array.from(deptMap.values())
    .filter(dept => dept.totalExams > 0)
    .map(dept => {
      const examUserCount = dept.examUsers.size
      return {
        deptName: dept.deptName,
        totalUsers: dept.totalUsers || examUserCount,
        examUsers: examUserCount,
        examRate: dept.totalUsers > 0 
          ? Math.round((examUserCount / dept.totalUsers) * 100) 
          : 100,
        totalExams: dept.totalExams,
        passCount: dept.passCount,
        failCount: dept.failCount,
        avgScore: dept.totalExams > 0 
          ? (dept.totalScore / dept.totalExams).toFixed(1) 
          : '0',
        passRate: dept.totalExams > 0 
          ? Math.round((dept.passCount / dept.totalExams) * 100) 
          : 0
      }
    })
    .sort((a, b) => b.totalExams - a.totalExams)
}

// 初始化通过率饼图
const initPassRateChart = () => {
  if (!passRateChartRef.value) return
  
  if (passRateChart) {
    passRateChart.dispose()
  }
  passRateChart = echarts.init(passRateChartRef.value)
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c}人次 ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left',
      top: 'center'
    },
    series: [
      {
        name: '考试通过率',
        type: 'pie',
        radius: ['45%', '75%'],
        center: ['60%', '50%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: true,
          position: 'center',
          formatter: () => {
            return `{a|通过率}\n{b|${reportData.passRate}%}`
          },
          rich: {
            a: {
              fontSize: 14,
              color: '#909399',
              lineHeight: 24
            },
            b: {
              fontSize: 28,
              fontWeight: 'bold',
              color: '#67c23a'
            }
          }
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 16,
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data: [
          { 
            value: reportData.passCount, 
            name: '通过',
            itemStyle: { 
              color: new echarts.graphic.LinearGradient(0, 0, 1, 1, [
                { offset: 0, color: '#67c23a' },
                { offset: 1, color: '#95d475' }
              ])
            }
          },
          { 
            value: reportData.failCount, 
            name: '未通过',
            itemStyle: { 
              color: new echarts.graphic.LinearGradient(0, 0, 1, 1, [
                { offset: 0, color: '#f56c6c' },
                { offset: 1, color: '#fab6b6' }
              ])
            }
          }
        ]
      }
    ]
  }
  
  passRateChart.setOption(option)
}

// 初始化成绩分布柱状图
const initScoreDistChart = () => {
  if (!scoreDistChartRef.value) return
  
  if (scoreDistChart) {
    scoreDistChart.dispose()
  }
  scoreDistChart = echarts.init(scoreDistChartRef.value)
  
  const paperNames = paperScoreData.value.map(p => p.paperName)
  const excellentData = paperScoreData.value.map(p => p.excellentCount)
  const goodData = paperScoreData.value.map(p => p.goodCount)
  const mediumData = paperScoreData.value.map(p => p.mediumCount)
  const passData = paperScoreData.value.map(p => p.passCount)
  const failData = paperScoreData.value.map(p => p.failCount)
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    legend: {
      data: ['优秀(90-100)', '良好(80-89)', '中等(70-79)', '及格(60-69)', '不及格(0-59)'],
      top: 0
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: paperNames,
      axisLabel: {
        interval: 0,
        rotate: paperNames.length > 5 ? 30 : 0,
        formatter: (value: string) => {
          return value.length > 8 ? value.substring(0, 8) + '...' : value
        }
      }
    },
    yAxis: {
      type: 'value',
      name: '人数'
    },
    series: [
      {
        name: '优秀(90-100)',
        type: 'bar',
        stack: 'total',
        data: excellentData,
        itemStyle: { color: '#67c23a' }
      },
      {
        name: '良好(80-89)',
        type: 'bar',
        stack: 'total',
        data: goodData,
        itemStyle: { color: '#95d475' }
      },
      {
        name: '中等(70-79)',
        type: 'bar',
        stack: 'total',
        data: mediumData,
        itemStyle: { color: '#409eff' }
      },
      {
        name: '及格(60-69)',
        type: 'bar',
        stack: 'total',
        data: passData,
        itemStyle: { color: '#e6a23c' }
      },
      {
        name: '不及格(0-59)',
        type: 'bar',
        stack: 'total',
        data: failData,
        itemStyle: { color: '#f56c6c' }
      }
    ]
  }
  
  scoreDistChart.setOption(option)
}

// 初始化趋势图表
const initTrendChart = () => {
  if (!trendChartRef.value) return
  
  if (trendChart) {
    trendChart.dispose()
  }
  trendChart = echarts.init(trendChartRef.value)
  
  // 基于真实考试记录计算趋势数据
  const dates: string[] = []
  const examCounts: number[] = []
  const passCounts: number[] = []
  const avgScores: number[] = []
  
  const now = dayjs()
  let dateFormat = 'MM-DD'
  let step = 1
  let points = 7
  
  if (trendType.value === 'week') {
    dateFormat = 'MM-DD'
    step = 7
    points = 7
  } else if (trendType.value === 'month') {
    dateFormat = 'YYYY-MM'
    step = 30
    points = 6
  }
  
  // 生成时间点并统计每个时间点的数据
  for (let i = points - 1; i >= 0; i--) {
    const date = now.subtract(i * step, 'day')
    const dateStr = date.format(dateFormat)
    dates.push(dateStr)
    
    // 根据时间类型筛选考试记录
    let startDate: dayjs.Dayjs
    let endDate: dayjs.Dayjs
    
    if (trendType.value === 'day') {
      startDate = date.startOf('day')
      endDate = date.endOf('day')
    } else if (trendType.value === 'week') {
      startDate = date.startOf('week')
      endDate = date.endOf('week')
    } else {
      startDate = date.startOf('month')
      endDate = date.endOf('month')
    }
    
    // 筛选该时间段内的考试记录
    const periodRecords = allExamRecords.value.filter((record: any) => {
      if (!record.submitTime && !record.examTime) return false
      const recordDate = dayjs(record.submitTime || record.examTime)
      return recordDate.isAfter(startDate) && recordDate.isBefore(endDate)
    })
    
    const examCount = periodRecords.length
    const passCount = periodRecords.filter((r: any) => r.passed === 1).length
    const totalScore = periodRecords.reduce((sum: number, r: any) => sum + (r.userScore || 0), 0)
    const avgScore = examCount > 0 ? Math.round(totalScore / examCount) : 0
    
    examCounts.push(examCount)
    passCounts.push(passCount)
    avgScores.push(avgScore)
  }
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross'
      }
    },
    legend: {
      data: ['考试人次', '通过人次', '平均分'],
      top: 0
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: dates,
      boundaryGap: false
    },
    yAxis: [
      {
        type: 'value',
        name: '人次',
        position: 'left'
      },
      {
        type: 'value',
        name: '分数',
        position: 'right',
        min: 0,
        max: 100
      }
    ],
    series: [
      {
        name: '考试人次',
        type: 'line',
        smooth: true,
        data: examCounts,
        itemStyle: { color: '#409eff' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
            { offset: 1, color: 'rgba(64, 158, 255, 0.05)' }
          ])
        }
      },
      {
        name: '通过人次',
        type: 'line',
        smooth: true,
        data: passCounts,
        itemStyle: { color: '#67c23a' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(103, 194, 58, 0.3)' },
            { offset: 1, color: 'rgba(103, 194, 58, 0.05)' }
          ])
        }
      },
      {
        name: '平均分',
        type: 'line',
        smooth: true,
        yAxisIndex: 1,
        data: avgScores,
        itemStyle: { color: '#e6a23c' },
        lineStyle: { type: 'dashed' }
      }
    ]
  }
  
  trendChart.setOption(option)
}

// 获取进度条颜色
const getProgressColor = (percentage: number) => {
  if (percentage >= 80) return '#67c23a'
  if (percentage >= 60) return '#409eff'
  if (percentage >= 40) return '#e6a23c'
  return '#f56c6c'
}

// 获取分数样式类
const getScoreClass = (score: number | string) => {
  const numScore = typeof score === 'string' ? parseFloat(score) : score
  if (numScore >= 80) return 'score-high'
  if (numScore >= 60) return 'score-medium'
  return 'score-low'
}

// 查询
const handleQuery = () => {
  getReportData()
}

// 重置
const handleReset = () => {
  filterForm.dateRange = []
  filterForm.deptId = null
  filterForm.paperId = null
  getReportData()
}

// 导出报表
const handleExport = async () => {
  exportLoading.value = true
  try {
    // 导出各试卷成绩分布
    const paperExportData = paperScoreData.value.map(item => ({
      '试卷名称': item.paperName,
      '考试人次': item.totalExams,
      '平均分': item.avgScore,
      '最高分': item.maxScore,
      '最低分': item.minScore,
      '优秀(90-100分)': item.excellentCount,
      '良好(80-89分)': item.goodCount,
      '中等(70-79分)': item.mediumCount,
      '及格(60-69分)': item.passCount,
      '不及格(0-59分)': item.failCount,
      '通过率': `${item.passRate}%`
    }))

    // 导出各部门考试情况
    const deptExportData = deptExamData.value.map(item => ({
      '部门名称': item.deptName,
      '部门人数': item.totalUsers,
      '参考人数': item.examUsers,
      '参考率': `${item.examRate}%`,
      '考试人次': item.totalExams,
      '通过人次': item.passCount,
      '未通过人次': item.failCount,
      '平均分': item.avgScore,
      '通过率': `${item.passRate}%`
    }))

    // 合并数据
    const allData = [
      ['=== 考试报表 ==='],
      ['导出时间', dayjs().format('YYYY-MM-DD HH:mm:ss')],
      [''],
      ['=== 总体统计 ==='],
      ['考试总人次', reportData.totalCount],
      ['通过人次', reportData.passCount],
      ['未通过人次', reportData.failCount],
      ['通过率', `${reportData.passRate}%`],
      ['平均分', reportData.avgScore],
      [''],
      ['=== 各试卷成绩分布 ==='],
      ...paperExportData.map(item => Object.values(item) as string[]),
      [''],
      ['=== 各部门考试情况 ==='],
      ...deptExportData.map(item => Object.values(item) as string[])
    ]

    // 构建CSV内容
    let csvContent = ''
    
    // 添加试卷成绩分布表头
    csvContent += '=== 各试卷成绩分布 ===\n'
    if (paperExportData.length > 0) {
      csvContent += Object.keys(paperExportData[0]).join(',') + '\n'
      csvContent += paperExportData.map(row => 
        Object.values(row).map(v => `"${v}"`).join(',')
      ).join('\n')
    }
    
    csvContent += '\n\n=== 各部门考试情况 ===\n'
    if (deptExportData.length > 0) {
      csvContent += Object.keys(deptExportData[0]).join(',') + '\n'
      csvContent += deptExportData.map(row => 
        Object.values(row).map(v => `"${v}"`).join(',')
      ).join('\n')
    }

    const blob = new Blob(['\ufeff' + csvContent], { type: 'text/csv;charset=utf-8;' })
    const link = document.createElement('a')
    link.href = URL.createObjectURL(blob)
    link.download = `考试报表_${dayjs().format('YYYY-MM-DD_HH-mm-ss')}.csv`
    link.click()
    URL.revokeObjectURL(link.href)

    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  } finally {
    exportLoading.value = false
  }
}

// 窗口大小变化时重绘图表
const handleResize = () => {
  passRateChart?.resize()
  scoreDistChart?.resize()
  trendChart?.resize()
}

onMounted(async () => {
  // 设置默认时间范围为最近一个月
  const end = dayjs()
  const start = end.subtract(30, 'day')
  filterForm.dateRange = [start.format('YYYY-MM-DD'), end.format('YYYY-MM-DD')]
  
  // 获取下拉列表数据
  await Promise.all([getDeptList(), getPaperListData()])
  
  // 获取报表数据
  await getReportData()
  
  // 监听窗口大小变化
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  passRateChart?.dispose()
  scoreDistChart?.dispose()
  trendChart?.dispose()
})
</script>

<style lang="scss" scoped>
.exam-report {
  .filter-card {
    margin-bottom: 20px;
  }

  .stats-row {
    margin-bottom: 20px;

    .stats-card {
      .stats-item {
        display: flex;
        align-items: center;
        padding: 10px 0;

        .stats-icon {
          width: 56px;
          height: 56px;
          border-radius: 12px;
          display: flex;
          align-items: center;
          justify-content: center;
          margin-right: 16px;

          .el-icon {
            font-size: 26px;
            color: #fff;
          }

          &.total {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          }

          &.pass {
            background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
          }

          &.fail {
            background: linear-gradient(135deg, #eb3349 0%, #f45c43 100%);
          }

          &.rate {
            background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
          }
        }

        .stats-info {
          flex: 1;

          .stats-value {
            font-size: 26px;
            font-weight: bold;
            color: #303133;
            line-height: 1.2;
          }

          .stats-label {
            font-size: 14px;
            color: #909399;
            margin-top: 4px;
          }
        }
      }
    }
  }

  .chart-row {
    margin-bottom: 20px;

    .el-card {
      margin-bottom: 20px;
    }
  }

  .chart-card {
    margin-bottom: 20px;
  }

  .table-card {
    margin-bottom: 20px;

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
  }

  .card-header {
    display: flex;
    align-items: center;
    gap: 8px;
    font-weight: 500;

    .el-icon {
      font-size: 18px;
    }
  }

  .chart-container {
    height: 300px;
  }

  .chart-container-lg {
    height: 350px;
  }

  .score-distribution {
    display: flex;
    gap: 8px;

    .dist-item {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      min-width: 32px;
      height: 24px;
      padding: 0 8px;
      border-radius: 4px;
      font-size: 12px;
      font-weight: 500;
      color: #fff;

      &.excellent {
        background-color: #67c23a;
      }

      &.good {
        background-color: #95d475;
      }

      &.medium {
        background-color: #409eff;
      }

      &.pass {
        background-color: #e6a23c;
      }

      &.fail {
        background-color: #f56c6c;
      }
    }
  }

  .score-avg {
    color: #409eff;
    font-weight: 500;
  }

  .score-max {
    color: #67c23a;
    font-weight: 500;
  }

  .score-min {
    color: #f56c6c;
    font-weight: 500;
  }

  .pass-text {
    color: #67c23a;
    font-weight: 500;
  }

  .fail-text {
    color: #f56c6c;
    font-weight: 500;
  }

  .score-high {
    color: #67c23a;
    font-weight: 500;
  }

  .score-medium {
    color: #e6a23c;
    font-weight: 500;
  }

  .score-low {
    color: #f56c6c;
    font-weight: 500;
  }

  :deep(.el-progress-bar__innerText) {
    font-size: 12px;
  }
}
</style>
