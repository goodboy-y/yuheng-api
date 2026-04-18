<template>
  <div class="api-container">
    <PaginatedTable title="API管理" :columns="columns" :search-fields="searchFields" :table-data="apiList"
      :loading="loading" :total="total" :current-page="currentPage" :page-size="pageSize" :show-test="true"
      @add="handleAdd" @view="handleView" @edit="handleEdit" @delete="handleDelete" @test="handleTest" @update:page="handlePageChange"
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

        <!-- 字段映射配置 -->
        <el-divider content-position="left">字段映射配置</el-divider>
        <el-form-item label="字段映射">
          <div class="field-mapping-section">
            <el-button type="primary" @click="handleAddMappingInForm" size="small" style="margin-bottom: 10px;">
              添加映射
            </el-button>

            <el-table :data="formData.fieldMappings" border style="width: 100%" max-height="300">
              <el-table-column prop="fieldName" label="原始字段名" width="200">
                <template #default="{ row }">
                  <el-input v-model="row.fieldName" placeholder="如：name" />
                </template>
              </el-table-column>
              <el-table-column prop="displayName" label="显示名称">
                <template #default="{ row }">
                  <el-input v-model="row.displayName" placeholder="如：姓名" />
                </template>
              </el-table-column>
              <el-table-column label="操作" width="100">
                <template #default="{ $index }">
                  <el-button type="danger" size="small" @click="handleRemoveMappingInForm($index)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>

            <div v-if="formData.fieldMappings.length === 0" class="empty-tip">
              暂无字段映射配置，导出Excel时将使用原始字段名
            </div>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailDialogVisible" title="API详情" width="600px">
      <el-descriptions :column="1" border>
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
    
    <!-- 测试API对话框 -->
    <el-dialog v-model="testDialogVisible" title="测试API" width="800px">
      <div v-if="testApiData" class="test-api-container">
        <div class="test-api-info">
          <h4>API信息</h4>
          <p><strong>名称:</strong> {{ testApiData.name }}</p>
          <p><strong>路径:</strong> {{ testApiData.path }}</p>
          <p><strong>SQL:</strong> <pre>{{ testApiData.sqlParam.sql }}</pre></p>
        </div>
        
        <el-form v-if="testApiData.sqlParam.params && testApiData.sqlParam.params.length > 0" :model="testParams" label-width="100px" class="test-form">
          <h4>参数设置</h4>
          <el-form-item v-for="param in testApiData.sqlParam.params" :key="param.name" :label="(param.name || '').split(':')[0]" :prop="(param.name || '').split(':')[0] as string">
            <el-input 
              v-model="testParams[(param.name || '').split(':')[0] as string]" 
              :placeholder="`请输入${(param.name || '').split(':')[0]}`"
              :type="param.type === 'number' ? 'number' : 'text'"
            />
          </el-form-item>
        </el-form>
        
        <div v-if="testResult" class="test-result">
          <h4>测试结果</h4>
          <pre>{{ JSON.stringify(testResult, null, 2) }}</pre>
        </div>
      </div>
      
      <template #footer>
        <el-button @click="testDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="handleTestSubmit" :loading="testLoading">执行测试</el-button>
        <el-button type="success" @click="handleExport" :loading="exportLoading">导出数据</el-button>
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
  testApi,
  exportApiTestData,
  getFieldMappings,
  saveFieldMappings,
  type ApiData,
  type ApiSqlParam,
  type ApiFieldMapping
} from '../api/api'
import { listDatasource, type Datasource } from '../api/datasource'

const columns = [
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
  fieldMappings: ApiFieldMapping[]
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
  },
  fieldMappings: []
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

// 测试API相关变量
const testDialogVisible = ref(false)
const testApiData = ref<ApiData | null>(null)
const testParams = ref<Record<string, any>>({})
const testResult = ref<any>(null)
const testLoading = ref(false)
const exportLoading = ref(false)

