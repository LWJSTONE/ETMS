<template>
  <div class="config-management">
    <!-- 搜索区域 -->
    <el-card shadow="never" class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="参数名称">
          <el-input 
            v-model="searchForm.configName" 
            placeholder="请输入参数名称" 
            clearable 
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="参数键名">
          <el-input 
            v-model="searchForm.configKey" 
            placeholder="请输入参数键名" 
            clearable 
            style="width: 200px"
          />
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
          <span>参数列表</span>
          <div class="header-buttons">
            <el-button type="warning" @click="handleRefreshCache">
              <el-icon><RefreshRight /></el-icon>刷新缓存
            </el-button>
            <el-button type="primary" @click="handleAdd">
              <el-icon><Plus /></el-icon>新增
            </el-button>
          </div>
        </div>
      </template>
      
      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column prop="configName" label="参数名称" width="180" show-overflow-tooltip />
        <el-table-column prop="configKey" label="参数键名" width="200" show-overflow-tooltip />
        <el-table-column prop="configValue" label="参数键值" width="150" show-overflow-tooltip />
        <el-table-column prop="sortOrder" label="排序" width="80" align="center" />
        <el-table-column prop="configType" label="系统内置" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.configType === 'Y' ? 'primary' : 'info'">
              {{ row.configType === 'Y' ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button 
              type="danger" 
              link 
              @click="handleDelete(row)"
              :disabled="row.configType === 'Y'"
            >
              删除
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
    
    <!-- 新增/编辑对话框 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="dialogTitle" 
      width="600px"
      :close-on-click-modal="false"
      @close="handleDialogClose"
    >
      <el-form 
        ref="formRef" 
        :model="form" 
        :rules="rules" 
        label-width="100px"
      >
        <el-form-item label="参数名称" prop="configName">
          <el-input 
            v-model="form.configName" 
            placeholder="请输入参数名称" 
            maxlength="100"
          />
        </el-form-item>
        <el-form-item label="参数键名" prop="configKey">
          <el-input 
            v-model="form.configKey" 
            placeholder="请输入参数键名" 
            maxlength="100"
            :disabled="isEdit && form.configType === 'Y'"
          />
        </el-form-item>
        <el-form-item label="参数键值" prop="configValue">
          <el-input 
            v-model="form.configValue" 
            placeholder="请输入参数键值" 
            maxlength="500"
          />
        </el-form-item>
        <el-form-item label="系统内置" prop="configType">
          <el-radio-group v-model="form.configType" :disabled="isEdit">
            <el-radio value="Y">是</el-radio>
            <el-radio value="N">否</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">正常</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input 
            v-model="form.remark" 
            type="textarea" 
            :rows="3" 
            placeholder="请输入备注"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import {
  getConfigList,
  createConfig,
  updateConfig,
  deleteConfig,
  refreshConfigCache
} from '@/api/config'
import type { Config } from '@/api/config'

// 定义配置项类型（使用API中的Config类型）
type ConfigItem = Config

// 搜索表单
const searchForm = reactive({
  configName: '',
  configKey: ''
})

// 表格数据
const tableData = ref<ConfigItem[]>([])
const loading = ref(false)
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 新增/编辑对话框
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const dialogTitle = computed(() => isEdit.value ? '编辑参数' : '新增参数')
const formRef = ref<FormInstance>()
const form = reactive({
  id: null as number | null,
  configName: '',
  configKey: '',
  configValue: '',
  configType: 'N' as 'Y' | 'N',
  remark: '',
  sortOrder: 0,
  status: 1
})

// 表单验证规则
const rules: FormRules = {
  configName: [
    { required: true, message: '请输入参数名称', trigger: 'blur' },
    { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  configKey: [
    { required: true, message: '请输入参数键名', trigger: 'blur' },
    { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' },
    { pattern: /^[a-zA-Z][a-zA-Z0-9._-]*$/, message: '以字母开头，只能包含字母、数字、点、下划线和横线', trigger: 'blur' }
  ],
  configValue: [
    { required: true, message: '请输入参数键值', trigger: 'blur' }
  ],
  configType: [
    { required: true, message: '请选择是否系统内置', trigger: 'change' }
  ]
}

// 获取列表
const getList = async () => {
  loading.value = true
  try {
    const res = await getConfigList({
      current: pagination.current,
      size: pagination.size,
      configName: searchForm.configName || undefined,
      configKey: searchForm.configKey || undefined
    })
    if (res) {
      tableData.value = res?.records
      pagination.total = res?.total
    }
  } catch (error: any) {
    ElMessage.error(error.message || '获取配置列表失败')
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
    configName: '',
    configKey: ''
  })
  handleSearch()
}

// 新增
const handleAdd = () => {
  isEdit.value = false
  Object.assign(form, {
    id: null,
    configName: '',
    configKey: '',
    configValue: '',
    configType: 'N',
    remark: '',
    sortOrder: 0,
    status: 1
  })
  dialogVisible.value = true
  // 使用 nextTick 确保表单已渲染后再清除验证
  nextTick(() => {
    formRef.value?.clearValidate()
  })
}

// 编辑
const handleEdit = (row: ConfigItem) => {
  isEdit.value = true
  Object.assign(form, {
    id: row.id,
    configName: row.configName,
    configKey: row.configKey,
    configValue: row.configValue,
    configType: row.configType,
    remark: row.remark,
    sortOrder: row.sortOrder || 0,
    status: row.status
  })
  dialogVisible.value = true
  // 使用 nextTick 确保表单已渲染后再清除验证
  nextTick(() => {
    formRef.value?.clearValidate()
  })
}

// 删除
const handleDelete = async (row: ConfigItem) => {
  if (row.configType === 'Y') {
    ElMessage.warning('系统内置参数不能删除')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要删除参数「${row.configName}」吗？`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const res = await deleteConfig(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      getList()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

// 刷新缓存
const handleRefreshCache = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要刷新系统参数缓存吗？',
      '刷新缓存',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    loading.value = true
    const res = await refreshConfigCache()
    if (res.code === 200) {
      ElMessage.success('缓存刷新成功')
    } else {
      ElMessage.error(res.message || '缓存刷新失败')
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '缓存刷新失败')
    }
  } finally {
    loading.value = false
  }
}

// 提交表单
const handleSubmit = async () => {
  const valid = await formRef.value?.validate()
  if (!valid) return
  
  submitLoading.value = true
  try {
    const data = {
      configName: form.configName,
      configKey: form.configKey,
      configValue: form.configValue,
      configType: form.configType,
      remark: form.remark,
      sortOrder: form.sortOrder,
      status: form.status
    }
    
    if (isEdit.value && form.id) {
      // 编辑
      const res = await updateConfig(form.id, data)
      if (res.code === 200) {
        ElMessage.success('更新成功')
        dialogVisible.value = false
        getList()
      } else {
        ElMessage.error(res.message || '更新失败')
      }
    } else {
      // 新增
      const res = await createConfig(data)
      if (res.code === 200) {
        ElMessage.success('新增成功')
        dialogVisible.value = false
        getList()
      } else {
        ElMessage.error(res.message || '新增失败')
      }
    }
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  } finally {
    submitLoading.value = false
  }
}

onMounted(() => {
  getList()
})

// 对话框关闭时重置表单
const handleDialogClose = () => {
  formRef.value?.resetFields()
  formRef.value?.clearValidate()
}
</script>

<style lang="scss" scoped>
.config-management {
  .search-card {
    margin-bottom: 20px;
  }
  
  .table-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      .header-buttons {
        display: flex;
        gap: 10px;
      }
    }
  }
  
  .el-pagination {
    margin-top: 20px;
    justify-content: flex-end;
  }
}
</style>
