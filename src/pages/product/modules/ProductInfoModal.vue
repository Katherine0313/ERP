<script setup>
import { computed, ref, watch } from 'vue'
import { message } from 'ant-design-vue'
import {
  addProduct,
  getProduct,
  listUnit,
  updateProduct,
} from '@/api/product/product.js'
import { listAttributeGroup } from '@/api/product/attributeGroup.js'
import { pageAttributeValue } from '@/api/product/attributeValue.js'
import {
  MAX_SKU_COUNT,
  PARTNER_TYPE,
  PRICE_TYPE,
  PRODUCT_STATUS,
  YES_NO,
} from '@/utils/dict.js'
import { AMOUNT_PRECISION, TAX_RATE_PRECISION } from '@/utils/number.js'

const props = defineProps({
  visible: { type: Boolean, default: false },
  productId: { type: [Number, String], default: null },
  categoryOptions: { type: Array, default: () => [] },
})

const emit = defineEmits(['update:visible', 'saved'])

// ---------------- 下拉数据 ----------------
const unitOptions = ref([])
const groupOptions = ref([])
const groupValues = ref({})

async function loadUnits() {
    const res = await listUnit()
    unitOptions.value = res.data.data
}

async function loadGroups() {
    const res = await listAttributeGroup()
    groupOptions.value = res.data.data
}

async function loadGroupValues() {
    const next = {}
    for (const groupId of form.attributeGroupIds) {
        const res = await pageAttributeValue({ groupId, pageNum: 1, pageSize: 200 })
        next[groupId] = res.data.data.records
    }
    groupValues.value = next
}

function groupNameOf(groupId) {
    for (const group of groupOptions.value) {
        if (group.id === groupId) {
            return group.name
        }
    }
    return ''
}

function unitNameOf(unitId) {
    if (!unitId || unitId === 0) {
        return '基础单位'
    }
    for (const unit of unitOptions.value) {
        if (unit.id === unitId) {
            return unit.name
        }
    }
    return '-'
}

// ---------------- 表单 ----------------
const formRef = ref()
const activeTab = ref('basic')
const form = ref({
    productNo: '',
    name: '',
    categoryId: undefined,
    unitId: undefined,
    mfrs: '',
    brand: '',
    mnemonic: '',
    expiryNum: null,
    weight: null,
    imgName: '',
    remark: '',
    status: '1',
    enableSerialNumber: '0',
    enableBatchNumber: '0',
    attributeGroupIds: [],
    skus: [],
})

const formRules = {
    productNo: [{ required: true, message: '请输入商品编码', trigger: 'blur' }],
    name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
}

// SKU 属性组合的可读文本
function skuLabel(sku, index) {
    const names = (sku.attributes || []).map(item => item.valueName).filter(Boolean)
    return names.length ? names.join(' / ') : `默认规格 ${index + 1}`
}

const skuSelectOptions = computed(() =>
    form.value.skus.map((sku, index) => ({
        value: index,
        label: `${index + 1}. ${skuLabel(sku, index)}`,
    }))
)

// ---------------- 价格与多单位（扁平行，提交时按 SKU 归组） ----------------
const priceRows = ref([])
const unitRows = ref([])

function newPriceRow(skuIndex) {
    return {
        skuIndex,
        unitId: 0,
        priceType: '2',
        partnerType: '0',
        partnerId: 0,
        customerLevel: '',
        price: null,
        taxRate: null,
        includeTax: '0',
        enabled: true,
    }
}

function newUnitRow(skuIndex) {
    return { skuIndex, unitId: null, defaultFlag: '0' }
}

function addPriceRow() {
    priceRows.value.push(newPriceRow(0))
}

function addUnitRow() {
    unitRows.value.push(newUnitRow(0))
}

function removeRow(list, index) {
    list.splice(index, 1)
}

function ratioOf(unitId) {
    for (const unit of unitOptions.value) {
        if (unit.id === unitId) {
            return unit.ratio
        }
    }
    return null
}

// ---------------- SKU 生成 ----------------
function newSku(attributes, index) {
    return {
        skuCode: '',
        barCode: '',
        defaultFlag: index === 0 ? '1' : '0',
        enabled: true,
        attributes: attributes || [],
        prices: [],
        units: [],
    }
}

