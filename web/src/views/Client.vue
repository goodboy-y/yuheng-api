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
      :show-test="false"
      @add="handleAdd"
      @view="handleView"
      @edit="handleEdit"
      @delete="handleDelete"
      @update:page="handlePageChange"
      @search="handleSearch"
      @reset="handleReset"
    />

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
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
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

onMounted(() => {
  fetchList()
})
</script>

<style scoped>
.client-container {
  padding: 20px;
}
</style>