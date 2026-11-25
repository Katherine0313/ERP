<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { message, Modal } from 'ant-design-vue'
import {
  addCategory,
  deleteCategory,
  listCategory,
  treeCategory,
  updateCategory,
} from '@/api/product/category.js'

// ---------------- 左侧树 ----------------
const treeData = ref([])
const loading = ref(false)
const selectedKeys = ref([])
const expandedKeys = ref([])
const treeSearch = ref('')

// ---------------- 右侧表单 ----------------
// formMode: 'view' 查看选中项 / 'create' 新增 / 'edit' 修改
const formMode = ref('view')
const saving = ref(false)
const form = reactive({
  id: null,
  parentId: 0,
  name: '',
  serialNo: '',
  sort: '',
  remark: '',
})
const formRef = ref()

const formRules = {
  name: [{ required: true, message: '请输入类目名称', trigger: 'blur' }],
}

// 当前选中的节点
const currentNode = computed(() => {
  if (selectedKeys.value.length === 0) {
    return null
  }
  return findNode(treeData.value, selectedKeys.value[0])
})

// 表单是否有可编辑内容
const formDisabled = computed(() => formMode.value === 'view')

// 树标题：新增时显示「新增类目 - 上级：xxx」
const formTitle = computed(() => {
  if (formMode.value === 'create') {
    const parentName = form.parentId === 0 ? '顶级' : findNameById(treeData.value, form.parentId)
    return `新增类目（上级：${parentName}）`
  }
  if (formMode.value === 'edit') {
    return '修改类目'
  }
  return '类目信息'
})

/**
 * 在树里按 id 找节点
 */
function findNode(nodes, id) {
  for (const node of nodes) {
    if (node.key === id) {
      return node
    }
    if (node.children && node.children.length > 0) {
      const hit = findNode(node.children, id)
      if (hit) {
        return hit
      }
    }
  }
  return null
}

/**
 * 在树里按 id 找名称
 */
function findNameById(nodes, id) {
  const node = findNode(nodes, id)
  return node ? node.title : '-'
}

/**
 * 后端 /tree 返回的是嵌套结构，这里转成 a-tree 需要的 key/title/children 格式
 */
function buildTree(treeList) {
  const result = []
  for (const item of treeList) {
    const node = {
      key: item.id,
      title: item.name,
      raw: item,
      children: [],
    }
    if (item.children && item.children.length > 0) {
      node.children = buildTree(item.children)
    }
    result.push(node)
  }
  return result
}

/**
 * 按关键字过滤树，命中节点及其父链保留
 */
function filterTree(nodes, keyword) {
  if (!keyword) {
    return nodes
  }
  const result = []
  for (const node of nodes) {
    const children = filterTree(node.children || [], keyword)
    const hit = node.title.includes(keyword)
    if (hit || children.length > 0) {
      result.push({
        ...node,
        children,
      })
    }
  }
  return result
}

/**
 * 加载类目树
 */
async function loadTree() {
  loading.value = true
  try {
    const res = await treeCategory()
    const flatList = res.data.data
    const allNodes = buildTree(flatList)
    treeData.value = allNodes
    // 默认展开第一层
    const firstLevel = []
    for (const node of allNodes) {
      firstLevel.push(node.key)
    }
    expandedKeys.value = firstLevel
  } finally {
    loading.value = false
  }
}

/**
 * 树搜索
 */
const searchedTreeData = computed(() => filterTree(treeData.value, treeSearch.value.trim()))

/**
 * 树节点选中变化
 */
function onTreeSelect(keys) {
  selectedKeys.value = keys
  if (keys.length === 0) {
    formMode.value = 'view'
    return
  }
  const node = currentNode.value
  if (!node) {
    return
  }
  const raw = node.raw
  form.id = raw.id
  form.parentId = raw.parentId || 0
  form.name = raw.name
  form.serialNo = raw.serialNo || ''
  form.sort = raw.sort || ''
  form.remark = raw.remark || ''
  formMode.value = 'view'
}

/**
 * 新增：上级默认取当前选中节点
 */
function onCreate() {
  formMode.value = 'create'
  form.id = null
  form.parentId = currentNode.value ? currentNode.value.raw.id : 0
  form.name = ''
  form.serialNo = ''
  form.sort = ''
  form.remark = ''
  formRef.value?.clearValidate()
}

/**
 * 修改：取当前选中节点回填
 */
function onEdit() {
  if (!currentNode.value) {
    message.warning('请先选择要修改的类目')
    return
  }
  const raw = currentNode.value.raw
  formMode.value = 'edit'
  form.id = raw.id
  form.parentId = raw.parentId || 0
  form.name = raw.name
  form.serialNo = raw.serialNo || ''
  form.sort = raw.sort || ''
  form.remark = raw.remark || ''
  formRef.value?.clearValidate()
}

