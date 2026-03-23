<template>
  <div class="category-management">
    <!-- 搜索区域 -->
    <el-card shadow="never" class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="分类名称">
          <el-input v-model="searchForm.categoryName" placeholder="请输入分类名称" clearable />
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
          <span>分类列表</span>
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
        <el-table-column prop="categoryName" label="分类名称" min-width="180" />
        <el-table-column prop="categoryCode" label="分类编码" width="120" />
        <el-table-column prop="sortOrder" label="排序" width="80" align="center" />
        <el-table-column prop="status" label="状态" width="80" align="center">
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
            <el-button type="success" link @click="handleAddChild(row)">新增</el-button>
            <el-button
              :type="row.status === 1 ? 'warning' : 'success'"
              link
              @click="handleToggleStatus(row)"
            >
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="550px" @close="handleDialogClose">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="上级分类" prop="parentId">
          <el-tree-select
            v-model="form.parentId"
            :data="categoryTreeOptions"
            :props="{ value: 'id', label: 'categoryName', children: 'children' }"
            value-key="id"
            placeholder="请选择上级分类"
            check-strictly
            clearable
            :render-after-expand="false"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="分类名称" prop="categoryName">
          <el-input v-model="form.categoryName" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="分类编码" prop="categoryCode">
          <el-input v-model="form.categoryCode" placeholder="请输入分类编码" />
        </el-form-item>
        <el-form-item label="分类类型" prop="categoryType">
          <el-radio-group v-model="form.categoryType">
            <el-radio :value="1">课程分类</el-radio>
            <el-radio :value="2">题目分类</el-radio>
          </el-radio-group>
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
import {
  getCategoryTree,
  getCategoryDetail,
  createCategory,
  updateCategory,
  deleteCategory,
  updateCategoryStatus,
  type Category
} from '@/api/category'

// 模拟数据 - 当后端接口不可用时使用
const mockCategoryData: Category[] = [
  {
    id: 1,
    parentId: null,
    categoryName: '技术培训',
    categoryCode: 'TECH',
    level: 1,
    sortOrder: 1,
    icon: null,
    status: 1,
    createTime: '2024-01-01 10:00:00',
    children: [
      {
        id: 11,
        parentId: 1,
        categoryName: '编程语言',
        categoryCode: 'TECH_LANG',
        level: 2,
        sortOrder: 1,
        icon: null,
        status: 1,
        createTime: '2024-01-01 10:00:00',
        children: [
          {
            id: 111,
            parentId: 11,
            categoryName: 'Java',
            categoryCode: 'TECH_LANG_JAVA',
            level: 3,
            sortOrder: 1,
            icon: null,
            status: 1,
            createTime: '2024-01-01 10:00:00'
          },
          {
            id: 112,
            parentId: 11,
            categoryName: 'Python',
            categoryCode: 'TECH_LANG_PYTHON',
            level: 3,
            sortOrder: 2,
            icon: null,
            status: 1,
            createTime: '2024-01-01 10:00:00'
          }
        ]
      },
      {
        id: 12,
        parentId: 1,
        categoryName: '数据库',
        categoryCode: 'TECH_DB',
        level: 2,
        sortOrder: 2,
        icon: null,
        status: 1,
        createTime: '2024-01-01 10:00:00'
      },
      {
        id: 13,
        parentId: 1,
        categoryName: '前端技术',
        categoryCode: 'TECH_FRONTEND',
        level: 2,
        sortOrder: 3,
        icon: null,
        status: 1,
        createTime: '2024-01-01 10:00:00'
      }
    ]
  },
  {
    id: 2,
    parentId: null,
    categoryName: '管理培训',
    categoryCode: 'MGMT',
    level: 1,
    sortOrder: 2,
    icon: null,
    status: 1,
    createTime: '2024-01-01 10:00:00',
    children: [
      {
        id: 21,
        parentId: 2,
        categoryName: '领导力',
        categoryCode: 'MGMT_LEADER',
        level: 2,
        sortOrder: 1,
        icon: null,
        status: 1,
        createTime: '2024-01-01 10:00:00'
      },
      {
        id: 22,
        parentId: 2,
        categoryName: '项目管理',
        categoryCode: 'MGMT_PROJECT',
        level: 2,
        sortOrder: 2,
        icon: null,
        status: 1,
        createTime: '2024-01-01 10:00:00'
      }
    ]
  },
  {
    id: 3,
    parentId: null,
    categoryName: '业务培训',
    categoryCode: 'BIZ',
    level: 1,
    sortOrder: 3,
    icon: null,
    status: 1,
    createTime: '2024-01-01 10:00:00',
    children: [
      {
        id: 31,
        parentId: 3,
        categoryName: '产品知识',
        categoryCode: 'BIZ_PRODUCT',
        level: 2,
        sortOrder: 1,
        icon: null,
        status: 1,
        createTime: '2024-01-01 10:00:00'
      },
      {
        id: 32,
        parentId: 3,
        categoryName: '销售技巧',
        categoryCode: 'BIZ_SALES',
        level: 2,
        sortOrder: 2,
        icon: null,
        status: 1,
        createTime: '2024-01-01 10:00:00'
      }
    ]
  },
  {
    id: 4,
    parentId: null,
    categoryName: '职业素养',
    categoryCode: 'PRO',
    level: 1,
    sortOrder: 4,
    icon: null,
    status: 1,
    createTime: '2024-01-01 10:00:00',
    children: [
      {
        id: 41,
        parentId: 4,
        categoryName: '沟通技巧',
        categoryCode: 'PRO_COMM',
        level: 2,
        sortOrder: 1,
        icon: null,
        status: 1,
        createTime: '2024-01-01 10:00:00'
      },
      {
        id: 42,
        parentId: 4,
        categoryName: '时间管理',
        categoryCode: 'PRO_TIME',
        level: 2,
        sortOrder: 2,
        icon: null,
        status: 0,
        createTime: '2024-01-01 10:00:00'
      }
    ]
  }
]

