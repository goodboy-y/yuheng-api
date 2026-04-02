<template>
  <div class="api-container">
    <PaginatedTable title="API管理" :columns="columns" :search-fields="searchFields" :table-data="apiList"
      :loading="loading" :total="total" :current-page="currentPage" :page-size="pageSize" :show-test="false"
      @add="handleAdd" @view="handleView" @edit="handleEdit" @delete="handleDelete" @update:page="handlePageChange"
      @search="handleSearch" @reset="handleReset" />

    <el-dialog v-model="dialogVisible" :title="dialogTitle" :fullscreen="true" @close="handleDialogClose">
      <el-form :model="formData" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="API名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入API名称"></el-input>
        </el-form-item>
        <el-form-item label="路径" prop="path">
          <el-input v-model="formData.path" placeholder="请输入路径"></el-input>
        </el-form-item>
        <el-form-item label="数据源ID" prop="datasourceId">
          <el-select v-model="formData.datasourceId" placeholder="请选择数据源" style="width: 100%">
            <el-option v-for="ds in datasourceList" :key="ds.id" :label="ds.name" :value="ds.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="SQL参数" prop="sql_param">
          <Codemirror v-model="formData.sqlParam.sql" :style="{ height: '200px', fontSize: '14px' }" :autofocus="true"
            :extensions="[sql()]" :theme="oneDark" :indent-with-tab="true" :tab-size="2" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="formData.status" placeholder="请选择状态" style="width: 100%">
            <el-option label="启用" :value="1"></el-option>
            <el-option label="禁用" :value="0"></el-option>
          </el-select>
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

    <el-dialog v-model="detailDialogVisible" title="API详情" width="600px">
      <el-descriptions :column="1" border>
        <!-- <el-descriptions-item label="ID">{{ detailData.id }}</el-descriptions-item> -->
        <el-descriptions-item label="API名称">{{ detailData.name }}</el-descriptions-item>
        <el-descriptions-item label="路径">{{ detailData.path }}</el-descriptions-item>
        <el-descriptions-item label="数据源">{{ detailData.datasource.name }}</el-descriptions-item>
        <el-descriptions-item label="SQL参数">{{ detailData.sqlParam.sql }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ detailData.status === 1 ? '启用' : '禁用' }}</el-descriptions-item>
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
import { Codemirror } from 'vue-codemirror'
import { sql } from '@codemirror/lang-sql'
import { oneDark } from '@codemirror/theme-one-dark'
import PaginatedTable from '../components/PaginatedTable.vue'
import {
  getApiList,
  addApi,
  deleteApi,
  getApiDetail,
  updateApi,
  type ApiData,
  type ApiSqlParam
} from '../api/api'
import { listDatasource, type Datasource } from '../api/datasource'

const columns = [
  // { prop: 'id', label: 'ID', width: 80 },
  { prop: 'name', label: 'API名称' },
  { prop: 'path', label: '路径' },
  {
    prop: 'datasource', label: '数据源',
    formatter: (_row: any, _column: any, cellValue: any) => {
      return cellValue?.name || String(cellValue)
    }
  },
  {
    prop: 'sqlParam', label: 'SQL参数',
    formatter: (_row: any, _column: any, cellValue: any) => cellValue?.sql || String(cellValue)
  },
  {
    prop: 'status',
    label: '状态',
    formatter: (_row: any, _column: any, cellValue: number) => cellValue === 1 ? '启用' : '未启用'
  }
]

const searchFields = [
  { prop: 'name', label: '名称', type: 'input' as const },
  { prop: 'path', label: '路径', type: 'input' as const },
  {
    prop: 'status',
    label: '状态',
    type: 'select' as const,
    options: [
      { label: '启用', value: 1 },
      { label: '禁用', value: 0 }
    ]
  }
]

const apiList = ref<ApiData[]>([])
const loading = ref(false)
const dialogVisible = ref(false)
const detailDialogVisible = ref(false)
const dialogTitle = ref('')
const submitLoading = ref(false)
const formRef = ref<FormInstance>()
const datasourceList = ref<Datasource[]>([])
const currentPage = ref(0)
const pageSize = ref(20)
const total = ref(0)
const searchParams = ref<Record<string, any>>({
  name: '',
  status: ''
})
interface FormData {
  id?: string
  name: string
  path: string
  datasourceId: string
  sql_param?: string
  status: number
  note: string
  sqlParam: {
    sql: string
    params: ApiSqlParam[]
  }
}