async function generateSkus() {
    await loadGroupValues()
    let combos = [[]]
    for (const groupId of form.value.attributeGroupIds) {
        const values = groupValues.value[groupId] || []
        if (values.length === 0) {
            message.warning(`属性组「${groupNameOf(groupId)}」下没有可用属性值`)
            return
        }
        const next = []
        for (const combo of combos) {
            for (const value of values) {
                next.push(combo.concat([{
                    groupId,
                    groupName: groupNameOf(groupId),
                    valueId: value.id,
                    valueName: value.value,
                }]))
            }
        }
        if (next.length > MAX_SKU_COUNT) {
            message.warning(`规格组合数 ${next.length} 超过上限 ${MAX_SKU_COUNT}，请减少属性组或属性值`)
            return
        }
        combos = next
    }

    form.value.skus = combos.map((attrs, index) => newSku(attrs, index))
    priceRows.value = form.value.skus.map((sku, index) => newPriceRow(index))
    unitRows.value = []
    activeTab.value = 'spec'
    message.success(`已生成 ${form.value.skus.length} 个规格`)
}

function removeSku(index) {
    form.value.skus.splice(index, 1)
    priceRows.value = priceRows.value.filter(row => row.skuIndex !== index)
    unitRows.value = unitRows.value.filter(row => row.skuIndex !== index)
    // 重新编号
    priceRows.value.forEach(row => {
        if (row.skuIndex > index) row.skuIndex -= 1
    })
    unitRows.value.forEach(row => {
        if (row.skuIndex > index) row.skuIndex -= 1
    })
}

// ---------------- 打开 / 关闭 ----------------
function resetForm() {
    form.value = {
        productNo: '',
        name: '',
        categoryId: undefined,
        unitId: undefined,
        mfrs: '',
        brand: '',
        mnemonic: '',
        expiryNum: null,
        weight: null,
        imgName: '',
        remark: '',
        status: '1',
        enableSerialNumber: '0',
        enableBatchNumber: '0',
        attributeGroupIds: [],
        skus: [newSku([], 0)],
    }
    priceRows.value = [newPriceRow(0)]
    unitRows.value = []
    groupValues.value = {}
    activeTab.value = 'basic'
}

async function loadDetail(id) {
    const res = await getProduct(id)
    const data = res.data.data
    form.value = {
        productNo: data.productNo,
        name: data.name,
        categoryId: data.categoryId,
        unitId: data.unitId,
        mfrs: data.mfrs,
        brand: data.brand,
        mnemonic: data.mnemonic,
        expiryNum: data.expiryNum,
        weight: data.weight,
        imgName: data.imgName,
        remark: data.remark,
        status: data.status,
        enableSerialNumber: data.enableSerialNumber,
        enableBatchNumber: data.enableBatchNumber,
        attributeGroupIds: data.attributeGroupIds || [],
        skus: [],
    }

    const skus = data.skus || []
    const prices = []
    const units = []
    form.value.skus = skus.map((sku, index) => {
        for (const price of sku.prices || []) {
            prices.push({ skuIndex: index, ...price })
        }
        for (const unit of sku.units || []) {
            units.push({ skuIndex: index, ...unit })
        }
        return {
            skuCode: sku.skuCode,
            barCode: sku.barCode,
            defaultFlag: sku.defaultFlag,
            enabled: sku.enabled,
            attributes: sku.attributes || [],
            prices: [],
            units: [],
        }
    })
    if (form.value.skus.length === 0) {
        form.value.skus.push(newSku([], 0))
    }
    priceRows.value = prices
    unitRows.value = units
    await loadGroupValues()
}

watch(() => props.visible, async visible => {
    if (!visible) return
    resetForm()
    await Promise.all([loadUnits(), loadGroups()])
    if (props.productId) {
        await loadDetail(props.productId)
    }
})

function close() {
    emit('update:visible', false)
}

// ---------------- 提交 ----------------
const submitting = ref(false)

