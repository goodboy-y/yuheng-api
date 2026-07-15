# 数据导出

玉衡API 支持将 API 查询结果导出为 Excel 文件（`.xlsx` 格式），自动应用字段映射中的显示名称作为表头。

## 导出方式

### 方式一：通过数据 API 导出

在数据 API 路径前添加 `/export` 前缀：

```http
GET /api/export/{path}?param1=value1
```

请求头同样需要 `clientId` 和 `secret`。

示例：

```shell
curl -o person.xlsx \
  'http://localhost:8520/yuheng-api/api/export/person?dept=技术部' \
  -H 'clientId: <your-client-id>' \
  -H 'secret: <your-secret>'
```

响应：
- Content-Type: `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`
- Content-Disposition: `attachment; filename="查询人员_1700000000000.xlsx"`

### 方式二：通过管理端测试导出

在管理端测试 API 时，可以通过导出接口下载测试结果：

```http
POST /apiConfig/export/{apiId}
Content-Type: application/json

{
  "dept": "技术部"
}
```

## Excel 导出特性

### 自动表头映射

导出时会自动应用字段映射配置：

- 配置了字段映射 → 使用 `displayName` 作为列标题
- 未配置字段映射 → 使用驼峰转换后的字段名
- 配置了 `columnWidth` → 设置对应的列宽

### 导出示例

**SQL 查询结果：**
```json
[
  { "userId": 1, "userName": "张三", "age": 28, "deptName": "技术部" },
  { "userId": 2, "userName": "李四", "age": 32, "deptName": "技术部" }
]
```

**字段映射配置：**
| fieldName  | displayName | columnWidth |
| ---------- | ----------- | ----------- |
| userId     | 用户编号     | 80          |
| userName   | 用户姓名     | 120         |
| age        | 年龄        | 60          |
| deptName   | 部门        | 150         |

**导出 Excel：**

| 用户编号 | 用户姓名 | 年龄 | 部门   |
| -------- | -------- | ---- | ------ |
| 1        | 张三     | 28   | 技术部 |
| 2        | 李四     | 32   | 技术部 |

## 导出实现

```java
// ApiController.exportData() 核心逻辑

// 1. 执行查询
Result<Object> result = apiService.executeSql(request, config, datasource);

// 2. 获取字段映射
List<ApiFieldMapping> fieldMappings = fieldMappingRepository.findByApiConfigId(config.getId());
Map<String, String> headerMapping = new HashMap<>();
for (ApiFieldMapping mapping : fieldMappings) {
    headerMapping.put(mapping.getFieldName(), mapping.getDisplayName());
}

// 3. 生成 Excel 文件
byte[] excelBytes = ExcelUtils.exportExcelWithHeaders(
    "API数据导出",
    (List<Map<String, Object>>) result.getData(),
    headerMapping
);

// 4. 返回文件下载
return ResponseEntity.ok()
    .contentType(MediaType.parseMediaType(
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
    .header(HttpHeaders.CONTENT_DISPOSITION,
        "attachment; filename=\"" + encodedFileName + "\"")
    .body(excelBytes);
```

## 技术细节

- 导出使用 Apache POI 生成 `.xlsx` 格式文件
- 文件名格式：`{API名称}_{时间戳}.xlsx`
- 文件名使用 UTF-8 编码并 URL 编码处理
- 支持 CORS，`Content-Disposition` 响应头对前端可见
- 导出数据量没有硬限制，但建议控制查询结果集大小
