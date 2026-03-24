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
                <el-button type="primary" link @click="handleRefreshCache" :loading="cacheLoading">
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
    <el-dialog v-model="typeDialogVisible" :title="typeDialogTitle" width="500px" @close="handleTypeDialogClose">
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
        <el-button type="primary" @click="handleTypeSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
    
    <!-- 字典数据对话框 -->
    <el-dialog v-model="dataDialogVisible" :title="dataDialogTitle" width="500px" @close="handleDataDialogClose">
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
        <el-button type="primary" @click="handleDataSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import {
  getDictTypeList,
  createDictType,
  updateDictType,
  deleteDictType,
  getDictDataList,
  createDictData,
  updateDictData,
  deleteDictData,
  refreshDictCache
} from '@/api/dict'
import type { DictType, DictData } from '@/api/dict'

// ==================== 响应式数据 ====================
// 字典类型相关
const typeList = ref<DictType[]>([])
const typeLoading = ref(false)
const typeSearchKeyword = ref('')
const currentType = ref<DictType | null>(null)
const typeTableRef = ref()

// 字典数据相关
const dataList = ref<DictData[]>([])
const dataLoading = ref(false)
const dataSearchForm = reactive({
  dictLabel: '',
  status: null as number | null
})

// 提交加载状态
const submitLoading = ref(false)
const cacheLoading = ref(false)

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
  dictName: [
    { required: true, message: '请输入字典名称', trigger: 'blur' },
    { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  dictType: [
    { required: true, message: '请输入字典类型', trigger: 'blur' },
    { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' }
  ]
}

const dataRules: FormRules = {
  dictLabel: [
    { required: true, message: '请输入数据标签', trigger: 'blur' },
    { min: 1, max: 100, message: '长度在 1 到 100 个字符', trigger: 'blur' }
  ],
  dictValue: [
    { required: true, message: '请输入数据键值', trigger: 'blur' },
    { min: 1, max: 100, message: '长度在 1 到 100 个字符', trigger: 'blur' }
  ]
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
  let result = dataList.value
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

// ==================== API 调用方法 ====================
// 获取字典类型列表
const fetchTypeList = async () => {
  typeLoading.value = true
  try {
    const res = await getDictTypeList({ current: 1, size: 1000 })
    if (res.code === 200 && res.data) {
      typeList.value = res.data.records || []
    } else {
      ElMessage.error(res.message || '获取字典类型列表失败')
    }
  } catch (error) {
    console.error('获取字典类型列表失败:', error)
    ElMessage.error('获取字典类型列表失败')
  } finally {
    typeLoading.value = false
  }
}

// 获取字典数据列表
const fetchDataList = async (dictTypeId: number) => {
  dataLoading.value = true
  try {
    const res = await getDictDataList(dictTypeId)
    if (res.code === 200 && res.data) {
      dataList.value = res.data || []
    } else {
      ElMessage.error(res.message || '获取字典数据列表失败')
    }
  } catch (error) {
    console.error('获取字典数据列表失败:', error)
    ElMessage.error('获取字典数据列表失败')
  } finally {
    dataLoading.value = false
  }
}

// ==================== 字典类型相关方法 ====================
// 字典类型搜索
const handleTypeSearch = () => {
  // 搜索已通过计算属性实现
}

// 选择字典类型
const handleTypeSelect = (row: DictType | null) => {
  currentType.value = row
  if (row) {
    fetchDataList(row.id)
  } else {
    dataList.value = []
  }
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
  try {
    await ElMessageBox.confirm(
      `确定要删除字典类型【${row.dictName}】吗？删除后将同时删除该类型下的所有字典数据！`,
      '提示',
      { type: 'warning' }
    )
    
    const res = await deleteDictType(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      // 重新获取列表
      await fetchTypeList()
      // 清除当前选中
      if (currentType.value?.id === row.id) {
        currentType.value = null
        dataList.value = []
      }
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除字典类型失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 提交字典类型表单
const handleTypeSubmit = async () => {
  const valid = await typeFormRef.value?.validate()
  if (!valid) return
  
  submitLoading.value = true
  try {
    const data = {
      dictName: typeForm.dictName,
      dictType: typeForm.dictType,
      status: typeForm.status,
      remark: typeForm.remark
    }
    
    let res
    if (isEditType.value) {
      res = await updateDictType(typeForm.id!, data)
    } else {
      res = await createDictType(data)
    }
    
    if (res.code === 200) {
      ElMessage.success(isEditType.value ? '修改成功' : '新增成功')
      typeDialogVisible.value = false
      // 重新获取列表
      await fetchTypeList()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    console.error('提交字典类型失败:', error)
    ElMessage.error('操作失败')
  } finally {
    submitLoading.value = false
  }
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
    dictSort: dataList.value.length > 0 ? Math.max(...dataList.value.map(d => d.dictSort)) + 1 : 1,
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
  try {
    await ElMessageBox.confirm(
      `确定要删除字典数据【${row.dictLabel}】吗？`,
      '提示',
      { type: 'warning' }
    )
    
    const res = await deleteDictData(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      // 重新获取当前类型的字典数据
      if (currentType.value) {
        await fetchDataList(currentType.value.id)
      }
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除字典数据失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 提交字典数据表单
const handleDataSubmit = async () => {
  const valid = await dataFormRef.value?.validate()
  if (!valid) return
  
  submitLoading.value = true
  try {
    const data = {
      dictTypeId: dataForm.dictTypeId,
      dictLabel: dataForm.dictLabel,
      dictValue: dataForm.dictValue,
      dictSort: dataForm.dictSort,
      status: dataForm.status,
      remark: dataForm.remark
    }
    
    let res
    if (isEditData.value) {
      res = await updateDictData(dataForm.id!, data)
    } else {
      res = await createDictData(data)
    }
    
    if (res.code === 200) {
      ElMessage.success(isEditData.value ? '修改成功' : '新增成功')
      dataDialogVisible.value = false
      // 重新获取当前类型的字典数据
      if (currentType.value) {
        await fetchDataList(currentType.value.id)
      }
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    console.error('提交字典数据失败:', error)
    ElMessage.error('操作失败')
  } finally {
    submitLoading.value = false
  }
}

// ==================== 刷新缓存 ====================
const handleRefreshCache = async () => {
  cacheLoading.value = true
  try {
    const res = await refreshDictCache()
    if (res.code === 200) {
      ElMessage.success('字典缓存刷新成功')
    } else {
      ElMessage.error(res.message || '刷新缓存失败')
    }
  } catch (error) {
    console.error('刷新缓存失败:', error)
    ElMessage.error('刷新缓存失败')
  } finally {
    cacheLoading.value = false
  }
}

// ==================== 对话框关闭处理 ====================
// 字典类型对话框关闭
const handleTypeDialogClose = () => {
  typeFormRef.value?.resetFields()
  typeFormRef.value?.clearValidate()
}

// 字典数据对话框关闭
const handleDataDialogClose = () => {
  dataFormRef.value?.resetFields()
  dataFormRef.value?.clearValidate()
}

// ==================== 初始化 ====================
onMounted(async () => {
  // 获取字典类型列表
  await fetchTypeList()
  
  // 默认选中第一个字典类型
  if (typeList.value.length > 0) {
    currentType.value = typeList.value[0]
    // 设置表格选中行
    setTimeout(() => {
      typeTableRef.value?.setCurrentRow(typeList.value[0])
    }, 100)
    // 获取对应的字典数据
    await fetchDataList(typeList.value[0].id)
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
