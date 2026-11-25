<script setup>
import { ref, watch } from 'vue'
import { message } from 'ant-design-vue'
import { batchEditProduct } from '@/api/product/product.js'
import { PRODUCT_STATUS } from '@/utils/dict.js'

const props = defineProps({
  visible: { type: Boolean, default: false },
  ids: { type: Array, default: () => [] },
  categoryOptions: { type: Array, default: () => [] },
})
const emit = defineEmits(['update:visible', 'saved'])

const formRef = ref()
const saving = ref(false)
const form = ref({
    categoryId: undefined,
    brand: '',
    mfrs: '',
    mnemonic: '',
    status: undefined,
    remark: '',
})

watch(() => props.visible, visible => {
    if (visible) {
        form.value = {
            categoryId: undefined,
            brand: '',
            mfrs: '',
            mnemonic: '',
            status: undefined,
            remark: '',
        }
    }
})

function close() {
    emit('update:visible', false)
}

async function onSubmit() {
    const payload = {}
    if (form.value.categoryId !== undefined && form.value.categoryId !== null) {
        payload.categoryId = form.value.categoryId
    }
    if (form.value.status !== undefined && form.value.status !== null) {
        payload.status = form.value.status
    }
    for (const key of ['brand', 'mfrs', 'mnemonic', 'remark']) {
        const value = form.value[key]
        if (value !== '' && value !== null && value !== undefined) {
            payload[key] = value
        }
    }
    if (Object.keys(payload).length === 0) {
        message.warning('请至少填写一个要修改的字段')
        return
    }

    saving.value = true
    try {
        await batchEditProduct(props.ids, payload)
        message.success(`已批量修改 ${props.ids.length} 个商品`)
        close()
        emit('saved')
    } finally {
        saving.value = false
    }
}
</script>

<template>
  <a-modal
      :visible="visible"
      title="批量编辑"
      width="560px"
      :confirm-loading="saving"
      ok-text="保存"
      cancel-text="取消"
      @ok="onSubmit"
      @cancel="close"
  >
    <a-alert
        type="info"
        show-icon
        :message="`将修改选中的 ${ids.length} 个商品；留空的字段保持不变。`"
        style="margin-bottom: 16px"
    />
    <a-form ref="formRef" :model="form" layout="vertical">
      <a-form-item label="商品类目">
        <a-select
            v-model:value="form.categoryId"
            placeholder="不修改则留空"
            allow-clear
            show-search
            :options="categoryOptions.map(item => ({ value: item.id, label: item.name }))"
        />
      </a-form-item>
      <a-form-item label="状态">
        <a-select
            v-model:value="form.status"
            placeholder="不修改则留空"
            allow-clear
            :options="PRODUCT_STATUS.map(item => ({ value: item.value, label: item.label }))"
        />
      </a-form-item>
      <a-form-item label="品牌">
        <a-input v-model:value="form.brand" placeholder="不修改则留空" />
      </a-form-item>
      <a-form-item label="制造商">
        <a-input v-model:value="form.mfrs" placeholder="不修改则留空" />
      </a-form-item>
      <a-form-item label="助记码">
        <a-input v-model:value="form.mnemonic" placeholder="不修改则留空" />
      </a-form-item>
      <a-form-item label="备注">
        <a-textarea v-model:value="form.remark" :rows="2" placeholder="不修改则留空" />
      </a-form-item>
    </a-form>
  </a-modal>
</template>
