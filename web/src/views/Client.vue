<template>
  <div class="client-container">
    <PaginatedTable
      title="连接客户端管理"
      :columns="columns"
      :search-fields="searchFields"
      :table-data="clientList"
      :loading="loading"
      :total="total"
      :current-page="currentPage"
      :page-size="pageSize"
      @add="handleAdd"
      @update:page="handlePageChange"
      @search="handleSearch"
      @reset="handleReset"
    >
      <template #actions="{ row }">
        <el-button size="small" @click="handleView(row)">查看</el-button>
        <el-button size="small" type="primary" @click="handleEdit(row)">修改</el-button>
        <el-button size="small" type="warning" @click="handleAuth(row)">授权管理</el-button>
        <el-button size="small" type="info" @click="handleRevoke(row)">取消授权</el-button>
        <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
      </template>
    </PaginatedTable>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="handleDialogClose"
    >
      <el-form :model="formData" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="formData.name" placeholder="名称"></el-input>
        </el-form-item>
        <el-form-item label="过期时间" prop="expireDuration">
          <el-input v-model="formData.expireDuration" placeholder="请输入过期时长,输入-1永不过期" type="number"></el-input>
        </el-form-item>
        <el-form-item label="备注" prop="note">
          <el-input v-model="formData.note" type="textarea" placeholder="请输入备注"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="detailDialogVisible"
      title="连接客户端详情"
      width="600px"
    >
      <el-descriptions :column="1" border>
        <el-descriptions-item label="名称">{{ detailData.name }}</el-descriptions-item>
        <el-descriptions-item label="客户端ID">{{ detailData.clientId }}</el-descriptions-item>
        <el-descriptions-item label="密钥">{{ detailData.secret }}</el-descriptions-item>
        <el-descriptions-item label="过期时长">{{ detailData.expireDuration }}</el-descriptions-item>
        <el-descriptions-item label="过期时间">{{ detailData.expireAt }}</el-descriptions-item>
        <el-descriptions-item label="账户">{{ detailData.accountId }}</el-descriptions-item>
        <el-descriptions-item label="备注">{{ detailData.note }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 授权管理弹窗 -->
    <el-dialog
      v-model="authDialogVisible"
      title="API授权管理"
      width="800px"
    >
      <div class="auth-dialog">
        <div class="search-bar">
          <el-input
            v-model="authSearchParams.name"
            placeholder="请输入API名称"
            style="width: 300px"
            @keyup.enter="fetchUnauthorizedApis"
          >
            <template #append>
              <el-button @click="fetchUnauthorizedApis">搜索</el-button>
            </template>
          </el-input>
        </div>

        <el-table
          ref="authTableRef"
          :data="unauthorizedApiList"
          style="width: 100%"
          v-loading="authLoading"
          @selection-change="handleAuthSelectionChange"
          @row-click="handleAuthRowClick"
        >
          <el-table-column type="selection" width="55"></el-table-column>
          <el-table-column prop="name" label="API名称"></el-table-column>
          <el-table-column prop="path" label="请求路径"></el-table-column>
          <el-table-column prop="method" label="请求方法"></el-table-column>
          <el-table-column prop="datasourceName" label="数据源"></el-table-column>
        </el-table>

        <div class="pagination">
          <el-pagination
            :current-page="authCurrentPage"
            :page-size="authPageSize"
            :total="authTotal"
            layout="total, prev, pager, next"
            @current-change="handleAuthPageChange"
          ></el-pagination>
        </div>
      </div>
      <template #footer>
        <el-button @click="authDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleGrant" :loading="authSubmitLoading" :disabled="selectedApis.length === 0">授权</el-button>
      </template>
    </el-dialog>

    <!-- 取消授权弹窗 -->
    <el-dialog
      v-model="revokeDialogVisible"
      title="取消API授权"
      width="800px"
    >
      <div class="revoke-dialog">
        <div class="search-bar">
          <el-input
            v-model="revokeSearchParams.apiPath"
            placeholder="请输入API路径"
            style="width: 300px"
            @keyup.enter="fetchAuthorizedApis"
          >
            <template #append>
              <el-button @click="fetchAuthorizedApis">搜索</el-button>
            </template>
          </el-input>
        </div>

        <el-table
          ref="revokeTableRef"
          :data="authorizedApiList"
          style="width: 100%"
          v-loading="revokeLoading"
          @selection-change="handleRevokeSelectionChange"
          @row-click="handleRevokeRowClick"
        >
          <el-table-column type="selection" width="55"></el-table-column>
          <el-table-column prop="apiConfig.name" label="API名称"></el-table-column>
          <el-table-column prop="apiConfig.path" label="请求路径"></el-table-column>
          <el-table-column prop="apiConfig.method" label="请求方法"></el-table-column>
        </el-table>

        <div class="pagination">
          <el-pagination
            :current-page="revokeCurrentPage"
            :page-size="revokePageSize"
            :total="revokeTotal"
            layout="total, prev, pager, next"
            @current-change="handleRevokePageChange"
          ></el-pagination>
        </div>
      </div>
      <template #footer>
        <el-button @click="revokeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleRevokeSubmit" :loading="revokeSubmitLoading" :disabled="selectedRevokeApis.length === 0">取消授权</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox, ElTable } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import PaginatedTable from '../components/PaginatedTable.vue'
