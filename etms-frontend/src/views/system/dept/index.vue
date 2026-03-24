<template>
  <div class="dept-management">
    <!-- 搜索区域 -->
    <el-card shadow="never" class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="部门名称">
          <el-input v-model="searchForm.deptName" placeholder="请输入部门名称" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="正常" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格 -->
    <el-card shadow="never" class="table-card">
      <template #header>
        <div class="card-header">
          <span>部门列表</span>
          <div class="header-buttons">
            <el-button type="primary" @click="handleAdd">
              <el-icon><Plus /></el-icon>新增
            </el-button>
            <el-button @click="toggleExpandAll">
              <el-icon><Sort /></el-icon>{{ isExpandAll ? '折叠' : '展开' }}
            </el-button>
          </div>
        </div>
      </template>

      <el-table
        ref="tableRef"
        :data="tableData"
        v-loading="loading"
        row-key="id"
        :default-expand-all="isExpandAll"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        stripe
        border
      >
        <el-table-column prop="deptName" label="部门名称" min-width="180" />
        <el-table-column prop="deptCode" label="部门编码" width="120" />
        <el-table-column prop="leaderName" label="负责人" width="100" />
        <el-table-column prop="sortOrder" label="排序" width="80" align="center" />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="success" link @click="handleAddChild(row)">新增</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="550px" @close="handleDialogClose">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="上级部门" prop="parentId">
          <el-tree-select
            v-model="form.parentId"
            :data="deptTreeOptions"
            :props="{ value: 'id', label: 'deptName', children: 'children' }"
            value-key="id"
            placeholder="请选择上级部门"
            check-strictly
            clearable
            :render-after-expand="false"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="部门名称" prop="deptName">
          <el-input v-model="form.deptName" placeholder="请输入部门名称" />
        </el-form-item>
        <el-form-item label="部门编码" prop="deptCode">
          <el-input v-model="form.deptCode" placeholder="请输入部门编码" />
        </el-form-item>
        <el-form-item label="负责人" prop="leaderId">
          <el-select v-model="form.leaderId" placeholder="请选择负责人" clearable filterable style="width: 100%">
            <el-option
              v-for="user in userOptions"
              :key="user.id"
              :label="user.realName"
              :value="user.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" :max="999" controls-position="right" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">正常</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
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
import { getDeptTree, getDeptDetail, createDept, updateDept, deleteDept } from '@/api/dept'
import { getUserList } from '@/api/user'

// 类型定义
interface Dept {
  id: number
  parentId: number | null
  deptName: string
  deptCode: string
  leaderId: number | null
  leaderName: string
  sortOrder: number
  level: number
  ancestors: string
  status: number
  children?: Dept[]
  createTime?: string
}

interface User {
  id: number
  realName: string
}

// 搜索表单
const searchForm = reactive({
  deptName: '',
  status: null as number | null
})

// 表格相关
const tableRef = ref()
const tableData = ref<Dept[]>([])
const loading = ref(false)
const isExpandAll = ref(true)

// 对话框相关
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const dialogTitle = computed(() => isEdit.value ? '编辑部门' : '新增部门')
const formRef = ref<FormInstance>()

// 部门树选项（用于选择父部门）
const deptTreeOptions = ref<Dept[]>([])
// 用户选项（用于选择负责人）
const userOptions = ref<User[]>([])

// 表单数据
const form = reactive({
  id: null as number | null,
  parentId: null as number | null,
  deptName: '',
  deptCode: '',
  leaderId: null as number | null,
  sortOrder: 0,
  status: 1
})

// 表单验证规则
const rules: FormRules = {
  deptName: [{ required: true, message: '请输入部门名称', trigger: 'blur' }],
  deptCode: [{ required: true, message: '请输入部门编码', trigger: 'blur' }]
}

// 获取部门树形数据
const getDeptTreeData = async () => {
  loading.value = true
  try {
    const res = await getDeptTree()
    const treeData = res.data || []
    // 根据搜索条件过滤
    if (searchForm.deptName || searchForm.status !== null) {
      tableData.value = filterTree(treeData, searchForm)
    } else {
      tableData.value = treeData
    }
    // 设置部门树选项（添加顶级节点选项）
    deptTreeOptions.value = [
      { id: 0, parentId: null, deptName: '顶级部门', deptCode: '', leaderId: null, leaderName: '', sortOrder: 0, level: 0, ancestors: '', status: 1, children: treeData }
    ] as Dept[]
  } catch (error) {
    console.error('获取部门树失败:', error)
    ElMessage.error('获取部门数据失败')
  } finally {
    loading.value = false
  }
}