async function onSubmit() {
    try {
        await formRef.value.validate()
    } catch (error) {
        activeTab.value = 'basic'
        return
    }
    if (form.value.skus.length === 0) {
        message.warning('请至少保留一个规格')
        activeTab.value = 'spec'
        return
    }

    const skus = form.value.skus.map((sku, index) => ({
        skuCode: (sku.skuCode || '').trim(),
        barCode: (sku.barCode || '').trim(),
        defaultFlag: sku.defaultFlag || '0',
        enabled: sku.enabled,
        attributes: (sku.attributes || []).map(item => ({
            groupId: item.groupId,
            valueId: item.valueId,
        })),
        prices: priceRows.value
            .filter(row => row.skuIndex === index && row.price !== null && row.price !== undefined && row.price !== '')
            .map(row => ({
                unitId: row.unitId || 0,
                priceType: row.priceType,
                partnerType: row.partnerType || '0',
                partnerId: row.partnerId || 0,
                customerLevel: row.customerLevel || '',
                price: row.price,
                taxRate: row.taxRate,
                includeTax: row.includeTax || '0',
                enabled: row.enabled,
            })),
        units: unitRows.value
            .filter(row => row.skuIndex === index && row.unitId)
            .map(row => ({ unitId: row.unitId, defaultFlag: row.defaultFlag || '0' })),
    }))

    const payload = {
        productNo: form.value.productNo.trim(),
        name: form.value.name.trim(),
        categoryId: form.value.categoryId,
        unitId: form.value.unitId,
        mfrs: form.value.mfrs,
        brand: form.value.brand,
        mnemonic: form.value.mnemonic,
        expiryNum: form.value.expiryNum,
        weight: form.value.weight,
        imgName: form.value.imgName,
        remark: form.value.remark,
        status: form.value.status,
        enableSerialNumber: form.value.enableSerialNumber,
        enableBatchNumber: form.value.enableBatchNumber,
        attributeGroupIds: form.value.attributeGroupIds,
        skus,
    }

    submitting.value = true
    try {
        if (props.productId) {
            await updateProduct(props.productId, payload)
            message.success('修改成功')
        } else {
            await addProduct(payload)
            message.success('新增成功')
        }
        close()
        emit('saved')
    } finally {
        submitting.value = false
    }
}
</script>