import {
  getClientList,
  addClient,
  deleteClient,
  getClientDetail,
  updateClient,
  type ClientData
} from '../api/client'
import {
  getUnauthorizedApis,
  type ApiData
} from '../api/api'
import {
  getApiConfigAccessList,
  grantAccess,
  revokeAccess,
  type ApiConfigAccessData
} from '../api/api-config-access'

const columns = [
  { prop: 'name', label: '名称' },
  { prop: 'clientId', label: '客户端ID' },
  { prop: 'secret', label: '客户端密钥' },
  { prop: 'expireDuration', label: '过期时长' },
  { prop: 'accountId', label: '创建账户' }
]

const searchFields = [
  { prop: 'name', label: '客户端名称', type: 'input' as const },
  { prop: 'clientId', label: '客户端ID', type: 'input' as const }
]

const clientList = ref<ClientData[]>([])
const loading = ref(false)
const dialogVisible = ref(false)
const detailDialogVisible = ref(false)
const dialogTitle = ref('')
const submitLoading = ref(false)
const formRef = ref<FormInstance>()
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)
const searchParams = ref<Record<string, any>>({
  name: '',
  clientId: ''
})
const detailData = ref<ClientData>({
  id: '',
  name: '',
  note: '',
  clientId: '',
  secret: '',
  expireDesc: '',
  expireDuration: '',
  token: '',
  expireAt: '',
  accountId: ''
})

const formData = ref<Partial<ClientData>>({
  name: '',
  note: '',
  clientId: '',
  secret: '',
  expireDuration: '',
  token: '',
  expireAt: '',
  accountId: ''
})

// 授权管理相关数据
const authDialogVisible = ref(false)
const authLoading = ref(false)
const authSubmitLoading = ref(false)
const unauthorizedApiList = ref<ApiData[]>([])
const authCurrentPage = ref(1)
const authPageSize = ref(20)
const authTotal = ref(0)
const currentClientId = ref('')
const selectedApis = ref<ApiData[]>([])
const authSearchParams = ref<Record<string, any>>({
  name: ''
})

// 取消授权相关数据
const revokeDialogVisible = ref(false)
const revokeLoading = ref(false)
const revokeSubmitLoading = ref(false)
const authorizedApiList = ref<ApiConfigAccessData[]>([])
const revokeCurrentPage = ref(1)
const revokePageSize = ref(20)
const revokeTotal = ref(0)
const selectedRevokeApis = ref<ApiConfigAccessData[]>([])
const revokeSearchParams = ref<Record<string, any>>({
  apiPath: ''
})

// 表格引用
const authTableRef = ref<InstanceType<typeof ElTable>>()
const revokeTableRef = ref<InstanceType<typeof ElTable>>()

const rules: FormRules = {
  name: [{ required: true, message: '请输入客户端名称', trigger: 'blur' }],
  expireDuration: [{ required: true, message: '请输入过期时长', trigger: 'blur' }]
}

