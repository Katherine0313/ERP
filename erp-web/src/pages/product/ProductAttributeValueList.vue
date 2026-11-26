<script setup>
import { onMounted, ref, reactive } from 'vue'
import { message, Modal } from 'ant-design-vue'
import {
  addAttributeValue,
  deleteAttributeValue,
  pageAttributeValue,
  updateAttributeValue,
  updateAttributeValueStatus,
} from '@/api/product/attributeValue.js'
import { listAttributeGroup } from '@/api/product/attributeGroup.js'
import { useListPage } from '@/composables/useListPage.js'

// ---------------- 属性组下拉 ----------------
const groupOptions = ref([])

async function loadGroupOptions() {
    const res = await listAttributeGroup()
    groupOptions.value = res.data.data
}

// ---------------- 列表 ----------------
const { records, total, loading, pageNum, pageSize, query, load, handleSearch, handleReset, handlePageChange } =
    useListPage(pageAttributeValue, { keyword: '', groupId: undefined })

const columns = [
    { title: '序号', key: 'index', width: 60, align: 'center' },
    { title: '属性组', dataIndex: 'groupName', key: 'groupName', width: 140 },
    { title: '属性值', dataIndex: 'value', key: 'value' },
    { title: '排序', dataIndex: 'sort', key: 'sort', width: 100 },
    { title: '状态', key: 'enabled', width: 100, align: 'center' },
    { title: '操作', key: 'action', width: 240, align: 'center' },
]

// ---------------- 弹窗表单 ----------------
const modalVisible = ref(false)
const modalLoading = ref(false)
const isEdit = ref(false)
const formRef = ref()
const form = reactive({
    id: null,
    groupId: undefined,
    value: '',
    sort: '',
    enabled: true,
})

const formRules = {
    groupId: [{ required: true, message: '请选择所属属性组', trigger: 'change' }],
    value: [{ required: true, message: '请输入属性值', trigger: 'blur' }],
}

/**
 * 打开新增弹窗
 */
function onCreate() {
    isEdit.value = false
    form.id = null
    form.groupId = query.groupId
    form.value = ''
    form.sort = ''
    form.enabled = true
    modalVisible.value = true
}

/**
 * 打开修改弹窗
 */
function onEdit(record) {
    isEdit.value = true
    form.id = record.id
    form.groupId = record.groupId
    form.value = record.value
    form.sort = record.sort || ''
    form.enabled = record.enabled
    modalVisible.value = true
}

/**
 * 提交
 */
async function onSubmit() {
    try {
        await formRef.value.validate()
    } catch (error) {
        return
    }
    modalLoading.value = true
    try {
        const payload = {
            groupId: form.groupId,
            value: form.value.trim(),
            sort: form.sort,
            enabled: form.enabled,
        }
        if (isEdit.value) {
            await updateAttributeValue(form.id, payload)
            message.success('修改成功')
        } else {
            await addAttributeValue(payload)
            message.success('新增成功')
        }
        modalVisible.value = false
        await load()
    } finally {
        modalLoading.value = false
    }
}

/**
 * 删除
 */
function onDelete(record) {
    Modal.confirm({
        title: '删除确认',
        content: `确定要删除属性值「${record.value}」吗？`,
        okText: '确定',
        cancelText: '取消',
        okType: 'danger',
        async onOk() {
            await deleteAttributeValue(record.id)
            message.success('删除成功')
            await load()
        },
    })
}

/**
 * 启用/停用
 */
async function onToggleStatus(record) {
    const target = !record.enabled
    await updateAttributeValueStatus(record.id, target)
    message.success(target ? '已启用' : '已停用')
    await load()
}

// 初始加载
onMounted(() => {
    loadGroupOptions()
    load()
})
</script>

<template>
  <a-card :bordered="false">
    <!-- 查询区 -->
    <a-form :model="query" layout="inline" @finish="handleSearch">
      <a-form-item label="属性组">
        <a-select
            v-model:value="query.groupId"
            placeholder="全部属性组"
            allow-clear
            show-search
            style="width: 160px"
            :options="groupOptions.map(item => ({ value: item.id, label: item.name }))"
        />
      </a-form-item>
      <a-form-item label="属性值">
        <a-input
            v-model:value="query.keyword"
            placeholder="请输入属性值"
            allow-clear
            style="width: 180px"
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
        <a-button @click="load">刷新</a-button>
      </a-space>
    </div>

    <!-- 表格 -->
    <a-table
        :columns="columns"
        :data-source="records"
        :loading="loading"
        :pagination="false"
        row-key="id"
        size="middle"
        bordered
    >
      <template #bodyCell="{ column, record, index }">
        <template v-if="column.key === 'index'">
          {{ (pageNum - 1) * pageSize + index + 1 }}
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

    <!-- 新增/修改弹窗（ant-design-vue 3.x 用 visible，不是 4.x 的 open） -->
    <a-modal
        v-model:visible="modalVisible"
        :title="isEdit ? '修改属性值' : '新增属性值'"
        :confirm-loading="modalLoading"
        ok-text="确定"
        cancel-text="取消"
        @ok="onSubmit"
    >
      <a-form ref="formRef" :model="form" :rules="formRules" layout="vertical">
        <a-form-item label="所属属性组" name="groupId">
          <a-select
              v-model:value="form.groupId"
              placeholder="请选择所属属性组"
              show-search
              :options="groupOptions.map(item => ({ value: item.id, label: item.name }))"
          />
        </a-form-item>
        <a-form-item label="属性值" name="value">
          <a-input v-model:value="form.value" placeholder="如 黑色 / Pro / 256G" allow-clear />
        </a-form-item>
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