// 过滤树形数据
const filterTree = (tree: Dept[], params: { deptName: string; status: number | null }): Dept[] => {
  const result: Dept[] = []
  for (const node of tree) {
    // 检查当前节点是否匹配
    const nameMatch = !params.deptName || node.deptName.includes(params.deptName)
    const statusMatch = params.status === null || node.status === params.status
    const isMatch = nameMatch && statusMatch

    // 递归过滤子节点
    const children = node.children ? filterTree(node.children, params) : []

    // 如果当前节点匹配或有匹配的子节点，则保留
    if (isMatch || children.length > 0) {
      result.push({ ...node, children: children.length > 0 ? children : undefined })
    }
  }
  return result
}

// 获取用户列表（用于负责人选择）
const getUserOptions = async () => {
  try {
    const res = await getUserList({ current: 1, size: 1000, status: 1 })
    userOptions.value = res.data?.records || []
  } catch (error) {
    console.error('获取用户列表失败:', error)
  }
}

// 搜索
const handleSearch = () => {
  getDeptTreeData()
}

// 重置
const handleReset = () => {
  Object.assign(searchForm, { deptName: '', status: null })
  getDeptTreeData()
}

// 展开/折叠所有
const toggleExpandAll = () => {
  isExpandAll.value = !isExpandAll.value
  // 刷新表格以应用展开状态
  nextTick(() => {
    getDeptTreeData()
  })
}

// 新增
const handleAdd = () => {
  isEdit.value = false
  // 先重置表单，再设置 parentId
  Object.assign(form, {
    id: null,
    parentId: null,
    deptName: '',
    deptCode: '',
    leaderId: null,
    sortOrder: 0,
    status: 1
  })
  // 重置表单验证状态
  nextTick(() => {
    formRef.value?.clearValidate()
  })
  dialogVisible.value = true
}

// 新增子部门
const handleAddChild = (row: Dept) => {
  isEdit.value = false
  // 先重置表单，再设置 parentId
  Object.assign(form, {
    id: null,
    parentId: row.id,
    deptName: '',
    deptCode: '',
    leaderId: null,
    sortOrder: 0,
    status: 1
  })
  // 重置表单验证状态
  nextTick(() => {
    formRef.value?.clearValidate()
  })
  dialogVisible.value = true
}

// 编辑
const handleEdit = async (row: Dept) => {
  isEdit.value = true
  try {
    const res = await getDeptDetail(row.id)
    const data = res.data
    resetForm()
    Object.assign(form, {
      id: data.id,
      parentId: data.parentId || null,
      deptName: data.deptName,
      deptCode: data.deptCode,
      leaderId: data.leaderId,
      sortOrder: data.sortOrder || 0,
      status: data.status ?? 1
    })
    dialogVisible.value = true
  } catch (error) {
    console.error('获取部门详情失败:', error)
    ElMessage.error('获取部门详情失败')
  }
}

// 删除
const handleDelete = async (row: Dept) => {
  // 检查是否有子部门
  if (row.children && row.children.length > 0) {
    ElMessage.warning('存在子部门，不能删除')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定要删除部门【${row.deptName}】吗？`,
      '提示',
      { type: 'warning' }
    )

    await deleteDept(row.id)
    ElMessage.success('删除成功')
    getDeptTreeData()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除部门失败:', error)
      ElMessage.error(error.message || '删除失败')
    }
  }
}

// 重置表单
const resetForm = () => {
  Object.assign(form, {
    id: null,
    parentId: null,
    deptName: '',
    deptCode: '',
    leaderId: null,
    sortOrder: 0,
    status: 1
  })
  formRef.value?.resetFields()
}

// 对话框关闭
const handleDialogClose = () => {
  resetForm()
}

// 提交表单
const handleSubmit = async () => {
  const valid = await formRef.value?.validate()
  if (!valid) return

  submitLoading.value = true
  try {
    const data = {
      parentId: form.parentId || null,
      deptName: form.deptName,
      deptCode: form.deptCode,
      leaderId: form.leaderId,
      sortOrder: form.sortOrder,
      status: form.status
    }

    if (isEdit.value) {
      await updateDept(form.id!, data)
      ElMessage.success('更新成功')
    } else {
      await createDept(data)
      ElMessage.success('新增成功')
    }

    dialogVisible.value = false
    getDeptTreeData()
  } catch (error: any) {
    console.error('保存部门失败:', error)
    ElMessage.error(error?.response?.data?.message || '保存失败')
  } finally {
    submitLoading.value = false
  }
}

onMounted(() => {
  getDeptTreeData()
  getUserOptions()
})
</script>

<style lang="scss" scoped>
.dept-management {
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
}
</style>
