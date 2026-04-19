<template>
  <div class="api-access-log-container">
    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane label="访问日志" name="log">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>API访问日志</span>
              <div>
                <el-button type="primary" @click="handleArchive">归档</el-button>
              </div>
            </div>
          </template>
          <div class="search-form">
            <el-form :inline="true" :model="searchForm" class="demo-form-inline">
              <el-form-item label="开始时间">
                <el-date-picker
                  v-model="searchForm.startTime"
                  type="datetime"
                  placeholder="选择开始时间"
                  value-format="YYYY-MM-DD HH:mm:ss"
                  style="width: 200px"
                />
              </el-form-item>
              <el-form-item label="结束时间">
                <el-date-picker
                  v-model="searchForm.endTime"
                  type="datetime"
                  placeholder="选择结束时间"
                  value-format="YYYY-MM-DD HH:mm:ss"
                  style="width: 200px"
                />
              </el-form-item>
              <el-form-item label="访问路径">
                <el-input v-model="searchForm.path" placeholder="请输入访问路径" style="width: 200px" />
              </el-form-item>
              <el-form-item label="客户端ID">
                <el-input v-model="searchForm.clientId" placeholder="请输入客户端ID" style="width: 200px" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleSearch">查询</el-button>
                <el-button @click="handleReset">重置</el-button>
              </el-form-item>
            </el-form>
          </div>
          <el-table :data="logList" style="width: 100%" v-loading="loading" :row-style="{ height: '60px' }">
            <el-table-column prop="accessTime" label="访问时间" width="200" />
            <el-table-column prop="path" label="访问路径" />
            <el-table-column prop="clientId" label="客户端ID" width="200" />
            <el-table-column prop="params" label="访问参数" width="120">
              <template #default="scope">
                <el-link v-if="scope.row.params" type="primary" @click="showParams(scope.row.params)">查看</el-link>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100">
              <template #default="scope">
                <el-button size="small" type="danger" @click="handleDelete(scope.row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination-container">
            <el-pagination
              :current-page="currentPage"
              :page-size="pageSize"
              :page-sizes="[10, 20, 50, 100]"
              :total="total"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
            />
          </div>
        </el-card>
      </el-tab-pane>
      <el-tab-pane label="归档日志" name="archive">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>归档日志</span>
            </div>
          </template>
          <div class="search-form">
            <el-form :inline="true" :model="searchForm" class="demo-form-inline">
              <el-form-item label="开始时间">
                <el-date-picker
                  v-model="searchForm.startTime"
                  type="datetime"
                  placeholder="选择开始时间"
                  value-format="YYYY-MM-DD HH:mm:ss"
                  style="width: 200px"
                />
              </el-form-item>
              <el-form-item label="结束时间">
                <el-date-picker
                  v-model="searchForm.endTime"
                  type="datetime"
                  placeholder="选择结束时间"
                  value-format="YYYY-MM-DD HH:mm:ss"
                  style="width: 200px"
                />
              </el-form-item>
              <el-form-item label="访问路径">
                <el-input v-model="searchForm.path" placeholder="请输入访问路径" style="width: 200px" />
              </el-form-item>
              <el-form-item label="客户端ID">
                <el-input v-model="searchForm.clientId" placeholder="请输入客户端ID" style="width: 200px" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleSearch">查询</el-button>
                <el-button @click="handleReset">重置</el-button>
              </el-form-item>
            </el-form>
          </div>
          <el-table :data="logList" style="width: 100%" v-loading="loading" :row-style="{ height: '60px' }">
            <el-table-column prop="accessTime" label="访问时间" width="180" />
            <el-table-column prop="path" label="访问路径" />
            <el-table-column prop="clientId" label="客户端ID" width="150" />
            <el-table-column prop="params" label="访问参数" width="120">
              <template #default="scope">
                <el-link v-if="scope.row.params" type="primary" @click="showParams(scope.row.params)">查看</el-link>
                <span v-else>-</span>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination-container">
            <el-pagination
              :current-page="currentPage"
              :page-size="pageSize"
              :page-sizes="[10, 20, 50, 100]"
              :total="total"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
            />
          </div>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="archiveDialogVisible" title="归档日志" width="400px">
      <el-form :model="archiveForm" label-width="100px">
        <el-form-item label="归档日期">
          <el-date-picker
            v-model="archiveForm.beforeTime"
            type="date"
            placeholder="选择日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item>
          <span style="color: #909399; font-size: 12px;">归档日期之前的数据将移动到归档表</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="archiveDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleArchiveSubmit" :loading="archiveLoading">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="paramsDialogVisible" title="访问参数" width="600px">
      <pre style="max-height: 400px; overflow: auto;">{{ formattedParams }}</pre>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getApiAccessLogList,
  deleteApiAccessLog,
  archiveApiAccessLog,
  getApiAccessLogArchiveList,
  type ApiAccessLogData
} from '../api/api-access-log'

const activeTab = ref('log')
const logList = ref<ApiAccessLogData[]>([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)
const searchForm = ref({
  startTime: '',
  endTime: '',
  path: '',
  clientId: ''
})
const archiveDialogVisible = ref(false)
const archiveLoading = ref(false)
const paramsDialogVisible = ref(false)
const currentParams = ref('')
const archiveForm = ref({
  beforeTime: ''
})

const formattedParams = computed(() => {
  try {
    return JSON.stringify(JSON.parse(currentParams.value), null, 2)
  } catch {
    return currentParams.value
  }
})

const fetchList = async () => {
  loading.value = true
  try {
    const params = {
      ...searchForm.value,
      page: currentPage.value - 1,
      pageSize: pageSize.value
    }
    const response = activeTab.value === 'log'
      ? await getApiAccessLogList(currentPage.value - 1, pageSize.value, params)
      : await getApiAccessLogArchiveList(currentPage.value - 1, pageSize.value, params)
    logList.value = response.data.data.data
    total.value = response.data.data.pageInfo.totalRows
  } catch (error) {
    ElMessage.error('获取日志列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  fetchList()
}

const handleReset = () => {
  searchForm.value = {
    startTime: '',
    endTime: '',
    path: '',
    clientId: ''
  }
  currentPage.value = 1
  fetchList()
}

const handleSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
  fetchList()
}

const handleCurrentChange = (page: number) => {
  currentPage.value = page
  fetchList()
}

const handleTabChange = () => {
  currentPage.value = 1
  fetchList()
}

const handleDelete = (id: string) => {
  ElMessageBox.confirm('确定要删除该日志吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteApiAccessLog(id)
      ElMessage.success('删除成功')
      fetchList()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

const showParams = (params: string) => {
  currentParams.value = params
  paramsDialogVisible.value = true
}

const handleArchive = () => {
  archiveForm.value.beforeTime = ''
  archiveDialogVisible.value = true
}

const handleArchiveSubmit = async () => {
  if (!archiveForm.value.beforeTime) {
    ElMessage.warning('请选择归档日期')
    return
  }
  archiveLoading.value = true
  try {
    await archiveApiAccessLog(archiveForm.value.beforeTime)
    ElMessage.success('归档成功')
    archiveDialogVisible.value = false
    fetchList()
  } catch (error) {
    ElMessage.error('归档失败')
  } finally {
    archiveLoading.value = false
  }
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped>
.api-access-log-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-form {
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>