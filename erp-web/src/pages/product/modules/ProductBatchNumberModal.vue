<script setup>
import { computed, ref, watch } from 'vue'
import { message } from 'ant-design-vue'
import {
  batchUpdateCost,
  batchUpdateStock,
} from '@/api/product/product.js'
import { AMOUNT_PRECISION, QUANTITY_PRECISION } from '@/utils/number.js'

const props = defineProps({
  visible: { type: Boolean, default: false },
  ids: { type: Array, default: () => [] },
  /** stock=修正库存，cost=修正成本 */
  mode: { type: String, default: 'stock' },
})
const emit = defineEmits(['update:visible', 'saved'])

const value = ref(null)
const saving = ref(false)
const isStock = computed(() => props.mode === 'stock')

watch(() => props.visible, visible => {
    if (visible) {
        value.value = null
    }
})

function close() {
    emit('update:visible', false)
}

async function onSubmit() {
    if (value.value === null || value.value === undefined) {
        message.warning('请输入数值')
        return
    }
    saving.value = true
    try {
        if (isStock.value) {
            await batchUpdateStock(props.ids, value.value)
        } else {
            await batchUpdateCost(props.ids, value.value)
        }
        message.success(`已${isStock.value ? '修正库存' : '修正成本'} ${props.ids.length} 个商品`)
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
      :title="isStock ? '修正库存' : '修正成本'"
      width="440px"
      :confirm-loading="saving"
      ok-text="确定"
      cancel-text="取消"
      @ok="onSubmit"
      @cancel="close"
  >
    <a-alert
        type="info"
        show-icon
        :message="`将把选中的 ${ids.length} 个商品的${isStock ? '库存数量' : '成本价'}统一设为下面的值。`"
        style="margin-bottom: 16px"
    />
    <a-form layout="vertical">
      <a-form-item :label="isStock ? '库存数量' : '成本价'">
        <a-input-number
            v-model:value="value"
            :min="0"
            :precision="isStock ? QUANTITY_PRECISION : AMOUNT_PRECISION"
            placeholder="请输入数值"
            style="width: 100%"
        />
      </a-form-item>
    </a-form>
  </a-modal>
</template>
