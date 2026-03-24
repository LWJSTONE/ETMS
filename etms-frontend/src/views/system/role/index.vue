<template>
  <div class="role-management">
    <!-- 搜索区域 -->
    <el-card shadow="never" class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="角色名称">
          <el-input v-model="searchForm.roleName" placeholder="请输入角色名称" clearable />
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
          <span>角色列表</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>新增
          </el-button>
        </div>
      </template>
      
      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column prop="roleName" label="角色名称" width="150" />
        <el-table-column prop="roleCode" label="角色编码" width="150" />
        <el-table-column prop="roleDesc" label="角色描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="dataScope" label="数据范围" width="120">
          <template #default="{ row }">
            {{ dataScopeMap[row.dataScope] || '未知' }}
          </template>
        </el-table-column>
        <el-table-column prop="permissionCount" label="权限数量" width="100" />
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column prop="status" label="状态" width="80">
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
            <el-button type="warning" link @click="handleAssignPermission(row)">分配权限</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <el-pagination
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next"
        @size-change="getList"
        @current-change="getList"
      />
    </el-card>
    
    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" @close="handleDialogClose">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="form.roleName" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="角色编码" prop="roleCode">
          <el-input v-model="form.roleCode" placeholder="请输入角色编码" />
        </el-form-item>
        <el-form-item label="角色描述" prop="roleDesc">
          <el-input v-model="form.roleDesc" type="textarea" :rows="3" placeholder="请输入角色描述" />
        </el-form-item>
        <el-form-item label="数据范围" prop="dataScope">
          <el-select v-model="form.dataScope" placeholder="请选择数据范围">
            <el-option label="全部数据" :value="1" />
            <el-option label="本部门数据" :value="2" />
            <el-option label="本人数据" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" :max="999" />
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
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
    
    <!-- 分配权限对话框 -->
    <el-dialog v-model="permDialogVisible" title="分配权限" width="500px" @close="handlePermDialogClose">
      <el-tree
        ref="permTreeRef"
        :data="permissionTree"
        :props="{ label: 'permName', children: 'children' }"
        show-checkbox
        node-key="id"
        default-expand-all
        :default-checked-keys="checkedPermIds"
      />
      <template #footer>
        <el-button @click="permDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handlePermSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { 
  getRoleList, 
  createRole, 
  updateRole, 
  deleteRole, 
  assignPermissions, 
  getRolePermissions,
  getPermissionTree,
  type PermissionTreeNode
} from '@/api/role'

// 数据范围映射
const dataScopeMap: Record<number, string> = {
  1: '全部数据',
  2: '本部门数据',
  3: '本人数据'
}

// 搜索表单
const searchForm = reactive({ 
  roleName: '', 
  status: null as number | null 
})

// 表格数据
const tableData = ref<any[]>([])
const loading = ref(false)
const pagination = reactive({ current: 1, size: 10, total: 0 })

// 新增/编辑对话框
const dialogVisible = ref(false)
const isEdit = ref(false)
const dialogTitle = computed(() => isEdit.value ? '编辑角色' : '新增角色')
const formRef = ref<FormInstance>()
const form = reactive({
  id: null as number | null,
  roleName: '',
  roleCode: '',
  roleDesc: '',
  dataScope: 3,
  sortOrder: 0,
  status: 1
})

