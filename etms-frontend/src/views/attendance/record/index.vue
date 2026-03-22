<template>
  <div class="attendance-record">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon total">
              <el-icon size="32"><DataLine /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.totalCount || 0 }}</div>
              <div class="stat-label">总签到次数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon normal">
              <el-icon size="32"><CircleCheck /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.normalCount || 0 }}</div>
              <div class="stat-label">正常签到</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon abnormal">
              <el-icon size="32"><Warning /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ (stats.totalCount || 0) - (stats.normalCount || 0) }}</div>
              <div class="stat-label">异常签到</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon rate">
              <el-icon size="32"><TrendCharts /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ (stats.attendanceRate || 0).toFixed(1) }}%</div>
              <div class="stat-label">出勤率</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 搜索区域 -->
    <el-card shadow="never" class="search-card">
      <el-form :model="searchForm" inline>
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
        <el-form-item label="签到状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 140px">
            <el-option label="正常" :value="1" />
            <el-option label="迟到" :value="2" />
            <el-option label="早退" :value="3" />
            <el-option label="缺勤" :value="4" />
            <el-option label="补签" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="审核状态">
          <el-select v-model="searchForm.auditStatus" placeholder="请选择审核状态" clearable style="width: 140px">
            <el-option label="待审核" :value="0" />
            <el-option label="已通过" :value="1" />
            <el-option label="已驳回" :value="2" />
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

    <!-- 表格 -->
    <el-card shadow="never" class="table-card">
      <template #header>
        <div class="card-header">
          <span>签到记录列表</span>
          <div class="header-actions">
            <el-button type="success" @click="handleSignIn">
              <el-icon><CircleCheck /></el-icon>签到
            </el-button>
            <el-button type="warning" @click="handleSignOut">
              <el-icon><CircleClose /></el-icon>签退
            </el-button>
          </div>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column prop="userName" label="用户名" width="120" />
        <el-table-column prop="realName" label="真实姓名" width="100" />
        <el-table-column prop="planName" label="培训计划" min-width="180" show-overflow-tooltip />
        <el-table-column prop="signTypeName" label="签到类型" width="100">
          <template #default="{ row }">
            <el-tag type="info" size="small">{{ row.signTypeName || getSignTypeName(row.signType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="signTime" label="签到时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.signTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="location" label="签到地点" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.location || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="statusName" label="签到状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ row.statusName || getStatusName(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="auditStatus" label="审核状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getAuditStatusType(row.auditStatus)" size="small">
              {{ getAuditStatusName(row.auditStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button 
              v-if="row.status === 5 && row.auditStatus === 0" 
              type="primary" 
              link 
              @click="handleAudit(row)"
            >
              审核
            </el-button>
            <el-button type="info" link @click="handleViewDetail(row)">
              详情
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

    <!-- 签到/签退对话框 -->
    <el-dialog v-model="signDialogVisible" :title="signDialogTitle" width="500px">
      <el-form ref="signFormRef" :model="signForm" :rules="signRules" label-width="100px">
        <el-form-item label="培训计划" prop="planId">
          <el-select 
            v-model="signForm.planId" 
            placeholder="请选择培训计划" 
            style="width: 100%"
            filterable
          >
            <el-option 
              v-for="plan in activePlanList" 
              :key="plan.id" 
              :label="plan.planName" 
              :value="plan.id" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="签到方式" prop="signMethod">
          <el-radio-group v-model="signForm.signMethod">
            <el-radio :value="1">二维码签到</el-radio>
            <el-radio :value="2">GPS定位签到</el-radio>
            <el-radio :value="3">人脸识别签到</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="签到地点" prop="location">
          <el-input v-model="signForm.location" placeholder="请输入签到地点" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="signDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSignSubmit" :loading="signLoading">确定</el-button>
      </template>
    </el-dialog>

    <!-- 审核对话框 -->
    <el-dialog v-model="auditDialogVisible" title="补签审核" width="500px">
      <el-form ref="auditFormRef" :model="auditForm" :rules="auditRules" label-width="100px">
        <el-form-item label="申请人">
          <el-input :value="currentRecord.realName" disabled />
        </el-form-item>
        <el-form-item label="培训计划">
          <el-input :value="currentRecord.planName" disabled />
        </el-form-item>
        <el-form-item label="签到时间">
          <el-input :value="formatDateTime(currentRecord.signTime)" disabled />
        </el-form-item>
        <el-form-item label="审核结果" prop="auditStatus">
          <el-radio-group v-model="auditForm.auditStatus">
            <el-radio :value="1">通过</el-radio>
            <el-radio :value="2">驳回</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审核备注" prop="auditRemark">
          <el-input 
            v-model="auditForm.auditRemark" 
            type="textarea" 
            :rows="3" 
            placeholder="请输入审核备注"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="auditDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAuditSubmit" :loading="auditLoading">确定</el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="签到详情" width="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="用户名">{{ currentRecord.userName }}</el-descriptions-item>
        <el-descriptions-item label="真实姓名">{{ currentRecord.realName }}</el-descriptions-item>
        <el-descriptions-item label="培训计划" :span="2">{{ currentRecord.planName }}</el-descriptions-item>
        <el-descriptions-item label="签到类型">
          <el-tag type="info" size="small">{{ currentRecord.signTypeName || getSignTypeName(currentRecord.signType) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="签到状态">
          <el-tag :type="getStatusType(currentRecord.status)" size="small">
            {{ currentRecord.statusName || getStatusName(currentRecord.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="签到时间">{{ formatDateTime(currentRecord.signTime) }}</el-descriptions-item>
        <el-descriptions-item label="审核状态">
          <el-tag :type="getAuditStatusType(currentRecord.auditStatus)" size="small">
            {{ getAuditStatusName(currentRecord.auditStatus) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="签到地点" :span="2">{{ currentRecord.location || '-' }}</el-descriptions-item>
        <el-descriptions-item label="迟到分钟" v-if="currentRecord.lateMinutes > 0">
          {{ currentRecord.lateMinutes }} 分钟
        </el-descriptions-item>
        <el-descriptions-item label="早退分钟" v-if="currentRecord.earlyMinutes > 0">
          {{ currentRecord.earlyMinutes }} 分钟
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatDateTime(currentRecord.createTime) }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { 
  DataLine, 
  CircleCheck, 
  Warning, 
  TrendCharts, 
  Search, 
  Refresh, 
  CircleClose 
} from '@element-plus/icons-vue'
import { 
  getAttendanceList, 
  signIn, 
  auditAttendance, 
  getAttendanceStats 
} from '@/api/attendance'
import { getPlanList } from '@/api/training'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

// 统计数据
const stats = ref({
  totalCount: 0,
  normalCount: 0,
  attendanceRate: 0
})

// 搜索表单
const searchForm = reactive({
  planId: null as number | null,
  status: null as number | null,
  auditStatus: null as number | null
})

// 培训计划列表
const planList = ref<any[]>([])
const activePlanList = computed(() => planList.value.filter(p => p.status === 1))

// 表格数据
const tableData = ref<any[]>([])
const loading = ref(false)
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 签到/签退
const signDialogVisible = ref(false)
const signDialogTitle = ref('签到')
const signFormRef = ref<FormInstance>()
const signLoading = ref(false)
const isSignOut = ref(false)
const signForm = reactive({
  planId: null as number | null,
  signMethod: 1, // 签到方式：1二维码 2GPS定位 3人脸识别
  location: ''
})

const signRules: FormRules = {
  planId: [{ required: true, message: '请选择培训计划', trigger: 'change' }],
  signMethod: [{ required: true, message: '请选择签到方式', trigger: 'change' }]
}

// 审核
const auditDialogVisible = ref(false)
const auditFormRef = ref<FormInstance>()
const auditLoading = ref(false)
const currentRecord = ref<any>({})
const auditForm = reactive({
  auditStatus: 1,
  auditRemark: ''
})

const auditRules: FormRules = {
  auditStatus: [{ required: true, message: '请选择审核结果', trigger: 'change' }]
}

// 详情
const detailDialogVisible = ref(false)

// 获取统计
const getStats = async () => {
  try {
    const userId = userStore.userInfo?.userId
    if (userId) {
      const res = await getAttendanceStats(userId)
      if (res.data) {
        stats.value = res.data
      }
    }
  } catch (error) {
    console.error('获取统计失败:', error)
  }
}

// 获取培训计划列表
const getPlans = async () => {
  try {
    const res = await getPlanList({ current: 1, size: 1000 })
    planList.value = res.data?.records || []
  } catch (error) {
    console.error('获取培训计划失败:', error)
  }
}

// 获取列表
const getList = async () => {
  loading.value = true
  try {
    const params: any = {
      current: pagination.current,
      size: pagination.size
    }
    if (searchForm.planId) params.planId = searchForm.planId
    if (searchForm.status !== null) params.status = searchForm.status
    if (searchForm.auditStatus !== null) params.auditStatus = searchForm.auditStatus

    const res = await getAttendanceList(params)
    tableData.value = res.data?.records || []
    pagination.total = res.data?.total || 0
  } catch (error) {
    console.error('获取列表失败:', error)
  } finally {
    loading.value = false
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
    planId: null,
    status: null,
    auditStatus: null
  })
  handleSearch()
}

// 签到
const handleSignIn = () => {
  isSignOut.value = false
  signDialogTitle.value = '签到'
  Object.assign(signForm, { planId: null, signMethod: 1, location: '' })
  signDialogVisible.value = true
}

// 签退
const handleSignOut = () => {
  isSignOut.value = true
  signDialogTitle.value = '签退'
  Object.assign(signForm, { planId: null, signMethod: 1, location: '' })
  signDialogVisible.value = true
}

// 提交签到/签退
const handleSignSubmit = async () => {
  const valid = await signFormRef.value?.validate()
  if (!valid) return

  signLoading.value = true
  try {
    // signType参数传递给后端表示签到方式
    // signCategory: 1-签到, 2-签退
    await signIn({
      planId: signForm.planId!,
      signType: signForm.signMethod,
      signCategory: isSignOut.value ? 2 : 1,
      location: signForm.location
    })
    ElMessage.success(isSignOut.value ? '签退成功' : '签到成功')
    signDialogVisible.value = false
    getList()
    getStats()
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  } finally {
    signLoading.value = false
  }
}

// 审核
const handleAudit = (row: any) => {
  currentRecord.value = row
  Object.assign(auditForm, { auditStatus: 1, auditRemark: '' })
  auditDialogVisible.value = true
}

// 提交审核
const handleAuditSubmit = async () => {
  const valid = await auditFormRef.value?.validate()
  if (!valid) return

  auditLoading.value = true
  try {
    await auditAttendance(currentRecord.value.id, auditForm)
    ElMessage.success('审核成功')
    auditDialogVisible.value = false
    getList()
  } catch (error: any) {
    ElMessage.error(error.message || '审核失败')
  } finally {
    auditLoading.value = false
  }
}

// 查看详情
const handleViewDetail = (row: any) => {
  currentRecord.value = row
  detailDialogVisible.value = true
}

// 格式化日期时间
const formatDateTime = (dateTime: string) => {
  if (!dateTime) return '-'
  return dateTime.replace('T', ' ')
}

// 获取签到类型名称
const getSignTypeName = (type: number) => {
  const types: Record<number, string> = {
    1: '二维码签到',
    2: 'GPS定位',
    3: '人脸识别'
  }
  return types[type] || '未知'
}

// 获取状态名称
const getStatusName = (status: number) => {
  const statuses: Record<number, string> = {
    1: '正常',
    2: '迟到',
    3: '早退',
    4: '缺勤',
    5: '补签'
  }
  return statuses[status] || '未知'
}

// 获取状态类型
const getStatusType = (status: number) => {
  const types: Record<number, string> = {
    1: 'success',
    2: 'warning',
    3: 'warning',
    4: 'danger',
    5: 'info'
  }
  return types[status] || 'info'
}

// 获取审核状态名称
const getAuditStatusName = (status: number) => {
  const statuses: Record<number, string> = {
    0: '待审核',
    1: '已通过',
    2: '已驳回'
  }
  return statuses[status] || '-'
}

// 获取审核状态类型
const getAuditStatusType = (status: number) => {
  const types: Record<number, string> = {
    0: 'warning',
    1: 'success',
    2: 'danger'
  }
  return types[status] || 'info'
}

onMounted(() => {
  getPlans()
  getList()
  getStats()
})
</script>

<style lang="scss" scoped>
.attendance-record {
  .stats-row {
    margin-bottom: 20px;

    .stat-card {
      .stat-content {
        display: flex;
        align-items: center;
        padding: 10px 0;

        .stat-icon {
          width: 60px;
          height: 60px;
          border-radius: 8px;
          display: flex;
          align-items: center;
          justify-content: center;
          margin-right: 16px;
          color: #fff;

          &.total {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          }

          &.normal {
            background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
          }

          &.abnormal {
            background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
          }

          &.rate {
            background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
          }
        }

        .stat-info {
          flex: 1;

          .stat-value {
            font-size: 28px;
            font-weight: bold;
            color: #303133;
            line-height: 1.2;
          }

          .stat-label {
            font-size: 14px;
            color: #909399;
            margin-top: 4px;
          }
        }
      }
    }
  }

  .search-card {
    margin-bottom: 20px;
  }

  .table-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .header-actions {
        display: flex;
        gap: 10px;
      }
    }

    .el-pagination {
      margin-top: 20px;
      justify-content: flex-end;
    }
  }
}
</style>
