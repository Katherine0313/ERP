<script setup>
import { onMounted, ref, reactive } from 'vue'
import { message, Modal } from 'ant-design-vue'
import {
  addUnit,
  batchUnitStatus,
  deleteUnit,
  listBasicUnit,
  pageUnit,
  updateUnit,
  updateUnitStatus,
} from '@/api/product/unit.js'
import { useListPage } from '@/composables/useListPage.js'
import { formatQuantity } from '@/utils/number.js'

// ---------------- 基础单位下拉（新增副单位用） ----------------
const basicOptions = ref([])

async function loadBasicOptions() {
    const res = await listBasicUnit()
    basicOptions.value = res.data.data
}

// ---------------- 列表 ----------------
const { records, total, loading, pageNum, pageSize, query, load, handleSearch, handleReset, handlePageChange } =
    useListPage(pageUnit, { keyword: '' })

// 勾选列
const selectedRowKeys = ref([])
const rowSelection = {
    selectedRowKeys,
    onChange(keys) {
        selectedRowKeys.value = keys
    },
}

const columns = [
    { title: '序号', key: 'index', width: 60, align: 'center' },
    { title: '单位名称', dataIndex: 'name', key: 'name', width: 140 },
    { title: '类型', key: 'type', width: 110, align: 'center' },
    { title: '所属基础单位', dataIndex: 'groupName', key: 'groupName', width: 140 },
    { title: '换算比例', key: 'ratio', width: 180 },
    { title: '排序', dataIndex: 'sort', key: 'sort', width: 90 },
    { title: '状态', key: 'enabled', width: 100, align: 'center' },
    { title: '操作', key: 'action', width: 220, align: 'center' },
]

// ---------------- 弹窗表单 ----------------
const modalVisible = ref(false)
const modalLoading = ref(false)
const isEdit = ref(false)
const formRef = ref()
const form = reactive({
    id: null,
    name: '',
    isBasic: true,
    groupId: undefined,
    ratio: null,
    sort: '',
    enabled: true,
})

const formRules = {
    name: [{ required: true, message: '请输入单位名称', trigger: 'blur' }],
    groupId: [{ required: true, message: '请选择所属基础单位', trigger: 'change' }],
    ratio: [{ required: true, message: '请输入换算比例', trigger: 'blur' }],
}

function onCreate() {
    isEdit.value = false
    form.id = null
    form.name = ''
    form.isBasic = true
    form.groupId = undefined
    form.ratio = null
    form.sort = ''
    form.enabled = true
    modalVisible.value = true
}

function onEdit(record) {
    isEdit.value = true
    form.id = record.id
    form.name = record.name
    form.isBasic = record.isBasic === '1'
    form.groupId = form.isBasic ? undefined : (record.groupId || undefined)
    form.ratio = record.ratio
    form.sort = record.sort || ''
    form.enabled = record.enabled
    modalVisible.value = true
}

async function onSubmit() {
    try {
        await formRef.value.validate()
    } catch (error) {
        return
    }
    modalLoading.value = true
    try {
        const payload = {
            name: form.name.trim(),
            isBasic: form.isBasic,
            groupId: form.isBasic ? 0 : form.groupId,
            ratio: form.isBasic ? 1 : form.ratio,
            sort: form.sort,
            enabled: form.enabled,
        }
        if (isEdit.value) {
            await updateUnit(form.id, payload)
            message.success('修改成功')
        } else {
            await addUnit(payload)
            message.success('新增成功')
        }
        modalVisible.value = false
        await Promise.all([load(), loadBasicOptions()])
    } finally {
        modalLoading.value = false
    }
}

function onDelete(record) {
    Modal.confirm({
        title: '删除确认',
        content: `确定要删除单位「${record.name}」吗？`,
        okText: '确定',
        cancelText: '取消',
        okType: 'danger',
        async onOk() {
            await deleteUnit(record.id)
            message.success('删除成功')
            await Promise.all([load(), loadBasicOptions()])
        },
    })
}

async function onToggleStatus(record) {
    const target = !record.enabled
    await updateUnitStatus(record.id, target)
    message.success(target ? '已启用' : '已停用')
    await Promise.all([load(), loadBasicOptions()])
}

