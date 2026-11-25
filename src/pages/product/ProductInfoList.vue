<script setup>
import { computed, onMounted, ref } from 'vue'
import { message, Modal } from 'ant-design-vue'
import {
  batchProductStatus,
  deleteProduct,
  exportProduct,
  pageProduct,
  updateProductStatus,
} from '@/api/product/product.js'
import { listCategory } from '@/api/product/category.js'
import { useListPage } from '@/composables/useListPage.js'
import { PRODUCT_STATUS } from '@/utils/dict.js'
import { formatAmount, formatQuantity } from '@/utils/number.js'
import ProductInfoModal from './modules/ProductInfoModal.vue'
import ProductImportModal from './modules/ProductImportModal.vue'
import ProductBatchEditModal from './modules/ProductBatchEditModal.vue'
import ProductBatchNumberModal from './modules/ProductBatchNumberModal.vue'

// ---------------- 下拉选项 ----------------
const categoryOptions = ref([])

async function loadCategoryOptions() {
    const res = await listCategory()
    categoryOptions.value = res.data.data
}

// ---------------- 列表 ----------------
const queryWithDefaults = { keyword: '', status: undefined, categoryId: undefined }
const { records, total, loading, pageNum, pageSize, query, load, handleSearch, handleReset, handlePageChange } =
    useListPage(pageProduct, queryWithDefaults)

// 可勾选的全部数据列（选择列由 rowSelection 提供，操作列固定显示）
const allColumns = [
    { title: '商品编码', dataIndex: 'productNo', key: 'productNo', width: 130 },
    { title: '商品名称', key: 'name', width: 200 },
    { title: '类目', dataIndex: 'categoryName', key: 'categoryName', width: 130 },
    { title: '默认单位', dataIndex: 'unitName', key: 'unitName', width: 100 },
    { title: '规格数', dataIndex: 'skuCount', key: 'skuCount', width: 80, align: 'center' },
    { title: '采购价', dataIndex: 'purchasePrice', key: 'purchasePrice', width: 100, align: 'right' },
    { title: '零售价', dataIndex: 'retailPrice', key: 'retailPrice', width: 100, align: 'right' },
    { title: '销售价', dataIndex: 'salePrice', key: 'salePrice', width: 100, align: 'right' },
    { title: '最低售价', dataIndex: 'lowPrice', key: 'lowPrice', width: 100, align: 'right' },
    { title: '库存数量', dataIndex: 'stockQuantity', key: 'stockQuantity', width: 100, align: 'right' },
    { title: '成本价', dataIndex: 'costPrice', key: 'costPrice', width: 100, align: 'right' },
    { title: '状态', dataIndex: 'status', key: 'status', width: 90, align: 'center' },
]
const allColumnKeys = allColumns.map(item => item.key)

// 列设置：勾选要显示的列，选择结果持久化到 localStorage
const COLUMN_STORAGE_KEY = 'erp_product_columns'
const visibleColumnKeys = ref([...allColumnKeys])

function loadColumnSetting() {
    const saved = localStorage.getItem(COLUMN_STORAGE_KEY)
    if (!saved) {
        return
    }
    try {
        const keys = JSON.parse(saved)
        if (Array.isArray(keys) && keys.length > 0) {
            visibleColumnKeys.value = keys.filter(key => allColumnKeys.includes(key))
        }
    } catch (error) {
        // 配置损坏时忽略，用默认列
    }
}

function onColumnChange(checkedKeys) {
    visibleColumnKeys.value = checkedKeys
    localStorage.setItem(COLUMN_STORAGE_KEY, JSON.stringify(checkedKeys))
}

const columns = computed(() => {
    const chosen = allColumns.filter(item => visibleColumnKeys.value.includes(item.key))
    return [...chosen, { title: '操作', key: 'action', width: 200, align: 'center' }]
})

// ---------------- 批量删除 ----------------
const selectedRowKeys = ref([])
const rowSelection = {
    selectedRowKeys,
    onChange(keys) {
        selectedRowKeys.value = keys
    },
}

// ---------------- 新增 / 修改弹窗 ----------------
const modalVisible = ref(false)
const editingId = ref(null)

function onCreate() {
    editingId.value = null
    modalVisible.value = true
}