<template>
  <a-modal
      :visible="visible"
      :title="productId ? '修改商品' : '新增商品'"
      width="1120px"
      :confirm-loading="submitting"
      ok-text="保存"
      cancel-text="取消"
      @ok="onSubmit"
      @cancel="close"
  >
    <a-form ref="formRef" :model="form" :rules="formRules" layout="vertical">
      <a-tabs v-model:activeKey="activeTab">
        <!-- ============ 基本信息 ============ -->
        <a-tab-pane key="basic" tab="基本信息">
          <a-row :gutter="16">
            <a-col :span="8">
              <a-form-item label="商品编码" name="productNo">
                <a-input v-model:value="form.productNo" placeholder="如 P0001" />
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item label="商品名称" name="name">
                <a-input v-model:value="form.name" placeholder="请输入商品名称" />
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item label="助记码">
                <a-input v-model:value="form.mnemonic" placeholder="拼音首字母等" />
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item label="商品类目">
                <a-select
                    v-model:value="form.categoryId"
                    placeholder="请选择类目"
                    allow-clear
                    show-search
                    :options="categoryOptions.map(item => ({ value: item.id, label: item.name }))"
                />
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item label="默认单位">
                <a-select
                    v-model:value="form.unitId"
                    placeholder="请选择单位"
                    allow-clear
                    show-search
                    :options="unitOptions.map(item => ({ value: item.id, label: item.name }))"
                />
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item label="状态">
                <a-select
                    v-model:value="form.status"
                    :options="PRODUCT_STATUS.map(item => ({ value: item.value, label: item.label }))"
                />
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item label="制造商">
                <a-input v-model:value="form.mfrs" placeholder="制造商" />
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item label="品牌">
                <a-input v-model:value="form.brand" placeholder="品牌" />
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item label="保质期(天)">
                <a-input-number
                    v-model:value="form.expiryNum"
                    :min="0"
                    :precision="0"
                    placeholder="启用批次时有效"
                    style="width: 100%"
                />
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item label="重量(kg)">
                <a-input-number
                    v-model:value="form.weight"
                    :min="0"
                    :precision="AMOUNT_PRECISION"
                    style="width: 100%"
                />
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item label="启用序列号">
                <a-select
                    v-model:value="form.enableSerialNumber"
                    :options="YES_NO.map(item => ({ value: item.value, label: item.label }))"
                />
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item label="启用批次">
                <a-select
                    v-model:value="form.enableBatchNumber"
                    :options="YES_NO.map(item => ({ value: item.value, label: item.label }))"
                />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="主图地址">
                <a-input v-model:value="form.imgName" placeholder="图片 URL（暂不提供上传）" />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="备注">
                <a-textarea v-model:value="form.remark" :rows="2" placeholder="备注" />
              </a-form-item>
            </a-col>
          </a-row>
        </a-tab-pane>

        <!-- ============ 属性与规格 ============ -->
        <a-tab-pane key="spec" tab="属性与规格">
          <a-form-item label="启用的属性组">
            <a-select
                v-model:value="form.attributeGroupIds"
                mode="multiple"
                placeholder="请选择属性组（决定规格维度）"
                :options="groupOptions.map(item => ({ value: item.id, label: item.name }))"
            />
          </a-form-item>
          <div class="tab-toolbar">
            <a-space>
              <a-button type="primary" @click="generateSkus">生成规格</a-button>
              <span class="hint">组合数超过 {{ MAX_SKU_COUNT }} 将被拦截</span>
            </a-space>
          </div>
          <a-table
              :columns="[
                { title: '属性组合', key: 'attrs', width: 200 },
                { title: 'SKU编码', key: 'skuCode', width: 160 },
                { title: '条码', key: 'barCode', width: 160 },
                { title: '默认', key: 'default', width: 90, align: 'center' },
                { title: '启用', key: 'enabled', width: 80, align: 'center' },
                { title: '操作', key: 'action', width: 80, align: 'center' },
              ]"
              :data-source="form.skus"
              :pagination="false"
              :row-key="(record, index) => index"
              :scroll="{ y: 240 }"
              size="small"
              bordered
          >
            <template #bodyCell="{ column, record, index }">
              <template v-if="column.key === 'attrs'">
                {{ skuLabel(record, index) }}
              </template>
              <template v-else-if="column.key === 'skuCode'">
                <a-input v-model:value="record.skuCode" placeholder="SKU编码" size="small" />
              </template>
              <template v-else-if="column.key === 'barCode'">
                <a-input v-model:value="record.barCode" placeholder="条码" size="small" />
              </template>
              <template v-else-if="column.key === 'default'">
                <a-select
                    v-model:value="record.defaultFlag"
                    size="small"
                    :options="YES_NO.map(item => ({ value: item.value, label: item.label }))"
                />
              </template>
              <template v-else-if="column.key === 'enabled'">
                <a-switch v-model:checked="record.enabled" size="small" />
              </template>
              <template v-else-if="column.key === 'action'">
                <a-button type="link" size="small" danger @click="removeSku(index)">删除</a-button>
              </template>
            </template>
          </a-table>
        </a-tab-pane>

        <!-- ============ 价格 ============ -->
        <a-tab-pane key="price" tab="价格">
          <div class="tab-toolbar">
            <a-button type="primary" @click="addPriceRow">新增价格</a-button>
          </div>
          <a-table
              :columns="[
                { title: 'SKU', key: 'sku', width: 180 },
                { title: '价格类型', key: 'priceType', width: 130 },
                { title: '计价单位', key: 'unitId', width: 120 },
                { title: '对象', key: 'partnerType', width: 100 },
                { title: '客户等级', key: 'customerLevel', width: 110 },
                { title: '价格', key: 'price', width: 130 },
                { title: '税率(%)', key: 'taxRate', width: 110 },
                { title: '含税', key: 'includeTax', width: 80 },
                { title: '启用', key: 'enabled', width: 70, align: 'center' },
                { title: '操作', key: 'action', width: 70, align: 'center' },
              ]"
              :data-source="priceRows"
              :pagination="false"
              :row-key="(record, index) => index"
              size="small"
              bordered
          >
            <template #bodyCell="{ column, record, index }">
              <template v-if="column.key === 'sku'">
                <a-select v-model:value="record.skuIndex" size="small" :options="skuSelectOptions" />
              </template>
              <template v-else-if="column.key === 'priceType'">
                <a-select v-model:value="record.priceType" size="small"
                          :options="PRICE_TYPE.map(item => ({ value: item.value, label: item.label }))" />
              </template>
              <template v-else-if="column.key === 'unitId'">
                <a-select v-model:value="record.unitId" size="small" show-search
                          :options="[{ value: 0, label: '基础单位' }, ...unitOptions.map(item => ({ value: item.id, label: item.name }))]" />
              </template>
              <template v-else-if="column.key === 'partnerType'">
                <a-select v-model:value="record.partnerType" size="small"
                          :options="PARTNER_TYPE.map(item => ({ value: item.value, label: item.label }))" />
              </template>
              <template v-else-if="column.key === 'customerLevel'">
                <a-input v-model:value="record.customerLevel" size="small" placeholder="如 VIP" />
              </template>
              <template v-else-if="column.key === 'price'">
                <a-input-number v-model:value="record.price" size="small" :min="0"
                                :precision="AMOUNT_PRECISION" style="width: 100%" />
              </template>
              <template v-else-if="column.key === 'taxRate'">
                <a-input-number v-model:value="record.taxRate" size="small" :min="0" :max="100"
                                :precision="TAX_RATE_PRECISION" style="width: 100%" />
              </template>
              <template v-else-if="column.key === 'includeTax'">
                <a-select v-model:value="record.includeTax" size="small"
                          :options="[{ value: '1', label: '含税' }, { value: '0', label: '不含税' }]" />
              </template>
              <template v-else-if="column.key === 'enabled'">
                <a-switch v-model:checked="record.enabled" size="small" />
              </template>
              <template v-else-if="column.key === 'action'">
                <a-button type="link" size="small" danger @click="removeRow(priceRows, index)">删除</a-button>
              </template>
            </template>
          </a-table>
        </a-tab-pane>

        <!-- ============ 多单位 ============ -->
        <a-tab-pane key="unit" tab="多单位">
          <div class="tab-toolbar">
            <a-button type="primary" @click="addUnitRow">新增单位</a-button>
          </div>
          <a-table
              :columns="[
                { title: 'SKU', key: 'sku', width: 200 },
                { title: '单位', key: 'unitId', width: 180 },
                { title: '换算比例', key: 'ratio', width: 120 },
                { title: '默认', key: 'default', width: 90 },
                { title: '操作', key: 'action', width: 80, align: 'center' },
              ]"
              :data-source="unitRows"
              :pagination="false"
              :row-key="(record, index) => index"
              size="small"
              bordered
          >
            <template #bodyCell="{ column, record, index }">
              <template v-if="column.key === 'sku'">
                <a-select v-model:value="record.skuIndex" size="small" :options="skuSelectOptions" />
              </template>
              <template v-else-if="column.key === 'unitId'">
                <a-select v-model:value="record.unitId" size="small" show-search
                          :options="unitOptions.map(item => ({ value: item.id, label: `${item.name}（换算比例 ${item.ratio}）` }))" />
              </template>
              <template v-else-if="column.key === 'ratio'">
                1 {{ unitNameOf(record.unitId) }} = {{ ratioOf(record.unitId) }} 基础单位
              </template>
              <template v-else-if="column.key === 'default'">
                <a-select v-model:value="record.defaultFlag" size="small"
                          :options="YES_NO.map(item => ({ value: item.value, label: item.label }))" />
              </template>
              <template v-else-if="column.key === 'action'">
                <a-button type="link" size="small" danger @click="removeRow(unitRows, index)">删除</a-button>
              </template>
            </template>
          </a-table>
        </a-tab-pane>
      </a-tabs>
    </a-form>
  </a-modal>
</template>

<style scoped>
.tab-toolbar {
  margin-bottom: 12px;
}

.hint {
  color: #999;
  font-size: 12px;
}
</style>
