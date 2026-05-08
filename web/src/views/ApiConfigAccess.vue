<template>
  <div class="api-config-access-container">
    <PaginatedTable
      title="API授权管理"
      :columns="columns"
      :search-fields="searchFields"
      :table-data="accessList"
      :loading="loading"
      :total="total"
      :current-page="currentPage"
      :page-size="pageSize"
      :show-add="true"
      @add="handleAdd"
      @update:page="handlePageChange"
      @search="handleSearch"
      @reset="handleReset"
    >
      <template #actions="{ row }">
        <el-button size="small" type="danger" @click="handleRevoke(row)">删除</el-button>
      </template>
    </PaginatedTable>

    <el-dialog
      v-model="dialogVisible"
      title="新增授权"
      width="600px"
      @close="handleDialogClose"
    >
      <el-form :model="formData" :rules="rules" ref="formRef" label-width="120px">
        <el-form-item label="客户端" prop="clientId">
          <el-select v-model="formData.clientId" placeholder="请选择客户端" style="width: 100%">
            <el-option
              v-for="client in clientOptions"
              :key="client.id"
              :label="client.name"
              :value="client.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="API配置" prop="apiConfigId">
          <el-select v-model="formData.apiConfigId" placeholder="请选择API配置" style="width: 100%">
            <el-option
              v-for="api in apiConfigOptions"
              :key="api.id"
              :label="`${api.name} (${api.path})`"
              :value="api.id"
            />
          </el-select>
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
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import PaginatedTable from '../components/PaginatedTable.vue'
import {
  getApiConfigAccessList,
  grantAccess,
  revokeAccess,
  type ApiConfigAccessData
} from '../api/api-config-access'
import { getClientList, type ClientData } from '../api/client'
import { getApiList, type ApiData } from '../api/api'

const columns = [
  { prop: 'clientName', label: '客户端名称', formatter: (row: ApiConfigAccessData) => row.client?.name || '-' },
  { prop: 'clientId', label: '客户端ID', formatter: (row: ApiConfigAccessData) => row.client?.clientId || '-' },
  { prop: 'apiName', label: '名称', formatter: (row: ApiConfigAccessData) => row.apiConfig?.name || '-' },
  { prop: 'apiPath', label: 'API路径', formatter: (row: ApiConfigAccessData) => row.apiConfig?.path || '-' },
  { prop: 'createTime', label: '授权时间' }
]

const searchFields = [
  { prop: 'clientName', label: '客户端名称', type: 'input' as const },
  { prop: 'apiPath', label: 'API路径', type: 'input' as const }
]

const accessList = ref<ApiConfigAccessData[]>([])
const loading = ref(false)
const dialogVisible = ref(false)
const submitLoading = ref(false)
const formRef = ref<FormInstance>()
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)
const clientOptions = ref<ClientData[]>([])
const apiConfigOptions = ref<ApiData[]>([])
const searchParams = ref<Record<string, any>>({
  clientName: '',
  apiPath: ''
})
const formData = ref({
  clientId: '',
  apiConfigId: ''
})

const rules: FormRules = {
  clientId: [{ required: true, message: '请选择客户端', trigger: 'change' }],
  apiConfigId: [{ required: true, message: '请选择API配置', trigger: 'change' }]
}

const fetchList = async (params?: Record<string, any>) => {
  loading.value = true
  try {
    const response = await getApiConfigAccessList(currentPage.value, pageSize.value, params || searchParams.value)
    accessList.value = response.data.data.data
    total.value = response.data.data.pageInfo.totalRows
  } catch (error) {
    ElMessage.error('获取授权列表失败')
  } finally {
    loading.value = false
  }
}

const fetchClientOptions = async () => {
  try {
    const response = await getClientList(0, 1000)
    clientOptions.value = response.data.data.data
  } catch (error) {
    console.error('获取客户端列表失败', error)
  }
}

const fetchApiConfigOptions = async () => {
  try {
    const response = await getApiList(0, 1000)
    apiConfigOptions.value = response.data.data.data
  } catch (error) {
    console.error('获取API配置列表失败', error)
  }
}

const handleSearch = (params: Record<string, any>) => {
  searchParams.value = params
  currentPage.value = 0
  fetchList()
}

const handleReset = () => {
  searchParams.value = {
    clientName: '',
    apiPath: ''
  }
  currentPage.value = 0
  fetchList()
}

const handleAdd = () => {
  formData.value = {
    clientId: '',
    apiConfigId: ''
  }
  dialogVisible.value = true
}

const handleRevoke = async (row: ApiConfigAccessData) => {
  try {
    await ElMessageBox.confirm('确定要撤销该授权吗?', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await revokeAccess(row.id)
    ElMessage.success('撤销授权成功')
    fetchList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('撤销授权失败')
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
        await grantAccess(formData.value.clientId, formData.value.apiConfigId)
        ElMessage.success('授权成功')
        dialogVisible.value = false
        fetchList()
      } catch (error: any) {
        ElMessage.error(error.response?.data?.message || '授权失败')
      } finally {
        submitLoading.value = false
      }
    }
  })
}

const handleDialogClose = () => {
  formRef.value?.resetFields()
}

onMounted(() => {
  fetchList()
  fetchClientOptions()
  fetchApiConfigOptions()
})
</script>

<style scoped>
.api-config-access-container {
  padding: 20px;
}
</style>