// ---------------- 批量启停 ----------------
function requireSelection() {
    if (selectedRowKeys.value.length === 0) {
        message.warning('请先选择要操作的单位')
        return false
    }
    return true
}

async function onBatchEnable() {
    if (!requireSelection()) return
    await batchUnitStatus(selectedRowKeys.value, true)
    message.success(`已启用 ${selectedRowKeys.value.length} 个单位`)
    afterBatch()
}

async function onBatchDisable() {
    if (!requireSelection()) return
    await batchUnitStatus(selectedRowKeys.value, false)
    message.success(`已停用 ${selectedRowKeys.value.length} 个单位`)
    afterBatch()
}

function afterBatch() {
    selectedRowKeys.value = []
    Promise.all([load(), loadBasicOptions()])
}

onMounted(() => {
    loadBasicOptions()
    load()
})
</script>

<template>
  <a-card :bordered="false">
    <!-- 查询区 -->
    <a-form :model="query" layout="inline" @finish="handleSearch">
      <a-form-item label="单位名称">
        <a-input
            v-model:value="query.keyword"
            placeholder="请输入单位名称"
            allow-clear
            style="width: 200px"
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
      <a-space>
        <a-button type="primary" @click="onCreate">新增</a-button>
        <a-button @click="onBatchEnable">启用</a-button>
        <a-button @click="onBatchDisable">禁用</a-button>
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
      <template #bodyCell="{ column, record, index }">
        <template v-if="column.key === 'index'">
          {{ (pageNum - 1) * pageSize + index + 1 }}
        </template>

        <template v-else-if="column.key === 'type'">
          <a-tag :color="record.isBasic === '1' ? 'blue' : 'default'">
            {{ record.isBasic === '1' ? '基础单位' : '副单位' }}
          </a-tag>
        </template>

        <template v-else-if="column.key === 'ratio'">
          <span v-if="record.isBasic === '1'">1</span>
          <span v-else>1 {{ record.name }} = {{ formatQuantity(record.ratio) }} {{ record.groupName || '基础单位' }}</span>
        </template>

        <template v-else-if="column.key === 'enabled'">
          <a-tag :color="record.enabled ? 'success' : 'default'">
            {{ record.enabled ? '启用' : '停用' }}
          </a-tag>
        </template>

        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button type="link" size="small" @click="onEdit(record)">修改</a-button>
            <a-button type="link" size="small" @click="onToggleStatus(record)">
              {{ record.enabled ? '停用' : '启用' }}
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

    <!-- 新增/修改弹窗 -->
    <a-modal
        v-model:visible="modalVisible"
        :title="isEdit ? '修改单位' : '新增单位'"
        :confirm-loading="modalLoading"
        ok-text="确定"
        cancel-text="取消"
        @ok="onSubmit"
    >
      <a-form ref="formRef" :model="form" :rules="formRules" layout="vertical">
        <a-form-item label="单位名称" name="name">
          <a-input v-model:value="form.name" placeholder="如 个 / 箱 / 托" allow-clear />
        </a-form-item>
        <a-form-item label="单位类型">
          <a-select
              v-model:value="form.isBasic"
              :disabled="isEdit"
              :options="[
                { value: true, label: '基础单位' },
                { value: false, label: '副单位' },
              ]"
          />
        </a-form-item>
        <template v-if="!form.isBasic">
          <a-form-item label="所属基础单位" name="groupId">
            <a-select
                v-model:value="form.groupId"
                placeholder="请选择基础单位"
                show-search
                :options="basicOptions.map(item => ({ value: item.id, label: item.name }))"
            />
          </a-form-item>
          <a-form-item label="换算比例" name="ratio">
            <a-input-number
                v-model:value="form.ratio"
                :min="0"
                placeholder="1 副单位 = ? 基础单位"
                style="width: 100%"
            />
          </a-form-item>
        </template>
        <a-form-item label="排序">
          <a-input v-model:value="form.sort" placeholder="数字越小越靠前" allow-clear />
        </a-form-item>
        <a-form-item label="状态">
          <a-switch v-model:checked="form.enabled" checked-children="启用" un-checked-children="停用" />
        </a-form-item>
      </a-form>
    </a-modal>
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
</style>