function onEdit(record) {
    editingId.value = record.id
    modalVisible.value = true
}

function onSaved() {
    modalVisible.value = false
    selectedRowKeys.value = []
    load()
}

// ---------------- 批量操作 ----------------
const importVisible = ref(false)
const batchEditVisible = ref(false)
const numberModalVisible = ref(false)
const numberMode = ref('stock')

function requireSelection() {
    if (selectedRowKeys.value.length === 0) {
        message.warning('请先选择商品')
        return false
    }
    return true
}

function onBatchSaved() {
    selectedRowKeys.value = []
    load()
}

async function onBatchEnable() {
    if (!requireSelection()) return
    await batchProductStatus(selectedRowKeys.value, '1')
    message.success(`已启用 ${selectedRowKeys.value.length} 个商品`)
    onBatchSaved()
}

async function onBatchDisable() {
    if (!requireSelection()) return
    await batchProductStatus(selectedRowKeys.value, '2')
    message.success(`已停用 ${selectedRowKeys.value.length} 个商品`)
    onBatchSaved()
}

function onBatchEdit() {
    if (requireSelection()) batchEditVisible.value = true
}

function onBatchStock() {
    if (!requireSelection()) return
    numberMode.value = 'stock'
    numberModalVisible.value = true
}

function onBatchCost() {
    if (!requireSelection()) return
    numberMode.value = 'cost'
    numberModalVisible.value = true
}

function onImported() {
    onBatchSaved()
}

// ---------------- 单条操作 ----------------
async function onToggleStatus(record) {
    const target = record.status === '1' ? '2' : '1'
    await updateProductStatus(record.id, target)
    message.success(target === '1' ? '已上架' : '已下架')
    await load()
}

function onDelete(record) {
    Modal.confirm({
        title: '删除确认',
        content: `确定要删除商品「${record.name}」吗？`,
        okText: '确定',
        cancelText: '取消',
        okType: 'danger',
        async onOk() {
            await deleteProduct(record.id)
            message.success('删除成功')
            await load()
        },
    })
}

function onBatchDelete() {
    if (selectedRowKeys.value.length === 0) {
        message.warning('请先选择要删除的商品')
        return
    }
    const count = selectedRowKeys.value.length
    Modal.confirm({
        title: '批量删除确认',
        content: `确定要删除选中的 ${count} 个商品吗？`,
        okText: '确定',
        cancelText: '取消',
        okType: 'danger',
        async onOk() {
            for (const id of selectedRowKeys.value) {
                await deleteProduct(id)
            }
            selectedRowKeys.value = []
            message.success(`已删除 ${count} 个商品`)
            await load()
        },
    })
}

