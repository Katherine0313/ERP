<script setup>
import { ref } from 'vue'
import { message } from 'ant-design-vue'
import {
  downloadImportTemplate,
  importProduct,
} from '@/api/product/product.js'

defineProps({ visible: { type: Boolean, default: false } })
const emit = defineEmits(['update:visible', 'imported'])

const file = ref(null)
const uploading = ref(false)
const result = ref(null)

function beforeUpload(selected) {
    file.value = selected
    result.value = null
    return false
}

function close() {
    emit('update:visible', false)
}

async function onUpload() {
    if (!file.value) {
        message.warning('请先选择 Excel 文件')
        return
    }
    const formData = new FormData()
    formData.append('file', file.value)
    uploading.value = true
    try {
        const res = await importProduct(formData)
        result.value = res.data.data
        message.success(`导入完成：成功 ${result.value.success} 条，失败 ${result.value.fail} 条`)
        emit('imported')
    } finally {
        uploading.value = false
    }
}

async function onDownloadTemplate() {
    const res = await downloadImportTemplate()
    const blob = new Blob([res.data], {
        type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = '商品导入模板.xlsx'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(url)
}
</script>

<template>
  <a-modal
      :visible="visible"
      title="导入商品"
      width="620px"
      :footer="null"
      @cancel="close"
  >
    <a-alert
        type="info"
        show-icon
        message="导入规则"
        description="按「商品编码」匹配：存在则更新，不存在则新增。编码与名称必填；状态可填 启用/草稿/停售/淘汰 或 1/0/2/3；类目、单位按名称匹配，匹配不到则留空。"
        style="margin-bottom: 16px"
    />

    <div class="import-row">
      <a-upload
          :before-upload="beforeUpload"
          :show-upload-list="false"
          accept=".xlsx,.xls"
      >
        <a-button>选择 Excel 文件</a-button>
      </a-upload>
      <span v-if="file" class="file-name">{{ file.name }}</span>
      <a-button type="link" @click="onDownloadTemplate">下载模板</a-button>
    </div>

    <div class="columns-hint">
      列顺序：商品编码* / 商品名称* / 助记码 / 类目名称 / 默认单位 / 状态 / 采购价 / 零售价 / 销售价 / 最低售价 / 成本价 / 库存数量 / 品牌 / 制造商 / 备注
    </div>

    <div class="footer-actions">
      <a-space>
        <a-button @click="close">取消</a-button>
        <a-button type="primary" :loading="uploading" @click="onUpload">开始导入</a-button>
      </a-space>
    </div>

    <a-alert
        v-if="result"
        :type="result.fail > 0 ? 'warning' : 'success'"
        show-icon
        :message="`共 ${result.total} 行，成功 ${result.success} 行，失败 ${result.fail} 行`"
        :description="result.messages.join('；')"
        style="margin-top: 16px"
    />
  </a-modal>
</template>

<style scoped>
.import-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.file-name {
  color: #333;
}

.columns-hint {
  margin-top: 12px;
  padding: 8px 12px;
  background: #fafafa;
  color: #666;
  font-size: 12px;
  line-height: 1.7;
}

.footer-actions {
  margin-top: 16px;
  text-align: right;
}
</style>