/**
 * 保存
 */
async function onSave() {
  try {
    await formRef.value.validate()
  } catch (error) {
    return
  }
  saving.value = true
  try {
    const payload = {
      parentId: form.parentId,
      name: form.name.trim(),
      serialNo: form.serialNo,
      sort: form.sort,
      remark: form.remark,
    }
    if (formMode.value === 'create') {
      await addCategory(payload)
      message.success('新增成功')
    } else {
      await updateCategory(form.id, payload)
      message.success('修改成功')
    }
    formMode.value = 'view'
    await loadTree()
  } finally {
    saving.value = false
  }
}

/**
 * 删除当前选中类目
 */
function onDelete() {
  if (!currentNode.value) {
    message.warning('请先选择要删除的类目')
    return
  }
  const raw = currentNode.value.raw
  Modal.confirm({
    title: '删除确认',
    content: `确定要删除类目「${raw.name}」吗？`,
    okText: '确定',
    cancelText: '取消',
    okType: 'danger',
    async onOk() {
      await deleteCategory(raw.id)
      message.success('删除成功')
      selectedKeys.value = []
      formMode.value = 'view'
      await loadTree()
    },
  })
}

/**
 * 切换上级类目
 */
function onParentChange(value) {
  form.parentId = value
}

/**
 * 树展开/收起
 */
function onExpand(keys) {
  expandedKeys.value = keys
}

// 页面加载时拉一次
loadTree()
</script>

<template>
  <div class="category-page">
    <!-- 左侧：类目树 -->
    <a-card class="tree-card" :bordered="false" title="商品类别">
      <template #extra>
        <a-space>
          <a-button type="primary" size="small" @click="onCreate">新增</a-button>
          <a-button size="small" :disabled="!currentNode" @click="onEdit">修改</a-button>
          <a-button size="small" danger :disabled="!currentNode" @click="onDelete">删除</a-button>
          <a-button size="small" @click="loadTree">刷新</a-button>
        </a-space>
      </template>

      <a-input
          v-model:value="treeSearch"
          placeholder="搜索类目名称"
          allow-clear
          class="tree-search"
      />

      <a-spin :spinning="loading">
        <a-tree
            v-if="treeData.length > 0"
            :tree-data="searchedTreeData"
            :selected-keys="selectedKeys"
            :expanded-keys="expandedKeys"
            block-node
            @select="onTreeSelect"
            @expand="onExpand"
        />
        <a-empty v-else description="暂无类目，请先新增" />
      </a-spin>
    </a-card>

    <!-- 右侧：类目表单 -->
    <a-card class="form-card" :bordered="false" :title="formTitle">
      <a-empty
          v-if="formMode === 'view' && !currentNode"
          description="请先在左侧选择类目"
      />

      <a-form
          v-else
          ref="formRef"
          :model="form"
          :rules="formRules"
          :disabled="formDisabled"
          layout="vertical"
      >
        <a-form-item label="类目名称" name="name">
          <a-input v-model:value="form.name" placeholder="请输入类目名称" allow-clear />
        </a-form-item>

        <a-form-item label="上级类目">
          <a-tree-select
              v-model:value="form.parentId"
              :tree-data="treeData"
              :field-names="{ label: 'title', value: 'key', children: 'children' }"
              placeholder="不选则为顶级类目"
              allow-clear
              show-search
              tree-default-expand-all
              @change="onParentChange"
          />
        </a-form-item>

        <a-form-item label="类目编号">
          <a-input v-model:value="form.serialNo" placeholder="请输入类目编号" allow-clear />
        </a-form-item>

        <a-form-item label="显示顺序">
          <a-input v-model:value="form.sort" placeholder="请输入显示顺序" allow-clear />
        </a-form-item>

        <a-form-item label="备注">
          <a-textarea v-model:value="form.remark" placeholder="请输入备注" :rows="3" />
        </a-form-item>

        <a-form-item v-if="formMode !== 'view'">
          <a-space>
            <a-button type="primary" :loading="saving" @click="onSave">保存</a-button>
            <a-button @click="formMode = 'view'">取消</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>
  </div>
</template>

<style scoped>
.category-page {
  display: flex;
  gap: 16px;
  align-items: flex-start;
}

.tree-card {
  width: 320px;
  flex-shrink: 0;
}

.form-card {
  flex: 1;
  min-width: 0;
}

.tree-search {
  margin-bottom: 12px;
}
</style>