async function onExport() {
    const res = await exportProduct(query)
    const blob = new Blob([res.data], {
        type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = '商品信息.xlsx'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(url)
    message.success('导出成功')
}

function statusColor(status) {
    for (const item of PRODUCT_STATUS) {
        if (item.value === status) {
            return item.color
        }
    }
    return 'default'
}

function statusLabel(status) {
    for (const item of PRODUCT_STATUS) {
        if (item.value === status) {
            return item.label
        }
    }
    return '-'
}

onMounted(() => {
    loadCategoryOptions()
    loadColumnSetting()
    load()
})
</script>

<template>
  <a-card :bordered="false">
    <!-- 查询区 -->
    <a-form :model="query" layout="inline" @finish="handleSearch">
      <a-form-item label="商品名称/编码">
        <a-input
            v-model:value="query.keyword"
            placeholder="名称 / 编码 / 助记码"
            allow-clear
            style="width: 220px"
        />
      </a-form-item>
      <a-form-item label="状态">
        <a-select
            v-model:value="query.status"
            placeholder="全部状态"
            allow-clear
            style="width: 120px"
            :options="PRODUCT_STATUS.map(item => ({ value: item.value, label: item.label }))"
        />
      </a-form-item>
      <a-form-item label="类目">
        <a-select
            v-model:value="query.categoryId"
            placeholder="全部类目"
            allow-clear
            show-search
            style="width: 160px"
            :options="categoryOptions.map(item => ({ value: item.id, label: item.name }))"
        />
      </a-form-item>
      <a-form-item>
        <a-space>
          <a-button type="primary" html-type="submit">查询</a-button>
          <a-button @click="handleReset">重置</a-button>
        </a-space>
      </a-form-item>
    </a-form>

    <!-- 工具栏 -->
    <div class="toolbar">
      <a-space wrap>
        <a-button type="primary" @click="onCreate">新增商品</a-button>
        <a-button danger :disabled="selectedRowKeys.length === 0" @click="onBatchDelete">批量删除</a-button>
        <a-button @click="importVisible = true">导入</a-button>
        <a-button @click="onExport">导出 Excel</a-button>
        <a-button @click="onBatchEnable">启用</a-button>
        <a-button @click="onBatchDisable">禁用</a-button>
        <a-button @click="onBatchEdit">批量编辑</a-button>
        <a-button @click="onBatchStock">修正库存</a-button>
        <a-button @click="onBatchCost">修正成本</a-button>
        <a-popover trigger="click" placement="bottomRight">
          <template #content>
            <div class="column-setting">
              <div class="column-setting-title">显示的列</div>
              <a-checkbox-group
                  v-model:value="visibleColumnKeys"
                  :options="allColumns.map(item => ({ label: item.title, value: item.key }))"
                  @change="onColumnChange"
              />
            </div>
          </template>
          <a-button>列设置 ▾</a-button>
        </a-popover>
        <a-button @click="load">刷新</a-button>
      </a-space>
    </div>

    <!-- 表格 -->
    <a-table
        :columns="columns"
        :data-source="records"
        :loading="loading"
        :pagination="false"
        :row-selection="rowSelection"
        row-key="id"
        size="middle"
        bordered
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'name'">
          <div class="product-name">{{ record.name }}</div>
          <div v-if="record.mnemonic" class="product-sub">{{ record.mnemonic }}</div>
        </template>

        <template v-else-if="['purchasePrice', 'retailPrice', 'salePrice', 'lowPrice'].includes(column.key)">
          {{ formatAmount(record[column.dataIndex]) }}
        </template>

        <template v-else-if="column.key === 'stockQuantity'">
          {{ formatQuantity(record.stockQuantity) }}
        </template>

        <template v-else-if="column.key === 'costPrice'">
          {{ formatAmount(record.costPrice) }}
        </template>

        <template v-else-if="column.key === 'status'">
          <a-tag :color="statusColor(record.status)">{{ statusLabel(record.status) }}</a-tag>
        </template>

        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button type="link" size="small" @click="onEdit(record)">修改</a-button>
            <a-button type="link" size="small" @click="onToggleStatus(record)">
              {{ record.status === '1' ? '下架' : '上架' }}
            </a-button>
            <a-button type="link" size="small" danger @click="onDelete(record)">删除</a-button>
          </a-space>
        </template>
      </template>
    </a-table>

    <!-- 分页 -->
    <div class="pager">
      <a-pagination
          :current="pageNum"
          :page-size="pageSize"
          :total="total"
          :page-size-options="['10', '20', '50', '100']"
          show-size-changer
          show-quick-jumper
          :show-total="(t) => `共 ${t} 条`"
          @change="handlePageChange"
      />
    </div>

    <!-- 新增 / 修改弹窗 -->
    <ProductInfoModal
        v-model:visible="modalVisible"
        :product-id="editingId"
        :category-options="categoryOptions"
        @saved="onSaved"
    />
    <ProductImportModal
        v-model:visible="importVisible"
        @imported="onImported"
    />
    <ProductBatchEditModal
        v-model:visible="batchEditVisible"
        :ids="selectedRowKeys"
        :category-options="categoryOptions"
        @saved="onBatchSaved"
    />
    <ProductBatchNumberModal
        v-model:visible="numberModalVisible"
        :ids="selectedRowKeys"
        :mode="numberMode"
        @saved="onBatchSaved"
    />
  </a-card>
</template>

<style scoped>
.toolbar {
  margin-bottom: 12px;
}

.pager {
  margin-top: 16px;
  text-align: right;
}

.product-name {
  font-weight: 500;
}

.product-sub {
  color: #999;
  font-size: 12px;
}

.column-setting-title {
  font-weight: 500;
  margin-bottom: 8px;
}

.column-setting :deep(.ant-checkbox-wrapper) {
  display: flex;
  margin-bottom: 4px;
}
</style>
