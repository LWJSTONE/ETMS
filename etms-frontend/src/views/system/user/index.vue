<template>
  <div class="user-management">
    <!-- 搜索区域 -->
    <el-card shadow="never" class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="用户名">
          <el-input v-model="searchForm.username" placeholder="请输入用户名" clearable />
        </el-form-item>
        <el-form-item label="真实姓名">
          <el-input v-model="searchForm.realName" placeholder="请输入真实姓名" clearable />
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
          <span>用户列表</span>
          <div class="header-buttons">
            <el-button type="primary" @click="handleAdd">
              <el-icon><Plus /></el-icon>新增
            </el-button>
            <el-button type="success" @click="handleExport">
              <el-icon><Download /></el-icon>导出
            </el-button>
          </div>
        </div>
      </template>
      
      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="realName" label="真实姓名" width="100" />
        <el-table-column prop="gender" label="性别" width="80">
          <template #default="{ row }">
            {{ row.gender === 1 ? '男' : row.gender === 2 ? '女' : '未知' }}
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="email" label="邮箱" width="180" />
        <el-table-column prop="deptName" label="部门" width="120" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="success" link @click="handleAssignRole(row)">分配角色</el-button>
            <el-button type="warning" link @click="handleResetPassword(row)">重置密码</el-button>
            <el-button 
              v-if="row.username !== 'admin'" 
              type="danger" 
              link 
              @click="handleDelete(row)"
            >删除</el-button>
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
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" :disabled="isEdit" />
        </el-form-item>
        <el-form-item v-if="!isEdit" label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码（默认123456）" show-password />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="form.gender">
            <el-radio :value="1">男</el-radio>
            <el-radio :value="2">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="部门" prop="deptId">
          <el-tree-select
            v-model="form.deptId"
            :data="deptTree"
            :props="{ label: 'deptName', value: 'id', children: 'children' }"
            placeholder="请选择部门"
            clearable
            check-strictly
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="职位" prop="positionId">
          <el-select v-model="form.positionId" placeholder="请选择职位" clearable style="width: 100%">
            <el-option
              v-for="position in positionList"
              :key="position.id"
              :label="position.positionName"
              :value="position.id"
            />
          </el-select>
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
    
    <!-- 分配角色对话框 -->
    <el-dialog v-model="roleDialogVisible" title="分配角色" width="500px" @close="handleRoleDialogClose">
      <el-form label-width="80px">
        <el-form-item label="用户名">
          <el-input :value="currentUser.username" disabled />
        </el-form-item>
        <el-form-item label="真实姓名">
          <el-input :value="currentUser.realName" disabled />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="selectedRoleIds" multiple placeholder="请选择角色" style="width: 100%">
            <el-option
              v-for="role in roleList"
              :key="role.id"
              :label="role.roleName"
              :value="role.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="roleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitRole">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { getUserList, getUserDetail, createUser, updateUser, deleteUser, resetPassword, assignRoles, exportUsers } from '@/api/user'
import { getRoleListAll } from '@/api/role'
import { getDeptTree } from '@/api/dept'
import { getPositionList } from '@/api/position'
import type { Dept } from '@/api/types'

const searchForm = reactive({ username: '', realName: '', status: null as number | null })
const tableData = ref<any[]>([])
const loading = ref(false)
const pagination = reactive({ current: 1, size: 10, total: 0 })
const dialogVisible = ref(false)
const isEdit = ref(false)
const dialogTitle = computed(() => isEdit.value ? '编辑用户' : '新增用户')
const formRef = ref<FormInstance>()
const form = reactive({
  id: null as number | null,
  username: '',
  password: '',  // 新增：密码字段
  realName: '',
  gender: 1,
  phone: '',
  email: '',
  deptId: null as number | null,
  positionId: null as number | null,
  status: 1
})

// 分配角色相关
const roleDialogVisible = ref(false)
const roleList = ref<any[]>([])
const selectedRoleIds = ref<number[]>([])
const currentUser = reactive({ id: 0, username: '', realName: '' })

// 部门和职位数据
const deptTree = ref<Dept[]>([])
const positionList = ref<any[]>([])

