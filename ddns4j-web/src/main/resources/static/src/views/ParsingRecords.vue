<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { parsingRecordApi, type ParsingRecord } from '@/api/parsingRecord'
import { useToast } from '@/composables/useToast'
import Button from '@/components/ui/Button.vue'
import Card from '@/components/ui/Card.vue'
import Input from '@/components/ui/Input.vue'
import Select from '@/components/ui/Select.vue'
import Dialog from '@/components/ui/Dialog.vue'
import RecordForm from '@/components/RecordForm.vue'

const { success, error } = useToast()

// State
const loading = ref(false)
const records = ref<ParsingRecord[]>([])
const total = ref(0)
const currentPage = ref(1)
const perPage = ref(10)
const showFormDialog = ref(false)
const editingRecord = ref<ParsingRecord | null>(null)
const formMode = ref<'add' | 'edit' | 'copy'>('add')

// Filters
const filters = ref({
  serviceProvider: '',
  recordType: '',
  domain: '',
})

// Options
const serviceProviderOptions = [
  { label: '全部', value: '' },
  { label: '阿里云', value: 1 },
  { label: '腾讯云', value: 2 },
  { label: 'Cloudflare', value: 3 },
  { label: '华为云', value: 4 },
]

const recordTypeOptions = [
  { label: '全部', value: '' },
  { label: 'AAAA (IPv6)', value: 1 },
  { label: 'A (IPv4)', value: 2 },
]

const getServiceProviderName = (code: number) => {
  const map: Record<number, string> = {
    1: '阿里云',
    2: '腾讯云',
    3: 'Cloudflare',
    4: '华为云',
  }
  return map[code] || '未知'
}

const getRecordTypeName = (code: number) => {
  const map: Record<number, string> = {
    1: 'AAAA',
    2: 'A',
  }
  return map[code] || '未知'
}

const getUpdateFrequencyDesc = (code: number) => {
  const map: Record<number, string> = {
    1: '1分钟',
    2: '2分钟',
    5: '5分钟',
    10: '10分钟',
  }
  return map[code] || '未知'
}

// Fetch records
const fetchRecords = async () => {
  loading.value = true
  try {
    const params: any = {
      page: currentPage.value,
      perPage: perPage.value,
    }
    if (filters.value.serviceProvider) {
      params.serviceProvider = Number(filters.value.serviceProvider)
    }
    if (filters.value.recordType) {
      params.recordType = Number(filters.value.recordType)
    }
    if (filters.value.domain) {
      params.domain = filters.value.domain
    }

    const result = await parsingRecordApi.getPage(params)
    records.value = result.items
    total.value = result.total
  } catch (err: any) {
    error(err.message || '获取解析记录失败')
  } finally {
    loading.value = false
  }
}

// Handle page change
const handlePageChange = (page: number) => {
  currentPage.value = page
  fetchRecords()
}

// Open form dialog
const openAddDialog = () => {
  editingRecord.value = null
  formMode.value = 'add'
  showFormDialog.value = true
}

const openEditDialog = (record: ParsingRecord) => {
  editingRecord.value = { ...record }
  formMode.value = 'edit'
  showFormDialog.value = true
}

const openCopyDialog = (record: ParsingRecord) => {
  editingRecord.value = { ...record }
  formMode.value = 'copy'
  showFormDialog.value = true
}

// Handle form submit
const handleFormSubmit = async () => {
  showFormDialog.value = false
  editingRecord.value = null
  await fetchRecords()
}

// Delete record
const handleDelete = async (id: number) => {
  if (!confirm('确定要删除这条解析记录吗？')) {
    return
  }

  try {
    await parsingRecordApi.delete(id)
    success('删除成功')
    await fetchRecords()
  } catch (err: any) {
    error(err.message || '删除失败')
  }
}

onMounted(() => {
  fetchRecords()
})
</script>