// 用于本地存储和操作的数据
let localCategoryData: Category[] = JSON.parse(JSON.stringify(mockCategoryData))
let nextId = 1000 // 用于新增分类时的临时ID

// 搜索表单
const searchForm = reactive({
  categoryName: '',
  status: null as number | null
})

// 表格相关
const tableRef = ref()
const tableData = ref<Category[]>([])
const loading = ref(false)
const isExpandAll = ref(true)

// 对话框相关
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const dialogTitle = computed(() => isEdit.value ? '编辑分类' : '新增分类')
const formRef = ref<FormInstance>()

// 分类树选项（用于选择父分类）
const categoryTreeOptions = ref<Category[]>([])

// 表单数据
const form = reactive({
  id: null as number | null,
  parentId: null as number | null,
  categoryName: '',
  categoryCode: '',
  categoryType: 1 as number,
  sortOrder: 0,
  status: 1
})

// 表单验证规则
const rules: FormRules = {
  categoryName: [{ required: true, message: '请输入分类名称', trigger: 'blur' }],
  categoryCode: [{ required: true, message: '请输入分类编码', trigger: 'blur' }]
}

// 获取分类树形数据
const getCategoryTreeData = async () => {
  loading.value = true
  try {
    const res = await getCategoryTree()
    const treeData = res.data || []
    // 根据搜索条件过滤
    if (searchForm.categoryName || searchForm.status !== null) {
      tableData.value = filterTree(treeData, searchForm)
    } else {
      tableData.value = treeData
    }
    // 设置分类树选项（添加顶级节点选项）
    categoryTreeOptions.value = [
      { id: 0, parentId: null, categoryName: '顶级分类', categoryCode: '', level: 0, sortOrder: 0, icon: null, status: 1, children: treeData }
    ] as Category[]
    // 同步到本地数据
    localCategoryData = JSON.parse(JSON.stringify(treeData))
  } catch (error) {
    console.warn('后端接口不可用，使用模拟数据:', error)
    // 使用模拟数据
    const treeData = JSON.parse(JSON.stringify(localCategoryData))
    if (searchForm.categoryName || searchForm.status !== null) {
      tableData.value = filterTree(treeData, searchForm)
    } else {
      tableData.value = treeData
    }
    // 设置分类树选项（添加顶级节点选项）
    categoryTreeOptions.value = [
      { id: 0, parentId: null, categoryName: '顶级分类', categoryCode: '', level: 0, sortOrder: 0, icon: null, status: 1, children: treeData }
    ] as Category[]
  } finally {
    loading.value = false
  }
}