const rules: FormRules = {
  name: [{ required: true, message: '请输入API名称', trigger: 'blur' }],
  path: [{ required: true, message: '请输入路径', trigger: 'blur' }],
  datasourceId: [{ required: true, message: '请输入数据源ID', trigger: 'blur' }],
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
    note: '',
    fieldMappings: []
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
      formData.value = {
        ...data,
        fieldMappings: []
      }
      dialogTitle.value = '修改API'
      dialogVisible.value = true

      // 加载字段映射配置
      loadFieldMappings(data.id)
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

const handleTest = async (row: ApiData) => {
  try {
    const response = await getApiDetail(row.id)
    if (response.data && response.data.data) {
      testApiData.value = response.data.data
      testParams.value = {}
      testResult.value = null
      testDialogVisible.value = true
    } else {
      ElMessage.error('获取API详情失败')
    }
  } catch (error) {
    console.error('获取API详情失败:', error)
    ElMessage.error('获取API详情失败')
  }
}

const handleTestSubmit = async () => {
  if (!testApiData.value) return
  
  testLoading.value = true
  testResult.value = null
  
  try {
    const response = await testApi(testApiData.value.id, testParams.value)
    if (response.data && response.data.data) {
      testResult.value = response.data.data
      ElMessage.success('测试成功')
    } else {
      ElMessage.error('测试失败：' + (response.data?.message || '未知错误'))
    }
  } catch (error: any) {
    console.error('测试API失败:', error)
    ElMessage.error('测试失败：' + (error.response?.data?.message || error.message || '未知错误'))
  } finally {
    testLoading.value = false
  }
}

const handleExport = async () => {
  if (!testApiData.value) return
  
  exportLoading.value = true
  
  try {
    const response = await exportApiTestData(testApiData.value.id, testParams.value)
    
    // 处理blob响应
    const blob = response.data
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    
    // 从响应头中获取文件名
    const contentDisposition = response.headers['content-disposition']
    let fileName = 'api_test_data.xlsx'
    if (contentDisposition) {
      const match = contentDisposition.match(/filename="([^"]+)"/)
      if (match && match[1]) {
        fileName = decodeURIComponent(match[1])
      }
    }
    
    link.setAttribute('download', fileName)
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    
    ElMessage.success('导出成功')
  } catch (error: any) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败：' + (error.response?.data?.message || error.message || '未知错误'))
  } finally {
    exportLoading.value = false
  }
}

const handlePageChange = (page: number, size: number) => {
  currentPage.value = page
  pageSize.value = size
  fetchList()
}

// 加载字段映射
const loadFieldMappings = async (apiConfigId: string) => {
  try {
    const response = await getFieldMappings(apiConfigId)
    if (response.data && response.data.data) {
      formData.value.fieldMappings = response.data.data
    }
  } catch (error) {
    console.error('获取字段映射失败:', error)
  }
}

// 在表单中添加字段映射
const handleAddMappingInForm = () => {
  formData.value.fieldMappings.push({
    apiConfigId: formData.value.id || '',
    fieldName: '',
    displayName: ''
  })
}

const handleRemoveMappingInForm = (index: number) => {
  formData.value.fieldMappings.splice(index, 1)
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        const isNewApi = !formData.value.id
        
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
        
        // 移除fieldMappings，不随API配置一起提交
        const fieldMappings = submitData.fieldMappings
        delete submitData.fieldMappings

        if (isNewApi) {
          // 新增API
          const result = await addApi(submitData as Omit<ApiData, 'id'>)
          const apiId = result.data?.data || result.data?.id
          
          // 保存字段映射
          if (apiId && fieldMappings && fieldMappings.length > 0) {
            const mappingsWithId = fieldMappings.map((m: ApiFieldMapping) => ({
              ...m,
              apiConfigId: apiId
            }))
            await saveFieldMappings(apiId, mappingsWithId)
          }
          
          ElMessage.success('新增成功')
        } else {
          // 更新API
          await updateApi(submitData as ApiData)
          
          // 保存字段映射
          if (fieldMappings) {
            await saveFieldMappings(formData.value.id!, fieldMappings)
          }
          
          ElMessage.success('修改成功')
        }
        
        dialogVisible.value = false
        fetchList()
      } catch (error) {
        console.error('提交失败:', error)
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

.test-api-container {
  max-height: 600px;
  overflow-y: auto;
}

.test-api-info {
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.test-api-info h4 {
  margin-top: 0;
  margin-bottom: 10px;
}

.test-api-info pre {
  background-color: #fff;
  padding: 10px;
  border-radius: 4px;
  border: 1px solid #e4e7ed;
  overflow-x: auto;
  max-height: 200px;
}

.test-form {
  margin-bottom: 20px;
}

.test-form h4 {
  margin-top: 0;
  margin-bottom: 15px;
}

.test-result {
  margin-top: 20px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.test-result h4 {
  margin-top: 0;
  margin-bottom: 10px;
}

.test-result pre {
  background-color: #fff;
  padding: 10px;
  border-radius: 4px;
  border: 1px solid #e4e7ed;
  overflow-x: auto;
  max-height: 300px;
  white-space: pre-wrap;
  word-wrap: break-word;
}

.field-mapping-section {
  width: 100%;
}

.empty-tip {
  text-align: center;
  padding: 20px;
  color: #909399;
  font-size: 14px;
}
</style>