// 手机号验证规则
const phoneValidator = (_rule: any, value: string, callback: (error?: Error) => void) => {
  if (!value) {
    callback()
    return
  }
  const phoneReg = /^1[3-9]\d{9}$/
  if (!phoneReg.test(value)) {
    callback(new Error('请输入正确的手机号格式'))
  } else {
    callback()
  }
}

// 邮箱验证规则
const emailValidator = (_rule: any, value: string, callback: (error?: Error) => void) => {
  if (!value) {
    callback()
    return
  }
  const emailReg = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/
  if (!emailReg.test(value)) {
    callback(new Error('请输入正确的邮箱格式'))
  } else {
    callback()
  }
}

const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度为3-20个字符', trigger: 'blur' },
    { pattern: /^[a-zA-Z][a-zA-Z0-9_]*$/, message: '用户名必须以字母开头，只能包含字母、数字和下划线', trigger: 'blur' }
  ],
  password: [{ 
    required: true, 
    message: '请输入密码', 
    trigger: 'blur',
    validator: (_rule: any, value: string, callback: (error?: Error) => void) => {
      if (!isEdit.value && (!value || value.trim() === '')) {
        callback(new Error('请输入密码'))
      } else {
        callback()
      }
    }
  }],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  phone: [{ validator: phoneValidator, trigger: 'blur' }],
  email: [{ validator: emailValidator, trigger: 'blur' }]
}

const getList = async () => {
  loading.value = true
  try {
    const res = await getUserList({ current: pagination.current, size: pagination.size, ...searchForm })
    tableData.value = res.data?.records || []
    pagination.total = res.data?.total || 0
  } catch (error: any) {
    console.error('获取用户列表失败:', error)
    ElMessage.error(error.message || '获取用户列表失败')
  } finally {
    loading.value = false
  }
}

const getRoleList = async () => {
  try {
    const res = await getRoleListAll()
    roleList.value = res.data || []
  } catch (error: any) {
    console.error('获取角色列表失败:', error)
    ElMessage.error(error.message || '获取角色列表失败')
  }
}

const getDeptTreeData = async () => {
  try {
    const res = await getDeptTree()
    deptTree.value = res.data || []
  } catch (error: any) {
    console.error('获取部门树失败:', error)
    ElMessage.error(error.message || '获取部门树失败')
  }
}

const getPositionListData = async () => {
  try {
    const res = await getPositionList({ current: 1, size: 1000, status: 1 })
    positionList.value = res.data?.records || []
  } catch (error: any) {
    console.error('获取职位列表失败:', error)
    ElMessage.error(error.message || '获取职位列表失败')
  }
}

const handleSearch = () => { pagination.current = 1; getList() }
const handleReset = () => { Object.assign(searchForm, { username: '', realName: '', status: null }); handleSearch() }

const handleAdd = () => { 
  isEdit.value = false
  Object.assign(form, { id: null, username: '', password: '', realName: '', gender: 1, phone: '', email: '', deptId: null, positionId: null, status: 1 })
  dialogVisible.value = true
}

const handleEdit = async (row: any) => { 
  isEdit.value = true
  try {
    // 调用API获取用户详情
    const res = await getUserDetail(row.id)
    const userData = res.data
    Object.assign(form, { 
      id: userData.id, 
      username: userData.username, 
      password: '',  // 编辑时清空密码
      realName: userData.realName, 
      gender: userData.gender || 1, 
      phone: userData.phone || '', 
      email: userData.email || '', 
      deptId: userData.deptId || null,
      positionId: userData.positionId || null,
      status: userData.status 
    })
  } catch (error) {
    console.warn('获取用户详情失败，使用表格数据:', error)
    // 如果API调用失败，使用表格行数据
    Object.assign(form, { 
      id: row.id, 
      username: row.username, 
      password: '',
      realName: row.realName, 
      gender: row.gender || 1, 
      phone: row.phone || '', 
      email: row.email || '', 
      deptId: row.deptId || null,
      positionId: row.positionId || null,
      status: row.status 
    })
  }
  dialogVisible.value = true
}

const handleDelete = async (row: any) => {
  // 禁止删除admin账户
  if (row.username === 'admin') {
    ElMessage.warning('admin账户不能删除')
    return
  }
  try {
    await ElMessageBox.confirm('确定要删除该用户吗？', '提示', { type: 'warning' })
    await deleteUser(row.id)
    ElMessage.success('删除成功')
    getList()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error(error)
      ElMessage.error(error.message || '删除失败')
    }
  }
}