const formData = ref<FormData>({
  name: '',
  path: '',
  datasourceId: '',
  status: 1,
  note: '',
  sqlParam: {
    sql: '',
    params: [] as ApiSqlParam[]
  }
})
const detailData = ref<ApiData>({
  id: '',
  name: '',
  note: '',
  path: '',
  datasource: {
    id: '',
    name: ''
  },
  datasourceId: '',
  sqlParam: {
    sql: '',
    params: [] as ApiSqlParam[]
  },
  status: 1
})

const rules: FormRules = {
  name: [{ required: true, message: '请输入API名称', trigger: 'blur' }],
  path: [{ required: true, message: '请输入路径', trigger: 'blur' }],
  datasourceId: [{ required: true, message: '请输入数据源ID', trigger: 'blur' }],
  // sql_param: [{ required: true, message: '请输入SQL参数', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

const fetchList = async (params?: Record<string, any>) => {
  loading.value = true
  try {
    const response = await getApiList(currentPage.value, pageSize.value, params || searchParams.value)
    apiList.value = response.data.data.data
    total.value = response.data.data.pageInfo.totalRows
  } catch (error) {
    console.error('获取API列表失败:', error)
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
    status: ''
  }
  currentPage.value = 0
  fetchList()
}

const handleAdd = () => {
  dialogTitle.value = '新增API'
  formData.value = {
    name: '',
    path: '',
    datasourceId: '',
    sql_param: '',
    status: 1,
    sqlParam: {
      sql: '',
      params: [] as ApiSqlParam[]
    },
    note: ''
  }
  dialogVisible.value = true
}

const handleView = async (row: ApiData) => {
  try {
    const response = await getApiDetail(row.id)

    if (response.data && response.data.data) {
      detailData.value = response.data.data
      detailDialogVisible.value = true
    } else {
      console.error('API响应数据格式不正确:', response)
      ElMessage.error('获取API详情失败：数据格式不正确')
    }
  } catch (error) {
    console.error('获取API详情失败:', error)
    ElMessage.error('获取API详情失败')
  }
}

const handleEdit = async (row: ApiData) => {
  try {
    const response = await getApiDetail(row.id)

    if (response.data && response.data.data) {
      const data = response.data.data
      // 将datasource对象转换为datasourceId
      if (data.datasource) {
        data.datasourceId = data.datasource.id
      }
      formData.value = data
      dialogTitle.value = '修改API'
      dialogVisible.value = true
    } else {
      console.error('API响应数据格式不正确:', response)
      ElMessage.error('获取API详情失败：数据格式不正确')
    }
  } catch (error) {
    console.error('获取API详情失败:', error)
    ElMessage.error('获取API详情失败')
  }
}

const handleDelete = async (row: ApiData) => {
  try {
    await ElMessageBox.confirm('确定要删除该API吗?', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteApi(row.id)
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
        // 准备提交数据
        const submitData: any = { ...formData.value }

        // 如果有datasourceId，转换为datasource对象
        if (submitData.datasourceId) {
          const ds = datasourceList.value.find(d => d.id === submitData.datasourceId)
          if (ds) {
            submitData.datasource = {
              id: ds.id,
              name: ds.name
            }
          }
          delete submitData.datasourceId
        }
        submitData.sql_param = JSON.stringify(formData.value.sqlParam)
        console.log('提交数据:', submitData)

        if (formData.value.id) {
          await updateApi(submitData as ApiData)
          ElMessage.success('修改成功')
        } else {
          await addApi(submitData as Omit<ApiData, 'id'>)
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

const fetchDatasourceList = async () => {
  try {
    const response = await listDatasource()
    datasourceList.value = response.data.data || []
  } catch (error) {
    console.error('获取数据源列表失败:', error)
    ElMessage.error('获取数据源列表失败')
  }
}

onMounted(() => {
  fetchList()
  fetchDatasourceList()
})
</script>

<style scoped>
.api-container {
  padding: 20px;
}

.CodeMirror {
  width: 100%;
}
</style>