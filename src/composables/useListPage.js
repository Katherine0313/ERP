import { ref, reactive } from 'vue'

/**
 * 列表页通用逻辑：分页 + 查询 + 重置。
 *
 * 用法：
 *   const { records, total, loading, pageNum, pageSize, query, load, handleSearch, handleReset, handlePageChange }
 *     = useListPage((params) => listApi(params), { keyword: '' })
 *
 * @param fetcher 分页查询函数，入参是 { ...query, pageNum, pageSize }
 * @param defaultQuery 查询条件的初始值
 */
export function useListPage(fetcher, defaultQuery = {}) {
    const records = ref([])
    const total = ref(0)
    const loading = ref(false)
    const pageNum = ref(1)
    const pageSize = ref(20)
    const query = reactive({ ...defaultQuery })

    async function load() {
        loading.value = true
        try {
            const res = await fetcher({ ...query, pageNum: pageNum.value, pageSize: pageSize.value })
            const data = res.data.data
            records.value = data.records
            total.value = data.total
        } finally {
            loading.value = false
        }
    }

    /** 查询：回到第一页 */
    function handleSearch() {
        pageNum.value = 1
        load()
    }

    /** 重置：恢复初始查询条件并重查 */
    function handleReset() {
        const keys = Object.keys(defaultQuery)
        for (const key of keys) {
            query[key] = defaultQuery[key]
        }
        pageNum.value = 1
        load()
    }

    /** 翻页 */
    function handlePageChange(page, size) {
        pageNum.value = page
        pageSize.value = size
        load()
    }

    return {
        records,
        total,
        loading,
        pageNum,
        pageSize,
        query,
        load,
        handleSearch,
        handleReset,
        handlePageChange,
    }
}
