<template>
  <div class="paginated-table">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>{{ title }}</span>
          <el-button v-if="showAdd" type="primary" @click="handleAdd">新增</el-button>
        </div>
      </template>
      <div v-if="searchFields && searchFields.length > 0" class="search-form">
        <el-form :inline="true" :model="searchForm" class="demo-form-inline">
          <el-form-item v-for="field in searchFields" :key="field.prop" :label="field.label">
            <el-input 
              v-if="field.type === 'input'" 
              v-model="searchForm[field.prop]" 
              :placeholder="`请输入${field.label}`"
              :style="{ width: field.width || '200px' }"
            />
            <el-select 
              v-else-if="field.type === 'select'" 
              v-model="searchForm[field.prop]" 
              :placeholder="`请选择${field.label}`"
              :style="{ width: field.width || '200px' }"
            >
              <el-option 
                v-for="option in field.options" 
                :key="option.value" 
                :label="option.label" 
                :value="option.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
      <el-table 
        :data="tableData" 
        style="width: 100%" 
        v-loading="loading"
        :row-style="{ height: '60px' }"
      >
        <el-table-column
          v-for="column in columns"
          :key="column.prop"
          :prop="column.prop"
          :label="column.label"
          :width="column.width"
        >
          <template #default="scope">
            <el-tooltip :content="column.formatter ? column.formatter(scope.row, column, scope.row[column.prop]) : scope.row[column.prop]" placement="top" :disabled="String(column.formatter ? column.formatter(scope.row, column, scope.row[column.prop]) : scope.row[column.prop]).length < 20">
              <div class="ellipsis">{{ column.formatter ? column.formatter(scope.row, column, scope.row[column.prop]) : scope.row[column.prop] }}</div>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column label="操作" :width="actionsWidth">
          <template #default="scope">
            <el-button v-if="showView" size="small" @click="handleView(scope.row)">查看</el-button>
            <el-button v-if="showEdit" size="small" type="primary" @click="handleEdit(scope.row)">修改</el-button>
            <el-button v-if="showTest" size="small" type="success" @click="handleTest(scope.row)">测试连接</el-button>
            <el-button v-if="showDelete" size="small" type="danger" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-container">
        <el-pagination
          :current-page="currentPage"
          :page-size="pageSize"
          :page-sizes="[5, 10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        ></el-pagination>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { watch, reactive } from 'vue'

interface Column {
  prop: string
  label: string
  width?: number | string
  formatter?: (row: any, column: any, cellValue: any) => string
}

interface SearchField {
  prop: string
  label: string
  type?: 'input' | 'select'
  width?: number | string
  options?: { value: string | number; label: string }[]
}

interface Props {
  title: string
  columns: Column[]
  searchFields?: SearchField[]
  tableData: any[]
  loading: boolean
  total: number
  currentPage: number
  pageSize: number
  showAdd?: boolean
  showView?: boolean
  showEdit?: boolean
  showTest?: boolean
  showDelete?: boolean
  actionsWidth?: number | string
}

const props = withDefaults(defineProps<Props>(), {
  searchFields: () => [],
  showAdd: true,
  showView: true,
  showEdit: true,
  showTest: false,
  showDelete: true,
  actionsWidth: 300
})

const emit = defineEmits<{
  add: []
  view: [row: any]
  edit: [row: any]
  test: [row: any]
  delete: [row: any]
  'update:page': [page: number, pageSize: number]
  search: [params: Record<string, any>]
  reset: []
}>()

const searchForm = reactive<Record<string, any>>({})

// 初始化搜索表单
watch(() => props.searchFields, (newFields) => {
  if (newFields) {
    newFields.forEach(field => {
      searchForm[field.prop] = ''
    })
  }
}, { immediate: true })


const handleAdd = () => {
  emit('add')
}

const handleView = (row: any) => {
  emit('view', row)
}

const handleEdit = (row: any) => {
  emit('edit', row)
}

const handleTest = (row: any) => {
  emit('test', row)
}

const handleDelete = (row: any) => {
  emit('delete', row)
}

const handleSizeChange = (size: number) => {
  emit('update:page', 1, size)
}

const handleCurrentChange = (page: number) => {
  emit('update:page', page, props.pageSize)
}

const handleSearch = () => {
  emit('search', searchForm)
}

const handleReset = () => {
  if (props.searchFields) {
    props.searchFields.forEach(field => {
      searchForm[field.prop] = ''
    })
  }
  emit('reset')
}
</script>

<style scoped>
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

.ellipsis {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 100%;
}

.el-table td {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>