<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
      <div>
        <h2 class="text-2xl font-bold text-gray-900">解析记录</h2>
        <p class="text-sm text-gray-500 mt-1">管理您的动态域名解析记录</p>
      </div>
      <Button @click="openAddDialog">
        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="mr-2">
          <line x1="12" y1="5" x2="12" y2="19" />
          <line x1="5" y1="12" x2="19" y2="12" />
        </svg>
        新增记录
      </Button>
    </div>

    <!-- Filters -->
    <Card class="p-4">
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">服务商</label>
          <Select v-model="filters.serviceProvider" :options="serviceProviderOptions" placeholder="选择服务商" @change="fetchRecords" />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">记录类型</label>
          <Select v-model="filters.recordType" :options="recordTypeOptions" placeholder="选择记录类型" @change="fetchRecords" />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">域名</label>
          <Input v-model="filters.domain" placeholder="输入域名搜索" @keyup.enter="fetchRecords" />
        </div>
      </div>
    </Card>

    <!-- Desktop Table View -->
    <Card class="hidden md:block overflow-hidden">
      <div class="overflow-x-auto">
        <table class="w-full">
          <thead class="bg-gray-50 border-b">
            <tr>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">服务商</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">域名</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">记录类型</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">IP地址</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">更新频率</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">更新时间</th>
              <th class="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">操作</th>
            </tr>
          </thead>
          <tbody class="bg-white divide-y divide-gray-200">
            <tr v-if="loading">
              <td colspan="7" class="px-6 py-12 text-center">
                <div class="flex items-center justify-center">
                  <svg class="animate-spin h-8 w-8 text-blue-500" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                    <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                    <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                  </svg>
                  <span class="ml-3 text-gray-500">加载中...</span>
                </div>
              </td>
            </tr>
            <tr v-else-if="records.length === 0">
              <td colspan="7" class="px-6 py-12 text-center text-gray-500">
                <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1" stroke-linecap="round" stroke-linejoin="round" class="mx-auto mb-4 opacity-50">
                  <circle cx="12" cy="12" r="10" />
                  <line x1="12" y1="8" x2="12" y2="12" />
                  <line x1="12" y1="16" x2="12.01" y2="16" />
                </svg>
                <p>暂无解析记录</p>
                <Button variant="link" @click="openAddDialog" class="mt-2">立即添加</Button>
              </td>
            </tr>
            <tr v-for="record in records" :key="record.id" class="hover:bg-gray-50 transition-colors">
              <td class="px-6 py-4 whitespace-nowrap">
                <div class="flex items-center">
                  <div class="w-8 h-8 rounded-full bg-gradient-to-br from-blue-400 to-blue-600 flex items-center justify-center text-white text-xs font-bold mr-3">
                    {{ getServiceProviderName(record.serviceProvider).charAt(0) }}
                  </div>
                  <span class="text-sm font-medium text-gray-900">{{ getServiceProviderName(record.serviceProvider) }}</span>
                </div>
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <span class="text-sm text-gray-900 font-mono">{{ record.domain }}</span>
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <span :class="['inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium', record.recordType === 1 ? 'bg-purple-100 text-purple-800' : 'bg-green-100 text-green-800']">
                  {{ getRecordTypeName(record.recordType) }}
                </span>
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <span class="text-sm text-gray-600 font-mono">{{ record.ip || '-' }}</span>
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <span class="text-sm text-gray-600">{{ getUpdateFrequencyDesc(record.updateFrequency) }}</span>
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                {{ record.updateDate || '-' }}
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                <div class="flex items-center justify-end gap-2">
                  <Button variant="ghost" size="sm" @click="openEditDialog(record)">编辑</Button>
                  <Button variant="ghost" size="sm" @click="openCopyDialog(record)">复制</Button>
                  <Button variant="destructive" size="sm" @click="handleDelete(record.id!)">删除</Button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Pagination -->
      <div v-if="total > 0" class="px-6 py-4 border-t bg-gray-50 flex items-center justify-between">
        <div class="text-sm text-gray-700">
          共 <span class="font-medium">{{ total }}</span> 条记录
        </div>
        <div class="flex items-center gap-2">
          <Button
            variant="outline"
            size="sm"
            :disabled="currentPage === 1"
            @click="handlePageChange(currentPage - 1)"
          >
            上一页
          </Button>
          <span class="text-sm text-gray-700">
            第 {{ currentPage }} / {{ Math.ceil(total / perPage) }} 页
          </span>
          <Button
            variant="outline"
            size="sm"
            :disabled="currentPage >= Math.ceil(total / perPage)"
            @click="handlePageChange(currentPage + 1)"
          >
            下一页
          </Button>
        </div>
      </div>
    </Card>

    <!-- Mobile Card View -->
    <div class="md:hidden space-y-4">
      <div v-if="loading" class="flex items-center justify-center py-12">
        <svg class="animate-spin h-8 w-8 text-blue-500" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
          <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
          <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
        </svg>
      </div>
      <div v-else-if="records.length === 0" class="text-center py-12">
        <p class="text-gray-500">暂无解析记录</p>
        <Button variant="link" @click="openAddDialog" class="mt-2">立即添加</Button>
      </div>
      <Card v-for="record in records" :key="record.id" class="p-4">
        <div class="space-y-3">
          <div class="flex items-center justify-between">
            <div class="flex items-center gap-2">
              <div class="w-8 h-8 rounded-full bg-gradient-to-br from-blue-400 to-blue-600 flex items-center justify-center text-white text-xs font-bold">
                {{ getServiceProviderName(record.serviceProvider).charAt(0) }}
              </div>
              <span class="font-medium text-gray-900">{{ getServiceProviderName(record.serviceProvider) }}</span>
            </div>
            <span :class="['inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium', record.recordType === 1 ? 'bg-purple-100 text-purple-800' : 'bg-green-100 text-green-800']">
              {{ getRecordTypeName(record.recordType) }}
            </span>
          </div>
          <div>
            <p class="text-xs text-gray-500 mb-1">域名</p>
            <p class="text-sm font-mono text-gray-900">{{ record.domain }}</p>
          </div>
          <div>
            <p class="text-xs text-gray-500 mb-1">IP地址</p>
            <p class="text-sm font-mono text-gray-600">{{ record.ip || '-' }}</p>
          </div>
          <div class="flex items-center justify-between text-sm">
            <span class="text-gray-500">更新频率: {{ getUpdateFrequencyDesc(record.updateFrequency) }}</span>
            <span class="text-gray-400 text-xs">{{ record.updateDate || '-' }}</span>
          </div>
          <div class="flex gap-2 pt-2 border-t">
            <Button variant="outline" size="sm" class="flex-1" @click="openEditDialog(record)">编辑</Button>
            <Button variant="outline" size="sm" class="flex-1" @click="openCopyDialog(record)">复制</Button>
            <Button variant="destructive" size="sm" class="flex-1" @click="handleDelete(record.id!)">删除</Button>
          </div>
        </div>
      </Card>

      <!-- Mobile Pagination -->
      <div v-if="total > 0" class="flex items-center justify-between">
        <Button variant="outline" size="sm" :disabled="currentPage === 1" @click="handlePageChange(currentPage - 1)">
          上一页
        </Button>
        <span class="text-sm text-gray-700">
          {{ currentPage }} / {{ Math.ceil(total / perPage) }}
        </span>
        <Button variant="outline" size="sm" :disabled="currentPage >= Math.ceil(total / perPage)" @click="handlePageChange(currentPage + 1)">
          下一页
        </Button>
      </div>
    </div>

    <!-- Form Dialog -->
    <Dialog v-model="showFormDialog" :title="formMode === 'add' ? '新增解析记录' : formMode === 'edit' ? '编辑解析记录' : '复制解析记录'" width="max-w-3xl">
      <RecordForm
        v-if="showFormDialog"
        :mode="formMode"
        :initial-data="editingRecord"
        @submit="handleFormSubmit"
        @cancel="showFormDialog = false"
      />
    </Dialog>
  </div>
</template>
