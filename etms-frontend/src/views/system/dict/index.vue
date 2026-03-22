<template>
  <div class="dict-management">
    <el-row :gutter="20">
      <!-- 左侧：字典类型列表 -->
      <el-col :span="8">
        <el-card shadow="never" class="type-card">
          <template #header>
            <div class="card-header">
              <span>字典类型</span>
              <div class="header-btns">
                <el-button type="primary" link @click="handleRefreshCache">
                  <el-icon><Refresh /></el-icon>刷新缓存
                </el-button>
                <el-button type="primary" @click="handleAddType">
                  <el-icon><Plus /></el-icon>新增
                </el-button>
              </div>
            </div>
          </template>
          
          <!-- 搜索区域 -->
          <div class="search-area">
            <el-input
              v-model="typeSearchKeyword"
              placeholder="请输入字典名称/类型搜索"
              clearable
              @input="handleTypeSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </div>
          
          <!-- 字典类型列表 -->
          <el-table
            ref="typeTableRef"
            :data="filteredTypeList"
            v-loading="typeLoading"
            highlight-current-row
            @current-change="handleTypeSelect"
            stripe
            border
            max-height="calc(100vh - 320px)"
          >
            <el-table-column prop="dictName" label="字典名称" min-width="120" show-overflow-tooltip />
            <el-table-column prop="dictType" label="字典类型" min-width="140" show-overflow-tooltip>
              <template #default="{ row }">
                <el-tag type="info">{{ row.dictType }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
                  {{ row.status === 1 ? '正常' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link size="small" @click.stop="handleEditType(row)">编辑</el-button>
                <el-button type="danger" link size="small" @click.stop="handleDeleteType(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      
      <!-- 右侧：字典数据列表 -->
      <el-col :span="16">
        <el-card shadow="never" class="data-card">
          <template #header>
            <div class="card-header">
              <span>字典数据{{ currentType ? ` - ${currentType.dictName}` : '' }}</span>
              <el-button type="primary" @click="handleAddData" :disabled="!currentType">
                <el-icon><Plus /></el-icon>新增
              </el-button>
            </div>
          </template>
          
          <!-- 搜索区域 -->
          <el-form :model="dataSearchForm" inline class="search-form">
            <el-form-item label="数据标签">
              <el-input v-model="dataSearchForm.dictLabel" placeholder="请输入数据标签" clearable />
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="dataSearchForm.status" placeholder="请选择状态" clearable>
                <el-option label="正常" :value="1" />
                <el-option label="禁用" :value="0" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleDataSearch">搜索</el-button>
              <el-button @click="handleDataReset">重置</el-button>
            </el-form-item>
          </el-form>
          
          <!-- 字典数据表格 -->
          <el-table :data="filteredDataList" v-loading="dataLoading" stripe border>
            <el-table-column prop="dictLabel" label="数据标签" min-width="120" show-overflow-tooltip />
            <el-table-column prop="dictValue" label="数据键值" min-width="100" />
            <el-table-column prop="dictSort" label="排序" width="80" />
            <el-table-column prop="status" label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
                  {{ row.status === 1 ? '正常' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip />
            <el-table-column prop="createTime" label="创建时间" width="180" />
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link @click="handleEditData(row)">编辑</el-button>
                <el-button type="danger" link @click="handleDeleteData(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 字典类型对话框 -->
    <el-dialog v-model="typeDialogVisible" :title="typeDialogTitle" width="500px">
      <el-form ref="typeFormRef" :model="typeForm" :rules="typeRules" label-width="100px">
        <el-form-item label="字典名称" prop="dictName">
          <el-input v-model="typeForm.dictName" placeholder="请输入字典名称" />
        </el-form-item>
        <el-form-item label="字典类型" prop="dictType">
          <el-input v-model="typeForm.dictType" placeholder="请输入字典类型" :disabled="isEditType" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="typeForm.status">
            <el-radio :value="1">正常</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="typeForm.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="typeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleTypeSubmit">确定</el-button>
      </template>
    </el-dialog>
    
    <!-- 字典数据对话框 -->
    <el-dialog v-model="dataDialogVisible" :title="dataDialogTitle" width="500px">
      <el-form ref="dataFormRef" :model="dataForm" :rules="dataRules" label-width="100px">
        <el-form-item label="数据标签" prop="dictLabel">
          <el-input v-model="dataForm.dictLabel" placeholder="请输入数据标签" />
        </el-form-item>
        <el-form-item label="数据键值" prop="dictValue">
          <el-input v-model="dataForm.dictValue" placeholder="请输入数据键值" />
        </el-form-item>
        <el-form-item label="排序" prop="dictSort">
          <el-input-number v-model="dataForm.dictSort" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="dataForm.status">
            <el-radio :value="1">正常</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="dataForm.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dataDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleDataSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'

// ==================== 类型定义 ====================
interface DictType {
  id: number
  dictName: string
  dictType: string
  status: number
  remark: string
  createTime: string
}

interface DictData {
  id: number
  dictTypeId: number
  dictLabel: string
  dictValue: string
  dictSort: number
  status: number
  remark: string
  createTime: string
}

// ==================== 模拟数据 ====================
const mockDictTypes: DictType[] = [
  { id: 1, dictName: '用户性别', dictType: 'sys_user_sex', status: 1, remark: '用户性别字典', createTime: '2024-01-01 10:00:00' },
  { id: 2, dictName: '菜单状态', dictType: 'sys_menu_status', status: 1, remark: '菜单状态字典', createTime: '2024-01-01 10:00:00' },
  { id: 3, dictName: '正常状态', dictType: 'sys_normal_disable', status: 1, remark: '正常禁用状态', createTime: '2024-01-01 10:00:00' },
  { id: 4, dictName: '任务状态', dictType: 'sys_job_status', status: 1, remark: '任务状态字典', createTime: '2024-01-01 10:00:00' },
  { id: 5, dictName: '任务分组', dictType: 'sys_job_group', status: 1, remark: '任务分组字典', createTime: '2024-01-01 10:00:00' },
  { id: 6, dictName: '通知类型', dictType: 'sys_notice_type', status: 1, remark: '通知类型字典', createTime: '2024-01-01 10:00:00' },
  { id: 7, dictName: '课程状态', dictType: 'course_status', status: 1, remark: '课程状态字典', createTime: '2024-01-01 10:00:00' },
  { id: 8, dictName: '培训类型', dictType: 'training_type', status: 1, remark: '培训类型字典', createTime: '2024-01-01 10:00:00' },
  { id: 9, dictName: '考试状态', dictType: 'exam_status', status: 0, remark: '考试状态字典', createTime: '2024-01-01 10:00:00' },
  { id: 10, dictName: '签到类型', dictType: 'attendance_type', status: 1, remark: '签到类型字典', createTime: '2024-01-01 10:00:00' }
]

const mockDictData: DictData[] = [
  // 用户性别
  { id: 1, dictTypeId: 1, dictLabel: '男', dictValue: '1', dictSort: 1, status: 1, remark: '性别-男', createTime: '2024-01-01 10:00:00' },
  { id: 2, dictTypeId: 1, dictLabel: '女', dictValue: '2', dictSort: 2, status: 1, remark: '性别-女', createTime: '2024-01-01 10:00:00' },
  { id: 3, dictTypeId: 1, dictLabel: '未知', dictValue: '0', dictSort: 3, status: 1, remark: '性别-未知', createTime: '2024-01-01 10:00:00' },
  // 菜单状态
  { id: 4, dictTypeId: 2, dictLabel: '显示', dictValue: '1', dictSort: 1, status: 1, remark: '菜单显示', createTime: '2024-01-01 10:00:00' },
  { id: 5, dictTypeId: 2, dictLabel: '隐藏', dictValue: '0', dictSort: 2, status: 1, remark: '菜单隐藏', createTime: '2024-01-01 10:00:00' },
  // 正常禁用状态
  { id: 6, dictTypeId: 3, dictLabel: '正常', dictValue: '1', dictSort: 1, status: 1, remark: '正常状态', createTime: '2024-01-01 10:00:00' },
  { id: 7, dictTypeId: 3, dictLabel: '禁用', dictValue: '0', dictSort: 2, status: 1, remark: '禁用状态', createTime: '2024-01-01 10:00:00' },
  // 任务状态
  { id: 8, dictTypeId: 4, dictLabel: '正常', dictValue: '0', dictSort: 1, status: 1, remark: '任务正常', createTime: '2024-01-01 10:00:00' },
  { id: 9, dictTypeId: 4, dictLabel: '暂停', dictValue: '1', dictSort: 2, status: 1, remark: '任务暂停', createTime: '2024-01-01 10:00:00' },
  // 任务分组
  { id: 10, dictTypeId: 5, dictLabel: '默认', dictValue: 'DEFAULT', dictSort: 1, status: 1, remark: '默认分组', createTime: '2024-01-01 10:00:00' },
  { id: 11, dictTypeId: 5, dictLabel: '系统', dictValue: 'SYSTEM', dictSort: 2, status: 1, remark: '系统分组', createTime: '2024-01-01 10:00:00' },
  // 通知类型
  { id: 12, dictTypeId: 6, dictLabel: '通知', dictValue: '1', dictSort: 1, status: 1, remark: '通知类型', createTime: '2024-01-01 10:00:00' },
  { id: 13, dictTypeId: 6, dictLabel: '公告', dictValue: '2', dictSort: 2, status: 1, remark: '公告类型', createTime: '2024-01-01 10:00:00' },
  // 课程状态
  { id: 14, dictTypeId: 7, dictLabel: '未发布', dictValue: '0', dictSort: 1, status: 1, remark: '课程未发布', createTime: '2024-01-01 10:00:00' },
  { id: 15, dictTypeId: 7, dictLabel: '已发布', dictValue: '1', dictSort: 2, status: 1, remark: '课程已发布', createTime: '2024-01-01 10:00:00' },
  { id: 16, dictTypeId: 7, dictLabel: '已下架', dictValue: '2', dictSort: 3, status: 1, remark: '课程已下架', createTime: '2024-01-01 10:00:00' },
  // 培训类型
  { id: 17, dictTypeId: 8, dictLabel: '入职培训', dictValue: '1', dictSort: 1, status: 1, remark: '入职培训', createTime: '2024-01-01 10:00:00' },
  { id: 18, dictTypeId: 8, dictLabel: '岗位培训', dictValue: '2', dictSort: 2, status: 1, remark: '岗位培训', createTime: '2024-01-01 10:00:00' },
  { id: 19, dictTypeId: 8, dictLabel: '技能培训', dictValue: '3', dictSort: 3, status: 1, remark: '技能培训', createTime: '2024-01-01 10:00:00' },
  { id: 20, dictTypeId: 8, dictLabel: '安全培训', dictValue: '4', dictSort: 4, status: 0, remark: '安全培训', createTime: '2024-01-01 10:00:00' },
  // 考试状态
  { id: 21, dictTypeId: 9, dictLabel: '未开始', dictValue: '0', dictSort: 1, status: 1, remark: '考试未开始', createTime: '2024-01-01 10:00:00' },
  { id: 22, dictTypeId: 9, dictLabel: '进行中', dictValue: '1', dictSort: 2, status: 1, remark: '考试进行中', createTime: '2024-01-01 10:00:00' },
  { id: 23, dictTypeId: 9, dictLabel: '已结束', dictValue: '2', dictSort: 3, status: 1, remark: '考试已结束', createTime: '2024-01-01 10:00:00' },
  // 签到类型
  { id: 24, dictTypeId: 10, dictLabel: '上班签到', dictValue: '1', dictSort: 1, status: 1, remark: '上班签到', createTime: '2024-01-01 10:00:00' },
  { id: 25, dictTypeId: 10, dictLabel: '下班签退', dictValue: '2', dictSort: 2, status: 1, remark: '下班签退', createTime: '2024-01-01 10:00:00' }
]

// ==================== 响应式数据 ====================
// 字典类型相关
const typeList = ref<DictType[]>([...mockDictTypes])
const typeLoading = ref(false)
const typeSearchKeyword = ref('')
const currentType = ref<DictType | null>(null)
const typeTableRef = ref()

// 字典数据相关
const dataList = ref<DictData[]>([...mockDictData])
const dataLoading = ref(false)
const dataSearchForm = reactive({
  dictLabel: '',
  status: null as number | null
})

// 字典类型对话框
const typeDialogVisible = ref(false)
const isEditType = ref(false)
const typeDialogTitle = computed(() => isEditType.value ? '编辑字典类型' : '新增字典类型')
const typeFormRef = ref<FormInstance>()
const typeForm = reactive({
  id: null as number | null,
  dictName: '',
  dictType: '',
  status: 1,
  remark: ''
})

// 字典数据对话框
const dataDialogVisible = ref(false)
const isEditData = ref(false)
const dataDialogTitle = computed(() => isEditData.value ? '编辑字典数据' : '新增字典数据')
const dataFormRef = ref<FormInstance>()
const dataForm = reactive({
  id: null as number | null,
  dictTypeId: null as number | null,
  dictLabel: '',
  dictValue: '',
  dictSort: 0,
  status: 1,
  remark: ''
})

// ==================== 表单验证规则 ====================
const typeRules: FormRules = {
  dictName: [{ required: true, message: '请输入字典名称', trigger: 'blur' }],
  dictType: [{ required: true, message: '请输入字典类型', trigger: 'blur' }]
}

const dataRules: FormRules = {
  dictLabel: [{ required: true, message: '请输入数据标签', trigger: 'blur' }],
  dictValue: [{ required: true, message: '请输入数据键值', trigger: 'blur' }]
}

// ==================== 计算属性 ====================
// 过滤后的字典类型列表
const filteredTypeList = computed(() => {
  if (!typeSearchKeyword.value) return typeList.value
  const keyword = typeSearchKeyword.value.toLowerCase()
  return typeList.value.filter(item => 
    item.dictName.toLowerCase().includes(keyword) || 
    item.dictType.toLowerCase().includes(keyword)
  )
})

// 过滤后的字典数据列表
const filteredDataList = computed(() => {
  let result = dataList.value.filter(item => item.dictTypeId === currentType.value?.id)
  if (dataSearchForm.dictLabel) {
    result = result.filter(item => 
      item.dictLabel.toLowerCase().includes(dataSearchForm.dictLabel.toLowerCase())
    )
  }
  if (dataSearchForm.status !== null) {
    result = result.filter(item => item.status === dataSearchForm.status)
  }
  return result.sort((a, b) => a.dictSort - b.dictSort)
})

// ==================== 字典类型相关方法 ====================
// 字典类型搜索
const handleTypeSearch = () => {
  // 搜索已通过计算属性实现
}

// 选择字典类型
const handleTypeSelect = (row: DictType | null) => {
  currentType.value = row
}

// 新增字典类型
const handleAddType = () => {
  isEditType.value = false
  Object.assign(typeForm, {
    id: null,
    dictName: '',
    dictType: '',
    status: 1,
    remark: ''
  })
  typeDialogVisible.value = true
}

// 编辑字典类型
const handleEditType = (row: DictType) => {
  isEditType.value = true
  Object.assign(typeForm, {
    id: row.id,
    dictName: row.dictName,
    dictType: row.dictType,
    status: row.status,
    remark: row.remark
  })
  typeDialogVisible.value = true
}

// 删除字典类型
const handleDeleteType = async (row: DictType) => {
  await ElMessageBox.confirm(
    `确定要删除字典类型【${row.dictName}】吗？删除后将同时删除该类型下的所有字典数据！`,
    '提示',
    { type: 'warning' }
  )
  
  // 删除字典类型
  const typeIndex = typeList.value.findIndex(item => item.id === row.id)
  if (typeIndex > -1) {
    typeList.value.splice(typeIndex, 1)
  }
  
  // 删除关联的字典数据
  dataList.value = dataList.value.filter(item => item.dictTypeId !== row.id)
  
  // 清除当前选中
  if (currentType.value?.id === row.id) {
    currentType.value = null
  }
  
  ElMessage.success('删除成功')
}

// 提交字典类型表单
const handleTypeSubmit = async () => {
  const valid = await typeFormRef.value?.validate()
  if (!valid) return
  
  if (isEditType.value) {
    // 编辑
    const index = typeList.value.findIndex(item => item.id === typeForm.id)
    if (index > -1) {
      typeList.value[index] = {
        ...typeList.value[index],
        dictName: typeForm.dictName,
        status: typeForm.status,
        remark: typeForm.remark
      }
    }
    ElMessage.success('修改成功')
  } else {
    // 新增
    const newType: DictType = {
      id: Math.max(...typeList.value.map(t => t.id), 0) + 1,
      dictName: typeForm.dictName,
      dictType: typeForm.dictType,
      status: typeForm.status,
      remark: typeForm.remark,
      createTime: new Date().toLocaleString().replace(/\//g, '-')
    }
    typeList.value.push(newType)
    ElMessage.success('新增成功')
  }
  
  typeDialogVisible.value = false
}

// ==================== 字典数据相关方法 ====================
// 字典数据搜索
const handleDataSearch = () => {
  // 搜索已通过计算属性实现
}

// 重置字典数据搜索
const handleDataReset = () => {
  Object.assign(dataSearchForm, {
    dictLabel: '',
    status: null
  })
}

// 新增字典数据
const handleAddData = () => {
  if (!currentType.value) {
    ElMessage.warning('请先选择字典类型')
    return
  }
  
  isEditData.value = false
  Object.assign(dataForm, {
    id: null,
    dictTypeId: currentType.value.id,
    dictLabel: '',
    dictValue: '',
    dictSort: Math.max(...dataList.value.filter(d => d.dictTypeId === currentType.value?.id).map(d => d.dictSort), 0) + 1,
    status: 1,
    remark: ''
  })
  dataDialogVisible.value = true
}

// 编辑字典数据
const handleEditData = (row: DictData) => {
  isEditData.value = true
  Object.assign(dataForm, {
    id: row.id,
    dictTypeId: row.dictTypeId,
    dictLabel: row.dictLabel,
    dictValue: row.dictValue,
    dictSort: row.dictSort,
    status: row.status,
    remark: row.remark
  })
  dataDialogVisible.value = true
}

// 删除字典数据
const handleDeleteData = async (row: DictData) => {
  await ElMessageBox.confirm(
    `确定要删除字典数据【${row.dictLabel}】吗？`,
    '提示',
    { type: 'warning' }
  )
  
  const index = dataList.value.findIndex(item => item.id === row.id)
  if (index > -1) {
    dataList.value.splice(index, 1)
  }
  
  ElMessage.success('删除成功')
}

// 提交字典数据表单
const handleDataSubmit = async () => {
  const valid = await dataFormRef.value?.validate()
  if (!valid) return
  
  if (isEditData.value) {
    // 编辑
    const index = dataList.value.findIndex(item => item.id === dataForm.id)
    if (index > -1) {
      dataList.value[index] = {
        ...dataList.value[index],
        dictLabel: dataForm.dictLabel,
        dictValue: dataForm.dictValue,
        dictSort: dataForm.dictSort,
        status: dataForm.status,
        remark: dataForm.remark
      }
    }
    ElMessage.success('修改成功')
  } else {
    // 新增
    const newData: DictData = {
      id: Math.max(...dataList.value.map(d => d.id), 0) + 1,
      dictTypeId: dataForm.dictTypeId!,
      dictLabel: dataForm.dictLabel,
      dictValue: dataForm.dictValue,
      dictSort: dataForm.dictSort,
      status: dataForm.status,
      remark: dataForm.remark,
      createTime: new Date().toLocaleString().replace(/\//g, '-')
    }
    dataList.value.push(newData)
    ElMessage.success('新增成功')
  }
  
  dataDialogVisible.value = false
}

// ==================== 刷新缓存 ====================
const handleRefreshCache = () => {
  ElMessage.success('字典缓存刷新成功')
}

// ==================== 初始化 ====================
onMounted(() => {
  // 默认选中第一个字典类型
  if (typeList.value.length > 0) {
    currentType.value = typeList.value[0]
    // 设置表格选中行
    setTimeout(() => {
      typeTableRef.value?.setCurrentRow(typeList.value[0])
    }, 100)
  }
})
</script>

<style lang="scss" scoped>
.dict-management {
  height: 100%;
  
  .type-card,
  .data-card {
    height: calc(100vh - 180px);
    display: flex;
    flex-direction: column;
    
    :deep(.el-card__body) {
      flex: 1;
      overflow: hidden;
      display: flex;
      flex-direction: column;
    }
  }
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .header-btns {
      display: flex;
      gap: 8px;
    }
  }
  
  .search-area {
    margin-bottom: 16px;
  }
  
  .search-form {
    margin-bottom: 16px;
  }
  
  .el-table {
    flex: 1;
  }
}
</style>