const handleResetPassword = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定要重置该用户的密码吗？', '提示', { type: 'warning' })
    const res = await resetPassword(row.id)
    // 显示新密码给管理员
    const newPassword = res.data
    if (newPassword) {
      ElMessageBox.alert(`新密码为: ${newPassword}\n\n请将此密码告知用户，建议用户登录后立即修改密码。`, '密码重置成功', { 
        type: 'success',
        confirmButtonText: '确定'
      })
    } else {
      ElMessage.success('密码已重置为新密码: 123456')
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('重置密码失败:', error)
      ElMessage.error(error.message || '重置密码失败')
    }
  }
}

// 分配角色
const handleAssignRole = async (row: any) => {
  currentUser.id = row.id
  currentUser.username = row.username
  currentUser.realName = row.realName
  
  // 获取角色列表
  if (roleList.value.length === 0) {
    await getRoleList()
  }
  
  // 调用API获取用户详情（包含角色信息）
  try {
    const res = await getUserDetail(row.id)
    // 从用户详情中获取角色ID列表
    selectedRoleIds.value = res.data.roles?.map((r: any) => r.id) || []
  } catch (error) {
    console.error('获取用户详情失败:', error)
    // 如果获取失败，使用列表中的角色信息作为降级方案
    selectedRoleIds.value = row.roles?.map((r: any) => r.id) || []
  }
  
  roleDialogVisible.value = true
}

const handleSubmitRole = async () => {
  try {
    await assignRoles(currentUser.id, selectedRoleIds.value)
    ElMessage.success('角色分配成功')
    roleDialogVisible.value = false
    getList()
  } catch (error: any) {
    console.error('角色分配失败:', error)
    ElMessage.error(error.message || '角色分配失败')
  }
}

const handleSubmit = async () => {
  try {
    const valid = await formRef.value?.validate()
    if (!valid) return
  } catch {
    return
  }
  try {
    if (isEdit.value) { 
      // 编辑时只传递需要的字段
      const updateData = {
        realName: form.realName,
        gender: form.gender,
        phone: form.phone,
        email: form.email,
        deptId: form.deptId,
        positionId: form.positionId,
        status: form.status
      }
      await updateUser(form.id!, updateData)
      ElMessage.success('更新成功')
    } else { 
      // 新增时传递所有字段
      const createData = {
        username: form.username,
        password: form.password,
        realName: form.realName,
        gender: form.gender,
        phone: form.phone,
        email: form.email,
        deptId: form.deptId,
        positionId: form.positionId,
        status: form.status
      }
      await createUser(createData)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    getList()
  } catch (error: any) { 
    console.error(error)
    ElMessage.error(error.message || '操作失败')
  }
}

// 导出用户
const handleExport = async () => {
  try {
    // 修复：限制导出数量，避免请求超时，与后端限制保持一致
    const exportSize = Math.min(pagination.total, 10000)
    if (pagination.total > 10000) {
      ElMessage.warning(`数据量超过10000条，将只导出前${exportSize}条数据`)
    }
    const blob = await exportUsers({ ...searchForm, current: 1, size: exportSize })
    // 创建下载链接
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `用户数据_${new Date().toISOString().slice(0, 10)}.csv`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  }
}

// 对话框关闭时重置表单
const handleDialogClose = () => {
  formRef.value?.resetFields()
  formRef.value?.clearValidate()
}

// 分配角色对话框关闭时重置
const handleRoleDialogClose = () => {
  currentUser.id = 0
  currentUser.username = ''
  currentUser.realName = ''
  selectedRoleIds.value = []
}

onMounted(() => { getList(); getRoleList(); getDeptTreeData(); getPositionListData() })
</script>

<style lang="scss" scoped>
.user-management {
  .search-card { margin-bottom: 20px; }
  .table-card .card-header { 
    display: flex; 
    justify-content: space-between; 
    align-items: center;
    
    .header-buttons {
      display: flex;
      gap: 10px;
    }
  }
  .el-pagination { margin-top: 20px; justify-content: flex-end; }
}
</style>