// 过滤树形数据
const filterTree = (tree: Category[], params: { categoryName: string; status: number | null }): Category[] => {
  const result: Category[] = []
  for (const node of tree) {
    // 检查当前节点是否匹配
    const nameMatch = !params.categoryName || node.categoryName.includes(params.categoryName)
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

// 计算分类层级
const calculateLevel = (parentId: number | null): number => {
  if (parentId === null || parentId === 0) {
    return 1
  }
  const findNode = (nodes: Category[], id: number): Category | null => {
    for (const node of nodes) {
      if (node.id === id) return node
      if (node.children) {
        const found = findNode(node.children, id)
        if (found) return found
      }
    }
    return null
  }
  const parent = findNode(localCategoryData, parentId)
  return parent ? parent.level + 1 : 1
}

// 查找节点及其父节点链
const findNodeAndParents = (nodes: Category[], id: number, parents: Category[] = []): { node: Category | null; parents: Category[] } => {
  for (const node of nodes) {
    if (node.id === id) {
      return { node, parents }
    }
    if (node.children) {
      const result = findNodeAndParents(node.children, id, [...parents, node])
      if (result.node) return result
    }
  }
  return { node: null, parents }
}

// 检查是否是自身的子节点
const isChildNode = (parentId: number, childId: number): boolean => {
  const { node } = findNodeAndParents(localCategoryData, parentId)
  if (!node) return false
  
  const checkChildren = (n: Category): boolean => {
    if (n.id === childId) return true
    if (n.children) {
      return n.children.some(checkChildren)
    }
    return false
  }
  
  return checkChildren(node)
}

// 搜索
const handleSearch = () => {
  getCategoryTreeData()
}

// 重置
const handleReset = () => {
  Object.assign(searchForm, { categoryName: '', status: null })
  getCategoryTreeData()
}

// 展开/折叠所有
const toggleExpandAll = () => {
  isExpandAll.value = !isExpandAll.value
  // 刷新表格以应用展开状态
  nextTick(() => {
    getCategoryTreeData()
  })
}

// 新增
const handleAdd = () => {
  isEdit.value = false
  resetForm()
  form.parentId = null
  dialogVisible.value = true
}

// 新增子分类
const handleAddChild = (row: Category) => {
  isEdit.value = false
  resetForm()
  form.parentId = row.id
  dialogVisible.value = true
}

// 编辑
const handleEdit = async (row: Category) => {
  isEdit.value = true
  try {
    const res = await getCategoryDetail(row.id)
    const data = res.data
    resetForm()
    Object.assign(form, {
      id: data.id,
      parentId: data.parentId || null,
      categoryName: data.categoryName,
      categoryCode: data.categoryCode,
      categoryType: data.categoryType || 1,
      sortOrder: data.sortOrder || 0,
      status: data.status ?? 1
    })
    dialogVisible.value = true
  } catch (error) {
    console.warn('获取分类详情失败，使用本地数据:', error)
    // 使用本地数据
    resetForm()
    Object.assign(form, {
      id: row.id,
      parentId: row.parentId || null,
      categoryName: row.categoryName,
      categoryCode: row.categoryCode,
      categoryType: row.categoryType || 1,
      sortOrder: row.sortOrder || 0,
      status: row.status ?? 1
    })
    dialogVisible.value = true
  }
}

// 删除
const handleDelete = async (row: Category) => {
  // 检查是否有子分类
  if (row.children && row.children.length > 0) {
    ElMessage.warning('存在子分类，不能删除')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定要删除分类【${row.categoryName}】吗？`,
      '提示',
      { type: 'warning' }
    )
    await deleteCategory(row.id)
    ElMessage.success('删除成功')
    getCategoryTreeData()
  } catch (error: any) {
    if (error === 'cancel') return
    console.warn('删除接口不可用，使用本地删除:', error)
    // 本地删除
    const removeNode = (nodes: Category[], id: number): boolean => {
      const index = nodes.findIndex(n => n.id === id)
      if (index !== -1) {
        nodes.splice(index, 1)
        return true
      }
      for (const node of nodes) {
        if (node.children && removeNode(node.children, id)) {
          return true
        }
      }
      return false
    }
    removeNode(localCategoryData, row.id)
    ElMessage.success('删除成功')
    getCategoryTreeData()
  }
}

// 切换状态
const handleToggleStatus = async (row: Category) => {
  const newStatus = row.status === 1 ? 0 : 1
  const statusText = newStatus === 1 ? '启用' : '禁用'

  try {
    await ElMessageBox.confirm(
      `确定要${statusText}分类【${row.categoryName}】吗？`,
      '提示',
      { type: 'warning' }
    )

    try {
      await updateCategoryStatus(row.id, newStatus)
      ElMessage.success(`${statusText}成功`)
      getCategoryTreeData()
    } catch (error) {
      console.warn('状态更新接口不可用，使用本地更新:', error)
      // 本地更新状态
      const updateStatus = (nodes: Category[], id: number, status: number): boolean => {
        for (const node of nodes) {
          if (node.id === id) {
            node.status = status
            return true
          }
          if (node.children && updateStatus(node.children, id, status)) {
            return true
          }
        }
        return false
      }
      updateStatus(localCategoryData, row.id, newStatus)
      ElMessage.success(`${statusText}成功`)
      getCategoryTreeData()
    }
  } catch {
    // 用户取消操作
  }
}

// 重置表单
const resetForm = () => {
  Object.assign(form, {
    id: null,
    parentId: null,
    categoryName: '',
    categoryCode: '',
    categoryType: 1,
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

  // 编辑时，检查父分类不能是自己或自己的子分类
  if (isEdit.value && form.parentId !== null && form.parentId !== 0) {
    if (form.parentId === form.id) {
      ElMessage.warning('父分类不能是自己')
      return
    }
    if (isChildNode(form.id, form.parentId)) {
      ElMessage.warning('父分类不能是自己的子分类')
      return
    }
  }

  submitLoading.value = true
  try {
    const data = {
      parentId: form.parentId || null,
      categoryName: form.categoryName,
      categoryCode: form.categoryCode,
      categoryType: form.categoryType,
      sortOrder: form.sortOrder,
      status: form.status,
      level: calculateLevel(form.parentId)
    }

    if (isEdit.value) {
      await updateCategory(form.id!, data)
      ElMessage.success('更新成功')
    } else {
      await createCategory(data)
      ElMessage.success('新增成功')
    }

    dialogVisible.value = false
    getCategoryTreeData()
  } catch (error: any) {
    console.warn('保存接口不可用，使用本地保存:', error)
    // 本地保存
    if (isEdit.value) {
      // 更新本地数据
      const updateNode = (nodes: Category[]): boolean => {
        for (let i = 0; i < nodes.length; i++) {
          if (nodes[i].id === form.id) {
            // 如果父分类变化，需要移动节点
            const oldParentId = nodes[i].parentId
            const newParentId = form.parentId || null
            
            if (oldParentId !== newParentId) {
              // 从原位置移除
              const nodeToUpdate = { ...nodes[i] }
              nodes.splice(i, 1)
              
              // 更新节点属性
              Object.assign(nodeToUpdate, {
                parentId: newParentId,
                categoryName: form.categoryName,
                categoryCode: form.categoryCode,
                sortOrder: form.sortOrder,
                status: form.status,
                level: calculateLevel(newParentId)
              })
              
              // 添加到新位置
              if (newParentId === null) {
                localCategoryData.push(nodeToUpdate)
              } else {
                const addToParent = (nodesList: Category[]) => {
                  for (const n of nodesList) {
                    if (n.id === newParentId) {
                      if (!n.children) n.children = []
                      n.children.push(nodeToUpdate)
                      return true
                    }
                    if (n.children && addToParent(n.children)) {
                      return true
                    }
                  }
                  return false
                }
                addToParent(localCategoryData)
              }
            } else {
              // 只是更新属性
              Object.assign(nodes[i], {
                categoryName: form.categoryName,
                categoryCode: form.categoryCode,
                sortOrder: form.sortOrder,
                status: form.status
              })
            }
            return true
          }
          if (nodes[i].children && updateNode(nodes[i].children!)) {
            return true
          }
        }
        return false
      }
      updateNode(localCategoryData)
      ElMessage.success('更新成功')
    } else {
      // 新增本地数据
      const newCategory: Category = {
        id: nextId++,
        parentId: form.parentId || null,
        categoryName: form.categoryName,
        categoryCode: form.categoryCode,
        sortOrder: form.sortOrder,
        status: form.status,
        level: calculateLevel(form.parentId),
        icon: null,
        createTime: new Date().toLocaleString('zh-CN', { hour12: false }).replace(/\//g, '-')
      }

      if (form.parentId === null || form.parentId === 0) {
        localCategoryData.push(newCategory)
      } else {
        const addToParent = (nodes: Category[]) => {
          for (const node of nodes) {
            if (node.id === form.parentId) {
              if (!node.children) node.children = []
              node.children.push(newCategory)
              return true
            }
            if (node.children && addToParent(node.children)) {
              return true
            }
          }
          return false
        }
        addToParent(localCategoryData)
      }
      ElMessage.success('新增成功')
    }

    dialogVisible.value = false
    getCategoryTreeData()
  } finally {
    submitLoading.value = false
  }
}

onMounted(() => {
  getCategoryTreeData()
})
</script>

<style lang="scss" scoped>
.category-management {
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