const fetchList = async (params?: Record<string, any>) => {
  loading.value = true
  try {
    const response = await getClientList(currentPage.value, pageSize.value, params || searchParams.value)
    clientList.value = response.data.data.data
    total.value = response.data.data.pageInfo.totalRows
  } catch (error) {
    ElMessage.error('获取客户端列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = (params: Record<string, any>) => {
  searchParams.value = params
  currentPage.value = 0
  fetchList()
}

const handleReset = () => {
  searchParams.value = {
    name: '',
    clientId: ''
  }
  currentPage.value = 0
  fetchList()
}

const handleAdd = () => {
  dialogTitle.value = '新增客户端'
  formData.value = {
    name: '',
    note: '',
    clientId: '',
    secret: '',
    expireDuration: '',
    expireAt: ''
  }
  dialogVisible.value = true
}

const handleView = async (row: ClientData) => {
  try {
    console.log('开始获取客户端详情，ID:', row.id)
    const response = await getClientDetail(row.id)
    console.log('API响应:', response)
    
    if (response.data && response.data.data) {
      detailData.value = response.data.data
      detailDialogVisible.value = true
    } else {
      console.error('API响应数据格式不正确:', response)
      ElMessage.error('获取客户端详情失败：数据格式不正确')
    }
  } catch (error) {
    console.error('获取客户端详情失败:', error)
    ElMessage.error('获取客户端详情失败')
  }
}

const handleEdit = async (row: ClientData) => {
  try {
    console.log('开始获取客户端详情，ID:', row.id)
    const response = await getClientDetail(row.id)
    console.log('API响应:', response)
    
    if (response.data && response.data.data) {
      formData.value = response.data.data
      dialogTitle.value = '修改客户端'
      dialogVisible.value = true
    } else {
      console.error('API响应数据格式不正确:', response)
      ElMessage.error('获取客户端详情失败：数据格式不正确')
    }
  } catch (error) {
    console.error('获取客户端详情失败:', error)
    ElMessage.error('获取客户端详情失败')
  }
}

const handleDelete = async (row: ClientData) => {
  try {
    await ElMessageBox.confirm('确定要删除该客户端吗?', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteClient(row.id)
    ElMessage.success('删除成功')
    fetchList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handlePageChange = (page: number, size: number) => {
  currentPage.value = page
  pageSize.value = size
  fetchList()
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        if (formData.value.id) {
          await updateClient(formData.value as ClientData)
          ElMessage.success('修改成功')
        } else {
          await addClient(formData.value as Omit<ClientData, 'id'>)
          ElMessage.success('新增成功')
        }
        dialogVisible.value = false
        fetchList()
      } catch (error) {
        ElMessage.error(formData.value.id ? '修改失败' : '新增失败')
      } finally {
        submitLoading.value = false
      }
    }
  })
}

const handleDialogClose = () => {
  formRef.value?.resetFields()
}

// 授权管理相关方法
const handleAuth = (row: ClientData) => {
  currentClientId.value = row.id
  authCurrentPage.value = 1
  authSearchParams.value = { name: '' }
  selectedApis.value = []
  authDialogVisible.value = true
  fetchUnauthorizedApis()
}

const fetchUnauthorizedApis = async () => {
  authLoading.value = true
  try {
    const response = await getUnauthorizedApis(currentClientId.value, authCurrentPage.value - 1, authPageSize.value, authSearchParams.value.name)
    unauthorizedApiList.value = response.data.data.data
    authTotal.value = response.data.data.pageInfo.totalRows
  } catch (error) {
    ElMessage.error('获取未授权API列表失败')
  } finally {
    authLoading.value = false
  }
}

const handleAuthPageChange = (page: number) => {
  authCurrentPage.value = page
  fetchUnauthorizedApis()
}

const handleAuthSelectionChange = (selection: ApiData[]) => {
  selectedApis.value = selection
}

const handleAuthRowClick = (row: ApiData) => {
  if (authTableRef.value) {
    // 检查当前行是否已选中
    const isSelected = selectedApis.value.some(item => item.id === row.id)
    if (isSelected) {
      // 如果已选中，则取消选中
      authTableRef.value.toggleRowSelection(row, false)
    } else {
      // 如果未选中，则选中
      authTableRef.value.toggleRowSelection(row, true)
    }
  }
}

const handleGrant = async () => {
  if (selectedApis.value.length === 0) {
    ElMessage.warning('请选择要授权的API')
    return
  }

  authSubmitLoading.value = true
  try {
    for (const api of selectedApis.value) {
      await grantAccess(currentClientId.value, api.id)
    }
    ElMessage.success('授权成功')
    authDialogVisible.value = false
    fetchUnauthorizedApis()
  } catch (error) {
    ElMessage.error('授权失败')
  } finally {
    authSubmitLoading.value = false
  }
}

// 取消授权相关方法
const handleRevoke = (row: ClientData) => {
  currentClientId.value = row.id
  revokeCurrentPage.value = 1
  revokeSearchParams.value = { apiPath: '' }
  selectedRevokeApis.value = []
  revokeDialogVisible.value = true
  fetchAuthorizedApis()
}

const fetchAuthorizedApis = async () => {
  revokeLoading.value = true
  try {
    const response = await getApiConfigAccessList(revokeCurrentPage.value - 1, revokePageSize.value, { 
      clientId: currentClientId.value, 
      apiPath: revokeSearchParams.value.apiPath 
    })
    authorizedApiList.value = response.data.data.data
    revokeTotal.value = response.data.data.pageInfo.totalRows
  } catch (error) {
    ElMessage.error('获取已授权API列表失败')
  } finally {
    revokeLoading.value = false
  }
}

const handleRevokePageChange = (page: number) => {
  revokeCurrentPage.value = page
  fetchAuthorizedApis()
}

const handleRevokeSelectionChange = (selection: ApiConfigAccessData[]) => {
  selectedRevokeApis.value = selection
}

const handleRevokeRowClick = (row: ApiConfigAccessData) => {
  if (revokeTableRef.value) {
    // 检查当前行是否已选中
    const isSelected = selectedRevokeApis.value.some(item => item.id === row.id)
    if (isSelected) {
      // 如果已选中，则取消选中
      revokeTableRef.value.toggleRowSelection(row, false)
    } else {
      // 如果未选中，则选中
      revokeTableRef.value.toggleRowSelection(row, true)
    }
  }
}

const handleRevokeSubmit = async () => {
  if (selectedRevokeApis.value.length === 0) {
    ElMessage.warning('请选择要取消授权的API')
    return
  }

  revokeSubmitLoading.value = true
  try {
    for (const access of selectedRevokeApis.value) {
      await revokeAccess(access.id)
    }
    ElMessage.success('取消授权成功')
    revokeDialogVisible.value = false
    fetchAuthorizedApis()
  } catch (error) {
    ElMessage.error('取消授权失败')
  } finally {
    revokeSubmitLoading.value = false
  }
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped>
.client-container {
  padding: 20px;
}

.auth-dialog,
.revoke-dialog {
  padding: 20px 0;
}

.search-bar {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>