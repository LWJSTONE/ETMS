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
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>新增
          </el-button>
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
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
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
    <el-dialog v-model="roleDialogVisible" title="分配角色" width="500px">
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
import { getUserList, getUserDetail, createUser, updateUser, deleteUser, resetPassword, assignRoles } from '@/api/user'
import { getRoleListAll } from '@/api/role'

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
  status: 1
})

// 分配角色相关
const roleDialogVisible = ref(false)
const roleList = ref<any[]>([])
const selectedRoleIds = ref<number[]>([])
const currentUser = reactive({ id: 0, username: '', realName: '' })

// 手机号验证规则
const phoneValidator = (rule: any, value: string, callback: any) => {
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
const emailValidator = (rule: any, value: string, callback: any) => {
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
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
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
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const getRoleList = async () => {
  try {
    const res = await getRoleListAll()
    roleList.value = res.data || []
  } catch (error) {
    console.error(error)
  }
}

const handleSearch = () => { pagination.current = 1; getList() }
const handleReset = () => { Object.assign(searchForm, { username: '', realName: '', status: null }); handleSearch() }

const handleAdd = () => { 
  isEdit.value = false
  Object.assign(form, { id: null, username: '', password: '', realName: '', gender: 1, phone: '', email: '', status: 1 })
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
  await ElMessageBox.confirm('确定要删除该用户吗？', '提示', { type: 'warning' })
  await deleteUser(row.id)
  ElMessage.success('删除成功')
  getList()
}

const handleResetPassword = async (row: any) => {
  await ElMessageBox.confirm('确定要重置该用户的密码吗？重置后的新密码将发送到用户手机或邮箱。', '提示', { type: 'warning' })
  await resetPassword(row.id)
  ElMessage.success('密码已重置，新密码已发送给用户')
}

// 分配角色
const handleAssignRole = async (row: any) => {
  currentUser.id = row.id
  currentUser.username = row.username
  currentUser.realName = row.realName
  
  // 获取用户当前的角色
  selectedRoleIds.value = row.roles?.map((r: any) => r.id) || []
  
  // 获取角色列表
  if (roleList.value.length === 0) {
    await getRoleList()
  }
  
  roleDialogVisible.value = true
}

const handleSubmitRole = async () => {
  try {
    await assignRoles(currentUser.id, selectedRoleIds.value)
    ElMessage.success('角色分配成功')
    roleDialogVisible.value = false
    getList()
  } catch (error) {
    console.error(error)
  }
}

const handleSubmit = async () => {
  const valid = await formRef.value?.validate()
  if (!valid) return
  try {
    if (isEdit.value) { 
      await updateUser(form.id!, form)
      ElMessage.success('更新成功')
    } else { 
      await createUser(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    getList()
  } catch (error) { console.error(error) }
}

onMounted(() => { getList(); getRoleList() })
</script>

<style lang="scss" scoped>
.user-management {
  .search-card { margin-bottom: 20px; }
  .table-card .card-header { display: flex; justify-content: space-between; align-items: center; }
  .el-pagination { margin-top: 20px; justify-content: flex-end; }
}
</style>
