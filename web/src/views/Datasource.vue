<template>
  <div class="datasource-container">
    <PaginatedTable
      title="数据源管理"
      :columns="columns"
      :search-fields="searchFields"
      :table-data="datasourceList"
      :loading="loading"
      :total="total"
      :current-page="currentPage"
      :page-size="pageSize"
      :show-test="true"
      @add="handleAdd"
      @view="handleView"
      @edit="handleEdit"
      @test="handleTest"
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
        <el-form-item label="数据源名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入数据源名称"></el-input>
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-select v-model="formData.type" placeholder="请选择类型" style="width: 100%">
            <el-option label="MySQL" value="MySQL"></el-option>
            <el-option label="PostgreSQL" value="PostgreSQL"></el-option>
            <el-option label="Oracle" value="Oracle"></el-option>
            <el-option label="SQL Server" value="SQL Server"></el-option>
            <el-option label="MongoDB" value="MongoDB"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="URL" prop="url">
          <el-input v-model="formData.url" placeholder="请输入URL"></el-input>
        </el-form-item>
        <el-form-item label="用户名" prop="username">
          <el-input v-model="formData.username" placeholder="请输入用户名"></el-input>
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="formData.password" type="password" placeholder="请输入密码" show-password></el-input>
        </el-form-item>
        <el-form-item label="备注" prop="note">
          <el-input v-model="formData.note" type="textarea" placeholder="请输入备注"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="success" @click="handleTestConnection" :loading="testLoading">测试连接</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="detailDialogVisible"
      title="数据源详情"
      width="600px"
    >
      <el-descriptions :column="1" border>
        <el-descriptions-item label="名称">{{ detailData.name }}</el-descriptions-item>
        <el-descriptions-item label="类型">{{ detailData.type }}</el-descriptions-item>
        <el-descriptions-item label="URL">{{ detailData.url }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ detailData.username }}</el-descriptions-item>
        <el-descriptions-item label="密码">{{ detailData.password }}</el-descriptions-item>
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
  getDatasourceList,
  addDatasource,
  deleteDatasource,
  getDatasourceDetail,
  updateDatasource,
  testDatasourceConnection,
  type Datasource
} from '../api/datasource'

const columns = [
  { prop: 'name', label: '名称' },
  { prop: 'type', label: '类型' },
  { prop: 'url', label: 'URL' },
  { prop: 'username', label: '用户名' },
  { prop: 'note', label: '备注' }
]

const searchFields = [
  { prop: 'name', label: '数据源名称', type: 'input' as const },
  { 
    prop: 'type', 
    label: '类型', 
    type: 'select' as const,
    options: [
      { label: 'MySQL', value: 'MySQL' },
      { label: 'PostgreSQL', value: 'PostgreSQL' },
      { label: 'Oracle', value: 'Oracle' },
      { label: 'SQL Server', value: 'SQL Server' },
      { label: 'MongoDB', value: 'MongoDB' }
    ]
  }
]

const datasourceList = ref<Datasource[]>([])
const loading = ref(false)
const dialogVisible = ref(false)
const detailDialogVisible = ref(false)
const dialogTitle = ref('')
const submitLoading = ref(false)
const testLoading = ref(false)
const formRef = ref<FormInstance>()
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)
const searchParams = ref<Record<string, any>>({
  name: '',
  type: ''
})
const formData = ref<Partial<Datasource>>({
  name: '',
  type: '',
  url: '',
  username: '',
  password: '',
  note: ''
})
const detailData = ref<Datasource>({
  id: '',
  name: '',
  note: '',
  url: '',
  username: '',
  password: '',
  type: ''
})

