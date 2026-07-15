# 字段映射

字段映射（FieldMapping）定义了数据库查询字段与 API 返回字段之间的映射关系，用于自定义字段的展示名称和 Excel 导出格式。

## 字段映射实体

```java
@Entity
@Table(name = "api_field_mapping")
public class ApiFieldMapping {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;           // UUID 主键
    private String apiConfigId;  // 关联的 API 配置 ID
    private String fieldName;    // 数据库字段名（SQL 返回的列名）
    private String displayName;  // 展示名称（API 响应中的字段别名）
    private Integer columnWidth; // Excel 导出时的列宽（可选）
}
```

## 主要功能

### 1. 字段自动驼峰转换

系统默认将数据库列名（下划线风格）自动转换为驼峰格式：

| 数据库列名         | API 返回字段     |
| ------------------ | ---------------- |
| `user_name`        | `userName`       |
| `department_id`    | `departmentId`   |
| `created_at`       | `createdAt`      |

### 2. 自定义显示名称

通过字段映射，可以自定义字段在 API 响应中的名称，并在 Excel 导出时作为列标题。

```json
{
  "fieldName": "user_name",
  "displayName": "用户姓名",
  "columnWidth": 120
}
```

### 3. Excel 列宽设置

通过 `columnWidth` 字段可以设置导出 Excel 时的列宽，优化导出文件的阅读体验。

## 使用场景

### API 响应优化

配置字段映射前，API 返回原始字段名：
```json
{ "user_name": "张三", "dept_name": "技术部" }
```

配置字段映射后：

```json
// 映射配置：
// user_name → 用户姓名
// dept_name → 部门名称

// API 响应经过驼峰转换：
{ "userName": "张三", "deptName": "技术部" }
```

### Excel 导出优化

导出 Excel 时，字段映射会作为表头：

| 用户姓名 | 部门名称 | 年龄 |
| -------- | -------- | ---- |
| 张三     | 技术部   | 28   |
| 李四     | 市场部   | 32   |

## Excel 导出映射实现

```java
// 获取字段映射配置
List<ApiFieldMapping> fieldMappings = fieldMappingRepository.findByApiConfigId(apiId);

// 构建表头映射和列宽映射
Map<String, String> headerMapping = new HashMap<>();
Map<String, Integer> columnWidthMapping = new HashMap<>();

for (ApiFieldMapping mapping : fieldMappings) {
    headerMapping.put(mapping.getFieldName(), mapping.getDisplayName());
    if (mapping.getColumnWidth() != null) {
        columnWidthMapping.put(mapping.getFieldName(), mapping.getColumnWidth());
    }
}

// 导出 Excel（带自定义表头和列宽）
byte[] excelBytes = ExcelUtils.exportExcelWithHeaders(
    "Sheet1",
    dataList,
    headerMapping,
    columnWidthMapping
);
```

## 字段映射管理

字段映射与 API 配置绑定，在创建或更新 API 时一同提交：

```http
POST /apiConfig/add-with-mappings
Content-Type: application/json

{
  "apiConfig": { /* ... */ },
  "fieldMappings": [
    { "fieldName": "id", "displayName": "编号", "columnWidth": 80 },
    { "fieldName": "name", "displayName": "姓名", "columnWidth": 120 },
    { "fieldName": "age", "displayName": "年龄", "columnWidth": 60 }
  ],
  "pluginIds": []
}
```

::: tip
字段映射配置是可选的。如果不配置，系统会自动对字段名做驼峰转换后返回。
:::