// 表单验证规则
const rules: FormRules = {
  roleName: [
    { required: true, message: '请输入角色名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  roleCode: [
    { required: true, message: '请输入角色编码', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  dataScope: [{ required: true, message: '请选择数据范围', trigger: 'change' }]
}

// 权限分配对话框
const permDialogVisible = ref(false)
const permTreeRef = ref()
const currentRoleId = ref<number | null>(null)
const checkedPermIds = ref<number[]>([])

// 权限树数据
const permissionTree = ref<PermissionTreeNode[]>([])
const permissionTreeLoading = ref(false)

// 获取列表
const getList = async () => {
  loading.value = true
  try {
    const res = await getRoleList({ 
      current: pagination.current, 
      size: pagination.size, 
      ...searchForm 
    })
    tableData.value = res.data?.records || []
    pagination.total = res.data?.total || 0
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 获取权限树数据
const getPermissionTreeData = async () => {
  permissionTreeLoading.value = true
  try {
    const res = await getPermissionTree()
    permissionTree.value = res.data || []
  } catch (error: any) {
    console.error('获取权限树失败:', error)
    ElMessage.error(error.message || '获取权限树失败，请刷新页面重试')
    permissionTree.value = []
  } finally {
    permissionTreeLoading.value = false
  }
}

// 搜索
const handleSearch = () => { 
  pagination.current = 1
  getList() 
}

// 重置
const handleReset = () => { 
  Object.assign(searchForm, { roleName: '', status: null })
  handleSearch() 
}

// 新增
const handleAdd = () => { 
  isEdit.value = false
  Object.assign(form, { 
    id: null, 
    roleName: '', 
    roleCode: '', 
    roleDesc: '', 
    dataScope: 3, 
    sortOrder: 0, 
    status: 1 
  })
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row: any) => { 
  isEdit.value = true
  // 明确指定需要复制的字段，避免传递额外数据
  Object.assign(form, {
    id: row.id,
    roleName: row.roleName,
    roleCode: row.roleCode,
    roleDesc: row.roleDesc,
    dataScope: row.dataScope,
    sortOrder: row.sortOrder,
    status: row.status
  })
  dialogVisible.value = true
}

// 删除
const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定要删除该角色吗？', '提示', { type: 'warning' })
    await deleteRole(row.id)
    ElMessage.success('删除成功')
    getList()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

// 分配权限
const handleAssignPermission = async (row: any) => {
  currentRoleId.value = row.id
  try {
    const res = await getRolePermissions(row.id)
    checkedPermIds.value = res.data || []
    permDialogVisible.value = true
  } catch (error: any) {
    ElMessage.error(error.message || '获取权限失败')
  }
}

// 提交权限分配
const handlePermSubmit = async () => {
  if (!currentRoleId.value) return
  // 使用 getHalfCheckedKeys() 获取半选状态的父节点，确保完整权限列表
  const checkedKeys = permTreeRef.value?.getCheckedKeys() || []
  const halfCheckedKeys = permTreeRef.value?.getHalfCheckedKeys() || []
  const allPermissionIds = [...checkedKeys, ...halfCheckedKeys]
  try {
    await assignPermissions(currentRoleId.value, allPermissionIds)
    ElMessage.success('权限分配成功')
    permDialogVisible.value = false
    getList()
  } catch (error: any) {
    ElMessage.error(error.message || '权限分配失败')
  }
}

// 提交表单
const handleSubmit = async () => {
  const valid = await formRef.value?.validate()
  if (!valid) return
  try {
    if (isEdit.value) { 
      // 编辑时排除id字段后再传递
      const { id, ...updateData } = form
      await updateRole(form.id!, updateData)
      ElMessage.success('更新成功') 
    } else { 
      await createRole(form)
      ElMessage.success('新增成功') 
    }
    dialogVisible.value = false
    getList()
  } catch (error: any) { 
    ElMessage.error(error.message || '操作失败')
  }
}

onMounted(() => { 
  getList()
  getPermissionTreeData()
})

// 对话框关闭时重置表单
const handleDialogClose = () => {
  formRef.value?.resetFields()
  formRef.value?.clearValidate()
}

// 权限对话框关闭时重置
const handlePermDialogClose = () => {
  currentRoleId.value = null
  checkedPermIds.value = []
  permTreeRef.value?.setCheckedKeys([])
}
</script>

<style lang="scss" scoped>
.role-management {
  .search-card { 
    margin-bottom: 20px; 
  }
  .table-card .card-header { 
    display: flex; 
    justify-content: space-between; 
    align-items: center; 
  }
  .el-pagination { 
    margin-top: 20px; 
    justify-content: flex-end; 
  }
}
</style>