const rules: FormRules = {
  name: [{ required: true, message: '请输入数据源名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择类型', trigger: 'change' }],
  url: [{ required: true, message: '请输入URL', trigger: 'blur' }],
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const fetchList = async (params?: Record<string, any>) => {
  loading.value = true
  try {
    const response = await getDatasourceList(currentPage.value, pageSize.value, params || searchParams.value)
    datasourceList.value = response.data.data.data
    total.value = response.data.data.pageInfo.totalRows
  } catch (error) {
    ElMessage.error('获取数据源列表失败')
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
    type: ''
  }
  currentPage.value = 0
  fetchList()
}

const handleAdd = () => {
  dialogTitle.value = '新增数据源'
  formData.value = {
    name: '',
    type: '',
    url: '',
    username: '',
    password: '',
    note: ''
  }
  dialogVisible.value = true
}

const handleView = async (row: Datasource) => {
  try {
    console.log('开始获取数据源详情，ID:', row.id)
    const response = await getDatasourceDetail(row.id)
    console.log('API响应:', response)
    
    if (response.data && response.data.data) {
      detailData.value = response.data.data
      detailDialogVisible.value = true
    } else {
      console.error('API响应数据格式不正确:', response)
      ElMessage.error('获取数据源详情失败：数据格式不正确')
    }
  } catch (error) {
    console.error('获取数据源详情失败:', error)
    ElMessage.error('获取数据源详情失败')
  }
}

const handleEdit = async (row: Datasource) => {
  try {
    console.log('开始获取数据源详情，ID:', row.id)
    const response = await getDatasourceDetail(row.id)
    console.log('API响应:', response)
    
    if (response.data && response.data.data) {
      formData.value = response.data.data
      dialogTitle.value = '修改数据源'
      dialogVisible.value = true
    } else {
      console.error('API响应数据格式不正确:', response)
      ElMessage.error('获取数据源详情失败：数据格式不正确')
    }
  } catch (error) {
    console.error('获取数据源详情失败:', error)
    ElMessage.error('获取数据源详情失败')
  }
}

const handleTest = async (row: Datasource) => {
  testLoading.value = true
  try {
    // 获取数据源详情
    const response = await getDatasourceDetail(row.id)
    if (response.data) {
      const datasource = response.data.data
      // 测试连接
      const testResponse = await testDatasourceConnection(datasource)
      console.log('测试连接响应:', testResponse)
      if (testResponse.data && testResponse.data.code === 200) {
        ElMessage.success(testResponse.data.data || '测试连接成功')
      } else {
        ElMessage.error(testResponse.data.message || '测试连接失败')
      }
    } else {
      ElMessage.error('获取数据源信息失败')
    }
  } catch (error: any) {
    console.error('测试连接失败:', error)
    ElMessage.error('测试连接失败：' + (error.response?.data?.message || error.message || '未知错误'))
  } finally {
    testLoading.value = false
  }
}

const handleTestConnection = async () => {
  if (!formRef.value) return
  
  testLoading.value = true
  try {
    // 验证必要的字段
    await formRef.value.validateField(['name', 'type', 'url', 'username', 'password'])
    
    // 使用表单数据测试连接
    const testResponse = await testDatasourceConnection(formData.value)
    console.log('测试连接响应:', testResponse)
    if (testResponse.data && testResponse.data.code === 200) {
      ElMessage.success(testResponse.data.data || '测试连接成功')
    } else {
      ElMessage.error(testResponse.data.message || '测试连接失败')
    }
  } catch (error: any) {
    console.error('测试连接失败:', error)
    // 如果是验证错误，Element Plus会自动显示错误信息，不需要额外处理
    if (error.response) {
      ElMessage.error('测试连接失败：' + (error.response.data?.message || '未知错误'))
    } else if (error.message && !error.message.includes('Validation failed')) {
      // 避免重复显示Element Plus的验证错误信息
      ElMessage.error('测试连接失败：' + error.message)
    }
  } finally {
    testLoading.value = false
  }
}

const handleDelete = async (row: Datasource) => {
  try {
    await ElMessageBox.confirm('确定要删除该数据源吗?', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteDatasource(row.id)
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
          await updateDatasource(formData.value as Datasource)
          ElMessage.success('修改成功')
        } else {
          await addDatasource(formData.value as Omit<Datasource, 'id'>)
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
.datasource-container {
  padding: 20px;
}
</style>